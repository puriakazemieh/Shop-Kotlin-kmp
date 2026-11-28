<?php
/**
 * Real WordPress Verification Suite for P04-WPPLUGIN-CODE-013
 * Host-Agnostic Privacy Export, Erasure/Anonymization, Co-Install Idempotency & Retention Policy
 *
 * Run inside Docker WordPress container:
 * docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-privacy-retention-013.php
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

use Carmilla\Core\Privacy\PrivacyManager;

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
echo "  P04-WPPLUGIN-CODE-013: Privacy Export, Erase & Retention QA       \n";
echo "====================================================================\n\n";

$original_theme = get_stylesheet();
$plugin_slug    = 'carmilla-bridge/carmilla-bridge.php';
$created_users  = [];
$created_posts  = [];
$orig_features  = get_option('cb_manifest_features', null);

function create_qa013_user(string $username, string $role): int {
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

function seed_user_privacy_fixture(int $uid): int {
    global $created_posts;
    update_user_meta($uid, 'cb_enrolled_courses', [101, 202]);
    update_user_meta($uid, 'cb_course_progress', [101 => 75, 202 => 20]);
    update_user_meta($uid, 'cb_psychtest_history', [
        ['title' => 'Synthetic Assessment', 'date' => '2026-11-01', 'score' => '12', 'interpretation' => 'Baseline'],
    ]);
    update_user_meta($uid, 'cb_favorites', [10, 20]);

    $appt_id = wp_insert_post([
        'post_title'  => 'QA013 Synthetic Appointment',
        'post_status' => 'publish',
        'post_type'   => 'cb_appointment',
        'post_author' => $uid,
    ]);
    update_post_meta($appt_id, 'cb_appointment_slot', '2026-12-01 10:00');
    update_post_meta($appt_id, 'cb_patient_notes', 'Synthetic non-PHI fixture note');
    $created_posts[] = $appt_id;
    return $appt_id;
}

try {
    $u_admin = create_qa013_user('qa013_admin', 'administrator');
    $u_sub_a = create_qa013_user('qa013_sub_a', 'subscriber');
    $u_sub_b = create_qa013_user('qa013_sub_b', 'subscriber');

    // -------------------------------------------------------------------------
    // Group 1: Co-Install Hook Idempotency & Retention Policy Registry
    // -------------------------------------------------------------------------
    echo "Group 1: Co-Install Hook Idempotency & Retention Policy Registry\n";

    PrivacyManager::boot();
    PrivacyManager::boot(); // Duplicate call in co-install

    $exporters = apply_filters('wp_privacy_personal_data_exporters', []);
    $erasers   = apply_filters('wp_privacy_personal_data_erasers', []);

    assert_check(isset($exporters['carmilla-core-data']), 'Exporter carmilla-core-data registered');
    assert_check(isset($erasers['carmilla-core-eraser']), 'Eraser carmilla-core-eraser registered');

    global $wp_filter;
    $exp_callbacks = 0;
    if (isset($wp_filter['wp_privacy_personal_data_exporters'])) {
        foreach ($wp_filter['wp_privacy_personal_data_exporters']->callbacks as $prio => $cbs) {
            foreach ($cbs as $cb) {
                if (is_array($cb['function']) && $cb['function'][0] === PrivacyManager::class) {
                    $exp_callbacks++;
                }
            }
        }
    }
    assert_check($exp_callbacks === 1, "Co-install registers exporter filter exactly ONCE (actual: {$exp_callbacks})");

    $registry = PrivacyManager::get_retention_policy_registry();
    assert_check(($registry['commerce_orders']['strategy'] ?? '') === 'anonymize_retain_ledger', 'Commerce retention uses anonymize_retain_ledger');
    assert_check(($registry['commerce_orders']['domain_card_ref'] ?? '') === 'P04-WPPLUGIN-CODE-015', 'Commerce retention references domain card P04-WPPLUGIN-CODE-015');
    assert_check(($registry['clinic_appointments']['policy_ref'] ?? '') === 'approved_clinic_retention_policy', 'Clinic retention references approved policy');
    assert_check(($registry['maintenance_policy']['when_feature_disabled'] ?? '') === 'allow_authorized_maintenance', 'Feature-disabled maintenance remains allowed');

    // -------------------------------------------------------------------------
    // Group 2: Authorization & Non-Owner Isolation
    // -------------------------------------------------------------------------
    echo "\nGroup 2: Authorization & Non-Owner Isolation\n";

    $auth_anon = PrivacyManager::can_perform_privacy_action(0, $u_sub_a);
    assert_check(!$auth_anon['allowed'] && $auth_anon['code'] === 'UNAUTHENTICATED', 'Unauthenticated privacy request rejected (401)');

    $auth_non_owner = PrivacyManager::can_perform_privacy_action($u_sub_b, $u_sub_a);
    assert_check(!$auth_non_owner['allowed'] && $auth_non_owner['code'] === 'FORBIDDEN' && $auth_non_owner['status'] === 403, 'Non-owner subscriber rejected with 403 FORBIDDEN');

    $auth_owner = PrivacyManager::can_perform_privacy_action($u_sub_a, $u_sub_a);
    assert_check($auth_owner['allowed'] && $auth_owner['code'] === 'OK', 'Owner allowed to export/erase own data');

    $auth_admin = PrivacyManager::can_perform_privacy_action($u_admin, $u_sub_a);
    assert_check($auth_admin['allowed'] && $auth_admin['code'] === 'OK', 'Admin allowed privacy maintenance');

    // -------------------------------------------------------------------------
    // Group 3: Export & Erase When Features Are Disabled
    // -------------------------------------------------------------------------
    echo "\nGroup 3: Export & Erase With Features Disabled\n";

    $appt_id = seed_user_privacy_fixture($u_sub_a);
    update_option('cb_manifest_features', [
        'academy.core'   => false,
        'clinic.booking' => false,
        'psych.tests'    => false,
        'commerce.core'  => false,
    ]);
    if (class_exists('\Carmilla_Kernel')) {
        \Carmilla_Kernel::resolve_features();
    }

    $export_disabled = PrivacyManager::export_user_data('qa013_sub_a@synthetic.local');
    assert_check($export_disabled['done'] === true, 'Export completes when features are disabled');
    assert_check(count($export_disabled['data']) >= 3, 'Export still returns appointment, psychtest, and academy fixture records even when features are OFF');

    $erase_disabled = PrivacyManager::erase_user_data('qa013_sub_a@synthetic.local');
    assert_check($erase_disabled['done'] === true && $erase_disabled['items_removed'] >= 4, 'Erase/anonymize executes when features are disabled');
    assert_check(empty(get_user_meta($u_sub_a, 'cb_enrolled_courses', true)), 'Course enrollments erased');
    assert_check(empty(get_user_meta($u_sub_a, 'cb_psychtest_history', true)), 'Psych test history scrubbed');
    assert_check(empty(get_post_meta($appt_id, 'cb_patient_notes', true)), 'Appointment notes scrubbed');

    // -------------------------------------------------------------------------
    // Group 4: Host-Agnostic Export/Erase Parity Across All 3 Host Modes
    // -------------------------------------------------------------------------
    echo "\nGroup 4: Host Parity Across Theme-Only, Plugin-Only (TT4), and Co-Install\n";

    // Mode 1: Theme-Only (Bridge Deactivated)
    switch_theme('carmilla-theme');
    deactivate_plugins($plugin_slug);
    seed_user_privacy_fixture($u_sub_b);
    $exp_theme = PrivacyManager::export_user_data('qa013_sub_b@synthetic.local');
    assert_check($exp_theme['done'] === true && count($exp_theme['data']) >= 3, 'Theme-Only: export succeeds without Bridge');
    $ers_theme = PrivacyManager::erase_user_data('qa013_sub_b@synthetic.local');
    assert_check($ers_theme['done'] === true && $ers_theme['items_removed'] >= 4, 'Theme-Only: erase succeeds without Bridge');

    // Mode 2: Plugin-Only (Twenty Twenty-Four + Bridge)
    activate_plugin($plugin_slug);
    switch_theme('twentytwentyfour');
    seed_user_privacy_fixture($u_sub_b);
    $exp_plugin = PrivacyManager::export_user_data('qa013_sub_b@synthetic.local');
    assert_check($exp_plugin['done'] === true && count($exp_plugin['data']) >= 3, 'Plugin-Only (TT4): export succeeds on non-Carmilla theme');
    $ers_plugin = PrivacyManager::erase_user_data('qa013_sub_b@synthetic.local');
    assert_check($ers_plugin['done'] === true && $ers_plugin['items_removed'] >= 4, 'Plugin-Only (TT4): erase succeeds on non-Carmilla theme');

} finally {
    switch_theme($original_theme ?: 'carmilla-theme');
    if (!is_plugin_active($plugin_slug)) {
        activate_plugin($plugin_slug);
    }
    if ($orig_features === null) {
        delete_option('cb_manifest_features');
    } else {
        update_option('cb_manifest_features', $orig_features);
    }
    foreach ($created_posts as $pid) {
        if ($pid > 0) {
            wp_delete_post($pid, true);
        }
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
