<?php
/** Real WordPress, isolated synthetic database only; no production hooks or routes. */
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
	require_once '/work/includes/class-cb-deletion-incident-store.php';
	$db = new mysqli( '127.0.0.1', 'synthetic_qa', 'synthetic_qa_password', 'carmilla_login_qa', 3306 );
	if ( ! $db->query( CB_Deletion_Incident_Store::schema_sql( $prefix ) ) ) { throw new RuntimeException(); }
	$site_a = hash( 'sha256', 'synthetic-site-a' ); $site_b = hash( 'sha256', 'synthetic-site-b' ); $time = 1700000000;
	$clock = static function () use ( &$time ) { return $time; };
	$a = new CB_Deletion_Incident_Store( $db, $prefix, $site_a, $clock ); $b = new CB_Deletion_Incident_Store( $db, $prefix, $site_b, $clock );
	$checks = 0; $failures = 0;
	$check = static function ( bool $ok, string $label ) use ( &$checks, &$failures ) { ++$checks; if ( ! $ok ) { ++$failures; } echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n"; };
	$admin = get_user_by( 'login', 'qa_admin' ); $editor = wp_insert_user( array( 'user_login' => 'qa_editor', 'user_email' => 'editor@example.test', 'user_pass' => 'synthetic-only-password', 'role' => 'editor' ) );
	if ( ! $admin instanceof WP_User || is_wp_error( $editor ) ) { throw new RuntimeException(); }
	$id = $a->record_partial_survivor( (int) $admin->ID, 10 ); $id_b = $b->record_partial_survivor( (int) $editor, 10 );
	$check( preg_match( '/\A[0-9a-f]{32}\z/', $id ) === 1 && $id !== $id_b, 'random incident references differ and use fixed encoding' );
	wp_set_current_user( (int) $editor ); $check( $a->read( $id ) === array( 'status' => 'FORBIDDEN' ), 'editor cannot read incident' );
	wp_set_current_user( 0 ); $check( $a->read( $id ) === array( 'status' => 'FORBIDDEN' ), 'logged-out caller cannot read incident' );
	wp_set_current_user( (int) $admin->ID );
	$check( $a->read( $id ) === array( 'status' => 'AVAILABLE', 'account_id' => (int) $admin->ID, 'code' => 'PARTIAL_USER_SURVIVES' ), 'authorized read returns only minimal synthetic reference' );
	$check( $b->read( $id ) === array( 'status' => 'MISSING' ), 'other installation cannot read incident' );
	$check( $a->read( str_repeat( '0', 32 ) ) === array( 'status' => 'MISSING' ), 'unknown reference is missing' );
	$check( $a->read( 'bad' ) === array( 'status' => 'UNVERIFIED' ), 'malformed reference fails closed' );
	$list_time = 1800000000; $list_clock = static function () use ( &$list_time ) { return $list_time; };
	$c = new CB_Deletion_Incident_Store( $db, $prefix, hash( 'sha256', 'synthetic-site-c' ), $list_clock );
	$first = $c->record_partial_survivor( (int) $admin->ID, 3 ); ++$list_time;
	$second = $c->record_survivor( (int) $editor, 'USER_SURVIVES', 10 );
	$list = $c->list_unexpired( 1 );
	$check( $list === array( 'status' => 'AVAILABLE', 'incidents' => array( array( 'incident_id' => $second, 'account_id' => (int) $editor, 'code' => 'USER_SURVIVES' ) ) ), 'authorized listing is bounded and newest first' );
	$check( count( $c->list_unexpired( 100 )['incidents'] ) === 2 && count( $a->list_unexpired( 100 )['incidents'] ) === 1, 'listing is scoped to current installation' );
	$check( $c->list_unexpired( 0 ) === array( 'status' => 'UNVERIFIED' ) && $c->list_unexpired( 101 ) === array( 'status' => 'UNVERIFIED' ), 'listing rejects invalid bounds' );
	$list_table = $prefix . 'carmilla_deletion_incidents';
	$db->query( "UPDATE `$list_table` SET account_id=0 WHERE incident_id='$second'" );
	$check( $c->list_unexpired( 100 ) === array( 'status' => 'UNVERIFIED' ), 'malformed record fails whole listing closed' );
	$db->query( "UPDATE `$list_table` SET account_id=" . (int) $editor . " WHERE incident_id='$second'" );
	wp_set_current_user( (int) $editor ); $check( $c->list_unexpired( 1 ) === array( 'status' => 'FORBIDDEN' ), 'editor cannot discover incidents' );
	wp_set_current_user( 0 ); $check( $c->list_unexpired( 1 ) === array( 'status' => 'FORBIDDEN' ), 'logged-out caller cannot discover incidents' );
	wp_set_current_user( (int) $admin->ID ); $list_time += 2;
	$check( count( $c->list_unexpired( 100 )['incidents'] ) === 1, 'listing excludes expired incidents at boundary' );
	$list_time += 8; $check( $c->list_unexpired( 100 ) === array( 'status' => 'AVAILABLE', 'incidents' => array() ) && $c->prune_expired( 100 ) === 2, 'empty listing and bounded expiry cleanup' );
	$table = $prefix . 'carmilla_deletion_incidents'; $result = $db->query( "SELECT * FROM `$table`" ); $rows = $result->fetch_all( MYSQLI_ASSOC );
	$check( array_keys( $rows[0] ) === array( 'incident_id', 'site_id', 'account_id', 'incident_code', 'recorded_at', 'expires_at' ) && strpos( var_export( $rows, true ), 'example.test' ) === false && strpos( var_export( $rows, true ), 'synthetic-only-password' ) === false, 'stored fields exclude synthetic contact details and password' );
	$time += 10; $check( $a->read( $id ) === array( 'status' => 'EXPIRED' ), 'expiry boundary withholds account reference' );
	wp_set_current_user( (int) $editor ); $forbidden_prune = false; try { $a->prune_expired( 1 ); } catch ( CB_Deletion_Incident_Storage_Exception $e ) { $forbidden_prune = true; }
	$check( $forbidden_prune, 'editor cannot prune incidents' );
	wp_set_current_user( (int) $admin->ID ); $check( $a->prune_expired( 1 ) === 1 && $a->read( $id ) === array( 'status' => 'MISSING' ) && $b->read( $id_b ) === array( 'status' => 'EXPIRED' ), 'bounded cleanup removes only expired records of current installation' );
	$check( $b->prune_expired( 1 ) === 1, 'other installation can clean its own expired record' );
	$db->query( "CREATE TRIGGER `{$prefix}incident_write_fault` BEFORE INSERT ON `$table` FOR EACH ROW BEGIN IF @incident_write_fault=1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='synthetic_private_marker'; END IF; END" );
	$db->query( 'SET @incident_write_fault=1' ); $write_fault = false;
	try { $a->record_partial_survivor( (int) $admin->ID, 10 ); } catch ( CB_Deletion_Incident_Storage_Exception $e ) { $write_fault = $e->getMessage() === 'Incident storage unavailable.'; }
	$db->query( 'SET @incident_write_fault=NULL' ); $result = $db->query( "SELECT COUNT(*) AS count FROM `$table`" ); $row = $result->fetch_assoc();
	$check( $write_fault && (int) $row['count'] === 0, 'injected insert failure is sanitized and leaves no incident row' );
	$temporary_table = $table . '_unavailable'; $db->query( "RENAME TABLE `$table` TO `$temporary_table`" );
	$check( $a->read( str_repeat( '0', 32 ) ) === array( 'status' => 'UNVERIFIED' ), 'actual failed table query is not reported as missing' );
	$check( $a->list_unexpired( 1 ) === array( 'status' => 'UNVERIFIED' ), 'failed listing is not reported as empty' );
	$db->query( "RENAME TABLE `$temporary_table` TO `$table`" );
	$invalid_ttl = false; try { $a->record_partial_survivor( (int) $admin->ID, 604801 ); } catch ( CB_Deletion_Incident_Storage_Exception $e ) { $invalid_ttl = true; }
	$check( $invalid_ttl, 'out-of-bound retention rejected' );
	$invalid_limit = false; try { $a->prune_expired( 101 ); } catch ( CB_Deletion_Incident_Storage_Exception $e ) { $invalid_limit = true; }
	$check( $invalid_limit, 'unbounded cleanup rejected' );
	$bad_prefix = $prefix . 'bad_'; $db->query( CB_Deletion_Incident_Store::schema_sql( $bad_prefix ) ); $bad_table = $bad_prefix . 'carmilla_deletion_incidents'; $db->query( "ALTER TABLE `$bad_table` ADD private_data TEXT" );
	$bad_schema = false; try { new CB_Deletion_Incident_Store( $db, $bad_prefix, $site_a, $clock ); } catch ( CB_Deletion_Incident_Storage_Exception $e ) { $bad_schema = true; }
	$check( $bad_schema, 'unexpected extra data column makes schema unavailable' );
	$bad_clock = new CB_Deletion_Incident_Store( $db, $prefix, $site_a, static function () { return 'invalid'; } );
	$check( $bad_clock->read( str_repeat( '0', 32 ) ) === array( 'status' => 'UNVERIFIED' ), 'invalid clock fails closed' );
	$check( $bad_clock->list_unexpired( 1 ) === array( 'status' => 'UNVERIFIED' ), 'invalid clock fails listing closed' );
	$db->close(); wp_set_current_user( (int) $editor ); $check( $a->read( str_repeat( '0', 32 ) ) === array( 'status' => 'FORBIDDEN' ), 'authorization precedes storage access' );
	$check( $a->list_unexpired( 1 ) === array( 'status' => 'FORBIDDEN' ), 'listing authorization precedes storage access' );
	wp_set_current_user( (int) $admin->ID ); $check( $a->read( str_repeat( '0', 32 ) ) === array( 'status' => 'UNVERIFIED' ), 'closed database read is not reported as missing or recovered' );
	$check( $a->list_unexpired( 1 ) === array( 'status' => 'UNVERIFIED' ), 'closed database listing fails closed' );
	$write_failed = false; try { $a->record_partial_survivor( (int) $admin->ID, 10 ); } catch ( CB_Deletion_Incident_Storage_Exception $e ) { $write_failed = $e->getMessage() === 'Incident storage unavailable.'; }
	$check( $write_failed, 'closed database write fails without logging account or raw error' );
	global $wp_version; echo "Checks: {$checks}; failures: {$failures}.\n";
	if ( ! $failures ) { echo 'PASS inactive deletion incident storage WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n"; }
	exit( $failures ? 1 : 0 );
} catch ( Throwable $e ) { fwrite( STDERR, 'Incident storage fixture infrastructure failed: ' . get_class( $e ) . "\n" ); exit( 2 ); }
