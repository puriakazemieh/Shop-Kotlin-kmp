<?php
/**
 * Auth endpoints mirroring the Spring AuthController, backed by WordPress users.
 *   POST api/auth/login       { username, password } -> AuthResponse
 *   POST api/auth/register    { email?, mobile?, password } -> AuthResponse
 *   POST api/auth/refresh     { refreshToken } -> RefreshTokenResponse
 *   GET  api/users/me         (Bearer) -> UserResponse
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Auth_Controller {

	public function register_routes(): void {
		$ns = CB_REST_NAMESPACE;

		register_rest_route( $ns, '/api/auth/login', array(
			'methods'             => 'POST',
			'callback'            => array( $this, 'login' ),
			'permission_callback' => '__return_true',
		) );

		register_rest_route( $ns, '/api/auth/register', array(
			'methods'             => 'POST',
			'callback'            => array( $this, 'register' ),
			'permission_callback' => '__return_true',
		) );

		register_rest_route( $ns, '/api/auth/refresh', array(
			'methods'             => 'POST',
			'callback'            => array( $this, 'refresh' ),
			'permission_callback' => '__return_true',
		) );

		register_rest_route( $ns, '/api/users/me', array(
			'methods'             => 'GET',
			'callback'            => array( $this, 'me' ),
			'permission_callback' => array( 'CB_Plugin', 'require_login' ),
		) );
	}

	public function login( WP_REST_Request $request ) {
		$username = trim( (string) $request->get_param( 'username' ) );
		$password = (string) $request->get_param( 'password' );

		if ( $username === '' || $password === '' ) {
			return cb_error( 'نام کاربری و رمز عبور الزامی است.', 400, 'VALIDATION' );
		}

		// Allow login by email, username, or mobile (billing phone / cb_mobile meta).
		$login = self::resolve_login( $username );

		$user = wp_authenticate( $login, $password );
		if ( is_wp_error( $user ) ) {
			return cb_error( 'نام کاربری یا رمز عبور نادرست است.', 401, 'BAD_CREDENTIALS' );
		}

		return cb_response( self::auth_response( $user ), 200 );
	}

	public function register( WP_REST_Request $request ) {
		$email    = sanitize_email( (string) $request->get_param( 'email' ) );
		$mobile   = trim( (string) $request->get_param( 'mobile' ) );
		$password = (string) $request->get_param( 'password' );

		if ( $password === '' || ( $email === '' && $mobile === '' ) ) {
			return cb_error( 'ایمیل یا موبایل به‌همراه رمز عبور الزامی است.', 400, 'VALIDATION' );
		}

		$login = $email !== '' ? $email : $mobile;
		if ( username_exists( $login ) || ( $email && email_exists( $email ) ) ) {
			return cb_error( 'این کاربر قبلاً ثبت شده است.', 409, 'USER_EXISTS' );
		}

		$user_id = wp_insert_user( array(
			'user_login' => $login,
			'user_email' => $email ?: '',
			'user_pass'  => $password,
			'role'       => 'customer',
		) );
		if ( is_wp_error( $user_id ) ) {
			return cb_error( $user_id->get_error_message(), 400, 'REGISTER_FAILED' );
		}
		if ( $mobile !== '' ) {
			update_user_meta( $user_id, 'cb_mobile', $mobile );
		}

		return cb_response( self::auth_response( get_user_by( 'id', $user_id ) ), 201 );
	}

	public function refresh( WP_REST_Request $request ) {
		$token   = (string) $request->get_param( 'refreshToken' );
		$payload = CB_JWT::decode( $token );

		if ( ! $payload || ( $payload['typ'] ?? '' ) !== 'refresh' ) {
			return cb_error( 'توکن نامعتبر یا منقضی‌شده است.', 401, 'INVALID_REFRESH_TOKEN' );
		}
		$user = get_user_by( 'id', (int) ( $payload['uid'] ?? 0 ) );
		if ( ! $user ) {
			return cb_error( 'کاربر یافت نشد.', 401, 'USER_NOT_FOUND' );
		}

		return cb_response( array(
			'accessToken'  => CB_JWT::issue( $user, 'access' ),
			'refreshToken' => CB_JWT::issue( $user, 'refresh' ),
		), 200 );
	}

	public function me( WP_REST_Request $request ) {
		$user = CB_Plugin::current_user( $request );
		if ( ! $user ) {
			return cb_error( 'احراز هویت لازم است.', 401, 'UNAUTHORIZED' );
		}
		return cb_response( self::user_response( $user ), 200 );
	}

	// ---- helpers ------------------------------------------------------------

	private static function resolve_login( string $identifier ): string {
		if ( is_email( $identifier ) ) {
			$u = get_user_by( 'email', $identifier );
			return $u ? $u->user_login : $identifier;
		}
		// Look up by stored mobile meta.
		$users = get_users( array(
			'meta_key'   => 'cb_mobile',
			'meta_value' => $identifier,
			'number'     => 1,
			'fields'     => array( 'user_login' ),
		) );
		if ( ! empty( $users ) ) {
			return $users[0]->user_login;
		}
		return $identifier;
	}

	public static function auth_response( WP_User $user ): array {
		return array(
			'accessToken'  => CB_JWT::issue( $user, 'access' ),
			'refreshToken' => CB_JWT::issue( $user, 'refresh' ),
			'user'         => self::user_response( $user ),
		);
	}

	public static function user_response( WP_User $user ): array {
		$postal = get_user_meta( $user->ID, 'cb_postal_code', true );
		return array(
			'id'         => (int) $user->ID,
			'email'      => $user->user_email ?: null,
			'mobile'     => get_user_meta( $user->ID, 'cb_mobile', true ) ?: null,
			'fullName'   => trim( $user->display_name ) ?: null,
			'firstName'  => $user->first_name ?: null,
			'lastName'   => $user->last_name ?: null,
			'phone'      => get_user_meta( $user->ID, 'billing_phone', true ) ?: null,
			'city'       => get_user_meta( $user->ID, 'billing_city', true ) ?: null,
			'postalCode' => $postal !== '' ? (int) $postal : null,
			'role'       => CB_JWT::primary_role( $user ),
			'active'     => true,
		);
	}
}
