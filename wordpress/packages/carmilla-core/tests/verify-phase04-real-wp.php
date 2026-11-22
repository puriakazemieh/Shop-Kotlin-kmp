<?php
/**
 * Real WordPress Acceptance Verification Script for Phase 04
 *
 * Runs inside an authentic, running WordPress container connected to MariaDB:
 * 1. P04-ENTITLEMENT-DATA-039: Versioned Capability Catalog & SKU Inventory on real WP
 * 2. P04-WPPLUGIN-CODE-003: Schema Runner, Real DB Migrations & Distributed Lock on MariaDB
 * 3. P04-WPPLUGIN-CODE-004: Shared Kernel Bootstrap & Capability Registration on Real Roles
 * 4. P04-WPTHEME-CODE-005: ThemeHostAdapter, Appearance vs Capability separation & HTTP check
 */

// 1. Bootstrap authentic WordPress environment
require_once '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
require_once ABSPATH . 'wp-admin/includes/template.php';

global $wpdb;

$passed = 0;
$failed = 0;

function check_wp(bool $condition, string $label): void {
    global $passed, $failed;
    if ($condition) {
        echo "PASS [Real WP] - {$label}\n";
        $passed++;
    } else {
        echo "FAIL [Real WP] - {$label}\n";
        $failed++;
    }
}

echo "=================================================================\n";
echo " Verifying Phase 04 on Real WordPress (MariaDB + Apache)\n";
echo "=================================================================\n";
echo "WordPress Version: " . get_bloginfo('version') . "\n";
echo "Site URL: " . get_site_url() . "\n";
echo "Database: MariaDB / MySQL host: " . DB_HOST . " (DB: " . DB_NAME . ")\n\n";

// -------------------------------------------------------------
// Module 1: P04-ENTITLEMENT-DATA-039 (Catalog, Claims & Resolver)
// -------------------------------------------------------------
echo "-- 1. P04-ENTITLEMENT-DATA-039: Entitlement Catalog on Real WP --\n";

$core_file = '/var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/Carmilla_Kernel.php';
check_wp(file_exists($core_file), "Carmilla_Kernel.php exists in packaged theme directory");

require_once $core_file;

use Carmilla\Core\Catalog\EntitlementSchema;
use Carmilla\Core\Catalog\EntitlementClaims;
use Carmilla\Core\Entitlement\EntitlementResolver;
use Carmilla\Core\Migration\SchemaRunner;

$skus = EntitlementSchema::get_sku_inventory();
check_wp(isset($skus['sku_theme_base']), "SKU inventory loaded: sku_theme_base exists");
check_wp(isset($skus['sku_commerce']), "SKU inventory loaded: sku_commerce exists");

$artifact_inv = EntitlementSchema::get_artifact_inventory();
check_wp(!empty($artifact_inv) && isset($artifact_inv['clinic.booking']), "Artifact inventory JSON loaded correctly on Real WP");

// Test site binding with real site URL
$site_url = get_site_url();
$valid_claims = [
    'schemaVersion' => 1,
    'license_key'   => 'real-wp-license',
    'status'        => 'active',
    'expires_at'    => time() + 86400,
    'site_url'      => $site_url,
    'skus'          => ['sku_theme_base'],
    'builders'      => ['android' => true, 'pwa' => true]
];
check_wp(EntitlementResolver::validate_license($valid_claims), "License bound to authentic site URL is VALID");

// Wrong-site license must fail closed
$wrong_claims = $valid_claims;
$wrong_claims['site_url'] = 'https://malicious-counterfeit-site.test';
check_wp(!EntitlementResolver::validate_license($wrong_claims), "License bound to foreign domain is REJECTED (fail-closed)");

// -------------------------------------------------------------
// Module 2: P04-WPPLUGIN-CODE-003 (Real DB Migration & Checkpoints)
// -------------------------------------------------------------
echo "\n-- 2. P04-WPPLUGIN-CODE-003: Real DB Migrations & Distributed Lock --\n";

