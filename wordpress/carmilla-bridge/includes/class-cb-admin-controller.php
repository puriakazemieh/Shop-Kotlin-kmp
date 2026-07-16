<?php
/**
 * Admin (shop) endpoints — dashboard stats, order management, categories,
 * discounts (WooCommerce coupons), wallet admin, course-requests and
 * return-requests moderation. All require an admin/shop-manager Bearer token.
 *
 *   GET   api/admin/stats
 *   GET   api/admin/orders ; GET api/admin/orders/{id} ; PATCH api/admin/orders/{id}/status
 *   GET/POST api/admin/categories ; PATCH/DELETE api/admin/categories/{id}
 *   GET/POST api/admin/discounts ; PATCH/DELETE api/admin/discounts/{id}
 *   GET api/admin/wallet/users/search ; POST api/admin/wallet/adjust
 *   GET api/admin/wallet/withdrawals ; POST api/admin/wallet/withdrawals/{id}/process
 *   GET api/admin/course-requests ; DELETE api/admin/course-requests/{id}
 *   GET api/admin/return-requests ; PATCH api/admin/return-requests/{id}
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Admin_Controller {

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$admin = array( 'CB_Plugin', 'require_admin' );

		register_rest_route( $ns, '/api/admin/stats', array( 'methods' => 'GET', 'callback' => array( $this, 'stats' ), 'permission_callback' => $admin ) );

		register_rest_route( $ns, '/api/admin/orders', array( 'methods' => 'GET', 'callback' => array( $this, 'orders' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/orders/(?P<id>\d+)', array( 'methods' => 'GET', 'callback' => array( $this, 'order_detail' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/orders/(?P<id>\d+)/status', array( 'methods' => array( 'PATCH', 'POST' ), 'callback' => array( $this, 'update_order_status' ), 'permission_callback' => $admin ) );

		register_rest_route( $ns, '/api/admin/categories', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_categories' ), 'permission_callback' => $admin ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'create_category' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/categories/(?P<id>\d+)', array(
			array( 'methods' => 'PATCH', 'callback' => array( $this, 'update_category' ), 'permission_callback' => $admin ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_category' ), 'permission_callback' => $admin ),
		) );

		register_rest_route( $ns, '/api/admin/discounts', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_discounts' ), 'permission_callback' => $admin ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'create_discount' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/discounts/(?P<id>\d+)', array(
			array( 'methods' => 'PATCH', 'callback' => array( $this, 'update_discount' ), 'permission_callback' => $admin ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_discount' ), 'permission_callback' => $admin ),
		) );

		register_rest_route( $ns, '/api/admin/wallet/users/search', array( 'methods' => 'GET', 'callback' => array( $this, 'wallet_search' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/wallet/adjust', array( 'methods' => 'POST', 'callback' => array( $this, 'wallet_adjust' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/wallet/withdrawals', array( 'methods' => 'GET', 'callback' => array( $this, 'withdrawals' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/wallet/withdrawals/(?P<id>\d+)/process', array( 'methods' => 'POST', 'callback' => array( $this, 'process_withdrawal' ), 'permission_callback' => $admin ) );

		register_rest_route( $ns, '/api/admin/course-requests', array( 'methods' => 'GET', 'callback' => array( $this, 'course_requests' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/course-requests/(?P<id>\d+)', array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_course_request' ), 'permission_callback' => $admin ) );

		register_rest_route( $ns, '/api/admin/return-requests', array( 'methods' => 'GET', 'callback' => array( $this, 'return_requests' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/return-requests/(?P<id>\d+)', array( 'methods' => 'PATCH', 'callback' => array( $this, 'update_return' ), 'permission_callback' => $admin ) );

		register_rest_route( $ns, '/api/admin/reviews', array( 'methods' => 'GET', 'callback' => array( $this, 'reviews' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/questions', array( 'methods' => 'GET', 'callback' => array( $this, 'questions' ), 'permission_callback' => $admin ) );
	}

	// ---- reviews / questions moderation ------------------------------------

	private function interactions( string $type, WP_REST_Request $request ): WP_REST_Response {
		$page = max( 0, (int) $request->get_param( 'page' ) );
		$size = max( 1, (int) ( $request->get_param( 'size' ) ?: 20 ) );
		$args = array( 'type' => $type, 'status' => 'approve', 'number' => $size, 'offset' => $page * $size, 'orderby' => 'comment_date_gmt', 'order' => 'DESC' );
		if ( $request->get_param( 'productId' ) ) {
			$args['post_id'] = (int) $request->get_param( 'productId' );
		}
		$total = (int) get_comments( array_merge( $args, array( 'count' => true, 'number' => 0, 'offset' => 0 ) ) );
		$week  = time() - 7 * DAY_IN_SECONDS;
		$items = array();
		foreach ( get_comments( $args ) as $c ) {
			$items[] = array(
				'id'           => (int) $c->comment_ID,
				'productId'    => (int) $c->comment_post_ID,
				'productTitle' => get_the_title( $c->comment_post_ID ),
				'userId'       => (int) $c->user_id,
				'userName'     => $c->comment_author ?: 'کاربر',
				'content'      => $c->comment_content,
				'rating'       => ( $r = get_comment_meta( $c->comment_ID, 'rating', true ) ) !== '' ? (int) $r : null,
				'isNew'        => strtotime( $c->comment_date_gmt ) >= $week,
				'createdAt'    => cb_iso( $c->comment_date_gmt ),
			);
		}
		return cb_response( cb_page( $items, $page, $size, $total, (int) ceil( $total / $size ) ) );
	}

	public function reviews( WP_REST_Request $request ): WP_REST_Response {
		return $this->interactions( 'review', $request );
	}

	public function questions( WP_REST_Request $request ): WP_REST_Response {
		return $this->interactions( 'cb_qna', $request );
	}

	// ---- stats --------------------------------------------------------------

	public function stats(): WP_REST_Response {
		$revenue = 0.0;
		$total_orders = 0;
		$sales_today  = 0.0;
		$orders_today = 0;
		$weekly       = array();
		$low_stock    = 0;

		if ( cb_woo_active() ) {
			$orders = wc_get_orders( array( 'limit' => -1, 'status' => array( 'processing', 'completed', 'on-hold' ) ) );
			$today  = gmdate( 'Y-m-d' );
			$buckets = array();
			for ( $i = 6; $i >= 0; $i-- ) {
				$buckets[ gmdate( 'Y-m-d', strtotime( "-$i days" ) ) ] = 0.0;
			}
			foreach ( $orders as $o ) {
				$total   = (float) $o->get_total();
				$revenue += $total;
				$total_orders++;
				$d = $o->get_date_created() ? $o->get_date_created()->date( 'Y-m-d' ) : $today;
				if ( isset( $buckets[ $d ] ) ) {
					$buckets[ $d ] += $total;
				}
				if ( $d === $today ) {
					$sales_today += $total;
					$orders_today++;
				}
			}
			foreach ( $buckets as $date => $sum ) {
				$weekly[] = array( 'date' => $date, 'total' => $sum );
			}
			$low = wc_get_products( array( 'limit' => -1, 'stock_status' => 'instock', 'return' => 'ids' ) );
			foreach ( $low as $pid ) {
				$p = wc_get_product( $pid );
				if ( $p && $p->get_manage_stock() && (int) $p->get_stock_quantity() <= 5 ) {
					$low_stock++;
				}
			}
		}

		return cb_response( array(
			'totalRevenue'   => $revenue,
			'totalOrders'    => $total_orders,
			'totalProducts'  => (int) wp_count_posts( 'product' )->publish,
			'totalCustomers' => (int) count_users()['total_users'],
			'weeklySales'    => $weekly,
			'verticalCounts' => array(
				'courses'    => (int) wp_count_posts( 'cb_course' )->publish,
				'therapists' => (int) wp_count_posts( 'cb_therapist' )->publish,
				'psychTests' => (int) wp_count_posts( 'cb_psychtest' )->publish,
			),
			'newOrdersToday'     => $orders_today,
			'ordersTrendPercent' => 0,
			'salesToday'         => $sales_today,
			'salesTrendPercent'  => 0,
			'lowStockCount'      => $low_stock,
		) );
	}

	// ---- orders -------------------------------------------------------------

	public function orders( WP_REST_Request $request ): WP_REST_Response {
		if ( ! cb_woo_active() ) {
			return cb_response( cb_page( array(), 0, 20, 0, 0 ) );
		}
		$page   = max( 0, (int) $request->get_param( 'page' ) );
		$size   = max( 1, (int) ( $request->get_param( 'size' ) ?: 20 ) );
		$status = $request->get_param( 'status' );
		$args   = array( 'limit' => $size, 'paged' => $page + 1, 'orderby' => 'date', 'order' => 'DESC', 'paginate' => true );
		if ( $status ) {
			$args['status'] = strtolower( cb_app_status_to_wc( (string) $status ) );
		}
		if ( $request->get_param( 'userId' ) ) {
			$args['customer_id'] = (int) $request->get_param( 'userId' );
		}
		$result = wc_get_orders( $args );
		$items  = array();
		foreach ( $result->orders as $o ) {
			$items[] = $this->order_summary( $o );
		}
		return cb_response( cb_page( $items, $page, $size, (int) $result->total, (int) $result->max_num_pages ) );
	}

	private function order_summary( WC_Order $o ): array {
		$user = $o->get_user();
		return array(
			'id'         => (int) $o->get_id(),
			'userId'     => (int) $o->get_customer_id(),
			'userEmail'  => $user ? $user->user_email : $o->get_billing_email(),
			'status'     => cb_effective_order_status( $o ),
			'totalPrice' => (float) $o->get_total(),
			'createdAt'  => $o->get_date_created() ? $o->get_date_created()->date( 'c' ) : null,
		);
	}

	public function order_detail( WP_REST_Request $request ): WP_REST_Response {
		$o = cb_woo_active() ? wc_get_order( (int) $request['id'] ) : null;
		if ( ! $o ) {
			return cb_error( 'سفارش یافت نشد', 404, 'NOT_FOUND', 'api/admin/orders' );
		}
		$items = array();
		foreach ( $o->get_items() as $item ) {
			$qty     = max( 1, (int) $item->get_quantity() );
			$options = array();
			foreach ( $item->get_meta_data() as $meta ) {
				$data = $meta->get_data();
				if ( ! empty( $data['key'] ) && strpos( $data['key'], '_' ) !== 0 ) {
					$options[ wc_attribute_label( $data['key'] ) ] = is_scalar( $data['value'] ) ? (string) $data['value'] : '';
				}
			}
			$items[] = array(
				'id'                => (int) $item->get_id(),
				'variantId'         => (int) ( $item->get_variation_id() ?: $item->get_product_id() ),
				'qty'               => $qty,
				'unitPriceSnapshot' => (float) ( $item->get_subtotal() / $qty ),
				'titleSnapshot'     => $item->get_name(),
				'optionsSnapshot'   => (object) $options,
			);
		}
		$user = $o->get_user();
		return cb_response( array(
			'id'            => (int) $o->get_id(),
			'userId'        => (int) $o->get_customer_id(),
			'userEmail'     => $user ? $user->user_email : $o->get_billing_email(),
			'status'        => cb_effective_order_status( $o ),
			'subtotalPrice' => (float) $o->get_subtotal(),
			'shippingPrice' => (float) $o->get_shipping_total(),
			'totalPrice'    => (float) $o->get_total(),
			'createdAt'     => $o->get_date_created() ? $o->get_date_created()->date( 'c' ) : null,
			'updatedAt'     => $o->get_date_modified() ? $o->get_date_modified()->date( 'c' ) : null,
			'addressSnapshot' => array(
				'receiverName'  => $o->get_billing_first_name(),
				'receiverPhone' => $o->get_billing_phone(),
				'country'       => $o->get_billing_country() ?: 'IR',
				'province'      => $o->get_billing_state(),
				'city'          => $o->get_billing_city(),
				'addressLine1'  => $o->get_billing_address_1(),
				'addressLine2'  => $o->get_billing_address_2() ?: null,
				'postalCode'    => $o->get_billing_postcode() ?: null,
			),
			'items'             => $items,
			'walletPaidAmount'  => (float) $o->get_meta( '_cb_wallet_paid' ) ?: null,
			'gatewayPaidAmount' => null,
		) );
	}

	public function update_order_status( WP_REST_Request $request ): WP_REST_Response {
		$o = cb_woo_active() ? wc_get_order( (int) $request['id'] ) : null;
		if ( ! $o ) {
			return cb_error( 'سفارش یافت نشد', 404, 'NOT_FOUND', 'api/admin/orders' );
		}
		$status = strtoupper( (string) ( $request->get_json_params()['status'] ?? '' ) );
		if ( $status === '' ) {
			return cb_error( 'وضعیت نامعتبر است', 400, 'INVALID_STATUS', 'api/admin/orders' );
		}
		$o->update_meta_data( '_cb_app_status', $status );
		if ( $status === 'SHIPPED' ) {
			$o->update_meta_data( '_cb_shipped_at', gmdate( 'c' ) );
		}
		$o->set_status( cb_app_status_to_wc( $status ) );
		$o->save();
		return cb_response( null, 200 );
	}

	// ---- categories (product_cat) ------------------------------------------

	private function category_dto( $term ): array {
		return array(
			'id'       => (int) $term->term_id,
			'name'     => $term->name,
			'slug'     => $term->slug,
			'parentId' => $term->parent ? (int) $term->parent : null,
		);
	}

	public function list_categories(): WP_REST_Response {
		$terms = get_terms( array( 'taxonomy' => 'product_cat', 'hide_empty' => false ) );
		$out   = array();
		foreach ( ( is_array( $terms ) ? $terms : array() ) as $t ) {
			$out[] = $this->category_dto( $t );
		}
		return cb_response( $out );
	}

	public function create_category( WP_REST_Request $request ): WP_REST_Response {
		$body = $request->get_json_params();
		$res  = wp_insert_term( sanitize_text_field( (string) ( $body['name'] ?? '' ) ), 'product_cat', array(
			'slug'   => sanitize_title( (string) ( $body['slug'] ?? '' ) ),
			'parent' => (int) ( $body['parentId'] ?? 0 ),
		) );
		if ( is_wp_error( $res ) ) {
			return cb_error( 'ساخت دسته ناموفق بود', 400, 'CREATE_FAILED', 'api/admin/categories' );
		}
		return cb_response( $this->category_dto( get_term( $res['term_id'], 'product_cat' ) ), 201 );
	}

	public function update_category( WP_REST_Request $request ): WP_REST_Response {
		$id   = (int) $request['id'];
		$body = $request->get_json_params();
		$args = array();
		if ( isset( $body['name'] ) ) {
			$args['name'] = sanitize_text_field( (string) $body['name'] );
		}
		if ( isset( $body['slug'] ) ) {
			$args['slug'] = sanitize_title( (string) $body['slug'] );
		}
		if ( isset( $body['parentId'] ) ) {
			$args['parent'] = (int) $body['parentId'];
		}
		$res = wp_update_term( $id, 'product_cat', $args );
		if ( is_wp_error( $res ) ) {
			return cb_error( 'ویرایش ناموفق بود', 400, 'UPDATE_FAILED', 'api/admin/categories' );
		}
		return cb_response( $this->category_dto( get_term( $id, 'product_cat' ) ) );
	}

	public function delete_category( WP_REST_Request $request ): WP_REST_Response {
		wp_delete_term( (int) $request['id'], 'product_cat' );
		return cb_response( null, 204 );
	}

	// ---- discounts (WooCommerce coupons) -----------------------------------

	private function discount_dto( WC_Coupon $c ): array {
		$type = $c->get_discount_type() === 'percent' ? 'PERCENTAGE' : 'FIXED_AMOUNT';
		return array(
			'id'                => (int) $c->get_id(),
			'code'              => $c->get_code(),
			'type'              => $type,
			'value'             => (float) $c->get_amount(),
			'maxDiscountAmount' => null,
			'minOrderAmount'    => $c->get_minimum_amount() ? (float) $c->get_minimum_amount() : null,
			'startDate'         => null,
			'endDate'           => $c->get_date_expires() ? $c->get_date_expires()->date( 'c' ) : null,
			'usageLimit'        => $c->get_usage_limit() ?: null,
			'usageCount'        => (int) $c->get_usage_count(),
			'isActive'          => true,
		);
	}

	public function list_discounts(): WP_REST_Response {
		if ( ! cb_woo_active() ) {
			return cb_response( array() );
		}
		$posts = get_posts( array( 'post_type' => 'shop_coupon', 'post_status' => 'publish', 'numberposts' => 100 ) );
		$out   = array();
		foreach ( $posts as $p ) {
			$out[] = $this->discount_dto( new WC_Coupon( $p->ID ) );
		}
		return cb_response( $out );
	}

	public function create_discount( WP_REST_Request $request ): WP_REST_Response {
		if ( ! cb_woo_active() ) {
			return cb_error( 'WooCommerce غیرفعال است', 400, 'WOO_INACTIVE', 'api/admin/discounts' );
		}
		$body   = $request->get_json_params();
		$coupon = new WC_Coupon();
		$coupon->set_code( sanitize_text_field( (string) ( $body['code'] ?? '' ) ) );
		$coupon->set_discount_type( ( strtoupper( (string) ( $body['type'] ?? '' ) ) === 'PERCENTAGE' ) ? 'percent' : 'fixed_cart' );
		$coupon->set_amount( (float) ( $body['value'] ?? 0 ) );
		if ( ! empty( $body['minOrderAmount'] ) ) {
			$coupon->set_minimum_amount( (float) $body['minOrderAmount'] );
		}
		if ( ! empty( $body['usageLimit'] ) ) {
			$coupon->set_usage_limit( (int) $body['usageLimit'] );
		}
		if ( ! empty( $body['endDate'] ) ) {
			$coupon->set_date_expires( strtotime( (string) $body['endDate'] ) );
		}
		$coupon->save();
		return cb_response( $this->discount_dto( $coupon ), 201 );
	}

	public function update_discount( WP_REST_Request $request ): WP_REST_Response {
		$coupon = cb_woo_active() ? new WC_Coupon( (int) $request['id'] ) : null;
		if ( ! $coupon || ! $coupon->get_id() ) {
			return cb_error( 'کد تخفیف یافت نشد', 404, 'NOT_FOUND', 'api/admin/discounts' );
		}
		$body = $request->get_json_params();
		if ( isset( $body['value'] ) ) {
			$coupon->set_amount( (float) $body['value'] );
		}
		if ( isset( $body['type'] ) ) {
			$coupon->set_discount_type( strtoupper( (string) $body['type'] ) === 'PERCENTAGE' ? 'percent' : 'fixed_cart' );
		}
		if ( isset( $body['minOrderAmount'] ) ) {
			$coupon->set_minimum_amount( (float) $body['minOrderAmount'] );
		}
		if ( isset( $body['usageLimit'] ) ) {
			$coupon->set_usage_limit( (int) $body['usageLimit'] );
		}
		if ( array_key_exists( 'endDate', $body ) ) {
			$coupon->set_date_expires( $body['endDate'] ? strtotime( (string) $body['endDate'] ) : null );
		}
		$coupon->save();
		return cb_response( $this->discount_dto( $coupon ) );
	}

	public function delete_discount( WP_REST_Request $request ): WP_REST_Response {
		wp_delete_post( (int) $request['id'], true );
		return cb_response( null, 204 );
	}

	// ---- wallet admin -------------------------------------------------------

	public function wallet_search( WP_REST_Request $request ): WP_REST_Response {
		$q     = sanitize_text_field( (string) $request->get_param( 'query' ) );
		$users = get_users( array( 'search' => '*' . $q . '*', 'search_columns' => array( 'user_email', 'user_login', 'display_name' ), 'number' => 20 ) );
		$out   = array();
		foreach ( $users as $u ) {
			$out[] = array(
				'userId'   => (int) $u->ID,
				'email'    => $u->user_email,
				'fullName' => $u->display_name,
				'balance'  => cb_wallet_balance( $u->ID ),
			);
		}
		return cb_response( $out );
	}

	public function wallet_adjust( WP_REST_Request $request ): WP_REST_Response {
		$body   = $request->get_json_params();
		$uid    = (int) ( $body['userId'] ?? 0 );
		$amount = (float) ( $body['amount'] ?? 0 );
		if ( ! get_user_by( 'id', $uid ) ) {
			return cb_error( 'کاربر یافت نشد', 404, 'NOT_FOUND', 'api/admin/wallet/adjust' );
		}
		cb_wallet_add( $uid, $amount, 'ADMIN_ADJUST', isset( $body['description'] ) ? sanitize_text_field( (string) $body['description'] ) : 'تعدیل توسط مدیر', null );
		return cb_response( null, 200 );
	}

	public function withdrawals( WP_REST_Request $request ): WP_REST_Response {
		$filter = $request->get_param( 'status' );
		$out    = array();
		$users  = get_users( array( 'meta_key' => 'cb_wallet_txns', 'number' => 500 ) );
		foreach ( $users as $u ) {
			foreach ( cb_wallet_txns( $u->ID ) as $txn ) {
				if ( ( $txn['type'] ?? '' ) !== 'WITHDRAW' ) {
					continue;
				}
				$status = $txn['status'] ?? 'PENDING';
				if ( $filter && strtoupper( (string) $filter ) !== strtoupper( $status ) ) {
					continue;
				}
				$out[] = array(
					'id'           => (int) $txn['id'],
					'userId'       => (int) $u->ID,
					'userFullName' => $u->display_name,
					'userEmail'    => $u->user_email,
					'amount'       => abs( (float) $txn['amount'] ),
					'iban'         => (string) ( $txn['referenceId'] ?? '' ),
					'status'       => $status,
					'adminNote'    => $txn['adminNote'] ?? null,
					'createdAt'    => (string) ( $txn['createdAt'] ?? gmdate( 'c' ) ),
				);
			}
		}
		return cb_response( $out );
	}

	public function process_withdrawal( WP_REST_Request $request ): WP_REST_Response {
		// Mark the matching withdrawal transaction; refund the balance if rejected.
		$txn_id = (int) $request['id'];
		$body   = $request->get_json_params();
		$status = strtoupper( (string) ( $body['status'] ?? 'APPROVED' ) );
		$users  = get_users( array( 'meta_key' => 'cb_wallet_txns', 'number' => 500 ) );
		foreach ( $users as $u ) {
			$txns    = cb_wallet_txns( $u->ID );
			$changed = false;
			foreach ( $txns as &$txn ) {
				if ( (int) $txn['id'] === $txn_id && ( $txn['type'] ?? '' ) === 'WITHDRAW' ) {
					$txn['status']    = $status;
					$txn['adminNote'] = isset( $body['adminNote'] ) ? sanitize_text_field( (string) $body['adminNote'] ) : null;
					$changed          = true;
					if ( $status === 'REJECTED' ) {
						cb_wallet_add( $u->ID, abs( (float) $txn['amount'] ), 'REFUND', 'رد درخواست برداشت', null );
					}
				}
			}
			unset( $txn );
			if ( $changed ) {
				update_user_meta( $u->ID, 'cb_wallet_txns', $txns );
				return cb_response( null, 200 );
			}
		}
		return cb_error( 'درخواست یافت نشد', 404, 'NOT_FOUND', 'api/admin/wallet/withdrawals' );
	}

	// ---- course requests (cb_course_request CPT) ---------------------------

	public function course_requests(): WP_REST_Response {
		$posts = get_posts( array( 'post_type' => 'cb_course_request', 'post_status' => array( 'publish', 'pending', 'draft' ), 'numberposts' => 200 ) );
		$out   = array();
		foreach ( $posts as $p ) {
			$out[] = array(
				'id'        => (int) $p->ID,
				'title'     => get_the_title( $p ),
				'body'      => $p->post_content,
				'likes'     => (int) get_post_meta( $p->ID, 'cb_likes', true ),
				'userId'    => (int) $p->post_author,
				'createdAt' => cb_iso( $p->post_date_gmt ),
			);
		}
		return cb_response( $out );
	}

	public function delete_course_request( WP_REST_Request $request ): WP_REST_Response {
		wp_delete_post( (int) $request['id'], true );
		return cb_response( null, 204 );
	}

	// ---- return requests (aggregated from user meta) -----------------------

	public function return_requests(): WP_REST_Response {
		$users = get_users( array( 'meta_key' => 'cb_returns', 'number' => 500 ) );
		$out   = array();
		foreach ( $users as $u ) {
			foreach ( (array) get_user_meta( $u->ID, 'cb_returns', true ) as $r ) {
				$r['userId']   = (int) $u->ID;
				$r['userName'] = $u->display_name;
				$out[]         = $r;
			}
		}
		return cb_response( $out );
	}

	public function update_return( WP_REST_Request $request ): WP_REST_Response {
		$id     = (int) $request['id'];
		$body   = $request->get_json_params();
		$status = sanitize_text_field( (string) ( $body['status'] ?? 'APPROVED' ) );
		$users  = get_users( array( 'meta_key' => 'cb_returns', 'number' => 500 ) );
		foreach ( $users as $u ) {
			$list    = (array) get_user_meta( $u->ID, 'cb_returns', true );
			$changed = false;
			foreach ( $list as &$r ) {
				if ( (int) $r['id'] === $id ) {
					$r['status']     = $status;
					$r['adminNote']  = isset( $body['adminNote'] ) ? sanitize_text_field( (string) $body['adminNote'] ) : null;
					$r['resolvedAt'] = gmdate( 'c' );
					$r['userId']     = (int) $u->ID;
					$r['userName']   = $u->display_name;
					$changed         = true;
					$updated         = $r;
				}
			}
			unset( $r );
			if ( $changed ) {
				update_user_meta( $u->ID, 'cb_returns', $list );
				return cb_response( $updated );
			}
		}
		return cb_error( 'درخواست یافت نشد', 404, 'NOT_FOUND', 'api/admin/return-requests' );
	}
}
