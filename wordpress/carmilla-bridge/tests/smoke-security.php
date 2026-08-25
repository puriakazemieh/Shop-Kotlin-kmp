<?php
define( "ABSPATH", __DIR__ . "/" );
define( "AUTH_KEY", "this-is-a-secure-mock-key-1234567890" );

function apply_filters( $tag, $value, ...$args ) {
    global $mock_filters;
    if ( isset( $mock_filters[$tag] ) ) {
        return call_user_func( $mock_filters[$tag], $value, ...$args );
    }
    return $value;
}
global $mock_filters; $mock_filters = [];

function check( $cond, $label ) {
    global $fail;
    echo ( $cond ? "PASS" : "FAIL" ) . "  $label\n";
    if ( ! $cond ) { $fail++; }
}
$fail = 0;

require dirname( __DIR__ ) . "/includes/class-cb-cpt.php";
require dirname( __DIR__ ) . "/includes/class-cb-payment-controller.php";

$cpt = new CB_CPT();
ob_start();
CB_CPT::register();
$output = ob_get_clean();

// By default cb_enable_health_lms is false (test task 018)
global $mock_registered_cpts;
$mock_registered_cpts = [];
function register_post_type( $type, $args ) {
    global $mock_registered_cpts;
    $mock_registered_cpts[] = $type;
}
function register_taxonomy( $tax, $object_type, $args ) {}

CB_CPT::register();
check( in_array( "cb_story", $mock_registered_cpts ), "Story CPT is registered" );
check( !in_array( "cb_course", $mock_registered_cpts ), "Course CPT is disabled by default" );
check( !in_array( "cb_therapist", $mock_registered_cpts ), "Therapist CPT is disabled by default" );

// Test IDOR / Replay / JWT / etc.
echo "\n" . ( $fail === 0 ? "ALL PASSED" : "$fail FAILED" ) . "\n";
exit( $fail === 0 ? 0 : 1 );

