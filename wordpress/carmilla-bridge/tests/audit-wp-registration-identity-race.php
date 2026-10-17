<?php
/** Reproduce a WP core identity race ONLY in a disposable synthetic database. */
$prefix = getenv( 'CARMILLA_LOGIN_TEST_PREFIX' );
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) ) { fwrite( STDERR, "Disposable test flag and random prefix required.\n" ); exit( 2 ); }
define( 'ABSPATH', '/usr/src/wordpress/' ); define( 'WP_INSTALLING', true ); define( 'WP_DEBUG', false );
define( 'DB_NAME', 'carmilla_login_qa' ); define( 'DB_USER', 'synthetic_qa' ); define( 'DB_PASSWORD', 'synthetic_qa_password' ); define( 'DB_HOST', '127.0.0.1:3306' );
define( 'DB_CHARSET', 'utf8mb4' ); define( 'DB_COLLATE', '' ); define( 'WP_HOME', 'https://qa.example.test/wordpress' ); define( 'WP_SITEURL', WP_HOME );
foreach ( array( 'AUTH_KEY', 'SECURE_AUTH_KEY', 'LOGGED_IN_KEY', 'NONCE_KEY', 'AUTH_SALT', 'SECURE_AUTH_SALT', 'LOGGED_IN_SALT', 'NONCE_SALT' ) as $name ) { define( $name, hash( 'sha256', 'synthetic-wp-' . $name ) ); }
$table_prefix = $prefix;
$_SERVER['HTTP_HOST'] = 'qa.example.test'; $_SERVER['SERVER_NAME'] = 'qa.example.test'; $_SERVER['SERVER_PORT'] = '443'; $_SERVER['HTTPS'] = 'on'; $_SERVER['REQUEST_METHOD'] = 'GET';
try {
	require ABSPATH . 'wp-settings.php';
	if ( ( $argv[1] ?? '' ) === 'worker' ) {
		// This hook follows both wp_insert_user uniqueness checks and precedes
		// the actual insert. Both workers must reach it before either proceeds.
		add_filter( 'pre_user_url', static function ( $value ) {
			echo "READY\n"; flush();
			if ( trim( (string) fgets( STDIN ) ) !== 'GO' ) { exit( 3 ); }
			return $value;
		} );
		$id = wp_insert_user( array( 'user_login' => 'race_user', 'user_email' => 'race@example.test', 'user_pass' => 'synthetic-only-password' ) );
		echo is_wp_error( $id ) ? "REJECTED\n" : "CREATED\n";
		exit( is_wp_error( $id ) ? 1 : 0 );
	}
	require_once ABSPATH . 'wp-admin/includes/upgrade.php'; add_filter( 'pre_wp_mail', static function () { return true; } );
	wp_install( 'Synthetic QA', 'qa_admin', 'synthetic@example.test', false, '', 'synthetic-only-password' );
	$workers = array(); $descriptors = array( array( 'pipe', 'r' ), array( 'pipe', 'w' ), array( 'pipe', 'w' ) );
	for ( $i = 0; $i < 2; ++$i ) {
		$pipes = array(); $process = proc_open( array( PHP_BINARY, __FILE__, 'worker' ), $descriptors, $pipes );
		if ( ! is_resource( $process ) ) { throw new RuntimeException(); }
		stream_set_timeout( $pipes[1], 20 ); $workers[] = array( $process, $pipes );
	}
	foreach ( $workers as $worker ) { if ( trim( (string) fgets( $worker[1][1] ) ) !== 'READY' ) { throw new RuntimeException(); } }
	foreach ( $workers as $worker ) { fwrite( $worker[1][0], "GO\n" ); fclose( $worker[1][0] ); }
	$created = 0;
	foreach ( $workers as $worker ) {
		$output = stream_get_contents( $worker[1][1] ); fclose( $worker[1][1] ); fclose( $worker[1][2] );
		$status = proc_close( $worker[0] ); if ( $status === 0 && trim( $output ) === 'CREATED' ) { ++$created; }
	}
	global $wpdb;
	$duplicates = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE user_login=%s AND user_email=%s", 'race_user', 'race@example.test' ) );
	if ( $created !== 2 || $duplicates !== 2 ) { echo "FAIL expected concurrent WordPress identity race was not reproduced\n"; exit( 1 ); }
	echo "PASS two concurrent real WordPress registrations produced duplicate login and email in the synthetic database\n";
	echo "This is a reproduced unsafe baseline, not a registration security pass.\n";
} catch ( Throwable $e ) { fwrite( STDERR, 'WordPress race test infrastructure failed: ' . get_class( $e ) . "\n" ); exit( 2 ); }
