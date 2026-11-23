<?php
/**
 * Master Verification Suite: Phase 04 End-to-End on Real WordPress
 *
 * Covers all completed Phase 04 tasks on authentic WordPress + MariaDB + Apache:
 * - P04-WPPLUGIN-ADR-001 (Host decoupling & standalone execution)
 * - P04-PRODUCT-ADR-038 (Product ownership matrix & domain separation)
 * - P04-WPPLUGIN-ADR-002 (Version negotiation & single boot authority)
 * - P04-ENTITLEMENT-DATA-039 (Catalog, SKU inventory, site binding & grace period)
 * - P04-WPPLUGIN-CODE-003 (Resumable schema runner & MariaDB distributed lock)
 * - P04-WPPLUGIN-CODE-004 (Shared kernel bootstrap & administrator capabilities)
 * - P04-WPTHEME-CODE-005 (Theme host connection, standalone theme, appearance vs capability)
 * - P04-WPPLUGIN-CODE-006 (CapabilityResolver, 6 status codes, prereq isolation, release gate)
 * - Dynamic Mode Transitions: Standalone Theme, Standalone Plugin (Twenty Twenty-Four), Co-install
 */

// Load authentic WordPress environment
require_once '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/plugin.php';

echo "=================================================================\n";
echo " Master Verification: Phase 04 on Real WordPress (MariaDB + Apache)\n";
echo "=================================================================\n";
echo "WordPress Version: " . get_bloginfo('version') . "\n";
echo "Site URL: " . get_site_url() . "\n";
echo "Active Theme: " . wp_get_theme()->get_stylesheet() . "\n";
echo "Database: MariaDB / MySQL (" . $GLOBALS['wpdb']->db_version() . ")\n\n";

$passed = 0;
$failed = 0;

function check_wp(bool $condition, string $description, string $details = ''): void {
    global $passed, $failed;
    if ($condition) {
        $passed++;
        echo "PASS [Real WP] - {$description}\n";
    } else {
        $failed++;
        echo "FAIL [Real WP] - {$description}" . ($details ? " ({$details})" : "") . "\n";
    }
}

use Carmilla\Core\Catalog\EntitlementSchema;
use Carmilla\Core\Catalog\EntitlementClaims;
use Carmilla\Core\Catalog\CapabilityResolver;
use Carmilla\Core\Entitlement\EntitlementResolver;
use Carmilla\Core\Migration\SchemaRunner;
use Carmilla\Theme\ThemeHostAdapter;

// =============================================================
// Module 1: P04-WPPLUGIN-ADR-001 & ADR-002: Co-Install Single Boot Authority
// =============================================================
echo "\n-- 1. P04-WPPLUGIN-ADR-001 & ADR-002: Co-Install Boot Authority --\n";

check_wp(class_exists('\Carmilla_Kernel'), "Carmilla_Kernel class loaded in WordPress runtime");
check_wp(\Carmilla_Kernel::is_booted(), "Carmilla_Kernel confirms it is booted");

$boot_contexts = \Carmilla_Kernel::get_boot_contexts();
check_wp(isset($boot_contexts['theme']), "Theme context registered in kernel");
check_wp(isset($boot_contexts['plugin']), "Plugin context registered in kernel (co-install mode)");

// Checkpoint prevents duplicate boot
check_wp(SchemaRunner::register_checkpoint('boot_kernel') === false, "Checkpoint 'boot_kernel' prevents duplicate boot execution");

// Verify REST API route singular method registration (no duplicate endpoints)
$rest_server = rest_get_server();
$routes = $rest_server->get_routes();
$manifest_route = $routes['/carmilla/v1/client-manifest'] ?? [];
$endpoints = $manifest_route ? ($manifest_route[0]['methods'] ?? []) : [];
check_wp(!empty($routes['/carmilla/v1/client-manifest']), "REST route /carmilla/v1/client-manifest is registered");
check_wp(count($manifest_route) === 1, "Singular endpoint registration for /carmilla/v1/client-manifest (no duplicate controllers)");

// =============================================================
// Module 2: P04-PRODUCT-ADR-038: Ownership Matrix & Separation of Concerns
// =============================================================
echo "\n-- 2. P04-PRODUCT-ADR-038: Product Ownership Matrix on Real WP --\n";

// Theme Mods store ONLY presentation appearance
set_theme_mod('carmilla_color_accent', '#FF6600');
set_theme_mod('carmilla_hero_title', 'عنوان تست فروشگاه');

