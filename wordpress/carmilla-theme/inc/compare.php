<?php
/**
 * Product comparison (← ComparisonScreen). Client keeps a list in localStorage;
 * a theme REST endpoint returns normalized product data; [carmilla_compare]
 * renders the full-width table. WooCommerce only.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/** Normalized product row for the compare table. */
function carmilla_compare_dto( $product ) {
	$attrs = array();
	$attrs[ __( 'برند', 'carmilla' ) ] = (string) get_post_meta( $product->get_id(), 'cb_brand', true );
	foreach ( $product->get_attributes() as $attribute ) {
		$name   = wc_attribute_label( $attribute->get_name() );
		$values = $attribute->is_taxonomy()
			? wc_get_product_terms( $product->get_id(), $attribute->get_name(), array( 'fields' => 'names' ) )
			: $attribute->get_options();
		$values = array_filter( array_map( 'trim', (array) $values ) );
		if ( $values ) {
			$attrs[ $name ] = implode( '، ', $values );
		}
	}
	$cats = wc_get_product_category_list( $product->get_id() );
	return array(
		'id'         => $product->get_id(),
		'name'       => $product->get_name(),
		'permalink'  => get_permalink( $product->get_id() ),
		'image'      => wp_get_attachment_image_url( $product->get_image_id(), 'medium' ) ?: wc_placeholder_img_src( 'medium' ),
		'priceHtml'  => $product->get_price_html(),
		'rating'     => (float) $product->get_average_rating(),
		'inStock'    => $product->is_in_stock(),
		'category'   => $cats ? wp_strip_all_tags( $cats ) : '',
		'attributes' => array_filter( $attrs, 'strlen' ),
	);
}

add_action( 'rest_api_init', function () {
	register_rest_route( 'carmilla/v1', '/compare', array(
		'methods'             => 'GET',
		'permission_callback' => '__return_true',
		'callback'            => function ( $req ) {
			$ids = array_filter( array_map( 'absint', explode( ',', (string) $req->get_param( 'ids' ) ) ) );
			$ids = array_slice( array_unique( $ids ), 0, 4 ); // compare up to 4
			$out = array();
			foreach ( $ids as $id ) {
				$p = wc_get_product( $id );
				if ( $p && $p->is_visible() ) {
					$out[] = carmilla_compare_dto( $p );
				}
			}
			return rest_ensure_response( $out );
		},
	) );
} );

/** [carmilla_compare] — container the JS fills with the comparison table. */
add_shortcode( 'carmilla_compare', function () {
	return '<div id="cb-compare" class="cb-compare" data-empty="' .
		esc_attr__( 'هنوز محصولی برای مقایسه انتخاب نکرده‌اید. از کارت محصول، «مقایسه» را بزنید.', 'carmilla' ) .
		'"></div>';
} );

/** Floating "compare bar" + a compare toggle on the single product page. */
add_action( 'woocommerce_after_add_to_cart_button', function () {
	global $product;
	if ( $product ) {
		echo '<button type="button" class="btn btn--ghost cb-compare-toggle" data-id="' . esc_attr( $product->get_id() ) . '" aria-pressed="false">' .
			carmilla_icon( 'grid', 16 ) . '<span>' . esc_html__( 'افزودن به مقایسه', 'carmilla' ) . '</span></button>'; // phpcs:ignore
	}
} );

/** A compare page URL for the floating bar (a page containing [carmilla_compare]). */
function carmilla_compare_page_url() {
	$page = get_page_by_path( 'compare' );
	return $page ? get_permalink( $page ) : home_url( '/compare/' );
}

add_action( 'wp_footer', function () {
	if ( ! class_exists( 'WooCommerce' ) || is_admin() ) {
		return;
	}
	echo '<div id="cb-compare-bar" class="cb-compare-bar" hidden>' .
		'<span id="cb-compare-count" class="cb-compare-bar__count"></span>' .
		'<a class="btn btn--primary" href="' . esc_url( carmilla_compare_page_url() ) . '">' . esc_html__( 'مشاهده‌ی مقایسه', 'carmilla' ) . '</a>' .
		'<button type="button" id="cb-compare-clear" class="btn btn--ghost">' . esc_html__( 'پاک کردن', 'carmilla' ) . '</button>' .
		'</div>';
} );
