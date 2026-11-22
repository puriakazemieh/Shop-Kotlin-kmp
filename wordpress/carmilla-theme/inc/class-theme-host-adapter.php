<?php
namespace Carmilla\Theme;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/**
 * Theme Host Adapter
 *
 * Connects the Carmilla Theme host to the bundled Carmilla_Kernel.
 * Enforces strict separation between Theme-owned appearance settings (Theme Mods)
 * and site-owned capability/entitlement status (Kernel).
 *
 * Enables the Theme to boot and execute all base UI and migrated services standalone
 * without requiring the Carmilla Bridge plugin or any companion plugin.
 *
 * Task: P04-WPTHEME-CODE-005
 */
class ThemeHostAdapter {

	private static bool $initialized = false;

	/**
	 * Map theme surface slugs to canonical entitlement feature identifiers.
	 */
	public const SLUG_FEATURE_MAP = [
		'shop'       => 'commerce.core',
		'blog'       => 'content.core',
		'courses'    => 'academy.core',
		'clinic'     => 'clinic.booking',
		'psychtests' => 'psych.tests',
		'stories'    => 'content.stories',
		'banners'    => 'content.banners',
	];

	/**
	 * Initialize the Theme Host Adapter.
	 */
	public static function init(): void {
		if ( self::$initialized ) {
			return;
		}
		self::$initialized = true;
	}

	/**
	 * Returns the theme version string.
	 */
	public static function get_version(): string {
		return defined( 'CARMILLA_THEME_VERSION' ) ? CARMILLA_THEME_VERSION : '0.8.0';
	}

	/**
	 * Checks whether the Theme Host Adapter is initialized.
	 */
	public static function is_initialized(): bool {
		return self::$initialized;
	}

	/**
	 * Checks whether the shared kernel is available and booted.
	 */
	public static function is_kernel_booted(): bool {
		return class_exists( '\Carmilla_Kernel' ) && \Carmilla_Kernel::is_booted();
	}

	/**
	 * Returns true if the Theme is running in standalone mode (Bridge plugin is not active).
	 */
	public static function is_standalone(): bool {
		if ( class_exists( '\Carmilla_Kernel' ) ) {
			return ! \Carmilla_Kernel::is_booted_by( 'plugin' );
		}
		return ! class_exists( 'CB_Plugin' );
	}

	/**
	 * Returns true if the Bridge plugin is also active (co-install mode).
	 */
	public static function has_bridge(): bool {
		if ( class_exists( '\Carmilla_Kernel' ) ) {
			return \Carmilla_Kernel::is_booted_by( 'plugin' );
		}
		return class_exists( 'CB_Plugin' );
	}

	/**
	 * Checks if a theme surface or feature is enabled.
	 * Queries the shared kernel's resolved active features.
	 *
	 * @param string $slug_or_feature Slug ('shop', 'blog', 'courses', 'clinic', 'psychtests', 'stories') or feature ID.
	 * @return bool True if active and entitled.
	 */
	public static function is_feature_enabled( string $slug_or_feature ): bool {
		$feature_id = self::SLUG_FEATURE_MAP[ $slug_or_feature ] ?? $slug_or_feature;

		// 1. If Kernel is booted, delegate to kernel's canonical resolution
		if ( class_exists( '\Carmilla_Kernel' ) && \Carmilla_Kernel::is_booted() ) {
			return \Carmilla_Kernel::is_feature_active( $feature_id );
		}

		// 2. If CapabilityResolver is loaded, query canonical resolver directly
		if ( class_exists( '\Carmilla\Core\Catalog\CapabilityResolver' ) ) {
			return \Carmilla\Core\Catalog\CapabilityResolver::is_feature_active( $feature_id );
		}

		// 2. Fallback to site option or default manifest for unbooted / characterization testing
		if ( function_exists( 'carmilla_manifest_features' ) ) {
			$features = carmilla_manifest_features();
			return ! empty( $features[ $feature_id ] );
		}

		return false;
	}

