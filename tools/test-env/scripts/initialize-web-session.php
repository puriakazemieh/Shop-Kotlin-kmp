<?php
/** Explicit and idempotent local-QA migration; never runs during HTTP bootstrap. */
if ( PHP_SAPI !== 'cli' || getenv( 'CARMILLA_LOCAL_WEB_SESSION_MIGRATE' ) !== 'yes' ) {
	fwrite( STDERR, "Refusing browser-session migration without explicit local-QA flag.\n" );
	exit( 2 );
}

require_once '/var/www/html/wp-load.php';

try {
	if ( ! defined( 'CB_WEB_SESSION_ENABLED' ) || CB_WEB_SESSION_ENABLED !== true || ! class_exists( 'CB_Web_Session_Migration' ) ) {
		throw new RuntimeException();
	}
	$db = new mysqli( '127.0.0.1', DB_USER, DB_PASSWORD, DB_NAME, 3306 );
	if ( $db->connect_errno !== 0 ) {
		throw new RuntimeException();
	}
	$result = ( new CB_Web_Session_Migration( $db, $GLOBALS['wpdb']->prefix ) )->ensure_v1();
	$db->close();
	echo "Local browser-session migration: {$result}\n";
} catch ( Throwable $ignored ) {
	fwrite( STDERR, "Local browser-session migration failed; inspect the disposable fixture.\n" );
	exit( 4 );
}
