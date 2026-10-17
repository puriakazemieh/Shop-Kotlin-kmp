<?php
/**
 * Isolated primitives for browser sessions. No hooks, routes or migration run here.
 *
 * The caller MUST supply a dedicated, non-persistent mysqli connection with
 * automatic reconnect disabled. Sharing WordPress's wpdb connection is unsafe:
 * wpdb may reconnect and retry a query after its transaction has been lost.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

final class CB_Web_Session_Storage_Exception extends RuntimeException {}

final class CB_Web_Session_Store {
	const ABSOLUTE_TTL = 2592000;
	const IDLE_TTL = 1209600;

	private $db;
	private $families;
	private $tokens;
	private $site;
	private $secret;
	private $password_hash_for_user;
	private $clock;
	private $random;

	/**
	 * $site is a SHA-256 digest of trusted, canonical site URL and blog ID.
	 * $password_hash_for_user must fetch the current stored WP password hash,
	 * bypassing stale caches and transaction snapshots, using a separate fresh
	 * connection or a suitable locking read; null means the user no longer exists.
	 * A database error must throw rather than being mistaken for a deleted user.
	 * Optional clock/random callbacks are for trusted integration/test code only.
	 */
	public function __construct( mysqli $db, string $prefix, string $site, string $secret, callable $password_hash_for_user, ?callable $clock = null, ?callable $random = null ) {
		if ( ! preg_match( '/\A[a-zA-Z0-9_]{1,35}\z/', $prefix ) || ! preg_match( '/\A[0-9a-f]{64}\z/', $site ) || strlen( $secret ) < 32 ) {
			throw new CB_Web_Session_Storage_Exception( 'Invalid session configuration.' );
		}
		$this->db = $db;
		$this->families = $prefix . 'carmilla_web_families';
		$this->tokens = $prefix . 'carmilla_web_tokens';
		$this->site = $site;
		$this->secret = $secret;
		$this->password_hash_for_user = $password_hash_for_user;
		$this->clock = $clock ?: static function () { return time(); };
		$this->random = $random ?: static function ( int $bytes ) { return random_bytes( $bytes ); };
		$this->assert_schema();
	}

	/** Return DDL for an explicit migration; this method does not modify a database. */
	public static function schema_sql( string $prefix ): array {
		if ( ! preg_match( '/\A[a-zA-Z0-9_]{1,35}\z/', $prefix ) ) {
			throw new CB_Web_Session_Storage_Exception( 'Invalid session table prefix.' );
		}
		$families = $prefix . 'carmilla_web_families';
		$tokens = $prefix . 'carmilla_web_tokens';
		return array(
			"CREATE TABLE `{$families}` (family_id CHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL, site_id CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL, user_id BIGINT UNSIGNED NOT NULL, password_fp CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL, created_at BIGINT UNSIGNED NOT NULL, last_used BIGINT UNSIGNED NOT NULL, absolute_expiry BIGINT UNSIGNED NOT NULL, idle_expiry BIGINT UNSIGNED NOT NULL, revoked TINYINT UNSIGNED NOT NULL DEFAULT 0, PRIMARY KEY (family_id), KEY site_user (site_id, user_id), KEY expiry (absolute_expiry)) ENGINE=InnoDB",
			"CREATE TABLE `{$tokens}` (token_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL, family_id CHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL, spent TINYINT UNSIGNED NOT NULL DEFAULT 0, PRIMARY KEY (token_hash), KEY family_id (family_id)) ENGINE=InnoDB",
		);
	}

	/** @return array{credential:string,family_id:string,absolute_expiry:int}|null */
	public function create( int $user_id ): ?array {
		if ( $user_id < 1 ) {
			return null;
		}
		$now = $this->now();
		$fingerprint = $this->password_fingerprint( $user_id );
		if ( $fingerprint === null ) {
			return null;
		}
		$family = bin2hex( $this->bytes( 16 ) );
		$credential = bin2hex( $this->bytes( 32 ) );
		$digest = hash( 'sha256', $credential );
		$absolute = $now + self::ABSOLUTE_TTL;
		$idle = min( $absolute, $now + self::IDLE_TTL );
		$this->transaction( function () use ( $user_id, $fingerprint, $family, $digest, $now, $absolute, $idle ) {
			$this->write( "INSERT INTO `{$this->families}` (family_id,site_id,user_id,password_fp,created_at,last_used,absolute_expiry,idle_expiry,revoked) VALUES (?,?,?,?,?,?,?,?,0)", 'ssisiiii', array( $family, $this->site, $user_id, $fingerprint, $now, $now, $absolute, $idle ) );
			$this->write( "INSERT INTO `{$this->tokens}` (token_hash,family_id,spent) VALUES (?,?,0)", 'ss', array( $digest, $family ) );
		} );
		return array( 'credential' => $credential, 'family_id' => $family, 'absolute_expiry' => $absolute );
	}

	/** Atomically rotate once; replay invalidates the entire known family. */
	public function rotate( string $credential ): ?array {
		$digest = $this->digest( $credential );
		if ( $digest === null ) {
			return null;
		}
		return $this->transaction( function () use ( $digest ) {
			$token = $this->one( "SELECT family_id FROM `{$this->tokens}` WHERE token_hash=?", 's', array( $digest ) );
			if ( $token === null ) {
				return null;
			}
			$family = $this->lock_family( $token['family_id'] );
			if ( $family === null || ! hash_equals( $this->site, $family['site_id'] ) ) {
				return null;
			}
			// Re-read after the family lock: another transaction may have spent it.
			$token = $this->one( "SELECT spent FROM `{$this->tokens}` WHERE token_hash=? AND family_id=? FOR UPDATE", 'ss', array( $digest, $family['family_id'] ) );
			if ( $token === null ) {
				return null;
			}
			if ( (int) $token['spent'] === 1 ) {
				$this->revoke_locked( $family['family_id'] );
				return null;
			}
			$now = $this->now();
			if ( ! $this->family_is_valid( $family, $now ) ) {
				return null;
			}
			$new_credential = bin2hex( $this->bytes( 32 ) );
			$new_digest = hash( 'sha256', $new_credential );
			if ( 1 !== $this->write( "UPDATE `{$this->tokens}` SET spent=1 WHERE token_hash=? AND family_id=? AND spent=0", 'ss', array( $digest, $family['family_id'] ) ) ) {
				throw new CB_Web_Session_Storage_Exception( 'Session update failed.' );
			}
			$this->write( "INSERT INTO `{$this->tokens}` (token_hash,family_id,spent) VALUES (?,?,0)", 'ss', array( $new_digest, $family['family_id'] ) );
			$idle = min( (int) $family['absolute_expiry'], $now + self::IDLE_TTL );
			$this->write( "UPDATE `{$this->families}` SET idle_expiry=?,last_used=? WHERE family_id=?", 'iis', array( $idle, $now, $family['family_id'] ) );
			return array( 'credential' => $new_credential, 'family_id' => $family['family_id'], 'absolute_expiry' => (int) $family['absolute_expiry'] );
		} );
	}

	/** Test the backing family for every web access-token authorization. */
	public function is_active( string $family_id, int $user_id ): bool {
		if ( ! preg_match( '/\A[0-9a-f]{32}\z/', $family_id ) || $user_id < 1 ) {
			return false;
		}
		$family = $this->one( "SELECT * FROM `{$this->families}` WHERE family_id=? AND site_id=? AND user_id=?", 'ssi', array( $family_id, $this->site, $user_id ) );
		return $family !== null && $this->family_is_valid( $family, $this->now() );
	}

	/** Idempotent for unknown credentials; known spent credentials revoke too. */
	public function revoke( string $credential ): void {
		$digest = $this->digest( $credential );
		if ( $digest === null ) {
			return;
		}
		$this->transaction( function () use ( $digest ) {
			$token = $this->one( "SELECT family_id FROM `{$this->tokens}` WHERE token_hash=?", 's', array( $digest ) );
			if ( $token === null ) {
				return;
			}
			$family = $this->lock_family( $token['family_id'] );
			if ( $family !== null && hash_equals( $this->site, $family['site_id'] ) ) {
				$this->revoke_locked( $family['family_id'] );
			}
		} );
	}

	public function revoke_user( int $user_id ): void {
		if ( $user_id < 1 ) {
			return;
		}
		$this->transaction( function () use ( $user_id ) {
			$this->write( "UPDATE `{$this->families}` SET revoked=1 WHERE site_id=? AND user_id=?", 'si', array( $this->site, $user_id ) );
		} );
	}

	/** Explicit, bounded cleanup of families past the absolute expiry. */
	public function prune_expired( int $limit ): int {
		if ( $limit < 1 || $limit > 100 ) {
			throw new CB_Web_Session_Storage_Exception( 'Invalid cleanup limit.' );
		}
		$now = $this->now();
		return $this->transaction( function () use ( $now, $limit ) {
			$rows = $this->all( "SELECT family_id FROM `{$this->families}` WHERE site_id=? AND absolute_expiry<=? ORDER BY absolute_expiry LIMIT {$limit} FOR UPDATE", 'si', array( $this->site, $now ) );
			foreach ( $rows as $row ) {
				$this->write( "DELETE FROM `{$this->tokens}` WHERE family_id=?", 's', array( $row['family_id'] ) );
				$this->write( "DELETE FROM `{$this->families}` WHERE family_id=? AND site_id=? AND absolute_expiry<=?", 'ssi', array( $row['family_id'], $this->site, $now ) );
			}
			return count( $rows );
		} );
	}

	private function family_is_valid( array $family, int $now ): bool {
		if ( (int) $family['revoked'] !== 0 || $now < (int) $family['created_at'] || $now < (int) $family['last_used'] || $now >= (int) $family['absolute_expiry'] || $now >= (int) $family['idle_expiry'] ) {
			return false;
		}
		$current = $this->password_fingerprint( (int) $family['user_id'] );
		return $current !== null && hash_equals( $family['password_fp'], $current );
	}

	private function password_fingerprint( int $user_id ): ?string {
		try {
			$password_hash = call_user_func( $this->password_hash_for_user, $user_id );
		} catch ( Throwable $e ) {
			throw new CB_Web_Session_Storage_Exception( 'Password state unavailable.' );
		}
		if ( $password_hash === null ) {
			return null;
		}
		if ( ! is_string( $password_hash ) || $password_hash === '' ) {
			throw new CB_Web_Session_Storage_Exception( 'Password state unavailable.' );
		}
		return hash_hmac( 'sha256', "carmilla-web-password-v1\0" . $this->site . "\0" . $user_id . "\0" . $password_hash, $this->secret );
	}

	private function lock_family( string $family_id ): ?array {
		return $this->one( "SELECT * FROM `{$this->families}` WHERE family_id=? FOR UPDATE", 's', array( $family_id ) );
	}

	private function revoke_locked( string $family_id ): void {
		$this->write( "UPDATE `{$this->families}` SET revoked=1 WHERE family_id=? AND site_id=?", 'ss', array( $family_id, $this->site ) );
	}

	private function digest( string $credential ): ?string {
		return preg_match( '/\A[0-9a-f]{64}\z/', $credential ) ? hash( 'sha256', $credential ) : null;
	}

	private function now(): int {
		try { $now = call_user_func( $this->clock ); } catch ( Throwable $e ) { throw new CB_Web_Session_Storage_Exception( 'Clock unavailable.' ); }
		if ( ! is_int( $now ) || $now < 1 || $now > PHP_INT_MAX - self::ABSOLUTE_TTL ) {
			throw new CB_Web_Session_Storage_Exception( 'Clock unavailable.' );
		}
		return $now;
	}

	private function bytes( int $length ): string {
		try { $bytes = call_user_func( $this->random, $length ); } catch ( Throwable $e ) { throw new CB_Web_Session_Storage_Exception( 'Random source unavailable.' ); }
		if ( ! is_string( $bytes ) || strlen( $bytes ) !== $length ) {
			throw new CB_Web_Session_Storage_Exception( 'Random source unavailable.' );
		}
		return $bytes;
	}

	private function assert_schema(): void {
		$rows = $this->all( 'SELECT TABLE_NAME, ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN (?,?)', 'ss', array( $this->families, $this->tokens ) );
		$engines = array();
		foreach ( $rows as $row ) { $engines[ $row['TABLE_NAME'] ] = strtoupper( (string) $row['ENGINE'] ); }
		if ( ( $engines[ $this->families ] ?? '' ) !== 'INNODB' || ( $engines[ $this->tokens ] ?? '' ) !== 'INNODB' ) {
			throw new CB_Web_Session_Storage_Exception( 'Transactional session tables unavailable.' );
		}
		$expected = array(
			$this->families => array( 'family_id' => 'char:32', 'site_id' => 'char:64', 'user_id' => 'bigint', 'password_fp' => 'char:64', 'created_at' => 'bigint', 'last_used' => 'bigint', 'absolute_expiry' => 'bigint', 'idle_expiry' => 'bigint', 'revoked' => 'tinyint' ),
			$this->tokens => array( 'token_hash' => 'char:64', 'family_id' => 'char:32', 'spent' => 'tinyint' ),
		);
		$columns = $this->all( 'SELECT TABLE_NAME,COLUMN_NAME,DATA_TYPE,COLUMN_TYPE,CHARACTER_MAXIMUM_LENGTH,COLLATION_NAME,IS_NULLABLE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN (?,?)', 'ss', array( $this->families, $this->tokens ) );
		foreach ( $columns as $column ) {
			$table = $column['TABLE_NAME'];
			$name = $column['COLUMN_NAME'];
			if ( ! isset( $expected[ $table ][ $name ] ) ) { continue; }
			$type = strtolower( $column['DATA_TYPE'] );
			$signature = $type === 'char' ? $type . ':' . $column['CHARACTER_MAXIMUM_LENGTH'] : $type;
			if ( $signature !== $expected[ $table ][ $name ] || $column['IS_NULLABLE'] !== 'NO' || ( $type === 'char' && $column['COLLATION_NAME'] !== 'ascii_bin' ) || ( $type !== 'char' && strpos( strtolower( $column['COLUMN_TYPE'] ), 'unsigned' ) === false ) ) {
				throw new CB_Web_Session_Storage_Exception( 'Invalid session schema.' );
			}
			unset( $expected[ $table ][ $name ] );
		}
		if ( $expected[ $this->families ] || $expected[ $this->tokens ] ) {
			throw new CB_Web_Session_Storage_Exception( 'Invalid session schema.' );
		}
		$indexes = $this->all( "SELECT TABLE_NAME,COLUMN_NAME,SEQ_IN_INDEX FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN (?,?) AND INDEX_NAME='PRIMARY' AND NON_UNIQUE=0 ORDER BY SEQ_IN_INDEX", 'ss', array( $this->families, $this->tokens ) );
		$primary = array( $this->families => array(), $this->tokens => array() );
		foreach ( $indexes as $index ) { $primary[ $index['TABLE_NAME'] ][] = $index['COLUMN_NAME']; }
		if ( $primary[ $this->families ] !== array( 'family_id' ) || $primary[ $this->tokens ] !== array( 'token_hash' ) ) {
			throw new CB_Web_Session_Storage_Exception( 'Invalid session indexes.' );
		}
	}

	private function transaction( callable $callback ) {
		try {
			if ( ! $this->db->begin_transaction() ) { throw new CB_Web_Session_Storage_Exception( 'Session transaction failed.' ); }
			$result = $callback();
			if ( ! $this->db->commit() ) { throw new CB_Web_Session_Storage_Exception( 'Session transaction failed.' ); }
			return $result;
		} catch ( Throwable $e ) {
			try { $this->db->rollback(); } catch ( Throwable $ignored ) {}
			if ( $e instanceof CB_Web_Session_Storage_Exception ) { throw $e; }
			throw new CB_Web_Session_Storage_Exception( 'Session transaction failed.' );
		}
	}

	private function one( string $sql, string $types, array $params ): ?array {
		$rows = $this->all( $sql, $types, $params );
		return $rows[0] ?? null;
	}

	private function all( string $sql, string $types, array $params ): array {
		$stmt = $this->statement( $sql, $types, $params );
		try {
			$result = $stmt->get_result();
			if ( $result === false ) { throw new CB_Web_Session_Storage_Exception( 'Session query failed.' ); }
			return $result->fetch_all( MYSQLI_ASSOC );
		} catch ( Throwable $e ) {
			throw new CB_Web_Session_Storage_Exception( 'Session query failed.' );
		} finally { $stmt->close(); }
	}

	private function write( string $sql, string $types, array $params ): int {
		$stmt = $this->statement( $sql, $types, $params );
		try { return $stmt->affected_rows; } finally { $stmt->close(); }
	}

	private function statement( string $sql, string $types, array $params ): mysqli_stmt {
		try {
			$stmt = $this->db->prepare( $sql );
			if ( ! $stmt ) { throw new CB_Web_Session_Storage_Exception( 'Session query failed.' ); }
			if ( ! $stmt->bind_param( $types, ...$params ) || ! $stmt->execute() ) {
				$stmt->close();
				throw new CB_Web_Session_Storage_Exception( 'Session query failed.' );
			}
			return $stmt;
		} catch ( Throwable $e ) {
			throw new CB_Web_Session_Storage_Exception( 'Session query failed.' );
		}
	}
}
