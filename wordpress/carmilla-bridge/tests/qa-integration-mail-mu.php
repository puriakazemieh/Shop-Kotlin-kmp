<?php
/** Temporary disposable-WordPress HTTP test hook: intercept every wp_mail call. */
if ( ! file_exists( '/tmp/carmilla-qa-integration-enabled' ) ) { return; }
add_filter( 'pre_wp_mail', static function ( $result, $args ) {
	header( 'X-Carmilla-QA-Mail: intercepted' );
	header( 'X-Carmilla-QA-Body: ' . ( ( $args['message'] ?? '' ) === "Hello'world" ? 'ok' : 'bad' ) );
	return true;
}, 10, 2 );
