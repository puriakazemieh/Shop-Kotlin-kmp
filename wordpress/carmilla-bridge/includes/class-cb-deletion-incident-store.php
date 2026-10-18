<?php
/** Inactive incident storage. No hooks, migrations, routes or notices. */
if ( ! defined( 'ABSPATH' ) ) { exit; }

final class CB_Deletion_Incident_Storage_Exception extends RuntimeException {}

final class CB_Deletion_Incident_Store {

	private $db;
	private $table;
	private $site;
	private $clock;

	/** Supply a dedicated non-persistent connection without automatic reconnect. */
	public function __construct( mysqli $db, string $prefix, string $site, ?callable $clock = null ) {
		if ( ! preg_match( '/\A[a-zA-Z0-9_]{1,35}\z/', $prefix ) || ! preg_match( '/\A[0-9a-f]{64}\z/', $site ) ) {
			throw new CB_Deletion_Incident_Storage_Exception( 'Incident storage unavailable.' );
		}
		$this->db = $db;
		$this->table = $prefix . 'carmilla_deletion_incidents';
		$this->site = $site;
		$this->clock = $clock ?: static function () { return time(); };
		$this->assert_schema();
	}

	/** Return DDL only; no production schema change is performed here. */
	public static function schema_sql( string $prefix ): string {
		if ( ! preg_match( '/\A[a-zA-Z0-9_]{1,35}\z/', $prefix ) ) { throw new CB_Deletion_Incident_Storage_Exception( 'Incident storage unavailable.' ); }
		$table = $prefix . 'carmilla_deletion_incidents';
		return "CREATE TABLE `{$table}` (incident_id CHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL, site_id CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL, account_id BIGINT UNSIGNED NOT NULL, incident_code VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL, recorded_at BIGINT UNSIGNED NOT NULL, expires_at BIGINT UNSIGNED NOT NULL, PRIMARY KEY (incident_id), KEY site_expiry (site_id,expires_at)) ENGINE=InnoDB";
	}

	/** Internal observer use only. TTL is explicit; this does not establish a retention policy. */
	public function record_partial_survivor( int $account_id, int $ttl ): string {
		return $this->record_survivor( $account_id, 'PARTIAL_USER_SURVIVES', $ttl );
	}

	/** Internal observer use only; no inference beyond the observed account state. */
	public function record_survivor( int $account_id, string $code, int $ttl ): string {
		if ( $account_id < 1 || ! in_array( $code, array( 'PARTIAL_USER_SURVIVES', 'USER_SURVIVES' ), true ) || $ttl < 1 || $ttl > 604800 ) { throw new CB_Deletion_Incident_Storage_Exception( 'Incident storage unavailable.' ); }
		$now = $this->now();
		try { $id = bin2hex( random_bytes( 16 ) ); } catch ( Throwable $e ) { throw new CB_Deletion_Incident_Storage_Exception( 'Incident storage unavailable.' ); }
		$stmt = $this->execute( "INSERT INTO `{$this->table}` (incident_id,site_id,account_id,incident_code,recorded_at,expires_at) VALUES (?,?,?,?,?,?)", 'ssisii', array( $id, $this->site, $account_id, $code, $now, $now + $ttl ) );
		try { if ( $stmt->affected_rows !== 1 ) { throw new CB_Deletion_Incident_Storage_Exception( 'Incident storage unavailable.' ); } } finally { $stmt->close(); }
		return $id;
	}

	/** Deny before querying. A stored reference does not prove the account still exists. */
	public function read( string $id ): array {
		if ( ! current_user_can( 'manage_options' ) ) { return array( 'status' => 'FORBIDDEN' ); }
		if ( ! preg_match( '/\A[0-9a-f]{32}\z/', $id ) ) { return array( 'status' => 'UNVERIFIED' ); }
		try {
			$now = $this->now();
			$stmt = $this->execute( "SELECT account_id,incident_code,recorded_at,expires_at FROM `{$this->table}` WHERE incident_id=? AND site_id=?", 'ss', array( $id, $this->site ) );
			try { $result = $stmt->get_result(); if ( $result === false ) { throw new CB_Deletion_Incident_Storage_Exception(); } $row = $result->fetch_assoc(); } finally { $stmt->close(); }
			if ( $row === null ) { return array( 'status' => 'MISSING' ); }
			if ( ! in_array( $row['incident_code'], array( 'PARTIAL_USER_SURVIVES', 'USER_SURVIVES' ), true ) || (int) $row['account_id'] < 1 || (int) $row['recorded_at'] < 1 || (int) $row['expires_at'] <= (int) $row['recorded_at'] || $now < (int) $row['recorded_at'] ) { return array( 'status' => 'UNVERIFIED' ); }
			if ( $now >= (int) $row['expires_at'] ) { return array( 'status' => 'EXPIRED' ); }
			return array( 'status' => 'AVAILABLE', 'account_id' => (int) $row['account_id'], 'code' => $row['incident_code'] );
		} catch ( Throwable $e ) { return array( 'status' => 'UNVERIFIED' ); }
	}

