<?php
/**
 * Shared helpers for shaping REST responses to match the Carmilla app DTO contract.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/**
 * Return a WP_REST_Response with the given payload and status.
 */
function cb_response( $data, int $status = 200 ) {
	return new WP_REST_Response( $data, $status );
}

/**
 * Return an error payload shaped like the server's ApiError
 * ({ message, status, code, errorCode, path, timestamp }) so the app's
 * ApiException decoder keeps working unchanged.
 */
function cb_error( string $message, int $status = 400, string $error_code = 'ERROR', string $path = '' ) {
	return new WP_REST_Response(
		array(
			'message'   => $message,
			'status'    => $status,
			'code'      => $status,
			'errorCode' => $error_code,
			'path'      => $path,
			'timestamp' => gmdate( 'c' ),
		),
		$status
	);
}

/**
 * Cast a WooCommerce price string to a nullable float (null when empty).
 */
function cb_price( $value ): ?float {
	if ( $value === '' || $value === null ) {
		return null;
	}
	return (float) $value;
}

/**
 * ISO-8601 timestamp from a WP date string / post.
 */
function cb_iso( $date ): string {
	if ( empty( $date ) ) {
		return gmdate( 'c' );
	}
	$ts = is_numeric( $date ) ? (int) $date : strtotime( $date );
	return gmdate( 'c', $ts ?: time() );
}

/**
 * Standard Spring-style Page<T> wrapper: { content, page:{size,number,totalElements,totalPages} }.
 */
function cb_page( array $content, int $page, int $size, int $total_elements, int $total_pages ): array {
	return array(
		'content' => $content,
		'page'    => array(
			'size'          => $size,
			'number'        => $page,
			'totalElements' => $total_elements,
			'totalPages'    => $total_pages,
		),
		// Backward-compat root fields (BlogListResponse reads these too).
		'totalPages'    => $total_pages,
		'totalElements' => $total_elements,
	);
}

/**
 * Whether WooCommerce is active.
 */
function cb_woo_active(): bool {
	return class_exists( 'WooCommerce' );
}

/**
 * Current effective unit price for a product or variation (sale price if set).
 */
function cb_effective_price( WC_Product $p ): float {
	$sale = $p->get_sale_price();
	if ( $sale !== '' && $sale !== null ) {
		return (float) $sale;
	}
	$regular = $p->get_regular_price();
	if ( $regular !== '' && $regular !== null ) {
		return (float) $regular;
	}
	return (float) $p->get_price();
}

/**
 * Available quantity for a product/variation (unmanaged stock => 999 when in stock).
 */
function cb_available_qty( WC_Product $p ): int {
	if ( $p->get_manage_stock() ) {
		return max( 0, (int) $p->get_stock_quantity() );
	}
	return $p->is_in_stock() ? 999 : 0;
}

/**
 * The sellable owner product for a variantId. A variantId is either a simple
 * product id or a variation post id (see catalog controller's variants()).
 * Returns [ WC_Product $variant, WC_Product|null $owner, int $product_id ] or null.
 */
function cb_variant_parts( int $variant_id ) {
	$p = wc_get_product( $variant_id );
	if ( ! $p ) {
		return null;
	}
	$is_variation = $p->is_type( 'variation' );
	$product_id   = $is_variation ? (int) $p->get_parent_id() : (int) $p->get_id();
	$owner        = $is_variation ? wc_get_product( $product_id ) : $p;
	return array( $p, $owner, $product_id );
}

/**
 * Variation attribute label => value map for a variation product.
 */
function cb_variation_options( WC_Product $p ): array {
	$options = array();
	if ( $p->is_type( 'variation' ) ) {
		foreach ( $p->get_variation_attributes() as $name => $value ) {
			$label             = wc_attribute_label( str_replace( 'attribute_', '', $name ) );
			$options[ $label ] = $value;
		}
	}
	return $options;
}

/**
 * Shape one stored cart line into a CartItemResponse array. Returns null when
 * the underlying product no longer exists.
 *
 * @param array $line ['id'=>int,'variantId'=>int,'qty'=>int,'saved'=>bool]
 */
