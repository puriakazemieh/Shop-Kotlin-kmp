<?php
/**
 * Acceptance Test Suite for P04-WPTHEME-CODE-005:
 * اتصال میزبان پوسته به هسته داخلی بدون Bridge (Theme host connection to internal kernel without Bridge)
 *
 * Validates:
 * 1. Independent Theme loader and ThemeHostAdapter initialization without Bridge.
 * 2. Strict separation of Theme-owned appearance settings (Theme Mods) and site-owned capability status (Kernel).
 * 3. Base fixture parity between Standalone Theme mode and Co-install with Bridge.
 * 4. Vertical availability, prerequisite handling, and template function execution without Bridge.
 */

define('ABSPATH', dirname(__DIR__, 3) . '/');

// Mock WordPress functions
if (!function_exists('add_action')) {
    $wp_actions = [];
    function add_action($tag, $callback, $priority = 10, $accepted_args = 1) {
        global $wp_actions;
        $wp_actions[$tag][] = ['callback' => $callback, 'priority' => $priority];
    }
}

if (!function_exists('add_filter')) {
    $wp_filters = [];
    function add_filter($tag, $callback, $priority = 10, $accepted_args = 1) {
        global $wp_filters;
        $wp_filters[$tag][] = ['callback' => $callback, 'priority' => $priority];
    }
}

if (!function_exists('apply_filters')) {
    function apply_filters($tag, $value, ...$args) {
        return $value;
    }
}

if (!function_exists('__')) {
    function __($text, $domain = 'default') {
        return $text;
    }
}

if (!function_exists('esc_html__')) {
    function esc_html__($text, $domain = 'default') {
        return $text;
    }
}

if (!function_exists('esc_url')) {
    function esc_url($url) {
        return $url;
    }
}

if (!function_exists('esc_attr')) {
    function esc_attr($attr) {
        return $attr;
    }
}

if (!function_exists('home_url')) {
    function home_url($path = '/') {
        return 'https://example.test' . $path;
    }
}

if (!function_exists('get_site_url')) {
    function get_site_url() {
        return 'https://example.com';
    }
}

if (!function_exists('is_rtl')) {
    function is_rtl() {
        return true;
    }
}

$wp_options = [];
if (!function_exists('get_option')) {
    function get_option($name, $default = false) {
        global $wp_options;
        return $wp_options[$name] ?? $default;
    }
}

if (!function_exists('update_option')) {
    function update_option($name, $value, $autoload = null) {
        global $wp_options;
        $wp_options[$name] = $value;
        return true;
    }
}

