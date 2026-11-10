<?php
/** The core must not offer its unsafe wallet gateway to WooCommerce. */
require_once dirname( __DIR__ ) . '/inc/Adapter/WooCompat.php';

$other = array( 'Other_Gateway' );
$actual = \Carmilla\Core\Adapter\WooCompat::register_gateways( $other );
if ( $actual !== $other || class_exists( '\\Carmilla\\Core\\Payment\\WC_Gateway_Carmilla_Wallet', false ) ) {
    fwrite( STDERR, "FAIL wallet gateway remains registered\n" );
    exit( 1 );
}
echo "PASS wallet gateway absent; other gateways preserved\n";