$appearance = ThemeHostAdapter::get_appearance_settings();
check_wp($appearance['colors']['accent'] === '#FF6600', "Theme Mod accent color is #FF6600");
check_wp($appearance['hero_title'] === 'عنوان تست فروشگاه', "Theme Mod hero title saved and retrieved");
check_wp(!isset($appearance['commerce.core']), "Theme Mods contain ZERO capability/entitlement toggles");

// Site-owned data in MariaDB (wp_options, custom tables)
$opt_val = get_option('carmilla_schema_version', '');
check_wp(!empty($opt_val), "Site-owned schema version exists in real wp_options");

// =============================================================
// Module 3: P04-ENTITLEMENT-DATA-039: Catalog, SKU Inventory & License Validation
// =============================================================
echo "\n-- 3. P04-ENTITLEMENT-DATA-039: Catalog & SKU Inventory on Real WP --\n";

$skus = EntitlementSchema::get_sku_inventory();
check_wp(isset($skus['sku_theme_base']), "SKU sku_theme_base defined in catalog");
check_wp(isset($skus['sku_plugin_base']), "SKU sku_plugin_base defined in catalog");
check_wp(isset($skus['sku_commerce']), "SKU sku_commerce defined in catalog");
check_wp(isset($skus['sku_academy']), "SKU sku_academy defined in catalog");
check_wp(isset($skus['sku_clinic_booking']), "SKU sku_clinic_booking defined in catalog");
check_wp(isset($skus['sku_psych']), "SKU sku_psych defined in catalog");
check_wp(isset($skus['sku_builder_android']), "SKU sku_builder_android defined in catalog");
check_wp(isset($skus['sku_builder_pwa']), "SKU sku_builder_pwa defined in catalog");

// Site Binding validation on authentic WordPress site URL
$site_url = get_site_url();
$valid_claim = [
    'status'     => 'active',
    'site_url'   => $site_url,
    'expires_at' => time() + 86400,
    'skus'       => ['sku_theme_base', 'sku_commerce']
];
check_wp(EntitlementResolver::validate_license($valid_claim), "License matching authentic site URL ({$site_url}) is VALID");

$foreign_claim = [
    'status'     => 'active',
    'site_url'   => 'https://unauthorized-domain.com',
    'expires_at' => time() + 86400,
    'skus'       => ['sku_theme_base', 'sku_commerce']
];
check_wp(EntitlementResolver::validate_license($foreign_claim) === false, "License bound to foreign domain is REJECTED (fail closed)");

// Grace Period (14 days)
$expired_5d_claim = [
    'status'     => 'active',
    'site_url'   => $site_url,
    'expires_at' => time() - (5 * 86400),
    'skus'       => ['sku_theme_base']
];
check_wp(EntitlementResolver::validate_license($expired_5d_claim, false) === false, "Expired license rejected in strict mode");
check_wp(EntitlementResolver::validate_license($expired_5d_claim, true) === true, "Expired license within 14-day window accepted with allow_grace");

// Revoked license
$revoked_claim = [
    'status'     => 'revoked',
    'site_url'   => $site_url,
    'skus'       => ['sku_theme_base']
];
check_wp(EntitlementResolver::validate_license($revoked_claim) === false, "Revoked license strictly rejected");

// =============================================================
// Module 4: P04-WPPLUGIN-CODE-003: Resumable MariaDB Migration & Distributed Lock
// =============================================================
echo "\n-- 4. P04-WPPLUGIN-CODE-003: Real MariaDB Schema Migrations --\n";

global $wpdb;
$synthetic_table = $wpdb->prefix . 'carmilla_synthetic_test';

// Prepare test state: clean synthetic checkpoints and drop table to test real migration
$wpdb->query("DROP TABLE IF EXISTS {$synthetic_table}");
$checkpoints = SchemaRunner::get_persisted_checkpoints();
unset($checkpoints['1.0.3_create_synthetic_table']);
unset($checkpoints['1.0.3_insert_seed_alpha']);
unset($checkpoints['1.0.3_insert_seed_beta']);
update_option(SchemaRunner::CHECKPOINTS_KEY, $checkpoints);
update_option(SchemaRunner::SCHEMA_VERSION_KEY, '1.0.2');
SchemaRunner::reset_checkpoints();

// Run migrations on real MariaDB
$mig_result = SchemaRunner::run_migrations(['force_run' => true]);
check_wp($mig_result['success'] === true, "Real MariaDB migration execution succeeded");
check_wp(SchemaRunner::get_current_schema_version() === '1.0.3', "Target schema version 1.0.3 reached in MariaDB");

