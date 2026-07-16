<?php
/**
 * Smoke test for Phase 7 shared logic: cb_match_questions() falls back to a
 * default set and honours an admin-configured option. Run: php tests/smoke-phase7.php
 */

define( 'ABSPATH', __DIR__ . '/' );

$GLOBALS['__opts'] = array();
function get_option( $k, $d = false ) {
	return array_key_exists( $k, $GLOBALS['__opts'] ) ? $GLOBALS['__opts'][ $k ] : $d;
}
function apply_filters( $t, $v ) { return $v; }

require dirname( __DIR__ ) . '/includes/helpers.php';

$fail = 0;
function check( $cond, $label ) {
	global $fail;
	echo ( $cond ? 'PASS' : 'FAIL' ) . "  $label\n";
	if ( ! $cond ) { $fail++; }
}

// Default set when no option configured.
$def = cb_match_questions();
check( count( $def ) === 4, 'default match questions = 4' );
check( $def[0]['tag'] === 'اضطراب', 'first default tag' );
check( isset( $def[0]['id'], $def[0]['questionText'], $def[0]['tag'] ), 'default shape has id/questionText/tag' );

// Admin-configured option wins.
$GLOBALS['__opts']['cb_match_questions'] = array(
	array( 'id' => 10, 'questionText' => 'سفارشی؟', 'tag' => 'خشم' ),
);
$custom = cb_match_questions();
check( count( $custom ) === 1 && $custom[0]['tag'] === 'خشم', 'configured questions override defaults' );

// Empty option falls back to defaults.
$GLOBALS['__opts']['cb_match_questions'] = array();
check( count( cb_match_questions() ) === 4, 'empty option falls back to defaults' );

echo "\n" . ( $fail === 0 ? 'ALL PASSED' : "$fail FAILED" ) . "\n";
exit( $fail === 0 ? 0 : 1 );
