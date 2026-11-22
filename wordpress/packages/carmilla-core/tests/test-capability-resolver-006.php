<?php
/**
 * Test Suite: P04-WPPLUGIN-CODE-006
 * Shared capability resolver, prerequisites, and release readiness
 *
 * Verifies:
 * 1. Canonical matrix resolution across standard ADR-006 fixtures.
 * 2. Distinct status codes (ACTIVE, NOT_ENTITLED, MISSING_CODE, DISABLED_BY_ADMIN, PREREQUISITE_MISSING, RELEASE_GATE_RESTRICTED).
 * 3. Admin toggle CANNOT grant entitlement or create missing code.
 * 4. Missing WooCommerce restricts ONLY commerce.* features, leaving base UI and other verticals active.
 * 5. Standalone Theme mode requires NO Carmilla Bridge prerequisite.
 * 6. Standalone Plugin mode requires NO Carmilla Theme prerequisite.
 * 7. Release gate (releaseReady = false) cannot be bypassed by admin toggle.
 * 8. Dependency closure propagation across transitive feature graphs.
 * 9. Integration with CB_Manifest_Controller and ThemeHostAdapter.
 */

if (!defined('ABSPATH')) {
    define('ABSPATH', __DIR__ . '/');
}

// Locate and load Kernel
$core_candidates = [
    dirname(__DIR__) . '/Carmilla_Kernel.php',
    dirname(__DIR__, 2) . '/packages/carmilla-core/Carmilla_Kernel.php',
    '/var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/Carmilla_Kernel.php',
    '/var/www/html/wp-content/plugins/carmilla-bridge/packages/carmilla-core/Carmilla_Kernel.php',
];
$loaded_kernel = false;
foreach ($core_candidates as $candidate) {
    if (file_exists($candidate)) {
        require_once $candidate;
        $loaded_kernel = true;
        break;
    }
}

if (!$loaded_kernel) {
    echo "FAILED: Could not locate Carmilla_Kernel.php\n";
    exit(1);
}

// Locate and load Theme Host Adapter and Manifest Controller
$theme_adapter_candidates = [
    dirname(__DIR__, 3) . '/carmilla-theme/inc/class-theme-host-adapter.php',
    dirname(__DIR__, 2) . '/carmilla-theme/inc/class-theme-host-adapter.php',
    dirname(__DIR__, 1) . '/../../carmilla-theme/inc/class-theme-host-adapter.php',
    '/var/www/html/wp-content/themes/carmilla-theme/inc/class-theme-host-adapter.php',
];
foreach ($theme_adapter_candidates as $candidate) {
    if (file_exists($candidate)) {
        require_once $candidate;
        break;
    }
}

$template_func_candidates = [
    dirname(__DIR__, 3) . '/carmilla-theme/inc/template-functions.php',
    dirname(__DIR__, 2) . '/carmilla-theme/inc/template-functions.php',
    '/var/www/html/wp-content/themes/carmilla-theme/inc/template-functions.php',
];
foreach ($template_func_candidates as $candidate) {
    if (file_exists($candidate)) {
        require_once $candidate;
        break;
    }
}

$manifest_ctrl_candidates = [
    dirname(__DIR__, 3) . '/carmilla-bridge/includes/class-cb-manifest-controller.php',
    dirname(__DIR__, 2) . '/carmilla-bridge/includes/class-cb-manifest-controller.php',
    '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-manifest-controller.php',
];
foreach ($manifest_ctrl_candidates as $candidate) {
    if (file_exists($candidate)) {
        require_once $candidate;
        break;
    }
}

use Carmilla\Core\Catalog\CapabilityResolver;
use Carmilla\Core\Catalog\EntitlementSchema;
use Carmilla\Core\Catalog\EntitlementClaims;

$passed = 0;
$failed = 0;

function assert_test(bool $condition, string $message): void {
    global $passed, $failed;
    if ($condition) {
        $passed++;
        echo "PASS - {$message}\n";
    } else {
        $failed++;
        echo "FAIL - {$message}\n";
    }
}

