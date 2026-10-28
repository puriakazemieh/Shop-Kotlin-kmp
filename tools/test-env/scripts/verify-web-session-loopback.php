<?php
/**
 * One-shot local QA probe. It never registers routes, migrates schema, or
 * prints credentials, salts, cookies, or tokens.
 */
require_once '/var/www/html/wp-load.php';

try {
	if ( ! class_exists( 'CB_Web_Session_WP_Adapter' ) || ! defined( 'AUTH_KEY' ) || ! is_string( AUTH_KEY ) || AUTH_KEY === '' ) {
		throw new RuntimeException();
	}
	if ( get_site_url( 1 ) !== 'http://localhost:8080' ) {
		throw new RuntimeException();
	}
	define( 'CB_WEB_SESSION_SITE_URL', 'http://localhost:8080' );
	define( 'CB_WEB_SESSION_SECRET_HEX', hash( 'sha256', AUTH_KEY . "\0carmilla-local-web-session-v1" ) );

	$store = CB_Web_Session_WP_Adapter::from_wordpress();
	if ( ! $store instanceof CB_Web_Session_Store ) {
		throw new RuntimeException();
	}
	echo "PASS: WordPress session adapter verified a loopback database fixture.\n";
} catch ( Throwable $ignored ) {
	fwrite( STDERR, "FAIL: loopback fixture verification failed.\n" );
	exit( 1 );
}
