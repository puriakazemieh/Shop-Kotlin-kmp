<?php
/**
 * Order endpoints backed by WooCommerce orders, shaped to the app's order DTOs.
 * All routes require a logged-in user; orders are scoped to that customer.
 *
 *   GET  api/orders                 -> List<OrderResponse>
 *   GET  api/orders/{id}            -> OrderDetailResponse
 *   POST api/orders                 -> OrderDetailResponse   (CreateOrderRequest)
 *   POST api/orders/{id}/cancel     -> 200
 *   GET  api/orders/{id}/track      -> OrderTrackingResponse
 *   POST api/orders/{id}/reorder    -> ReorderResponse
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Order_Controller {

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$login = array( 'CB_Plugin', 'require_login' );

		register_rest_route( $ns, '/api/orders', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_orders' ), 'permission_callback' => $login ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'create_order' ), 'permission_callback' => $login ),
		) );
		register_rest_route( $ns, '/api/orders/(?P<id>\d+)', array(
			'methods' => 'GET', 'callback' => array( $this, 'get_order' ), 'permission_callback' => $login,
		) );
		register_rest_route( $ns, '/api/orders/(?P<id>\d+)/cancel', array(
			'methods' => 'POST', 'callback' => array( $this, 'cancel_order' ), 'permission_callback' => $login,
		) );
		register_rest_route( $ns, '/api/orders/(?P<id>\d+)/track', array(
			'methods' => 'GET', 'callback' => array( $this, 'track_order' ), 'permission_callback' => $login,
		) );
		register_rest_route( $ns, '/api/orders/(?P<id>\d+)/reorder', array(
			'methods' => 'POST', 'callback' => array( $this, 'reorder' ), 'permission_callback' => $login,
		) );
	}

	private function guard_woo() {
		if ( ! cb_woo_active() ) {
			return cb_error( 'WooCommerce غیرفعال است', 400, 'WOO_INACTIVE', 'api/orders' );
		}
		return null;
	}

	/** Load an order and ensure it belongs to the current user. */
	private function owned_order( int $id ) {
		$order = wc_get_order( $id );
		if ( ! $order || (int) $order->get_customer_id() !== get_current_user_id() ) {
			return null;
		}
		return $order;
	}

	public function list_orders(): WP_REST_Response {
		if ( $g = $this->guard_woo() ) {
			return $g;
		}
		$orders = wc_get_orders( array(
			'customer_id' => get_current_user_id(),
			'limit'       => 100,
			'orderby'     => 'date',
			'order'       => 'DESC',
		) );
		$out = array();
		foreach ( $orders as $order ) {
			$out[] = $this->order_summary( $order );
		}
		return cb_response( $out );
	}

	public function get_order( WP_REST_Request $request ): WP_REST_Response {
		if ( $g = $this->guard_woo() ) {
			return $g;
		}
		$order = $this->owned_order( (int) $request['id'] );
		if ( ! $order ) {
			return cb_error( 'سفارش یافت نشد', 404, 'NOT_FOUND', 'api/orders' );
		}
		return cb_response( $this->order_detail( $order ) );
	}

	public function create_order( WP_REST_Request $request ): WP_REST_Response {
		if ( $g = $this->guard_woo() ) {
			return $g;
		}
		$body      = $request->get_json_params();
		$items     = isset( $body['items'] ) && is_array( $body['items'] ) ? $body['items'] : array();
		$uid       = get_current_user_id();

		// Fall back to the server cart when no explicit items are supplied.
		if ( empty( $items ) ) {
			foreach ( cb_cart_lines( $uid ) as $line ) {
				if ( empty( $line['saved'] ) ) {
					$items[] = array( 'variantId' => (int) $line['variantId'], 'qty' => (int) $line['qty'] );
				}
			}
		}
		if ( empty( $items ) ) {
			return cb_error( 'سبد خرید خالی است', 400, 'EMPTY_CART', 'api/orders' );
		}

		$order = wc_create_order( array( 'customer_id' => $uid ) );
		if ( is_wp_error( $order ) ) {
			return cb_error( 'ساخت سفارش ناموفق بود', 500, 'ORDER_FAILED', 'api/orders' );
		}

		foreach ( $items as $it ) {
			$variant_id = (int) ( $it['variantId'] ?? 0 );
			$qty        = max( 1, (int) ( $it['qty'] ?? 1 ) );
			$product    = wc_get_product( $variant_id );
			if ( ! $product ) {
				continue;
			}
			$order->add_product( $product, $qty );
		}

		// Address snapshot from a stored address (physical orders).
		$address_id = isset( $body['addressId'] ) ? (int) $body['addressId'] : 0;
		if ( $address_id ) {
			$addr = cb_find_address( $uid, $address_id );
			if ( $addr ) {
				$order->set_address( array(
					'first_name' => $addr['receiverName'] ?? '',
					'phone'      => $addr['receiverPhone'] ?? '',
					'country'    => $addr['country'] ?? 'IR',
					'state'      => $addr['province'] ?? '',
					'city'       => $addr['city'] ?? '',
					'address_1'  => $addr['addressLine1'] ?? '',
					'address_2'  => $addr['addressLine2'] ?? '',
					'postcode'   => $addr['postalCode'] ?? '',
				), 'billing' );
				$order->update_meta_data( '_cb_address_id', $address_id );
			}
		}

		if ( ! empty( $body['isGift'] ) ) {
			$order->update_meta_data( '_cb_is_gift', 1 );
			$order->update_meta_data( '_cb_gift_message', sanitize_text_field( (string) ( $body['giftMessage'] ?? '' ) ) );
		}

		$order->calculate_totals();

		$gateway_amount = (float) $order->get_total();
		$order->update_status( 'pending', 'ساخته‌شده از اپ', true );

		$order->update_meta_data( '_cb_app_status', 'AWAITING_PAYMENT' );
		$order->save();

		// Clear the purchased items from the cart.
		if ( empty( $body['items'] ) ) {
			$remaining = array_filter( cb_cart_lines( $uid ), function ( $l ) {
				return ! empty( $l['saved'] );
			} );
			cb_save_cart_lines( $uid, $remaining );
		}

		return cb_response( $this->order_detail( $order ), 201 );
	}

	public function cancel_order( WP_REST_Request $request ): WP_REST_Response {
		if ( $g = $this->guard_woo() ) {
			return $g;
		}
		$order = $this->owned_order( (int) $request['id'] );
		if ( ! $order ) {
			return cb_error( 'سفارش یافت نشد', 404, 'NOT_FOUND', 'api/orders' );
		}
		if ( in_array( $order->get_status(), array( 'completed', 'cancelled', 'refunded' ), true ) ) {
			return cb_error( 'این سفارش قابل لغو نیست', 409, 'NOT_CANCELLABLE', 'api/orders' );
		}
		$order->update_status( 'cancelled', 'لغو توسط کاربر', true );
		return cb_response( null, 200 );
	}

	public function track_order( WP_REST_Request $request ): WP_REST_Response {
		if ( $g = $this->guard_woo() ) {
			return $g;
		}
		$order = $this->owned_order( (int) $request['id'] );
		if ( ! $order ) {
			return cb_error( 'سفارش یافت نشد', 404, 'NOT_FOUND', 'api/orders' );
		}
		$created  = $order->get_date_created() ? $order->get_date_created()->date( 'c' ) : gmdate( 'c' );
		$paid     = $order->get_date_paid() ? $order->get_date_paid()->date( 'c' ) : null;
		$done     = $order->get_date_completed() ? $order->get_date_completed()->date( 'c' ) : null;
		$shipped  = $order->get_meta( '_cb_shipped_at' ) ?: null;

		$history = array( array( 'status' => 'PLACED', 'at' => $created ) );
		if ( $paid ) {
			$history[] = array( 'status' => 'PROCESSING', 'at' => $paid );
		}
		if ( $shipped ) {
			$history[] = array( 'status' => 'SHIPPED', 'at' => $shipped );
		}
		if ( $done ) {
			$history[] = array( 'status' => 'COMPLETED', 'at' => $done );
		}

		return cb_response( array(
			'id'           => (int) $order->get_id(),
			'status'       => cb_order_status( $order->get_status() ),
			'trackingCode' => $order->get_meta( '_cb_tracking_code' ) ?: null,
			'orderedAt'    => $created,
			'shippedAt'    => $shipped,
			'history'      => $history,
		) );
	}

	public function reorder( WP_REST_Request $request ): WP_REST_Response {
		if ( $g = $this->guard_woo() ) {
			return $g;
		}
		$order = $this->owned_order( (int) $request['id'] );
		if ( ! $order ) {
			return cb_error( 'سفارش یافت نشد', 404, 'NOT_FOUND', 'api/orders' );
		}
		$uid     = get_current_user_id();
		$lines   = cb_cart_lines( $uid );
		$skipped = array();
		foreach ( $order->get_items() as $item ) {
			$variant_id = $item->get_variation_id() ?: $item->get_product_id();
			$product    = wc_get_product( $variant_id );
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
				$lines[] = array( 'id' => cb_cart_next_id( $uid ), 'variantId' => (int) $variant_id, 'qty' => (int) $qty, 'saved' => false );
			}
		}
		cb_save_cart_lines( $uid, $lines );
		return cb_response( array(
			'cart'          => cb_cart_response( $uid ),
			'skippedTitles' => array_values( $skipped ),
		) );
	}

	// ---- DTO shaping --------------------------------------------------------

	private function order_summary( WC_Order $order ): array {
		$count = 0;
		foreach ( $order->get_items() as $item ) {
			$count += (int) $item->get_quantity();
		}
		return array(
			'id'            => (int) $order->get_id(),
			'status'        => cb_order_status( $order->get_status() ),
			'subtotalPrice' => (float) $order->get_subtotal(),
			'shippingPrice' => (float) $order->get_shipping_total(),
			'totalPrice'    => (float) $order->get_total(),
			'itemCount'     => $count,
			'createdAt'     => $order->get_date_created() ? $order->get_date_created()->date( 'c' ) : gmdate( 'c' ),
		);
	}

	private function order_detail( WC_Order $order ): array {
		$items = array();
		foreach ( $order->get_items() as $item ) {
			$variant_id = $item->get_variation_id() ?: $item->get_product_id();
			$qty        = max( 1, (int) $item->get_quantity() );
			$options    = array();
			foreach ( $item->get_meta_data() as $meta ) {
				$data = $meta->get_data();
				if ( ! empty( $data['key'] ) && strpos( $data['key'], '_' ) !== 0 ) {
					$options[ wc_attribute_label( $data['key'] ) ] = is_scalar( $data['value'] ) ? (string) $data['value'] : '';
				}
			}
			$items[] = array(
				'id'        => (int) $item->get_id(),
				'variantId' => (int) $variant_id,
				'qty'       => $qty,
				'unitPrice' => (float) ( $item->get_subtotal() / $qty ),
				'title'     => $item->get_name(),
				'options'   => (object) $options,
			);
		}

		$addr = array(
			'receiverName'  => $order->get_billing_first_name(),
			'receiverPhone' => $order->get_billing_phone(),
			'country'       => $order->get_billing_country() ?: 'IR',
			'province'      => $order->get_billing_state(),
			'city'          => $order->get_billing_city(),
			'addressLine1'  => $order->get_billing_address_1(),
			'addressLine2'  => $order->get_billing_address_2() ?: null,
			'postalCode'    => $order->get_billing_postcode() ?: null,
		);

		return array(
			'id'                => (int) $order->get_id(),
			'status'            => cb_order_status( $order->get_status() ),
			'subtotalPrice'     => (float) $order->get_subtotal(),
			'shippingPrice'     => (float) $order->get_shipping_total(),
			'totalPrice'        => (float) $order->get_total(),
			'createdAt'         => $order->get_date_created() ? $order->get_date_created()->date( 'c' ) : gmdate( 'c' ),
			'address'           => cb_address_snapshot( $addr ),
			'items'             => $items,
			'shippingCarrier'   => $order->get_meta( '_cb_shipping_carrier' ) ?: null,
			'trackingCode'      => $order->get_meta( '_cb_tracking_code' ) ?: null,
			'shippedAt'         => $order->get_meta( '_cb_shipped_at' ) ?: null,
			'deliveredAt'       => $order->get_date_completed() ? $order->get_date_completed()->date( 'c' ) : null,
			'isGift'            => (bool) $order->get_meta( '_cb_is_gift' ),
			'giftMessage'       => $order->get_meta( '_cb_gift_message' ) ?: null,
		);
	}
}
