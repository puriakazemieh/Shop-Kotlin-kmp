<?php
/** Isolated browser login primitives. Registers no route, hook or HTTP header. */
if ( ! defined( 'ABSPATH' ) ) { exit; }
require_once __DIR__ . '/class-cb-web-request-policy.php';
require_once __DIR__ . '/class-cb-web-access-token.php';
require_once __DIR__ . '/class-cb-jwt.php';
require_once __DIR__ . '/class-cb-auth-controller.php';

/** Only trusted HTTP adapter code may consume this one-time cookie handoff. */
final class CB_Web_Cookie_Handoff {
	private $consume;
	public function __construct( string $credential, array $options ) {
		$this->consume = static function () use ( $credential, $options ) { return array( 'value' => $credential, 'options' => $options ); };
	}
	public function take(): ?array {
		if ( $this->consume === null ) { return null; }
		$consume = $this->consume; $this->consume = null;
		return $consume();
	}
	public function __debugInfo(): array { return array( 'cookie' => '[redacted]' ); }
	public function __serialize(): array { throw new LogicException( 'Cookie handoff cannot be serialized.' ); }
	public function __unserialize( array $data ): void { throw new LogicException( 'Cookie handoff cannot be unserialized.' ); }
	private function __clone() {}
}

/** Public body and cookie are deliberately separate; do not log the handoff. */
final class CB_Web_Auth_Result {
	private $status;
	private $body;
	private $cookie;
	public function __construct( int $status, array $body, ?CB_Web_Cookie_Handoff $cookie = null ) { $this->status = $status; $this->body = $body; $this->cookie = $cookie; }
	public function status(): int { return $this->status; }
	public function body(): array { return $this->body; }
	public function cookie(): ?CB_Web_Cookie_Handoff { return $this->cookie; }
	public function __debugInfo(): array { return array( 'status' => $this->status, 'body' => '[redacted]', 'cookie' => '[redacted]' ); }
	public function __serialize(): array { throw new LogicException( 'Authentication result cannot be serialized.' ); }
	public function __unserialize( array $data ): void { throw new LogicException( 'Authentication result cannot be unserialized.' ); }
	private function __clone() {}
}

/** Trusted internal issuer; callers must authorize and authenticate first. */
final class CB_Web_Auth_Session_Issuer {
	private $policy;
	private $store;
	private $tokens;
	private $clock;
	public function __construct( CB_Web_Request_Policy $policy, CB_Web_Session_Store $store, CB_Web_Access_Token $tokens, ?callable $clock = null ) {
		if ( ! hash_equals( $policy->site_id(), $store->site_id() ) ) { throw new InvalidArgumentException( 'Browser service installation mismatch.' ); }
		$this->policy = $policy; $this->store = $store; $this->tokens = $tokens;
		$this->clock = $clock ?: static function () { return time(); };
	}
	public function site_id(): string { return $this->store->site_id(); }
	public function issue_for( WP_User $user ): CB_Web_Auth_Result {
		$session = null;
		try {
			$current = get_user_by( 'id', (int) $user->ID );
			if ( ! $current instanceof WP_User || ! $current->exists() ) { return self::failure(); }
			$profile = CB_Auth_Controller::user_response( $current );
			if ( ! is_string( $user->user_pass ) || $user->user_pass === '' ) { return self::failure(); }
			$session = $this->store->create( (int) $current->ID, $user->user_pass );
			if ( $session === null ) { return self::failure(); }
			$access = $this->tokens->issue( (int) $current->ID, $session['family_id'] );
			$now = call_user_func( $this->clock );
			if ( ! is_int( $now ) ) { throw new RuntimeException(); }
			$options = $this->policy->cookie( $session['absolute_expiry'], $now );
			return new CB_Web_Auth_Result( 200, array( 'accessToken' => $access, 'user' => $profile ), new CB_Web_Cookie_Handoff( $session['credential'], $options ) );
		} catch ( Throwable $e ) {
			// A committed family may remain on storage failure. Never claim that
			// revocation succeeded and never return its credential to the client.
			if ( is_array( $session ) && isset( $session['credential'] ) ) {
				try { $this->store->revoke( $session['credential'] ); } catch ( Throwable $ignored ) {}
			}
			return self::failure();
		}
	}
	private static function failure(): CB_Web_Auth_Result { return new CB_Web_Auth_Result( 503, array( 'error' => 'authentication_unavailable' ) ); }
}

final class CB_Web_Password_Login {
	private $policy;
	private $issuer;
	public function __construct( CB_Web_Request_Policy $policy, CB_Web_Auth_Session_Issuer $issuer ) {
		if ( ! hash_equals( $policy->site_id(), $issuer->site_id() ) ) { throw new InvalidArgumentException( 'Browser service installation mismatch.' ); }
		$this->policy = $policy; $this->issuer = $issuer;
	}
	public function login( string $method, array $headers, array $server, array $input ): CB_Web_Auth_Result {
		if ( ! $this->policy->allows_post( $method, $headers, $server ) ) { return new CB_Web_Auth_Result( 403, array( 'error' => 'request_denied' ) ); }
		$username = $input['username'] ?? null; $password = $input['password'] ?? null;
		if ( ! is_string( $username ) || ! is_string( $password ) || trim( $username ) === '' || $password === '' || strlen( $username ) > 255 || strlen( $password ) > 4096 ) { return new CB_Web_Auth_Result( 400, array( 'error' => 'invalid_input' ) ); }
		try {
			$identifier = trim( $username );
			global $wpdb;
			// WP's login, email and usermeta indexes are not unique. Check all
			// three namespaces together using the database's own collation.
			$sql = $wpdb->prepare( "SELECT DISTINCT u.ID FROM {$wpdb->users} u LEFT JOIN {$wpdb->usermeta} m ON m.user_id=u.ID AND m.meta_key='cb_mobile' WHERE u.user_login=%s OR u.user_email=%s OR m.meta_value=%s LIMIT 2", $identifier, $identifier, $identifier );
			$matches = $wpdb->get_col( $sql );
			if ( $wpdb->last_error !== '' ) { throw new RuntimeException(); }
			if ( count( $matches ) !== 1 ) { return new CB_Web_Auth_Result( 401, array( 'error' => 'invalid_credentials' ) ); }
			$matched = get_user_by( 'id', (int) $matches[0] );
			if ( ! $matched instanceof WP_User || ! $matched->exists() ) { return new CB_Web_Auth_Result( 401, array( 'error' => 'invalid_credentials' ) ); }
			$user = wp_authenticate( $matched->user_login, $password );
			if ( is_wp_error( $user ) || ! $user instanceof WP_User || ! $user->exists() || (int) $user->ID !== (int) $matched->ID ) { return new CB_Web_Auth_Result( 401, array( 'error' => 'invalid_credentials' ) ); }
			return $this->issuer->issue_for( $user );
		} catch ( Throwable $e ) {
			return new CB_Web_Auth_Result( 503, array( 'error' => 'authentication_unavailable' ) );
		}
	}
}
