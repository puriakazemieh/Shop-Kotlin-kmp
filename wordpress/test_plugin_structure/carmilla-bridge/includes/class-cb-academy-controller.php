<?php
/**
 * Academy endpoints — courses, lessons, enrollment, progress, quiz, certificate,
 * placement, project assessment, refunds. Backed by the cb_course CPT with a
 * line-based lesson model in meta (cb_lessons), aligned with the Carmilla theme
 * so a single site can run both and share data.
 *
 * Lesson ids are synthetic and stable: courseId * 100000 + (index + 1).
 * Progress / certificates / submissions live in user meta + options, matching
 * the theme's keys (cb_course_prog_{id}, cb_certs, cb_cert_index).
 *
 * Public reads:
 *   GET  api/courses                                   -> List<CourseSummaryResponse>
 *   GET  api/courses/{slug}                            -> CourseDetailResponse
 *   GET  api/courses/certificates/verify/{certNumber}  -> CertificateVerifyResponse
 * Auth:
 *   GET  api/academy/my-courses                        -> List<CourseSummaryResponse>
 *   POST api/academy/courses/{id}/enroll               -> CourseDetailResponse
 *   POST api/academy/courses/{id}/waitlist             -> WaitlistResponse
 *   POST api/academy/lessons/{lessonId}/progress       -> ProgressResponse
 *   GET  api/academy/courses/{id}/quiz                 -> QuizResponse
 *   POST api/academy/courses/{id}/quiz/submit          -> QuizResultResponse
 *   GET  api/academy/lessons/{lessonId}/quiz           -> LessonQuizResponse
 *   POST api/academy/lessons/{lessonId}/quiz/submit    -> LessonQuizResultResponse
 *   GET  api/academy/certificates                      -> List<CertificateResponse>
 *   GET/POST api/academy/placement-quiz[/submit]
 *   POST api/academy/courses/{id}/project[/link]       -> ProjectSubmissionResponse
 *   GET  api/academy/courses/{id}/project              -> MyProjectResponse
 *   GET  api/academy/courses/{id}/project/peers        -> List<ProjectSubmissionResponse>
 *   GET/POST api/academy/project/{submissionId}/comments
 *   GET  api/academy/lessons/{lessonId}/questions ; POST same
 *   POST api/academy/courses/{id}/mark-update-seen
 *   POST api/academy/courses/{id}/refund-request ; GET api/academy/refund-requests/mine
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Academy_Controller {

	const LESSON_BASE = 100000;

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$pub   = '__return_true';
		$login = array( 'CB_Plugin', 'require_login' );

		register_rest_route( $ns, '/api/courses', array( 'methods' => 'GET', 'callback' => array( $this, 'list_courses' ), 'permission_callback' => $pub ) );
		register_rest_route( $ns, '/api/courses/certificates/verify/(?P<num>[A-Za-z0-9\-]+)', array( 'methods' => 'GET', 'callback' => array( $this, 'verify_certificate' ), 'permission_callback' => $pub ) );
		register_rest_route( $ns, '/api/courses/(?P<slug>[a-zA-Z0-9\-_%]+)', array( 'methods' => 'GET', 'callback' => array( $this, 'get_course' ), 'permission_callback' => $pub ) );

		register_rest_route( $ns, '/api/academy/my-courses', array( 'methods' => 'GET', 'callback' => array( $this, 'my_courses' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/academy/courses/(?P<id>\d+)/enroll', array( 'methods' => 'POST', 'callback' => array( $this, 'enroll' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/academy/courses/(?P<id>\d+)/waitlist', array( 'methods' => 'POST', 'callback' => array( $this, 'waitlist' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/academy/courses/(?P<id>\d+)/quiz', array( 'methods' => 'GET', 'callback' => array( $this, 'get_quiz' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/academy/courses/(?P<id>\d+)/quiz/submit', array( 'methods' => 'POST', 'callback' => array( $this, 'submit_quiz' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/academy/courses/(?P<id>\d+)/mark-update-seen', array( 'methods' => 'POST', 'callback' => array( $this, 'mark_update_seen' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/academy/courses/(?P<id>\d+)/project', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'my_project' ), 'permission_callback' => $login ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'submit_project_file' ), 'permission_callback' => $login ),
		) );
		register_rest_route( $ns, '/api/academy/courses/(?P<id>\d+)/project/link', array( 'methods' => 'POST', 'callback' => array( $this, 'submit_project_link' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/academy/courses/(?P<id>\d+)/project/peers', array( 'methods' => 'GET', 'callback' => array( $this, 'peer_submissions' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/academy/courses/(?P<id>\d+)/refund-request', array( 'methods' => 'POST', 'callback' => array( $this, 'refund_request' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/academy/refund-requests/mine', array( 'methods' => 'GET', 'callback' => array( $this, 'refund_mine' ), 'permission_callback' => $login ) );

		register_rest_route( $ns, '/api/academy/lessons/(?P<lid>\d+)/progress', array( 'methods' => 'POST', 'callback' => array( $this, 'lesson_progress' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/academy/lessons/(?P<lid>\d+)/quiz', array( 'methods' => 'GET', 'callback' => array( $this, 'lesson_quiz' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/academy/lessons/(?P<lid>\d+)/quiz/submit', array( 'methods' => 'POST', 'callback' => array( $this, 'lesson_quiz_submit' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/academy/lessons/(?P<lid>\d+)/questions', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'lesson_questions' ), 'permission_callback' => '__return_true' ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'create_lesson_question' ), 'permission_callback' => $login ),
		) );

		register_rest_route( $ns, '/api/academy/project/(?P<sid>\d+)/comments', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'peer_comments' ), 'permission_callback' => $login ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'add_peer_comment' ), 'permission_callback' => $login ),
		) );

		register_rest_route( $ns, '/api/academy/certificates', array( 'methods' => 'GET', 'callback' => array( $this, 'certificates' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/academy/placement-quiz', array( 'methods' => 'GET', 'callback' => array( $this, 'placement_quiz' ), 'permission_callback' => '__return_true' ) );
		register_rest_route( $ns, '/api/academy/placement-quiz/submit', array( 'methods' => 'POST', 'callback' => array( $this, 'placement_submit' ), 'permission_callback' => '__return_true' ) );
	}

	// ---- course model helpers ----------------------------------------------

	private function lessons( int $course_id ): array {
		$raw   = (string) get_post_meta( $course_id, 'cb_lessons', true );
		$lines = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $raw ) ) );
		$out   = array();
		foreach ( $lines as $line ) {
			$p     = array_map( 'trim', explode( '|', $line ) );
			$out[] = array(
				'title' => $p[0] ?? '',
				'url'   => isset( $p[1] ) ? esc_url_raw( $p[1] ) : '',
				'free'  => isset( $p[2] ) && in_array( strtolower( $p[2] ), array( 'free', 'رایگان', '1', 'true' ), true ),
			);
		}
		return $out;
	}

	private function product_id_for( int $course_id ): int {
		$slug = get_post_meta( $course_id, 'cb_product_slug', true );
		if ( ! $slug ) {
			return 0;
		}
		$product = get_page_by_path( $slug, OBJECT, 'product' );
		return $product ? (int) $product->ID : 0;
	}

	private function accessible( int $course_id, int $user_id ): bool {
		$slug = get_post_meta( $course_id, 'cb_product_slug', true );
		if ( ! $slug || ! function_exists( 'wc_customer_bought_product' ) ) {
			return true; // free course
		}
		if ( ! $user_id ) {
			return false;
		}
		$pid = $this->product_id_for( $course_id );
		return $pid ? wc_customer_bought_product( '', $user_id, $pid ) : true;
	}

	private function completed_indices( int $course_id, int $user_id ): array {
		if ( ! $user_id ) {
			return array();
		}
		$done = get_user_meta( $user_id, "cb_course_prog_$course_id", true );
		return is_array( $done ) ? array_map( 'intval', $done ) : array();
	}

	private function percent( int $course_id, int $user_id ): int {
		$total = count( $this->lessons( $course_id ) );
		if ( ! $total ) {
			return 0;
		}
		return (int) round( count( $this->completed_indices( $course_id, $user_id ) ) / $total * 100 );
	}

	private function enrolled( int $course_id, int $user_id ): bool {
		if ( ! $user_id ) {
			return false;
		}
		$slug = get_post_meta( $course_id, 'cb_product_slug', true );
		if ( $slug ) {
			// Paid course: enrolled once the linked product is purchased.
			return $this->accessible( $course_id, $user_id );
		}
		// Free course: enrolled once a progress record exists (seeded on enroll).
		return is_array( get_user_meta( $user_id, "cb_course_prog_$course_id", true ) );
	}

	private function price( int $course_id ): float {
		return (float) get_post_meta( $course_id, 'cb_price', true );
	}

	private function discounted( int $course_id ) {
		$d = get_post_meta( $course_id, 'cb_discounted_price', true );
		return ( $d !== '' && $d !== null ) ? (float) $d : null;
	}

	// ---- quiz parsing (question? | correct* | opt | opt) --------------------

	private function quiz_questions( int $course_id, bool $reveal ): array {
		return self::parse_quiz_lines( (string) get_post_meta( $course_id, 'cb_quiz', true ), $reveal );
	}

	/**
	 * Parse a line-based quiz. Each line: «question? | opt | opt* | opt», where a
	 * trailing * or ✓ marks the correct option. Returns questions with an
	 * internal _correct index; option `correct` flags are only exposed when
	 * $reveal is true (server-side scoring never leaks answers to the client).
	 */
	public static function parse_quiz_lines( string $raw, bool $reveal ): array {
		$lines = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $raw ) ) );
		$out   = array();
		$i     = 0;
		foreach ( $lines as $line ) {
			$parts = array_map( 'trim', explode( '|', $line ) );
			$text  = array_shift( $parts );
			if ( $text === '' || empty( $parts ) ) {
				continue;
			}
			$options = array();
			$correct = null;
			foreach ( $parts as $oi => $opt ) {
				$is_correct = ( strpos( $opt, '*' ) !== false ) || ( strpos( $opt, '✓' ) !== false );
				if ( $is_correct ) {
					$correct = $oi;
				}
				$clean     = trim( str_replace( array( '*', '✓' ), '', $opt ) );
				$options[] = array( 'text' => $clean, 'correct' => $reveal ? ( $is_correct ? true : false ) : null );
			}
			$out[] = array( 'index' => $i, 'text' => $text, 'options' => $options, '_correct' => $correct );
			$i++;
		}
		return $out;
	}

	/** Placement total score -> level/label (0-2 مبتدی, 3-4 متوسط, 5+ پیشرفته). */
	public static function placement_level( int $total ): array {
		if ( $total <= 2 ) {
			return array( 'level' => 'beginner', 'label' => 'مبتدی' );
		}
		if ( $total <= 4 ) {
			return array( 'level' => 'intermediate', 'label' => 'متوسط' );
		}
		return array( 'level' => 'advanced', 'label' => 'پیشرفته' );
	}

	private function pass_score( int $course_id ): int {
		$s = (int) get_post_meta( $course_id, 'cb_pass_score', true );
		return $s > 0 ? $s : 60;
	}

	// ---- certificate (theme-compatible number + storage) --------------------

	public static function cert_number( int $course_id, int $user_id ): string {
		return 'CB-' . strtoupper( substr( md5( $course_id . '-' . $user_id . '-' . wp_salt( 'auth' ) ), 0, 10 ) );
	}

	private function issue_certificate( int $course_id, int $user_id ): string {
		$number = self::cert_number( $course_id, $user_id );
		$certs  = get_user_meta( $user_id, 'cb_certs', true );
		$certs  = is_array( $certs ) ? $certs : array();
		foreach ( $certs as $c ) {
			if ( ( $c['certNumber'] ?? '' ) === $number ) {
				return $number; // already issued
			}
		}
		$entry = array(
			'id'         => count( $certs ) + 1,
			'courseId'   => $course_id,
			'courseTitle' => get_the_title( $course_id ),
			'certNumber' => $number,
			'issuedAt'   => gmdate( 'c' ),
			'userName'   => wp_get_current_user()->display_name,
		);
		$certs[] = $entry;
		update_user_meta( $user_id, 'cb_certs', $certs );

		$index            = get_option( 'cb_cert_index', array() );
		$index[ $number ] = array( 'courseTitle' => $entry['courseTitle'], 'issuedAt' => $entry['issuedAt'] );
		update_option( 'cb_cert_index', $index, false );
		return $number;
	}

	// ---- DTO shaping --------------------------------------------------------

	private function summary_dto( WP_Post $course, int $user_id ): array {
		$id      = (int) $course->ID;
		$lessons = $this->lessons( $id );
		$done    = $this->completed_indices( $id, $user_id );
		return array(
			'id'              => $id,
			'title'           => get_the_title( $course ),
			'slug'            => $course->post_name,
			'thumbnailUrl'    => get_the_post_thumbnail_url( $course, 'large' ) ?: null,
			'instructor'      => get_post_meta( $id, 'cb_instructor', true ) ?: null,
			'price'           => $this->price( $id ),
			'discountedPrice' => $this->discounted( $id ),
			'lessonCount'     => count( $lessons ),
			'completedLessons' => count( $done ),
			'progressPercent' => $this->percent( $id, $user_id ),
			'enrolled'        => $this->enrolled( $id, $user_id ),
			'courseType'      => 'COURSE',
			'format'          => get_post_meta( $id, 'cb_format', true ) ?: 'ONLINE_RECORDED',
			'online'          => true,
			'level'           => get_post_meta( $id, 'cb_level', true ) ?: null,
		);
	}

	/** Public accessor so the admin controller can return CourseDetailResponse. */
	public function detail_by_id( int $course_id, int $user_id ) {
		$course = get_post( $course_id );
		if ( ! $course || $course->post_type !== 'cb_course' ) {
			return null;
		}
		return $this->detail_dto( $course, $user_id );
	}

	private function detail_dto( WP_Post $course, int $user_id ): array {
		$id        = (int) $course->ID;
		$lessons   = $this->lessons( $id );
		$done      = $this->completed_indices( $id, $user_id );
		$access    = $this->accessible( $id, $user_id );
		$capacity  = (int) get_post_meta( $id, 'cb_capacity', true );
		$seats     = (int) get_option( "cb_course_seats_$id", 0 );
		$duration  = 0;
		$lesson_dtos = array();
		foreach ( $lessons as $idx => $l ) {
			$open = $l['free'] || $access;
			$files = (array) get_post_meta( $id, 'cb_lesson_files_' . $idx, true );
			$quiz  = get_post_meta( $id, 'cb_lesson_quiz_' . $idx, true );
			$lesson_dtos[] = array(
				'id'                 => self::lesson_id( $id, $idx ),
				'title'              => $l['title'],
				'durationSeconds'    => 0,
				'freePreview'        => $l['free'],
				'videoUrl'           => $open ? ( $l['url'] ?: null ) : null,
				'completed'          => in_array( $idx, $done, true ),
				'lastPositionSeconds' => 0,
				'resourceFiles'      => $open ? array_values( $files ) : array(),
				'hasQuiz'            => is_array( $quiz ) && ! empty( $quiz['questions'] ),
			);
		}
		$syllabus_skills = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', (string) get_post_meta( $id, 'cb_syllabus', true ) ) ) );

		return array(
			'id'            => $id,
			'title'         => get_the_title( $course ),
			'slug'          => $course->post_name,
			'description'   => apply_filters( 'the_content', $course->post_content ) ?: null,
			'thumbnailUrl'  => get_the_post_thumbnail_url( $course, 'large' ) ?: null,
			'instructor'    => get_post_meta( $id, 'cb_instructor', true ) ?: null,
			'price'         => $this->price( $id ),
			'discountedPrice' => $this->discounted( $id ),
			'enrolled'      => $this->enrolled( $id, $user_id ),
			'progressPercent' => $this->percent( $id, $user_id ),
			'sections'      => array( array( 'id' => 1, 'title' => 'سرفصل‌ها', 'lessons' => $lesson_dtos ) ),
			'courseType'    => 'COURSE',
			'format'        => get_post_meta( $id, 'cb_format', true ) ?: 'ONLINE_RECORDED',
			'online'        => true,
			'level'         => get_post_meta( $id, 'cb_level', true ) ?: null,
			'capacity'      => $capacity ?: null,
			'seatsTaken'    => $seats,
			'seatsRemaining' => $capacity ? max( 0, $capacity - $seats ) : null,
			'instructorBio' => get_post_meta( $id, 'cb_instructor_bio', true ) ?: null,
			'instructorSkills' => array_values( $syllabus_skills ),
			'full'          => $capacity ? ( $seats >= $capacity ) : false,
			'onWaitlist'    => $user_id ? in_array( $user_id, (array) get_option( "cb_course_waitlist_$id", array() ), true ) : false,
			'productId'     => $this->product_id_for( $id ) ?: null,
			'requiresProjectSubmission' => (bool) get_post_meta( $id, 'cb_requires_project', true ),
			'totalDurationSeconds' => $duration,
			'hasUnseenUpdate' => false,
		);
	}

	public static function lesson_id( int $course_id, int $index ): int {
		return $course_id * self::LESSON_BASE + ( $index + 1 );
	}

	public static function decode_lesson( int $lesson_id ): array {
		$course_id = intdiv( $lesson_id, self::LESSON_BASE );
		$index     = ( $lesson_id % self::LESSON_BASE ) - 1;
		return array( $course_id, $index );
	}

	// ---- endpoints ----------------------------------------------------------

	public function list_courses(): WP_REST_Response {
		$uid   = get_current_user_id();
		$posts = get_posts( array( 'post_type' => 'cb_course', 'post_status' => 'publish', 'numberposts' => 100, 'orderby' => 'date', 'order' => 'DESC' ) );
		$out   = array();
		foreach ( $posts as $c ) {
			$out[] = $this->summary_dto( $c, $uid );
		}
		return cb_response( $out );
	}

	public function get_course( WP_REST_Request $request ): WP_REST_Response {
		$course = get_page_by_path( sanitize_title( $request['slug'] ), OBJECT, 'cb_course' );
		if ( ! $course ) {
			return cb_error( 'دوره یافت نشد', 404, 'NOT_FOUND', 'api/courses' );
		}
		return cb_response( $this->detail_dto( $course, get_current_user_id() ) );
	}

	public function my_courses(): WP_REST_Response {
		$uid   = get_current_user_id();
		$posts = get_posts( array( 'post_type' => 'cb_course', 'post_status' => 'publish', 'numberposts' => 100 ) );
		$out   = array();
		foreach ( $posts as $c ) {
			if ( $this->enrolled( (int) $c->ID, $uid ) ) {
				$out[] = $this->summary_dto( $c, $uid );
			}
		}
		return cb_response( $out );
	}

	public function enroll( WP_REST_Request $request ): WP_REST_Response {
		$id     = (int) $request['id'];
		$course = get_post( $id );
		if ( ! $course || $course->post_type !== 'cb_course' ) {
			return cb_error( 'دوره یافت نشد', 404, 'NOT_FOUND', 'api/academy' );
		}
		$uid = get_current_user_id();
		// Free course: mark enrolled by seeding an (empty) progress record.
		if ( ! get_post_meta( $id, 'cb_product_slug', true ) ) {
			$done = get_user_meta( $uid, "cb_course_prog_$id", true );
			if ( ! is_array( $done ) ) {
				update_user_meta( $uid, "cb_course_prog_$id", array() );
			}
		}
		return cb_response( $this->detail_dto( $course, $uid ) );
	}

	public function lesson_progress( WP_REST_Request $request ): WP_REST_Response {
		list( $course_id, $index ) = self::decode_lesson( (int) $request['lid'] );
		$course = get_post( $course_id );
		if ( ! $course || $course->post_type !== 'cb_course' ) {
			return cb_error( 'درس یافت نشد', 404, 'NOT_FOUND', 'api/academy/lessons' );
		}
		$uid       = get_current_user_id();
		$completed = $request->get_json_params()['completed'] ?? null;
		$done      = $this->completed_indices( $course_id, $uid );
		if ( $completed === true && ! in_array( $index, $done, true ) ) {
			$done[] = $index;
		} elseif ( $completed === false ) {
			$done = array_values( array_diff( $done, array( $index ) ) );
		}
		update_user_meta( $uid, "cb_course_prog_$course_id", array_values( $done ) );
		$total = count( $this->lessons( $course_id ) );
		return cb_response( array(
			'courseId'         => $course_id,
			'totalLessons'     => $total,
			'completedLessons' => count( $done ),
			'progressPercent'  => $total ? (int) round( count( $done ) / $total * 100 ) : 0,
		) );
	}

	public function get_quiz( WP_REST_Request $request ): WP_REST_Response {
		$id        = (int) $request['id'];
		$questions = $this->quiz_questions( $id, false );
		$uid       = get_current_user_id();
		$passed    = (bool) get_user_meta( $uid, "cb_quiz_passed_$id", true );
		foreach ( $questions as &$q ) {
			unset( $q['_correct'] );
		}
		unset( $q );
		return cb_response( array(
			'courseId'      => $id,
			'title'         => 'آزمونِ پایانِ دوره',
			'passScore'     => $this->pass_score( $id ),
			'questions'     => $questions,
			'alreadyPassed' => $passed,
			'hasQuiz'       => ! empty( $questions ),
		) );
	}

	public function submit_quiz( WP_REST_Request $request ): WP_REST_Response {
		$id        = (int) $request['id'];
		$uid       = get_current_user_id();
		$answers   = (array) ( $request->get_json_params()['answers'] ?? array() );
		$questions = $this->quiz_questions( $id, true );
		if ( empty( $questions ) ) {
			return cb_error( 'این دوره آزمون ندارد', 400, 'NO_QUIZ', 'api/academy' );
		}
		$correct = 0;
		foreach ( $questions as $q ) {
			$given = $answers[ (string) $q['index'] ] ?? ( $answers[ $q['index'] ] ?? null );
			if ( $given !== null && (int) $given === (int) $q['_correct'] ) {
				$correct++;
			}
		}
		$score     = (int) round( $correct / count( $questions ) * 100 );
		$pass      = $this->pass_score( $id );
		$passed    = $score >= $pass;
		$cert      = null;
		if ( $passed ) {
			update_user_meta( $uid, "cb_quiz_passed_$id", 1 );
			$cert = $this->issue_certificate( $id, $uid );
		}
		return cb_response( array(
			'courseId'          => $id,
			'score'             => $score,
			'passed'            => $passed,
			'passScore'         => $pass,
			'certificateNumber' => $cert,
		) );
	}

	public function lesson_quiz( WP_REST_Request $request ): WP_REST_Response {
		$lid = (int) $request['lid'];
		list( $course_id, $index ) = self::decode_lesson( $lid );
		$stored = get_post_meta( $course_id, 'cb_lesson_quiz_' . $index, true );
		$questions = ( is_array( $stored ) && ! empty( $stored['questions'] ) )
			? self::parse_quiz_lines( (string) $stored['questions'], false ) : array();
		foreach ( $questions as &$q ) {
			unset( $q['_correct'] );
		}
		unset( $q );
		return cb_response( array(
			'lessonId'      => $lid,
			'title'         => is_array( $stored ) ? ( $stored['title'] ?? 'آزمونِ این درس' ) : 'آزمونِ این درس',
			'passScore'     => is_array( $stored ) ? (int) ( $stored['passScore'] ?? 60 ) : 60,
			'questions'     => $questions,
			'alreadyPassed' => (bool) get_user_meta( get_current_user_id(), "cb_lquiz_passed_{$course_id}_{$index}", true ),
		) );
	}

	public function lesson_quiz_submit( WP_REST_Request $request ): WP_REST_Response {
		$lid = (int) $request['lid'];
		list( $course_id, $index ) = self::decode_lesson( $lid );
		$stored    = get_post_meta( $course_id, 'cb_lesson_quiz_' . $index, true );
		$questions = ( is_array( $stored ) && ! empty( $stored['questions'] ) )
			? self::parse_quiz_lines( (string) $stored['questions'], true ) : array();
		$answers = (array) ( $request->get_json_params()['answers'] ?? array() );
		$correct = 0;
		foreach ( $questions as $q ) {
			$given = $answers[ (string) $q['index'] ] ?? ( $answers[ $q['index'] ] ?? null );
			if ( $given !== null && (int) $given === (int) $q['_correct'] ) {
				$correct++;
			}
		}
		$pass   = is_array( $stored ) ? (int) ( $stored['passScore'] ?? 60 ) : 60;
		$score  = $questions ? (int) round( $correct / count( $questions ) * 100 ) : 0;
		$passed = $score >= $pass;
		if ( $passed ) {
			update_user_meta( get_current_user_id(), "cb_lquiz_passed_{$course_id}_{$index}", 1 );
		}
		return cb_response( array( 'lessonId' => $lid, 'score' => $score, 'passed' => $passed, 'passScore' => $pass ) );
	}

	public function certificates(): WP_REST_Response {
		$certs = get_user_meta( get_current_user_id(), 'cb_certs', true );
		$certs = is_array( $certs ) ? $certs : array();
		$out   = array();
		foreach ( $certs as $c ) {
			$out[] = array(
				'id'          => (int) ( $c['id'] ?? 0 ),
				'courseId'    => (int) ( $c['courseId'] ?? 0 ),
				'courseTitle' => (string) ( $c['courseTitle'] ?? '' ),
				'certNumber'  => (string) ( $c['certNumber'] ?? '' ),
				'issuedAt'    => (string) ( $c['issuedAt'] ?? gmdate( 'c' ) ),
				'userName'    => $c['userName'] ?? null,
			);
		}
		return cb_response( $out );
	}

	public function verify_certificate( WP_REST_Request $request ): WP_REST_Response {
		$number = (string) $request['num'];
		$index  = get_option( 'cb_cert_index', array() );
		if ( isset( $index[ $number ] ) ) {
			return cb_response( array(
				'valid'       => true,
				'courseTitle' => $index[ $number ]['courseTitle'] ?? null,
				'certNumber'  => $number,
				'issuedAt'    => $index[ $number ]['issuedAt'] ?? null,
			) );
		}
		return cb_response( array( 'valid' => false, 'courseTitle' => null, 'certNumber' => $number, 'issuedAt' => null ) );
	}

	public function waitlist( WP_REST_Request $request ): WP_REST_Response {
		$id   = (int) $request['id'];
		$uid  = get_current_user_id();
		$key  = "cb_course_waitlist_$id";
		$list = (array) get_option( $key, array() );
		if ( ! in_array( $uid, $list, true ) ) {
			$list[] = $uid;
			update_option( $key, $list, false );
		}
		return cb_response( array(
			'courseId' => $id,
			'joined'   => true,
			'position' => array_search( $uid, array_values( $list ), true ) + 1,
		) );
	}

	public function mark_update_seen( WP_REST_Request $request ): WP_REST_Response {
		update_user_meta( get_current_user_id(), 'cb_course_seen_' . (int) $request['id'], gmdate( 'c' ) );
		return cb_response( null, 200 );
	}

	// ---- placement quiz -----------------------------------------------------

	private function placement_defaults(): array {
		return array(
			array( 'id' => 1, 'text' => 'چقدر با مفاهیمِ پایه آشنا هستید؟', 'options' => array(
				array( 'label' => 'تازه‌کار', 'score' => 0 ), array( 'label' => 'کمی', 'score' => 1 ), array( 'label' => 'زیاد', 'score' => 2 ) ) ),
			array( 'id' => 2, 'text' => 'تجربه‌ی عملیِ شما چقدر است؟', 'options' => array(
				array( 'label' => 'هیچ', 'score' => 0 ), array( 'label' => 'چند پروژه', 'score' => 1 ), array( 'label' => 'حرفه‌ای', 'score' => 2 ) ) ),
			array( 'id' => 3, 'text' => 'هدفِ شما از این دوره چیست؟', 'options' => array(
				array( 'label' => 'شروع', 'score' => 0 ), array( 'label' => 'تقویت', 'score' => 1 ), array( 'label' => 'تخصص', 'score' => 2 ) ) ),
		);
	}

	public function placement_quiz(): WP_REST_Response {
		return cb_response( array( 'questions' => $this->placement_defaults() ) );
	}

	public function placement_submit( WP_REST_Request $request ): WP_REST_Response {
		$answers = (array) ( $request->get_json_params()['answers'] ?? array() );
		$total = array_sum( array_map( "intval", $answers ) );
		return cb_response( self::placement_level( $total ) );
	}

	// ---- project assessment (option-backed submissions) ---------------------

	private function submissions(): array {
		$s = get_option( 'cb_submissions', array() );
		return is_array( $s ) ? $s : array();
	}

	private function submission_dto( array $s ): array {
		return array(
			'id'             => (int) $s['id'],
			'courseId'       => (int) $s['courseId'],
			'userId'         => (int) $s['userId'],
			'fileUrl'        => (string) $s['fileUrl'],
			'note'           => $s['note'] ?? null,
			'status'         => (string) ( $s['status'] ?? 'PENDING' ),
			'mentorFeedback' => $s['mentorFeedback'] ?? null,
			'submittedAt'    => (string) ( $s['submittedAt'] ?? gmdate( 'c' ) ),
			'reviewedAt'     => $s['reviewedAt'] ?? null,
			'userName'       => $s['userName'] ?? null,
		);
	}

	private function store_submission( int $course_id, int $uid, string $file_url, $note ) {
		$all = $this->submissions();
		// One submission per user per course (upsert).
		foreach ( $all as &$s ) {
			if ( (int) $s['courseId'] === $course_id && (int) $s['userId'] === $uid ) {
				$s['fileUrl']     = $file_url;
				$s['note']        = $note;
				$s['status']      = 'PENDING';
				$s['submittedAt'] = gmdate( 'c' );
				update_option( 'cb_submissions', $all, false );
				return $s;
			}
		}
		unset( $s );
		$seq   = (int) get_option( 'cb_submission_seq', 0 ) + 1;
		update_option( 'cb_submission_seq', $seq, false );
		$entry = array(
			'id' => $seq, 'courseId' => $course_id, 'userId' => $uid, 'fileUrl' => $file_url,
			'note' => $note, 'status' => 'PENDING', 'mentorFeedback' => null,
			'submittedAt' => gmdate( 'c' ), 'reviewedAt' => null, 'userName' => wp_get_current_user()->display_name,
		);
		$all[] = $entry;
		update_option( 'cb_submissions', $all, false );
		return $entry;
	}

	public function submit_project_link( WP_REST_Request $request ): WP_REST_Response {
		$id  = (int) $request['id'];
		$body = $request->get_json_params();
		$url  = esc_url_raw( (string) ( $body['fileUrl'] ?? '' ) );
		if ( ! $url ) {
			return cb_error( 'لینکِ فایل نامعتبر است', 400, 'INVALID_URL', 'api/academy' );
		}
		return cb_response( $this->submission_dto( $this->store_submission( $id, get_current_user_id(), $url, isset( $body['note'] ) ? sanitize_text_field( (string) $body['note'] ) : null ) ), 201 );
	}

	public function submit_project_file( WP_REST_Request $request ): WP_REST_Response {
		$id = (int) $request['id'];
		if ( empty( $_FILES['file'] ) ) {
			return cb_error( 'فایل ارسال نشد', 400, 'NO_FILE', 'api/academy' );
		}
		require_once ABSPATH . 'wp-admin/includes/file.php';
		require_once ABSPATH . 'wp-admin/includes/media.php';
		require_once ABSPATH . 'wp-admin/includes/image.php';
		$attachment_id = media_handle_upload( 'file', 0 );
		if ( is_wp_error( $attachment_id ) ) {
			return cb_error( 'آپلود ناموفق بود', 400, 'UPLOAD_FAILED', 'api/academy' );
		}
		$url  = wp_get_attachment_url( $attachment_id );
		$note = $request->get_param( 'note' );
		return cb_response( $this->submission_dto( $this->store_submission( $id, get_current_user_id(), (string) $url, $note ? sanitize_text_field( (string) $note ) : null ) ), 201 );
	}

	public function my_project( WP_REST_Request $request ): WP_REST_Response {
		$id  = (int) $request['id'];
		$uid = get_current_user_id();
		foreach ( $this->submissions() as $s ) {
			if ( (int) $s['courseId'] === $id && (int) $s['userId'] === $uid ) {
				return cb_response( array( 'found' => true, 'submission' => $this->submission_dto( $s ) ) );
			}
		}
		return cb_response( array( 'found' => false, 'submission' => null ) );
	}

	public function peer_submissions( WP_REST_Request $request ): WP_REST_Response {
		$id  = (int) $request['id'];
		$uid = get_current_user_id();
		$out = array();
		foreach ( $this->submissions() as $s ) {
			if ( (int) $s['courseId'] === $id && (int) $s['userId'] !== $uid && ( $s['status'] ?? '' ) === 'APPROVED' ) {
				$out[] = $this->submission_dto( $s );
			}
		}
		return cb_response( $out );
	}

	// ---- peer comments (option-backed by submission id) ---------------------

	public function peer_comments( WP_REST_Request $request ): WP_REST_Response {
		$sid  = (int) $request['sid'];
		$list = (array) get_option( "cb_peer_comments_$sid", array() );
		return cb_response( array_values( $list ) );
	}

	public function add_peer_comment( WP_REST_Request $request ): WP_REST_Response {
		$sid     = (int) $request['sid'];
		$comment = sanitize_text_field( (string) ( $request->get_json_params()['comment'] ?? '' ) );
		if ( $comment === '' ) {
			return cb_error( 'متنِ نقد خالی است', 400, 'EMPTY', 'api/academy/project' );
		}
		$key   = "cb_peer_comments_$sid";
		$list  = (array) get_option( $key, array() );
		$user  = wp_get_current_user();
		$entry = array(
			'id'        => count( $list ) + 1,
			'userId'    => (int) $user->ID,
			'userName'  => $user->display_name ?: 'کاربر',
			'comment'   => $comment,
			'createdAt' => gmdate( 'c' ),
		);
		$list[] = $entry;
		update_option( $key, $list, false );
		return cb_response( $entry, 201 );
	}

	// ---- lesson questions (comments on the course, tagged by lesson) --------

	public function lesson_questions( WP_REST_Request $request ): WP_REST_Response {
		list( $course_id, $index ) = self::decode_lesson( (int) $request['lid'] );
		$comments = get_comments( array( 'post_id' => $course_id, 'type' => 'cb_lesson_q', 'meta_key' => 'lesson_index', 'meta_value' => $index, 'status' => 'approve', 'orderby' => 'comment_date_gmt', 'order' => 'ASC' ) );
		$out = array();
		foreach ( $comments as $c ) {
			$out[] = array(
				'id'        => (int) $c->comment_ID,
				'userId'    => (int) $c->user_id,
				'userName'  => $c->comment_author ?: null,
				'content'   => $c->comment_content,
				'parentId'  => $c->comment_parent ? (int) $c->comment_parent : null,
				'createdAt' => cb_iso( $c->comment_date_gmt ),
			);
		}
		return cb_response( $out );
	}

	public function create_lesson_question( WP_REST_Request $request ): WP_REST_Response {
		list( $course_id, $index ) = self::decode_lesson( (int) $request['lid'] );
		$body = $request->get_json_params();
		$user = wp_get_current_user();
		$cid  = wp_insert_comment( array(
			'comment_post_ID'  => $course_id,
			'comment_type'     => 'cb_lesson_q',
			'comment_content'  => sanitize_textarea_field( (string) ( $body['content'] ?? '' ) ),
			'comment_parent'   => (int) ( $body['parentId'] ?? 0 ),
			'user_id'          => $user->ID,
			'comment_author'   => $user->display_name,
			'comment_author_email' => $user->user_email,
			'comment_approved' => 1,
		) );
		if ( ! $cid ) {
			return cb_error( 'ثبت پرسش ناموفق بود', 400, 'CREATE_FAILED', 'api/academy/lessons' );
		}
		add_comment_meta( $cid, 'lesson_index', $index );
		$c = get_comment( $cid );
		return cb_response( array(
			'id'        => (int) $c->comment_ID,
			'userId'    => (int) $c->user_id,
			'userName'  => $c->comment_author ?: null,
			'content'   => $c->comment_content,
			'parentId'  => $c->comment_parent ? (int) $c->comment_parent : null,
			'createdAt' => cb_iso( $c->comment_date_gmt ),
		), 201 );
	}

	// ---- course refund requests (user meta) ---------------------------------

	public function refund_request( WP_REST_Request $request ): WP_REST_Response {
		$id     = (int) $request['id'];
		$uid    = get_current_user_id();
		$reason = sanitize_text_field( (string) ( $request->get_json_params()['reason'] ?? '' ) );
		$list   = get_user_meta( $uid, 'cb_course_refunds', true );
		$list   = is_array( $list ) ? $list : array();
		$entry  = array(
			'id'          => count( $list ) + 1,
			'courseId'    => $id,
			'courseTitle' => get_the_title( $id ),
			'amount'      => $this->discounted( $id ) ?? $this->price( $id ),
			'reason'      => $reason ?: null,
			'status'      => 'PENDING',
			'adminNote'   => null,
			'createdAt'   => gmdate( 'c' ),
			'resolvedAt'  => null,
		);
		$list[] = $entry;
		update_user_meta( $uid, 'cb_course_refunds', $list );
		return cb_response( $entry, 201 );
	}

	public function refund_mine(): WP_REST_Response {
		$list = get_user_meta( get_current_user_id(), 'cb_course_refunds', true );
		return cb_response( is_array( $list ) ? array_values( $list ) : array() );
	}
}
