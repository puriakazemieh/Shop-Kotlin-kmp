<?php
/** Isolated browser access tokens; no hooks, routes or HTTP effects. */
if ( ! defined( 'ABSPATH' ) ) { exit; }
require_once __DIR__ . '/class-cb-web-session-store.php';

final class CB_Web_Access_Token {
	const TTL = 900;
	private $store;
	private $site;
	private $key;
	private $clock;

	/** Master key must be 32 cryptographically random server-only bytes. */
	public function __construct( CB_Web_Session_Store $store, string $site, string $master_key, ?callable $clock = null ) {
		if ( ! preg_match( '/\A[0-9a-f]{64}\z/', $site ) || ! hash_equals( $site, $store->site_id() ) || strlen( $master_key ) !== 32 || preg_match( '/\A(.)\1{31}\z/s', $master_key ) ) {
			throw new InvalidArgumentException( 'Invalid browser token configuration.' );
		}
		$this->store = $store;
		$this->site = $site;
		// Distinct from the legacy mobile JWT key and password fingerprint key.
		$this->key = hash_hmac( 'sha256', "carmilla-web-access-signing-v1\0" . $site, $master_key, true );
		$this->clock = $clock ?: static function () { return time(); };
	}

	/** Trusted wiring check: the issuer and verifier must use the same store. */
	public function uses_store( CB_Web_Session_Store $store ): bool { return $this->store === $store; }

	/** Never issues for an absent, expired, revoked or password-changed family. */
	public function issue( int $user_id, string $family_id ): string {
		try {
			$now = $this->now();
			if ( $user_id < 1 || ! preg_match( '/\A[0-9a-f]{32}\z/', $family_id ) || ! $this->store->is_active( $family_id, $user_id ) ) { throw new RuntimeException(); }
			$header = array( 'alg' => 'HS256', 'typ' => 'JWT' );
			$claims = array( 'typ' => 'carmilla_web_access', 'site' => $this->site, 'uid' => $user_id, 'family' => $family_id, 'iat' => $now, 'exp' => $now + self::TTL );
			$input = self::encode( self::json( $header ) ) . '.' . self::encode( self::json( $claims ) );
			return $input . '.' . self::encode( hash_hmac( 'sha256', $input, $this->key, true ) );
		} catch ( Throwable $e ) {
			throw new RuntimeException( 'Browser token issuance unavailable.' );
		}
	}

	/** Authentication only: capability checks must use the current WP user. */
	public function verify( string $token ): ?int {
		try {
			if ( strlen( $token ) > 2048 ) { return null; }
			$parts = explode( '.', $token );
			if ( count( $parts ) !== 3 ) { return null; }
			$header = self::decode( $parts[0] );
			$body = self::decode( $parts[1] );
			$signature = self::decode( $parts[2] );
			if ( $header !== '{"alg":"HS256","typ":"JWT"}' || ! is_string( $body ) || ! is_string( $signature ) || strlen( $signature ) !== 32 || ! hash_equals( hash_hmac( 'sha256', $parts[0] . '.' . $parts[1], $this->key, true ), $signature ) ) { return null; }
			$claims = json_decode( $body, true );
			// Exact canonical representation rejects duplicate keys and ambiguous
			// JSON, additional claims and type coercion. Tokens are server-issued.
			if ( ! is_array( $claims ) || array_keys( $claims ) !== array( 'typ', 'site', 'uid', 'family', 'iat', 'exp' ) || self::json( $claims ) !== $body || $claims['typ'] !== 'carmilla_web_access' || ! is_string( $claims['site'] ) || ! hash_equals( $this->site, $claims['site'] ) || ! is_int( $claims['uid'] ) || $claims['uid'] < 1 || ! is_string( $claims['family'] ) || ! preg_match( '/\A[0-9a-f]{32}\z/', $claims['family'] ) || ! is_int( $claims['iat'] ) || ! is_int( $claims['exp'] ) ) { return null; }
			$now = $this->now();
			if ( $claims['iat'] < 1 || $claims['iat'] > $now || $claims['iat'] > PHP_INT_MAX - self::TTL || $claims['exp'] <= $claims['iat'] || $claims['exp'] > $claims['iat'] + self::TTL || $now >= $claims['exp'] || ! $this->store->is_active( $claims['family'], $claims['uid'] ) ) { return null; }
			return $claims['uid'];
		} catch ( Throwable $e ) {
			// Invalid state, storage and clock failures all deny authentication.
			return null;
		}
	}

	/** The trusted caller must perform a fresh server-side WP capability check. */
	public function authorize( string $token, string $capability, callable $current_user_can ): bool {
		if ( ! preg_match( '/\A[a-zA-Z0-9_]{1,100}\z/', $capability ) ) { return false; }
		$user_id = $this->verify( $token );
		if ( $user_id === null ) { return false; }
		try { return $current_user_can( $user_id, $capability ) === true; } catch ( Throwable $e ) { return false; }
	}

	private function now(): int {
		$now = call_user_func( $this->clock );
		if ( ! is_int( $now ) || $now < 1 || $now > PHP_INT_MAX - self::TTL ) { throw new RuntimeException(); }
		return $now;
	}
	private static function json( array $data ): string {
		$json = json_encode( $data, JSON_UNESCAPED_SLASHES );
		if ( ! is_string( $json ) ) { throw new RuntimeException(); }
		return $json;
	}
	private static function encode( string $data ): string { return rtrim( strtr( base64_encode( $data ), '+/', '-_' ), '=' ); }
	private static function decode( string $text ): ?string {
		if ( $text === '' || ! preg_match( '/\A[A-Za-z0-9_-]+\z/', $text ) || strlen( $text ) % 4 === 1 ) { return null; }
		$raw = base64_decode( strtr( $text, '-_', '+/' ) . str_repeat( '=', ( 4 - strlen( $text ) % 4 ) % 4 ), true );
		return is_string( $raw ) && self::encode( $raw ) === $text ? $raw : null;
	}
}
