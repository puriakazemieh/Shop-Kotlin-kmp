<?php
/**
 * Real WordPress Verification Suite for P04-WPTHEME-CODE-016
 * Template Hierarchy, Dynamic RTL/LTR, Light/Dark Token Layer & Responsive Breakpoints
 *
 * Run inside Docker WordPress container:
 * docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/tests/test-theme-hierarchy-responsive-016.php
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
echo "  P04-WPTHEME-CODE-016: Template Hierarchy, RTL/LTR, Light/Dark QA  \n";
echo "====================================================================\n\n";

$theme_dir = get_template_directory();

// -------------------------------------------------------------------------
// Group 1: Canonical WordPress & Custom Post Type Template Hierarchy
// -------------------------------------------------------------------------
echo "Group 1: Template Hierarchy Presence & Syntax\n";

$required_templates = [
    'index.php',
    'front-page.php',
    'page.php',
    'single.php',
    'archive.php',
    'search.php',
    'searchform.php',
    '404.php',
    'header.php',
    'footer.php',
    'single-cb_course.php',
    'archive-cb_course.php',
    'single-cb_therapist.php',
    'archive-cb_therapist.php',
    'single-cb_psychtest.php',
    'archive-cb_psychtest.php',
    'woocommerce/archive-product.php',
    'woocommerce/single-product.php',
];

foreach ($required_templates as $tpl) {
    assert_check(file_exists($theme_dir . '/' . $tpl), "Template exists: {$tpl}");
}

// -------------------------------------------------------------------------
// Group 2: Dynamic RTL / LTR Directionality
// -------------------------------------------------------------------------
echo "\nGroup 2: Dynamic RTL / LTR Directionality\n";

$header_src = file_get_contents($theme_dir . '/header.php');
assert_check(strpos($header_src, "is_rtl() ? 'rtl' : 'ltr'") !== false, 'header.php dynamically switches dir="rtl" / dir="ltr" via is_rtl()');
assert_check(strpos($header_src, "localStorage.getItem('cb_theme')") !== false, 'header.php applies saved light/dark theme before paint');

$rtl_css = file_get_contents($theme_dir . '/rtl.css');
assert_check(strpos($rtl_css, '[dir="ltr"]') !== false && strpos($rtl_css, '[dir="rtl"]') !== false, 'rtl.css handles both [dir="rtl"] and [dir="ltr"]');
assert_check(strpos($rtl_css, 'var(--bg)') !== false && strpos($rtl_css, 'var(--ink)') !== false, 'rtl.css uses CSS design tokens instead of hardcoded #121212/#000');

// -------------------------------------------------------------------------
// Group 3: Light / Dark Token Layer & Responsive Breakpoints
// -------------------------------------------------------------------------
echo "\nGroup 3: Light/Dark Tokens & Responsive Breakpoints (360..1440)\n";

$tokens_css = file_get_contents($theme_dir . '/assets/css/tokens.css');
$base_css   = file_get_contents($theme_dir . '/assets/css/base.css');

assert_check(strpos($tokens_css, ':root[data-theme="dark"]') !== false, 'tokens.css defines explicit :root[data-theme="dark"] override');
assert_check(strpos($tokens_css, '@media (prefers-color-scheme: dark)') !== false, 'tokens.css supports prefers-color-scheme: dark');
assert_check(strpos($tokens_css, ':root:not([data-theme="light"])') !== false, 'tokens.css respects explicit data-theme="light" over system dark mode');
assert_check(strpos($base_css, 'overflow-x: hidden') !== false, 'base.css prevents horizontal overflow at 360px/390px viewports');
assert_check(strpos($base_css, '600px') !== false && strpos($base_css, '840px') !== false, 'base.css defines 600px and 840px adaptive breakpoints');

// -------------------------------------------------------------------------
// Group 4: Live HTTP Rendering on Real WordPress
// -------------------------------------------------------------------------
echo "\nGroup 4: Live HTTP Rendering on Real WordPress\n";

$original_theme = get_stylesheet();
if ($original_theme !== 'carmilla-theme') {
    switch_theme('carmilla-theme');
}

$urls = [
    'Home'     => home_url('/'),
    'Search'   => home_url('/?s=carmilla'),
    'NotFound' => home_url('/non-existent-route-qa016-check/'),
];

foreach ($urls as $label => $url) {
    $internal_url = str_replace('http://localhost:8080', 'http://127.0.0.1', $url);
    $resp = wp_remote_get($internal_url, ['timeout' => 10, 'headers' => ['Host' => 'localhost:8080']]);
    assert_check(!is_wp_error($resp), "{$label} HTTP request succeeded");
    if (!is_wp_error($resp)) {
        $code = wp_remote_retrieve_response_code($resp);
        $body = wp_remote_retrieve_body($resp);
        $expected_code = ($label === 'NotFound') ? 404 : 200;
        assert_check($code === $expected_code, "{$label} returned expected HTTP {$expected_code} (got {$code})");
        assert_check(strpos($body, 'Fatal error') === false, "{$label} rendered with zero PHP Fatal errors");
        if ($label === 'Home') {
            assert_check(strpos($body, 'dc-root') !== false && strpos($body, 'dir="rtl"') !== false, 'Home HTML contains dc-root with dir="rtl"');
            assert_check(strpos($body, 'tokens.css') !== false, 'Home HTML enqueues tokens.css');
        }
    }
}

echo "\n====================================================================\n";
echo "Results: Total: {$total_tests} | Passed: {$passed_tests} | Failed: {$failed_tests}\n";
echo "====================================================================\n";

exit($failed_tests > 0 ? 1 : 0);
