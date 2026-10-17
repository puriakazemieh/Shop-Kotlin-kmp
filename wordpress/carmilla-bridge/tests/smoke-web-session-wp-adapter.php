<?php
/**
 * Uses synthetic data in a disposable MariaDB database named
 * carmilla_adapter_qa. No WordPress installation or real data is modified.
 * Requires CARMILLA_ADAPTER_TEST_PORT and a unique qa_<12 hex>_ prefix.
 */
define( 'ABSPATH', __DIR__ . '/' );
$port = (int) getenv( 'CARMILLA_ADAPTER_TEST_PORT' );
$prefix = getenv( 'CARMILLA_ADAPTER_TEST_PREFIX' );
if ( $port < 1 || $port > 65535 || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) ) {
	fwrite( STDERR, "A disposable local database port and random test prefix are required.\n" );
	exit( 2 );
}
$mode = $argv[1] ?? 'main';
$local_host = getenv( 'CARMILLA_ADAPTER_TEST_USE_LOCALHOST' ) === '1' ? 'localhost' : '127.0.0.1';
define( 'DB_HOST', $mode === 'remote' ? '192.0.2.1:3306' : ( $mode === 'persistent' ? 'p:127.0.0.1' : ( $mode === 'socket' ? 'localhost:/tmp/mysql.sock' : $local_host . ':' . $port ) ) );
define( 'DB_USER', 'synthetic_qa' );
define( 'DB_PASSWORD', 'synthetic_qa_password' );
define( 'DB_NAME', 'carmilla_adapter_qa' );
define( 'WP_CONTENT_DIR', __DIR__ . '/nonexistent-wp-content-fixture' );
define( 'CB_WEB_SESSION_SITE_URL', 'https://qa.example.test/wordpress' );
if ( $mode !== 'missing_key' ) {
	define( 'CB_WEB_SESSION_SECRET_HEX', $mode === 'weak_key' ? str_repeat( '0', 64 ) : hash( 'sha256', 'synthetic-test-secret-only' ) );
}
if ( $mode === 'custom_users' ) { define( 'CUSTOM_USER_TABLE', 'synthetic_other_users' ); }
if ( $mode === 'client_flags' ) { define( 'MYSQL_CLIENT_FLAGS', 1 ); }

mysqli_report( MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT );
require_once dirname( __DIR__ ) . '/includes/class-cb-web-session-wp-adapter.php';
$wp_site = 'https://qa.example.test/wordpress/';
$wp_blog_id = 1;
$wp_multisite = false;
function get_site_url( $blog_id ) { global $wp_site; return $wp_site; }
function get_current_blog_id() { global $wp_blog_id; return $wp_blog_id; }
function is_multisite() { global $wp_multisite; return $wp_multisite; }

/** Deliberate test double: no real WordPress plugins or configuration loaded. */
class wpdb {
	public $prefix;
	public $base_prefix;
	public $users;
	public $is_mysql = true;
	public $fake_primary_id = false;
	public $fake_database = false;
	private $db;
	public function __construct( mysqli $db, string $prefix ) {
		$this->db = $db;
		$this->prefix = $prefix;
		$this->base_prefix = $prefix;
		$this->users = $prefix . 'users';
	}
	public function get_blog_prefix( $blog_id ) { return $this->prefix; }
	public function get_var( $sql ) {
		if ( $this->fake_primary_id && $sql === 'SELECT CONNECTION_ID()' ) { return '999999999'; }
		if ( $this->fake_database && $sql === 'SELECT DATABASE()' ) { return 'other_database'; }
		return $this->db->query( $sql )->fetch_row()[0];
	}
}

