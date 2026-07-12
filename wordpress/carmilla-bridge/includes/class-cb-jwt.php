<?php
/**
 * Minimal dependency-free HS256 JWT, mirroring the Spring server's token claims
 * (sub=email, uid=userId, role, typ=access|refresh). Access TTL 15 min, refresh 30 days.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_JWT {

	const ACCESS_TTL  = 900;        // 15 minutes
	const REFRESH_TTL = 2592000;    // 30 days

	/**
	 * Secret key: define('CB_JWT_SECRET', '...') in wp-config.php, else fall back to AUTH_KEY.
	 */
	public static function secret(): string {
		if ( defined( 'CB_JWT_SECRET' ) && CB_JWT_SECRET ) {
			return CB_JWT_SECRET;
		}
		if ( defined( 'AUTH_KEY' ) && AUTH_KEY ) {
			return AUTH_KEY;
		}
		return 'carmilla-bridge-insecure-default-change-me';
	}

	private static function base64url_encode( string $data ): string {
		return rtrim( strtr( base64_encode( $data ), '+/', '-_' ), '=' );
	}

	private static function base64url_decode( string $data ): string {
		return base64_decode( strtr( $data, '-_', '+/' ) );
	}

	/**
	 * Issue a signed token for a user.
	 */
	public static function issue( WP_User $user, string $type = 'access' ): string {
		$now = time();
		$ttl = ( $type === 'refresh' ) ? self::REFRESH_TTL : self::ACCESS_TTL;

		$payload = array(
			'sub'  => $user->user_email,
			'uid'  => (int) $user->ID,
			'role' => self::primary_role( $user ),
			'typ'  => $type,
			'iat'  => $now,
			'exp'  => $now + $ttl,
		);

		$header  = array( 'alg' => 'HS256', 'typ' => 'JWT' );
		$segments = array(
			self::base64url_encode( wp_json_encode( $header ) ),
			self::base64url_encode( wp_json_encode( $payload ) ),
		);
		$signing_input = implode( '.', $segments );
		$signature     = hash_hmac( 'sha256', $signing_input, self::secret(), true );
		$segments[]    = self::base64url_encode( $signature );

		return implode( '.', $segments );
	}

	/**
	 * Verify and decode a token. Returns the payload array or null when invalid/expired.
	 */
	public static function decode( string $jwt ): ?array {
		$parts = explode( '.', $jwt );
		if ( count( $parts ) !== 3 ) {
			return null;
		}
		list( $head64, $body64, $sig64 ) = $parts;

		$expected = hash_hmac( 'sha256', "$head64.$body64", self::secret(), true );
		$provided = self::base64url_decode( $sig64 );
		if ( ! hash_equals( $expected, $provided ) ) {
			return null;
		}

		$payload = json_decode( self::base64url_decode( $body64 ), true );
		if ( ! is_array( $payload ) ) {
			return null;
		}
		if ( isset( $payload['exp'] ) && time() >= (int) $payload['exp'] ) {
			return null;
		}
		return $payload;
	}

	/**
	 * Map WP roles to the app's CUSTOMER / ADMIN convention.
	 * administrator + shop_manager => ADMIN (may write), everyone else => CUSTOMER.
	 */
	public static function primary_role( WP_User $user ): string {
		$roles = (array) $user->roles;
		if ( in_array( 'administrator', $roles, true ) || in_array( 'shop_manager', $roles, true ) ) {
			return 'ADMIN';
		}
		return 'CUSTOMER';
	}
}
