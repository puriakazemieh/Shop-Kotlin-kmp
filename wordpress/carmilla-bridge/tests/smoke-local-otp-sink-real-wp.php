<?php
/** Disposable localhost WordPress only. No OTP, user id, or credential is emitted. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
update_option( 'cb_local_qa_otp_sink', true, false );
if ( class_exists( 'CB_Local_QA_OTP_Sink' ) ) { CB_Local_QA_OTP_Sink::boot(); }
$checks = 0; $failures = 0;
$check = static function ( bool $ok, string $label ) use ( &$checks, &$failures ) { ++$checks; if ( ! $ok ) { ++$failures; } echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n"; };
$suffix = substr( hash( 'sha256', microtime( true ) . random_int( 1, PHP_INT_MAX ) ), 0, 12 );
$mobile = 'otpqa_' . $suffix;
$controller = new CB_Auth_Controller();
$send = new WP_REST_Request( 'POST' ); $send->set_param( 'mobile', $mobile );
$sent = $controller->send_login_otp( $send );
$stored = get_transient( 'cb_local_qa_otp_' . md5( $mobile . '_login' ) );
$code = is_array( $stored ) ? ( $stored['code'] ?? '' ) : '';
$check( $sent->get_status() === 200 && is_string( $code ) && preg_match( '/\A[0-9]{6}\z/', $code ), 'local-only sink captures one synthetic OTP without SMS' );
$login = new WP_REST_Request( 'POST' ); $login->set_param( 'mobile', $mobile ); $login->set_param( 'otpCode', $code );
$first = $controller->login_with_otp( $login ); $first_data = $first->get_data();
$check( $first->get_status() === 200 && is_array( $first_data ) && isset( $first_data['accessToken'], $first_data['refreshToken'] ), 'captured code authenticates once through the active route' );
$again = $controller->login_with_otp( $login ); $again_data = $again->get_data();
$check( $again->get_status() === 401 && is_array( $again_data ) && ! isset( $again_data['accessToken'], $again_data['refreshToken'] ), 'same code cannot be reused' );
delete_option( 'cb_local_qa_otp_sink' );
echo "Checks: {$checks}; failures: {$failures}.\n";
exit( $failures ? 1 : 0 );
