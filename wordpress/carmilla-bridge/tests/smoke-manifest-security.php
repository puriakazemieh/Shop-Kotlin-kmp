<?php
define( 'ABSPATH', __DIR__ . '/' );

function __( $text ) { return $text; }
function get_option( $name, $default = false ) {
	return $name === 'cb_manifest_features' ? array( 'content.blog' => true, 'commerce.core' => false ) : $default;
}
function is_wp_error( $value ) { return $value instanceof WP_Error; }
class WP_Error {
	private $code;
	private $data;
	public function __construct( $code, $message, $data = array() ) { $this->code = $code; $this->data = $data; }
	public function get_error_code() { return $this->code; }
	public function get_error_data() { return $this->data; }
}
class FakeRequest {
	private $route;
	public function __construct( $route ) { $this->route = $route; }
	public function get_route() { return $this->route; }
}
require dirname( __DIR__ ) . '/includes/class-cb-manifest-controller.php';

$fail = 0;
function check( $condition, $label ) {
	global $fail;
	echo ( $condition ? 'PASS' : 'FAIL' ) . "  $label\n";
	if ( ! $condition ) { $fail++; }
}

check( CB_Manifest_Controller::feature_for_route( '/carmilla/v1/api/blogs' ) === 'content.blog', 'Blog route maps to content.blog' );
check( CB_Manifest_Controller::feature_for_route( '/carmilla/v1/api/auth/login' ) === null, 'Auth route remains available' );
$allowed = CB_Manifest_Controller::guard_rest_request( null, null, new FakeRequest( '/carmilla/v1/api/blogs' ) );
check( $allowed === null, 'Enabled feature reaches its callback' );
$blocked = CB_Manifest_Controller::guard_rest_request( null, null, new FakeRequest( '/carmilla/v1/api/products' ) );
check( $blocked instanceof WP_Error && $blocked->get_error_code() === 'FEATURE_DISABLED', 'Disabled feature returns FEATURE_DISABLED' );
check( $blocked->get_error_data()['status'] === 403, 'Disabled feature uses HTTP 403' );

echo $fail === 0 ? "ALL PASSED\n" : "$fail FAILED\n";
exit( $fail === 0 ? 0 : 1 );
