<?php
/** Disposable WordPress only. Two synthetic users, actual HTTP, no external services. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
$users = array();
$tokens = array();
$suffix = bin2hex( random_bytes( 4 ) );
foreach ( array( 'a', 'b' ) as $who ) {
	$id = wp_create_user( 'qa_alias_isolation_' . $who . '_' . $suffix, wp_generate_password( 24 ), 'qa-alias-isolation-' . $who . '-' . $suffix . '@example.test' );
	if ( ! is_int( $id ) ) { exit( 2 ); }
	( new WP_User( $id ) )->set_role( 'customer' );
	$users[ $who ] = $id;
	$tokens[ $who ] = CB_JWT::issue( get_user_by( 'id', $id ), 'access' );
}
$session = WP_Session_Tokens::get_instance( $users['b'] )->create( time() + 3600 );
$expires = time() + 3600;
$cookie = AUTH_COOKIE . '=' . wp_generate_auth_cookie( $users['b'], $expires, 'auth', $session ) . '; ' . LOGGED_IN_COOKIE . '=' . wp_generate_auth_cookie( $users['b'], $expires, 'logged_in', $session );
$address = static function ( $id, $name ) { return array( 'id' => $id, 'receiverName' => $name, 'country' => 'IR', 'city' => 'Test', 'default' => true ); };
update_user_meta( $users['a'], 'cb_addresses', array( $address( 101, 'Alpha' ) ) );
update_user_meta( $users['b'], 'cb_addresses', array( $address( 202, 'Beta' ) ) );
update_user_meta( $users['a'], 'cb_wishlist', array( 101 ) );
update_user_meta( $users['b'], 'cb_wishlist', array( 202, 203 ) );
update_user_meta( $users['a'], 'cb_recently_viewed', array( 101 ) );
update_user_meta( $users['b'], 'cb_recently_viewed', array( 202, 203 ) );
$meta = static function ( $who, $key ) use ( $users ) { wp_cache_delete( $users[ $who ], 'user_meta' ); return get_user_meta( $users[ $who ], $key, true ); };
$request = static function ( $route, $method, $path, $auth, $body = '' ) use ( $tokens, $cookie ) {
	$headers = array( 'Content-Type' => 'application/json' );
	if ( isset( $tokens[ $auth ] ) ) { $headers['Authorization'] = 'Bearer ' . $tokens[ $auth ]; }
	if ( 'b_cookie_no_nonce' === $auth ) { $headers['Cookie'] = $cookie; }
	$url = 'http://127.0.0.1' . ( 'root' === $route ? $path : '/wp-json/carmilla/v1' . $path );
	$response = wp_remote_request( $url, array( 'method' => $method, 'headers' => $headers, 'body' => $body, 'timeout' => 15, 'redirection' => 0 ) );
	return is_wp_error( $response ) ? array( 0, null, '' ) : array( wp_remote_retrieve_response_code( $response ), json_decode( wp_remote_retrieve_body( $response ), true ), wp_remote_retrieve_header( $response, 'x-carmilla-deprecated' ) );
};
$failed = 0;
$check = static function ( $ok, $name ) use ( &$failed ) { echo ( $ok ? 'PASS ' : 'FAIL ' ) . $name . "\n"; $failed += $ok ? 0 : 1; };
try {
	foreach ( array( 'root', 'canonical' ) as $route ) {
		$prefix = $route . '_';
		$a = $request( $route, 'GET', '/api/addresses', 'a' );
		$b = $request( $route, 'GET', '/api/addresses', 'b' );
		$check( $a[0] === 200 && $b[0] === 200 && ( $a[1][0]['id'] ?? null ) === 101 && ( $b[1][0]['id'] ?? null ) === 202, $prefix . 'address_lists_are_per_user' );
		$missing = $request( $route, 'GET', '/api/addresses/202', 'a' );
		$before_a = $meta( 'a', 'cb_addresses' ); $before_b = $meta( 'b', 'cb_addresses' );
		$rejected = $request( $route, 'PATCH', '/api/addresses/202', 'a', '{"city":"WrongUser"}' );
		$check( $missing[0] === 404 && $rejected[0] === 404 && $meta( 'a', 'cb_addresses' ) === $before_a && $meta( 'b', 'cb_addresses' ) === $before_b, $prefix . 'cross_user_address_read_write_denied' );
		foreach ( array( 'favorites' => 'cb_wishlist', 'recently-viewed' => 'cb_recently_viewed' ) as $path => $key ) {
			$a = $request( $route, 'GET', '/api/' . $path, 'a' );
			$b = $request( $route, 'GET', '/api/' . $path, 'b' );
			$check( $a[0] === 200 && $b[0] === 200 && ( $a[1]['totalElements'] ?? null ) === 1 && ( $b[1]['totalElements'] ?? null ) === 2, $prefix . $path . '_read_isolation' );
			$before_b = $meta( 'b', $key );
			$write = $request( $route, 'POST', '/api/' . $path . '/303', 'a' );
			$check( $write[0] === 200 && in_array( 303, (array) $meta( 'a', $key ), true ) && $meta( 'b', $key ) === $before_b, $prefix . $path . '_write_isolation' );
			update_user_meta( $users['a'], $key, array( 101 ) );
		}
		$before_a = $meta( 'a', 'cb_wishlist' ); $before_b = $meta( 'b', 'cb_wishlist' );
		$denied = $request( $route, 'POST', '/api/favorites/505', 'anonymous' );
		$check( $denied[0] === 401 && $meta( 'a', 'cb_wishlist' ) === $before_a && $meta( 'b', 'cb_wishlist' ) === $before_b, $prefix . 'anonymous_write_unchanged' );
		$before_a = $meta( 'a', 'cb_recently_viewed' ); $before_b = $meta( 'b', 'cb_recently_viewed' );
		$denied = $request( $route, 'POST', '/api/recently-viewed/606', 'b_cookie_no_nonce' );
		$check( $denied[0] === 401 && $meta( 'a', 'cb_recently_viewed' ) === $before_a && $meta( 'b', 'cb_recently_viewed' ) === $before_b, $prefix . 'cookie_without_nonce_write_unchanged' );
	}
	$unknown = $request( 'root', 'GET', '/api/unrecognized-private/7', 'a' );
	$check( in_array( $unknown[0], array( 301, 404 ), true ) && $unknown[2] === '', 'unknown_root_path_uses_wordpress_routing' );
} finally {
	WP_Session_Tokens::get_instance( $users['b'] )->destroy( $session );
	foreach ( $users as $id ) { wp_delete_user( $id ); }
}
$check( false === get_user_by( 'id', $users['a'] ) && false === get_user_by( 'id', $users['b'] ), 'synthetic_users_cleaned' );
echo 'Failures: ' . $failed . "\n";
exit( $failed ? 1 : 0 );
