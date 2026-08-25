<?php
/**
 * Shopping assistant (← ShoppingAssistantScreen): a short guided question flow
 * (category → budget → sort) that returns matching products. WooCommerce only.
 * Theme REST returns the results; [carmilla_assistant] + assistant.js drive the UI.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

add_action( 'rest_api_init', function () {
	register_rest_route( 'carmilla/v1', '/assistant', array(
		'methods'             => 'GET',
		'permission_callback' => '__return_true',
		'callback'            => 'carmilla_rest_assistant',
	) );
} );

function carmilla_rest_assistant( WP_REST_Request $req ) {
	$cat  = absint( $req->get_param( 'cat' ) );
	$max  = (float) $req->get_param( 'max' );
	$sort = sanitize_key( (string) $req->get_param( 'sort' ) );

	$args = array(
		'post_type'      => 'product',
		'post_status'    => 'publish',
		'posts_per_page' => 12,
		'meta_query'     => array( array( 'key' => '_price', 'value' => 0, 'compare' => '>=', 'type' => 'NUMERIC' ) ),
	);
	if ( $cat ) {
		$args['tax_query'] = array( array( 'taxonomy' => 'product_cat', 'field' => 'term_id', 'terms' => $cat ) );
	}
	if ( $max > 0 ) {
		$args['meta_query'][] = array( 'key' => '_price', 'value' => $max, 'compare' => '<=', 'type' => 'NUMERIC' );
	}
	if ( 'price_asc' === $sort ) {
		$args['meta_key'] = '_price'; $args['orderby'] = 'meta_value_num'; $args['order'] = 'ASC';
	} elseif ( 'price_desc' === $sort ) {
		$args['meta_key'] = '_price'; $args['orderby'] = 'meta_value_num'; $args['order'] = 'DESC';
	} elseif ( 'rating' === $sort ) {
		$args['meta_key'] = '_wc_average_rating'; $args['orderby'] = 'meta_value_num'; $args['order'] = 'DESC';
	} else {
		$args['orderby'] = 'date';
	}

	$q   = new WP_Query( $args );
	$out = array();
	foreach ( $q->posts as $post ) {
		$p = wc_get_product( $post->ID );
		if ( ! $p || ! $p->is_visible() ) {
			continue;
		}
		$out[] = array(
			'id'        => $p->get_id(),
			'name'      => $p->get_name(),
			'permalink' => get_permalink( $p->get_id() ),
			'image'     => wp_get_attachment_image_url( $p->get_image_id(), 'medium' ) ?: wc_placeholder_img_src( 'medium' ),
			'priceHtml' => $p->get_price_html(),
			'rating'    => (float) $p->get_average_rating(),
		);
	}
	return rest_ensure_response( $out );
}

/** [carmilla_assistant] — renders the question flow (categories from the store). */
add_shortcode( 'carmilla_assistant', function () {
	$cats = get_terms( array( 'taxonomy' => 'product_cat', 'hide_empty' => true, 'number' => 12, 'parent' => 0 ) );
	$opts = array();
	if ( ! is_wp_error( $cats ) ) {
		foreach ( $cats as $c ) {
			$opts[] = array( 'id' => $c->term_id, 'name' => $c->name );
		}
	}
	ob_start();
	?>
	<div id="cb-assistant" class="cb-assistant" data-cats="<?php echo esc_attr( wp_json_encode( $opts ) ); ?>">
		<div id="cb-assistant-step" class="cb-assistant__step"></div>
		<div id="cb-assistant-results" class="grid-adaptive"></div>
	</div>
	<?php
	return ob_get_clean();
} );
