<?php
/** Disposable research fixture, not a production incident recorder. */
$prefix = getenv( 'CARMILLA_LOGIN_TEST_PREFIX' );
$mode = getenv( 'CARMILLA_INCIDENT_MODE' );
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) || ! in_array( $mode, array( 'write', 'read' ), true ) ) {
	fwrite( STDERR, "Disposable flag, random prefix and write/read mode required.\n" ); exit( 2 );
}

if ( $mode === 'read' ) {
	// This process does not load WordPress or share memory with the writer.
	try {
		$db = new mysqli( '127.0.0.1', 'synthetic_qa', 'synthetic_qa_password', 'carmilla_login_qa', 3306 );
		$table_name = $prefix . 'deletion_incidents';
		$stmt = $db->prepare( 'SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? ORDER BY ORDINAL_POSITION' );
		$stmt->bind_param( 's', $table_name ); $stmt->execute(); $columns = array();
		$result = $stmt->get_result(); while ( $row = $result->fetch_row() ) { $columns[] = $row[0]; } $stmt->close();
		if ( $columns !== array( 'incident_id', 'incident_code', 'source_code', 'recorded_at' ) ) { throw new RuntimeException( 'schema' ); }
		$result = $db->query( "SELECT incident_id,incident_code,source_code,recorded_at FROM `$table_name` ORDER BY incident_id" );
		$rows = $result->fetch_all( MYSQLI_ASSOC );
		if ( count( $rows ) !== 2 ) { throw new RuntimeException( 'row-count' ); }
		foreach ( $rows as $row ) {
			if ( ! preg_match( '/\A[0-9a-f]{32}\z/', $row['incident_id'] ) || $row['incident_code'] !== 'PARTIAL_USER_SURVIVES' ||
				$row['source_code'] !== 'NATIVE_DELETED_USER' || ! preg_match( '/\A\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\z/', $row['recorded_at'] ) ) { throw new RuntimeException( 'redaction' ); }
		}
		if ( $rows[0]['incident_id'] === $rows[1]['incident_id'] ) { throw new RuntimeException( 'duplicate' ); }
		$db->close();
		echo "PASS independent process readback, fixed schema and allowlisted redacted rows\n"; exit( 0 );
	} catch ( Throwable $error ) {
		fwrite( STDERR, 'FAIL independent incident readback: ' . get_class( $error ) . "\n" ); exit( 1 );
	}
}

require __DIR__ . '/experiment-identity-shared-references.php';

function incident_store( $incident_id ) {
	global $wpdb, $prefix;
	if ( ! preg_match( '/\A[0-9a-f]{32}\z/', $incident_id ) ) { return 'UNVERIFIED'; }
	$sql = $wpdb->prepare( "INSERT INTO `{$prefix}deletion_incidents` (incident_id,incident_code,source_code,recorded_at) VALUES (%s,'PARTIAL_USER_SURVIVES','NATIVE_DELETED_USER',UTC_TIMESTAMP()) ON DUPLICATE KEY UPDATE incident_id=incident_id", $incident_id );
	if ( $wpdb->query( $sql ) === false ) { return 'UNVERIFIED'; }
	$row = $wpdb->get_row( $wpdb->prepare( "SELECT incident_code,source_code FROM `{$prefix}deletion_incidents` WHERE incident_id=%s", $incident_id ), ARRAY_A );
	if ( $wpdb->last_error !== '' || $row !== array( 'incident_code' => 'PARTIAL_USER_SURVIVES', 'source_code' => 'NATIVE_DELETED_USER' ) ) { return 'UNVERIFIED'; }
	return 'RECORDED';
}

