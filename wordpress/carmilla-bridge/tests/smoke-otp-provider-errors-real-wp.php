<?php
/** Disposable installed WordPress only; synthetic numbers and provider failures. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';

$failed = 0;
$check = static function ( bool $ok, string $label ) use ( &$failed ): void {
	if ( ! $ok ) { ++$failed; }
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n";
};
$send = static function ( string $mobile ): WP_REST_Response {
	$request = new WP_REST_Request( 'POST' );
	$request->set_param( 'mobile', $mobile );
	return ( new CB_Auth_Controller() )->send_login_otp( $request );
};
$assert_safe = static function ( WP_REST_Response $response, string $label ) use ( $check ): void {
	$data = $response->get_data();
	$message = is_array( $data ) ? (string) ( $data['message'] ?? '' ) : '';
	$check( $response->get_status() === 503 && ( $data['errorCode'] ?? '' ) === 'OTP_UNAVAILABLE', "$label returns stable unavailable error" );
	$check( $message === 'ارسال کد تأیید موقتاً در دسترس نیست.' && strpos( $message, 'secret-provider-marker' ) === false, "$label hides provider details" );
};
$clear = static function ( string $mobile ): void {
	$key = md5( $mobile . '_login' );
	delete_transient( 'cb_otp_' . $key );
	delete_transient( 'cb_otp_cooldown_' . $key );
	delete_transient( 'cb_otp_attempts_' . $key );
};
$without_cooldown = static function ( string $mobile ) use ( $check ): void {
	$key = md5( $mobile . '_login' );
	$check( get_transient( 'cb_otp_' . $key ) === false && get_transient( 'cb_otp_cooldown_' . $key ) === false, 'failed dispatch leaves no OTP or cooldown' );
};

$override_mobile = '09120000061';
$clear( $override_mobile );
$failure = static function () { return new WP_Error( 'synthetic_failure', 'secret-provider-marker https://private.example.test' ); };
add_filter( 'cb_send_otp_override', $failure );
$assert_safe( $send( $override_mobile ), 'override failure' );
$without_cooldown( $override_mobile );
remove_filter( 'cb_send_otp_override', $failure );
$success = static function () { return true; };
add_filter( 'cb_send_otp_override', $success );
$retry = $send( $override_mobile );
$check( $retry->get_status() === 200, 'override failure can retry immediately' );
remove_filter( 'cb_send_otp_override', $success );
$clear( $override_mobile );

$adapter_mobile = '09120000062';
$clear( $adapter_mobile );
$empty_settings = static function () { return array(); };
add_filter( 'pre_option_carmilla_message_settings', $empty_settings );
$assert_safe( $send( $adapter_mobile ), 'adapter failure' );
$without_cooldown( $adapter_mobile );
remove_filter( 'pre_option_carmilla_message_settings', $empty_settings );
$clear( $adapter_mobile );

echo "Failures: {$failed}.\n";
exit( $failed ? 1 : 0 );
