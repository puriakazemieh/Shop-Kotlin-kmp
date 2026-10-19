<?php
/** Inactive, bounded synthetic exact-byte research audit. Not a migration gate. */
if ( ! defined( 'ABSPATH' ) ) { exit; }

final class CB_Identity_Exact_Audit {

	private $db;
	public function __construct( $db ) { $this->db = $db; }

	/** Return aggregate counts only; unsupported or partial scans must never be accepted. */
	public function scan( int $limit = 10000 ): array {
		if ( $limit < 1 || $limit > 10000 || ! isset( $this->db->users, $this->db->usermeta ) ||
			! preg_match( '/\A[A-Za-z0-9_]+\z/', $this->db->users ) ||
			! preg_match( '/\A[A-Za-z0-9_]+\z/', $this->db->usermeta ) ) {
			return array( 'status' => 'UNSUPPORTED' );
		}
		// A single SELECT observes the database statement's snapshot; no writes or locks.
		$sql = "SELECT identity_value, owner_id, owner_exists FROM (
			SELECT user_login AS identity_value, ID AS owner_id, 1 AS owner_exists FROM `{$this->db->users}`
			UNION ALL SELECT user_email, ID, 1 FROM `{$this->db->users}` WHERE user_email <> ''
			UNION ALL SELECT m.meta_value, m.user_id, IF(u.ID IS NULL, 0, 1)
			FROM `{$this->db->usermeta}` m LEFT JOIN `{$this->db->users}` u ON u.ID = m.user_id
			WHERE BINARY m.meta_key = BINARY %s
		) AS identity_sources ORDER BY BINARY identity_value, owner_id LIMIT %d";
		try {
			$previous = $this->db->suppress_errors( true );
			try {
				$rows = $this->db->get_results( $this->db->prepare( $sql, 'cb_mobile', $limit + 1 ), ARRAY_A );
				$failed = $this->db->last_error !== '' || ! is_array( $rows );
			} finally {
				$this->db->suppress_errors( $previous );
			}
		} catch ( Throwable $e ) { return array( 'status' => 'UNAVAILABLE' ); }
		if ( $failed ) { return array( 'status' => 'UNAVAILABLE' ); }
		if ( count( $rows ) > $limit ) { return array( 'status' => 'INCOMPLETE' ); }

		$unsupported = 0; $conflicts = 0; $aliases = 0;
		$key = null; $first_owner = null; $previous_owner = null; $conflict = false; $alias = false;
		foreach ( $rows as $row ) {
			$value = $row['identity_value'] ?? null; $owner = $row['owner_id'] ?? null;
			$owner_integer = is_string( $owner ) || is_int( $owner ) ? filter_var( $owner, FILTER_VALIDATE_INT, array( 'options' => array( 'min_range' => 1 ) ) ) : false;
			if ( ! is_string( $value ) || ! preg_match( '/\A[A-Za-z0-9_.@+\-]{1,190}\z/D', $value ) ||
				$owner_integer === false || ! in_array( $row['owner_exists'] ?? null, array( 1, '1' ), true ) ) {
				++$unsupported; continue;
			}
			$owner = $owner_integer;
			if ( $key !== $value ) {
				if ( $conflict ) { ++$conflicts; } if ( $alias ) { ++$aliases; }
				$key = $value; $first_owner = $owner; $previous_owner = null; $conflict = false; $alias = false;
			}
			if ( $owner !== $first_owner ) { $conflict = true; }
			if ( $owner === $previous_owner ) { $alias = true; }
			$previous_owner = $owner;
		}
		if ( $conflict ) { ++$conflicts; } if ( $alias ) { ++$aliases; }
		return array(
			'status' => $unsupported ? 'UNSUPPORTED' : ( $conflicts ? 'CONFLICT' : 'EXACT_MATCH_ONLY' ),
			'unsupported_rows' => $unsupported, 'conflicting_keys' => $conflicts, 'same_owner_alias_keys' => $aliases,
		);
	}
}
