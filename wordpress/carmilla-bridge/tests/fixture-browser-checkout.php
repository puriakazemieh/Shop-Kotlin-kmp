<?php
/** Run by WP-CLI exclusively inside a disposable synthetic container. */
if ( getenv( 'CARMILLA_BROWSER_QA_ONLY' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) getenv( 'CARMILLA_REFERENCE_PREFIX' ) ) ) { fwrite( STDERR, "Disposable fixture required.\n" ); exit( 2 ); }
if ( ! class_exists( 'WooCommerce' ) || WC_VERSION !== '8.5.2' ) { fwrite( STDERR, "Expected synthetic WooCommerce unavailable.\n" ); exit( 2 ); }
try {
	update_option( 'woocommerce_default_country', 'US:CA' );
	update_option( 'woocommerce_currency', 'USD' );
	update_option( 'woocommerce_calc_taxes', 'no' );
	update_option( 'woocommerce_enable_guest_checkout', 'yes' );
	update_option( 'woocommerce_cod_settings', array( 'enabled' => 'yes', 'title' => 'Synthetic offline payment', 'description' => 'Synthetic fixture only', 'enable_for_methods' => array(), 'enable_for_virtual' => 'yes' ) );
	$cart = wp_insert_post( array( 'post_title' => 'Synthetic Cart', 'post_name' => 'synthetic-cart', 'post_status' => 'publish', 'post_type' => 'page', 'post_content' => '[woocommerce_cart]' ), true );
	$checkout = wp_insert_post( array( 'post_title' => 'Synthetic Checkout', 'post_name' => 'synthetic-checkout', 'post_status' => 'publish', 'post_type' => 'page', 'post_content' => '[woocommerce_checkout]' ), true );
	if ( is_wp_error( $cart ) || is_wp_error( $checkout ) || ! $cart || ! $checkout ) { throw new RuntimeException(); }
	update_option( 'woocommerce_cart_page_id', $cart ); update_option( 'woocommerce_checkout_page_id', $checkout );
	$product = new WC_Product_Simple(); $product->set_name( 'Synthetic QA Product' ); $product->set_regular_price( '100' ); $product->set_virtual( true ); $product->set_status( 'publish' ); $id = $product->save();
	if ( ! $id ) { throw new RuntimeException(); }
	echo 'PRODUCT_ID=' . (int) $id . "\nCART_ID=" . (int) $cart . "\nCHECKOUT_ID=" . (int) $checkout . "\nPASS synthetic browser fixture setup\n";
} catch ( Throwable $e ) { fwrite( STDERR, 'Fixture setup failed: ' . get_class( $e ) . "\n" ); exit( 1 ); }
