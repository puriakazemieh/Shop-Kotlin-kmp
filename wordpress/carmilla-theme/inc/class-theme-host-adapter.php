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

		// Register standalone REST routes when Bridge plugin is absent
		add_action( 'rest_api_init', [ self::class, 'register_standalone_routes' ], 15 );
		add_action( 'init', [ self::class, 'register_content_shortcodes' ], 20 );
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
	 * Registers standalone REST routes when Bridge plugin is absent.
	 */
	public static function register_standalone_routes(): void {
		if ( ! self::is_standalone() ) {
			return;
		}
		if ( ! class_exists( '\Carmilla\Core\Rest\RestInfrastructure' ) ) {
			return;
		}

		$ns = \Carmilla\Core\Rest\RestInfrastructure::NAMESPACE;

		// Auth endpoints
		register_rest_route( $ns, '/api/auth/login', [
			'methods'             => 'POST',
			'callback'            => [ self::class, 'rest_login' ],
			'permission_callback' => '__return_true',
		] );

		// Commerce: Products (registered once by CatalogRestController in Carmilla_Kernel)
		if ( ! class_exists( '\Carmilla\Core\Catalog\CatalogRestController' ) ) {
			register_rest_route( $ns, '/api/products', [
				'methods'             => 'GET',
				'callback'            => [ self::class, 'rest_get_products' ],
				'permission_callback' => '__return_true',
			] );

			register_rest_route( $ns, '/api/products/(?P<id>\d+)', [
				'methods'             => 'GET',
				'callback'            => [ self::class, 'rest_get_product' ],
				'permission_callback' => '__return_true',
			] );
		}

		// Account: Profile (/api/users/me)
		register_rest_route( $ns, '/api/users/me', [
			'methods'             => [ 'GET', 'PATCH' ],
			'callback'            => [ self::class, 'rest_user_profile' ],
			'permission_callback' => [ self::class, 'check_user_logged_in' ],
		] );
	}

	/**
	 * Formats a WC_Product into a standard product summary DTO.
	 */
	public static function format_product_summary( $product ): array {
		if ( ! is_object( $product ) || ! method_exists( $product, 'get_id' ) ) {
			return [];
		}

		$cats   = method_exists( $product, 'get_category_ids' ) ? $product->get_category_ids() : [];
		$cat_id = ! empty( $cats ) ? (int) $cats[0] : null;

		$reg_price  = method_exists( $product, 'get_regular_price' ) ? (float) $product->get_regular_price() : 0.0;
		$sale_price = method_exists( $product, 'get_sale_price' ) && $product->get_sale_price() !== '' ? (float) $product->get_sale_price() : null;

		$image_id = method_exists( $product, 'get_image_id' ) ? (int) $product->get_image_id() : 0;
		$images   = [];
		if ( $image_id > 0 && function_exists( 'wp_get_attachment_url' ) ) {
			$images[] = [
				'id'        => $image_id,
				'url'       => (string) wp_get_attachment_url( $image_id ),
				'sortOrder' => 0,
			];
		}

		return [
			'id'              => $product->get_id(),
			'title'           => method_exists( $product, 'get_name' ) ? $product->get_name() : '',
			'slug'            => method_exists( $product, 'get_slug' ) ? $product->get_slug() : '',
			'description'     => method_exists( $product, 'get_description' ) ? $product->get_description() : '',
			'brand'           => method_exists( $product, 'get_meta' ) ? ( $product->get_meta( 'cb_brand' ) ?: null ) : null,
			'attributes'      => [],
			'categoryId'      => $cat_id,
			'categoryName'    => ( $cat_id && function_exists( 'get_term' ) ) ? ( ( $term = get_term( $cat_id, 'product_cat' ) ) && ! is_wp_error( $term ) ? $term->name : null ) : null,
			'images'          => $images,
			'videos'          => [],
			'variants'        => [],
			'createdAt'       => method_exists( $product, 'get_date_created' ) && $product->get_date_created() ? $product->get_date_created()->date( 'c' ) : gmdate( 'c' ),
			'basePrice'       => $reg_price,
			'discountedPrice' => $sale_price,
			'isFavorite'      => false,
		];
	}

	/**
	 * Standalone REST handler for GET /api/products
	 */
	public static function rest_get_products( $request ) {
		$page     = (int) ( is_object( $request ) ? ( $request->get_param( 'page' ) ?: 1 ) : 1 );
		$per_page = (int) ( is_object( $request ) ? ( $request->get_param( 'per_page' ) ?: 10 ) : 10 );
		$per_page = \Carmilla\Core\Rest\RestInfrastructure::sanitize_per_page( $per_page );

		if ( ! class_exists( 'WooCommerce' ) ) {
			return \Carmilla\Core\Rest\RestInfrastructure::paginated_response( [], 0, $page, $per_page );
		}

		$query_args = [
			'limit'    => $per_page,
			'page'     => $page,
			'paginate' => true,
			'status'   => 'publish',
		];

		if ( class_exists( '\Carmilla\Core\Adapter\WooCommerceAdapter' ) ) {
			$result = \Carmilla\Core\Adapter\WooCommerceAdapter::get_products( $query_args );
		} else {
			$result = wc_get_products( $query_args );
		}

		$products = [];
		$total    = 0;
		if ( is_object( $result ) && isset( $result->products ) ) {
			$products = $result->products;
			$total    = (int) $result->total;
		} elseif ( is_array( $result ) ) {
			$products = $result;
			$total    = count( $result );
		}

		$items = [];
		foreach ( $products as $product ) {
			$items[] = self::format_product_summary( $product );
		}

		return \Carmilla\Core\Rest\RestInfrastructure::paginated_response( $items, $total, $page, $per_page );
	}

	/**
	 * Standalone REST handler for GET /api/products/(?P<id>\d+)
	 */
	public static function rest_get_product( $request ) {
		$id = (int) ( is_object( $request ) ? $request->get_param( 'id' ) : ( $request['id'] ?? 0 ) );
		if ( $id <= 0 ) {
			return \Carmilla\Core\Rest\RestInfrastructure::error_response( 'NOT_FOUND', 'شناسه محصول نامعتبر است', 404, 'api/products' );
		}

		$product = null;
		if ( class_exists( '\Carmilla\Core\Adapter\WooCommerceAdapter' ) ) {
			$product = \Carmilla\Core\Adapter\WooCommerceAdapter::get_product( $id );
		} elseif ( function_exists( 'wc_get_product' ) ) {
			$product = wc_get_product( $id );
		}

		if ( ! $product ) {
			return \Carmilla\Core\Rest\RestInfrastructure::error_response( 'NOT_FOUND', 'محصول یافت نشد', 404, 'api/products' );
		}

		return \Carmilla\Core\Rest\RestInfrastructure::success_response( self::format_product_summary( $product ) );
	}

	/**
	 * Standalone REST handler for POST /api/auth/login
	 */
	public static function rest_login( $request ) {
		$username = trim( (string) ( is_object( $request ) ? $request->get_param( 'username' ) : ( $request['username'] ?? '' ) ) );
		$password = (string) ( is_object( $request ) ? $request->get_param( 'password' ) : ( $request['password'] ?? '' ) );

		if ( $username === '' || $password === '' ) {
			return \Carmilla\Core\Rest\RestInfrastructure::error_response( 'VALIDATION', 'نام کاربری و رمز عبور الزامی است.', 400, 'api/auth/login' );
		}

		$user = wp_authenticate( $username, $password );
		if ( is_wp_error( $user ) ) {
			return \Carmilla\Core\Rest\RestInfrastructure::error_response( 'BAD_CREDENTIALS', 'نام کاربری یا رمز عبور نادرست است.', 401, 'api/auth/login' );
		}

		$token   = \Carmilla\Core\Rest\JwtService::issue( $user, 'access' );
		$refresh = \Carmilla\Core\Rest\JwtService::issue( $user, 'refresh' );
		$role    = ! empty( $user->roles ) ? reset( $user->roles ) : 'customer';

		$data = [
			'token'        => $token,
			'accessToken'  => $token,
			'refreshToken' => $refresh,
			'user'         => [
				'id'        => (string) $user->ID,
				'username'  => $user->user_login,
				'email'     => $user->user_email,
				'role'      => $role,
				'firstName' => $user->first_name ?: $user->display_name,
				'lastName'  => $user->last_name ?: '',
			],
		];

		return \Carmilla\Core\Rest\RestInfrastructure::success_response( $data, 200 );
	}

	/**
	 * Standalone REST handler for GET /api/users/me
	 */
	public static function rest_user_profile( $request ) {
		$user_id = get_current_user_id();
		if ( $user_id <= 0 ) {
			return \Carmilla\Core\Rest\RestInfrastructure::error_response( 'UNAUTHORIZED', 'لطفاً وارد شوید.', 401, 'api/users/me' );
		}

		$user = get_user_by( 'id', $user_id );
		if ( ! $user ) {
			return \Carmilla\Core\Rest\RestInfrastructure::error_response( 'NOT_FOUND', 'کاربر یافت نشد.', 404, 'api/users/me' );
		}

		$role = ! empty( $user->roles ) ? reset( $user->roles ) : 'customer';
		$data = [
			'id'        => (string) $user->ID,
			'username'  => $user->user_login,
			'email'     => $user->user_email,
			'role'      => $role,
			'firstName' => $user->first_name ?: $user->display_name,
			'lastName'  => $user->last_name ?: '',
		];

		return \Carmilla\Core\Rest\RestInfrastructure::success_response( $data, 200 );
	}

	/**
	 * Permission check: user logged in.
	 */
	public static function check_user_logged_in() {
		if ( is_user_logged_in() ) {
			return true;
		}
		return \Carmilla\Core\Rest\RestInfrastructure::error_response( 'UNAUTHORIZED', 'لطفاً وارد شوید.', 401, 'api' );
	}

	/**
	 * Runs independent onboarding preflight for the Theme host.
	 * Base Theme SKU (`sku_theme_base`) does NOT require Bridge or WooCommerce.
	 *
	 * @param array<string>        $skus      Optional purchased SKUs; defaults to ['sku_theme_base'] or stored claims.
	 * @param array<string, mixed> $overrides Optional environment overrides for testing.
	 * @return array<string, mixed>
	 */
	public static function get_onboarding_preflight( array $skus = [], array $overrides = [] ): array {
		if ( class_exists( '\Carmilla\Core\Health\HealthChecker' ) ) {
			return \Carmilla\Core\Health\HealthChecker::run_preflight( 'theme', $skus, $overrides );
		}
		return [
			'ready'           => false,
			'host_context'    => 'theme',
			'blocking_issues' => [
				[
					'id'          => 'kernel_missing',
					'name'        => 'Carmilla Shared Kernel',
					'scope'       => 'shared',
					'message'     => 'HealthChecker is not loaded.',
					'remediation' => 'Ensure packages/carmilla-core is bundled with the theme.',
				],
			],
		];
	}

	/**
	 * Delegates operational settings read/write to canonical shared SettingsService.
	 */
	public static function save_operational_settings( array $data, int $user_id = 0, ?string $nonce = null, bool $require_nonce = true ): array {
		if ( class_exists( '\Carmilla\Core\Settings\SettingsService' ) ) {
			return \Carmilla\Core\Settings\SettingsService::save_operational_settings( $data, $user_id, $nonce, $require_nonce );
		}
		return [ 'ok' => false, 'code' => 'KERNEL_MISSING', 'status' => 500, 'message' => 'SettingsService not loaded.' ];
	}

	/**
	 * Reads and saves purchased-feature toggles via shared FeatureToggleService.
	 */
	public static function get_feature_panel( ?array $matrix_args = null ): array {
		return class_exists( '\Carmilla\Core\Entitlement\FeatureToggleService' )
			? \Carmilla\Core\Entitlement\FeatureToggleService::get_panel_state( $matrix_args )
			: [];
	}

	public static function save_feature_toggles( array $features, int $user_id = 0, ?string $nonce = null, bool $require_nonce = true, array $builders = [], ?array $matrix_args = null ): array {
		if ( class_exists( '\Carmilla\Core\Entitlement\FeatureToggleService' ) ) {
			return \Carmilla\Core\Entitlement\FeatureToggleService::save_toggles( $features, $user_id, $nonce, $require_nonce, $builders, $matrix_args );
		}
		return [ 'ok' => false, 'code' => 'KERNEL_MISSING', 'status' => 500, 'message' => 'FeatureToggleService not loaded.' ];
	}

	/**
	 * Registers shared content shortcodes in Theme-only mode when Bridge is inactive.
	 */
	public static function register_content_shortcodes(): void {
		if ( ! function_exists( 'add_shortcode' ) ) {
			return;
		}
		if ( ! shortcode_exists( 'carmilla_posts' ) ) {
			add_shortcode( 'carmilla_posts', [ self::class, 'render_posts_shortcode' ] );
		}
		if ( ! shortcode_exists( 'carmilla_page' ) ) {
			add_shortcode( 'carmilla_page', [ self::class, 'render_page_shortcode' ] );
		}
		if ( ! shortcode_exists( 'carmilla_catalog' ) ) {
			add_shortcode( 'carmilla_catalog', [ self::class, 'render_catalog_shortcode' ] );
		}
		if ( ! shortcode_exists( 'carmilla_product' ) ) {
			add_shortcode( 'carmilla_product', [ self::class, 'render_product_shortcode' ] );
		}
	}

	/**
	 * @param array<string, mixed> $atts
	 */
	public static function render_posts_shortcode( $atts = [] ): string {
		if ( ! class_exists( '\Carmilla\Core\Content\ContentService' ) ) {
			return '';
		}
		$atts = shortcode_atts( [ 'size' => 10, 'page' => 0 ], is_array( $atts ) ? $atts : [], 'carmilla_posts' );
		$carmilla_posts_data = \Carmilla\Core\Content\ContentService::list_posts( [ 'size' => (int) $atts['size'], 'page' => (int) $atts['page'] ], [ 'publish' ] );
		$tpl = locate_template( [ 'carmilla/content-posts.php', 'carmilla-content-posts.php' ] );
		if ( is_string( $tpl ) && '' !== $tpl && file_exists( $tpl ) ) {
			ob_start();
			include $tpl;
			return (string) ob_get_clean();
		}
		return '';
	}

	/**
	 * @param array<string, mixed> $atts
	 */
	public static function render_page_shortcode( $atts = [] ): string {
		if ( ! class_exists( '\Carmilla\Core\Content\ContentService' ) ) {
			return '';
		}
		$atts   = shortcode_atts( [ 'slug' => '', 'id' => 0 ], is_array( $atts ) ? $atts : [], 'carmilla_page' );
		$lookup = ! empty( $atts['id'] ) ? (int) $atts['id'] : (string) $atts['slug'];
		if ( '' === $lookup || 0 === $lookup ) {
			return '';
		}
		$page = \Carmilla\Core\Content\ContentService::get_page( $lookup, [ 'publish' ] );
		if ( is_wp_error( $page ) ) {
			return '<div class="card">' . esc_html( $page->get_error_message() ) . '</div>';
		}
		$carmilla_page_data = $page;
		$tpl = locate_template( [ 'carmilla/content-page.php', 'carmilla-content-page.php' ] );
		if ( is_string( $tpl ) && '' !== $tpl && file_exists( $tpl ) ) {
			ob_start();
			include $tpl;
			return (string) ob_get_clean();
		}
		return '';
	}

	/**
	 * @param array<string, mixed> $atts
	 */
	public static function render_catalog_shortcode( $atts = [] ): string {
		if ( ! class_exists( '\Carmilla\Core\Catalog\CatalogService' ) ) {
			return '';
		}
		$atts = shortcode_atts( [ 'size' => 12, 'page' => 0, 'query' => '' ], is_array( $atts ) ? $atts : [], 'carmilla_catalog' );
		$res  = \Carmilla\Core\Catalog\CatalogService::list_products( [
			'size'  => (int) $atts['size'],
			'page'  => (int) $atts['page'],
			'query' => (string) $atts['query'],
		] );
		if ( is_wp_error( $res ) ) {
			return '<div class="card" data-carmilla-surface="catalog-prerequisite">' . esc_html( $res->get_error_message() ) . '</div>';
		}
		$carmilla_catalog_data = $res;
		$tpl = locate_template( [ 'carmilla/catalog-list.php', 'carmilla-catalog-list.php' ] );
		if ( is_string( $tpl ) && '' !== $tpl && file_exists( $tpl ) ) {
			ob_start();
			include $tpl;
			return (string) ob_get_clean();
		}
		return '';
	}

	/**
	 * @param array<string, mixed> $atts
	 */
	public static function render_product_shortcode( $atts = [] ): string {
		if ( ! class_exists( '\Carmilla\Core\Catalog\CatalogService' ) ) {
			return '';
		}
		$atts   = shortcode_atts( [ 'id' => 0, 'slug' => '' ], is_array( $atts ) ? $atts : [], 'carmilla_product' );
		$lookup = ! empty( $atts['id'] ) ? (int) $atts['id'] : (string) $atts['slug'];
		if ( '' === $lookup || 0 === $lookup ) {
			return '';
		}
		$product = \Carmilla\Core\Catalog\CatalogService::get_product_detail( $lookup );
		if ( is_wp_error( $product ) ) {
			return '<div class="card" data-carmilla-surface="catalog-prerequisite">' . esc_html( $product->get_error_message() ) . '</div>';
		}
		$carmilla_product_data = $product;
		$tpl = locate_template( [ 'carmilla/catalog-product.php', 'carmilla-catalog-product.php' ] );
		if ( is_string( $tpl ) && '' !== $tpl && file_exists( $tpl ) ) {
			ob_start();
			include $tpl;
			return (string) ob_get_clean();
		}
		return '';
	}

	/**
	 * Resets adapter state (used in testing).
	 */
	public static function reset_state(): void {
		self::$initialized = false;
	}
}
