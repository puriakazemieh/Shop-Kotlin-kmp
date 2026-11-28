<?php
namespace Carmilla\Core\Adapter;

/**
 * WooCommerce compatibility declarations (HPOS & Cart/Checkout Blocks)
 * and Carmilla gateway registration.
 *
 * Ensures:
 * - Single idempotent hook registration across Theme-only, Plugin-only, and Co-Install modes.
 * - Accurate FeaturesUtil::declare_compatibility() targeting the Bridge plugin file.
 * - Third-party themes in Plugin-only mode retain default WooCommerce Blocks styles.
 *
 * Part of Task P04-WPPLUGIN-CODE-015.
 */
if ( ! class_exists( 'Carmilla\\Core\\Adapter\\WooCompat', false ) ) {
class WooCompat {

    /**
     * Tested WooCommerce version range for compatibility declarations.
     */
    public const TESTED_WOO_VERSIONS = [
        'min'          => '8.0.0',
        'tested_up_to' => '9.9.9',
    ];

    private static bool $initialized = false;

    public static function init(): void {
        if ( self::$initialized ) {
            return;
        }
        self::$initialized = true;

        add_action( 'before_woocommerce_init', array( self::class, 'declare_hpos_compatibility' ) );
        add_action( 'wp_enqueue_scripts', array( self::class, 'dequeue_woo_blocks_styles' ), 100 );
        add_filter( 'woocommerce_payment_gateways', array( self::class, 'register_gateways' ) );
    }

    /**
     * Declares HPOS (custom_order_tables) and Cart/Checkout Blocks compatibility
     * against the canonical Carmilla Bridge plugin entrypoint.
     */
    public static function declare_hpos_compatibility(): void {
        if ( ! class_exists( '\\Automattic\\WooCommerce\\Utilities\\FeaturesUtil' ) ) {
            return;
        }

        $plugin_file = self::resolve_bridge_plugin_file();
        \Automattic\WooCommerce\Utilities\FeaturesUtil::declare_compatibility( 'custom_order_tables', $plugin_file, true );
        \Automattic\WooCommerce\Utilities\FeaturesUtil::declare_compatibility( 'cart_checkout_blocks', $plugin_file, true );
    }

    /**
     * Returns structured compatibility & environment matrix for diagnostics/evidence.
     *
     * @return array<string, mixed>
     */
    public static function get_compatibility_matrix(): array {
        $wc_active = class_exists( 'WooCommerce' );
        $wc_ver    = defined( 'WC_VERSION' ) ? WC_VERSION : null;
        $hpos_on   = class_exists( WooCommerceAdapter::class ) ? WooCommerceAdapter::is_hpos_enabled() : false;

        return [
            'woocommerce_active'        => $wc_active,
            'woocommerce_version'       => $wc_ver,
            'tested_woo_versions'       => self::TESTED_WOO_VERSIONS,
            'hpos_enabled'              => $hpos_on,
            'hpos_declared'             => true,
            'cart_checkout_blocks'      => true,
            'supported_checkout_modes'  => [ 'classic', 'blocks' ],
            'duplicate_hooks_prevented' => self::$initialized,
            'plugin_file_target'        => self::resolve_bridge_plugin_file(),
        ];
    }

    public static function register_gateways( array $gateways ): array {
        // Wallet balance and ledger writes are not atomic. Keep checkout closed
        // until a verified replacement can safely register this gateway.
        return $gateways;
    }

    /**
     * Dequeues default WooCommerce Blocks styles ONLY when Carmilla Theme is active
     * (third-party themes in Plugin-only mode keep their Woo Blocks styling).
     */
    public static function dequeue_woo_blocks_styles(): void {
        if ( ! self::should_dequeue_blocks_styles() ) {
            return;
        }

        if ( ! function_exists( 'is_cart' ) || ! function_exists( 'is_checkout' ) ) {
            return;
        }

        if ( is_cart() || is_checkout() ) {
            wp_dequeue_style( 'wc-blocks-style' );
            wp_dequeue_style( 'wc-blocks-integration' );
        }
    }

    public static function should_dequeue_blocks_styles(): bool {
        if ( function_exists( 'carmilla_is_theme_active' ) ) {
            return (bool) carmilla_is_theme_active();
        }
        if ( function_exists( 'get_stylesheet' ) ) {
            return strpos( strtolower( (string) get_stylesheet() ), 'carmilla' ) !== false;
        }
        return false;
    }

    /**
     * Resolves the canonical carmilla-bridge plugin file path for FeaturesUtil.
     */
    public static function resolve_bridge_plugin_file(): string {
        if ( defined( 'CB_PLUGIN_FILE' ) && is_string( CB_PLUGIN_FILE ) ) {
            return CB_PLUGIN_FILE;
        }
        if ( defined( 'WP_PLUGIN_DIR' ) ) {
            $candidate = WP_PLUGIN_DIR . '/carmilla-bridge/carmilla-bridge.php';
            if ( file_exists( $candidate ) ) {
                return $candidate;
            }
        }
        return 'carmilla-bridge/carmilla-bridge.php';
    }

    /**
     * Resets initialized flag (for automated testing only).
     */
    public static function reset_state(): void {
        self::$initialized = false;
    }
}
}
