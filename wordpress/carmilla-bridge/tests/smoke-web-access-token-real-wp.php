<?php
/** Real WP core against disposable synthetic DB; never target a real site. */
$prefix = getenv( 'CARMILLA_TOKEN_TEST_PREFIX' );
if ( ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) || getenv( 'CARMILLA_TOKEN_DISPOSABLE' ) !== 'yes' ) {
	fwrite( STDERR, "Disposable test flag and random QA prefix required.\n" ); exit( 2 );
}
define( 'ABSPATH', '/usr/src/wordpress/' );
define( 'WP_INSTALLING', true ); define( 'WP_DEBUG', false );
define( 'DB_NAME', 'carmilla_token_qa' ); define( 'DB_USER', 'synthetic_qa' ); define( 'DB_PASSWORD', 'synthetic_qa_password' ); define( 'DB_HOST', '127.0.0.1:3306' );
define( 'DB_CHARSET', 'utf8mb4' ); define( 'DB_COLLATE', '' );
define( 'WP_HOME', 'https://qa.example.test/wordpress' ); define( 'WP_SITEURL', WP_HOME );
define( 'CB_WEB_SESSION_SITE_URL', WP_HOME );
define( 'CB_WEB_SESSION_SECRET_HEX', hash( 'sha256', 'synthetic-test-secret-only' ) );
define( 'CB_JWT_SECRET', hex2bin( CB_WEB_SESSION_SECRET_HEX ) );
foreach ( array( 'AUTH_KEY', 'SECURE_AUTH_KEY', 'LOGGED_IN_KEY', 'NONCE_KEY', 'AUTH_SALT', 'SECURE_AUTH_SALT', 'LOGGED_IN_SALT', 'NONCE_SALT' ) as $name ) { define( $name, hash( 'sha256', 'synthetic-wp-' . $name ) ); }
$table_prefix = $prefix;
$_SERVER['HTTP_HOST'] = 'qa.example.test'; $_SERVER['SERVER_NAME'] = 'qa.example.test'; $_SERVER['SERVER_PORT'] = '443'; $_SERVER['HTTPS'] = 'on'; $_SERVER['REQUEST_METHOD'] = 'GET';
try {
	require ABSPATH . 'wp-settings.php'; require_once ABSPATH . 'wp-admin/includes/upgrade.php';
	add_filter( 'pre_wp_mail', static function () { return true; } );
	wp_install( 'Synthetic QA', 'qa_admin', 'synthetic@example.test', false, '', 'synthetic-only-password' );
	require_once '/work/includes/class-cb-web-session-wp-adapter.php';
	require_once '/work/includes/class-cb-web-access-token.php';
	require_once '/work/includes/class-cb-jwt.php';
	global $wpdb;
	foreach ( CB_Web_Session_Store::schema_sql( $prefix ) as $ddl ) { if ( $wpdb->query( $ddl ) === false ) { throw new RuntimeException(); } }
	$user = get_user_by( 'login', 'qa_admin' ); if ( ! $user ) { throw new RuntimeException(); }
	$id = (int) $user->ID;
	$store = CB_Web_Session_WP_Adapter::from_wordpress();
	$tokens = new CB_Web_Access_Token( $store, CB_Web_Session_WP_Adapter::site_id( WP_HOME, 1 ), hex2bin( CB_WEB_SESSION_SECRET_HEX ) );
	$checks = 0; $failures = 0;
	$check = static function ( bool $condition, string $name ) use ( &$checks, &$failures ) { ++$checks; if ( ! $condition ) { ++$failures; } echo ( $condition ? 'PASS ' : 'FAIL ' ) . $name . "\n"; };
	$session = $store->create( $id ); $access = $tokens->issue( $id, $session['family_id'] );
	$wp_can = static function ( int $uid, string $capability ) { return user_can( $uid, $capability ); };
	$check( $tokens->verify( $access ) === $id, 'real WP user and database validate browser token' );
	$check( $tokens->authorize( $access, 'manage_options', $wp_can ), 'actual WordPress admin capability is checked' );
	$user->set_role( 'subscriber' );
	$check( $tokens->verify( $access ) === $id && ! $tokens->authorize( $access, 'manage_options', $wp_can ), 'real capability change immediately denies existing token' );
	$mobile = CB_JWT::issue( $user, 'access' );
	$check( CB_JWT::decode( $mobile )['typ'] === 'access' && $tokens->verify( $mobile ) === null && CB_JWT::decode( $access ) === null, 'real WP mobile and browser token formats stay isolated' );
	wp_set_password( 'synthetic-changed-password', $id );
	$check( $tokens->verify( $access ) === null, 'real wp_set_password invalidates existing access token' );
	$new = $store->create( $id ); $new_access = $tokens->issue( $id, $new['family_id'] );
	$check( $tokens->verify( $new_access ) === $id, 'new family uses the updated persisted password state' );
	$store->revoke( $new['credential'] );
	$check( $tokens->verify( $new_access ) === null, 'real store logout invalidates token' );
	$replay = $store->create( $id ); $replay_access = $tokens->issue( $id, $replay['family_id'] );
	$rotated = $store->rotate( $replay['credential'] ); $store->rotate( $replay['credential'] );
	$check( is_array( $rotated ) && $tokens->verify( $replay_access ) === null, 'real store replay invalidates token' );
	echo "Checks: {$checks}; failures: {$failures}.\n"; exit( $failures ? 1 : 0 );
} catch ( Throwable $e ) {
	fwrite( STDERR, 'WordPress test infrastructure failed: ' . get_class( $e ) . "\n" ); exit( 2 );
}
