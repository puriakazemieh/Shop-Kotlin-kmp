<?php
namespace Carmilla\Core\Clinic;

use Carmilla\Core\Rest\RestInfrastructure;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\AppointmentBookingRestController', false ) ) {

/**
 * Canonical Shared REST Controller for Appointment Booking, Cancellation & Management.
 *
 * Registers canonical `/api/clinic/...` endpoints, `/api/admin/therapists/appointments...`,
 * and backwards-compatible `/appointments...` alias routes for Theme JS (`booking.js` & `appointments.js`).
 * Registered once per request from RestInfrastructure.
 *
 * Part of Task P04-WORDPRESS-CODE-028A.
 */
class AppointmentBookingRestController {

	private static bool $registered = false;

	public static function register_routes(): void {
		if ( self::$registered ) {
			return;
		}

		$server = function_exists( 'rest_get_server' ) ? rest_get_server() : null;
		if ( $server && is_object( $server ) ) {
			$routes = $server->get_routes();
			if ( ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/clinic/appointments' ] )
				&& ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/admin/therapists/appointments' ] ) ) {
				self::$registered = true;
				return;
			}
		}

		self::$registered = true;
		$login            = [ self::class, 'require_login' ];
		$admin            = [ AppointmentBookingService::class, 'can_manage_appointments' ];

		// Canonical clinic appointment endpoints
		RestInfrastructure::register_route( '/api/clinic/my-appointments', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'my_appointments' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/clinic/appointments', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'book' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/clinic/appointments/(?P<id>\d+)/cancel', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'cancel' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/clinic/appointments/(?P<id>\d+)/receipt', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'receipt' ],
			'permission_callback' => $login,
		] );

		// Legacy/Theme JS alias routes (booking.js & appointments.js)
		RestInfrastructure::register_route( '/appointments', [
			[ 'methods' => 'GET', 'callback' => [ self::class, 'my_appointments' ], 'permission_callback' => $login ],
			[ 'methods' => 'POST', 'callback' => [ self::class, 'book' ], 'permission_callback' => $login ],
		] );

		RestInfrastructure::register_route( '/appointments/(?P<id>\d+)/cancel', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'cancel' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/appointments/(?P<id>\d+)/receipt', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'receipt' ],
			'permission_callback' => $login,
		] );

		// Admin appointment management endpoints
		RestInfrastructure::register_route( '/api/admin/therapists/appointments', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'admin_list' ],
			'permission_callback' => $admin,
		] );

		RestInfrastructure::register_route( '/api/admin/therapists/appointments/(?P<id>\d+)/confirm', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'admin_confirm' ],
			'permission_callback' => $admin,
		] );

		RestInfrastructure::register_route( '/api/admin/therapists/appointments/(?P<id>\d+)/complete', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'admin_complete' ],
			'permission_callback' => $admin,
		] );
	}

	public static function require_login(): bool {
		return is_user_logged_in();
	}

	public static function my_appointments( \WP_REST_Request $request ): \WP_REST_Response {
		if ( ! AppointmentBookingService::is_enabled() ) {
			return RestInfrastructure::error_response( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', 403, (string) $request->get_route() );
		}
		return new \WP_REST_Response( AppointmentBookingService::my_appointments( get_current_user_id() ), 200 );
	}

	public static function book( \WP_REST_Request $request ): \WP_REST_Response {
		$params = $request->get_json_params();
		if ( ! is_array( $params ) || empty( $params ) ) {
			$params = $request->get_params();
		}
		$res = AppointmentBookingService::book( get_current_user_id(), is_array( $params ) ? $params : [] );
		return self::to_response( $res, 201, (string) $request->get_route() );
	}

	public static function cancel( \WP_REST_Request $request ): \WP_REST_Response {
		$res = AppointmentBookingService::cancel( (int) $request['id'], get_current_user_id() );
		return is_wp_error( $res )
			? self::to_response( $res, 400, (string) $request->get_route() )
			: new \WP_REST_Response( null, 200 );
	}

	public static function receipt( \WP_REST_Request $request ): \WP_REST_Response {
		$res = AppointmentBookingService::receipt( (int) $request['id'], get_current_user_id() );
		return self::to_response( $res, 200, (string) $request->get_route() );
	}

	public static function admin_list(): \WP_REST_Response {
		return new \WP_REST_Response( AppointmentBookingService::admin_list_appointments(), 200 );
	}

	public static function admin_confirm( \WP_REST_Request $request ): \WP_REST_Response {
		$params = $request->get_json_params();
		if ( ! is_array( $params ) ) {
			$params = $request->get_params();
		}
		$res = AppointmentBookingService::admin_confirm_appointment( (int) $request['id'], is_array( $params ) ? $params : [] );
		return is_wp_error( $res )
			? self::to_response( $res, 404, (string) $request->get_route() )
			: new \WP_REST_Response( null, 200 );
	}

	public static function admin_complete( \WP_REST_Request $request ): \WP_REST_Response {
		$res = AppointmentBookingService::admin_complete_appointment( (int) $request['id'] );
		return is_wp_error( $res )
			? self::to_response( $res, 404, (string) $request->get_route() )
			: new \WP_REST_Response( null, 200 );
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