try {
	$primary = new mysqli( $local_host, DB_USER, DB_PASSWORD, DB_NAME, $port );
	$wpdb = new wpdb( $primary, $prefix );
	if ( $mode !== 'main' ) {
		try { CB_Web_Session_WP_Adapter::from_wordpress(); echo "UNEXPECTED_SUCCESS\n"; exit( 1 ); }
		catch ( CB_Web_Session_Storage_Exception $e ) {
			echo $e->getMessage() === 'WordPress session adapter unavailable.' ? "BLOCKED\n" : "UNEXPECTED_ERROR\n";
			exit( $e->getMessage() === 'WordPress session adapter unavailable.' ? 0 : 1 );
		}
	}
	$primary->query( "CREATE TABLE `{$prefix}users` (ID BIGINT UNSIGNED NOT NULL PRIMARY KEY,user_pass VARCHAR(255) NOT NULL) ENGINE=InnoDB" );
	$primary->query( "INSERT INTO `{$prefix}users` (ID,user_pass) VALUES (1,'synthetic-password-hash-one')" );
	foreach ( CB_Web_Session_Store::schema_sql( $prefix ) as $ddl ) { $primary->query( $ddl ); }
	$checks = 0;
	$failures = 0;
	$check = static function ( bool $condition, string $name ) use ( &$checks, &$failures ) {
		++$checks; if ( ! $condition ) { ++$failures; }
		echo ( $condition ? 'PASS  ' : 'FAIL  ' ) . $name . "\n";
	};
	$reject = static function ( callable $fn ): bool {
		try { $fn(); return false; } catch ( CB_Web_Session_Storage_Exception $e ) { return true; }
	};
	$store = CB_Web_Session_WP_Adapter::from_wordpress();
	$session = $store->create( 1 );
	$check( is_array( $session ) && $store->is_active( $session['family_id'], 1 ), 'local dedicated adapter creates an active session' );
	$check( $store->create( 999 ) === null, 'missing WordPress user does not create a session' );
	$old = $store->rotate( $session['credential'] );
	$check( is_array( $old ) && $store->is_active( $session['family_id'], 1 ), 'adapter supports real session rotation' );
	$primary->begin_transaction();
	$primary->query( "SELECT user_pass FROM `{$prefix}users` WHERE ID=1" )->fetch_row();
	$writer = new mysqli( '127.0.0.1', DB_USER, DB_PASSWORD, DB_NAME, $port );
	$writer->query( "UPDATE `{$prefix}users` SET user_pass='synthetic-password-hash-two' WHERE ID=1" );
	$check( ! $store->is_active( $session['family_id'], 1 ) && $store->rotate( $old['credential'] ) === null, 'password provider bypasses stale primary transaction snapshot' );
	$primary->rollback();
	$writer->query( "UPDATE `{$prefix}users` SET user_pass='synthetic-password-hash-one' WHERE ID=1" );
	$other = $store->create( 1 );
	$writer->query( "DELETE FROM `{$prefix}users` WHERE ID=1" );
	$check( ! $store->is_active( $other['family_id'], 1 ), 'deleting the synthetic user invalidates the session' );
	$writer->query( "INSERT INTO `{$prefix}users` (ID,user_pass) VALUES (1,'synthetic-password-hash-one')" );
	$wpdb->fake_primary_id = true;
	$check( $reject( static function () { CB_Web_Session_WP_Adapter::from_wordpress(); } ), 'different primary connection is rejected' );
	$wpdb->fake_primary_id = false;
	$wpdb->fake_database = true;
	$check( $reject( static function () { CB_Web_Session_WP_Adapter::from_wordpress(); } ), 'different primary database is rejected' );
	$wpdb->fake_database = false;
	$wp_site = 'https://other.example.test/wordpress';
	$check( $reject( static function () { CB_Web_Session_WP_Adapter::from_wordpress(); } ), 'configured and WordPress site URL mismatch is rejected' );
	$wp_site = 'https://qa.example.test/wordpress/';
	$wp_multisite = true;
	$check( $reject( static function () { CB_Web_Session_WP_Adapter::from_wordpress(); } ), 'unverified multisite is rejected' );
	$wp_multisite = false; $wp_blog_id = 2;
	$check( $reject( static function () { CB_Web_Session_WP_Adapter::from_wordpress(); } ), 'unexpected blog id is rejected' );
	$wp_blog_id = 1;
	$wpdb->users = 'unexpected_users';
	$check( $reject( static function () { CB_Web_Session_WP_Adapter::from_wordpress(); } ), 'unexpected users table is rejected' );
	$wpdb->users = $prefix . 'users';
	$wpdb->base_prefix = 'unexpected_';
	$check( $reject( static function () { CB_Web_Session_WP_Adapter::from_wordpress(); } ), 'unexpected database prefix is rejected' );
	$wpdb->base_prefix = $prefix;
	$before_host = CB_Web_Session_WP_Adapter::canonical_site_url( CB_WEB_SESSION_SITE_URL );
	$_SERVER['HTTP_HOST'] = 'attacker.invalid'; $_SERVER['HTTP_X_FORWARDED_HOST'] = 'attacker.invalid';
	$check( $before_host === CB_Web_Session_WP_Adapter::canonical_site_url( CB_WEB_SESSION_SITE_URL ) && is_array( CB_Web_Session_WP_Adapter::from_wordpress()->create( 1 ) ), 'request host headers do not select site identity' );
	$check( CB_Web_Session_WP_Adapter::canonical_site_url( 'HTTPS://QA.EXAMPLE.TEST:8443/wordpress/' ) === 'https://qa.example.test:8443/wordpress', 'site URL canonicalizes scheme, host and trailing slash' );
	$site_id = CB_Web_Session_WP_Adapter::site_id( 'https://qa.example.test/wordpress/', 1 );
	$check( $site_id === CB_Web_Session_WP_Adapter::site_id( 'https://QA.EXAMPLE.TEST/wordpress', 1 ) && $site_id !== CB_Web_Session_WP_Adapter::site_id( 'https://qa.example.test/wordpress', 2 ) && $site_id !== CB_Web_Session_WP_Adapter::site_id( 'https://qa.example.test:8443/wordpress', 1 ), 'canonical identity is stable and binds blog and explicit port' );
	$check( $reject( static function () { CB_Web_Session_WP_Adapter::site_id( 'https://qa.example.test/', 0 ); } ), 'invalid blog identity is rejected' );
	foreach ( array( 'https://qa.example.test/../x', 'https://qa.example.test/%2e%2e/x', 'https://user@qa.example.test/x', 'https://qa.example.test/x?query=1', 'https://qa.example.test:0/x', 'https://qa.example.test//x', 'https://-bad.example.test/x' ) as $invalid ) {
		$check( $reject( static function () use ( $invalid ) { CB_Web_Session_WP_Adapter::canonical_site_url( $invalid ); } ), 'invalid canonical site URL rejected' );
	}
	foreach ( array( 'remote', 'persistent', 'socket', 'missing_key', 'weak_key', 'custom_users', 'client_flags' ) as $child_mode ) {
		$proc = proc_open( array( PHP_BINARY, __FILE__, $child_mode ), array( 0 => array( 'pipe', 'r' ), 1 => array( 'pipe', 'w' ), 2 => array( 'pipe', 'w' ) ), $pipes );
		if ( ! is_resource( $proc ) ) { throw new RuntimeException(); }
		fclose( $pipes[0] ); $output = trim( stream_get_contents( $pipes[1] ) ); $errors = stream_get_contents( $pipes[2] );
		fclose( $pipes[1] ); fclose( $pipes[2] ); $exit = proc_close( $proc );
		$check( $exit === 0 && $output === 'BLOCKED' && $errors === '', 'unsupported configuration fails without secret-bearing diagnostics: ' . $child_mode );
	}
	$disconnect_store = CB_Web_Session_WP_Adapter::from_wordpress();
	$disconnect_session = $disconnect_store->create( 1 );
	$password_property = new ReflectionProperty( CB_Web_Session_Store::class, 'password_hash_for_user' );
	$password_property->setAccessible( true );
	$password_closure = $password_property->getValue( $disconnect_store );
	$password_vars = ( new ReflectionFunction( $password_closure ) )->getStaticVariables();
	$writer->kill( $password_vars['password_db']->thread_id );
	$check( $reject( static function () use ( $disconnect_store, $disconnect_session ) { $disconnect_store->is_active( $disconnect_session['family_id'], 1 ); } ), 'killed password connection fails closed without reconnect' );
	$disconnect_store = CB_Web_Session_WP_Adapter::from_wordpress();
	$connection_property = new ReflectionProperty( CB_Web_Session_Store::class, 'db' );
	$connection_property->setAccessible( true );
	$store_connection = $connection_property->getValue( $disconnect_store );
	$writer->kill( $store_connection->thread_id );
	$check( $reject( static function () use ( $disconnect_store ) { $disconnect_store->create( 1 ); } ), 'killed store connection fails closed without reconnect' );
	// Break the users table only in the disposable fixture to check failure is
	// not confused with a deleted user or a valid family.
	$writer->query( "RENAME TABLE `{$prefix}users` TO `{$prefix}users_hidden`" );
	$check( $reject( static function () use ( $store, $other ) { $store->is_active( $other['family_id'], 1 ); } ), 'password database failure rejects access' );
	echo "Checks: {$checks}; failures: {$failures}.\n";
	exit( $failures === 0 ? 0 : 1 );
} catch ( Throwable $e ) {
	fwrite( STDERR, "Test infrastructure failed: " . get_class( $e ) . "\n" );
	exit( 2 );
}