try {
	global $wpdb, $prefix, $wp_version;
	$table = "`{$prefix}deletion_incidents`";
	shared_check( $wpdb->query( "CREATE TABLE $table (incident_id CHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL PRIMARY KEY, incident_code ENUM('PARTIAL_USER_SURVIVES') NOT NULL, source_code ENUM('NATIVE_DELETED_USER') NOT NULL, recorded_at DATETIME NOT NULL) ENGINE=InnoDB" ) !== false, 'create synthetic redacted incident table' );
	shared_check( $wpdb->query( "CREATE TRIGGER `{$prefix}incident_fail_bi` BEFORE INSERT ON $table FOR EACH ROW BEGIN IF @incident_write_fault=1 THEN SIGNAL SQLSTATE '45000'; END IF; END" ) !== false, 'create injected incident-write fault' );
	shared_check( $wpdb->query( "CREATE TRIGGER `{$prefix}incident_delete_bd` BEFORE DELETE ON {$wpdb->usermeta} FOR EACH ROW BEGIN IF @incident_delete_mid=OLD.umeta_id THEN SIGNAL SQLSTATE '45000'; END IF; END" ) !== false, 'create injected metadata-delete fault' );
	$before = array(); $events = array(); $ids = array(); $broken_probe = array(); $listener = array();
	add_action( 'delete_user', static function ( $id ) use ( &$before, &$ids ) {
		global $wpdb;
		// Synthetic metadata is held only in this process, never in incident rows or output.
		$rows = $wpdb->get_results( $wpdb->prepare( "SELECT umeta_id,meta_key,meta_value FROM {$wpdb->usermeta} WHERE user_id=%d ORDER BY umeta_id", $id ), ARRAY_A );
		$before[ $id ] = $wpdb->last_error === '' && is_array( $rows ) ? $rows : null;
		$ids[ $id ] = bin2hex( random_bytes( 16 ) );
	}, 10, 1 );
	add_action( 'deleted_user', static function ( $id ) use ( &$before, &$events, &$ids, &$broken_probe, $prefix ) {
		global $wpdb;
		try {
			if ( ! isset( $before[ $id ], $ids[ $id ] ) || ! empty( $broken_probe[ $id ] ) ) { $events[ $id ] = 'UNVERIFIED'; return; }
			$user_count = $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE ID=%d", $id ) );
			if ( $wpdb->last_error !== '' || $user_count === null ) { $events[ $id ] = 'UNVERIFIED'; return; }
			$after = $wpdb->get_results( $wpdb->prepare( "SELECT umeta_id,meta_key,meta_value FROM {$wpdb->usermeta} WHERE user_id=%d ORDER BY umeta_id", $id ), ARRAY_A );
			if ( $wpdb->last_error !== '' || ! is_array( $after ) ) { $events[ $id ] = 'UNVERIFIED'; return; }
			$claims = $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM `{$prefix}identity_claims` WHERE owner_id=%d", $id ) );
			if ( $wpdb->last_error !== '' || $claims === null ) { $events[ $id ] = 'UNVERIFIED'; return; }
			$refs = $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM `{$prefix}identity_refs` WHERE owner_id=%d", $id ) );
			if ( $wpdb->last_error !== '' || $refs === null ) { $events[ $id ] = 'UNVERIFIED'; return; }
			if ( (int) $user_count === 1 && $after !== $before[ $id ] ) { $events[ $id ] = incident_store( $ids[ $id ] ); return; }
			if ( (int) $user_count === 0 && count( $after ) === 0 && (int) $claims === 0 && (int) $refs === 0 ) { $events[ $id ] = 'NO_LOCAL_ANOMALY'; return; }
			$events[ $id ] = 'UNVERIFIED';
		} catch ( Throwable $error ) { $events[ $id ] = 'UNVERIFIED'; }
	}, 50, 1 );
	add_action( 'deleted_user', static function ( $id ) use ( &$listener ) { $listener[ $id ] = true; }, 99, 1 );

	$clean = shared_user( 'incident_clean', 'incident_clean@example.test' );
	add_user_meta( $clean, 'cb_mobile', '09127770001' );
	shared_check( wp_delete_user( $clean ) && ( $events[ $clean ] ?? '' ) === 'NO_LOCAL_ANOMALY' && ! empty( $listener[ $clean ] ) && (int) $wpdb->get_var( "SELECT COUNT(*) FROM $table" ) === 0, 'native clean local deletion creates no incident and other listener runs' );

	$partial = shared_user( 'incident_partial', 'incident_partial@example.test' );
	$mid = add_user_meta( $partial, 'cb_mobile', '09127770002' );
	$wpdb->query( $wpdb->prepare( 'SET @incident_delete_mid=%d', $mid ) );
	$reported = wp_delete_user( $partial ); $wpdb->query( 'SET @incident_delete_mid=NULL' );
	shared_check( $reported === true && ( $events[ $partial ] ?? '' ) === 'RECORDED' && ! empty( $listener[ $partial ] ) &&
		(int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE ID=%d", $partial ) ) === 1 && shared_meta_count( $partial, 'cb_mobile' ) === 1, 'native false success detected and redacted incident stored' );
	shared_check( incident_store( $ids[ $partial ] ) === 'RECORDED' && incident_store( $ids[ $partial ] ) === 'RECORDED' && (int) $wpdb->get_var( "SELECT COUNT(*) FROM $table" ) === 1, 'retries for the same observed incident do not duplicate it' );

	$write_fail = shared_user( 'incident_failed_write', 'incident_failed_write@example.test' );
	$mid = add_user_meta( $write_fail, 'cb_mobile', '09127770003' );
	$wpdb->query( $wpdb->prepare( 'SET @incident_delete_mid=%d', $mid ) ); $wpdb->query( 'SET @incident_write_fault=1' );
	$reported = wp_delete_user( $write_fail ); $wpdb->query( 'SET @incident_delete_mid=NULL' ); $wpdb->query( 'SET @incident_write_fault=NULL' );
	shared_check( $reported === true && ( $events[ $write_fail ] ?? '' ) === 'UNVERIFIED' && ! empty( $listener[ $write_fail ] ) && (int) $wpdb->get_var( "SELECT COUNT(*) FROM $table" ) === 1, 'failed incident write is unverified, not clean or recorded' );
	shared_check( incident_store( $ids[ $write_fail ] ) === 'RECORDED' && incident_store( $ids[ $write_fail ] ) === 'RECORDED' && (int) $wpdb->get_var( "SELECT COUNT(*) FROM $table" ) === 2, 'explicit retry after injected write failure stores one incident' );

	$probe = shared_user( 'incident_unverified_probe', 'incident_unverified_probe@example.test' ); $broken_probe[ $probe ] = true;
	shared_check( wp_delete_user( $probe ) && ( $events[ $probe ] ?? '' ) === 'UNVERIFIED' && (int) $wpdb->get_var( "SELECT COUNT(*) FROM $table" ) === 2, 'simulated unavailable probe is unverified and does not create clean evidence' );
	shared_check( incident_store( 'invalid' ) === 'UNVERIFIED' && (int) $wpdb->get_var( "SELECT COUNT(*) FROM $table" ) === 2, 'malformed incident key refused' );
	echo 'PASS incident writer WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n";
	echo "Writer exiting normally; an independent process must perform readback.\n";
} catch ( Throwable $error ) {
	fwrite( STDERR, 'FAIL synthetic incident experiment: ' . get_class( $error ) . "\n" ); exit( 1 );
}
