<?php
/**
 * Isolated WordPress adapter for the browser-session store.
 *
 * This file registers nothing. It supports only the verified, local mysqli
 * configuration below; unsupported database setups must fail closed.
 */
if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

require_once __DIR__ . '/class-cb-web-session-store.php';

final class CB_Web_Session_WP_Adapter {
	/**
	 * Build the store from trusted server-side WordPress configuration only.
	 * The explicit site URL and random key must be set outside request data.
	 */
	public static function from_wordpress(): CB_Web_Session_Store {
		global $wpdb;

		try {
			foreach ( array( 'DB_HOST', 'DB_USER', 'DB_PASSWORD', 'DB_NAME', 'WP_CONTENT_DIR', 'CB_WEB_SESSION_SITE_URL', 'CB_WEB_SESSION_SECRET_HEX' ) as $name ) {
				if ( ! defined( $name ) || ! is_string( constant( $name ) ) ) {
					throw new RuntimeException();
				}
			}
			if ( ! class_exists( 'wpdb', false ) || ! is_object( $wpdb ) || get_class( $wpdb ) !== 'wpdb' || $wpdb->is_mysql !== true || ! function_exists( 'get_current_blog_id' ) || ! function_exists( 'get_site_url' ) || ! function_exists( 'is_multisite' ) ) {
				throw new RuntimeException();
			}
			// A drop-in may route or rewrite the WordPress DB connection. This
			// adapter cannot establish its equivalence safely.
			if ( file_exists( WP_CONTENT_DIR . '/db.php' ) || is_multisite() || defined( 'CUSTOM_USER_TABLE' ) || defined( 'CUSTOM_USER_META_TABLE' ) || ( defined( 'MYSQL_CLIENT_FLAGS' ) && MYSQL_CLIENT_FLAGS !== 0 ) ) {
				throw new RuntimeException();
			}
			$blog_id = get_current_blog_id();
			if ( ! is_int( $blog_id ) || $blog_id !== 1 || ! is_string( $wpdb->prefix ) || ! preg_match( '/\A[a-zA-Z0-9_]{1,35}\z/', $wpdb->prefix ) || $wpdb->base_prefix !== $wpdb->prefix || $wpdb->users !== $wpdb->prefix . 'users' || $wpdb->get_blog_prefix( $blog_id ) !== $wpdb->prefix ) {
				throw new RuntimeException();
			}
			if ( DB_NAME === '' || DB_USER === '' || ! preg_match( '/\A[0-9a-f]{64}\z/', CB_WEB_SESSION_SECRET_HEX ) || preg_match( '/\A(.)\1{63}\z/', CB_WEB_SESSION_SECRET_HEX ) ) {
				throw new RuntimeException();
			}
			$secret = hex2bin( CB_WEB_SESSION_SECRET_HEX );
			if ( $secret === false ) {
				throw new RuntimeException();
			}
			$site_url = self::canonical_site_url( CB_WEB_SESSION_SITE_URL );
			$wp_url = get_site_url( $blog_id );
			if ( ! is_string( $wp_url ) || ! hash_equals( $site_url, self::canonical_site_url( $wp_url ) ) ) {
				throw new RuntimeException();
			}
			$site = self::site_id( $site_url, $blog_id );
			$port = self::local_port( DB_HOST );
			$primary_db = $wpdb->get_var( 'SELECT DATABASE()' );
			$primary_id = $wpdb->get_var( 'SELECT CONNECTION_ID()' );
			if ( ! is_string( $primary_db ) || ! hash_equals( DB_NAME, $primary_db ) || ! is_scalar( $primary_id ) || ! ctype_digit( (string) $primary_id ) || (int) $primary_id < 1 ) {
				throw new RuntimeException();
			}

			$store_db = self::connect_local( $port );
			self::assert_same_primary( $store_db, (int) $primary_id );
			$password_db = self::connect_local( $port );
			self::assert_same_primary( $password_db, (int) $primary_id );
			if ( $store_db->thread_id === $password_db->thread_id || $store_db->thread_id === (int) $primary_id || $password_db->thread_id === (int) $primary_id ) {
				throw new RuntimeException();
			}
			$users_table = $wpdb->users;
			$password_provider = static function ( int $user_id ) use ( $password_db, $users_table ): ?string {
				return self::current_password_hash( $password_db, $users_table, $user_id );
			};
			// A separate domain is used for the fingerprint key.
			$fingerprint_key = hash_hmac( 'sha256', 'carmilla-web-session-password-state-v1', $secret, true );
			return new CB_Web_Session_Store( $store_db, $wpdb->prefix, $site, $fingerprint_key, $password_provider );
		} catch ( Throwable $e ) {
			// Never include configuration, SQL, credentials or the underlying
			// driver error in exceptions returned to callers or logs.
			throw new CB_Web_Session_Storage_Exception( 'WordPress session adapter unavailable.' );
		}
	}

	/** Stable installation identity; the later cookie policy must use this too. */
	public static function site_id( string $site_url, int $blog_id ): string {
		if ( $blog_id < 1 ) {
			throw new CB_Web_Session_Storage_Exception( 'Invalid site identity.' );
		}
		return hash( 'sha256', self::canonical_site_url( $site_url ) . "\0" . $blog_id );
	}

