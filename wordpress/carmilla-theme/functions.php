<?php
/**
 * Carmilla theme bootstrap.
 * Design tokens rebuilt from the Compose app's core/designSystem (Colors/Typography/Shape/Dimens).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

define( 'CARMILLA_THEME_VERSION', '0.2.0' );

require_once get_template_directory() . '/inc/icons.php';
require_once get_template_directory() . '/inc/customizer.php';
require_once get_template_directory() . '/inc/template-functions.php';
require_once get_template_directory() . '/inc/post-types.php';
require_once get_template_directory() . '/inc/meta-boxes.php';
require_once get_template_directory() . '/inc/rest.php';
require_once get_template_directory() . '/inc/psychtest.php';
require_once get_template_directory() . '/inc/cpt-public.php';
if ( is_admin() ) {
	require_once get_template_directory() . '/inc/admin-page.php';
}
if ( class_exists( 'WooCommerce' ) ) {
	require_once get_template_directory() . '/inc/woocommerce.php';
}

if ( ! function_exists( 'carmilla_setup' ) ) {
	function carmilla_setup() {
		load_theme_textdomain( 'carmilla', get_template_directory() . '/languages' );

		add_theme_support( 'title-tag' );
		add_theme_support( 'post-thumbnails' );
		add_theme_support( 'automatic-feed-links' );
		add_theme_support( 'html5', array( 'search-form', 'gallery', 'caption', 'style', 'script' ) );
		add_theme_support( 'align-wide' );
		add_theme_support( 'responsive-embeds' );
		add_theme_support( 'editor-styles' );
		add_editor_style( 'assets/css/tokens.css' );

		// WooCommerce.
		add_theme_support( 'woocommerce' );
		add_theme_support( 'wc-product-gallery-zoom' );
		add_theme_support( 'wc-product-gallery-lightbox' );
		add_theme_support( 'wc-product-gallery-slider' );

		register_nav_menus( array(
			'primary' => __( 'منوی اصلی', 'carmilla' ),
			'footer'  => __( 'منوی فوتر', 'carmilla' ),
		) );

		add_image_size( 'carmilla-card', 600, 800, true ); // 3:4 product/course cards

		add_theme_support( 'custom-logo', array(
			'height'      => 48,
			'width'       => 200,
			'flex-width'  => true,
			'flex-height' => true,
		) );
		add_theme_support( 'customize-selective-refresh-widgets' );
	}
}
add_action( 'after_setup_theme', 'carmilla_setup' );

/**
 * Enqueue the token + base stylesheets (order matters: tokens first).
 */
function carmilla_enqueue_assets() {
	$dir = get_template_directory_uri();
	$ver = CARMILLA_THEME_VERSION;

	wp_enqueue_style( 'carmilla-tokens', $dir . '/assets/css/tokens.css', array(), $ver );
	wp_enqueue_style( 'carmilla-base', $dir . '/assets/css/base.css', array( 'carmilla-tokens' ), $ver );
	wp_enqueue_style( 'carmilla-components', $dir . '/assets/css/components.css', array( 'carmilla-base' ), $ver );

	// Keep the required theme header stylesheet last (mostly metadata).
	wp_enqueue_style( 'carmilla-style', get_stylesheet_uri(), array( 'carmilla-components' ), $ver );

	// Take-test screen (theme REST + JS).
	if ( is_singular( 'cb_psychtest' ) ) {
		wp_enqueue_script( 'carmilla-psychtest', $dir . '/assets/js/psychtest.js', array(), $ver, true );
		wp_localize_script( 'carmilla-psychtest', 'CarmillaData', array(
			'restUrl' => esc_url_raw( rest_url( 'carmilla/v1/' ) ),
			'nonce'   => wp_create_nonce( 'wp_rest' ),
		) );
	}

	// Interactive course-requests screen (theme REST + JS).
	if ( is_post_type_archive( 'cb_course_request' ) ) {
		wp_enqueue_script( 'carmilla-course-requests', $dir . '/assets/js/course-requests.js', array(), $ver, true );
		wp_localize_script( 'carmilla-course-requests', 'CarmillaData', array(
			'restUrl'  => esc_url_raw( rest_url( 'carmilla/v1/' ) ),
			'nonce'    => wp_create_nonce( 'wp_rest' ),
			'loggedIn' => is_user_logged_in(),
			'loginUrl' => wp_login_url( get_post_type_archive_link( 'cb_course_request' ) ),
		) );
	}
}
add_action( 'wp_enqueue_scripts', 'carmilla_enqueue_assets' );

/**
 * Mirror the app's RTL default: force an RTL <html> even before user locale kicks in.
 */
function carmilla_html_dir( $output ) {
	if ( ! is_rtl() && strpos( $output, 'dir=' ) === false ) {
		$output .= ' dir="rtl"';
	}
	return $output;
}
add_filter( 'language_attributes', 'carmilla_html_dir' );