	/** Explicit capability-guarded bounded cleanup; not scheduled automatically. */
	public function prune_expired( int $limit ): int {
		if ( ! current_user_can( 'manage_options' ) || $limit < 1 || $limit > 100 ) { throw new CB_Deletion_Incident_Storage_Exception( 'Incident cleanup unavailable.' ); }
		$stmt = $this->execute( "DELETE FROM `{$this->table}` WHERE site_id=? AND expires_at<=? ORDER BY expires_at LIMIT {$limit}", 'si', array( $this->site, $this->now() ) );
		try { return $stmt->affected_rows; } finally { $stmt->close(); }
	}

	private function now(): int {
		try { $now = call_user_func( $this->clock ); } catch ( Throwable $e ) { throw new CB_Deletion_Incident_Storage_Exception( 'Incident storage unavailable.' ); }
		if ( ! is_int( $now ) || $now < 1 || $now > PHP_INT_MAX - 604800 ) { throw new CB_Deletion_Incident_Storage_Exception( 'Incident storage unavailable.' ); }
		return $now;
	}

	private function assert_schema(): void {
		$stmt = $this->execute( 'SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?', 's', array( $this->table ) );
		try { $result = $stmt->get_result(); $row = $result ? $result->fetch_assoc() : null; } finally { $stmt->close(); }
		if ( ! $row || strtoupper( (string) $row['ENGINE'] ) !== 'INNODB' ) { throw new CB_Deletion_Incident_Storage_Exception( 'Incident storage unavailable.' ); }
		$expected = array( 'incident_id' => 'char:32', 'site_id' => 'char:64', 'account_id' => 'bigint', 'incident_code' => 'varchar:32', 'recorded_at' => 'bigint', 'expires_at' => 'bigint' );
		$stmt = $this->execute( 'SELECT COLUMN_NAME,DATA_TYPE,COLUMN_TYPE,CHARACTER_MAXIMUM_LENGTH,COLLATION_NAME,IS_NULLABLE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?', 's', array( $this->table ) );
		try {
			$result = $stmt->get_result(); if ( ! $result ) { throw new CB_Deletion_Incident_Storage_Exception(); }
			while ( $column = $result->fetch_assoc() ) {
				$name = $column['COLUMN_NAME']; $type = strtolower( $column['DATA_TYPE'] );
				$signature = in_array( $type, array( 'char', 'varchar' ), true ) ? $type . ':' . $column['CHARACTER_MAXIMUM_LENGTH'] : $type;
				if ( ! isset( $expected[ $name ] ) || $signature !== $expected[ $name ] || $column['IS_NULLABLE'] !== 'NO' || ( in_array( $type, array( 'char', 'varchar' ), true ) && $column['COLLATION_NAME'] !== 'ascii_bin' ) || ( $type === 'bigint' && strpos( strtolower( $column['COLUMN_TYPE'] ), 'unsigned' ) === false ) ) { throw new CB_Deletion_Incident_Storage_Exception(); }
				unset( $expected[ $name ] );
			}
		} finally { $stmt->close(); }
		if ( $expected ) { throw new CB_Deletion_Incident_Storage_Exception( 'Incident storage unavailable.' ); }
		$stmt = $this->execute( "SELECT COLUMN_NAME FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND INDEX_NAME='PRIMARY' AND NON_UNIQUE=0 ORDER BY SEQ_IN_INDEX", 's', array( $this->table ) );
		try { $result = $stmt->get_result(); if ( ! $result ) { throw new CB_Deletion_Incident_Storage_Exception(); } $primary = array(); while ( $row = $result->fetch_assoc() ) { $primary[] = $row['COLUMN_NAME']; } } finally { $stmt->close(); }
		if ( $primary !== array( 'incident_id' ) ) { throw new CB_Deletion_Incident_Storage_Exception( 'Incident storage unavailable.' ); }
	}

	private function execute( string $sql, string $types, array $params ): mysqli_stmt {
		try {
			$stmt = $this->db->prepare( $sql );
			if ( ! $stmt ) { throw new CB_Deletion_Incident_Storage_Exception(); }
			if ( ! $stmt->bind_param( $types, ...$params ) || ! $stmt->execute() ) { $stmt->close(); throw new CB_Deletion_Incident_Storage_Exception(); }
			return $stmt;
		} catch ( Throwable $e ) { throw new CB_Deletion_Incident_Storage_Exception( 'Incident storage unavailable.' ); }
	}
}
