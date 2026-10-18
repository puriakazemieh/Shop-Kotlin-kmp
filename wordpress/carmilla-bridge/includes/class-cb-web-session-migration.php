<?php
/** Explicit, inactive initial migration. No hooks, routes or automatic execution. */
if ( ! defined( 'ABSPATH' ) ) { exit; }
require_once __DIR__ . '/class-cb-web-session-store.php';

final class CB_Web_Session_Migration_Exception extends RuntimeException {}

final class CB_Web_Session_Migration {
	private $db;
	private $prefix;

	/** Dedicated non-persistent, non-reconnecting connection; never use wpdb's connection. */
	public function __construct( mysqli $db, string $prefix ) {
		if ( ! preg_match( '/\A[a-zA-Z0-9_]{1,35}\z/', $prefix ) ) { throw new CB_Web_Session_Migration_Exception( 'Session migration unavailable.' ); }
		$this->db = $db; $this->prefix = $prefix;
	}

	/** Create version 1 only on an empty namespace; never delete, repair or downgrade. */
	public function ensure_v1(): string {
		$locked = false; $lock = '';
		try {
			$dbname = $this->scalar( 'SELECT DATABASE()' );
			if ( ! is_string( $dbname ) || $dbname === '' ) { throw new RuntimeException(); }
			$lock = 'cbm:' . substr( hash( 'sha256', $dbname . "\0" . $this->prefix ), 0, 60 );
			$stmt = $this->statement( 'SELECT GET_LOCK(?,0)', 's', array( $lock ) );
			try { $result = $stmt->get_result(); $row = $result ? $result->fetch_row() : null; } finally { $stmt->close(); }
			if ( ! is_array( $row ) || (string) $row[0] !== '1' ) { throw new RuntimeException(); }
			$locked = true;
			$names = array( $this->prefix . 'carmilla_web_families', $this->prefix . 'carmilla_web_tokens', $this->prefix . 'carmilla_web_schema' );
			$present = $this->rows( 'SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN (?,?,?)', 'sss', $names );
			if ( count( $present ) === 0 ) {
				foreach ( CB_Web_Session_Store::schema_sql( $this->prefix ) as $sql ) { if ( ! $this->db->query( $sql ) ) { throw new RuntimeException(); } }
				$table = $names[2];
				if ( ! $this->db->query( "CREATE TABLE `{$table}` (id TINYINT UNSIGNED NOT NULL, version SMALLINT UNSIGNED NOT NULL, PRIMARY KEY (id)) ENGINE=InnoDB" ) ) { throw new RuntimeException(); }
				$this->verify_schema( $names );
				if ( ! $this->db->query( "INSERT INTO `{$table}` (id,version) VALUES (1,1)" ) ) { throw new RuntimeException(); }
				$this->verify_marker( $table );
				return 'INSTALLED';
			}
			if ( count( $present ) !== 3 ) { throw new RuntimeException(); }
			$this->verify_schema( $names );
			$this->verify_marker( $names[2] );
			return 'CURRENT';
		} catch ( Throwable $e ) {
			throw new CB_Web_Session_Migration_Exception( 'Session migration unavailable; preserve data and inspect recovery procedure.' );
		} finally {
			if ( $locked ) {
				try {
					$stmt = $this->statement( 'SELECT RELEASE_LOCK(?)', 's', array( $lock ) );
					try { $result = $stmt->get_result(); $row = $result ? $result->fetch_row() : null; } finally { $stmt->close(); }
					if ( ! is_array( $row ) || (string) $row[0] !== '1' ) { throw new RuntimeException(); }
				} catch ( Throwable $e ) { throw new CB_Web_Session_Migration_Exception( 'Session migration unavailable; preserve data and inspect recovery procedure.' ); }
			}
		}
	}

