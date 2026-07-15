<?php
/**
 * Theme options via the WordPress Customizer:
 *   - Branding: logo (native), brand colors
 *   - Header/Home: header title, hero title + subtitle
 *   - Features: enable/disable shop, blog, courses, clinic, psych-tests, stories
 *
 * Colors are emitted as CSS custom properties that override tokens.css, so the
 * whole design (app-derived) re-skins live — full white-label from the dashboard.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/** Default brand colors (mirror tokens.css light theme). */
function carmilla_color_defaults() {
	return array(
		'accent'  => '#20305C',
		'accent2' => '#34487E',
		'gold'    => '#B08D57',
		'bg'      => '#F6F5F1',
		'surface' => '#FFFFFF',
		'ink'     => '#192038',
	);
}

/** The feature toggles (slug => label), all default-on. */
function carmilla_feature_toggles() {
	return array(
		'shop'       => __( 'فروشگاه (WooCommerce)', 'carmilla' ),
		'blog'       => __( 'مجله / بلاگ', 'carmilla' ),
		'courses'    => __( 'دوره‌ها (آکادمی)', 'carmilla' ),
		'clinic'     => __( 'مشاوره / نوبت‌دهی', 'carmilla' ),
		'psychtests' => __( 'تست‌های روان‌شناسی', 'carmilla' ),
		'stories'    => __( 'استوری‌ها', 'carmilla' ),
	);
}

/** Whether a feature is enabled (default true). */
function carmilla_feature_enabled( $slug ) {
	return (bool) get_theme_mod( "carmilla_enable_$slug", true );
}

add_action( 'customize_register', function ( $wp_customize ) {
	$wp_customize->add_panel( 'carmilla_panel', array(
		'title'    => __( 'تنظیمات کارمیلا', 'carmilla' ),
		'priority' => 10,
	) );

	// ---- Branding: colors ----
	$wp_customize->add_section( 'carmilla_colors', array(
		'title' => __( 'رنگ‌های برند', 'carmilla' ),
		'panel' => 'carmilla_panel',
	) );
	$labels = array(
		'accent'  => __( 'رنگ اصلی (Accent)', 'carmilla' ),
		'accent2' => __( 'رنگ ثانویه', 'carmilla' ),
		'gold'    => __( 'طلایی', 'carmilla' ),
		'bg'      => __( 'پس‌زمینه', 'carmilla' ),
		'surface' => __( 'سطح کارت', 'carmilla' ),
		'ink'     => __( 'متن اصلی', 'carmilla' ),
	);
	foreach ( carmilla_color_defaults() as $key => $default ) {
		$wp_customize->add_setting( "carmilla_color_$key", array(
			'default'           => $default,
			'sanitize_callback' => 'sanitize_hex_color',
			'transport'         => 'refresh',
		) );
		$wp_customize->add_control( new WP_Customize_Color_Control( $wp_customize, "carmilla_color_$key", array(
			'label'   => $labels[ $key ],
			'section' => 'carmilla_colors',
		) ) );
	}

	// ---- Header / Home ----
	$wp_customize->add_section( 'carmilla_header', array(
		'title' => __( 'هدر و صفحه‌ی خانه', 'carmilla' ),
		'panel' => 'carmilla_panel',
	) );
	$texts = array(
		'carmilla_header_title' => array( __( 'عنوان هدر (خالی = نام سایت)', 'carmilla' ), '' ),
		'carmilla_hero_title'   => array( __( 'عنوان هیرو', 'carmilla' ), 'کالکشن پاییز و زمستان' ),
		'carmilla_hero_sub'     => array( __( 'زیرعنوان هیرو', 'carmilla' ), 'جدیدترین محصولات، دوره‌ها و خدمات مشاوره — همه در یک‌جا.' ),
	);
	foreach ( $texts as $id => $conf ) {
		$wp_customize->add_setting( $id, array(
			'default'           => $conf[1],
			'sanitize_callback' => 'sanitize_text_field',
			'transport'         => 'refresh',
		) );
		$wp_customize->add_control( $id, array(
			'label'   => $conf[0],
			'section' => 'carmilla_header',
			'type'    => 'text',
		) );
	}

	// ---- Clinic / emergency ----
	$wp_customize->add_section( 'carmilla_clinic', array(
		'title' => __( 'مشاوره و اورژانس', 'carmilla' ),
		'panel' => 'carmilla_panel',
	) );
	$clinic_texts = array(
		'carmilla_emergency_phone' => array( __( 'شماره‌ی تماس اورژانس روانی', 'carmilla' ), '۱۴۸۰' ),
		'carmilla_emergency_note'  => array( __( 'یادداشت اورژانس', 'carmilla' ), 'اگر در وضعیت بحرانی هستید یا افکار آسیب به خود دارید، همین حالا تماس بگیرید.' ),
	);
	foreach ( $clinic_texts as $id => $conf ) {
		$wp_customize->add_setting( $id, array(
			'default'           => $conf[1],
			'sanitize_callback' => 'sanitize_text_field',
			'transport'         => 'refresh',
		) );
		$wp_customize->add_control( $id, array(
			'label'   => $conf[0],
			'section' => 'carmilla_clinic',
			'type'    => 'text',
		) );
	}

	// ---- Features ----
	$wp_customize->add_section( 'carmilla_features', array(
		'title'       => __( 'فعال/غیرفعال‌کردن بخش‌ها', 'carmilla' ),
		'description' => __( 'هر بخش را می‌توانید روشن یا خاموش کنید؛ خاموش‌کردن، ناوبری و محتوای آن را پنهان می‌کند.', 'carmilla' ),
		'panel'       => 'carmilla_panel',
	) );
	foreach ( carmilla_feature_toggles() as $slug => $label ) {
		$wp_customize->add_setting( "carmilla_enable_$slug", array(
			'default'           => true,
			'sanitize_callback' => 'carmilla_sanitize_bool',
			'transport'         => 'refresh',
		) );
		$wp_customize->add_control( "carmilla_enable_$slug", array(
			'label'   => $label,
			'section' => 'carmilla_features',
			'type'    => 'checkbox',
		) );
	}
} );

/** Boolean sanitizer for checkboxes. */
function carmilla_sanitize_bool( $checked ) {
	return ( isset( $checked ) && true == $checked );
}

/**
 * Emit the Customizer color choices as CSS variables overriding tokens.css.
 */
function carmilla_customizer_css() {
	$d    = carmilla_color_defaults();
	$vars = array(
		'--accent'   => get_theme_mod( 'carmilla_color_accent', $d['accent'] ),
		'--accent-2' => get_theme_mod( 'carmilla_color_accent2', $d['accent2'] ),
		'--gold'     => get_theme_mod( 'carmilla_color_gold', $d['gold'] ),
		'--bg'       => get_theme_mod( 'carmilla_color_bg', $d['bg'] ),
		'--surface'  => get_theme_mod( 'carmilla_color_surface', $d['surface'] ),
		'--ink'      => get_theme_mod( 'carmilla_color_ink', $d['ink'] ),
	);
	// Only emit when at least one differs from default (keeps dark-mode overrides intact otherwise).
	$css = '';
	foreach ( $vars as $name => $val ) {
		if ( $val ) {
			$css .= "$name:$val;";
		}
	}
	if ( $css ) {
		echo "\n<style id=\"carmilla-customizer\">:root{" . esc_html( $css ) . "}</style>\n";
	}
}
add_action( 'wp_head', 'carmilla_customizer_css', 20 );
