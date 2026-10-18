<?php
/** Synthetic concurrency research. Does not install or protect a real site. */
$prefix = getenv( 'CARMILLA_LOGIN_TEST_PREFIX' );
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) ) {
	fwrite( STDERR, "Disposable test flag and random prefix required.\n" ); exit( 2 );
}

function race_check( $condition, $label ) {
	if ( ! $condition ) { echo 'FAIL ' . $label . "\n"; throw new RuntimeException( 'assertion' ); }
	echo 'PASS ' . $label . "\n";
}
function race_id( $login ) {
	global $wpdb;
	return (int) $wpdb->get_var( $wpdb->prepare( "SELECT ID FROM {$wpdb->users} WHERE BINARY user_login=BINARY %s", $login ) );
}
function race_mid( $user, $value ) {
	global $wpdb;
	return (int) $wpdb->get_var( $wpdb->prepare( "SELECT umeta_id FROM {$wpdb->usermeta} WHERE user_id=%d AND BINARY meta_key=BINARY 'cb_mobile' AND BINARY meta_value=BINARY %s ORDER BY umeta_id LIMIT 1", $user, $value ) );
}
function race_meta_exists( $user, $value ) { return race_mid( $user, $value ) > 0; }
function race_pair( $left, $right ) {
	$workers = array(); $descriptors = array( array( 'pipe', 'r' ), array( 'pipe', 'w' ), array( 'pipe', 'w' ) );
	$held = strpos( $left, 'snapshot-' ) === 0 && strpos( $right, 'snapshot-' ) === 0;
	try {
		foreach ( array( $left, $right ) as $operation ) {
			$pipes = array(); $process = proc_open( array( PHP_BINARY, __FILE__, 'worker', $operation ), $descriptors, $pipes );
			if ( ! is_resource( $process ) ) { throw new RuntimeException( 'worker-start' ); }
			stream_set_timeout( $pipes[1], 15 ); $workers[] = array( $process, $pipes );
		}
		foreach ( $workers as $worker ) { if ( trim( (string) fgets( $worker[1][1] ) ) !== 'READY' ) { throw new RuntimeException( 'worker-barrier' ); } }
		foreach ( $workers as $worker ) { fwrite( $worker[1][0], "GO\n" ); if ( ! $held ) { fclose( $worker[1][0] ); } }
		if ( $held ) {
			foreach ( $workers as $worker ) { if ( ! preg_match( '/\AEXEC (APPLIED|REJECTED)\z/', trim( (string) fgets( $worker[1][1] ) ) ) ) { throw new RuntimeException( 'worker-result' ); } }
			foreach ( $workers as $worker ) { fwrite( $worker[1][0], "COMMIT\n" ); fclose( $worker[1][0] ); }
		}
		$results = array();
		foreach ( $workers as $i => $worker ) {
			$result = trim( (string) stream_get_contents( $worker[1][1] ) ); fclose( $worker[1][1] ); fclose( $worker[1][2] );
			$status = proc_close( $worker[0] ); unset( $workers[ $i ] );
			if ( $status !== 0 || ! preg_match( '/\A(APPLIED|REJECTED_DB_[0-9]+|WP_APPLIED_PRE_POST|WP_REJECTED_PRE_DB_[0-9]+)\z/', $result ) ) { throw new RuntimeException( 'worker-result' ); }
			$results[] = $result;
		}
		echo 'OUTCOME ' . $left . ' ' . $results[0] . '; ' . $right . ' ' . $results[1] . "\n";
		return $results;
	} finally {
		foreach ( $workers as $worker ) { foreach ( $worker[1] as $pipe ) { if ( is_resource( $pipe ) ) { fclose( $pipe ); } } if ( is_resource( $worker[0] ) ) { proc_terminate( $worker[0] ); proc_close( $worker[0] ); } }
	}
}
function race_consistent() {
	global $wpdb, $prefix;
	$refs = "`{$prefix}identity_refs`"; $claims = "`{$prefix}identity_claims`"; $users = "`{$prefix}users`"; $meta = "`{$prefix}usermeta`";
	$queries = array(
		"SELECT COUNT(*) FROM $refs r LEFT JOIN $claims c ON c.claim_key=r.claim_key WHERE c.claim_key IS NULL OR c.owner_id<>r.owner_id",
		"SELECT COUNT(*) FROM $claims c LEFT JOIN $refs r ON c.claim_key=r.claim_key WHERE r.claim_key IS NULL",
		"SELECT COUNT(*) FROM $refs r LEFT JOIN $users u ON u.ID=r.owner_id WHERE u.ID IS NULL",
		"SELECT COUNT(*) FROM $refs r LEFT JOIN $meta m ON r.source_kind='mobile' AND m.umeta_id=r.source_id WHERE r.source_kind='mobile' AND (m.umeta_id IS NULL OR m.user_id<>r.owner_id OR BINARY m.meta_key<>BINARY 'cb_mobile' OR BINARY m.meta_value<>r.claim_key)",
		"SELECT COUNT(*) FROM $meta m LEFT JOIN $refs r ON r.source_kind='mobile' AND r.source_id=m.umeta_id WHERE BINARY m.meta_key=BINARY 'cb_mobile' AND r.source_id IS NULL",
		"SELECT COUNT(*) FROM $refs r LEFT JOIN $users u ON u.ID=r.source_id WHERE r.source_kind IN ('login','email') AND (u.ID IS NULL OR u.ID<>r.owner_id OR (r.source_kind='login' AND BINARY u.user_login<>r.claim_key) OR (r.source_kind='email' AND BINARY u.user_email<>r.claim_key))",
		"SELECT COUNT(*) FROM $users u LEFT JOIN $refs r ON r.source_kind='login' AND r.source_id=u.ID WHERE BINARY u.user_login<>BINARY 'qa_admin' AND r.source_id IS NULL",
		"SELECT COUNT(*) FROM $users u LEFT JOIN $refs r ON r.source_kind='email' AND r.source_id=u.ID WHERE BINARY u.user_login<>BINARY 'qa_admin' AND u.user_email<>'' AND r.source_id IS NULL",
	);
	foreach ( $queries as $query ) { $count = $wpdb->get_var( $query ); if ( $count === null || (int) $count !== 0 ) { return false; } }
	return true;
}