	private function verify_schema( array $names ): void {
		$tables = $this->rows( 'SELECT TABLE_NAME,ENGINE,TABLE_TYPE FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN (?,?,?)', 'sss', $names );
		$engines = array(); foreach ( $tables as $row ) { $engines[$row['TABLE_NAME']] = array( strtoupper( (string) $row['ENGINE'] ), $row['TABLE_TYPE'] ); }
		foreach ( $names as $name ) { if ( ( $engines[$name] ?? null ) !== array( 'INNODB', 'BASE TABLE' ) ) { throw new RuntimeException(); } }
		// Reuse the session store's validation, without creating a session or retaining this instance.
		new CB_Web_Session_Store( $this->db, $this->prefix, str_repeat( 'a', 64 ), str_repeat( 'b', 32 ), static function () { return null; } );
		$columns = $this->rows( 'SELECT TABLE_NAME,COLUMN_NAME,DATA_TYPE,COLUMN_TYPE,IS_NULLABLE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN (?,?,?)', 'sss', $names );
		$counts = array_fill_keys( $names, 0 ); $version_columns = array();
		foreach ( $columns as $row ) { ++$counts[$row['TABLE_NAME']]; if ( $row['TABLE_NAME'] === $names[2] ) { $version_columns[$row['COLUMN_NAME']] = array( strtolower( $row['DATA_TYPE'] ), strtolower( $row['COLUMN_TYPE'] ), $row['IS_NULLABLE'] ); } }
		if ( $counts[$names[0]] !== 9 || $counts[$names[1]] !== 3 || $counts[$names[2]] !== 2 || count( $version_columns ) !== 2 ) { throw new RuntimeException(); }
		foreach ( array( 'id' => 'tinyint', 'version' => 'smallint' ) as $name => $type ) {
			$column = $version_columns[$name] ?? null;
			if ( ! $column || $column[0] !== $type || strpos( $column[1], 'unsigned' ) === false || $column[2] !== 'NO' ) { throw new RuntimeException(); }
		}
		$indexes = $this->rows( 'SELECT TABLE_NAME,INDEX_NAME,NON_UNIQUE,SEQ_IN_INDEX,COLUMN_NAME,SUB_PART FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN (?,?,?) ORDER BY TABLE_NAME,INDEX_NAME,SEQ_IN_INDEX', 'sss', $names );
		$actual = array(); foreach ( $indexes as $row ) { if ( $row['SUB_PART'] !== null ) { throw new RuntimeException(); } $actual[$row['TABLE_NAME']][$row['INDEX_NAME']][] = (int) $row['NON_UNIQUE'] . ':' . $row['COLUMN_NAME']; }
		$expected = array( $names[0] => array( 'PRIMARY' => array( '0:family_id' ), 'expiry' => array( '1:absolute_expiry' ), 'site_user' => array( '1:site_id', '1:user_id' ) ), $names[1] => array( 'PRIMARY' => array( '0:token_hash' ), 'family_id' => array( '1:family_id' ) ), $names[2] => array( 'PRIMARY' => array( '0:id' ) ) );
		foreach ( $expected as $table => $wanted ) { if ( ! isset( $actual[$table] ) || $actual[$table] != $wanted ) { throw new RuntimeException(); } }
		foreach ( array( 'TRIGGERS' => 'EVENT_OBJECT_TABLE', 'KEY_COLUMN_USAGE' => 'TABLE_NAME' ) as $table => $column ) {
			$extra = $table === 'KEY_COLUMN_USAGE' ? ' AND REFERENCED_TABLE_NAME IS NOT NULL' : '';
			$rows = $this->rows( "SELECT COUNT(*) AS n FROM information_schema.{$table} WHERE " . ( $table === 'TRIGGERS' ? 'TRIGGER_SCHEMA' : 'TABLE_SCHEMA' ) . "=DATABASE() AND {$column} IN (?,?,?){$extra}", 'sss', $names );
			if ( count( $rows ) !== 1 || (int) $rows[0]['n'] !== 0 ) { throw new RuntimeException(); }
		}
	}

	private function verify_marker( string $table ): void {
		$result = $this->db->query( "SELECT id,version FROM `{$table}`" );
		if ( ! $result ) { throw new RuntimeException(); }
		$rows = $result->fetch_all( MYSQLI_ASSOC );
		if ( count( $rows ) !== 1 || (int) $rows[0]['id'] !== 1 || (int) $rows[0]['version'] !== 1 ) { throw new RuntimeException(); }
	}
	private function scalar( string $sql ) { $result = $this->db->query( $sql ); if ( ! $result ) { throw new RuntimeException(); } $row = $result->fetch_row(); return $row[0] ?? null; }
	private function rows( string $sql, string $types, array $params ): array { $stmt = $this->statement( $sql, $types, $params ); try { $result = $stmt->get_result(); if ( ! $result ) { throw new RuntimeException(); } return $result->fetch_all( MYSQLI_ASSOC ); } finally { $stmt->close(); } }
	private function statement( string $sql, string $types, array $params ): mysqli_stmt { $stmt = $this->db->prepare( $sql ); if ( ! $stmt ) { throw new RuntimeException(); } if ( ! $stmt->bind_param( $types, ...$params ) || ! $stmt->execute() ) { $stmt->close(); throw new RuntimeException(); } return $stmt; }
}
