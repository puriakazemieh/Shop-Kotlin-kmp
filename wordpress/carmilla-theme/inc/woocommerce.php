<?php
/**
 * WooCommerce integration: column counts, wrappers, and content widths that
 * match the app (shop grid = wide, single product = readable, cart = medium).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/** Shop loop columns follow the adaptive grid (2/3/4) via CSS; tell WC to output 4. */
add_filter( 'loop_shop_columns', function () {
	return 4;
} );

/** Products per page. */
add_filter( 'loop_shop_per_page', function () {
	return 12;
} );

/**
 * Replace WooCommerce's default page wrappers with theme containers.
 * Single product uses the readable width; archives/cart use wide/medium.
 */
remove_action( 'woocommerce_before_main_content', 'woocommerce_output_content_wrapper', 10 );
remove_action( 'woocommerce_after_main_content', 'woocommerce_output_content_wrapper_end', 10 );

add_action( 'woocommerce_before_main_content', function () {
	$class = is_product() ? 'container container--readable' : 'container container--wide';
	echo '<main class="' . esc_attr( $class ) . ' wc-main" style="padding-block: var(--sp-xl);">';
}, 10 );

add_action( 'woocommerce_after_main_content', function () {
	echo '</main>';
}, 10 );

/** Remove the default sidebar (theme is single-column, app-like). */
remove_action( 'woocommerce_sidebar', 'woocommerce_get_sidebar', 10 );

/** Cart/checkout/account read best at medium width — add a class hook via body. */
add_filter( 'body_class', function ( $classes ) {
	if ( function_exists( 'is_cart' ) && ( is_cart() || is_checkout() || is_account_page() ) ) {
		$classes[] = 'cw-medium';
	}
	return $classes;
} );

/**
 * Order status → [label, fg, bg] chip on the design tokens. Shared by the
 * my-account orders list and the invoice view.
 */
function carmilla_acct_status_chip( $status ) {
	switch ( $status ) {
		case 'completed':
			return array( __( 'تحویل شد', 'carmilla' ), 'var(--ok)', 'color-mix(in srgb, var(--ok) 12%, transparent)' );
		case 'processing':
			return array( __( 'در حال پردازش', 'carmilla' ), 'var(--accent)', 'var(--accent-soft)' );
		case 'on-hold':
		case 'pending':
			return array( __( 'در انتظار پرداخت', 'carmilla' ), 'var(--gold)', 'color-mix(in srgb, var(--gold) 14%, transparent)' );
		case 'cancelled':
		case 'failed':
			return array( __( 'لغو شده', 'carmilla' ), 'var(--sale)', 'color-mix(in srgb, var(--sale) 12%, transparent)' );
		case 'refunded':
			return array( __( 'مرجوع شده', 'carmilla' ), 'var(--ink-soft)', 'var(--surface-2)' );
		default:
			return array( wc_get_order_status_name( $status ), 'var(--ink-soft)', 'var(--surface-2)' );
	}
}

/* -------------------------------------------------------------------------
 * Custom My-Account tabs: wallet + referral (native WooCommerce endpoints).
 * ---------------------------------------------------------------------- */

add_action( 'init', function () {
	add_rewrite_endpoint( 'wallet', EP_ROOT | EP_PAGES );
	add_rewrite_endpoint( 'referral', EP_ROOT | EP_PAGES );
	add_rewrite_endpoint( 'club', EP_ROOT | EP_PAGES );
} );

add_filter( 'woocommerce_account_menu_items', function ( $items ) {
	$logout = isset( $items['customer-logout'] ) ? array( 'customer-logout' => $items['customer-logout'] ) : array();
	unset( $items['customer-logout'] );
	$items['club']     = __( 'باشگاه مشتریان', 'carmilla' );
	$items['wallet']   = __( 'کیف پول', 'carmilla' );
	$items['referral'] = __( 'معرفی به دوستان', 'carmilla' );
	return array_merge( $items, $logout );
} );

