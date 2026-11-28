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
}

}
