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
}

}
