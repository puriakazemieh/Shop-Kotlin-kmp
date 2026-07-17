<?php
/**
 * Carmilla theme bootstrap.
 * Design tokens rebuilt from the Compose app's core/designSystem (Colors/Typography/Shape/Dimens).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

define( 'CARMILLA_THEME_VERSION', '0.7.5' );

require_once get_template_directory() . '/inc/icons.php';
require_once get_template_directory() . '/inc/customizer.php';
require_once get_template_directory() . '/inc/template-functions.php';
require_once get_template_directory() . '/inc/dc.php';
require_once get_template_directory() . '/inc/post-types.php';
require_once get_template_directory() . '/inc/meta-boxes.php';
require_once get_template_directory() . '/inc/rest.php';
require_once get_template_directory() . '/inc/psychtest.php';
require_once get_template_directory() . '/inc/access.php';
require_once get_template_directory() . '/inc/booking.php';
require_once get_template_directory() . '/inc/course.php';
require_once get_template_directory() . '/inc/support.php';
require_once get_template_directory() . '/inc/clinic-extra.php';
require_once get_template_directory() . '/inc/academy-extra.php';
require_once get_template_directory() . '/inc/orders-extra.php';
require_once get_template_directory() . '/inc/wishlist.php';
require_once get_template_directory() . '/inc/catalog-extra.php';
require_once get_template_directory() . '/inc/cpt-public.php';
if ( is_admin() ) {
	require_once get_template_directory() . '/inc/admin-page.php';
	require_once get_template_directory() . '/inc/demo-import.php';
}
if ( class_exists( 'WooCommerce' ) ) {
	require_once get_template_directory() . '/inc/woocommerce.php';
	require_once get_template_directory() . '/inc/product.php';
	require_once get_template_directory() . '/inc/compare.php';
	require_once get_template_directory() . '/inc/bundle.php';
	require_once get_template_directory() . '/inc/assistant.php';
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
	// Shared layer for the DC (Design-Compose) faithful templates (reset, utilities, keyframes).
	wp_enqueue_style( 'carmilla-dc', $dir . '/assets/css/dc.css', array( 'carmilla-components' ), $ver );
	if ( class_exists( 'WooCommerce' ) ) {
		wp_enqueue_style( 'carmilla-woo', $dir . '/assets/css/woocommerce.css', array( 'carmilla-components' ), $ver );
	}

	// Keep the required theme header stylesheet last (mostly metadata).
	wp_enqueue_style( 'carmilla-style', get_stylesheet_uri(), array( 'carmilla-components' ), $ver );

	// Fullscreen story viewer on the home page.
	if ( is_front_page() || is_home() ) {
		wp_enqueue_script( 'carmilla-stories', $dir . '/assets/js/stories.js', array(), $ver, true );
	}

	// Shared REST payload — identical across scripts that read window.CarmillaData
	// so co-enqueued widgets on the same page never clobber each other's data.
	$carmilla_common = array(
		'restUrl'  => esc_url_raw( rest_url( 'carmilla/v1/' ) ),
		'nonce'    => wp_create_nonce( 'wp_rest' ),
		'loggedIn' => is_user_logged_in(),
		'loginUrl' => wp_login_url(),
	);

	// Product comparison (toggles on cards/single + the compare page). Woo only.
	if ( class_exists( 'WooCommerce' ) ) {
		wp_enqueue_script( 'carmilla-compare', $dir . '/assets/js/compare.js', array(), $ver, true );
		wp_localize_script( 'carmilla-compare', 'CarmillaData', $carmilla_common );

		// Wishlist hearts on product cards / single product (site-wide under Woo).
		wp_enqueue_script( 'carmilla-wishlist', $dir . '/assets/js/wishlist.js', array(), $ver, true );
		wp_localize_script( 'carmilla-wishlist', 'CarmillaData', $carmilla_common );
	}

	// Account extras: returns, recurring orders, settings theme toggle.
	if ( function_exists( 'is_account_page' ) && is_account_page() ) {
		wp_enqueue_script( 'carmilla-orders-extra', $dir . '/assets/js/orders-extra.js', array(), $ver, true );
		wp_localize_script( 'carmilla-orders-extra', 'CarmillaData', $carmilla_common );
	}

	// Academy widgets: course quiz + project/peer (single course), placement &
	// certificate verification (shortcode pages).
	$needs_academy = is_singular( 'cb_course' );
	if ( ! $needs_academy && is_singular() ) {
		$p = get_post();
		if ( $p && ( has_shortcode( $p->post_content, 'carmilla_placement' ) || has_shortcode( $p->post_content, 'carmilla_verify' ) ) ) {
			$needs_academy = true;
		}
	}
	if ( $needs_academy ) {
		wp_enqueue_script( 'carmilla-academy', $dir . '/assets/js/academy.js', array(), $ver, true );
		wp_localize_script( 'carmilla-academy', 'CarmillaData', $carmilla_common );
	}

	// Shopping assistant — only where the [carmilla_assistant] shortcode is used.
	if ( class_exists( 'WooCommerce' ) && is_singular() ) {
		$post = get_post();
		if ( $post && has_shortcode( $post->post_content, 'carmilla_assistant' ) ) {
			wp_enqueue_script( 'carmilla-assistant', $dir . '/assets/js/assistant.js', array(), $ver, true );
			wp_localize_script( 'carmilla-assistant', 'CarmillaData', array(
				'restUrl' => esc_url_raw( rest_url( 'carmilla/v1/' ) ),
			) );
		}
	}

	// Product Q&A (theme REST + JS) on single product pages.
	if ( function_exists( 'is_product' ) && is_product() ) {
		wp_enqueue_script( 'carmilla-product-qna', $dir . '/assets/js/product-qna.js', array(), $ver, true );
		wp_localize_script( 'carmilla-product-qna', 'CarmillaData', array(
			'restUrl'  => esc_url_raw( rest_url( 'carmilla/v1/' ) ),
			'nonce'    => wp_create_nonce( 'wp_rest' ),
			'loggedIn' => is_user_logged_in(),
			'loginUrl' => wp_login_url( get_permalink() ),
		) );
	}

	// Course player (theme REST + JS).
	if ( is_singular( 'cb_course' ) ) {
		wp_enqueue_script( 'carmilla-course-learn', $dir . '/assets/js/course-learn.js', array(), $ver, true );
		wp_localize_script( 'carmilla-course-learn', 'CarmillaData', array(
			'restUrl'  => esc_url_raw( rest_url( 'carmilla/v1/' ) ),
			'nonce'    => wp_create_nonce( 'wp_rest' ),
			'loggedIn' => is_user_logged_in(),
		) );
	}

	// Therapist match on the therapist archive.
	if ( is_post_type_archive( 'cb_therapist' ) ) {
		wp_enqueue_script( 'carmilla-therapist-match', $dir . '/assets/js/therapist-match.js', array(), $ver, true );
		wp_localize_script( 'carmilla-therapist-match', 'CarmillaData', array(
			'restUrl' => esc_url_raw( rest_url( 'carmilla/v1/' ) ),
		) );
		wp_localize_script( 'carmilla-therapist-match', 'CarmillaMatch', array(
			'concerns' => carmilla_match_concerns(),
		) );
	}

	// Clinic file + appointments tabs on the account page.
	if ( function_exists( 'is_account_page' ) && is_account_page() ) {
		wp_enqueue_script( 'carmilla-clinic', $dir . '/assets/js/clinic.js', array(), $ver, true );
		wp_localize_script( 'carmilla-clinic', 'CarmillaData', $carmilla_common );
		wp_enqueue_script( 'carmilla-appointments', $dir . '/assets/js/appointments.js', array(), $ver, true );
		wp_localize_script( 'carmilla-appointments', 'CarmillaData', $carmilla_common );
	}

	// Appointment booking (theme REST + JS).
	if ( is_singular( 'cb_therapist' ) ) {
		wp_enqueue_script( 'carmilla-booking', $dir . '/assets/js/booking.js', array(), $ver, true );
		wp_localize_script( 'carmilla-booking', 'CarmillaData', array(
			'restUrl'  => esc_url_raw( rest_url( 'carmilla/v1/' ) ),
			'nonce'    => wp_create_nonce( 'wp_rest' ),
			'loggedIn' => is_user_logged_in(),
			'loginUrl' => wp_login_url( get_permalink() ),
		) );
	}

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
