<?php
/** Disposable installed WordPress only. Requires temporary qa-admin-slots-mu.php and marker. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || ! file_exists( '/tmp/carmilla-qa-admin-slots-enabled' ) ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/helpers.php';
global $wpdb;
cb_create_tables();
$failed = 0;
$check = static function ( bool $ok, string $label ) use ( &$failed ): void {
	if ( ! $ok ) { ++$failed; }
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n";
};
$suffix = bin2hex( random_bytes( 5 ) );
$user = wp_create_user( 'qa_admin_slots_' . $suffix, wp_generate_password( 24 ), 'qa-admin-slots-' . $suffix . '@example.test' );
if ( ! is_int( $user ) ) { exit( 2 ); }
$admin = new WP_User( $user );
$admin->set_role( 'administrator' );
$password = WP_Application_Passwords::create_new_application_password( $user, array( 'name' => 'Synthetic QA slots' ) );
if ( is_wp_error( $password ) ) { wp_delete_user( $user ); exit( 2 ); }
$id = wp_insert_post( array( 'post_type' => 'cb_therapist', 'post_status' => 'publish', 'post_title' => 'Synthetic admin HTTP slot ' . $suffix ), true );
if ( is_wp_error( $id ) || ! $id ) { wp_delete_user( $user ); exit( 2 ); }
$slot = gmdate( 'Y-m-d H:i:s', time() + 86400 );
update_post_meta( $id, 'cb_slots', $slot );
$url = 'http://127.0.0.1/?rest_route=/' . CB_REST_NAMESPACE . '/api/admin/therapists/' . $id . '/slots';
$headers = array( 'Authorization' => 'Basic ' . base64_encode( $admin->user_login . ':' . $password[0] ) );
$fetch = static function ( bool $fail = false ) use ( $url, $headers ): array {
	$h = $headers;
	if ( $fail ) { $h['X-Carmilla-QA-DB-Failure'] = 'synthetic'; }
	$response = wp_remote_get( $url, array( 'headers' => $h, 'timeout' => 15 ) );
	if ( is_wp_error( $response ) ) { return array( 0, array( 'transportError' => $response->get_error_message() ) ); }
	$code = wp_remote_retrieve_response_code( $response );
	$body = json_decode( wp_remote_retrieve_body( $response ), true );
	echo 'HTTP status=' . $code . ' errorCode=' . ( is_array( $body ) ? ( $body['errorCode'] ?? $body['code'] ?? '' ) : 'non-json' ) . "\n";
	return array( $code, $body );
};
try {
	$unauthorized = wp_remote_get( $url, array( 'timeout' => 15 ) );
	$check( ! is_wp_error( $unauthorized ) && in_array( wp_remote_retrieve_response_code( $unauthorized ), array( 401, 403 ), true ), 'HTTP unauthenticated request denied' );
	list( $status, $body ) = $fetch();
	$check( $status === 200 && is_array( $body ) && count( $body ) === 1 && $body[0]['booked'] === false, 'HTTP admin free slot' );
	$wpdb->insert( cb_bookings_table(), array( 'therapist_id' => $id, 'slot_time' => $slot, 'user_id' => $user, 'created_at' => current_time( 'mysql', true ) ) );
	list( $status, $body ) = $fetch();
	$check( $status === 200 && is_array( $body ) && count( $body ) === 1 && $body[0]['booked'] === true, 'HTTP admin booked slot' );
	$wpdb->delete( cb_bookings_table(), array( 'therapist_id' => $id ), array( '%d' ) );
	list( $status, $body ) = $fetch( true );
	$check( $status === 503 && ( $body['errorCode'] ?? '' ) === 'AVAILABILITY_UNAVAILABLE', 'HTTP admin DB failure fails closed' );
	list( $status, $body ) = $fetch();
	$check( $status === 200 && is_array( $body ) && count( $body ) === 1 && $body[0]['booked'] === false, 'HTTP admin recovered slot' );
} finally {
	$wpdb->delete( cb_bookings_table(), array( 'therapist_id' => $id ), array( '%d' ) );
	wp_delete_post( $id, true );
	wp_delete_user( $user );
}
$remaining = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE ID=%d", $user ) )
	+ (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->posts} WHERE ID=%d", $id ) )
	+ (int) $wpdb->get_var( $wpdb->prepare( 'SELECT COUNT(*) FROM ' . cb_bookings_table() . ' WHERE therapist_id=%d', $id ) );
$check( $remaining === 0, 'synthetic HTTP fixture cleaned' );
echo "Failures: $failed.\n";
exit( $failed ? 1 : 0 );
