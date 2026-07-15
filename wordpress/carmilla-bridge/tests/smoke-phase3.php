<?php
/**
 * Dependency-free smoke test for Phase 3 academy pure-logic (quiz parsing,
 * placement leveling, lesson-id codec, certificate numbering).
 * Run: php tests/smoke-phase3.php
 */

define( 'ABSPATH', __DIR__ . '/' );
function wp_salt( $s = 'auth' ) { return 'test-salt-value'; }

require dirname( __DIR__ ) . '/includes/class-cb-academy-controller.php';

$fail = 0;
function check( $cond, $label ) {
	global $fail;
	echo ( $cond ? 'PASS' : 'FAIL' ) . "  $label\n";
	if ( ! $cond ) { $fail++; }
}

// ---- quiz parsing ----
$raw = "پایتخت ایران؟ | تهران* | شیراز | مشهد\n۲+۲؟ | ۳ | ۴✓ | ۵";
$hidden = CB_Academy_Controller::parse_quiz_lines( $raw, false );
check( count( $hidden ) === 2, 'two questions parsed' );
check( $hidden[0]['text'] === 'پایتخت ایران؟', 'question text kept' );
check( $hidden[0]['options'][0]['text'] === 'تهران', 'marker * stripped from option text' );
check( $hidden[0]['options'][0]['correct'] === null, 'correct hidden when reveal=false' );
check( $hidden[0]['_correct'] === 0, 'correct index computed (option 0)' );
check( $hidden[1]['_correct'] === 1, 'second question correct index (option 1 via ✓)' );

$shown = CB_Academy_Controller::parse_quiz_lines( $raw, true );
check( $shown[0]['options'][0]['correct'] === true, 'correct revealed when reveal=true' );
check( $shown[0]['options'][1]['correct'] === false, 'wrong option flagged false' );

// scoring emulation
$answers = array( 0 => 0, 1 => 1 ); // both correct
$correct = 0;
foreach ( $shown as $q ) {
	if ( (int) ( $answers[ $q['index'] ] ?? -1 ) === (int) $q['_correct'] ) { $correct++; }
}
check( (int) round( $correct / count( $shown ) * 100 ) === 100, 'all-correct -> 100%' );

// ---- placement leveling ----
check( CB_Academy_Controller::placement_level( 0 )['level'] === 'beginner', 'total 0 -> beginner' );
check( CB_Academy_Controller::placement_level( 2 )['level'] === 'beginner', 'total 2 -> beginner' );
check( CB_Academy_Controller::placement_level( 3 )['level'] === 'intermediate', 'total 3 -> intermediate' );
check( CB_Academy_Controller::placement_level( 4 )['level'] === 'intermediate', 'total 4 -> intermediate' );
check( CB_Academy_Controller::placement_level( 5 )['level'] === 'advanced', 'total 5 -> advanced' );
check( CB_Academy_Controller::placement_level( 6 )['label'] === 'پیشرفته', 'total 6 -> پیشرفته label' );

// ---- lesson-id codec ----
$lid = CB_Academy_Controller::lesson_id( 42, 0 );
check( $lid === 4200001, 'lesson_id(42,0) = 4200001' );
list( $cid, $idx ) = CB_Academy_Controller::decode_lesson( $lid );
check( $cid === 42 && $idx === 0, 'decode round-trips course 42 index 0' );
list( $cid2, $idx2 ) = CB_Academy_Controller::decode_lesson( CB_Academy_Controller::lesson_id( 7, 5 ) );
check( $cid2 === 7 && $idx2 === 5, 'decode round-trips course 7 index 5' );

// ---- certificate numbering (deterministic + unique) ----
$n1 = CB_Academy_Controller::cert_number( 10, 3 );
$n2 = CB_Academy_Controller::cert_number( 10, 3 );
$n3 = CB_Academy_Controller::cert_number( 10, 4 );
check( $n1 === $n2, 'cert number deterministic for same course+user' );
check( $n1 !== $n3, 'cert number differs per user' );
check( strpos( $n1, 'CB-' ) === 0 && strlen( $n1 ) === 13, 'cert number format CB-XXXXXXXXXX' );

echo "\n" . ( $fail === 0 ? 'ALL PASSED' : "$fail FAILED" ) . "\n";
exit( $fail === 0 ? 0 : 1 );
