<?php
/**
 * Course playback + progress — data-driven, theme-only.
 * Lessons are a line-based course meta (cb_lessons): «عنوان | https://ویدیو | free».
 * Progress is stored per user in user meta and updated via a theme REST route.
 * Non-free lessons require purchasing the linked WooCommerce product.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/** Parse lessons: [ ['title'=>, 'url'=>, 'free'=>bool], ... ]. */
function carmilla_course_lessons( $course_id ) {
	$raw   = (string) get_post_meta( $course_id, 'cb_lessons', true );
	$lines = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $raw ) ) );
	$out   = array();
	foreach ( $lines as $line ) {
		$p    = array_map( 'trim', explode( '|', $line ) );
		$out[] = array(
			'title' => $p[0] ?? '',
			'url'   => isset( $p[1] ) ? esc_url_raw( $p[1] ) : '',
			'free'  => isset( $p[2] ) && in_array( strtolower( $p[2] ), array( 'free', 'رایگان', '1', 'true' ), true ),
		);
	}
	return $out;
}

/** Whether the visitor bought the course (free when no product slug). */
function carmilla_course_accessible( $course_id ) {
	$slug = get_post_meta( $course_id, 'cb_product_slug', true );
	if ( ! $slug || ! function_exists( 'wc_customer_bought_product' ) ) {
		return true;
	}
	if ( ! is_user_logged_in() ) {
		return false;
	}
	$product = get_page_by_path( $slug, OBJECT, 'product' );
	return $product ? wc_customer_bought_product( '', get_current_user_id(), $product->ID ) : true;
}

/** Completed lesson indices for the current user on a course. */
function carmilla_course_progress( $course_id, $user_id = 0 ) {
	$user_id = $user_id ?: get_current_user_id();
	if ( ! $user_id ) {
		return array();
	}
	$done = get_user_meta( $user_id, "cb_course_prog_$course_id", true );
	return is_array( $done ) ? array_map( 'intval', $done ) : array();
}

function carmilla_course_percent( $course_id, $user_id = 0 ) {
	$total = count( carmilla_course_lessons( $course_id ) );
	if ( ! $total ) {
		return 0;
	}
	return (int) round( count( carmilla_course_progress( $course_id, $user_id ) ) / $total * 100 );
}

add_action( 'rest_api_init', function () {
	register_rest_route( 'carmilla/v1', '/courses/(?P<id>\d+)/lessons/(?P<index>\d+)/complete', array(
		'methods'             => 'POST',
		'callback'            => 'carmilla_rest_complete_lesson',
		'permission_callback' => 'is_user_logged_in',
	) );
} );

function carmilla_rest_complete_lesson( WP_REST_Request $req ) {
	$course_id = (int) $req['id'];
	$index     = (int) $req['index'];
	if ( get_post_type( $course_id ) !== 'cb_course' ) {
		return new WP_Error( 'not_found', 'دوره یافت نشد.', array( 'status' => 404 ) );
	}
	if ( ! carmilla_course_accessible( $course_id ) ) {
		return new WP_Error( 'forbidden', 'دسترسی ندارید.', array( 'status' => 403 ) );
	}
	$done = carmilla_course_progress( $course_id );
	if ( ! in_array( $index, $done, true ) ) {
		$done[] = $index;
		update_user_meta( get_current_user_id(), "cb_course_prog_$course_id", $done );
	}
	return rest_ensure_response( array(
		'completed' => array_values( $done ),
		'percent'   => carmilla_course_percent( $course_id ),
	) );
}
