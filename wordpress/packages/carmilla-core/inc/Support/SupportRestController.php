<?php
namespace Carmilla\Core\Support;

use Carmilla\Core\Rest\RestInfrastructure;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\SupportRestController', false ) ) {

/**
 * Canonical Shared REST Controller for Support Tickets & Threads.
 *
 * Registers `/api/support/tickets*`, legacy Theme `support.js` `/support` alias,
 * and `/api/admin/support/tickets*` management routes.
 *
 * Part of Task P04-WORDPRESS-CODE-029A.
 */
class SupportRestController {

	private static bool $registered = false;

	public static function register_routes(): void {
		if ( self::$registered ) {
			return;
		}

		$server = function_exists( 'rest_get_server' ) ? rest_get_server() : null;
		if ( $server && is_object( $server ) ) {
			$routes = $server->get_routes();
			if ( ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/support/tickets' ] ) ) {
				self::$registered = true;
				return;
			}
		}

		self::$registered = true;
		$login            = [ self::class, 'require_login' ];
		$admin            = [ SupportService::class, 'can_manage_support' ];

		RestInfrastructure::register_route( '/api/support/tickets', [
			[ 'methods' => 'GET', 'callback' => [ self::class, 'list_tickets' ], 'permission_callback' => $login ],
			[ 'methods' => 'POST', 'callback' => [ self::class, 'create_ticket' ], 'permission_callback' => $login ],
		] );

		RestInfrastructure::register_route( '/api/support/tickets/(?P<id>\d+)', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'get_ticket' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/support/tickets/(?P<id>\d+)/messages', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'post_message' ],
			'permission_callback' => $login,
		] );

		// Legacy Theme support.js alias
		RestInfrastructure::register_route( '/support', [
			[ 'methods' => 'GET', 'callback' => [ self::class, 'legacy_get' ], 'permission_callback' => $login ],
			[ 'methods' => 'POST', 'callback' => [ self::class, 'legacy_post' ], 'permission_callback' => $login ],
		] );

		// Admin support management
		RestInfrastructure::register_route( '/api/admin/support/tickets', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'admin_list' ],
			'permission_callback' => $admin,
		] );

		RestInfrastructure::register_route( '/api/admin/support/tickets/(?P<uid>\d+)/(?P<id>\d+)/reply', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'admin_reply' ],
			'permission_callback' => $admin,
		] );

		RestInfrastructure::register_route( '/api/admin/support/tickets/(?P<uid>\d+)/(?P<id>\d+)/status', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'admin_status' ],
			'permission_callback' => $admin,
		] );
	}

	public static function require_login(): bool {
		return is_user_logged_in();
	}

	public static function list_tickets( \WP_REST_Request $request ): \WP_REST_Response {
		if ( ! SupportService::is_enabled() ) {
			return RestInfrastructure::error_response( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', 403, (string) $request->get_route() );
		}
		return new \WP_REST_Response( SupportService::list_tickets( get_current_user_id() ), 200 );
	}

	public static function create_ticket( \WP_REST_Request $request ): \WP_REST_Response {
		return self::to_response( SupportService::create_ticket( get_current_user_id(), self::extract_params( $request ) ), 201, (string) $request->get_route() );
	}

	public static function get_ticket( \WP_REST_Request $request ): \WP_REST_Response {
		return self::to_response( SupportService::get_ticket( get_current_user_id(), (int) $request['id'] ), 200, (string) $request->get_route() );
	}

	public static function post_message( \WP_REST_Request $request ): \WP_REST_Response {
		return self::to_response( SupportService::post_message( get_current_user_id(), (int) $request['id'], self::extract_params( $request ), 'USER' ), 200, (string) $request->get_route() );
	}

	public static function legacy_get( \WP_REST_Request $request ): \WP_REST_Response {
		if ( ! SupportService::is_enabled() ) {
			return RestInfrastructure::error_response( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', 403, (string) $request->get_route() );
		}
		return new \WP_REST_Response( SupportService::legacy_thread( get_current_user_id() ), 200 );
	}

	public static function legacy_post( \WP_REST_Request $request ): \WP_REST_Response {
		if ( ! SupportService::is_enabled() ) {
			return RestInfrastructure::error_response( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', 403, (string) $request->get_route() );
		}
		$params = self::extract_params( $request );
		$res    = SupportService::legacy_post( get_current_user_id(), (string) ( $params['message'] ?? '' ) );
		return self::to_response( $res, 200, (string) $request->get_route() );
	}

	public static function admin_list(): \WP_REST_Response {
		return new \WP_REST_Response( SupportService::admin_list_tickets(), 200 );
	}

	public static function admin_reply( \WP_REST_Request $request ): \WP_REST_Response {
		return self::to_response( SupportService::post_message( (int) $request['uid'], (int) $request['id'], self::extract_params( $request ), 'ADMIN' ), 200, (string) $request->get_route() );
	}

	public static function admin_status( \WP_REST_Request $request ): \WP_REST_Response {
		$params = self::extract_params( $request );
		return self::to_response( SupportService::admin_set_status( (int) $request['uid'], (int) $request['id'], (string) ( $params['status'] ?? '' ) ), 200, (string) $request->get_route() );
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