try {
	if ( ( $argv[1] ?? '' ) === 'worker' ) {
		$operation = $argv[2] ?? '';
		$allowed = array( 'wp-add-a', 'sql-add-b', 'wp-update-a', 'sql-login-b', 'sql-delete-duplicate', 'sql-add-duplicate', 'wp-mobile-email-a', 'sql-email-b', 'snapshot-delete-user', 'snapshot-add-meta', 'deadlock-a', 'deadlock-b' );
		if ( ! in_array( $operation, $allowed, true ) ) { exit( 2 ); }
		define( 'ABSPATH', '/usr/src/wordpress/' ); define( 'WP_INSTALLING', true ); define( 'WP_DEBUG', false );
		define( 'DB_NAME', 'carmilla_login_qa' ); define( 'DB_USER', 'synthetic_qa' ); define( 'DB_PASSWORD', 'synthetic_qa_password' ); define( 'DB_HOST', '127.0.0.1:3306' );
		define( 'DB_CHARSET', 'utf8mb4' ); define( 'DB_COLLATE', '' ); define( 'WP_HOME', 'https://qa.example.test/wordpress' ); define( 'WP_SITEURL', WP_HOME );
		foreach ( array( 'AUTH_KEY', 'SECURE_AUTH_KEY', 'LOGGED_IN_KEY', 'NONCE_KEY', 'AUTH_SALT', 'SECURE_AUTH_SALT', 'LOGGED_IN_SALT', 'NONCE_SALT' ) as $name ) { define( $name, hash( 'sha256', 'synthetic-wp-' . $name ) ); }
		$table_prefix = $prefix; $_SERVER['HTTP_HOST'] = 'qa.example.test'; $_SERVER['SERVER_NAME'] = 'qa.example.test'; $_SERVER['SERVER_PORT'] = '443'; $_SERVER['HTTPS'] = 'on'; $_SERVER['REQUEST_METHOD'] = 'GET';
		require ABSPATH . 'wp-settings.php'; global $wpdb; $wpdb->hide_errors(); $wpdb->suppress_errors( true );
		$wpdb->query( 'SET SESSION innodb_lock_wait_timeout=3' );
		$a = race_id( 'race_a' ); $b = race_id( 'race_b' ); $duplicate = race_id( '09129991004' ); $deleting = race_id( 'race_delete' );
		if ( ! $a || ! $b || ! $duplicate || ! $deleting ) { throw new RuntimeException( 'fixture-user' ); }
		$pre = false; $post = false;
		add_action( 'add_user_meta', static function () use ( &$pre ) { $pre = true; } );
		add_action( 'added_user_meta', static function () use ( &$post ) { $post = true; } );
		$snapshot = strpos( $operation, 'snapshot-' ) === 0;
		if ( $snapshot && $wpdb->query( 'START TRANSACTION WITH CONSISTENT SNAPSHOT' ) === false ) { throw new RuntimeException( 'transaction' ); }
		$deadlock = strpos( $operation, 'deadlock-' ) === 0;
		if ( $deadlock ) {
			$locked_email = $operation === 'deadlock-a' ? 'deadlock_a@example.test' : 'deadlock_b@example.test';
			if ( $wpdb->query( 'START TRANSACTION' ) === false || ! $wpdb->get_var( $wpdb->prepare( "SELECT owner_id FROM `{$prefix}identity_claims` WHERE claim_key=%s FOR UPDATE", $locked_email ) ) ) { throw new RuntimeException( 'transaction' ); }
		}
		echo "READY\n"; flush(); if ( trim( (string) fgets( STDIN ) ) !== 'GO' ) { exit( 3 ); }
		if ( $operation === 'wp-add-a' ) { $result = add_user_meta( $a, 'cb_mobile', '09129991001' ); }
		elseif ( $operation === 'sql-add-b' ) { $result = $wpdb->insert( $wpdb->usermeta, array( 'user_id' => $b, 'meta_key' => 'cb_mobile', 'meta_value' => '09129991001' ) ); }
		elseif ( $operation === 'wp-update-a' ) { $result = update_metadata_by_mid( 'user', race_mid( $a, '09129991002' ), '09129991003', 'cb_mobile' ); }
		elseif ( $operation === 'sql-login-b' ) { $result = $wpdb->update( $wpdb->users, array( 'user_login' => '09129991003' ), array( 'ID' => $b ) ); }
		elseif ( $operation === 'sql-delete-duplicate' ) { $result = $wpdb->delete( $wpdb->usermeta, array( 'umeta_id' => race_mid( $duplicate, '09129991004' ) ) ); }
		elseif ( $operation === 'sql-add-duplicate' ) { $result = $wpdb->insert( $wpdb->usermeta, array( 'user_id' => $b, 'meta_key' => 'cb_mobile', 'meta_value' => '09129991004' ) ); }
		elseif ( $operation === 'wp-mobile-email-a' ) { $result = add_user_meta( $a, 'cb_mobile', '09129991010' ); }
		elseif ( $operation === 'sql-email-b' ) { $result = $wpdb->update( $wpdb->users, array( 'user_email' => '09129991010' ), array( 'ID' => $b ) ); }
		elseif ( $operation === 'snapshot-delete-user' ) { $result = $wpdb->delete( $wpdb->users, array( 'ID' => $deleting ) ); }
		elseif ( $operation === 'snapshot-add-meta' ) { $result = $wpdb->insert( $wpdb->usermeta, array( 'user_id' => $deleting, 'meta_key' => 'cb_mobile', 'meta_value' => '09129991005' ) ); }
		else {
			$target = $operation === 'deadlock-a' ? 'deadlock_a' : 'deadlock_b';
			$email = $operation === 'deadlock-a' ? 'deadlock_b@example.test' : 'deadlock_a@example.test';
			$result = $wpdb->update( $wpdb->users, array( 'user_email' => $email ), array( 'ID' => race_id( $target ) ) );
		}
		$applied = $result !== false && $result !== 0;
		$error = $applied ? 0 : ( isset( $wpdb->dbh ) && $wpdb->dbh instanceof mysqli ? mysqli_errno( $wpdb->dbh ) : 0 );
		if ( $snapshot ) {
			echo $applied ? "EXEC APPLIED\n" : "EXEC REJECTED\n"; flush();
			if ( trim( (string) fgets( STDIN ) ) !== 'COMMIT' ) { exit( 3 ); }
		}
		if ( $snapshot && $wpdb->query( $applied ? 'COMMIT' : 'ROLLBACK' ) === false ) { throw new RuntimeException( 'transaction' ); }
		if ( $deadlock ) { $wpdb->query( 'ROLLBACK' ); }
		if ( in_array( $operation, array( 'wp-add-a', 'wp-mobile-email-a' ), true ) ) {
			echo $applied && $pre && $post ? "WP_APPLIED_PRE_POST\n" : ( ! $applied && $pre && ! $post ? 'WP_REJECTED_PRE_DB_' . $error . "\n" : "UNEXPECTED\n" );
		} else { echo $applied ? "APPLIED\n" : 'REJECTED_DB_' . $error . "\n"; }
		exit( 0 );
	}

	// Runs the unchanged sequential setup and baseline on a fresh, disposable prefix.
	require __DIR__ . '/experiment-identity-shared-references.php';
	global $wpdb;
	$a = shared_user( 'race_a', 'race_a@example.test' ); $b = shared_user( 'race_b', 'race_b@example.test' );
	$dup = shared_user( '09129991004', 'duplicate@example.test' ); $delete = shared_user( 'race_delete', 'delete@example.test' );
	$dead_a = shared_user( 'deadlock_a', 'deadlock_a@example.test' ); $dead_b = shared_user( 'deadlock_b', 'deadlock_b@example.test' );
	add_user_meta( $a, 'cb_mobile', '09129991002' ); add_user_meta( $dup, 'cb_mobile', '09129991004' ); add_user_meta( $dup, 'cb_mobile', '09129991004' );
	$results = race_pair( 'wp-add-a', 'sql-add-b' );
	$wp_won = $results[0] === 'WP_APPLIED_PRE_POST'; $sql_won = $results[1] === 'APPLIED';
	race_check( $wp_won !== $sql_won && shared_owner( '09129991001' ) === ( $wp_won ? $a : $b ) && shared_refs( '09129991001' ) === 1 && race_consistent(), 'simultaneous WordPress and SQL mobile insert keep one owner' );
	$results = race_pair( 'wp-update-a', 'sql-login-b' );
	$wp_won = $results[0] === 'APPLIED'; $sql_won = $results[1] === 'APPLIED';
	race_check( $wp_won !== $sql_won && shared_owner( '09129991003' ) === ( $wp_won ? $a : $b ) && ( race_meta_exists( $a, '09129991003' ) === $wp_won ) && race_consistent(), 'simultaneous WordPress mobile update versus SQL login keep one owner' );
	// The previous race may have changed B's login; resolve it for later workers.
	if ( race_id( 'race_b' ) === 0 ) { race_check( $wpdb->update( $wpdb->users, array( 'user_login' => 'race_b' ), array( 'ID' => $b ) ) === 1, 'restore synthetic contender login' ); }
	$results = race_pair( 'sql-delete-duplicate', 'sql-add-duplicate' );
	race_check( $results[0] === 'APPLIED' && strpos( $results[1], 'REJECTED_DB_' ) === 0 && shared_owner( '09129991004' ) === $dup && shared_refs( '09129991004' ) === 2 && race_consistent(), 'deleting one duplicate while another owner adds keeps original claim' );
	$results = race_pair( 'wp-mobile-email-a', 'sql-email-b' );
	$wp_won = $results[0] === 'WP_APPLIED_PRE_POST'; $sql_won = $results[1] === 'APPLIED';
	race_check( $wp_won !== $sql_won && shared_owner( '09129991010' ) === ( $wp_won ? $a : $b ) && shared_refs( '09129991010' ) === 1 && race_consistent(), 'simultaneous WordPress mobile versus SQL email keep one owner' );
	$mid = race_mid( $dup, '09129991004' );
	$result = $wpdb->update( $wpdb->usermeta, array( 'user_id' => $b ), array( 'umeta_id' => $mid ) );
	race_check( $result === false && shared_owner( '09129991004' ) === $dup && shared_refs( '09129991004' ) === 2 && race_consistent(), 'reassignment rejected while original owner has other references' );
	$solo = add_user_meta( $a, 'cb_mobile', '09129991008' );
	$result = $wpdb->update( $wpdb->usermeta, array( 'user_id' => $b ), array( 'umeta_id' => $solo ) );
	race_check( $result === 1 && shared_owner( '09129991008' ) === $b && shared_refs( '09129991008' ) === 1 && race_meta_exists( $b, '09129991008' ) && race_consistent(), 'explicit single-reference reassignment tracks new owner' );
	$result = $wpdb->insert( $wpdb->usermeta, array( 'user_id' => 987654321, 'meta_key' => 'cb_mobile', 'meta_value' => '09129991006' ) );
	race_check( $result === false && shared_owner( '09129991006' ) === 0 && race_consistent(), 'missing owner rejected without dangling claim' );
	$result = $wpdb->insert( $wpdb->usermeta, array( 'user_id' => $b, 'meta_key' => 'cb_mobile', 'meta_value' => 'not-a-phone' ) );
	race_check( $result === false && race_consistent(), 'unsupported value rejected without dangling reference' );
	$pre = 0; $post = 0;
	add_action( 'add_user_meta', static function () use ( &$pre ) { ++$pre; } );
	add_action( 'added_user_meta', static function () use ( &$post ) { ++$post; } );
	$wpdb->query( 'START TRANSACTION' ); $result = add_user_meta( $a, 'cb_mobile', '09129991007' ); $wpdb->query( 'ROLLBACK' );
	race_check( $result !== false && $pre === 1 && $post === 1 && ! race_meta_exists( $a, '09129991007' ) && shared_owner( '09129991007' ) === 0 && race_consistent(), 'rollback removes database changes but both WordPress callbacks already ran' );
	$results = race_pair( 'deadlock-a', 'deadlock-b' );
	race_check( in_array( 'REJECTED_DB_1213', $results, true ) && shared_owner( 'deadlock_a@example.test' ) === $dead_a && shared_owner( 'deadlock_b@example.test' ) === $dead_b && race_consistent(), 'forced database deadlock rolls back without changing ownership' );
	$fault_user = shared_user( 'fault_delete', 'fault_delete@example.test' );
	$fault_mid = (int) add_user_meta( $fault_user, 'cb_mobile', '09129991009' );
	$before_count = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->usermeta} WHERE user_id=%d", $fault_user ) );
	$fault_sql = "CREATE TRIGGER `{$prefix}fixture_fault_meta_bd` BEFORE DELETE ON `{$prefix}usermeta` FOR EACH ROW BEGIN IF OLD.umeta_id=" . $fault_mid . " THEN SIGNAL SQLSTATE '45000'; END IF; END";
	race_check( $fault_mid > 0 && $wpdb->query( $fault_sql ) !== false, 'install scoped synthetic metadata deletion fault' );
	$meta_callback = false; $user_callback = false;
	add_action( 'deleted_user_meta', static function ( $ids, $id, $key ) use ( &$meta_callback, $fault_user ) { if ( (int) $id === $fault_user && $key === 'cb_mobile' ) { $meta_callback = true; } }, 10, 3 );
	add_action( 'deleted_user', static function ( $id ) use ( &$user_callback, $fault_user ) { if ( (int) $id === $fault_user ) { $user_callback = true; } } );
	$result = wp_delete_user( $fault_user );
	$after_count = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->usermeta} WHERE user_id=%d", $fault_user ) );
	$user_survives = race_id( 'fault_delete' ) === $fault_user; $mobile_survives = race_meta_exists( $fault_user, '09129991009' );
	echo 'OBSERVATION wp-delete-failure returns_true=' . (int) ( $result === true ) . ' user_survives=' . (int) $user_survives . ' mobile_survives=' . (int) $mobile_survives . ' metadata_partial=' . (int) ( $after_count < $before_count ) . ' deleted_meta_callback=' . (int) $meta_callback . ' deleted_user_callback=' . (int) $user_callback . "\n";
	race_check( $result === true && $user_survives && $mobile_survives && $after_count < $before_count && $meta_callback && $user_callback && shared_owner( '09129991009' ) === $fault_user && race_consistent(), 'WordPress reports deletion despite injected failure and partially deleted metadata' );
	$results = race_pair( 'snapshot-delete-user', 'snapshot-add-meta' );
	$exists = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE ID=%d", $delete ) );
	$orphan = race_meta_exists( $delete, '09129991005' ); $owner = shared_owner( '09129991005' ); $consistent = race_consistent();
	echo 'OBSERVATION snapshot-delete-add user_exists=' . $exists . ' mobile_exists=' . (int) $orphan . ' claim_matches_deleted_owner=' . (int) ( $owner === $delete ) . ' consistent=' . (int) $consistent . "\n";
	if ( ! $consistent ) { echo "BLOCKER concurrent deletion can leave orphaned mobile identity.\n"; }
	echo "BLOCKER WordPress reports deletion and runs callbacks although injected metadata failure left the user partially modified.\n";
	exit( 4 );
} catch ( Throwable $e ) {
	$known = array( 'assertion', 'fixture-user', 'worker-start', 'worker-barrier', 'worker-result', 'transaction' );
	$category = in_array( $e->getMessage(), $known, true ) ? $e->getMessage() : get_class( $e );
	fwrite( STDERR, 'FAIL synthetic concurrency experiment: ' . $category . "\n" ); exit( 1 );
}
