<?php
/**
 * Dedicated Acceptance Test Suite for Task P04-ENTITLEMENT-CODE-040:
 * 1. License signature, site binding, tampering, expiration, grace period, and revocation enforcement.
 * 2. Fail-closed feature resolution on invalid or unpurchased licenses.
 * 3. Direct POST request rejection (HTTP 403 FEATURE_DISABLED) on unpurchased features.
 * 4. App Builder independence: disabling builders never shuts down client data APIs.
 * 5. Co-install single-counted quotas (zero duplication).
 * 6. External prerequisite isolation (missing WooCommerce only closes commerce).
 * 7. Minimal public manifest (zero credentials, secrets, or license keys exposed).
 */

if (!defined('DAY_IN_SECONDS')) {
    define('DAY_IN_SECONDS', 86400);
}

if (!defined('ABSPATH')) {
    define('ABSPATH', __DIR__ . '/');
}

// In-memory options, transients, and mocks
$GLOBALS['mock_options'] = [];
$GLOBALS['mock_transients'] = [];

if (!function_exists('get_option')) {
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
if (!function_exists('delete_option')) {
    function delete_option($name) {
        unset($GLOBALS['mock_options'][$name]);
        return true;
    }
}
if (!function_exists('get_transient')) {
    function get_transient($transient) {
        return array_key_exists($transient, $GLOBALS['mock_transients']) ? $GLOBALS['mock_transients'][$transient] : false;
    }
}
if (!function_exists('set_transient')) {
    function set_transient($transient, $value, $expiration = 0) {
        $GLOBALS['mock_transients'][$transient] = $value;
        return true;
    }
}
if (!function_exists('delete_transient')) {
    function delete_transient($transient) {
        unset($GLOBALS['mock_transients'][$transient]);
        return true;
    }
}
if (!function_exists('get_site_url')) {
    function get_site_url($blog_id = null, $path = '', $scheme = null) {
        return 'https://example.com';
    }
}
if (!function_exists('add_action')) {
    function add_action($hook, $callback, $priority = 10, $accepted_args = 1) {
        return true;
    }
}
if (!function_exists('add_filter')) {
    function add_filter($hook, $callback, $priority = 10, $accepted_args = 1) {
        return true;
    }
}
if (!function_exists('do_action')) {
    function do_action($hook, ...$args) {
        return true;
    }
}
if (!class_exists('WooCommerce')) {
    class WooCommerce {}
}

if (!function_exists('sanitize_key')) {
    function sanitize_key($key) {
        return preg_replace('/[^a-z0-9_\-]/', '', strtolower($key));
    }
}
if (!function_exists('get_current_blog_id')) {
    function get_current_blog_id() {
        return 1;
    }
}
if (!function_exists('wp_json_encode')) {
    function wp_json_encode($data, $options = 0, $depth = 512) {
        return json_encode($data, $options, $depth);
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

// Minimal mock WP_Error
if (!class_exists('WP_Error')) {
    class WP_Error {
        public $code;
        public $message;
        public $data;

        public function __construct($code = '', $message = '', $data = '') {
            $this->code = $code;
            $this->message = $message;
            $this->data = $data;
        }

        public function get_error_code() {
            return $this->code;
        }

        public function get_error_message() {
            return $this->message;
        }

        public function get_error_data() {
            return $this->data;
        }
    }
}

if (!function_exists('is_wp_error')) {
    function is_wp_error($thing) {
        return $thing instanceof WP_Error;
    }
}

// Minimal mock WP_REST_Request
if (!class_exists('WP_REST_Request')) {
    class WP_REST_Request {
        private $method;
        private $route;
        private $params = [];

        public function __construct($method = 'GET', $route = '') {
            $this->method = strtoupper($method);
            $this->route  = $route;
        }

        public function get_method() {
            return $this->method;
        }

        public function get_route() {
            return $this->route;
        }

        public function get_header($name) {
            return null;
        }
    }
}

// Minimal mock WP_REST_Response
if (!class_exists('WP_REST_Response')) {
    class WP_REST_Response {
        public $data;
        public $status;
        public $headers = [];

        public function __construct($data = null, $status = 200, $headers = []) {
            $this->data = $data;
            $this->status = $status;
            $this->headers = $headers;
        }

        public function header($name, $value) {
            $this->headers[$name] = $value;
        }
    }
}

// Load Core classes
require_once dirname(__DIR__) . '/Carmilla_Kernel.php';
require_once dirname(__DIR__) . '/inc/Catalog/EntitlementSchema.php';
require_once dirname(__DIR__) . '/inc/Catalog/EntitlementClaims.php';
require_once dirname(__DIR__) . '/inc/Catalog/CapabilityResolver.php';
require_once dirname(__DIR__) . '/inc/Entitlement/EntitlementResolver.php';

// Load Bridge Manifest Controller for REST dispatch guard testing
$manifest_ctrl_candidates = [
    dirname(__DIR__, 3) . '/carmilla-bridge/includes/class-cb-manifest-controller.php',
    '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-manifest-controller.php',
    '/app/wordpress/carmilla-bridge/includes/class-cb-manifest-controller.php',
];
foreach ($manifest_ctrl_candidates as $cand) {
    if (file_exists($cand)) {
        require_once $cand;
        break;
    }
}

use Carmilla\Core\Catalog\EntitlementSchema;
use Carmilla\Core\Catalog\EntitlementClaims;
use Carmilla\Core\Catalog\CapabilityResolver;
use Carmilla\Core\Entitlement\EntitlementResolver;

$passed = 0;
$failed = 0;

function assert_test(bool $condition, string $message): void {
    global $passed, $failed;
    if ($condition) {
        echo "PASS - {$message}\n";
        $passed++;
    } else {
        echo "FAIL - {$message}\n";
        $failed++;
    }
}

echo "=================================================================\n";
echo " Acceptance Suite: P04-ENTITLEMENT-CODE-040\n";
echo "=================================================================\n\n";

// -------------------------------------------------------------
// Group 1: Signature & Tampering Enforcement
// -------------------------------------------------------------
echo "-- Group 1: Signature & Tampering Enforcement --\n";

$valid_base = EntitlementClaims::get_fully_unlocked_fixture();
assert_test(EntitlementResolver::validate_license($valid_base) === true, 'Valid signed license passes validation');

// Explicitly tampered
$tampered = array_merge($valid_base, ['tampered' => true]);
assert_test(EntitlementResolver::validate_license($tampered) === false, 'Tampered license is rejected');
$tampered_features = EntitlementResolver::resolve($tampered);
assert_test($tampered_features['commerce.core'] === false, 'Tampered license yields false for commerce.core');
assert_test($tampered_features['content.core'] === false, 'Tampered license yields false for content.core');

// Invalid flag
$invalid = array_merge($valid_base, ['is_valid' => false]);
assert_test(EntitlementResolver::validate_license($invalid) === false, 'License with is_valid=false is rejected');

// Unsigned license
$unsigned = array_merge($valid_base, ['signed' => false]);
assert_test(EntitlementResolver::validate_license($unsigned) === false, 'Unsigned license (signed=false) is rejected');

// Empty signature string
$empty_sig = array_merge($valid_base, ['signature' => '']);
assert_test(EntitlementResolver::validate_license($empty_sig) === false, 'License with empty signature is rejected');

// Fail-closed in CapabilityResolver::resolve_matrix
$matrix_tampered = CapabilityResolver::resolve_matrix(['entitlements' => $tampered]);
assert_test($matrix_tampered['features']['commerce.core']['is_active'] === false, 'CapabilityResolver: tampered commerce.core is inactive');
assert_test($matrix_tampered['features']['commerce.core']['status'] === CapabilityResolver::STATUS_NOT_ENTITLED, 'CapabilityResolver: tampered status is NOT_ENTITLED');
assert_test($matrix_tampered['features']['theme.base']['is_active'] === false, 'CapabilityResolver: tampered theme.base is inactive');

// -------------------------------------------------------------
// Group 2: Site Binding Enforcement
// -------------------------------------------------------------
echo "\n-- Group 2: Site Binding Enforcement --\n";

$wrong_site = array_merge($valid_base, ['site_url' => 'https://unauthorized-domain.com']);
assert_test(EntitlementResolver::validate_license($wrong_site) === false, 'License bound to foreign domain is rejected');

$matrix_wrong_site = CapabilityResolver::resolve_matrix(['entitlements' => $wrong_site]);
assert_test($matrix_wrong_site['features']['commerce.core']['is_active'] === false, 'Wrong-site license fails closed on commerce.core');
assert_test($matrix_wrong_site['features']['clinic.booking']['is_active'] === false, 'Wrong-site license fails closed on clinic.booking');

// Normalized match (port, trailing slash, protocol differences)
$same_site_normalized = array_merge($valid_base, ['site_url' => 'http://example.com/']);
assert_test(EntitlementResolver::validate_license($same_site_normalized) === true, 'URL normalized match passes validation');

// -------------------------------------------------------------
// Group 3: Expiration & Grace Period Lifecycle
// -------------------------------------------------------------
echo "\n-- Group 3: Expiration & Grace Period Lifecycle --\n";

// License expired 5 days ago
$grace_claims = EntitlementClaims::get_grace_period_fixture();
assert_test(EntitlementResolver::get_license_status($grace_claims) === 'grace_period', 'Status of license expired 5 days ago is grace_period');
assert_test(EntitlementResolver::validate_license($grace_claims, false) === false, 'Strict validation without grace rejects expired license');
assert_test(EntitlementResolver::validate_license($grace_claims, true) === true, 'Validation with allow_grace accepts grace_period license');

// In CapabilityResolver with allow_grace, grace period keeps features running
$matrix_grace = CapabilityResolver::resolve_matrix(['entitlements' => $grace_claims]);
assert_test($matrix_grace['features']['commerce.core']['is_active'] === true, 'Grace period maintains commerce.core active');
assert_test($matrix_grace['features']['clinic.booking']['is_active'] === true, 'Grace period maintains clinic.booking active');

// License expired 40 days ago (past 14-day grace)
$long_expired = array_merge($valid_base, ['expires_at' => time() - (40 * 86400)]);
assert_test(EntitlementResolver::get_license_status($long_expired) === 'expired', 'Status of license expired 40 days ago is expired');
assert_test(EntitlementResolver::validate_license($long_expired, true) === false, 'License expired past grace period strictly rejected');
$matrix_expired = CapabilityResolver::resolve_matrix(['entitlements' => $long_expired]);
assert_test($matrix_expired['features']['commerce.core']['is_active'] === false, 'Expired license fails closed in CapabilityResolver');

// -------------------------------------------------------------
// Group 4: Revocation Handling
// -------------------------------------------------------------
echo "\n-- Group 4: Revocation Handling --\n";

$revoked = EntitlementClaims::get_revoked_fixture();
assert_test(EntitlementResolver::get_license_status($revoked) === 'revoked', 'Status is revoked');
assert_test(EntitlementResolver::validate_license($revoked) === false, 'Revoked license is rejected');

$matrix_revoked = CapabilityResolver::resolve_matrix(['entitlements' => $revoked]);
assert_test($matrix_revoked['features']['commerce.core']['is_active'] === false, 'Revocation shuts down commerce.core');
assert_test($matrix_revoked['features']['academy.core']['is_active'] === false, 'Revocation shuts down academy.core');
assert_test($matrix_revoked['features']['commerce.core']['status'] === CapabilityResolver::STATUS_NOT_ENTITLED, 'Revocation status is NOT_ENTITLED');

// -------------------------------------------------------------
// Group 5: Direct POST Rejection on Unpurchased Features
// -------------------------------------------------------------
echo "\n-- Group 5: Direct POST Rejection on Unpurchased Features --\n";

if (class_exists('CB_Manifest_Controller')) {
    // 1. Setup Theme-Only license in options (only theme.base and content.*, NO academy, NO commerce)
    $theme_only = EntitlementClaims::get_base_theme_fixture();
    update_option('carmilla_license_claims', $theme_only);
    update_option('cb_manifest_features', [
        'commerce.core' => true, // Admin toggles ON unpurchased feature
        'academy.core'  => true, // Admin toggles ON unpurchased feature
    ]);

    // Send direct POST request to /api/courses
    $post_course_req = new WP_REST_Request('POST', '/carmilla/v1/api/courses');
    $course_block = CB_Manifest_Controller::guard_rest_request(null, null, $post_course_req);
    assert_test(is_wp_error($course_block), 'Direct POST to unpurchased /api/courses returns WP_Error');
    assert_test($course_block->get_error_code() === 'FEATURE_DISABLED', 'Error code is FEATURE_DISABLED');
    assert_test($course_block->get_error_data()['status'] === 403, 'Error HTTP status is 403 Forbidden');
    assert_test($course_block->get_error_data()['feature'] === 'academy.core', 'Blocked feature identified as academy.core');

    // Send direct POST request to /api/cart
    $post_cart_req = new WP_REST_Request('POST', '/carmilla/v1/api/cart');
    $cart_block = CB_Manifest_Controller::guard_rest_request(null, null, $post_cart_req);
    assert_test(is_wp_error($cart_block), 'Direct POST to unpurchased /api/cart returns WP_Error');
    assert_test($cart_block->get_error_data()['status'] === 403, 'Cart error HTTP status is 403');

    // Send direct POST request to /api/my-psych-tests/1/submit
    $post_psych_req = new WP_REST_Request('POST', '/carmilla/v1/api/my-psych-tests/1/submit');
    $psych_block = CB_Manifest_Controller::guard_rest_request(null, null, $post_psych_req);
    assert_test(is_wp_error($psych_block), 'Direct POST to unpurchased /api/my-psych-tests returns WP_Error');
    assert_test($psych_block->get_error_data()['feature'] === 'psych.tests', 'Blocked feature is psych.tests');

    // Authentication route passes through unblocked
    $auth_req = new WP_REST_Request('POST', '/carmilla/v1/api/auth/web/request-otp');
    $auth_res = CB_Manifest_Controller::guard_rest_request(null, null, $auth_req);
    assert_test($auth_res === null, 'Authentication routes pass through unblocked');

    // Clean up options
    delete_option('carmilla_license_claims');
    delete_option('cb_manifest_features');
} else {
    echo "SKIPPED - CB_Manifest_Controller not available\n";
}

// -------------------------------------------------------------
// Group 6: App Builder vs Data Client API Independence
// -------------------------------------------------------------
echo "\n-- Group 6: App Builder vs Data Client API Independence --\n";

// License with ALL verticals, but ALL builders disabled
$no_builders_claims = [
    'schemaVersion' => 1,
    'license_key'   => 'fixture-no-builders',
    'status'        => 'active',
    'site_url'      => 'https://example.com',
    'skus'          => ['sku_theme_base', 'sku_commerce', 'sku_academy', 'sku_clinic_booking'],
    'builders'      => [
        'android' => false,
        'ios'     => false,
        'desktop' => false,
        'pwa'     => false,
        'web'     => false
    ]
];

assert_test(EntitlementResolver::has_builder_target($no_builders_claims, 'android') === false, 'Android builder target is FALSE');
assert_test(EntitlementResolver::has_builder_target($no_builders_claims, 'ios') === false, 'iOS builder target is FALSE');
assert_test(EntitlementResolver::has_builder_target($no_builders_claims, 'pwa') === false, 'PWA builder target is FALSE');

$matrix_no_builders = CapabilityResolver::resolve_matrix(['entitlements' => $no_builders_claims]);
assert_test($matrix_no_builders['features']['builder.target.android']['is_active'] === false, 'builder.target.android is inactive');
assert_test($matrix_no_builders['features']['commerce.core']['is_active'] === true, 'commerce.core data API remains ACTIVE without builder');
assert_test($matrix_no_builders['features']['academy.core']['is_active'] === true, 'academy.core data API remains ACTIVE without builder');
assert_test($matrix_no_builders['features']['clinic.booking']['is_active'] === true, 'clinic.booking data API remains ACTIVE without builder');
assert_test($matrix_no_builders['features']['content.core']['is_active'] === true, 'content.core data API remains ACTIVE without builder');

// -------------------------------------------------------------
// Group 7: Co-Install Single-Counted Quota Resolution
// -------------------------------------------------------------
echo "\n-- Group 7: Co-Install Single-Counted Quota Resolution --\n";

EntitlementResolver::reset_quota_cache();
\Carmilla_Kernel::reset_state();

$quota_claims = [
    'schemaVersion' => 1,
    'license_key'   => 'fixture-license-quotas',
    'status'        => 'active',
    'site_url'      => 'https://example.com',
    'skus'          => ['sku_commerce', 'sku_academy'],
    'quotas'        => [
        'max_products' => 250,
        'max_courses'  => 50
    ]
];

// First host queries quota
$q_host1 = EntitlementResolver::resolve_effective_quota($quota_claims, 'max_products', 0);
assert_test($q_host1 === 250, 'Host 1 (Theme) resolves max_products to 250');

// Second host in co-install queries quota in same lifecycle
$q_host2 = EntitlementResolver::resolve_effective_quota($quota_claims, 'max_products', 0);
assert_test($q_host2 === 250, 'Host 2 (Plugin) resolves exact same single-counted 250 (NOT 500)');

// Kernel get_quota integration
update_option('carmilla_license_claims', $quota_claims);
\Carmilla_Kernel::boot('theme', '0.8.0');
\Carmilla_Kernel::boot('plugin', '1.0.0');

$kernel_prod_quota = \Carmilla_Kernel::get_quota('max_products');
$kernel_course_quota = \Carmilla_Kernel::get_quota('max_courses');
assert_test($kernel_prod_quota === 250, 'Carmilla_Kernel::get_quota(max_products) returns single-counted 250');
assert_test($kernel_course_quota === 50, 'Carmilla_Kernel::get_quota(max_courses) returns single-counted 50');

delete_option('carmilla_license_claims');

// -------------------------------------------------------------
// Group 8: External Prerequisite Isolation
// -------------------------------------------------------------
echo "\n-- Group 8: External Prerequisite Isolation --\n";

$no_woo_matrix = CapabilityResolver::resolve_matrix([
    'environment' => ['woocommerce_active' => false]
]);

assert_test($no_woo_matrix['features']['commerce.core']['is_active'] === false, 'commerce.core is inactive when WooCommerce missing');
assert_test($no_woo_matrix['features']['commerce.core']['status'] === CapabilityResolver::STATUS_PREREQUISITE_MISSING, 'commerce.core status is PREREQUISITE_MISSING');
assert_test($no_woo_matrix['features']['commerce.cart']['status'] === CapabilityResolver::STATUS_PREREQUISITE_MISSING, 'commerce.cart status is PREREQUISITE_MISSING');
assert_test($no_woo_matrix['features']['theme.base']['is_active'] === true, 'theme.base remains ACTIVE when WooCommerce missing');
assert_test($no_woo_matrix['features']['content.core']['is_active'] === true, 'content.core remains ACTIVE when WooCommerce missing');
assert_test($no_woo_matrix['features']['academy.core']['is_active'] === true, 'academy.core remains ACTIVE when WooCommerce missing');
assert_test($no_woo_matrix['features']['clinic.booking']['is_active'] === true, 'clinic.booking remains ACTIVE when WooCommerce missing');
assert_test($no_woo_matrix['features']['psych.tests']['is_active'] === true, 'psych.tests remains ACTIVE when WooCommerce missing');

// -------------------------------------------------------------
// Group 9: Minimal Public Manifest
// -------------------------------------------------------------
echo "\n-- Group 9: Minimal Public Manifest --\n";

if (class_exists('CB_Manifest_Controller')) {
    $ctrl = new CB_Manifest_Controller();
    $manifest_resp = $ctrl->get_manifest(new WP_REST_Request('GET', '/carmilla/v1/client-manifest'));
    assert_test($manifest_resp instanceof WP_REST_Response, 'get_manifest returns WP_REST_Response');
    assert_test($manifest_resp->status === 200, 'Manifest returns HTTP 200');

    $manifest_data = $manifest_resp->data;
    assert_test(!empty($manifest_data['features']), 'Manifest exposes features map');
    assert_test(!isset($manifest_data['license_key']), 'Manifest strictly omits license_key');
    assert_test(!isset($manifest_data['signature']), 'Manifest strictly omits signature');
    assert_test(!isset($manifest_data['secret']), 'Manifest strictly omits secrets');
    assert_test(!isset($manifest_data['audit_log']), 'Manifest strictly omits audit_log');
    assert_test(is_bool(reset($manifest_data['features'])), 'Feature entries are minimal boolean values');
} else {
    echo "SKIPPED - CB_Manifest_Controller not available\n";
}

echo "\n=================================================================\n";
if ($failed === 0) {
    echo "ALL {$passed} P04-ENTITLEMENT-CODE-040 ACCEPTANCE TESTS PASSED!\n";
    echo "=================================================================\n";
    exit(0);
} else {
    echo "FAILED: {$failed} test(s) failed out of " . ($passed + $failed) . ".\n";
    echo "=================================================================\n";
    exit(1);
}
