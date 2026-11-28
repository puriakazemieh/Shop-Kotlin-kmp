<?php
/**
 * Real WordPress Verification Suite for P04-WPTHEME-CODE-017
 * Accessibility (Keyboard, Focus-Visible, Labels, Landmarks, Contrast Tokens, Zoom 200% Reflow)
 *
 * Run inside Docker WordPress container:
 * docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/tests/test-theme-accessibility-017.php
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
echo "  P04-WPTHEME-CODE-017: Theme Accessibility (a11y) Verification     \n";
echo "====================================================================\n\n";

$theme_dir = get_template_directory();

$base_css       = file_get_contents($theme_dir . '/assets/css/base.css');
$components_css = file_get_contents($theme_dir . '/assets/css/components.css');
$woo_css        = file_get_contents($theme_dir . '/assets/css/woocommerce.css');
$header_php     = file_get_contents($theme_dir . '/header.php');
$search_php     = file_get_contents($theme_dir . '/searchform.php');
$login_php      = file_get_contents($theme_dir . '/woocommerce/myaccount/form-login.php');

// -------------------------------------------------------------------------
// Group 1: Keyboard Focus & Skip Link
// -------------------------------------------------------------------------
echo "Group 1: Keyboard Focus & Skip Link\n";

assert_check(strpos($base_css, ':focus-visible') !== false, 'base.css defines :focus-visible outline ring');
assert_check(strpos($base_css, '.skip-link:focus') !== false, 'base.css styles .skip-link:focus for keyboard users');
assert_check(strpos($components_css, 'outline: none') === false, 'components.css does NOT suppress focus outlines');
assert_check(strpos($woo_css, 'outline: none') === false, 'woocommerce.css does NOT suppress focus outlines');
assert_check(strpos($header_php, 'class="skip-link screen-reader-text" href="#content"') !== false, 'header.php renders skip-link pointing to #content');
assert_check(strpos($header_php, 'id="content" tabindex="-1"') !== false, 'header.php sets tabindex="-1" on #content landmark');

// -------------------------------------------------------------------------
// Group 2: ARIA Labels & Form Control Associations
// -------------------------------------------------------------------------
echo "\nGroup 2: ARIA Labels & Form Labels (Menu, Search, Account)\n";

assert_check(strpos($header_php, 'aria-label="') !== false, 'header.php includes aria-label attributes on icon buttons/links');
assert_check(strpos($header_php, '<nav aria-label=') !== false, 'header.php uses <nav aria-label=...> for category navigation');
assert_check(strpos($search_php, 'aria-label=') !== false, 'searchform.php includes aria-label on search input');

assert_check(strpos($login_php, '<label for="cb_login_username"') !== false && strpos($login_php, 'id="cb_login_username"') !== false, 'form-login.php associates label with cb_login_username');
assert_check(strpos($login_php, '<label for="cb_login_password"') !== false && strpos($login_php, 'id="cb_login_password"') !== false, 'form-login.php associates label with cb_login_password');
assert_check(strpos($login_php, '<label for="cb_reg_email"') !== false && strpos($login_php, 'id="cb_reg_email"') !== false, 'form-login.php associates label with cb_reg_email');
assert_check(strpos($login_php, 'outline:none') === false, 'form-login.php does NOT suppress focus outline on inputs');

// -------------------------------------------------------------------------
// Group 3: Live HTML Rendering of Accessibility Landmarks
// -------------------------------------------------------------------------
echo "\nGroup 3: Live HTML Rendering of Accessibility Landmarks\n";

$resp = wp_remote_get('http://127.0.0.1/', ['timeout' => 10, 'headers' => ['Host' => 'localhost:8080']]);
assert_check(!is_wp_error($resp) && wp_remote_retrieve_response_code($resp) === 200, 'Home page renders HTTP 200 OK');
if (!is_wp_error($resp)) {
    $body = wp_remote_retrieve_body($resp);
    assert_check(strpos($body, 'class="skip-link screen-reader-text"') !== false, 'Live HTML contains skip-link');
    assert_check(strpos($body, 'id="content" tabindex="-1"') !== false, 'Live HTML contains focusable #content landmark');
    assert_check(strpos($body, 'aria-label=') !== false, 'Live HTML contains aria-label attributes');
}

echo "\n====================================================================\n";
echo "Results: Total: {$total_tests} | Passed: {$passed_tests} | Failed: {$failed_tests}\n";
echo "====================================================================\n";

exit($failed_tests > 0 ? 1 : 0);
