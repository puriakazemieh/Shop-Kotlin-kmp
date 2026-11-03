<?php
/** Disposable installed WordPress only. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/helpers.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-admin-content-controller.php';
global $wpdb;
cb_create_tables();
$failed = 0;
$check = static function ( bool $ok, string $label ) use ( &$failed ): void {
	if ( ! $ok ) { ++$failed; }
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n";
};
$id = wp_insert_post( array( 'post_type' => 'cb_therapist', 'post_status' => 'publish', 'post_title' => 'Synthetic admin slot therapist', 'post_name' => 'qa-admin-slots-' . bin2hex( random_bytes( 4 ) ) ), true );
if ( is_wp_error( $id ) || ! $id ) { exit( 2 ); }
$slot = gmdate( 'Y-m-d H:i:s', time() + 86400 );
update_post_meta( $id, 'cb_slots', $slot );
$request = new WP_REST_Request( 'GET', '/api/admin/therapists/' . $id . '/slots' );
$request->set_param( 'id', $id );
$controller = new CB_Admin_Content_Controller();
$needle = 'SELECT slot_time FROM ' . cb_bookings_table() . ' WHERE therapist_id = ' . (int) $id;
$inject = static function ( string $sql ) use ( $needle ): string {
	return strpos( $sql, $needle ) !== false ? 'SELECT slot_time FROM cb_synthetic_missing_bookings' : $sql;
};
try {
	$free = $controller->therapist_slots( $request );
	$check( $free->get_status() === 200 && count( $free->get_data() ) === 1 && $free->get_data()[0]['booked'] === false, 'healthy slot is free' );
	$wpdb->insert( cb_bookings_table(), array( 'therapist_id' => $id, 'slot_time' => $slot, 'user_id' => 1, 'created_at' => current_time( 'mysql', true ) ) );
	$booked = $controller->therapist_slots( $request );
	$check( $booked->get_status() === 200 && $booked->get_data()[0]['booked'] === true, 'healthy slot is booked' );
	$wpdb->delete( cb_bookings_table(), array( 'therapist_id' => $id ), array( '%d' ) );
	add_filter( 'query', $inject );
	$error = $controller->therapist_slots( $request );
	$check( $error->get_status() === 503 && ( $error->get_data()['errorCode'] ?? '' ) === 'AVAILABILITY_UNAVAILABLE', 'DB read failure fails closed' );
	remove_filter( 'query', $inject );
	$recovered = $controller->therapist_slots( $request );
	$check( $recovered->get_status() === 200 && $recovered->get_data()[0]['booked'] === false, 'normal read recovers' );
} finally {
	remove_filter( 'query', $inject );
	$wpdb->delete( cb_bookings_table(), array( 'therapist_id' => $id ), array( '%d' ) );
	wp_delete_post( $id, true );
}
echo "Failures: $failed.\n";
exit( $failed ? 1 : 0 );
