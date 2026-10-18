<?php
/** Disposable research fixture. Not a production recovery mechanism. */
require __DIR__ . '/experiment-identity-shared-references.php';

function recovery_read( $user_id, $lock = false ) {
	global $wpdb, $prefix;
	$suffix = $lock ? ' FOR UPDATE' : '';
	$queries = array(
		'user' => $wpdb->prepare( "SELECT * FROM {$wpdb->users} WHERE ID=%d$suffix", $user_id ),
		'meta' => $wpdb->prepare( "SELECT * FROM {$wpdb->usermeta} WHERE user_id=%d ORDER BY umeta_id$suffix", $user_id ),
		'claims' => $wpdb->prepare( "SELECT * FROM `{$prefix}identity_claims` WHERE owner_id=%d ORDER BY claim_key$suffix", $user_id ),
		'refs' => $wpdb->prepare( "SELECT * FROM `{$prefix}identity_refs` WHERE owner_id=%d ORDER BY source_kind,source_id$suffix", $user_id ),
	);
	$state = array();
	foreach ( $queries as $key => $sql ) {
		$state[ $key ] = $wpdb->get_results( $sql, ARRAY_A );
		if ( $wpdb->last_error !== '' || ! is_array( $state[ $key ] ) ) { throw new RuntimeException( 'read' ); }
	}
	return $state;
}

function recovery_bundle( $id, $before, $after ) {
	global $recovery_fixture_key;
	$body = array( 'id' => $id, 'before' => $before, 'after' => $after );
	return array( 'body' => $body, 'digest' => hash_hmac( 'sha256', serialize( $body ), $recovery_fixture_key ) );
}

function recovery_attempt( $bundle, $after_lock = null ) {
	global $wpdb, $recovery_fixture_key;
	// Ephemeral fixture seal only: no persisted keys, operator authorization or production trust model.
	if ( ! is_array( $bundle ) || ! isset( $bundle['body'], $bundle['digest'] ) || ! is_string( $bundle['digest'] ) ||
		! hash_equals( $bundle['digest'], hash_hmac( 'sha256', serialize( $bundle['body'] ), $recovery_fixture_key ) ) ) { return 'INVALID_SNAPSHOT'; }
	$body = $bundle['body'];
	if ( ! isset( $body['id'], $body['before'], $body['after'] ) || ! is_int( $body['id'] ) || $body['id'] < 1 ) { return 'INVALID_SNAPSHOT'; }
	foreach ( array( 'before', 'after' ) as $part ) {
		foreach ( array( 'user', 'meta', 'claims', 'refs' ) as $key ) {
			if ( ! isset( $body[ $part ][ $key ] ) || ! is_array( $body[ $part ][ $key ] ) ) { return 'INVALID_SNAPSHOT'; }
		}
		if ( count( $body[ $part ]['user'] ) !== 1 || (int) $body[ $part ]['user'][0]['ID'] !== $body['id'] ||
			strpos( $body[ $part ]['user'][0]['user_login'], 'recovery_' ) !== 0 ) { return 'INVALID_SNAPSHOT'; }
	}
	$before = $body['before']; $after = $body['after'];
	// Supported case: the user and all claims survive unchanged; only non-identity metadata disappeared.
	if ( $before['user'] !== $after['user'] || $before['claims'] !== $after['claims'] || $before['refs'] !== $after['refs'] ) { return 'UNSUPPORTED'; }
	$original = array();
	foreach ( $before['meta'] as $row ) {
		if ( ! isset( $row['umeta_id'], $row['user_id'], $row['meta_key'] ) || (int) $row['user_id'] !== $body['id'] ) { return 'INVALID_SNAPSHOT'; }
		$original[ $row['umeta_id'] ] = $row;
	}
	$missing = $original;
	foreach ( $after['meta'] as $row ) {
		if ( ! isset( $row['umeta_id'], $original[ $row['umeta_id'] ] ) || $original[ $row['umeta_id'] ] !== $row ) { return 'UNSUPPORTED'; }
		unset( $missing[ $row['umeta_id'] ] );
	}
	foreach ( $missing as $row ) {
		if ( $row['meta_key'] === 'cb_mobile' ) { return 'UNSUPPORTED'; }
	}
	if ( ! $missing ) { return 'UNSUPPORTED'; }
	if ( $wpdb->query( 'START TRANSACTION' ) === false ) { return 'INDETERMINATE'; }
	try {
		$current = recovery_read( $body['id'], true );
		if ( $current === $before ) { $status = 'ALREADY_RESTORED'; }
		elseif ( $current !== $after ) { $status = 'STALE'; }
		elseif ( $after_lock && ! $after_lock( $body['id'] ) ) { $status = 'RACE_UNSAFE'; }
		else {
			$status = 'RESTORED';
			foreach ( $missing as $row ) {
				// Explicit original primary key: never update or replace any existing row.
				if ( $wpdb->insert( $wpdb->usermeta, $row ) !== 1 ) { $status = 'INSERT_FAILED'; break; }
			}
			if ( $status === 'RESTORED' && recovery_read( $body['id'], true ) !== $before ) { $status = 'VERIFY_FAILED'; }
		}
		if ( $status === 'RESTORED' ) {
			if ( $wpdb->query( 'COMMIT' ) === false ) { return 'INDETERMINATE'; }
			wp_cache_delete( $body['id'], 'user_meta' );
			return $status;
		}
		return $wpdb->query( 'ROLLBACK' ) === false ? 'INDETERMINATE' : $status;
	} catch ( Throwable $error ) {
		return $wpdb->query( 'ROLLBACK' ) === false ? 'INDETERMINATE' : 'ERROR';
	}
}

