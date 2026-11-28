<?php
namespace Carmilla\Core\Interactions;

use Carmilla\Core\Rest\RestInfrastructure;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\InteractionRestController', false ) ) {

/**
 * Canonical Shared REST Controller for Product Reviews, Q&A & Favorites.
 *
 * Part of Task P04-WORDPRESS-CODE-029B.
 */
class InteractionRestController {

	private static bool $registered = false;

	public static function register_routes(): void {
		if ( self::$registered ) {
			return;
		}

		$server = function_exists( 'rest_get_server' ) ? rest_get_server() : null;
		if ( $server && is_object( $server ) ) {
			$routes = $server->get_routes();
			if ( ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/reviews' ] )
				&& ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/favorites' ] ) ) {
				self::$registered = true;
				return;
			}
		}

		self::$registered = true;
		$login            = [ self::class, 'require_login' ];

		// Reviews
		RestInfrastructure::register_route( '/api/reviews/product/(?P<id>\d+)', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'list_reviews' ],
			'permission_callback' => '__return_true',
		] );

		RestInfrastructure::register_route( '/api/reviews', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'create_review' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/reviews/(?P<id>\d+)', [
			[ 'methods' => 'PUT', 'callback' => [ self::class, 'update_review' ], 'permission_callback' => $login ],
			[ 'methods' => 'DELETE', 'callback' => [ self::class, 'delete_review' ], 'permission_callback' => $login ],
		] );

		RestInfrastructure::register_route( '/api/reviews/(?P<id>\d+)/helpful', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'helpful' ],
			'permission_callback' => $login,
		] );

		// Q&A
		RestInfrastructure::register_route( '/api/questions/product/(?P<id>\d+)', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'list_questions' ],
			'permission_callback' => '__return_true',
		] );

		RestInfrastructure::register_route( '/api/questions', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'create_question' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/questions/(?P<id>\d+)', [
			[ 'methods' => 'PUT', 'callback' => [ self::class, 'update_question' ], 'permission_callback' => $login ],
			[ 'methods' => 'DELETE', 'callback' => [ self::class, 'delete_question' ], 'permission_callback' => $login ],
		] );

		// Favorites
		RestInfrastructure::register_route( '/api/favorites', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'list_favorites' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/favorites/(?P<pid>\d+)', [
			[ 'methods' => 'POST', 'callback' => [ self::class, 'add_favorite' ], 'permission_callback' => $login ],
			[ 'methods' => 'DELETE', 'callback' => [ self::class, 'remove_favorite' ], 'permission_callback' => $login ],
		] );
	}

	public static function require_login(): bool {
		return is_user_logged_in();
	}

	public static function list_reviews( \WP_REST_Request $request ): \WP_REST_Response {
		return new \WP_REST_Response( InteractionService::list_reviews( (int) $request['id'] ), 200 );
	}

	public static function create_review( \WP_REST_Request $request ): \WP_REST_Response {
		return self::to_response( InteractionService::create_review( get_current_user_id(), self::extract_params( $request ) ), 201, (string) $request->get_route() );
	}

	public static function update_review( \WP_REST_Request $request ): \WP_REST_Response {
		return self::to_response( InteractionService::update_review( get_current_user_id(), (int) $request['id'], self::extract_params( $request ) ), 200, (string) $request->get_route() );
	}

	public static function delete_review( \WP_REST_Request $request ): \WP_REST_Response {
		$res = InteractionService::delete_review( get_current_user_id(), (int) $request['id'] );
		return is_wp_error( $res ) ? self::to_response( $res, 404, (string) $request->get_route() ) : new \WP_REST_Response( null, 204 );
	}

	public static function helpful( \WP_REST_Request $request ): \WP_REST_Response {
		return self::to_response( InteractionService::mark_helpful( (int) $request['id'] ), 200, (string) $request->get_route() );
	}

	public static function list_questions( \WP_REST_Request $request ): \WP_REST_Response {
		return new \WP_REST_Response( InteractionService::list_questions( (int) $request['id'] ), 200 );
	}

	public static function create_question( \WP_REST_Request $request ): \WP_REST_Response {
		return self::to_response( InteractionService::create_question( get_current_user_id(), self::extract_params( $request ) ), 201, (string) $request->get_route() );
	}

	public static function update_question( \WP_REST_Request $request ): \WP_REST_Response {
		return self::to_response( InteractionService::update_question( get_current_user_id(), (int) $request['id'], self::extract_params( $request ) ), 200, (string) $request->get_route() );
	}

	public static function delete_question( \WP_REST_Request $request ): \WP_REST_Response {
		$res = InteractionService::delete_question( get_current_user_id(), (int) $request['id'] );
		return is_wp_error( $res ) ? self::to_response( $res, 404, (string) $request->get_route() ) : new \WP_REST_Response( null, 204 );
	}

	public static function list_favorites( \WP_REST_Request $request ): \WP_REST_Response {
		$page = max( 0, (int) ( $request->get_param( 'page' ) ?? 0 ) );
		$size = max( 1, min( 100, (int) ( $request->get_param( 'size' ) ?? 20 ) ) );
		return new \WP_REST_Response( InteractionService::list_favorites( get_current_user_id(), $page, $size ), 200 );
	}

	public static function add_favorite( \WP_REST_Request $request ): \WP_REST_Response {
		InteractionService::add_favorite( get_current_user_id(), (int) $request['pid'] );
		return new \WP_REST_Response( null, 200 );
	}

	public static function remove_favorite( \WP_REST_Request $request ): \WP_REST_Response {
		InteractionService::remove_favorite( get_current_user_id(), (int) $request['pid'] );
		return new \WP_REST_Response( null, 200 );
	}

	/**
	 * @return array<string, mixed>
	 */
	private static function extract_params( \WP_REST_Request $request ): array {
		$params = $request->get_json_params();
		if ( ! is_array( $params ) || empty( $params ) ) {
			$params = $request->get_params();
		}
		return is_array( $params ) ? $params : [];
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
