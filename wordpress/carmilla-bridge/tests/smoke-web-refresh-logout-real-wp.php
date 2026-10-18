<?php
/** Real WP and a synthetic disposable database only; no HTTP route is enabled. */
$prefix = getenv( 'CARMILLA_LOGIN_TEST_PREFIX' );
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) ) { fwrite( STDERR, "Disposable test flag and random prefix required.\n" ); exit( 2 ); }
define( 'ABSPATH', '/usr/src/wordpress/' ); define( 'WP_INSTALLING', true ); define( 'WP_DEBUG', false );
define( 'DB_NAME', 'carmilla_login_qa' ); define( 'DB_USER', 'synthetic_qa' ); define( 'DB_PASSWORD', 'synthetic_qa_password' ); define( 'DB_HOST', '127.0.0.1:3306' );
define( 'DB_CHARSET', 'utf8mb4' ); define( 'DB_COLLATE', '' ); define( 'WP_HOME', 'https://qa.example.test/wordpress' ); define( 'WP_SITEURL', WP_HOME );
define( 'CB_WEB_SESSION_SITE_URL', WP_HOME ); define( 'CB_WEB_SESSION_SECRET_HEX', hash( 'sha256', 'synthetic-login-qa-only' ) ); define( 'CB_JWT_SECRET', hex2bin( CB_WEB_SESSION_SECRET_HEX ) );
foreach ( array( 'AUTH_KEY', 'SECURE_AUTH_KEY', 'LOGGED_IN_KEY', 'NONCE_KEY', 'AUTH_SALT', 'SECURE_AUTH_SALT', 'LOGGED_IN_SALT', 'NONCE_SALT' ) as $name ) { define( $name, hash( 'sha256', 'synthetic-wp-' . $name ) ); }
$table_prefix = $prefix;
$_SERVER['HTTP_HOST'] = 'qa.example.test'; $_SERVER['SERVER_NAME'] = 'qa.example.test'; $_SERVER['SERVER_PORT'] = '443'; $_SERVER['HTTPS'] = 'on'; $_SERVER['REQUEST_METHOD'] = 'GET';
try {
	require ABSPATH . 'wp-settings.php'; require_once ABSPATH . 'wp-admin/includes/upgrade.php';
	add_filter( 'pre_wp_mail', static function () { return true; } );
	wp_install( 'Synthetic QA', 'qa_admin', 'synthetic@example.test', false, '', 'synthetic-only-password' );
	require_once '/work/includes/class-cb-web-auth-session.php';
	global $wpdb;
	foreach ( CB_Web_Session_Store::schema_sql( $prefix ) as $ddl ) { if ( $wpdb->query( $ddl ) === false ) { throw new RuntimeException(); } }
	$store = CB_Web_Session_WP_Adapter::from_wordpress();
	$site = CB_Web_Session_WP_Adapter::site_id( WP_HOME, 1 );
	$tokens = new CB_Web_Access_Token( $store, $site, hex2bin( CB_WEB_SESSION_SECRET_HEX ) );
	$policy = new CB_Web_Request_Policy( WP_HOME, 1, array( 'https://qa.example.test' ), 'production' );
	$session = new CB_Web_Auth_Session( $policy, $store, $tokens );
	$login = new CB_Web_Password_Login( $policy, new CB_Web_Auth_Session_Issuer( $policy, $store, $tokens ) );
	$headers = array( 'Host' => 'qa.example.test', 'Origin' => 'https://qa.example.test', 'Content-Type' => 'application/json', 'X-Carmilla-Web-Request' => '1' );
	$server = array( 'REMOTE_ADDR' => '192.0.2.42', 'SERVER_ADDR' => '192.0.2.1', 'HTTPS' => 'on' );
	$input = array( 'username' => 'synthetic@example.test', 'password' => 'synthetic-only-password' );
	$checks = 0; $failures = 0;
	$check = static function ( bool $ok, string $name ) use ( &$checks, &$failures ) { ++$checks; if ( ! $ok ) { ++$failures; } echo ( $ok ? 'PASS ' : 'FAIL ' ) . $name . "\n"; };
	$new_login = static function () use ( $login, $headers, $server, $input ) { $r = $login->login( 'POST', $headers, $server, $input ); if ( $r->status() !== 200 || $r->cookie() === null ) { throw new RuntimeException(); } return array( $r->body(), $r->cookie()->take()['value'] ); };
	list( $first, $old ) = $new_login(); list( $other, $other_credential ) = $new_login();
	$hostile = $headers; $hostile['Origin'] = 'https://evil.example.test';
	$check( $session->refresh( 'POST', $hostile, $server, $old )->status() === 403 && $session->logout( 'POST', $hostile, $server, $old )->status() === 403 && $tokens->verify( $first['accessToken'] ) !== null, 'origin rejected before refresh or logout mutation' );
	$missing_header = $headers; unset( $missing_header['X-Carmilla-Web-Request'] );
	$check( $session->refresh( 'POST', $missing_header, $server, $old )->status() === 403 && $session->logout( 'GET', $headers, $server, $old )->status() === 403 && $tokens->verify( $first['accessToken'] ) !== null, 'missing CSRF header and wrong method rejected without mutation' );
	$check( $session->refresh( 'POST', $headers, $server, '' )->status() === 401 && $session->refresh( 'POST', $headers, $server, str_repeat( 'a', 64 ) )->status() === 401, 'missing and unknown credentials denied' );
	$expired_session = $store->create( (int) $first['user']['id'] );
	$updated = $wpdb->query( $wpdb->prepare( "UPDATE `{$prefix}carmilla_web_families` SET idle_expiry=%d WHERE family_id=%s", time() - 1, $expired_session['family_id'] ) );
	$check( $updated === 1 && $session->refresh( 'POST', $headers, $server, $expired_session['credential'] )->status() === 401, 'expired family cannot refresh' );
	$r = $session->refresh( 'POST', $headers, $server, $old ); $body = $r->body(); $handoff = $r->cookie();
	$check( $r->status() === 200 && array_keys( $body ) === array( 'accessToken', 'user' ) && $tokens->verify( $body['accessToken'] ) === (int) $body['user']['id'] && $handoff instanceof CB_Web_Cookie_Handoff, 'refresh issues scoped access token and cookie handoff' );
	ob_start(); var_dump( $r, $handoff ); $dump = ob_get_clean(); $export = var_export( $handoff, true ); $encoded = json_encode( $body ); $cookie = $handoff->take();
	$check( $handoff->take() === null && $cookie['value'] !== $old && strpos( $dump . $export . $encoded, $cookie['value'] ) === false && strpos( $encoded, $old ) === false, 'rotated credential is one-time handoff and absent from body and debug output' );
	$check( $cookie['options']['secure'] && $cookie['options']['httponly'] && $cookie['options']['samesite'] === 'Strict' && $cookie['options']['path'] === '/', 'refresh cookie preserves secure attributes' );
	$check( $session->refresh( 'POST', $headers, $server, $old )->status() === 401 && $tokens->verify( $body['accessToken'] ) === null && $session->refresh( 'POST', $headers, $server, $cookie['value'] )->status() === 401 && $tokens->verify( $other['accessToken'] ) !== null, 'replay revokes only the known family including rotated credential' );
	$r = $session->logout( 'POST', $headers, $server, $other_credential ); $expired = $r->cookie()->take();
	$check( $r->status() === 200 && $tokens->verify( $other['accessToken'] ) === null && $expired['value'] === '' && $expired['options']['expires'] < time() && $expired['options']['secure'] && $expired['options']['httponly'] && $expired['options']['samesite'] === 'Strict', 'logout revokes family and returns expired secure cookie' );
	$check( $session->logout( 'POST', $headers, $server, $other_credential )->status() === 200 && $session->logout( 'POST', $headers, $server, '' )->status() === 200, 'logout is idempotent for revoked and absent credentials' );
	list( $changed, $changed_credential ) = $new_login(); wp_set_password( 'synthetic-new-password', (int) $changed['user']['id'] );
	$check( $session->refresh( 'POST', $headers, $server, $changed_credential )->status() === 401 && $tokens->verify( $changed['accessToken'] ) === null, 'password change invalidates refresh and access' );
	wp_set_password( $input['password'], (int) $changed['user']['id'] );
	list( $signing_body, $signing_credential ) = $new_login(); $badclock = static function () { throw new RuntimeException(); };
	$bad_tokens = new CB_Web_Access_Token( $store, $site, hex2bin( CB_WEB_SESSION_SECRET_HEX ), $badclock );
	$bad_session = new CB_Web_Auth_Session( $policy, $store, $bad_tokens ); $r = $bad_session->refresh( 'POST', $headers, $server, $signing_credential );
	$check( $r->status() === 503 && $r->cookie() === null && $tokens->verify( $signing_body['accessToken'] ) === null, 'signer failure issues no cookie and revokes rotated family when store is available' );
	list( $cleanup_body, $cleanup_credential ) = $new_login(); $cleanup_store = CB_Web_Session_WP_Adapter::from_wordpress();
	$connection = new ReflectionProperty( CB_Web_Session_Store::class, 'db' ); $connection->setAccessible( true ); $cleanup_db = $connection->getValue( $cleanup_store );
	$close_on_issue = static function () use ( $cleanup_db ) { $cleanup_db->close(); throw new RuntimeException(); };
	$cleanup_tokens = new CB_Web_Access_Token( $cleanup_store, $site, hex2bin( CB_WEB_SESSION_SECRET_HEX ), $close_on_issue );
	$cleanup_session = new CB_Web_Auth_Session( $policy, $cleanup_store, $cleanup_tokens ); $r = $cleanup_session->refresh( 'POST', $headers, $server, $cleanup_credential );
	$check( $r->status() === 503 && $r->cookie() === null && $tokens->verify( $cleanup_body['accessToken'] ) !== null, 'failed cleanup is reported as unavailable without claiming revocation' );
	list( $failure_body, $failure_credential ) = $new_login(); $broken_store = CB_Web_Session_WP_Adapter::from_wordpress();
	$connection->getValue( $broken_store )->close();
	$broken_tokens = new CB_Web_Access_Token( $broken_store, $site, hex2bin( CB_WEB_SESSION_SECRET_HEX ) ); $broken = new CB_Web_Auth_Session( $policy, $broken_store, $broken_tokens );
	$check( $broken->refresh( 'POST', $headers, $server, $failure_credential )->status() === 503, 'refresh reports unavailable store as error' );
	$r = $broken->logout( 'POST', $headers, $server, $failure_credential );
	$check( $r->status() === 503 && $r->cookie() !== null && $tokens->verify( $failure_body['accessToken'] ) !== null, 'logout reports failed server revocation and offers local cookie clearing' );
	$mismatch = false; try { new CB_Web_Auth_Session( new CB_Web_Request_Policy( 'https://qa.example.test/other', 1, array( 'https://qa.example.test' ), 'production' ), $store, $tokens ); } catch ( InvalidArgumentException $ignored ) { $mismatch = true; }
	$check( $mismatch, 'different installation configuration rejected' );
	$mismatch = false; try { new CB_Web_Auth_Session( $policy, CB_Web_Session_WP_Adapter::from_wordpress(), $tokens ); } catch ( InvalidArgumentException $ignored ) { $mismatch = true; }
	$check( $mismatch, 'different store object rejected even with the same site identity' );
	echo "Checks: {$checks}; failures: {$failures}.\n"; echo $failures ? '' : 'PASS web refresh logout WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n"; exit( $failures ? 1 : 0 );
} catch ( Throwable $e ) { fwrite( STDERR, 'WordPress test infrastructure failed: ' . get_class( $e ) . "\n" ); exit( 2 ); }
