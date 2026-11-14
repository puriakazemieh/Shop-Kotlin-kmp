<?php
/**
 * Disposable installed WordPress only.
 * Verifies public clinic availability REST endpoints over real HTTP via Apache.
 * Tests:
 * 1. Healthy availability on list, detail, and match endpoints.
 * 2. Booked slot is hidden from public availability.
 * 3. Fail-closed on DB read failure (HTTP 503 AVAILABILITY_UNAVAILABLE).
 * 4. Recovery after fault removal.
 */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) {
	fwrite( STDERR, "Disposable flag required.\n" );
	exit( 2 );
}

$mu_dest = '/var/www/html/wp-content/mu-plugins/qa-admin-slots-mu.php';
$mu_src = '/var/www/html/wp-content/plugins/carmilla-bridge/tests/qa-admin-slots-mu.php';
$marker = '/tmp/carmilla-qa-admin-slots-enabled';
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
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/helpers.php';

global $wpdb;
cb_create_tables();

$failed = 0;
$check = static function ( bool $ok, string $label ) use ( &$failed ): void {
	if ( ! $ok ) { ++$failed; }
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n";
};

$suffix = bin2hex( random_bytes( 5 ) );
$id = wp_insert_post( array(
	'post_type'   => 'cb_therapist',
	'post_status' => 'publish',
	'post_title'  => 'Synthetic HTTP availability therapist ' . $suffix,
	'post_name'   => 'qa-avail-http-' . $suffix,
), true );

if ( is_wp_error( $id ) || ! $id ) {
	fwrite( STDERR, "Failed to create therapist fixture.\n" );
	exit( 2 );
}

$slug = get_post( $id )->post_name;
$slot = gmdate( 'Y-m-d H:i:s', time() + 86400 );
update_post_meta( $id, 'cb_slots', $slot );
update_post_meta( $id, 'cb_specialty', 'synthetic-anxiety' );

$base_url = 'http://127.0.0.1/?rest_route=/' . CB_REST_NAMESPACE;

$http_get = static function ( string $path, bool $inject_fault = false ) use ( $base_url ): array {
	$headers = array( 'Content-Type' => 'application/json' );
	if ( $inject_fault ) {
		$headers['X-Carmilla-QA-DB-Failure'] = 'synthetic';
	}
	$response = wp_remote_get( $base_url . $path, array( 'headers' => $headers, 'timeout' => 15 ) );
	if ( is_wp_error( $response ) ) {
		return array( 0, array( 'error' => $response->get_error_message() ) );
	}
	$code = wp_remote_retrieve_response_code( $response );
	$body = json_decode( wp_remote_retrieve_body( $response ), true );
	return array( $code, $body );
};

$http_post = static function ( string $path, array $payload, bool $inject_fault = false ) use ( $base_url ): array {
	$headers = array( 'Content-Type' => 'application/json' );
	if ( $inject_fault ) {
		$headers['X-Carmilla-QA-DB-Failure'] = 'synthetic';
	}
	$response = wp_remote_post( $base_url . $path, array(
		'headers' => $headers,
		'body'    => wp_json_encode( $payload ),
		'timeout' => 15,
	) );
	if ( is_wp_error( $response ) ) {
		return array( 0, array( 'error' => $response->get_error_message() ) );
	}
	$code = wp_remote_retrieve_response_code( $response );
	$body = json_decode( wp_remote_retrieve_body( $response ), true );
	return array( $code, $body );
};

