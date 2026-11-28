<?php
namespace Carmilla\Core\Content;

use Carmilla\Core\Rest\RestInfrastructure;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\ContentRestController', false ) ) {

/**
 * Canonical Shared REST Controller for Posts, Pages, Categories, and Media.
 *
 * Registered once per request from Carmilla_Kernel / RestInfrastructure so
 * Theme-only, Plugin-only, and Co-install modes expose identical routes and DTOs
 * with zero duplicate route registrations.
 *
 * Part of Task P04-WORDPRESS-CODE-026.
 */
class ContentRestController {

	private static bool $registered = false;

	public static function register_routes(): void {
		if ( self::$registered ) {
			return;
		}

		// Also guard against WordPress REST server already having the route.
		$server = function_exists( 'rest_get_server' ) ? rest_get_server() : null;
		if ( $server && is_object( $server ) ) {
			$routes = $server->get_routes();
			if ( ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/blogs' ] )
				&& ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/pages' ] ) ) {
				self::$registered = true;
				return;
			}
		}

		self::$registered = true;

		// ---- Public Blog / Posts ----
		RestInfrastructure::register_route( '/api/blogs', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'list_blogs' ],
			'permission_callback' => '__return_true',
		] );
		RestInfrastructure::register_route( '/api/blogs/featured', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'featured_blogs' ],
			'permission_callback' => '__return_true',
		] );
		RestInfrastructure::register_route( '/api/blogs/categories', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'list_categories' ],
			'permission_callback' => '__return_true',
		] );
		RestInfrastructure::register_route( '/api/blogs/(?P<slug>[a-zA-Z0-9\-_%]+)', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'get_blog' ],
			'permission_callback' => '__return_true',
		] );
		RestInfrastructure::register_route( '/api/blogs/(?P<slug>[a-zA-Z0-9\-_%]+)/related', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'related_blogs' ],
			'permission_callback' => '__return_true',
		] );

		// ---- Admin Blog / Posts ----
		RestInfrastructure::register_route( '/api/admin/blogs', [
			[
				'methods'             => 'GET',
				'callback'            => [ self::class, 'admin_list_blogs' ],
				'permission_callback' => [ ContentService::class, 'can_manage_content' ],
			],
			[
				'methods'             => 'POST',
				'callback'            => [ self::class, 'create_blog' ],
				'permission_callback' => [ ContentService::class, 'can_manage_content' ],
			],
		] );
		RestInfrastructure::register_route( '/api/admin/blogs/categories', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'create_category' ],
			'permission_callback' => [ ContentService::class, 'can_manage_content' ],
		] );
		RestInfrastructure::register_route( '/api/admin/blogs/categories/(?P<id>\d+)', [
			[
				'methods'             => 'PUT',
				'callback'            => [ self::class, 'update_category' ],
				'permission_callback' => [ ContentService::class, 'can_manage_content' ],
			],
			[
				'methods'             => 'DELETE',
				'callback'            => [ self::class, 'delete_category' ],
				'permission_callback' => [ ContentService::class, 'can_manage_content' ],
			],
		] );
		RestInfrastructure::register_route( '/api/admin/blogs/(?P<slug>[a-zA-Z0-9\-_%]+)', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'admin_get_blog' ],
			'permission_callback' => [ ContentService::class, 'can_manage_content' ],
		] );
		RestInfrastructure::register_route( '/api/admin/blogs/(?P<id>\d+)', [
			[
				'methods'             => 'PUT',
				'callback'            => [ self::class, 'update_blog' ],
				'permission_callback' => [ ContentService::class, 'can_manage_content' ],
			],
			[
				'methods'             => 'DELETE',
				'callback'            => [ self::class, 'delete_blog' ],
				'permission_callback' => [ ContentService::class, 'can_manage_content' ],
			],
		] );

		// ---- Public Pages ----
		RestInfrastructure::register_route( '/api/pages', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'list_pages' ],
			'permission_callback' => '__return_true',
		] );
		RestInfrastructure::register_route( '/api/pages/(?P<slug>[a-zA-Z0-9\-_%]+)', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'get_page' ],
			'permission_callback' => '__return_true',
		] );

		// ---- Admin Pages ----
		RestInfrastructure::register_route( '/api/admin/pages', [
			[
				'methods'             => 'GET',
				'callback'            => [ self::class, 'admin_list_pages' ],
				'permission_callback' => [ ContentService::class, 'can_manage_content' ],
			],
			[
				'methods'             => 'POST',
				'callback'            => [ self::class, 'create_page' ],
				'permission_callback' => [ ContentService::class, 'can_manage_content' ],
			],
		] );
		RestInfrastructure::register_route( '/api/admin/pages/(?P<id>\d+)', [
			[
				'methods'             => 'GET',
				'callback'            => [ self::class, 'admin_get_page' ],
				'permission_callback' => [ ContentService::class, 'can_manage_content' ],
			],
			[
				'methods'             => 'PUT',
				'callback'            => [ self::class, 'update_page' ],
				'permission_callback' => [ ContentService::class, 'can_manage_content' ],
			],
			[
				'methods'             => 'DELETE',
				'callback'            => [ self::class, 'delete_page' ],
				'permission_callback' => [ ContentService::class, 'can_manage_content' ],
			],
		] );

		// ---- Media (legacy blog upload + canonical media routes) ----
		RestInfrastructure::register_route( '/api/admin/blogs/media/upload', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'upload_media' ],
			'permission_callback' => [ ContentService::class, 'can_upload_media' ],
		] );
		RestInfrastructure::register_route( '/api/admin/media', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'list_media' ],
			'permission_callback' => [ ContentService::class, 'can_upload_media' ],
		] );
		RestInfrastructure::register_route( '/api/admin/media/upload', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'upload_media' ],
			'permission_callback' => [ ContentService::class, 'can_upload_media' ],
		] );
	}

	// ---- Blog Callbacks -----------------------------------------------------

	public static function list_blogs( \WP_REST_Request $request ): \WP_REST_Response {
		return new \WP_REST_Response( ContentService::list_posts( $request->get_params(), [ 'publish' ] ), 200 );
	}

	public static function admin_list_blogs( \WP_REST_Request $request ): \WP_REST_Response {
		return new \WP_REST_Response( ContentService::list_posts( $request->get_params(), [ 'publish', 'draft', 'pending', 'private' ] ), 200 );
	}

	public static function featured_blogs( \WP_REST_Request $request ): \WP_REST_Response {
		$size = (int) ( $request->get_param( 'size' ) ?: 10 );
		return new \WP_REST_Response( ContentService::featured_posts( $size ), 200 );
	}

	public static function get_blog( \WP_REST_Request $request ) {
		$res = ContentService::get_post_by_slug( (string) $request['slug'], [ 'publish' ], true );
		return self::to_response( $res, 200, (string) $request->get_route() );
	}

	public static function admin_get_blog( \WP_REST_Request $request ) {
		$res = ContentService::get_post_by_slug( (string) $request['slug'], [ 'publish', 'draft', 'pending', 'private' ], false );
		return self::to_response( $res, 200, (string) $request->get_route() );
	}

	public static function related_blogs( \WP_REST_Request $request ): \WP_REST_Response {
		return new \WP_REST_Response( ContentService::related_posts( (string) $request['slug'], 4 ), 200 );
	}

	public static function list_categories(): \WP_REST_Response {
		return new \WP_REST_Response( ContentService::list_categories(), 200 );
	}

	public static function create_blog( \WP_REST_Request $request ) {
		$res = ContentService::create_post( $request->get_params() );
		return self::to_response( $res, 201, (string) $request->get_route() );
	}

	public static function update_blog( \WP_REST_Request $request ) {
		$res = ContentService::update_post( (int) $request['id'], $request->get_params() );
		return self::to_response( $res, 200, (string) $request->get_route() );
	}

	public static function delete_blog( \WP_REST_Request $request ) {
		$res = ContentService::delete_post( (int) $request['id'] );
		if ( is_wp_error( $res ) ) {
			return self::to_response( $res, 404, (string) $request->get_route() );
		}
		return new \WP_REST_Response( null, 204 );
	}

	public static function create_category( \WP_REST_Request $request ) {
		$res = ContentService::create_category( $request->get_params() );
		return self::to_response( $res, 201, (string) $request->get_route() );
	}

	public static function update_category( \WP_REST_Request $request ) {
		$res = ContentService::update_category( (int) $request['id'], $request->get_params() );
		return self::to_response( $res, 200, (string) $request->get_route() );
	}

	public static function delete_category( \WP_REST_Request $request ): \WP_REST_Response {
		ContentService::delete_category( (int) $request['id'] );
		return new \WP_REST_Response( null, 204 );
	}

	// ---- Page Callbacks -----------------------------------------------------

	public static function list_pages( \WP_REST_Request $request ): \WP_REST_Response {
		return new \WP_REST_Response( ContentService::list_pages( $request->get_params(), [ 'publish' ] ), 200 );
	}

	public static function admin_list_pages( \WP_REST_Request $request ): \WP_REST_Response {
		return new \WP_REST_Response( ContentService::list_pages( $request->get_params(), [ 'publish', 'draft', 'pending', 'private' ] ), 200 );
	}

	public static function get_page( \WP_REST_Request $request ) {
		$res = ContentService::get_page( (string) $request['slug'], [ 'publish' ] );
		return self::to_response( $res, 200, (string) $request->get_route() );
	}

	public static function admin_get_page( \WP_REST_Request $request ) {
		$res = ContentService::get_page( (int) $request['id'], [ 'publish', 'draft', 'pending', 'private' ] );
		return self::to_response( $res, 200, (string) $request->get_route() );
	}

	public static function create_page( \WP_REST_Request $request ) {
		$res = ContentService::create_page( $request->get_params() );
		return self::to_response( $res, 201, (string) $request->get_route() );
	}

	public static function update_page( \WP_REST_Request $request ) {
		$res = ContentService::update_page( (int) $request['id'], $request->get_params() );
		return self::to_response( $res, 200, (string) $request->get_route() );
	}

	public static function delete_page( \WP_REST_Request $request ) {
		$res = ContentService::delete_page( (int) $request['id'] );
		if ( is_wp_error( $res ) ) {
			return self::to_response( $res, 404, (string) $request->get_route() );
		}
		return new \WP_REST_Response( null, 204 );
	}

	// ---- Media Callbacks ----------------------------------------------------

	public static function list_media( \WP_REST_Request $request ): \WP_REST_Response {
		return new \WP_REST_Response( ContentService::list_media( $request->get_params() ), 200 );
	}

	public static function upload_media( \WP_REST_Request $request ) {
		$files = $request->get_file_params();
		$file  = isset( $files['file'] ) && is_array( $files['file'] ) ? $files['file'] : [];
		$res   = ContentService::upload_media( $file, 0 );
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
