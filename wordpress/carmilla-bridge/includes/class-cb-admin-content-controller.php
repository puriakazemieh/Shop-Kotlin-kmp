<?php
/**
 * Admin (content/verticals) endpoints — manage courses, therapists, psych tests
 * and stories from the app, plus review project submissions, course-refund and
 * therapist-switch requests. Admin/shop-manager Bearer only.
 *
 * Courses/tests keep the same line-based meta the read side parses, so content
 * created here shows up identically in the app and the Carmilla theme.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Admin_Content_Controller {

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$admin = array( 'CB_Plugin', 'require_admin' );

		// Courses.
		register_rest_route( $ns, '/api/admin/courses', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_courses' ), 'permission_callback' => $admin ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'create_course' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/courses/(?P<id>\d+)', array(
			array( 'methods' => 'PATCH', 'callback' => array( $this, 'update_course' ), 'permission_callback' => $admin ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_course' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/courses/(?P<id>\d+)/quiz', array( 'methods' => 'POST', 'callback' => array( $this, 'upsert_course_quiz' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/courses/(?P<id>\d+)/lessons', array( 'methods' => 'POST', 'callback' => array( $this, 'add_lesson' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/courses/(?P<id>\d+)/projects', array( 'methods' => 'GET', 'callback' => array( $this, 'course_projects' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/courses/projects/(?P<sid>\d+)/review', array( 'methods' => 'POST', 'callback' => array( $this, 'review_project' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/academy/refund-requests', array( 'methods' => 'GET', 'callback' => array( $this, 'refund_requests' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/academy/refund-requests/(?P<id>\d+)/review', array( 'methods' => 'POST', 'callback' => array( $this, 'review_refund' ), 'permission_callback' => $admin ) );

		// Therapists.
		register_rest_route( $ns, '/api/admin/therapists', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_therapists' ), 'permission_callback' => $admin ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'create_therapist' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/therapists/(?P<id>\d+)', array(
			array( 'methods' => 'PATCH', 'callback' => array( $this, 'update_therapist' ), 'permission_callback' => $admin ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_therapist' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/therapists/(?P<id>\d+)/generate-slots', array( 'methods' => 'POST', 'callback' => array( $this, 'generate_slots' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/therapists/appointments', array( 'methods' => 'GET', 'callback' => array( $this, 'appointments' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/therapists/appointments/(?P<id>\d+)/confirm', array( 'methods' => 'POST', 'callback' => array( $this, 'confirm_appointment' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/therapists/appointments/(?P<id>\d+)/complete', array( 'methods' => 'POST', 'callback' => array( $this, 'complete_appointment' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/clinic/switch-requests', array( 'methods' => 'GET', 'callback' => array( $this, 'switch_requests' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/clinic/switch-requests/(?P<id>\d+)/review', array( 'methods' => 'POST', 'callback' => array( $this, 'review_switch' ), 'permission_callback' => $admin ) );

		// Psych tests.
		register_rest_route( $ns, '/api/admin/psych-tests', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_tests' ), 'permission_callback' => $admin ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'create_test' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/psych-tests/(?P<id>\d+)', array(
			array( 'methods' => 'PATCH', 'callback' => array( $this, 'update_test' ), 'permission_callback' => $admin ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_test' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/psych-tests/pending-interpretations', array( 'methods' => 'GET', 'callback' => array( $this, 'pending_interpretations' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/psych-tests/user-tests/(?P<uid>\d+)/interpret', array( 'methods' => 'POST', 'callback' => array( $this, 'interpret' ), 'permission_callback' => $admin ) );

		// Stories.
		register_rest_route( $ns, '/api/admin/stories', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'admin_stories' ), 'permission_callback' => $admin ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'create_story' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/stories/(?P<id>\d+)', array(
			array( 'methods' => 'PATCH', 'callback' => array( $this, 'update_story' ), 'permission_callback' => $admin ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_story' ), 'permission_callback' => $admin ),
		) );
	}

	private function product_slug_from_id( $product_id ): string {
		$product_id = (int) $product_id;
		if ( ! $product_id ) {
			return '';
		}
		$p = get_post( $product_id );
		return $p ? $p->post_name : '';
	}

	// ---- courses ------------------------------------------------------------

	public function list_courses(): WP_REST_Response {
		$posts = get_posts( array( 'post_type' => 'cb_course', 'post_status' => array( 'publish', 'draft' ), 'numberposts' => 200 ) );
		$out   = array();
		foreach ( $posts as $p ) {
			$out[] = array( 'id' => (int) $p->ID, 'title' => get_the_title( $p ), 'slug' => $p->post_name, 'price' => (float) get_post_meta( $p->ID, 'cb_price', true ), 'isPublished' => $p->post_status === 'publish' );
		}
		return cb_response( $out );
	}

	public function create_course( WP_REST_Request $request ): WP_REST_Response {
		$b  = $request->get_json_params();
		$id = wp_insert_post( array(
			'post_type'    => 'cb_course',
			'post_status'  => ! empty( $b['isPublished'] ) || ! isset( $b['isPublished'] ) ? 'publish' : 'draft',
			'post_title'   => sanitize_text_field( (string) ( $b['title'] ?? 'دوره' ) ),
			'post_name'    => sanitize_title( (string) ( $b['slug'] ?? '' ) ),
			'post_content' => wp_kses_post( (string) ( $b['description'] ?? '' ) ),
		) );
		if ( is_wp_error( $id ) ) {
			return cb_error( 'ساخت دوره ناموفق بود', 400, 'CREATE_FAILED', 'api/admin/courses' );
		}
		$this->apply_course_meta( $id, $b );
		return cb_response( array( 'id' => (int) $id ), 201 );
	}

	private function apply_course_meta( int $id, array $b ): void {
		$map = array(
			'instructor' => 'cb_instructor', 'level' => 'cb_level', 'format' => 'cb_format',
			'location' => 'cb_location', 'instructorBio' => 'cb_instructor_bio',
		);
		foreach ( $map as $in => $meta ) {
			if ( isset( $b[ $in ] ) ) {
				update_post_meta( $id, $meta, sanitize_text_field( (string) $b[ $in ] ) );
			}
		}
		if ( isset( $b['price'] ) ) {
			update_post_meta( $id, 'cb_price', (float) $b['price'] );
		}
		if ( isset( $b['discountedPrice'] ) ) {
			update_post_meta( $id, 'cb_discounted_price', $b['discountedPrice'] !== null ? (float) $b['discountedPrice'] : '' );
		}
		if ( isset( $b['capacity'] ) ) {
			update_post_meta( $id, 'cb_capacity', (int) $b['capacity'] );
		}
		if ( isset( $b['requiresProjectSubmission'] ) ) {
			update_post_meta( $id, 'cb_requires_project', $b['requiresProjectSubmission'] ? 1 : 0 );
		}
		if ( isset( $b['productId'] ) ) {
			update_post_meta( $id, 'cb_product_slug', $this->product_slug_from_id( $b['productId'] ) );
		}
	}

	public function update_course( WP_REST_Request $request ): WP_REST_Response {
		$id = (int) $request['id'];
		if ( get_post_type( $id ) !== 'cb_course' ) {
			return cb_error( 'دوره یافت نشد', 404, 'NOT_FOUND', 'api/admin/courses' );
		}
		$b   = $request->get_json_params();
		$upd = array( 'ID' => $id );
		if ( isset( $b['title'] ) ) {
			$upd['post_title'] = sanitize_text_field( (string) $b['title'] );
		}
		if ( isset( $b['description'] ) ) {
			$upd['post_content'] = wp_kses_post( (string) $b['description'] );
		}
		if ( isset( $b['isPublished'] ) ) {
			$upd['post_status'] = $b['isPublished'] ? 'publish' : 'draft';
		}
		wp_update_post( $upd );
		$this->apply_course_meta( $id, $b );
		return cb_response( array( 'id' => $id ) );
	}

	public function delete_course( WP_REST_Request $request ): WP_REST_Response {
		wp_delete_post( (int) $request['id'], true );
		return cb_response( null, 204 );
	}

	public function upsert_course_quiz( WP_REST_Request $request ): WP_REST_Response {
		$id = (int) $request['id'];
		$b  = $request->get_json_params();
		update_post_meta( $id, 'cb_quiz', cb_build_quiz_lines( (array) ( $b['questions'] ?? array() ) ) );
		update_post_meta( $id, 'cb_pass_score', (int) ( $b['passScore'] ?? 60 ) );
		return cb_response( array( 'id' => $id ) );
	}

	public function add_lesson( WP_REST_Request $request ): WP_REST_Response {
		$id   = (int) $request['id'];
		$b    = $request->get_json_params();
		$line = trim( (string) ( $b['title'] ?? '' ) );
		if ( ! empty( $b['videoUrl'] ) ) {
			$line .= ' | ' . esc_url_raw( (string) $b['videoUrl'] );
		}
		if ( ! empty( $b['isFreePreview'] ) ) {
			$line .= ' | free';
		}
		$raw = trim( (string) get_post_meta( $id, 'cb_lessons', true ) );
		update_post_meta( $id, 'cb_lessons', $raw === '' ? $line : $raw . "\n" . $line );
		return cb_response( array( 'id' => $id ), 201 );
	}

	public function course_projects( WP_REST_Request $request ): WP_REST_Response {
		$id  = (int) $request['id'];
		$all = get_option( 'cb_submissions', array() );
		$out = array();
		foreach ( ( is_array( $all ) ? $all : array() ) as $s ) {
			if ( (int) $s['courseId'] === $id ) {
				$out[] = $s;
			}
		}
		return cb_response( $out );
	}

	public function review_project( WP_REST_Request $request ): WP_REST_Response {
		$sid = (int) $request['sid'];
		$b   = $request->get_json_params();
		$all = get_option( 'cb_submissions', array() );
		$all = is_array( $all ) ? $all : array();
		foreach ( $all as &$s ) {
			if ( (int) $s['id'] === $sid ) {
				$s['status']         = sanitize_text_field( (string) ( $b['status'] ?? 'APPROVED' ) );
				$s['mentorFeedback'] = isset( $b['mentorFeedback'] ) ? sanitize_text_field( (string) $b['mentorFeedback'] ) : null;
				$s['reviewedAt']     = gmdate( 'c' );
				$updated             = $s;
			}
		}
		unset( $s );
		update_option( 'cb_submissions', $all, false );
		return isset( $updated ) ? cb_response( $updated ) : cb_error( 'یافت نشد', 404, 'NOT_FOUND', 'api/admin/courses/projects' );
	}

	public function refund_requests(): WP_REST_Response {
		$users = get_users( array( 'meta_key' => 'cb_course_refunds', 'number' => 500 ) );
		$out   = array();
		foreach ( $users as $u ) {
			foreach ( (array) get_user_meta( $u->ID, 'cb_course_refunds', true ) as $r ) {
				$r['userId']   = (int) $u->ID;
				$r['userName'] = $u->display_name;
				$out[]         = $r;
			}
		}
		return cb_response( $out );
	}

	public function review_refund( WP_REST_Request $request ): WP_REST_Response {
		return $this->review_user_meta_list( 'cb_course_refunds', (int) $request['id'], $request, 'api/admin/academy/refund-requests' );
	}

	// ---- therapists ---------------------------------------------------------

	public function list_therapists(): WP_REST_Response {
		$posts = get_posts( array( 'post_type' => 'cb_therapist', 'post_status' => array( 'publish', 'draft' ), 'numberposts' => 200 ) );
		$out   = array();
		foreach ( $posts as $p ) {
			$out[] = array( 'id' => (int) $p->ID, 'name' => get_the_title( $p ), 'slug' => $p->post_name, 'specialty' => get_post_meta( $p->ID, 'cb_specialty', true ) ?: null, 'sessionPrice' => (float) get_post_meta( $p->ID, 'cb_session_price', true ) );
		}
		return cb_response( $out );
	}

	public function create_therapist( WP_REST_Request $request ): WP_REST_Response {
		$b  = $request->get_json_params();
		$id = wp_insert_post( array(
			'post_type'    => 'cb_therapist',
			'post_status'  => ( isset( $b['isActive'] ) && ! $b['isActive'] ) ? 'draft' : 'publish',
			'post_title'   => sanitize_text_field( (string) ( $b['name'] ?? 'درمانگر' ) ),
			'post_name'    => sanitize_title( (string) ( $b['slug'] ?? '' ) ),
			'post_content' => wp_kses_post( (string) ( $b['bio'] ?? '' ) ),
		) );
		if ( is_wp_error( $id ) ) {
			return cb_error( 'ساخت درمانگر ناموفق بود', 400, 'CREATE_FAILED', 'api/admin/therapists' );
		}
		$this->apply_therapist_meta( $id, $b );
		return cb_response( array( 'id' => (int) $id ), 201 );
	}

	private function apply_therapist_meta( int $id, array $b ): void {
		if ( isset( $b['specialty'] ) ) {
			update_post_meta( $id, 'cb_specialty', sanitize_text_field( (string) $b['specialty'] ) );
		}
		if ( isset( $b['bio'] ) ) {
			update_post_meta( $id, 'cb_approach', sanitize_text_field( (string) $b['bio'] ) );
		}
		if ( isset( $b['sessionPrice'] ) ) {
			update_post_meta( $id, 'cb_session_price', (float) $b['sessionPrice'] );
		}
		if ( isset( $b['sessionDurationMinutes'] ) ) {
			update_post_meta( $id, 'cb_duration', (int) $b['sessionDurationMinutes'] );
		}
		if ( isset( $b['mode'] ) ) {
			update_post_meta( $id, 'cb_mode', sanitize_text_field( (string) $b['mode'] ) );
		}
		if ( isset( $b['location'] ) ) {
			update_post_meta( $id, 'cb_location', sanitize_text_field( (string) $b['location'] ) );
		}
		if ( isset( $b['productId'] ) ) {
			update_post_meta( $id, 'cb_product_slug', $this->product_slug_from_id( $b['productId'] ) );
		}
	}

	public function update_therapist( WP_REST_Request $request ): WP_REST_Response {
		$id = (int) $request['id'];
		if ( get_post_type( $id ) !== 'cb_therapist' ) {
			return cb_error( 'درمانگر یافت نشد', 404, 'NOT_FOUND', 'api/admin/therapists' );
		}
		$b   = $request->get_json_params();
		$upd = array( 'ID' => $id );
		if ( isset( $b['name'] ) ) {
			$upd['post_title'] = sanitize_text_field( (string) $b['name'] );
		}
		if ( isset( $b['isActive'] ) ) {
			$upd['post_status'] = $b['isActive'] ? 'publish' : 'draft';
		}
		wp_update_post( $upd );
		$this->apply_therapist_meta( $id, $b );
		return cb_response( array( 'id' => $id ) );
	}

	public function delete_therapist( WP_REST_Request $request ): WP_REST_Response {
		wp_delete_post( (int) $request['id'], true );
		return cb_response( null, 204 );
	}

	public function generate_slots( WP_REST_Request $request ): WP_REST_Response {
		$id    = (int) $request['id'];
		$b     = $request->get_json_params();
		$start = strtotime( (string) ( $b['windowStart'] ?? '' ) );
		$end   = strtotime( (string) ( $b['windowEnd'] ?? '' ) );
		$mins  = max( 15, (int) ( $b['slotMinutes'] ?? 45 ) );
		if ( ! $start || ! $end || $end <= $start ) {
			return cb_error( 'بازه‌ی زمانی نامعتبر است', 400, 'INVALID_WINDOW', 'api/admin/therapists' );
		}
		$existing = (string) get_post_meta( $id, 'cb_slots', true );
		$lines    = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $existing ) ) );
		$created  = 0;
		for ( $t = $start; $t + $mins * 60 <= $end; $t += $mins * 60 ) {
			$slot = gmdate( 'Y-m-d\TH:i', $t );
			if ( ! in_array( $slot, $lines, true ) ) {
				$lines[] = $slot;
				$created++;
			}
		}
		update_post_meta( $id, 'cb_slots', implode( "\n", $lines ) );
		return cb_response( array( 'created' => $created ), 201 );
	}

	public function appointments(): WP_REST_Response {
		$posts = get_posts( array( 'post_type' => 'cb_appointment', 'post_status' => 'publish', 'numberposts' => 300, 'orderby' => 'date', 'order' => 'DESC' ) );
		$out   = array();
		foreach ( $posts as $p ) {
			$tid  = (int) get_post_meta( $p->ID, 'cb_therapist_id', true );
			$slot = (string) get_post_meta( $p->ID, 'cb_slot', true );
			$ts   = strtotime( $slot ) ?: time();
			$out[] = array(
				'id'           => (int) $p->ID,
				'userId'       => (int) get_post_meta( $p->ID, 'cb_user_id', true ),
				'therapistId'  => $tid,
				'therapistName' => get_the_title( $tid ),
				'status'       => get_post_meta( $p->ID, 'cb_status', true ) ?: 'PENDING',
				'dayLabel'     => gmdate( 'Y-m-d', $ts ),
				'timeLabel'    => gmdate( 'H:i', $ts ),
				'videoRoomUrl' => get_post_meta( $p->ID, 'cb_video_url', true ) ?: null,
				'notes'        => get_post_meta( $p->ID, 'cb_notes', true ) ?: null,
				'mode'         => get_post_meta( $tid, 'cb_mode', true ) ?: 'ONLINE',
			);
		}
		return cb_response( $out );
	}

	public function confirm_appointment( WP_REST_Request $request ): WP_REST_Response {
		$id = (int) $request['id'];
		if ( get_post_type( $id ) !== 'cb_appointment' ) {
			return cb_error( 'نوبت یافت نشد', 404, 'NOT_FOUND', 'api/admin/therapists/appointments' );
		}
		$url = esc_url_raw( (string) ( $request->get_json_params()['videoRoomUrl'] ?? '' ) );
		update_post_meta( $id, 'cb_video_url', $url );
		update_post_meta( $id, 'cb_status', 'CONFIRMED' );
		return cb_response( null, 200 );
	}

	public function complete_appointment( WP_REST_Request $request ): WP_REST_Response {
		update_post_meta( (int) $request['id'], 'cb_status', 'COMPLETED' );
		return cb_response( null, 200 );
	}

	public function switch_requests(): WP_REST_Response {
		$users = get_users( array( 'meta_key' => 'cb_switch_requests', 'number' => 500 ) );
		$out   = array();
		foreach ( $users as $u ) {
			foreach ( (array) get_user_meta( $u->ID, 'cb_switch_requests', true ) as $r ) {
				$r['userId']   = (int) $u->ID;
				$r['userName'] = $u->display_name;
				$out[]         = $r;
			}
		}
		return cb_response( $out );
	}

	public function review_switch( WP_REST_Request $request ): WP_REST_Response {
		return $this->review_user_meta_list( 'cb_switch_requests', (int) $request['id'], $request, 'api/admin/clinic/switch-requests' );
	}

	// ---- psych tests --------------------------------------------------------

	public function list_tests(): WP_REST_Response {
		$posts = get_posts( array( 'post_type' => 'cb_psychtest', 'post_status' => array( 'publish', 'draft' ), 'numberposts' => 200 ) );
		$out   = array();
		foreach ( $posts as $p ) {
			$out[] = array( 'id' => (int) $p->ID, 'title' => get_the_title( $p ), 'slug' => $p->post_name, 'resultMode' => get_post_meta( $p->ID, 'cb_result_mode', true ) ?: 'AUTO', 'published' => $p->post_status === 'publish' );
		}
		return cb_response( $out );
	}

	public function create_test( WP_REST_Request $request ): WP_REST_Response {
		$b  = $request->get_json_params();
		$id = wp_insert_post( array(
			'post_type'    => 'cb_psychtest',
			'post_status'  => ( isset( $b['isPublished'] ) && ! $b['isPublished'] ) ? 'draft' : 'publish',
			'post_title'   => sanitize_text_field( (string) ( $b['title'] ?? 'تست' ) ),
			'post_name'    => sanitize_title( (string) ( $b['slug'] ?? '' ) ),
			'post_content' => wp_kses_post( (string) ( $b['description'] ?? '' ) ),
		) );
		if ( is_wp_error( $id ) ) {
			return cb_error( 'ساخت تست ناموفق بود', 400, 'CREATE_FAILED', 'api/admin/psych-tests' );
		}
		$this->apply_test_meta( $id, $b );
		return cb_response( array( 'id' => (int) $id ), 201 );
	}

	private function apply_test_meta( int $id, array $b ): void {
		if ( isset( $b['resultMode'] ) ) {
			update_post_meta( $id, 'cb_result_mode', strtoupper( (string) $b['resultMode'] ) === 'COUNSELOR' ? 'COUNSELOR' : 'AUTO' );
		}
		if ( isset( $b['price'] ) ) {
			update_post_meta( $id, 'cb_price', (float) $b['price'] );
		}
		if ( isset( $b['discountedPrice'] ) ) {
			update_post_meta( $id, 'cb_discounted_price', $b['discountedPrice'] !== null ? (float) $b['discountedPrice'] : '' );
		}
		if ( isset( $b['productId'] ) ) {
			update_post_meta( $id, 'cb_product_slug', $this->product_slug_from_id( $b['productId'] ) );
		}
		if ( isset( $b['questions'] ) && is_array( $b['questions'] ) ) {
			update_post_meta( $id, 'cb_questions', cb_build_test_question_lines( $b['questions'] ) );
		}
		if ( isset( $b['ranges'] ) && is_array( $b['ranges'] ) ) {
			update_post_meta( $id, 'cb_ranges', cb_build_range_lines( $b['ranges'] ) );
		}
	}

	public function update_test( WP_REST_Request $request ): WP_REST_Response {
		$id = (int) $request['id'];
		if ( get_post_type( $id ) !== 'cb_psychtest' ) {
			return cb_error( 'تست یافت نشد', 404, 'NOT_FOUND', 'api/admin/psych-tests' );
		}
		$b   = $request->get_json_params();
		$upd = array( 'ID' => $id );
		if ( isset( $b['title'] ) ) {
			$upd['post_title'] = sanitize_text_field( (string) $b['title'] );
		}
		if ( isset( $b['description'] ) ) {
			$upd['post_content'] = wp_kses_post( (string) $b['description'] );
		}
		if ( isset( $b['isPublished'] ) ) {
			$upd['post_status'] = $b['isPublished'] ? 'publish' : 'draft';
		}
		wp_update_post( $upd );
		$this->apply_test_meta( $id, $b );
		return cb_response( array( 'id' => $id ) );
	}

	public function delete_test( WP_REST_Request $request ): WP_REST_Response {
		wp_delete_post( (int) $request['id'], true );
		return cb_response( null, 204 );
	}

	public function pending_interpretations(): WP_REST_Response {
		$users = get_users( array( 'meta_key' => 'cb_psych_attempts', 'number' => 500 ) );
		$out   = array();
		foreach ( $users as $u ) {
			foreach ( (array) get_user_meta( $u->ID, 'cb_psych_attempts', true ) as $a ) {
				if ( ( $a['status'] ?? '' ) === 'AWAITING_INTERPRETATION' ) {
					$a['userId']   = (int) $u->ID;
					$a['userName'] = $u->display_name;
					$out[]         = $a;
				}
			}
		}
		return cb_response( $out );
	}

	public function interpret( WP_REST_Request $request ): WP_REST_Response {
		$attempt_id = (int) $request['uid'];
		$text       = sanitize_textarea_field( (string) ( $request->get_json_params()['interpretation'] ?? '' ) );
		$users      = get_users( array( 'meta_key' => 'cb_psych_attempts', 'number' => 500 ) );
		foreach ( $users as $u ) {
			$list    = (array) get_user_meta( $u->ID, 'cb_psych_attempts', true );
			$changed = false;
			foreach ( $list as &$a ) {
				if ( (int) $a['id'] === $attempt_id ) {
					$a['interpretation'] = $text;
					$a['status']         = 'COMPLETED';
					$changed             = true;
				}
			}
			unset( $a );
			if ( $changed ) {
				update_user_meta( $u->ID, 'cb_psych_attempts', $list );
				return cb_response( null, 200 );
			}
		}
		return cb_error( 'آزمون یافت نشد', 404, 'NOT_FOUND', 'api/admin/psych-tests' );
	}

	// ---- stories ------------------------------------------------------------

	public function admin_stories(): WP_REST_Response {
		$posts = get_posts( array( 'post_type' => 'cb_story', 'post_status' => array( 'publish', 'draft' ), 'numberposts' => 100 ) );
		$out   = array();
		foreach ( $posts as $p ) {
			$out[] = array(
				'id'        => (int) $p->ID,
				'mediaUrl'  => get_post_meta( $p->ID, 'media_url', true ) ?: '',
				'mediaType' => get_post_meta( $p->ID, 'media_type', true ) ?: 'IMAGE',
				'linkType'  => get_post_meta( $p->ID, 'link_type', true ) ?: 'NONE',
				'productId' => ( $pid = (int) get_post_meta( $p->ID, 'product_id', true ) ) ? $pid : null,
				'title'     => get_the_title( $p ) ?: null,
				'createdAt' => cb_iso( $p->post_date_gmt ),
			);
		}
		return cb_response( $out );
	}

	public function create_story( WP_REST_Request $request ): WP_REST_Response {
		$b  = $request->get_json_params();
		$id = wp_insert_post( array(
			'post_type'   => 'cb_story',
			'post_status' => 'publish',
			'post_title'  => sanitize_text_field( (string) ( $b['title'] ?? 'استوری' ) ),
		) );
		if ( is_wp_error( $id ) ) {
			return cb_error( 'ساخت استوری ناموفق بود', 400, 'CREATE_FAILED', 'api/admin/stories' );
		}
		$this->apply_story_meta( $id, $b );
		return cb_response( array( 'id' => (int) $id ), 201 );
	}

	private function apply_story_meta( int $id, array $b ): void {
		if ( isset( $b['mediaUrl'] ) ) {
			update_post_meta( $id, 'media_url', esc_url_raw( (string) $b['mediaUrl'] ) );
		}
		if ( isset( $b['mediaType'] ) ) {
			update_post_meta( $id, 'media_type', sanitize_text_field( (string) $b['mediaType'] ) );
		}
		if ( isset( $b['linkType'] ) ) {
			update_post_meta( $id, 'link_type', sanitize_text_field( (string) $b['linkType'] ) );
		}
		if ( isset( $b['productId'] ) ) {
			update_post_meta( $id, 'product_id', (int) $b['productId'] );
		}
		if ( isset( $b['expiresAt'] ) ) {
			update_post_meta( $id, 'expires_at', sanitize_text_field( (string) $b['expiresAt'] ) );
		}
	}

	public function update_story( WP_REST_Request $request ): WP_REST_Response {
		$id = (int) $request['id'];
		if ( get_post_type( $id ) !== 'cb_story' ) {
			return cb_error( 'استوری یافت نشد', 404, 'NOT_FOUND', 'api/admin/stories' );
		}
		$b = $request->get_json_params();
		if ( isset( $b['title'] ) ) {
			wp_update_post( array( 'ID' => $id, 'post_title' => sanitize_text_field( (string) $b['title'] ) ) );
		}
		$this->apply_story_meta( $id, $b );
		return cb_response( array( 'id' => $id ) );
	}

	public function delete_story( WP_REST_Request $request ): WP_REST_Response {
		wp_delete_post( (int) $request['id'], true );
		return cb_response( null, 204 );
	}

	// ---- shared review helper (user-meta lists with approve/reject) ---------

	private function review_user_meta_list( string $key, int $id, WP_REST_Request $request, string $path ): WP_REST_Response {
		$b        = $request->get_json_params();
		$approve  = ! empty( $b['approve'] );
		$note     = isset( $b['adminNote'] ) ? sanitize_text_field( (string) $b['adminNote'] ) : null;
		$users    = get_users( array( 'meta_key' => $key, 'number' => 500 ) );
		foreach ( $users as $u ) {
			$list    = (array) get_user_meta( $u->ID, $key, true );
			$changed = false;
			foreach ( $list as &$r ) {
				if ( (int) $r['id'] === $id ) {
					$r['status']     = $approve ? 'APPROVED' : 'REJECTED';
					$r['adminNote']  = $note;
					$r['resolvedAt'] = gmdate( 'c' );
					$changed         = true;
				}
			}
			unset( $r );
			if ( $changed ) {
				update_user_meta( $u->ID, $key, $list );
				return cb_response( null, 200 );
			}
		}
		return cb_error( 'یافت نشد', 404, 'NOT_FOUND', $path );
	}
}
