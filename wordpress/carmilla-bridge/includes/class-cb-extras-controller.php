<?php
/**
 * Phase 5 shop extras — membership/club, referral, favorites, recently viewed,
 * returns, recurring orders, stock/price alerts, frequently-bought-together.
 * State lives in user meta (theme-compatible keys where they overlap).
 *
 *   GET  api/memberships/mine ; POST api/memberships/subscribe
 *   GET  api/referrals/mine
 *   GET/POST/DELETE /api/favorites[/{productId}]
 *   GET/POST /api/recently-viewed[/{productId}]
 *   POST api/return-requests ; GET api/return-requests/mine
 *   POST api/recurring-orders ; GET api/recurring-orders/mine ; POST .../{id}/cancel
 *   POST api/stock-notifications ; POST api/price-alerts
 *   GET  api/products/{productId}/frequently-bought-together
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Extras_Controller {

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$login = array( 'CB_Plugin', 'require_login' );

		register_rest_route( $ns, '/api/memberships/mine', array( 'methods' => 'GET', 'callback' => array( $this, 'membership_mine' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/memberships/subscribe', array( 'methods' => 'POST', 'callback' => array( $this, 'membership_subscribe' ), 'permission_callback' => $login ) );

		register_rest_route( $ns, '/api/referrals/mine', array( 'methods' => 'GET', 'callback' => array( $this, 'referral_mine' ), 'permission_callback' => $login ) );

		// Favorites use a host-root path; aliased in CB_Plugin::maybe_root_alias.
		register_rest_route( $ns, '/api/favorites', array( 'methods' => 'GET', 'callback' => array( $this, 'favorites' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/favorites/(?P<pid>\d+)', array(
			array( 'methods' => 'POST', 'callback' => array( $this, 'add_favorite' ), 'permission_callback' => $login ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'remove_favorite' ), 'permission_callback' => $login ),
		) );

		register_rest_route( $ns, '/api/recently-viewed', array( 'methods' => 'GET', 'callback' => array( $this, 'recently_viewed' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/recently-viewed/(?P<pid>\d+)', array( 'methods' => 'POST', 'callback' => array( $this, 'record_view' ), 'permission_callback' => $login ) );

		register_rest_route( $ns, '/api/return-requests', array( 'methods' => 'POST', 'callback' => array( $this, 'create_return' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/return-requests/mine', array( 'methods' => 'GET', 'callback' => array( $this, 'my_returns' ), 'permission_callback' => $login ) );

		register_rest_route( $ns, '/api/recurring-orders', array( 'methods' => 'POST', 'callback' => array( $this, 'create_recurring' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/recurring-orders/mine', array( 'methods' => 'GET', 'callback' => array( $this, 'my_recurring' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/recurring-orders/(?P<id>\d+)/cancel', array( 'methods' => 'POST', 'callback' => array( $this, 'cancel_recurring' ), 'permission_callback' => $login ) );

		register_rest_route( $ns, '/api/stock-notifications', array( 'methods' => 'POST', 'callback' => array( $this, 'stock_notify' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/price-alerts', array( 'methods' => 'POST', 'callback' => array( $this, 'price_alert' ), 'permission_callback' => $login ) );

		register_rest_route( $ns, '/api/products/(?P<pid>\d+)/frequently-bought-together', array( 'methods' => 'GET', 'callback' => array( $this, 'fbt' ), 'permission_callback' => '__return_true' ) );
	}

	// ---- membership / club --------------------------------------------------

	private function membership_price(): float {
		return (float) get_option( 'cb_membership_price', 200000 );
	}

	private function membership_discount(): float {
		return (float) get_option( 'cb_membership_discount', 5 ); // percent
	}

	/** Whether a membership expiry timestamp is still in the future. */
	public static function membership_active( $expires ): bool {
		return $expires && strtotime( (string) $expires ) > time();
	}

	/** Deterministic referral code for a user. */
	public static function referral_code( int $uid, string $salt ): string {
		return 'REF' . strtoupper( substr( md5( $uid . $salt ), 0, 6 ) );
	}

	public function membership_mine(): WP_REST_Response {
		$uid     = get_current_user_id();
		$expires = get_user_meta( $uid, 'cb_membership_expires', true );
		$active  = self::membership_active( $expires );
		return cb_response( array(
			'isActive'        => (bool) $active,
			'tier'            => $active ? 'VIP' : null,
			'expiresAt'       => $active ? $expires : null,
			'discountPercent' => $this->membership_discount(),
			'price'           => $this->membership_price(),
		) );
	}

	public function membership_subscribe(): WP_REST_Response {
		$uid   = get_current_user_id();
		$price = $this->membership_price();
		if ( cb_wallet_balance( $uid ) < $price ) {
			return cb_error( 'موجودی کیف پول کافی نیست', 402, 'INSUFFICIENT_BALANCE', 'api/memberships/subscribe' );
		}
		cb_wallet_add( $uid, -$price, 'MEMBERSHIP', 'اشتراک باشگاه مشتریان', null );
		$base    = get_user_meta( $uid, 'cb_membership_expires', true );
		$start   = ( $base && strtotime( $base ) > time() ) ? strtotime( $base ) : time();
		$expires = gmdate( 'c', strtotime( '+30 days', $start ) );
		update_user_meta( $uid, 'cb_membership_expires', $expires );
		return $this->membership_mine();
	}

	// ---- referral -----------------------------------------------------------

	public function referral_mine(): WP_REST_Response {
		$uid  = get_current_user_id();
		$code = get_user_meta( $uid, 'cb_referral_code', true );
		if ( ! $code ) {
			$code = self::referral_code( $uid, wp_salt() );
			update_user_meta( $uid, 'cb_referral_code', $code );
		}
		return cb_response( array(
			'code'          => $code,
			'referredCount' => (int) get_user_meta( $uid, 'cb_referred_count', true ),
			'totalEarned'   => (float) get_user_meta( $uid, 'cb_referral_earned', true ),
		) );
	}

	// ---- favorites (user meta cb_wishlist) ---------------------------------

	private function wishlist( int $uid ): array {
		$v = get_user_meta( $uid, 'cb_wishlist', true );
		return is_array( $v ) ? array_values( array_map( 'intval', $v ) ) : array();
	}

	public function favorites( WP_REST_Request $request ): WP_REST_Response {
		$uid  = get_current_user_id();
		$ids  = $this->wishlist( $uid );
		return cb_response( $this->paged_products( $ids, $request, $uid ) );
	}

	public function add_favorite( WP_REST_Request $request ): WP_REST_Response {
		$uid = get_current_user_id();
		$pid = (int) $request['pid'];
		$ids = $this->wishlist( $uid );
		if ( ! in_array( $pid, $ids, true ) ) {
			$ids[] = $pid;
			update_user_meta( $uid, 'cb_wishlist', $ids );
		}
		return cb_response( null, 200 );
	}

	public function remove_favorite( WP_REST_Request $request ): WP_REST_Response {
		$uid = get_current_user_id();
		$pid = (int) $request['pid'];
		update_user_meta( $uid, 'cb_wishlist', array_values( array_diff( $this->wishlist( $uid ), array( $pid ) ) ) );
		return cb_response( null, 200 );
	}

	// ---- recently viewed (user meta cb_recently_viewed) --------------------

	public function recently_viewed( WP_REST_Request $request ): WP_REST_Response {
		$uid = get_current_user_id();
		$ids = (array) get_user_meta( $uid, 'cb_recently_viewed', true );
		return cb_response( $this->paged_products( array_map( 'intval', $ids ), $request, $uid ) );
	}

	public function record_view( WP_REST_Request $request ): WP_REST_Response {
		$uid = get_current_user_id();
		$pid = (int) $request['pid'];
		$ids = array_map( 'intval', (array) get_user_meta( $uid, 'cb_recently_viewed', true ) );
		$ids = array_values( array_diff( $ids, array( $pid ) ) );
		array_unshift( $ids, $pid );
		update_user_meta( $uid, 'cb_recently_viewed', array_slice( $ids, 0, 40 ) );
		return cb_response( null, 200 );
	}

	/** Page a list of product ids into PageResponse<ProductSummaryResponse>. */
	private function paged_products( array $ids, WP_REST_Request $request, int $uid ): array {
		$page  = max( 0, (int) $request->get_param( 'page' ) );
		$size  = max( 1, (int) ( $request->get_param( 'size' ) ?: 20 ) );
		$total = count( $ids );
		$slice = array_slice( $ids, $page * $size, $size );
		$items = array();
		foreach ( $slice as $pid ) {
			$product = cb_woo_active() ? wc_get_product( $pid ) : null;
			if ( $product ) {
				$items[] = cb_product_summary_dto( $product, $uid );
			}
		}
		return cb_page( $items, $page, $size, $total, (int) ceil( $total / $size ) );
	}

	// ---- returns (user meta cb_returns) ------------------------------------

	public function create_return( WP_REST_Request $request ): WP_REST_Response {
		$uid   = get_current_user_id();
		$body  = $request->get_json_params();
		$list  = (array) get_user_meta( $uid, 'cb_returns', true );
		$list  = array_values( $list );
		$item_id = (int) ( $body['orderItemId'] ?? 0 );
		$title = '';
		$order_id = 0;
		if ( cb_woo_active() ) {
			$item = new WC_Order_Item_Product( $item_id );
			$title = $item->get_name();
			$order_id = (int) $item->get_order_id();
		}
		$entry = array(
			'id'          => count( $list ) + 1,
			'orderId'     => $order_id,
			'orderItemId' => $item_id,
			'itemTitle'   => $title ?: 'کالا',
			'type'        => sanitize_text_field( (string) ( $body['type'] ?? 'RETURN' ) ),
			'reason'      => sanitize_text_field( (string) ( $body['reason'] ?? '' ) ),
			'status'      => 'PENDING',
			'adminNote'   => null,
			'createdAt'   => gmdate( 'c' ),
			'resolvedAt'  => null,
		);
		$list[] = $entry;
		update_user_meta( $uid, 'cb_returns', $list );
		return cb_response( $entry, 201 );
	}

	public function my_returns(): WP_REST_Response {
		$list = (array) get_user_meta( get_current_user_id(), 'cb_returns', true );
		return cb_response( array_values( $list ) );
	}

	// ---- recurring orders (user meta cb_recurring) -------------------------

	public function create_recurring( WP_REST_Request $request ): WP_REST_Response {
		$uid   = get_current_user_id();
		$body  = $request->get_json_params();
		$list  = array_values( (array) get_user_meta( $uid, 'cb_recurring', true ) );
		$interval = max( 1, (int) ( $body['intervalDays'] ?? 30 ) );
		$entry = array(
			'id'          => count( $list ) + 1,
			'variantId'   => (int) ( $body['variantId'] ?? 0 ),
			'qty'         => max( 1, (int) ( $body['qty'] ?? 1 ) ),
			'intervalDays' => $interval,
			'nextRunAt'   => gmdate( 'c', strtotime( "+$interval days" ) ),
			'isActive'    => true,
			'lastOrderId' => null,
		);
		$list[] = $entry;
		update_user_meta( $uid, 'cb_recurring', $list );
		return cb_response( $entry, 201 );
	}

	public function my_recurring(): WP_REST_Response {
		$list = (array) get_user_meta( get_current_user_id(), 'cb_recurring', true );
		return cb_response( array_values( $list ) );
	}

	public function cancel_recurring( WP_REST_Request $request ): WP_REST_Response {
		$uid  = get_current_user_id();
		$id   = (int) $request['id'];
		$list = array_values( (array) get_user_meta( $uid, 'cb_recurring', true ) );
		foreach ( $list as &$r ) {
			if ( (int) $r['id'] === $id ) {
				$r['isActive'] = false;
			}
		}
		unset( $r );
		update_user_meta( $uid, 'cb_recurring', $list );
		return cb_response( null, 200 );
	}

	// ---- stock / price alerts (user meta) ----------------------------------

	public function stock_notify( WP_REST_Request $request ): WP_REST_Response {
		$uid  = get_current_user_id();
		$body = $request->get_json_params();
		$list = array_values( (array) get_user_meta( $uid, 'cb_stock_notifs', true ) );
		$list[] = array(
			'productId' => (int) ( $body['productId'] ?? 0 ),
			'variantId' => (int) ( $body['variantId'] ?? 0 ),
			'createdAt' => gmdate( 'c' ),
		);
		update_user_meta( $uid, 'cb_stock_notifs', $list );
		return cb_response( null, 200 );
	}

	public function price_alert( WP_REST_Request $request ): WP_REST_Response {
		$uid  = get_current_user_id();
		$body = $request->get_json_params();
		$list = array_values( (array) get_user_meta( $uid, 'cb_price_alerts', true ) );
		$list[] = array(
			'productId'   => (int) ( $body['productId'] ?? 0 ),
			'variantId'   => (int) ( $body['variantId'] ?? 0 ),
			'targetPrice' => (float) ( $body['targetPrice'] ?? 0 ),
			'createdAt'   => gmdate( 'c' ),
		);
		update_user_meta( $uid, 'cb_price_alerts', $list );
		return cb_response( null, 200 );
	}

	// ---- frequently bought together ----------------------------------------

	public function fbt( WP_REST_Request $request ): WP_REST_Response {
		if ( ! cb_woo_active() ) {
			return cb_response( array() );
		}
		$pid     = (int) $request['pid'];
		$product = wc_get_product( $pid );
		if ( ! $product ) {
			return cb_response( array() );
		}
		$cats = $product->get_category_ids();
		if ( ! $cats ) {
			return cb_response( array() );
		}
		$related = wc_get_products( array(
			'category' => wp_list_pluck( array_map( 'get_term', $cats ), 'slug' ),
			'exclude'  => array( $pid ),
			'limit'    => 4,
			'status'   => 'publish',
		) );
		$uid = get_current_user_id();
		$out = array();
		foreach ( $related as $p ) {
			$out[] = cb_product_summary_dto( $p, $uid );
		}
		return cb_response( $out );
	}
}
