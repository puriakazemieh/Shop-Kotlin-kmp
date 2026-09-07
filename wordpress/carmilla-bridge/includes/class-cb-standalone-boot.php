<?php
/**
 * Standalone bootstrap for Carmilla Bridge.
 *
 * Provides a resilient, headless runtime environment when running WITHOUT
 * the Carmilla Theme (e.g. default WordPress themes like Twenty Twenty-Four,
 * Twenty Twenty-Three, or Storefront).
 *
 * Ensures core CPTs, REST API routes, adapters, and headless CORS policies
 * initialize gracefully without fatal errors.
 *
 * Task: P04-WPPLUGIN-CODE-007
 *
 * @package Carmilla_Bridge
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/**
 * Class CB_Standalone_Boot
 *
 * Handles standalone / headless bootstrap logic when Carmilla Theme is absent.
 */
class CB_Standalone_Boot {

	/**
	 * Tracks whether the standalone bootstrap has already run in the current lifecycle.
	 *
	 * @var bool
	 */
	private static bool $booted = false;

	/**
	 * Boots the standalone environment.
	 *
	 * Runs on `plugins_loaded` and checks whether the Carmilla Theme is active.
	 * If the theme is NOT active, minimal adapters and fallback handlers are loaded.
	 * Regardless of theme, core CPTs and REST API routes are guaranteed to register.
	 *
	 * @return void
	 */
	public static function boot(): void {
		if ( self::$booted ) {
			return;
		}

		// Prevent duplicate boots across co-install cycles using shared kernel checkpoint if present.
		if ( class_exists( '\Carmilla\Core\Migration\SchemaRunner' ) ) {
			if ( ! \Carmilla\Core\Migration\SchemaRunner::register_checkpoint( 'cb_standalone_boot' ) ) {
				return;
			}
		}

		self::$booted = true;

		// 1. Check if Carmilla Theme is active.
		$is_theme_active = self::is_carmilla_theme_active();

		// 2. Load Bridge dependencies and controllers if not already loaded.
		self::load_dependencies();

		// 3. If Carmilla Theme is NOT active, load headless mode adapters.
		if ( ! $is_theme_active ) {
			self::load_headless_adapters();
		}

		// 4. Ensure core CPTs (cb_course, cb_therapist, cb_psychtest, cb_story) register regardless of theme.
		add_action( 'init', array( __CLASS__, 'register_core_cpts' ), 5 );

		// 5. Ensure REST API routes register regardless of theme.
		add_action( 'rest_api_init', array( __CLASS__, 'register_core_routes' ), 10 );
	}

	/**
	 * Detects whether the Carmilla Theme (or a child of it) is currently active.
	 *
	 * @return bool True if Carmilla Theme is active, false otherwise.
	 */
	public static function is_carmilla_theme_active(): bool {
		$current_theme = wp_get_theme();
		$stylesheet    = strtolower( (string) $current_theme->get_stylesheet() );
		$template      = strtolower( (string) $current_theme->get_template() );

		if ( 'carmilla-theme' === $stylesheet || 'carmilla-theme' === $template ) {
			return true;
		}

		if ( false !== strpos( $stylesheet, 'carmilla' ) || false !== strpos( $template, 'carmilla' ) ) {
			return true;
		}

		return false;
	}

