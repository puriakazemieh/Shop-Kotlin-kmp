<?php
namespace Carmilla\Core\Adapter;

/**
 * WooCommerce compatibility declarations and Carmilla gateway registration.
 */
class WooCompat {

    public static function init(): void {
        add_action( 'before_woocommerce_init', array( self::class, 'declare_hpos_compatibility' ) );
        add_action( 'wp_enqueue_scripts', array( self::class, 'dequeue_woo_blocks_styles' ), 100 );
        add_filter( 'woocommerce_payment_gateways', array( self::class, 'register_gateways' ) );
    }

    public static function declare_hpos_compatibility(): void {
        if ( ! class_exists( '\\Automattic\\WooCommerce\\Utilities\\FeaturesUtil' ) ) {
            return;
        }

        \Automattic\WooCommerce\Utilities\FeaturesUtil::declare_compatibility( 'custom_order_tables', __FILE__, true );
        \Automattic\WooCommerce\Utilities\FeaturesUtil::declare_compatibility( 'cart_checkout_blocks', __FILE__, true );
    }

    public static function register_gateways( array $gateways ): array {
        $wallet_service = dirname( __DIR__ ) . '/Payment/WalletService.php';
        $wallet_gateway = dirname( __DIR__ ) . '/Payment/WC_Gateway_Carmilla_Wallet.php';
        if ( ! file_exists( $wallet_service ) || ! file_exists( $wallet_gateway ) ) {
            return $gateways;
        }

        require_once $wallet_service;
        require_once $wallet_gateway;
        $gateways[] = '\\Carmilla\\Core\\Payment\\WC_Gateway_Carmilla_Wallet';

        return $gateways;
    }

    public static function dequeue_woo_blocks_styles(): void {
        if ( ! function_exists( 'is_cart' ) || ! function_exists( 'is_checkout' ) ) {
            return;
        }

        if ( is_cart() || is_checkout() ) {
            wp_dequeue_style( 'wc-blocks-style' );
            wp_dequeue_style( 'wc-blocks-integration' );
        }
    }
}
