<?php
/** Disposable research fixture: not a WordPress deletion implementation. */
require __DIR__ . '/experiment-identity-shared-references.php';

function managed_snapshot( $id ) {
	global $wpdb, $prefix;
	return array(
		$wpdb->get_results( $wpdb->prepare( "SELECT * FROM {$wpdb->users} WHERE ID=%d", $id ), ARRAY_A ),
		$wpdb->get_results( $wpdb->prepare( "SELECT * FROM {$wpdb->usermeta} WHERE user_id=%d ORDER BY umeta_id", $id ), ARRAY_A ),
		$wpdb->get_results( $wpdb->prepare( "SELECT * FROM `{$prefix}identity_claims` WHERE owner_id=%d ORDER BY claim_key", $id ), ARRAY_A ),
		$wpdb->get_results( $wpdb->prepare( "SELECT * FROM `{$prefix}identity_refs` WHERE owner_id=%d ORDER BY source_kind,source_id", $id ), ARRAY_A ),
	);
}

/** Only used with a dedicated fixture connection and a synthetic account. */
function managed_delete_fixture( $id, $notify ) {
	global $wpdb;
	if ( is_multisite() ) { return 'UNSUPPORTED'; }
	$user = get_userdata( $id );
	if ( ! $user ) { return 'NOT_FOUND'; }
	if ( ! in_array( $user->user_login, array( 'managed_success', 'managed_fault', 'managed_posts', 'managed_notifyfail' ), true ) ) { return 'UNSUPPORTED'; }
	if ( false === $wpdb->query( 'START TRANSACTION' ) ) { return 'INDETERMINATE'; }
	try {
		$locked = $wpdb->get_var( $wpdb->prepare( "SELECT ID FROM {$wpdb->users} WHERE ID=%d FOR UPDATE", $id ) );
		if ( $wpdb->last_error || (int) $locked !== (int) $id ) { throw new RuntimeException( 'lock' ); }
		foreach ( array(
			$wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->posts} WHERE post_author=%d", $id ),
			$wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->links} WHERE link_owner=%d", $id ),
			$wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->comments} WHERE user_id=%d", $id ),
		) as $query ) {
			$count = $wpdb->get_var( $query );
			if ( $wpdb->last_error ) { throw new RuntimeException( 'check' ); }
			if ( (int) $count !== 0 ) {
				$result = $wpdb->query( 'ROLLBACK' );
				wp_cache_delete( $id, 'user_meta' ); clean_user_cache( $user );
				return false === $result ? 'INDETERMINATE' : 'UNSUPPORTED';
			}
		}
		$mids = $wpdb->get_col( $wpdb->prepare( "SELECT umeta_id FROM {$wpdb->usermeta} WHERE user_id=%d ORDER BY umeta_id FOR UPDATE", $id ) );
		if ( $wpdb->last_error ) { throw new RuntimeException( 'metadata-lock' ); }
		foreach ( $mids as $mid ) {
			if ( 1 !== $wpdb->delete( $wpdb->usermeta, array( 'umeta_id' => (int) $mid, 'user_id' => $id ) ) ) { throw new RuntimeException( 'metadata-delete' ); }
		}
		if ( 1 !== $wpdb->delete( $wpdb->users, array( 'ID' => $id ) ) ) { throw new RuntimeException( 'user-delete' ); }
		if ( false === $wpdb->query( 'COMMIT' ) ) {
			wp_cache_delete( $id, 'user_meta' ); clean_user_cache( $user );
			return 'INDETERMINATE'; // Do not assume a failed COMMIT means the server rolled back.
		}
	} catch ( Throwable $e ) {
		$result = $wpdb->query( 'ROLLBACK' );
		wp_cache_delete( $id, 'user_meta' ); clean_user_cache( $user );
		return false === $result ? 'INDETERMINATE' : 'FAILED';
	}
	wp_cache_delete( $id, 'user_meta' ); clean_user_cache( $user );
	try { $notify( $id ); } catch ( Throwable $e ) { return 'COMMITTED_NOTIFICATION_FAILED'; }
	return 'COMMITTED';
}