echo "=================================================================\n";
echo " Acceptance Suite: P04-WPPLUGIN-CODE-006 (Capability Resolver)\n";
echo "=================================================================\n\n";

// -------------------------------------------------------------
// Group 1: Canonical Status Codes & Fixtures Matrix
// -------------------------------------------------------------
echo "-- Group 1: Status Codes & Fixtures Matrix --\n";

$fixtures = CapabilityResolver::get_test_fixtures();
assert_test(isset($fixtures['base_ui_only']), 'Fixture base_ui_only defined');
assert_test(isset($fixtures['admin_toggle_unpurchased']), 'Fixture admin_toggle_unpurchased defined');
assert_test(isset($fixtures['missing_code_package']), 'Fixture missing_code_package defined');
assert_test(isset($fixtures['disabled_by_admin']), 'Fixture disabled_by_admin defined');
assert_test(isset($fixtures['missing_woocommerce']), 'Fixture missing_woocommerce defined');
assert_test(isset($fixtures['release_gate_restricted']), 'Fixture release_gate_restricted defined');
assert_test(isset($fixtures['standalone_theme_no_bridge']), 'Fixture standalone_theme_no_bridge defined');
assert_test(isset($fixtures['standalone_plugin_no_theme']), 'Fixture standalone_plugin_no_theme defined');

// -------------------------------------------------------------
// Group 2: Base UI Only Mode
// -------------------------------------------------------------
echo "\n-- Group 2: Base UI Only Mode --\n";
$f_base = $fixtures['base_ui_only'];
$matrix_base = CapabilityResolver::resolve_matrix([
    'package_code'   => $f_base['package_code'],
    'entitlements'   => $f_base['entitlements'],
    'admin_settings' => $f_base['admin_settings'],
    'environment'    => $f_base['environment'],
]);

assert_test($matrix_base['features']['theme.base']['is_active'] === true, 'theme.base is active in Base UI mode');
assert_test($matrix_base['features']['theme.base']['status'] === CapabilityResolver::STATUS_ACTIVE, 'theme.base status is ACTIVE');
assert_test($matrix_base['features']['content.core']['is_active'] === true, 'content.core is active in Base UI mode');
assert_test($matrix_base['features']['commerce.core']['is_active'] === false, 'commerce.core is inactive in Base UI mode');
assert_test($matrix_base['features']['commerce.core']['status'] === CapabilityResolver::STATUS_NOT_ENTITLED, 'commerce.core status is NOT_ENTITLED');
assert_test($matrix_base['features']['academy.core']['is_active'] === false, 'academy.core is inactive in Base UI mode');
assert_test($matrix_base['features']['academy.core']['status'] === CapabilityResolver::STATUS_NOT_ENTITLED, 'academy.core status is NOT_ENTITLED');
assert_test($matrix_base['features']['clinic.booking']['status'] === CapabilityResolver::STATUS_NOT_ENTITLED, 'clinic.booking status is NOT_ENTITLED');

// -------------------------------------------------------------
// Group 3: Admin Toggle CANNOT Grant Entitlement or Invent Code
// -------------------------------------------------------------
echo "\n-- Group 3: Admin Toggle Invariance Gate --\n";

$f_toggle = $fixtures['admin_toggle_unpurchased'];
$matrix_toggle = CapabilityResolver::resolve_matrix([
    'package_code'   => $f_toggle['package_code'],
    'entitlements'   => $f_toggle['entitlements'],
    'admin_settings' => $f_toggle['admin_settings'], // admin toggled commerce.core & academy.core ON
    'environment'    => $f_toggle['environment'],
]);

assert_test($matrix_toggle['features']['commerce.core']['admin_enabled'] === true, 'commerce.core was toggled ON by admin');
assert_test($matrix_toggle['features']['commerce.core']['is_active'] === false, 'commerce.core is NOT active despite admin toggle');
assert_test($matrix_toggle['features']['commerce.core']['status'] === CapabilityResolver::STATUS_NOT_ENTITLED, 'commerce.core status remains NOT_ENTITLED');
assert_test($matrix_toggle['features']['academy.core']['admin_enabled'] === true, 'academy.core was toggled ON by admin');
assert_test($matrix_toggle['features']['academy.core']['is_active'] === false, 'academy.core is NOT active despite admin toggle');
assert_test($matrix_toggle['features']['academy.core']['status'] === CapabilityResolver::STATUS_NOT_ENTITLED, 'academy.core status remains NOT_ENTITLED');