// Verify table exists in MariaDB
$table_exists = $wpdb->get_var("SHOW TABLES LIKE '{$synthetic_table}'") === $synthetic_table;
check_wp($table_exists, "Real MariaDB table {$synthetic_table} exists in database");

// Verify rows in MariaDB
$row_count = (int) $wpdb->get_var("SELECT COUNT(*) FROM {$synthetic_table}");
check_wp($row_count === 2, "Synthetic table contains exactly 2 rows (expected: 2, actual: {$row_count})");

// Verify checkpoints persisted in real wp_options
check_wp(SchemaRunner::is_step_completed('1.0.3_create_synthetic_table'), "Checkpoint 1.0.3_create_synthetic_table recorded in real wp_options");
check_wp(SchemaRunner::is_step_completed('1.0.3_insert_seed_alpha'), "Checkpoint 1.0.3_insert_seed_alpha recorded in real wp_options");
check_wp(SchemaRunner::is_step_completed('1.0.3_insert_seed_beta'), "Checkpoint 1.0.3_insert_seed_beta recorded in real wp_options");

// Idempotent re-run
SchemaRunner::reset_checkpoints();
$rerun_result = SchemaRunner::run_migrations(['force_run' => true]);
check_wp($rerun_result['success'] === true && $rerun_result['status'] === 'noop', "Idempotent re-run returns noop on up-to-date schema");
$row_count_after = (int) $wpdb->get_var("SELECT COUNT(*) FROM {$synthetic_table}");
check_wp($row_count_after === 2, "Row count remains exactly 2 on idempotent re-run (zero duplicate data)");

// =============================================================
// Module 5: P04-WPPLUGIN-CODE-004: Shared Kernel Boot & Roles on Real WP
// =============================================================
echo "\n-- 5. P04-WPPLUGIN-CODE-004: Capabilities & Roles on Real WP --\n";

$admin_role = get_role('administrator');
check_wp($admin_role instanceof WP_Role, "Administrator role exists in WordPress");
check_wp($admin_role->has_cap('carmilla_manage_settings'), "Admin role has carmilla_manage_settings capability");
check_wp($admin_role->has_cap('carmilla_manage_content'), "Admin role has carmilla_manage_content capability");
check_wp($admin_role->has_cap('manage_carmilla_integrations'), "Admin role has manage_carmilla_integrations capability");

// Subscriber must NOT have these capabilities
$sub_role = get_role('subscriber');
if ($sub_role) {
    check_wp(!$sub_role->has_cap('carmilla_manage_settings'), "Subscriber does NOT have carmilla_manage_settings");
    check_wp(!$sub_role->has_cap('manage_carmilla_integrations'), "Subscriber does NOT have manage_carmilla_integrations");
}

// =============================================================
// Module 6: P04-WPPLUGIN-CODE-006: Shared Capability Resolver on Real WP
// =============================================================
echo "\n-- 6. P04-WPPLUGIN-CODE-006: Shared Capability Resolver on Real WP --\n";

// Save custom admin settings in wp_options
update_option('cb_manifest_features', [
    'commerce.core'    => true,
    'commerce.cart'    => true,
    'commerce.payment' => true,
    'commerce.wallet'  => true,
    'clinic.booking'   => true,
    'clinic.therapists'=> true,
    'academy.core'     => false,
]);

\Carmilla_Kernel::resolve_features();
$matrix = \Carmilla_Kernel::get_feature_matrix();

check_wp($matrix['features']['theme.base']['status'] === CapabilityResolver::STATUS_ACTIVE, "theme.base status is ACTIVE");
check_wp($matrix['features']['content.core']['status'] === CapabilityResolver::STATUS_ACTIVE, "content.core status is ACTIVE");
check_wp($matrix['features']['clinic.booking']['status'] === CapabilityResolver::STATUS_ACTIVE, "clinic.booking status is ACTIVE");
check_wp($matrix['features']['academy.core']['status'] === CapabilityResolver::STATUS_DISABLED_BY_ADMIN, "academy.core is DISABLED_BY_ADMIN");

