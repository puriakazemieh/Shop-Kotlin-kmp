<?php
/** Disposable installed WordPress only. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/helpers.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-clinic-controller.php';

global $wpdb;
cb_create_tables();
$failed = 0;
$check = static function ( bool $ok, string $label ) use ( &$failed ): void {
	if ( ! $ok ) { ++$failed; }
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n";
};
$id = wp_insert_post( array( 'post_type' => 'cb_therapist', 'post_status' => 'publish', 'post_title' => 'Synthetic availability therapist', 'post_name' => 'qa-availability-' . bin2hex( random_bytes( 4 ) ) ), true );
if ( is_wp_error( $id ) || ! $id ) { exit( 2 ); }
$slot = gmdate( 'Y-m-d H:i:s', time() + 86400 );
update_post_meta( $id, 'cb_slots', $slot );
update_post_meta( $id, 'cb_specialty', 'synthetic-anxiety' );
$controller = new CB_Clinic_Controller();
$detail_request = new WP_REST_Request( 'GET', '/api/therapists/' . get_post( $id )->post_name );
$detail_request->set_param( 'slug', get_post( $id )->post_name );
$match_request = new WP_REST_Request( 'POST', '/api/clinic/therapist-match/submit' );
$match_request->set_body( wp_json_encode( array( 'selectedTags' => array( 'synthetic-anxiety' ) ) ) );
$match_request->set_header( 'content-type', 'application/json' );
$needle = 'SELECT slot_time FROM ' . cb_bookings_table() . ' WHERE therapist_id = ' . (int) $id;
$inject = static function ( string $sql ) use ( $needle ): string {
	return strpos( $sql, $needle ) !== false ? 'SELECT slot_time FROM cb_synthetic_missing_bookings' : $sql;
};

try {
	$healthy = $controller->therapist_detail( $detail_request );
	$check( $healthy->get_status() === 200 && count( $healthy->get_data()['slots'] ) === 1, 'healthy detail advertises one free slot' );
	$list = $controller->list_therapists();
	$listed = array_values( array_filter( $list->get_data(), static function ( $row ) use ( $id ) { return $row['id'] === $id; } ) );
	$check( $list->get_status() === 200 && count( $listed ) === 1 && $listed[0]['availableSlotCount'] === 1, 'healthy list reports one free slot' );
	$match = $controller->submit_match( $match_request );
	$matches = array_values( array_filter( $match->get_data(), static function ( $row ) use ( $id ) { return $row['therapist']['id'] === $id; } ) );
	$check( $match->get_status() === 200 && count( $matches ) === 1 && $matches[0]['therapist']['availableSlotCount'] === 1, 'healthy match reports one free slot' );
	$wpdb->insert( cb_bookings_table(), array( 'therapist_id' => $id, 'slot_time' => $slot, 'user_id' => 1, 'created_at' => current_time( 'mysql', true ) ) );
	$booked = $controller->therapist_detail( $detail_request );
	$check( $booked->get_status() === 200 && count( $booked->get_data()['slots'] ) === 0, 'healthy detail hides booked slot' );
	$wpdb->delete( cb_bookings_table(), array( 'therapist_id' => $id ), array( '%d' ) );
	add_filter( 'query', $inject );
	foreach ( array( 'detail' => $controller->therapist_detail( $detail_request ), 'list' => $controller->list_therapists(), 'match' => $controller->submit_match( $match_request ) ) as $name => $response ) {
		$data = $response->get_data();
		$check( $response->get_status() === 503 && ( $data['errorCode'] ?? '' ) === 'AVAILABILITY_UNAVAILABLE', $name . ' fails closed on bookings read error' );
	}
	remove_filter( 'query', $inject );
	$recovered = $controller->therapist_detail( $detail_request );
	$check( $recovered->get_status() === 200 && count( $recovered->get_data()['slots'] ) === 1, 'healthy availability recovers without stale result' );
} finally {
	remove_filter( 'query', $inject );
	$wpdb->delete( cb_bookings_table(), array( 'therapist_id' => $id ), array( '%d' ) );
	wp_delete_post( $id, true );
}
echo "Failures: {$failed}.\n";
exit( $failed ? 1 : 0 );
