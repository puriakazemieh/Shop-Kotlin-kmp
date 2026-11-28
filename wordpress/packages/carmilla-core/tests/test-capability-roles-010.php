<?php
/**
 * Real WordPress Verification Suite for P04-WPPLUGIN-CODE-010
 * Shared Capability Registry, Domain Roles, Tri-Layer Decoupled Security & Host Parity
 *
 * Run inside Docker WordPress container:
 * docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-capability-roles-010.php
 */

declare(strict_types=1);

// Load Real WordPress Environment
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

use Carmilla\Core\Capabilities\CapabilityRegistry;

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
echo "  P04-WPPLUGIN-CODE-010: Roles & Tri-Layer Security QA (Real WP)    \n";
echo "====================================================================\n\n";

// Ensure capabilities and roles are freshly registered
CapabilityRegistry::register_capabilities();
CapabilityRegistry::register_roles();

// -------------------------------------------------------------------------
// Group 1: Role Registration & Least-Privilege Mapping
// -------------------------------------------------------------------------
echo "Group 1: Role Registration & Least-Privilege Mapping\n";

$admin_role = get_role('administrator');
assert_check($admin_role !== null, 'Administrator role exists');
assert_check($admin_role && $admin_role->has_cap('carmilla_manage_settings'), 'Admin has carmilla_manage_settings');
assert_check($admin_role && $admin_role->has_cap('carmilla_manage_courses'), 'Admin has carmilla_manage_courses');
assert_check($admin_role && $admin_role->has_cap('carmilla_manage_appointments'), 'Admin has carmilla_manage_appointments');
assert_check($admin_role && $admin_role->has_cap('carmilla_manage_tickets'), 'Admin has carmilla_manage_tickets');
assert_check($admin_role && $admin_role->has_cap('carmilla_manage_shop'), 'Admin has carmilla_manage_shop');
assert_check($admin_role && $admin_role->has_cap(CapabilityRegistry::MANAGE_INTEGRATIONS), 'Admin has manage_carmilla_integrations');

$domain_roles = ['carmilla_instructor', 'carmilla_therapist', 'carmilla_support_agent', 'carmilla_shop_manager'];
foreach ($domain_roles as $r_slug) {
    $r_obj = get_role($r_slug);
    assert_check($r_obj !== null, "Role '{$r_slug}' registered in WordPress");
    assert_check($r_obj && !$r_obj->has_cap('manage_options'), "Role '{$r_slug}' does NOT have manage_options");
    assert_check($r_obj && !$r_obj->has_cap('carmilla_manage_settings'), "Role '{$r_slug}' does NOT have carmilla_manage_settings");
    assert_check($r_obj && !$r_obj->has_cap('carmilla_manage_licenses'), "Role '{$r_slug}' does NOT have carmilla_manage_licenses");
}

$inst_role = get_role('carmilla_instructor');
assert_check($inst_role && $inst_role->has_cap('carmilla_edit_courses'), 'Instructor can edit courses');
assert_check($inst_role && $inst_role->has_cap('carmilla_view_courses'), 'Instructor can view courses');
assert_check($inst_role && !$inst_role->has_cap('carmilla_manage_courses'), 'Instructor cannot manage/publish/delete courses');
assert_check($inst_role && !$inst_role->has_cap('carmilla_edit_appointments'), 'Instructor cannot edit clinic appointments');

$therapist_role = get_role('carmilla_therapist');
assert_check($therapist_role && $therapist_role->has_cap('carmilla_view_appointments'), 'Therapist can view appointments');
assert_check($therapist_role && $therapist_role->has_cap('carmilla_edit_appointments'), 'Therapist can edit appointments');
assert_check($therapist_role && !$therapist_role->has_cap('carmilla_edit_courses'), 'Therapist cannot edit courses');

$support_role = get_role('carmilla_support_agent');
assert_check($support_role && $support_role->has_cap('carmilla_view_tickets'), 'Support agent can view tickets');
assert_check($support_role && $support_role->has_cap('carmilla_reply_tickets'), 'Support agent can reply tickets');
assert_check($support_role && !$support_role->has_cap('carmilla_edit_products'), 'Support agent cannot edit shop products');

// -------------------------------------------------------------------------
// Create Synthetic Test Users
// -------------------------------------------------------------------------
$created_users = [];
function create_synthetic_user(string $username, string $role): int {
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
    $user = new WP_User($uid);
    $user->set_role($role);
    $created_users[] = $uid;
    return $uid;
}

