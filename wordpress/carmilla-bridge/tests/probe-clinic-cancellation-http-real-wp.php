<?php
/**
 * Disposable installed WordPress only.
 * Verifies public clinic appointment cancellation REST endpoint over real HTTP via Apache.
 * Tests:
 * 1. Unauthenticated request is rejected (401 or 403).
 * 2. Cross-patient cancellation attempt is rejected with 404 NOT_FOUND without state change.
 * 3. Injected database failure during slot release fails closed (HTTP 503 CANCELLATION_UNAVAILABLE)
 *    with rollback keeping cb_status as PENDING and slot lock intact.
 * 4. Healthy cancellation releases the slot lock and updates cb_status to CANCELLED (HTTP 200).
 * 5. Repeat cancellation is idempotent (HTTP 200).
 * 6. Fixtures are completely cleaned up.
 */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) {
	fwrite( STDERR, "Disposable flag required.\n" );
	exit( 2 );
}

$mu_dest = '/var/www/html/wp-content/mu-plugins/qa-clinic-cancel-mu.php';
$mu_src = '/var/www/html/wp-content/plugins/carmilla-bridge/tests/qa-clinic-cancel-mu.php';
$marker = '/tmp/carmilla-qa-clinic-cancel-enabled';
$installed_mu = false;
$created_marker = false;

if ( ! file_exists( $marker ) ) {
	touch( $marker );
	$created_marker = true;
}
if ( ! file_exists( $mu_dest ) && file_exists( $mu_src ) ) {
	if ( ! is_dir( dirname( $mu_dest ) ) ) {
		mkdir( dirname( $mu_dest ), 0755, true );
	}
	copy( $mu_src, $mu_dest );
	$installed_mu = true;
}

require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/helpers.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-jwt.php';

global $wpdb;
cb_create_tables();

$failed = 0;
$check = static function ( bool $ok, string $label ) use ( &$failed ): void {
	if ( ! $ok ) { ++$failed; }
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n";
};

$suffix = bin2hex( random_bytes( 5 ) );

// 1. Create Patient User A
$user_a = wp_create_user( 'qa_cancel_patient_' . $suffix, wp_generate_password( 24 ), 'qa-patient-' . $suffix . '@example.test' );
if ( ! is_int( $user_a ) ) {
	fwrite( STDERR, "Failed to create patient user.\n" );
	exit( 2 );
}
$user_a_obj = new WP_User( $user_a );
$user_a_obj->set_role( 'customer' );
$token_a = CB_JWT::issue( $user_a_obj, 'access' );

// 2. Create Patient User B (for cross-patient test)
$user_b = wp_create_user( 'qa_cancel_other_' . $suffix, wp_generate_password( 24 ), 'qa-other-' . $suffix . '@example.test' );
if ( ! is_int( $user_b ) ) {
	wp_delete_user( $user_a );
	fwrite( STDERR, "Failed to create other patient user.\n" );
	exit( 2 );
}
$user_b_obj = new WP_User( $user_b );
$user_b_obj->set_role( 'customer' );
$token_b = CB_JWT::issue( $user_b_obj, 'access' );

// 3. Create Therapist and Appointment for User A
$therapist_id = wp_insert_post( array(
	'post_type'   => 'cb_therapist',
	'post_status' => 'publish',
	'post_title'  => 'Synthetic Cancel Therapist ' . $suffix,
), true );

$appointment_id = wp_insert_post( array(
	'post_type'   => 'cb_appointment',
	'post_status' => 'publish',
	'post_title'  => 'Synthetic Cancel Appointment ' . $suffix,
	'post_author' => $user_a,
), true );

if ( ! is_int( $therapist_id ) || ! is_int( $appointment_id ) ) {
	wp_delete_user( $user_a );
	wp_delete_user( $user_b );
	fwrite( STDERR, "Failed to create therapist or appointment fixture.\n" );
	exit( 2 );
}

$slot = gmdate( 'Y-m-d H:i:s', time() + 86400 );
update_post_meta( $appointment_id, 'cb_user_id', $user_a );
update_post_meta( $appointment_id, 'cb_therapist_id', $therapist_id );
update_post_meta( $appointment_id, 'cb_slot', $slot );
update_post_meta( $appointment_id, 'cb_status', 'PENDING' );

$table = cb_bookings_table();
$wpdb->insert( $table, array(
	'therapist_id'   => $therapist_id,
	'slot_time'      => $slot,
	'appointment_id' => $appointment_id,
	'user_id'        => $user_a,
	'created_at'     => current_time( 'mysql', true ),
) );

