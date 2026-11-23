<?php
namespace Carmilla\Core\Rest;

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

if ( ! class_exists( 'Carmilla\Core\Rest\JwtService', false ) ) {

/**
 * Shared JWT Service for Carmilla Core
 *
 * Provides dependency-free HS256 JWT token generation and verification,
 * mirroring Spring server token claims (sub=email, uid=userId, role, typ=access|refresh).
 * Usable by Carmilla Theme, Carmilla Bridge, and standalone KMP clients.
 *
 * Task: P04-WPPLUGIN-CODE-009
 */
class JwtService {

    public const ACCESS_TTL  = 900;       // 15 minutes
    public const REFRESH_TTL = 2592000;   // 30 days

    /**
     * Resolves the secret key for signing/verifying JWTs.
     *
     * @return string
     * @throws \Exception If no secure secret key is configured.
     */
    public static function secret(): string {
        if ( defined( 'CB_JWT_SECRET' ) && CB_JWT_SECRET ) {
            return CB_JWT_SECRET;
        }
        if ( defined( 'AUTH_KEY' ) && AUTH_KEY && AUTH_KEY !== 'put your unique phrase here' ) {
            return AUTH_KEY;
        }
        throw new \Exception( 'Insecure JWT configuration. Please define CB_JWT_SECRET in wp-config.php.' );
    }

    public static function base64url_encode( string $data ): string {
        return rtrim( strtr( base64_encode( $data ), '+/', '-_' ), '=' );
    }

    public static function base64url_decode( string $data ): string {
        return base64_decode( strtr( $data, '-_', '+/' ) );
    }

    /**
     * Issue a signed HS256 JWT token for a WordPress user.
     *
     * @param \WP_User $user
     * @param string   $type 'access' | 'refresh'
     * @return string
     */
    public static function issue( \WP_User $user, string $type = 'access' ): string {
        $now = time();
        $ttl = ( $type === 'refresh' ) ? self::REFRESH_TTL : self::ACCESS_TTL;

        $role = ! empty( $user->roles ) ? reset( $user->roles ) : 'customer';

        $payload = array(
            'iss'  => function_exists( 'get_site_url' ) ? get_site_url() : 'http://localhost',
            'aud'  => 'carmilla-client',
            'sub'  => $user->user_email,
            'uid'  => (int) $user->ID,
            'role' => $role,
            'typ'  => $type,
            'iat'  => $now,
            'exp'  => $now + $ttl,
        );

        $header        = array( 'alg' => 'HS256', 'typ' => 'JWT' );
        $segments      = array(
            self::base64url_encode( function_exists( 'wp_json_encode' ) ? wp_json_encode( $header ) : json_encode( $header ) ),
            self::base64url_encode( function_exists( 'wp_json_encode' ) ? wp_json_encode( $payload ) : json_encode( $payload ) ),
        );
        $signing_input = implode( '.', $segments );
        $signature     = hash_hmac( 'sha256', $signing_input, self::secret(), true );
        $segments[]    = self::base64url_encode( $signature );

        return implode( '.', $segments );
    }

    /**
     * Verify and decode a JWT token.
     * Returns the payload array or null if invalid/expired.
     *
     * @param string $jwt
     * @return array|null
     */
    public static function decode( string $jwt ): ?array {
        $parts = explode( '.', $jwt );
        if ( count( $parts ) !== 3 ) {
            return null;
        }
        list( $head64, $body64, $sig64 ) = $parts;

        try {
            $expected = hash_hmac( 'sha256', "$head64.$body64", self::secret(), true );
        } catch ( \Exception $e ) {
            return null;
        }

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
        $site_url = function_exists( 'get_site_url' ) ? get_site_url() : 'http://localhost';
        if ( ! isset( $payload['iss'] ) || $payload['iss'] !== $site_url ) {
            return null;
        }
        if ( ! isset( $payload['aud'] ) || $payload['aud'] !== 'carmilla-client' ) {
            return null;
        }

        return $payload;
    }
}
}
