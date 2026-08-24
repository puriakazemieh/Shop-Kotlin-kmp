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

		// NOTE: GET/PATCH /api/users/me is owned by CB_Account_Controller
		// (ProfileResponse), which is what the app's ProfileApi expects.

		// Password reset + OTP login (all public).
		foreach ( array(
			'forgot-password'          => 'forgot_password',
			'send-login-otp'           => 'send_login_otp',
			'login-with-otp'           => 'login_with_otp',
			'reset-password'           => 'reset_password',
			'reset-password-with-otp'  => 'reset_password_with_otp',
		) as $path => $method ) {
			register_rest_route( $ns, '/api/auth/' . $path, array(
				'methods'             => 'POST',
				'callback'            => array( $this, $method ),
				'permission_callback' => '__return_true',
			) );
		}
	}

	// ---- OTP + password reset ----------------------------------------------

	/** Find a user by their stored mobile (cb_mobile meta or login). */
	private static function user_by_mobile( string $mobile ) {
		$mobile = trim( $mobile );
		if ( $mobile === '' ) {
			return null;
		}
		$users = get_users( array( 'meta_key' => 'cb_mobile', 'meta_value' => $mobile, 'number' => 1 ) );
		if ( ! empty( $users ) ) {
			return $users[0];
		}
		$u = get_user_by( 'login', $mobile );
		return $u ?: null;
	}

	/**
	 * Generate + "send" a 6-digit OTP for a mobile. Sites wire a real SMS gateway
	 * via the cb_send_otp filter; without one the code is stored in a transient
	 * (and returned only when the cb_otp_debug option is on, for testing).
	 */
	private function issue_otp( string $mobile ): string {
		$code = (string) wp_rand( 100000, 999999 );
		set_transient( 'cb_otp_' . md5( $mobile ), $code, 5 * MINUTE_IN_SECONDS );
		do_action( 'cb_send_otp', $mobile, $code );
		return $code;
	}

	private function verify_otp( string $mobile, string $code ): bool {
		$stored = get_transient( 'cb_otp_' . md5( $mobile ) );
		if ( $stored && hash_equals( (string) $stored, trim( $code ) ) ) {
			delete_transient( 'cb_otp_' . md5( $mobile ) );
			return true;
		}
		return false;
	}

	public function send_login_otp( WP_REST_Request $request ) {
		$mobile = trim( (string) $request->get_param( 'mobile' ) );
		if ( $mobile === '' ) {
			return cb_error( 'شماره موبایل الزامی است.', 400, 'VALIDATION' );
		}
		$code = $this->issue_otp( $mobile );
		$out  = array( 'sent' => true );
		if ( get_option( 'cb_otp_debug' ) === '1' ) {
			$out['code'] = $code; // dev only
		}
		return cb_response( $out, 200 );
	}

	public function login_with_otp( WP_REST_Request $request ) {
		$mobile = trim( (string) $request->get_param( 'mobile' ) );
		$code   = (string) $request->get_param( 'otpCode' );
		if ( ! $this->verify_otp( $mobile, $code ) ) {
			return cb_error( 'کد تأیید نادرست یا منقضی است.', 401, 'BAD_OTP' );
		}
		$user = self::user_by_mobile( $mobile );
		if ( ! $user ) {
			// First OTP login registers the mobile-only account.
			$uid = wp_insert_user( array( 'user_login' => $mobile, 'user_pass' => wp_generate_password( 20 ), 'role' => 'customer' ) );
			if ( is_wp_error( $uid ) ) {
				return cb_error( 'ساخت حساب ناموفق بود.', 400, 'REGISTER_FAILED' );
			}
			update_user_meta( $uid, 'cb_mobile', $mobile );
			$user = get_user_by( 'id', $uid );
		}
		return cb_response( self::auth_response( $user ), 200 );
	}

	public function forgot_password( WP_REST_Request $request ) {
		$email  = sanitize_email( (string) $request->get_param( 'email' ) );
		$mobile = trim( (string) $request->get_param( 'mobile' ) );
		if ( $email !== '' ) {
			$user = get_user_by( 'email', $email );
			if ( $user ) {
				// Issue a reset token and email a WP reset link.
				$key = get_password_reset_key( $user );
				if ( ! is_wp_error( $key ) ) {
					set_transient( 'cb_reset_' . $key, $user->ID, HOUR_IN_SECONDS );
					retrieve_password( $user->user_login );
				}
			}
		} elseif ( $mobile !== '' ) {
			$this->issue_otp( $mobile ); // reuse OTP channel for mobile reset
		} else {
			return cb_error( 'ایمیل یا موبایل الزامی است.', 400, 'VALIDATION' );
		}
		// Always 200 so we don't leak which accounts exist.
		return cb_response( array( 'sent' => true ), 200 );
	}

	public function reset_password( WP_REST_Request $request ) {
		$token    = (string) $request->get_param( 'token' );
		$password = (string) $request->get_param( 'newPassword' );
		if ( strlen( $password ) < 6 ) {
			return cb_error( 'رمز عبور حداقل ۶ نویسه باشد.', 400, 'WEAK_PASSWORD' );
		}
		$uid = get_transient( 'cb_reset_' . $token );
		if ( ! $uid ) {
			return cb_error( 'توکن نامعتبر یا منقضی است.', 400, 'INVALID_TOKEN' );
		}
		$user = get_user_by( 'id', (int) $uid );
		if ( ! $user ) {
			return cb_error( 'کاربر یافت نشد.', 404, 'USER_NOT_FOUND' );
		}
		reset_password( $user, $password );
		delete_transient( 'cb_reset_' . $token );
		return cb_response( self::auth_response( get_user_by( 'id', $user->ID ) ), 200 );
	}

	public function reset_password_with_otp( WP_REST_Request $request ) {
		$mobile   = trim( (string) $request->get_param( 'mobile' ) );
		$code     = (string) $request->get_param( 'otpCode' );
		$password = (string) $request->get_param( 'newPassword' );
		if ( strlen( $password ) < 6 ) {
			return cb_error( 'رمز عبور حداقل ۶ نویسه باشد.', 400, 'WEAK_PASSWORD' );
		}
		if ( ! $this->verify_otp( $mobile, $code ) ) {
			return cb_error( 'کد تأیید نادرست یا منقضی است.', 401, 'BAD_OTP' );
		}
		$user = self::user_by_mobile( $mobile );
		if ( ! $user ) {
			return cb_error( 'کاربر یافت نشد.', 404, 'USER_NOT_FOUND' );
		}
		reset_password( $user, $password );
		return cb_response( self::auth_response( get_user_by( 'id', $user->ID ) ), 200 );
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
