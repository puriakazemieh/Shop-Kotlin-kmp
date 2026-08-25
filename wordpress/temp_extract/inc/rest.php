<?php
/**
 * Theme-owned REST API (no plugin) powering interactive, data-driven features.
 * Namespace: carmilla/v1. Currently: course requests (list / create / like) —
 * a full example of a data-driven vertical implemented entirely in the theme.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

add_action( 'rest_api_init', function () {
	register_rest_route( 'carmilla/v1', '/course-requests', array(
		array(
			'methods'             => 'GET',
			'callback'            => 'carmilla_rest_list_course_requests',
			'permission_callback' => '__return_true',
		),
		array(
			'methods'             => 'POST',
			'callback'            => 'carmilla_rest_create_course_request',
			'permission_callback' => function () {
				return is_user_logged_in();
			},
		),
	) );

	register_rest_route( 'carmilla/v1', '/course-requests/(?P<id>\d+)/like', array(
		'methods'             => 'POST',
		'callback'            => 'carmilla_rest_like_course_request',
		'permission_callback' => function () {
			return is_user_logged_in();
		},
	) );
} );

/** Read the liked-user id list, normalized to an int array (empty meta -> []). */
function carmilla_liked_users( $post_id ) {
	$raw = get_post_meta( $post_id, 'cb_liked_users', true );
	return is_array( $raw ) ? array_map( 'intval', $raw ) : array();
}

/** Shape a course-request post for the API. */
function carmilla_course_request_dto( $post ) {
	$liked_users = carmilla_liked_users( $post->ID );
	$uid         = get_current_user_id();
	return array(
		'id'            => (int) $post->ID,
		'title'         => get_the_title( $post ),
		'description'   => wp_strip_all_tags( $post->post_content ),
		'requesterName' => get_the_author_meta( 'display_name', $post->post_author ) ?: null,
		'likeCount'     => count( $liked_users ),
		'liked'         => $uid && in_array( $uid, array_map( 'intval', $liked_users ), true ),
		'fulfilled'     => get_post_meta( $post->ID, 'cb_fulfilled', true ) === '1',
		'createdAt'     => get_post_time( 'c', true, $post ),
	);
}

function carmilla_rest_list_course_requests( WP_REST_Request $req ) {
	$q = new WP_Query( array(
		'post_type'      => 'cb_course_request',
		'post_status'    => 'publish',
		'posts_per_page' => 50,
		'orderby'        => 'meta_value_num',
		'meta_key'       => 'cb_like_count',
		'order'          => 'DESC',
	) );
	$out = array_map( 'carmilla_course_request_dto', $q->posts );
	return rest_ensure_response( $out );
}

function carmilla_rest_create_course_request( WP_REST_Request $req ) {
	$title = sanitize_text_field( (string) $req->get_param( 'title' ) );
	$desc  = sanitize_textarea_field( (string) $req->get_param( 'description' ) );
	if ( '' === trim( $title ) ) {
		return new WP_Error( 'validation', 'عنوان درخواست الزامی است.', array( 'status' => 400 ) );
	}
	$id = wp_insert_post( array(
		'post_type'    => 'cb_course_request',
		'post_title'   => $title,
		'post_content' => $desc,
		'post_status'  => 'publish',
		'post_author'  => get_current_user_id(),
	), true );
	if ( is_wp_error( $id ) ) {
		return new WP_Error( 'create_failed', $id->get_error_message(), array( 'status' => 400 ) );
	}
	update_post_meta( $id, 'cb_like_count', 0 );
	return rest_ensure_response( carmilla_course_request_dto( get_post( $id ) ) );
}

function carmilla_rest_like_course_request( WP_REST_Request $req ) {
	$id = (int) $req['id'];
	if ( get_post_type( $id ) !== 'cb_course_request' ) {
		return new WP_Error( 'not_found', 'یافت نشد.', array( 'status' => 404 ) );
	}
	$uid   = get_current_user_id();
	$users = carmilla_liked_users( $id );

	if ( in_array( $uid, $users, true ) ) {
		$users = array_values( array_diff( $users, array( $uid ) ) );
	} else {
		$users[] = $uid;
	}
	update_post_meta( $id, 'cb_liked_users', $users );
	update_post_meta( $id, 'cb_like_count', count( $users ) );

	return rest_ensure_response( array(
		'liked'     => in_array( $uid, $users, true ),
		'likeCount' => count( $users ),
	) );
}
