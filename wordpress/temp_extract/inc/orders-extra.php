<?php
/**
 * Order & account extras — parity with the app's order/profile screens that the
 * base theme didn't cover yet:
 *   - OrderTrackingScreen  → status timeline on the WooCommerce view-order page.
 *   - ReturnRequestScreen  → my-account «returns» tab (CPT cb_return + REST).
 *   - RecurringOrdersScreen→ my-account «recurring» tab (user meta + REST).
 *   - SettingsScreen       → my-account «settings» tab (theme + language, client-side).
 * Theme-only: native WooCommerce endpoints, a lightweight CPT, user meta, theme REST.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/* =========================================================================
 * 1) Order tracking timeline (← OrderTrackingScreen)
 * ====================================================================== */

/** Ordered pipeline mirrored from the app: PLACED → PROCESSING → SHIPPING → COMPLETED. */
function carmilla_tracking_steps() {
	return array(
		'placed'     => __( 'ثبت سفارش', 'carmilla' ),
		'processing' => __( 'در حال پردازش', 'carmilla' ),
		'shipping'   => __( 'ارسال شد', 'carmilla' ),
		'completed'  => __( 'تحویل شد', 'carmilla' ),
	);
}

/** Map a WooCommerce order status to a pipeline index (0..3), or -1 when cancelled. */
function carmilla_tracking_index( $status ) {
	switch ( $status ) {
		case 'cancelled':
		case 'failed':
		case 'refunded':
			return -1;
		case 'pending':
		case 'on-hold':
			return 0;
		case 'processing':
			return 1;
		case 'shipping':   // common custom status in IR shops
		case 'wc-shipping':
			return 2;
		case 'completed':
			return 3;
		default:
			return 0;
	}
}

/** Render the timeline under the order table on the my-account view-order screen. */
add_action( 'woocommerce_order_details_after_order_table', function ( $order ) {
	if ( ! $order instanceof WC_Order ) {
		return;
	}
	$status = $order->get_status();
	$idx    = carmilla_tracking_index( $status );
	$steps  = carmilla_tracking_steps();

	echo '<section class="cb-track card card--pad">';
	echo '<h3 class="t-title-sm">' . esc_html__( 'رهگیری سفارش', 'carmilla' ) . '</h3>';

	if ( -1 === $idx ) {
		echo '<p class="cb-track__cancel">' . esc_html__( 'این سفارش لغو شده است.', 'carmilla' ) . '</p>';
	} else {
		echo '<ol class="cb-track__line">';
		$i = 0;
		foreach ( $steps as $key => $label ) {
			$done   = $idx >= $i;
			$active = $idx === $i;
			$cls    = 'cb-track__step' . ( $done ? ' is-done' : '' ) . ( $active ? ' is-active' : '' );
			echo '<li class="' . esc_attr( $cls ) . '">';
			echo '<span class="cb-track__dot">' . ( $done ? carmilla_icon( 'check', 13 ) : '' ) . '</span>';
			echo '<span class="cb-track__label">' . esc_html( $label ) . '</span>';
			echo '</li>';
			$i++;
		}
		echo '</ol>';
	}

	// Tracking / postal code, if the shop set one on the order.
	$code = $order->get_meta( '_tracking_number' );
	if ( ! $code ) {
		$code = $order->get_meta( 'cb_tracking_code' );
	}
	if ( $code ) {
		echo '<p class="cb-track__code"><span>' . esc_html__( 'کد رهگیری پستی:', 'carmilla' ) . '</span> <strong>' . esc_html( carmilla_to_persian_digits( $code ) ) . '</strong></p>';
	} elseif ( -1 !== $idx && $idx < 2 ) {
		echo '<p class="t-body-sm t-muted">' . esc_html__( 'کد رهگیری پس از ارسال سفارش نمایش داده می‌شود.', 'carmilla' ) . '</p>';
	}
	echo '</section>';
}, 20 );

/* =========================================================================
 * 2) Return / exchange requests (← ReturnRequestScreen)
 *    Stored as CPT cb_return so staff can manage them from wp-admin.
 * ====================================================================== */

add_action( 'init', function () {
	register_post_type( 'cb_return', array(
		'label'           => __( 'درخواست‌های مرجوعی', 'carmilla' ),
		'public'          => false,
		'show_ui'         => true,
		'show_in_menu'    => true,
		'menu_icon'       => 'dashicons-image-rotate',
		'supports'        => array( 'title', 'editor' ),
		'capability_type' => 'post',
		'map_meta_cap'    => true,
	) );
} );

