<?php
/**
 * Real WordPress Verification Suite for P04-ENTITLEMENT-CODE-041
 * Shared Purchased Features & Builder Targets Toggle Panel Across Both Hosts
 *
 * Run inside Docker WordPress container:
 * docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-entitlement-panel-041.php
 */

declare(strict_types=1);

$wp_load_paths = [
    '/var/www/html/wp-load.php',
    dirname(__DIR__, 5) . '/wp-load.php',
];
$loaded = false;
foreach ($wp_load_paths as $path) {
    if (file_exists($path)) {
        require_once $path;
        $loaded = true;
        break;
    }
}
if (!$loaded) {
    fwrite(STDERR, "FATAL: Could not locate wp-load.php\n");
    exit(1);
}

require_once ABSPATH . 'wp-admin/includes/plugin.php';
require_once ABSPATH . 'wp-admin/includes/user.php';

use Carmilla\Core\Entitlement\FeatureToggleService;

$total_tests  = 0;
$passed_tests = 0;
$failed_tests = 0;

function assert_check(bool $condition, string $label): void {
    global $total_tests, $passed_tests, $failed_tests;
    $total_tests++;
    if ($condition) {
        $passed_tests++;
        echo "  [PASS] {$label}\n";
    } else {
        $failed_tests++;
        echo "  [FAIL] {$label}\n";
    }
}

echo "====================================================================\n";
echo "  P04-ENTITLEMENT-CODE-041: Shared Toggle Panel & Builder Targets   \n";
echo "====================================================================\n\n";

$original_theme = get_stylesheet();
$plugin_slug    = 'carmilla-bridge/carmilla-bridge.php';
$created_users  = [];
$created_post   = 0;
$orig_features  = get_option(FeatureToggleService::FEATURES_OPTION, null);
$orig_builders  = get_option(FeatureToggleService::BUILDERS_OPTION, null);

function create_qa041_user(string $username, string $role): int {
    global $created_users;
    $existing = get_user_by('login', $username);
    if ($existing) {
        wp_delete_user($existing->ID);
    }
    $uid = wp_create_user($username, 'TestPass!2026#' . $username, $username . '@synthetic.local');
    if (is_wp_error($uid)) {
        fwrite(STDERR, "Failed to create synthetic user {$username}: " . $uid->get_error_message() . "\n");
        exit(1);
    }
    (new WP_User($uid))->set_role($role);
    $created_users[] = $uid;
    return $uid;
}

