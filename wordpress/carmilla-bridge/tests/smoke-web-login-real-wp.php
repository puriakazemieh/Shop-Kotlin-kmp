<?php
/** Actual WP core, exclusively on an isolated disposable synthetic database. */
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
	require_once '/work/includes/class-cb-web-auth-login.php';
	global $wpdb;
	foreach ( CB_Web_Session_Store::schema_sql( $prefix ) as $ddl ) { if ( $wpdb->query( $ddl ) === false ) { throw new RuntimeException(); } }
	$store = CB_Web_Session_WP_Adapter::from_wordpress();
	$site = CB_Web_Session_WP_Adapter::site_id( WP_HOME, 1 );
	$tokens = new CB_Web_Access_Token( $store, $site, hex2bin( CB_WEB_SESSION_SECRET_HEX ) );
	$policy = new CB_Web_Request_Policy( WP_HOME, 1, array( 'https://qa.example.test' ), 'production' );
	$issuer = new CB_Web_Auth_Session_Issuer( $policy, $store, $tokens );
	$login = new CB_Web_Password_Login( $policy, $issuer );
	$headers = array( 'Host' => 'qa.example.test', 'Origin' => 'https://qa.example.test', 'Content-Type' => 'application/json', 'X-Carmilla-Web-Request' => '1' );
	$server = array( 'REMOTE_ADDR' => '192.0.2.42', 'SERVER_ADDR' => '192.0.2.1', 'HTTPS' => 'on' );
	$input = array( 'username' => 'synthetic@example.test', 'password' => 'synthetic-only-password' );
	$checks = 0; $failures = 0;
	$check = static function ( bool $ok, string $name ) use ( &$checks, &$failures ) { ++$checks; if ( ! $ok ) { ++$failures; } echo ( $ok ? 'PASS ' : 'FAIL ' ) . $name . "\n"; };
	$count = static function ( string $where = '1=1' ) use ( $wpdb, $prefix ): int { return (int) $wpdb->get_var( "SELECT COUNT(*) FROM `{$prefix}carmilla_web_families` WHERE {$where}" ); };
	$calls = 0; $observer = static function ( $user ) use ( &$calls ) { ++$calls; return $user; }; add_filter( 'authenticate', $observer, 99 );
	$hostile = $headers; $hostile['Origin'] = 'https://evil.example.test';
	$r = $login->login( 'POST', $hostile, $server, $input );
	$check( $r->status() === 403 && $calls === 0 && $count() === 0 && $r->cookie() === null, 'hostile origin rejected before WordPress authentication and mutation' );
	$r = $login->login( 'GET', $headers, $server, $input );
	$check( $r->status() === 403 && $calls === 0 && $count() === 0, 'wrong method rejected before authentication' );
	$bad_header = $headers; unset( $bad_header['X-Carmilla-Web-Request'] );
	$check( $login->login( 'POST', $bad_header, $server, $input )->status() === 403 && $calls === 0 && $count() === 0, 'missing CSRF header rejected before authentication' );
	$bad_transport = $server; $bad_transport['HTTPS'] = '';
	$check( $login->login( 'POST', $headers, $bad_transport, $input )->status() === 403 && $calls === 0 && $count() === 0, 'unprotected transport rejected before authentication' );
	$r = $login->login( 'POST', $headers, $server, array( 'username' => array(), 'password' => 'x' ) );
	$check( $r->status() === 400 && $calls === 0 && $count() === 0, 'malformed input rejected before authentication' );
	$r = $login->login( 'POST', $headers, $server, array( 'username' => $input['username'], 'password' => 'wrong' ) );
	$check( $r->status() === 401 && $calls > 0 && $count() === 0 && $r->cookie() === null, 'incorrect password creates no family or cookie' );
	$reject = static function () { return new WP_Error( 'synthetic_rejection', 'rejected' ); }; add_filter( 'authenticate', $reject, 100 );
	$r = $login->login( 'POST', $headers, $server, $input ); remove_filter( 'authenticate', $reject, 100 );
	$check( $r->status() === 401 && $count() === 0 && $r->cookie() === null, 'real WordPress authentication filter rejection is honored' );
	$swap = static function ( $user ) { if ( $user instanceof WP_User ) { wp_set_password( 'synthetic-new-password', $user->ID ); } return $user; };
	add_filter( 'authenticate', $swap, 100 ); $r = $login->login( 'POST', $headers, $server, $input ); remove_filter( 'authenticate', $swap, 100 );
	$check( $r->status() === 503 && $count() === 0 && $r->cookie() === null, 'password change between authentication and session creation fails closed' );
	wp_set_password( $input['password'], get_user_by( 'login', 'qa_admin' )->ID );
	$r = $login->login( 'POST', $headers, $server, $input );
	$body = $r->body(); $handoff = $r->cookie();
	$check( $r->status() === 200 && array_keys( $body ) === array( 'accessToken', 'user' ) && $tokens->verify( $body['accessToken'] ) === (int) $body['user']['id'], 'real WP email/password yields valid scoped access token and compatible user' );
	$check( $handoff instanceof CB_Web_Cookie_Handoff && $count() === 1, 'one committed family has an isolated cookie handoff' );
	ob_start(); var_dump( $r, $handoff ); $dump = ob_get_clean();
	$export = var_export( $handoff, true ); $json = json_encode( $body );
	$serialization_blocked = false; try { serialize( $handoff ); } catch ( Throwable $ignored ) { $serialization_blocked = true; }
	$cookie = $handoff->take();
	$check( $serialization_blocked && $handoff->take() === null && strpos( $dump . $export . $json, $cookie['value'] ) === false && ! isset( $body['refreshToken'], $body['family_id'] ), 'credential absent from public JSON, debug dump and export; handoff is one-time and non-serializable' );
	$check( $cookie['options']['secure'] && $cookie['options']['httponly'] && $cookie['options']['samesite'] === 'Strict' && $store->rotate( $cookie['value'] ) !== null, 'handoff carries secure cookie options and valid server-side credential' );
	$uid = (int) $body['user']['id']; update_user_meta( $uid, 'cb_mobile', '09120000001' );
	$r = $login->login( 'POST', $headers, $server, array( 'username' => '09120000001', 'password' => $input['password'] ) );
	$check( $r->status() === 200, 'unambiguous stored mobile identifier works through real WordPress filters' );
	$second = wp_create_user( 'other_qa', 'synthetic-other-password', 'other@example.test' ); update_user_meta( $second, 'cb_mobile', '09120000001' );
	$before = $count(); $r = $login->login( 'POST', $headers, $server, array( 'username' => '09120000001', 'password' => $input['password'] ) );
	$check( $r->status() === 401 && $count() === $before, 'duplicate mobile identifiers fail closed without creating a session' );
	$badclock = static function () { throw new RuntimeException(); };
	$failing_tokens = new CB_Web_Access_Token( $store, $site, hex2bin( CB_WEB_SESSION_SECRET_HEX ), $badclock );
	$failing_login = new CB_Web_Password_Login( $policy, new CB_Web_Auth_Session_Issuer( $policy, $store, $failing_tokens ) );
	$before = $count(); $revoked = $count( 'revoked=1' ); $r = $failing_login->login( 'POST', $headers, $server, $input );
	$check( $r->status() === 503 && $r->cookie() === null && $count() === $before + 1 && $count( 'revoked=1' ) === $revoked + 1, 'post-commit signing failure returns no credential and revokes the created family' );
	$missing = new WP_User(); $missing->ID = 9999999;
	$check( $issuer->issue_for( $missing )->status() === 503, 'missing user cannot receive a session' );
	$mismatch = false; try { new CB_Web_Auth_Session_Issuer( new CB_Web_Request_Policy( 'https://qa.example.test/other', 1, array( 'https://qa.example.test' ), 'production' ), $store, $tokens ); } catch ( InvalidArgumentException $ignored ) { $mismatch = true; }
	$check( $mismatch, 'different installation configuration is rejected' );
	$broken_store = CB_Web_Session_WP_Adapter::from_wordpress();
	$connection = new ReflectionProperty( CB_Web_Session_Store::class, 'db' ); $connection->setAccessible( true );
	$broken_db = $connection->getValue( $broken_store ); $broken_db->close();
	$broken_tokens = new CB_Web_Access_Token( $broken_store, $site, hex2bin( CB_WEB_SESSION_SECRET_HEX ) );
	$broken_login = new CB_Web_Password_Login( $policy, new CB_Web_Auth_Session_Issuer( $policy, $broken_store, $broken_tokens ) );
	$before = $count(); $r = $broken_login->login( 'POST', $headers, $server, $input );
	$check( $r->status() === 503 && $r->cookie() === null && $count() === $before, 'closed database connection yields no credentials and no partial family' );
	$revocation_store = CB_Web_Session_WP_Adapter::from_wordpress(); $revocation_db = $connection->getValue( $revocation_store );
	$close_on_issue = static function () use ( $revocation_db ) { $revocation_db->close(); throw new RuntimeException(); };
	$revocation_tokens = new CB_Web_Access_Token( $revocation_store, $site, hex2bin( CB_WEB_SESSION_SECRET_HEX ), $close_on_issue );
	$revocation_login = new CB_Web_Password_Login( $policy, new CB_Web_Auth_Session_Issuer( $policy, $revocation_store, $revocation_tokens ) );
	$before = $count(); $active = $count( 'revoked=0' ); $r = $revocation_login->login( 'POST', $headers, $server, $input );
	$check( $r->status() === 503 && $r->cookie() === null && $r->body() === array( 'error' => 'authentication_unavailable' ) && $count() === $before + 1 && $count( 'revoked=0' ) === $active + 1, 'failed cleanup returns storage error and does not claim orphaned family was revoked' );
	echo "Checks: {$checks}; failures: {$failures}.\n"; exit( $failures ? 1 : 0 );
} catch ( Throwable $e ) { fwrite( STDERR, 'WordPress test infrastructure failed: ' . get_class( $e ) . "\n" ); exit( 2 ); }
