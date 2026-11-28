<?php
/**
 * Real WordPress Verification Suite for P04-WPPLUGIN-CODE-014
 * Shared Operational Settings API, Access Control, CSRF, Entitlement Isolation & Audit
 *
 * Run inside Docker WordPress container:
 * docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-settings-api-014.php
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

use Carmilla\Core\Settings\SettingsService;

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
echo "  P04-WPPLUGIN-CODE-014: Shared Settings API, CSRF & Audit QA       \n";
echo "====================================================================\n\n";

$original_theme = get_stylesheet();
$plugin_slug    = 'carmilla-bridge/carmilla-bridge.php';
$created_users  = [];
$orig_settings  = get_option(SettingsService::OPTION_KEY, null);
$orig_audit     = get_option(SettingsService::AUDIT_LOG_KEY, null);

function create_qa014_user(string $username, string $role): int {
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
    $u_admin = create_qa014_user('qa014_admin', 'administrator');
    $u_sub   = create_qa014_user('qa014_sub', 'subscriber');

    do_action('rest_api_init');
    $routes = rest_get_server()->get_routes();

    // -------------------------------------------------------------------------
    // Group 1: REST Route Registration & Authorization
    // -------------------------------------------------------------------------
    echo "Group 1: REST Route Registration & Authorization\n";

    assert_check(isset($routes['/carmilla/v1/settings']), '/carmilla/v1/settings route registered');
    assert_check(isset($routes['/carmilla/v1/settings/audit']), '/carmilla/v1/settings/audit route registered');

    wp_set_current_user(0);
    $res_anon = rest_do_request(new WP_REST_Request('GET', '/carmilla/v1/settings'));
    assert_check(in_array($res_anon->get_status(), [401, 403], true), 'Unauthenticated GET /settings rejected (401/403)');

    wp_set_current_user($u_sub);
    $res_sub_get = rest_do_request(new WP_REST_Request('GET', '/carmilla/v1/settings'));
    assert_check($res_sub_get->get_status() === 403, 'Subscriber GET /settings rejected with 403 FORBIDDEN');

    $req_sub_post = new WP_REST_Request('POST', '/carmilla/v1/settings');
    $req_sub_post->set_header('Content-Type', 'application/json');
    $req_sub_post->set_body(json_encode(['sms_provider' => 'ghasedak']));
    $res_sub_post = rest_do_request($req_sub_post);
    assert_check($res_sub_post->get_status() === 403, 'Subscriber POST /settings rejected with 403 FORBIDDEN');

    // -------------------------------------------------------------------------
    // Group 2: CSRF / Nonce, Unknown Option, Entitlement & Presentation Guards
    // -------------------------------------------------------------------------
    echo "\nGroup 2: CSRF, Unknown Option, Entitlement & Presentation Guards\n";

    wp_set_current_user($u_admin);
    $valid_nonce = wp_create_nonce('wp_rest');

    // 1. Missing CSRF nonce
    $req_no_csrf = new WP_REST_Request('POST', '/carmilla/v1/settings');
    $req_no_csrf->set_header('Content-Type', 'application/json');
    $req_no_csrf->set_body(json_encode(['sms_provider' => 'ghasedak']));
    $res_no_csrf = rest_do_request($req_no_csrf);
    assert_check($res_no_csrf->get_status() === 403 && $res_no_csrf->get_data()['code'] === 'CSRF_INVALID', 'Missing CSRF nonce rejected with 403 CSRF_INVALID');

    // 2. Entitlement write forgery attempt
    $req_entitlement = new WP_REST_Request('POST', '/carmilla/v1/settings');
    $req_entitlement->set_header('X-WP-Nonce', $valid_nonce);
    $req_entitlement->set_header('Content-Type', 'application/json');
    $req_entitlement->set_body(json_encode(['entitlement_academy' => true, 'skus' => ['sku_commerce']]));
    $res_entitlement = rest_do_request($req_entitlement);
    assert_check($res_entitlement->get_status() === 403 && $res_entitlement->get_data()['code'] === 'ENTITLEMENT_WRITE_FORBIDDEN', 'Entitlement forgery rejected with 403 ENTITLEMENT_WRITE_FORBIDDEN');

    // 3. Presentation key separated
    $req_pres = new WP_REST_Request('POST', '/carmilla/v1/settings');
    $req_pres->set_header('X-WP-Nonce', $valid_nonce);
    $req_pres->set_header('Content-Type', 'application/json');
    $req_pres->set_body(json_encode(['primary_color' => '#FF0000']));
    $res_pres = rest_do_request($req_pres);
    assert_check($res_pres->get_status() === 400 && $res_pres->get_data()['code'] === 'PRESENTATION_KEY_SEPARATED', 'Theme presentation key rejected from operational settings API');

    // 4. Unknown option rejected
    $req_unknown = new WP_REST_Request('POST', '/carmilla/v1/settings');
    $req_unknown->set_header('X-WP-Nonce', $valid_nonce);
    $req_unknown->set_header('Content-Type', 'application/json');
    $req_unknown->set_body(json_encode(['unknown_option_xyz' => 'hack']));
    $res_unknown = rest_do_request($req_unknown);
    assert_check($res_unknown->get_status() === 400 && $res_unknown->get_data()['code'] === 'UNKNOWN_OPTION', 'Unknown option rejected with 400 UNKNOWN_OPTION');

    // 5. Out-of-bounds value rejected
    $req_bad_val = new WP_REST_Request('POST', '/carmilla/v1/settings');
    $req_bad_val->set_header('X-WP-Nonce', $valid_nonce);
    $req_bad_val->set_header('Content-Type', 'application/json');
    $req_bad_val->set_body(json_encode(['max_upload_size_mb' => 9999]));
    $res_bad_val = rest_do_request($req_bad_val);
    assert_check($res_bad_val->get_status() === 400 && $res_bad_val->get_data()['code'] === 'INVALID_OPTION_VALUE', 'Out-of-bounds integer rejected with 400 INVALID_OPTION_VALUE');

    // -------------------------------------------------------------------------
    // Group 3: Canonical Save, Audit Log & Cross-Host Parity
    // -------------------------------------------------------------------------
    echo "\nGroup 3: Canonical Save, Audit Log & Cross-Host Parity\n";

    // Mode 1: Theme-Only save via ThemeHostAdapter
    switch_theme('carmilla-theme');
    deactivate_plugins($plugin_slug);
    $theme_adapter = get_theme_file_path('inc/class-theme-host-adapter.php');
    if (file_exists($theme_adapter)) {
        require_once $theme_adapter;
    }
    $save_theme = \Carmilla\Theme\ThemeHostAdapter::save_operational_settings(
        ['sms_provider' => 'ghasedak', 'max_upload_size_mb' => 25],
        $u_admin,
        $valid_nonce,
        true
    );
    assert_check($save_theme['ok'] === true && $save_theme['code'] === 'SETTINGS_SAVED', 'Theme-Only: valid operational settings saved');
    assert_check(SettingsService::get('sms_provider') === 'ghasedak', 'Canonical storage reflects sms_provider=ghasedak');

    // Mode 2: Plugin-Only (TT4) reads exact same state and updates via REST
    activate_plugin($plugin_slug);
    switch_theme('twentytwentyfour');
    assert_check(SettingsService::get('sms_provider') === 'ghasedak', 'Plugin-Only (TT4): reads identical site state saved by Theme host');
    assert_check(SettingsService::get('max_upload_size_mb') === 25, 'Plugin-Only (TT4): reads identical max_upload_size_mb=25');

    $save_plugin = \CB_Standalone_Boot::save_operational_settings(
        ['payment_gateway' => 'zibal'],
        $u_admin,
        $valid_nonce,
        true
    );
    assert_check($save_plugin['ok'] === true, 'Plugin-Only (TT4): updates payment_gateway=zibal');

    // Mode 3: Co-Install GET /settings & /settings/audit
    switch_theme('carmilla-theme');
    $res_get = rest_do_request(new WP_REST_Request('GET', '/carmilla/v1/settings'));
    $data_get = $res_get->get_data();
    assert_check($res_get->get_status() === 200, 'Co-Install GET /settings returns 200 OK');
    assert_check(($data_get['operational']['sms_provider'] ?? '') === 'ghasedak', 'Co-Install GET /settings shows sms_provider=ghasedak');
    assert_check(($data_get['operational']['payment_gateway'] ?? '') === 'zibal', 'Co-Install GET /settings shows payment_gateway=zibal');
    assert_check($data_get['entitlement_readonly'] === true, 'Co-Install GET /settings declares entitlement_readonly=true');

    $res_audit = rest_do_request(new WP_REST_Request('GET', '/carmilla/v1/settings/audit'));
    $data_audit = $res_audit->get_data();
    assert_check($res_audit->get_status() === 200 && !empty($data_audit['secrets_redacted']), 'GET /settings/audit returns redacted audit log');
    assert_check(!empty($data_audit['audit']) && count($data_audit['audit']) >= 2, 'Audit log recorded both Theme and Plugin host updates');

} finally {
    switch_theme($original_theme ?: 'carmilla-theme');
    if (!is_plugin_active($plugin_slug)) {
        activate_plugin($plugin_slug);
    }
    if ($orig_settings === null) {
        delete_option(SettingsService::OPTION_KEY);
    } else {
        update_option(SettingsService::OPTION_KEY, $orig_settings);
    }
    if ($orig_audit === null) {
        delete_option(SettingsService::AUDIT_LOG_KEY);
    } else {
        update_option(SettingsService::AUDIT_LOG_KEY, $orig_audit);
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
