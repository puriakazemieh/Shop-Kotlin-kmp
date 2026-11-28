<?php
/**
 * Real WordPress Verification Suite for P04-WPPLUGIN-CODE-045
 * Plugin Public Display Infrastructure on Third-Party Themes (Scoped CSS, Shortcode/Block, Route, Permission, Override)
 *
 * Run inside Docker WordPress container:
 * docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/test-plugin-public-renderer-045.php
 */

declare(strict_types=1);

$wp_load_paths = [
    '/var/www/html/wp-load.php',
    dirname(__DIR__, 4) . '/wp-load.php',
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
echo "  P04-WPPLUGIN-CODE-045: Plugin Public Display on Third-Party Theme \n";
echo "====================================================================\n\n";

$original_theme = get_stylesheet();
$bridge_slug    = 'carmilla-bridge/carmilla-bridge.php';
$created_users  = [];

try {
    if (!is_plugin_active($bridge_slug)) {
        activate_plugin($bridge_slug);
    }

    // Switch to third-party theme (Twenty Twenty-Four)
    switch_theme('twentytwentyfour');

    // -------------------------------------------------------------------------
    // Group 1: Scoped CSS, Shortcode & Block Registry Without Carmilla Theme
    // -------------------------------------------------------------------------
    echo "Group 1: Scoped CSS, Shortcode & Block Registry on Twenty Twenty-Four\n";

    $scoped_css_path = WP_CONTENT_DIR . '/plugins/carmilla-bridge/assets/css/carmilla-public-scoped.css';
    assert_check(file_exists($scoped_css_path), 'Scoped CSS file exists in carmilla-bridge');
    $scoped_css = file_get_contents($scoped_css_path);
    assert_check(strpos($scoped_css, '.carmilla-public-surface') !== false, 'Scoped CSS uses .carmilla-public-surface namespace');
    assert_check(!preg_match('/^\s*(body|html|\*|h1|h2|a)\s*\{/m', $scoped_css), 'Scoped CSS does NOT alter global third-party theme selectors');

    assert_check(shortcode_exists('carmilla_public_sample'), 'Shortcode [carmilla_public_sample] is registered');
    assert_check(WP_Block_Type_Registry::get_instance()->is_registered('carmilla/public-sample'), 'Block carmilla/public-sample is registered');

    $rendered = do_shortcode('[carmilla_public_sample title="Test <script>xss</script>"]');
    assert_check(strpos($rendered, 'class="carmilla-public-surface"') !== false, 'Shortcode renders .carmilla-public-surface wrapper');
    assert_check(strpos($rendered, '<script>') === false && strpos($rendered, '&lt;script&gt;') !== false, 'Shortcode escapes HTML/script safely');

    // -------------------------------------------------------------------------
    // Group 2: Live HTTP Route `/?carmilla_sample=1` on Twenty Twenty-Four
    // -------------------------------------------------------------------------
    echo "\nGroup 2: Live HTTP Sample Route on Twenty Twenty-Four\n";

    $resp = wp_remote_get('http://127.0.0.1/?carmilla_sample=1', ['timeout' => 10, 'headers' => ['Host' => 'localhost:8080']]);
    assert_check(!is_wp_error($resp) && wp_remote_retrieve_response_code($resp) === 200, 'Plugin-Only (TT4): /?carmilla_sample=1 returns HTTP 200 OK');
    if (!is_wp_error($resp)) {
        $body = wp_remote_retrieve_body($resp);
        assert_check(strpos($body, 'Fatal error') === false, 'Plugin-Only (TT4): zero PHP Fatal errors');
        assert_check(strpos($body, 'class="carmilla-public-surface"') !== false, 'Plugin-Only (TT4): sample page renders .carmilla-public-surface');
        assert_check(strpos($body, 'carmilla-public-scoped.css') !== false, 'Plugin-Only (TT4): enqueues scoped CSS');
        assert_check(strpos($body, 'wp-site-blocks') !== false, 'Plugin-Only (TT4): preserves Twenty Twenty-Four block theme wrapper');
    }

    // -------------------------------------------------------------------------
    // Group 3: REST Route & Permission Gates
    // -------------------------------------------------------------------------
    echo "\nGroup 3: REST Public Sample Route & Permission Gates\n";

    do_action('rest_api_init');
    $routes = rest_get_server()->get_routes();
    assert_check(isset($routes['/carmilla/v1/public/sample']), 'REST /carmilla/v1/public/sample route registered');

    wp_set_current_user(0);
    $res_pub = rest_do_request(new WP_REST_Request('GET', '/carmilla/v1/public/sample'));
    assert_check($res_pub->get_status() === 200 && !empty($res_pub->get_data()['ok']), 'Public scope returns 200 OK when content.core active');

    $req_priv = new WP_REST_Request('GET', '/carmilla/v1/public/sample');
    $req_priv->set_param('scope', 'private');
    $res_priv_anon = rest_do_request($req_priv);
    assert_check(in_array($res_priv_anon->get_status(), [401, 403], true), 'Private scope rejects unauthenticated caller (401/403)');

    $uid_sub = wp_create_user('qa045_sub', 'TestPass!2026#045', 'qa045@synthetic.local');
    if (!is_wp_error($uid_sub)) {
        (new WP_User($uid_sub))->set_role('subscriber');
        $created_users[] = $uid_sub;
        wp_set_current_user($uid_sub);
        $res_priv_sub = rest_do_request($req_priv);
        assert_check($res_priv_sub->get_status() === 403, 'Private scope rejects subscriber with 403 FORBIDDEN');
    }
    wp_set_current_user(0);

    // -------------------------------------------------------------------------
    // Group 4: Theme Override Hook
    // -------------------------------------------------------------------------
    echo "\nGroup 4: Theme Override Hook\n";

    $tmp_tpl = sys_get_temp_dir() . '/carmilla-override-045.php';
    file_put_contents($tmp_tpl, '<div class="theme-custom-override">Override OK</div>');
    $override_cb = static function () use ($tmp_tpl) { return $tmp_tpl; };
    add_filter('carmilla_public_sample_template', $override_cb);
    $overridden = do_shortcode('[carmilla_public_sample]');
    remove_filter('carmilla_public_sample_template', $override_cb);
    @unlink($tmp_tpl);
    assert_check(strpos($overridden, 'theme-custom-override') !== false, 'Theme override filter replaces default plugin template');

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