function cb_cart_item_dto( array $line ): ?array {
	$variant_id = (int) ( $line['variantId'] ?? 0 );
	$parts      = cb_variant_parts( $variant_id );
	if ( ! $parts ) {
		return null;
	}
	list( $variant, $owner, $product_id ) = $parts;
	$qty     = max( 1, (int) ( $line['qty'] ?? 1 ) );
	$price   = cb_effective_price( $variant );
	$regular = (float) $variant->get_regular_price();
	$img_id  = $variant->get_image_id();
	if ( ! $img_id && $owner ) {
		$img_id = $owner->get_image_id();
	}

	return array(
		'id'             => (int) ( $line['id'] ?? 0 ),
		'variantId'      => $variant_id,
		'qty'            => $qty,
		'productId'      => $product_id,
		'productTitle'   => $owner ? $owner->get_name() : $variant->get_name(),
		'productSlug'    => $owner ? $owner->get_slug() : $variant->get_slug(),
		'imageUrl'       => $img_id ? wp_get_attachment_url( $img_id ) : null,
		// Cast to object so an empty map serializes as {} (not []) for kotlinx Map.
		'options'        => (object) cb_variation_options( $variant ),
		'price'          => $price,
		'compareAtPrice' => ( $regular > $price ) ? $regular : null,
		'availableQty'   => cb_available_qty( $variant ),
		'savedForLater'  => (bool) ( $line['saved'] ?? false ),
		'active'         => ( $owner ? $owner->get_status() === 'publish' : true ) && $variant->is_in_stock(),
		'lineTotal'      => $price * $qty,
	);
}

/**
 * Read the current user's stored cart lines (user meta cb_cart).
 */
function cb_cart_lines( int $user_id ): array {
	$lines = get_user_meta( $user_id, 'cb_cart', true );
	return is_array( $lines ) ? array_values( $lines ) : array();
}

/**
 * Persist cart lines for a user.
 */
function cb_save_cart_lines( int $user_id, array $lines ): void {
	update_user_meta( $user_id, 'cb_cart', array_values( $lines ) );
	update_user_meta( $user_id, 'cb_cart_updated', gmdate( 'c' ) );
}

/**
 * Next monotonic cart line id for a user.
 */
function cb_cart_next_id( int $user_id ): int {
	$seq = (int) get_user_meta( $user_id, 'cb_cart_seq', true );
	$seq++;
	update_user_meta( $user_id, 'cb_cart_seq', $seq );
	return $seq;
}

/**
 * Compute a coupon's discount amount against a subtotal (best-effort, headless).
 */