try {
    $u_admin = create_qa041_user('qa041_admin', 'administrator');
    $u_sub   = create_qa041_user('qa041_sub', 'subscriber');

    $created_post = wp_insert_post([
        'post_title'  => 'QA041 Academy Fixture Course',
        'post_status' => 'publish',
        'post_type'   => 'post',
    ]);

    do_action('rest_api_init');
    $routes = rest_get_server()->get_routes();

    // -------------------------------------------------------------------------
    // Group 1: Panel Badges (Active, Off, Not-Entitled, Not-Packaged, Missing-Prereq)
    // -------------------------------------------------------------------------
    echo "Group 1: Panel Badges & Packaging/Entitlement Isolation\n";

    $mixed_matrix_args = [
        'package_code' => [
            'theme.base'     => true,
            'content.core'   => true,
            'academy.core'   => true,
            'commerce.core'  => true,
            'clinic.booking' => false, // NOT PACKAGED IN ZIP
        ],
        'entitlements' => [
            'skus'     => ['sku_theme_base', 'sku_academy', 'sku_commerce'],
            'builders' => ['android' => true, 'ios' => true, 'pwa' => true, 'desktop' => false],
        ],
        'admin_settings' => [
            'academy.core'  => false, // OFF BY ADMIN
            'commerce.core' => true,  // ON BY ADMIN, BUT MISSING WOOCOMMERCE
        ],
        'environment' => [
            'woocommerce_active' => false, // MISSING PREREQ FOR COMMERCE
        ],
    ];

    $panel = FeatureToggleService::get_panel_state($mixed_matrix_args);
    assert_check(($panel['features']['content.core']['badge'] ?? '') === 'ACTIVE', 'content.core badge is ACTIVE');
    assert_check(($panel['features']['academy.core']['badge'] ?? '') === 'OFF', 'academy.core badge is OFF when disabled by admin');
    assert_check(($panel['features']['clinic.booking']['badge'] ?? '') === 'NOT_PACKAGED', 'clinic.booking badge is NOT_PACKAGED when absent from ZIP');
    assert_check(!empty($panel['features']['clinic.booking']['upgrade_message']), 'NOT_PACKAGED includes new-version upgrade message');
    assert_check(($panel['features']['clinic.booking']['can_toggle'] ?? true) === false, 'NOT_PACKAGED cannot be toggled on');
    assert_check(($panel['features']['psych.tests']['badge'] ?? '') === 'NOT_ENTITLED', 'psych.tests badge is NOT_ENTITLED when not bought');
    assert_check(($panel['features']['commerce.core']['badge'] ?? '') === 'MISSING_PREREQUISITE', 'commerce.core badge is MISSING_PREREQUISITE when WooCommerce absent');

    // -------------------------------------------------------------------------
    // Group 2: Security Gates (Non-Admin, CSRF, Not-Packaged, Invalid Dependencies)
    // -------------------------------------------------------------------------
    echo "\nGroup 2: Non-Admin, CSRF, Packaging & Dependency Guards\n";

    wp_set_current_user($u_admin);
    $valid_nonce = wp_create_nonce('wp_rest');

    // Non-admin rejected
    $res_sub = FeatureToggleService::save_toggles(['academy.core' => false], $u_sub, $valid_nonce, true);
    assert_check(!$res_sub['ok'] && $res_sub['code'] === 'FORBIDDEN' && $res_sub['status'] === 403, 'Non-admin subscriber rejected (403 FORBIDDEN)');

    // Missing CSRF nonce rejected
    $res_csrf = FeatureToggleService::save_toggles(['academy.core' => false], $u_admin, 'bad_nonce', true);
    assert_check(!$res_csrf['ok'] && $res_csrf['code'] === 'CSRF_INVALID' && $res_csrf['status'] === 403, 'Invalid CSRF nonce rejected (403 CSRF_INVALID)');

    // Attempting to enable NOT_PACKAGED feature rejected
    $res_not_pkg = FeatureToggleService::save_toggles(['clinic.booking' => true], $u_admin, $valid_nonce, true, [], $mixed_matrix_args);
    assert_check(!$res_not_pkg['ok'] && $res_not_pkg['code'] === 'FEATURE_NOT_PACKAGED' && $res_not_pkg['status'] === 403, 'Enabling NOT_PACKAGED feature rejected (403 FEATURE_NOT_PACKAGED)');

    // Attempting to enable NOT_ENTITLED feature rejected
    $res_not_ent = FeatureToggleService::save_toggles(['psych.tests' => true], $u_admin, $valid_nonce, true, [], $mixed_matrix_args);
    assert_check(!$res_not_ent['ok'] && $res_not_ent['code'] === 'FEATURE_NOT_ENTITLED' && $res_not_ent['status'] === 403, 'Enabling NOT_ENTITLED feature rejected (403 FEATURE_NOT_ENTITLED)');

    // Invalid dependency: commerce.cart=true while commerce.core=false
    $res_dep = FeatureToggleService::save_toggles(['commerce.core' => false, 'commerce.cart' => true], $u_admin, $valid_nonce, true);
    assert_check(!$res_dep['ok'] && $res_dep['code'] === 'INVALID_DEPENDENCIES' && $res_dep['status'] === 400, 'Invalid dependency (commerce.cart without commerce.core) rejected (400)');
    assert_check(!empty($res_dep['violations']) && in_array('commerce.cart', $res_dep['violations'], true), 'Dependency violation explicitly names commerce.cart');

    // -------------------------------------------------------------------------
    // Group 3: Independent Builder Targets & Data Retention on Feature Off
    // -------------------------------------------------------------------------
    echo "\nGroup 3: Independent Builder Targets & Data Retention\n";

    // Enable android, ios, pwa; then disable ONLY ios
    $res_b1 = FeatureToggleService::save_toggles(
        ['academy.core' => false],
        $u_admin,
        $valid_nonce,
        true,
        ['android' => true, 'ios' => false, 'pwa' => true],
        $mixed_matrix_args
    );
    assert_check($res_b1['ok'] === true && $res_b1['code'] === 'TOGGLES_SAVED', 'Saved feature toggle + independent builder targets');
    assert_check($res_b1['panel']['builders']['ios']['enabled'] === false, 'iOS builder target disabled');
    assert_check($res_b1['panel']['builders']['android']['enabled'] === true, 'Android builder target unaffected by iOS off');
    assert_check($res_b1['panel']['builders']['pwa']['enabled'] === true, 'PWA builder target unaffected by iOS off');
    assert_check(get_post($created_post) !== null, 'Disabling academy.core did NOT delete existing site post');

    // Unentitled builder target (desktop=false in mixed_matrix_args) cannot be enabled
    $res_b_unent = FeatureToggleService::save_toggles([], $u_admin, $valid_nonce, true, ['desktop' => true], $mixed_matrix_args);
    assert_check(!$res_b_unent['ok'] && $res_b_unent['code'] === 'BUILDER_NOT_ENTITLED', 'Enabling unentitled desktop target rejected (403)');

    // -------------------------------------------------------------------------
    // Group 4: Cross-Host Save/Read Parity (Theme-Only vs Plugin-Only TT4)
    // -------------------------------------------------------------------------
    echo "\nGroup 4: Cross-Host Save/Read Parity & REST Endpoint\n";

    assert_check(isset($routes['/carmilla/v1/entitlements/panel']), 'REST /carmilla/v1/entitlements/panel registered');

    // Mode 1: Theme-Only (Bridge Deactivated)
    switch_theme('carmilla-theme');
    deactivate_plugins($plugin_slug);
    $theme_adapter = get_theme_file_path('inc/class-theme-host-adapter.php');
    if (file_exists($theme_adapter)) {
        require_once $theme_adapter;
    }
    $save_theme = \Carmilla\Theme\ThemeHostAdapter::save_feature_toggles(['academy.core' => true], $u_admin, $valid_nonce, true);
    assert_check($save_theme['ok'] === true, 'Theme-Only: saved academy.core=true without Bridge');

    // Mode 2: Plugin-Only (Twenty Twenty-Four + Bridge)
    activate_plugin($plugin_slug);
    switch_theme('twentytwentyfour');
    $panel_plugin = \CB_Standalone_Boot::get_feature_panel();
    assert_check(($panel_plugin['features']['academy.core']['admin_enabled'] ?? false) === true, 'Plugin-Only (TT4): reads exact same academy.core=true saved by Theme');

    $save_plugin = \CB_Standalone_Boot::save_feature_toggles(['academy.core' => false], $u_admin, $valid_nonce, true);
    assert_check($save_plugin['ok'] === true, 'Plugin-Only (TT4): updated academy.core=false');

    // Verify Theme reads updated state
    switch_theme('carmilla-theme');
    $panel_theme = \Carmilla\Theme\ThemeHostAdapter::get_feature_panel();
    assert_check(($panel_theme['features']['academy.core']['admin_enabled'] ?? true) === false, 'Theme host reads exact same academy.core=false updated by Plugin');

} finally {
    switch_theme($original_theme ?: 'carmilla-theme');
    if (!is_plugin_active($plugin_slug)) {
        activate_plugin($plugin_slug);
    }
    if ($orig_features === null) {
        delete_option(FeatureToggleService::FEATURES_OPTION);
    } else {
        update_option(FeatureToggleService::FEATURES_OPTION, $orig_features);
    }
    if ($orig_builders === null) {
        delete_option(FeatureToggleService::BUILDERS_OPTION);
    } else {
        update_option(FeatureToggleService::BUILDERS_OPTION, $orig_builders);
    }
    if ($created_post > 0) {
        wp_delete_post($created_post, true);
    }
    foreach ($created_users as $uid) {
        wp_delete_user($uid);
    }
    wp_set_current_user(0);
}

echo "\n====================================================================\n";
echo "Results: Total: {$total_tests} | Passed: {$passed_tests} | Failed: {$failed_tests}\n";
echo "====================================================================\n";

exit($failed_tests > 0 ? 1 : 0);