function recovery_incident( $name, $mobile ) {
	global $wpdb;
	$id = shared_user( 'recovery_' . $name, 'recovery_' . $name . '@example.test' );
	shared_check( (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->posts} WHERE post_author=%d", $id ) ) === 0 &&
		(int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->comments} WHERE user_id=%d", $id ) ) === 0, 'fixture user has no posts or comments: ' . $name );
	add_user_meta( $id, 'qa_restore_marker', 'synthetic-marker' );
	$mobile_id = add_user_meta( $id, 'cb_mobile', $mobile );
	$before = recovery_read( $id );
	$wpdb->query( $wpdb->prepare( 'SET @recovery_fault_mid=%d', $mobile_id ) );
	$reported = wp_delete_user( $id );
	$wpdb->query( 'SET @recovery_fault_mid=NULL' );
	$after = recovery_read( $id );
	shared_check( $reported === true && count( $after['user'] ) === 1 && count( $after['meta'] ) < count( $before['meta'] ) &&
		$before['claims'] === $after['claims'] && $before['refs'] === $after['refs'], 'native false success and supported partial state: ' . $name );
	return recovery_bundle( $id, $before, $after );
}

try {
	global $wpdb, $prefix, $wp_version;
	$recovery_fixture_key = random_bytes( 32 );
	shared_check( $wpdb->query( 'SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ' ) !== false, 'fixture isolation level' );
	$trigger = "CREATE TRIGGER `{$prefix}recovery_fault_bd` BEFORE DELETE ON `{$prefix}usermeta` FOR EACH ROW BEGIN IF @recovery_fault_mid=OLD.umeta_id THEN SIGNAL SQLSTATE '45000'; END IF; END";
	shared_check( $wpdb->query( $trigger ) !== false, 'create synthetic delete fault' );
	$success = recovery_incident( 'success', '09128880001' );
	$id = $success['body']['id']; $post_fault = recovery_read( $id );
	shared_check( recovery_attempt( null ) === 'INVALID_SNAPSHOT' && recovery_read( $id ) === $post_fault, 'missing snapshot refused without writes' );
	$incomplete = $success; unset( $incomplete['body']['before']['meta'] );
	shared_check( recovery_attempt( $incomplete ) === 'INVALID_SNAPSHOT' && recovery_read( $id ) === $post_fault, 'incomplete snapshot refused without writes' );
	$incomplete['digest'] = hash_hmac( 'sha256', serialize( $incomplete['body'] ), $recovery_fixture_key );
	shared_check( recovery_attempt( $incomplete ) === 'INVALID_SNAPSHOT' && recovery_read( $id ) === $post_fault, 'missing required snapshot section refused even with recomputed digest' );
	$truncated = $success; array_shift( $truncated['body']['before']['meta'] );
	shared_check( recovery_attempt( $truncated ) === 'INVALID_SNAPSHOT' && recovery_read( $id ) === $post_fault, 'truncated metadata snapshot refused without writes' );

	// An independent writer attempts an insert while the target metadata range is locked.
	$race = static function ( $locked_id ) use ( $wpdb ) {
		$conn = new mysqli( '127.0.0.1', 'synthetic_qa', 'synthetic_qa_password', 'carmilla_login_qa', 3306 );
		$conn->query( 'SET SESSION innodb_lock_wait_timeout=1' );
		mysqli_report( MYSQLI_REPORT_OFF );
		$stmt = $conn->prepare( "INSERT INTO {$wpdb->usermeta} (user_id,meta_key,meta_value) VALUES (?, 'qa_concurrent', 'synthetic-writer')" );
		$stmt->bind_param( 'i', $locked_id );
		$ok = $stmt->execute(); $errno = $stmt->errno;
		$stmt->close(); $conn->close();
		return ! $ok && $errno === 1205;
	};
	shared_check( recovery_attempt( $success, $race ) === 'RESTORED' && recovery_read( $id ) === $success['body']['before'], 'locked independent writer times out and exact metadata plus identity references restored' );
	shared_check( get_user_meta( $id, 'qa_restore_marker', true ) === 'synthetic-marker', 'restored marker visible through WordPress metadata API' );
	shared_check( recovery_attempt( $success ) === 'ALREADY_RESTORED' && recovery_read( $id ) === $success['body']['before'], 'repeat recovery is a no-write no-op' );
	$conn = new mysqli( '127.0.0.1', 'synthetic_qa', 'synthetic_qa_password', 'carmilla_login_qa', 3306 );
	$stmt = $conn->prepare( "INSERT INTO {$wpdb->usermeta} (user_id,meta_key,meta_value) VALUES (?, 'qa_concurrent', 'synthetic-after-commit')" );
	$stmt->bind_param( 'i', $id ); $written = $stmt->execute(); $stmt->close(); $conn->close();
	shared_check( $written && shared_meta_count( $id, 'qa_concurrent' ) === 1, 'independent writer can retry after commit' );

	$stale = recovery_incident( 'stale', '09128880002' ); $stale_id = $stale['body']['id'];
	$conn = new mysqli( '127.0.0.1', 'synthetic_qa', 'synthetic_qa_password', 'carmilla_login_qa', 3306 );
	$stmt = $conn->prepare( "INSERT INTO {$wpdb->usermeta} (user_id,meta_key,meta_value) VALUES (?, 'qa_concurrent', 'synthetic-other-writer')" );
	$stmt->bind_param( 'i', $stale_id ); $written = $stmt->execute(); $stmt->close(); $conn->close();
	$changed = recovery_read( $stale_id );
	shared_check( $written && recovery_attempt( $stale ) === 'STALE' && recovery_read( $stale_id ) === $changed, 'changed state by independent writer refused and preserved' );
	$stale_user = recovery_incident( 'stale_user', '09128880004' ); $stale_user_id = $stale_user['body']['id'];
	$written = $wpdb->update( $wpdb->users, array( 'display_name' => 'Synthetic changed name' ), array( 'ID' => $stale_user_id ) );
	$changed = recovery_read( $stale_user_id );
	shared_check( $written === 1 && recovery_attempt( $stale_user ) === 'STALE' && recovery_read( $stale_user_id ) === $changed, 'changed user record refused and preserved' );

	$collision = recovery_incident( 'collision', '09128880003' ); $collision_id = $collision['body']['id'];
	$before_ids = array_column( $collision['body']['before']['meta'], null, 'umeta_id' );
	$after_ids = array_column( $collision['body']['after']['meta'], null, 'umeta_id' );
	$missing_ids = array_diff_key( $before_ids, $after_ids ); $occupied_id = (int) array_key_last( $missing_ids );
	shared_check( count( $missing_ids ) > 1, 'collision exercises rollback after earlier metadata inserts' );
	$other = shared_user( 'recovery_other', 'recovery_other@example.test' );
	shared_check( $wpdb->insert( $wpdb->usermeta, array( 'umeta_id' => $occupied_id, 'user_id' => $other, 'meta_key' => 'qa_other', 'meta_value' => 'synthetic-other' ) ) === 1, 'occupy missing metadata id for conflict test' );
	$target_before = recovery_read( $collision_id ); $other_before = recovery_read( $other );
	shared_check( recovery_attempt( $collision ) === 'INSERT_FAILED' && recovery_read( $collision_id ) === $target_before && recovery_read( $other ) === $other_before, 'primary-key collision rolls back without overwriting either account' );
	echo 'PASS bounded disposable recovery matrix WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n";
	echo "Synthetic in-memory research only; no production recovery or external side effects tested.\n";
} catch ( Throwable $error ) {
	fwrite( STDERR, 'FAIL recovery experiment: ' . get_class( $error ) . "\n" ); exit( 1 );
}
