<?php
/** Isolated real-WP test. Never point this at a retained site. */
$prefix = getenv( 'CARMILLA_LOGIN_TEST_PREFIX' );
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) ) {
	fwrite( STDERR, "Disposable test flag and random prefix required.\n" ); exit( 2 );
}
$task_mobile = '09129991110';
if ( ( $argv[1] ?? '' ) === 'worker' ) {
	define( 'ABSPATH', '/usr/src/wordpress/' ); define( 'WP_INSTALLING', true ); define( 'WP_DEBUG', false );
	define( 'DB_NAME', 'carmilla_login_qa' ); define( 'DB_USER', 'synthetic_qa' ); define( 'DB_PASSWORD', 'synthetic_qa_password' ); define( 'DB_HOST', '127.0.0.1:3306' );
	define( 'DB_CHARSET', 'utf8mb4' ); define( 'DB_COLLATE', '' ); define( 'WP_HOME', 'https://qa.example.test/wordpress' ); define( 'WP_SITEURL', WP_HOME );
	foreach ( array( 'AUTH_KEY', 'SECURE_AUTH_KEY', 'LOGGED_IN_KEY', 'NONCE_KEY', 'AUTH_SALT', 'SECURE_AUTH_SALT', 'LOGGED_IN_SALT', 'NONCE_SALT' ) as $name ) { define( $name, hash( 'sha256', 'synthetic-wp-' . $name ) ); }
	$table_prefix = $prefix;
	$_SERVER['HTTP_HOST'] = 'qa.example.test'; $_SERVER['SERVER_NAME'] = 'qa.example.test'; $_SERVER['SERVER_PORT'] = '443'; $_SERVER['HTTPS'] = 'on';
	try {
		require ABSPATH . 'wp-settings.php'; wp_installing( false );
		require_once '/bridge/includes/class-cb-otp-atomic-consumer.php';
		$code = trim( (string) fgets( STDIN ) );
		add_filter( 'check_password', static function ( $valid ) {
			if ( ! $valid ) { echo "BAD_CHECK\n"; flush(); exit( 3 ); }
			echo "READY\n"; flush();
			if ( trim( (string) fgets( STDIN ) ) !== 'GO' ) { exit( 3 ); }
			return $valid;
		}, 10, 1 );
		echo ( new CB_OTP_Atomic_Consumer() )->consume( $task_mobile, 'login', $code ) ? "CONSUMED\n" : "REJECTED\n";
		exit( 0 );
	} catch ( Throwable $e ) { fwrite( STDERR, 'FAIL synthetic OTP worker ' . get_class( $e ) . ".\n" ); exit( 1 ); }
}

