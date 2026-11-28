<?php
namespace Carmilla\Core\Clinic;

use Carmilla\Core\Rest\RestInfrastructure;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\TherapistCatalogRestController', false ) ) {

/**
 * Canonical Shared REST Controller for Therapist Catalog & Availability.
 *
 * Registered once per request from RestInfrastructure.
 * Booking/cancel belong to P04-WORDPRESS-CODE-028A.
 *
 * Part of Task P04-WORDPRESS-CODE-028.
 */
class TherapistCatalogRestController {

	private static bool $registered = false;

	public static function register_routes(): void {
		if ( self::$registered ) {
			return;
		}

		$server = function_exists( 'rest_get_server' ) ? rest_get_server() : null;
		if ( $server && is_object( $server ) ) {
			$routes = $server->get_routes();
			if ( ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/therapists' ] )
				&& ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/admin/therapists' ] ) ) {
				self::$registered = true;
				return;
			}
		}

		self::$registered = true;
		$admin            = [ TherapistCatalogService::class, 'can_manage_therapists' ];

		RestInfrastructure::register_route( '/api/therapists', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'list_therapists' ],
			'permission_callback' => '__return_true',
		] );

		RestInfrastructure::register_route( '/api/therapists/(?P<slug>[a-zA-Z0-9\-_%]+)', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'therapist_detail' ],
			'permission_callback' => '__return_true',
		] );

		RestInfrastructure::register_route( '/api/admin/therapists', [
			[ 'methods' => 'GET', 'callback' => [ self::class, 'admin_list_therapists' ], 'permission_callback' => $admin ],
			[ 'methods' => 'POST', 'callback' => [ self::class, 'create_therapist' ], 'permission_callback' => $admin ],
		] );

		RestInfrastructure::register_route( '/api/admin/therapists/(?P<id>\d+)', [
			[ 'methods' => 'GET', 'callback' => [ self::class, 'admin_therapist_detail' ], 'permission_callback' => $admin ],
			[ 'methods' => [ 'PUT', 'POST' ], 'callback' => [ self::class, 'update_therapist' ], 'permission_callback' => $admin ],
			[ 'methods' => 'DELETE', 'callback' => [ self::class, 'delete_therapist' ], 'permission_callback' => $admin ],
		] );

		RestInfrastructure::register_route( '/api/admin/therapists/(?P<id>\d+)/slots', [
			[ 'methods' => 'GET', 'callback' => [ self::class, 'therapist_slots' ], 'permission_callback' => $admin ],
			[ 'methods' => 'POST', 'callback' => [ self::class, 'add_slot' ], 'permission_callback' => $admin ],
		] );
	}

	public static function list_therapists(): \WP_REST_Response {
		return new \WP_REST_Response( TherapistCatalogService::list_therapists( false ), 200 );
	}

	public static function admin_list_therapists(): \WP_REST_Response {
		return new \WP_REST_Response( TherapistCatalogService::list_therapists( true ), 200 );
	}

	public static function therapist_detail( \WP_REST_Request $request ) {
		return self::to_response( TherapistCatalogService::get_therapist_detail( (string) $request['slug'], [ 'publish' ] ), 200, (string) $request->get_route() );
	}

	public static function admin_therapist_detail( \WP_REST_Request $request ) {
		return self::to_response( TherapistCatalogService::get_therapist_detail( (int) $request['id'], [ 'publish', 'draft', 'pending', 'private' ] ), 200, (string) $request->get_route() );
	}

	public static function create_therapist( \WP_REST_Request $request ) {
		return self::to_response( TherapistCatalogService::create_therapist( $request->get_params() ), 201, (string) $request->get_route() );
	}

	public static function update_therapist( \WP_REST_Request $request ) {
		return self::to_response( TherapistCatalogService::update_therapist( (int) $request['id'], $request->get_params() ), 200, (string) $request->get_route() );
	}

	public static function delete_therapist( \WP_REST_Request $request ) {
		$res = TherapistCatalogService::delete_therapist( (int) $request['id'] );
		return is_wp_error( $res ) ? self::to_response( $res, 404, (string) $request->get_route() ) : new \WP_REST_Response( null, 204 );
	}

	public static function therapist_slots( \WP_REST_Request $request ) {
		return new \WP_REST_Response( TherapistCatalogService::slot_dtos( (int) $request['id'] ), 200 );
	}

	public static function add_slot( \WP_REST_Request $request ) {
		return self::to_response( TherapistCatalogService::add_slot( (int) $request['id'], $request->get_params() ), 201, (string) $request->get_route() );
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
