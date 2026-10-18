<?php
/** Read-only verification against the explicitly named synthetic restored database. */
$prefix = getenv( 'CARMILLA_ADAPTER_TEST_PREFIX' );
if ( getenv( 'CARMILLA_ADAPTER_RESTORE_DISPOSABLE' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) ) { fwrite( STDERR, "Synthetic restored fixture required.\n" ); exit( 2 ); }
define( 'ABSPATH', '/usr/src/wordpress/' ); define( 'WP_DEBUG', false );
define( 'DB_NAME', 'carmilla_adapter_restore_qa' ); define( 'DB_USER', 'synthetic_qa' ); define( 'DB_PASSWORD', 'synthetic_qa_password' ); define( 'DB_HOST', '127.0.0.1:3306' ); define( 'DB_CHARSET', 'utf8mb4' ); define( 'DB_COLLATE', '' ); define( 'WP_HOME', 'https://qa.example.test/' ); define( 'WP_SITEURL', WP_HOME );
foreach ( array( 'AUTH_KEY', 'SECURE_AUTH_KEY', 'LOGGED_IN_KEY', 'NONCE_KEY', 'AUTH_SALT', 'SECURE_AUTH_SALT', 'LOGGED_IN_SALT', 'NONCE_SALT' ) as $name ) { define( $name, hash( 'sha256', 'synthetic-' . $name ) ); }
$table_prefix = $prefix; $_SERVER['HTTP_HOST'] = 'qa.example.test'; $_SERVER['SERVER_NAME'] = 'qa.example.test'; $_SERVER['SERVER_PORT'] = '443'; $_SERVER['HTTPS'] = 'on'; $_SERVER['REQUEST_METHOD'] = 'GET';
try {
	require ABSPATH . 'wp-settings.php'; require_once '/work/includes/class-cb-web-session-store.php';
	global $wpdb; $user = get_user_by( 'login', 'qa_admin' );
	$post = get_page_by_title( 'Synthetic preservation sentinel', OBJECT, 'post' );
	$db = new mysqli( '127.0.0.1', 'synthetic_qa', 'synthetic_qa_password', DB_NAME, 3306 );
	$marker_table = $prefix . 'carmilla_web_schema'; $marker_result = $db->query( "SELECT id,version FROM `$marker_table`" );
	$markers = $marker_result ? $marker_result->fetch_all( MYSQLI_ASSOC ) : array();
	$current = count( $markers ) === 1 && (int) $markers[0]['id'] === 1 && (int) $markers[0]['version'] === 1;
	$store = new CB_Web_Session_Store( $db, $prefix, str_repeat( 'a', 64 ), str_repeat( 'b', 32 ), static function () { return 'synthetic-hash'; } );
	$table = $prefix . 'carmilla_web_families'; $result = $db->query( "SELECT family_id,user_id FROM `$table`" ); $family = $result ? $result->fetch_assoc() : null;
	if ( ! $user instanceof WP_User || ! $post instanceof WP_Post || ! $current || ! $family || ! $store->is_active( $family['family_id'], (int) $family['user_id'] ) || (int) $family['user_id'] !== (int) $user->ID ) { throw new RuntimeException(); }
	global $wp_version; echo 'PASS restored synthetic WordPress, migration marker and active session WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n";
} catch ( Throwable $e ) { fwrite( STDERR, 'Restored fixture verification failed: ' . get_class( $e ) . "\n" ); exit( 2 ); }
