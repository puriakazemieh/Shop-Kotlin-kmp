<?php
/** Isolated decision logic. This file registers no hooks and emits no headers. */
if ( ! defined( 'ABSPATH' ) ) { exit; }
require_once __DIR__ . '/class-cb-web-session-wp-adapter.php';

final class CB_Web_Request_Policy {
	private $site_url;
	private $site_origin;
	private $cookie_name;
	private $origins;
	private $local_qa;
	private $proxy_ips;
	private $local_qa_client_ips;
	private $site_id;

	/** All arguments are trusted server configuration, never request data. */
	public function __construct( string $site_url, int $blog_id, array $origins, string $environment, bool $enable_local_qa = false, array $proxy_ips = array(), bool $proxy_strips_headers = false, array $local_qa_client_ips = array() ) {
		try {
			if ( ! preg_match( '#\Ahttps?://[a-zA-Z0-9.-]+(?::[0-9]{1,5})?(?:/[a-zA-Z0-9._~/-]*)?\z#i', $site_url ) ) { throw new RuntimeException(); }
			$this->site_url = CB_Web_Session_WP_Adapter::canonical_site_url( $site_url );
			$this->site_origin = self::origin_from_site( $this->site_url );
			$this->site_id = CB_Web_Session_WP_Adapter::site_id( $this->site_url, $blog_id );
			$this->cookie_name = '__Host-carmilla_wp_' . substr( $this->site_id, 0, 32 );
			$this->local_qa = $enable_local_qa && $environment === 'local';
			$site = parse_url( $this->site_origin );
			if ( $site['scheme'] === 'http' && ( ! $this->local_qa || $site['host'] !== 'localhost' || $proxy_ips ) ) { throw new RuntimeException(); }
			if ( $site['scheme'] === 'https' && $enable_local_qa ) { throw new RuntimeException(); }
			if ( ! $origins ) { throw new RuntimeException(); }
			$this->origins = array();
			foreach ( $origins as $origin ) {
				if ( ! is_string( $origin ) ) { throw new RuntimeException(); }
				$normalized = self::origin( $origin );
				$parts = parse_url( $normalized );
				// Conservative same-host subset of same-site; unrelated or
				// unverified sibling subdomains are not accepted.
				if ( $parts['scheme'] !== $site['scheme'] || $parts['host'] !== $site['host'] || isset( $this->origins[ $normalized ] ) ) { throw new RuntimeException(); }
				$this->origins[ $normalized ] = true;
			}
			$this->proxy_ips = array();
			if ( $proxy_ips && ( ! $proxy_strips_headers || $site['scheme'] !== 'https' ) ) { throw new RuntimeException(); }
			foreach ( $proxy_ips as $ip ) {
				if ( ! is_string( $ip ) || ! filter_var( $ip, FILTER_VALIDATE_IP ) || isset( $this->proxy_ips[ $ip ] ) ) { throw new RuntimeException(); }
				$this->proxy_ips[ $ip ] = true;
			}
			$this->local_qa_client_ips = array( '127.0.0.1' => true, '::1' => true );
			if ( $local_qa_client_ips && ! $this->local_qa ) { throw new RuntimeException(); }
			foreach ( $local_qa_client_ips as $ip ) {
				if ( ! is_string( $ip ) || ! filter_var( $ip, FILTER_VALIDATE_IP ) ) { throw new RuntimeException(); }
				$this->local_qa_client_ips[ $ip ] = true;
			}
		} catch ( Throwable $e ) {
			throw new InvalidArgumentException( 'Invalid browser session policy configuration.' );
		}
	}

	/** Trusted installation identity, for checking service configuration. */
	public function site_id(): string { return $this->site_id; }

	/** Cookie options only; the caller must issue after the session transaction. */
	public function cookie( int $absolute_deadline, int $now ): array {
		if ( $now < 0 || $absolute_deadline <= $now ) { throw new InvalidArgumentException( 'Invalid cookie deadline.' ); }
		return array( 'name' => $this->cookie_name, 'expires' => $absolute_deadline, 'path' => '/', 'secure' => true, 'httponly' => true, 'samesite' => 'Strict' );
	}

	/** Same scoped attributes with a past expiry; caller emits only after guard. */
	public function expired_cookie(): array {
		$options = $this->cookie( 2, 1 );
		$options['expires'] = 1;
		return $options;
	}

	/** Input headers must preserve all occurrences, including duplicate names. */
	public function allows_post( string $method, array $headers, array $server ): bool {
		$h = self::headers( $headers );
		if ( $method !== 'POST' || $h === null || ! $this->transport_ok( $h, $server ) || ! $this->allowed_origin( $h ) ) { return false; }
		return isset( $h['content-type'], $h['x-carmilla-web-request'] ) && strtolower( $h['content-type'] ) === 'application/json' && $h['x-carmilla-web-request'] === '1';
	}

	/** Candidate response headers, not emitted by this isolated helper. */
	public function cors_for_post( string $method, array $headers, array $server ): ?array {
		if ( ! $this->allows_post( $method, $headers, $server ) ) { return null; }
		$h = self::headers( $headers );
		return array( 'Access-Control-Allow-Origin' => $h['origin'], 'Access-Control-Allow-Credentials' => 'true', 'Vary' => 'Origin' );
	}