/** Tier ladder by lifetime spend (toman). Returns [tier, next_threshold]. */
function carmilla_club_tiers() {
	return array(
		array( 'slug' => 'bronze', 'name' => __( 'برنزی', 'carmilla' ), 'min' => 0 ),
		array( 'slug' => 'silver', 'name' => __( 'نقره‌ای', 'carmilla' ), 'min' => 5000000 ),
		array( 'slug' => 'gold',   'name' => __( 'طلایی', 'carmilla' ),  'min' => 20000000 ),
		array( 'slug' => 'vip',    'name' => __( 'الماس', 'carmilla' ),  'min' => 50000000 ),
	);
}

add_action( 'woocommerce_account_club_endpoint', function () {
	$uid   = get_current_user_id();
	$spent = (float) wc_get_customer_total_spent( $uid );
	$points = (int) floor( $spent / 10000 ); // 1 point per 10,000 toman
	$tiers = carmilla_club_tiers();
	$current = $tiers[0];
	$next    = null;
	foreach ( $tiers as $i => $t ) {
		if ( $spent >= $t['min'] ) {
			$current = $t;
			$next    = isset( $tiers[ $i + 1 ] ) ? $tiers[ $i + 1 ] : null;
		}
	}
	echo '<div class="card card--pad cb-club">';
	echo '<span class="badge badge--rating cb-club__tier">' . esc_html( $current['name'] ) . '</span>';
	echo '<h2 class="t-headline" style="margin:8px 0">' . esc_html( carmilla_to_persian_digits( number_format_i18n( $points ) ) ) . ' ' . esc_html__( 'امتیاز', 'carmilla' ) . '</h2>';
	echo '<p class="t-body-sm t-muted">' . esc_html__( 'مجموع خرید شما:', 'carmilla' ) . ' ' . wp_kses_post( carmilla_price( $spent ) ) . '</p>';
	if ( $next ) {
		$span = max( 1, $next['min'] - $current['min'] );
		$pct  = min( 100, round( ( $spent - $current['min'] ) * 100 / $span ) );
		echo '<div class="cb-club__bar"><span class="cb-club__fill" style="width:' . esc_attr( $pct ) . '%"></span></div>';
		$remain = max( 0, $next['min'] - $spent );
		echo '<p class="t-body-sm">' . sprintf( esc_html__( 'تا سطح %1$s: %2$s دیگر', 'carmilla' ), esc_html( $next['name'] ), wp_kses_post( carmilla_price( $remain ) ) ) . '</p>';
	} else {
		echo '<p class="t-body-sm">' . esc_html__( 'به بالاترین سطح باشگاه رسیده‌اید!', 'carmilla' ) . '</p>';
	}
	echo '</div>';
} );

add_action( 'woocommerce_account_wallet_endpoint', function () {
	$balance = (float) get_user_meta( get_current_user_id(), 'cb_wallet_balance', true );
	echo '<div class="card card--pad">';
	echo '<p class="t-caption">' . esc_html__( 'موجودی کیف پول', 'carmilla' ) . '</p>';
	echo '<h2 class="t-headline" style="margin:4px 0">' . esc_html( carmilla_price( $balance ) ) . '</h2>';
	echo '<p class="t-body-sm t-muted">' . esc_html__( 'کیف پول هنگام پرداخت سفارش و بازگشت وجه به‌روزرسانی می‌شود.', 'carmilla' ) . '</p>';
	echo '</div>';
} );

add_action( 'woocommerce_account_referral_endpoint', function () {
	$uid  = get_current_user_id();
	$code = 'CAR' . $uid;
	$link = add_query_arg( 'ref', $uid, home_url( '/' ) );
	echo '<div class="card card--pad">';
	echo '<p class="t-body">' . esc_html__( 'کد معرفی شما:', 'carmilla' ) . ' <strong>' . esc_html( $code ) . '</strong></p>';
	echo '<p class="t-body-sm t-muted">' . esc_html__( 'این لینک را با دوستانتان به اشتراک بگذارید:', 'carmilla' ) . '</p>';
	echo '<input type="text" readonly value="' . esc_attr( $link ) . '" onclick="this.select()">';
	echo '</div>';
} );