// Reset synthetic test table and checkpoints
$test_table = $wpdb->prefix . 'carmilla_synthetic_test';
$wpdb->query("DROP TABLE IF EXISTS `{$test_table}`");
delete_option('carmilla_schema_version');
delete_option('carmilla_schema_checkpoints');
SchemaRunner::reset_checkpoints();

// Run real migration against MariaDB
$res = SchemaRunner::run_migrations();
check_wp($res['success'] === true, "Real MariaDB migration executed successfully");
check_wp($res['status'] === 'migrated', "Migration status is 'migrated'");
check_wp($res['to'] === '1.0.3' && get_option('carmilla_schema_version') === '1.0.3', "Target schema version 1.0.3 reached in database");

// Verify real table in MariaDB
$table_exists = $wpdb->get_var("SHOW TABLES LIKE '{$test_table}'") === $test_table;
check_wp($table_exists, "Real MariaDB table '{$test_table}' was created by dbDelta");

$row_count = (int) $wpdb->get_var("SELECT COUNT(*) FROM `{$test_table}`");
check_wp($row_count === 2, "Real rows inserted by migration: expected 2, got {$row_count}");

// Verify checkpoints stored in real wp_options
$checkpoints = get_option('carmilla_schema_checkpoints', []);
check_wp(isset($checkpoints['1.0.3_create_synthetic_table']), "Checkpoint 1.0.3_create_synthetic_table persisted in real wp_options");
check_wp(isset($checkpoints['1.0.3_insert_seed_alpha']), "Checkpoint 1.0.3_insert_seed_alpha persisted in real wp_options");
check_wp(isset($checkpoints['1.0.3_insert_seed_beta']), "Checkpoint 1.0.3_insert_seed_beta persisted in real wp_options");

// Verify distributed locking in real MariaDB transients
$lock_name = 'qa_real_wp_lock';
$locked = SchemaRunner::acquire_lock($lock_name, 30);
check_wp($locked === true, "Acquired real distributed lock via WordPress transients");

$lock_conflict = SchemaRunner::acquire_lock($lock_name, 30);
check_wp($lock_conflict === false, "Concurrent lock attempt rejected");

SchemaRunner::release_lock($lock_name);
$lock_reacquired = SchemaRunner::acquire_lock($lock_name, 30);
check_wp($lock_reacquired === true, "Lock successfully released and re-acquired");
SchemaRunner::release_lock($lock_name);

// -------------------------------------------------------------
// Module 3: P04-WPPLUGIN-CODE-004 (Kernel Boot & Capability Roles)
// -------------------------------------------------------------
echo "\n-- 3. P04-WPPLUGIN-CODE-004: Shared Kernel Boot & Roles on Real WP --\n";

Carmilla_Kernel::reset_state();
Carmilla_Kernel::boot('theme', '0.8.0');

check_wp(Carmilla_Kernel::is_booted(), "Kernel is booted in real WordPress environment");
check_wp(Carmilla_Kernel::is_booted_by('theme'), "Kernel registered 'theme' context");
check_wp(!Carmilla_Kernel::is_booted_by('plugin'), "Plugin context is false in theme-only boot");

// Check administrator role in real database
$admin_role = get_role('administrator');
check_wp($admin_role !== null, "Administrator role loaded from real WordPress database");
check_wp($admin_role->has_cap('carmilla_manage_settings'), "Real admin role has 'carmilla_manage_settings' capability");
check_wp($admin_role->has_cap('carmilla_manage_content'), "Real admin role has 'carmilla_manage_content' capability");
check_wp($admin_role->has_cap('manage_carmilla_integrations'), "Real admin role has 'manage_carmilla_integrations' capability");

// Test co-install single boot idempotency
Carmilla_Kernel::boot('plugin', '1.0.0');
check_wp(Carmilla_Kernel::is_booted_by('plugin'), "Co-install: Plugin context registered without collisions");
check_wp(Carmilla_Kernel::is_booted_by('theme'), "Co-install: Theme context maintained");

// -------------------------------------------------------------
// Module 4: P04-WPTHEME-CODE-005 (ThemeHostAdapter & Real WP Theme)
// -------------------------------------------------------------
echo "\n-- 4. P04-WPTHEME-CODE-005: ThemeHostAdapter & Theme on Real WP --\n";

