<?php
/**
 * Real MariaDB/MySQL concurrency test. Use ONLY a disposable database named
 * carmilla_session_qa, with a dedicated local connection and synthetic data.
 * CARMILLA_TEST_DB_PORT selects its loopback-only port. Never point at WP data.
 */
define( 'ABSPATH', __DIR__ . '/' );
require_once dirname( __DIR__ ) . '/includes/class-cb-web-session-store.php';

$port = (int) getenv( 'CARMILLA_TEST_DB_PORT' );
if ( getenv( 'CARMILLA_SESSION_DISPOSABLE' ) !== 'yes' || $port < 1 || $port > 65535 ) {
	fwrite( STDERR, "A disposable loopback database port is required.\n" );
	exit( 2 );
}
$prefix = getenv( 'CARMILLA_TEST_TABLE_PREFIX' );
if ( ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) ) {
	fwrite( STDERR, "A random qa_ test table prefix is required.\n" );
	exit( 2 );
}
mysqli_report( MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT );
define( 'CARMILLA_TEST_DB_USER', 'synthetic_qa' );
define( 'CARMILLA_TEST_DB_PASSWORD', 'synthetic_qa_password' );
$site_a = hash( 'sha256', 'https://qa.example.test/site-a|1' );
$site_b = hash( 'sha256', 'https://qa.example.test/site-b|2' );
$secret = str_repeat( 'test-only-key-', 4 );
$now = 1700000000;
$passwords = array( 1 => 'synthetic-password-hash-a', 2 => 'synthetic-password-hash-b' );
$db = new mysqli( '127.0.0.1', CARMILLA_TEST_DB_USER, CARMILLA_TEST_DB_PASSWORD, 'carmilla_session_qa', $port );
$db->set_charset( 'utf8mb4' );
$password_provider = static function ( int $user_id ) use ( &$passwords ) { return $passwords[ $user_id ] ?? null; };
$password_db = new mysqli( '127.0.0.1', CARMILLA_TEST_DB_USER, CARMILLA_TEST_DB_PASSWORD, 'carmilla_session_qa', $port );
$shared_password_provider = static function ( int $user_id ) use ( $password_db, $prefix ) {
	$stmt = $password_db->prepare( 'SELECT password_hash FROM `' . $prefix . 'synthetic_users` WHERE user_id=?' );
	$stmt->bind_param( 'i', $user_id ); $stmt->execute();
	$row = $stmt->get_result()->fetch_assoc(); $stmt->close();
	return $row['password_hash'] ?? null;
};
$clock = static function () use ( &$now ) { return $now; };

$worker_mode = $argv[1] ?? '';
if ( in_array( $worker_mode, array( 'race-worker', 'disconnect-worker', 'revoke-worker', 'password-race-worker' ), true ) ) {
	$credential = trim( (string) fgets( STDIN ) );
	$now = (int) trim( (string) fgets( STDIN ) );
	$store = new CB_Web_Session_Store( $db, $prefix, $site_a, $secret, $worker_mode === 'password-race-worker' ? $shared_password_provider : $password_provider, $clock );
	$delayed = in_array( $worker_mode, array( 'disconnect-worker', 'password-race-worker' ), true );
	if ( $delayed ) { $db->query( 'SET @qa_wait_insert=1' ); }
	fwrite( STDOUT, $delayed ? 'READY:' . $db->thread_id . "\n" : "READY\n" );
	fflush( STDOUT );
	if ( trim( (string) fgets( STDIN ) ) !== 'GO' ) { exit( 3 ); }
	try {
		if ( $worker_mode === 'revoke-worker' ) {
			$store->revoke( $credential );
			fwrite( STDOUT, "{\"revoked\":true}\n" );
			exit( 0 );
		}
		$result = $store->rotate( $credential );
		fwrite( STDOUT, $worker_mode === 'disconnect-worker' ? "unexpected-success\n" : json_encode( array( 'result' => $result ) ) . "\n" );
	} catch ( Throwable $e ) {
		fwrite( STDOUT, $worker_mode === 'disconnect-worker' ? "rejected\n" : "{\"error\":true}\n" );
	}
	exit( 0 );
}

