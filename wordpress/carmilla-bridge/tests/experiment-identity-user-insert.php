<?php
/** INSERT-only research fixture. Never install this schema on a real site. */
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

function assert_fixture( $condition, $label ) {
	if ( ! $condition ) { echo 'FAIL ' . $label . "\n"; throw new RuntimeException( 'assertion' ); }
	echo 'PASS ' . $label . "\n";
}

function insert_direct( $login, $email ) {
	global $wpdb;
	return $wpdb->insert( $wpdb->users, array(
		'user_login' => $login, 'user_pass' => 'synthetic-only-password', 'user_nicename' => $login,
		'user_email' => $email, 'user_url' => '', 'user_registered' => '2026-01-01 00:00:00',
		'user_activation_key' => '', 'user_status' => 0, 'display_name' => $login,
	) );
}

function claims_count( $key ) {
	global $wpdb, $prefix;
	return (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM `{$prefix}identity_claims` WHERE claim_key = %s", $key ) );
}

function users_count( $login ) {
	global $wpdb;
	return (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE BINARY user_login = BINARY %s", $login ) );
}

function race( $mode, $left, $right ) {
	$workers = array(); $descriptors = array( array( 'pipe', 'r' ), array( 'pipe', 'w' ), array( 'pipe', 'w' ) );
	try {
		foreach ( array( $left, $right ) as $fixture ) {
			$pipes = array(); $process = proc_open( array( PHP_BINARY, __FILE__, 'worker', $mode, $fixture ), $descriptors, $pipes );
			if ( ! is_resource( $process ) ) { throw new RuntimeException( 'worker-start' ); }
			stream_set_timeout( $pipes[1], 20 ); $workers[] = array( $process, $pipes );
		}
		foreach ( $workers as $worker ) { if ( trim( (string) fgets( $worker[1][1] ) ) !== 'READY' ) { throw new RuntimeException( 'worker-barrier' ); } }
		foreach ( $workers as $worker ) { fwrite( $worker[1][0], "GO\n" ); fclose( $worker[1][0] ); }
		$results = array();
		foreach ( $workers as $i => $worker ) {
			$result = trim( (string) stream_get_contents( $worker[1][1] ) ); fclose( $worker[1][1] ); fclose( $worker[1][2] );
			$status = proc_close( $worker[0] ); unset( $workers[ $i ] );
			if ( $status !== 0 || ! in_array( $result, array( 'CREATED', 'REJECTED', 'ZERO_ID', 'ZERO_ID_HOOK' ), true ) ) { throw new RuntimeException( 'worker-result' ); }
			$results[] = $result;
		}
		sort( $results ); return $results;
	} finally {
		foreach ( $workers as $worker ) { foreach ( $worker[1] as $pipe ) { if ( is_resource( $pipe ) ) { fclose( $pipe ); } } if ( is_resource( $worker[0] ) ) { proc_terminate( $worker[0] ); proc_close( $worker[0] ); } }
	}
}

try {
	require ABSPATH . 'wp-settings.php';
	global $wpdb; $wpdb->hide_errors(); $wpdb->suppress_errors( true );
	$fixtures = array(
		'same' => array( 'race_user', 'race@example.test' ),
		'cross-a' => array( 'cross_a', 'cross@example.test' ),
		'cross-b' => array( 'cross@example.test', 'cross_b@example.test' ),
		'direct' => array( 'direct_race', 'direct_race@example.test' ),
		'independent-a' => array( 'independent_a', 'independent_a@example.test' ),
		'independent-b' => array( 'independent_b', 'independent_b@example.test' ),
	);
	if ( ( $argv[1] ?? '' ) === 'worker' ) {
		$mode = $argv[2] ?? ''; $fixture = $fixtures[ $argv[3] ?? '' ] ?? null;
		if ( ! $fixture || ! in_array( $mode, array( 'wp', 'sql' ), true ) ) { exit( 2 ); }
		if ( $mode === 'wp' ) {
			$zero_id_hook = false;
			add_action( 'user_register', static function ( $id ) use ( &$zero_id_hook ) { if ( (int) $id === 0 ) { $zero_id_hook = true; } } );
			add_filter( 'pre_user_url', static function ( $value ) { echo "READY\n"; flush(); if ( trim( (string) fgets( STDIN ) ) !== 'GO' ) { exit( 3 ); } return $value; } );
			$id = wp_insert_user( array( 'user_login' => $fixture[0], 'user_email' => $fixture[1], 'user_pass' => 'synthetic-only-password' ) );
			if ( is_wp_error( $id ) ) { echo "REJECTED\n"; }
			elseif ( (int) $id === 0 ) { echo $zero_id_hook ? "ZERO_ID_HOOK\n" : "ZERO_ID\n"; }
			else { echo "CREATED\n"; }
		} else {
			echo "READY\n"; flush(); if ( trim( (string) fgets( STDIN ) ) !== 'GO' ) { exit( 3 ); }
			echo insert_direct( $fixture[0], $fixture[1] ) === 1 ? "CREATED\n" : "REJECTED\n";
		}
		exit( 0 );
	}

	require_once ABSPATH . 'wp-admin/includes/upgrade.php'; add_filter( 'pre_wp_mail', static function () { return true; } );
	wp_install( 'Synthetic QA', 'qa_admin', 'synthetic@example.test', false, '', 'synthetic-only-password' );
	$engine = $wpdb->get_var( $wpdb->prepare( 'SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = %s', $prefix . 'users' ) );
	assert_fixture( strtoupper( (string) $engine ) === 'INNODB', 'synthetic user table is transactional' );
	// Existing installation admin is deliberately not backfilled: migration is outside this fixture.
	$table = "`{$prefix}identity_claims`"; $trigger = "`{$prefix}identity_users_ai`";
	assert_fixture( $wpdb->query( "CREATE TABLE $table (claim_key VARBINARY(255) NOT NULL PRIMARY KEY, owner_id BIGINT UNSIGNED NOT NULL) ENGINE=InnoDB" ) !== false, 'create synthetic claims table' );
	$sql = "CREATE TRIGGER $trigger AFTER INSERT ON `{$prefix}users` FOR EACH ROW BEGIN INSERT INTO $table (claim_key, owner_id) VALUES (CAST(NEW.user_login AS BINARY), NEW.ID); IF NEW.user_email <> '' AND BINARY NEW.user_email <> BINARY NEW.user_login THEN INSERT INTO $table (claim_key, owner_id) VALUES (CAST(NEW.user_email AS BINARY), NEW.ID); END IF; END";
	assert_fixture( $wpdb->query( $sql ) !== false, 'create synthetic insert trigger' );
	$same_results = race( 'wp', 'same', 'same' );
	assert_fixture( $same_results === array( 'CREATED', 'ZERO_ID_HOOK' ) && users_count( 'race_user' ) === 1 && claims_count( 'race_user' ) === 1 && claims_count( 'race@example.test' ) === 1, 'concurrent WordPress identical identities; failed insert returns zero and fires user_register' );
	$cross_results = race( 'wp', 'cross-a', 'cross-b' );
	assert_fixture( $cross_results === array( 'CREATED', 'ZERO_ID_HOOK' ) && users_count( 'cross_a' ) + users_count( 'cross@example.test' ) === 1 && claims_count( 'cross@example.test' ) === 1 && claims_count( 'cross_a' ) === users_count( 'cross_a' ) && claims_count( 'cross_b@example.test' ) === users_count( 'cross@example.test' ), 'concurrent WordPress cross-namespace conflict; failed insert returns zero and fires user_register' );
	assert_fixture( race( 'sql', 'direct', 'direct' ) === array( 'CREATED', 'REJECTED' ) && users_count( 'direct_race' ) === 1 && claims_count( 'direct_race' ) === 1 && claims_count( 'direct_race@example.test' ) === 1, 'concurrent uncooperative SQL identities' );
	assert_fixture( race( 'wp', 'independent-a', 'independent-b' ) === array( 'CREATED', 'CREATED' ) && users_count( 'independent_a' ) === 1 && users_count( 'independent_b' ) === 1, 'concurrent independent WordPress accounts' );
	$alias = wp_insert_user( array( 'user_login' => 'alias@example.test', 'user_email' => 'alias@example.test', 'user_pass' => 'synthetic-only-password' ) );
	assert_fixture( ! is_wp_error( $alias ) && users_count( 'alias@example.test' ) === 1 && claims_count( 'alias@example.test' ) === 1, 'identical login and email for same owner' );
	assert_fixture( insert_direct( 'race_user', 'unused@example.test' ) === false && users_count( 'race_user' ) === 1 && claims_count( 'unused@example.test' ) === 0, 'direct duplicate login rejected' );
	assert_fixture( insert_direct( 'new_conflict', 'race@example.test' ) === false && users_count( 'new_conflict' ) === 0 && claims_count( 'new_conflict' ) === 0, 'statement conflict rolls back earlier claim' );
	assert_fixture( insert_direct( 'race@example.test', 'different@example.test' ) === false && users_count( 'race@example.test' ) === 0 && claims_count( 'different@example.test' ) === 0, 'direct cross-namespace conflict' );
	$wpdb->query( 'START TRANSACTION' );
	$inserted = insert_direct( 'rollback_user', 'rollback@example.test' ) === 1;
	$wpdb->query( 'ROLLBACK' );
	assert_fixture( $inserted && users_count( 'rollback_user' ) === 0 && claims_count( 'rollback_user' ) === 0 && claims_count( 'rollback@example.test' ) === 0, 'transaction rollback removes user and claims' );
	echo "INSERT-only synthetic research; not production protection.\n";
} catch ( Throwable $e ) {
	$known = array( 'worker-start', 'worker-barrier', 'worker-result', 'assertion' );
	$category = in_array( $e->getMessage(), $known, true ) ? $e->getMessage() : get_class( $e );
	fwrite( STDERR, 'FAIL synthetic insert experiment: ' . $category . "\n" ); exit( 1 );
}
