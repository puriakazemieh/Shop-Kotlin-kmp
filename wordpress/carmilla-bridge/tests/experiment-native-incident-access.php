<?php
/** Disposable synthetic access research; no route or production authorization. */
require __DIR__ . '/experiment-identity-shared-references.php';

function access_fixture_store( $event_id, $account_id ) {
	global $wpdb, $prefix;
	if ( ! preg_match( '/\A[0-9a-f]{32}\z/', $event_id ) || $account_id < 1 ) { return 'UNVERIFIED'; }
	$sql = $wpdb->prepare( "INSERT INTO `{$prefix}incident_access` (incident_id,account_id,incident_code,recorded_at,expires_at) VALUES (%s,%d,'PARTIAL_USER_SURVIVES',UTC_TIMESTAMP(),DATE_ADD(UTC_TIMESTAMP(),INTERVAL 1 HOUR))", $event_id, $account_id );
	return $wpdb->query( $sql ) === 1 ? 'RECORDED' : 'UNVERIFIED';
}

function access_fixture_read( $event_id ) {
	global $wpdb, $prefix, $fixture_read_queries, $fixture_bad_probe;
	// Guard before any incident or account query; this capability is only a fixture choice.
	if ( ! current_user_can( 'manage_options' ) ) { return array( 'status' => 'FORBIDDEN' ); }
	if ( ! preg_match( '/\A[0-9a-f]{32}\z/', $event_id ) ) { return array( 'status' => 'UNVERIFIED' ); }
	++$fixture_read_queries;
	$table = $fixture_bad_probe ? "`{$prefix}missing_incident_table`" : "`{$prefix}incident_access`";
	$row = $wpdb->get_row( $wpdb->prepare( "SELECT account_id,incident_code,(expires_at > UTC_TIMESTAMP()) AS active FROM $table WHERE incident_id=%s", $event_id ), ARRAY_A );
	if ( $wpdb->last_error !== '' ) { return array( 'status' => 'UNVERIFIED' ); }
	if ( $row === null ) { return array( 'status' => 'MISSING_INCIDENT' ); }
	if ( $row['incident_code'] !== 'PARTIAL_USER_SURVIVES' || (int) $row['account_id'] < 1 ) { return array( 'status' => 'UNVERIFIED' ); }
	if ( (int) $row['active'] !== 1 ) { return array( 'status' => 'EXPIRED' ); }
	$exists = $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE ID=%d", (int) $row['account_id'] ) );
	if ( $wpdb->last_error !== '' || $exists === null ) { return array( 'status' => 'UNVERIFIED' ); }
	if ( (int) $exists !== 1 ) { return array( 'status' => 'MISSING_ACCOUNT' ); }
	return array( 'status' => 'AVAILABLE', 'account_id' => (int) $row['account_id'] );
}

