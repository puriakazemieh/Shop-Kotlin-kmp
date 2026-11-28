<?php
/**
 * Public Display Infrastructure for Carmilla Bridge on Third-Party Themes.
 *
 * Provides:
 * - Shortcode `[carmilla_public_sample]` & Block `carmilla/public-sample` registry
 * - Strictly scoped CSS (`.carmilla-public-surface`) so third-party themes stay intact
 * - Public sample route (`/?carmilla_sample=1`) & REST endpoint (`/carmilla/v1/public/sample`)
 * - Theme override hook (`carmilla/public-sample.php` or `carmilla_public_sample_template`)
 *
 * Part of Task P04-WPPLUGIN-CODE-045.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( 'CB_Public_Renderer', false ) ) {

class CB_Public_Renderer {

	private static bool $booted = false;

	public static function boot(): void {
		if ( self::$booted ) {
			return;
		}
		self::$booted = true;

		add_action( 'init', array( __CLASS__, 'register_shortcodes_and_blocks' ) );
		add_action( 'wp_enqueue_scripts', array( __CLASS__, 'enqueue_scoped_assets' ), 20 );
		add_action( 'template_redirect', array( __CLASS__, 'handle_sample_route' ) );
		add_action( 'rest_api_init', array( __CLASS__, 'register_rest_routes' ) );
	}

	public static function register_shortcodes_and_blocks(): void {
		add_shortcode( 'carmilla_public_sample', array( __CLASS__, 'render_sample_shortcode' ) );
		add_shortcode( 'carmilla_posts', array( __CLASS__, 'render_posts_shortcode' ) );
		add_shortcode( 'carmilla_page', array( __CLASS__, 'render_page_shortcode' ) );
		add_shortcode( 'carmilla_content_admin', array( __CLASS__, 'render_content_admin_shortcode' ) );
		add_shortcode( 'carmilla_catalog', array( __CLASS__, 'render_catalog_shortcode' ) );
		add_shortcode( 'carmilla_product', array( __CLASS__, 'render_product_shortcode' ) );
		add_shortcode( 'carmilla_cart', array( __CLASS__, 'render_cart_shortcode' ) );
		add_shortcode( 'carmilla_orders', array( __CLASS__, 'render_orders_shortcode' ) );
		add_shortcode( 'carmilla_courses', array( __CLASS__, 'render_courses_shortcode' ) );
		add_shortcode( 'carmilla_course', array( __CLASS__, 'render_course_shortcode' ) );
		add_shortcode( 'carmilla_my_courses', array( __CLASS__, 'render_my_courses_shortcode' ) );
		add_shortcode( 'carmilla_certificates', array( __CLASS__, 'render_certificates_shortcode' ) );
		add_shortcode( 'carmilla_therapists', array( __CLASS__, 'render_therapists_shortcode' ) );
		add_shortcode( 'carmilla_therapist', array( __CLASS__, 'render_therapist_shortcode' ) );
		add_shortcode( 'carmilla_booking', array( __CLASS__, 'render_booking_shortcode' ) );
		add_shortcode( 'carmilla_my_appointments', array( __CLASS__, 'render_my_appointments_shortcode' ) );
		add_shortcode( 'carmilla_psych_tests', array( __CLASS__, 'render_psych_tests_shortcode' ) );
		add_shortcode( 'carmilla_psych_test', array( __CLASS__, 'render_psych_test_shortcode' ) );
		add_shortcode( 'carmilla_my_psych_results', array( __CLASS__, 'render_my_psych_results_shortcode' ) );

		if ( function_exists( 'register_block_type' ) && ! WP_Block_Type_Registry::get_instance()->is_registered( 'carmilla/public-sample' ) ) {
			register_block_type(
				'carmilla/public-sample',
				array(
					'render_callback' => array( __CLASS__, 'render_sample_shortcode' ),
				)
			);
		}
	}

	/**
	 * Enqueues strictly scoped CSS only when running on a third-party theme.
	 */
	public static function enqueue_scoped_assets(): void {
		if ( class_exists( 'CB_Standalone_Boot' ) && CB_Standalone_Boot::is_carmilla_theme_active() ) {
			return;
		}

		$css_url = plugins_url( 'assets/css/carmilla-public-scoped.css', dirname( __DIR__ ) . '/carmilla-bridge.php' );
		wp_enqueue_style( 'carmilla-public-scoped', $css_url, array(), defined( 'CB_VERSION' ) ? CB_VERSION : '1.0.0' );
	}

	/**
	 * Renders the public sample surface, allowing Carmilla Theme (or child themes) to override.
	 *
	 * @param array<string, mixed> $atts
	 * @return string
	 */
	public static function render_sample_shortcode( $atts = array() ): string {
		$atts = shortcode_atts(
			array(
				'title' => __( 'نمایش عمومی کارمیلا روی قالب ثالث', 'carmilla-bridge' ),
				'body'  => __( 'این صفحه نمونه از هسته و افزونه کارمیلا بدون نیاز به پوسته کارمیلا رندر شده است.', 'carmilla-bridge' ),
			),
			is_array( $atts ) ? $atts : array(),
			'carmilla_public_sample'
		);

		$host_mode = class_exists( '\Carmilla\Core\Rest\RestInfrastructure' )
			? \Carmilla\Core\Rest\RestInfrastructure::get_provider_mode()
			: 'plugin';

		// Allow active theme override via locate_template or filter
		$override_tpl = locate_template( array( 'carmilla/public-sample.php', 'carmilla-public-sample.php' ) );
		$override_tpl = apply_filters( 'carmilla_public_sample_template', $override_tpl, $atts, $host_mode );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			include $override_tpl;
			return (string) ob_get_clean();
		}

		$dir = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';

		ob_start();
		?>
		<section class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-host="<?php echo esc_attr( $host_mode ); ?>">
			<span class="carmilla-public-surface__badge"><?php echo esc_html( 'Carmilla Plugin Public Surface' ); ?></span>
			<h2 class="carmilla-public-surface__title"><?php echo esc_html( $atts['title'] ); ?></h2>
			<p class="carmilla-public-surface__body"><?php echo esc_html( $atts['body'] ); ?></p>
		</section>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Handles public sample route `/?carmilla_sample=1` inside the active third-party theme chrome.
	 */
	public static function handle_sample_route(): void {
		if ( empty( $_GET['carmilla_sample'] ) ) { // phpcs:ignore WordPress.Security.NonceVerification.Recommended
			return;
		}

		// Respect content.core entitlement/toggle gate
		if ( class_exists( '\Carmilla\Core\Capabilities\CapabilityRegistry' ) ) {
			$access = \Carmilla\Core\Capabilities\CapabilityRegistry::check_access( 0, 'content', 'read', 'content.core', array( 'is_public' => true ) );
			if ( empty( $access['allowed'] ) ) {
				status_header( (int) ( $access['status'] ?? 403 ) );
				wp_die( esc_html( $access['reason'] ?? 'Feature unavailable.' ), esc_html( $access['code'] ?? 'FORBIDDEN' ), array( 'response' => (int) ( $access['status'] ?? 403 ) ) );
			}
		}

		status_header( 200 );
		if ( function_exists( 'wp_is_block_theme' ) && wp_is_block_theme() ) {
			?><!doctype html>
			<html <?php language_attributes(); ?>>
			<head>
				<meta charset="<?php bloginfo( 'charset' ); ?>">
				<meta name="viewport" content="width=device-width, initial-scale=1">
				<?php wp_head(); ?>
			</head>
			<body <?php body_class(); ?>>
			<?php wp_body_open(); ?>
			<div class="wp-site-blocks">
				<main id="content"><?php echo self::render_sample_shortcode(); // phpcs:ignore WordPress.Security.EscapeOutput.OutputNotEscaped ?></main>
			</div>
			<?php wp_footer(); ?>
			</body>
			</html>
			<?php
			exit;
		}

		get_header();
		echo '<main id="content">' . self::render_sample_shortcode() . '</main>'; // phpcs:ignore WordPress.Security.EscapeOutput.OutputNotEscaped
		get_footer();
		exit;
	}

	/**
	 * Registers public sample REST route `/carmilla/v1/public/sample`.
	 */
	public static function register_rest_routes(): void {
		$ns = defined( 'CB_REST_NAMESPACE' ) ? CB_REST_NAMESPACE : 'carmilla/v1';
		register_rest_route(
			$ns,
			'/public/sample',
			array(
				'methods'             => 'GET',
				'callback'            => array( __CLASS__, 'rest_get_sample' ),
				'permission_callback' => array( __CLASS__, 'rest_sample_permission' ),
			)
		);
	}

	public static function rest_sample_permission( $request ) {
		$scope = is_object( $request ) && method_exists( $request, 'get_param' ) ? (string) $request->get_param( 'scope' ) : 'public';
		$uid   = function_exists( 'get_current_user_id' ) ? get_current_user_id() : 0;

		if ( 'private' === $scope ) {
			if ( class_exists( '\Carmilla\Core\Capabilities\CapabilityRegistry' ) ) {
				$res = \Carmilla\Core\Capabilities\CapabilityRegistry::check_access( $uid, 'admin', 'read', 'theme.base' );
				if ( empty( $res['allowed'] ) ) {
					return new \WP_Error( $res['code'], $res['reason'], array( 'status' => $res['status'] ) );
				}
				return true;
			}
			return current_user_can( 'manage_options' );
		}

		if ( class_exists( '\Carmilla\Core\Capabilities\CapabilityRegistry' ) ) {
			$res = \Carmilla\Core\Capabilities\CapabilityRegistry::check_access( $uid, 'content', 'read', 'content.core', array( 'is_public' => true ) );
			if ( empty( $res['allowed'] ) ) {
				return new \WP_Error( $res['code'], $res['reason'], array( 'status' => $res['status'] ) );
			}
		}

		return true;
	}

	public static function rest_get_sample( $request ) {
		return rest_ensure_response(
			array(
				'ok'        => true,
				'host_mode' => class_exists( '\Carmilla\Core\Rest\RestInfrastructure' ) ? \Carmilla\Core\Rest\RestInfrastructure::get_provider_mode() : 'plugin',
				'html'      => self::render_sample_shortcode(),
			)
		);
	}

	/**
	 * Renders public posts list via shared ContentService (or active theme override).
	 *
	 * @param array<string, mixed> $atts
	 */
	public static function render_posts_shortcode( $atts = array() ): string {
		$atts = shortcode_atts(
			array(
				'size' => 10,
				'page' => 0,
			),
			is_array( $atts ) ? $atts : array(),
			'carmilla_posts'
		);

		$data = class_exists( '\Carmilla\Core\Content\ContentService' )
			? \Carmilla\Core\Content\ContentService::list_posts( array( 'size' => (int) $atts['size'], 'page' => (int) $atts['page'] ), array( 'publish' ) )
			: array( 'content' => array() );

		$override_tpl = locate_template( array( 'carmilla/content-posts.php', 'carmilla-content-posts.php' ) );
		$override_tpl = apply_filters( 'carmilla_content_posts_template', $override_tpl, $data );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_posts_data = $data;
			include $override_tpl;
			return (string) ob_get_clean();
		}

		$dir   = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		$items = isset( $data['content'] ) && is_array( $data['content'] ) ? $data['content'] : array();
		ob_start();
		?>
		<section class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="posts">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html__( 'نوشته‌ها', 'carmilla-bridge' ); ?></h2>
			<ul class="carmilla-public-surface__list">
				<?php foreach ( $items as $item ) : ?>
					<li data-post-id="<?php echo esc_attr( (string) ( $item['id'] ?? 0 ) ); ?>">
						<strong><?php echo esc_html( (string) ( $item['title'] ?? '' ) ); ?></strong>
						<?php if ( ! empty( $item['summary'] ) ) : ?>
							<p class="carmilla-public-surface__body"><?php echo esc_html( (string) $item['summary'] ); ?></p>
						<?php endif; ?>
					</li>
				<?php endforeach; ?>
			</ul>
		</section>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Renders a single published page via shared ContentService (or active theme override).
	 *
	 * @param array<string, mixed> $atts
	 */
	public static function render_page_shortcode( $atts = array() ): string {
		$atts = shortcode_atts(
			array(
				'slug' => '',
				'id'   => 0,
			),
			is_array( $atts ) ? $atts : array(),
			'carmilla_page'
		);

		$lookup = ! empty( $atts['id'] ) ? (int) $atts['id'] : (string) $atts['slug'];
		if ( '' === $lookup || 0 === $lookup ) {
			return '';
		}

		$page = class_exists( '\Carmilla\Core\Content\ContentService' )
			? \Carmilla\Core\Content\ContentService::get_page( $lookup, array( 'publish' ) )
			: new \WP_Error( 'UNAVAILABLE', 'ContentService unavailable.' );

		if ( is_wp_error( $page ) ) {
			return '<div class="carmilla-public-surface">' . esc_html( $page->get_error_message() ) . '</div>';
		}

		$override_tpl = locate_template( array( 'carmilla/content-page.php', 'carmilla-content-page.php' ) );
		$override_tpl = apply_filters( 'carmilla_content_page_template', $override_tpl, $page );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_page_data = $page;
			include $override_tpl;
			return (string) ob_get_clean();
		}

		$dir = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		ob_start();
		?>
		<article class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="page" data-page-id="<?php echo esc_attr( (string) ( $page['id'] ?? 0 ) ); ?>">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html( (string) ( $page['title'] ?? '' ) ); ?></h2>
			<div class="carmilla-public-surface__body"><?php echo wp_kses_post( (string) ( $page['rawContent'] ?? '' ) ); ?></div>
		</article>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Basic admin content management form (requires content management capability + nonce).
	 */
	public static function render_content_admin_shortcode(): string {
		if ( ! class_exists( '\Carmilla\Core\Content\ContentService' ) || ! \Carmilla\Core\Content\ContentService::can_manage_content() ) {
			return '<div class="carmilla-public-surface">' . esc_html__( 'دسترسی غیرمجاز برای مدیریت محتوا.', 'carmilla-bridge' ) . '</div>';
		}

		$notice = '';
		if ( isset( $_POST['carmilla_content_action'], $_POST['carmilla_content_nonce'] )
			&& wp_verify_nonce( sanitize_text_field( wp_unslash( $_POST['carmilla_content_nonce'] ) ), 'carmilla_content_admin_save' ) ) {
			$kind    = sanitize_key( (string) wp_unslash( $_POST['carmilla_content_kind'] ?? 'post' ) );
			$title   = sanitize_text_field( (string) wp_unslash( $_POST['carmilla_content_title'] ?? '' ) );
			$summary = sanitize_textarea_field( (string) wp_unslash( $_POST['carmilla_content_summary'] ?? '' ) );

			$payload = array(
				'title'   => $title,
				'summary' => $summary,
				'status'  => 'PUBLISHED',
			);
			$result = ( 'page' === $kind )
				? \Carmilla\Core\Content\ContentService::create_page( $payload )
				: \Carmilla\Core\Content\ContentService::create_post( $payload );

			$notice = is_wp_error( $result )
				? $result->get_error_message()
				: sprintf( 'ثبت شد (شناسه: %d)', (int) $result['id'] );
		}

		$dir = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		ob_start();
		?>
		<section class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="content-admin">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html__( 'مدیریت نوشته و برگه', 'carmilla-bridge' ); ?></h2>
			<?php if ( '' !== $notice ) : ?>
				<p class="carmilla-public-surface__badge"><?php echo esc_html( $notice ); ?></p>
			<?php endif; ?>
			<form method="post">
				<?php wp_nonce_field( 'carmilla_content_admin_save', 'carmilla_content_nonce' ); ?>
				<input type="hidden" name="carmilla_content_action" value="create" />
				<p><label><?php echo esc_html__( 'نوع:', 'carmilla-bridge' ); ?> <select name="carmilla_content_kind"><option value="post">نوشته</option><option value="page">برگه</option></select></label></p>
				<p><label><?php echo esc_html__( 'عنوان:', 'carmilla-bridge' ); ?> <input type="text" name="carmilla_content_title" required /></label></p>
				<p><label><?php echo esc_html__( 'خلاصه:', 'carmilla-bridge' ); ?> <textarea name="carmilla_content_summary"></textarea></label></p>
				<button type="submit"><?php echo esc_html__( 'ذخیره', 'carmilla-bridge' ); ?></button>
			</form>
		</section>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Renders catalog product list via shared CatalogService (or theme override).
	 *
	 * @param array<string, mixed> $atts
	 */
	public static function render_catalog_shortcode( $atts = array() ): string {
		if ( ! class_exists( '\Carmilla\Core\Catalog\CatalogService' ) ) {
			return '';
		}
		$atts = shortcode_atts(
			array(
				'size'  => 12,
				'page'  => 0,
				'query' => '',
			),
			is_array( $atts ) ? $atts : array(),
			'carmilla_catalog'
		);

		$res = \Carmilla\Core\Catalog\CatalogService::list_products( array(
			'size'  => (int) $atts['size'],
			'page'  => (int) $atts['page'],
			'query' => (string) $atts['query'],
		) );

		if ( is_wp_error( $res ) ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="catalog-prerequisite">' . esc_html( $res->get_error_message() ) . '</div>';
		}

		$override_tpl = locate_template( array( 'carmilla/catalog-list.php', 'carmilla-catalog-list.php' ) );
		$override_tpl = apply_filters( 'carmilla_catalog_list_template', $override_tpl, $res );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_catalog_data = $res;
			include $override_tpl;
			return (string) ob_get_clean();
		}

		$dir   = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		$items = isset( $res['content'] ) && is_array( $res['content'] ) ? $res['content'] : array();
		ob_start();
		?>
		<section class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="catalog">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html__( 'فروشگاه', 'carmilla-bridge' ); ?></h2>
			<ul class="carmilla-public-surface__list">
				<?php foreach ( $items as $item ) : ?>
					<li data-product-id="<?php echo esc_attr( (string) ( $item['id'] ?? 0 ) ); ?>">
						<strong><?php echo esc_html( (string) ( $item['title'] ?? '' ) ); ?></strong>
					</li>
				<?php endforeach; ?>
			</ul>
		</section>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Renders single product detail via shared CatalogService (or theme override).
	 *
	 * @param array<string, mixed> $atts
	 */
	public static function render_product_shortcode( $atts = array() ): string {
		if ( ! class_exists( '\Carmilla\Core\Catalog\CatalogService' ) ) {
			return '';
		}
		$atts   = shortcode_atts( array( 'id' => 0, 'slug' => '' ), is_array( $atts ) ? $atts : array(), 'carmilla_product' );
		$lookup = ! empty( $atts['id'] ) ? (int) $atts['id'] : (string) $atts['slug'];
		if ( '' === $lookup || 0 === $lookup ) {
			return '';
		}

		$product = \Carmilla\Core\Catalog\CatalogService::get_product_detail( $lookup );
		if ( is_wp_error( $product ) ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="catalog-prerequisite">' . esc_html( $product->get_error_message() ) . '</div>';
		}

		$override_tpl = locate_template( array( 'carmilla/catalog-product.php', 'carmilla-catalog-product.php' ) );
		$override_tpl = apply_filters( 'carmilla_catalog_product_template', $override_tpl, $product );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_product_data = $product;
			include $override_tpl;
			return (string) ob_get_clean();
		}

		$dir = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		ob_start();
		?>
		<article class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="product" data-product-id="<?php echo esc_attr( (string) ( $product['id'] ?? 0 ) ); ?>">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html( (string) ( $product['title'] ?? '' ) ); ?></h2>
			<div class="carmilla-public-surface__body"><?php echo wp_kses_post( (string) ( $product['description'] ?? '' ) ); ?></div>
		</article>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Renders cart summary via shared CartOrderService (or theme override).
	 */
	public static function render_cart_shortcode(): string {
		if ( ! class_exists( '\Carmilla\Core\Commerce\CartOrderService' ) ) {
			return '';
		}
		if ( ! \Carmilla\Core\Commerce\CartOrderService::is_enabled() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="cart-disabled">' . esc_html__( 'قابلیت سبد خرید فعال نیست.', 'carmilla-bridge' ) . '</div>';
		}
		if ( ! is_user_logged_in() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="cart-auth">' . esc_html__( 'برای مشاهده سبد خرید وارد شوید.', 'carmilla-bridge' ) . '</div>';
		}

		$cart = \Carmilla\Core\Commerce\CartOrderService::get_cart( get_current_user_id() );
		$override_tpl = locate_template( array( 'carmilla/cart-summary.php', 'carmilla-cart-summary.php' ) );
		$override_tpl = apply_filters( 'carmilla_cart_summary_template', $override_tpl, $cart );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_cart_data = $cart;
			include $override_tpl;
			return (string) ob_get_clean();
		}

		$dir = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		ob_start();
		?>
		<section class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="cart" data-cart-total="<?php echo esc_attr( (string) ( $cart['total'] ?? 0 ) ); ?>">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html__( 'سبد خرید', 'carmilla-bridge' ); ?></h2>
			<p class="carmilla-public-surface__body"><?php echo esc_html( sprintf( 'تعداد اقلام: %d | جمع کل: %s', (int) ( $cart['totalQty'] ?? 0 ), (string) ( $cart['total'] ?? 0 ) ) ); ?></p>
		</section>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Renders user orders list via shared CartOrderService (or theme override).
	 */
	public static function render_orders_shortcode(): string {
		if ( ! class_exists( '\Carmilla\Core\Commerce\CartOrderService' ) ) {
			return '';
		}
		if ( ! \Carmilla\Core\Commerce\CartOrderService::is_enabled() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="orders-disabled">' . esc_html__( 'قابلیت سفارش‌ها فعال نیست.', 'carmilla-bridge' ) . '</div>';
		}
		if ( ! is_user_logged_in() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="orders-auth">' . esc_html__( 'برای مشاهده سفارش‌ها وارد شوید.', 'carmilla-bridge' ) . '</div>';
		}

		$orders = \Carmilla\Core\Commerce\CartOrderService::list_orders( get_current_user_id() );
		if ( is_wp_error( $orders ) ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="orders-prerequisite">' . esc_html( $orders->get_error_message() ) . '</div>';
		}

		$override_tpl = locate_template( array( 'carmilla/orders-list.php', 'carmilla-orders-list.php' ) );
		$override_tpl = apply_filters( 'carmilla_orders_list_template', $override_tpl, $orders );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_orders_data = $orders;
			include $override_tpl;
			return (string) ob_get_clean();
		}

		$dir = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		ob_start();
		?>
		<section class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="orders">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html__( 'سفارش‌های من', 'carmilla-bridge' ); ?></h2>
			<ul class="carmilla-public-surface__list">
				<?php foreach ( $orders as $o ) : ?>
					<li data-order-id="<?php echo esc_attr( (string) ( $o['id'] ?? 0 ) ); ?>">#<?php echo esc_html( (string) ( $o['id'] ?? 0 ) ); ?> — <?php echo esc_html( (string) ( $o['totalPrice'] ?? 0 ) ); ?></li>
				<?php endforeach; ?>
			</ul>
		</section>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Renders courses list via shared CourseCatalogService (or theme override).
	 *
	 * @param array<string, mixed> $atts
	 */
	public static function render_courses_shortcode( $atts = array() ): string {
		if ( ! class_exists( '\Carmilla\Core\Academy\CourseCatalogService' ) ) {
			return '';
		}
		if ( ! \Carmilla\Core\Academy\CourseCatalogService::is_enabled() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="academy-disabled">' . esc_html__( 'قابلیت آموزش فعال نیست.', 'carmilla-bridge' ) . '</div>';
		}

		$atts    = shortcode_atts( array( 'size' => 20 ), is_array( $atts ) ? $atts : array(), 'carmilla_courses' );
		$courses = \Carmilla\Core\Academy\CourseCatalogService::list_courses( get_current_user_id(), array( 'size' => (int) $atts['size'] ) );

		$override_tpl = locate_template( array( 'carmilla/courses-list.php', 'carmilla-courses-list.php' ) );
		$override_tpl = apply_filters( 'carmilla_courses_list_template', $override_tpl, $courses );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_courses_data = $courses;
			include $override_tpl;
			return (string) ob_get_clean();
		}

		$dir = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		ob_start();
		?>
		<section class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="courses">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html__( 'دوره‌های آموزشی', 'carmilla-bridge' ); ?></h2>
			<ul class="carmilla-public-surface__list">
				<?php foreach ( $courses as $c ) : ?>
					<li data-course-id="<?php echo esc_attr( (string) ( $c['id'] ?? 0 ) ); ?>">
						<strong><?php echo esc_html( (string) ( $c['title'] ?? '' ) ); ?></strong>
					</li>
				<?php endforeach; ?>
			</ul>
		</section>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Renders single course detail & lesson structure via CourseCatalogService.
	 *
	 * @param array<string, mixed> $atts
	 */
	public static function render_course_shortcode( $atts = array() ): string {
		if ( ! class_exists( '\Carmilla\Core\Academy\CourseCatalogService' ) ) {
			return '';
		}
		if ( ! \Carmilla\Core\Academy\CourseCatalogService::is_enabled() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="academy-disabled">' . esc_html__( 'قابلیت آموزش فعال نیست.', 'carmilla-bridge' ) . '</div>';
		}

		$atts   = shortcode_atts( array( 'id' => 0, 'slug' => '' ), is_array( $atts ) ? $atts : array(), 'carmilla_course' );
		$lookup = ! empty( $atts['id'] ) ? (int) $atts['id'] : (string) $atts['slug'];
		if ( '' === $lookup || 0 === $lookup ) {
			return '';
		}

		$course = \Carmilla\Core\Academy\CourseCatalogService::get_course_detail( $lookup, get_current_user_id() );
		if ( is_wp_error( $course ) ) {
			return '<div class="carmilla-public-surface">' . esc_html( $course->get_error_message() ) . '</div>';
		}

		$override_tpl = locate_template( array( 'carmilla/course-detail.php', 'carmilla-course-detail.php' ) );
		$override_tpl = apply_filters( 'carmilla_course_detail_template', $override_tpl, $course );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_course_data = $course;
			include $override_tpl;
			return (string) ob_get_clean();
		}

		$dir     = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		$lessons = $course['sections'][0]['lessons'] ?? array();
		ob_start();
		?>
		<article class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="course" data-course-id="<?php echo esc_attr( (string) ( $course['id'] ?? 0 ) ); ?>">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html( (string) ( $course['title'] ?? '' ) ); ?></h2>
			<ul class="carmilla-public-surface__list">
				<?php foreach ( $lessons as $l ) : ?>
					<li data-lesson-id="<?php echo esc_attr( (string) ( $l['id'] ?? 0 ) ); ?>" data-locked="<?php echo empty( $l['videoUrl'] ) && empty( $l['freePreview'] ) ? '1' : '0'; ?>">
						<?php echo esc_html( (string) ( $l['title'] ?? '' ) ); ?>
					</li>
				<?php endforeach; ?>
			</ul>
		</article>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Renders enrolled courses & progress via shared CourseEnrollmentService.
	 */
	public static function render_my_courses_shortcode(): string {
		if ( ! class_exists( '\Carmilla\Core\Academy\CourseEnrollmentService' ) ) {
			return '';
		}
		if ( ! \Carmilla\Core\Academy\CourseCatalogService::is_enabled() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="academy-disabled">' . esc_html__( 'قابلیت آموزش فعال نیست.', 'carmilla-bridge' ) . '</div>';
		}
		if ( ! is_user_logged_in() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="my-courses-auth">' . esc_html__( 'برای مشاهده دوره‌های من وارد شوید.', 'carmilla-bridge' ) . '</div>';
		}

		$courses      = \Carmilla\Core\Academy\CourseEnrollmentService::my_courses( get_current_user_id() );
		$override_tpl = locate_template( array( 'carmilla/my-courses.php', 'carmilla-my-courses.php' ) );
		$override_tpl = apply_filters( 'carmilla_my_courses_template', $override_tpl, $courses );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_my_courses_data = $courses;
			include $override_tpl;
			return (string) ob_get_clean();
		}

		$dir = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		ob_start();
		?>
		<section class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="my-courses">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html__( 'دوره‌های من', 'carmilla-bridge' ); ?></h2>
			<ul class="carmilla-public-surface__list">
				<?php foreach ( $courses as $c ) : ?>
					<li data-course-id="<?php echo esc_attr( (string) ( $c['id'] ?? 0 ) ); ?>" data-progress="<?php echo esc_attr( (string) ( $c['progressPercent'] ?? 0 ) ); ?>">
						<strong><?php echo esc_html( (string) ( $c['title'] ?? '' ) ); ?></strong>
						<span>(<?php echo esc_html( (string) ( $c['progressPercent'] ?? 0 ) ); ?>%)</span>
					</li>
				<?php endforeach; ?>
			</ul>
		</section>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Renders user certificates via shared CourseQuizService.
	 */
	public static function render_certificates_shortcode(): string {
		if ( ! class_exists( '\Carmilla\Core\Academy\CourseQuizService' ) ) {
			return '';
		}
		if ( ! \Carmilla\Core\Academy\CourseCatalogService::is_enabled() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="academy-disabled">' . esc_html__( 'قابلیت آموزش فعال نیست.', 'carmilla-bridge' ) . '</div>';
		}
		if ( ! is_user_logged_in() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="certificates-auth">' . esc_html__( 'برای مشاهده گواهی‌ها وارد شوید.', 'carmilla-bridge' ) . '</div>';
		}

		$certs        = \Carmilla\Core\Academy\CourseQuizService::list_certificates( get_current_user_id() );
		$override_tpl = locate_template( array( 'carmilla/certificates.php', 'carmilla-certificates.php' ) );
		$override_tpl = apply_filters( 'carmilla_certificates_template', $override_tpl, $certs );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_certs_data = $certs;
			include $override_tpl;
			return (string) ob_get_clean();
		}

		$dir = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		ob_start();
		?>
		<section class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="certificates">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html__( 'گواهی‌های من', 'carmilla-bridge' ); ?></h2>
			<ul class="carmilla-public-surface__list">
				<?php foreach ( $certs as $c ) : ?>
					<li data-cert-number="<?php echo esc_attr( (string) ( $c['certNumber'] ?? '' ) ); ?>">
						<strong><?php echo esc_html( (string) ( $c['courseTitle'] ?? '' ) ); ?></strong>
						<span>(<?php echo esc_html( (string) ( $c['certNumber'] ?? '' ) ); ?>)</span>
					</li>
				<?php endforeach; ?>
			</ul>
		</section>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Renders therapists catalog via shared TherapistCatalogService.
	 */
	public static function render_therapists_shortcode(): string {
		if ( ! class_exists( '\Carmilla\Core\Clinic\TherapistCatalogService' ) ) {
			return '';
		}
		if ( ! \Carmilla\Core\Clinic\TherapistCatalogService::is_enabled() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="clinic-disabled">' . esc_html__( 'قابلیت معرفی متخصص و نوبت‌دهی فعال نیست.', 'carmilla-bridge' ) . '</div>';
		}

		$therapists   = \Carmilla\Core\Clinic\TherapistCatalogService::list_therapists( false );
		$override_tpl = locate_template( array( 'carmilla/therapists-list.php', 'carmilla-therapists-list.php' ) );
		$override_tpl = apply_filters( 'carmilla_therapists_list_template', $override_tpl, $therapists );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_therapists_data = $therapists;
			include $override_tpl;
			return (string) ob_get_clean();
		}

		$dir = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		ob_start();
		?>
		<section class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="therapists">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html__( 'متخصصان و مشاوران', 'carmilla-bridge' ); ?></h2>
			<ul class="carmilla-public-surface__list">
				<?php foreach ( $therapists as $t ) : ?>
					<li data-therapist-id="<?php echo esc_attr( (string) ( $t['id'] ?? 0 ) ); ?>">
						<strong><?php echo esc_html( (string) ( $t['name'] ?? '' ) ); ?></strong>
						<span>(<?php echo esc_html( (string) ( $t['availableSlotCount'] ?? 0 ) ); ?> بازه)</span>
					</li>
				<?php endforeach; ?>
			</ul>
		</section>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Renders single therapist profile & available slots via TherapistCatalogService.
	 *
	 * @param array<string, mixed> $atts
	 */
	public static function render_therapist_shortcode( $atts = array() ): string {
		if ( ! class_exists( '\Carmilla\Core\Clinic\TherapistCatalogService' ) ) {
			return '';
		}
		if ( ! \Carmilla\Core\Clinic\TherapistCatalogService::is_enabled() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="clinic-disabled">' . esc_html__( 'قابلیت معرفی متخصص و نوبت‌دهی فعال نیست.', 'carmilla-bridge' ) . '</div>';
		}

		$atts   = shortcode_atts( array( 'id' => 0, 'slug' => '' ), is_array( $atts ) ? $atts : array(), 'carmilla_therapist' );
		$lookup = ! empty( $atts['id'] ) ? (int) $atts['id'] : (string) $atts['slug'];
		if ( '' === $lookup || 0 === $lookup ) {
			return '';
		}

		$t = \Carmilla\Core\Clinic\TherapistCatalogService::get_therapist_detail( $lookup );
		if ( is_wp_error( $t ) ) {
			return '<div class="carmilla-public-surface">' . esc_html( $t->get_error_message() ) . '</div>';
		}

		$override_tpl = locate_template( array( 'carmilla/therapist-detail.php', 'carmilla-therapist-detail.php' ) );
		$override_tpl = apply_filters( 'carmilla_therapist_detail_template', $override_tpl, $t );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_therapist_data = $t;
			include $override_tpl;
			return (string) ob_get_clean();
		}

		$dir   = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		$slots = isset( $t['slots'] ) && is_array( $t['slots'] ) ? $t['slots'] : array();
		ob_start();
		?>
		<article class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="therapist" data-therapist-id="<?php echo esc_attr( (string) ( $t['id'] ?? 0 ) ); ?>" data-timezone="<?php echo esc_attr( (string) ( $t['timezone'] ?? 'UTC' ) ); ?>">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html( (string) ( $t['name'] ?? '' ) ); ?></h2>
			<ul class="carmilla-public-surface__list">
				<?php foreach ( $slots as $s ) : ?>
					<li data-slot-id="<?php echo esc_attr( (string) ( $s['id'] ?? 0 ) ); ?>">
						<?php echo esc_html( (string) ( $s['dayLabel'] ?? '' ) . ' ' . (string) ( $s['timeLabel'] ?? '' ) ); ?>
					</li>
				<?php endforeach; ?>
			</ul>
		</article>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Renders booking form for a therapist via AppointmentBookingService & TherapistCatalogService.
	 *
	 * @param array<string, mixed> $atts
	 */
	public static function render_booking_shortcode( $atts = array() ): string {
		if ( ! class_exists( '\Carmilla\Core\Clinic\AppointmentBookingService' ) || ! class_exists( '\Carmilla\Core\Clinic\TherapistCatalogService' ) ) {
			return '';
		}
		if ( ! \Carmilla\Core\Clinic\AppointmentBookingService::is_enabled() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="clinic-disabled">' . esc_html__( 'قابلیت معرفی متخصص و نوبت‌دهی فعال نیست.', 'carmilla-bridge' ) . '</div>';
		}

		$atts   = shortcode_atts( array( 'therapist_id' => 0, 'slug' => '' ), is_array( $atts ) ? $atts : array(), 'carmilla_booking' );
		$lookup = ! empty( $atts['therapist_id'] ) ? (int) $atts['therapist_id'] : (string) $atts['slug'];
		if ( '' === $lookup || 0 === $lookup ) {
			return '';
		}

		$t = \Carmilla\Core\Clinic\TherapistCatalogService::get_therapist_detail( $lookup );
		if ( is_wp_error( $t ) ) {
			return '<div class="carmilla-public-surface">' . esc_html( $t->get_error_message() ) . '</div>';
		}

		$override_tpl = locate_template( array( 'carmilla/booking-form.php', 'carmilla-booking-form.php' ) );
		$override_tpl = apply_filters( 'carmilla_booking_form_template', $override_tpl, $t );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_therapist_data = $t;
			include $override_tpl;
			return (string) ob_get_clean();
		}

		$dir   = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		$slots = isset( $t['slots'] ) && is_array( $t['slots'] ) ? $t['slots'] : array();
		ob_start();
		?>
		<section class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="booking-form" data-therapist-id="<?php echo esc_attr( (string) ( $t['id'] ?? 0 ) ); ?>">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html( sprintf( 'رزرو نوبت: %s', (string) ( $t['name'] ?? '' ) ) ); ?></h2>
			<form class="carmilla-public-surface__form" method="post" action="<?php echo esc_url( rest_url( 'carmilla/v1/api/clinic/appointments' ) ); ?>">
				<ul class="carmilla-public-surface__list">
					<?php foreach ( $slots as $s ) : ?>
						<li>
							<label>
								<input type="radio" name="slotId" value="<?php echo esc_attr( (string) ( $s['id'] ?? 0 ) ); ?>" required />
								<span><?php echo esc_html( (string) ( $s['dayLabel'] ?? '' ) . ' ' . (string) ( $s['timeLabel'] ?? '' ) ); ?></span>
							</label>
						</li>
					<?php endforeach; ?>
				</ul>
				<button type="submit" class="carmilla-public-surface__btn"><?php echo esc_html__( 'ثبت نوبت', 'carmilla-bridge' ); ?></button>
			</form>
		</section>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Renders current user's appointments via AppointmentBookingService.
	 */
	public static function render_my_appointments_shortcode(): string {
		if ( ! class_exists( '\Carmilla\Core\Clinic\AppointmentBookingService' ) ) {
			return '';
		}
		if ( ! \Carmilla\Core\Clinic\AppointmentBookingService::is_enabled() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="clinic-disabled">' . esc_html__( 'قابلیت معرفی متخصص و نوبت‌دهی فعال نیست.', 'carmilla-bridge' ) . '</div>';
		}
		if ( ! is_user_logged_in() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="my-appointments-auth">' . esc_html__( 'برای مشاهده نوبت‌های من وارد شوید.', 'carmilla-bridge' ) . '</div>';
		}

		$appointments = \Carmilla\Core\Clinic\AppointmentBookingService::my_appointments( get_current_user_id() );
		$override_tpl = locate_template( array( 'carmilla/my-appointments.php', 'carmilla-my-appointments.php' ) );
		$override_tpl = apply_filters( 'carmilla_my_appointments_template', $override_tpl, $appointments );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_appointments_data = $appointments;
			include $override_tpl;
			return (string) ob_get_clean();
		}

		$dir = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		ob_start();
		?>
		<section class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="my-appointments">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html__( 'نوبت‌های من', 'carmilla-bridge' ); ?></h2>
			<ul class="carmilla-public-surface__list">
				<?php foreach ( $appointments as $a ) : ?>
					<li data-appointment-id="<?php echo esc_attr( (string) ( $a['id'] ?? 0 ) ); ?>" data-status="<?php echo esc_attr( (string) ( $a['status'] ?? '' ) ); ?>">
						<strong><?php echo esc_html( (string) ( $a['therapistName'] ?? '' ) ); ?></strong>
						<span>(<?php echo esc_html( (string) ( $a['dayLabel'] ?? '' ) . ' ' . (string) ( $a['timeLabel'] ?? '' ) ); ?>)</span>
						<em><?php echo esc_html( (string) ( $a['status'] ?? '' ) ); ?></em>
					</li>
				<?php endforeach; ?>
			</ul>
		</section>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Renders psychological tests catalog via PsychTestService.
	 */
	public static function render_psych_tests_shortcode(): string {
		if ( ! class_exists( '\Carmilla\Core\Psych\PsychTestService' ) ) {
			return '';
		}
		if ( ! \Carmilla\Core\Psych\PsychTestService::is_enabled() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="psych-disabled">' . esc_html__( 'قابلیت تست روان‌شناسی فعال نیست.', 'carmilla-bridge' ) . '</div>';
		}
		$tests        = \Carmilla\Core\Psych\PsychTestService::list_tests( get_current_user_id(), false );
		$override_tpl = locate_template( array( 'carmilla/psych-tests-list.php', 'carmilla-psych-tests-list.php' ) );
		$override_tpl = apply_filters( 'carmilla_psych_tests_list_template', $override_tpl, $tests );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_psych_tests_data = $tests;
			include $override_tpl;
			return (string) ob_get_clean();
		}
		$dir = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		ob_start();
		?>
		<section class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="psych-tests">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html__( 'تست‌های روان‌شناسی', 'carmilla-bridge' ); ?></h2>
			<ul class="carmilla-public-surface__list">
				<?php foreach ( $tests as $t ) : ?>
					<li data-test-id="<?php echo esc_attr( (string) ( $t['id'] ?? 0 ) ); ?>">
						<strong><?php echo esc_html( (string) ( $t['title'] ?? '' ) ); ?></strong>
						<span>(<?php echo esc_html( (string) ( $t['questionCount'] ?? 0 ) ); ?> سؤال)</span>
					</li>
				<?php endforeach; ?>
			</ul>
		</section>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Renders single psychological test form (option scores never exposed).
	 *
	 * @param array<string, mixed> $atts
	 */
	public static function render_psych_test_shortcode( $atts = array() ): string {
		if ( ! class_exists( '\Carmilla\Core\Psych\PsychTestService' ) ) {
			return '';
		}
		if ( ! \Carmilla\Core\Psych\PsychTestService::is_enabled() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="psych-disabled">' . esc_html__( 'قابلیت تست روان‌شناسی فعال نیست.', 'carmilla-bridge' ) . '</div>';
		}
		$atts   = shortcode_atts( array( 'id' => 0, 'slug' => '' ), is_array( $atts ) ? $atts : array(), 'carmilla_psych_test' );
		$lookup = ! empty( $atts['id'] ) ? (int) $atts['id'] : (string) $atts['slug'];
		if ( '' === $lookup || 0 === $lookup ) {
			return '';
		}
		$t = \Carmilla\Core\Psych\PsychTestService::get_test_detail( $lookup, get_current_user_id() );
		if ( is_wp_error( $t ) ) {
			return '<div class="carmilla-public-surface">' . esc_html( $t->get_error_message() ) . '</div>';
		}
		$override_tpl = locate_template( array( 'carmilla/psych-test-detail.php', 'carmilla-psych-test-detail.php' ) );
		$override_tpl = apply_filters( 'carmilla_psych_test_detail_template', $override_tpl, $t );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_psych_test_data = $t;
			include $override_tpl;
			return (string) ob_get_clean();
		}
		$dir       = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		$questions = isset( $t['questions'] ) && is_array( $t['questions'] ) ? $t['questions'] : array();
		ob_start();
		?>
		<section class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="psych-test-form" data-test-id="<?php echo esc_attr( (string) ( $t['id'] ?? 0 ) ); ?>">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html( (string) ( $t['title'] ?? '' ) ); ?></h2>
			<form class="carmilla-public-surface__form" method="post" action="<?php echo esc_url( rest_url( 'carmilla/v1/api/psych-tests/' . (int) ( $t['id'] ?? 0 ) . '/submit' ) ); ?>">
				<?php foreach ( $questions as $q ) : ?>
					<fieldset>
						<legend><?php echo esc_html( (string) ( $q['text'] ?? '' ) ); ?></legend>
						<?php foreach ( (array) ( $q['options'] ?? array() ) as $oi => $o ) : ?>
							<label><input type="radio" name="q<?php echo esc_attr( (string) ( $q['index'] ?? 0 ) ); ?>" value="<?php echo esc_attr( (string) $oi ); ?>" required /> <?php echo esc_html( (string) ( $o['text'] ?? '' ) ); ?></label>
						<?php endforeach; ?>
					</fieldset>
				<?php endforeach; ?>
				<button type="submit" class="carmilla-public-surface__btn"><?php echo esc_html__( 'ثبت پاسخ و مشاهده نتیجه', 'carmilla-bridge' ); ?></button>
			</form>
		</section>
		<?php
		return (string) ob_get_clean();
	}

	/**
	 * Renders current user's private psychological test results (owner-only).
	 */
	public static function render_my_psych_results_shortcode(): string {
		if ( ! class_exists( '\Carmilla\Core\Psych\PsychTestService' ) ) {
			return '';
		}
		if ( ! \Carmilla\Core\Psych\PsychTestService::is_enabled() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="psych-disabled">' . esc_html__( 'قابلیت تست روان‌شناسی فعال نیست.', 'carmilla-bridge' ) . '</div>';
		}
		if ( ! is_user_logged_in() ) {
			return '<div class="carmilla-public-surface" data-carmilla-surface="psych-results-auth">' . esc_html__( 'برای مشاهده نتایج تست‌ها وارد شوید.', 'carmilla-bridge' ) . '</div>';
		}
		$results      = \Carmilla\Core\Psych\PsychTestService::my_tests( get_current_user_id() );
		$override_tpl = locate_template( array( 'carmilla/my-psych-results.php', 'carmilla-my-psych-results.php' ) );
		$override_tpl = apply_filters( 'carmilla_my_psych_results_template', $override_tpl, $results );
		if ( is_string( $override_tpl ) && ! empty( $override_tpl ) && file_exists( $override_tpl ) ) {
			ob_start();
			$carmilla_psych_results_data = $results;
			include $override_tpl;
			return (string) ob_get_clean();
		}
		$dir = function_exists( 'is_rtl' ) && is_rtl() ? 'rtl' : 'ltr';
		ob_start();
		?>
		<section class="carmilla-public-surface" dir="<?php echo esc_attr( $dir ); ?>" data-carmilla-surface="my-psych-results">
			<h2 class="carmilla-public-surface__title"><?php echo esc_html__( 'نتایج تست‌های من', 'carmilla-bridge' ); ?></h2>
			<ul class="carmilla-public-surface__list">
				<?php foreach ( $results as $r ) : ?>
					<li data-attempt-id="<?php echo esc_attr( (string) ( $r['id'] ?? 0 ) ); ?>" data-status="<?php echo esc_attr( (string) ( $r['status'] ?? '' ) ); ?>">
						<strong><?php echo esc_html( (string) ( $r['testTitle'] ?? '' ) ); ?></strong>
						<span><?php echo esc_html( (string) ( $r['status'] ?? '' ) ); ?></span>
						<?php if ( isset( $r['totalScore'] ) && null !== $r['totalScore'] ) : ?>
							<em>امتیاز: <?php echo esc_html( (string) $r['totalScore'] ); ?></em>
						<?php endif; ?>
					</li>
				<?php endforeach; ?>
			</ul>
		</section>
		<?php
		return (string) ob_get_clean();
	}
}

}
