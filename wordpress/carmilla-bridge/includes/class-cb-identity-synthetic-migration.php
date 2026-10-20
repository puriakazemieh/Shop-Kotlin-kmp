<?php
/**
 * Bounded identity-marker migration for a disposable WordPress database.
 *
 * It deliberately does not normalize credentials, repair collisions or add a
 * global uniqueness constraint.  Only exact-audit-clean cb_mobile owners are
 * marked, and every marker written by a run is journalled for a guarded
 * rollback.  No hook or route calls this class.
 */
if ( ! defined( 'ABSPATH' ) ) { exit; }

final class CB_Identity_Synthetic_Migration {
	private $db;

	public function __construct( $db = null ) {
		global $wpdb;
		$this->db = $db ?: $wpdb;
	}

	public function plan( int $limit = 10000 ): array {
		if ( ! class_exists( 'CB_Identity_Exact_Audit' ) ) { return array( 'status' => 'UNAVAILABLE' ); }
		$audit = ( new CB_Identity_Exact_Audit( $this->db ) )->scan( $limit );
		if ( ( $audit['status'] ?? '' ) !== 'EXACT_MATCH_ONLY' ) { return $audit; }
		$count = $this->target_count();
		return $count === null ? array( 'status' => 'UNAVAILABLE' ) : array( 'status' => 'READY', 'target_count' => $count );
	}

	/** Apply one generated run only after an exact, bounded, clean audit. */
	public function apply( string $run_id, int $limit = 10000 ): array {
		if ( ! preg_match( '/\A[a-f0-9]{8}-(?:[a-f0-9]{4}-){3}[a-f0-9]{12}\z/D', $run_id ) ) { return array( 'status' => 'UNSUPPORTED' ); }
		$plan = $this->plan( $limit );
		if ( ( $plan['status'] ?? '' ) !== 'READY' || ! $this->ensure_journal() ) { return $plan['status'] === 'READY' ? array( 'status' => 'UNAVAILABLE' ) : $plan; }
		$targets = $this->target_ids( $limit );
		if ( $targets === null || count( $targets ) !== (int) $plan['target_count'] ) { return array( 'status' => 'UNAVAILABLE' ); }
		if ( false === $this->db->query( 'START TRANSACTION' ) ) { return array( 'status' => 'UNAVAILABLE' ); }
		try {
			foreach ( $targets as $user_id ) {
				$prior = $this->db->get_var( $this->db->prepare( "SELECT meta_value FROM `{$this->db->usermeta}` WHERE user_id=%d AND meta_key='cb_identity_managed' ORDER BY umeta_id ASC LIMIT 1", $user_id ) );
				$prior_run = $this->db->get_var( $this->db->prepare( "SELECT meta_value FROM `{$this->db->usermeta}` WHERE user_id=%d AND meta_key='cb_identity_migration_run' ORDER BY umeta_id ASC LIMIT 1", $user_id ) );
				if ( $prior_run !== null && $prior_run !== '' ) { throw new RuntimeException( 'existing migration marker' ); }
				if ( false === $this->db->insert( $this->journal_table(), array( 'run_id' => $run_id, 'user_id' => $user_id, 'prior_managed' => $prior, 'prior_run' => $prior_run ), array( '%s', '%d', '%s', '%s' ) ) ) { throw new RuntimeException( 'journal write' ); }
				if ( $prior === null && false === $this->db->insert( $this->db->usermeta, array( 'user_id' => $user_id, 'meta_key' => 'cb_identity_managed', 'meta_value' => '1' ), array( '%d', '%s', '%s' ) ) ) { throw new RuntimeException( 'managed write' ); }
				if ( false === $this->db->insert( $this->db->usermeta, array( 'user_id' => $user_id, 'meta_key' => 'cb_identity_migration_run', 'meta_value' => $run_id ), array( '%d', '%s', '%s' ) ) ) { throw new RuntimeException( 'run write' ); }
			}
			if ( false === $this->db->query( 'COMMIT' ) ) { throw new RuntimeException( 'commit' ); }
			foreach ( $targets as $user_id ) { clean_user_cache( $user_id ); }
			return array( 'status' => 'APPLIED', 'target_count' => count( $targets ) );
		} catch ( Throwable $e ) {
			$this->db->query( 'ROLLBACK' );
			return array( 'status' => 'UNAVAILABLE' );
		}
	}

