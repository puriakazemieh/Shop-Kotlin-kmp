<?php

// Prevent duplicate loading
if (class_exists('Carmilla_Kernel')) {
    return;
}

// Load core files
require_once __DIR__ . '/inc/Catalog/EntitlementSchema.php';
require_once __DIR__ . '/inc/Catalog/EntitlementClaims.php';
require_once __DIR__ . '/inc/Catalog/DependencyResolver.php';
require_once __DIR__ . '/inc/Migration/SchemaRunner.php';

/**
 * Carmilla Shared Kernel
 * Used by both Carmilla Theme and Carmilla Bridge to initialize
 * the base schema, capabilities, and migrations without duplicating boots.
 */
class Carmilla_Kernel {
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
        
        // Initialize Entitlements and Claims
        add_action('init', [self::class, 'resolve_features']);
    }

    public static function resolve_features() {
        // Retrieve fixtures or actual claims here
        $claims = \Carmilla\Core\Catalog\EntitlementClaims::get_fully_unlocked_fixture();
        $active_features = \Carmilla\Core\Catalog\EntitlementClaims::resolve_effective_features($claims);

        // Store active features in memory or global for this request
        global $carmilla_active_features;
        $carmilla_active_features = $active_features;
    }

    public static function get_boot_contexts() {
        return self::$booted_by;
    }
}
