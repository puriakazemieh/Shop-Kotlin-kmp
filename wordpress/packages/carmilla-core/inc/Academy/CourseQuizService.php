<?php
namespace Carmilla\Core\Academy;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\CourseQuizService', false ) ) {

/**
 * Canonical Shared Course Quiz & Certificate Service.
 *
 * Centralizes quiz parsing, server-side scoring (without leaking answers),
 * duplicate attempt/certificate control, and certificate verification using
 * existing meta/option keys (cb_quiz, cb_pass_score, cb_lesson_quiz_{idx},
 * cb_quiz_passed_{id}, cb_lquiz_passed_{cid}_{idx}, cb_certs, cb_cert_index).
 *
 * Part of Task P04-WORDPRESS-CODE-027B.
 */
class CourseQuizService {

	/**
	 * Parse a line-based quiz: «question? | opt | opt* | opt».
	 * Option `correct` flags are only exposed when $reveal is true.
	 *
	 * @return array<int, array<string, mixed>>
	 */
	public static function parse_quiz_lines( string $raw, bool $reveal ): array {
		$lines = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $raw ) ?: [] ) );
		$out   = [];
		$i     = 0;
		foreach ( $lines as $line ) {
			$parts = array_map( 'trim', explode( '|', $line ) );
			$text  = (string) array_shift( $parts );
			if ( '' === $text || empty( $parts ) ) {
				continue;
			}
			$options = [];
			$correct = null;
			foreach ( $parts as $oi => $opt ) {
				$is_correct = ( false !== strpos( $opt, '*' ) ) || ( false !== strpos( $opt, '✓' ) );
				if ( $is_correct ) {
					$correct = $oi;
				}
				$clean     = trim( str_replace( [ '*', '✓' ], '', $opt ) );
				$options[] = [
					'text'    => $clean,
					'correct' => $reveal ? $is_correct : null,
				];
			}
			$out[] = [
				'index'    => $i,
				'text'     => $text,
				'options'  => $options,
				'_correct' => $correct,
			];
			++$i;
		}
		return $out;
	}

	public static function pass_score( int $course_id ): int {
		$s = (int) get_post_meta( $course_id, 'cb_pass_score', true );
		return $s > 0 ? $s : 60;
	}

	public static function cert_number( int $course_id, int $user_id ): string {
		return 'CB-' . strtoupper( substr( md5( $course_id . '-' . $user_id . '-' . wp_salt( 'auth' ) ), 0, 10 ) );
	}

	/**
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function get_course_quiz( int $course_id, int $user_id ) {
		$course = get_post( $course_id );
		if ( ! $course || 'cb_course' !== $course->post_type ) {
			return new \WP_Error( 'NOT_FOUND', 'دوره یافت نشد', [ 'status' => 404 ] );
		}
		if ( ! CourseCatalogService::is_course_accessible( $course_id, $user_id ) ) {
			return new \WP_Error( 'ENROLLMENT_REQUIRED', 'برای دسترسی به آزمون ابتدا در دوره ثبت‌نام کنید.', [ 'status' => 403 ] );
		}

		$questions = self::parse_quiz_lines( (string) get_post_meta( $course_id, 'cb_quiz', true ), false );
		foreach ( $questions as &$q ) {
			unset( $q['_correct'] );
		}
		unset( $q );

		return [
			'courseId'      => $course_id,
			'title'         => 'آزمونِ پایانِ دوره',
			'passScore'     => self::pass_score( $course_id ),
			'questions'     => $questions,
			'alreadyPassed' => (bool) get_user_meta( $user_id, "cb_quiz_passed_{$course_id}", true ),
			'hasQuiz'       => ! empty( $questions ),
		];
	}

	/**
	 * @param array<string|int, mixed> $answers
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function submit_course_quiz( int $course_id, int $user_id, array $answers ) {
		if ( $user_id <= 0 ) {
			return new \WP_Error( 'UNAUTHORIZED', 'لطفاً وارد حساب کاربری خود شوید.', [ 'status' => 401 ] );
		}
		$course = get_post( $course_id );
		if ( ! $course || 'cb_course' !== $course->post_type ) {
			return new \WP_Error( 'NOT_FOUND', 'دوره یافت نشد', [ 'status' => 404 ] );
		}
		if ( ! CourseCatalogService::is_course_accessible( $course_id, $user_id ) ) {
			return new \WP_Error( 'ENROLLMENT_REQUIRED', 'برای شرکت در آزمون ابتدا در دوره ثبت‌نام کنید.', [ 'status' => 403 ] );
		}

		$questions = self::parse_quiz_lines( (string) get_post_meta( $course_id, 'cb_quiz', true ), true );
		if ( empty( $questions ) ) {
			return new \WP_Error( 'NO_QUIZ', 'این دوره آزمون ندارد', [ 'status' => 400 ] );
		}

		// Duplicate attempt control: if already passed, return existing pass & certificate without re-issuing.
		if ( get_user_meta( $user_id, "cb_quiz_passed_{$course_id}", true ) ) {
			return [
				'courseId'          => $course_id,
				'score'             => 100,
				'passed'            => true,
				'passScore'         => self::pass_score( $course_id ),
				'certificateNumber' => self::issue_certificate( $course_id, $user_id ),
				'duplicateAttempt'  => true,
			];
		}

		$correct = 0;
		foreach ( $questions as $q ) {
			$given = $answers[ (string) $q['index'] ] ?? ( $answers[ $q['index'] ] ?? null );
			if ( null !== $given && (int) $given === (int) $q['_correct'] ) {
				++$correct;
			}
		}

		$score  = (int) round( $correct / count( $questions ) * 100 );
		$pass   = self::pass_score( $course_id );
		$passed = $score >= $pass;
		$cert   = null;
		if ( $passed ) {
			update_user_meta( $user_id, "cb_quiz_passed_{$course_id}", 1 );
			$cert = self::issue_certificate( $course_id, $user_id );
		}

		return [
			'courseId'          => $course_id,
			'score'             => $score,
			'passed'            => $passed,
			'passScore'         => $pass,
			'certificateNumber' => $cert,
			'duplicateAttempt'  => false,
		];
	}

	/**
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function get_lesson_quiz( int $lesson_id, int $user_id ) {
		[ $course_id, $index ] = CourseCatalogService::decode_lesson( $lesson_id );
		$course                = get_post( $course_id );
		if ( ! $course || 'cb_course' !== $course->post_type ) {
			return new \WP_Error( 'NOT_FOUND', 'درس یافت نشد', [ 'status' => 404 ] );
		}
		if ( ! CourseCatalogService::is_course_accessible( $course_id, $user_id ) ) {
			return new \WP_Error( 'ENROLLMENT_REQUIRED', 'برای دسترسی به آزمون درس ابتدا در دوره ثبت‌نام کنید.', [ 'status' => 403 ] );
		}

		$stored    = get_post_meta( $course_id, 'cb_lesson_quiz_' . $index, true );
		$questions = ( is_array( $stored ) && ! empty( $stored['questions'] ) )
			? self::parse_quiz_lines( (string) $stored['questions'], false )
			: [];
		foreach ( $questions as &$q ) {
			unset( $q['_correct'] );
		}
		unset( $q );

		return [
			'lessonId'      => $lesson_id,
			'title'         => is_array( $stored ) ? ( $stored['title'] ?? 'آزمونِ این درس' ) : 'آزمونِ این درس',
			'passScore'     => is_array( $stored ) ? (int) ( $stored['passScore'] ?? 60 ) : 60,
			'questions'     => $questions,
			'alreadyPassed' => (bool) get_user_meta( $user_id, "cb_lquiz_passed_{$course_id}_{$index}", true ),
		];
	}

	/**
	 * @param array<string|int, mixed> $answers
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function submit_lesson_quiz( int $lesson_id, int $user_id, array $answers ) {
		if ( $user_id <= 0 ) {
			return new \WP_Error( 'UNAUTHORIZED', 'لطفاً وارد حساب کاربری خود شوید.', [ 'status' => 401 ] );
		}
		[ $course_id, $index ] = CourseCatalogService::decode_lesson( $lesson_id );
		$course                = get_post( $course_id );
		if ( ! $course || 'cb_course' !== $course->post_type ) {
			return new \WP_Error( 'NOT_FOUND', 'درس یافت نشد', [ 'status' => 404 ] );
		}
		if ( ! CourseCatalogService::is_course_accessible( $course_id, $user_id ) ) {
			return new \WP_Error( 'ENROLLMENT_REQUIRED', 'برای شرکت در آزمون درس ابتدا در دوره ثبت‌نام کنید.', [ 'status' => 403 ] );
		}

		$stored    = get_post_meta( $course_id, 'cb_lesson_quiz_' . $index, true );
		$questions = ( is_array( $stored ) && ! empty( $stored['questions'] ) )
			? self::parse_quiz_lines( (string) $stored['questions'], true )
			: [];

		$correct = 0;
		foreach ( $questions as $q ) {
			$given = $answers[ (string) $q['index'] ] ?? ( $answers[ $q['index'] ] ?? null );
			if ( null !== $given && (int) $given === (int) $q['_correct'] ) {
				++$correct;
			}
		}

		$pass   = is_array( $stored ) ? (int) ( $stored['passScore'] ?? 60 ) : 60;
		$score  = $questions ? (int) round( $correct / count( $questions ) * 100 ) : 0;
		$passed = $score >= $pass;
		if ( $passed ) {
			update_user_meta( $user_id, "cb_lquiz_passed_{$course_id}_{$index}", 1 );
		}

		return [
			'lessonId'  => $lesson_id,
			'score'     => $score,
			'passed'    => $passed,
			'passScore' => $pass,
		];
	}

	/**
	 * Admin upsert for course quiz.
	 *
	 * @param array<string, mixed> $data
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function upsert_course_quiz( int $course_id, array $data ) {
		$course = get_post( $course_id );
		if ( ! $course || 'cb_course' !== $course->post_type ) {
			return new \WP_Error( 'NOT_FOUND', 'دوره یافت نشد', [ 'status' => 404 ] );
		}
		if ( isset( $data['questions'] ) ) {
			update_post_meta( $course_id, 'cb_quiz', sanitize_textarea_field( (string) $data['questions'] ) );
		}
		if ( isset( $data['passScore'] ) ) {
			update_post_meta( $course_id, 'cb_pass_score', max( 1, min( 100, (int) $data['passScore'] ) ) );
		}
		return [ 'ok' => true, 'courseId' => $course_id ];
	}

	/**
	 * Issues a certificate once per (course, user) and registers it in cb_cert_index.
	 */
	public static function issue_certificate( int $course_id, int $user_id ): string {
		$number = self::cert_number( $course_id, $user_id );
		$certs  = get_user_meta( $user_id, 'cb_certs', true );
		$certs  = is_array( $certs ) ? $certs : [];
		foreach ( $certs as $c ) {
			if ( ( $c['certNumber'] ?? '' ) === $number ) {
				return $number;
			}
		}

		$user  = get_userdata( $user_id );
		$entry = [
			'id'          => count( $certs ) + 1,
			'courseId'    => $course_id,
			'courseTitle' => get_the_title( $course_id ),
			'certNumber'  => $number,
			'issuedAt'    => gmdate( 'c' ),
			'userName'    => $user ? $user->display_name : '',
		];
		$certs[] = $entry;
		update_user_meta( $user_id, 'cb_certs', $certs );

		$index            = get_option( 'cb_cert_index', [] );
		$index            = is_array( $index ) ? $index : [];
		$index[ $number ] = [
			'courseTitle' => $entry['courseTitle'],
			'issuedAt'    => $entry['issuedAt'],
		];
		update_option( 'cb_cert_index', $index, false );

		return $number;
	}

	/**
	 * @return array<int, array<string, mixed>>
	 */
	public static function list_certificates( int $user_id ): array {
		if ( $user_id <= 0 ) {
			return [];
		}
		$certs = get_user_meta( $user_id, 'cb_certs', true );
		$certs = is_array( $certs ) ? $certs : [];
		$out   = [];
		foreach ( $certs as $c ) {
			$out[] = [
				'id'          => (int) ( $c['id'] ?? 0 ),
				'courseId'    => (int) ( $c['courseId'] ?? 0 ),
				'courseTitle' => (string) ( $c['courseTitle'] ?? '' ),
				'certNumber'  => (string) ( $c['certNumber'] ?? '' ),
				'issuedAt'    => (string) ( $c['issuedAt'] ?? gmdate( 'c' ) ),
				'userName'    => $c['userName'] ?? null,
			];
		}
		return $out;
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function verify_certificate( string $number ): array {
		$index = get_option( 'cb_cert_index', [] );
		if ( is_array( $index ) && isset( $index[ $number ] ) ) {
			return [
				'valid'       => true,
				'courseTitle' => $index[ $number ]['courseTitle'] ?? null,
				'certNumber'  => $number,
				'issuedAt'    => $index[ $number ]['issuedAt'] ?? null,
			];
		}
		return [
			'valid'       => false,
			'courseTitle' => null,
			'certNumber'  => $number,
			'issuedAt'    => null,
		];
	}
}

}
