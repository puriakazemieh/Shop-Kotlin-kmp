<?php
/**
 * Dependency-free smoke test for Phase 4 pure-logic (slot-id codec, therapist
 * match scoring, psych-test question/range parsing, scoring, interpretation).
 * Run: php tests/smoke-phase4.php
 */

define( 'ABSPATH', __DIR__ . '/' );

require dirname( __DIR__ ) . '/includes/class-cb-clinic-controller.php';
require dirname( __DIR__ ) . '/includes/class-cb-psychtest-controller.php';

$fail = 0;
function check( $cond, $label ) {
	global $fail;
	echo ( $cond ? 'PASS' : 'FAIL' ) . "  $label\n";
	if ( ! $cond ) { $fail++; }
}

// ---- clinic slot-id codec ----
$sid = CB_Clinic_Controller::slot_id( 12, 3 );
check( $sid === 1200003, 'slot_id(12,3) = 1200003' );
list( $tid, $idx ) = CB_Clinic_Controller::decode_slot( $sid );
check( $tid === 12 && $idx === 3, 'decode_slot round-trips therapist 12 index 3' );
list( $t2, $i2 ) = CB_Clinic_Controller::decode_slot( CB_Clinic_Controller::slot_id( 500, 0 ) );
check( $t2 === 500 && $i2 === 0, 'decode_slot round-trips therapist 500 index 0' );

// ---- therapist match scoring ----
$hay = 'اضطراب و افسردگی — رویکرد شناختی رفتاری';
check( CB_Clinic_Controller::match_score( $hay, array( 'اضطراب' ) ) === 1, 'one matching tag -> 1' );
check( CB_Clinic_Controller::match_score( $hay, array( 'اضطراب', 'افسردگی' ) ) === 2, 'two matching tags -> 2' );
check( CB_Clinic_Controller::match_score( $hay, array( 'زوج' ) ) === 0, 'non-matching tag -> 0' );
check( CB_Clinic_Controller::match_score( $hay, array( 'اضطراب', 'کودک' ) ) === 1, 'partial overlap -> 1' );

// ---- psych-test question parsing ----
$raw = "چقدر مضطرب هستید؟ | اصلا=0 , کمی=1 , زیاد=2\nخوابتان چطور است؟ | خوب=0 , بد=2";
$qs = CB_Psychtest_Controller::parse_questions( $raw );
check( count( $qs ) === 2, 'two questions parsed' );
check( $qs[0]['options'][2]['text'] === 'زیاد' && $qs[0]['options'][2]['score'] === 2, 'option label + score parsed' );
check( $qs[0]['options'][0]['score'] === 0, 'zero-score option' );

// ---- range parsing + interpretation ----
$ranges = CB_Psychtest_Controller::parse_ranges( "0 | 1 | کم\n2 | 3 | متوسط\n4 | 10 | زیاد" );
check( count( $ranges ) === 3, 'three ranges parsed' );
check( CB_Psychtest_Controller::interpret( $ranges, 0 ) === 'کم', 'score 0 -> کم' );
check( CB_Psychtest_Controller::interpret( $ranges, 3 ) === 'متوسط', 'score 3 -> متوسط' );
check( CB_Psychtest_Controller::interpret( $ranges, 9 ) === 'زیاد', 'score 9 -> زیاد' );
check( CB_Psychtest_Controller::interpret( $ranges, 99 ) === null, 'out-of-range -> null' );

// ---- scoring (server-side) ----
$answers = array( 0 => 2, 1 => 1 ); // زیاد(2) + بد(2) = 4
check( CB_Psychtest_Controller::score_answers( $qs, $answers ) === 4, 'answers 2+2 -> score 4' );
$answers2 = array( 0 => 0, 1 => 0 ); // اصلا(0) + خوب(0) = 0
check( CB_Psychtest_Controller::score_answers( $qs, $answers2 ) === 0, 'answers 0+0 -> score 0' );
check( CB_Psychtest_Controller::score_answers( $qs, array( 0 => 1 ) ) === 1, 'partial answers scored (کمی=1)' );

echo "\n" . ( $fail === 0 ? 'ALL PASSED' : "$fail FAILED" ) . "\n";
exit( $fail === 0 ? 0 : 1 );
