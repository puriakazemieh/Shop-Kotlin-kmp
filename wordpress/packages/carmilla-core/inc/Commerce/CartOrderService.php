<?php
namespace Carmilla\Core\Commerce;

use Carmilla\Core\Adapter\WooCommerceAdapter;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\CartOrderService', false ) ) {

/**
 * Canonical Shared Cart & Order Service.
 *
 * Centralizes per-user server cart (cb_cart user meta) and HPOS-compatible
 * WooCommerce order CRUD via WooCommerceAdapter so Theme-only, Plugin-only,
 * and Co-install modes produce identical IDs/totals with zero duplicate writes.
 *
 * Part of Task P04-WORDPRESS-CODE-026B.
 */
class CartOrderService {

	/**
	 * Checks if commerce.core / commerce.cart is enabled in the kernel.
	 */
	public static function is_enabled(): bool {
		if ( class_exists( '\Carmilla_Kernel' ) && \Carmilla_Kernel::is_booted() ) {
			return \Carmilla_Kernel::is_feature_active( 'commerce.core' );
		}
		return true;
	}

	/**
	 * @return \WP_Error|null
	 */
	public static function ensure_woo() {
		if ( WooCommerceAdapter::is_available() && function_exists( 'wc_get_product' ) ) {
			return null;
		}
		return new \WP_Error(
			'WOO_INACTIVE',
			'WooCommerce غیرفعال است',
			[ 'status' => 503 ]
		);
	}

	// ---- Cart Storage & DTOs ------------------------------------------------

	/**
	 * @return array<int, array<string, mixed>>
	 */
	public static function get_cart_lines( int $user_id ): array {
		$lines = get_user_meta( $user_id, 'cb_cart', true );
		return is_array( $lines ) ? array_values( $lines ) : [];
	}

	/**
	 * @param array<int, array<string, mixed>> $lines
	 */
	public static function save_cart_lines( int $user_id, array $lines ): void {
		update_user_meta( $user_id, 'cb_cart', array_values( $lines ) );
		update_user_meta( $user_id, 'cb_cart_updated', gmdate( 'c' ) );
	}

