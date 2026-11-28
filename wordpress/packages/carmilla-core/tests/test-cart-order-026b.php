<?php
/**
 * Real WordPress Verification for P04-WORDPRESS-CODE-026B
 * Shared Cart & Order Service (HPOS-compatible, single write/callback, capability gate).
 *
 * Run inside WordPress container:
 *   php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-cart-order-026b.php
 */

$wp_load = '/var/www/html/wp-load.php';
if ( ! file_exists( $wp_load ) ) {
	fwrite( STDERR, "wp-load.php not found\n" );
	exit( 1 );
}
require_once $wp_load;

use Carmilla\Core\Adapter\WooCommerceAdapter;
use Carmilla\Core\Commerce\CartOrderService;

$passed = 0;
$failed = 0;

function assert_026b( bool $cond, string $label ): void {
	global $passed, $failed;
	if ( $cond ) {
		++$passed;
	} else {
		++$failed;
		fwrite( STDERR, "FAIL: {$label}\n" );
	}
}

$orig_features = get_option( 'cb_manifest_features', null );
update_option( 'cb_manifest_features', array(
	'theme.base'    => true,
	'commerce.core' => true,
) );
Carmilla_Kernel::resolve_features();

$user_id = username_exists( 'qa_cart_user' );
if ( ! $user_id ) {
	$user_id = wp_create_user( 'qa_cart_user', 'Pass123456!', 'qa_cart_user@example.com' );
}

try {
	$server = rest_get_server();
	$routes = $server->get_routes();

	// 1. Singular REST routes registered (zero duplicate callbacks)
	assert_026b( ! empty( $routes['/carmilla/v1/api/cart'] ), 'REST route /carmilla/v1/api/cart registered' );
	assert_026b( ! empty( $routes['/carmilla/v1/api/orders'] ), 'REST route /carmilla/v1/api/orders registered' );
	assert_026b( count( $routes['/carmilla/v1/api/cart'] ) === 2, 'Single GET+DELETE handler pair on /api/cart (no duplicate controller)' );
	assert_026b( count( $routes['/carmilla/v1/api/orders'] ) === 2, 'Single GET+POST handler pair on /api/orders (no duplicate controller)' );

	// 2. Unauthenticated blocked (401/403)
	wp_set_current_user( 0 );
	$req_unauth_cart = new WP_REST_Request( 'GET', '/carmilla/v1/api/cart' );
	$res_unauth_cart = $server->dispatch( $req_unauth_cart );
	assert_026b( in_array( $res_unauth_cart->get_status(), array( 401, 403 ), true ), 'Unauthenticated blocked on /api/cart' );

	$req_unauth_orders = new WP_REST_Request( 'GET', '/carmilla/v1/api/orders' );
	$res_unauth_orders = $server->dispatch( $req_unauth_orders );
	assert_026b( in_array( $res_unauth_orders->get_status(), array( 401, 403 ), true ), 'Unauthenticated blocked on /api/orders' );

	// 3. Disabled commerce.core closes cart & order routes (403 FEATURE_DISABLED) and shortcodes
	wp_set_current_user( $user_id );
	update_option( 'cb_manifest_features', array(
		'theme.base'    => true,
		'commerce.core' => false,
	) );
	Carmilla_Kernel::resolve_features();

	$req_gated_cart = new WP_REST_Request( 'GET', '/carmilla/v1/api/cart' );
	$res_gated_cart = $server->dispatch( $req_gated_cart );
	assert_026b( 403 === $res_gated_cart->get_status(), 'Disabled commerce.core returns 403 on /api/cart' );

	$req_gated_orders = new WP_REST_Request( 'POST', '/carmilla/v1/api/orders' );
	$res_gated_orders = $server->dispatch( $req_gated_orders );
	assert_026b( 403 === $res_gated_orders->get_status(), 'Disabled commerce.core returns 403 on POST /api/orders' );

	$sc_cart_off = do_shortcode( '[carmilla_cart]' );
	assert_026b( false !== strpos( $sc_cart_off, 'cart-disabled' ), '[carmilla_cart] closes when commerce.core is disabled' );

	// Re-enable commerce.core
	update_option( 'cb_manifest_features', array(
		'theme.base'    => true,
		'commerce.core' => true,
	) );
	Carmilla_Kernel::resolve_features();

	// 4. Cart & Order behavior (Woo active vs Woo absent)
	$cart = CartOrderService::get_cart( $user_id );
	assert_026b( is_array( $cart ) && array_key_exists( 'subtotal', $cart ) && array_key_exists( 'total', $cart ), 'CartOrderService::get_cart returns canonical CartResponse' );

	$sc_cart = do_shortcode( '[carmilla_cart]' );
	assert_026b( false !== strpos( $sc_cart, 'data-carmilla-surface="cart"' ), '[carmilla_cart] renders canonical cart surface' );

	if ( WooCommerceAdapter::is_available() && function_exists( 'wc_create_order' ) ) {
		$orders = CartOrderService::list_orders( $user_id );
		assert_026b( is_array( $orders ), 'CartOrderService::list_orders succeeds with WooCommerce active' );
	} else {
		$orders = CartOrderService::list_orders( $user_id );
		assert_026b( is_wp_error( $orders ) && 'WOO_INACTIVE' === $orders->get_error_code(), 'CartOrderService::list_orders returns WOO_INACTIVE cleanly without Woo' );

		$add_res = CartOrderService::add_item( $user_id, array( 'productId' => 1, 'qty' => 1 ) );
		assert_026b( is_wp_error( $add_res ) && 'WOO_INACTIVE' === $add_res->get_error_code(), 'CartOrderService::add_item returns WOO_INACTIVE cleanly without Woo' );

		$sc_orders = do_shortcode( '[carmilla_orders]' );
		assert_026b( false !== strpos( $sc_orders, 'orders-prerequisite' ), '[carmilla_orders] renders prerequisite notice without fatal' );
	}
} finally {
	CartOrderService::clear_cart( $user_id );
	if ( null === $orig_features ) {
		delete_option( 'cb_manifest_features' );
	} else {
		update_option( 'cb_manifest_features', $orig_features );
	}
	wp_set_current_user( 0 );
}

echo "Cart & Order 026B QA: {$passed} passed, {$failed} failed.\n";
exit( $failed > 0 ? 1 : 0 );
