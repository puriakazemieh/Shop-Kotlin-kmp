<?php
namespace Carmilla\Core\Academy;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\CourseCatalogService', false ) ) {

/**
 * Canonical Shared Course Catalog & Lesson Content Service.
 *
 * Centralizes cb_course CPT reading/writing, cb_lessons parsing, synthetic
 * lesson IDs (courseId * 100000 + index + 1), and protected-lesson redaction
 * across Theme-only, Plugin-only, and Co-install modes.
 *
 * Enrollment/progress belong to P04-WORDPRESS-CODE-027A; quizzes/certificates
 * belong to P04-WORDPRESS-CODE-027B.
 *
 * Part of Task P04-WORDPRESS-CODE-027.
 */
class CourseCatalogService {

	public const LESSON_BASE = 100000;

	public static function is_enabled(): bool {
		if ( class_exists( '\Carmilla_Kernel' ) && \Carmilla_Kernel::is_booted() ) {
			return \Carmilla_Kernel::is_feature_active( 'academy.core' );
		}
		return true;
	}

	public static function can_manage_courses(): bool {
		return current_user_can( 'manage_options' )
			|| current_user_can( 'carmilla_manage_courses' )
			|| current_user_can( 'carmilla_edit_courses' )
			|| current_user_can( 'edit_others_posts' );
	}

	public static function lesson_id( int $course_id, int $index ): int {
		return $course_id * self::LESSON_BASE + ( $index + 1 );
	}

	/**
	 * @return array{0:int,1:int}
	 */
	public static function decode_lesson( int $lesson_id ): array {
		$course_id = intdiv( $lesson_id, self::LESSON_BASE );
		$index     = ( $lesson_id % self::LESSON_BASE ) - 1;
		return [ $course_id, $index ];
	}

