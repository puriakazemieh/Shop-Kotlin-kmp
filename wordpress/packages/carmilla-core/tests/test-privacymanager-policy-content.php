<?php
/**
 * Test PrivacyManager Policy Content & Theme Check i18n compatibility.
 * Task: P02-CORE-CODE-010C3D4
 */

$rootDir = dirname(dirname(dirname(__DIR__)));
$coreDir = dirname(__DIR__);

if (!defined('ABSPATH')) {
    define('ABSPATH', '/app/');
}

require_once $coreDir . '/Carmilla_Kernel.php';
require_once $coreDir . '/inc/Privacy/PrivacyManager.php';

// Mock minimal WordPress functions if not loaded
if (!function_exists('__')) {
    function __($text, $domain = 'default') {
        return $text;
    }
}
if (!function_exists('wp_add_privacy_policy_content')) {
    function wp_add_privacy_policy_content($name, $content) {}
}
if (!function_exists('wpautop')) {
    function wpautop($text) { return $text; }
}
if (!function_exists('wp_kses_post')) {
    function wp_kses_post($text) { return $text; }
}

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

echo "=== Test PrivacyManager Policy Content (P02-CORE-CODE-010C3D4) ===\n\n";

// 1. Content completeness
echo "1. Verify get_privacy_policy_content output:\n";
$content = \Carmilla\Core\Privacy\PrivacyManager::get_privacy_policy_content();
assert_true(is_string($content) && strlen($content) > 100, 'get_privacy_policy_content() returns non-empty string');
assert_true(strpos($content, '<h2>Carmilla Platform Privacy Policy Guide</h2>') !== false, 'contains Guide heading');
assert_true(strpos($content, '<h3>What Personal Data We Collect and Why</h3>') !== false, 'contains What Personal Data We Collect section');
assert_true(strpos($content, 'Commerce & Orders:') !== false, 'contains Commerce & Orders description');
assert_true(strpos($content, 'Clinic & Appointments:') !== false, 'contains Clinic & Appointments description');
assert_true(strpos($content, 'Psychological Testing:') !== false, 'contains Psychological Testing description');
assert_true(strpos($content, 'Academy & Courses:') !== false, 'contains Academy & Courses description');
assert_true(strpos($content, '<h3>Data Retention and Financial Compliance</h3>') !== false, 'contains Data Retention section');
assert_true(strpos($content, '<h3>User Rights</h3>') !== false, 'contains User Rights section');

// 2. Syntax & source analysis
echo "\n2. Verify source code structure of PrivacyManager.php:\n";
$sourceFile = $coreDir . '/inc/Privacy/PrivacyManager.php';
assert_true(file_exists($sourceFile), 'PrivacyManager.php exists');
$source = file_get_contents($sourceFile);

// Check that get_privacy_policy_content does not invoke __() on concatenated HTML string
$methodBody = '';
if (preg_match('/function get_privacy_policy_content\(\)[^{]*\{(.*?)\n\t\}/s', $source, $m)) {
    $methodBody = $m[1];
}
assert_true(!empty($methodBody), 'Extracted get_privacy_policy_content() method body');
assert_true(strpos($methodBody, '__(') === false, 'get_privacy_policy_content() does not wrap concatenated HTML in __()');

// 3. Theme Check TextDomain_Check token simulation
echo "\n3. Theme Check TextDomain_Check token simulation:\n";
$tokens = token_get_all($source);
$inFunc = false;
$argsStarted = false;
$parensBalance = 0;
$argCount = 0;
$argWarnings = [];
$foundDomains = [];

$funcs = ['__', '_e', '_x', 'esc_attr__', 'esc_html__'];

foreach ($tokens as $token) {
    if (is_array($token)) {
        list($id, $text) = $token;
        if (T_STRING == $id && in_array($text, $funcs, true)) {
            $inFunc = true;
            $func = $text;
            $parensBalance = 0;
            $argsStarted = false;
        } elseif (T_CONSTANT_ENCAPSED_STRING == $id && $inFunc && $argsStarted) {
            if ($parensBalance === 1) {
                if ($argCount >= 2 && ($func === '__' || $func === '_e' || $func === 'esc_attr__' || $func === 'esc_html__')) {
                    $argWarnings[] = "Too many arguments ($argCount) for $func: $text";
                } elseif ($argCount === 1) {
                    $foundDomains[] = trim($text, "'\"");
                }
                $argCount++;
            }
        }
    } elseif ($token === '(') {
        if ($parensBalance === 0) {
            $argsStarted = true;
            $argCount = 0;
        }
        $parensBalance++;
    } elseif ($token === ')') {
        $parensBalance--;
        if ($inFunc && $parensBalance === 0) {
            $inFunc = false;
            $argsStarted = false;
        }
    }
}

assert_true(count($argWarnings) === 0, 'No incorrect argument count warnings detected by Theme Check parser simulation');
$uniqueDomains = array_unique($foundDomains);
assert_true(count($uniqueDomains) === 1 && in_array('carmilla', $uniqueDomains, true), 'Only carmilla textdomain detected across all translation calls');

echo "\nSummary: $passed passed, $failed failed.\n";
if ($failed > 0) {
    exit(1);
}
echo "PrivacyManager policy content & i18n checks passed successfully!\n";