function cb_coupon_discount( ?string $code, float $subtotal, int $qty ): float {
	if ( ! $code || ! cb_woo_active() || ! class_exists( 'WC_Coupon' ) ) {
		return 0.0;
	}
	$coupon = new WC_Coupon( $code );
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
 * Build a full CartResponse array for a user from stored lines + coupon.
 */
function cb_cart_response( int $user_id ): array {
	$lines  = cb_cart_lines( $user_id );
	$items  = array();
	$saved  = array();
	foreach ( $lines as $line ) {
		$dto = cb_cart_item_dto( $line );
		if ( ! $dto ) {
			continue;
		}
		if ( ! empty( $line['saved'] ) ) {
			$saved[] = $dto;
		} else {
			$items[] = $dto;
		}
	}

	$subtotal = 0.0;
	$total_qty = 0;
	foreach ( $items as $it ) {
		$subtotal  += (float) $it['lineTotal'];
		$total_qty += (int) $it['qty'];
	}

	$code     = get_user_meta( $user_id, 'cb_cart_coupon', true ) ?: null;
	$discount = cb_coupon_discount( $code ? (string) $code : null, $subtotal, $total_qty );

	return array(
		'items'               => $items,
		'savedForLater'       => $saved,
		'subtotal'            => $subtotal,
		'totalQty'            => $total_qty,
		'discountAmount'      => $discount,
		'total'              => max( 0.0, $subtotal - $discount ),
		'appliedDiscountCode' => $code ? (string) $code : null,
		'updatedAt'           => get_user_meta( $user_id, 'cb_cart_updated', true ) ?: gmdate( 'c' ),
	);
}

/**
 * Map a WooCommerce order status to the app's status vocabulary.
 */
function cb_order_status( string $wc_status ): string {
	$wc_status = ltrim( $wc_status, 'w' ); // 'wc-processing' safety
	$wc_status = str_replace( 'wc-', '', $wc_status );
	switch ( $wc_status ) {
		case 'pending':
		case 'failed':
			return 'AWAITING_PAYMENT';
		case 'on-hold':
			return 'PLACED';
		case 'processing':
			return 'PROCESSING';
		case 'completed':
			return 'COMPLETED';
		case 'cancelled':
		case 'refunded':
			return 'CANCELLED';
		default:
			return strtoupper( str_replace( '-', '_', $wc_status ) );
	}
}

/**
 * Effective app-facing order status: an admin-set _cb_app_status meta (e.g.
 * SHIPPED, which WooCommerce has no native status for) wins over the mapping
 * from the WooCommerce status.
 */
function cb_effective_order_status( WC_Order $order ): string {
	$app = $order->get_meta( '_cb_app_status' );
	return $app ?: cb_order_status( $order->get_status() );
}

/**
 * Build the line-based cb_quiz meta from an admin quiz payload
 * (questions: [{text, options:[{text, correct}]}]). The correct option gets a
 * trailing "*". Inverse of CB_Academy_Controller::parse_quiz_lines.
 */
function cb_build_quiz_lines( array $questions ): string {
	$lines = array();
	foreach ( $questions as $q ) {
		$parts = array( trim( (string) ( $q['text'] ?? '' ) ) );
		foreach ( (array) ( $q['options'] ?? array() ) as $o ) {
			$text = trim( (string) ( $o['text'] ?? '' ) );
			$parts[] = ! empty( $o['correct'] ) ? $text . '*' : $text;
		}
		$lines[] = implode( ' | ', $parts );
	}
	return implode( "\n", $lines );
}

/**
 * Build line-based cb_questions meta for a psych test from an admin payload
 * (questions: [{text, options:[{text, score}]}]) -> «text | label=score , ...».
 */
function cb_build_test_question_lines( array $questions ): string {
	$lines = array();
	foreach ( $questions as $q ) {
		$opts = array();
		foreach ( (array) ( $q['options'] ?? array() ) as $o ) {
			$opts[] = trim( (string) ( $o['text'] ?? '' ) ) . '=' . (int) ( $o['score'] ?? 0 );
		}
		$lines[] = trim( (string) ( $q['text'] ?? '' ) ) . ' | ' . implode( ' , ', $opts );
	}
	return implode( "\n", $lines );
}

/** Build line-based cb_ranges meta -> «min | max | interpretation». */
function cb_build_range_lines( array $ranges ): string {
	$lines = array();
	foreach ( $ranges as $r ) {
		$lines[] = (int) ( $r['minScore'] ?? 0 ) . ' | ' . (int) ( $r['maxScore'] ?? 0 ) . ' | ' . trim( (string) ( $r['interpretation'] ?? '' ) );
	}
	return implode( "\n", $lines );
}

/** Map an app order-status string to the closest WooCommerce status. */
function cb_app_status_to_wc( string $status ): string {
	switch ( strtoupper( $status ) ) {
		case 'COMPLETED':
		case 'DELIVERED':
			return 'completed';
		case 'CANCELLED':
		case 'CANCELED':
			return 'cancelled';
		case 'PROCESSING':
		case 'SHIPPED':
			return 'processing';
		case 'PLACED':
			return 'on-hold';
		case 'AWAITING_PAYMENT':
		case 'PENDING':
		default:
			return 'pending';
	}
}

// ---- wallet (user meta cb_wallet_balance + cb_wallet_txns) -------------------

function cb_wallet_balance( int $user_id ): float {
	return (float) get_user_meta( $user_id, 'cb_wallet_balance', true );
}

/**
 * Append a wallet transaction and update the balance atomically-ish.
 * $amount is signed (positive = credit, negative = debit). Returns new balance.
 */
function cb_wallet_add( int $user_id, float $amount, string $type, ?string $description = null, ?string $reference = null ): float {
	$balance = cb_wallet_balance( $user_id ) + $amount;
	update_user_meta( $user_id, 'cb_wallet_balance', $balance );
	$txns = get_user_meta( $user_id, 'cb_wallet_txns', true );
	$txns = is_array( $txns ) ? $txns : array();
	$seq  = (int) get_user_meta( $user_id, 'cb_wallet_seq', true ) + 1;
	update_user_meta( $user_id, 'cb_wallet_seq', $seq );
	array_unshift( $txns, array(
		'id'          => $seq,
		'amount'      => $amount,
		'type'        => $type,
		'description' => $description,
		'referenceId' => $reference,
		'createdAt'   => gmdate( 'c' ),
	) );
	update_user_meta( $user_id, 'cb_wallet_txns', $txns );
	return $balance;
}

function cb_wallet_txns( int $user_id ): array {
	$txns = get_user_meta( $user_id, 'cb_wallet_txns', true );
	return is_array( $txns ) ? array_values( $txns ) : array();
}

// ---- addresses (user meta cb_addresses) -------------------------------------

function cb_addresses( int $user_id ): array {
	$list = get_user_meta( $user_id, 'cb_addresses', true );
	return is_array( $list ) ? array_values( $list ) : array();
}

function cb_save_addresses( int $user_id, array $list ): void {
	update_user_meta( $user_id, 'cb_addresses', array_values( $list ) );
}

function cb_find_address( int $user_id, int $id ): ?array {
	foreach ( cb_addresses( $user_id ) as $a ) {
		if ( (int) $a['id'] === $id ) {
			return $a;
		}
	}
	return null;
}

/**
 * Shape a stored address array into an AddressResponse.
 */
function cb_address_dto( array $a ): array {
	return array(
		'id'            => (int) ( $a['id'] ?? 0 ),
		'receiverName'  => (string) ( $a['receiverName'] ?? '' ),
		'receiverPhone' => (string) ( $a['receiverPhone'] ?? '' ),
		'country'       => (string) ( $a['country'] ?? 'IR' ),
		'province'      => (string) ( $a['province'] ?? '' ),
		'city'          => (string) ( $a['city'] ?? '' ),
		'addressLine1'  => (string) ( $a['addressLine1'] ?? '' ),
		'addressLine2'  => $a['addressLine2'] ?? null,
		'postalCode'    => $a['postalCode'] ?? null,
		'default'       => (bool) ( $a['default'] ?? false ),
		'createdAt'     => $a['createdAt'] ?? null,
	);
}

/**
 * Shape a stored address into the order's AddressSnapshotResponse (all fields
 * present; snapshot is embedded in the order and never null on the app side).
 */
function cb_address_snapshot( ?array $a ): array {
	$a = is_array( $a ) ? $a : array();
	return array(
		'receiverName'  => (string) ( $a['receiverName'] ?? '' ),
		'receiverPhone' => (string) ( $a['receiverPhone'] ?? '' ),
		'country'       => (string) ( $a['country'] ?? 'IR' ),
		'province'      => (string) ( $a['province'] ?? '' ),
		'city'          => (string) ( $a['city'] ?? '' ),
		'addressLine1'  => (string) ( $a['addressLine1'] ?? '' ),
		'addressLine2'  => $a['addressLine2'] ?? null,
		'postalCode'    => $a['postalCode'] ?? null,
	);
}

// ---- ZarinPal gateway (shared by payment + wallet top-up) -------------------

function cb_zp_merchant(): string {
	return (string) get_option( 'cb_zarinpal_merchant', defined( 'CB_ZARINPAL_MERCHANT' ) ? CB_ZARINPAL_MERCHANT : '' );
}

function cb_zp_is_sandbox(): bool {
	return get_option( 'cb_zarinpal_sandbox', '' ) === '1';
}

function cb_zp_base(): string {
	return cb_zp_is_sandbox() ? 'https://sandbox.zarinpal.com' : 'https://api.zarinpal.com';
}

function cb_zp_startpay( string $authority ): string {
	$base = cb_zp_is_sandbox() ? 'https://sandbox.zarinpal.com/pg/StartPay/' : 'https://www.zarinpal.com/pg/StartPay/';
	return $base . $authority;
}

/** Toman total -> Rial amount for the gateway (filterable for Rial stores). */
function cb_zp_amount( float $total ): int {
	return (int) apply_filters( 'cb_zarinpal_amount', (int) round( $total * 10 ), $total );
}

/** Create a payment request; returns the authority string or null on failure. */
function cb_zp_request( int $amount, string $callback, string $description ): ?string {
	$merchant = cb_zp_merchant();
	if ( ! $merchant ) {
		return null;
	}
	$response = wp_remote_post( cb_zp_base() . '/pg/v4/payment/request.json', array(
		'headers' => array( 'Content-Type' => 'application/json', 'Accept' => 'application/json' ),
		'timeout' => 20,
		'body'    => wp_json_encode( array(
			'merchant_id'  => $merchant,
			'amount'       => $amount,
			'callback_url' => $callback,
			'description'  => $description,
		) ),
	) );
	if ( is_wp_error( $response ) ) {
		return null;
	}
	$data = json_decode( wp_remote_retrieve_body( $response ), true );
	$code = $data['data']['code'] ?? null;
	if ( $code == 100 && ! empty( $data['data']['authority'] ) ) {
		return (string) $data['data']['authority'];
	}
	return null;
}

/** Verify a payment; returns the ref_id string on success, null otherwise. */
function cb_zp_verify( int $amount, string $authority ): ?string {
	$response = wp_remote_post( cb_zp_base() . '/pg/v4/payment/verify.json', array(
		'headers' => array( 'Content-Type' => 'application/json', 'Accept' => 'application/json' ),
		'timeout' => 20,
		'body'    => wp_json_encode( array(
			'merchant_id' => cb_zp_merchant(),
			'amount'      => $amount,
			'authority'   => $authority,
		) ),
	) );
	if ( is_wp_error( $response ) ) {
		return null;
	}
	$data = json_decode( wp_remote_retrieve_body( $response ), true );
	$code = $data['data']['code'] ?? null;
	if ( $code == 100 || $code == 101 ) {
		return (string) ( $data['data']['ref_id'] ?? '' );
	}
	return null;
}

/** Deep-link URL back into the app after a gateway round-trip. */
function cb_app_return_url( array $args ): string {
	$base = get_option( 'cb_app_return_url', 'carmilla://payment/result' );
	return add_query_arg( $args, $base );
}

/**
 * Create custom tables on activation. The bookings table has a UNIQUE key on
 * (therapist_id, slot_time) so concurrent bookings of the same slot are
 * rejected at the database level — the atomic slot lock the clinic relies on.
 */
function cb_create_tables(): void {
	global $wpdb;
	$charset = $wpdb->get_charset_collate();
	$table   = $wpdb->prefix . 'cb_bookings';
	require_once ABSPATH . 'wp-admin/includes/upgrade.php';
	dbDelta( "CREATE TABLE $table (
		id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
		therapist_id BIGINT UNSIGNED NOT NULL,
		slot_time VARCHAR(40) NOT NULL,
		appointment_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
		user_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
		created_at DATETIME NOT NULL,
		PRIMARY KEY  (id),
		UNIQUE KEY uniq_slot (therapist_id, slot_time)
	) $charset;" );
}

function cb_bookings_table(): string {
	global $wpdb;
	return $wpdb->prefix . 'cb_bookings';
}

/**
 * Shape a WooCommerce product into a ProductSummaryResponse (shared by
 * favorites, recently-viewed, bundles and frequently-bought-together).
 * Favorites are read from user meta cb_wishlist (theme-compatible).
 */
function cb_product_summary_dto( WC_Product $product, int $user_id = 0 ): array {
	$cats   = $product->get_category_ids();
	$cat_id = $cats ? (int) $cats[0] : null;

	if ( $product->is_type( 'variable' ) ) {
		$prices   = $product->get_variation_prices( true );
		$reg      = array_map( 'floatval', $prices['regular_price'] ?? array() );
		$sales    = array();
		foreach ( ( $prices['sale_price'] ?? array() ) as $k => $sp ) {
			if ( isset( $reg[ $k ] ) && (float) $sp < $reg[ $k ] ) {
				$sales[] = (float) $sp;
			}
		}
		$min_reg  = $reg ? min( $reg ) : null;
		$max_reg  = $reg ? max( $reg ) : null;
		$min_sale = $sales ? min( $sales ) : null;
		$max_sale = $sales ? max( $sales ) : null;
		$options  = array();
		foreach ( $product->get_variation_attributes() as $name => $vals ) {
			$options[ wc_attribute_label( str_replace( 'attribute_', '', $name ) ) ] = array_values( $vals );
		}
	} else {
		$reg      = (float) $product->get_regular_price();
		$sale     = $product->get_sale_price();
		$min_reg  = $max_reg = $reg ?: (float) $product->get_price();
		$min_sale = $max_sale = ( $sale !== '' && $sale !== null ) ? (float) $sale : null;
		$options  = array();
	}

	$favs = $user_id ? (array) get_user_meta( $user_id, 'cb_wishlist', true ) : array();
	$term = $cat_id ? get_term( $cat_id ) : null;

	return array(
		'id'                 => (int) $product->get_id(),
		'title'              => $product->get_name(),
		'slug'               => $product->get_slug(),
		'thumbnailUrl'       => wp_get_attachment_url( $product->get_image_id() ) ?: null,
		'minPrice'           => $min_reg,
		'maxPrice'           => $max_reg,
		'minDiscountedPrice' => $min_sale,
		'maxDiscountedPrice' => $max_sale,
		'inStock'            => $product->is_in_stock(),
		'categoryId'         => $cat_id,
		'categoryName'       => ( $term && ! is_wp_error( $term ) ) ? $term->name : null,
		'options'            => (object) $options,
		'isFavorite'         => in_array( (int) $product->get_id(), array_map( 'intval', $favs ), true ),
		'averageRating'      => (float) $product->get_average_rating() ?: null,
		'reviewCount'        => (int) $product->get_review_count(),
	);
}