	/**
	 * @return array<int, array{title:string,url:string,free:bool}>
	 */
	public static function parse_lessons( int $course_id ): array {
		$raw   = (string) get_post_meta( $course_id, 'cb_lessons', true );
		$lines = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $raw ) ?: [] ) );
		$out   = [];
		foreach ( $lines as $line ) {
			$p     = array_map( 'trim', explode( '|', $line ) );
			$out[] = [
				'title' => $p[0] ?? '',
				'url'   => isset( $p[1] ) ? esc_url_raw( $p[1] ) : '',
				'free'  => isset( $p[2] ) && in_array( strtolower( $p[2] ), [ 'free', 'رایگان', '1', 'true' ], true ),
			];
		}
		return $out;
	}

	/**
	 * Fail-closed access check for protected course lessons.
	 * Paid courses (cb_product_slug set) require authenticated user + purchase or explicit enrollment.
	 */
	public static function is_course_accessible( int $course_id, int $user_id ): bool {
		$slug = (string) get_post_meta( $course_id, 'cb_product_slug', true );
		if ( '' === $slug ) {
			return true; // Free course.
		}
		if ( $user_id <= 0 ) {
			return false;
		}
		if ( get_user_meta( $user_id, "cb_course_enrolled_{$course_id}", true ) ) {
			return true;
		}
		if ( function_exists( 'wc_customer_bought_product' ) ) {
			$pid = self::product_id_for( $course_id );
			if ( $pid > 0 ) {
				return (bool) wc_customer_bought_product( '', $user_id, $pid );
			}
		}
		return false;
	}

	public static function product_id_for( int $course_id ): int {
		$slug = (string) get_post_meta( $course_id, 'cb_product_slug', true );
		if ( '' === $slug ) {
			return 0;
		}
		$product = get_page_by_path( $slug, OBJECT, 'product' );
		return $product ? (int) $product->ID : 0;
	}

	/**
	 * @param array<string, mixed> $params
	 * @return array<int, array<string, mixed>>
	 */
	public static function list_courses( int $user_id = 0, array $params = [] ): array {
		$statuses = ! empty( $params['include_drafts'] ) ? [ 'publish', 'draft', 'pending', 'private' ] : [ 'publish' ];
		$posts    = get_posts( [
			'post_type'   => 'cb_course',
			'post_status' => $statuses,
			'numberposts' => isset( $params['size'] ) ? max( 1, min( 100, (int) $params['size'] ) ) : 100,
			'orderby'     => 'date',
			'order'       => 'DESC',
		] );

		$out = [];
		foreach ( $posts as $course ) {
			if ( $course instanceof \WP_Post ) {
				$out[] = self::summary_dto( $course, $user_id );
			}
		}
		return $out;
	}

	/**
	 * @param int|string $id_or_slug
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function get_course_detail( $id_or_slug, int $user_id = 0, array $statuses = [ 'publish' ] ) {
		$course = null;
		if ( is_numeric( $id_or_slug ) ) {
			$candidate = get_post( (int) $id_or_slug );
			if ( $candidate instanceof \WP_Post && 'cb_course' === $candidate->post_type && in_array( $candidate->post_status, $statuses, true ) ) {
				$course = $candidate;
			}
		} else {
			$slug  = rawurldecode( (string) $id_or_slug );
			$query = new \WP_Query( [
				'post_type'      => 'cb_course',
				'name'           => $slug,
				'post_status'    => $statuses,
				'posts_per_page' => 1,
			] );
			$course = $query->have_posts() ? $query->posts[0] : null;
		}

		if ( ! $course ) {
			return new \WP_Error( 'NOT_FOUND', 'دوره یافت نشد', [ 'status' => 404 ] );
		}

		return self::detail_dto( $course, $user_id );
	}

	/**
	 * @param array<string, mixed> $data
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function create_course( array $data ) {
		$title = trim( (string) ( $data['title'] ?? '' ) );
		if ( '' === $title ) {
			return new \WP_Error( 'VALIDATION', 'عنوان دوره الزامی است', [ 'status' => 400 ] );
		}

		$insert = [
			'post_type'    => 'cb_course',
			'post_title'   => sanitize_text_field( $title ),
			'post_content' => wp_kses_post( (string) ( $data['description'] ?? '' ) ),
			'post_status'  => 'publish',
		];
		if ( ! empty( $data['slug'] ) ) {
			$insert['post_name'] = sanitize_title( (string) $data['slug'] );
		}

		$id = wp_insert_post( $insert, true );
		if ( is_wp_error( $id ) ) {
			return new \WP_Error( 'CREATE_FAILED', $id->get_error_message(), [ 'status' => 400 ] );
		}

		self::apply_course_meta( (int) $id, $data );
		return self::detail_dto( get_post( $id ), get_current_user_id() );
	}

	/**
	 * @param array<string, mixed> $data
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function update_course( int $id, array $data ) {
		$course = get_post( $id );
		if ( ! $course || 'cb_course' !== $course->post_type ) {
			return new \WP_Error( 'NOT_FOUND', 'دوره یافت نشد', [ 'status' => 404 ] );
		}

		$update = [ 'ID' => $course->ID ];
		if ( array_key_exists( 'title', $data ) && null !== $data['title'] ) {
			$update['post_title'] = sanitize_text_field( (string) $data['title'] );
		}
		if ( array_key_exists( 'description', $data ) && null !== $data['description'] ) {
			$update['post_content'] = wp_kses_post( (string) $data['description'] );
		}

		$res = wp_update_post( $update, true );
		if ( is_wp_error( $res ) ) {
			return new \WP_Error( 'UPDATE_FAILED', $res->get_error_message(), [ 'status' => 400 ] );
		}

		self::apply_course_meta( $course->ID, $data );
		return self::detail_dto( get_post( $course->ID ), get_current_user_id() );
	}

	/**
	 * @return true|\WP_Error
	 */
	public static function delete_course( int $id ) {
		$course = get_post( $id );
		if ( ! $course || 'cb_course' !== $course->post_type ) {
			return new \WP_Error( 'NOT_FOUND', 'دوره یافت نشد', [ 'status' => 404 ] );
		}
		wp_delete_post( $course->ID, true );
		return true;
	}

	/**
	 * Appends a lesson line to cb_lessons meta.
	 *
	 * @param array<string, mixed> $data
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function add_lesson( int $course_id, array $data ) {
		$course = get_post( $course_id );
		if ( ! $course || 'cb_course' !== $course->post_type ) {
			return new \WP_Error( 'NOT_FOUND', 'دوره یافت نشد', [ 'status' => 404 ] );
		}

		$title = trim( (string) ( $data['title'] ?? '' ) );
		if ( '' === $title ) {
			return new \WP_Error( 'VALIDATION', 'عنوان درس الزامی است', [ 'status' => 400 ] );
		}

		$url  = esc_url_raw( (string) ( $data['videoUrl'] ?? ( $data['url'] ?? '' ) ) );
		$free = ! empty( $data['freePreview'] ) || ! empty( $data['free'] ) ? 'free' : 'paid';
		$line = sanitize_text_field( $title ) . ' | ' . $url . ' | ' . $free;

		$raw   = trim( (string) get_post_meta( $course_id, 'cb_lessons', true ) );
		$next  = '' === $raw ? $line : ( $raw . "\n" . $line );
		update_post_meta( $course_id, 'cb_lessons', $next );

		return self::detail_dto( get_post( $course_id ), get_current_user_id() );
	}

	/**
	 * @param array<string, mixed> $data
	 */
	private static function apply_course_meta( int $course_id, array $data ): void {
		$map = [
			'instructor'      => 'cb_instructor',
			'instructorBio'   => 'cb_instructor_bio',
			'level'           => 'cb_level',
			'format'          => 'cb_format',
			'duration'        => 'cb_duration',
			'productSlug'     => 'cb_product_slug',
			'syllabus'        => 'cb_syllabus',
			'lessonsRaw'      => 'cb_lessons',
		];
		foreach ( $map as $key => $meta_key ) {
			if ( array_key_exists( $key, $data ) && null !== $data[ $key ] ) {
				update_post_meta( $course_id, $meta_key, sanitize_textarea_field( (string) $data[ $key ] ) );
			}
		}
		if ( array_key_exists( 'price', $data ) && null !== $data['price'] ) {
			update_post_meta( $course_id, 'cb_price', (float) $data['price'] );
		}
		if ( array_key_exists( 'discountedPrice', $data ) ) {
			$disc = $data['discountedPrice'];
			update_post_meta( $course_id, 'cb_discounted_price', ( null === $disc || '' === $disc ) ? '' : (float) $disc );
		}
		if ( array_key_exists( 'capacity', $data ) && null !== $data['capacity'] ) {
			update_post_meta( $course_id, 'cb_capacity', (int) $data['capacity'] );
		}
		if ( isset( $data['lessons'] ) && is_array( $data['lessons'] ) ) {
			$lines = [];
			foreach ( $data['lessons'] as $l ) {
				$t       = sanitize_text_field( (string) ( $l['title'] ?? '' ) );
				$u       = esc_url_raw( (string) ( $l['videoUrl'] ?? ( $l['url'] ?? '' ) ) );
				$f       = ! empty( $l['freePreview'] ) || ! empty( $l['free'] ) ? 'free' : 'paid';
				if ( '' !== $t ) {
					$lines[] = "{$t} | {$u} | {$f}";
				}
			}
			update_post_meta( $course_id, 'cb_lessons', implode( "\n", $lines ) );
		}
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function summary_dto( \WP_Post $course, int $user_id ): array {
		$id      = (int) $course->ID;
		$lessons = self::parse_lessons( $id );
		$done    = $user_id > 0 ? get_user_meta( $user_id, "cb_course_prog_{$id}", true ) : [];
		$done    = is_array( $done ) ? array_map( 'intval', $done ) : [];
		$total   = count( $lessons );
		$pct     = $total > 0 ? (int) round( count( $done ) / $total * 100 ) : 0;
		$disc    = get_post_meta( $id, 'cb_discounted_price', true );

		return [
			'id'               => $id,
			'title'            => get_the_title( $course ),
			'slug'             => $course->post_name,
			'thumbnailUrl'     => get_the_post_thumbnail_url( $course, 'large' ) ?: null,
			'instructor'       => get_post_meta( $id, 'cb_instructor', true ) ?: null,
			'price'            => (float) get_post_meta( $id, 'cb_price', true ),
			'discountedPrice'  => ( '' !== $disc && null !== $disc ) ? (float) $disc : null,
			'lessonCount'      => $total,
			'completedLessons' => count( $done ),
			'progressPercent'  => $pct,
			'enrolled'         => self::is_course_accessible( $id, $user_id ) && $user_id > 0,
			'courseType'       => 'COURSE',
			'format'           => get_post_meta( $id, 'cb_format', true ) ?: 'ONLINE_RECORDED',
			'online'           => true,
			'level'            => get_post_meta( $id, 'cb_level', true ) ?: null,
		];
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function detail_dto( \WP_Post $course, int $user_id ): array {
		$id          = (int) $course->ID;
		$lessons     = self::parse_lessons( $id );
		$done        = $user_id > 0 ? get_user_meta( $user_id, "cb_course_prog_{$id}", true ) : [];
		$done        = is_array( $done ) ? array_map( 'intval', $done ) : [];
		$access      = self::is_course_accessible( $id, $user_id );
		$capacity    = (int) get_post_meta( $id, 'cb_capacity', true );
		$seats       = (int) get_option( "cb_course_seats_{$id}", 0 );
		$disc        = get_post_meta( $id, 'cb_discounted_price', true );
		$lesson_dtos = [];

		foreach ( $lessons as $idx => $l ) {
			$open          = $l['free'] || $access;
			$files         = (array) get_post_meta( $id, 'cb_lesson_files_' . $idx, true );
			$quiz          = get_post_meta( $id, 'cb_lesson_quiz_' . $idx, true );
			$lesson_dtos[] = [
				'id'                  => self::lesson_id( $id, $idx ),
				'title'               => $l['title'],
				'durationSeconds'     => 0,
				'freePreview'         => $l['free'],
				'videoUrl'            => $open ? ( $l['url'] ?: null ) : null,
				'completed'           => in_array( $idx, $done, true ),
				'lastPositionSeconds' => 0,
				'resourceFiles'       => $open ? array_values( $files ) : [],
				'hasQuiz'             => is_array( $quiz ) && ! empty( $quiz['questions'] ),
			];
		}

		$syllabus_skills = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', (string) get_post_meta( $id, 'cb_syllabus', true ) ) ?: [] ) );
		$total           = count( $lessons );
		$pct             = $total > 0 ? (int) round( count( $done ) / $total * 100 ) : 0;

		return [
			'id'                        => $id,
			'title'                     => get_the_title( $course ),
			'slug'                      => $course->post_name,
			'description'               => apply_filters( 'the_content', $course->post_content ) ?: null,
			'thumbnailUrl'              => get_the_post_thumbnail_url( $course, 'large' ) ?: null,
			'instructor'                => get_post_meta( $id, 'cb_instructor', true ) ?: null,
			'price'                     => (float) get_post_meta( $id, 'cb_price', true ),
			'discountedPrice'           => ( '' !== $disc && null !== $disc ) ? (float) $disc : null,
			'enrolled'                  => $access && $user_id > 0,
			'progressPercent'           => $pct,
			'sections'                  => [ [ 'id' => 1, 'title' => 'سرفصل‌ها', 'lessons' => $lesson_dtos ] ],
			'courseType'                => 'COURSE',
			'format'                    => get_post_meta( $id, 'cb_format', true ) ?: 'ONLINE_RECORDED',
			'online'                    => true,
			'level'                     => get_post_meta( $id, 'cb_level', true ) ?: null,
			'capacity'                  => $capacity ?: null,
			'seatsTaken'                => $seats,
			'seatsRemaining'            => $capacity ? max( 0, $capacity - $seats ) : null,
			'instructorBio'             => get_post_meta( $id, 'cb_instructor_bio', true ) ?: null,
			'instructorSkills'          => array_values( $syllabus_skills ),
			'full'                      => $capacity ? ( $seats >= $capacity ) : false,
			'onWaitlist'                => $user_id ? in_array( $user_id, (array) get_option( "cb_course_waitlist_{$id}", [] ), true ) : false,
			'productId'                 => self::product_id_for( $id ) ?: null,
			'requiresProjectSubmission' => (bool) get_post_meta( $id, 'cb_requires_project', true ),
			'totalDurationSeconds'      => 0,
			'hasUnseenUpdate'           => false,
		];
	}
}

}
