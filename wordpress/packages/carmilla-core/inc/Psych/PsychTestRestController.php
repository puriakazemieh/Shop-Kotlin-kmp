<?php
namespace Carmilla\Core\Psych;

use Carmilla\Core\Rest\RestInfrastructure;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\PsychTestRestController', false ) ) {

/**
 * Canonical Shared REST Controller for Psychological Tests.
 *
 * Registers public catalog, owner-scoped attempt/submit/result routes,
 * Theme JS alias `/psych-tests/{id}/submit`, and admin management routes.
 *
 * Part of Task P04-WORDPRESS-CODE-029.
 */
class PsychTestRestController {

	private static bool $registered = false;

	public static function register_routes(): void {
		if ( self::$registered ) {
			return;
		}

		$server = function_exists( 'rest_get_server' ) ? rest_get_server() : null;
		if ( $server && is_object( $server ) ) {
			$routes = $server->get_routes();
			if ( ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/psych-tests' ] )
				&& ! empty( $routes[ '/' . RestInfrastructure::NAMESPACE . '/api/admin/psych-tests' ] ) ) {
				self::$registered = true;
				return;
			}
		}

		self::$registered = true;
		$login            = [ self::class, 'require_login' ];
		$admin            = [ PsychTestService::class, 'can_manage_psych_tests' ];

		// Public catalog & detail (option scores stripped)
		RestInfrastructure::register_route( '/api/psych-tests', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'list_tests' ],
			'permission_callback' => '__return_true',
		] );

		RestInfrastructure::register_route( '/api/psych-tests/(?P<slug>[a-zA-Z0-9\-_%]+)', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'test_detail' ],
			'permission_callback' => '__return_true',
		] );

		// Owner-scoped attempts, submit & private results
		RestInfrastructure::register_route( '/api/my-psych-tests', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'my_tests' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/my-psych-tests/(?P<id>\d+)/questions', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'attempt_questions' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/my-psych-tests/(?P<id>\d+)/submit', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'submit_attempt' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/api/my-psych-tests/(?P<id>\d+)/result', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'private_result' ],
			'permission_callback' => $login,
		] );

		// Direct test submit (canonical + Theme psychtest.js alias)
		RestInfrastructure::register_route( '/api/psych-tests/(?P<id>\d+)/submit', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'submit_test' ],
			'permission_callback' => $login,
		] );

		RestInfrastructure::register_route( '/psych-tests/(?P<id>\d+)/submit', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'submit_test' ],
			'permission_callback' => $login,
		] );

		// Admin management & counselor interpretation
		RestInfrastructure::register_route( '/api/admin/psych-tests', [
			[ 'methods' => 'GET', 'callback' => [ self::class, 'admin_list' ], 'permission_callback' => $admin ],
			[ 'methods' => 'POST', 'callback' => [ self::class, 'admin_create' ], 'permission_callback' => $admin ],
		] );

		RestInfrastructure::register_route( '/api/admin/psych-tests/(?P<id>\d+)', [
			[ 'methods' => 'GET', 'callback' => [ self::class, 'admin_detail' ], 'permission_callback' => $admin ],
			[ 'methods' => [ 'PUT', 'POST', 'PATCH' ], 'callback' => [ self::class, 'admin_update' ], 'permission_callback' => $admin ],
			[ 'methods' => 'DELETE', 'callback' => [ self::class, 'admin_delete' ], 'permission_callback' => $admin ],
		] );

		RestInfrastructure::register_route( '/api/admin/psych-tests/pending-interpretations', [
			'methods'             => 'GET',
			'callback'            => [ self::class, 'pending_interpretations' ],
			'permission_callback' => $admin,
		] );

		RestInfrastructure::register_route( '/api/admin/psych-tests/user-tests/(?P<uid>\d+)/interpret', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'interpret_attempt' ],
			'permission_callback' => $admin,
		] );
	}

	public static function require_login(): bool {
		return is_user_logged_in();
	}

	public static function list_tests(): \WP_REST_Response {
		return new \WP_REST_Response( PsychTestService::list_tests( get_current_user_id(), false ), 200 );
	}

	public static function test_detail( \WP_REST_Request $request ): \WP_REST_Response {
		return self::to_response( PsychTestService::get_test_detail( (string) $request['slug'], get_current_user_id(), [ 'publish' ] ), 200, (string) $request->get_route() );
	}

	public static function my_tests(): \WP_REST_Response {
		return new \WP_REST_Response( PsychTestService::my_tests( get_current_user_id() ), 200 );
	}

	public static function attempt_questions( \WP_REST_Request $request ): \WP_REST_Response {
		return self::to_response( PsychTestService::attempt_questions( (int) $request['id'], get_current_user_id() ), 200, (string) $request->get_route() );
	}

	public static function submit_attempt( \WP_REST_Request $request ): \WP_REST_Response {
		$answers = self::extract_answers( $request );
		return self::to_response( PsychTestService::submit_attempt( (int) $request['id'], get_current_user_id(), $answers ), 200, (string) $request->get_route() );
	}

	public static function private_result( \WP_REST_Request $request ): \WP_REST_Response {
		return self::to_response( PsychTestService::get_private_result( (int) $request['id'], get_current_user_id() ), 200, (string) $request->get_route() );
	}

	public static function submit_test( \WP_REST_Request $request ): \WP_REST_Response {
		$answers = self::extract_answers( $request );
		return self::to_response( PsychTestService::submit_for_test( (int) $request['id'], get_current_user_id(), $answers ), 200, (string) $request->get_route() );
	}

	public static function admin_list(): \WP_REST_Response {
		return new \WP_REST_Response( PsychTestService::list_tests( get_current_user_id(), true ), 200 );
	}

	public static function admin_detail( \WP_REST_Request $request ): \WP_REST_Response {
		return self::to_response( PsychTestService::get_test_detail( (int) $request['id'], get_current_user_id(), [ 'publish', 'draft', 'pending', 'private' ] ), 200, (string) $request->get_route() );
	}

	public static function admin_create( \WP_REST_Request $request ): \WP_REST_Response {
		return self::to_response( PsychTestService::create_test( self::extract_params( $request ) ), 201, (string) $request->get_route() );
	}

	public static function admin_update( \WP_REST_Request $request ): \WP_REST_Response {
		return self::to_response( PsychTestService::update_test( (int) $request['id'], self::extract_params( $request ) ), 200, (string) $request->get_route() );
	}

	public static function admin_delete( \WP_REST_Request $request ): \WP_REST_Response {
		$res = PsychTestService::delete_test( (int) $request['id'] );
		return is_wp_error( $res ) ? self::to_response( $res, 404, (string) $request->get_route() ) : new \WP_REST_Response( null, 204 );
	}

	public static function pending_interpretations(): \WP_REST_Response {
		return new \WP_REST_Response( PsychTestService::pending_interpretations(), 200 );
	}

	public static function interpret_attempt( \WP_REST_Request $request ): \WP_REST_Response {
		$params = self::extract_params( $request );
		$res    = PsychTestService::interpret_attempt( (int) $request['uid'], (string) ( $params['interpretation'] ?? '' ) );
		return is_wp_error( $res ) ? self::to_response( $res, 404, (string) $request->get_route() ) : new \WP_REST_Response( null, 200 );
	}

	/**
	 * @return array<int|string, mixed>
	 */
	private static function extract_answers( \WP_REST_Request $request ): array {
		$params = self::extract_params( $request );
		return isset( $params['answers'] ) && is_array( $params['answers'] ) ? $params['answers'] : [];
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
