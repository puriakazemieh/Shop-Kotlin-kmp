<?php
/**
 * Wishlist / Favorites (← FavoritesScreen). Theme-only heart toggle on product
 * cards + single product, a my-account «favorites» tab, and a REST store.
 * Logged-in users persist to user meta (cb_wishlist); guests fall back to
 * localStorage in the browser (handled in wishlist.js).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

function carmilla_wishlist_ids( $user_id = 0 ) {
	$user_id = $user_id ?: get_current_user_id();
	if ( ! $user_id ) {
		return array();
	}
	$ids = get_user_meta( $user_id, 'cb_wishlist', true );
	return is_array( $ids ) ? array_values( array_unique( array_map( 'intval', $ids ) ) ) : array();
}

add_action( 'rest_api_init', function () {
	register_rest_route( 'carmilla/v1', '/wishlist', array(
		array( 'methods' => 'GET', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_wishlist_get' ),
		array( 'methods' => 'POST', 'permission_callback' => 'is_user_logged_in', 'callback' => 'carmilla_rest_wishlist_toggle' ),
	) );
} );

function carmilla_rest_wishlist_get() {
	return rest_ensure_response( array( 'ids' => carmilla_wishlist_ids() ) );
}

function carmilla_rest_wishlist_toggle( WP_REST_Request $req ) {
	$pid = (int) $req->get_param( 'productId' );
	if ( ! $pid ) {
		return new WP_Error( 'validation', 'شناسه‌ی محصول نامعتبر است.', array( 'status' => 400 ) );
	}
	$uid = get_current_user_id();
	$ids = carmilla_wishlist_ids( $uid );
	$pos = array_search( $pid, $ids, true );
	if ( false === $pos ) {
		$ids[] = $pid;
		$active = true;
	} else {
		array_splice( $ids, $pos, 1 );
		$active = false;
	}
	update_user_meta( $uid, 'cb_wishlist', $ids );
	return rest_ensure_response( array( 'ids' => array_values( $ids ), 'active' => $active ) );
}

/** Heart button markup reused on cards and the single product page. */
function carmilla_wishlist_button( $product_id, $class = '' ) {
	$active = in_array( (int) $product_id, carmilla_wishlist_ids(), true );
	printf(
		'<button type="button" class="cb-wish-toggle %s%s" data-id="%d" aria-pressed="%s" aria-label="%s">%s</button>',
		esc_attr( $class ),
		$active ? ' is-on' : '',
		(int) $product_id,
		$active ? 'true' : 'false',
		esc_attr__( 'افزودن به علاقه‌مندی‌ها', 'carmilla' ),
		carmilla_icon( 'heart', 18 )
	);
}

/** Heart overlaid on the single-product gallery. */
add_action( 'woocommerce_before_single_product_summary', function () {
	global $product;
	if ( $product instanceof WC_Product ) {
		echo '<div class="cb-wish-single">';
		carmilla_wishlist_button( $product->get_id(), 'cb-wish-toggle--float' );
		echo '</div>';
	}
}, 5 );

/* -------- My-account «favorites» tab -------- */

add_action( 'init', function () {
	add_rewrite_endpoint( 'favorites', EP_ROOT | EP_PAGES );
} );

add_filter( 'woocommerce_account_menu_items', function ( $items ) {
	$logout = isset( $items['customer-logout'] ) ? array( 'customer-logout' => $items['customer-logout'] ) : array();
	unset( $items['customer-logout'] );
	$items['favorites'] = __( 'علاقه‌مندی‌ها', 'carmilla' );
	return array_merge( $items, $logout );
} );

add_action( 'woocommerce_account_favorites_endpoint', function () {
	$ids = carmilla_wishlist_ids();
	if ( ! $ids ) {
		echo '<div class="cb-fav-empty"><span class="cb-fav-empty__emoji">🤍</span>';
		echo '<p class="t-body">' . esc_html__( 'هنوز محصولی به علاقه‌مندی‌ها اضافه نکرده‌اید.', 'carmilla' ) . '</p></div>';
		return;
	}
	echo '<div class="product-grid grid-adaptive">';
	$q = new WP_Query( array(
		'post_type'      => 'product',
		'post__in'       => $ids,
		'orderby'        => 'post__in',
		'posts_per_page' => -1,
		'post_status'    => 'publish',
	) );
	while ( $q->have_posts() ) {
		$q->the_post();
		wc_get_template_part( 'content', 'product' );
	}
	wp_reset_postdata();
	echo '</div>';
} );
