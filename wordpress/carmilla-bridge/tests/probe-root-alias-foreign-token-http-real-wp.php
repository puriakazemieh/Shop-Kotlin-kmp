<?php
/** Disposable WordPress only. A correctly signed token from another issuer must be denied. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
$suffix = bin2hex( random_bytes( 4 ) );
$user = wp_create_user( 'qa_alias_foreign_' . $suffix, wp_generate_password( 24 ), 'qa-alias-foreign-' . $suffix . '@example.test' );
if ( ! is_int( $user ) ) { exit( 2 ); }
( new WP_User( $user ) )->set_role( 'customer' );
$valid = CB_JWT::issue( get_user_by( 'id', $user ), 'access' );
$parts = explode( '.', $valid );
$payload = json_decode( base64_decode( strtr( $parts[1], '-_', '+/' ) ), true );
$payload['iss'] = 'https://other-tenant.example.test';
$body = rtrim( strtr( base64_encode( wp_json_encode( $payload ) ), '+/', '-_' ), '=' );
$signature = rtrim( strtr( base64_encode( hash_hmac( 'sha256', $parts[0] . '.' . $body, CB_JWT::secret(), true ) ), '+/', '-_' ), '=' );
$foreign = $parts[0] . '.' . $body . '.' . $signature;
$failed = 0;
try {
	foreach ( array( 'root', 'canonical' ) as $route ) {
		foreach ( array( 'GET', 'POST' ) as $method ) {
			$url = 'http://127.0.0.1' . ( 'root' === $route ? '' : '/wp-json/carmilla/v1' ) . '/api/favorites' . ( 'POST' === $method ? '/909090' : '' );
			$response = wp_remote_request( $url, array( 'method' => $method, 'headers' => array( 'Authorization' => 'Bearer ' . $foreign ), 'timeout' => 15, 'redirection' => 0 ) );
			wp_cache_delete( $user, 'user_meta' );
			$ok = ! is_wp_error( $response ) && wp_remote_retrieve_response_code( $response ) === 401 && ! get_user_meta( $user, 'cb_wishlist', true );
			echo ( $ok ? 'PASS ' : 'FAIL ' ) . $route . '_' . strtolower( $method ) . '_foreign_issuer_denied' . "\n";
			$failed += $ok ? 0 : 1;
		}
	}
} finally {
	wp_delete_user( $user );
}
echo 'Failures: ' . $failed . '; cleanup=' . (int) ( false === get_user_by( 'id', $user ) ) . "\n";
exit( $failed ? 1 : 0 );
