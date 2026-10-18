<?php
/** Research observer for disposable synthetic WordPress only; not a product hook. */
require __DIR__ . '/experiment-identity-shared-references.php';

try {
	global $wpdb, $prefix, $wp_version;
	echo 'Runtime WordPress ' . $wp_version . ', PHP ' . PHP_VERSION . ', database ' . $wpdb->db_version() . "\n";
	$before = array(); $results = array(); $events = array(); $broken_probe = array(); $broken_observer = array();
	// The fixture stores only synthetic metadata in process memory, never in logs.
	add_action( 'delete_user', static function ( $id ) use ( &$before, &$events ) {
		global $wpdb;
		$events[ $id ][] = 'native-delete-begin';
		$rows = $wpdb->get_results( $wpdb->prepare( "SELECT umeta_id,meta_key,meta_value FROM {$wpdb->usermeta} WHERE user_id=%d ORDER BY umeta_id", $id ), ARRAY_A );
		$before[ $id ] = $wpdb->last_error === '' ? $rows : null;
	}, 10, 1 );
	add_action( 'delete_user_meta', static function ( $mids, $id, $key ) use ( &$events ) {
		if ( $key === 'cb_mobile' ) { $events[ $id ][] = 'native-mobile-before'; }
	}, 10, 3 );
	add_action( 'deleted_user_meta', static function ( $mids, $id, $key ) use ( &$events ) {
		if ( $key === 'cb_mobile' ) { $events[ $id ][] = 'native-mobile-after'; }
	}, 10, 3 );
	add_action( 'deleted_user', static function ( $id ) use ( &$before, &$results, &$events, &$broken_probe, &$broken_observer, $prefix ) {
		global $wpdb;
		$events[ $id ][] = 'observer';
		try {
			if ( ! empty( $broken_observer[ $id ] ) ) { throw new RuntimeException( 'synthetic observer failure' ); }
			if ( ! array_key_exists( $id, $before ) || $before[ $id ] === null ) { $results[ $id ] = 'UNVERIFIED'; return; }
			if ( ! empty( $broken_probe[ $id ] ) ) {
				$wpdb->get_var( "SELECT COUNT(*) FROM `{$prefix}synthetic_table_that_does_not_exist`" );
				if ( $wpdb->last_error !== '' ) { $results[ $id ] = 'UNVERIFIED'; return; }
			}
			$user_count = $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE ID=%d", $id ) );
			if ( $wpdb->last_error !== '' || $user_count === null ) { $results[ $id ] = 'UNVERIFIED'; return; }
			$after = $wpdb->get_results( $wpdb->prepare( "SELECT umeta_id,meta_key,meta_value FROM {$wpdb->usermeta} WHERE user_id=%d ORDER BY umeta_id", $id ), ARRAY_A );
			if ( $wpdb->last_error !== '' ) { $results[ $id ] = 'UNVERIFIED'; return; }
			$claim_count = $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM `{$prefix}identity_claims` WHERE owner_id=%d", $id ) );
			if ( $wpdb->last_error !== '' || $claim_count === null ) { $results[ $id ] = 'UNVERIFIED'; return; }
			$ref_count = $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM `{$prefix}identity_refs` WHERE owner_id=%d", $id ) );
			if ( $wpdb->last_error !== '' || $ref_count === null ) { $results[ $id ] = 'UNVERIFIED'; return; }
			if ( (int) $user_count > 0 ) { $results[ $id ] = $after === $before[ $id ] ? 'USER_SURVIVES' : 'PARTIAL_USER_SURVIVES'; return; }
			$results[ $id ] = count( $after ) === 0 && (int) $claim_count === 0 && (int) $ref_count === 0 ? 'NO_LOCAL_ANOMALY' : 'ORPHANED_LOCAL_DATA';
		} catch ( Throwable $e ) { $results[ $id ] = 'UNVERIFIED'; }
	}, 50, 1 );
	// Represents a separate plugin listener, not disabled or intercepted.
	add_action( 'deleted_user', static function ( $id ) use ( &$events ) { $events[ $id ][] = 'other-listener'; }, 99, 1 );

	$trigger = "CREATE TRIGGER `{$prefix}observer_fault_bd` BEFORE DELETE ON {$wpdb->usermeta} FOR EACH ROW BEGIN IF @observer_fault_mid=OLD.umeta_id THEN SIGNAL SQLSTATE '45000'; END IF; END";
	shared_check( false !== $wpdb->query( $trigger ), 'create session-scoped synthetic deletion fault' );
	$ok = shared_user( 'observer_ok', 'observer_ok@example.test' );
	shared_check( (bool) add_user_meta( $ok, 'cb_mobile', '09129993001' ), 'successful synthetic user has mobile reference' );
	shared_check( true === wp_delete_user( $ok ) && ( $results[ $ok ] ?? '' ) === 'NO_LOCAL_ANOMALY', 'native successful delete has no local anomaly' );
	shared_check( $events[ $ok ] === array( 'native-delete-begin', 'native-mobile-before', 'native-mobile-after', 'observer', 'other-listener' ), 'native success hooks and separate listener execute in observed order' );

	$fault = shared_user( 'observer_fault', 'observer_fault@example.test' );
	$mid = add_user_meta( $fault, 'cb_mobile', '09129993002' );
	shared_check( (bool) $mid, 'failure synthetic user has mobile reference' );
	$wpdb->query( 'SET @observer_fault_mid=' . (int) $mid );
	$return = wp_delete_user( $fault );
	$wpdb->query( 'SET @observer_fault_mid=NULL' );
	shared_check( true === $return && ( $results[ $fault ] ?? '' ) === 'PARTIAL_USER_SURVIVES', 'detect surviving partially changed user despite native success' );
	shared_check( $events[ $fault ] === array( 'native-delete-begin', 'native-mobile-before', 'native-mobile-after', 'observer', 'other-listener' ) && shared_owner( '09129993002' ) === $fault, 'native failure hooks and other listener still ran; mobile claim retained' );
	shared_check( (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE ID=%d", $fault ) ) === 1 && shared_meta_count( $fault, 'cb_mobile' ) === 1, 'raw database confirms surviving synthetic user and metadata' );

	$unreadable = shared_user( 'observer_read_error', 'observer_read_error@example.test' );
	$broken_probe[ $unreadable ] = true;
	shared_check( true === wp_delete_user( $unreadable ) && ( $results[ $unreadable ] ?? '' ) === 'UNVERIFIED', 'observer query error is unverified, not success' );
	$exception = shared_user( 'observer_exception', 'observer_exception@example.test' );
	$broken_observer[ $exception ] = true;
	shared_check( true === wp_delete_user( $exception ) && ( $results[ $exception ] ?? '' ) === 'UNVERIFIED' && in_array( 'other-listener', $events[ $exception ], true ), 'caught observer exception is unverified and other listener still runs' );
	$missing = shared_user( 'observer_missing_before', 'observer_missing_before@example.test' );
	do_action( 'deleted_user', $missing, null, get_userdata( $missing ) ); // Test-only simulation of a missing before-event.
	shared_check( ( $results[ $missing ] ?? '' ) === 'UNVERIFIED', 'missing before-event cannot be reported as success' );

	$direct = shared_user( 'observer_direct_sql', 'observer_direct_sql@example.test' );
	shared_check( 1 === $wpdb->delete( $wpdb->users, array( 'ID' => $direct ) ) && ! isset( $results[ $direct ] ) && ! isset( $events[ $direct ] ), 'direct SQL bypasses native hook observer' );
	echo "Bounded in-memory observer only; no durable alert or recovery.\n";
} catch ( Throwable $e ) {
	fwrite( STDERR, 'FAIL native deletion observer: ' . get_class( $e ) . "\n" ); exit( 1 );
}
