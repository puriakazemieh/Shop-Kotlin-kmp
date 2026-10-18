<?php
/** Executed by WP CLI inside a disposable, synthetic WordPress installation. */
if ( getenv( 'CARMILLA_REFERENCE_DISPOSABLE' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) getenv( 'CARMILLA_REFERENCE_PREFIX' ) ) ) { fwrite( STDERR, "Disposable fixture required.\n" ); exit( 2 ); }
$checks = 0; $failures = 0;
$check = static function ( bool $ok, string $name ) use ( &$checks, &$failures ) { ++$checks; if ( ! $ok ) { ++$failures; } echo ( $ok ? 'PASS ' : 'FAIL ' ) . $name . "\n"; };
$check( class_exists( 'WooCommerce' ) && defined( 'WC_VERSION' ) && WC_VERSION === '8.5.2', 'expected WooCommerce version is loaded' );
$check( class_exists( 'Carmilla_Kernel' ) && get_stylesheet() === 'carmilla-theme' && is_plugin_active( 'carmilla-bridge/carmilla-bridge.php' ), 'Carmilla theme and Bridge active with shared core' );
if ( ! class_exists( 'WooCommerce' ) ) { fwrite( STDERR, "WooCommerce unavailable.\n" ); exit( 1 ); }
try {
	WC()->initialize_session(); WC()->initialize_cart();
	$product = new WC_Product_Simple(); $product->set_name( 'Synthetic QA product' ); $product->set_regular_price( '100000' ); $product->set_virtual( true ); $product->set_status( 'publish' );
	$id = $product->save();
	$check( $id > 0 && wc_get_product( $id ) instanceof WC_Product, 'synthetic product persisted' );
	$key = WC()->cart->add_to_cart( $id, 2 ); WC()->cart->calculate_totals();
	$check( is_string( $key ) && $key !== '' && WC()->cart->get_cart_contents_count() === 2 && (float) WC()->cart->get_subtotal() === 200000.0, 'synthetic cart quantity and subtotal correct' );
	$request = new WP_REST_Request( 'GET', '/wc/store/v1/cart' );
	$response = rest_do_request( $request );
	$check( $response->get_status() === 200 && is_array( $response->get_data() ), 'Woo Store API cart route returns structured response' );
	$request = new WP_REST_Request( 'GET', '/carmilla/v1/client-manifest' );
	$response = rest_do_request( $request );
	$check( $response->get_status() === 200 && is_array( $response->get_data() ), 'Carmilla manifest REST route returns structured response' );
	$request = new WP_REST_Request( 'GET', '/carmilla/v1/api/products' );
	$response = rest_do_request( $request ); $json = wp_json_encode( $response->get_data() );
	$check( $response->get_status() === 200 && is_string( $json ) && strpos( $json, 'Synthetic QA product' ) !== false, 'Carmilla catalog REST route returns the synthetic product' );
	global $wp_version;
	echo 'Observed WP ' . $wp_version . ' PHP ' . PHP_VERSION . ' Woo ' . WC_VERSION . "\n";
	echo "Checks: {$checks}; failures: {$failures}.\n";
	if ( ! $failures ) { echo "PASS disposable Woo reference smoke\n"; }
	exit( $failures ? 1 : 0 );
} catch ( Throwable $e ) { fwrite( STDERR, 'Reference smoke failed: ' . get_class( $e ) . "\n" ); exit( 1 ); }
