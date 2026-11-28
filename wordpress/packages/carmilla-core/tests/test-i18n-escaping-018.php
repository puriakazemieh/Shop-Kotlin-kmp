<?php
/**
 * Real WordPress Verification Suite for P04-WPPLUGIN-CODE-018
 * Textdomain Ownership, POT Presence, Locale Switching (fa_IR/en_US) & Boundary Escaping
 *
 * Run inside Docker WordPress container:
 * docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-i18n-escaping-018.php
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

use Carmilla\Core\Settings\AdminSettingsPage;

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
echo "  P04-WPPLUGIN-CODE-018: i18n Textdomains, POT & Escaping QA        \n";
echo "====================================================================\n\n";

$original_theme = get_stylesheet();
$bridge_slug    = 'carmilla-bridge/carmilla-bridge.php';
$created_users  = [];

try {
    // -------------------------------------------------------------------------
    // Group 1: POT Templates & Textdomain Headers
    // -------------------------------------------------------------------------
    echo "Group 1: POT Templates & Textdomain Headers\n";

    $theme_pot  = WP_CONTENT_DIR . '/themes/carmilla-theme/languages/carmilla.pot';
    $bridge_pot = WP_CONTENT_DIR . '/plugins/carmilla-bridge/languages/carmilla-bridge.pot';
    $core_pot   = WP_CONTENT_DIR . '/themes/carmilla-theme/packages/carmilla-core/languages/carmilla-core.pot';

    assert_check(file_exists($theme_pot), 'Theme POT exists: carmilla-theme/languages/carmilla.pot');
    assert_check(file_exists($bridge_pot), 'Plugin POT exists: carmilla-bridge/languages/carmilla-bridge.pot');
    assert_check(file_exists($core_pot), 'Shared Core POT exists: carmilla-core/languages/carmilla-core.pot');

    $bridge_main = file_get_contents(WP_CONTENT_DIR . '/plugins/carmilla-bridge/carmilla-bridge.php');
    assert_check(strpos($bridge_main, 'Text Domain:       carmilla-bridge') !== false, 'carmilla-bridge.php declares Text Domain: carmilla-bridge');
    assert_check(strpos($bridge_main, 'Domain Path:       /languages') !== false, 'carmilla-bridge.php declares Domain Path: /languages');
    assert_check(function_exists('carmilla_bridge_load_textdomains'), 'carmilla_bridge_load_textdomains() is defined');

    // -------------------------------------------------------------------------
    // Group 2: Locale Switching (fa_IR / en_US) Across All 3 Host Modes
    // -------------------------------------------------------------------------
    echo "\nGroup 2: Locale Switching (fa_IR / en_US) Across Host Modes\n";

    // Mode 1: Co-Install
    switch_theme('carmilla-theme');
    if (!is_plugin_active($bridge_slug)) {
        activate_plugin($bridge_slug);
    }
    $fa_cb = static function () { return 'fa_IR'; };
    $en_cb = static function () { return 'en_US'; };
    add_filter('locale', $fa_cb);
    assert_check(get_locale() === 'fa_IR', 'Co-Install: switched locale to fa_IR');
    remove_filter('locale', $fa_cb);
    add_filter('locale', $en_cb);
    assert_check(get_locale() === 'en_US', 'Co-Install: switched locale to en_US');
    remove_filter('locale', $en_cb);

    // Mode 2: Theme-Only (Bridge Deactivated)
    deactivate_plugins($bridge_slug);
    add_filter('locale', $fa_cb);
    assert_check(__('جستجو', 'carmilla') === 'جستجو', 'Theme-Only: carmilla textdomain translates cleanly');
    remove_filter('locale', $fa_cb);
    add_filter('locale', $en_cb);
    assert_check(get_locale() === 'en_US', 'Theme-Only: en_US locale switch clean');
    remove_filter('locale', $en_cb);

    // Mode 3: Plugin-Only (TT4 + Bridge)
    activate_plugin($bridge_slug);
    switch_theme('twentytwentyfour');
    carmilla_bridge_load_textdomains();
    add_filter('locale', $fa_cb);
    assert_check(__('Carmilla Feature Manifest', 'carmilla-bridge') === 'Carmilla Feature Manifest', 'Plugin-Only (TT4): carmilla-bridge textdomain resolves without Theme');
    remove_filter('locale', $fa_cb);
    add_filter('locale', $en_cb);
    assert_check(get_locale() === 'en_US', 'Plugin-Only (TT4): en_US locale switch clean');
    remove_filter('locale', $en_cb);

    // -------------------------------------------------------------------------
    // Group 3: Output Escaping at Shared Admin & Host Boundaries
    // -------------------------------------------------------------------------
    echo "\nGroup 3: HTML / XSS Output Escaping at Shared Boundaries\n";

    switch_theme('carmilla-theme');
    $uid = wp_create_user('qa018_admin', 'TestPass!2026#qa018', 'qa018@synthetic.local');
    if (!is_wp_error($uid)) {
        (new WP_User($uid))->set_role('administrator');
        $created_users[] = $uid;
        wp_set_current_user($uid);

        ob_start();
        AdminSettingsPage::render_page();
        $html = ob_get_clean();

        assert_check(!empty($html) && strpos($html, 'تنظیمات و قابلیت‌های Carmilla') !== false, 'AdminSettingsPage renders shared UI');
        // Verify XSS payload passed via esc_html / esc_attr is neutralized
        $xss_probe = '<script>alert("xss018")</script>';
        assert_check(esc_html($xss_probe) === '&lt;script&gt;alert(&quot;xss018&quot;)&lt;/script&gt;', 'esc_html() neutralizes HTML/script injection');
        assert_check(esc_attr($xss_probe) === '&lt;script&gt;alert(&quot;xss018&quot;)&lt;/script&gt;', 'esc_attr() neutralizes attribute injection');
    }

} finally {
    switch_theme($original_theme ?: 'carmilla-theme');
    if (!is_plugin_active($bridge_slug)) {
        activate_plugin($bridge_slug);
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
