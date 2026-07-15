<?php
/**
 * Clinic appointment booking — data-driven, theme-only.
 * Therapist availability is a line-based meta (cb_slots); bookings are stored as
 * private cb_appointment posts. Slot listing subtracts already-booked slots, and
 * booking re-checks availability just before insert to avoid obvious double-books.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/** Future availability slots a therapist offers (raw strings, past ones dropped). */
function carmilla_therapist_slots( $therapist_id ) {
	$raw   = (string) get_post_meta( $therapist_id, 'cb_slots', true );
	$lines = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $raw ) ) );
	$now   = time();
	$out   = array();
	foreach ( $lines as $s ) {
		$ts = strtotime( $s );
		if ( ! $ts || $ts > $now ) {
			$out[] = $s;
		}
	}
	return array_values( array_unique( $out ) );
}

/** Slots already booked (status != CANCELLED) for a therapist. */
function carmilla_booked_slots( $therapist_id ) {
	$q = new WP_Query( array(
		'post_type'      => 'cb_appointment',
		'post_status'    => 'publish',
		'posts_per_page' => -1,
		'fields'         => 'ids',
		'meta_query'     => array(
			array( 'key' => 'cb_therapist_id', 'value' => (int) $therapist_id ),
			array( 'key' => 'cb_status', 'value' => 'CANCELLED', 'compare' => '!=' ),
		),
	) );
	return array_map( function ( $id ) {
		return (string) get_post_meta( $id, 'cb_slot', true );
	}, $q->posts );
}

/** Available = offered − booked. */
function carmilla_available_slots( $therapist_id ) {
	return array_values( array_diff( carmilla_therapist_slots( $therapist_id ), carmilla_booked_slots( $therapist_id ) ) );
}

/** Gating: free unless a product slug is set; then requires ≥1 session credit
 *  (credits granted on completed WooCommerce orders — see inc/access.php). */
function carmilla_therapist_accessible( $therapist_id ) {
	$slug = get_post_meta( $therapist_id, 'cb_product_slug', true );
	if ( ! $slug ) {
		return true;
	}
	if ( ! is_user_logged_in() ) {
		return false;
	}
	return carmilla_therapist_credits( $therapist_id ) > 0;
}

function carmilla_appointment_dto( $id ) {
	$tid = (int) get_post_meta( $id, 'cb_therapist_id', true );
	return array(
		'id'            => (int) $id,
		'therapistId'   => $tid,
		'therapistName' => get_the_title( $tid ),
		'slot'          => (string) get_post_meta( $id, 'cb_slot', true ),
		'status'        => (string) get_post_meta( $id, 'cb_status', true ),
	);
}

add_action( 'rest_api_init', function () {
	register_rest_route( 'carmilla/v1', '/therapists/(?P<id>\d+)/slots', array(
		'methods'             => 'GET',
		'callback'            => function ( $req ) {
			return rest_ensure_response( array_values( carmilla_available_slots( (int) $req['id'] ) ) );
		},
		'permission_callback' => '__return_true',
	) );

	register_rest_route( 'carmilla/v1', '/appointments', array(
		array(
			'methods'             => 'GET',
			'callback'            => 'carmilla_rest_my_appointments',
			'permission_callback' => 'is_user_logged_in',
		),
		array(
			'methods'             => 'POST',
			'callback'            => 'carmilla_rest_book_appointment',
			'permission_callback' => 'is_user_logged_in',
		),
	) );

	register_rest_route( 'carmilla/v1', '/appointments/(?P<id>\d+)/cancel', array(
		'methods'             => 'POST',
		'callback'            => 'carmilla_rest_cancel_appointment',
		'permission_callback' => 'is_user_logged_in',
	) );
} );

function carmilla_rest_my_appointments( WP_REST_Request $req ) {
	$q = new WP_Query( array(
		'post_type'      => 'cb_appointment',
		'post_status'    => 'publish',
		'posts_per_page' => -1,
		'fields'         => 'ids',
		'meta_query'     => array( array( 'key' => 'cb_user_id', 'value' => get_current_user_id() ) ),
	) );
	return rest_ensure_response( array_map( 'carmilla_appointment_dto', $q->posts ) );
}

function carmilla_rest_book_appointment( WP_REST_Request $req ) {
	$tid  = (int) $req->get_param( 'therapistId' );
	$slot = sanitize_text_field( (string) $req->get_param( 'slot' ) );

	if ( get_post_type( $tid ) !== 'cb_therapist' || '' === $slot ) {
		return new WP_Error( 'validation', 'اطلاعات نوبت نامعتبر است.', array( 'status' => 400 ) );
	}
	if ( ! carmilla_therapist_accessible( $tid ) ) {
		return new WP_Error( 'forbidden', 'برای رزرو، ابتدا اعتبار جلسه را خریداری کنید.', array( 'status' => 403 ) );
	}
	// Re-check availability right before insert.
	if ( ! in_array( $slot, carmilla_available_slots( $tid ), true ) ) {
		return new WP_Error( 'taken', 'این بازه دیگر در دسترس نیست.', array( 'status' => 409 ) );
	}

	$id = wp_insert_post( array(
		'post_type'   => 'cb_appointment',
		'post_status' => 'publish',
		'post_title'  => sprintf( 'نوبت %s — %s', get_the_title( $tid ), $slot ),
		'post_author' => get_current_user_id(),
	), true );
	if ( is_wp_error( $id ) ) {
		return new WP_Error( 'create_failed', $id->get_error_message(), array( 'status' => 400 ) );
	}
	update_post_meta( $id, 'cb_therapist_id', $tid );
	update_post_meta( $id, 'cb_user_id', get_current_user_id() );
	update_post_meta( $id, 'cb_slot', $slot );
	update_post_meta( $id, 'cb_status', 'BOOKED' );

	// Consume one session credit when the therapist is paid.
	if ( get_post_meta( $tid, 'cb_product_slug', true ) ) {
		carmilla_spend_therapist_credit( $tid, get_current_user_id() );
	}

	return rest_ensure_response( carmilla_appointment_dto( $id ) );
}

function carmilla_rest_cancel_appointment( WP_REST_Request $req ) {
	$id = (int) $req['id'];
	if ( get_post_type( $id ) !== 'cb_appointment' ) {
		return new WP_Error( 'not_found', 'یافت نشد.', array( 'status' => 404 ) );
	}
	if ( (int) get_post_meta( $id, 'cb_user_id', true ) !== get_current_user_id() && ! current_user_can( 'edit_others_posts' ) ) {
		return new WP_Error( 'forbidden', 'اجازه‌ی لغو ندارید.', array( 'status' => 403 ) );
	}
	update_post_meta( $id, 'cb_status', 'CANCELLED' );

	// Refund the session credit if the therapist is paid.
	$tid = (int) get_post_meta( $id, 'cb_therapist_id', true );
	if ( get_post_meta( $tid, 'cb_product_slug', true ) ) {
		carmilla_add_therapist_credit( $tid, (int) get_post_meta( $id, 'cb_user_id', true ), 1 );
	}
	return rest_ensure_response( carmilla_appointment_dto( $id ) );
}
