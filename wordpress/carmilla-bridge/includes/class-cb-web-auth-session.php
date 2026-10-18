<?php
/** Isolated refresh/logout decisions; registers no routes, hooks or headers. */
if ( ! defined( 'ABSPATH' ) ) { exit; }
require_once __DIR__ . '/class-cb-web-auth-login.php';

final class CB_Web_Auth_Session {
	private $policy;
	private $store;
	private $tokens;
	private $clock;

	public function __construct( CB_Web_Request_Policy $policy, CB_Web_Session_Store $store, CB_Web_Access_Token $tokens, ?callable $clock = null ) {
		if ( ! hash_equals( $policy->site_id(), $store->site_id() ) || ! $tokens->uses_store( $store ) ) { throw new InvalidArgumentException( 'Browser service installation mismatch.' ); }
		$this->policy = $policy; $this->store = $store; $this->tokens = $tokens;
		$this->clock = $clock ?: static function () { return time(); };
	}

	public function refresh( string $method, array $headers, array $server, string $credential ): CB_Web_Auth_Result {
		if ( ! $this->policy->allows_post( $method, $headers, $server ) ) { return new CB_Web_Auth_Result( 403, array( 'error' => 'request_denied' ) ); }
		$rotated = null;
		try {
			$rotated = $this->store->rotate( $credential );
			if ( $rotated === null ) { return new CB_Web_Auth_Result( 401, array( 'error' => 'invalid_session' ) ); }
			$user = get_user_by( 'id', $rotated['user_id'] );
			if ( ! $user instanceof WP_User || ! $user->exists() || (int) $user->ID !== $rotated['user_id'] ) { throw new RuntimeException(); }
			$body = array( 'accessToken' => $this->tokens->issue( $rotated['user_id'], $rotated['family_id'] ), 'user' => CB_Auth_Controller::user_response( $user ) );
			$now = call_user_func( $this->clock );
			if ( ! is_int( $now ) ) { throw new RuntimeException(); }
			$options = $this->policy->cookie( $rotated['absolute_expiry'], $now );
			return new CB_Web_Auth_Result( 200, $body, new CB_Web_Cookie_Handoff( $rotated['credential'], $options ) );
		} catch ( Throwable $e ) {
			// Rotation may already be committed. Try revocation, but neither a
			// successful cleanup nor a durable notification is asserted on error.
			if ( is_array( $rotated ) && isset( $rotated['credential'] ) ) { try { $this->store->revoke( $rotated['credential'] ); } catch ( Throwable $ignored ) {} }
			return new CB_Web_Auth_Result( 503, array( 'error' => 'authentication_unavailable' ) );
		}
	}

	public function logout( string $method, array $headers, array $server, string $credential ): CB_Web_Auth_Result {
		if ( ! $this->policy->allows_post( $method, $headers, $server ) ) { return new CB_Web_Auth_Result( 403, array( 'error' => 'request_denied' ) ); }
		$clear = new CB_Web_Cookie_Handoff( '', $this->policy->expired_cookie() );
		try {
			$this->store->revoke( $credential );
			return new CB_Web_Auth_Result( 200, array( 'ok' => true ), $clear );
		} catch ( Throwable $e ) {
			// Clearing a browser cookie is not proof of server-side revocation.
			return new CB_Web_Auth_Result( 503, array( 'error' => 'authentication_unavailable' ), $clear );
		}
	}
}
