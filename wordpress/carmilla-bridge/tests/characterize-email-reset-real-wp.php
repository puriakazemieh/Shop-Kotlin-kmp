<?php
/** Disposable real-WP verification for the email reset bridge. */
require __DIR__ . '/experiment-identity-shared-references.php';
wp_installing( false );
$_SERVER['REMOTE_ADDR'] = '192.0.2.1'; // Documentation-only synthetic address for WP mail template.
require_once '/work/includes/helpers.php';
require_once '/work/includes/class-cb-jwt.php';
require_once '/work/includes/class-cb-auth-controller.php';

try {
	global $wp_version;
	$user_id = shared_user( 'reset_qa', 'reset@example.test' ); $user = get_user_by( 'id', $user_id );
	$keys = array(); $mail_key = null;
	add_action( 'retrieve_password_key', static function ( $login, $key ) use ( &$keys ) { $keys[] = $key; }, 10, 2 );
	add_filter( 'retrieve_password_message', static function ( $message, $key ) use ( &$mail_key ) { $mail_key = $key; return $message; }, 10, 2 );
	$controller = new CB_Auth_Controller(); $request = new WP_REST_Request( 'POST' ); $request->set_param( 'email', 'reset@example.test' );
	$response = $controller->forgot_password( $request );
	shared_check( $response->get_status() === 200 && $response->get_data() === array( 'sent' => true ), 'forgot-password returns generic sent response' );
	shared_check( count( $keys ) === 1 && is_string( $mail_key ) && hash_equals( $keys[0], $mail_key ), 'one WordPress key is generated and selected for mail' );
	$mapping = 'cb_reset_' . hash( 'sha256', $mail_key );
	shared_check( get_transient( $mapping ) === $user->user_login, 'hashed mapping points to the login for the emailed key only' );
	shared_check( check_password_reset_key( $mail_key, $user->user_login ) instanceof WP_User, 'WP core accepts the emailed key before reset' );
	$reset = new WP_REST_Request( 'POST' ); $reset->set_param( 'token', $mail_key ); $reset->set_param( 'newPassword', 'synthetic-new-password' );
	$response = $controller->reset_password( $reset );
	$updated = get_user_by( 'id', $user_id );
	$data = $response->get_data();
	shared_check( $response->get_status() === 200 && $data === array( 'reset' => true ) && $updated instanceof WP_User && wp_check_password( 'synthetic-new-password', $updated->user_pass, $user_id ), 'reset endpoint accepts the emailed key and returns no credentials' );
	$reused = $controller->reset_password( $reset );
	shared_check( $reused->get_status() === 400 && ( $reused->get_data()['errorCode'] ?? '' ) === 'INVALID_TOKEN' && get_transient( $mapping ) === false, 'reset mapping is consumed atomically and reuse is rejected' );
	$missing = new WP_REST_Request( 'POST' ); $missing->set_param( 'email', 'absent@example.test' ); $before = count( $keys );
	$response = $controller->forgot_password( $missing );
	shared_check( $response->get_status() === 200 && $response->get_data() === array( 'sent' => true ) && count( $keys ) === $before, 'unknown email receives generic response without key generation' );
	echo 'PASS email reset lifecycle verification WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n";
} catch ( Throwable $e ) { fwrite( STDERR, 'FAIL reset characterization: ' . get_class( $e ) . "\n" ); exit( 1 ); }
