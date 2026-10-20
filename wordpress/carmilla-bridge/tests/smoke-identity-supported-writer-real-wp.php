<?php
/** Disposable real WordPress verification for the supported writer only. */
$prefix = getenv( 'CARMILLA_LOGIN_TEST_PREFIX' );
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) ) { fwrite( STDERR, "Disposable test flag and random prefix required.\n" ); exit( 2 ); }
try {
	$local_root = getenv( 'CARMILLA_LOGIN_WP_ROOT' );
	if ( is_string( $local_root ) && $local_root !== '' ) {
		$root = realpath( $local_root );
		if ( ! $root || ! is_file( $root . DIRECTORY_SEPARATOR . 'wp-load.php' ) ) { throw new RuntimeException(); }
		require $root . DIRECTORY_SEPARATOR . 'wp-load.php';
		global $wpdb; $wpdb->set_prefix( $prefix ); wp_installing( true );
	} else {
		define( 'ABSPATH', '/usr/src/wordpress/' ); define( 'WP_INSTALLING', true ); define( 'WP_DEBUG', false );
		define( 'DB_NAME', 'carmilla_login_qa' ); define( 'DB_USER', 'synthetic_qa' ); define( 'DB_PASSWORD', 'synthetic_qa_password' ); define( 'DB_HOST', '127.0.0.1:3306' );
		define( 'DB_CHARSET', 'utf8mb4' ); define( 'DB_COLLATE', '' ); define( 'WP_HOME', 'https://qa.example.test/' ); define( 'WP_SITEURL', WP_HOME );
		foreach ( array( 'AUTH_KEY', 'SECURE_AUTH_KEY', 'LOGGED_IN_KEY', 'NONCE_KEY', 'AUTH_SALT', 'SECURE_AUTH_SALT', 'LOGGED_IN_SALT', 'NONCE_SALT' ) as $name ) { define( $name, hash( 'sha256', 'synthetic-writer-' . $name ) ); }
		$table_prefix = $prefix;
		$_SERVER['HTTP_HOST'] = 'qa.example.test'; $_SERVER['SERVER_NAME'] = 'qa.example.test'; $_SERVER['SERVER_PORT'] = '443'; $_SERVER['HTTPS'] = 'on'; $_SERVER['REQUEST_METHOD'] = 'POST';
		require ABSPATH . 'wp-settings.php';
	}
	require_once ABSPATH . 'wp-admin/includes/upgrade.php';
	add_filter( 'pre_wp_mail', static function () { return true; } );
	wp_install( 'Synthetic writer QA', 'qa_admin', 'synthetic@example.test', false, '', 'synthetic-only-password' );
	// Exercise ordinary runtime transient storage, rather than the installer-only
	// in-memory transient shortcut.
	wp_installing( false );
	require_once '/work/includes/helpers.php'; require_once '/work/includes/class-cb-jwt.php';
	require_once '/work/includes/class-cb-identity-supported-writer.php'; require_once '/work/includes/class-cb-auth-controller.php';
	global $wpdb; $wpdb->hide_errors();
	$checks = 0; $failures = 0;
	$check = static function ( bool $ok, string $label ) use ( &$checks, &$failures ) { ++$checks; if ( ! $ok ) { ++$failures; } echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n"; };
	$writer = new CB_Identity_Supported_Writer();
	$registers = 0; add_action( 'user_register', static function () use ( &$registers ) { ++$registers; } );
	$first = $writer->create_customer( 'writer_one', 'writer.one@example.test', '09120000001', 'synthetic-only-password' );
	$check( is_int( $first ) && $first > 0 && $registers === 1 && $writer->unique_owner( '09120000001' ) === $first, 'successful supported write commits one exact owner after normal callback' );
	$count = (int) $wpdb->get_var( "SELECT COUNT(*) FROM {$wpdb->users}" );
	$duplicate = $writer->create_customer( 'writer_two', 'writer.two@example.test', '09120000001', 'synthetic-only-password' );
	$check( is_wp_error( $duplicate ) && $duplicate->get_error_code() === 'cb_identity_exists' && (int) $wpdb->get_var( "SELECT COUNT(*) FROM {$wpdb->users}" ) === $count && $registers === 1, 'pre-write conflict creates no account and fires no callback' );
	$other = wp_insert_user( array( 'user_login' => 'external_owner', 'user_email' => 'external@example.test', 'user_pass' => 'synthetic-only-password' ) );
	$inject = static function ( $user_id ) use ( $other ) {
		$user = get_user_by( 'id', (int) $user_id );
		if ( $user && $user->user_login === 'writer_race' ) { add_user_meta( $other, 'cb_mobile', '09120000002', true ); }
	};
	add_action( 'user_register', $inject, 20 );
	$race = $writer->create_customer( 'writer_race', 'writer.race@example.test', '09120000002', 'synthetic-only-password' );
	remove_action( 'user_register', $inject, 20 );
	$race_user = get_user_by( 'login', 'writer_race' );
	$check( is_wp_error( $race ) && $race->get_error_code() === 'cb_identity_recovery_required' && $race_user && get_user_meta( $race_user->ID, 'cb_identity_writer_state', true ) === 'needs_recovery' && $writer->unique_owner( '09120000002' ) === null, 'post-callback conflict issues no successful result and is marked for recovery' );
	$unsupported = $writer->create_customer( 'writer_bad', 'writer.bad@example.test', 'شماره', 'synthetic-only-password' );
	$check( is_wp_error( $unsupported ) && $unsupported->get_error_code() === 'cb_identity_invalid', 'unsupported exact bytes fail before a WordPress write' );
	$check( $writer->unique_owner( '09120000001' ) === $first && $writer->unique_owner( 'writer.one@example.test' ) === $first && $writer->unique_owner( 'writer_one' ) === $first, 'login email and mobile resolve only when one owner exists' );
	$controller = new CB_Auth_Controller(); $request = new WP_REST_Request( 'POST' );
	$request->set_param( 'email', 'controller@example.test' ); $request->set_param( 'mobile', '09120000003' ); $request->set_param( 'password', 'synthetic-only-password' );
	$response = $controller->register( $request ); $data = $response->get_data();
	$check( $response->get_status() === 201 && is_array( $data ) && isset( $data['accessToken'], $data['refreshToken'], $data['user'] ), 'registration endpoint issues credentials only after supported writer success' );
	$duplicate_request = new WP_REST_Request( 'POST' ); $duplicate_request->set_param( 'email', 'second@example.test' ); $duplicate_request->set_param( 'mobile', '09120000003' ); $duplicate_request->set_param( 'password', 'synthetic-only-password' );
	$duplicate_response = $controller->register( $duplicate_request ); $duplicate_data = $duplicate_response->get_data();
	$check( $duplicate_response->get_status() === 409 && is_array( $duplicate_data ) && ! isset( $duplicate_data['accessToken'], $duplicate_data['refreshToken'] ), 'registration endpoint rejects duplicate without credentials' );
	$ambiguous_request = new WP_REST_Request( 'POST' ); $ambiguous_request->set_param( 'username', '09120000002' ); $ambiguous_request->set_param( 'password', 'synthetic-only-password' );
	$ambiguous_response = $controller->login( $ambiguous_request ); $ambiguous_data = $ambiguous_response->get_data();
	$check( $ambiguous_response->get_status() === 401 && is_array( $ambiguous_data ) && ! isset( $ambiguous_data['accessToken'], $ambiguous_data['refreshToken'] ), 'login endpoint fails closed for ambiguous identity' );
	$issued_codes = array();
	$otp_override = static function ( $override, $mobile, $code ) use ( &$issued_codes ) {
		$issued_codes[ $mobile ] = $code;
		return true;
	};
	add_filter( 'cb_send_otp_override', $otp_override, 10, 3 );
	$otp_mobile = '09120000004';
	$send_otp = new WP_REST_Request( 'POST' ); $send_otp->set_param( 'mobile', $otp_mobile );
	$send_response = $controller->send_login_otp( $send_otp );
	$otp_login = new WP_REST_Request( 'POST' ); $otp_login->set_param( 'mobile', $otp_mobile ); $otp_login->set_param( 'otpCode', $issued_codes[ $otp_mobile ] ?? '' );
	$otp_response = $controller->login_with_otp( $otp_login ); $otp_data = $otp_response->get_data();
	$check( $send_response->get_status() === 200 && $otp_response->get_status() === 200 && is_array( $otp_data ) && isset( $otp_data['accessToken'], $otp_data['refreshToken'] ), 'OTP login creates or authenticates only after atomic consumption' );
	$otp_reuse = $controller->login_with_otp( $otp_login ); $otp_reuse_data = $otp_reuse->get_data();
	$check( $otp_reuse->get_status() === 401 && is_array( $otp_reuse_data ) && ! isset( $otp_reuse_data['accessToken'], $otp_reuse_data['refreshToken'] ), 'OTP login endpoint rejects sequential code reuse without credentials' );
	$lock_mobile = '09120000005'; $lock_send = new WP_REST_Request( 'POST' ); $lock_send->set_param( 'mobile', $lock_mobile ); $controller->send_login_otp( $lock_send );
	$wrong_otp = new WP_REST_Request( 'POST' ); $wrong_otp->set_param( 'mobile', $lock_mobile ); $wrong_otp->set_param( 'otpCode', '111111' );
	$controller->login_with_otp( $wrong_otp ); $controller->login_with_otp( $wrong_otp ); $controller->login_with_otp( $wrong_otp );
	$locked_valid = new WP_REST_Request( 'POST' ); $locked_valid->set_param( 'mobile', $lock_mobile ); $locked_valid->set_param( 'otpCode', $issued_codes[ $lock_mobile ] ?? '' );
	$check( $controller->login_with_otp( $locked_valid )->get_status() === 401, 'three invalid OTP attempts lock the database-backed code' );
	wp_using_ext_object_cache( true );
	$cache_request = new WP_REST_Request( 'POST' ); $cache_request->set_param( 'mobile', '09120000006' );
	$cache_response = $controller->send_login_otp( $cache_request ); wp_using_ext_object_cache( false );
	$check( $cache_response->get_status() === 503 && ! isset( $issued_codes['09120000006'] ), 'OTP issuance fails closed when a persistent object cache is active' );
	remove_filter( 'cb_send_otp_override', $otp_override, 10 );
	global $wp_version; echo "Checks: {$checks}; failures: {$failures}.\n";
	if ( ! $failures ) { echo 'PASS supported identity writer WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n"; }
	exit( $failures ? 1 : 0 );
} catch ( Throwable $e ) { fwrite( STDERR, 'Synthetic supported writer fixture failed: ' . get_class( $e ) . ' at ' . basename( $e->getFile() ) . ':' . $e->getLine() . "\n" ); exit( 2 ); }