	/** Reverse only untouched markers attributable to the supplied run. */
	public function rollback( string $run_id ): array {
		if ( ! preg_match( '/\A[a-f0-9]{8}-(?:[a-f0-9]{4}-){3}[a-f0-9]{12}\z/D', $run_id ) || ! $this->ensure_journal() ) { return array( 'status' => 'UNSUPPORTED' ); }
		$rows = $this->db->get_results( $this->db->prepare( "SELECT user_id, prior_managed, prior_run FROM `{$this->journal_table()}` WHERE run_id=%s ORDER BY user_id", $run_id ), ARRAY_A );
		if ( ! is_array( $rows ) || $this->db->last_error !== '' ) { return array( 'status' => 'UNAVAILABLE' ); }
		if ( false === $this->db->query( 'START TRANSACTION' ) ) { return array( 'status' => 'UNAVAILABLE' ); }
		try {
			foreach ( $rows as $row ) {
				$user_id = (int) $row['user_id'];
				$run = $this->db->get_var( $this->db->prepare( "SELECT meta_value FROM `{$this->db->usermeta}` WHERE user_id=%d AND meta_key='cb_identity_migration_run' ORDER BY umeta_id ASC LIMIT 1", $user_id ) );
				if ( ! hash_equals( $run_id, (string) $run ) ) { throw new RuntimeException( 'marker changed' ); }
				if ( $row['prior_managed'] === null ) {
					if ( false === $this->db->query( $this->db->prepare( "DELETE FROM `{$this->db->usermeta}` WHERE user_id=%d AND meta_key='cb_identity_managed' AND meta_value='1'", $user_id ) ) ) { throw new RuntimeException( 'managed delete' ); }
				}
				if ( false === $this->db->query( $this->db->prepare( "DELETE FROM `{$this->db->usermeta}` WHERE user_id=%d AND meta_key='cb_identity_migration_run' AND meta_value=%s", $user_id, $run_id ) ) ) { throw new RuntimeException( 'run delete' ); }
			}
			if ( false === $this->db->query( $this->db->prepare( "DELETE FROM `{$this->journal_table()}` WHERE run_id=%s", $run_id ) ) || false === $this->db->query( 'COMMIT' ) ) { throw new RuntimeException( 'commit' ); }
			foreach ( $rows as $row ) { clean_user_cache( (int) $row['user_id'] ); }
			return array( 'status' => 'ROLLED_BACK', 'target_count' => count( $rows ) );
		} catch ( Throwable $e ) {
			$this->db->query( 'ROLLBACK' );
			return array( 'status' => 'UNAVAILABLE' );
		}
	}

	private function target_count() {
		$value = $this->db->get_var( "SELECT COUNT(DISTINCT user_id) FROM `{$this->db->usermeta}` WHERE meta_key='cb_mobile'" );
		return $this->db->last_error === '' && $value !== null ? (int) $value : null;
	}
	private function target_ids( int $limit ) {
		$rows = $this->db->get_col( $this->db->prepare( "SELECT DISTINCT user_id FROM `{$this->db->usermeta}` WHERE meta_key='cb_mobile' ORDER BY user_id LIMIT %d", $limit ) );
		return is_array( $rows ) && $this->db->last_error === '' ? array_map( 'intval', $rows ) : null;
	}
	private function journal_table(): string { return $this->db->prefix . 'cb_identity_migration_journal'; }
	private function ensure_journal(): bool {
		$table = $this->journal_table();
		if ( ! preg_match( '/\A[A-Za-z0-9_]+\z/', $table ) ) { return false; }
		$sql = "CREATE TABLE IF NOT EXISTS `{$table}` (run_id char(36) NOT NULL, user_id bigint(20) unsigned NOT NULL, prior_managed longtext NULL, prior_run longtext NULL, PRIMARY KEY (run_id,user_id)) ENGINE=InnoDB";
		return false !== $this->db->query( $sql );
	}
}
