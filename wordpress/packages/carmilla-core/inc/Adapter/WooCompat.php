<?php
namespace Carmilla\Core\Adapter;

/**
 * WooCommerce Compatibility Layer
 * Handles HPOS and Cart/Checkout blocks declarations.
 */
class WooCompat {

    public static function init() {
        add_action('before_woocommerce_init', [self::class, 'declare_hpos_compatibility']);
        add_action('wp_enqueue_scripts', [self::class, 'dequeue_woo_blocks_styles'], 100);
    }

    /**
     * Declare High-Performance Order Storage (HPOS) compatibility.
     */
    public static function declare_hpos_compatibility() {
        if (class_exists('\Automattic\WooCommerce\Utilities\FeaturesUtil')) {
            \Automattic\WooCommerce\Utilities\FeaturesUtil::declare_compatibility('custom_order_tables', __FILE__, true);
            \Automattic\WooCommerce\Utilities\FeaturesUtil::declare_compatibility('cart_checkout_blocks', __FILE__, true);
        }
    }

    /**
     * Prevent WooCommerce block styles from disrupting the custom React/Native views
     * when loaded in headless or hybrid modes.
     */
    public static function dequeue_woo_blocks_styles() {
        if (is_cart() || is_checkout()) {
            wp_dequeue_style('wc-blocks-style');
            wp_dequeue_style('wc-blocks-integration');
        }
    }
}
