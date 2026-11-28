<?php
/**
 * Real WordPress Verification for P04-WORDPRESS-CODE-028A
 * Shared Appointment Booking, Cancellation, Receipts & Admin Management.
 *
 * Run inside WordPress container:
 *   php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-appointment-booking-028a.php
 */

$wp_load = '/var/www/html/wp-load.php';
if ( ! file_exists( $wp_load ) ) {
	fwrite( STDERR, "wp-load.php not found\n" );
	exit( 1 );
}
require_once $wp_load;

use Carmilla\Core\Clinic\AppointmentBookingService;
use Carmilla\Core\Clinic\TherapistCatalogService;

$passed = 0;
$failed = 0;

function assert_028a( bool $cond, string $label ): void {
	global $passed, $failed;
	if ( $cond ) {
		++$passed;
	} else {
		++$failed;
		fwrite( STDERR, "FAIL: {$label}\n" );
	}
}

AppointmentBookingService::ensure_bookings_table();
$orig_features = get_option( 'cb_manifest_features', null );
update_option( 'cb_manifest_features', array(
	'theme.base'     => true,
	'clinic.booking' => true,
	'psych.tests'    => false,
) );
Carmilla_Kernel::resolve_features();

$created_therapists   = array();
$created_appointments = array();
global $wpdb;
$table = AppointmentBookingService::bookings_table();