require_once '/var/www/html/wp-content/themes/carmilla-theme/inc/class-theme-host-adapter.php';
require_once '/var/www/html/wp-content/themes/carmilla-theme/inc/customizer.php';
require_once '/var/www/html/wp-content/themes/carmilla-theme/inc/template-functions.php';

use Carmilla\Theme\ThemeHostAdapter;

ThemeHostAdapter::init();
check_wp(ThemeHostAdapter::is_initialized(), "ThemeHostAdapter initialized on Real WP");
check_wp(ThemeHostAdapter::is_kernel_booted(), "ThemeHostAdapter confirms kernel is active");
check_wp(ThemeHostAdapter::has_bridge(), "ThemeHostAdapter detects co-installed bridge");

// Test appearance settings stored in real Theme Mods
set_theme_mod('carmilla_color_accent', '#00AAFF');
set_theme_mod('carmilla_hero_title', 'عنوان تست روی وردپرس واقعی');

$appearance = ThemeHostAdapter::get_appearance_settings();
check_wp($appearance['colors']['accent'] === '#00AAFF', "Custom accent color saved in real Theme Mod and retrieved");
check_wp($appearance['hero_title'] === 'عنوان تست روی وردپرس واقعی', "Hero title saved in real Theme Mod and retrieved");

// Capability status strictly from Kernel
$cap_status = ThemeHostAdapter::get_capability_status();
check_wp(is_array($cap_status) && !empty($cap_status), "Capability status retrieved from Kernel on Real WP");
check_wp(isset($cap_status['theme.base']), "Capability 'theme.base' active in site capability status");

// Verify navigation destinations
$nav = ThemeHostAdapter::get_navigation_destinations();
check_wp(!empty($nav) && $nav[0]['label'] === 'خانه', "Navigation destinations built successfully on Real WP");

// Test Persian price formatting function
check_wp(carmilla_price(250000) === '۲۵۰٬۰۰۰ تومان', "carmilla_price() formats Persian numerals and commas correctly");

// -------------------------------------------------------------
// Module 5: P04-WPPLUGIN-CODE-006: Shared Capability Resolver on Real WP
// -------------------------------------------------------------
echo "\n-- 5. P04-WPPLUGIN-CODE-006: Shared Capability Resolver on Real WP --\n";

use Carmilla\Core\Catalog\CapabilityResolver;

// Verify CapabilityResolver is available in real WordPress runtime
check_wp(class_exists('\Carmilla\Core\Catalog\CapabilityResolver'), "CapabilityResolver class exists in real WordPress runtime");

// Test matrix resolution on real WordPress
$real_matrix = CapabilityResolver::resolve_matrix();
check_wp(!empty($real_matrix['features']), "CapabilityResolver generated full feature matrix on Real WP");
check_wp(isset($real_matrix['features']['theme.base']), "theme.base present in matrix");
check_wp($real_matrix['features']['theme.base']['is_active'] === true, "theme.base is active on Real WP");
check_wp($real_matrix['features']['theme.base']['status'] === CapabilityResolver::STATUS_ACTIVE, "theme.base status is ACTIVE on Real WP");

// Test distinct status codes on Real WP
$fixtures = CapabilityResolver::get_test_fixtures();

// Test unpurchased toggle invariance on Real WP
$toggle_res = CapabilityResolver::resolve_matrix([
    'entitlements'   => $fixtures['admin_toggle_unpurchased']['entitlements'],
    'admin_settings' => $fixtures['admin_toggle_unpurchased']['admin_settings'],
]);
check_wp($toggle_res['features']['commerce.core']['is_active'] === false, "Admin toggle cannot grant unpurchased commerce.core on Real WP");
check_wp($toggle_res['features']['commerce.core']['status'] === CapabilityResolver::STATUS_NOT_ENTITLED, "commerce.core status is NOT_ENTITLED on Real WP");

