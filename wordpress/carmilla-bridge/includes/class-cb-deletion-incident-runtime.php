<?php
/**
 * Operational wiring for the bounded native-user-deletion observer.
 *
 * This component deliberately records an observed survivor; it never claims
 * that WordPress has rolled back a partially executed delete or that external
 * callbacks can be undone.
 */
if ( ! defined( 'ABSPATH' ) ) { exit; }

require_once __DIR__ . '/class-cb-deletion-incident-observer.php';
require_once __DIR__ . '/class-cb-deletion-incident-notice.php';

final class CB_Deletion_Incident_Runtime {
	private static $instance = null;
	private $connection;
	private $observer;
	private $notice;

	public static function boot(): void {
		if ( self::$instance !== null ) { return; }
		try {
			self::$instance = new self();
		} catch ( Throwable $error ) {
			// A detection facility must fail closed: never turn an unavailable
			// observer into a claim that the native deletion was verified.
			self::$instance = false;
		}
	}

	private function __construct() {
		global $wpdb;
		if ( ! $wpdb instanceof wpdb ) { throw new RuntimeException(); }
		$this->connection = self::connect();
		$prefix = (string) $wpdb->prefix;
		$ddl = str_replace( 'CREATE TABLE ', 'CREATE TABLE IF NOT EXISTS ', CB_Deletion_Incident_Store::schema_sql( $prefix ) );
		if ( ! $this->connection->query( $ddl ) ) { throw new RuntimeException(); }
		$site = hash( 'sha256', (string) get_site_url() );
		$store = new CB_Deletion_Incident_Store( $this->connection, $prefix, $site );
		$this->observer = new CB_Deletion_Incident_Observer( $this->connection, $wpdb->users, $wpdb->usermeta, $store, 86400 );
		$this->notice = new CB_Deletion_Incident_Notice( $store );
		add_action( 'delete_user', array( $this, 'before_delete' ), 1, 1 );
		add_action( 'deleted_user', array( $this, 'after_delete' ), PHP_INT_MAX, 1 );
		add_action( 'admin_notices', array( $this, 'render_notice' ) );
	}

	public function before_delete( $account_id ): void {
		$this->observer->begin( (int) $account_id );
	}

	public function after_delete( $account_id ): void {
		$this->observer->after( (int) $account_id );
	}

	public function render_notice(): void {
		$this->notice->render();
	}

	private static function connect(): mysqli {
		global $wpdb;
		if ( ! $wpdb instanceof wpdb || get_class( $wpdb ) !== 'wpdb' || $wpdb->is_mysql !== true || is_multisite() || ! defined( 'WP_CONTENT_DIR' ) || file_exists( WP_CONTENT_DIR . '/db.php' ) || defined( 'CUSTOM_USER_TABLE' ) || defined( 'CUSTOM_USER_META_TABLE' ) || ( defined( 'MYSQL_CLIENT_FLAGS' ) && MYSQL_CLIENT_FLAGS !== 0 ) ) { throw new RuntimeException(); }
		if ( ! is_string( $wpdb->prefix ) || ! preg_match( '/\A[A-Za-z0-9_]{1,35}\z/', $wpdb->prefix ) || $wpdb->base_prefix !== $wpdb->prefix || $wpdb->users !== $wpdb->prefix . 'users' || $wpdb->usermeta !== $wpdb->prefix . 'usermeta' ) { throw new RuntimeException(); }
		$host = defined( 'DB_HOST' ) ? (string) DB_HOST : '';
		$name = defined( 'DB_NAME' ) ? (string) DB_NAME : '';
		$user = defined( 'DB_USER' ) ? (string) DB_USER : '';
		$pass = defined( 'DB_PASSWORD' ) ? (string) DB_PASSWORD : '';
		if ( $name === '' || $user === '' || ! preg_match( '/\A([A-Za-z0-9.-]+)(?::([0-9]{1,5}))?\z/', $host, $parts ) ) { throw new RuntimeException(); }
		$port = isset( $parts[2] ) ? (int) $parts[2] : 0;
		if ( isset( $parts[2] ) && ( $port < 1 || $port > 65535 ) ) { throw new RuntimeException(); }
		$wpdb->last_error = '';
		$primary_name = $wpdb->get_var( 'SELECT DATABASE()' );
		if ( $wpdb->last_error !== '' || ! is_string( $primary_name ) || ! hash_equals( $name, $primary_name ) ) { throw new RuntimeException(); }
		$wpdb->last_error = '';
		$primary_id = $wpdb->get_var( 'SELECT CONNECTION_ID()' );
		if ( $wpdb->last_error !== '' || ! is_scalar( $primary_id ) || ! ctype_digit( (string) $primary_id ) || (int) $primary_id < 1 ) { throw new RuntimeException(); }
		$db = null;
		try {
			$db = mysqli_init();
			if ( ! $db || ! @mysqli_real_connect( $db, $parts[1], $user, $pass, $name, $port ) ) { throw new RuntimeException(); }
			$stmt = $db->prepare( 'SELECT COUNT(*) FROM information_schema.PROCESSLIST WHERE ID=? AND DB=DATABASE()' );
			if ( ! $stmt || ! $stmt->bind_param( 'i', $primary_id ) || ! $stmt->execute() || ! $stmt->bind_result( $matches ) || ! $stmt->fetch() || (int) $matches !== 1 ) { throw new RuntimeException(); }
			$stmt->close();
			return $db;
		} catch ( Throwable $error ) {
			if ( $db instanceof mysqli ) { $db->close(); }
			throw new RuntimeException( 'Deletion observer database unavailable.' );
		}
	}
}