	/** Restricted ASCII URL canonicalization shared with later cookie logic. */
	public static function canonical_site_url( string $url ): string {
		if ( $url === '' || preg_match( '/[^\x21-\x7e]/', $url ) || strpos( $url, '%' ) !== false || strpos( $url, '\\' ) !== false ) {
			throw new CB_Web_Session_Storage_Exception( 'Invalid site identity.' );
		}
		$parts = parse_url( $url );
		if ( ! is_array( $parts ) || ! isset( $parts['scheme'], $parts['host'] ) || isset( $parts['user'] ) || isset( $parts['pass'] ) || isset( $parts['query'] ) || isset( $parts['fragment'] ) ) {
			throw new CB_Web_Session_Storage_Exception( 'Invalid site identity.' );
		}
		$scheme = strtolower( $parts['scheme'] );
		$host = strtolower( $parts['host'] );
		if ( ! in_array( $scheme, array( 'http', 'https' ), true ) || strlen( $host ) > 253 || ! preg_match( '/\A[a-z0-9]+(?:[a-z0-9-]*[a-z0-9])?(?:\.[a-z0-9]+(?:[a-z0-9-]*[a-z0-9])?)*\z/', $host ) ) {
			throw new CB_Web_Session_Storage_Exception( 'Invalid site identity.' );
		}
		foreach ( explode( '.', $host ) as $label ) {
			if ( strlen( $label ) > 63 ) { throw new CB_Web_Session_Storage_Exception( 'Invalid site identity.' ); }
		}
		if ( isset( $parts['port'] ) && $parts['port'] < 1 ) {
			throw new CB_Web_Session_Storage_Exception( 'Invalid site identity.' );
		}
		$path = $parts['path'] ?? '';
		if ( $path !== '' && ( $path[0] !== '/' || strpos( $path, '//' ) !== false || ! preg_match( '/\A\/[a-zA-Z0-9._~\/-]*\z/', $path ) || preg_match( '#(?:^|/)\.{1,2}(?:/|$)#', $path ) ) ) {
			throw new CB_Web_Session_Storage_Exception( 'Invalid site identity.' );
		}
		$port = isset( $parts['port'] ) ? ':' . $parts['port'] : '';
		return $scheme . '://' . $host . $port . rtrim( $path, '/' );
	}

	private static function local_port( string $host ): int {
		if ( ! preg_match( '/\A(127\.0\.0\.1|localhost)(?::([0-9]{1,5}))?\z/', $host, $parts ) ) {
			throw new RuntimeException();
		}
		// On Unix, localhost may select a socket instead of TCP. Never assume
		// that the socket and TCP server are the same database.
		if ( $parts[1] === 'localhost' && PHP_OS_FAMILY !== 'Windows' ) {
			throw new RuntimeException();
		}
		$port = isset( $parts[2] ) ? (int) $parts[2] : 3306;
		if ( $port < 1 || $port > 65535 ) {
			throw new RuntimeException();
		}
		return $port;
	}

	private static function connect_local( int $port ): mysqli {
		try {
			$db = mysqli_init();
			if ( ! $db || ! $db->options( MYSQLI_OPT_CONNECT_TIMEOUT, 3 ) || ! @$db->real_connect( '127.0.0.1', DB_USER, DB_PASSWORD, DB_NAME, $port, null, 0 ) || ! $db->set_charset( 'utf8mb4' ) || ! $db->autocommit( true ) ) {
				throw new RuntimeException();
			}
			return $db;
		} catch ( Throwable $e ) {
			throw new CB_Web_Session_Storage_Exception( 'Database connection unavailable.' );
		}
	}

	/** Confirm this is the same local server and selected DB as WordPress. */
	private static function assert_same_primary( mysqli $db, int $primary_id ): void {
		try {
			$stmt = $db->prepare( 'SELECT COUNT(*) AS matches FROM information_schema.PROCESSLIST WHERE ID=? AND DB=DATABASE()' );
			if ( ! $stmt || ! $stmt->bind_param( 'i', $primary_id ) || ! $stmt->execute() ) {
				throw new RuntimeException();
			}
			$result = $stmt->get_result();
			$row = $result ? $result->fetch_assoc() : null;
			$stmt->close();
			if ( ! is_array( $row ) || (int) $row['matches'] !== 1 ) {
				throw new RuntimeException();
			}
		} catch ( Throwable $e ) {
			throw new CB_Web_Session_Storage_Exception( 'Database identity unavailable.' );
		}
	}

	/** Read directly on a separate autocommit connection, not WP object cache. */
	private static function current_password_hash( mysqli $db, string $users_table, int $user_id ): ?string {
		try {
			if ( $user_id < 1 || ! preg_match( '/\A[a-zA-Z0-9_]{1,40}\z/', $users_table ) ) {
				throw new RuntimeException();
			}
			$stmt = $db->prepare( "SELECT user_pass FROM `{$users_table}` WHERE ID=?" );
			if ( ! $stmt || ! $stmt->bind_param( 'i', $user_id ) || ! $stmt->execute() ) {
				throw new RuntimeException();
			}
			$result = $stmt->get_result();
			if ( ! $result ) { throw new RuntimeException(); }
			$row = $result->fetch_assoc();
			$stmt->close();
			if ( $row === null ) { return null; }
			if ( ! isset( $row['user_pass'] ) || ! is_string( $row['user_pass'] ) || $row['user_pass'] === '' ) { throw new RuntimeException(); }
			return $row['user_pass'];
		} catch ( Throwable $e ) {
			throw new CB_Web_Session_Storage_Exception( 'Password state unavailable.' );
		}
	}
}
