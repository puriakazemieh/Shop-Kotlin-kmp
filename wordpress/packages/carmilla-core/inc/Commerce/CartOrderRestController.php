<?php
namespace Carmilla\Core\Commerce;

use Carmilla\Core\Rest\RestInfrastructure;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\CartOrderRestController', false ) ) {

/**
 * Canonical Shared REST Controller for Cart & Orders.
 *
 * Registered once per request from RestInfrastructure so Theme-only,
 * Plugin-only, and Co-install modes share a single write/callback path.
 *
 * Part of Task P04-WORDPRESS-CODE-026B.
 */
class CartOrderRestController {

	private static bool $registered = false;

	public static function register_routes(): void {
		if ( self::$registered ) {
			return;
		}

		$server = function_exists( 'rest_get_server' ) ? rest_get_server() : null;
		if ( $server && is_object( $server ) ) {
			$routes = $server->get_routes();
			if ( ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/cart' ] )
				&& ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/orders' ] ) ) {
				self::$registered = true;
				return;
			}
		}

		self::$registered = true;
		$login = [ self::class, 'require_login' ];

		// ---- Cart ----
		RestInfrastructure::register_route( '/api/cart', [
			[ 'methods' => 'GET', 'callback' => [ self::class, 'get_cart' ], 'permission_callback' => $login ],
			[ 'methods' => 'DELETE', 'callback' => [ self::class, 'clear_cart' ], 'permission_callback' => $login ],
		] );
		RestInfrastructure::register_route( '/api/cart/items', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'add_item' ],
			'permission_callback' => $login,
		] );
		RestInfrastructure::register_route( '/api/cart/items/(?P<id>\d+)', [
			[ 'methods' => 'PATCH', 'callback' => [ self::class, 'update_qty' ], 'permission_callback' => $login ],
			[ 'methods' => 'DELETE', 'callback' => [ self::class, 'remove_item' ], 'permission_callback' => $login ],
			[ 'methods' => 'PUT', 'callback' => [ self::class, 'set_variant_qty' ], 'permission_callback' => $login ],
		] );
		RestInfrastructure::register_route( '/api/cart/items/(?P<id>\d+)/adjust', [
			'methods'             => 'PATCH',
			'callback'            => [ self::class, 'adjust_variant_qty' ],
			'permission_callback' => $login,
		] );
		RestInfrastructure::register_route( '/api/cart/items/(?P<id>\d+)/save-for-later', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'save_for_later' ],
			'permission_callback' => $login,
		] );
		RestInfrastructure::register_route( '/api/cart/items/(?P<id>\d+)/move-to-cart', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'move_to_cart' ],
			'permission_callback' => $login,
		] );
		RestInfrastructure::register_route( '/api/cart/discount', [
			[ 'methods' => 'POST', 'callback' => [ self::class, 'apply_discount' ], 'permission_callback' => $login ],
			[ 'methods' => 'DELETE', 'callback' => [ self::class, 'remove_discount' ], 'permission_callback' => $login ],
		] );

		// ---- Orders ----
		RestInfrastructure::register_route( '/api/orders', [
			[ 'methods' => 'GET', 'callback' => [ self::class, 'list_orders' ], 'permission_callback' => $login ],
			[ 'methods' => 'POST', 'callback' => [ self::class, 'create_order' ], 'permission_callback' => $login ],
		] );
		RestInfrastructure::register_route( '/api/orders/(?P<id>\d+)', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'get_order' ],
			'permission_callback' => $login,
		] );
		RestInfrastructure::register_route( '/api/orders/(?P<id>\d+)/cancel', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'cancel_order' ],
			'permission_callback' => $login,
		] );
		RestInfrastructure::register_route( '/api/orders/(?P<id>\d+)/track', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'track_order' ],
			'permission_callback' => $login,
		] );
		RestInfrastructure::register_route( '/api/orders/(?P<id>\d+)/reorder', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'reorder' ],
			'permission_callback' => $login,
		] );
	}

	public static function require_login(): bool {
		return is_user_logged_in();
	}

	public static function get_cart(): \WP_REST_Response {
		return new \WP_REST_Response( CartOrderService::get_cart( get_current_user_id() ), 200 );
	}

	public static function clear_cart(): \WP_REST_Response {
		return new \WP_REST_Response( CartOrderService::clear_cart( get_current_user_id() ), 200 );
	}

	public static function add_item( \WP_REST_Request $request ) {
		$body = (array) $request->get_json_params();
		return self::to_response( CartOrderService::add_item( get_current_user_id(), $body ), 200, (string) $request->get_route() );
	}

	public static function update_qty( \WP_REST_Request $request ): \WP_REST_Response {
		$qty = (int) ( ( (array) $request->get_json_params() )['qty'] ?? 1 );
		return new \WP_REST_Response( CartOrderService::update_qty( get_current_user_id(), (int) $request['id'], $qty ), 200 );
	}

	public static function remove_item( \WP_REST_Request $request ): \WP_REST_Response {
		return new \WP_REST_Response( CartOrderService::remove_item( get_current_user_id(), (int) $request['id'] ), 200 );
	}

	public static function set_variant_qty( \WP_REST_Request $request ): \WP_REST_Response {
		$qty = (int) ( ( (array) $request->get_json_params() )['qty'] ?? 0 );
		return new \WP_REST_Response( CartOrderService::set_variant_qty( get_current_user_id(), (int) $request['id'], $qty ), 200 );
	}

	public static function adjust_variant_qty( \WP_REST_Request $request ): \WP_REST_Response {
		$delta = (int) ( ( (array) $request->get_json_params() )['delta'] ?? 0 );
		return new \WP_REST_Response( CartOrderService::adjust_variant_qty( get_current_user_id(), (int) $request['id'], $delta ), 200 );
	}

	public static function save_for_later( \WP_REST_Request $request ): \WP_REST_Response {
		return new \WP_REST_Response( CartOrderService::set_saved_state( get_current_user_id(), (int) $request['id'], true ), 200 );
	}

	public static function move_to_cart( \WP_REST_Request $request ): \WP_REST_Response {
		return new \WP_REST_Response( CartOrderService::set_saved_state( get_current_user_id(), (int) $request['id'], false ), 200 );
	}

	public static function apply_discount( \WP_REST_Request $request ) {
		$code = (string) ( ( (array) $request->get_json_params() )['code'] ?? '' );
		return self::to_response( CartOrderService::apply_discount( get_current_user_id(), $code ), 200, (string) $request->get_route() );
	}

	public static function remove_discount(): \WP_REST_Response {
		return new \WP_REST_Response( CartOrderService::remove_discount( get_current_user_id() ), 200 );
	}

	public static function list_orders( \WP_REST_Request $request ) {
		return self::to_response( CartOrderService::list_orders( get_current_user_id() ), 200, (string) $request->get_route() );
	}

	public static function get_order( \WP_REST_Request $request ) {
		return self::to_response( CartOrderService::get_order_detail( get_current_user_id(), (int) $request['id'] ), 200, (string) $request->get_route() );
	}

	public static function create_order( \WP_REST_Request $request ) {
		return self::to_response( CartOrderService::create_order( get_current_user_id(), (array) $request->get_json_params() ), 201, (string) $request->get_route() );
	}

	public static function cancel_order( \WP_REST_Request $request ) {
		$res = CartOrderService::cancel_order( get_current_user_id(), (int) $request['id'] );
		return is_wp_error( $res ) ? self::to_response( $res, 400, (string) $request->get_route() ) : new \WP_REST_Response( null, 200 );
	}

	public static function track_order( \WP_REST_Request $request ) {
		return self::to_response( CartOrderService::track_order( get_current_user_id(), (int) $request['id'] ), 200, (string) $request->get_route() );
	}

	public static function reorder( \WP_REST_Request $request ) {
		return self::to_response( CartOrderService::reorder( get_current_user_id(), (int) $request['id'] ), 200, (string) $request->get_route() );
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
