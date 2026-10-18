<?php
/** Characterization only; the shared fixture enforces disposable synthetic WP. */
require __DIR__ . '/experiment-identity-shared-references.php';
$_SERVER['REMOTE_ADDR'] = '192.0.2.1'; // Documentation-only synthetic address for WP mail template.
require_once '/bridge/includes/helpers.php';
require_once '/bridge/includes/class-cb-jwt.php';
require_once '/bridge/includes/class-cb-auth-controller.php';

try {
	global $wp_version;
	$user_id = shared_user( 'reset_qa', 'reset@example.test' ); $user = get_user_by( 'id', $user_id );
	$keys = array(); $mail_key = null;
	add_action( 'retrieve_password_key', static function ( $login, $key ) use ( &$keys ) { $keys[] = $key; }, 10, 2 );
	add_filter( 'retrieve_password_message', static function ( $message, $key ) use ( &$mail_key ) { $mail_key = $key; return $message; }, 10, 2 );
	$controller = new CB_Auth_Controller(); $request = new WP_REST_Request( 'POST' ); $request->set_param( 'email', 'reset@example.test' );
	$response = $controller->forgot_password( $request );
	shared_check( $response->get_status() === 200 && $response->get_data() === array( 'sent' => true ), 'forgot-password returns generic sent response' );
	shared_check( count( $keys ) === 2 && is_string( $mail_key ) && $keys[0] !== $keys[1] && hash_equals( $keys[1], $mail_key ), 'two distinct reset keys generated and WP mail selects second' );
	shared_check( (int) get_transient( 'cb_reset_' . $keys[0] ) === $user_id && get_transient( 'cb_reset_' . $keys[1] ) === false, 'legacy transient points to first key, not the emailed key' );
	shared_check( is_wp_error( check_password_reset_key( $keys[0], $user->user_login ) ) && check_password_reset_key( $keys[1], $user->user_login ) instanceof WP_User, 'WP core rejects first key and accepts second before reset' );
	$reset = new WP_REST_Request( 'POST' ); $reset->set_param( 'token', $keys[1] ); $reset->set_param( 'newPassword', 'synthetic-new-password' );
	$response = $controller->reset_password( $reset );
	shared_check( $response->get_status() === 400 && ( $response->get_data()['errorCode'] ?? '' ) === 'INVALID_TOKEN', 'legacy reset endpoint rejects key selected for WP mail' );
	$reset->set_param( 'token', $keys[0] ); $response = $controller->reset_password( $reset );
	$updated = get_user_by( 'id', $user_id );
	shared_check( $response->get_status() === 200 && $updated instanceof WP_User && wp_check_password( 'synthetic-new-password', $updated->user_pass, $user_id ), 'legacy endpoint accepts transient first key even though WP core invalidated it' );
	shared_check( get_transient( 'cb_reset_' . $keys[0] ) === false, 'legacy successful reset deletes its synthetic transient' );
	$missing = new WP_REST_Request( 'POST' ); $missing->set_param( 'email', 'absent@example.test' ); $before = count( $keys );
	$response = $controller->forgot_password( $missing );
	shared_check( $response->get_status() === 200 && $response->get_data() === array( 'sent' => true ) && count( $keys ) === $before, 'unknown email receives generic response without key generation' );
	echo 'PASS email reset lifecycle characterization WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n";
} catch ( Throwable $e ) { fwrite( STDERR, 'FAIL reset characterization: ' . get_class( $e ) . "\n" ); exit( 1 ); }