$original_theme = get_stylesheet();
$plugin_slug    = 'carmilla-bridge/carmilla-bridge.php';

try {
    $u_admin     = create_synthetic_user('qa010_admin', 'administrator');
    $u_inst_a    = create_synthetic_user('qa010_inst_a', 'carmilla_instructor');
    $u_inst_b    = create_synthetic_user('qa010_inst_b', 'carmilla_instructor');
    $u_therapist = create_synthetic_user('qa010_therapist', 'carmilla_therapist');
    $u_support   = create_synthetic_user('qa010_support', 'carmilla_support_agent');
    $u_sub       = create_synthetic_user('qa010_sub', 'subscriber');

    // Synthetic feature matrices for decoupled layer testing
    $matrix_not_entitled = [
        'features' => [
            'academy.core' => [
                'entitled'      => false,
                'admin_enabled' => true,
                'status'        => 'NOT_ENTITLED',
            ],
        ],
    ];

    $matrix_disabled_by_admin = [
        'features' => [
            'academy.core' => [
                'entitled'      => true,
                'admin_enabled' => false,
                'status'        => 'DISABLED_BY_ADMIN',
            ],
        ],
    ];

    $matrix_prereq_missing = [
        'features' => [
            'commerce.core' => [
                'entitled'      => true,
                'admin_enabled' => true,
                'status'        => 'PREREQUISITE_MISSING',
            ],
        ],
    ];

    $matrix_all_active = [
        'features' => [
            'academy.core'    => ['entitled' => true, 'admin_enabled' => true, 'status' => 'ACTIVE'],
            'clinic.booking'  => ['entitled' => true, 'admin_enabled' => true, 'status' => 'ACTIVE'],
            'content.support' => ['entitled' => true, 'admin_enabled' => true, 'status' => 'ACTIVE'],
            'commerce.core'   => ['entitled' => true, 'admin_enabled' => true, 'status' => 'ACTIVE'],
            'content.core'    => ['entitled' => true, 'admin_enabled' => true, 'status' => 'ACTIVE'],
            'theme.base'      => ['entitled' => true, 'admin_enabled' => true, 'status' => 'ACTIVE'],
        ],
    ];

    // -------------------------------------------------------------------------
    // Group 2: Layer 1 - Site Entitlement Gate
    // -------------------------------------------------------------------------
    echo "\nGroup 2: Layer 1 - Site Entitlement Gate (Unpurchased Feature)\n";

    $res = CapabilityRegistry::check_access($u_admin, 'education', 'manage', 'academy.core', ['feature_matrix' => $matrix_not_entitled]);
    assert_check(!$res['allowed'] && $res['code'] === 'FEATURE_NOT_ENTITLED' && $res['layer'] === 'entitlement', 'Admin blocked on unpurchased academy.core (FEATURE_NOT_ENTITLED)');

    $res = CapabilityRegistry::check_access($u_inst_a, 'education', 'edit', 'academy.core', ['feature_matrix' => $matrix_not_entitled]);
    assert_check(!$res['allowed'] && $res['code'] === 'FEATURE_NOT_ENTITLED' && $res['layer'] === 'entitlement', 'Instructor blocked on unpurchased academy.core');

    $res = CapabilityRegistry::check_access($u_sub, 'education', 'read', 'academy.core', ['feature_matrix' => $matrix_not_entitled]);
    assert_check(!$res['allowed'] && $res['code'] === 'FEATURE_NOT_ENTITLED', 'Subscriber blocked at entitlement layer on unpurchased feature');

    // -------------------------------------------------------------------------
    // Group 3: Layer 2 - Admin Feature Toggle & Prerequisite Gate
    // -------------------------------------------------------------------------
    echo "\nGroup 3: Layer 2 - Admin Feature Toggle & Prerequisite Gate\n";

    $res = CapabilityRegistry::check_access($u_admin, 'education', 'manage', 'academy.core', ['feature_matrix' => $matrix_disabled_by_admin]);
    assert_check(!$res['allowed'] && $res['code'] === 'FEATURE_DISABLED' && $res['layer'] === 'toggle', 'Admin blocked when academy.core is toggled OFF (FEATURE_DISABLED)');

    $res = CapabilityRegistry::check_access($u_inst_a, 'education', 'edit', 'academy.core', ['feature_matrix' => $matrix_disabled_by_admin]);
    assert_check(!$res['allowed'] && $res['code'] === 'FEATURE_DISABLED' && $res['layer'] === 'toggle', 'Instructor blocked when academy.core is toggled OFF');

    $res = CapabilityRegistry::check_access($u_admin, 'shop', 'manage', 'commerce.core', ['feature_matrix' => $matrix_prereq_missing]);
    assert_check(!$res['allowed'] && $res['code'] === 'PREREQUISITE_MISSING' && $res['layer'] === 'prerequisite', 'Shop blocked when prerequisite (WooCommerce) is missing');

    // -------------------------------------------------------------------------
    // Group 4: Layer 3 - RBAC & Least Privilege (Active Feature)
    // -------------------------------------------------------------------------
    echo "\nGroup 4: Layer 3 - RBAC & Least Privilege (Feature Active)\n";

    $res = CapabilityRegistry::check_access(0, 'education', 'edit', 'academy.core', ['feature_matrix' => $matrix_all_active]);
    assert_check(!$res['allowed'] && $res['code'] === 'UNAUTHENTICATED' && $res['status'] === 401, 'Unauthenticated user receives 401 UNAUTHENTICATED');

    // Critical security invariant: Purchasing/enabling feature NEVER grants subscriber admin/edit rights
    $res = CapabilityRegistry::check_access($u_sub, 'education', 'manage', 'academy.core', ['feature_matrix' => $matrix_all_active]);
    assert_check(!$res['allowed'] && $res['code'] === 'FORBIDDEN' && $res['status'] === 403 && $res['layer'] === 'rbac', 'Subscriber CANNOT manage courses even when academy.core is ACTIVE');

    $res = CapabilityRegistry::check_access($u_sub, 'admin', 'manage', 'theme.base', ['feature_matrix' => $matrix_all_active]);
    assert_check(!$res['allowed'] && $res['code'] === 'FORBIDDEN' && $res['layer'] === 'rbac', 'Subscriber CANNOT manage site settings');

    // Positive & Negative checks for Instructor
    $res = CapabilityRegistry::check_access($u_inst_a, 'education', 'edit', 'academy.core', ['feature_matrix' => $matrix_all_active]);
    assert_check($res['allowed'] && $res['code'] === 'OK' && $res['status'] === 200, 'Instructor ALLOWED to edit courses when active');

    $res = CapabilityRegistry::check_access($u_inst_a, 'education', 'manage', 'academy.core', ['feature_matrix' => $matrix_all_active]);
    assert_check(!$res['allowed'] && $res['code'] === 'FORBIDDEN', 'Instructor DENIED course manage/admin cap (least privilege)');

    $res = CapabilityRegistry::check_access($u_inst_a, 'clinic', 'edit', 'clinic.booking', ['feature_matrix' => $matrix_all_active]);
    assert_check(!$res['allowed'] && $res['code'] === 'FORBIDDEN', 'Instructor DENIED clinic appointment edit (cross-domain isolation)');

    // Positive & Negative checks for Therapist
    $res = CapabilityRegistry::check_access($u_therapist, 'clinic', 'edit', 'clinic.booking', ['feature_matrix' => $matrix_all_active]);
    assert_check($res['allowed'] && $res['code'] === 'OK', 'Therapist ALLOWED to edit clinic appointments when active');

    $res = CapabilityRegistry::check_access($u_therapist, 'education', 'edit', 'academy.core', ['feature_matrix' => $matrix_all_active]);
    assert_check(!$res['allowed'] && $res['code'] === 'FORBIDDEN', 'Therapist DENIED education course edit');

    // Positive & Negative checks for Support Agent
    $res = CapabilityRegistry::check_access($u_support, 'support', 'edit', 'content.support', ['feature_matrix' => $matrix_all_active]);
    assert_check($res['allowed'] && $res['code'] === 'OK', 'Support agent ALLOWED to reply to tickets');

    $res = CapabilityRegistry::check_access($u_support, 'admin', 'edit', 'theme.base', ['feature_matrix' => $matrix_all_active]);
    assert_check(!$res['allowed'] && $res['code'] === 'FORBIDDEN', 'Support agent DENIED license management');

    // Positive check for Admin
    $res = CapabilityRegistry::check_access($u_admin, 'admin', 'manage', 'theme.base', ['feature_matrix' => $matrix_all_active]);
    assert_check($res['allowed'] && $res['code'] === 'OK', 'Admin ALLOWED to manage settings');

    // -------------------------------------------------------------------------
    // Group 5: Layer 4 - Private Data & Resource Ownership Isolation
    // -------------------------------------------------------------------------
    echo "\nGroup 5: Layer 4 - Private Data & Resource Ownership\n";

    // Instructor A accessing their own course
    $res = CapabilityRegistry::check_access($u_inst_a, 'education', 'edit', 'academy.core', [
        'feature_matrix' => $matrix_all_active,
        'owner_id'       => $u_inst_a,
    ]);
    assert_check($res['allowed'] && $res['code'] === 'OK', 'Instructor A ALLOWED to edit own course');

    // Instructor A accessing Instructor B's private course draft
    $res = CapabilityRegistry::check_access($u_inst_a, 'education', 'edit', 'academy.core', [
        'feature_matrix' => $matrix_all_active,
        'owner_id'       => $u_inst_b,
    ]);
    assert_check(!$res['allowed'] && $res['code'] === 'FORBIDDEN' && $res['layer'] === 'ownership', 'Instructor A DENIED access to Instructor B private resource (ownership gate)');

    // Admin accessing Instructor B's course (manager override)
    $res = CapabilityRegistry::check_access($u_admin, 'education', 'edit', 'academy.core', [
        'feature_matrix' => $matrix_all_active,
        'owner_id'       => $u_inst_b,
    ]);
    assert_check($res['allowed'] && $res['code'] === 'OK', 'Admin ALLOWED manager override on Instructor B resource');

    // REST Permission Callback Factory check
    wp_set_current_user($u_sub);
    $perm_cb = CapabilityRegistry::get_permission_callback('education', 'manage', 'academy.core', ['feature_matrix' => $matrix_all_active]);
    $cb_res  = $perm_cb(new WP_REST_Request('POST', '/carmilla/v1/api/courses'));
    assert_check(is_wp_error($cb_res) && $cb_res->get_error_code() === 'FORBIDDEN', 'Permission callback returns WP_Error(FORBIDDEN) for subscriber');

    wp_set_current_user($u_inst_a);
    $perm_cb_inst = CapabilityRegistry::get_permission_callback('education', 'edit', 'academy.core', ['feature_matrix' => $matrix_all_active]);
    $cb_res_inst  = $perm_cb_inst(new WP_REST_Request('POST', '/carmilla/v1/api/courses'));
    assert_check($cb_res_inst === true, 'Permission callback returns true for authorized instructor');
    wp_set_current_user(0);

    // -------------------------------------------------------------------------
    // Group 6: Host Parity Verification (Theme-Only, Plugin-Only, Co-Install)
    // -------------------------------------------------------------------------
    echo "\nGroup 6: Host Parity Verification Across All 3 Host Modes\n";

    // Helper to test host parity
    $run_host_parity = function(string $mode_label) use ($u_admin, $u_inst_a, $u_sub, $matrix_all_active) {
        $r_sub = CapabilityRegistry::check_access($u_sub, 'education', 'manage', 'academy.core', ['feature_matrix' => $matrix_all_active]);
        assert_check(!$r_sub['allowed'] && $r_sub['code'] === 'FORBIDDEN', "{$mode_label}: Subscriber denied education.manage");

        $r_inst = CapabilityRegistry::check_access($u_inst_a, 'education', 'edit', 'academy.core', ['feature_matrix' => $matrix_all_active]);
        assert_check($r_inst['allowed'] && $r_inst['code'] === 'OK', "{$mode_label}: Instructor allowed education.edit");

        $r_adm = CapabilityRegistry::check_access($u_admin, 'admin', 'manage', 'theme.base', ['feature_matrix' => $matrix_all_active]);
        assert_check($r_adm['allowed'] && $r_adm['code'] === 'OK', "{$mode_label}: Admin allowed admin.manage");

        assert_check(get_role('carmilla_instructor') !== null, "{$mode_label}: carmilla_instructor role present");
        assert_check(get_role('carmilla_therapist') !== null, "{$mode_label}: carmilla_therapist role present");
    };

    // Mode 1: Co-Install
    switch_theme('carmilla-theme');
    if (!is_plugin_active($plugin_slug)) {
        activate_plugin($plugin_slug);
    }
    $run_host_parity('Co-Install');

    // Mode 2: Theme-Only (Bridge Deactivated)
    deactivate_plugins($plugin_slug);
    $run_host_parity('Theme-Only');

    // Mode 3: Plugin-Only (Twenty Twenty-Four + Bridge)
    activate_plugin($plugin_slug);
    switch_theme('twentytwentyfour');
    $run_host_parity('Plugin-Only (TT4)');

} finally {
    // Restore clean environment
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
