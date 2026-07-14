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