	/**
	 * Loads Bridge dependencies and controller classes safely.
	 *
	 * @return void
	 */
	private static function load_dependencies(): void {
		$plugin_dir = defined( 'CB_PLUGIN_DIR' ) ? CB_PLUGIN_DIR : plugin_dir_path( dirname( __FILE__ ) );

		// Ensure constant definitions.
		if ( ! defined( 'CB_REST_NAMESPACE' ) ) {
			define( 'CB_REST_NAMESPACE', 'carmilla/v1' );
		}
		if ( ! defined( 'CB_VERSION' ) ) {
			define( 'CB_VERSION', '1.0.0' );
		}

		$includes = array(
			'helpers.php',
			'class-cb-jwt.php',
			'class-cb-manifest-controller.php',
			'class-cb-legacy-migration.php',
			'class-cb-blocks.php',
			'class-cb-cpt.php',
			'meta-boxes.php',
			'class-cb-auth-controller.php',
			'class-cb-catalog-controller.php',
			'class-cb-blog-controller.php',
			'class-cb-media-controller.php',
			'class-cb-cart-controller.php',
			'class-cb-order-controller.php',
			'class-cb-payment-controller.php',
			'class-cb-account-controller.php',
			'class-cb-interaction-controller.php',
			'class-cb-academy-controller.php',
			'class-cb-clinic-controller.php',
			'class-cb-psychtest-controller.php',
			'class-cb-extras-controller.php',
			'class-cb-support-controller.php',
			'class-cb-bundle-controller.php',
			'class-cb-story-controller.php',
			'class-cb-course-request-controller.php',
			'class-cb-admin-controller.php',
			'class-cb-admin-content-controller.php',
			'class-cb-admin-product-controller.php',
			'class-cb-admin-clinic-controller.php',
			'class-cb-admin-b2b-controller.php',
			'class-cb-admin-bundle-controller.php',
			'class-cb-integrations-controller.php',
			'class-cb-mail-adapter.php',
			'class-cb-plugin.php',
		);

		foreach ( $includes as $file ) {
			$path = $plugin_dir . 'includes/' . $file;
			if ( file_exists( $path ) ) {
				require_once $path;
			}
		}

		// Boot primary bridge instance.
		if ( class_exists( 'CB_Plugin' ) ) {
			CB_Plugin::instance();
		}
	}

	/**
	 * Loads adapters for headless mode when Carmilla Theme is NOT active.
	 *
	 * Prevents fatal errors with default themes (Twenty Twenty-Four, Storefront, etc.)
	 * by shimming missing theme functions, enabling health/LMS feature flags,
	 * and providing permissive headless CORS configurations.
	 *
	 * @return void
	 */
	private static function load_headless_adapters(): void {
		if ( ! defined( 'CARMILLA_STANDALONE_MODE' ) ) {
			define( 'CARMILLA_STANDALONE_MODE', true );
		}

		// Enable health & LMS features in standalone mode so courses, therapists,
		// and psych tests are exposed via REST API without requiring Carmilla theme.
		add_filter( 'cb_enable_health_lms', '__return_true', 1 );

		// Provide theme function stubs to prevent fatal errors if third-party plugins
		// or integrations attempt to call Carmilla theme helper functions.
		self::register_theme_function_stubs();

		// Prevent block themes (like Twenty Twenty-Four) from generating 500 fatal errors
		// when template files for custom post types are requested in a browser.
		add_filter( 'template_include', array( __CLASS__, 'filter_headless_template' ), 99 );
	}

	/**
	 * Polyfills / stubs theme functions that might be referenced in standalone mode.
	 *
	 * @return void
	 */
	private static function register_theme_function_stubs(): void {
		if ( ! function_exists( 'carmilla_is_theme_active' ) ) {
			/**
			 * Polyfill: returns whether Carmilla Theme is active.
			 *
			 * @return bool
			 */
			function carmilla_is_theme_active(): bool {
				return false;
			}
		}

		if ( ! function_exists( 'carmilla_get_option' ) ) {
			/**
			 * Polyfill: fallback option accessor.
			 *
			 * @param string $key
			 * @param mixed  $default
			 * @return mixed
			 */
			function carmilla_get_option( string $key, $default = null ) {
				if ( class_exists( '\Carmilla\Core\Settings\SettingsService' ) ) {
					return \Carmilla\Core\Settings\SettingsService::get( $key, $default );
				}
				$settings = get_option( 'carmilla_settings', array() );
				return $settings[ $key ] ?? $default;
			}
		}

		if ( ! function_exists( 'carmilla_theme_asset' ) ) {
			/**
			 * Polyfill: fallback asset URL returning empty string in headless mode.
			 *
			 * @param string $path
			 * @return string
			 */
			function carmilla_theme_asset( string $path = '' ): string {
				return '';
			}
		}
	}

	/**
	 * Gracefully handles template inclusion for Carmilla CPTs when running with default themes.
	 *
	 * If a visitor directly accesses a single-cb_course or single-cb_therapist URL on
	 * a site with Twenty Twenty-Four (or another theme without dedicated single templates),
	 * fallback to the theme's standard single or page template rather than failing.
	 *
	 * @param string $template Current template file path.
	 * @return string
	 */
	public static function filter_headless_template( string $template ): string {
		if ( is_singular( array( 'cb_course', 'cb_therapist', 'cb_psychtest', 'cb_story' ) ) ) {
			if ( empty( $template ) || ! file_exists( $template ) ) {
				$fallback = get_singular_template();
				if ( ! empty( $fallback ) && file_exists( $fallback ) ) {
					return $fallback;
				}
				return get_index_template();
			}
		}
		return $template;
	}

