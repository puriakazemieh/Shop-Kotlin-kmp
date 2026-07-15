<?php
/**
 * Catalog extras — the remaining catalog/academy list screens:
 *   - CategoriesScreen      → [carmilla_categories] product-category grid.
 *     (CategorySearch is the native WooCommerce category archive it links to.)
 *   - CoursesByLevel / InstructorCourses / FreeCourses → cb_course archive
 *     filters via query args (?level= / ?instructor= / ?free=1) + filter chips.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/* =========================================================================
 * Categories index (← CategoriesScreen)
 * ====================================================================== */

add_shortcode( 'carmilla_categories', function () {
	if ( ! taxonomy_exists( 'product_cat' ) ) {
		return '';
	}
	$terms = get_terms( array( 'taxonomy' => 'product_cat', 'hide_empty' => false ) );
	if ( is_wp_error( $terms ) || ! $terms ) {
		return '<p class="t-body t-muted">' . esc_html__( 'هنوز دسته‌بندی‌ای ثبت نشده.', 'carmilla' ) . '</p>';
	}
	ob_start();
	echo '<div class="cb-cats grid-adaptive container container--wide">';
	foreach ( $terms as $term ) {
		$thumb_id = get_term_meta( $term->term_id, 'thumbnail_id', true );
		$img      = $thumb_id ? wp_get_attachment_image_url( $thumb_id, 'carmilla-card' ) : '';
		echo '<a class="card cb-cat" href="' . esc_url( get_term_link( $term ) ) . '">';
		if ( $img ) {
			echo '<span class="thumb"><img src="' . esc_url( $img ) . '" alt="' . esc_attr( $term->name ) . '" loading="lazy"></span>';
		} else {
			echo '<span class="thumb cb-cat__ph">' . carmilla_icon( 'grid', 28 ) . '</span>';
		}
		echo '<span class="card--pad cb-cat__body"><span class="t-title-sm">' . esc_html( $term->name ) . '</span>';
		echo '<span class="t-body-sm t-muted">' . esc_html( carmilla_to_persian_digits( number_format_i18n( $term->count ) ) ) . ' ' . esc_html__( 'محصول', 'carmilla' ) . '</span></span>';
		echo '</a>';
	}
	echo '</div>';
	return ob_get_clean();
} );

/* =========================================================================
 * Course archive filters (← CoursesByLevel / InstructorCourses / FreeCourses)
 * ====================================================================== */

/** Level slug (used by the placement quiz link) → the Persian label admins type. */
function carmilla_level_label( $slug ) {
	$map = array(
		'beginner'     => 'مبتدی',
		'intermediate' => 'متوسط',
		'advanced'     => 'پیشرفته',
	);
	return $map[ $slug ] ?? $slug;
}

add_action( 'pre_get_posts', function ( $q ) {
	if ( is_admin() || ! $q->is_main_query() || ! $q->is_post_type_archive( 'cb_course' ) ) {
		return;
	}
	$meta = array();

	if ( ! empty( $_GET['level'] ) ) {
		$label  = carmilla_level_label( sanitize_text_field( wp_unslash( $_GET['level'] ) ) );
		$meta[] = array( 'key' => 'cb_level', 'value' => $label, 'compare' => 'LIKE' );
	}
	if ( ! empty( $_GET['instructor'] ) ) {
		$meta[] = array( 'key' => 'cb_instructor', 'value' => sanitize_text_field( wp_unslash( $_GET['instructor'] ) ), 'compare' => 'LIKE' );
	}
	if ( ! empty( $_GET['free'] ) ) {
		// Free = no linked WooCommerce product slug.
		$meta[] = array(
			'relation' => 'OR',
			array( 'key' => 'cb_product_slug', 'compare' => 'NOT EXISTS' ),
			array( 'key' => 'cb_product_slug', 'value' => '', 'compare' => '=' ),
		);
	}
	if ( $meta ) {
		if ( count( $meta ) > 1 ) {
			$meta['relation'] = 'AND';
		}
		$q->set( 'meta_query', $meta );
	}
} );

/** Renders the active-filter heading + quick chips at the top of the course archive. */
function carmilla_course_filter_bar() {
	$level      = isset( $_GET['level'] ) ? sanitize_text_field( wp_unslash( $_GET['level'] ) ) : '';
	$instructor = isset( $_GET['instructor'] ) ? sanitize_text_field( wp_unslash( $_GET['instructor'] ) ) : '';
	$free       = ! empty( $_GET['free'] );
	$base       = get_post_type_archive_link( 'cb_course' );

	echo '<div class="cb-course-filters">';
	$all_cls = ( ! $level && ! $instructor && ! $free ) ? ' is-on' : '';
	echo '<a class="cb-chip' . esc_attr( $all_cls ) . '" href="' . esc_url( $base ) . '">' . esc_html__( 'همه', 'carmilla' ) . '</a>';
	echo '<a class="cb-chip' . ( $free ? ' is-on' : '' ) . '" href="' . esc_url( add_query_arg( 'free', '1', $base ) ) . '">' . esc_html__( 'رایگان', 'carmilla' ) . '</a>';
	foreach ( array( 'beginner', 'intermediate', 'advanced' ) as $lv ) {
		$on = ( $level === $lv ) ? ' is-on' : '';
		echo '<a class="cb-chip' . esc_attr( $on ) . '" href="' . esc_url( add_query_arg( 'level', $lv, $base ) ) . '">' . esc_html( carmilla_level_label( $lv ) ) . '</a>';
	}
	echo '</div>';

	if ( $instructor ) {
		echo '<p class="t-body-sm t-muted cb-course-filters__note">' . sprintf( esc_html__( 'دوره‌های مدرس: %s', 'carmilla' ), esc_html( $instructor ) ) . '</p>';
	}
}
