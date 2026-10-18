<?php
/** Inactive lifecycle observer. Does not register hooks, routes or migrations. */
if ( ! defined( 'ABSPATH' ) ) { exit; }
require_once __DIR__ . '/class-cb-deletion-incident-store.php';

final class CB_Deletion_Incident_Observer {
	private $db;
	private $users;
	private $meta;
	private $store;
	private $ttl;
	private $key;
	private $before = array();

	/** Dedicated non-persistent, non-reconnecting connection; table names from trusted WP config. */
	public function __construct( mysqli $db, string $users, string $meta, CB_Deletion_Incident_Store $store, int $ttl ) {
		if ( ! preg_match( '/\A[a-zA-Z0-9_]{1,64}\z/', $users ) || ! preg_match( '/\A[a-zA-Z0-9_]{1,64}\z/', $meta ) || $ttl < 1 || $ttl > 604800 ) { throw new InvalidArgumentException( 'Observer configuration unavailable.' ); }
		$this->db = $db; $this->users = $users; $this->meta = $meta; $this->store = $store; $this->ttl = $ttl;
		try { $this->key = random_bytes( 32 ); } catch ( Throwable $e ) { throw new RuntimeException( 'Observer unavailable.' ); }
	}

	/** Internal lifecycle call; false means the snapshot is not trustworthy. */
	public function begin( int $account_id ): bool {
		if ( $account_id < 1 ) { return false; }
		if ( array_key_exists( $account_id, $this->before ) ) { unset( $this->before[ $account_id ] ); return false; }
		try {
			if ( ! $this->exists( $account_id ) ) { return false; }
			$this->before[ $account_id ] = $this->metadata_digest( $account_id );
			return true;
		} catch ( Throwable $e ) { return false; }
	}

	/** Internal lifecycle call; never asserts that deletion or external effects succeeded. */
	public function after( int $account_id ): array {
		if ( ! array_key_exists( $account_id, $this->before ) ) { return array( 'status' => 'UNVERIFIED' ); }
		$before = $this->before[ $account_id ]; unset( $this->before[ $account_id ] );
		try {
			if ( ! $this->exists( $account_id ) ) { return array( 'status' => 'NO_SURVIVING_USER_OBSERVED' ); }
			$after = $this->metadata_digest( $account_id );
			$code = hash_equals( $before, $after ) ? 'USER_SURVIVES' : 'PARTIAL_USER_SURVIVES';
			$id = $this->store->record_survivor( $account_id, $code, $this->ttl );
			return array( 'status' => 'RECORDED', 'incident_id' => $id );
		} catch ( Throwable $e ) { return array( 'status' => 'UNVERIFIED' ); }
	}

	private function exists( int $account_id ): bool {
		$stmt = $this->prepare( "SELECT COUNT(*) FROM `{$this->users}` WHERE ID=?", $account_id );
		try { if ( ! $stmt->bind_result( $count ) || ! $stmt->fetch() || (int) $count > 1 ) { throw new RuntimeException(); } return (int) $count === 1; } finally { $stmt->close(); }
	}

	/** Raw metadata exists only while streaming a row; only its keyed digest is retained. */
	private function metadata_digest( int $account_id ): string {
		$stmt = $this->prepare( "SELECT umeta_id,meta_key,meta_value FROM `{$this->meta}` WHERE user_id=? ORDER BY umeta_id", $account_id );
		try {
			if ( ! $stmt->bind_result( $id, $key, $value ) ) { throw new RuntimeException(); }
			$hash = hash_init( 'sha256', HASH_HMAC, $this->key ); $count = 0;
			while ( true ) {
				$fetched = $stmt->fetch();
				if ( $fetched === null ) { break; }
				if ( $fetched !== true ) { throw new RuntimeException(); }
				if ( ++$count > 10000 || ( $key !== null && strlen( $key ) > 1048576 ) || ( $value !== null && strlen( $value ) > 1048576 ) ) { throw new RuntimeException(); }
				foreach ( array( (string) $id, $key, $value ) as $part ) {
					if ( $part === null ) { hash_update( $hash, "\x00" ); continue; }
					hash_update( $hash, "\x01" . pack( 'N', strlen( $part ) ) . $part );
				}
			}
			return hash_final( $hash );
		} finally { $stmt->close(); }
	}

	private function prepare( string $sql, int $account_id ): mysqli_stmt {
		try {
			$stmt = $this->db->prepare( $sql );
			if ( ! $stmt ) { throw new RuntimeException(); }
			if ( ! $stmt->bind_param( 'i', $account_id ) || ! $stmt->execute() ) { $stmt->close(); throw new RuntimeException(); }
			return $stmt;
		} catch ( Throwable $e ) { throw new RuntimeException( 'Observer unavailable.' ); }
	}
}
