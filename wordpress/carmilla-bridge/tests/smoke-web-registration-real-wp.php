<?php
/** Disposable installed WordPress only; no credentials are printed. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
$bridge = dirname( __DIR__ );
require_once $bridge . '/includes/class-cb-web-auth-registration.php';
global $wpdb;
$checks = 0; $failures = 0;
$check = static function ( bool $ok, string $label ) use ( &$checks, &$failures ) { ++$checks; if ( ! $ok ) { ++$failures; } echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n"; };
$suffix = substr( hash( 'sha256', microtime( true ) . random_int( 1, PHP_INT_MAX ) ), 0, 12 );
$policy = new CB_Web_Request_Policy( 'http://localhost:8080', 1, array( 'http://localhost:8080' ), 'local', true );
$headers = array( 'Host' => 'localhost:8080', 'Origin' => 'http://localhost:8080', 'Content-Type' => 'application/json', 'X-Carmilla-Web-Request' => '1' );
$server = array( 'REMOTE_ADDR' => '127.0.0.1', 'SERVER_ADDR' => '127.0.0.1', 'HTTPS' => '' );
$input = array( 'email' => 'webreg_' . $suffix . '@example.test', 'mobile' => 'webreg_' . $suffix, 'password' => 'synthetic-only-password' );
$issued = 0;
$issuer = static function ( WP_User $user ) use ( &$issued ) { ++$issued; return new CB_Web_Auth_Result( 200, array( 'accessToken' => 'synthetic-access', 'user' => array( 'id' => $user->ID ) ), new CB_Web_Cookie_Handoff( 'synthetic-opaque-cookie', array( 'secure' => true ) ) ); };
$registration = new CB_Web_Browser_Registration( $policy, $issuer );
$hostile = $headers; $hostile['Origin'] = 'http://evil.example.test';
$check( $registration->register( 'POST', $hostile, $server, $input )->status() === 403 && $issued === 0, 'untrusted origin is rejected before account creation' );
$success = $registration->register( 'POST', $headers, $server, $input ); $body = $success->body();
$check( $success->status() === 201 && $issued === 1 && isset( $body['accessToken'], $body['user'] ) && ! isset( $body['refreshToken'] ) && $success->cookie() instanceof CB_Web_Cookie_Handoff, 'supported account receives only redacted cookie handoff after success' );
$duplicate = $registration->register( 'POST', $headers, $server, array( 'email' => 'other_' . $suffix . '@example.test', 'mobile' => $input['mobile'], 'password' => $input['password'] ) );
$check( $duplicate->status() === 409 && $issued === 1 && $duplicate->cookie() === null, 'duplicate identity receives no session or cookie' );
$failing = new CB_Web_Browser_Registration( $policy, static function () { return new CB_Web_Auth_Result( 503, array( 'error' => 'authentication_unavailable' ) ); } );
$partial_input = array( 'email' => 'partial_' . $suffix . '@example.test', 'mobile' => 'partial_' . $suffix, 'password' => 'synthetic-only-password' );
$partial = $failing->register( 'POST', $headers, $server, $partial_input );
$partial_user = get_user_by( 'email', $partial_input['email'] );
$check( $partial->status() === 503 && $partial->body() === array( 'error' => 'account_recovery_required' ) && $partial->cookie() === null && $partial_user instanceof WP_User && get_user_meta( $partial_user->ID, 'cb_identity_writer_state', true ) === 'needs_recovery', 'session failure preserves account truthfully without a credential' );
$check( $registration->register( 'POST', $headers, $server, array( 'email' => 'not-an-email', 'mobile' => '', 'password' => 'x' ) )->status() === 400, 'invalid input is rejected before account creation' );
echo "Checks: {$checks}; failures: {$failures}.\n";
exit( $failures ? 1 : 0 );