	public static function next_line_id( int $user_id ): int {
		$seq = (int) get_user_meta( $user_id, 'cb_cart_seq', true );
		++$seq;
		update_user_meta( $user_id, 'cb_cart_seq', $seq );
		return $seq;
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function get_cart( int $user_id ): array {
		$lines = self::get_cart_lines( $user_id );
		$items = [];
		$saved = [];

		foreach ( $lines as $line ) {
			$dto = self::cart_item_dto( $line );
			if ( ! $dto ) {
				continue;
			}
			if ( ! empty( $line['saved'] ) ) {
				$saved[] = $dto;
			} else {
				$items[] = $dto;
			}
		}

		$subtotal  = 0.0;
		$total_qty = 0;
		foreach ( $items as $it ) {
			$subtotal  += (float) $it['lineTotal'];
			$total_qty += (int) $it['qty'];
		}

		$code     = get_user_meta( $user_id, 'cb_cart_coupon', true ) ?: null;
		$discount = self::coupon_discount( $code ? (string) $code : null, $subtotal, $total_qty );

		return [
			'items'               => $items,
			'savedForLater'       => $saved,
			'subtotal'            => $subtotal,
			'totalQty'            => $total_qty,
			'discountAmount'      => $discount,
			'total'               => max( 0.0, $subtotal - $discount ),
			'appliedDiscountCode' => $code ? (string) $code : null,
			'updatedAt'           => get_user_meta( $user_id, 'cb_cart_updated', true ) ?: gmdate( 'c' ),
		];
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function clear_cart( int $user_id ): array {
		self::save_cart_lines( $user_id, [] );
		delete_user_meta( $user_id, 'cb_cart_coupon' );
		return self::get_cart( $user_id );
	}

	/**
	 * @param array<string, mixed> $body
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function add_item( int $user_id, array $body ) {
		if ( $err = self::ensure_woo() ) {
			return $err;
		}
		$qty        = max( 1, (int) ( $body['qty'] ?? 1 ) );
		$variant_id = (int) ( $body['variantId'] ?? 0 );
		$product_id = (int) ( $body['productId'] ?? 0 );

		if ( ! $variant_id && $product_id ) {
			$p = WooCommerceAdapter::get_product( $product_id );
			if ( $p && $p->is_type( 'variable' ) ) {
				return new \WP_Error( 'VARIANT_REQUIRED', 'برای محصولِ متغیر باید variantId بفرستید', [ 'status' => 400 ] );
			}
			$variant_id = $product_id;
		}

		if ( ! self::variant_parts( $variant_id ) ) {
			return new \WP_Error( 'NOT_FOUND', 'محصول یافت نشد', [ 'status' => 404 ] );
		}

		$lines  = self::get_cart_lines( $user_id );
		$merged = false;
		foreach ( $lines as &$line ) {
			if ( (int) $line['variantId'] === $variant_id && empty( $line['saved'] ) ) {
				$line['qty'] = (int) $line['qty'] + $qty;
				$merged      = true;
				break;
			}
		}
		unset( $line );

		if ( ! $merged ) {
			$lines[] = [
				'id'        => self::next_line_id( $user_id ),
				'variantId' => $variant_id,
				'qty'       => $qty,
				'saved'     => false,
			];
		}

		self::save_cart_lines( $user_id, $lines );
		return self::get_cart( $user_id );
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function update_qty( int $user_id, int $item_id, int $qty ): array {
		$qty   = max( 1, $qty );
		$lines = self::get_cart_lines( $user_id );
		foreach ( $lines as &$line ) {
			if ( (int) $line['id'] === $item_id ) {
				$line['qty'] = $qty;
				break;
			}
		}
		unset( $line );
		self::save_cart_lines( $user_id, $lines );
		return self::get_cart( $user_id );
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function remove_item( int $user_id, int $item_id ): array {
		$lines = array_filter( self::get_cart_lines( $user_id ), static function ( $l ) use ( $item_id ) {
			return (int) $l['id'] !== $item_id;
		} );
		self::save_cart_lines( $user_id, $lines );
		return self::get_cart( $user_id );
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function set_variant_qty( int $user_id, int $variant_id, int $qty ): array {
		$qty   = max( 0, $qty );
		$lines = self::get_cart_lines( $user_id );
		$found = false;

		foreach ( $lines as &$line ) {
			if ( (int) $line['variantId'] === $variant_id && empty( $line['saved'] ) ) {
				$line['qty'] = $qty;
				$found       = true;
				break;
			}
		}
		unset( $line );

		if ( ! $found && $qty > 0 && self::variant_parts( $variant_id ) ) {
			$lines[] = [
				'id'        => self::next_line_id( $user_id ),
				'variantId' => $variant_id,
				'qty'       => $qty,
				'saved'     => false,
			];
		}

		$lines = array_filter( $lines, static function ( $l ) {
			return (int) $l['qty'] > 0;
		} );
		self::save_cart_lines( $user_id, $lines );
		return self::get_cart( $user_id );
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function adjust_variant_qty( int $user_id, int $variant_id, int $delta ): array {
		$lines = self::get_cart_lines( $user_id );
		$found = false;

		foreach ( $lines as &$line ) {
			if ( (int) $line['variantId'] === $variant_id && empty( $line['saved'] ) ) {
				$line['qty'] = (int) $line['qty'] + $delta;
				$found       = true;
				break;
			}
		}
		unset( $line );

		if ( ! $found && $delta > 0 && self::variant_parts( $variant_id ) ) {
			$lines[] = [
				'id'        => self::next_line_id( $user_id ),
				'variantId' => $variant_id,
				'qty'       => $delta,
				'saved'     => false,
			];
		}

		$lines = array_filter( $lines, static function ( $l ) {
			return (int) $l['qty'] > 0;
		} );
		self::save_cart_lines( $user_id, $lines );
		return self::get_cart( $user_id );
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function set_saved_state( int $user_id, int $item_id, bool $saved ): array {
		$lines = self::get_cart_lines( $user_id );
		foreach ( $lines as &$line ) {
			if ( (int) $line['id'] === $item_id ) {
				$line['saved'] = $saved;
				break;
			}
		}
		unset( $line );
		self::save_cart_lines( $user_id, $lines );
		return self::get_cart( $user_id );
	}

	/**
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function apply_discount( int $user_id, string $code ) {
		$code = trim( $code );
		if ( '' === $code ) {
			return new \WP_Error( 'EMPTY_CODE', 'کد تخفیف خالی است', [ 'status' => 400 ] );
		}
		if ( WooCommerceAdapter::is_available() && class_exists( '\WC_Coupon' ) ) {
			$coupon = new \WC_Coupon( $code );
			if ( ! $coupon->get_id() ) {
				return new \WP_Error( 'INVALID_COUPON', 'کد تخفیف نامعتبر است', [ 'status' => 404 ] );
			}
		}
		update_user_meta( $user_id, 'cb_cart_coupon', $code );
		return self::get_cart( $user_id );
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function remove_discount( int $user_id ): array {
		delete_user_meta( $user_id, 'cb_cart_coupon' );
		return self::get_cart( $user_id );
	}

	// ---- Orders (HPOS-compatible via WooCommerceAdapter) --------------------

	/**
	 * @return array<int, array<string, mixed>>|\WP_Error
	 */
	public static function list_orders( int $user_id ) {
		if ( $err = self::ensure_woo() ) {
			return $err;
		}

		$orders = WooCommerceAdapter::get_orders( [
			'customer_id' => $user_id,
			'limit'       => 100,
			'orderby'     => 'date',
			'order'       => 'DESC',
		] );

		$out = [];
		foreach ( $orders as $order ) {
			if ( $order instanceof \WC_Order ) {
				$out[] = self::order_summary( $order );
			}
		}
		return $out;
	}

	/**
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function get_order_detail( int $user_id, int $order_id ) {
		if ( $err = self::ensure_woo() ) {
			return $err;
		}
		$order = self::owned_order( $user_id, $order_id );
		if ( ! $order ) {
			return new \WP_Error( 'NOT_FOUND', 'سفارش یافت نشد', [ 'status' => 404 ] );
		}
		return self::order_detail( $order );
	}

	/**
	 * @param array<string, mixed> $body
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function create_order( int $user_id, array $body ) {
		if ( $err = self::ensure_woo() ) {
			return $err;
		}

		$items = isset( $body['items'] ) && is_array( $body['items'] ) ? $body['items'] : [];
		if ( empty( $items ) ) {
			foreach ( self::get_cart_lines( $user_id ) as $line ) {
				if ( empty( $line['saved'] ) ) {
					$items[] = [
						'variantId' => (int) $line['variantId'],
						'qty'       => (int) $line['qty'],
					];
				}
			}
		}
		if ( empty( $items ) ) {
			return new \WP_Error( 'EMPTY_CART', 'سبد خرید خالی است', [ 'status' => 400 ] );
		}

		$order = wc_create_order( [ 'customer_id' => $user_id ] );
		if ( is_wp_error( $order ) ) {
			return new \WP_Error( 'ORDER_FAILED', 'ساخت سفارش ناموفق بود', [ 'status' => 500 ] );
		}

		foreach ( $items as $it ) {
			$variant_id = (int) ( $it['variantId'] ?? 0 );
			$qty        = max( 1, (int) ( $it['qty'] ?? 1 ) );
			$product    = WooCommerceAdapter::get_product( $variant_id );
			if ( ! $product ) {
				continue;
			}
			$order->add_product( $product, $qty );
		}

		$address_id = isset( $body['addressId'] ) ? (int) $body['addressId'] : 0;
		if ( $address_id && function_exists( 'cb_find_address' ) ) {
			$addr = cb_find_address( $user_id, $address_id );
			if ( $addr ) {
				$order->set_address( [
					'first_name' => $addr['receiverName'] ?? '',
					'phone'      => $addr['receiverPhone'] ?? '',
					'country'    => $addr['country'] ?? 'IR',
					'state'      => $addr['province'] ?? '',
					'city'       => $addr['city'] ?? '',
					'address_1'  => $addr['addressLine1'] ?? '',
					'address_2'  => $addr['addressLine2'] ?? '',
					'postcode'   => $addr['postalCode'] ?? '',
				], 'billing' );
				$order->update_meta_data( '_cb_address_id', $address_id );
			}
		}

		if ( ! empty( $body['isGift'] ) ) {
			$order->update_meta_data( '_cb_is_gift', 1 );
			$order->update_meta_data( '_cb_gift_message', sanitize_text_field( (string) ( $body['giftMessage'] ?? '' ) ) );
		}

		$order->calculate_totals();
		$order->update_status( 'pending', 'ساخته‌شده از اپ', true );
		$order->update_meta_data( '_cb_app_status', 'AWAITING_PAYMENT' );
		$order->save();

		if ( empty( $body['items'] ) ) {
			$remaining = array_filter( self::get_cart_lines( $user_id ), static function ( $l ) {
				return ! empty( $l['saved'] );
			} );
			self::save_cart_lines( $user_id, $remaining );
		}

		return self::order_detail( $order );
	}

	/**
	 * @return true|\WP_Error
	 */
	public static function cancel_order( int $user_id, int $order_id ) {
		if ( $err = self::ensure_woo() ) {
			return $err;
		}
		$order = self::owned_order( $user_id, $order_id );
		if ( ! $order ) {
			return new \WP_Error( 'NOT_FOUND', 'سفارش یافت نشد', [ 'status' => 404 ] );
		}
		if ( in_array( $order->get_status(), [ 'completed', 'cancelled', 'refunded' ], true ) ) {
			return new \WP_Error( 'NOT_CANCELLABLE', 'این سفارش قابل لغو نیست', [ 'status' => 409 ] );
		}
		$order->update_status( 'cancelled', 'لغو توسط کاربر', true );
		return true;
	}

	/**
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function track_order( int $user_id, int $order_id ) {
		if ( $err = self::ensure_woo() ) {
			return $err;
		}
		$order = self::owned_order( $user_id, $order_id );
		if ( ! $order ) {
			return new \WP_Error( 'NOT_FOUND', 'سفارش یافت نشد', [ 'status' => 404 ] );
		}

		$created = $order->get_date_created() ? gmdate( 'c', $order->get_date_created()->getTimestamp() ) : gmdate( 'c' );
		$paid    = $order->get_date_paid() ? gmdate( 'c', $order->get_date_paid()->getTimestamp() ) : null;
		$done    = $order->get_date_completed() ? gmdate( 'c', $order->get_date_completed()->getTimestamp() ) : null;
		$shipped = $order->get_meta( '_cb_shipped_at' ) ?: null;

		$history = [ [ 'status' => 'PLACED', 'at' => $created ] ];
		if ( $paid ) {
			$history[] = [ 'status' => 'PROCESSING', 'at' => $paid ];
		}
		if ( $shipped ) {
			$history[] = [ 'status' => 'SHIPPED', 'at' => $shipped ];
		}
		if ( $done ) {
			$history[] = [ 'status' => 'COMPLETED', 'at' => $done ];
		}

		return [
			'id'           => (int) $order->get_id(),
			'status'       => self::map_order_status( $order->get_status() ),
			'trackingCode' => $order->get_meta( '_cb_tracking_code' ) ?: null,
			'orderedAt'    => $created,
			'shippedAt'    => $shipped,
			'history'      => $history,
		];
	}

	/**
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function reorder( int $user_id, int $order_id ) {
		if ( $err = self::ensure_woo() ) {
			return $err;
		}
		$order = self::owned_order( $user_id, $order_id );
		if ( ! $order ) {
			return new \WP_Error( 'NOT_FOUND', 'سفارش یافت نشد', [ 'status' => 404 ] );
		}

		$lines   = self::get_cart_lines( $user_id );
		$skipped = [];
		foreach ( $order->get_items() as $item ) {
			$variant_id = $item->get_variation_id() ?: $item->get_product_id();
			$product    = WooCommerceAdapter::get_product( $variant_id );
			if ( ! $product || ! $product->is_in_stock() ) {
				$skipped[] = $item->get_name();
				continue;
			}
			$qty    = $item->get_quantity();
			$merged = false;
			foreach ( $lines as &$line ) {
				if ( (int) $line['variantId'] === (int) $variant_id && empty( $line['saved'] ) ) {
					$line['qty'] = (int) $line['qty'] + $qty;
					$merged      = true;
					break;
				}
			}
			unset( $line );
			if ( ! $merged ) {
				$lines[] = [
					'id'        => self::next_line_id( $user_id ),
					'variantId' => (int) $variant_id,
					'qty'       => (int) $qty,
					'saved'     => false,
				];
			}
		}

		self::save_cart_lines( $user_id, $lines );
		return [
			'cart'          => self::get_cart( $user_id ),
			'skippedTitles' => array_values( $skipped ),
		];
	}

	// ---- Helpers ------------------------------------------------------------

	private static function owned_order( int $user_id, int $order_id ): ?\WC_Order {
		$order = WooCommerceAdapter::get_order( $order_id );
		if ( ! $order || (int) $order->get_customer_id() !== $user_id ) {
			return null;
		}
		return $order;
	}

	/**
	 * @return array{0:\WC_Product,1:\WC_Product|null,2:int}|null
	 */
	private static function variant_parts( int $variant_id ): ?array {
		$p = WooCommerceAdapter::get_product( $variant_id );
		if ( ! $p ) {
			return null;
		}
		$is_variation = $p->is_type( 'variation' );
		$product_id   = $is_variation ? (int) $p->get_parent_id() : (int) $p->get_id();
		$owner        = $is_variation ? WooCommerceAdapter::get_product( $product_id ) : $p;
		return [ $p, $owner, $product_id ];
	}

	/**
	 * @param array<string, mixed> $line
	 * @return array<string, mixed>|null
	 */
	private static function cart_item_dto( array $line ): ?array {
		if ( ! WooCommerceAdapter::is_available() ) {
			return null;
		}
		$variant_id = (int) ( $line['variantId'] ?? 0 );
		$parts      = self::variant_parts( $variant_id );
		if ( ! $parts ) {
			return null;
		}

		[ $variant, $owner, $product_id ] = $parts;
		$qty     = max( 1, (int) ( $line['qty'] ?? 1 ) );
		$sale    = $variant->get_sale_price();
		$regular = (float) $variant->get_regular_price();
		$price   = ( '' !== $sale && null !== $sale ) ? (float) $sale : (float) $variant->get_price();

		$img_id = $variant->get_image_id();
		if ( ! $img_id && $owner ) {
			$img_id = $owner->get_image_id();
		}

		$options = [];
		if ( $variant->is_type( 'variation' ) ) {
			foreach ( $variant->get_variation_attributes() as $name => $value ) {
				$label             = wc_attribute_label( str_replace( 'attribute_', '', $name ) );
				$options[ $label ] = $value;
			}
		}

		$avail = $variant->get_manage_stock() ? max( 0, (int) $variant->get_stock_quantity() ) : ( $variant->is_in_stock() ? 999 : 0 );

		return [
			'id'             => (int) ( $line['id'] ?? 0 ),
			'variantId'      => $variant_id,
			'qty'            => $qty,
			'productId'      => $product_id,
			'productTitle'   => $owner ? $owner->get_name() : $variant->get_name(),
			'productSlug'    => $owner ? $owner->get_slug() : $variant->get_slug(),
			'imageUrl'       => $img_id ? wp_get_attachment_url( $img_id ) : null,
			'options'        => (object) $options,
			'price'          => $price,
			'compareAtPrice' => ( $regular > $price ) ? $regular : null,
			'availableQty'   => $avail,
			'savedForLater'  => (bool) ( $line['saved'] ?? false ),
			'active'         => ( $owner ? 'publish' === $owner->get_status() : true ) && $variant->is_in_stock(),
			'lineTotal'      => $price * $qty,
		];
	}

	private static function coupon_discount( ?string $code, float $subtotal, int $qty ): float {
		if ( ! $code || ! WooCommerceAdapter::is_available() || ! class_exists( '\WC_Coupon' ) ) {
			return 0.0;
		}
		$coupon = new \WC_Coupon( $code );
		if ( ! $coupon->get_id() ) {
			return 0.0;
		}
		$amount = (float) $coupon->get_amount();
		switch ( $coupon->get_discount_type() ) {
			case 'percent':
				return round( $subtotal * $amount / 100, 2 );
			case 'fixed_product':
				return min( $subtotal, $amount * max( 1, $qty ) );
			case 'fixed_cart':
			default:
				return min( $subtotal, $amount );
		}
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function order_summary( \WC_Order $order ): array {
		$count = 0;
		foreach ( $order->get_items() as $item ) {
			$count += (int) $item->get_quantity();
		}
		$created = $order->get_date_created();
		return [
			'id'            => (int) $order->get_id(),
			'status'        => self::map_order_status( $order->get_status() ),
			'subtotalPrice' => (float) $order->get_subtotal(),
			'shippingPrice' => (float) $order->get_shipping_total(),
			'totalPrice'    => (float) $order->get_total(),
			'itemCount'     => $count,
			'createdAt'     => $created ? gmdate( 'c', $created->getTimestamp() ) : gmdate( 'c' ),
		];
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function order_detail( \WC_Order $order ): array {
		$items = [];
		foreach ( $order->get_items() as $item ) {
			$variant_id = $item->get_variation_id() ?: $item->get_product_id();
			$qty        = max( 1, (int) $item->get_quantity() );
			$options    = [];
			foreach ( $item->get_meta_data() as $meta ) {
				$data = $meta->get_data();
				if ( ! empty( $data['key'] ) && 0 !== strpos( (string) $data['key'], '_' ) ) {
					$options[ wc_attribute_label( (string) $data['key'] ) ] = is_scalar( $data['value'] ) ? (string) $data['value'] : '';
				}
			}
			$items[] = [
				'id'        => (int) $item->get_id(),
				'variantId' => (int) $variant_id,
				'qty'       => $qty,
				'unitPrice' => (float) ( $item->get_subtotal() / $qty ),
				'title'     => $item->get_name(),
				'options'   => (object) $options,
			];
		}

		$addr = [
			'receiverName'  => $order->get_billing_first_name(),
			'receiverPhone' => $order->get_billing_phone(),
			'country'       => $order->get_billing_country() ?: 'IR',
			'province'      => $order->get_billing_state(),
			'city'          => $order->get_billing_city(),
			'addressLine1'  => $order->get_billing_address_1(),
			'addressLine2'  => $order->get_billing_address_2() ?: null,
			'postalCode'    => $order->get_billing_postcode() ?: null,
		];

		$created = $order->get_date_created();
		$done    = $order->get_date_completed();

		return [
			'id'              => (int) $order->get_id(),
			'status'          => self::map_order_status( $order->get_status() ),
			'subtotalPrice'   => (float) $order->get_subtotal(),
			'shippingPrice'   => (float) $order->get_shipping_total(),
			'totalPrice'      => (float) $order->get_total(),
			'createdAt'       => $created ? gmdate( 'c', $created->getTimestamp() ) : gmdate( 'c' ),
			'address'         => $addr,
			'items'           => $items,
			'shippingCarrier' => $order->get_meta( '_cb_shipping_carrier' ) ?: null,
			'trackingCode'    => $order->get_meta( '_cb_tracking_code' ) ?: null,
			'shippedAt'       => $order->get_meta( '_cb_shipped_at' ) ?: null,
			'deliveredAt'     => $done ? gmdate( 'c', $done->getTimestamp() ) : null,
			'isGift'          => (bool) $order->get_meta( '_cb_is_gift' ),
			'giftMessage'     => $order->get_meta( '_cb_gift_message' ) ?: null,
		];
	}

	private static function map_order_status( string $wc_status ): string {
		$map = [
			'pending'    => 'AWAITING_PAYMENT',
			'on-hold'    => 'AWAITING_PAYMENT',
			'processing' => 'PROCESSING',
			'completed'  => 'COMPLETED',
			'cancelled'  => 'CANCELLED',
			'refunded'   => 'REFUNDED',
			'failed'     => 'CANCELLED',
		];
		return $map[ $wc_status ] ?? 'AWAITING_PAYMENT';
	}
}

}
