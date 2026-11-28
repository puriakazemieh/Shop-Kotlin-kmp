<?php
namespace Carmilla\Core\Academy;

use Carmilla\Core\Rest\RestInfrastructure;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\CourseQuizRestController', false ) ) {

/**
 * Canonical Shared REST Controller for Academy Quizzes & Certificates.
 *
 * Part of Task P04-WORDPRESS-CODE-027B.
 */
class CourseQuizRestController {

	private static bool $registered = false;

	public static function register_routes(): void {
		if ( self::$registered ) {
			return;
		}

		$server = function_exists( 'rest_get_server' ) ? rest_get_server() : null;
		if ( $server && is_object( $server ) ) {
			$routes = $server->get_routes();
			if ( ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/academy/certificates' ] ) ) {
				self::$registered = true;
				return;
			}
		}

		self::$registered = true;
		$login            = [ self::class, 'require_login' ];
		$admin            = [ CourseCatalogService::class, 'can_manage_courses' ];

		RestInfrastructure::register_route( '/api/courses/certificates/verify/(?P<num>[A-Za-z0-9\-]+)', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'verify_certificate' ],
			'permission_callback' => '__return_true',
		] );

		RestInfrastructure::register_route( '/api/academy/courses/(?P<id>\d+)/quiz', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'get_quiz' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/academy/courses/(?P<id>\d+)/quiz/submit', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'submit_quiz' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/academy/lessons/(?P<lid>\d+)/quiz', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'lesson_quiz' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/academy/lessons/(?P<lid>\d+)/quiz/submit', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'lesson_quiz_submit' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/academy/certificates', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'certificates' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/admin/courses/(?P<id>\d+)/quiz', [
			'methods'             => [ 'PUT', 'POST' ],
			'callback'            => [ self::class, 'admin_upsert_quiz' ],
			'permission_callback' => $admin,
		] );
	}

	public static function require_login(): bool {
		return is_user_logged_in();
	}

	public static function get_quiz( \WP_REST_Request $request ) {
		return self::to_response( CourseQuizService::get_course_quiz( (int) $request['id'], get_current_user_id() ), 200, (string) $request->get_route() );
	}

	public static function submit_quiz( \WP_REST_Request $request ) {
		$answers = (array) ( ( (array) $request->get_json_params() )['answers'] ?? [] );
		return self::to_response( CourseQuizService::submit_course_quiz( (int) $request['id'], get_current_user_id(), $answers ), 200, (string) $request->get_route() );
	}

	public static function lesson_quiz( \WP_REST_Request $request ) {
		return self::to_response( CourseQuizService::get_lesson_quiz( (int) $request['lid'], get_current_user_id() ), 200, (string) $request->get_route() );
	}

	public static function lesson_quiz_submit( \WP_REST_Request $request ) {
		$answers = (array) ( ( (array) $request->get_json_params() )['answers'] ?? [] );
		return self::to_response( CourseQuizService::submit_lesson_quiz( (int) $request['lid'], get_current_user_id(), $answers ), 200, (string) $request->get_route() );
	}

	public static function certificates(): \WP_REST_Response {
		return new \WP_REST_Response( CourseQuizService::list_certificates( get_current_user_id() ), 200 );
	}

	public static function verify_certificate( \WP_REST_Request $request ): \WP_REST_Response {
		return new \WP_REST_Response( CourseQuizService::verify_certificate( (string) $request['num'] ), 200 );
	}

	public static function admin_upsert_quiz( \WP_REST_Request $request ) {
		return self::to_response( CourseQuizService::upsert_course_quiz( (int) $request['id'], $request->get_params() ), 200, (string) $request->get_route() );
	}

	/**
	 * @param mixed $res
	 */
	private static function to_response( $res, int $ok_status, string $path ): \WP_REST_Response {
		if ( is_wp_error( $res ) ) {
			$data   = $res->get_error_data();
			$status = is_array( $data ) && isset( $data['status'] ) ? (int) $data['status'] : 400;
			return RestInfrastructure::error_response(
				(string) $res->get_error_code(),
				$res->get_error_message(),
				$status,
				$path
			);
		}
		return new \WP_REST_Response( $res, $ok_status );
	}
}

}
