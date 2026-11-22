<?php
/**
 * Dedicated Acceptance Test Suite for Task P04-WPPLUGIN-CODE-004:
 * 1. Both ZIPs bundle identical kernel from shared packages/carmilla-core source
 * 2. Theme-only independent bootstrap without Bridge plugin or companion plugin
 * 3. Plugin-only independent bootstrap without Carmilla Theme (running on third-party theme)
 * 4. Co-install single boot: Plugin + Theme co-install boots exactly once with zero collisions
 * 5. Kernel state reset and multi-context tracking
 *
 * Can be executed via Docker:
 * docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/packages/carmilla-core/tests/test-kernel-bootstrap-004.php
 */

if (!defined('ABSPATH')) {
    define('ABSPATH', __DIR__ . '/');
}

// Global action tracking
$GLOBALS['carmilla_test_actions'] = [];
if (!function_exists('add_action')) {
    function add_action($hook, $callback, $priority = 10, $accepted_args = 1) {
        $GLOBALS['carmilla_test_actions'][] = [
            'hook'     => $hook,
            'callback' => $callback,
            'priority' => $priority
        ];
    }
}
if (!function_exists('get_option')) {
    $GLOBALS['mock_options'] = [];
    function get_option($name, $default = false) {
        return array_key_exists($name, $GLOBALS['mock_options']) ? $GLOBALS['mock_options'][$name] : $default;
    }
}
if (!function_exists('update_option')) {
    function update_option($name, $value, $autoload = null) {
        $GLOBALS['mock_options'][$name] = $value;
        return true;
    }
}
if (!function_exists('add_option')) {
    function add_option($name, $value) {
        if (!array_key_exists($name, $GLOBALS['mock_options'])) {
            $GLOBALS['mock_options'][$name] = $value;
            return true;
        }
        return false;
    }
}
if (!function_exists('get_transient')) {
    function get_transient($name) { return false; }
}
if (!function_exists('set_transient')) {
    function set_transient($name, $val, $exp = 0) { return true; }
}
if (!function_exists('delete_transient')) {
    function delete_transient($name) { return true; }
}
if (!function_exists('get_role')) {
    class Mock_Admin_Role {
        public $caps = [];
        public function add_cap($cap) { $this->caps[$cap] = true; }
    }
    $GLOBALS['mock_admin_role'] = new Mock_Admin_Role();
    function get_role($role) {
        return $role === 'administrator' ? $GLOBALS['mock_admin_role'] : null;
    }
}
if (!function_exists('get_template_directory')) {
    function get_template_directory() {
        return dirname(__DIR__, 2) . '/carmilla-theme';
    }
}
if (!function_exists('plugin_dir_path')) {
    function plugin_dir_path($file) {
        return dirname(__DIR__, 2) . '/carmilla-bridge/';
    }
}

if (!function_exists('add_filter')) {
    function add_filter($tag, $callback, $priority = 10, $accepted_args = 1) { return true; }
}

// Load Carmilla Kernel
require_once dirname(__DIR__) . '/Carmilla_Kernel.php';

use Carmilla\Core\Migration\SchemaRunner;

$fail = 0;
function assert_test(bool $condition, string $description) {
    global $fail;
    echo ($condition ? 'PASS' : 'FAIL') . " - " . $description . "\n";
    if (!$condition) {
        $fail++;
    }
}

echo "=================================================================\n";
echo " Running Acceptance Tests for P04-WPPLUGIN-CODE-004\n";
echo "=================================================================\n\n";

// -------------------------------------------------------------
// Test Group 1: Source Packaging and ZIP Artifact Inventory
// -------------------------------------------------------------
echo "-- Group 1: Shared Kernel Source & ZIP Packaging Scripts --\n";

