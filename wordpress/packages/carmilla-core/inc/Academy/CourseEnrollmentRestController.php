<?php
namespace Carmilla\Core\Academy;

use Carmilla\Core\Rest\RestInfrastructure;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\CourseEnrollmentRestController', false ) ) {

/**
 * Canonical Shared REST Controller for Academy Enrollment & Progress.
 *
 * Part of Task P04-WORDPRESS-CODE-027A.
 */
class CourseEnrollmentRestController {

	private static bool $registered = false;

	public static function register_routes(): void {
		if ( self::$registered ) {
			return;
		}

		$server = function_exists( 'rest_get_server' ) ? rest_get_server() : null;
		if ( $server && is_object( $server ) ) {
			$routes = $server->get_routes();
			if ( ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/academy/my-courses' ] ) ) {
				self::$registered = true;
				return;
			}
		}

		self::$registered = true;
		$login            = [ self::class, 'require_login' ];

		RestInfrastructure::register_route( '/api/academy/my-courses', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'my_courses' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/academy/courses/(?P<id>\d+)/enroll', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'enroll' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/academy/lessons/(?P<lid>\d+)/progress', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'lesson_progress' ],
			'permission_callback' => $login,
		] );
	}

	public static function require_login(): bool {
		return is_user_logged_in();
	}

	public static function my_courses(): \WP_REST_Response {
		return new \WP_REST_Response( CourseEnrollmentService::my_courses( get_current_user_id() ), 200 );
	}

	public static function enroll( \WP_REST_Request $request ) {
		$res = CourseEnrollmentService::enroll( (int) $request['id'], get_current_user_id() );
		return self::to_response( $res, 200, (string) $request->get_route() );
	}

	public static function lesson_progress( \WP_REST_Request $request ) {
		$body      = (array) $request->get_json_params();
		$completed = isset( $body['completed'] ) ? (bool) $body['completed'] : null;
		$res       = CourseEnrollmentService::update_lesson_progress( (int) $request['lid'], get_current_user_id(), $completed );
		return self::to_response( $res, 200, (string) $request->get_route() );
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
