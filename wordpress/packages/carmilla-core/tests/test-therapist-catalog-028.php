<?php
/**
 * Real WordPress Verification for P04-WORDPRESS-CODE-028
 * Shared Therapist Catalog & Availability Service.
 *
 * Run inside WordPress container:
 *   php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-therapist-catalog-028.php
 */

$wp_load = '/var/www/html/wp-load.php';
if ( ! file_exists( $wp_load ) ) {
	fwrite( STDERR, "wp-load.php not found\n" );
	exit( 1 );
}
require_once $wp_load;

use Carmilla\Core\Clinic\TherapistCatalogService;

$passed = 0;
$failed = 0;

function assert_028( bool $cond, string $label ): void {
	global $passed, $failed;
	if ( $cond ) {
		++$passed;
	} else {
		++$failed;
		fwrite( STDERR, "FAIL: {$label}\n" );
	}
}

$orig_features = get_option( 'cb_manifest_features', null );
// Enable clinic.booking WITHOUT psych.tests to prove independence!
update_option( 'cb_manifest_features', array(
	'theme.base'     => true,
	'clinic.booking' => true,
	'psych.tests'    => false,
) );
Carmilla_Kernel::resolve_features();

$created_ids = array();

try {
	$admin_id = username_exists( 'qa_clinic_admin' );
	if ( ! $admin_id ) {
		$admin_id = wp_create_user( 'qa_clinic_admin', 'Pass123456!', 'qa_clinic_admin@example.com' );
	}
	( new WP_User( $admin_id ) )->set_role( 'administrator' );

	$sub_id = username_exists( 'qa_clinic_sub' );
	if ( ! $sub_id ) {
		$sub_id = wp_create_user( 'qa_clinic_sub', 'Pass123456!', 'qa_clinic_sub@example.com' );
	}
	( new WP_User( $sub_id ) )->set_role( 'subscriber' );

	// 1. Invalid timezone rejected
	wp_set_current_user( $admin_id );
	$bad_tz = TherapistCatalogService::create_therapist( array(
		'name'     => 'متخصص نامعتبر',
		'timezone' => 'Mars/Olympus',
	) );
	assert_028( is_wp_error( $bad_tz ) && 'INVALID_TIMEZONE' === $bad_tz->get_error_code(), 'Invalid timezone rejected with INVALID_TIMEZONE' );

	// 2. Create therapist with Asia/Tehran timezone and 2 future slots
	$future_1 = gmdate( 'Y-m-d\T10:00', strtotime( '+3 days' ) );
	$future_2 = gmdate( 'Y-m-d\T14:30', strtotime( '+4 days' ) );
	$t = TherapistCatalogService::create_therapist( array(
		'name'                   => 'دکتر آزمایشی ۰۲۸',
		'slug'                   => 'qa-therapist-028',
		'specialty'              => 'روان‌شناسی بالینی',
		'timezone'               => 'Asia/Tehran',
		'sessionPrice'           => 400000,
		'sessionDurationMinutes' => 50,
		'slots'                  => array( $future_1, $future_2 ),
	) );
	assert_028( is_array( $t ) && ! empty( $t['id'] ), 'Therapist created via TherapistCatalogService' );
	$tid = (int) $t['id'];
	$created_ids[] = $tid;
	assert_028( 'Asia/Tehran' === $t['timezone'] && count( $t['slots'] ) === 2, 'Therapist timezone Asia/Tehran and 2 slots persisted' );

	// 3. Invalid/past slot rejected
	$bad_slot = TherapistCatalogService::add_slot( $tid, array( 'slotTime' => '2020-01-01T10:00' ) );
	assert_028( is_wp_error( $bad_slot ) && 'INVALID_SLOT_TIME' === $bad_slot->get_error_code(), 'Past/invalid slot rejected with INVALID_SLOT_TIME' );

	// 4. REST routes singular & clinic.booking works while psych.tests is false
	$server = rest_get_server();
	$routes = $server->get_routes();
	assert_028( ! empty( $routes['/carmilla/v1/api/therapists'] ), 'REST /api/therapists registered' );
	assert_028( count( $routes['/carmilla/v1/api/therapists'] ) === 1, 'No duplicate /api/therapists handler' );

	wp_set_current_user( 0 );
	$res_pub = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/therapists/qa-therapist-028' ) );
	$pub_data = $res_pub->get_data();
	assert_028( 200 === $res_pub->get_status() && (int) $pub_data['id'] === $tid && ! isset( $pub_data['patientNotes'] ), 'Public GET /api/therapists/{slug} works without psych.tests and exposes no private clinical data' );

	// 5. Subscriber blocked from admin therapist edit
	wp_set_current_user( $sub_id );
	$req_unauth = new WP_REST_Request( 'POST', '/carmilla/v1/api/admin/therapists' );
	$req_unauth->set_param( 'name', 'غیرمجاز' );
	$res_unauth = $server->dispatch( $req_unauth );
	assert_028( 403 === $res_unauth->get_status(), 'Subscriber blocked (403) on POST /api/admin/therapists' );

	// 6. Shortcodes render canonical ID and timezone
	$sc_list = do_shortcode( '[carmilla_therapists]' );
	$sc_det  = do_shortcode( '[carmilla_therapist id="' . $tid . '"]' );
	assert_028( false !== strpos( $sc_list, 'data-therapist-id="' . $tid . '"' ), '[carmilla_therapists] renders canonical ID' );
	assert_028( false !== strpos( $sc_det, 'data-timezone="Asia/Tehran"' ), '[carmilla_therapist] renders Asia/Tehran timezone' );

	// 7. Disabled clinic.booking closes routes
	update_option( 'cb_manifest_features', array(
		'theme.base'     => true,
		'clinic.booking' => false,
	) );
	Carmilla_Kernel::resolve_features();
	$res_gated = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/therapists' ) );
	assert_028( 403 === $res_gated->get_status(), 'Disabled clinic.booking returns 403 on /api/therapists' );
} finally {
	foreach ( $created_ids as $tid ) {
		TherapistCatalogService::delete_therapist( $tid );
	}
	if ( null === $orig_features ) {
		delete_option( 'cb_manifest_features' );
	} else {
		update_option( 'cb_manifest_features', $orig_features );
	}
	wp_set_current_user( 0 );
}

echo "Therapist Catalog 028 QA: {$passed} passed, {$failed} failed.\n";
exit( $failed > 0 ? 1 : 0 );