// Admin toggle invariance on unpurchased license
$theme_only_claims = EntitlementClaims::get_base_theme_fixture();
$invariance_matrix = CapabilityResolver::resolve_matrix([
    'entitlements'   => $theme_only_claims,
    'admin_settings' => ['commerce.core' => true, 'academy.core' => true],
]);
check_wp($invariance_matrix['features']['commerce.core']['is_active'] === false, "Admin toggle cannot activate unpurchased commerce.core");
check_wp($invariance_matrix['features']['commerce.core']['status'] === CapabilityResolver::STATUS_NOT_ENTITLED, "commerce.core status is NOT_ENTITLED");

// Clean up admin options before testing prerequisite defaults
delete_option('cb_manifest_features');

// Missing WooCommerce prerequisite isolation
$no_woo_matrix = CapabilityResolver::resolve_matrix([
    'environment' => ['woocommerce_active' => false],
]);
check_wp($no_woo_matrix['features']['commerce.core']['status'] === CapabilityResolver::STATUS_PREREQUISITE_MISSING, "Missing WooCommerce marks commerce.core as PREREQUISITE_MISSING");
check_wp($no_woo_matrix['features']['commerce.cart']['status'] === CapabilityResolver::STATUS_PREREQUISITE_MISSING, "commerce.cart gets PREREQUISITE_MISSING");
check_wp($no_woo_matrix['features']['theme.base']['status'] === CapabilityResolver::STATUS_ACTIVE, "theme.base remains ACTIVE when WooCommerce missing");
check_wp($no_woo_matrix['features']['clinic.booking']['status'] === CapabilityResolver::STATUS_ACTIVE, "clinic.booking remains ACTIVE when WooCommerce missing");
check_wp($no_woo_matrix['features']['academy.core']['status'] === CapabilityResolver::STATUS_ACTIVE, "academy.core remains ACTIVE when WooCommerce missing");

// Release gate restriction (releaseReady = false)
$gated_matrix = CapabilityResolver::resolve_matrix([
    'release_catalog' => ['clinic.consultation' => false],
    'admin_settings'  => ['clinic.consultation' => true],
]);
check_wp($gated_matrix['features']['clinic.consultation']['status'] === CapabilityResolver::STATUS_RELEASE_GATE_RESTRICTED, "clinic.consultation restricted by release gate");
check_wp(strpos($gated_matrix['features']['clinic.consultation']['reason'], 'گیت انتشار') !== false, "Persian reason clearly cites release gate restriction");

// Clean up option
delete_option('cb_manifest_features');
\Carmilla_Kernel::resolve_features();

// =============================================================
// Module 7: P04-WPTHEME-CODE-005: Standalone Theme Mode (Bridge Deactivated)
// =============================================================
echo "\n-- 7. P04-WPTHEME-CODE-005: Standalone Theme Mode on Real WP --\n";

// Deactivate Bridge
deactivate_plugins('carmilla-bridge/carmilla-bridge.php', true);
check_wp(!is_plugin_active('carmilla-bridge/carmilla-bridge.php'), "Carmilla Bridge plugin successfully DEACTIVATED");

// Reset kernel state to simulate fresh Theme-only request
\Carmilla_Kernel::reset_state();
\Carmilla_Kernel::boot('theme', '0.8.0');

check_wp(\Carmilla_Kernel::is_booted_by('theme'), "Kernel confirms booted by 'theme'");
check_wp(!\Carmilla_Kernel::is_booted_by('plugin'), "Kernel confirms 'plugin' is NOT booted in Standalone Theme mode");
check_wp(ThemeHostAdapter::is_standalone(), "ThemeHostAdapter confirms standalone mode (Bridge absent)");
check_wp(ThemeHostAdapter::has_bridge() === false, "ThemeHostAdapter confirms has_bridge is false");

// Global helper functions work standalone
check_wp(carmilla_feature_enabled('stories') === true, "carmilla_feature_enabled('stories') works standalone without Bridge");
check_wp(carmilla_feature_enabled('blog') === true, "carmilla_feature_enabled('blog') works standalone without Bridge");
check_wp(carmilla_price(350000) === '۳۵۰٬۰۰۰ تومان', "carmilla_price(350000) formats Persian price correctly without Bridge");

// Real HTTP request to Apache in Standalone Theme Mode
$theme_http_resp = wp_remote_get('http://127.0.0.1/', ['timeout' => 10, 'headers' => ['Host' => 'localhost:8080']]);
$theme_http_code = wp_remote_retrieve_response_code($theme_http_resp);
$theme_http_body = wp_remote_retrieve_body($theme_http_resp);

