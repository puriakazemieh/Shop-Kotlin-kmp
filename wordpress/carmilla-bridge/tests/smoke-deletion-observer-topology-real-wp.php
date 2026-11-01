<?php
/** Disposable installed WordPress only; no account deletion or customer data. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';

global $wpdb;
$failed = 0;
$check = static function ( bool $ok, string $label ) use ( &$failed ): void {
	if ( ! $ok ) { ++$failed; }
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n";
};
$connect = new ReflectionMethod( 'CB_Deletion_Incident_Runtime', 'connect' );
$connect->setAccessible( true );
$blocked = static function () use ( $connect ): bool {
	try { $db = $connect->invoke( null ); if ( $db instanceof mysqli ) { $db->close(); } return false; }
	catch ( RuntimeException $error ) { return true; }
};

$db = $connect->invoke( null );
$check( $db instanceof mysqli && $db->query( 'SELECT DATABASE()' )->fetch_row()[0] === DB_NAME, 'standard single-site topology observes the active database' );
$db->close();

$dropin = WP_CONTENT_DIR . '/db.php';
if ( file_exists( $dropin ) ) { throw new RuntimeException( 'Disposable site already has a DB drop-in.' ); }
file_put_contents( $dropin, "<?php /* synthetic disposable topology probe */\n" );
try { $check( $blocked(), 'DB drop-in topology fails closed' ); }
finally { unlink( $dropin ); }

$wpdb->select( 'information_schema' );
$check( $wpdb->get_var( 'SELECT DATABASE()' ) === 'information_schema', 'synthetic alternate schema selected' );
try { $check( $blocked(), 'mismatched active schema fails closed' ); }
finally { $wpdb->select( DB_NAME ); }

if ( ! defined( 'CUSTOM_USER_TABLE' ) ) {
	define( 'CUSTOM_USER_TABLE', 'synthetic_other_users' );
	$check( $blocked(), 'custom user table topology fails closed' );
}

echo "Failures: {$failed}.\n";
exit( $failed ? 1 : 0 );
