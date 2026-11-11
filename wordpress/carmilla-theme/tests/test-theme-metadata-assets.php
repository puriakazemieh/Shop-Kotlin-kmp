<?php
/**
 * Test Theme Metadata & Assets for Carmilla Theme.
 * Validates screenshot.png, readme.txt, and style.css headers/metadata per WordPress standards.
 * Task: P02-THEME-CODE-010C3D3
 */

$themeDir = dirname(__DIR__);

$passed = 0;
$failed = 0;

function assert_true($cond, $msg) {
    global $passed, $failed;
    if ($cond) {
        echo "  [PASS] $msg\n";
        $passed++;
    } else {
        echo "  [FAIL] $msg\n";
        $failed++;
    }
}

echo "=== Test Theme Metadata & Assets (P02-THEME-CODE-010C3D3) ===\n\n";

// 1. Screenshot verification
echo "1. Screenshot validation:\n";
$screenshotPath = $themeDir . '/screenshot.png';
assert_true(file_exists($screenshotPath), 'screenshot.png exists in theme root');
if (file_exists($screenshotPath)) {
    $imgInfo = getimagesize($screenshotPath);
    assert_true(is_array($imgInfo), 'screenshot.png is a valid image readable by getimagesize()');
    if (is_array($imgInfo)) {
        assert_true($imgInfo[0] === 1200, "screenshot width is 1200px (actual: {$imgInfo[0]})");
        assert_true($imgInfo[1] === 900, "screenshot height is 900px (actual: {$imgInfo[1]})");
        assert_true($imgInfo['mime'] === 'image/png', "screenshot MIME type is image/png (actual: {$imgInfo['mime']})");
    }
}

// 2. Readme.txt verification
echo "\n2. Readme.txt validation:\n";
$readmePath = $themeDir . '/readme.txt';
assert_true(file_exists($readmePath), 'readme.txt exists in theme root');
if (file_exists($readmePath)) {
    $readmeContent = file_get_contents($readmePath);
    assert_true(strpos($readmeContent, '=== Carmilla ===') !== false, 'readme.txt has theme header === Carmilla ===');
    assert_true(strpos($readmeContent, 'License: GPLv2 or later') !== false, 'readme.txt defines GPL license');
    assert_true(strpos($readmeContent, 'License URI: https://www.gnu.org/licenses/gpl-2.0.html') !== false, 'readme.txt defines License URI');
    assert_true(strpos($readmeContent, 'Tested up to: 6.4') !== false, 'readme.txt defines Tested up to: 6.4');
    assert_true(strpos($readmeContent, 'Requires at least: 6.3') !== false, 'readme.txt defines Requires at least: 6.3');
    assert_true(strpos($readmeContent, '== Description ==') !== false, 'readme.txt contains Description section');
    assert_true(strpos($readmeContent, '== Installation ==') !== false, 'readme.txt contains Installation section');
    assert_true(strpos($readmeContent, '== Changelog ==') !== false, 'readme.txt contains Changelog section');
    assert_true(strpos($readmeContent, '== Copyright ==') !== false, 'readme.txt contains Copyright section');
}

// 3. style.css header metadata verification
echo "\n3. style.css metadata validation:\n";
$stylePath = $themeDir . '/style.css';
assert_true(file_exists($stylePath), 'style.css exists in theme root');
if (file_exists($stylePath)) {
    $styleContent = file_get_contents($stylePath);
    
    // Check required header fields
    $requiredHeaders = [
        'Theme Name:' => 'Carmilla',
        'Theme URI:' => 'https://github.com/puriakazemieh/Shop-Kotlin-kmp',
        'Author:' => 'Carmilla',
        'Author URI:' => 'https://github.com/puriakazemieh/Shop-Kotlin-kmp',
        'Description:' => 'پوسته‌ی فارسی/RTL کارمیلا',
        'Version:' => '0.8.0',
        'Requires at least:' => '6.3',
        'Tested up to:' => '6.4',
        'Requires PHP:' => '7.4',
        'License:' => 'GPL-2.0-or-later',
        'License URI:' => 'https://www.gnu.org/licenses/gpl-2.0.html',
        'Text Domain:' => 'carmilla',
        'Tags:' => 'e-commerce, custom-colors, theme-options, translation-ready',
    ];
    
    foreach ($requiredHeaders as $header => $expectedFragment) {
        $found = false;
        if (preg_match('/' . preg_quote($header, '/') . '\s*(.+)/', $styleContent, $matches)) {
            $val = trim($matches[1]);
            $found = (strpos($val, $expectedFragment) !== false);
        }
        assert_true($found, "style.css header contains '$header $expectedFragment'");
    }
    
    // Check GPL copyright notice comment
    assert_true(strpos($styleContent, 'Carmilla WordPress Theme, (C) 2026') !== false, 'style.css has copyright declaration');
    assert_true(strpos($styleContent, 'GNU GPL version 2 or later') !== false, 'style.css has GPL declaration');
    
    // Check that disallowed/flagged tags ('rtl', 'woocommerce') are NOT present in Tags
    if (preg_match('/Tags:\s*(.+)/', $styleContent, $tagsMatch)) {
        $tagsStr = $tagsMatch[1];
        $tags = array_map('trim', explode(',', $tagsStr));
        assert_true(!in_array('rtl', $tags, true), "Tags list does NOT contain deprecated non-standard 'rtl' tag");
        assert_true(!in_array('woocommerce', $tags, true), "Tags list does NOT contain non-standard 'woocommerce' tag");
    } else {
        assert_true(false, "Tags header line found");
    }
}

echo "\nSummary: $passed passed, $failed failed.\n";
if ($failed > 0) {
    exit(1);
}
echo "All metadata and asset tests passed successfully!\n";
