<?php
/**
 * Dependency-free smoke test for Phase 5 pure-logic (membership active window,
 * deterministic referral code). Run: php tests/smoke-phase5.php
 */

define( 'ABSPATH', __DIR__ . '/' );

require dirname( __DIR__ ) . '/includes/class-cb-extras-controller.php';

$fail = 0;
function check( $cond, $label ) {
	global $fail;
	echo ( $cond ? 'PASS' : 'FAIL' ) . "  $label\n";
	if ( ! $cond ) { $fail++; }
}

// ---- membership active window ----
$future = gmdate( 'c', time() + 86400 );
$past   = gmdate( 'c', time() - 86400 );
check( CB_Extras_Controller::membership_active( $future ) === true, 'future expiry -> active' );
check( CB_Extras_Controller::membership_active( $past ) === false, 'past expiry -> inactive' );
check( CB_Extras_Controller::membership_active( '' ) === false, 'empty expiry -> inactive' );
check( CB_Extras_Controller::membership_active( null ) === false, 'null expiry -> inactive' );

// ---- referral code (deterministic + formatted) ----
$c1 = CB_Extras_Controller::referral_code( 42, 'salt' );
$c2 = CB_Extras_Controller::referral_code( 42, 'salt' );
$c3 = CB_Extras_Controller::referral_code( 43, 'salt' );
check( $c1 === $c2, 'referral code deterministic for same user+salt' );
check( $c1 !== $c3, 'referral code differs per user' );
check( strpos( $c1, 'REF' ) === 0 && strlen( $c1 ) === 9, 'referral code format REFxxxxxx' );
check( CB_Extras_Controller::referral_code( 42, 'salt' ) !== CB_Extras_Controller::referral_code( 42, 'other' ), 'salt changes the code' );

echo "\n" . ( $fail === 0 ? 'ALL PASSED' : "$fail FAILED" ) . "\n";
exit( $fail === 0 ? 0 : 1 );