$workers = array(); $stage = 'setup';
try {
	require __DIR__ . '/experiment-identity-shared-references.php'; wp_installing( false );
	require_once '/bridge/includes/class-cb-otp-atomic-consumer.php';
	global $wpdb, $wp_version;
	$consumer = new CB_OTP_Atomic_Consumer(); $key = 'cb_otp_' . md5( $task_mobile . '_login' );
	$code = (string) wp_rand( 100000, 999999 );
	shared_check( ! wp_using_ext_object_cache(), 'fixture uses database-backed transients' );
	$engine = $wpdb->get_var( $wpdb->prepare( 'SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=%s', $wpdb->options ) );
	shared_check( strtoupper( (string) $engine ) === 'INNODB', 'options table uses InnoDB' );
	shared_check( set_transient( $key, wp_hash_password( $code ), 180 ), 'issue synthetic hashed transient' );
	shared_check( ! $consumer->consume( $task_mobile, 'login', 'invalid' ), 'invalid input rejected' );
	shared_check( ! $consumer->consume( $task_mobile, 'other', $code ), 'unsupported purpose rejected' );
	$wrong = $code === '111111' ? '222222' : '111111';
	shared_check( ! $consumer->consume( $task_mobile, 'login', $wrong ), 'wrong code rejected without consuming' );
	wp_using_ext_object_cache( true );
	shared_check( ! $consumer->consume( $task_mobile, 'login', $code ), 'persistent cache configuration rejected' );
	wp_using_ext_object_cache( false );
	$stage = 'concurrent-workers';
	foreach ( array( 1, 2 ) as $i ) {
		$pipes = array(); $process = proc_open( array( PHP_BINARY, __FILE__, 'worker' ), array( array( 'pipe', 'r' ), array( 'pipe', 'w' ), array( 'pipe', 'w' ) ), $pipes );
		if ( ! is_resource( $process ) ) { throw new RuntimeException(); }
		stream_set_timeout( $pipes[1], 20 ); $workers[] = array( $process, $pipes );
		fwrite( $pipes[0], $code . "\n" ); fflush( $pipes[0] );
	}
	foreach ( $workers as $worker ) {
		$ready = trim( (string) fgets( $worker[1][1] ) );
		if ( $ready !== 'READY' ) {
			$diagnostic = trim( (string) stream_get_contents( $worker[1][2] ) );
			if ( preg_match( '/\AFAIL synthetic OTP worker [A-Za-z_]+\.\z/', $diagnostic ) ) { fwrite( STDERR, $diagnostic . "\n" ); }
			fwrite( STDERR, "Worker did not reach verified-code barrier.\n" ); throw new RuntimeException();
		}
	}
	foreach ( $workers as $worker ) { fwrite( $worker[1][0], "GO\n" ); fclose( $worker[1][0] ); }
	$results = array();
	foreach ( $workers as $i => $worker ) {
		$results[] = trim( (string) stream_get_contents( $worker[1][1] ) ); fclose( $worker[1][1] );
		stream_get_contents( $worker[1][2] ); fclose( $worker[1][2] );
		$status = proc_close( $worker[0] ); unset( $workers[$i] ); if ( $status !== 0 ) { throw new RuntimeException(); }
	}
	sort( $results );
	shared_check( $results === array( 'CONSUMED', 'REJECTED' ), 'only one of two already-verified concurrent requests consumes the code' );
	shared_check( ! $consumer->consume( $task_mobile, 'login', $code ), 'sequential reuse rejected even with stale local cache' );
	wp_cache_flush();
	shared_check( set_transient( $key, wp_hash_password( $code ), 180 ), 'issue replacement synthetic transient' );
	$timeout = '_transient_timeout_' . $key;
	shared_check( $wpdb->update( $wpdb->options, array( 'option_value' => (string) ( time() - 1 ) ), array( 'option_name' => $timeout ) ) === 1, 'expire synthetic transient' );
	shared_check( ! $consumer->consume( $task_mobile, 'login', $code ), 'expired code rejected' );
	shared_check( $wpdb->update( $wpdb->options, array( 'option_value' => (string) ( time() + 180 ) ), array( 'option_name' => $timeout ) ) === 1, 'restore synthetic expiry for failure injection' );
	$trigger = $prefix . 'otp_delete_failure';
	shared_check( $wpdb->query( "CREATE TRIGGER `$trigger` BEFORE DELETE ON {$wpdb->options} FOR EACH ROW SIGNAL SQLSTATE '45000'" ) !== false, 'install synthetic delete-failure trigger' );
	shared_check( ! $consumer->consume( $task_mobile, 'login', $code ), 'database delete failure rejected' );
	shared_check( is_string( $wpdb->get_var( $wpdb->prepare( "SELECT option_value FROM {$wpdb->options} WHERE option_name=%s", '_transient_' . $key ) ) ), 'failed delete leaves synthetic OTP row intact' );
	$old_options = $wpdb->options; $wpdb->options = $prefix . 'missing_options';
	shared_check( ! $consumer->consume( $task_mobile, 'login', $code ), 'database read failure rejected' );
	$wpdb->options = $old_options;
	echo 'PASS atomic OTP consumer WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n";
} catch ( Throwable $e ) {
	fwrite( STDERR, 'FAIL synthetic OTP consumer at ' . $stage . ".\n" ); exit( 1 );
} finally {
	foreach ( $workers as $worker ) {
		foreach ( $worker[1] as $pipe ) { if ( is_resource( $pipe ) ) { fclose( $pipe ); } }
		if ( is_resource( $worker[0] ) ) { proc_terminate( $worker[0] ); proc_close( $worker[0] ); }
	}
}