function carmilla_return_status_labels() {
	return array(
		'PENDING'   => __( 'در انتظار بررسی', 'carmilla' ),
		'APPROVED'  => __( 'تاییدشده', 'carmilla' ),
		'REJECTED'  => __( 'ردشده', 'carmilla' ),
		'COMPLETED' => __( 'تکمیل‌شده', 'carmilla' ),
	);
}

function carmilla_return_dto( $post ) {
	$labels = carmilla_return_status_labels();
	$status = get_post_meta( $post->ID, 'cb_status', true ) ?: 'PENDING';
	return array(
		'id'          => $post->ID,
		'itemTitle'   => get_the_title( $post ),
		'type'        => get_post_meta( $post->ID, 'cb_type', true ) ?: 'RETURN',
		'status'      => $status,
		'statusLabel' => $labels[ $status ] ?? $status,
		'reason'      => $post->post_content,
		'adminNote'   => (string) get_post_meta( $post->ID, 'cb_admin_note', true ),
		'date'        => get_the_date( 'Y-m-d', $post ),
	);
}

add_action( 'rest_api_init', function () {
	register_rest_route( 'carmilla/v1', '/returns', array(
		array( 'methods' => 'GET', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_returns_get' ),
		array( 'methods' => 'POST', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_returns_post' ),
	) );
	register_rest_route( 'carmilla/v1', '/recurring', array(
		array( 'methods' => 'GET', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_recurring_get' ),
		array( 'methods' => 'POST', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_recurring_post' ),
	) );
	register_rest_route( 'carmilla/v1', '/recurring/(?P<id>\d+)/cancel', array(
		'methods' => 'POST', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_recurring_cancel',
	) );
} );

function carmilla_rest_returns_get() {
	$posts = get_posts( array(
		'post_type'      => 'cb_return',
		'post_status'    => 'publish',
		'posts_per_page' => 50,
		'author'         => get_current_user_id(),
	) );
	return rest_ensure_response( array_map( 'carmilla_return_dto', $posts ) );
}

function carmilla_rest_returns_post( WP_REST_Request $req ) {
	$title  = sanitize_text_field( (string) $req->get_param( 'itemTitle' ) );
	$type   = strtoupper( sanitize_text_field( (string) $req->get_param( 'type' ) ) );
	$type   = in_array( $type, array( 'RETURN', 'EXCHANGE' ), true ) ? $type : 'RETURN';
	$reason = trim( wp_strip_all_tags( (string) $req->get_param( 'reason' ) ) );
	if ( '' === $reason ) {
		return new WP_Error( 'validation', 'دلیل درخواست را بنویسید.', array( 'status' => 400 ) );
	}
	$order_id = (int) $req->get_param( 'orderId' );
	$id = wp_insert_post( array(
		'post_type'    => 'cb_return',
		'post_status'  => 'publish',
		'post_author'  => get_current_user_id(),
		'post_title'   => $title ?: sprintf( __( 'درخواست مرجوعی #%d', 'carmilla' ), $order_id ),
		'post_content' => $reason,
	) );
	if ( is_wp_error( $id ) || ! $id ) {
		return new WP_Error( 'create_failed', 'ثبت نشد.', array( 'status' => 400 ) );
	}
	update_post_meta( $id, 'cb_type', $type );
	update_post_meta( $id, 'cb_status', 'PENDING' );
	update_post_meta( $id, 'cb_order_id', $order_id );
	return rest_ensure_response( carmilla_return_dto( get_post( $id ) ) );
}

/* =========================================================================
 * 3) Recurring orders (← RecurringOrdersScreen). Schedule list stored per user.
 * ====================================================================== */

function carmilla_rest_recurring_get() {
	$list = get_user_meta( get_current_user_id(), 'cb_recurring', true );
	$list = is_array( $list ) ? $list : array();
	return rest_ensure_response( array_values( array_filter( $list, function ( $r ) {
		return ! empty( $r['active'] );
	} ) ) );
}

function carmilla_rest_recurring_post( WP_REST_Request $req ) {
	$product_id = (int) $req->get_param( 'productId' );
	$qty        = max( 1, (int) $req->get_param( 'qty' ) );
	$interval   = max( 1, (int) $req->get_param( 'intervalDays' ) );
	if ( ! $product_id || ! function_exists( 'wc_get_product' ) || ! wc_get_product( $product_id ) ) {
		return new WP_Error( 'validation', 'محصول نامعتبر است.', array( 'status' => 400 ) );
	}
	$uid  = get_current_user_id();
	$list = get_user_meta( $uid, 'cb_recurring', true );
	$list = is_array( $list ) ? $list : array();
	$list[] = array(
		'id'           => time(),
		'productId'    => $product_id,
		'productName'  => get_the_title( $product_id ),
		'qty'          => $qty,
		'intervalDays' => $interval,
		'active'       => true,
		'nextRunAt'    => gmdate( 'Y-m-d', time() + $interval * DAY_IN_SECONDS ),
	);
	update_user_meta( $uid, 'cb_recurring', $list );
	return rest_ensure_response( array_values( array_filter( $list, function ( $r ) {
		return ! empty( $r['active'] );
	} ) ) );
}

function carmilla_rest_recurring_cancel( WP_REST_Request $req ) {
	$id   = (int) $req['id'];
	$uid  = get_current_user_id();
	$list = get_user_meta( $uid, 'cb_recurring', true );
	$list = is_array( $list ) ? $list : array();
	foreach ( $list as &$r ) {
		if ( (int) $r['id'] === $id ) {
			$r['active'] = false;
		}
	}
	unset( $r );
	update_user_meta( $uid, 'cb_recurring', $list );
	return rest_ensure_response( array_values( array_filter( $list, function ( $r ) {
		return ! empty( $r['active'] );
	} ) ) );
}

/* =========================================================================
 * 4) My-account endpoints + menu items
 * ====================================================================== */

add_action( 'init', function () {
	add_rewrite_endpoint( 'returns', EP_ROOT | EP_PAGES );
	add_rewrite_endpoint( 'recurring', EP_ROOT | EP_PAGES );
	add_rewrite_endpoint( 'membership', EP_ROOT | EP_PAGES );
	add_rewrite_endpoint( 'settings', EP_ROOT | EP_PAGES );
} );

add_filter( 'woocommerce_account_menu_items', function ( $items ) {
	$logout = isset( $items['customer-logout'] ) ? array( 'customer-logout' => $items['customer-logout'] ) : array();
	unset( $items['customer-logout'] );
	$items['returns']    = __( 'مرجوعی و تعویض', 'carmilla' );
	$items['recurring']  = __( 'خریدهای تکراری', 'carmilla' );
	$items['membership'] = __( 'عضویت ویژه', 'carmilla' );
	$items['settings']   = __( 'تنظیمات', 'carmilla' );
	return array_merge( $items, $logout );
} );

/* -------- Membership: VIP subscription paid from wallet (← MembershipScreen) -------- */

function carmilla_membership_price() {
	return (float) apply_filters( 'carmilla_membership_price', 200000 ); // toman / 30 days
}
function carmilla_membership_discount() {
	return (float) apply_filters( 'carmilla_membership_discount', 0.05 ); // 5%
}
function carmilla_membership_active( $user_id = 0 ) {
	$user_id = $user_id ?: get_current_user_id();
	$exp     = (int) get_user_meta( $user_id, 'cb_membership_expires', true );
	return $exp > time();
}

add_action( 'rest_api_init', function () {
	register_rest_route( 'carmilla/v1', '/membership/subscribe', array(
		'methods' => 'POST', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_membership_subscribe',
	) );
} );

function carmilla_rest_membership_subscribe() {
	$uid     = get_current_user_id();
	$price   = carmilla_membership_price();
	$balance = (float) get_user_meta( $uid, 'cb_wallet_balance', true );
	if ( $balance < $price ) {
		return new WP_Error( 'insufficient', 'موجودی کیف پول کافی نیست.', array( 'status' => 400 ) );
	}
	update_user_meta( $uid, 'cb_wallet_balance', $balance - $price );
	$current = max( time(), (int) get_user_meta( $uid, 'cb_membership_expires', true ) );
	update_user_meta( $uid, 'cb_membership_expires', $current + 30 * DAY_IN_SECONDS );
	return rest_ensure_response( array(
		'active'    => true,
		'expiresAt' => gmdate( 'Y-m-d', $current + 30 * DAY_IN_SECONDS ),
		'balance'   => $balance - $price,
	) );
}

add_action( 'woocommerce_account_membership_endpoint', function () {
	$uid    = get_current_user_id();
	$active = carmilla_membership_active( $uid );
	$exp    = (int) get_user_meta( $uid, 'cb_membership_expires', true );
	$price  = carmilla_membership_price();
	$pct    = (int) round( carmilla_membership_discount() * 100 );

	echo '<div id="cb-membership" class="cb-membership">';
	echo '<div class="card card--pad cb-membership__banner ' . ( $active ? 'is-active' : '' ) . '">';
	echo '<h3 class="t-title-sm">' . ( $active ? esc_html__( 'عضویتِ ویژه فعال است', 'carmilla' ) : esc_html__( 'عضویتِ ویژه فعال نیست', 'carmilla' ) ) . '</h3>';
	echo '<p class="t-body-sm">' . sprintf( esc_html__( '%d٪ تخفیفِ خودکار روی همه‌ی خریدها', 'carmilla' ), $pct );
	if ( $active && $exp ) {
		echo ' · ' . esc_html__( 'تا', 'carmilla' ) . ' ' . esc_html( carmilla_to_persian_digits( gmdate( 'Y-m-d', $exp ) ) );
	}
	echo '</p></div>';
	echo '<p class="t-body-sm t-muted" style="margin-block:var(--sp-md)">' . sprintf( esc_html__( 'هزینه‌ی هر ۳۰ روز: %s (از کیف پول کسر می‌شود)', 'carmilla' ), wp_kses_post( carmilla_price( $price ) ) ) . '</p>';
	echo '<button type="button" class="btn btn--primary cb-membership__btn" data-price="' . esc_attr( $price ) . '">' . ( $active ? esc_html__( 'تمدیدِ ۳۰ روزِ دیگر', 'carmilla' ) : esc_html__( 'فعال‌سازیِ عضویتِ ویژه', 'carmilla' ) ) . '</button>';
	echo '<p class="cb-membership__msg" role="status"></p>';
	echo '</div>';
} );

/** Apply the VIP auto-discount at checkout for active members. */
add_action( 'woocommerce_cart_calculate_fees', function ( $cart ) {
	if ( is_admin() && ! defined( 'DOING_AJAX' ) ) {
		return;
	}
	if ( ! carmilla_membership_active() ) {
		return;
	}
	$discount = $cart->get_subtotal() * carmilla_membership_discount();
	if ( $discount > 0 ) {
		$cart->add_fee( __( 'تخفیف عضویت ویژه', 'carmilla' ), -1 * $discount );
	}
} );

add_action( 'woocommerce_account_returns_endpoint', function () {
	echo '<div id="cb-returns" class="cb-returns"></div>';
} );

add_action( 'woocommerce_account_recurring_endpoint', function () {
	echo '<div id="cb-recurring" class="cb-recurring"></div>';
} );

add_action( 'woocommerce_account_settings_endpoint', function () {
	echo '<div id="cb-settings" class="cb-settings">';

	echo '<h3 class="t-title-sm">' . esc_html__( 'پوسته (روشن/تاریک)', 'carmilla' ) . '</h3>';
	echo '<div class="card card--pad cb-settings__group" data-setting="theme">';
	foreach ( array(
		'system' => __( 'پیش‌فرض سیستم', 'carmilla' ),
		'light'  => __( 'روشن', 'carmilla' ),
		'dark'   => __( 'تاریک', 'carmilla' ),
	) as $val => $label ) {
		echo '<label class="cb-settings__row"><input type="radio" name="cb-theme" value="' . esc_attr( $val ) . '"><span>' . esc_html( $label ) . '</span></label>';
	}
	echo '</div>';

	echo '<h3 class="t-title-sm" style="margin-block-start:var(--sp-lg)">' . esc_html__( 'زبان', 'carmilla' ) . '</h3>';
	echo '<div class="card card--pad cb-settings__group" data-setting="lang">';
	// Language is served by WordPress locale; offer quick links when a Polylang/WPML switcher exists,
	// otherwise show the active locale so the screen has parity with the app.
	$locale = get_locale();
	echo '<label class="cb-settings__row"><input type="radio" name="cb-lang" value="fa"' . checked( strpos( $locale, 'fa' ) === 0, true, false ) . '><span>' . esc_html__( 'فارسی', 'carmilla' ) . '</span></label>';
	echo '<label class="cb-settings__row"><input type="radio" name="cb-lang" value="en"' . checked( strpos( $locale, 'en' ) === 0, true, false ) . '><span>English</span></label>';
	echo '<p class="t-body-sm t-muted" style="margin-block-start:var(--sp-sm)">' . esc_html__( 'برای تغییر کاملِ زبانِ سایت، از افزونه‌ی چندزبانه (Polylang/WPML) استفاده کنید.', 'carmilla' ) . '</p>';
	echo '</div>';

	echo '</div>';
} );
