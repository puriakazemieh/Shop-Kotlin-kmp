<?php
/** Isolated WordPress and synthetic disposable database only. */
$prefix = getenv( 'CARMILLA_ADAPTER_TEST_PREFIX' );
if ( getenv( 'CARMILLA_ADAPTER_DISPOSABLE' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) ) { fwrite( STDERR, "Disposable fixture required.\n" ); exit( 2 ); }
define( 'ABSPATH', '/usr/src/wordpress/' ); define( 'WP_INSTALLING', true ); define( 'WP_DEBUG', false );
define( 'DB_NAME', 'carmilla_adapter_qa' ); define( 'DB_USER', 'synthetic_qa' ); define( 'DB_PASSWORD', 'synthetic_qa_password' ); define( 'DB_HOST', '127.0.0.1:3306' ); define( 'DB_CHARSET', 'utf8mb4' ); define( 'DB_COLLATE', '' ); define( 'WP_HOME', 'https://qa.example.test/' ); define( 'WP_SITEURL', WP_HOME );
foreach ( array( 'AUTH_KEY', 'SECURE_AUTH_KEY', 'LOGGED_IN_KEY', 'NONCE_KEY', 'AUTH_SALT', 'SECURE_AUTH_SALT', 'LOGGED_IN_SALT', 'NONCE_SALT' ) as $name ) { define( $name, hash( 'sha256', 'synthetic-' . $name ) ); }
$table_prefix = $prefix; $_SERVER['HTTP_HOST'] = 'qa.example.test'; $_SERVER['SERVER_NAME'] = 'qa.example.test'; $_SERVER['SERVER_PORT'] = '443'; $_SERVER['HTTPS'] = 'on'; $_SERVER['REQUEST_METHOD'] = 'GET';
try {
	require ABSPATH . 'wp-settings.php'; require_once ABSPATH . 'wp-admin/includes/upgrade.php'; add_filter( 'pre_wp_mail', static function () { return true; } );
	wp_install( 'Synthetic migration QA', 'qa_admin', 'synthetic@example.test', false, '', 'synthetic-only-password' );
	require_once '/work/includes/class-cb-web-session-migration.php';
	$db = new mysqli( '127.0.0.1', 'synthetic_qa', 'synthetic_qa_password', DB_NAME, 3306 );
	$admin = get_user_by( 'login', 'qa_admin' ); $post_id = wp_insert_post( array( 'post_title' => 'Synthetic preservation sentinel', 'post_status' => 'publish' ), true );
	if ( ! $admin instanceof WP_User || is_wp_error( $post_id ) || ! $post_id ) { throw new RuntimeException(); }
	$checks = 0; $failures = 0;
	$check = static function ( bool $ok, string $label ) use ( &$checks, &$failures ) { ++$checks; if ( ! $ok ) { ++$failures; } echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n"; };
	$refused = static function ( CB_Web_Session_Migration $migration ): bool { try { $migration->ensure_v1(); return false; } catch ( CB_Web_Session_Migration_Exception $e ) { return $e->getMessage() === 'Session migration unavailable; preserve data and inspect recovery procedure.'; } };
	$migration = new CB_Web_Session_Migration( $db, $prefix );
	$check( $migration->ensure_v1() === 'INSTALLED', 'empty namespace installs version 1' );
	$check( $migration->ensure_v1() === 'CURRENT', 'repeat is idempotent' );
	$store = new CB_Web_Session_Store( $db, $prefix, str_repeat( 'a', 64 ), str_repeat( 'b', 32 ), static function () { return 'synthetic-hash'; } );
	$session = $store->create( (int) $admin->ID );
	$check( is_array( $session ) && $store->is_active( $session['family_id'], (int) $admin->ID ), 'installed schema supports real session store' );
	$check( $migration->ensure_v1() === 'CURRENT' && $store->is_active( $session['family_id'], (int) $admin->ID ), 'repeat preserves existing session' );
	$check( get_user_by( 'id', $admin->ID ) instanceof WP_User && get_post( $post_id )->post_title === 'Synthetic preservation sentinel', 'existing synthetic WordPress account and post preserved' );
	$partial = $prefix . 'partial_'; $ddl = CB_Web_Session_Store::schema_sql( $partial ); $db->query( $ddl[0] );
	$partial_table = $partial . 'carmilla_web_families'; $partial_tokens = $partial . 'carmilla_web_tokens'; $partial_schema = $partial . 'carmilla_web_schema';
	$check( $refused( new CB_Web_Session_Migration( $db, $partial ) ) && (int) $db->query( "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('$partial_table','$partial_tokens','$partial_schema')" )->fetch_row()[0] === 1, 'partial creation refused without repair or deletion' );
	$future = $prefix . 'future_'; $future_migration = new CB_Web_Session_Migration( $db, $future ); $future_migration->ensure_v1(); $future_table = $future . 'carmilla_web_schema';
	$db->query( "UPDATE `$future_table` SET version=2 WHERE id=1" );
	$check( $refused( $future_migration ) && (int) $db->query( "SELECT version FROM `$future_table` WHERE id=1" )->fetch_row()[0] === 2, 'unknown future version refused without downgrade' );
	$broken = $prefix . 'broken_'; $broken_migration = new CB_Web_Session_Migration( $db, $broken ); $broken_migration->ensure_v1(); $broken_table = $broken . 'carmilla_web_tokens';
	$db->query( "ALTER TABLE `$broken_table` ADD extra_column INT NULL" );
	$check( $refused( $broken_migration ), 'incompatible extra column refused' );
	$bad_index = $prefix . 'index_'; $index_migration = new CB_Web_Session_Migration( $db, $bad_index ); $index_migration->ensure_v1(); $index_table = $bad_index . 'carmilla_web_tokens';
	$db->query( "ALTER TABLE `$index_table` ADD KEY extra_key (spent)" );
	$check( $refused( $index_migration ), 'unexpected index refused' );
	$bad_engine = $prefix . 'engine_'; $engine_migration = new CB_Web_Session_Migration( $db, $bad_engine ); $engine_migration->ensure_v1(); $engine_table = $bad_engine . 'carmilla_web_schema';
	$db->query( "ALTER TABLE `$engine_table` ENGINE=MyISAM" );
	$check( $refused( $engine_migration ), 'nontransactional version table refused' );
	$bad_marker = $prefix . 'marker_'; $marker_migration = new CB_Web_Session_Migration( $db, $bad_marker ); $marker_migration->ensure_v1(); $marker_table = $bad_marker . 'carmilla_web_schema';
	$db->query( "INSERT INTO `$marker_table` (id,version) VALUES (2,1)" );
	$check( $refused( $marker_migration ), 'unexpected additional schema marker refused' );
	$bad_trigger = $prefix . 'trigger_'; $trigger_migration = new CB_Web_Session_Migration( $db, $bad_trigger ); $trigger_migration->ensure_v1(); $trigger_table = $bad_trigger . 'carmilla_web_tokens'; $trigger_name = $prefix . 'migration_test_trigger';
	$db->query( "CREATE TRIGGER `$trigger_name` BEFORE INSERT ON `$trigger_table` FOR EACH ROW SET @synthetic_trigger=1" );
	$check( $refused( $trigger_migration ), 'unexpected trigger refused' );
	$lock_db = new mysqli( '127.0.0.1', 'synthetic_qa', 'synthetic_qa_password', DB_NAME, 3306 );
	$lock = 'cbm:' . substr( hash( 'sha256', DB_NAME . "\0" . $prefix ), 0, 60 ); $lock_stmt = $lock_db->prepare( 'SELECT GET_LOCK(?,0)' ); $lock_stmt->bind_param( 's', $lock ); $lock_stmt->execute(); $lock_stmt->get_result()->fetch_row(); $lock_stmt->close();
	$check( $refused( $migration ), 'concurrent migration lock fails closed' );
	$lock_stmt = $lock_db->prepare( 'SELECT RELEASE_LOCK(?)' ); $lock_stmt->bind_param( 's', $lock ); $lock_stmt->execute(); $lock_stmt->close(); $lock_db->close();
	$check( $migration->ensure_v1() === 'CURRENT', 'migration remains current after lock release' );
	$invalid_prefix = false; try { new CB_Web_Session_Migration( $db, 'bad-name' ); } catch ( CB_Web_Session_Migration_Exception $e ) { $invalid_prefix = true; }
	$check( $invalid_prefix, 'unsafe prefix refused' );
	$db->close(); $check( $refused( $migration ), 'closed database fails with sanitized error' );
	global $wp_version; echo "Checks: {$checks}; failures: {$failures}.\n";
	if ( ! $failures ) { echo 'PASS inactive session migration WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n"; }
	exit( $failures ? 1 : 0 );
} catch ( Throwable $e ) { fwrite( STDERR, 'Migration fixture failed: ' . get_class( $e ) . "\n" ); exit( 2 ); }
