<?php
/**
 * Product bundles (← Bundle List/Detail) built on WooCommerce grouped products.
 * A grouped product = a bundle; children = bundled items. We add a bundle total
 * summary on the single page and a [carmilla_bundles] grid for the list.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/** Sum of the min prices of a grouped product's children. */
function carmilla_bundle_total( $grouped ) {
	$total = 0.0;
	foreach ( $grouped->get_children() as $child_id ) {
		$child = wc_get_product( $child_id );
		if ( $child ) {
			$total += (float) wc_get_price_to_display( $child );
		}
	}
	return $total;
}

/** Bundle summary card under the grouped product list (total + item count). */
add_action( 'woocommerce_after_single_product_summary', function () {
	global $product;
	if ( ! $product || ! $product->is_type( 'grouped' ) ) {
		return;
	}
	$count = count( $product->get_children() );
	$total = carmilla_bundle_total( $product );
	echo '<section class="cb-psection cb-bundle-sum">';
	echo '<div class="card card--pad">';
	echo '<div class="cb-bundle-sum__row"><span class="t-body">' . sprintf( esc_html__( 'این مجموعه شامل %s کالا است', 'carmilla' ), esc_html( carmilla_to_persian_digits( $count ) ) ) . '</span>';
	echo '<span class="t-title-sm">' . esc_html__( 'مجموع:', 'carmilla' ) . ' ' . wp_kses_post( carmilla_price( $total ) ) . '</span></div>';
	echo '</div></section>';
}, 11 );

/** [carmilla_bundles] — grid of grouped products (the Bundle List). */
add_shortcode( 'carmilla_bundles', function ( $atts ) {
	$atts = shortcode_atts( array( 'count' => 12 ), $atts );
	$q = new WP_Query( array(
		'post_type'      => 'product',
		'posts_per_page' => (int) $atts['count'],
		'post_status'    => 'publish',
		'tax_query'      => array(
			array( 'taxonomy' => 'product_type', 'field' => 'slug', 'terms' => 'grouped' ),
		),
	) );
	if ( ! $q->have_posts() ) {
		return '<p class="t-body t-muted">' . esc_html__( 'فعلاً مجموعه‌ای موجود نیست.', 'carmilla' ) . '</p>';
	}
	ob_start();
	echo '<div class="grid-adaptive">';
	while ( $q->have_posts() ) {
		$q->the_post();
		$p     = wc_get_product( get_the_ID() );
		$total = $p ? carmilla_bundle_total( $p ) : 0;
		$count = $p ? count( $p->get_children() ) : 0;
		echo '<article class="card cb-bundle-card">';
		echo '<a href="' . esc_url( get_permalink() ) . '" class="thumb">' . get_the_post_thumbnail( get_the_ID(), 'carmilla-card' ) . '</a>';
		echo '<div class="body">';
		echo '<span class="badge badge--new">' . sprintf( esc_html__( 'مجموعه‌ی %s کالایی', 'carmilla' ), esc_html( carmilla_to_persian_digits( $count ) ) ) . '</span>';
		echo '<h3 class="t-title-sm"><a href="' . esc_url( get_permalink() ) . '">' . esc_html( get_the_title() ) . '</a></h3>';
		echo '<div class="price-row" style="margin-block-start:6px"><span class="price">' . wp_kses_post( carmilla_price( $total ) ) . '</span></div>';
		echo '</div></article>';
	}
	echo '</div>';
	wp_reset_postdata();
	return ob_get_clean();
} );
