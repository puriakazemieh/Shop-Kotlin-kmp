<?php
/**
 * Elementor integration — makes the theme a first-class Elementor citizen:
 *
 *  1. A «کارمیلا» widget category with widgets wrapping the theme's own
 *     components (hero, product grid, deals strip, course/therapist/test grids,
 *     blog cards, stories, category tiles, brand button) — so pages built in
 *     Elementor look exactly like the theme.
 *  2. Brand palette + Vazirmatn synced into Elementor's Global Colors, and
 *     Elementor's default color/typography schemes disabled so widgets inherit
 *     the theme design tokens.
 *  3. Theme Locations (header/footer) registered for Elementor Pro's Theme
 *     Builder; header.php / footer.php fall back to the theme chrome when no
 *     Elementor template targets the location.
 *  4. Theme design tokens loaded inside the Elementor editor/preview.
 *
 * Everything is additive and self-guarded: without Elementor installed this
 * file is inert and the theme behaves exactly as before.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/** Elementor Pro Theme Builder locations (header/footer/single/archive). */
add_action( 'elementor/theme/register_locations', function ( $manager ) {
	$manager->register_all_core_location();
} );

/* Everything below needs Elementor core. */
add_action( 'elementor/init', function () {

	// ---- widget category -------------------------------------------------
	add_action( 'elementor/elements/categories_registered', function ( $manager ) {
		$manager->add_category( 'carmilla', array(
			'title' => __( 'کارمیلا', 'carmilla' ),
			'icon'  => 'fa fa-star',
		) );
	} );

	// ---- widgets ---------------------------------------------------------
	add_action( 'elementor/widgets/register', function ( $manager ) {
		$dir = get_template_directory() . '/inc/elementor/';
		require_once $dir . 'class-widget-base.php';
		require_once $dir . 'class-widget-hero.php';
		require_once $dir . 'class-widget-button.php';
		require_once $dir . 'class-widget-products.php';
		require_once $dir . 'class-widget-deals.php';
		require_once $dir . 'class-widget-media-grids.php';
		require_once $dir . 'class-widget-posts.php';
		require_once $dir . 'class-widget-stories.php';
		require_once $dir . 'class-widget-categories.php';

		$manager->register( new \Carmilla_El_Hero() );
		$manager->register( new \Carmilla_El_Button() );
		if ( class_exists( 'WooCommerce' ) ) {
			$manager->register( new \Carmilla_El_Products() );
			$manager->register( new \Carmilla_El_Deals() );
			$manager->register( new \Carmilla_El_Categories() );
		}
		$manager->register( new \Carmilla_El_Courses() );
		$manager->register( new \Carmilla_El_Therapists() );
		$manager->register( new \Carmilla_El_Psychtests() );
		$manager->register( new \Carmilla_El_Posts() );
		$manager->register( new \Carmilla_El_Stories() );
	} );

	// ---- theme tokens inside the editor & preview ------------------------
	$enqueue_tokens = function () {
		$dir = get_template_directory_uri();
		$ver = wp_get_theme()->get( 'Version' );
		wp_enqueue_style( 'carmilla-tokens', $dir . '/assets/css/tokens.css', array(), $ver );
		wp_enqueue_style( 'carmilla-dc', $dir . '/assets/css/dc.css', array( 'carmilla-tokens' ), $ver );
	};
	add_action( 'elementor/editor/after_enqueue_styles', $enqueue_tokens );
	add_action( 'elementor/preview/enqueue_styles', $enqueue_tokens );
} );

/**
 * Make Elementor inherit the theme's typography/colors instead of its own
 * defaults, and push the Carmilla palette into Global Colors — once per theme
 * version, and again whenever the customizer brand colors are saved.
 */
function carmilla_elementor_sync_defaults() {
	if ( ! class_exists( '\Elementor\Plugin' ) ) {
		return;
	}
	// Inherit theme fonts/colors (only set when the site owner hasn't chosen).
	add_option( 'elementor_disable_color_schemes', 'yes' );
	add_option( 'elementor_disable_typography_schemes', 'yes' );

	try {
		$kit = \Elementor\Plugin::$instance->kits_manager->get_active_kit();
		if ( ! $kit || ! $kit->get_id() ) {
			return;
		}
		$defaults = function_exists( 'carmilla_color_defaults' ) ? carmilla_color_defaults() : array();
		$get      = function ( $key, $fallback ) use ( $defaults ) {
			return get_theme_mod( "carmilla_color_$key", $defaults[ $key ] ?? $fallback );
		};
		$palette = array(
			array( '_id' => 'carmilla_accent', 'title' => 'کارمیلا — اصلی', 'color' => $get( 'accent', '#20305C' ) ),
			array( '_id' => 'carmilla_accent2', 'title' => 'کارمیلا — ثانویه', 'color' => $get( 'accent2', '#34487E' ) ),
			array( '_id' => 'carmilla_gold', 'title' => 'کارمیلا — طلایی', 'color' => $get( 'gold', '#B08D57' ) ),
			array( '_id' => 'carmilla_bg', 'title' => 'کارمیلا — پس‌زمینه', 'color' => $get( 'bg', '#F6F4EF' ) ),
			array( '_id' => 'carmilla_surface', 'title' => 'کارمیلا — سطح', 'color' => $get( 'surface', '#FFFFFF' ) ),
			array( '_id' => 'carmilla_ink', 'title' => 'کارمیلا — متن', 'color' => $get( 'ink', '#192038' ) ),
		);
		$existing = (array) $kit->get_settings( 'custom_colors' );
		$keep     = array_values( array_filter( $existing, function ( $c ) {
			return isset( $c['_id'] ) && strpos( (string) $c['_id'], 'carmilla_' ) !== 0;
		} ) );
		$kit->update_settings( array( 'custom_colors' => array_merge( $keep, $palette ) ) );
	} catch ( \Throwable $e ) { // phpcs:ignore
		// Never break the site over palette sugar.
	}
}

add_action( 'admin_init', function () {
	$flag = 'carmilla_elementor_synced_' . wp_get_theme()->get( 'Version' );
	if ( get_option( $flag ) ) {
		return;
	}
	carmilla_elementor_sync_defaults();
	update_option( $flag, 1, false );
} );
add_action( 'customize_save_after', 'carmilla_elementor_sync_defaults' );