$checks = 0;
$failures = 0;
/** Test-only connection that drops before its next COMMIT reaches the server. */
class CB_Test_Commit_Failure_Connection extends mysqli {
	public $fail_next_commit = false;

	public function commit( int $flags = 0, ?string $name = null ): bool {
		if ( $this->fail_next_commit ) {
			$this->close();
		}
		return parent::commit( $flags, $name );
	}
}

function session_check( bool $condition, string $name ): void {
	global $checks, $failures;
	++$checks;
	if ( ! $condition ) { ++$failures; }
	echo ( $condition ? 'PASS  ' : 'FAIL  ' ) . $name . "\n";
}
function session_fails( callable $callback ): bool {
	try { $callback(); return false; } catch ( CB_Web_Session_Storage_Exception $e ) { return true; }
}
function scalar( mysqli $db, string $sql ): int {
	$result = $db->query( $sql );
	return (int) $result->fetch_row()[0];
}
function wait_for_sleep( mysqli $db, int $thread_id ): bool {
	for ( $i = 0; $i < 100; ++$i ) {
		$result = $db->query( 'SELECT STATE FROM information_schema.PROCESSLIST WHERE ID=' . $thread_id );
		$state = $result->fetch_assoc()['STATE'] ?? '';
		if ( stripos( $state, 'sleep' ) !== false ) { return true; }
		usleep( 50000 );
	}
	return false;
}

