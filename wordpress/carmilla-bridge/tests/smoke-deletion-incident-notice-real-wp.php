<?php
/** Real WP and a synthetic database only. Does not register a product hook. */
$prefix = getenv( 'CARMILLA_LOGIN_TEST_PREFIX' );
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) ) { fwrite( STDERR, "Disposable test flag and random prefix required.\n" ); exit( 2 ); }
define( 'ABSPATH', '/usr/src/wordpress/' ); define( 'WP_INSTALLING', true ); define( 'WP_DEBUG', false );
define( 'DB_NAME', 'carmilla_login_qa' ); define( 'DB_USER', 'synthetic_qa' ); define( 'DB_PASSWORD', 'synthetic_qa_password' ); define( 'DB_HOST', '127.0.0.1:3306' );
define( 'DB_CHARSET', 'utf8mb4' ); define( 'DB_COLLATE', '' ); define( 'WP_HOME', 'https://qa.example.test/' ); define( 'WP_SITEURL', WP_HOME );
foreach ( array( 'AUTH_KEY', 'SECURE_AUTH_KEY', 'LOGGED_IN_KEY', 'NONCE_KEY', 'AUTH_SALT', 'SECURE_AUTH_SALT', 'LOGGED_IN_SALT', 'NONCE_SALT' ) as $name ) { define( $name, hash( 'sha256', 'synthetic-' . $name ) ); }
$table_prefix = $prefix;
$_SERVER['HTTP_HOST'] = 'qa.example.test'; $_SERVER['SERVER_NAME'] = 'qa.example.test'; $_SERVER['SERVER_PORT'] = '443'; $_SERVER['HTTPS'] = 'on'; $_SERVER['REQUEST_METHOD'] = 'GET';
try {
	require ABSPATH . 'wp-settings.php'; require_once ABSPATH . 'wp-admin/includes/upgrade.php';
	add_filter( 'pre_wp_mail', static function () { return true; } );
	wp_install( 'Synthetic QA', 'qa_admin', 'synthetic@example.test', false, '', 'synthetic-only-password' );
	require_once '/work/includes/class-cb-deletion-incident-notice.php';
	$db = new mysqli( '127.0.0.1', 'synthetic_qa', 'synthetic_qa_password', 'carmilla_login_qa', 3306 );
	if ( ! $db->query( CB_Deletion_Incident_Store::schema_sql( $prefix ) ) ) { throw new RuntimeException(); }
	$site_a = new CB_Deletion_Incident_Store( $db, $prefix, hash( 'sha256', 'synthetic-notice-a' ) );
	$site_b = new CB_Deletion_Incident_Store( $db, $prefix, hash( 'sha256', 'synthetic-notice-b' ) );
	$notice = new CB_Deletion_Incident_Notice( $site_a ); $other = new CB_Deletion_Incident_Notice( $site_b );
	$admin = get_user_by( 'login', 'qa_admin' );
	$editor = wp_insert_user( array( 'user_login' => 'qa_editor', 'user_email' => 'editor@example.test', 'user_pass' => 'synthetic-only-password', 'role' => 'editor' ) );
	if ( ! $admin instanceof WP_User || is_wp_error( $editor ) ) { throw new RuntimeException(); }
	$checks = 0; $failures = 0;
	$check = static function ( bool $ok, string $label ) use ( &$checks, &$failures ) { ++$checks; if ( ! $ok ) { ++$failures; } echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n"; };
	$render = static function ( CB_Deletion_Incident_Notice $target ): string { ob_start(); try { $target->render(); return (string) ob_get_contents(); } finally { ob_end_clean(); } };
	wp_set_current_user( (int) $admin->ID );
	$check( $render( $notice ) === '', 'no synthetic incidents yields no notice' );
	$site_a->record_partial_survivor( (int) $admin->ID, 600 );
	$site_a->record_survivor( (int) $editor, 'USER_SURVIVES', 600 );
	$html = $render( $notice );
	$check( strpos( $html, 'notice-warning' ) !== false && substr_count( $html, '<li>' ) === 2, 'administrator sees two bounded observations' );
	$check( strpos( $html, 'example.test' ) === false && strpos( $html, 'synthetic-only-password' ) === false && strpos( $html, '<script' ) === false, 'notice excludes synthetic contact details and credential' );
	$check( $render( $other ) === '', 'different installation does not expose observations' );
	wp_set_current_user( (int) $editor ); $check( $render( $notice ) === '', 'editor cannot view notice' );
	wp_set_current_user( 0 ); $check( $render( $notice ) === '', 'anonymous user cannot view notice' );
	wp_set_current_user( (int) $admin->ID );
	for ( $i = 0; $i < 103; ++$i ) { $site_a->record_survivor( (int) $admin->ID, 'USER_SURVIVES', 600 ); }
	$check( substr_count( $render( $notice ), '<li>' ) === 100, 'notice remains bounded to 100 records' );
	$db->close();
	$html = $render( $notice );
	$check( strpos( $html, 'notice-error' ) !== false && strpos( $html, 'notice-warning' ) === false && strpos( $html, 'mysqli' ) === false, 'unavailable storage is an explicit sanitized error' );
	wp_set_current_user( (int) $editor ); $check( $render( $notice ) === '', 'unauthorized denial precedes failed storage access' );
	global $wp_version; echo "Checks: {$checks}; failures: {$failures}.\n";
	if ( ! $failures ) { echo 'PASS inactive deletion incident notice WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n"; }
	exit( $failures ? 1 : 0 );
} catch ( Throwable $e ) { fwrite( STDERR, 'Synthetic incident notice fixture failed: ' . get_class( $e ) . "\n" ); exit( 2 ); }
