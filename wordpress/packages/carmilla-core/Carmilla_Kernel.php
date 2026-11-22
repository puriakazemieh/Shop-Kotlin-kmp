<?php

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

if ( ! class_exists( 'Carmilla_Kernel', false ) ) {
    require_once __DIR__ . '/inc/Catalog/EntitlementSchema.php';
    require_once __DIR__ . '/inc/Catalog/EntitlementClaims.php';
    require_once __DIR__ . '/inc/Catalog/DependencyResolver.php';
    require_once __DIR__ . '/inc/Migration/SchemaRunner.php';
    require_once __DIR__ . '/inc/Entitlement/EntitlementResolver.php';
    require_once __DIR__ . '/inc/Adapter/WooCommerceAdapter.php';
    require_once __DIR__ . '/inc/Rest/RestInfrastructure.php';
    require_once __DIR__ . '/inc/Capabilities/CapabilityRegistry.php';
    require_once __DIR__ . '/inc/Health/HealthChecker.php';
    require_once __DIR__ . '/inc/Settings/MessageSettings.php';
    require_once __DIR__ . '/inc/Messaging/MessageSender.php';
    require_once __DIR__ . '/inc/Import/Carmilla_Importer.php';

/**
 * Carmilla Shared Kernel
 * Used by both Carmilla Theme and Carmilla Bridge to initialize
 * the base schema, capabilities, and migrations without duplicating boots.
 */
class Carmilla_Kernel {
    public const VERSION = '1.0.0';
    private static $booted_by = [];
    private static $version = '1.0.0';

    /**
     * Boot the shared kernel. 
     * Can be called by 'plugin' or 'theme'.
     */
    public static function boot(string $context, string $product_version) {
        self::$booted_by[$context] = $product_version;

        // Register boot checkpoint
        if (\Carmilla\Core\Migration\SchemaRunner::register_checkpoint("boot_kernel")) {
            // First time this request boots the kernel
            self::init_core();
        }
    }

    private static function init_core() {
        // Run core migrations
        \Carmilla\Core\Migration\SchemaRunner::run_migrations();
        
        // Register custom capabilities to administrator role
        \Carmilla\Core\Capabilities\CapabilityRegistry::register_capabilities();

        // Messaging settings are shared by the Theme and Bridge.  Register the
        // schema during the canonical Kernel boot so either host can save the
        // same site-owned option.
        ( new \Carmilla\Core\Settings\MessageSettings() )->init();
        \Carmilla\Core\Messaging\MessageSender::init();

        // Initialize Entitlements and Claims
        add_action('init', [self::class, 'resolve_features']);
        // WooCommerce Compatibility
        require_once __DIR__ . '/inc/Adapter/WooCompat.php';
        \Carmilla\Core\Adapter\WooCompat::init();

        // Register admin page
        require_once __DIR__ . '/inc/Settings/AdminSettingsPage.php';
        \Carmilla\Core\Settings\AdminSettingsPage::init();

    }

    public static function resolve_features() {
        // Retrieve fixtures or actual claims here
        $claims = \Carmilla\Core\Catalog\EntitlementClaims::get_fully_unlocked_fixture();
        $active_features = \Carmilla\Core\Entitlement\EntitlementResolver::resolve($claims);

        // Store active features in memory or global for this request
        global $carmilla_active_features;
        $carmilla_active_features = $active_features;
    }

    /**
     * Returns all active features resolved by the kernel for this request.
     *
     * @return array<string, bool>
     */
    public static function get_active_features(): array {
        global $carmilla_active_features;
        if ($carmilla_active_features === null) {
            self::resolve_features();
        }
        return $carmilla_active_features ?? [];
    }

    /**
     * Checks whether a specific feature (by canonical id or alias) is currently active.
     *
     * @param string $feature_id
     * @return bool
     */
    public static function is_feature_active(string $feature_id): bool {
        $active = self::get_active_features();
        $canonical = class_exists('\Carmilla\Core\Catalog\EntitlementSchema')
            ? \Carmilla\Core\Catalog\EntitlementSchema::resolve_feature_id($feature_id)
            : $feature_id;
        return !empty($active[$canonical]);
    }

    public static function get_boot_contexts() {
        return self::$booted_by;
    }

    /**
     * Checks if the kernel has been booted by any host.
     *
     * @return bool
     */
    public static function is_booted(): bool {
        return !empty(self::$booted_by);
    }

    /**
     * Checks if the kernel was booted by a specific host context ('theme' or 'plugin').
     *
     * @param string $context
     * @return bool
     */
    public static function is_booted_by(string $context): bool {
        return isset(self::$booted_by[$context]);
    }

    /**
     * Resets kernel boot state and checkpoints (for testing and isolation).
     *
     * @return void
     */
    public static function reset_state(): void {
        self::$booted_by = [];
        global $carmilla_active_features;
        $carmilla_active_features = null;
        if (class_exists('\Carmilla\Core\Migration\SchemaRunner')) {
            \Carmilla\Core\Migration\SchemaRunner::reset_checkpoints();
        }
        if (class_exists('\Carmilla\Core\Entitlement\EntitlementResolver')) {
            \Carmilla\Core\Entitlement\EntitlementResolver::reset_quota_cache();
        }
    }
}
}
