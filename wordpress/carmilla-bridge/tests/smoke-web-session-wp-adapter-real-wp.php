<?php
/**
 * Actual WordPress core integration in a disposable MariaDB database only.
 * Run in an isolated WordPress container sharing the DB container's network.
 * It installs synthetic WordPress tables under an explicit random QA prefix.
 */
$prefix = getenv( 'CARMILLA_ADAPTER_TEST_PREFIX' );
if ( ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) || getenv( 'CARMILLA_ADAPTER_DISPOSABLE' ) !== 'yes' ) {
	fwrite( STDERR, "Disposable test flag and random QA prefix are required.\n" );
	exit( 2 );
}
define( 'ABSPATH', '/usr/src/wordpress/' );
define( 'WP_INSTALLING', true );
define( 'WP_DEBUG', false );
define( 'DB_NAME', 'carmilla_adapter_qa' );
define( 'DB_USER', 'synthetic_qa' );
define( 'DB_PASSWORD', 'synthetic_qa_password' );
define( 'DB_HOST', '127.0.0.1:3306' );
define( 'DB_CHARSET', 'utf8mb4' );
define( 'DB_COLLATE', '' );
define( 'WP_HOME', 'https://qa.example.test/wordpress' );
define( 'WP_SITEURL', WP_HOME );
define( 'CB_WEB_SESSION_SITE_URL', WP_HOME );
define( 'CB_WEB_SESSION_SECRET_HEX', hash( 'sha256', 'synthetic-test-secret-only' ) );
foreach ( array( 'AUTH_KEY', 'SECURE_AUTH_KEY', 'LOGGED_IN_KEY', 'NONCE_KEY', 'AUTH_SALT', 'SECURE_AUTH_SALT', 'LOGGED_IN_SALT', 'NONCE_SALT' ) as $name ) {
	define( $name, hash( 'sha256', 'synthetic-wp-' . $name ) );
}
$table_prefix = $prefix;
$_SERVER['HTTP_HOST'] = 'qa.example.test';
$_SERVER['REQUEST_METHOD'] = 'GET';
$_SERVER['SERVER_NAME'] = 'qa.example.test';
$_SERVER['SERVER_PORT'] = '443';
$_SERVER['HTTPS'] = 'on';

try {
	require ABSPATH . 'wp-settings.php';
	require_once ABSPATH . 'wp-admin/includes/upgrade.php';
	add_filter( 'pre_wp_mail', static function () { return true; } );
	wp_install( 'Synthetic QA', 'qa_admin', 'synthetic@example.test', false, '', 'synthetic-only-password' );
	require_once '/work/includes/class-cb-web-session-wp-adapter.php';
	global $wpdb;
	foreach ( CB_Web_Session_Store::schema_sql( $prefix ) as $ddl ) {
		if ( $wpdb->query( $ddl ) === false ) { throw new RuntimeException(); }
	}
	$user = get_user_by( 'login', 'qa_admin' );
	if ( ! $user ) { throw new RuntimeException(); }
	$checks = 0;
	$failures = 0;
	$check = static function ( bool $condition, string $name ) use ( &$checks, &$failures ) {
		++$checks; if ( ! $condition ) { ++$failures; }
		echo ( $condition ? 'PASS  ' : 'FAIL  ' ) . $name . "\n";
	};
	$check( get_class( $wpdb ) === 'wpdb' && ! is_multisite() && get_current_blog_id() === 1, 'real WordPress core and single-site context loaded' );
	$store = CB_Web_Session_WP_Adapter::from_wordpress();
	$session = $store->create( (int) $user->ID );
	$check( is_array( $session ) && $store->is_active( $session['family_id'], (int) $user->ID ), 'real WordPress user creates an active family' );
	$check( $store->create( 9999999 ) === null, 'nonexistent real WordPress user is rejected' );
	$rotated = $store->rotate( $session['credential'] );
	$check( is_array( $rotated ) && $store->is_active( $session['family_id'], (int) $user->ID ), 'real WordPress user refresh rotates successfully' );
	wp_set_password( 'synthetic-new-password', (int) $user->ID );
	$check( ! $store->is_active( $session['family_id'], (int) $user->ID ) && $store->rotate( $rotated['credential'] ) === null, 'wp_set_password immediately invalidates old family' );
	$new = $store->create( (int) $user->ID );
	$check( is_array( $new ) && $store->is_active( $new['family_id'], (int) $user->ID ), 'a new family uses the current real WordPress password hash' );
	$store->revoke( $new['credential'] );
	$check( ! $store->is_active( $new['family_id'], (int) $user->ID ), 'real WordPress user family can be revoked' );
	echo "Checks: {$checks}; failures: {$failures}.\n";
	exit( $failures === 0 ? 0 : 1 );
} catch ( Throwable $e ) {
	fwrite( STDERR, "WordPress test infrastructure failed: " . get_class( $e ) . "\n" );
	exit( 2 );
}