try {
	global $wpdb, $prefix, $wp_version;
	$fixture_read_queries = 0; $fixture_bad_probe = false; $before = array(); $results = array(); $event_ids = array();
	$table = "`{$prefix}incident_access`";
	shared_check( $wpdb->query( "CREATE TABLE $table (incident_id CHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL PRIMARY KEY, account_id BIGINT UNSIGNED NOT NULL, incident_code ENUM('PARTIAL_USER_SURVIVES') NOT NULL, recorded_at DATETIME NOT NULL, expires_at DATETIME NOT NULL) ENGINE=InnoDB" ) !== false, 'create minimal synthetic incident/account table' );
	$columns = $wpdb->get_col( "SHOW COLUMNS FROM $table" );
	shared_check( $columns === array( 'incident_id', 'account_id', 'incident_code', 'recorded_at', 'expires_at' ), 'incident schema excludes account metadata, credentials and contact fields' );
	shared_check( $wpdb->query( "CREATE TRIGGER `{$prefix}access_write_fault` BEFORE INSERT ON $table FOR EACH ROW BEGIN IF @access_write_fault=1 THEN SIGNAL SQLSTATE '45000'; END IF; END" ) !== false, 'create synthetic incident write fault' );
	shared_check( $wpdb->query( "CREATE TRIGGER `{$prefix}access_delete_fault` BEFORE DELETE ON {$wpdb->usermeta} FOR EACH ROW BEGIN IF @access_delete_mid=OLD.umeta_id THEN SIGNAL SQLSTATE '45000'; END IF; END" ) !== false, 'create synthetic native metadata delete fault' );
	add_action( 'delete_user', static function ( $id ) use ( &$before, &$event_ids ) {
		global $wpdb;
		$rows = $wpdb->get_results( $wpdb->prepare( "SELECT umeta_id,meta_key,meta_value FROM {$wpdb->usermeta} WHERE user_id=%d ORDER BY umeta_id", $id ), ARRAY_A );
		$before[ $id ] = $wpdb->last_error === '' && is_array( $rows ) ? $rows : null;
		$event_ids[ $id ] = bin2hex( random_bytes( 16 ) );
	}, 10, 1 );
	add_action( 'deleted_user', static function ( $id ) use ( &$before, &$event_ids, &$results ) {
		global $wpdb;
		if ( ! isset( $before[ $id ], $event_ids[ $id ] ) ) { $results[ $id ] = 'UNVERIFIED'; return; }
		$count = $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE ID=%d", $id ) );
		if ( $wpdb->last_error !== '' || $count === null ) { $results[ $id ] = 'UNVERIFIED'; return; }
		$after = $wpdb->get_results( $wpdb->prepare( "SELECT umeta_id,meta_key,meta_value FROM {$wpdb->usermeta} WHERE user_id=%d ORDER BY umeta_id", $id ), ARRAY_A );
		if ( $wpdb->last_error !== '' || ! is_array( $after ) ) { $results[ $id ] = 'UNVERIFIED'; return; }
		if ( (int) $count === 1 && $before[ $id ] !== $after ) { $results[ $id ] = access_fixture_store( $event_ids[ $id ], $id ); return; }
		$results[ $id ] = 'NOT_PARTIAL_OR_UNVERIFIED';
	}, 50, 1 );

	$target = shared_user( 'access_target', 'access_target@example.test' );
	$mid = add_user_meta( $target, 'cb_mobile', '09126660001' );
	$wpdb->query( $wpdb->prepare( 'SET @access_delete_mid=%d', $mid ) );
	$returned = wp_delete_user( $target ); $wpdb->query( 'SET @access_delete_mid=NULL' );
	shared_check( $returned === true && ( $results[ $target ] ?? '' ) === 'RECORDED' && shared_meta_count( $target, 'cb_mobile' ) === 1, 'partial native deletion produces synthetic account reference despite WP success' );
	$event_id = $event_ids[ $target ];
	$editor = wp_insert_user( array( 'user_login' => 'access_editor', 'user_email' => 'access_editor@example.test', 'user_pass' => 'synthetic-only-password', 'role' => 'editor' ) );
	$admin = get_user_by( 'login', 'qa_admin' );
	shared_check( ! is_wp_error( $editor ) && $admin instanceof WP_User, 'synthetic administrator and editor exist' );
	wp_set_current_user( (int) $editor ); $before_queries = $fixture_read_queries;
	shared_check( access_fixture_read( $event_id ) === array( 'status' => 'FORBIDDEN' ) && $fixture_read_queries === $before_queries, 'editor forbidden before incident lookup' );
	wp_set_current_user( 0 ); $before_queries = $fixture_read_queries;
	shared_check( access_fixture_read( $event_id ) === array( 'status' => 'FORBIDDEN' ) && $fixture_read_queries === $before_queries, 'logged-out caller forbidden before incident lookup' );
	wp_set_current_user( $admin->ID );
	$expected = array( 'status' => 'AVAILABLE', 'account_id' => $target );
	$rows_before = (int) $wpdb->get_var( "SELECT COUNT(*) FROM $table" );
	shared_check( access_fixture_read( $event_id ) === $expected && access_fixture_read( $event_id ) === $expected && (int) $wpdb->get_var( "SELECT COUNT(*) FROM $table" ) === $rows_before, 'authorized repeated read returns same synthetic account without writes' );
	shared_check( access_fixture_read( bin2hex( random_bytes( 16 ) ) ) === array( 'status' => 'MISSING_INCIDENT' ), 'unknown incident remains unresolved' );
	$fixture_bad_probe = true;
	shared_check( access_fixture_read( $event_id ) === array( 'status' => 'UNVERIFIED' ), 'real failed incident query is unverified' );
	$fixture_bad_probe = false;
	$wpdb->query( 'SET @access_write_fault=1' );
	$store_failed = access_fixture_store( bin2hex( random_bytes( 16 ) ), $target ); $wpdb->query( 'SET @access_write_fault=NULL' );
	shared_check( $store_failed === 'UNVERIFIED' && (int) $wpdb->get_var( "SELECT COUNT(*) FROM $table" ) === $rows_before, 'injected incident write failure is unverified with no row' );

	$wpdb->query( $wpdb->prepare( "UPDATE $table SET expires_at=UTC_TIMESTAMP() WHERE incident_id=%s", $event_id ) );
	shared_check( access_fixture_read( $event_id ) === array( 'status' => 'EXPIRED' ) && (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE ID=%d", $target ) ) === 1, 'at expiry boundary reference is withheld and user is preserved' );
	$wpdb->query( $wpdb->prepare( "UPDATE $table SET expires_at=DATE_ADD(UTC_TIMESTAMP(),INTERVAL 1 HOUR) WHERE incident_id=%s", $event_id ) );
	shared_check( access_fixture_read( $event_id ) === $expected, 'unexpired synthetic reference is available again' );
	shared_check( wp_delete_user( $target ) === true && (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE ID=%d", $target ) ) === 0 && access_fixture_read( $event_id ) === array( 'status' => 'MISSING_ACCOUNT' ), 'later deleted synthetic user is unresolved and not returned' );
	echo 'PASS synthetic incident access WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n";
	echo "Fixture only: no production route, durable fallback or administrator alert.\n";
} catch ( Throwable $error ) {
	fwrite( STDERR, 'FAIL synthetic incident access: ' . get_class( $error ) . "\n" ); exit( 1 );
}
