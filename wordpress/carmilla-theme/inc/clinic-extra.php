<?php
/**
 * Clinic extras — therapist matching (← TherapistMatchScreen) and the user's
 * clinic file: mood check-in, journal, homework (my-account tabs). Theme-only,
 * backed by user meta + theme REST.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/** Concern → keyword list matched against a therapist's specialty/approach/body. */
function carmilla_match_concerns() {
	return array(
		'anxiety'    => __( 'اضطراب و استرس', 'carmilla' ),
		'depression' => __( 'افسردگی', 'carmilla' ),
		'relation'   => __( 'رابطه و زوج', 'carmilla' ),
		'family'     => __( 'خانواده و فرزند', 'carmilla' ),
		'self'       => __( 'اعتماد‌به‌نفس و رشد فردی', 'carmilla' ),
	);
}

function carmilla_therapist_card_dto( $id ) {
	return array(
		'id'        => $id,
		'name'      => get_the_title( $id ),
		'permalink' => get_permalink( $id ),
		'image'     => get_the_post_thumbnail_url( $id, 'medium' ) ?: '',
		'specialty' => (string) get_post_meta( $id, 'cb_specialty', true ),
	);
}

add_action( 'rest_api_init', function () {
	register_rest_route( 'carmilla/v1', '/therapist-match', array(
		'methods'             => 'GET',
		'permission_callback' => '__return_true',
		'callback'            => 'carmilla_rest_therapist_match',
	) );

	register_rest_route( 'carmilla/v1', '/mood', array(
		array( 'methods' => 'GET', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_mood_get' ),
		array( 'methods' => 'POST', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_mood_post' ),
	) );
	register_rest_route( 'carmilla/v1', '/journal', array(
		array( 'methods' => 'GET', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_journal_get' ),
		array( 'methods' => 'POST', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_journal_post' ),
	) );
	register_rest_route( 'carmilla/v1', '/homework', array(
		array( 'methods' => 'GET', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_homework_get' ),
		array( 'methods' => 'POST', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_homework_post' ),
	) );
	register_rest_route( 'carmilla/v1', '/homework/(?P<i>\d+)/toggle', array(
		'methods' => 'POST', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_homework_toggle',
	) );
} );

function carmilla_rest_therapist_match( WP_REST_Request $req ) {
	$concern  = sanitize_key( (string) $req->get_param( 'concern' ) );
	$concerns = carmilla_match_concerns();
	$term     = isset( $concerns[ $concern ] ) ? $concerns[ $concern ] : '';

	$ids = get_posts( array(
		'post_type'      => 'cb_therapist',
		'post_status'    => 'publish',
		'posts_per_page' => -1,
		'fields'         => 'ids',
	) );

	$matched = array();
	$others  = array();
	foreach ( $ids as $id ) {
		$hay = get_the_title( $id ) . ' ' . get_post_meta( $id, 'cb_specialty', true ) . ' ' .
			get_post_meta( $id, 'cb_approach', true ) . ' ' . get_post_field( 'post_content', $id );
		if ( $term && mb_strpos( $hay, $term ) !== false ) {
			$matched[] = carmilla_therapist_card_dto( $id );
		} else {
			$others[] = carmilla_therapist_card_dto( $id );
		}
	}
	// Prefer matches; fall back to everyone so the list is never empty.
	$out = $matched ? $matched : $others;
	return rest_ensure_response( array_slice( $out, 0, 6 ) );
}

/* -------- Mood / Journal / Homework (per-user meta) -------- */

function carmilla_rest_mood_get() {
	$moods = get_user_meta( get_current_user_id(), 'cb_moods', true );
	return rest_ensure_response( is_array( $moods ) ? array_slice( $moods, -14 ) : array() );
}
function carmilla_rest_mood_post( WP_REST_Request $req ) {
	$score = max( 1, min( 5, (int) $req->get_param( 'score' ) ) );
	$note  = sanitize_text_field( (string) $req->get_param( 'note' ) );
	$uid   = get_current_user_id();
	$moods = get_user_meta( $uid, 'cb_moods', true );
	$moods = is_array( $moods ) ? $moods : array();
	$moods[] = array( 'date' => gmdate( 'Y-m-d' ), 'score' => $score, 'note' => $note );
	update_user_meta( $uid, 'cb_moods', array_slice( $moods, -60 ) );
	return rest_ensure_response( array( 'ok' => true ) );
}

function carmilla_rest_journal_get() {
	$j = get_user_meta( get_current_user_id(), 'cb_journal', true );
	return rest_ensure_response( is_array( $j ) ? array_reverse( $j ) : array() );
}
function carmilla_rest_journal_post( WP_REST_Request $req ) {
	$text = trim( wp_strip_all_tags( (string) $req->get_param( 'text' ) ) );
	if ( '' === $text ) {
		return new WP_Error( 'validation', 'متن خالی است.', array( 'status' => 400 ) );
	}
	$uid = get_current_user_id();
	$j   = get_user_meta( $uid, 'cb_journal', true );
	$j   = is_array( $j ) ? $j : array();
	$j[] = array( 'time' => current_time( 'c' ), 'text' => $text );
	update_user_meta( $uid, 'cb_journal', array_slice( $j, -200 ) );
	return rest_ensure_response( array( 'ok' => true ) );
}

function carmilla_rest_homework_get() {
	$h = get_user_meta( get_current_user_id(), 'cb_homework', true );
	return rest_ensure_response( is_array( $h ) ? $h : array() );
}
function carmilla_rest_homework_post( WP_REST_Request $req ) {
	$text = trim( wp_strip_all_tags( (string) $req->get_param( 'text' ) ) );
	if ( '' === $text ) {
		return new WP_Error( 'validation', 'متن خالی است.', array( 'status' => 400 ) );
	}
	$uid = get_current_user_id();
	$h   = get_user_meta( $uid, 'cb_homework', true );
	$h   = is_array( $h ) ? $h : array();
	$h[] = array( 'text' => $text, 'done' => false );
	update_user_meta( $uid, 'cb_homework', $h );
	return rest_ensure_response( array_values( $h ) );
}
function carmilla_rest_homework_toggle( WP_REST_Request $req ) {
	$i   = (int) $req['i'];
	$uid = get_current_user_id();
	$h   = get_user_meta( $uid, 'cb_homework', true );
	if ( ! is_array( $h ) || ! isset( $h[ $i ] ) ) {
		return new WP_Error( 'not_found', 'یافت نشد.', array( 'status' => 404 ) );
	}
	$h[ $i ]['done'] = empty( $h[ $i ]['done'] );
	update_user_meta( $uid, 'cb_homework', $h );
	return rest_ensure_response( array_values( $h ) );
}

/* -------- My-account clinic tabs -------- */

add_action( 'init', function () {
	if ( ! carmilla_feature_enabled( 'clinic' ) ) {
		return;
	}
	add_rewrite_endpoint( 'clinic', EP_ROOT | EP_PAGES );
} );

add_filter( 'woocommerce_account_menu_items', function ( $items ) {
	if ( ! carmilla_feature_enabled( 'clinic' ) ) {
		return $items;
	}
	$logout = isset( $items['customer-logout'] ) ? array( 'customer-logout' => $items['customer-logout'] ) : array();
	unset( $items['customer-logout'] );
	$items['clinic'] = __( 'پرونده‌ی مشاوره', 'carmilla' );
	return array_merge( $items, $logout );
} );

add_action( 'woocommerce_account_clinic_endpoint', function () {
	echo '<div id="cb-clinic" class="cb-clinic">';
	echo '<div class="cb-tabs" role="tablist">';
	echo '<button class="cb-tab is-on" data-tab="mood">' . esc_html__( 'حال امروز', 'carmilla' ) . '</button>';
	echo '<button class="cb-tab" data-tab="journal">' . esc_html__( 'ژورنال', 'carmilla' ) . '</button>';
	echo '<button class="cb-tab" data-tab="homework">' . esc_html__( 'تمرین‌ها', 'carmilla' ) . '</button>';
	echo '</div>';
	echo '<div class="cb-tabpane" data-pane="mood"></div>';
	echo '<div class="cb-tabpane" data-pane="journal" hidden></div>';
	echo '<div class="cb-tabpane" data-pane="homework" hidden></div>';
	echo '</div>';
} );
