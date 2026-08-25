<?php
/**
 * Dependency-free smoke test for Phase 6 admin pure-logic: the line-builders
 * that persist admin-authored content, round-tripped through the read-side
 * parsers (proving content created from the app reads back identically), plus
 * order-status mapping. Run: php tests/smoke-phase6.php
 */

define( 'ABSPATH', __DIR__ . '/' );

require dirname( __DIR__ ) . '/includes/helpers.php';
require dirname( __DIR__ ) . '/includes/class-cb-academy-controller.php';
require dirname( __DIR__ ) . '/includes/class-cb-psychtest-controller.php';

$fail = 0;
function check( $cond, $label ) {
	global $fail;
	echo ( $cond ? 'PASS' : 'FAIL' ) . "  $label\n";
	if ( ! $cond ) { $fail++; }
}

// ---- quiz builder -> parser round trip ----
$questions = array(
	array( 'text' => 'پایتخت؟', 'options' => array(
		array( 'text' => 'تهران', 'correct' => true ),
		array( 'text' => 'شیراز', 'correct' => false ),
	) ),
	array( 'text' => '۲+۲؟', 'options' => array(
		array( 'text' => '۳', 'correct' => false ),
		array( 'text' => '۴', 'correct' => true ),
	) ),
);
$lines  = cb_build_quiz_lines( $questions );
$parsed = CB_Academy_Controller::parse_quiz_lines( $lines, true );
check( count( $parsed ) === 2, 'quiz round-trip: two questions' );
check( $parsed[0]['text'] === 'پایتخت؟', 'quiz round-trip: text preserved' );
check( $parsed[0]['options'][0]['text'] === 'تهران', 'quiz round-trip: option text (marker stripped)' );
check( $parsed[0]['_correct'] === 0, 'quiz round-trip: correct index q0 = 0' );
check( $parsed[1]['_correct'] === 1, 'quiz round-trip: correct index q1 = 1' );

// ---- psych-test builder -> parser round trip ----
$tq = array(
	array( 'text' => 'اضطراب؟', 'options' => array(
		array( 'text' => 'کم', 'score' => 0 ),
		array( 'text' => 'زیاد', 'score' => 2 ),
	) ),
);
$tlines  = cb_build_test_question_lines( $tq );
$tparsed = CB_Psychtest_Controller::parse_questions( $tlines );
check( $tparsed[0]['options'][1]['text'] === 'زیاد' && $tparsed[0]['options'][1]['score'] === 2, 'test round-trip: label + score' );
check( $tparsed[0]['options'][0]['score'] === 0, 'test round-trip: zero score' );

$rlines  = cb_build_range_lines( array(
	array( 'minScore' => 0, 'maxScore' => 3, 'interpretation' => 'کم' ),
	array( 'minScore' => 4, 'maxScore' => 10, 'interpretation' => 'زیاد' ),
) );
$rparsed = CB_Psychtest_Controller::parse_ranges( $rlines );
check( count( $rparsed ) === 2, 'range round-trip: two ranges' );
check( CB_Psychtest_Controller::interpret( $rparsed, 5 ) === 'زیاد', 'range round-trip: score 5 -> زیاد' );

// ---- order status mapping (app -> WooCommerce) ----
check( cb_app_status_to_wc( 'COMPLETED' ) === 'completed', 'COMPLETED -> completed' );
check( cb_app_status_to_wc( 'CANCELLED' ) === 'cancelled', 'CANCELLED -> cancelled' );
check( cb_app_status_to_wc( 'SHIPPED' ) === 'processing', 'SHIPPED -> processing' );
check( cb_app_status_to_wc( 'PROCESSING' ) === 'processing', 'PROCESSING -> processing' );
check( cb_app_status_to_wc( 'PLACED' ) === 'on-hold', 'PLACED -> on-hold' );
check( cb_app_status_to_wc( 'AWAITING_PAYMENT' ) === 'pending', 'AWAITING_PAYMENT -> pending' );

echo "\n" . ( $fail === 0 ? 'ALL PASSED' : "$fail FAILED" ) . "\n";
exit( $fail === 0 ? 0 : 1 );