$wp_candidates = [
    dirname(__DIR__, 3),
    dirname(__DIR__, 4) . '/wordpress',
    dirname(__DIR__, 5) . '/wordpress',
    '/var/www/html/wp-content',
    '/app/wordpress',
];
$theme_build_script = '';
foreach ($wp_candidates as $cand) {
    if (file_exists($cand . '/build-theme-zip.sh')) {
        $theme_build_script = $cand . '/build-theme-zip.sh';
        break;
    }
}
$bridge_build_script = '';
foreach ($wp_candidates as $cand) {
    if (file_exists($cand . '/build-bridge-zip.sh')) {
        $bridge_build_script = $cand . '/build-bridge-zip.sh';
        break;
    }
}

assert_test(!empty($theme_build_script) && file_exists($theme_build_script), 'build-theme-zip.sh exists');
assert_test(!empty($bridge_build_script) && file_exists($bridge_build_script), 'build-bridge-zip.sh exists');

$theme_script_content = file_get_contents($theme_build_script);
$bridge_script_content = file_get_contents($bridge_build_script);

assert_test(strpos($theme_script_content, 'packages/carmilla-core') !== false, 'Theme build script packages packages/carmilla-core');
assert_test(strpos($bridge_script_content, 'packages/carmilla-core') !== false, 'Bridge build script packages packages/carmilla-core');
assert_test(strpos($theme_script_content, 'companion') === false, 'No companion plugin packaged in Theme ZIP');
assert_test(strpos($bridge_script_content, 'companion') === false, 'No companion plugin packaged in Bridge ZIP');

// Check built zip files
$theme_zip = dirname($theme_build_script) . '/carmilla-theme.zip';
$bridge_zip = dirname($bridge_build_script) . '/carmilla-bridge.zip';

assert_test(file_exists($theme_zip), 'carmilla-theme.zip artifact exists');
assert_test(file_exists($bridge_zip), 'carmilla-bridge.zip artifact exists');

// -------------------------------------------------------------
// Test Group 2: Independent Theme-Only Bootstrap
// -------------------------------------------------------------
echo "\n-- Group 2: Independent Theme-Only Bootstrap --\n";

Carmilla_Kernel::reset_state();
assert_test(Carmilla_Kernel::is_booted() === false, 'Kernel is unbooted initially');

// Simulate Theme-only boot
Carmilla_Kernel::boot('theme', '0.8.0');

assert_test(Carmilla_Kernel::is_booted() === true, 'Kernel successfully boots in Theme-only mode');
assert_test(Carmilla_Kernel::is_booted_by('theme') === true, 'Kernel registers theme context');
assert_test(Carmilla_Kernel::is_booted_by('plugin') === false, 'Plugin context is false in Theme-only mode');

$theme_contexts = Carmilla_Kernel::get_boot_contexts();
assert_test(isset($theme_contexts['theme']) && $theme_contexts['theme'] === '0.8.0', 'Theme version recorded correctly');
assert_test(!isset($theme_contexts['plugin']), 'No plugin context present in Theme-only mode');

// -------------------------------------------------------------
// Test Group 3: Independent Plugin-Only Bootstrap
// -------------------------------------------------------------
echo "\n-- Group 3: Independent Plugin-Only Bootstrap (Third-Party Theme) --\n";

Carmilla_Kernel::reset_state();
assert_test(Carmilla_Kernel::is_booted() === false, 'Kernel reset clean');

// Simulate Plugin-only boot
Carmilla_Kernel::boot('plugin', '1.0.0');

assert_test(Carmilla_Kernel::is_booted() === true, 'Kernel successfully boots in Plugin-only mode');
assert_test(Carmilla_Kernel::is_booted_by('plugin') === true, 'Kernel registers plugin context');
assert_test(Carmilla_Kernel::is_booted_by('theme') === false, 'Theme context is false in Plugin-only mode');

$plugin_contexts = Carmilla_Kernel::get_boot_contexts();
assert_test(isset($plugin_contexts['plugin']) && $plugin_contexts['plugin'] === '1.0.0', 'Plugin version recorded correctly');
assert_test(!isset($plugin_contexts['theme']), 'No theme context present in Plugin-only mode');

