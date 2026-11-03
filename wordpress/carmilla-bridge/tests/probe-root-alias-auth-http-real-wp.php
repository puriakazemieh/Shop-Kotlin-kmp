<?php
/** Disposable installed WordPress only. Actual HTTP parity probe with synthetic user state. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
$suffix = bin2hex( random_bytes( 4 ) );
$user = wp_create_user( 'qa_alias_' . $suffix, wp_generate_password( 24 ), 'qa-alias-' . $suffix . '@example.test' );
if ( ! is_int( $user ) ) { exit( 2 ); }
( new WP_User( $user ) )->set_role( 'customer' );
$token = WP_Session_Tokens::get_instance( $user )->create( time() + 3600 );
$expires = time() + 3600;
$logged_cookie = wp_generate_auth_cookie( $user, $expires, 'logged_in', $token );
$cookie = AUTH_COOKIE . '=' . wp_generate_auth_cookie( $user, $expires, 'auth', $token ) . '; ' . LOGGED_IN_COOKIE . '=' . $logged_cookie;
$_COOKIE[ LOGGED_IN_COOKIE ] = $logged_cookie;
wp_set_current_user( $user );
$nonce = wp_create_nonce( 'wp_rest' );
$bearer = CB_JWT::issue( get_user_by( 'id', $user ), 'access' );
wp_set_current_user( 0 );
$keys = array( 'first_name', 'cb_addresses', 'cb_addr_seq', 'cb_wishlist', 'cb_recently_viewed' );
$reset = static function () use ( $user, $keys ) { foreach ( $keys as $key ) { delete_user_meta( $user, $key ); } };
$snapshot = static function () use ( $user, $keys ) { wp_cache_delete( $user, 'user_meta' ); $out = array(); foreach ( $keys as $key ) { $out[ $key ] = get_user_meta( $user, $key, true ); } return $out; };
$cases = array(
	array( 'profile_read', 'GET', '/api/users/me', '' ),
	array( 'profile_write', 'PATCH', '/api/users/me', '{"firstName":"AliasProbe"}' ),
	array( 'addresses_read', 'GET', '/api/addresses', '' ),
	array( 'addresses_write', 'POST', '/api/addresses', '{"receiverName":"Probe","city":"Test"}' ),
	array( 'favorites_read', 'GET', '/api/favorites', '' ),
	array( 'favorites_write', 'POST', '/api/favorites/987654321', '' ),
	array( 'recent_read', 'GET', '/api/recently-viewed', '' ),
	array( 'recent_write', 'POST', '/api/recently-viewed/987654321', '' ),
);
$auths = array( 'anonymous', 'cookie_no_nonce', 'cookie_nonce', 'bearer' );
$failures = 0;
$findings = 0;
try {
	foreach ( $cases as $case ) {
		list( $name, $method, $path, $body ) = $case;
		foreach ( $auths as $auth ) {
			$results = array();
			foreach ( array( 'root', 'canonical' ) as $route ) {
				$reset();
				$before = $snapshot();
				$headers = array( 'Content-Type' => 'application/json' );
				if ( 'cookie_no_nonce' === $auth || 'cookie_nonce' === $auth ) { $headers['Cookie'] = $cookie; }
				if ( 'cookie_nonce' === $auth ) { $headers['X-WP-Nonce'] = $nonce; }
				if ( 'bearer' === $auth ) { $headers['Authorization'] = 'Bearer ' . $bearer; }
				$url = 'http://127.0.0.1' . ( 'root' === $route ? $path : '/wp-json/carmilla/v1' . $path );
				$response = wp_remote_request( $url, array( 'method' => $method, 'headers' => $headers, 'body' => $body, 'timeout' => 15, 'redirection' => 0 ) );
				if ( is_wp_error( $response ) ) { $results[ $route ] = array( 'status' => 0, 'code' => 'transport_error', 'changed' => false, 'body' => '' ); continue; }
				$data = json_decode( wp_remote_retrieve_body( $response ), true );
				$results[ $route ] = array(
					'status' => wp_remote_retrieve_response_code( $response ),
					'code' => is_array( $data ) ? (string) ( $data['code'] ?? $data['error']['code'] ?? '' ) : '',
					'changed' => $snapshot() !== $before,
					'body' => $data,
				);
			}
			$root = $results['root']; $canonical = $results['canonical'];
			$parity = $root['status'] === $canonical['status'] && $root['code'] === $canonical['code'] && $root['changed'] === $canonical['changed'];
			if ( 'GET' === $method && $root['status'] >= 200 && $root['status'] < 300 ) { $parity = $parity && $root['body'] === $canonical['body']; }
			$no_auth = 'anonymous' === $auth || 'cookie_no_nonce' === $auth;
			$bypass = $no_auth && ( $root['changed'] || ( $root['status'] >= 200 && $root['status'] < 300 ) );
			$findings += ( $parity && ! $bypass ) ? 0 : 1;
			$failures += 0 === $root['status'] || 0 === $canonical['status'] ? 1 : 0;
			echo ( $parity && ! $bypass ? 'PASS ' : 'FINDING ' ) . $name . ' ' . $auth . ' root=' . $root['status'] . '/' . $root['code'] . '/' . (int) $root['changed'] . ' canonical=' . $canonical['status'] . '/' . $canonical['code'] . '/' . (int) $canonical['changed'] . "\n";
		}
	}
} finally {
	$reset();
	WP_Session_Tokens::get_instance( $user )->destroy( $token );
	wp_delete_user( $user );
}
echo 'Transport failures: ' . $failures . '; parity/auth findings: ' . $findings . '; cleanup=' . (int) ( false === get_user_by( 'id', $user ) ) . "\n";
exit( $failures ? 1 : 0 );
