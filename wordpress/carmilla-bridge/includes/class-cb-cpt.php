<?php
/**
 * Custom post types + meta the Carmilla app needs beyond WooCommerce/WP core:
 *   - story   (ephemeral Instagram-style stories, linkable to product/category/blog)
 *   - banner  (home merchandising banners)
 *   - campaign (timed product campaigns)
 * Plus registered meta for product brand/attributes and post SEO/featured/reading-time.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_CPT {

	public static function boot(): void {
		add_action( 'init', array( __CLASS__, 'register' ) );
		add_action( 'init', array( __CLASS__, 'register_meta' ) );
	}

	public static function register(): void {
		register_post_type( 'cb_story', self::args( 'Story', 'Stories', 'dashicons-format-image', array( 'title', 'thumbnail' ) ) );
		register_post_type( 'cb_banner', self::args( 'Banner', 'Banners', 'dashicons-images-alt2', array( 'title', 'thumbnail' ) ) );
		register_post_type( 'cb_campaign', self::args( 'Campaign', 'Campaigns', 'dashicons-megaphone', array( 'title', 'thumbnail' ) ) );
		if ( apply_filters( 'cb_enable_health_lms', false ) ) {
			// Academy course. Guarded so it coexists with the Carmilla theme's own
			// cb_course registration (whichever loads first wins; meta keys match).
			if ( ! post_type_exists( 'cb_course' ) ) {
				register_post_type( 'cb_course', self::args( 'Course', 'Courses', 'dashicons-welcome-learn-more', array( 'title', 'editor', 'thumbnail' ) ) );
			}
			// Clinic + psych test (guarded, theme-aligned meta).
			if ( ! post_type_exists( 'cb_therapist' ) ) {
				register_post_type( 'cb_therapist', self::args( 'Therapist', 'Therapists', 'dashicons-heart', array( 'title', 'editor', 'thumbnail' ) ) );
			}
			if ( ! post_type_exists( 'cb_psychtest' ) ) {
				register_post_type( 'cb_psychtest', self::args( 'Psych Test', 'Psych Tests', 'dashicons-forms', array( 'title', 'editor' ) ) );
			}
			if ( ! post_type_exists( 'cb_course_request' ) ) {
				register_post_type( 'cb_course_request', self::args( 'Course Request', 'Course Requests', 'dashicons-megaphone', array( 'title', 'editor' ) ) );
			}
			if ( ! post_type_exists( 'cb_appointment' ) ) {
				register_post_type( 'cb_appointment', array(
					'labels'      => array( 'name' => 'Appointments', 'singular_name' => 'Appointment' ),
					'public'      => false,
					'show_ui'     => true,
					'show_in_menu' => true,
					'show_in_rest' => false,
					'menu_icon'   => 'dashicons-calendar-alt',
					'supports'    => array( 'title' ),
					'has_archive' => false,
				) );
			}
		}
	}

	private static function args( string $singular, string $plural, string $icon, array $supports ): array {
		return array(
			'labels'       => array(
				'name'          => $plural,
				'singular_name' => $singular,
			),
			'public'       => false,
			'show_ui'      => true,
			'show_in_menu' => true,
			'show_in_rest' => true,
			'menu_icon'    => $icon,
			'supports'     => $supports,
			'has_archive'  => false,
		);
	}

	public static function register_meta(): void {
		// Story meta.
		self::meta( 'cb_story', 'media_url', 'string' );
		self::meta( 'cb_story', 'media_type', 'string' );      // IMAGE | VIDEO
		self::meta( 'cb_story', 'link_type', 'string' );       // PRODUCT | CATEGORY | BLOG | NONE
		self::meta( 'cb_story', 'product_id', 'integer' );
		self::meta( 'cb_story', 'category_id', 'integer' );
		self::meta( 'cb_story', 'blog_slug', 'string' );
		self::meta( 'cb_story', 'expires_at', 'string' );
		self::meta( 'cb_story', 'is_active', 'boolean' );

		// Banner meta.
		self::meta( 'cb_banner', 'subtitle', 'string' );
		self::meta( 'cb_banner', 'image_url', 'string' );
		self::meta( 'cb_banner', 'category_id', 'integer' );
		self::meta( 'cb_banner', 'sort_order', 'integer' );
		self::meta( 'cb_banner', 'is_active', 'boolean' );

		// Campaign meta.
		self::meta( 'cb_campaign', 'ends_at', 'string' );
		self::meta( 'cb_campaign', 'is_active', 'boolean' );
		self::meta( 'cb_campaign', 'product_ids', 'string' ); // CSV of product IDs

		// Product extras (WooCommerce products are the `product` post type).
		self::meta( 'product', 'cb_brand', 'string' );

		// Post extras.
		self::meta( 'post', 'cb_is_featured', 'boolean' );
		self::meta( 'post', 'cb_meta_title', 'string' );
		self::meta( 'post', 'cb_meta_description', 'string' );
		self::meta( 'post', 'cb_summary', 'string' );

		// Course meta (aligned with the Carmilla theme so a site running both
		// shares the same data). Lessons are line-based in cb_lessons.
		self::meta( 'cb_course', 'cb_instructor', 'string' );
		self::meta( 'cb_course', 'cb_level', 'string' );
		self::meta( 'cb_course', 'cb_format', 'string' );
		self::meta( 'cb_course', 'cb_duration', 'string' );
		self::meta( 'cb_course', 'cb_product_slug', 'string' );
		self::meta( 'cb_course', 'cb_price', 'string' );
		self::meta( 'cb_course', 'cb_discounted_price', 'string' );
		self::meta( 'cb_course', 'cb_capacity', 'integer' );
		self::meta( 'cb_course', 'cb_syllabus', 'string' );
		self::meta( 'cb_course', 'cb_lessons', 'string' );
		self::meta( 'cb_course', 'cb_quiz', 'string' );
		self::meta( 'cb_course', 'cb_pass_score', 'integer' );
		self::meta( 'cb_course', 'cb_instructor_bio', 'string' );
		self::meta( 'cb_course', 'cb_requires_project', 'boolean' );

		// Therapist meta (aligned with the theme).
		self::meta( 'cb_therapist', 'cb_specialty', 'string' );
		self::meta( 'cb_therapist', 'cb_approach', 'string' );
		self::meta( 'cb_therapist', 'cb_duration', 'integer' );      // session minutes
		self::meta( 'cb_therapist', 'cb_session_price', 'string' );
		self::meta( 'cb_therapist', 'cb_product_slug', 'string' );
		self::meta( 'cb_therapist', 'cb_messaging_product_slug', 'string' );
		self::meta( 'cb_therapist', 'cb_slots', 'string' );          // line-based datetimes
		self::meta( 'cb_therapist', 'cb_mode', 'string' );           // ONLINE | IN_PERSON
		self::meta( 'cb_therapist', 'cb_location', 'string' );

		// Psych test meta (aligned with the theme).
		self::meta( 'cb_psychtest', 'cb_questions', 'string' );      // «text | opt=score , ...»
		self::meta( 'cb_psychtest', 'cb_ranges', 'string' );         // «min | max | interpretation»
		self::meta( 'cb_psychtest', 'cb_result_mode', 'string' );    // AUTO | COUNSELOR
		self::meta( 'cb_psychtest', 'cb_price', 'string' );
		self::meta( 'cb_psychtest', 'cb_discounted_price', 'string' );
		self::meta( 'cb_psychtest', 'cb_product_slug', 'string' );
	}

	private static function meta( string $post_type, string $key, string $type ): void {
		register_post_meta( $post_type, $key, array(
			'type'         => $type,
			'single'       => true,
			'show_in_rest' => true,
			'auth_callback' => function () {
				return current_user_can( 'edit_posts' );
			},
		) );
	}
}
