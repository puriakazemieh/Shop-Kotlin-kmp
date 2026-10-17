<?php
/** UPDATE/DELETE research fixture, building on the isolated INSERT experiment only. */
$prefix = getenv( 'CARMILLA_LOGIN_TEST_PREFIX' );
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) ) {
	fwrite( STDERR, "Disposable test flag and random prefix required.\n" ); exit( 2 );
}
define( 'ABSPATH', '/usr/src/wordpress/' ); define( 'WP_INSTALLING', true ); define( 'WP_DEBUG', false );
define( 'DB_NAME', 'carmilla_login_qa' ); define( 'DB_USER', 'synthetic_qa' ); define( 'DB_PASSWORD', 'synthetic_qa_password' ); define( 'DB_HOST', '127.0.0.1:3306' );
define( 'DB_CHARSET', 'utf8mb4' ); define( 'DB_COLLATE', '' ); define( 'WP_HOME', 'https://qa.example.test/wordpress' ); define( 'WP_SITEURL', WP_HOME );
foreach ( array( 'AUTH_KEY', 'SECURE_AUTH_KEY', 'LOGGED_IN_KEY', 'NONCE_KEY', 'AUTH_SALT', 'SECURE_AUTH_SALT', 'LOGGED_IN_SALT', 'NONCE_SALT' ) as $name ) { define( $name, hash( 'sha256', 'synthetic-wp-' . $name ) ); }
$table_prefix = $prefix;
$_SERVER['HTTP_HOST'] = 'qa.example.test'; $_SERVER['SERVER_NAME'] = 'qa.example.test'; $_SERVER['SERVER_PORT'] = '443'; $_SERVER['HTTPS'] = 'on'; $_SERVER['REQUEST_METHOD'] = 'GET';

function verify_case( $result, $label ) {
	if ( ! $result ) { echo 'FAIL ' . $label . "\n"; throw new RuntimeException( 'assertion' ); }
	echo 'PASS ' . $label . "\n";
}
function fixture_user( $login, $email ) {
	$id = wp_insert_user( array( 'user_login' => $login, 'user_email' => $email, 'user_pass' => 'synthetic-only-password' ) );
	if ( is_wp_error( $id ) || (int) $id < 1 ) { throw new RuntimeException( 'fixture-user' ); }
	return (int) $id;
}
function claim_owner( $key ) {
	global $wpdb, $prefix;
	return (int) $wpdb->get_var( $wpdb->prepare( "SELECT owner_id FROM `{$prefix}identity_claims` WHERE claim_key=%s", $key ) );
}
function row_id( $login ) {
	global $wpdb;
	return (int) $wpdb->get_var( $wpdb->prepare( "SELECT ID FROM {$wpdb->users} WHERE BINARY user_login=BINARY %s", $login ) );
}
function db_email( $id ) {
	global $wpdb;
	return (string) $wpdb->get_var( $wpdb->prepare( "SELECT user_email FROM {$wpdb->users} WHERE ID=%d", $id ) );
}
function direct_add( $login, $email ) {
	global $wpdb;
	return $wpdb->insert( $wpdb->users, array( 'user_login' => $login, 'user_pass' => 'synthetic-only-password', 'user_nicename' => $login, 'user_email' => $email, 'user_url' => '', 'user_registered' => '2026-01-01 00:00:00', 'user_activation_key' => '', 'user_status' => 0, 'display_name' => $login ) );
}
function run_pair( $first, $second ) {
	$workers = array(); $descriptors = array( array( 'pipe', 'r' ), array( 'pipe', 'w' ), array( 'pipe', 'w' ) );
	try {
		foreach ( array( $first, $second ) as $operation ) {
			$pipes = array(); $process = proc_open( array( PHP_BINARY, __FILE__, 'worker', $operation ), $descriptors, $pipes );
			if ( ! is_resource( $process ) ) { throw new RuntimeException( 'worker-start' ); }
			stream_set_timeout( $pipes[1], 20 ); $workers[] = array( $process, $pipes );
		}
		foreach ( $workers as $worker ) { if ( trim( (string) fgets( $worker[1][1] ) ) !== 'READY' ) { throw new RuntimeException( 'worker-barrier' ); } }
		foreach ( $workers as $worker ) { fwrite( $worker[1][0], "GO\n" ); fclose( $worker[1][0] ); }
		$results = array();
		foreach ( $workers as $i => $worker ) {
			$result = trim( (string) stream_get_contents( $worker[1][1] ) ); fclose( $worker[1][1] ); fclose( $worker[1][2] );
			$status = proc_close( $worker[0] ); unset( $workers[ $i ] );
			if ( $status !== 0 || ! in_array( $result, array( 'APPLIED', 'REJECTED', 'NOT_FOUND' ), true ) ) { throw new RuntimeException( 'worker-result' ); }
			$results[] = $result;
		}
		return $results;
	} finally {
		foreach ( $workers as $worker ) { foreach ( $worker[1] as $pipe ) { if ( is_resource( $pipe ) ) { fclose( $pipe ); } } if ( is_resource( $worker[0] ) ) { proc_terminate( $worker[0] ); proc_close( $worker[0] ); } }
	}
}

