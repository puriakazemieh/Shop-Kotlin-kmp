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
			$policy = new CB_Web_Request_Policy( CB_WEB_SESSION_SITE_URL, get_current_blog_id(), CB_WEB_SESSION_ALLOWED_ORIGINS, CB_WEB_SESSION_ENVIRONMENT, $local );
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

	private function __construct( CB_Web_Request_Policy $policy, CB_Web_Password_Login $login, CB_Web_Browser_Registration $registration, CB_Web_Auth_Session $session ) {
		$this->policy = $policy; $this->login = $login; $this->registration = $registration; $this->session = $session;
	}

	/** Register only after configuration and the read-only migration-marker check succeed. */
	public function register_routes(): void {
		$ns = CB_REST_NAMESPACE;
		foreach ( array( 'login', 'register', 'refresh', 'logout' ) as $route ) {
			register_rest_route( $ns, '/api/auth/web/' . $route, array( 'methods' => 'POST', 'callback' => array( $this, $route ), 'permission_callback' => '__return_true' ) );
		}
	}

	public function login( WP_REST_Request $request ) { return $this->respond( $this->login->login( 'POST', $this->headers( $request ), $this->server(), $this->json( $request ) ) ); }
	public function register( WP_REST_Request $request ) { return $this->respond( $this->registration->register( 'POST', $this->headers( $request ), $this->server(), $this->json( $request ) ) ); }
	public function refresh( WP_REST_Request $request ) { return $this->respond( $this->session->refresh( 'POST', $this->headers( $request ), $this->server(), $this->credential() ) ); }
	public function logout( WP_REST_Request $request ) { return $this->respond( $this->session->logout( 'POST', $this->headers( $request ), $this->server(), $this->credential() ) ); }

	private function json( WP_REST_Request $request ): array { $data = $request->get_json_params(); return is_array( $data ) ? $data : array(); }
	private function server(): array { return array( 'REMOTE_ADDR' => $_SERVER['REMOTE_ADDR'] ?? '', 'SERVER_ADDR' => $_SERVER['SERVER_ADDR'] ?? '', 'HTTPS' => $_SERVER['HTTPS'] ?? '' ); }
	private function headers( WP_REST_Request $request ): array {
		$headers = $request->get_headers(); $result = array();
		foreach ( $headers as $name => $values ) { $result[strtolower( str_replace( '_', '-', $name ) )] = is_array( $values ) && count( $values ) === 1 ? reset( $values ) : $values; }
		if ( ! isset( $result['host'] ) && isset( $_SERVER['HTTP_HOST'] ) && is_string( $_SERVER['HTTP_HOST'] ) ) { $result['host'] = $_SERVER['HTTP_HOST']; }
		return $result;
	}
	private function credential(): string {
		$options = $this->policy->expired_cookie(); $name = $options['name'];
		return isset( $_COOKIE[$name] ) && is_string( $_COOKIE[$name] ) ? $_COOKIE[$name] : '';
	}
	private function respond( CB_Web_Auth_Result $result ) {
		$status = $result->status(); $body = $result->body();
		if ( $status >= 400 ) {
			$map = array( 'request_denied' => array( 'درخواست مرورگر مجاز نیست.', 'WEB_REQUEST_DENIED' ), 'invalid_input' => array( 'اطلاعات ورود نامعتبر است.', 'INVALID_INPUT' ), 'invalid_credentials' => array( 'ایمیل یا رمز عبور اشتباه است.', 'BAD_CREDENTIALS' ), 'identity_unavailable' => array( 'این ایمیل یا شماره موبایل قبلاً استفاده شده است.', 'IDENTITY_UNAVAILABLE' ), 'invalid_session' => array( 'نشست ورود نامعتبر یا منقضی شده است.', 'INVALID_SESSION' ), 'account_recovery_required' => array( 'ایجاد حساب کامل نشد؛ دوباره تلاش کنید.', 'ACCOUNT_RECOVERY_REQUIRED' ), 'authentication_unavailable' => array( 'ورود موقتاً در دسترس نیست. دوباره تلاش کنید.', 'AUTH_UNAVAILABLE' ) );
			$item = $map[$body['error'] ?? ''] ?? array( 'درخواست قابل انجام نیست.', 'WEB_AUTH_ERROR' );
			$response = cb_error( $item[0], $status, $item[1] );
		} else { $response = cb_response( $body, $status ); }
		$response->header( 'Cache-Control', 'no-store' );
		$cors = $this->policy->cors_for_post( 'POST', $this->headers_from_server(), $this->server() );
		if ( is_array( $cors ) ) { foreach ( $cors as $name => $value ) { $response->header( $name, $value ); } }
		$handoff = $result->cookie();
		if ( $handoff !== null && $status < 500 ) {
			$cookie = $handoff->take();
			if ( is_array( $cookie ) && ! headers_sent() ) { setcookie( $cookie['options']['name'], $cookie['value'], $cookie['options'] ); }
		}
		return $response;
	}
	private function headers_from_server(): array {
		$result = array(); foreach ( $_SERVER as $name => $value ) { if ( strpos( $name, 'HTTP_' ) === 0 && is_string( $value ) ) { $result[strtolower( str_replace( '_', '-', substr( $name, 5 ) ) )] = $value; } }
		return $result;
	}
}
