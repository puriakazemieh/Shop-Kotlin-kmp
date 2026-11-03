<?php
/** Disposable installed WordPress only. Requires temporary qa-integration-mail-mu.php and marker. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || ! file_exists( '/tmp/carmilla-qa-integration-enabled' ) ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
$suffix = bin2hex( random_bytes( 4 ) );
$user = wp_create_user( 'qa_integration_http_' . $suffix, wp_generate_password( 24 ), 'qa-integration-http-' . $suffix . '@example.test' );
if ( ! is_int( $user ) ) { exit( 2 ); }
( new WP_User( $user ) )->set_role( 'administrator' );
$token = WP_Session_Tokens::get_instance( $user )->create( time() + 3600 );
$expires = time() + 3600;
$auth = wp_generate_auth_cookie( $user, $expires, 'auth', $token );
$logged = wp_generate_auth_cookie( $user, $expires, 'logged_in', $token );
$_COOKIE[ LOGGED_IN_COOKIE ] = $logged;
wp_set_current_user( $user );
$nonce = wp_create_nonce( 'carmilla_test_message' );
$cookie = AUTH_COOKIE . '=' . $auth . '; ' . LOGGED_IN_COOKIE . '=' . $logged;
$url = 'http://127.0.0.1/wp-admin/admin-post.php';
$failed = 0;
$check = static function ( bool $ok, string $name ) use ( &$failed ): void { if ( ! $ok ) { ++$failed; } echo ( $ok ? 'PASS ' : 'FAIL ' ) . $name . "\n"; };
$post = static function ( array $body ) use ( $url, $cookie ) {
	return wp_remote_post( $url, array( 'body' => $body, 'headers' => array( 'Cookie' => $cookie ), 'redirection' => 0, 'timeout' => 15 ) );
};
try {
	$base = array( 'action' => 'carmilla_test_message', 'carmilla_test_message_nonce' => $nonce, 'test_type' => 'email', 'test_recipient' => 'qa@example.test', 'test_body' => "Hello'world" );
	$success = $post( $base );
	$check( ! is_wp_error( $success ) && wp_remote_retrieve_response_code( $success ) === 302 && wp_remote_retrieve_header( $success, 'x-carmilla-qa-mail' ) === 'intercepted' && wp_remote_retrieve_header( $success, 'x-carmilla-qa-body' ) === 'ok' && strpos( wp_remote_retrieve_header( $success, 'location' ), 'admin.php?page=carmilla-integrations' ) !== false, 'HTTP admin email intercepted and redirected safely' );
	$invalid = $base; $invalid['test_type'] = 'fax';
	$denied = $post( $invalid );
	$check( ! is_wp_error( $denied ) && wp_remote_retrieve_response_code( $denied ) !== 302 && wp_remote_retrieve_header( $denied, 'x-carmilla-qa-mail' ) === '', 'HTTP invalid type denied without delivery' );
	$bad_nonce = $base; $bad_nonce['carmilla_test_message_nonce'] = 'invalid';
	$denied = $post( $bad_nonce );
	$check( ! is_wp_error( $denied ) && wp_remote_retrieve_response_code( $denied ) !== 302 && wp_remote_retrieve_header( $denied, 'x-carmilla-qa-mail' ) === '', 'HTTP bad nonce denied without delivery' );
} finally {
	WP_Session_Tokens::get_instance( $user )->destroy( $token );
	wp_delete_user( $user );
}
$check( get_user_by( 'id', $user ) === false, 'synthetic HTTP admin removed' );
echo "Failures: $failed.\n";
exit( $failed ? 1 : 0 );
