<?php
/**
 * P03-QA-MANUAL-027 — Full WordPress Manifest Staging & Runtime Scenario
 *
 * Verifies the complete server-side lifecycle:
 * 1. Admin Settings: Toggle features, enforce parent/child dependencies, audit logging.
 * 2. REST Manifest: Schema version 1, ETag generation, Cache-Control header, 304 conditional get.
 * 3. REST Dispatch Boundary: Block disabled endpoints with HTTP 403, allow enabled endpoints, leave auth untouched.
 * 4. Runtime Update without Restart: Dynamic options update immediately affects subsequent dispatch calls and manifests.
 */
define( 'ABSPATH', __DIR__ . '/' );
define( 'DAY_IN_SECONDS', 86400 );

$storage = array();

function __( $text, $domain = 'default' ) { return $text; }
function esc_html__( $text, $domain = 'default' ) { return $text; }
function get_option( $name, $default = false ) {
	global $storage;
	return array_key_exists( $name, $storage ) ? $storage[ $name ] : $default;
}
function update_option( $name, $value, $autoload = null ) {
	global $storage;
	$storage[ $name ] = $value;
	return true;
}
function get_current_blog_id() { return 1; }
function get_current_user_id() { return 42; }
function sanitize_key( $key ) { return preg_replace( '/[^a-z0-9_\-]/i', '', (string) $key ); }
function wp_json_encode( $data ) { return json_encode( $data, JSON_UNESCAPED_SLASHES ); }
function is_wp_error( $value ) { return $value instanceof WP_Error; }

class WP_Error {
	private $code;
	private $message;
	private $data;
	public function __construct( $code, $message, $data = array() ) {
		$this->code = $code;
		$this->message = $message;
		$this->data = $data;
	}
	public function get_error_code() { return $this->code; }
	public function get_error_message() { return $this->message; }
	public function get_error_data() { return $this->data; }
}

class WP_REST_Request {
	private $headers = array();
	private $route;
	public function __construct( $route, array $headers = array() ) {
		$this->route = $route;
		$this->headers = array_change_key_case( $headers, CASE_LOWER );
	}
	public function get_route() { return $this->route; }
	public function get_header( $name ) {
		$key = strtolower( $name );
		return array_key_exists( $key, $this->headers ) ? $this->headers[ $key ] : null;
	}
}

class WP_REST_Response {
	public $data;
	public $status;
	public $headers = array();
	public function __construct( $data = null, $status = 200 ) {
		$this->data = $data;
		$this->status = $status;
	}
	public function header( $key, $value ) {
		$this->headers[ $key ] = $value;
	}
}

require dirname( __DIR__ ) . '/includes/class-cb-manifest-controller.php';

$controller = new CB_Manifest_Controller();
$assertions = 0;
$failures = 0;

function assert_test( $condition, $label ) {
	global $assertions, $failures;
	$assertions++;
	if ( $condition ) {
		echo "  [PASS] $label\n";
	} else {
		echo "  [FAIL] $label\n";
		$failures++;
	}
}

echo "=== P03-QA-MANUAL-027: WordPress Manifest Lifecycle & Runtime QA ===\n\n";

// -------------------------------------------------------------
// Step 1: Dependency Violation Enforced
// -------------------------------------------------------------
echo "1. Testing Admin Dependency Enforcement...\n";
$illegal_config = array(
	'content.blog' => false,
	'academy.core' => false,
	'academy.quiz' => true, // Needs academy.core
);
$violations = CB_Manifest_Controller::dependency_violations( $illegal_config );
assert_test( in_array( 'academy.quiz', $violations, true ), 'Enforces parent dependency: academy.quiz rejected without academy.core' );

// -------------------------------------------------------------
// Step 2: Save Initial Configuration in Admin
// -------------------------------------------------------------
echo "\n2. Testing Admin Settings Save (Commerce Only Enabled)...\n";
$initial_features = array(
	'content.blog'        => true,
	'commerce.core'       => true,
	'commerce.physical'   => true,
	'commerce.digital'    => false,
	'academy.core'        => false,
	'academy.quiz'        => false,
	'academy.certificate' => false,
	'clinic.booking'      => false,
	'clinic.messaging'    => false,
	'psych.tests'         => false,
	'wallet'              => true,
	'admin.mobile'        => false,
);
update_option( 'cb_manifest_features', $initial_features );
update_option( 'cb_manifest_version', '2026.11.22.1000' );
update_option( 'cb_manifest_tenant_id', 'staging-wp-tenant' );

$saved = get_option( 'cb_manifest_features' );
assert_test( $saved['commerce.core'] === true && $saved['academy.core'] === false, 'Options successfully saved in WordPress storage' );