// Missing code test
$f_code = $fixtures['missing_code_package'];
$matrix_code = CapabilityResolver::resolve_matrix([
    'package_code'   => $f_code['package_code'], // academy.core & clinic.records omitted from ZIP
    'entitlements'   => $f_code['entitlements'],
    'admin_settings' => $f_code['admin_settings'],
    'environment'    => $f_code['environment'],
]);

assert_test($matrix_code['features']['academy.core']['code_present'] === false, 'academy.core code is marked absent');
assert_test($matrix_code['features']['academy.core']['is_active'] === false, 'academy.core is NOT active when code is missing');
assert_test($matrix_code['features']['academy.core']['status'] === CapabilityResolver::STATUS_MISSING_CODE, 'academy.core status is MISSING_CODE');
assert_test(!empty($matrix_code['features']['academy.core']['reason']), 'academy.core provides non-empty Persian reason for missing code');

// -------------------------------------------------------------
// Group 4: Disabled by Admin
// -------------------------------------------------------------
echo "\n-- Group 4: Disabled by Admin --\n";

$f_disabled = $fixtures['disabled_by_admin'];
$matrix_disabled = CapabilityResolver::resolve_matrix([
    'package_code'   => $f_disabled['package_code'],
    'entitlements'   => $f_disabled['entitlements'],
    'admin_settings' => $f_disabled['admin_settings'], // clinic.booking & psych.tests set to false
    'environment'    => $f_disabled['environment'],
]);

assert_test($matrix_disabled['features']['clinic.booking']['admin_enabled'] === false, 'clinic.booking admin_enabled is false');
assert_test($matrix_disabled['features']['clinic.booking']['status'] === CapabilityResolver::STATUS_DISABLED_BY_ADMIN, 'clinic.booking status is DISABLED_BY_ADMIN');
assert_test($matrix_disabled['features']['psych.tests']['admin_enabled'] === false, 'psych.tests admin_enabled is false');
assert_test($matrix_disabled['features']['psych.tests']['status'] === CapabilityResolver::STATUS_DISABLED_BY_ADMIN, 'psych.tests status is DISABLED_BY_ADMIN');
assert_test($matrix_disabled['features']['clinic.therapists']['status'] === CapabilityResolver::STATUS_PREREQUISITE_MISSING, 'clinic.therapists child feature gets PREREQUISITE_MISSING');

// -------------------------------------------------------------
// Group 5: Missing WooCommerce Restricts ONLY Commerce
// -------------------------------------------------------------
echo "\n-- Group 5: External Prerequisite Isolation (WooCommerce) --\n";

$f_woo = $fixtures['missing_woocommerce'];
$matrix_woo = CapabilityResolver::resolve_matrix([
    'package_code'   => $f_woo['package_code'],
    'entitlements'   => $f_woo['entitlements'],
    'admin_settings' => $f_woo['admin_settings'],
    'environment'    => $f_woo['environment'], // woocommerce_active = false
]);

assert_test($matrix_woo['features']['commerce.core']['is_active'] === false, 'commerce.core is inactive when WooCommerce missing');
assert_test($matrix_woo['features']['commerce.core']['status'] === CapabilityResolver::STATUS_PREREQUISITE_MISSING, 'commerce.core status is PREREQUISITE_MISSING');
assert_test(in_array('woocommerce', $matrix_woo['features']['commerce.core']['missing_prerequisites'], true), 'commerce.core lists woocommerce as missing prerequisite');
assert_test($matrix_woo['features']['commerce.cart']['status'] === CapabilityResolver::STATUS_PREREQUISITE_MISSING, 'commerce.cart status is PREREQUISITE_MISSING');
assert_test($matrix_woo['features']['commerce.payment']['status'] === CapabilityResolver::STATUS_PREREQUISITE_MISSING, 'commerce.payment status is PREREQUISITE_MISSING');
assert_test($matrix_woo['features']['commerce.wallet']['status'] === CapabilityResolver::STATUS_PREREQUISITE_MISSING, 'commerce.wallet status is PREREQUISITE_MISSING');