	/** Return candidate CORS headers only for a fully valid preflight. */
	public function preflight( string $method, array $headers, array $server ): ?array {
		$h = self::headers( $headers );
		if ( $method !== 'OPTIONS' || $h === null || ! $this->transport_ok( $h, $server ) || ! $this->allowed_origin( $h ) || ( $h['access-control-request-method'] ?? '' ) !== 'POST' || ! isset( $h['access-control-request-headers'] ) ) { return null; }
		$names = array_map( 'trim', explode( ',', strtolower( $h['access-control-request-headers'] ) ) );
		sort( $names );
		if ( $names !== array( 'content-type', 'x-carmilla-web-request' ) ) { return null; }
		return array( 'Access-Control-Allow-Origin' => $h['origin'], 'Access-Control-Allow-Credentials' => 'true', 'Access-Control-Allow-Methods' => 'POST', 'Access-Control-Allow-Headers' => 'Content-Type, X-Carmilla-Web-Request', 'Vary' => 'Origin, Access-Control-Request-Method, Access-Control-Request-Headers' );
	}

	private function allowed_origin( array $headers ): bool {
		if ( isset( $headers['origin'] ) ) {
			try { return isset( $this->origins[ self::origin( $headers['origin'] ) ] ); } catch ( Throwable $e ) { return false; }
		}
		// Browsers omit Origin on some same-origin POSTs. The endpoint still
		// requires the non-simple request header, and a matching Referer keeps
		// that browser case scoped to an explicitly configured origin. A
		// cross-origin request carries Origin and therefore never reaches this
		// fallback.
		if ( ! isset( $headers['referer'] ) ) { return false; }
		$referer = parse_url( $headers['referer'] );
		if ( ! is_array( $referer ) || ! isset( $referer['scheme'], $referer['host'] ) || isset( $referer['user'], $referer['pass'] ) ) { return false; }
		try {
			$origin = self::origin( $referer['scheme'] . '://' . $referer['host'] . ( isset( $referer['port'] ) ? ':' . $referer['port'] : '' ) );
			return isset( $this->origins[ $origin ] );
		} catch ( Throwable $e ) { return false; }
	}

	private function transport_ok( array $headers, array $server ): bool {
		$remote = $server['REMOTE_ADDR'] ?? null;
		$local = $server['SERVER_ADDR'] ?? null;
		$https = $server['HTTPS'] ?? '';
		if ( ! is_string( $remote ) || ! filter_var( $remote, FILTER_VALIDATE_IP ) || ! is_string( $local ) || ! filter_var( $local, FILTER_VALIDATE_IP ) || ! is_string( $https ) || ! isset( $headers['host'] ) ) { return false; }
		$site = parse_url( $this->site_origin );
		try { if ( self::origin( $site['scheme'] . '://' . $headers['host'] ) !== $this->site_origin ) { return false; } } catch ( Throwable $e ) { return false; }
		// Reject all proxy metadata except a single X-Forwarded-Proto from a
		// configured direct peer whose deployment strips client-supplied values.
		foreach ( $headers as $name => $value ) {
			if ( $name === 'forwarded' || $name === 'x-real-ip' || ( strpos( $name, 'x-forwarded-' ) === 0 && $name !== 'x-forwarded-proto' ) ) { return false; }
		}
		$forwarded = $headers['x-forwarded-proto'] ?? null;
		$direct_tls = $https === 'on' || $https === '1';
		if ( $site['scheme'] === 'http' ) {
			// Apache on Windows commonly reports plain HTTP as "off", while
			// other SAPIs omit HTTPS. Both are non-TLS only in the strict
			// localhost exception; any other value remains denied.
			$loopback = in_array( $remote, array( '127.0.0.1', '::1' ), true );
			return $this->local_qa && ! $direct_tls && ( $https === '' || strtolower( $https ) === 'off' ) && $forwarded === null && isset( $this->local_qa_client_ips[ $remote ] ) && ( ( $loopback && in_array( $local, array( '127.0.0.1', '::1' ), true ) ) || ! $loopback );
		}
		if ( $this->proxy_ips ) {
			// A proxy deployment must not silently fall back to accepting a
			// direct request when the expected peer or protocol is missing.
			return ! $direct_tls && $https === '' && isset( $this->proxy_ips[ $remote ] ) && $forwarded === 'https';
		}
		return $forwarded === null && $direct_tls;
	}

	private static function headers( array $headers ): ?array {
		$result = array();
		foreach ( $headers as $name => $value ) {
			if ( ! is_string( $name ) || ! preg_match( '/\A[!#$%&\'*+.^_`|~0-9a-zA-Z-]+\z/', $name ) ) { return null; }
			$name = strtolower( $name );
			if ( isset( $result[ $name ] ) ) { return null; }
			if ( is_array( $value ) ) { if ( count( $value ) !== 1 ) { return null; } $value = reset( $value ); }
			if ( ! is_string( $value ) || $value === '' || preg_match( '/[\x00-\x1f\x7f]/', $value ) ) { return null; }
			$result[ $name ] = $value;
		}
		return $result;
	}

	private static function origin_from_site( string $url ): string {
		$parts = parse_url( $url );
		return self::origin( $parts['scheme'] . '://' . $parts['host'] . ( isset( $parts['port'] ) ? ':' . $parts['port'] : '' ) );
	}

	private static function origin( string $value ): string {
		if ( ! preg_match( '/\A(https?):\/\/([a-zA-Z0-9.-]+)(?::([0-9]{1,5}))?\z/i', $value, $match ) ) { throw new InvalidArgumentException(); }
		$normalized = CB_Web_Session_WP_Adapter::canonical_site_url( strtolower( $match[1] . '://' . $match[2] ) . ( isset( $match[3] ) ? ':' . $match[3] : '' ) );
		$parts = parse_url( $normalized );
		$port = $parts['port'] ?? null;
		if ( $port === 443 && $parts['scheme'] === 'https' || $port === 80 && $parts['scheme'] === 'http' ) { $port = null; }
		return $parts['scheme'] . '://' . $parts['host'] . ( $port !== null ? ':' . $port : '' );
	}
}
