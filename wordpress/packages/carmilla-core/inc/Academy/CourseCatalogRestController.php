<?php
namespace Carmilla\Core\Academy;

use Carmilla\Core\Rest\RestInfrastructure;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\CourseCatalogRestController', false ) ) {

/**
 * Canonical Shared REST Controller for Course Catalog & Lesson Content.
 *
 * Registered once per request from RestInfrastructure.
 * Enrollment/progress (027A) and quizzes/certificates (027B) stay separate.
 *
 * Part of Task P04-WORDPRESS-CODE-027.
 */
class CourseCatalogRestController {

	private static bool $registered = false;

	public static function register_routes(): void {
		if ( self::$registered ) {
			return;
		}

		$server = function_exists( 'rest_get_server' ) ? rest_get_server() : null;
		if ( $server && is_object( $server ) ) {
			$routes = $server->get_routes();
			if ( ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/courses' ] )
				&& ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/admin/courses' ] ) ) {
				self::$registered = true;
				return;
			}
		}

		self::$registered = true;

		RestInfrastructure::register_route( '/api/courses', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'list_courses' ],
			'permission_callback' => '__return_true',
		] );

		RestInfrastructure::register_route( '/api/courses/(?P<slug>(?!certificates)[a-zA-Z0-9\-_%]+)', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'get_course' ],
			'permission_callback' => '__return_true',
		] );

		RestInfrastructure::register_route( '/api/admin/courses', [
			[
				'methods'             => 'GET',
				'callback'            => [ self::class, 'admin_list_courses' ],
				'permission_callback' => [ CourseCatalogService::class, 'can_manage_courses' ],
			],
			[
				'methods'             => 'POST',
				'callback'            => [ self::class, 'create_course' ],
				'permission_callback' => [ CourseCatalogService::class, 'can_manage_courses' ],
			],
		] );

		RestInfrastructure::register_route( '/api/admin/courses/(?P<id>\d+)', [
			[
				'methods'             => 'GET',
				'callback'            => [ self::class, 'admin_get_course' ],
				'permission_callback' => [ CourseCatalogService::class, 'can_manage_courses' ],
			],
			[
				'methods'             => 'PUT',
				'callback'            => [ self::class, 'update_course' ],
				'permission_callback' => [ CourseCatalogService::class, 'can_manage_courses' ],
			],
			[
				'methods'             => 'DELETE',
				'callback'            => [ self::class, 'delete_course' ],
				'permission_callback' => [ CourseCatalogService::class, 'can_manage_courses' ],
			],
		] );

		RestInfrastructure::register_route( '/api/admin/courses/(?P<id>\d+)/lessons', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'add_lesson' ],
			'permission_callback' => [ CourseCatalogService::class, 'can_manage_courses' ],
		] );
	}

	public static function list_courses( \WP_REST_Request $request ): \WP_REST_Response {
		return new \WP_REST_Response( CourseCatalogService::list_courses( get_current_user_id(), $request->get_params() ), 200 );
	}

	public static function admin_list_courses( \WP_REST_Request $request ): \WP_REST_Response {
		$params                   = $request->get_params();
		$params['include_drafts'] = true;
		return new \WP_REST_Response( CourseCatalogService::list_courses( get_current_user_id(), $params ), 200 );
	}

	public static function get_course( \WP_REST_Request $request ) {
		$res = CourseCatalogService::get_course_detail( (string) $request['slug'], get_current_user_id(), [ 'publish' ] );
		return self::to_response( $res, 200, (string) $request->get_route() );
	}

	public static function admin_get_course( \WP_REST_Request $request ) {
		$res = CourseCatalogService::get_course_detail( (int) $request['id'], get_current_user_id(), [ 'publish', 'draft', 'pending', 'private' ] );
		return self::to_response( $res, 200, (string) $request->get_route() );
	}

	public static function create_course( \WP_REST_Request $request ) {
		return self::to_response( CourseCatalogService::create_course( $request->get_params() ), 201, (string) $request->get_route() );
	}

	public static function update_course( \WP_REST_Request $request ) {
		return self::to_response( CourseCatalogService::update_course( (int) $request['id'], $request->get_params() ), 200, (string) $request->get_route() );
	}

	public static function delete_course( \WP_REST_Request $request ) {
		$res = CourseCatalogService::delete_course( (int) $request['id'] );
		if ( is_wp_error( $res ) ) {
			return self::to_response( $res, 404, (string) $request->get_route() );
		}
		return new \WP_REST_Response( null, 204 );
	}

	public static function add_lesson( \WP_REST_Request $request ) {
		return self::to_response( CourseCatalogService::add_lesson( (int) $request['id'], $request->get_params() ), 201, (string) $request->get_route() );
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