try {
	// The previous isolated INSERT fixture seeds a fresh installation and test-only claims.
	if ( ( $argv[1] ?? '' ) !== 'worker' ) {
		$pipes = array(); $seed = proc_open( array( PHP_BINARY, __DIR__ . '/experiment-identity-user-insert.php' ), array( array( 'pipe', 'r' ), array( 'pipe', 'w' ), array( 'pipe', 'w' ) ), $pipes );
		if ( ! is_resource( $seed ) ) { throw new RuntimeException( 'seed' ); }
		fclose( $pipes[0] ); $output = stream_get_contents( $pipes[1] ); fclose( $pipes[1] ); stream_get_contents( $pipes[2] ); fclose( $pipes[2] );
		if ( proc_close( $seed ) !== 0 ) { throw new RuntimeException( 'seed' ); }
		if ( strpos( $output, 'INSERT-only synthetic research; not production protection.' ) === false ) { throw new RuntimeException( 'seed' ); }
		echo "PASS isolated insert fixture baseline\n";
	}
	require ABSPATH . 'wp-settings.php'; global $wpdb; $wpdb->hide_errors(); $wpdb->suppress_errors( true );
	// Never send notification email during synthetic profile updates.
	add_filter( 'pre_wp_mail', static function () { return true; } );
	if ( ( $argv[1] ?? '' ) === 'worker' ) {
		$operation = $argv[2] ?? '';
		$allowed = array( 'change-a', 'change-b', 'insert-contender', 'update-contender', 'delete-release', 'insert-release', 'delete-same', 'change-same', 'insert-wp-contender', 'wp-update-contender' );
		if ( ! in_array( $operation, $allowed, true ) ) { exit( 2 ); }
		if ( $operation === 'wp-update-contender' ) {
			$id = row_id( 'wp_contender' ); if ( ! $id ) { exit( 2 ); }
			add_filter( 'pre_user_url', static function ( $value ) { echo "READY\n"; flush(); if ( trim( (string) fgets( STDIN ) ) !== 'GO' ) { exit( 3 ); } return $value; } );
			$result = wp_update_user( array( 'ID' => $id, 'user_email' => 'wp_contended@example.test' ) );
			echo ! is_wp_error( $result ) && db_email( $id ) === 'wp_contended@example.test' ? "APPLIED\n" : "REJECTED\n"; exit( 0 );
		}
		echo "READY\n"; flush(); if ( trim( (string) fgets( STDIN ) ) !== 'GO' ) { exit( 3 ); }
		if ( $operation === 'insert-contender' ) { $result = direct_add( 'contender_insert', 'contended@example.test' ); }
		elseif ( $operation === 'insert-wp-contender' ) { $result = direct_add( 'wp_contender_insert', 'wp_contended@example.test' ); }
		elseif ( $operation === 'insert-release' ) { $result = direct_add( 'new_release', 'released@example.test' ); }
		else {
			$logins = array( 'change-a' => 'changer_a', 'change-b' => 'changer_b', 'update-contender' => 'contender_update', 'delete-release' => 'old_release', 'delete-same' => 'same_row', 'change-same' => 'same_row' );
			$id = row_id( $logins[ $operation ] );
			if ( ! $id ) { echo "NOT_FOUND\n"; exit( 0 ); }
			if ( strpos( $operation, 'delete-' ) === 0 ) { $result = $wpdb->delete( $wpdb->users, array( 'ID' => $id ) ); }
			else {
				$email = in_array( $operation, array( 'change-a', 'change-b' ), true ) ? 'shared_change@example.test' : ( $operation === 'update-contender' ? 'contended@example.test' : 'same_new@example.test' );
				$result = $wpdb->update( $wpdb->users, array( 'user_email' => $email ), array( 'ID' => $id ) );
			}
		}
		echo $result === false ? "REJECTED\n" : ( $result === 0 ? "NOT_FOUND\n" : "APPLIED\n" ); exit( 0 );
	}
	$table = "`{$prefix}identity_claims`"; $users = "`{$prefix}users`";
	// Only references in the two fields of this row are modeled. All comparisons are exact binary.
	$update = "CREATE TRIGGER `{$prefix}identity_users_au` AFTER UPDATE ON $users FOR EACH ROW BEGIN
		IF OLD.ID <> NEW.ID THEN SIGNAL SQLSTATE '45000'; END IF;
		IF BINARY OLD.user_login <> BINARY NEW.user_login AND BINARY OLD.user_login <> BINARY NEW.user_email THEN DELETE FROM $table WHERE claim_key=CAST(OLD.user_login AS BINARY) AND owner_id=OLD.ID; END IF;
		IF OLD.user_email <> '' AND BINARY OLD.user_email <> BINARY NEW.user_login AND BINARY OLD.user_email <> BINARY NEW.user_email THEN DELETE FROM $table WHERE claim_key=CAST(OLD.user_email AS BINARY) AND owner_id=OLD.ID; END IF;
		IF BINARY NEW.user_login <> BINARY OLD.user_login AND BINARY NEW.user_login <> BINARY OLD.user_email THEN INSERT INTO $table (claim_key,owner_id) VALUES (CAST(NEW.user_login AS BINARY),NEW.ID); END IF;
		IF NEW.user_email <> '' AND BINARY NEW.user_email <> BINARY NEW.user_login AND BINARY NEW.user_email <> BINARY OLD.user_login AND BINARY NEW.user_email <> BINARY OLD.user_email THEN INSERT INTO $table (claim_key,owner_id) VALUES (CAST(NEW.user_email AS BINARY),NEW.ID); END IF;
	END";
	$delete = "CREATE TRIGGER `{$prefix}identity_users_ad` AFTER DELETE ON $users FOR EACH ROW BEGIN DELETE FROM $table WHERE owner_id=OLD.ID AND (claim_key=CAST(OLD.user_login AS BINARY) OR (OLD.user_email <> '' AND claim_key=CAST(OLD.user_email AS BINARY))); END";
	verify_case( $wpdb->query( $update ) !== false && $wpdb->query( $delete ) !== false, 'create isolated lifecycle triggers' );

	$alias = fixture_user( 'lifecycle_alias@example.test', 'lifecycle_alias@example.test' );
	$result = wp_update_user( array( 'ID' => $alias, 'user_email' => 'changed@example.test' ) );
	verify_case( ! is_wp_error( $result ) && (int) $result === $alias && db_email( $alias ) === 'changed@example.test' && claim_owner( 'lifecycle_alias@example.test' ) === $alias && claim_owner( 'changed@example.test' ) === $alias, 'WordPress update preserves same-owner login reference' );
	$result = wp_update_user( array( 'ID' => $alias, 'user_email' => 'lifecycle_alias@example.test' ) );
	verify_case( ! is_wp_error( $result ) && db_email( $alias ) === 'lifecycle_alias@example.test' && claim_owner( 'lifecycle_alias@example.test' ) === $alias && claim_owner( 'changed@example.test' ) === 0, 'WordPress update restores alias and releases unused email' );
	$other = fixture_user( 'different_user', 'different@example.test' );
	$result = $wpdb->update( $wpdb->users, array( 'user_email' => 'different@example.test' ), array( 'ID' => $alias ) );
	verify_case( $result === false && db_email( $alias ) === 'lifecycle_alias@example.test' && claim_owner( 'lifecycle_alias@example.test' ) === $alias && claim_owner( 'different@example.test' ) === $other, 'conflicting direct update rolls back claims and owner' );
	$result = $wpdb->update( $wpdb->users, array( 'user_login' => 'different@example.test' ), array( 'ID' => $alias ) );
	verify_case( $result === false && row_id( 'lifecycle_alias@example.test' ) === $alias && claim_owner( 'different@example.test' ) === $other, 'cross-namespace login update rejected' );
	$result = $wpdb->update( $wpdb->users, array( 'ID' => $alias + 100000 ), array( 'ID' => $alias ) );
	verify_case( $result === false && row_id( 'lifecycle_alias@example.test' ) === $alias && claim_owner( 'lifecycle_alias@example.test' ) === $alias, 'unmodeled source owner ID change rejected' );
	$switch = fixture_user( 'switch_old', 'switch_old@example.test' );
	$result = $wpdb->update( $wpdb->users, array( 'user_login' => 'switch_new', 'user_email' => 'switch_old' ), array( 'ID' => $switch ) );
	verify_case( $result === 1 && row_id( 'switch_new' ) === $switch && claim_owner( 'switch_new' ) === $switch && claim_owner( 'switch_old' ) === $switch && claim_owner( 'switch_old@example.test' ) === 0, 'direct update moves and retains same-owner cross-namespace references' );
	$result = $wpdb->update( $wpdb->users, array( 'user_email' => 'switch_new' ), array( 'ID' => $switch ) );
	verify_case( $result === 1 && claim_owner( 'switch_new' ) === $switch && claim_owner( 'switch_old' ) === 0, 'direct update collapses alias without releasing active claim' );
	$cross_owner = fixture_user( 'owned_cross@example.test', 'owner_distinct@example.test' );
	$hook_fired = false; add_action( 'profile_update', static function ( $id ) use ( &$hook_fired, $alias ) { if ( (int) $id === $alias ) { $hook_fired = true; } } );
	$result = wp_update_user( array( 'ID' => $alias, 'user_email' => 'owned_cross@example.test' ) );
	verify_case( ! is_wp_error( $result ) && (int) $result === $alias && $hook_fired && db_email( $alias ) === 'lifecycle_alias@example.test' && claim_owner( 'owned_cross@example.test' ) === $cross_owner, 'rejected WordPress update returns user ID and fires profile_update despite unchanged row' );
	$wpdb->query( 'START TRANSACTION' ); $result = $wpdb->update( $wpdb->users, array( 'user_email' => 'rolledback@example.test' ), array( 'ID' => $alias ) ); $wpdb->query( 'ROLLBACK' );
	verify_case( $result === 1 && db_email( $alias ) === 'lifecycle_alias@example.test' && claim_owner( 'rolledback@example.test' ) === 0 && claim_owner( 'lifecycle_alias@example.test' ) === $alias, 'transaction rollback restores user and references' );
	$wpdb->query( 'START TRANSACTION' ); $result = $wpdb->delete( $wpdb->users, array( 'ID' => $other ) ); $wpdb->query( 'ROLLBACK' );
	verify_case( $result === 1 && row_id( 'different_user' ) === $other && claim_owner( 'different@example.test' ) === $other, 'transaction rollback restores deleted user and claims' );
	require_once ABSPATH . 'wp-admin/includes/user.php';
	verify_case( wp_delete_user( $alias ) && ! row_id( 'lifecycle_alias@example.test' ) && claim_owner( 'lifecycle_alias@example.test' ) === 0, 'WordPress delete releases last user-row reference' );

	$a = fixture_user( 'changer_a', 'change_a@example.test' ); $b = fixture_user( 'changer_b', 'change_b@example.test' );
	$results = run_pair( 'change-a', 'change-b' ); $winner = claim_owner( 'shared_change@example.test' );
	verify_case( count( array_filter( $results, static function ( $r ) { return $r === 'APPLIED'; } ) ) === 1 && in_array( $winner, array( $a, $b ), true ) && (int) ( db_email( $a ) === 'shared_change@example.test' ) + (int) ( db_email( $b ) === 'shared_change@example.test' ) === 1 && claim_owner( 'change_a@example.test' ) === ( $winner === $a ? 0 : $a ) && claim_owner( 'change_b@example.test' ) === ( $winner === $b ? 0 : $b ), 'concurrent direct changes preserve exactly one owner' );
	$c = fixture_user( 'contender_update', 'contender_old@example.test' ); $results = run_pair( 'insert-contender', 'update-contender' ); $inserted = row_id( 'contender_insert' ); $winner = claim_owner( 'contended@example.test' );
	verify_case( count( array_filter( $results, static function ( $r ) { return $r === 'APPLIED'; } ) ) === 1 && $winner > 0 && ( $winner === $inserted || $winner === $c ) && (int) ( $inserted > 0 ) + (int) ( db_email( $c ) === 'contended@example.test' ) === 1 && claim_owner( 'contender_old@example.test' ) === ( $winner === $c ? 0 : $c ), 'concurrent insert versus update preserves owner' );
	$c = fixture_user( 'wp_contender', 'wp_contender_old@example.test' ); $results = run_pair( 'insert-wp-contender', 'wp-update-contender' ); $inserted = row_id( 'wp_contender_insert' ); $winner = claim_owner( 'wp_contended@example.test' );
	verify_case( count( array_filter( $results, static function ( $r ) { return $r === 'APPLIED'; } ) ) === 1 && $winner > 0 && ( $winner === $inserted || $winner === $c ) && (int) ( $inserted > 0 ) + (int) ( db_email( $c ) === 'wp_contended@example.test' ) === 1 && claim_owner( 'wp_contender_old@example.test' ) === ( $winner === $c ? 0 : $c ), 'concurrent independent SQL insert versus real WordPress update preserves owner' );
	fixture_user( 'old_release', 'released@example.test' ); $results = run_pair( 'delete-release', 'insert-release' ); $new = row_id( 'new_release' );
	verify_case( $results[0] === 'APPLIED' && row_id( 'old_release' ) === 0 && claim_owner( 'old_release' ) === 0 && claim_owner( 'released@example.test' ) === $new && ( $results[1] === 'APPLIED' ? $new > 0 : $new === 0 ), 'concurrent delete versus insert retains only surviving owner' );
	fixture_user( 'same_row', 'same_old@example.test' ); $results = run_pair( 'delete-same', 'change-same' );
	verify_case( $results[0] === 'APPLIED' && row_id( 'same_row' ) === 0 && claim_owner( 'same_row' ) === 0 && claim_owner( 'same_old@example.test' ) === 0 && claim_owner( 'same_new@example.test' ) === 0, 'concurrent deletion and change of one row leaves no stale claim' );
	echo "User-row lifecycle only; not production protection.\n";
} catch ( Throwable $e ) {
	$known = array( 'assertion', 'fixture-user', 'worker-start', 'worker-barrier', 'worker-result', 'seed' );
	$category = in_array( $e->getMessage(), $known, true ) ? $e->getMessage() : get_class( $e );
	fwrite( STDERR, 'FAIL synthetic lifecycle experiment: ' . $category . "\n" ); exit( 1 );
}
