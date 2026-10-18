<?php
/** Synthetic concurrency characterization; never use with a retained site. */
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
	$_SERVER['HTTP_HOST'] = 'qa.example.test'; $_SERVER['SERVER_NAME'] = 'qa.example.test'; $_SERVER['SERVER_PORT'] = '443'; $_SERVER['HTTPS'] = 'on'; $_SERVER['REQUEST_METHOD'] = 'POST';
	try {
		require ABSPATH . 'wp-settings.php'; wp_installing( false );
		require_once '/bridge/includes/helpers.php'; require_once '/bridge/includes/class-cb-jwt.php'; require_once '/bridge/includes/class-cb-auth-controller.php';
		$code = trim( (string) fgets( STDIN ) );
		if ( ! preg_match( '/\A[0-9]{6}\z/', $code ) ) { exit( 3 ); }
		$transient = 'cb_otp_' . md5( $task_mobile . '_login' );
		add_filter( 'transient_' . $transient, static function ( $value ) {
			if ( ! is_string( $value ) || $value === '' ) { echo "NOT_READY\n"; flush(); exit( 3 ); }
			echo "READY\n"; flush();
			if ( trim( (string) fgets( STDIN ) ) !== 'GO' ) { exit( 3 ); }
			return $value;
		} );
		$request = new WP_REST_Request( 'POST' ); $request->set_param( 'mobile', $task_mobile ); $request->set_param( 'otpCode', $code );
		$response = ( new CB_Auth_Controller() )->login_with_otp( $request );
		$body = $response->get_data();
		echo $response->get_status() === 200 && ! empty( $body['accessToken'] ) && ! empty( $body['refreshToken'] ) ? "SUCCESS\n" : "REJECTED\n";
		exit( 0 );
	} catch ( Throwable $e ) { fwrite( STDERR, 'FAIL synthetic OTP worker ' . get_class( $e ) . ".\n" ); exit( 1 ); }
}

$workers = array();
$stage = 'setup';
try {
	require __DIR__ . '/experiment-identity-shared-references.php'; wp_installing( false );
	require_once '/bridge/includes/helpers.php'; require_once '/bridge/includes/class-cb-jwt.php'; require_once '/bridge/includes/class-cb-auth-controller.php';
	global $wp_version;
	shared_check( ! wp_using_ext_object_cache(), 'fixture uses process-local WP object cache and database-backed transients' );
	$user = shared_user( 'otp_synthetic', 'otp_synthetic@example.test' );
	shared_check( add_user_meta( $user, 'cb_mobile', $task_mobile ) !== false, 'create synthetic mobile identity' );
	$code = null;
	add_filter( 'cb_send_otp_override', static function ( $override, $number, $generated, $purpose ) use ( &$code, $task_mobile ) {
		if ( $number === $task_mobile && $purpose === 'login' ) { $code = $generated; }
		return true;
	}, 10, 4 );
	$send = new WP_REST_Request( 'POST' ); $send->set_param( 'mobile', $task_mobile );
	$controller = new CB_Auth_Controller(); $response = $controller->send_login_otp( $send );
	shared_check( $response->get_status() === 200 && is_string( $code ) && preg_match( '/\A[0-9]{6}\z/', $code ), 'real OTP issuer succeeded with SMS intercepted' );
	$stage = 'start-workers';
	foreach ( array( 1, 2 ) as $index ) {
		$pipes = array(); $process = proc_open( array( PHP_BINARY, __FILE__, 'worker' ), array( array( 'pipe', 'r' ), array( 'pipe', 'w' ), array( 'pipe', 'w' ) ), $pipes );
		if ( ! is_resource( $process ) ) { throw new RuntimeException(); }
		stream_set_timeout( $pipes[1], 20 ); $workers[] = array( $process, $pipes );
		fwrite( $pipes[0], $code . "\n" ); fflush( $pipes[0] );
	}
	$stage = 'read-barrier';
	foreach ( $workers as $worker ) {
		$ready = trim( (string) fgets( $worker[1][1] ) );
		if ( $ready !== 'READY' ) {
			fwrite( STDERR, $ready === 'NOT_READY' ? "Worker did not find the synthetic transient.\n" : "Worker did not reach the read barrier.\n" );
			throw new RuntimeException();
		}
	}
	$stage = 'worker-results';
	foreach ( $workers as $worker ) { fwrite( $worker[1][0], "GO\n" ); fclose( $worker[1][0] ); }
	$results = array();
	foreach ( $workers as $index => $worker ) {
		$results[] = trim( (string) stream_get_contents( $worker[1][1] ) ); fclose( $worker[1][1] );
		stream_get_contents( $worker[1][2] ); fclose( $worker[1][2] );
		$status = proc_close( $worker[0] ); unset( $workers[$index] );
		if ( $status !== 0 ) { throw new RuntimeException(); }
	}
	shared_check( $results === array( 'SUCCESS', 'SUCCESS' ), 'both independent OTP login requests issue tokens after the shared read barrier' );
	// The issuer process cached its own pre-race option. A fresh request would
	// have a new in-process cache; never flush an external/shared object cache.
	wp_cache_flush();
	$reuse = new WP_REST_Request( 'POST' ); $reuse->set_param( 'mobile', $task_mobile ); $reuse->set_param( 'otpCode', $code );
	shared_check( $controller->login_with_otp( $reuse )->get_status() === 401, 'later sequential reuse is rejected' );
	echo 'PASS OTP concurrency characterization WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n";
} catch ( Throwable $e ) {
	fwrite( STDERR, 'FAIL synthetic OTP concurrency characterization at ' . $stage . ".\n" ); exit( 1 );
} finally {
	foreach ( $workers as $worker ) {
		foreach ( $worker[1] as $pipe ) { if ( is_resource( $pipe ) ) { fclose( $pipe ); } }
		if ( is_resource( $worker[0] ) ) { proc_terminate( $worker[0] ); proc_close( $worker[0] ); }
	}
}