try {
	$admin_id = username_exists( 'qa_appt_admin' );
	if ( ! $admin_id ) {
		$admin_id = wp_create_user( 'qa_appt_admin', 'Pass123456!', 'qa_appt_admin@example.com' );
	}
	( new WP_User( $admin_id ) )->set_role( 'administrator' );

	$patient_id = username_exists( 'qa_appt_patient' );
	if ( ! $patient_id ) {
		$patient_id = wp_create_user( 'qa_appt_patient', 'Pass123456!', 'qa_appt_patient@example.com' );
	}
	( new WP_User( $patient_id ) )->set_role( 'subscriber' );

	$other_id = username_exists( 'qa_appt_other' );
	if ( ! $other_id ) {
		$other_id = wp_create_user( 'qa_appt_other', 'Pass123456!', 'qa_appt_other@example.com' );
	}
	( new WP_User( $other_id ) )->set_role( 'subscriber' );

	// 1. Create therapist with 2 future slots
	wp_set_current_user( $admin_id );
	$future_1 = gmdate( 'Y-m-d\T10:00', strtotime( '+5 days' ) );
	$future_2 = gmdate( 'Y-m-d\T15:00', strtotime( '+6 days' ) );
	$t        = TherapistCatalogService::create_therapist( array(
		'name'                   => 'دکتر نوبت ۰۲۸ الف',
		'slug'                   => 'qa-therapist-028a',
		'specialty'              => 'مشاوره خانواده',
		'timezone'               => 'Asia/Tehran',
		'sessionPrice'           => 350000,
		'sessionDurationMinutes' => 45,
		'slots'                  => array( $future_1, $future_2 ),
	) );
	assert_028a( is_array( $t ) && ! empty( $t['id'] ), 'Therapist created for 028A' );
	$tid                  = (int) $t['id'];
	$created_therapists[] = $tid;
	$slot_id_1            = (int) $t['slots'][0]['id'];

	// 2. REST routes singular registration check
	$server = rest_get_server();
	$routes = $server->get_routes();
	assert_028a( ! empty( $routes['/carmilla/v1/api/clinic/appointments'] ) && count( $routes['/carmilla/v1/api/clinic/appointments'] ) === 1, 'Canonical /api/clinic/appointments registered once' );
	assert_028a( ! empty( $routes['/carmilla/v1/appointments'] ) && count( $routes['/carmilla/v1/appointments'] ) === 2, 'Legacy /appointments alias registered once (GET+POST)' );
	assert_028a( ! empty( $routes['/carmilla/v1/api/admin/therapists/appointments'] ), 'Admin /api/admin/therapists/appointments registered' );

	// 3. Unauthenticated booking rejected
	wp_set_current_user( 0 );
	$req_anon = new WP_REST_Request( 'POST', '/carmilla/v1/api/clinic/appointments' );
	$req_anon->set_param( 'slotId', $slot_id_1 );
	$res_anon = $server->dispatch( $req_anon );
	assert_028a( in_array( $res_anon->get_status(), array( 401, 403 ), true ), 'Anonymous booking rejected' );

	// 4. Patient books slot 1 via canonical { slotId }
	wp_set_current_user( $patient_id );
	$req_book1 = new WP_REST_Request( 'POST', '/carmilla/v1/api/clinic/appointments' );
	$req_book1->set_header( 'content-type', 'application/json' );
	$req_book1->set_body( wp_json_encode( array( 'slotId' => $slot_id_1, 'notes' => 'یادداشت آزمایشی' ) ) );
	$res_book1 = $server->dispatch( $req_book1 );
	$b1_data   = $res_book1->get_data();
	assert_028a( 201 === $res_book1->get_status() && ! empty( $b1_data['id'] ), 'Patient booked slot 1 via canonical POST /api/clinic/appointments' );
	$appt_1_id              = (int) ( $b1_data['id'] ?? 0 );
	$created_appointments[] = $appt_1_id;

	// 5. Double-booking same slot rejected with 409 SLOT_TAKEN
	wp_set_current_user( $other_id );
	$req_dup = new WP_REST_Request( 'POST', '/carmilla/v1/api/clinic/appointments' );
	$req_dup->set_header( 'content-type', 'application/json' );
	$req_dup->set_body( wp_json_encode( array( 'slotId' => $slot_id_1 ) ) );
	$res_dup = $server->dispatch( $req_dup );
	assert_028a( 409 === $res_dup->get_status() && 'SLOT_TAKEN' === ( $res_dup->get_data()['errorCode'] ?? '' ), 'Double booking rejected with 409 SLOT_TAKEN' );

	// 6. Legacy booking.js payload { therapistId, slot } via /appointments alias
	$req_legacy = new WP_REST_Request( 'POST', '/carmilla/v1/appointments' );
	$req_legacy->set_header( 'content-type', 'application/json' );
	$req_legacy->set_body( wp_json_encode( array( 'therapistId' => $tid, 'slot' => $future_2 ) ) );
	$res_legacy = $server->dispatch( $req_legacy );
	$b2_data    = $res_legacy->get_data();
	assert_028a( 201 === $res_legacy->get_status() && ! empty( $b2_data['id'] ), 'Legacy booking.js payload {therapistId, slot} works via /appointments alias' );
	$appt_2_id              = (int) ( $b2_data['id'] ?? 0 );
	$created_appointments[] = $appt_2_id;

	// 7. Ownership: other user cannot view receipt or cancel patient_id's appointment
	$res_other_receipt = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/clinic/appointments/' . $appt_1_id . '/receipt' ) );
	assert_028a( 404 === $res_other_receipt->get_status(), 'Other user cannot view receipt (404)' );
	$res_other_cancel = $server->dispatch( new WP_REST_Request( 'POST', '/carmilla/v1/api/clinic/appointments/' . $appt_1_id . '/cancel' ) );
	assert_028a( 404 === $res_other_cancel->get_status(), 'Other user cannot cancel appointment (404)' );

	// 8. Owner views my-appointments & receipt
	wp_set_current_user( $patient_id );
	$res_mine = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/clinic/my-appointments' ) );
	$mine     = $res_mine->get_data();
	assert_028a( 200 === $res_mine->get_status() && is_array( $mine ) && count( $mine ) >= 1 && (int) $mine[0]['id'] === $appt_1_id, 'Owner sees appointment in my-appointments' );

	$res_receipt = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/clinic/appointments/' . $appt_1_id . '/receipt' ) );
	assert_028a( 200 === $res_receipt->get_status() && (int) ( $res_receipt->get_data()['appointmentId'] ?? 0 ) === $appt_1_id, 'Owner retrieves receipt (200)' );

	// 9. Shortcodes [carmilla_booking] & [carmilla_my_appointments]
	$html_booking = do_shortcode( '[carmilla_booking therapist_id="' . $tid . '"]' );
	$html_my      = do_shortcode( '[carmilla_my_appointments]' );
	assert_028a( strpos( $html_booking, 'data-carmilla-surface="booking-form"' ) !== false, 'Booking shortcode renders form' );
	assert_028a( strpos( $html_my, 'data-carmilla-surface="my-appointments"' ) !== false && strpos( $html_my, (string) $appt_1_id ) !== false, 'My-appointments shortcode renders user appointment' );

	// 10. Admin list, confirm & complete
	wp_set_current_user( $admin_id );
	$res_adm = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/admin/therapists/appointments' ) );
	assert_028a( 200 === $res_adm->get_status() && is_array( $res_adm->get_data() ), 'Admin lists appointments' );

	$req_conf = new WP_REST_Request( 'POST', '/carmilla/v1/api/admin/therapists/appointments/' . $appt_1_id . '/confirm' );
	$req_conf->set_header( 'content-type', 'application/json' );
	$req_conf->set_body( wp_json_encode( array( 'videoRoomUrl' => 'https://meet.example.test/room-028a' ) ) );
	$res_conf = $server->dispatch( $req_conf );
	assert_028a( 200 === $res_conf->get_status() && 'CONFIRMED' === get_post_meta( $appt_1_id, 'cb_status', true ), 'Admin confirms appointment' );

	$res_comp = $server->dispatch( new WP_REST_Request( 'POST', '/carmilla/v1/api/admin/therapists/appointments/' . $appt_1_id . '/complete' ) );
	assert_028a( 200 === $res_comp->get_status() && 'COMPLETED' === get_post_meta( $appt_1_id, 'cb_status', true ), 'Admin completes appointment' );

	// 11. Owner cancels appt_2 and lock is released
	wp_set_current_user( $other_id );
	$res_cancel = $server->dispatch( new WP_REST_Request( 'POST', '/carmilla/v1/api/clinic/appointments/' . $appt_2_id . '/cancel' ) );
	$lock_left  = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM $table WHERE appointment_id=%d", $appt_2_id ) );
	assert_028a( 200 === $res_cancel->get_status() && 'CANCELLED' === get_post_meta( $appt_2_id, 'cb_status', true ) && 0 === $lock_left, 'Cancellation updates status to CANCELLED and releases slot lock' );

	// 12. Feature disabled gate
	update_option( 'cb_manifest_features', array(
		'theme.base'     => true,
		'clinic.booking' => false,
	) );
	Carmilla_Kernel::resolve_features();
	$res_dis = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/clinic/my-appointments' ) );
	$dis_data = $res_dis->get_data();
	$dis_code = $dis_data['errorCode'] ?? $dis_data['code'] ?? '';
	assert_028a( 403 === $res_dis->get_status() && 'FEATURE_DISABLED' === $dis_code, 'Disabled clinic.booking returns 403 FEATURE_DISABLED on REST' );
	$html_dis = do_shortcode( '[carmilla_booking therapist_id="' . $tid . '"]' );
	assert_028a( strpos( $html_dis, 'data-carmilla-surface="clinic-disabled"' ) !== false, 'Disabled clinic.booking renders disabled notice on shortcode' );
} finally {
	if ( null === $orig_features ) {
		delete_option( 'cb_manifest_features' );
	} else {
		update_option( 'cb_manifest_features', $orig_features );
	}
	Carmilla_Kernel::resolve_features();

	foreach ( $created_therapists as $tid ) {
		$wpdb->delete( $table, array( 'therapist_id' => $tid ), array( '%d' ) );
		wp_delete_post( $tid, true );
	}
	foreach ( $created_appointments as $aid ) {
		if ( $aid > 0 ) {
			wp_delete_post( $aid, true );
		}
	}
	wp_set_current_user( 0 );
}

echo "P04-WORDPRESS-CODE-028A: {$passed} passed, {$failed} failed.\n";
exit( $failed > 0 ? 1 : 0 );