try {
	$db->query( 'CREATE TABLE `' . $prefix . 'synthetic_users` (user_id BIGINT UNSIGNED NOT NULL PRIMARY KEY,password_hash VARCHAR(100) NOT NULL) ENGINE=InnoDB' );
	$db->query( "INSERT INTO `{$prefix}synthetic_users` (user_id,password_hash) VALUES (1,'synthetic-password-hash-a')" );
	foreach ( CB_Web_Session_Store::schema_sql( $prefix ) as $ddl ) { $db->query( $ddl ); }
	$store = new CB_Web_Session_Store( $db, $prefix, $site_a, $secret, $password_provider, $clock );
	$other_site = new CB_Web_Session_Store( $db, $prefix, $site_b, $secret, $password_provider, $clock );
	$families = $prefix . 'carmilla_web_families';
	$tokens = $prefix . 'carmilla_web_tokens';

	$a = $store->create( 1 );
	$b = $store->create( 1 );
	$c = $other_site->create( 1 );
	session_check( is_array( $a ) && strlen( $a['credential'] ) === 64 && strlen( $a['family_id'] ) === 32, 'fresh credential and family use required sizes' );
	session_check( $a['credential'] !== $b['credential'] && $a['family_id'] !== $b['family_id'], 'sessions have distinct credentials and families' );
	session_check( $a['absolute_expiry'] === $now + CB_Web_Session_Store::ABSOLUTE_TTL, 'absolute expiry is fixed at creation' );
	session_check( $store->is_active( $a['family_id'], 1 ) && ! $store->is_active( $a['family_id'], 2 ), 'active family is bound to user' );
	session_check( ! $other_site->is_active( $a['family_id'], 1 ) && $other_site->is_active( $c['family_id'], 1 ), 'families are bound to site' );
	$stmt = $db->prepare( "SELECT token_hash FROM `{$tokens}` WHERE family_id=?" );
	$stmt->bind_param( 's', $a['family_id'] ); $stmt->execute();
	$stored = $stmt->get_result()->fetch_assoc()['token_hash']; $stmt->close();
	session_check( $stored === hash( 'sha256', $a['credential'] ) && $stored !== $a['credential'], 'database stores digest, never raw refresh credential' );

	$now += 500;
	$a2 = $store->rotate( $a['credential'] );
	session_check( is_array( $a2 ) && $a2['credential'] !== $a['credential'] && $a2['absolute_expiry'] === $a['absolute_expiry'], 'rotation changes credential without extending absolute expiry' );
	--$now;
	session_check( ! $store->is_active( $a['family_id'], 1 ) && $store->rotate( $a2['credential'] ) === null, 'backward clock relative to last use fails closed' );
	++$now;
	session_check( $store->rotate( $a['credential'] ) === null && ! $store->is_active( $a['family_id'], 1 ) && $store->rotate( $a2['credential'] ) === null, 'replay revokes entire affected family including replacement' );
	session_check( $store->is_active( $b['family_id'], 1 ) && $other_site->is_active( $c['family_id'], 1 ), 'replay does not revoke other devices or sites' );
	session_check( $store->rotate( 'bad' ) === null && $store->rotate( str_repeat( 'f', 64 ) ) === null && $store->is_active( $b['family_id'], 1 ), 'malformed and unknown credentials fail without revoking others' );
	$store->revoke( $b['credential'] );
	$store->revoke( $b['credential'] );
	session_check( ! $store->is_active( $b['family_id'], 1 ) && $other_site->is_active( $c['family_id'], 1 ), 'logout is idempotent and scoped to one family' );
	$other_site->revoke( $a2['credential'] );
	session_check( $other_site->is_active( $c['family_id'], 1 ), 'foreign-site credential cannot revoke local family' );

	$pwd = $store->create( 1 );
	$passwords[1] = 'synthetic-password-hash-changed';
	session_check( ! $store->is_active( $pwd['family_id'], 1 ) && $store->rotate( $pwd['credential'] ) === null, 'password hash change invalidates refresh and access family' );
	$passwords[1] = 'synthetic-password-hash-a';
	$missing = $store->create( 999 );
	session_check( $missing === null, 'unknown user cannot create session' );
	$expire = $store->create( 1 );
	$now += CB_Web_Session_Store::IDLE_TTL;
	session_check( ! $store->is_active( $expire['family_id'], 1 ) && $store->rotate( $expire['credential'] ) === null, 'idle timeout rejects active and refresh checks' );
	$absolute = $store->create( 1 );
	$now += CB_Web_Session_Store::ABSOLUTE_TTL;
	session_check( ! $store->is_active( $absolute['family_id'], 1 ), 'absolute expiry rejects old family' );
	session_check( $store->prune_expired( 100 ) > 0 && ! $store->is_active( $absolute['family_id'], 1 ), 'bounded cleanup removes expired families' );
	session_check( session_fails( static function () use ( $store ) { $store->prune_expired( 101 ); } ), 'cleanup rejects unbounded request' );

	$before = scalar( $db, "SELECT COUNT(*) FROM `{$families}`" );
	$bad_rng = new CB_Web_Session_Store( $db, $prefix, $site_a, $secret, $password_provider, $clock, static function () { throw new RuntimeException( 'test' ); } );
	session_check( session_fails( static function () use ( $bad_rng ) { $bad_rng->create( 1 ); } ) && scalar( $db, "SELECT COUNT(*) FROM `{$families}`" ) === $before, 'random failure issues no session or partial rows' );
	$bad_clock = new CB_Web_Session_Store( $db, $prefix, $site_a, $secret, $password_provider, static function () { return 0; } );
	session_check( session_fails( static function () use ( $bad_clock ) { $bad_clock->create( 1 ); } ), 'invalid clock fails closed' );
	session_check( session_fails( static function () use ( $db, $prefix, $site_a, $password_provider ) { new CB_Web_Session_Store( $db, $prefix, $site_a, 'weak', $password_provider ); } ), 'weak server key is refused' );

	$trigger = $prefix . 'web_session_insert_fail';
	$db->query( "CREATE TRIGGER `{$trigger}` BEFORE INSERT ON `{$tokens}` FOR EACH ROW BEGIN IF @qa_fail_insert = 1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'synthetic failure'; END IF; IF @qa_wait_insert = 1 THEN DO SLEEP(2); END IF; END" );
	$db->query( 'SET @qa_fail_insert=1' );
	$before = scalar( $db, "SELECT COUNT(*) FROM `{$families}`" );
	session_check( session_fails( static function () use ( $store ) { $store->create( 1 ); } ) && scalar( $db, "SELECT COUNT(*) FROM `{$families}`" ) === $before, 'failed token insert rolls back new family' );
	$db->query( 'SET @qa_fail_insert=0' );
	$rollback = $store->create( 1 );
	$db->query( 'SET @qa_fail_insert=1' );
	session_check( session_fails( static function () use ( $store, $rollback ) { $store->rotate( $rollback['credential'] ); } ), 'failed rotated-token insert reports failure' );
	$db->query( 'SET @qa_fail_insert=0' );
	session_check( is_array( $store->rotate( $rollback['credential'] ) ), 'failed rotation rolled back old credential consumption' );

	$commit_db = new CB_Test_Commit_Failure_Connection( '127.0.0.1', CARMILLA_TEST_DB_USER, CARMILLA_TEST_DB_PASSWORD, 'carmilla_session_qa', $port );
	$commit_store = new CB_Web_Session_Store( $commit_db, $prefix, $site_a, $secret, $password_provider, $clock );
	$before = scalar( $db, "SELECT COUNT(*) FROM `{$families}`" );
	$commit_db->fail_next_commit = true;
	session_check( session_fails( static function () use ( $commit_store ) { $commit_store->create( 1 ); } ) && scalar( $db, "SELECT COUNT(*) FROM `{$families}`" ) === $before, 'failed creation commit returns no session and rolls back rows' );

	$commit_rotate = $store->create( 1 );
	$commit_db = new CB_Test_Commit_Failure_Connection( '127.0.0.1', CARMILLA_TEST_DB_USER, CARMILLA_TEST_DB_PASSWORD, 'carmilla_session_qa', $port );
	$commit_store = new CB_Web_Session_Store( $commit_db, $prefix, $site_a, $secret, $password_provider, $clock );
	$commit_db->fail_next_commit = true;
	session_check( session_fails( static function () use ( $commit_store, $commit_rotate ) { $commit_store->rotate( $commit_rotate['credential'] ); } ) && is_array( $store->rotate( $commit_rotate['credential'] ) ), 'failed rotation commit returns no replacement and preserves old credential' );

	$disconnect = $store->create( 1 );
	$descriptors = array( 0 => array( 'pipe', 'r' ), 1 => array( 'pipe', 'w' ), 2 => array( 'pipe', 'w' ) );
	$disconnect_proc = proc_open( array( PHP_BINARY, __FILE__, 'disconnect-worker' ), $descriptors, $disconnect_pipes );
	if ( ! is_resource( $disconnect_proc ) ) { throw new RuntimeException( 'disconnect worker failed to start' ); }
	fwrite( $disconnect_pipes[0], $disconnect['credential'] . "\n" . $now . "\n" ); fflush( $disconnect_pipes[0] );
	$ready = trim( (string) fgets( $disconnect_pipes[1] ) );
	if ( ! preg_match( '/\AREADY:([0-9]+)\z/', $ready, $matches ) ) { throw new RuntimeException( 'disconnect worker not ready' ); }
	$thread_id = (int) $matches[1];
	fwrite( $disconnect_pipes[0], "GO\n" ); fflush( $disconnect_pipes[0] );
	if ( ! wait_for_sleep( $db, $thread_id ) ) { throw new RuntimeException( 'worker did not reach transaction failure point' ); }
	$db->kill( $thread_id );
	$disconnect_outcome = trim( (string) fgets( $disconnect_pipes[1] ) );
	fclose( $disconnect_pipes[0] ); fclose( $disconnect_pipes[1] ); fclose( $disconnect_pipes[2] );
	if ( proc_close( $disconnect_proc ) !== 0 ) { throw new RuntimeException( 'disconnect worker failed' ); }
	session_check( $disconnect_outcome === 'rejected' && is_array( $store->rotate( $disconnect['credential'] ) ), 'lost connection rolls back consumed credential and returns no replacement' );

	$race = $store->create( 1 );
	$workers = array();
	for ( $i = 0; $i < 2; ++$i ) {
		$proc = proc_open( array( PHP_BINARY, __FILE__, 'race-worker' ), $descriptors, $pipes );
		if ( ! is_resource( $proc ) ) { throw new RuntimeException( 'worker failed to start' ); }
		fwrite( $pipes[0], $race['credential'] . "\n" . $now . "\n" ); fflush( $pipes[0] );
		if ( trim( (string) fgets( $pipes[1] ) ) !== 'READY' ) { throw new RuntimeException( 'worker failed to initialize' ); }
		$workers[] = array( $proc, $pipes );
	}
	foreach ( $workers as $worker ) { fwrite( $worker[1][0], "GO\n" ); fflush( $worker[1][0] ); }
	$results = array();
	foreach ( $workers as $worker ) {
		$results[] = json_decode( (string) fgets( $worker[1][1] ), true );
		fclose( $worker[1][0] ); fclose( $worker[1][1] ); fclose( $worker[1][2] );
		if ( proc_close( $worker[0] ) !== 0 ) { throw new RuntimeException( 'worker failed' ); }
	}
	$rotations = array_values( array_filter( $results, static function ( $r ) { return isset( $r['result'] ) && is_array( $r['result'] ); } ) );
	$rejections = array_values( array_filter( $results, static function ( $r ) { return is_array( $r ) && array_key_exists( 'result', $r ) && $r['result'] === null; } ) );
	session_check( count( $rotations ) === 1 && count( $rejections ) === 1, 'two real concurrent refreshes yield one rotation and one replay' );
	$winning = $rotations[0]['result']['credential'] ?? '';
	session_check( ! $store->is_active( $race['family_id'], 1 ) && $store->rotate( $winning ) === null, 'concurrent replay invalidates winner and access family' );

	$logout_race = $store->create( 1 );
	$workers = array();
	foreach ( array( 'race-worker', 'revoke-worker' ) as $mode ) {
		$proc = proc_open( array( PHP_BINARY, __FILE__, $mode ), $descriptors, $pipes );
		if ( ! is_resource( $proc ) ) { throw new RuntimeException( 'logout race worker failed' ); }
		fwrite( $pipes[0], $logout_race['credential'] . "\n" . $now . "\n" ); fflush( $pipes[0] );
		if ( trim( (string) fgets( $pipes[1] ) ) !== 'READY' ) { throw new RuntimeException( 'logout race worker not ready' ); }
		$workers[] = array( $proc, $pipes );
	}
	foreach ( $workers as $worker ) { fwrite( $worker[1][0], "GO\n" ); fflush( $worker[1][0] ); }
	$logout_results = array();
	foreach ( $workers as $worker ) {
		$logout_results[] = json_decode( (string) fgets( $worker[1][1] ), true );
		fclose( $worker[1][0] ); fclose( $worker[1][1] ); fclose( $worker[1][2] );
		if ( proc_close( $worker[0] ) !== 0 ) { throw new RuntimeException( 'logout race worker failed' ); }
	}
	$race_credential = $logout_results[0]['result']['credential'] ?? '';
	session_check( ! empty( $logout_results[1]['revoked'] ) && ! $store->is_active( $logout_race['family_id'], 1 ) && $store->rotate( $race_credential ) === null, 'concurrent logout and refresh leave no usable family' );

	$shared_store = new CB_Web_Session_Store( $db, $prefix, $site_a, $secret, $shared_password_provider, $clock );
	$password_race = $shared_store->create( 1 );
	$password_proc = proc_open( array( PHP_BINARY, __FILE__, 'password-race-worker' ), $descriptors, $password_pipes );
	if ( ! is_resource( $password_proc ) ) { throw new RuntimeException( 'password race worker failed' ); }
	fwrite( $password_pipes[0], $password_race['credential'] . "\n" . $now . "\n" ); fflush( $password_pipes[0] );
	$ready = trim( (string) fgets( $password_pipes[1] ) );
	if ( ! preg_match( '/\AREADY:([0-9]+)\z/', $ready, $matches ) ) { throw new RuntimeException( 'password race worker not ready' ); }
	fwrite( $password_pipes[0], "GO\n" ); fflush( $password_pipes[0] );
	if ( ! wait_for_sleep( $db, (int) $matches[1] ) ) { throw new RuntimeException( 'password race did not reach insert' ); }
	$db->query( "UPDATE `{$prefix}synthetic_users` SET password_hash='synthetic-password-hash-new' WHERE user_id=1" );
	$password_result = json_decode( (string) fgets( $password_pipes[1] ), true );
	fclose( $password_pipes[0] ); fclose( $password_pipes[1] ); fclose( $password_pipes[2] );
	if ( proc_close( $password_proc ) !== 0 ) { throw new RuntimeException( 'password race worker failed' ); }
	$password_credential = $password_result['result']['credential'] ?? '';
	session_check( ! $shared_store->is_active( $password_race['family_id'], 1 ) && $shared_store->rotate( $password_credential ) === null, 'password change during rotation makes resulting family unusable' );

	$user_one = $store->create( 1 ); $user_one_2 = $store->create( 1 ); $user_two = $store->create( 2 );
	$store->revoke_user( 1 );
	session_check( ! $store->is_active( $user_one['family_id'], 1 ) && ! $store->is_active( $user_one_2['family_id'], 1 ) && $store->is_active( $user_two['family_id'], 2 ), 'revoke-user affects only that user and site' );

	$wrong_prefix = 'qa_' . bin2hex( random_bytes( 6 ) ) . '_';
	session_check( session_fails( static function () use ( $db, $wrong_prefix, $site_a, $secret, $password_provider ) { new CB_Web_Session_Store( $db, $wrong_prefix, $site_a, $secret, $password_provider ); } ), 'missing schema fails closed' );
	$bad_prefix = 'qa_' . bin2hex( random_bytes( 6 ) ) . '_';
	foreach ( CB_Web_Session_Store::schema_sql( $bad_prefix ) as $ddl ) { $db->query( str_replace( 'ENGINE=InnoDB', 'ENGINE=MyISAM', $ddl ) ); }
	session_check( session_fails( static function () use ( $db, $bad_prefix, $site_a, $secret, $password_provider ) { new CB_Web_Session_Store( $db, $bad_prefix, $site_a, $secret, $password_provider ); } ), 'nontransactional tables fail closed' );
	$bad_index = 'qa_' . bin2hex( random_bytes( 6 ) ) . '_';
	foreach ( CB_Web_Session_Store::schema_sql( $bad_index ) as $ddl ) { $db->query( $ddl ); }
	$db->query( 'ALTER TABLE `' . $bad_index . 'carmilla_web_tokens` DROP PRIMARY KEY' );
	session_check( session_fails( static function () use ( $db, $bad_index, $site_a, $secret, $password_provider ) { new CB_Web_Session_Store( $db, $bad_index, $site_a, $secret, $password_provider ); } ), 'missing unique token digest index fails closed' );

	echo "Checks: {$checks}; failures: {$failures}.\n";
	exit( $failures === 0 ? 0 : 1 );
} catch ( Throwable $e ) {
	fwrite( STDERR, "Test infrastructure failed: " . get_class( $e ) . "\n" );
	exit( 2 );
}

