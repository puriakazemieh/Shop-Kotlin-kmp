<?php
/** Explicit, fail-closed REST adapter for the isolated browser-session services. */
if ( ! defined( 'ABSPATH' ) ) { exit; }
require_once __DIR__ . '/class-cb-web-auth-registration.php';
require_once __DIR__ . '/class-cb-web-auth-session.php';
require_once __DIR__ . '/class-cb-web-session-migration.php';

final class CB_Web_Auth_Controller {
	private $policy;
	private $login;
	private $registration;
	private $session;

	/** Returns null unless an operator explicitly supplies all trusted config. */
	public static function from_runtime(): ?self {
		try {
			if ( ! defined( 'CB_WEB_SESSION_ENABLED' ) || CB_WEB_SESSION_ENABLED !== true || ! defined( 'CB_WEB_SESSION_ENVIRONMENT' ) || ! is_string( CB_WEB_SESSION_ENVIRONMENT ) || ! defined( 'CB_WEB_SESSION_ALLOWED_ORIGINS' ) || ! is_array( CB_WEB_SESSION_ALLOWED_ORIGINS ) || ! defined( 'CB_WEB_SESSION_TRUSTED_INGRESS' ) || CB_WEB_SESSION_TRUSTED_INGRESS !== true ) { return null; }
			$local = defined( 'CB_WEB_SESSION_LOCAL_QA' ) && CB_WEB_SESSION_LOCAL_QA === true;
			$local_clients = defined( 'CB_WEB_SESSION_LOCAL_QA_CLIENT_IPS' ) && is_array( CB_WEB_SESSION_LOCAL_QA_CLIENT_IPS ) ? CB_WEB_SESSION_LOCAL_QA_CLIENT_IPS : array();
			$policy = new CB_Web_Request_Policy( CB_WEB_SESSION_SITE_URL, get_current_blog_id(), CB_WEB_SESSION_ALLOWED_ORIGINS, CB_WEB_SESSION_ENVIRONMENT, $local, array(), false, $local_clients );
			$store = CB_Web_Session_WP_Adapter::from_wordpress();
			global $wpdb;
			$marker = $wpdb->get_var( "SELECT version FROM `{$wpdb->prefix}carmilla_web_schema` WHERE id=1" );
			if ( $wpdb->last_error !== '' || (int) $marker !== 1 ) { throw new RuntimeException(); }
			$key = hex2bin( CB_WEB_SESSION_SECRET_HEX );
			if ( $key === false ) { throw new RuntimeException(); }
			$tokens = new CB_Web_Access_Token( $store, $policy->site_id(), $key );
			$issuer = new CB_Web_Auth_Session_Issuer( $policy, $store, $tokens );
			return new self( $policy, new CB_Web_Password_Login( $policy, $issuer ), new CB_Web_Browser_Registration( $policy, array( $issuer, 'issue_for' ) ), new CB_Web_Auth_Session( $policy, $store, $tokens ) );
		} catch ( Throwable $ignored ) { return null; }
	}

	/**
	 * Verifies only the short-lived browser access-token format.  This is used
	 * by the normal REST authentication bridge after the web-session adapter
	 * has been explicitly enabled and its schema marker has been validated.
	 */
	public static function verify_runtime_access_token( string $token ): ?int {
		try {
			if ( $token === '' || ! defined( 'CB_WEB_SESSION_ENABLED' ) || CB_WEB_SESSION_ENABLED !== true ) {
				return null;
			}
			$controller = self::from_runtime();
			if ( ! $controller instanceof self ) {
				return null;
			}
			$policy = $controller->policy;
			$store = CB_Web_Session_WP_Adapter::from_wordpress();
			$key = hex2bin( CB_WEB_SESSION_SECRET_HEX );
			if ( $key === false ) {
				return null;
			}
			return ( new CB_Web_Access_Token( $store, $policy->site_id(), $key ) )->verify( $token );
		} catch ( Throwable $ignored ) {
			return null;
		}
	}

	private function __construct( CB_Web_Request_Policy $policy, CB_Web_Password_Login $login, CB_Web_Browser_Registration $registration, CB_Web_Auth_Session $session ) {
		$this->policy = $policy; $this->login = $login; $this->registration = $registration; $this->session = $session;
	}

	/** Register only after configuration and the read-only migration-marker check succeed. */
	public function register_routes(): void {
		$ns = CB_REST_NAMESPACE;
		foreach ( array( 'login' => 'login', 'register' => 'register', 'refresh' => 'refresh', 'logout' => 'logout', 'forgot-password' => 'forgot_password', 'reset-password' => 'reset_password', 'reset-password-with-otp' => 'reset_password_with_otp' ) as $route => $method ) {
			register_rest_route( $ns, '/api/auth/web/' . $route, array( 'methods' => 'POST', 'callback' => array( $this, $method ), 'permission_callback' => '__return_true' ) );
		}
	}

	public function login( WP_REST_Request $request ) { $headers = $this->headers( $request ); return $this->respond( $this->login->login( 'POST', $headers, $this->server(), $this->json( $request ) ), $headers ); }
	public function register( WP_REST_Request $request ) { $headers = $this->headers( $request ); return $this->respond( $this->registration->register( 'POST', $headers, $this->server(), $this->json( $request ) ), $headers ); }
	public function refresh( WP_REST_Request $request ) { $headers = $this->headers( $request ); return $this->respond( $this->session->refresh( 'POST', $headers, $this->server(), $this->credential() ), $headers ); }
	public function logout( WP_REST_Request $request ) { $headers = $this->headers( $request ); return $this->respond( $this->session->logout( 'POST', $headers, $this->server(), $this->credential() ), $headers ); }
	public function forgot_password( WP_REST_Request $request ) { return $this->recovery( $request, 'forgot_password' ); }
	public function reset_password( WP_REST_Request $request ) { return $this->recovery( $request, 'reset_password' ); }
	public function reset_password_with_otp( WP_REST_Request $request ) { return $this->recovery( $request, 'reset_password_with_otp' ); }

