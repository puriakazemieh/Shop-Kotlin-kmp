<?php
/**
 * Real WordPress Verification Suite for P04-WPPLUGIN-CODE-012
 * Dual-Host Lifecycle, Data Retention on Deactivation/Theme-Switch, Last-Host Halt & Explicit Opt-In Cleanup
 *
 * Run inside Docker WordPress container:
 * docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-lifecycle-retention-012.php
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

use Carmilla\Core\Lifecycle\LifecycleManager;

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
echo "  P04-WPPLUGIN-CODE-012: Dual-Host Lifecycle & Data Retention QA    \n";
echo "====================================================================\n\n";

$original_theme = get_stylesheet();
$plugin_slug    = 'carmilla-bridge/carmilla-bridge.php';
$created_users  = [];
$created_post   = 0;

function create_qa012_user(string $username, string $role): int {
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
    $u_admin = create_qa012_user('qa012_admin', 'administrator');
    $u_sub   = create_qa012_user('qa012_sub', 'subscriber');

    // Seed synthetic site data (post + carmilla_settings option)
    $created_post = wp_insert_post([
        'post_title'   => 'QA012 Retention Fixture Course',
        'post_content' => 'Synthetic content preserved across lifecycle transitions.',
        'post_status'  => 'publish',
        'post_type'    => 'post',
    ]);
    update_option('carmilla_settings', ['retention_marker' => 'qa012_keep_me']);
    update_option(LifecycleManager::OPT_DELETE_ON_UNINSTALL, false);

    // -------------------------------------------------------------------------
    // Group 1: Activation & Default Retention Policy
    // -------------------------------------------------------------------------
    echo "Group 1: Activation & Default Retention Policy\n";

    LifecycleManager::reset_memory_checkpoints();
    LifecycleManager::handle_activation();

    assert_check(get_option('carmilla_lifecycle_status') === 'active', 'Activation records lifecycle status=active');
    assert_check(!get_option(LifecycleManager::OPT_DELETE_ON_UNINSTALL, false), 'Default delete_on_uninstall is false');

    $status_coinstall = LifecycleManager::get_host_lifecycle_status(true, true);
    assert_check($status_coinstall['services_running'] === true, 'Co-Install: services_running=true');
    assert_check($status_coinstall['execution_status'] === 'active', 'Co-Install: execution_status=active');
    assert_check($status_coinstall['adoption_task_ref'] === 'P04-WORDPRESS-DATA-042', 'Adoption/license transfer explicitly references P04-WORDPRESS-DATA-042');

    // -------------------------------------------------------------------------
    // Group 2: Deactivating One Host in Co-Install Preserves Data & Services
    // -------------------------------------------------------------------------
    echo "\nGroup 2: Deactivating One Host Preserves Data & Continues Services\n";

    // Deactivate plugin while Carmilla Theme remains active
    deactivate_plugins($plugin_slug);
    LifecycleManager::reset_memory_checkpoints();
    LifecycleManager::handle_deactivation(true);

    $status_theme_only = LifecycleManager::get_host_lifecycle_status(true, false);
    assert_check($status_theme_only['services_running'] === true, 'Theme-Only after plugin deactivation: services continue running');
    assert_check($status_theme_only['execution_status'] === 'active_single_host', 'Theme-Only: execution_status=active_single_host');
    assert_check(get_option('carmilla_lifecycle_status') === 'active_single_host', 'Persisted status updated to active_single_host');
    assert_check(get_post($created_post) !== null, 'Synthetic post preserved after plugin deactivation');
    assert_check((get_option('carmilla_settings')['retention_marker'] ?? '') === 'qa012_keep_me', 'carmilla_settings option preserved after plugin deactivation');

    // Switch theme to TT4 while Bridge Plugin is active
    activate_plugin($plugin_slug);
    switch_theme('twentytwentyfour');
    LifecycleManager::reset_memory_checkpoints();
    LifecycleManager::handle_theme_switch('twentytwentyfour', null, null, true);

    $status_plugin_only = LifecycleManager::get_host_lifecycle_status(false, true);
    assert_check($status_plugin_only['services_running'] === true, 'Plugin-Only after theme switch: services continue running');
    assert_check($status_plugin_only['execution_status'] === 'active_single_host', 'Plugin-Only: execution_status=active_single_host');
    assert_check(get_post($created_post) !== null, 'Synthetic post preserved after theme switch');
    assert_check((get_option('carmilla_settings')['retention_marker'] ?? '') === 'qa012_keep_me', 'carmilla_settings preserved after theme switch');

    // -------------------------------------------------------------------------
    // Group 3: Shutting Down Last Host Halts Execution Explicitly & Preserves Data
    // -------------------------------------------------------------------------
    echo "\nGroup 3: Shutting Down Last Host Halts Execution & Preserves Data\n";

    deactivate_plugins($plugin_slug);
    LifecycleManager::reset_memory_checkpoints();
    LifecycleManager::handle_deactivation(false);

    $status_halted = LifecycleManager::get_host_lifecycle_status(false, false);
    assert_check($status_halted['services_running'] === false, 'Last host off: services_running=false');
    assert_check($status_halted['execution_status'] === 'halted_no_active_host', 'Last host off: execution_status=halted_no_active_host');
    assert_check(get_option('carmilla_lifecycle_status') === 'halted_no_active_host', 'Persisted status explicitly records halted_no_active_host');
    assert_check(get_post($created_post) !== null, 'Last host off: synthetic post STILL preserved');
    assert_check((get_option('carmilla_settings')['retention_marker'] ?? '') === 'qa012_keep_me', 'Last host off: carmilla_settings STILL preserved');

    // -------------------------------------------------------------------------
    // Group 4: Uninstall Without Opt-In Preserves Data
    // -------------------------------------------------------------------------
    echo "\nGroup 4: Uninstall Without Opt-In Preserves Data\n";

    update_option(LifecycleManager::OPT_DELETE_ON_UNINSTALL, false);
    LifecycleManager::reset_memory_checkpoints();
    LifecycleManager::handle_uninstall();

    assert_check((get_option('carmilla_settings')['retention_marker'] ?? '') === 'qa012_keep_me', 'Uninstall without opt-in preserves carmilla_settings');
    assert_check(get_post($created_post) !== null, 'Uninstall without opt-in preserves posts');

    // -------------------------------------------------------------------------
    // Group 5: Explicit Cleanup Security (Capability + Nonce + Opt-In)
    // -------------------------------------------------------------------------
    echo "\nGroup 5: Explicit Cleanup Security (Capability + Nonce + Opt-In)\n";

    wp_set_current_user($u_admin);
    $valid_nonce = wp_create_nonce(LifecycleManager::CLEANUP_NONCE_ACTION);

    // 1. Non-admin (subscriber) rejected even with nonce + opt-in
    wp_set_current_user($u_sub);
    $sub_nonce = wp_create_nonce(LifecycleManager::CLEANUP_NONCE_ACTION);
    $res_sub = LifecycleManager::execute_explicit_cleanup($u_sub, $sub_nonce, true);
    assert_check(!$res_sub['ok'] && $res_sub['code'] === 'FORBIDDEN' && $res_sub['status'] === 403, 'Non-admin subscriber rejected with 403 FORBIDDEN');
    assert_check((get_option('carmilla_settings')['retention_marker'] ?? '') === 'qa012_keep_me', 'Data preserved when non-admin attempts cleanup');

    // 2. Admin with invalid nonce rejected
    wp_set_current_user($u_admin);
    $res_bad_nonce = LifecycleManager::execute_explicit_cleanup($u_admin, 'bad_nonce_value', true);
    assert_check(!$res_bad_nonce['ok'] && $res_bad_nonce['code'] === 'INVALID_NONCE' && $res_bad_nonce['status'] === 403, 'Invalid nonce rejected with 403 INVALID_NONCE');
    assert_check((get_option('carmilla_settings')['retention_marker'] ?? '') === 'qa012_keep_me', 'Data preserved when nonce is invalid');

    // 3. Admin with valid nonce but WITHOUT opt-in rejected
    $res_no_optin = LifecycleManager::execute_explicit_cleanup($u_admin, $valid_nonce, false);
    assert_check(!$res_no_optin['ok'] && $res_no_optin['code'] === 'OPT_IN_REQUIRED' && $res_no_optin['status'] === 400, 'Missing opt-in rejected with 400 OPT_IN_REQUIRED');
    assert_check((get_option('carmilla_settings')['retention_marker'] ?? '') === 'qa012_keep_me', 'Data preserved when opt-in is false');

    // 4. Admin with valid nonce AND explicit opt-in succeeds
    $res_ok = LifecycleManager::execute_explicit_cleanup($u_admin, $valid_nonce, true);
    assert_check($res_ok['ok'] && $res_ok['code'] === 'CLEANUP_EXECUTED' && $res_ok['status'] === 200, 'Admin + valid nonce + opt-in executes explicit cleanup');
    assert_check(get_option('carmilla_settings', null) === null, 'carmilla_settings removed after explicit opt-in cleanup');
    assert_check(get_post($created_post) !== null, 'User/site posts remain intact after option cleanup');

} finally {
    // Restore clean Co-Install state
    switch_theme($original_theme ?: 'carmilla-theme');
    if (!is_plugin_active($plugin_slug)) {
        activate_plugin($plugin_slug);
    }
    if ($created_post > 0) {
        wp_delete_post($created_post, true);
    }
    foreach ($created_users as $uid) {
        wp_delete_user($uid);
    }
    wp_set_current_user(0);
    LifecycleManager::reset_memory_checkpoints();
    LifecycleManager::handle_activation();
}

echo "\n====================================================================\n";
echo "Results: Total: {$total_tests} | Passed: {$passed_tests} | Failed: {$failed_tests}\n";
echo "====================================================================\n";

exit($failed_tests > 0 ? 1 : 0);