if (!function_exists('add_option')) {
    function add_option($name, $value) {
        global $wp_options;
        if (!array_key_exists($name, $wp_options)) {
            $wp_options[$name] = $value;
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
    class Mock_Admin_Role_005 {
        public $capabilities = [];
        public function add_cap($cap) { $this->capabilities[$cap] = true; }
    }
    function get_role($role) {
        static $mock = null;
        if ($mock === null) { $mock = new Mock_Admin_Role_005(); }
        return $mock;
    }
}

$theme_mod_access_count = 0;
$wp_theme_mods = [];
if (!function_exists('get_theme_mod')) {
    function get_theme_mod($name, $default = false) {
        global $wp_theme_mods, $theme_mod_access_count;
        $theme_mod_access_count++;
        return $wp_theme_mods[$name] ?? $default;
    }
}

if (!function_exists('set_theme_mod')) {
    function set_theme_mod($name, $value) {
        global $wp_theme_mods;
        $wp_theme_mods[$name] = $value;
        return true;
    }
}

if (!function_exists('post_type_exists')) {
    function post_type_exists($post_type) {
        return in_array($post_type, ['post', 'page', 'cb_course', 'cb_therapist', 'cb_psychtest', 'cb_story', 'cb_banner'], true);
    }
}

if (!function_exists('get_post_type_archive_link')) {
    function get_post_type_archive_link($post_type) {
        return 'https://example.com/' . $post_type;
    }
}

if (!function_exists('get_permalink')) {
    function get_permalink($post = 0) {
        return 'https://example.com/blog';
    }
}

$theme_dir = file_exists(dirname(__DIR__, 3) . '/carmilla-theme/inc/class-theme-host-adapter.php')
    ? dirname(__DIR__, 3) . '/carmilla-theme'
    : (file_exists(dirname(__DIR__, 3) . '/inc/class-theme-host-adapter.php') ? dirname(__DIR__, 3) : '/var/www/html/wp-content/themes/carmilla-theme');

if (!function_exists('get_template_directory')) {
    function get_template_directory() {
        global $theme_dir;
        return $theme_dir;
    }
}

// Load Kernel and Theme components
require_once dirname(__DIR__) . '/Carmilla_Kernel.php';
require_once $theme_dir . '/inc/class-theme-host-adapter.php';
require_once $theme_dir . '/inc/customizer.php';
require_once $theme_dir . '/inc/template-functions.php';

use Carmilla\Theme\ThemeHostAdapter;

$passed = 0;
$failed = 0;

function assert_test(bool $condition, string $description): void {
    global $passed, $failed;
    if ($condition) {
        echo "PASS - {$description}\n";
        $passed++;
    } else {
        echo "FAIL - {$description}\n";
        $failed++;
    }
}

echo "=================================================================\n";
echo " Running Acceptance Tests for P04-WPTHEME-CODE-005\n";
echo "=================================================================\n\n";

// -------------------------------------------------------------
// Group 1: Theme Host Boot & Adapter Independence (Without Bridge)
// -------------------------------------------------------------
echo "-- Group 1: Theme Host Boot & Adapter Independence --\n";

Carmilla_Kernel::reset_state();
ThemeHostAdapter::reset_state();

assert_test(!Carmilla_Kernel::is_booted(), "Kernel is initially unbooted");
assert_test(!ThemeHostAdapter::is_initialized(), "ThemeHostAdapter is initially uninitialized");

// Simulate Theme boot in theme-only mode
Carmilla_Kernel::boot('theme', '0.8.0');
ThemeHostAdapter::init();

assert_test(Carmilla_Kernel::is_booted(), "Kernel is booted in Theme-only mode");
assert_test(Carmilla_Kernel::is_booted_by('theme'), "Kernel confirms boot by 'theme'");
assert_test(!Carmilla_Kernel::is_booted_by('plugin'), "Kernel confirms 'plugin' is NOT booted");
assert_test(ThemeHostAdapter::is_initialized(), "ThemeHostAdapter is initialized");
assert_test(ThemeHostAdapter::is_kernel_booted(), "ThemeHostAdapter confirms kernel is booted");
assert_test(ThemeHostAdapter::is_standalone(), "ThemeHostAdapter reports standalone mode (no Bridge)");
assert_test(!ThemeHostAdapter::has_bridge(), "ThemeHostAdapter reports has_bridge is false");
assert_test(ThemeHostAdapter::get_version() === '0.8.0', "ThemeHostAdapter returns correct theme version (0.8.0)");

// Verify Bridge classes are completely absent
assert_test(!class_exists('CB_Plugin', false), "CB_Plugin class is NOT loaded");
assert_test(!class_exists('CB_Standalone_Boot', false), "CB_Standalone_Boot class is NOT loaded");
assert_test(!class_exists('CB_Integrations_Controller', false), "CB_Integrations_Controller class is NOT loaded");

// -------------------------------------------------------------
// Group 2: Strict Separation of Appearance vs Capability Settings
// -------------------------------------------------------------
echo "\n-- Group 2: Separation of Appearance vs Capability Settings --\n";

// 1. Appearance settings must come strictly from Theme Mods
$wp_theme_mods['carmilla_color_accent'] = '#112233';
$wp_theme_mods['carmilla_hero_title'] = 'تست عنوان هیرو';

$theme_mod_access_count = 0;
$appearance = ThemeHostAdapter::get_appearance_settings();

assert_test($theme_mod_access_count > 0, "Appearance settings accessed Theme Mods");
assert_test($appearance['colors']['accent'] === '#112233', "Custom accent color loaded from Theme Mod");
assert_test($appearance['hero_title'] === 'تست عنوان هیرو', "Hero title loaded from Theme Mod");
assert_test($appearance['rtl'] === true, "RTL flag present in appearance settings");

// Verify NO capability keys exist in appearance settings
$forbidden_capability_keys = ['commerce.core', 'academy.core', 'clinic.booking', 'psych.tests', 'shop', 'courses'];
$appearance_flattened_keys = array_merge(array_keys($appearance), array_keys($appearance['colors']));
$has_forbidden_in_appearance = false;
foreach ($forbidden_capability_keys as $forbidden) {
    if (in_array($forbidden, $appearance_flattened_keys, true)) {
        $has_forbidden_in_appearance = true;
        break;
    }
}
assert_test(!$has_forbidden_in_appearance, "Appearance settings contain ZERO capability/entitlement toggles");

// 2. Capability status must come strictly from Kernel (NOT Theme Mods)
$theme_mod_access_count = 0;
$capabilities = ThemeHostAdapter::get_capability_status();

assert_test($theme_mod_access_count === 0, "Theme Mods were NEVER consulted for capability status");
assert_test(is_array($capabilities) && !empty($capabilities), "Capabilities returned as non-empty array from Kernel");
assert_test(isset($capabilities['theme.base']), "Capability 'theme.base' present in site capability status");
assert_test(isset($capabilities['content.core']), "Capability 'content.core' present in site capability status");

// Verify customizer function carmilla_feature_enabled() does NOT consult Theme Mods
$theme_mod_access_count = 0;
$blog_enabled = carmilla_feature_enabled('blog');
$stories_enabled = carmilla_feature_enabled('stories');
assert_test($theme_mod_access_count === 0, "carmilla_feature_enabled() does NOT consult Theme Mods");
assert_test($blog_enabled === true, "carmilla_feature_enabled('blog') is true in unlocked base fixture");
assert_test($stories_enabled === true, "carmilla_feature_enabled('stories') is true in unlocked base fixture");

// -------------------------------------------------------------
// Group 3: Base Fixture Parity (Standalone Theme vs Co-install)
// -------------------------------------------------------------
echo "\n-- Group 3: Base Fixture Parity (Standalone vs Co-install) --\n";

// Capture standalone capabilities
$standalone_capabilities = ThemeHostAdapter::get_capability_status();
$standalone_blog = ThemeHostAdapter::is_feature_enabled('blog');
$standalone_stories = ThemeHostAdapter::is_feature_enabled('stories');
$standalone_courses = ThemeHostAdapter::is_feature_enabled('courses');

// Now simulate Co-install by booting plugin as second context
Carmilla_Kernel::boot('plugin', '1.0.0');

assert_test(Carmilla_Kernel::is_booted_by('theme'), "Co-install: Theme context preserved");
assert_test(Carmilla_Kernel::is_booted_by('plugin'), "Co-install: Plugin context registered");
assert_test(ThemeHostAdapter::has_bridge(), "ThemeHostAdapter detects Bridge presence in co-install");
assert_test(!ThemeHostAdapter::is_standalone(), "ThemeHostAdapter is_standalone is false in co-install");

// Verify feature resolution parity between standalone and co-install
$coinstall_capabilities = ThemeHostAdapter::get_capability_status();
$coinstall_blog = ThemeHostAdapter::is_feature_enabled('blog');
$coinstall_stories = ThemeHostAdapter::is_feature_enabled('stories');
$coinstall_courses = ThemeHostAdapter::is_feature_enabled('courses');

assert_test($standalone_blog === $coinstall_blog, "Parity: 'blog' feature state identical between Standalone and Co-install");
assert_test($standalone_stories === $coinstall_stories, "Parity: 'stories' feature state identical between Standalone and Co-install");
assert_test($standalone_courses === $coinstall_courses, "Parity: 'courses' feature state identical between Standalone and Co-install");

$diff_keys = array_diff_assoc($standalone_capabilities, $coinstall_capabilities);
assert_test(empty($diff_keys), "Parity: 100% exact match across all active capabilities between Standalone and Co-install");

// -------------------------------------------------------------
// Group 4: Vertical Availability & Prerequisite Checks
// -------------------------------------------------------------
echo "\n-- Group 4: Vertical Availability & Prerequisite Checks --\n";

// In the current test environment, WooCommerce class is not defined
assert_test(!class_exists('WooCommerce', false), "WooCommerce is currently absent");

// Shop vertical must be disabled without WooCommerce even if entitlement claims exist
assert_test(!ThemeHostAdapter::is_vertical_available('shop'), "Shop vertical unavailable when WooCommerce is missing");
assert_test(!carmilla_feature_enabled('shop'), "carmilla_feature_enabled('shop') is false without WooCommerce");

// Content / Academy / Clinic verticals do not depend on WooCommerce
assert_test(ThemeHostAdapter::is_vertical_available('blog'), "Blog vertical is available standalone");
assert_test(ThemeHostAdapter::is_vertical_available('courses'), "Courses vertical is available standalone");
assert_test(ThemeHostAdapter::is_vertical_available('clinic'), "Clinic vertical is available standalone");
assert_test(ThemeHostAdapter::is_vertical_available('psychtests'), "Psych tests vertical is available standalone");

// CPT Inventory check
$cpts = ThemeHostAdapter::get_cpt_inventory();
assert_test(count($cpts) === 6, "Theme manages exactly 6 vertical CPTs in standalone mode");
assert_test(isset($cpts['cb_course']), "CPT 'cb_course' managed by theme");
assert_test(isset($cpts['cb_therapist']), "CPT 'cb_therapist' managed by theme");
assert_test(isset($cpts['cb_psychtest']), "CPT 'cb_psychtest' managed by theme");
assert_test(isset($cpts['cb_course_request']), "CPT 'cb_course_request' managed by theme");
assert_test(isset($cpts['cb_story']), "CPT 'cb_story' managed by theme");
assert_test(isset($cpts['cb_banner']), "CPT 'cb_banner' managed by theme");

// -------------------------------------------------------------
// Group 5: Template Functions & Navigation Rendering Standalone
// -------------------------------------------------------------
echo "\n-- Group 5: Template Functions & Navigation Standalone --\n";

// Verify price formatting without WooCommerce
assert_test(carmilla_price(150000) === '۱۵۰٬۰۰۰ تومان', "carmilla_price() formats Persian price correctly without WooCommerce");
assert_test(carmilla_cart_count() === 0, "carmilla_cart_count() returns 0 safely without WooCommerce");

// Verify navigation destinations exclude shop/cart when WooCommerce is absent
$nav = carmilla_bottom_nav_items();
assert_test(is_array($nav) && !empty($nav), "carmilla_bottom_nav_items() returns valid destinations array");
$labels = array_column($nav, 'label');
assert_test(in_array('خانه', $labels, true), "Navigation includes 'خانه' (Home)");
assert_test(!in_array('فروشگاه', $labels, true), "Navigation excludes 'فروشگاه' (Shop) when WooCommerce is absent");
assert_test(!in_array('سبد', $labels, true), "Navigation excludes 'سبد' (Cart) when WooCommerce is absent");

echo "\n=================================================================\n";
if ($failed === 0) {
    echo "ALL {$passed} P04-WPTHEME-CODE-005 ACCEPTANCE TESTS PASSED!\n";
    echo "=================================================================\n";
    exit(0);
} else {
    echo "FAILED: {$failed} test(s) failed out of " . ($passed + $failed) . ".\n";
    echo "=================================================================\n";
    exit(1);
}
