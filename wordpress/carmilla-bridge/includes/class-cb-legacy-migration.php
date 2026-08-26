<?php
/** One-time, non-destructive migration of legacy Theme Mod feature flags. */
if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Legacy_Migration {
	private const VERSION_OPTION = 'cb_legacy_migration_version';
	private const VERSION = 1;

	/** Return the stable mapping without reading or writing WordPress state. */
	public static function plan( array $legacy ): array {
		$map = array(
			'shop'       => 'commerce.core',
			'blog'       => 'content.blog',
			'courses'    => 'academy.core',
			'clinic'     => 'clinic.booking',
			'psychtests' => 'psych.tests',
			'stories'    => 'content.blog',
		);
		$manifest = array();
		foreach ( $map as $legacy_slug => $feature_id ) {
			if ( array_key_exists( $legacy_slug, $legacy ) ) {
				$manifest[ $feature_id ] = (bool) $legacy[ $legacy_slug ];
			}
		}
		return $manifest;
	}

	/**
	 * Migrate only when the canonical option is absent. A marker makes retries
	 * safe, and no token/package/signing data is ever read or rewritten here.
	 */
	public static function run(): array {
		$version = (int) get_option( self::VERSION_OPTION, 0 );
		if ( $version >= self::VERSION ) {
			return array( 'status' => 'already_migrated' );
		}
		$canonical = get_option( 'cb_manifest_features', null );
		if ( is_array( $canonical ) && ! empty( $canonical ) ) {
			update_option( self::VERSION_OPTION, self::VERSION, false );
			return array( 'status' => 'canonical_preserved' );
		}
		$legacy = array();
		if ( function_exists( 'get_theme_mod' ) ) {
			foreach ( array( 'shop', 'blog', 'courses', 'clinic', 'psychtests', 'stories' ) as $slug ) {
				$value = get_theme_mod( 'carmilla_enable_' . $slug, null );
				if ( $value !== null ) {
					$legacy[ $slug ] = $value;
				}
			}
		}
		$manifest = self::plan( $legacy );
		if ( ! empty( $manifest ) ) {
			update_option( 'cb_manifest_features', $manifest, false );
		}
		update_option( self::VERSION_OPTION, self::VERSION, false );
		return array( 'status' => empty( $manifest ) ? 'no_legacy_flags' : 'migrated', 'features' => array_keys( $manifest ) );
	}
}
