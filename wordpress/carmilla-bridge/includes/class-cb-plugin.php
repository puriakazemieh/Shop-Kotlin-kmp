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
	}

	public function register_routes(): void {
		( new CB_Auth_Controller() )->register_routes();
		( new CB_Catalog_Controller() )->register_routes();
		( new CB_Blog_Controller() )->register_routes();
		( new CB_Media_Controller() )->register_routes();
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
