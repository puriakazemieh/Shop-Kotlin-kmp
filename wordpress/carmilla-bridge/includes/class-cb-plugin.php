<?php
/**
 * Plugin bootstrap: registers REST routes, wires JWT Bearer authentication,
 * and exposes permission callbacks used across controllers.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Plugin {

	private static $instance = null;

	/** Cache of the user resolved from the Bearer token for the current request. */
	private static $bearer_user = null;

	public static function instance(): CB_Plugin {
		if ( self::$instance === null ) {
			self::$instance = new self();
		}
		return self::$instance;
	}

	private function __construct() {
		CB_CPT::boot();
		add_action( 'rest_api_init', array( $this, 'register_routes' ) );
		// Let a valid Bearer token authenticate normal WP REST requests too.
		add_filter( 'determine_current_user', array( $this, 'authenticate_bearer' ), 20 );
		// Keep product Q&A comments out of the normal comment feed/counts.
		add_filter( 'comments_clauses', array( 'CB_Interaction_Controller', 'hide_qna_clauses' ) );
		// A few app endpoints use a host-root path (/api/users/me, /api/addresses)
		// that would fall outside /wp-json/carmilla/v1/. Alias them to the REST
		// routes so the app needs no client-side change.
		add_action( 'parse_request', array( $this, 'maybe_root_alias' ) );
	}

	/**
	 * Dispatch root-level /api/users/me and /api/addresses* to the matching REST
	 * route via rest_do_request, then emit the JSON and stop. No-op for any other
	 * URL, so normal WordPress routing is untouched.
	 */
	public function maybe_root_alias(): void {
		$uri = isset( $_SERVER['REQUEST_URI'] ) ? (string) wp_parse_url( $_SERVER['REQUEST_URI'], PHP_URL_PATH ) : '';
		$uri = '/' . ltrim( untrailingslashit( $uri ), '/' );
		$root_paths = array( '/api/users/me', '/api/addresses', '/api/favorites', '/api/recently-viewed' );
		$match = false;
		foreach ( $root_paths as $rp ) {
			if ( $uri === $rp || strpos( $uri, $rp . '/' ) === 0 ) {
				$match = true;
				break;
			}
		}
		if ( ! $match ) {
			return;
		}

		$method  = isset( $_SERVER['REQUEST_METHOD'] ) ? strtoupper( (string) $_SERVER['REQUEST_METHOD'] ) : 'GET';
		$request = new WP_REST_Request( $method, '/' . CB_REST_NAMESPACE . $uri );
		foreach ( (array) $_GET as $k => $v ) {
			$request->set_param( sanitize_text_field( (string) $k ), $v );
		}
		$body = file_get_contents( 'php://input' );
		if ( $body !== '' && $body !== false ) {
			$request->set_body( $body );
			$request->set_header( 'Content-Type', 'application/json' );
		}

		$response = rest_do_request( $request );
		$server   = rest_get_server();
		$data     = $server->response_to_data( $response, false );

		status_header( $response->get_status() );
		header( 'Content-Type: application/json; charset=utf-8' );
		echo wp_json_encode( $data );
		exit;
	}

	public function register_routes(): void {
		( new CB_Auth_Controller() )->register_routes();
		( new CB_Catalog_Controller() )->register_routes();
		( new CB_Blog_Controller() )->register_routes();
		( new CB_Media_Controller() )->register_routes();
		// Phase 2: full commerce.
		( new CB_Cart_Controller() )->register_routes();
		( new CB_Order_Controller() )->register_routes();
		( new CB_Payment_Controller() )->register_routes();
		( new CB_Wallet_Controller() )->register_routes();
		( new CB_Account_Controller() )->register_routes();
		( new CB_Interaction_Controller() )->register_routes();
		// Phase 3: academy.
		( new CB_Academy_Controller() )->register_routes();
		// Phase 4: clinic + psych tests.
		( new CB_Clinic_Controller() )->register_routes();
		( new CB_Psychtest_Controller() )->register_routes();
		// Phase 5: shop extras.
		( new CB_Extras_Controller() )->register_routes();
		( new CB_Support_Controller() )->register_routes();
		( new CB_Bundle_Controller() )->register_routes();
		( new CB_Story_Controller() )->register_routes();
		( new CB_Course_Request_Controller() )->register_routes();
		// Phase 6: admin panel.
		( new CB_Admin_Controller() )->register_routes();
		( new CB_Admin_Content_Controller() )->register_routes();
		( new CB_Admin_Product_Controller() )->register_routes();
		( new CB_Admin_Clinic_Controller() )->register_routes();
		( new CB_Admin_B2B_Controller() )->register_routes();
		( new CB_Admin_Bundle_Controller() )->register_routes();
	}

	/**
	 * Resolve the WP user id from an Authorization: Bearer <jwt> header.
	 * Runs on determine_current_user so downstream capability checks work.
	 */
	public function authenticate_bearer( $user_id ) {
		if ( ! empty( $user_id ) ) {
			return $user_id; // Already authenticated (cookie/app-password).
		}
		$token = self::bearer_token();
		if ( ! $token ) {
			return $user_id;
		}
		$payload = CB_JWT::decode( $token );
		if ( ! $payload || ( $payload['typ'] ?? '' ) !== 'access' ) {
			return $user_id;
		}
		$uid = (int) ( $payload['uid'] ?? 0 );
		if ( $uid > 0 && get_user_by( 'id', $uid ) ) {
			self::$bearer_user = $uid;
			return $uid;
		}
		return $user_id;
	}

	// ---- static accessors used by controllers -------------------------------

	public static function bearer_token(): ?string {
		$header = '';
		if ( isset( $_SERVER['HTTP_AUTHORIZATION'] ) ) {
			$header = $_SERVER['HTTP_AUTHORIZATION'];
		} elseif ( isset( $_SERVER['REDIRECT_HTTP_AUTHORIZATION'] ) ) {
			$header = $_SERVER['REDIRECT_HTTP_AUTHORIZATION'];
		} elseif ( function_exists( 'getallheaders' ) ) {
			$all = getallheaders();
			foreach ( $all as $k => $v ) {
				if ( strtolower( $k ) === 'authorization' ) {
					$header = $v;
					break;
				}
			}
		}
		if ( $header && stripos( $header, 'Bearer ' ) === 0 ) {
			return trim( substr( $header, 7 ) );
		}
		return null;
	}

	/**
	 * The WP_User for the current request (from Bearer or logged-in cookie), or null.
	 */
	public static function current_user( ?WP_REST_Request $request = null ): ?WP_User {
		$id = get_current_user_id();
		if ( $id ) {
			return get_user_by( 'id', $id );
		}
		return null;
	}

	// ---- permission callbacks ----------------------------------------------

	public static function require_login(): bool {
		return is_user_logged_in();
	}

	/**
	 * Write access = Administrator or Shop Manager (edit_others_posts / manage_woocommerce).
	 */
	public static function require_admin(): bool {
		return current_user_can( 'edit_others_posts' ) || current_user_can( 'manage_woocommerce' );
	}
}
