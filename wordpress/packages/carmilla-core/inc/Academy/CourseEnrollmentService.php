<?php
namespace Carmilla\Core\Academy;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\CourseEnrollmentService', false ) ) {

/**
 * Canonical Shared Course Enrollment & Progress Service.
 *
 * Uses cb_course_prog_{$course_id} user meta so Theme-only, Plugin-only,
 * and Co-install modes share identical enrollment state and progress percentages.
 * Paid courses fail closed unless purchased or explicitly granted.
 *
 * Part of Task P04-WORDPRESS-CODE-027A.
 */
class CourseEnrollmentService {

	public static function is_enrolled( int $course_id, int $user_id ): bool {
		if ( $user_id <= 0 ) {
			return false;
		}
		$slug = (string) get_post_meta( $course_id, 'cb_product_slug', true );
		if ( '' !== $slug ) {
			return CourseCatalogService::is_course_accessible( $course_id, $user_id );
		}
		return is_array( get_user_meta( $user_id, "cb_course_prog_{$course_id}", true ) );
	}

	/**
	 * @return array<int, array<string, mixed>>
	 */
	public static function my_courses( int $user_id ): array {
		if ( $user_id <= 0 ) {
			return [];
		}
		$posts = get_posts( [
			'post_type'   => 'cb_course',
			'post_status' => 'publish',
			'numberposts' => 100,
		] );
		$out = [];
		foreach ( $posts as $c ) {
			if ( $c instanceof \WP_Post && self::is_enrolled( (int) $c->ID, $user_id ) ) {
				$out[] = CourseCatalogService::summary_dto( $c, $user_id );
			}
		}
		return $out;
	}

	/**
	 * Enrolls a user in a free course, or verifies access for a paid course.
	 *
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function enroll( int $course_id, int $user_id ) {
		if ( $user_id <= 0 ) {
			return new \WP_Error( 'UNAUTHORIZED', 'لطفاً وارد حساب کاربری خود شوید.', [ 'status' => 401 ] );
		}

		$course = get_post( $course_id );
		if ( ! $course || 'cb_course' !== $course->post_type || 'publish' !== $course->post_status ) {
			return new \WP_Error( 'NOT_FOUND', 'دوره یافت نشد', [ 'status' => 404 ] );
		}

		$slug = (string) get_post_meta( $course_id, 'cb_product_slug', true );
		if ( '' !== $slug && ! CourseCatalogService::is_course_accessible( $course_id, $user_id ) ) {
			return new \WP_Error( 'ENROLLMENT_REQUIRED', 'برای ثبت‌نام در این دوره ابتدا باید آن را تهیه کنید.', [ 'status' => 403 ] );
		}

		$done = get_user_meta( $user_id, "cb_course_prog_{$course_id}", true );
		if ( ! is_array( $done ) ) {
			update_user_meta( $user_id, "cb_course_prog_{$course_id}", [] );
		}

		return CourseCatalogService::detail_dto( $course, $user_id );
	}

	/**
	 * Updates lesson completion progress in cb_course_prog_{$course_id}.
	 *
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function update_lesson_progress( int $lesson_id, int $user_id, ?bool $completed ) {
		if ( $user_id <= 0 ) {
			return new \WP_Error( 'UNAUTHORIZED', 'لطفاً وارد حساب کاربری خود شوید.', [ 'status' => 401 ] );
		}

		[ $course_id, $index ] = CourseCatalogService::decode_lesson( $lesson_id );
		$course                = get_post( $course_id );
		if ( ! $course || 'cb_course' !== $course->post_type ) {
			return new \WP_Error( 'NOT_FOUND', 'درس یافت نشد', [ 'status' => 404 ] );
		}

		$lessons = CourseCatalogService::parse_lessons( $course_id );
		if ( $index < 0 || $index >= count( $lessons ) ) {
			return new \WP_Error( 'NOT_FOUND', 'درس یافت نشد', [ 'status' => 404 ] );
		}

		$is_free = ! empty( $lessons[ $index ]['free'] );
		if ( ! $is_free && ! CourseCatalogService::is_course_accessible( $course_id, $user_id ) ) {
			return new \WP_Error( 'ENROLLMENT_REQUIRED', 'دسترسی به پیشرفت این درس نیازمند ثبت‌نام در دوره است.', [ 'status' => 403 ] );
		}

		$done = get_user_meta( $user_id, "cb_course_prog_{$course_id}", true );
		$done = is_array( $done ) ? array_map( 'intval', $done ) : [];

		if ( true === $completed && ! in_array( $index, $done, true ) ) {
			$done[] = $index;
		} elseif ( false === $completed ) {
			$done = array_values( array_diff( $done, [ $index ] ) );
		}

		update_user_meta( $user_id, "cb_course_prog_{$course_id}", array_values( $done ) );

		$total = count( $lessons );
		return [
			'courseId'         => $course_id,
			'totalLessons'     => $total,
			'completedLessons' => count( $done ),
			'progressPercent'  => $total > 0 ? (int) round( count( $done ) / $total * 100 ) : 0,
		];
	}
}

}