check_wp(!is_wp_error($theme_http_resp), "Standalone Theme: HTTP request succeeded without WP_Error");
check_wp($theme_http_code === 200, "Standalone Theme: HTTP status 200 OK (actual: {$theme_http_code})");
check_wp(strpos($theme_http_body, 'carmilla-tokens') !== false, "Standalone Theme: Enqueued tokens.css stylesheet");
check_wp(strpos($theme_http_body, 'Fatal error') === false, "Standalone Theme: Zero PHP Fatal errors");

// =============================================================
// Module 8: Standalone Plugin Mode on Real WP (Theme Switched to Twenty Twenty-Four)
// =============================================================
echo "\n-- 8. Standalone Plugin Mode on Real WP (Twenty Twenty-Four Theme) --\n";

// Reactivate Bridge first
activate_plugin('carmilla-bridge/carmilla-bridge.php');
check_wp(is_plugin_active('carmilla-bridge/carmilla-bridge.php'), "Carmilla Bridge plugin re-activated");

// Switch to Twenty Twenty-Four
switch_theme('twentytwentyfour');
check_wp(wp_get_theme()->get_stylesheet() === 'twentytwentyfour', "Active theme successfully switched to Twenty Twenty-Four");

// In standalone mode with standard theme, verify CB_Standalone_Boot
CB_Standalone_Boot::register_theme_function_stubs();
check_wp(CB_Standalone_Boot::is_carmilla_theme_active() === false, "CB_Standalone_Boot correctly detects Carmilla Theme is NOT active");

// Verify polyfill functions work
check_wp(carmilla_is_theme_active() === false, "Polyfill carmilla_is_theme_active() returns false");
check_wp(function_exists('carmilla_feature_enabled'), "Polyfill carmilla_feature_enabled() exists in standalone plugin mode");
check_wp(carmilla_feature_enabled('blog') === true, "carmilla_feature_enabled('blog') returns true in standalone plugin mode");
check_wp(strpos(carmilla_price(120000), 'تومان') !== false, "carmilla_price(120000) works in standalone plugin mode");

// Real HTTP request to REST API on Twenty Twenty-Four
$rest_resp = wp_remote_get('http://127.0.0.1/wp-json/carmilla/v1/client-manifest', ['timeout' => 10, 'headers' => ['Host' => 'localhost:8080']]);
$rest_code = wp_remote_retrieve_response_code($rest_resp);
$rest_json = json_decode(wp_remote_retrieve_body($rest_resp), true);

check_wp($rest_code === 200, "Standalone Plugin on TT4: /carmilla/v1/client-manifest returns HTTP 200 OK");
check_wp(!empty($rest_json['features']), "Standalone Plugin on TT4: client-manifest contains features map");
check_wp(isset($rest_json['features']['plugin.base']) && $rest_json['features']['plugin.base'] === true, "Standalone Plugin on TT4: plugin.base is true");

// Real HTTP request to front-end on Twenty Twenty-Four
$tt4_resp = wp_remote_get('http://127.0.0.1/', ['timeout' => 10, 'headers' => ['Host' => 'localhost:8080']]);
$tt4_code = wp_remote_retrieve_response_code($tt4_resp);
check_wp($tt4_code === 200, "Standalone Plugin on TT4: Front-end returns HTTP 200 OK");

// =============================================================
// Module 9: Restoration to Healthy Co-Install Mode
// =============================================================
echo "\n-- 9. Restoration to Co-Install Mode --\n";

switch_theme('carmilla-theme');
check_wp(wp_get_theme()->get_stylesheet() === 'carmilla-theme', "Restored active theme to carmilla-theme");
check_wp(is_plugin_active('carmilla-bridge/carmilla-bridge.php'), "Carmilla Bridge remains active in co-install mode");

// Reset kernel for co-install boot
\Carmilla_Kernel::reset_state();
\Carmilla_Kernel::boot('theme', '0.8.0');
\Carmilla_Kernel::boot('plugin', '1.0.0');

check_wp(\Carmilla_Kernel::is_booted_by('theme') && \Carmilla_Kernel::is_booted_by('plugin'), "Co-install boot confirmed for both hosts");

// Clean up synthetic test table
$wpdb->query("DROP TABLE IF EXISTS {$synthetic_table}");

echo "\n=================================================================\n";
if ($failed === 0) {
    echo "ALL {$passed} MASTER PHASE 04 REAL WORDPRESS CHECKS PASSED!\n";
    echo "=================================================================\n";
    exit(0);
} else {
    echo "FAILED: {$failed} check(s) failed out of " . ($passed + $failed) . ".\n";
    echo "=================================================================\n";
    exit(1);
}
