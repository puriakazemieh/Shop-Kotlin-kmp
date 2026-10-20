<?php
/**
 * Supported WordPress identity writer.
 *
 * This deliberately uses normal WordPress writers: it installs no trigger and
 * no global hook.  It therefore cannot make raw SQL or third-party writers
 * safe.  Instead it serializes Carmilla's own registration calls, verifies the
 * committed rows after WordPress callbacks run, and never lets its caller issue
 * credentials when that verification is inconclusive.
 */
if ( ! defined( 'ABSPATH' ) ) { exit; }

final class CB_Identity_Supported_Writer {
	private $db;

	public function __construct( $db = null ) {
		global $wpdb;
		$this->db = $db ?: $wpdb;
	}

	/**
	 * Create a Carmilla customer only after exact-byte identity checks.
	 * Returns a positive user id or WP_Error; it never returns a misleading zero.
	 */
	public function create_customer( string $login, string $email, string $mobile, string $password ) {
		$claims = $this->claims( $login, $email, $mobile );
		if ( is_wp_error( $claims ) || $password === '' ) {
			return is_wp_error( $claims ) ? $claims : new WP_Error( 'cb_identity_invalid', 'Account details are unavailable.' );
		}
		$locks = $this->acquire( $claims );
		if ( is_wp_error( $locks ) ) { return $locks; }
		try {
			if ( $this->owners( $login, $email, $mobile ) !== array() ) {
				return new WP_Error( 'cb_identity_exists', 'An account already uses one of these details.' );
			}
			$user_id = wp_insert_user( array(
				'user_login' => $login,
				'user_email' => $email,
				'user_pass'  => $password,
				'role'       => 'customer',
			), true );
			if ( is_wp_error( $user_id ) || (int) $user_id < 1 ) {
				return is_wp_error( $user_id ) ? $user_id : new WP_Error( 'cb_identity_write_failed', 'Account creation could not be verified.' );
			}
			$user_id = (int) $user_id;
			if ( $mobile !== '' && ! add_user_meta( $user_id, 'cb_mobile', $mobile, true ) ) {
				$this->mark_recovery( $user_id );
				return new WP_Error( 'cb_identity_recovery_required', 'Account creation requires recovery.' );
			}
			if ( ! update_user_meta( $user_id, 'cb_identity_managed', '1' ) ) {
				$this->mark_recovery( $user_id );
				return new WP_Error( 'cb_identity_recovery_required', 'Account creation requires recovery.' );
			}
			if ( ! $this->matches_source( $user_id, $login, $email, $mobile ) || $this->owners( $login, $email, $mobile ) !== array( $user_id ) ) {
				// Callbacks may have had irreversible effects.  Do not delete the
				// account or claim rollback; retain a minimal local recovery marker.
				$this->mark_recovery( $user_id );
				return new WP_Error( 'cb_identity_recovery_required', 'Account creation requires recovery.' );
			}
			return $user_id;
		} finally {
			$this->release( $locks );
		}
	}

	/** Returns exactly one owner for a supported identifier, otherwise null. */
	public function unique_owner( string $identifier ) {
		$result = $this->lookup( $identifier );
		return $result['status'] === 'UNIQUE' ? $result['user_id'] : null;
	}

	/** Aggregate lookup state; callers must not treat a conflict as no account. */
	public function lookup( string $identifier ): array {
		$claims = $this->claims( $identifier, $identifier, $identifier );
		if ( is_wp_error( $claims ) ) { return array( 'status' => 'UNSUPPORTED' ); }
		$owners = $this->owners( $identifier, $identifier, $identifier );
		if ( $owners === array( -1 ) ) { return array( 'status' => 'UNAVAILABLE' ); }
		if ( count( $owners ) === 0 ) { return array( 'status' => 'NONE' ); }
		if ( count( $owners ) === 1 ) { return array( 'status' => 'UNIQUE', 'user_id' => $owners[0] ); }
		return array( 'status' => 'CONFLICT' );
	}

	private function claims( string $login, string $email, string $mobile ) {
		$claims = array();
		foreach ( array( $login, $email, $mobile ) as $claim ) {
			if ( $claim === '' ) { continue; }
			if ( ! preg_match( '/\A[A-Za-z0-9_.@+\-]{1,191}\z/D', $claim ) ) {
				return new WP_Error( 'cb_identity_invalid', 'Account details are unavailable.' );
			}
			$claims[ $claim ] = true;
		}
		return array_keys( $claims );
	}

	private function acquire( array $claims ) {
		if ( ! isset( $this->db->users, $this->db->usermeta ) || ! preg_match( '/\A[A-Za-z0-9_]+\z/', $this->db->users ) || ! preg_match( '/\A[A-Za-z0-9_]+\z/', $this->db->usermeta ) ) {
			return new WP_Error( 'cb_identity_unavailable', 'Account creation is unavailable.' );
		}
		$locks = array();
		sort( $claims, SORT_STRING );
		foreach ( $claims as $claim ) {
			$lock = 'cbid:' . substr( hash( 'sha256', $claim ), 0, 56 );
			$result = $this->db->get_var( $this->db->prepare( 'SELECT GET_LOCK(%s, 5)', $lock ) );
			if ( (string) $result !== '1' ) {
				$this->release( $locks );
				return new WP_Error( 'cb_identity_busy', 'Account creation is temporarily unavailable.' );
			}
			$locks[] = $lock;
		}
		return $locks;
	}

	private function release( array $locks ): void {
		foreach ( array_reverse( $locks ) as $lock ) {
			$this->db->get_var( $this->db->prepare( 'SELECT RELEASE_LOCK(%s)', $lock ) );
		}
	}

	private function owners( string $login, string $email, string $mobile ): array {
		$sql = "SELECT DISTINCT u.ID FROM `{$this->db->users}` u LEFT JOIN `{$this->db->usermeta}` m ON m.user_id=u.ID AND BINARY m.meta_key=BINARY 'cb_mobile'
			WHERE BINARY u.user_login=BINARY %s OR (%s<>'' AND BINARY u.user_email=BINARY %s) OR (%s<>'' AND BINARY m.meta_value=BINARY %s) ORDER BY u.ID LIMIT 3";
		try {
			$rows = $this->db->get_col( $this->db->prepare( $sql, $login, $email, $email, $mobile, $mobile ) );
		} catch ( Throwable $e ) { return array( -1 ); }
		if ( ! is_array( $rows ) || $this->db->last_error !== '' ) { return array( -1 ); }
		return array_map( 'intval', $rows );
	}

	private function matches_source( int $user_id, string $login, string $email, string $mobile ): bool {
		$user = get_user_by( 'id', $user_id );
		if ( ! $user || ! hash_equals( $login, (string) $user->user_login ) || ! hash_equals( $email, (string) $user->user_email ) ) { return false; }
		if ( $mobile === '' ) { return true; }
		$values = get_user_meta( $user_id, 'cb_mobile', false );
		return count( $values ) === 1 && is_string( $values[0] ) && hash_equals( $mobile, $values[0] );
	}

	private function mark_recovery( int $user_id ): void {
		// Marker only: no identifier or request data is persisted here.
		update_user_meta( $user_id, 'cb_identity_writer_state', 'needs_recovery' );
	}
}
