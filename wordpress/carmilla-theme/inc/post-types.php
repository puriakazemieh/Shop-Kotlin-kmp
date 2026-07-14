<?php
/**
 * Self-contained content types so the theme works standalone (no companion plugin
 * required). Each vertical CPT is only registered when its feature toggle is on,
 * and registration is guarded so an external plugin can still own it if present.
 *
 * Note: WP best practice is to register CPTs in a plugin so content survives a
 * theme switch. Per the project's "theme-only if possible" requirement these live
 * in the theme; the guards below let a Carmilla plugin take over transparently.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

add_action( 'init', 'carmilla_register_post_types' );

function carmilla_register_post_types() {

	// ---- Courses (academy) ----
	if ( carmilla_feature_enabled( 'courses' ) && ! post_type_exists( 'cb_course' ) ) {
		register_post_type( 'cb_course', carmilla_cpt_args( 'دوره', 'دوره‌ها', 'dashicons-welcome-learn', 'courses', array( 'title', 'editor', 'thumbnail', 'excerpt' ) ) );
		if ( ! taxonomy_exists( 'cb_course_cat' ) ) {
			register_taxonomy( 'cb_course_cat', 'cb_course', array(
				'labels'       => array( 'name' => 'دسته‌ی دوره', 'singular_name' => 'دسته‌ی دوره' ),
				'hierarchical' => true,
				'show_in_rest' => true,
				'rewrite'      => array( 'slug' => 'course-category' ),
			) );
		}
	}

	// ---- Therapists (clinic) ----
	if ( carmilla_feature_enabled( 'clinic' ) && ! post_type_exists( 'cb_therapist' ) ) {
		register_post_type( 'cb_therapist', carmilla_cpt_args( 'مشاور', 'مشاوران', 'dashicons-heart', 'therapists', array( 'title', 'editor', 'thumbnail', 'excerpt' ) ) );
	}

	// ---- Psychology tests ----
	if ( carmilla_feature_enabled( 'psychtests' ) && ! post_type_exists( 'cb_psychtest' ) ) {
		register_post_type( 'cb_psychtest', carmilla_cpt_args( 'تست روان‌شناسی', 'تست‌های روان‌شناسی', 'dashicons-clipboard', 'psych-tests', array( 'title', 'editor', 'thumbnail', 'excerpt' ) ) );
	}

	// ---- Course requests ----
	if ( carmilla_feature_enabled( 'courses' ) && ! post_type_exists( 'cb_course_request' ) ) {
		register_post_type( 'cb_course_request', carmilla_cpt_args( 'درخواست دوره', 'درخواست‌های دوره', 'dashicons-megaphone', 'course-requests', array( 'title', 'editor' ) ) );
	}

	// ---- Stories (embedded, no standalone page) ----
	if ( carmilla_feature_enabled( 'stories' ) && ! post_type_exists( 'cb_story' ) ) {
		$args               = carmilla_cpt_args( 'استوری', 'استوری‌ها', 'dashicons-format-image', 'stories', array( 'title', 'thumbnail' ) );
		$args['public']     = false;
		$args['show_ui']    = true;
		$args['has_archive'] = false;
		register_post_type( 'cb_story', $args );
	}

	// ---- Home banners (embedded) ----
	if ( ! post_type_exists( 'cb_banner' ) ) {
		$args               = carmilla_cpt_args( 'بنر', 'بنرها', 'dashicons-images-alt2', 'banners', array( 'title', 'thumbnail' ) );
		$args['public']     = false;
		$args['show_ui']    = true;
		$args['has_archive'] = false;
		register_post_type( 'cb_banner', $args );
	}
}

/** Shared CPT args (public, REST-enabled, front-end archive). */
function carmilla_cpt_args( $singular, $plural, $icon, $slug, $supports ) {
	return array(
		'labels'       => array(
			'name'          => $plural,
			'singular_name' => $singular,
			'add_new_item'  => sprintf( 'افزودن %s', $singular ),
			'edit_item'     => sprintf( 'ویرایش %s', $singular ),
			'search_items'  => sprintf( 'جستجوی %s', $plural ),
			'not_found'     => 'موردی یافت نشد.',
		),
		'public'       => true,
		'has_archive'  => true,
		'show_in_rest' => true,
		'menu_icon'    => $icon,
		'supports'     => $supports,
		'rewrite'      => array( 'slug' => $slug ),
	);
}

/** Flush rewrite rules once after CPTs are registered (on theme switch). */
add_action( 'after_switch_theme', function () {
	carmilla_register_post_types();
	flush_rewrite_rules();
} );