	/**
	 * Returns Theme-owned appearance settings.
	 * STRICT RULE: These are strictly presentation values stored in Theme Mods (get_theme_mod).
	 * They NEVER store or dictate capability/entitlement states.
	 *
	 * @return array<string, mixed>
	 */
	public static function get_appearance_settings(): array {
		$color_defaults = function_exists( 'carmilla_color_defaults' )
			? carmilla_color_defaults()
			: [
				'accent'  => '#20305C',
				'accent2' => '#34487E',
				'gold'    => '#B08D57',
				'bg'      => '#F6F5F1',
				'surface' => '#FFFFFF',
				'ink'     => '#192038',
			];

		$colors = [];
		foreach ( $color_defaults as $key => $default ) {
			$colors[ $key ] = function_exists( 'get_theme_mod' )
				? get_theme_mod( "carmilla_color_{$key}", $default )
				: $default;
		}

		return [
			'colors'        => $colors,
			'header_title'  => function_exists( 'get_theme_mod' ) ? get_theme_mod( 'carmilla_header_title', '' ) : '',
			'hero_title'    => function_exists( 'get_theme_mod' ) ? get_theme_mod( 'carmilla_hero_title', '' ) : '',
			'hero_subtitle' => function_exists( 'get_theme_mod' ) ? get_theme_mod( 'carmilla_hero_subtitle', '' ) : '',
			'logo_id'       => function_exists( 'get_theme_mod' ) ? get_theme_mod( 'custom_logo', 0 ) : 0,
			'rtl'           => function_exists( 'is_rtl' ) ? is_rtl() : true,
		];
	}

	/**
	 * Returns site-owned capability status.
	 * STRICT RULE: Stored and resolved at the site/kernel level.
	 * Theme Mods are never read or modified for capability status.
	 *
	 * @return array<string, bool>
	 */
	public static function get_capability_status(): array {
		if ( class_exists( '\Carmilla_Kernel' ) && \Carmilla_Kernel::is_booted() ) {
			return \Carmilla_Kernel::get_active_features();
		}
		if ( function_exists( 'carmilla_manifest_features' ) ) {
			return carmilla_manifest_features();
		}
		return [];
	}

	/**
	 * Checks if a vertical is available for front-end rendering,
	 * ensuring both entitlement and environmental prerequisites are satisfied.
	 *
	 * @param string $vertical 'shop'|'courses'|'clinic'|'psychtests'|'blog'|'stories'
	 * @return bool
	 */
	public static function is_vertical_available( string $vertical ): bool {
		if ( ! self::is_feature_enabled( $vertical ) ) {
			return false;
		}

		if ( 'shop' === $vertical ) {
			return class_exists( 'WooCommerce' );
		}

		return true;
	}

	/**
	 * Returns navigation destinations according to available verticals.
	 *
	 * @return array<int, array{label: string, url: string, icon: string}>
	 */
	public static function get_navigation_destinations(): array {
		$home = function_exists( 'home_url' ) ? home_url( '/' ) : '/';
		$items = [
			[ 'label' => 'خانه', 'url' => $home, 'icon' => 'home' ],
		];

		if ( self::is_vertical_available( 'shop' ) && function_exists( 'wc_get_page_permalink' ) ) {
			$items[] = [ 'label' => 'فروشگاه', 'url' => wc_get_page_permalink( 'shop' ), 'icon' => 'shop' ];
		}

		if ( self::is_vertical_available( 'courses' ) && function_exists( 'get_post_type_archive_link' ) ) {
			$items[] = [ 'label' => 'دوره‌ها', 'url' => get_post_type_archive_link( 'cb_course' ), 'icon' => 'academy' ];
		}

		if ( self::is_vertical_available( 'blog' ) ) {
			$blog_page = function_exists( 'get_option' ) ? get_option( 'page_for_posts' ) : null;
			$blog_url = ( $blog_page && function_exists( 'get_permalink' ) )
				? get_permalink( $blog_page )
				: ( function_exists( 'home_url' ) ? home_url( '/blog' ) : '/blog' );
			$items[] = [ 'label' => 'مجله', 'url' => $blog_url, 'icon' => 'blog' ];
		}

		if ( self::is_vertical_available( 'shop' ) && function_exists( 'wc_get_cart_url' ) ) {
			$items[] = [ 'label' => 'سبد', 'url' => wc_get_cart_url(), 'icon' => 'cart' ];
		}

		$account_url = function_exists( 'wc_get_page_permalink' )
			? wc_get_page_permalink( 'myaccount' )
			: ( function_exists( 'home_url' ) ? home_url( '/account' ) : '/account' );
		$items[] = [ 'label' => 'حساب', 'url' => $account_url, 'icon' => 'user' ];

		return array_slice( $items, 0, 5 );
	}

	/**
	 * Returns the vertical content types that the theme manages in standalone mode.
	 *
	 * @return array<string, string> Map of post type name to plural label.
	 */
	public static function get_cpt_inventory(): array {
		return [
			'cb_course'         => 'دوره‌ها',
			'cb_therapist'      => 'مشاوران',
			'cb_psychtest'      => 'تست‌های روان‌شناسی',
			'cb_course_request' => 'درخواست‌های دوره',
			'cb_story'          => 'استوری‌ها',
			'cb_banner'         => 'بنرهای خانه',
		];
	}

	/**
	 * Resets adapter state (used in testing).
	 */
	public static function reset_state(): void {
		self::$initialized = false;
	}
}
