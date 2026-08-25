<?php
/**
 * Course requests ("درخواست دوره") — users submit topics they want taught and
 * like others' requests. Backed by the cb_course_request CPT; likers are stored
 * in post meta cb_likers.
 *
 *   GET  api/course-requests            -> List<CourseRequestResponse>
 *   GET  api/course-requests/mine       -> List<CourseRequestResponse>
 *   POST api/course-requests            -> CourseRequestResponse (CreateCourseRequestRequestDto)
 *   POST api/course-requests/{id}/like  -> ToggleLikeResponse
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Course_Request_Controller {

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$login = array( 'CB_Plugin', 'require_login' );

		register_rest_route( $ns, '/api/course-requests', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_all' ), 'permission_callback' => '__return_true' ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'create' ), 'permission_callback' => $login ),
		) );
		register_rest_route( $ns, '/api/course-requests/mine', array( 'methods' => 'GET', 'callback' => array( $this, 'mine' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/course-requests/(?P<id>\d+)/like', array( 'methods' => 'POST', 'callback' => array( $this, 'toggle_like' ), 'permission_callback' => $login ) );
	}

	private function likers( int $id ): array {
		$v = get_post_meta( $id, 'cb_likers', true );
		return is_array( $v ) ? array_map( 'intval', $v ) : array();
	}

	private function dto( WP_Post $p, int $uid ): array {
		$likers = $this->likers( $p->ID );
		$author = get_userdata( (int) $p->post_author );
		return array(
			'id'            => (int) $p->ID,
			'title'         => get_the_title( $p ),
			'description'   => $p->post_content ?: null,
			'requesterName' => $author ? $author->display_name : null,
			'likeCount'     => count( $likers ),
			'liked'         => $uid && in_array( $uid, $likers, true ),
			'fulfilled'     => (bool) get_post_meta( $p->ID, 'cb_fulfilled', true ),
			'createdAt'     => cb_iso( $p->post_date_gmt ),
		);
	}

	public function list_all(): WP_REST_Response {
		$uid   = get_current_user_id();
		$posts = get_posts( array( 'post_type' => 'cb_course_request', 'post_status' => 'publish', 'numberposts' => 200, 'orderby' => 'date', 'order' => 'DESC' ) );
		$out   = array();
		foreach ( $posts as $p ) {
			$out[] = $this->dto( $p, $uid );
		}
		return cb_response( $out );
	}

	public function mine(): WP_REST_Response {
		$uid   = get_current_user_id();
		$posts = get_posts( array( 'post_type' => 'cb_course_request', 'post_status' => array( 'publish', 'pending', 'draft' ), 'numberposts' => 200, 'author' => $uid, 'orderby' => 'date', 'order' => 'DESC' ) );
		$out   = array();
		foreach ( $posts as $p ) {
			$out[] = $this->dto( $p, $uid );
		}
		return cb_response( $out );
	}

	public function create( WP_REST_Request $request ): WP_REST_Response {
		$b     = $request->get_json_params();
		$title = trim( (string) ( $b['title'] ?? '' ) );
		if ( $title === '' ) {
			return cb_error( 'عنوان الزامی است', 400, 'VALIDATION', 'api/course-requests' );
		}
		$id = wp_insert_post( array(
			'post_type'    => 'cb_course_request',
			'post_status'  => 'publish',
			'post_title'   => sanitize_text_field( $title ),
			'post_content' => sanitize_textarea_field( (string) ( $b['description'] ?? '' ) ),
			'post_author'  => get_current_user_id(),
		) );
		if ( is_wp_error( $id ) ) {
			return cb_error( 'ثبت درخواست ناموفق بود', 400, 'CREATE_FAILED', 'api/course-requests' );
		}
		return cb_response( $this->dto( get_post( $id ), get_current_user_id() ), 201 );
	}

	public function toggle_like( WP_REST_Request $request ): WP_REST_Response {
		$id = (int) $request['id'];
		if ( get_post_type( $id ) !== 'cb_course_request' ) {
			return cb_error( 'درخواست یافت نشد', 404, 'NOT_FOUND', 'api/course-requests' );
		}
		$uid    = get_current_user_id();
		$likers = $this->likers( $id );
		if ( in_array( $uid, $likers, true ) ) {
			$likers = array_values( array_diff( $likers, array( $uid ) ) );
			$liked  = false;
		} else {
			$likers[] = $uid;
			$liked    = true;
		}
		update_post_meta( $id, 'cb_likers', $likers );
		return cb_response( array( 'liked' => $liked, 'likeCount' => count( $likers ) ) );
	}
}
