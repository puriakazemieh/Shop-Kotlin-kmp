<?php
/** Disposable WordPress only. Compares malformed HTTP inputs on root/canonical REST routes. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
$suffix = bin2hex( random_bytes( 4 ) );
$user = wp_create_user( 'qa_alias_malformed_' . $suffix, wp_generate_password( 24 ), 'qa-alias-malformed-' . $suffix . '@example.test' );
if ( ! is_int( $user ) ) { exit( 2 ); }
( new WP_User( $user ) )->set_role( 'customer' );
$session = WP_Session_Tokens::get_instance( $user )->create( time() + 3600 );
$expires = time() + 3600;
$logged = wp_generate_auth_cookie( $user, $expires, 'logged_in', $session );
$cookie = AUTH_COOKIE . '=' . wp_generate_auth_cookie( $user, $expires, 'auth', $session ) . '; ' . LOGGED_IN_COOKIE . '=' . $logged;
$_COOKIE[ LOGGED_IN_COOKIE ] = $logged;
wp_set_current_user( $user );
$nonce = wp_create_nonce( 'wp_rest' );
$bearer = CB_JWT::issue( get_user_by( 'id', $user ), 'access' );
wp_set_current_user( 0 );
$cases = array(
	array( 'array_page', 'GET', '/api/favorites?page%5B%5D=1', array( 'Authorization' => 'Bearer ' . $bearer ), '' ),
	array( 'array_size', 'GET', '/api/recently-viewed?size%5B%5D=2', array( 'Authorization' => 'Bearer ' . $bearer ), '' ),
	array( 'nested_query', 'GET', '/api/users/me?unexpected%5Bx%5D=value', array( 'Authorization' => 'Bearer ' . $bearer ), '' ),
	array( 'invalid_nonce', 'GET', '/api/addresses', array( 'Cookie' => $cookie, 'X-WP-Nonce' => 'malformed-nonce' ), '' ),
	array( 'invalid_bearer', 'GET', '/api/addresses', array( 'Authorization' => 'Bearer malformed-token' ), '' ),
	array( 'form_content_type', 'POST', '/api/favorites/987654321', array( 'Authorization' => 'Bearer ' . $bearer, 'Content-Type' => 'application/x-www-form-urlencoded' ), 'probe=1' ),
);
$fail = 0;
try {
	foreach ( $cases as $case ) {
		list( $name, $method, $path, $headers, $body ) = $case;
		$out = array();
		foreach ( array( 'root', 'canonical' ) as $route ) {
			$url = 'http://127.0.0.1' . ( 'root' === $route ? $path : '/wp-json/carmilla/v1' . $path );
			$response = wp_remote_request( $url, array( 'method' => $method, 'headers' => $headers, 'body' => $body, 'timeout' => 15, 'redirection' => 0 ) );
			if ( is_wp_error( $response ) ) { $out[ $route ] = array( 0, 'transport_error', null, '' ); continue; }
			$data = json_decode( wp_remote_retrieve_body( $response ), true );
			$out[ $route ] = array( wp_remote_retrieve_response_code( $response ), is_array( $data ) ? (string) ( $data['code'] ?? '' ) : '', $data, wp_remote_retrieve_header( $response, 'x-carmilla-deprecated' ) );
		}
		$root = $out['root']; $canonical = $out['canonical'];
		$ok = $root[0] > 0 && $root[0] === $canonical[0] && $root[1] === $canonical[1] && $root[2] === $canonical[2] && $root[3] === 'root-alias' && $canonical[3] === '';
		echo ( $ok ? 'PASS ' : 'FAIL ' ) . $name . ' root=' . $root[0] . '/' . $root[1] . ' canonical=' . $canonical[0] . '/' . $canonical[1] . "\n";
		$fail += $ok ? 0 : 1;
	}
} finally {
	delete_user_meta( $user, 'cb_wishlist' );
	WP_Session_Tokens::get_instance( $user )->destroy( $session );
	wp_delete_user( $user );
}
echo 'Failures: ' . $fail . '; cleanup=' . (int) ( false === get_user_by( 'id', $user ) ) . "\n";
exit( $fail ? 1 : 0 );