// Critical isolation check: Non-commerce features MUST REMAIN ACTIVE
assert_test($matrix_woo['features']['theme.base']['is_active'] === true, 'theme.base remains ACTIVE when WooCommerce missing');
assert_test($matrix_woo['features']['content.core']['is_active'] === true, 'content.core remains ACTIVE when WooCommerce missing');
assert_test($matrix_woo['features']['academy.core']['is_active'] === true, 'academy.core remains ACTIVE when WooCommerce missing');
assert_test($matrix_woo['features']['clinic.booking']['is_active'] === true, 'clinic.booking remains ACTIVE when WooCommerce missing');
assert_test($matrix_woo['features']['psych.tests']['is_active'] === true, 'psych.tests remains ACTIVE when WooCommerce missing');
assert_test($matrix_woo['features']['builder.core']['is_active'] === true, 'builder.core remains ACTIVE when WooCommerce missing');

// -------------------------------------------------------------
// Group 6: Standalone Mode Independence (No Carmilla Host Prereq)
// -------------------------------------------------------------
echo "\n-- Group 6: Host Standalone Independence --\n";

$f_theme_no_bridge = $fixtures['standalone_theme_no_bridge'];
$matrix_theme_only = CapabilityResolver::resolve_matrix([
    'package_code'   => $f_theme_no_bridge['package_code'],
    'entitlements'   => $f_theme_no_bridge['entitlements'],
    'admin_settings' => $f_theme_no_bridge['admin_settings'],
    'environment'    => $f_theme_no_bridge['environment'], // is_bridge_active = false
]);

assert_test($matrix_theme_only['features']['theme.base']['is_active'] === true, 'Theme-only: theme.base is ACTIVE');
assert_test($matrix_theme_only['features']['commerce.core']['is_active'] === true, 'Theme-only: commerce.core is ACTIVE without Bridge');
assert_test($matrix_theme_only['features']['clinic.booking']['is_active'] === true, 'Theme-only: clinic.booking is ACTIVE without Bridge');
assert_test($matrix_theme_only['features']['academy.core']['is_active'] === true, 'Theme-only: academy.core is ACTIVE without Bridge');

$f_plugin_no_theme = $fixtures['standalone_plugin_no_theme'];
$matrix_plugin_only = CapabilityResolver::resolve_matrix([
    'package_code'   => $f_plugin_no_theme['package_code'],
    'entitlements'   => $f_plugin_no_theme['entitlements'],
    'admin_settings' => $f_plugin_no_theme['admin_settings'],
    'environment'    => $f_plugin_no_theme['environment'], // is_theme_active = false
]);

assert_test($matrix_plugin_only['features']['plugin.base']['is_active'] === true, 'Plugin-only: plugin.base is ACTIVE');
assert_test($matrix_plugin_only['features']['commerce.core']['is_active'] === true, 'Plugin-only: commerce.core is ACTIVE without Theme');
assert_test($matrix_plugin_only['features']['clinic.booking']['is_active'] === true, 'Plugin-only: clinic.booking is ACTIVE without Theme');
assert_test($matrix_plugin_only['features']['academy.core']['is_active'] === true, 'Plugin-only: academy.core is ACTIVE without Theme');

// -------------------------------------------------------------
// Group 7: Release Readiness Gate (releaseReady = false)
// -------------------------------------------------------------
echo "\n-- Group 7: Release Readiness Gate --\n";

$f_release = $fixtures['release_gate_restricted'];
$matrix_release = CapabilityResolver::resolve_matrix([
    'package_code'    => $f_release['package_code'],
    'entitlements'    => $f_release['entitlements'],
    'admin_settings'  => $f_release['admin_settings'], // admin toggled ON
    'release_catalog' => $f_release['release_catalog'], // releaseReady = false
    'environment'     => $f_release['environment'],
]);

