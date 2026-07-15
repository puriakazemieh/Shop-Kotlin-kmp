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

	register_rest_route( 'carmilla/v1', '/appointments/(?P<id>\d+)/receipt', array(
		'methods' => 'GET', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_appointment_receipt',
	) );
	register_rest_route( 'carmilla/v1', '/appointments/(?P<id>\d+)/messages', array(
		array( 'methods' => 'GET', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_appointment_messages_get' ),
		array( 'methods' => 'POST', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_appointment_messages_post' ),
	) );
} );

/** Guard: the appointment must belong to the current user (or a manager). */
function carmilla_appointment_owned( $id ) {
	if ( get_post_type( $id ) !== 'cb_appointment' ) {
		return false;
	}
	return (int) get_post_meta( $id, 'cb_user_id', true ) === get_current_user_id() || current_user_can( 'edit_others_posts' );
}

/** Session receipt (← SessionReceiptScreen): therapist, slot, status, price. */
function carmilla_rest_appointment_receipt( WP_REST_Request $req ) {
	$id = (int) $req['id'];
	if ( ! carmilla_appointment_owned( $id ) ) {
		return new WP_Error( 'forbidden', 'اجازه‌ی دسترسی ندارید.', array( 'status' => 403 ) );
	}
	$tid   = (int) get_post_meta( $id, 'cb_therapist_id', true );
	$slug  = get_post_meta( $tid, 'cb_product_slug', true );
	$price = '';
	if ( $slug && function_exists( 'wc_get_product' ) ) {
		$product = get_page_by_path( $slug, OBJECT, 'product' );
		if ( $product ) {
			$wc = wc_get_product( $product->ID );
			if ( $wc ) {
				$price = $wc->get_price_html();
			}
		}
	}
	return rest_ensure_response( array(
		'id'            => $id,
		'therapistName' => get_the_title( $tid ),
		'specialty'     => (string) get_post_meta( $tid, 'cb_specialty', true ),
		'slot'          => (string) get_post_meta( $id, 'cb_slot', true ),
		'status'        => (string) get_post_meta( $id, 'cb_status', true ),
		'priceHtml'     => $price,
		'bookedAt'      => get_the_date( 'c', $id ),
	) );
}

/** Per-appointment messaging thread (stored as comments on the appointment). */
function carmilla_rest_appointment_messages_get( WP_REST_Request $req ) {
	$id = (int) $req['id'];
	if ( ! carmilla_appointment_owned( $id ) ) {
		return new WP_Error( 'forbidden', 'اجازه‌ی دسترسی ندارید.', array( 'status' => 403 ) );
	}
	$uid      = get_current_user_id();
	$comments = get_comments( array( 'post_id' => $id, 'type' => 'cb_msg', 'order' => 'ASC', 'status' => 'approve' ) );
	$out = array_map( function ( $c ) use ( $uid ) {
		return array(
			'me'   => (int) $c->user_id === (int) $uid,
			'text' => $c->comment_content,
			'time' => get_comment_date( 'c', $c ),
		);
	}, $comments );
	return rest_ensure_response( $out );
}

function carmilla_rest_appointment_messages_post( WP_REST_Request $req ) {
	$id = (int) $req['id'];
	if ( ! carmilla_appointment_owned( $id ) ) {
		return new WP_Error( 'forbidden', 'اجازه‌ی دسترسی ندارید.', array( 'status' => 403 ) );
	}
	$text = trim( wp_strip_all_tags( (string) $req->get_param( 'text' ) ) );
	if ( '' === $text ) {
		return new WP_Error( 'validation', 'پیام خالی است.', array( 'status' => 400 ) );
	}
	$user = wp_get_current_user();
	$cid  = wp_insert_comment( array(
		'comment_post_ID'      => $id,
		'comment_content'      => $text,
		'comment_type'         => 'cb_msg',
		'user_id'              => $user->ID,
		'comment_author'       => $user->display_name,
		'comment_author_email' => $user->user_email,
		'comment_approved'     => 1,
	) );
	if ( ! $cid ) {
		return new WP_Error( 'create_failed', 'ارسال نشد.', array( 'status' => 400 ) );
	}
	return rest_ensure_response( array( 'me' => true, 'text' => $text, 'time' => current_time( 'c' ) ) );
}

/** Keep messaging comments out of default comment queries/counts. */
add_filter( 'comments_clauses', function ( $clauses, $query ) {
	if ( ! is_admin() && empty( $query->query_vars['type'] ) ) {
		$clauses['where'] .= " AND comment_type != 'cb_msg'";
	}
	return $clauses;
}, 11, 2 );

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
	add_rewrite_endpoint( 'appointments', EP_ROOT | EP_PAGES );
} );

add_filter( 'woocommerce_account_menu_items', function ( $items ) {
	if ( ! carmilla_feature_enabled( 'clinic' ) ) {
		return $items;
	}
	$logout = isset( $items['customer-logout'] ) ? array( 'customer-logout' => $items['customer-logout'] ) : array();
	unset( $items['customer-logout'] );
	$items['appointments'] = __( 'نوبت‌های من', 'carmilla' );
	$items['clinic']       = __( 'پرونده‌ی مشاوره', 'carmilla' );
	return array_merge( $items, $logout );
} );

/** Emergency resources banner (← EmergencyScreen), configurable in the Customizer. */
function carmilla_emergency_banner() {
	$phone = get_theme_mod( 'carmilla_emergency_phone', '۱۴۸۰' );
	$note  = get_theme_mod( 'carmilla_emergency_note', 'اگر در وضعیت بحرانی هستید یا افکار آسیب به خود دارید، همین حالا تماس بگیرید.' );
	if ( ! $phone ) {
		return;
	}
	$tel = preg_replace( '/\D/', '', carmilla_to_english_digits( $phone ) );
	echo '<div class="card card--pad cb-emergency">';
	echo '<h3 class="t-title-sm">' . esc_html__( 'کمک فوری', 'carmilla' ) . '</h3>';
	echo '<p class="t-body-sm">' . esc_html( $note ) . '</p>';
	echo '<a class="btn btn--primary" href="tel:' . esc_attr( $tel ) . '">' . esc_html__( 'تماس با اورژانس', 'carmilla' ) . ' ' . esc_html( $phone ) . '</a>';
	echo '</div>';
}

add_action( 'woocommerce_account_appointments_endpoint', function () {
	carmilla_emergency_banner();
	echo '<h3 class="t-title-sm" style="margin-block-start:var(--sp-lg)">' . esc_html__( 'نوبت‌های من', 'carmilla' ) . '</h3>';
	echo '<div id="cb-appointments" class="cb-appointments"></div>';
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