try {
	// 1. Healthy HTTP availability reads
	list( $status_list, $body_list ) = $http_get( '/api/therapists' );
	$matching_in_list = array_values( array_filter( (array) $body_list, static function ( $t ) use ( $id ) {
		return ( $t['id'] ?? 0 ) === $id;
	} ) );
	$check(
		$status_list === 200 && count( $matching_in_list ) === 1 && ( $matching_in_list[0]['availableSlotCount'] ?? 0 ) === 1,
		'HTTP GET /api/therapists reports free slot for therapist'
	);

	list( $status_detail, $body_detail ) = $http_get( '/api/therapists/' . $slug );
	$check(
		$status_detail === 200 && is_array( $body_detail ) && count( $body_detail['slots'] ?? array() ) === 1,
		'HTTP GET /api/therapists/{slug} returns available slot in slots list'
	);

	list( $status_match, $body_match ) = $http_post( '/api/clinic/therapist-match/submit', array(
		'selectedTags' => array( 'synthetic-anxiety' ),
	) );
	$matching_in_match = array_values( array_filter( (array) $body_match, static function ( $m ) use ( $id ) {
		return ( $m['therapist']['id'] ?? 0 ) === $id;
	} ) );
	$check(
		$status_match === 200 && count( $matching_in_match ) === 1 && ( $matching_in_match[0]['therapist']['availableSlotCount'] ?? 0 ) === 1,
		'HTTP POST /api/clinic/therapist-match/submit returns therapist with free slot'
	);

	// 2. Booked slot is hidden from public availability
	$wpdb->insert( cb_bookings_table(), array(
		'therapist_id' => $id,
		'slot_time'    => $slot,
		'user_id'      => 1,
		'created_at'   => current_time( 'mysql', true ),
	) );

	list( $status_booked, $body_booked ) = $http_get( '/api/therapists/' . $slug );
	$check(
		$status_booked === 200 && is_array( $body_booked ) && count( $body_booked['slots'] ?? array() ) === 0,
		'HTTP GET /api/therapists/{slug} hides booked slot (count = 0)'
	);

	$wpdb->delete( cb_bookings_table(), array( 'therapist_id' => $id ), array( '%d' ) );

	// 3. Inject DB failure: all endpoints MUST fail closed with HTTP 503 AVAILABILITY_UNAVAILABLE
	list( $status_fail_list, $body_fail_list ) = $http_get( '/api/therapists', true );
	$check(
		$status_fail_list === 503 && ( $body_fail_list['errorCode'] ?? '' ) === 'AVAILABILITY_UNAVAILABLE',
		'HTTP GET /api/therapists fails closed on DB error (HTTP 503 AVAILABILITY_UNAVAILABLE)'
	);

	list( $status_fail_detail, $body_fail_detail ) = $http_get( '/api/therapists/' . $slug, true );
	$check(
		$status_fail_detail === 503 && ( $body_fail_detail['errorCode'] ?? '' ) === 'AVAILABILITY_UNAVAILABLE',
		'HTTP GET /api/therapists/{slug} fails closed on DB error (HTTP 503 AVAILABILITY_UNAVAILABLE)'
	);

	list( $status_fail_match, $body_fail_match ) = $http_post( '/api/clinic/therapist-match/submit', array(
		'selectedTags' => array( 'synthetic-anxiety' ),
	), true );
	$check(
		$status_fail_match === 503 && ( $body_fail_match['errorCode'] ?? '' ) === 'AVAILABILITY_UNAVAILABLE',
		'HTTP POST /api/clinic/therapist-match/submit fails closed on DB error (HTTP 503 AVAILABILITY_UNAVAILABLE)'
	);

	// 4. Recovery: after removing fault, normal availability restores
	list( $status_recovered, $body_recovered ) = $http_get( '/api/therapists/' . $slug );
	$check(
		$status_recovered === 200 && is_array( $body_recovered ) && count( $body_recovered['slots'] ?? array() ) === 1,
		'HTTP GET /api/therapists/{slug} recovers healthy availability after fault removal'
	);

} finally {
	$wpdb->delete( cb_bookings_table(), array( 'therapist_id' => $id ), array( '%d' ) );
	wp_delete_post( $id, true );
	if ( $created_marker && file_exists( $marker ) ) {
		unlink( $marker );
	}
	if ( $installed_mu && file_exists( $mu_dest ) ) {
		unlink( $mu_dest );
	}
}

$cleanup_posts = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->posts} WHERE ID = %d", $id ) );
$cleanup_bookings = (int) $wpdb->get_var( $wpdb->prepare( 'SELECT COUNT(*) FROM ' . cb_bookings_table() . ' WHERE therapist_id = %d', $id ) );
$check( $cleanup_posts === 0 && $cleanup_bookings === 0, 'Synthetic fixtures cleaned up completely' );

echo "Failures: $failed.\n";
exit( $failed ? 1 : 0 );