assert_test($matrix_release['features']['clinic.consultation']['release_ready'] === false, 'clinic.consultation release_ready is false');
assert_test($matrix_release['features']['clinic.consultation']['is_active'] === false, 'clinic.consultation is NOT active');
assert_test($matrix_release['features']['clinic.consultation']['status'] === CapabilityResolver::STATUS_RELEASE_GATE_RESTRICTED, 'clinic.consultation status is RELEASE_GATE_RESTRICTED');
assert_test(strpos($matrix_release['features']['clinic.consultation']['reason'], 'گیت انتشار') !== false, 'Reason clearly cites release gate');

// -------------------------------------------------------------
// Group 8: Summary Statistics Calculation
// -------------------------------------------------------------
echo "\n-- Group 8: Summary Statistics --\n";

$summary = $matrix_woo['summary'];
assert_test($summary['total'] > 15, 'Total features tracked in matrix summary');
assert_test($summary['missing_prereq_count'] === 4, 'Exactly 4 commerce features counted as missing prerequisite');
assert_test($summary['active_count'] > 0, 'Active features properly tallied in summary');

// -------------------------------------------------------------
// Group 9: Host Integrations & Backward-Compatible Aliases
// -------------------------------------------------------------
echo "\n-- Group 9: Host Integrations & Backward Compatibility --\n";

// ThemeHostAdapter query
if (class_exists('\Carmilla\Theme\ThemeHostAdapter')) {
    assert_test(method_exists('\Carmilla\Theme\ThemeHostAdapter', 'is_feature_enabled'), 'ThemeHostAdapter::is_feature_enabled exists');
    $theme_check = \Carmilla\Theme\ThemeHostAdapter::is_feature_enabled('blog');
    assert_test($theme_check === true, 'ThemeHostAdapter::is_feature_enabled(blog) resolves via kernel');
}

// Global template helper
if (function_exists('carmilla_feature_enabled')) {
    assert_test(carmilla_feature_enabled('blog') === true, 'carmilla_feature_enabled(blog) returns true');
    assert_test(carmilla_feature_enabled('stories') === true, 'carmilla_feature_enabled(stories) returns true');
}

// CB_Manifest_Controller query
if (class_exists('CB_Manifest_Controller')) {
    $manifest_features = CB_Manifest_Controller::resolve_features([
        'academy.quiz' => true,
        'academy.core' => false,
        'wallet'       => true,
        'commerce.core'=> false,
    ]);
    assert_test($manifest_features['academy.quiz'] === false, 'CB_Manifest_Controller: academy.quiz is false when academy.core false');
    assert_test($manifest_features['wallet'] === false, 'CB_Manifest_Controller: wallet is false when commerce.core false');
    assert_test($manifest_features['content.blog'] === true, 'CB_Manifest_Controller: content.blog is true');

    $invalid_configs = CB_Manifest_Controller::dependency_violations([
        'content.blog' => false,
        'academy.core' => false,
        'academy.quiz' => true,
    ]);
    assert_test(count($invalid_configs) === 1 && $invalid_configs[0] === 'academy.quiz', 'CB_Manifest_Controller: detects dependent violation');
}

// Kernel Matrix Query
if (class_exists('Carmilla_Kernel')) {
    \Carmilla_Kernel::resolve_features();
    $kernel_matrix = \Carmilla_Kernel::get_feature_matrix();
    assert_test(!empty($kernel_matrix['features']), 'Carmilla_Kernel::get_feature_matrix() returns structured feature rows');
    assert_test(\Carmilla_Kernel::is_feature_active('content.core') === true, 'Carmilla_Kernel::is_feature_active(content.core) is true');
}

echo "\n=================================================================\n";
echo " Acceptance Suite Complete: {$passed} Passed, {$failed} Failed\n";
echo "=================================================================\n";

exit($failed === 0 ? 0 : 1);