// Test missing code in package on Real WP
$code_res = CapabilityResolver::resolve_matrix([
    'package_code'   => ['academy.core' => false],
    'admin_settings' => ['academy.core' => true],
]);
check_wp($code_res['features']['academy.core']['status'] === CapabilityResolver::STATUS_MISSING_CODE, "Missing code yields MISSING_CODE status on Real WP");
check_wp(!empty($code_res['features']['academy.core']['reason']), "Missing code provides Persian explanation on Real WP");

// Test missing WooCommerce prerequisite on Real WP
$woo_res = CapabilityResolver::resolve_matrix([
    'environment' => ['woocommerce_active' => false],
]);
check_wp($woo_res['features']['commerce.core']['status'] === CapabilityResolver::STATUS_PREREQUISITE_MISSING, "Missing WooCommerce sets commerce.core to PREREQUISITE_MISSING on Real WP");
check_wp(in_array('woocommerce', $woo_res['features']['commerce.core']['missing_prerequisites'], true), "commerce.core lists woocommerce missing on Real WP");
check_wp($woo_res['features']['academy.core']['status'] === CapabilityResolver::STATUS_ACTIVE, "academy.core remains ACTIVE when WooCommerce is missing on Real WP");
check_wp($woo_res['features']['clinic.booking']['status'] === CapabilityResolver::STATUS_ACTIVE, "clinic.booking remains ACTIVE when WooCommerce is missing on Real WP");
check_wp($woo_res['features']['theme.base']['status'] === CapabilityResolver::STATUS_ACTIVE, "theme.base remains ACTIVE when WooCommerce is missing on Real WP");

// Test release gate on Real WP
$release_res = CapabilityResolver::resolve_matrix([
    'release_catalog' => ['clinic.consultation' => false],
    'admin_settings'  => ['clinic.consultation' => true],
]);
check_wp($release_res['features']['clinic.consultation']['status'] === CapabilityResolver::STATUS_RELEASE_GATE_RESTRICTED, "Release gate restricts clinic.consultation on Real WP");

// Test real WordPress option integration
update_option('cb_manifest_features', [
    'content.core' => true,
    'clinic.booking' => false,
]);
Carmilla_Kernel::resolve_features();
check_wp(Carmilla_Kernel::is_feature_active('clinic.booking') === false, "Real wp_option cb_manifest_features disables clinic.booking in Kernel");
check_wp(carmilla_feature_enabled('stories') === true, "carmilla_feature_enabled('stories') works via Real WP Kernel");

// Reset option
delete_option('cb_manifest_features');
Carmilla_Kernel::resolve_features();

// -------------------------------------------------------------
// Module 6: Real HTTP Front-End Request (Apache + PHP Engine)
// -------------------------------------------------------------
echo "\n-- 6. Real Front-End HTTP Request to WordPress --\n";

// Execute local HTTP GET request to Apache on 127.0.0.1
$response = wp_remote_get('http://127.0.0.1/', ['timeout' => 10, 'headers' => ['Host' => 'localhost:8080']]);
$status_code = wp_remote_retrieve_response_code($response);
$body = wp_remote_retrieve_body($response);

check_wp(!is_wp_error($response), "HTTP GET request to WordPress root succeeded without WP_Error");
check_wp($status_code === 200, "WordPress returned HTTP status 200 OK (actual: {$status_code})");
check_wp(strlen($body) > 100, "WordPress served non-empty HTML response (" . strlen($body) . " bytes)");
check_wp(strpos($body, 'Fatal error') === false, "Zero PHP Fatal errors in WordPress HTTP output");
check_wp(strpos($body, 'Parse error') === false, "Zero PHP Parse errors in WordPress HTTP output");

// Clean up test table
$wpdb->query("DROP TABLE IF EXISTS {$test_table}");

echo "\n=================================================================\n";
if ($failed === 0) {
    echo "ALL {$passed} REAL WORDPRESS ACCEPTANCE CHECKS PASSED!\n";
    echo "=================================================================\n";
    exit(0);
} else {
    echo "FAILED: {$failed} check(s) failed out of " . ($passed + $failed) . ".\n";
    echo "=================================================================\n";
    exit(1);
}