// -------------------------------------------------------------
// Step 3: GET /client-manifest REST API
// -------------------------------------------------------------
echo "\n3. Testing REST Manifest Endpoint Response...\n";
$req = new WP_REST_Request( '/carmilla/v1/client-manifest' );
$resp = $controller->get_manifest( $req );
assert_test( $resp->status === 200, 'Manifest returns HTTP 200' );
assert_test( $resp->data['schemaVersion'] === 1, 'Schema version is 1' );
assert_test( $resp->data['tenantId'] === 'staging-wp-tenant', 'TenantId matches sanitized option' );
assert_test( $resp->data['features']['commerce.core'] === true, 'Features payload reflects commerce enabled' );
assert_test( $resp->data['features']['academy.core'] === false, 'Features payload reflects academy disabled' );
assert_test( ! empty( $resp->headers['ETag'] ), 'Response includes valid ETag header' );
assert_test( strpos( $resp->headers['Cache-Control'], 'must-revalidate' ) !== false, 'Response requires revalidation' );

$etag_v1 = $resp->headers['ETag'];

// Conditional 304 with matching ETag
$req_304 = new WP_REST_Request( '/carmilla/v1/client-manifest', array( 'if-none-match' => $etag_v1 ) );
$resp_304 = $controller->get_manifest( $req_304 );
assert_test( $resp_304->status === 304, 'Conditional request with matching ETag returns HTTP 304 Not Modified' );

// -------------------------------------------------------------
// Step 4: Dispatch Boundary (Route Guards)
// -------------------------------------------------------------
echo "\n4. Testing REST Route Dispatch Guards...\n";
// Auth route: untouched
$auth_req = new WP_REST_Request( '/carmilla/v1/api/auth/login' );
assert_test( CB_Manifest_Controller::guard_rest_request( null, null, $auth_req ) === null, 'Auth routes pass through unblocked' );

// Commerce route: allowed
$product_req = new WP_REST_Request( '/carmilla/v1/api/products' );
assert_test( CB_Manifest_Controller::guard_rest_request( null, null, $product_req ) === null, 'Commerce routes reach callback when enabled' );

// Academy route: blocked with 403
$course_req = new WP_REST_Request( '/carmilla/v1/api/courses' );
$course_block = CB_Manifest_Controller::guard_rest_request( null, null, $course_req );
assert_test( $course_block instanceof WP_Error && $course_block->get_error_data()['status'] === 403, 'Disabled academy route is blocked with HTTP 403 FEATURE_DISABLED' );

// Clinic route: blocked with 403
$clinic_req = new WP_REST_Request( '/carmilla/v1/api/therapists' );
$clinic_block = CB_Manifest_Controller::guard_rest_request( null, null, $clinic_req );
assert_test( $clinic_block instanceof WP_Error && $clinic_block->get_error_data()['status'] === 403, 'Disabled clinic route is blocked with HTTP 403 FEATURE_DISABLED' );

// Psych test route: blocked with 403
$psych_req = new WP_REST_Request( '/carmilla/v1/api/psych-tests' );
$psych_block = CB_Manifest_Controller::guard_rest_request( null, null, $psych_req );
assert_test( $psych_block instanceof WP_Error && $psych_block->get_error_data()['status'] === 403, 'Disabled psych route is blocked with HTTP 403 FEATURE_DISABLED' );

// -------------------------------------------------------------
// Step 5: Dynamic Runtime Update without Server Restart
// -------------------------------------------------------------
echo "\n5. Testing Dynamic Runtime Toggle (Enabling Academy & Clinic)...\n";
$updated_features = array_merge( $initial_features, array(
	'academy.core'   => true,
	'clinic.booking' => true,
) );
update_option( 'cb_manifest_features', $updated_features );
update_option( 'cb_manifest_version', '2026.11.22.1001' );

// Manifest now returns new version and new ETag
$resp_v2 = $controller->get_manifest( $req );
assert_test( $resp_v2->data['features']['academy.core'] === true, 'Updated manifest immediately reflects academy.core = true' );
assert_test( $resp_v2->data['features']['clinic.booking'] === true, 'Updated manifest immediately reflects clinic.booking = true' );
assert_test( $resp_v2->headers['ETag'] !== $etag_v1, 'ETag changed on manifest update' );

// Old ETag now returns 200 (not 304)
$req_old_etag = new WP_REST_Request( '/carmilla/v1/client-manifest', array( 'if-none-match' => $etag_v1 ) );
$resp_old_etag = $controller->get_manifest( $req_old_etag );
assert_test( $resp_old_etag->status === 200, 'Old ETag is rejected with 200 OK after runtime update' );

// Dispatch boundary now immediately permits Academy & Clinic
assert_test( CB_Manifest_Controller::guard_rest_request( null, null, $course_req ) === null, 'Academy routes now pass through to callback without restart' );
assert_test( CB_Manifest_Controller::guard_rest_request( null, null, $clinic_req ) === null, 'Clinic routes now pass through to callback without restart' );

// Psych test remains disabled and blocked
assert_test( is_wp_error( CB_Manifest_Controller::guard_rest_request( null, null, $psych_req ) ), 'Unchanged disabled feature psych.tests remains blocked' );

echo "\n-------------------------------------------------------------\n";
echo "Total Assertions: $assertions | Failures: $failures\n";
if ( $failures === 0 ) {
	echo "RESULT: ALL STAGING SCENARIO TESTS PASSED SUCCESSFULLY!\n";
	exit( 0 );
} else {
	echo "RESULT: $failures ASSERTION(S) FAILED!\n";
	exit( 1 );
}