	/** Recovery reuses the already-reviewed WordPress handler; it never issues a browser session. */
	private function recovery( WP_REST_Request $request, string $method ) {
		if ( ! $this->policy->allows_post( 'POST', $this->headers( $request ), $this->server() ) ) { return $this->respond( new CB_Web_Auth_Result( 403, array( 'error' => 'request_denied' ) ) ); }
		try {
			$response = call_user_func( array( new CB_Auth_Controller(), $method ), $request );
			if ( ! $response instanceof WP_REST_Response ) { throw new RuntimeException(); }
			$response->header( 'Cache-Control', 'no-store' );
			$cors = $this->policy->cors_for_post( 'POST', $this->headers( $request ), $this->server() );
			if ( is_array( $cors ) ) { foreach ( $cors as $name => $value ) { $response->header( $name, $value ); } }
			return $response;
		} catch ( Throwable $ignored ) { return $this->respond( new CB_Web_Auth_Result( 503, array( 'error' => 'authentication_unavailable' ) ) ); }
	}

	private function json( WP_REST_Request $request ): array { $data = $request->get_json_params(); return is_array( $data ) ? $data : array(); }
	private function server(): array { return array( 'REMOTE_ADDR' => $_SERVER['REMOTE_ADDR'] ?? '', 'SERVER_ADDR' => $_SERVER['SERVER_ADDR'] ?? '', 'HTTPS' => $_SERVER['HTTPS'] ?? '' ); }
	private function headers( WP_REST_Request $request ): array {
		$headers = $request->get_headers(); $result = array();
		foreach ( $headers as $name => $values ) {
			$name = strtolower( str_replace( '_', '-', $name ) );
			$value = is_array( $values ) && count( $values ) === 1 ? reset( $values ) : $values;
			// WP_REST_Request synthesizes an empty authorization entry when no
			// Authorization header exists. It is not an input header and must not
			// make every otherwise-valid browser request fail closed.
			if ( $name === 'authorization' && $value === '' ) { continue; }
			$result[$name] = $value;
		}
		if ( ! isset( $result['host'] ) && isset( $_SERVER['HTTP_HOST'] ) && is_string( $_SERVER['HTTP_HOST'] ) ) { $result['host'] = $_SERVER['HTTP_HOST']; }
		return $result;
	}
	private function credential(): string {
		$options = $this->policy->expired_cookie(); $name = $options['name'];
		return isset( $_COOKIE[$name] ) && is_string( $_COOKIE[$name] ) ? $_COOKIE[$name] : '';
	}
	private function respond( CB_Web_Auth_Result $result, ?array $request_headers = null ) {
		$status = $result->status(); $body = $result->body();
		if ( $status >= 400 ) {
			$map = array( 'request_denied' => array( 'درخواست مرورگر مجاز نیست.', 'WEB_REQUEST_DENIED' ), 'invalid_input' => array( 'اطلاعات ورود نامعتبر است.', 'INVALID_INPUT' ), 'invalid_credentials' => array( 'ایمیل یا رمز عبور اشتباه است.', 'BAD_CREDENTIALS' ), 'identity_unavailable' => array( 'این ایمیل یا شماره موبایل قبلاً استفاده شده است.', 'IDENTITY_UNAVAILABLE' ), 'invalid_session' => array( 'نشست ورود نامعتبر یا منقضی شده است.', 'INVALID_SESSION' ), 'account_recovery_required' => array( 'ایجاد حساب کامل نشد؛ دوباره تلاش کنید.', 'ACCOUNT_RECOVERY_REQUIRED' ), 'authentication_unavailable' => array( 'ورود موقتاً در دسترس نیست. دوباره تلاش کنید.', 'AUTH_UNAVAILABLE' ) );
			$item = $map[$body['error'] ?? ''] ?? array( 'درخواست قابل انجام نیست.', 'WEB_AUTH_ERROR' );
			$response = cb_error( $item[0], $status, $item[1] );
		} else { $response = cb_response( $body, $status ); }
		$response->header( 'Cache-Control', 'no-store' );
		$cors = $this->policy->cors_for_post( 'POST', $request_headers ?? $this->headers_from_server(), $this->server() );
		if ( is_array( $cors ) ) { foreach ( $cors as $name => $value ) { $response->header( $name, $value ); } }
		$handoff = $result->cookie();
		if ( $handoff !== null && $status < 500 ) {
			$cookie = $handoff->take();
			if ( is_array( $cookie ) && ! headers_sent() ) {
				$options = $cookie['options'];
				$name = $options['name'] ?? null;
				unset( $options['name'] );
				if ( is_string( $name ) && $name !== '' ) { setcookie( $name, $cookie['value'], $options ); }
			}
		}
		return $response;
	}
	private function headers_from_server(): array {
		$result = array(); foreach ( $_SERVER as $name => $value ) { if ( strpos( $name, 'HTTP_' ) === 0 && is_string( $value ) ) { $result[strtolower( str_replace( '_', '-', substr( $name, 5 ) ) )] = $value; } }
		return $result;
	}
}
