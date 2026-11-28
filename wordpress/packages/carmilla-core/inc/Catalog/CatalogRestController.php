<?php
namespace Carmilla\Core\Catalog;

use Carmilla\Core\Rest\RestInfrastructure;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\CatalogRestController', false ) ) {

/**
 * Canonical Shared REST Controller for Catalog & Products.
 *
 * Registered once per request from RestInfrastructure so Theme-only,
 * Plugin-only, and Co-install modes expose identical catalog routes.
 *
 * Part of Task P04-WORDPRESS-CODE-026A.
 */
class CatalogRestController {

	private static bool $registered = false;

	public static function register_routes(): void {
		if ( self::$registered ) {
			return;
		}

		$server = function_exists( 'rest_get_server' ) ? rest_get_server() : null;
		if ( $server && is_object( $server ) ) {
			$routes = $server->get_routes();
			if ( ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/products' ] )
				&& ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/categories' ] ) ) {
				self::$registered = true;
				return;
			}
		}

		self::$registered = true;

		RestInfrastructure::register_route( '/api/categories', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'categories' ],
			'permission_callback' => '__return_true',
		] );

		RestInfrastructure::register_route( '/api/products', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'products' ],
			'permission_callback' => '__return_true',
		] );

		RestInfrastructure::register_route( '/api/products/(?P<slug>[a-zA-Z0-9\-_%]+)', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'product_detail' ],
			'permission_callback' => '__return_true',
		] );

		RestInfrastructure::register_route( '/api/campaigns/active', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'active_campaign' ],
			'permission_callback' => '__return_true',
		] );

		RestInfrastructure::register_route( '/api/banners', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'banners' ],
			'permission_callback' => '__return_true',
		] );

		RestInfrastructure::register_route( '/api/admin/products', [
			[
				'methods'             => 'GET',
				'callback'            => [ self::class, 'products' ],
				'permission_callback' => [ CatalogService::class, 'can_manage_catalog' ],
			],
			[
				'methods'             => 'POST',
				'callback'            => [ self::class, 'create_product' ],
				'permission_callback' => [ CatalogService::class, 'can_manage_catalog' ],
			],
		] );

		RestInfrastructure::register_route( '/api/admin/products/(?P<id>\d+)', [
			[
				'methods'             => 'GET',
				'callback'            => [ self::class, 'admin_product_by_id' ],
				'permission_callback' => [ CatalogService::class, 'can_manage_catalog' ],
			],
			[
				'methods'             => 'PATCH',
				'callback'            => [ self::class, 'update_product' ],
				'permission_callback' => [ CatalogService::class, 'can_manage_catalog' ],
			],
			[
				'methods'             => 'DELETE',
				'callback'            => [ self::class, 'delete_product' ],
				'permission_callback' => [ CatalogService::class, 'can_manage_catalog' ],
			],
		] );
	}

	public static function categories( \WP_REST_Request $request ) {
		return self::to_response( CatalogService::list_categories(), 200, (string) $request->get_route() );
	}

	public static function products( \WP_REST_Request $request ) {
		return self::to_response( CatalogService::list_products( $request->get_params() ), 200, (string) $request->get_route() );
	}

	public static function product_detail( \WP_REST_Request $request ) {
		return self::to_response( CatalogService::get_product_detail( (string) $request['slug'] ), 200, (string) $request->get_route() );
	}

	public static function admin_product_by_id( \WP_REST_Request $request ) {
		return self::to_response( CatalogService::get_product_detail( (int) $request['id'] ), 200, (string) $request->get_route() );
	}

	public static function create_product( \WP_REST_Request $request ) {
		return self::to_response( CatalogService::create_product( $request->get_params() ), 201, (string) $request->get_route() );
	}

	public static function update_product( \WP_REST_Request $request ) {
		return self::to_response( CatalogService::update_product( (int) $request['id'], $request->get_params() ), 200, (string) $request->get_route() );
	}

	public static function delete_product( \WP_REST_Request $request ) {
		$res = CatalogService::delete_product( (int) $request['id'] );
		if ( is_wp_error( $res ) ) {
			return self::to_response( $res, 404, (string) $request->get_route() );
		}
		return new \WP_REST_Response( null, 204 );
	}

	public static function active_campaign(): \WP_REST_Response {
		return new \WP_REST_Response( CatalogService::active_campaign(), 200 );
	}

	public static function banners(): \WP_REST_Response {
		return new \WP_REST_Response( CatalogService::list_banners(), 200 );
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