// -------------------------------------------------------------
// Test Group 4: Co-Install Single Boot (Zero Duplication & No Collisions)
// -------------------------------------------------------------
echo "\n-- Group 4: Co-Install Single Boot (Zero Collisions) --\n";

Carmilla_Kernel::reset_state();
$initial_actions_count = count($GLOBALS['carmilla_test_actions']);

// 1. In WordPress, plugins_loaded fires first -> Plugin boots
Carmilla_Kernel::boot('plugin', '1.0.0');
$after_plugin_actions = count($GLOBALS['carmilla_test_actions']);

assert_test(Carmilla_Kernel::is_booted_by('plugin') === true, 'Co-install: Plugin context registered on first boot');
assert_test($after_plugin_actions > $initial_actions_count, 'Kernel initialized on first boot (actions added)');

// 2. In WordPress, after_setup_theme fires second -> Theme boots
Carmilla_Kernel::boot('theme', '0.8.0');
$after_theme_actions = count($GLOBALS['carmilla_test_actions']);

assert_test(Carmilla_Kernel::is_booted_by('theme') === true, 'Co-install: Theme context registered on second call');
assert_test($after_theme_actions === $after_plugin_actions, 'Co-install: Zero duplicate actions registered on second boot');

$co_contexts = Carmilla_Kernel::get_boot_contexts();
assert_test(isset($co_contexts['plugin']) && $co_contexts['plugin'] === '1.0.0', 'Both contexts: plugin is 1.0.0');
assert_test(isset($co_contexts['theme']) && $co_contexts['theme'] === '0.8.0', 'Both contexts: theme is 0.8.0');

// -------------------------------------------------------------
// Test Group 5: Theme Loader Fallback Verification
// -------------------------------------------------------------
echo "\n-- Group 5: Host Loader Paths and Clean Inclusion --\n";

$theme_fn_candidates = [
    dirname(__DIR__, 3) . '/carmilla-theme/functions.php',
    dirname(__DIR__, 4) . '/wordpress/carmilla-theme/functions.php',
    '/var/www/html/wp-content/themes/carmilla-theme/functions.php',
];
$bridge_fn_candidates = [
    dirname(__DIR__, 3) . '/carmilla-bridge/carmilla-bridge.php',
    dirname(__DIR__, 4) . '/wordpress/carmilla-bridge/carmilla-bridge.php',
    '/var/www/html/wp-content/plugins/carmilla-bridge/carmilla-bridge.php',
];

$theme_functions = '';
foreach ($theme_fn_candidates as $cand) {
    if (file_exists($cand)) {
        $theme_functions = file_get_contents($cand);
        break;
    }
}
$bridge_main = '';
foreach ($bridge_fn_candidates as $cand) {
    if (file_exists($cand)) {
        $bridge_main = file_get_contents($cand);
        break;
    }
}

assert_test(!empty($theme_functions) && strpos($theme_functions, 'Carmilla_Kernel') !== false, 'Theme functions.php includes Carmilla_Kernel check');
assert_test(!empty($theme_functions) && strpos($theme_functions, 'Carmilla_Kernel::boot') !== false, 'Theme functions.php calls Carmilla_Kernel::boot');
assert_test(!empty($bridge_main) && strpos($bridge_main, 'Carmilla_Kernel') !== false, 'Bridge main file includes Carmilla_Kernel check');
assert_test(!empty($bridge_main) && strpos($bridge_main, 'Carmilla_Kernel::boot') !== false, 'Bridge main file calls Carmilla_Kernel::boot');

echo "\n=================================================================\n";
echo ($fail === 0 ? "ALL P04-WPPLUGIN-CODE-004 ACCEPTANCE TESTS PASSED!\n" : "$fail TESTS FAILED!\n");
echo "=================================================================\n";

exit($fail === 0 ? 0 : 1);