try {
	global $wpdb, $prefix, $wp_version;
	echo 'Runtime WordPress ' . $wp_version . ', PHP ' . PHP_VERSION . ', database ' . $wpdb->db_version() . "\n";
	foreach ( array( 'users', 'usermeta', 'identity_claims', 'identity_refs' ) as $suffix ) {
		$engine = $wpdb->get_var( $wpdb->prepare( 'SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=%s', $prefix . $suffix ) );
		shared_check( strtoupper( (string) $engine ) === 'INNODB', 'managed fixture transactional ' . $suffix );
	}
	shared_check( ! is_multisite(), 'single-site fixture only' );
	$hooks = array();
	foreach ( array( 'delete_user', 'deleted_user' ) as $hook ) {
		add_action( $hook, static function ( $id ) use ( &$hooks, $hook ) { $hooks[ $id ][ $hook ] = ( $hooks[ $id ][ $hook ] ?? 0 ) + 1; } );
	}
	foreach ( array( 'delete_user_meta', 'deleted_user_meta' ) as $hook ) {
		add_action( $hook, static function ( $mids, $id ) use ( &$hooks, $hook ) { $hooks[ $id ][ $hook ] = ( $hooks[ $id ][ $hook ] ?? 0 ) + 1; }, 10, 2 );
	}
	$sql = "CREATE TRIGGER `{$prefix}managed_fault_bd` BEFORE DELETE ON {$wpdb->usermeta} FOR EACH ROW BEGIN IF @managed_fault_mid = OLD.umeta_id THEN SIGNAL SQLSTATE '45000'; END IF; END";
	shared_check( false !== $wpdb->query( $sql ), 'create session-scoped synthetic fault' );
	$native = shared_user( 'managed_native', 'native@example.test' );
	$native_mid = add_user_meta( $native, 'cb_mobile', '09129992001' );
	$native_before = managed_snapshot( $native );
	$wpdb->query( 'SET @managed_fault_mid=' . (int) $native_mid );
	$native_result = wp_delete_user( $native );
	$wpdb->query( 'SET @managed_fault_mid=NULL' );
	$native_after = managed_snapshot( $native );
	shared_check( true === $native_result && count( $native_after[0] ) === 1 && count( $native_after[1] ) < count( $native_before[1] ) && shared_owner( '09129992001' ) === $native, 'native failure reports success despite surviving user and partial metadata' );
	shared_check( ! empty( $hooks[ $native ]['deleted_user'] ) && ! empty( $hooks[ $native ]['deleted_user_meta'] ), 'native failure emits deletion callbacks' );

	$success = shared_user( 'managed_success', 'success@example.test' );
	shared_check( (bool) add_user_meta( $success, 'cb_mobile', '09129992002' ), 'success synthetic mobile created' );
	get_userdata( $success ); get_user_meta( $success );
	$notifications = 0; $after_commit = false;
	$notify = static function ( $id ) use ( &$notifications, &$after_commit, $prefix ) {
		++$notifications;
		// An independent connection must see the committed result before the notification.
		$observer = new mysqli( '127.0.0.1', 'synthetic_qa', 'synthetic_qa_password', 'carmilla_login_qa', 3306 );
		$after_commit = true;
		foreach ( array( 'users' => 'ID', 'usermeta' => 'user_id', 'identity_claims' => 'owner_id', 'identity_refs' => 'owner_id' ) as $table => $column ) {
			$result = $observer->query( "SELECT COUNT(*) AS n FROM `{$prefix}{$table}` WHERE `$column`=" . (int) $id );
			if ( ! $result || (int) $result->fetch_assoc()['n'] !== 0 ) { $after_commit = false; }
		}
		$observer->close();
	};
	shared_check( 'COMMITTED' === managed_delete_fixture( $success, $notify ), 'managed successful deletion commits' );
	shared_check( 1 === $notifications && $after_commit && ! get_userdata( $success ) && array() === get_user_meta( $success ), 'committed state, local cache and controlled notification' );
	shared_check( empty( $hooks[ $success ] ), 'managed path intentionally bypasses native deletion hooks' );

	$fault = shared_user( 'managed_fault', 'fault@example.test' );
	$fault_mid = add_user_meta( $fault, 'cb_mobile', '09129992003' );
	$before = managed_snapshot( $fault ); get_userdata( $fault ); get_user_meta( $fault );
	shared_check( $fault_mid && count( $before[1] ) > 1 && (int) $before[1][0]['umeta_id'] !== (int) $fault_mid, 'fault occurs after an earlier metadata deletion' );
	$wpdb->query( 'SET @managed_fault_mid=' . (int) $fault_mid );
	shared_check( 'FAILED' === managed_delete_fixture( $fault, $notify ), 'managed injected failure reports failure' );
	$wpdb->query( 'SET @managed_fault_mid=NULL' );
	shared_check( $before === managed_snapshot( $fault ) && get_userdata( $fault ) && get_user_meta( $fault, 'cb_mobile', true ) === '09129992003', 'rollback restores persisted user, all metadata and identity references; cache agrees' );
	shared_check( 1 === $notifications && empty( $hooks[ $fault ] ), 'failed attempt emits no controlled notification or native deletion hooks' );
	shared_check( 'COMMITTED' === managed_delete_fixture( $fault, $notify ) && 2 === $notifications && $after_commit, 'retry after removing fault commits once' );
	shared_check( 'NOT_FOUND' === managed_delete_fixture( $fault, $notify ) && 2 === $notifications, 'repeated retry does not notify twice in this process' );

	$posts_user = shared_user( 'managed_posts', 'posts@example.test' );
	$post = wp_insert_post( array( 'post_title' => 'Synthetic post', 'post_status' => 'publish', 'post_author' => $posts_user ) );
	$post_snapshot = managed_snapshot( $posts_user );
	shared_check( ! is_wp_error( $post ) && (int) $post > 0 && 'UNSUPPORTED' === managed_delete_fixture( $posts_user, $notify ) && $post_snapshot === managed_snapshot( $posts_user ), 'user with post is refused without data changes' );

	$notify_user = shared_user( 'managed_notifyfail', 'notify@example.test' );
	shared_check( 'COMMITTED_NOTIFICATION_FAILED' === managed_delete_fixture( $notify_user, static function () { throw new RuntimeException( 'synthetic notification failure' ); } ) && ! get_userdata( $notify_user ), 'notification failure after commit does not claim rollback' );
	shared_check( 'NOT_FOUND' === managed_delete_fixture( $notify_user, $notify ), 'notification is not durably retried by fixture' );
	echo "Managed synthetic path only; no production or native-delete guarantee.\n";
} catch ( Throwable $e ) {
	fwrite( STDERR, 'FAIL managed synthetic deletion: ' . get_class( $e ) . "\n" ); exit( 1 );
}
