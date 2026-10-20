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
		$host = defined( 'DB_HOST' ) ? (string) DB_HOST : '';
		$name = defined( 'DB_NAME' ) ? (string) DB_NAME : '';
		$user = defined( 'DB_USER' ) ? (string) DB_USER : '';
		$pass = defined( 'DB_PASSWORD' ) ? (string) DB_PASSWORD : '';
		if ( $host === '' || $name === '' || $user === '' ) { throw new RuntimeException(); }
		$port = null;
		if ( preg_match( '/^([^:]+):(\\d+)$/', $host, $matches ) ) { $host = $matches[1]; $port = (int) $matches[2]; }
		$db = mysqli_init();
		if ( ! $db || ! @mysqli_real_connect( $db, $host, $user, $pass, $name, $port ?: 0 ) ) { throw new RuntimeException(); }
		return $db;
	}
}
