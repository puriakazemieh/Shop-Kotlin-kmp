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

/* -------------------------------------------------------------------------
 * Custom My-Account tabs: wallet + referral (native WooCommerce endpoints).
 * ---------------------------------------------------------------------- */

add_action( 'init', function () {
	add_rewrite_endpoint( 'wallet', EP_ROOT | EP_PAGES );
	add_rewrite_endpoint( 'referral', EP_ROOT | EP_PAGES );
} );

add_filter( 'woocommerce_account_menu_items', function ( $items ) {
	$logout = isset( $items['customer-logout'] ) ? array( 'customer-logout' => $items['customer-logout'] ) : array();
	unset( $items['customer-logout'] );
	$items['wallet']   = __( 'کیف پول', 'carmilla' );
	$items['referral'] = __( 'معرفی به دوستان', 'carmilla' );
	return array_merge( $items, $logout );
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
