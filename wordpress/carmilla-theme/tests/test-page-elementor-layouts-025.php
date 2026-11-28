<?php
/**
 * Real WordPress Verification Suite for P04-WPTHEME-CODE-025
 * Page Template Hierarchy, Elementor Page Layouts (Default, Full Width, Blank/Canvas) & Bridge ON/OFF Parity
 *
 * Run inside Docker WordPress container:
 * docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/tests/test-page-elementor-layouts-025.php
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
echo "  P04-WPTHEME-CODE-025: Page Template Hierarchy & Elementor Layouts \n";
echo "====================================================================\n\n";

$original_theme = get_stylesheet();
$bridge_slug    = 'carmilla-bridge/carmilla-bridge.php';
$created_pages  = [];
$created_post   = 0;

try {
    if ($original_theme !== 'carmilla-theme') {
        switch_theme('carmilla-theme');
    }

    $marker_heading = 'QA025 Synthetic Layout Heading';
    $marker_text    = 'QA025 body paragraph verifying the_content execution.';
    $marker_btn     = 'QA025 Action Button';
    $synthetic_html = "<h2>{$marker_heading}</h2><p>{$marker_text}</p><img src=\"https://example.invalid/qa025.png\" alt=\"QA025\"><a class=\"btn btn--primary\" href=\"#qa\">{$marker_btn}</a>";

    $page_default = wp_insert_post([
        'post_title'   => 'QA025 Default Page',
        'post_content' => $synthetic_html,
        'post_status'  => 'publish',
        'post_type'    => 'page',
    ]);
    $created_pages[] = $page_default;

    $page_full = wp_insert_post([
        'post_title'   => 'QA025 Full Width Page',
        'post_content' => $synthetic_html,
        'post_status'  => 'publish',
        'post_type'    => 'page',
        'meta_input'   => ['_wp_page_template' => 'page-templates/elementor-full-width.php'],
    ]);
    $created_pages[] = $page_full;

    $page_blank = wp_insert_post([
        'post_title'   => 'QA025 Blank Canvas Page',
        'post_content' => $synthetic_html,
        'post_status'  => 'publish',
        'post_type'    => 'page',
        'meta_input'   => ['_wp_page_template' => 'page-templates/elementor-blank.php'],
    ]);
    $created_pages[] = $page_blank;

    $created_post = wp_insert_post([
        'post_title'   => 'QA025 Regular Post',
        'post_content' => $synthetic_html,
        'post_status'  => 'publish',
        'post_type'    => 'post',
    ]);

    // -------------------------------------------------------------------------
    // Group 1: Template Hierarchy Trace (Confirm no fallback to index.php)
    // -------------------------------------------------------------------------
    echo "Group 1: Template Hierarchy Trace\n";

    assert_check(file_exists(get_template_directory() . '/page.php'), 'page.php exists (prevents fallback to index.php)');
    assert_check(strpos(file_get_contents(get_template_directory() . '/page.php'), 'the_content()') !== false, 'page.php calls the_content()');
    assert_check(strpos(file_get_contents(get_template_directory() . '/page-templates/elementor-full-width.php'), 'the_content()') !== false, 'elementor-full-width.php calls the_content()');
    assert_check(strpos(file_get_contents(get_template_directory() . '/page-templates/elementor-blank.php'), 'the_content()') !== false, 'elementor-blank.php calls the_content()');
    assert_check(strpos(file_get_contents(get_template_directory() . '/page-templates/elementor-blank.php'), 'wp_head()') !== false, 'elementor-blank.php calls wp_head()');
    assert_check(strpos(file_get_contents(get_template_directory() . '/page-templates/elementor-blank.php'), 'wp_footer()') !== false, 'elementor-blank.php calls wp_footer()');

    // -------------------------------------------------------------------------
    // Group 2: Live HTTP Layout Matrix (Bridge OFF vs Bridge ON)
    // -------------------------------------------------------------------------
    $verify_mode = function(string $mode_label) use ($page_default, $page_full, $page_blank, $created_post, $marker_heading, $marker_text, $marker_btn) {
        echo "\n--- Testing Layout Matrix ({$mode_label}) ---\n";

        $targets = [
            'Default Page'    => ['url' => str_replace('http://localhost:8080', 'http://127.0.0.1', get_permalink($page_default)), 'expect_header' => true],
            'Full Width Page' => ['url' => str_replace('http://localhost:8080', 'http://127.0.0.1', get_permalink($page_full)), 'expect_header' => true],
            'Blank Canvas'    => ['url' => str_replace('http://localhost:8080', 'http://127.0.0.1', get_permalink($page_blank)), 'expect_header' => false],
            'Regular Post'    => ['url' => str_replace('http://localhost:8080', 'http://127.0.0.1', get_permalink($created_post)), 'expect_header' => true],
        ];

        foreach ($targets as $name => $cfg) {
            $resp = wp_remote_get($cfg['url'], ['timeout' => 10, 'headers' => ['Host' => 'localhost:8080']]);
            assert_check(!is_wp_error($resp) && wp_remote_retrieve_response_code($resp) === 200, "{$mode_label} | {$name}: HTTP 200 OK");
            if (!is_wp_error($resp)) {
                $body = wp_remote_retrieve_body($resp);
                assert_check(strpos($body, 'Fatal error') === false, "{$mode_label} | {$name}: zero PHP Fatal errors");
                assert_check(strpos($body, $marker_heading) !== false && strpos($body, $marker_text) !== false && strpos($body, $marker_btn) !== false, "{$mode_label} | {$name}: renders full the_content() payload");
                $has_header = strpos($body, 'class="dc-header"') !== false;
                if ($cfg['expect_header']) {
                    assert_check($has_header, "{$mode_label} | {$name}: includes theme header chrome");
                    assert_check(substr_count($body, 'class="dc-header"') === 1, "{$mode_label} | {$name}: zero duplicate headers");
                } else {
                    assert_check(!$has_header, "{$mode_label} | {$name}: Canvas/Blank omits theme header chrome");
                    assert_check(strpos($body, 'carmilla-blank') !== false, "{$mode_label} | {$name}: body_class contains carmilla-blank");
                }
            }
        }
    };

    // Mode 1: Bridge Deactivated (Theme-Only)
    deactivate_plugins($bridge_slug);
    $verify_mode('Bridge OFF (Theme-Only)');

    // Mode 2: Bridge Activated (Co-Install)
    activate_plugin($bridge_slug);
    $verify_mode('Bridge ON (Co-Install)');

} finally {
    switch_theme($original_theme ?: 'carmilla-theme');
    if (!is_plugin_active($bridge_slug)) {
        activate_plugin($bridge_slug);
    }
    foreach ($created_pages as $pid) {
        if ($pid > 0) {
            wp_delete_post($pid, true);
        }
    }
    if ($created_post > 0) {
        wp_delete_post($created_post, true);
    }
}

echo "\n====================================================================\n";
echo "Results: Total: {$total_tests} | Passed: {$passed_tests} | Failed: {$failed_tests}\n";
echo "====================================================================\n";

exit($failed_tests > 0 ? 1 : 0);
