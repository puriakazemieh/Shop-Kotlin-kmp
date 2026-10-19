<?php
/** Disposable real WordPress only. Reports no synthetic identities or credentials. */
$prefix = getenv( 'CARMILLA_LOGIN_TEST_PREFIX' );
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) ) { fwrite( STDERR, "Disposable test flag and random prefix required.\n" ); exit( 2 ); }
define( 'ABSPATH', '/usr/src/wordpress/' ); define( 'WP_INSTALLING', true ); define( 'WP_DEBUG', false );
define( 'DB_NAME', 'carmilla_login_qa' ); define( 'DB_USER', 'synthetic_qa' ); define( 'DB_PASSWORD', 'synthetic_qa_password' ); define( 'DB_HOST', '127.0.0.1:3306' );
define( 'DB_CHARSET', 'utf8mb4' ); define( 'DB_COLLATE', '' ); define( 'WP_HOME', 'https://qa.example.test/' ); define( 'WP_SITEURL', WP_HOME );
foreach ( array( 'AUTH_KEY', 'SECURE_AUTH_KEY', 'LOGGED_IN_KEY', 'NONCE_KEY', 'AUTH_SALT', 'SECURE_AUTH_SALT', 'LOGGED_IN_SALT', 'NONCE_SALT' ) as $name ) { define( $name, hash( 'sha256', 'synthetic-audit-' . $name ) ); }
$table_prefix = $prefix;
$_SERVER['HTTP_HOST'] = 'qa.example.test'; $_SERVER['SERVER_NAME'] = 'qa.example.test'; $_SERVER['SERVER_PORT'] = '443'; $_SERVER['HTTPS'] = 'on'; $_SERVER['REQUEST_METHOD'] = 'GET';
try {
	require ABSPATH . 'wp-settings.php'; require_once ABSPATH . 'wp-admin/includes/upgrade.php';
	add_filter( 'pre_wp_mail', static function () { return true; } );
	wp_install( 'Synthetic QA', 'qa_admin', 'synthetic@example.test', false, '', 'synthetic-only-password' );
	require_once '/work/includes/class-cb-identity-exact-audit.php';
	global $wpdb; $wpdb->hide_errors();
	$a = wp_insert_user( array( 'user_login' => 'owner_a', 'user_email' => 'alias@example.test', 'user_pass' => 'synthetic-only-password' ) );
	$b = wp_insert_user( array( 'user_login' => 'owner_b', 'user_email' => 'owner_b@example.test', 'user_pass' => 'synthetic-only-password' ) );
	if ( is_wp_error( $a ) || is_wp_error( $b ) || ! $a || ! $b ) { throw new RuntimeException(); }
	add_user_meta( $a, 'cb_mobile', 'alias@example.test' ); add_user_meta( $a, 'cb_mobile', 'alias@example.test' );
	add_user_meta( $a, 'billing_phone', 'owner_b' );
	$audit = new CB_Identity_Exact_Audit( $wpdb ); $checks = 0; $failures = 0;
	$check = static function ( bool $ok, string $label ) use ( &$checks, &$failures ) { ++$checks; if ( ! $ok ) { ++$failures; } echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n"; };
	$before = hash( 'sha256', serialize( array( $wpdb->get_results( "SELECT * FROM {$wpdb->users} ORDER BY ID", ARRAY_A ), $wpdb->get_results( "SELECT * FROM {$wpdb->usermeta} ORDER BY umeta_id", ARRAY_A ) ) ) );
	$result = $audit->scan();
	$check( $result['status'] === 'EXACT_MATCH_ONLY' && $result['conflicting_keys'] === 0 && $result['same_owner_alias_keys'] === 1, 'same-owner duplicates allowed and billing contact excluded' );
	$after = hash( 'sha256', serialize( array( $wpdb->get_results( "SELECT * FROM {$wpdb->users} ORDER BY ID", ARRAY_A ), $wpdb->get_results( "SELECT * FROM {$wpdb->usermeta} ORDER BY umeta_id", ARRAY_A ) ) ) );
	$check( hash_equals( $before, $after ), 'source rows unchanged by audit' );
	add_user_meta( $b, 'cb_mobile', 'alias@example.test' );
	add_user_meta( $b, 'cb_mobile', 'ALIAS@example.test' );
	$result = $audit->scan();
	$check( $result['status'] === 'CONFLICT' && $result['conflicting_keys'] === 1 && $result['same_owner_alias_keys'] === 1, 'cross-owner conflict detected; distinct case remains distinct' );
	add_user_meta( $b, 'cb_mobile', 'owner_a' );
	$result = $audit->scan();
	$check( $result['status'] === 'CONFLICT' && $result['conflicting_keys'] === 2, 'separate login-versus-mobile collision detected' );
	$check( $audit->scan( 2 )['status'] === 'INCOMPLETE', 'bounded incomplete scan is not accepted' );
	$check( $audit->scan( 11 )['status'] === 'CONFLICT' && $audit->scan( 10 )['status'] === 'INCOMPLETE', 'exact row bound includes all rows and refuses one fewer' );
	$check( $audit->scan( 0 )['status'] === 'UNSUPPORTED' && $audit->scan( 10001 )['status'] === 'UNSUPPORTED', 'invalid bounds rejected' );
	add_user_meta( $b, 'cb_mobile', '۱۲۳' );
	$wpdb->insert( $wpdb->usermeta, array( 'user_id' => 99999999, 'meta_key' => 'cb_mobile', 'meta_value' => 'synthetic_orphan' ) );
	$result = $audit->scan();
	$check( $result['status'] === 'UNSUPPORTED' && $result['unsupported_rows'] === 2 && $result['conflicting_keys'] === 2, 'non-ASCII and orphan metadata fail closed' );
	$original_table = $wpdb->usermeta; $wpdb->usermeta = $prefix . 'missing_usermeta';
	$check( $audit->scan()['status'] === 'UNAVAILABLE', 'database query failure is unavailable without details' );
	$wpdb->usermeta = $original_table;
	$check( $audit->scan()['status'] === 'UNSUPPORTED', 'query failure does not mutate database binding' );
	global $wp_version; echo "Checks: {$checks}; failures: {$failures}.\n";
	if ( ! $failures ) { echo 'PASS inactive identity exact audit WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n"; }
	exit( $failures ? 1 : 0 );
} catch ( Throwable $e ) { fwrite( STDERR, 'Synthetic identity audit fixture failed: ' . get_class( $e ) . "\n" ); exit( 2 ); }
