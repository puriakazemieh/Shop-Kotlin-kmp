<?php

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

if ( ! class_exists( 'Carmilla_Kernel', false ) ) {
    require_once __DIR__ . '/inc/Catalog/EntitlementSchema.php';
    require_once __DIR__ . '/inc/Catalog/EntitlementClaims.php';
    require_once __DIR__ . '/inc/Catalog/DependencyResolver.php';
    require_once __DIR__ . '/inc/Catalog/CapabilityResolver.php';
    require_once __DIR__ . '/inc/Migration/SchemaRunner.php';
    require_once __DIR__ . '/inc/Entitlement/EntitlementResolver.php';
    require_once __DIR__ . '/inc/Entitlement/FeatureToggleService.php';
    require_once __DIR__ . '/inc/Adapter/WooCommerceAdapter.php';
    require_once __DIR__ . '/inc/Rest/RestInfrastructure.php';
    require_once __DIR__ . '/inc/Capabilities/CapabilityRegistry.php';
    require_once __DIR__ . '/inc/Health/HealthChecker.php';
    require_once __DIR__ . '/inc/Lifecycle/LifecycleManager.php';
    require_once __DIR__ . '/inc/Privacy/PrivacyManager.php';
    require_once __DIR__ . '/inc/Settings/SettingsService.php';
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
        
        // Register custom capabilities and roles in kernel
        \Carmilla\Core\Capabilities\CapabilityRegistry::register_capabilities();
        \Carmilla\Core\Capabilities\CapabilityRegistry::register_roles();

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

        // Initialize unified REST infrastructure
        \Carmilla\Core\Rest\RestInfrastructure::init();

        // Register shared lifecycle handlers (theme switch + explicit cleanup)
        \Carmilla\Core\Lifecycle\LifecycleManager::register_theme_switch_handler();
        \Carmilla\Core\Lifecycle\LifecycleManager::register_admin_cleanup_handler();

        // Boot host-agnostic GDPR/Privacy exporter, eraser, and retention policy
        \Carmilla\Core\Privacy\PrivacyManager::boot();

        // Register shared purchased-features toggle panel handler
        \Carmilla\Core\Entitlement\FeatureToggleService::register_admin_post_handler();
    }

    public static function resolve_features() {
        // Retrieve stored claims if present, or fallback to default unlocked fixture
        $claims = function_exists('get_option') ? get_option('carmilla_license_claims', null) : null;
        if (empty($claims) || !is_array($claims)) {
            $claims = \Carmilla\Core\Catalog\EntitlementClaims::get_fully_unlocked_fixture();
        }
        $admin_settings = function_exists('get_option') ? get_option('cb_manifest_features', null) : null;

        $matrix = \Carmilla\Core\Catalog\CapabilityResolver::resolve_matrix([
            'entitlements'   => $claims,
            'admin_settings' => is_array($admin_settings) ? $admin_settings : null,
        ]);

        // Store active features and full matrix in memory or global for this request
        global $carmilla_active_features, $carmilla_feature_matrix;
        $carmilla_active_features = $matrix['effective_features'];
        $carmilla_feature_matrix  = $matrix;
    }

    /**
     * Resolves an effective site-level quota, ensuring single-counting across co-installed hosts.
     *
     * @param string $quota_key Quota identifier.
     * @param int    $default   Default fallback value.
     * @return int
     */
    public static function get_quota(string $quota_key, int $default = 0): int {
        $claims = function_exists('get_option') ? get_option('carmilla_license_claims', null) : null;
        if (empty($claims) || !is_array($claims)) {
            $claims = \Carmilla\Core\Catalog\EntitlementClaims::get_fully_unlocked_fixture();
        }
        return \Carmilla\Core\Entitlement\EntitlementResolver::resolve_effective_quota($claims, $quota_key, $default);
    }

    /**
     * Returns the full capability resolution matrix for the current request.
     *
     * @return array
     */
    public static function get_feature_matrix(): array {
        global $carmilla_feature_matrix;
        if ($carmilla_feature_matrix === null) {
            self::resolve_features();
        }
        return $carmilla_feature_matrix ?? [];
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
        global $carmilla_active_features, $carmilla_feature_matrix;
        $carmilla_active_features = null;
        $carmilla_feature_matrix  = null;
        if (class_exists('\Carmilla\Core\Migration\SchemaRunner')) {
            \Carmilla\Core\Migration\SchemaRunner::reset_checkpoints();
        }
        if (class_exists('\Carmilla\Core\Entitlement\EntitlementResolver')) {
            \Carmilla\Core\Entitlement\EntitlementResolver::reset_quota_cache();
        }
        if (class_exists('\Carmilla\Core\Rest\RestInfrastructure')) {
            \Carmilla\Core\Rest\RestInfrastructure::reset_state();
        }
        if (class_exists('\Carmilla\Core\Privacy\PrivacyManager')) {
            \Carmilla\Core\Privacy\PrivacyManager::reset_state();
        }
    }
}
}