$status_db = static function () use ( $wpdb, $appointment_id ): string {
	wp_cache_delete( $appointment_id, 'post_meta' );
	return (string) $wpdb->get_var( $wpdb->prepare(
		"SELECT meta_value FROM {$wpdb->postmeta} WHERE post_id=%d AND meta_key='cb_status' ORDER BY meta_id DESC LIMIT 1",
		$appointment_id
	) );
};

$lock_count = static function () use ( $wpdb, $table, $appointment_id ): int {
	return (int) $wpdb->get_var( $wpdb->prepare(
		"SELECT COUNT(*) FROM $table WHERE appointment_id=%d",
		$appointment_id
	) );
};

$base_url = 'http://127.0.0.1/?rest_route=/' . CB_REST_NAMESPACE;

$http_post = static function ( string $path, ?string $token = null, bool $inject_fault = false ) use ( $base_url ): array {
	$headers = array( 'Content-Type' => 'application/json' );
	if ( $token !== null ) {
		$headers['Authorization'] = 'Bearer ' . $token;
	}
	if ( $inject_fault ) {
		$headers['X-Carmilla-QA-DB-Failure'] = 'synthetic';
	}
	$response = wp_remote_post( $base_url . $path, array(
		'headers' => $headers,
		'timeout' => 15,
	) );
	if ( is_wp_error( $response ) ) {
		return array( 0, array( 'error' => $response->get_error_message() ) );
	}
	$code = wp_remote_retrieve_response_code( $response );
	$body = json_decode( wp_remote_retrieve_body( $response ), true );
	return array( $code, $body );
};

$cancel_path = '/api/clinic/appointments/' . $appointment_id . '/cancel';

try {
	// 1. Unauthenticated request denied
	list( $status_unauth, $body_unauth ) = $http_post( $cancel_path, null );
	$check(
		in_array( $status_unauth, array( 401, 403 ), true ) && $status_db() === 'PENDING' && $lock_count() === 1,
		'HTTP unauthenticated request is denied (401/403) and state preserved'
	);

	// 2. Cross-patient cancellation denied
	list( $status_cross, $body_cross ) = $http_post( $cancel_path, $token_b );
	$check(
		$status_cross === 404 && ( $body_cross['errorCode'] ?? $body_cross['code'] ?? '' ) === 'NOT_FOUND' && $status_db() === 'PENDING' && $lock_count() === 1,
		'HTTP cross-patient cancellation is rejected (404 NOT_FOUND) and state preserved'
	);

	// 3. Injected database failure during slot release fails closed
	list( $status_fault, $body_fault ) = $http_post( $cancel_path, $token_a, true );
	$check(
		$status_fault === 503 && ( $body_fault['errorCode'] ?? $body_fault['code'] ?? '' ) === 'CANCELLATION_UNAVAILABLE' && $status_db() === 'PENDING' && $lock_count() === 1,
		'HTTP slot-delete failure fails closed (HTTP 503 CANCELLATION_UNAVAILABLE) with rollback preserving PENDING status and lock'
	);

	// 4. Healthy cancellation updates status and releases slot lock
	list( $status_ok, $body_ok ) = $http_post( $cancel_path, $token_a, false );
	$check(
		$status_ok === 200 && $status_db() === 'CANCELLED' && $lock_count() === 0,
		'HTTP healthy cancellation succeeds (HTTP 200), updates status to CANCELLED and releases lock'
	);

	// 5. Repeat cancellation is idempotent
	list( $status_repeat, $body_repeat ) = $http_post( $cancel_path, $token_a, false );
	$check(
		$status_repeat === 200 && $status_db() === 'CANCELLED' && $lock_count() === 0,
		'HTTP repeat cancellation is idempotent (HTTP 200)'
	);

} finally {
	$wpdb->delete( $table, array( 'appointment_id' => $appointment_id ), array( '%d' ) );
	$wpdb->delete( $table, array( 'therapist_id' => $therapist_id ), array( '%d' ) );
	wp_delete_post( $appointment_id, true );
	wp_delete_post( $therapist_id, true );
	wp_delete_user( $user_a );
	wp_delete_user( $user_b );

	if ( $created_marker && file_exists( $marker ) ) {
		unlink( $marker );
	}
	if ( $installed_mu && file_exists( $mu_dest ) ) {
		unlink( $mu_dest );
	}
}

$cleanup_posts = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->posts} WHERE ID IN (%d,%d)", $appointment_id, $therapist_id ) );
$cleanup_locks = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM $table WHERE appointment_id=%d OR therapist_id=%d", $appointment_id, $therapist_id ) );
$cleanup_users = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE ID IN (%d,%d)", $user_a, $user_b ) );

$check(
	$cleanup_posts === 0 && $cleanup_locks === 0 && $cleanup_users === 0,
	'Synthetic fixtures cleaned up completely'
);

echo "Failures: $failed.\n";
exit( $failed ? 1 : 0 );