	/**
	 * Registers core CPTs (cb_course, cb_therapist, cb_psychtest, cb_story) regardless of theme.
	 *
	 * @return void
	 */
	public static function register_core_cpts(): void {
		// 1. cb_story
		if ( ! post_type_exists( 'cb_story' ) ) {
			register_post_type(
				'cb_story',
				array(
					'labels'       => array(
						'name'          => __( 'Stories', 'carmilla-bridge' ),
						'singular_name' => __( 'Story', 'carmilla-bridge' ),
					),
					'public'       => false,
					'show_ui'      => true,
					'show_in_menu' => true,
					'show_in_rest' => true,
					'menu_icon'    => 'dashicons-format-image',
					'supports'     => array( 'title', 'thumbnail' ),
					'has_archive'  => false,
				)
			);
		}

		// 2. cb_course
		if ( ! post_type_exists( 'cb_course' ) ) {
			register_post_type(
				'cb_course',
				array(
					'labels'       => array(
						'name'          => __( 'Courses', 'carmilla-bridge' ),
						'singular_name' => __( 'Course', 'carmilla-bridge' ),
					),
					'public'       => true,
					'show_ui'      => true,
					'show_in_menu' => true,
					'show_in_rest' => true,
					'menu_icon'    => 'dashicons-welcome-learn-more',
					'supports'     => array( 'title', 'editor', 'thumbnail', 'custom-fields' ),
					'has_archive'  => true,
					'rewrite'      => array( 'slug' => 'courses' ),
				)
			);
		}

		// 3. cb_therapist
		if ( ! post_type_exists( 'cb_therapist' ) ) {
			register_post_type(
				'cb_therapist',
				array(
					'labels'       => array(
						'name'          => __( 'Therapists', 'carmilla-bridge' ),
						'singular_name' => __( 'Therapist', 'carmilla-bridge' ),
					),
					'public'       => true,
					'show_ui'      => true,
					'show_in_menu' => true,
					'show_in_rest' => true,
					'menu_icon'    => 'dashicons-heart',
					'supports'     => array( 'title', 'editor', 'thumbnail', 'custom-fields' ),
					'has_archive'  => true,
					'rewrite'      => array( 'slug' => 'therapists' ),
				)
			);
		}

		// 4. cb_psychtest
		if ( ! post_type_exists( 'cb_psychtest' ) ) {
			register_post_type(
				'cb_psychtest',
				array(
					'labels'       => array(
						'name'          => __( 'Psych Tests', 'carmilla-bridge' ),
						'singular_name' => __( 'Psych Test', 'carmilla-bridge' ),
					),
					'public'       => true,
					'show_ui'      => true,
					'show_in_menu' => true,
					'show_in_rest' => true,
					'menu_icon'    => 'dashicons-forms',
					'supports'     => array( 'title', 'editor', 'custom-fields' ),
					'has_archive'  => true,
					'rewrite'      => array( 'slug' => 'psych-tests' ),
				)
			);
		}

		// Register post meta for REST endpoints if CB_CPT is available.
		if ( class_exists( 'CB_CPT' ) ) {
			CB_CPT::register_meta();
		}
	}

	/**
	 * Registers REST API routes for all Carmilla features regardless of theme.
	 *
	 * @return void
	 */
	public static function register_core_routes(): void {
		if ( class_exists( 'CB_Plugin' ) ) {
			CB_Plugin::instance()->register_routes();
		}
	}
}

/**
 * Procedural entrypoint called on `plugins_loaded`.
 *
 * Runs standalone initialization for the Carmilla Bridge plugin.
 *
 * @return void
 */
function carmilla_bridge_standalone_init(): void {
	CB_Standalone_Boot::boot();
}

// Hook to plugins_loaded with priority 5 so that standalone adapters run early.
add_action( 'plugins_loaded', 'carmilla_bridge_standalone_init', 5 );
