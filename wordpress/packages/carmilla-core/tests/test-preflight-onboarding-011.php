<?php
/**
 * Real WordPress Verification Suite for P04-WPPLUGIN-CODE-011
 * Independent SKU-Aware Onboarding & Preflight Diagnostics (Theme-Only, Plugin-Only, Co-Install)
 *
 * Run inside Docker WordPress container:
 * docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-preflight-onboarding-011.php
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

use Carmilla\Core\Health\HealthChecker;

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
echo "  P04-WPPLUGIN-CODE-011: SKU Preflight & Independent Onboarding QA  \n";
echo "====================================================================\n\n";

$original_theme = get_stylesheet();
$plugin_slug    = 'carmilla-bridge/carmilla-bridge.php';
$created_users  = [];

function create_qa011_user(string $username, string $role): int {
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
    $u_admin = create_qa011_user('qa011_admin', 'administrator');
    $u_sub   = create_qa011_user('qa011_sub', 'subscriber');

    // -------------------------------------------------------------------------
    // Group 1: Shared vs SKU-Specific Prerequisites
    // -------------------------------------------------------------------------
    echo "Group 1: Shared vs SKU-Specific Prerequisites\n";

    $base_healthy_env = [
        'is_ssl'              => true,
        'permalink_structure' => '/%postname%/',
        'rest_available'      => true,
        'wp_cron_disabled'    => false,
        'woocommerce_active'  => false,
        'kernel_version'      => '1.0.0',
    ];

    $pf_base = HealthChecker::run_preflight('theme', ['sku_theme_base'], $base_healthy_env);
    assert_check($pf_base['ready'] === true, 'Base Theme SKU is ready even when WooCommerce is ABSENT');
    assert_check(empty($pf_base['blocking_issues']), 'Base Theme SKU has zero blocking issues without WooCommerce');
    assert_check($pf_base['checks']['woocommerce']['status'] === 'info', 'WooCommerce check is info (non-blocking) for Base SKU');
    assert_check($pf_base['checks']['woocommerce']['scope'] === 'sku:sku_commerce', 'WooCommerce check is scoped to sku:sku_commerce');
    assert_check($pf_base['checks']['https']['scope'] === 'shared', 'HTTPS check is scoped to shared');

    $pf_commerce_missing_woo = HealthChecker::run_preflight('plugin', ['sku_plugin_base', 'sku_commerce'], $base_healthy_env);
    assert_check($pf_commerce_missing_woo['ready'] === false, 'Commerce SKU is BLOCKED when WooCommerce is absent');
    assert_check(count($pf_commerce_missing_woo['blocking_issues']) === 1, 'Exactly 1 blocking issue (WooCommerce) reported');
    assert_check($pf_commerce_missing_woo['blocking_issues'][0]['id'] === 'woocommerce', 'Blocking issue id is woocommerce');
    assert_check(!empty($pf_commerce_missing_woo['blocking_issues'][0]['remediation']), 'Missing WooCommerce provides clear remediation');

    $commerce_ready_env = array_merge($base_healthy_env, ['woocommerce_active' => true]);
    $pf_commerce_ready = HealthChecker::run_preflight('plugin', ['sku_plugin_base', 'sku_commerce'], $commerce_ready_env);
    assert_check($pf_commerce_ready['ready'] === true, 'Commerce SKU is ready when WooCommerce is active');
    assert_check($pf_commerce_ready['checks']['woocommerce']['status'] === 'pass', 'WooCommerce check passes when active');

    // -------------------------------------------------------------------------
    // Group 2: Failure Simulation & Remediation Paths
    // -------------------------------------------------------------------------
    echo "\nGroup 2: Failure Simulation & Remediation Paths\n";

    $pf_bad_perm = HealthChecker::run_preflight('theme', ['sku_theme_base'], array_merge($base_healthy_env, ['permalink_structure' => '']));
    assert_check($pf_bad_perm['ready'] === false && $pf_bad_perm['checks']['permalinks']['status'] === 'fail', 'Plain permalinks fail preflight');
    assert_check(!empty($pf_bad_perm['checks']['permalinks']['remediation']), 'Plain permalinks failure includes remediation');

    $pf_bad_rest = HealthChecker::run_preflight('theme', ['sku_theme_base'], array_merge($base_healthy_env, ['rest_available' => false]));
    assert_check($pf_bad_rest['ready'] === false && $pf_bad_rest['checks']['rest_api']['status'] === 'fail', 'Disabled REST API fails preflight');
    assert_check(!empty($pf_bad_rest['checks']['rest_api']['remediation']), 'Disabled REST API includes remediation');

    $pf_bad_cron = HealthChecker::run_preflight('theme', ['sku_theme_base'], array_merge($base_healthy_env, ['wp_cron_disabled' => true]));
    assert_check($pf_bad_cron['checks']['wp_cron']['status'] === 'warning', 'DISABLE_WP_CRON reports warning status');
    assert_check(!empty($pf_bad_cron['warnings']) && !empty($pf_bad_cron['checks']['wp_cron']['remediation']), 'WP-Cron warning includes crontab remediation');

    $pf_no_ssl = HealthChecker::run_preflight('theme', ['sku_theme_base'], array_merge($base_healthy_env, ['is_ssl' => false]));
    assert_check($pf_no_ssl['ready'] === false && $pf_no_ssl['checks']['https']['status'] === 'fail', 'Non-HTTPS fails preflight');
    assert_check(!empty($pf_no_ssl['checks']['https']['remediation']), 'Non-HTTPS failure includes SSL remediation');

    $pf_old_kernel = HealthChecker::run_preflight('theme', ['sku_theme_base'], array_merge($base_healthy_env, ['kernel_version' => '0.5.0']));
    assert_check($pf_old_kernel['ready'] === false && $pf_old_kernel['checks']['kernel_version']['status'] === 'fail', 'Outdated kernel fails preflight');

    // -------------------------------------------------------------------------
    // Group 3: Secret & PII Redaction Guarantee
    // -------------------------------------------------------------------------
    echo "\nGroup 3: Secret & PII Redaction Guarantee\n";

    $sensitive_fixture = [
        'php_version'    => PHP_VERSION,
        'jwt_secret'     => 'super-secret-jwt-key-123',
        'db_password'    => 'my-db-pass',
        'api_key'        => 'ck_live_987654',
        'customer_email' => 'victim@example.com',
        'nested'         => [
            'auth_token' => 'Bearer abc.def.ghi',
            'safe_flag'  => true,
        ],
    ];
    $redacted = HealthChecker::redact_payload($sensitive_fixture);
    assert_check($redacted['php_version'] === PHP_VERSION, 'Non-sensitive fields preserved');
    assert_check($redacted['jwt_secret'] === '[REDACTED]', 'jwt_secret redacted');
    assert_check($redacted['db_password'] === '[REDACTED]', 'db_password redacted');
    assert_check($redacted['api_key'] === '[REDACTED]', 'api_key redacted');
    assert_check($redacted['customer_email'] === '[REDACTED]', 'customer_email (PII) redacted');
    assert_check($redacted['nested']['auth_token'] === '[REDACTED]', 'Nested auth_token redacted');
    assert_check($redacted['nested']['safe_flag'] === true, 'Nested safe_flag preserved');

    $diag = HealthChecker::get_diagnostics(['sku_theme_base'], $base_healthy_env);
    $diag_json = json_encode($diag);
    assert_check($diag['secrets_redacted'] === true, 'Diagnostics explicitly flags secrets_redacted=true');
    assert_check(strpos($diag_json, defined('DB_PASSWORD') ? DB_PASSWORD : 'impossible_sentinel_xyz') === false, 'Diagnostics JSON never leaks DB_PASSWORD');
    assert_check(strpos($diag_json, defined('AUTH_KEY') ? AUTH_KEY : 'impossible_sentinel_xyz') === false, 'Diagnostics JSON never leaks AUTH_KEY');

    // -------------------------------------------------------------------------
    // Group 4: REST Preflight Endpoint & Authorization
    // -------------------------------------------------------------------------
    echo "\nGroup 4: REST Preflight Endpoint & Authorization\n";

    do_action('rest_api_init');
    $server = rest_get_server();
    $routes = $server->get_routes();
    assert_check(isset($routes['/carmilla/v1/preflight']), 'GET /carmilla/v1/preflight route registered');
    assert_check(isset($routes['/carmilla/v1/preflight/recheck']), 'POST /carmilla/v1/preflight/recheck route registered');

    wp_set_current_user(0);
    $req_anon = new WP_REST_Request('GET', '/carmilla/v1/preflight');
    $res_anon = rest_do_request($req_anon);
    assert_check(in_array($res_anon->get_status(), [401, 403], true), 'Unauthenticated /preflight rejected (401/403)');

    wp_set_current_user($u_sub);
    $req_sub = new WP_REST_Request('GET', '/carmilla/v1/preflight');
    $res_sub = rest_do_request($req_sub);
    assert_check($res_sub->get_status() === 403, 'Subscriber /preflight rejected with 403 FORBIDDEN');

    wp_set_current_user($u_admin);
    $req_admin = new WP_REST_Request('GET', '/carmilla/v1/preflight');
    $res_admin = rest_do_request($req_admin);
    $data_admin = $res_admin->get_data();
    assert_check($res_admin->get_status() === 200, 'Admin GET /preflight returns 200 OK');
    assert_check(!empty($data_admin['secrets_redacted']) && isset($data_admin['checks']), 'Admin GET /preflight returns redacted diagnostics and checks');

    $req_recheck = new WP_REST_Request('POST', '/carmilla/v1/preflight/recheck');
    $res_recheck = rest_do_request($req_recheck);
    assert_check($res_recheck->get_status() === 200, 'Admin POST /preflight/recheck returns 200 OK');
    wp_set_current_user(0);

    // -------------------------------------------------------------------------
    // Group 5: Independent Onboarding Parity Across All 3 Host Modes
    // -------------------------------------------------------------------------
    echo "\nGroup 5: Independent Onboarding Parity Across All 3 Host Modes\n";

    // Mode 1: Theme-Only (Bridge Deactivated)
    switch_theme('carmilla-theme');
    deactivate_plugins($plugin_slug);
    $theme_adapter_path = get_theme_file_path('inc/class-theme-host-adapter.php');
    if (file_exists($theme_adapter_path)) {
        require_once $theme_adapter_path;
    }
    $pf_theme_only = \Carmilla\Theme\ThemeHostAdapter::get_onboarding_preflight(['sku_theme_base'], $base_healthy_env);
    assert_check($pf_theme_only['ready'] === true && $pf_theme_only['host_context'] === 'theme', 'Theme-Only: independent onboarding preflight succeeds without Bridge/Woo');

    // Mode 2: Plugin-Only (Twenty Twenty-Four + Bridge)
    activate_plugin($plugin_slug);
    switch_theme('twentytwentyfour');
    $pf_plugin_only = \CB_Standalone_Boot::get_onboarding_preflight(['sku_plugin_base'], $base_healthy_env);
    assert_check($pf_plugin_only['ready'] === true && $pf_plugin_only['host_context'] === 'plugin', 'Plugin-Only (TT4): independent onboarding preflight succeeds on non-Carmilla theme');

    // Mode 3: Co-Install
    switch_theme('carmilla-theme');
    $pf_coinstall = HealthChecker::run_preflight('co-install', ['sku_theme_base', 'sku_plugin_base'], $base_healthy_env);
    assert_check($pf_coinstall['ready'] === true && $pf_coinstall['host_context'] === 'co-install', 'Co-Install: preflight succeeds across both hosts');

} finally {
    switch_theme($original_theme ?: 'carmilla-theme');
    if (!is_plugin_active($plugin_slug)) {
        activate_plugin($plugin_slug);
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
