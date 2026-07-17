<?php
/**
 * Product bundles — mapped from WooCommerce grouped products. The grouped
 * product itself is the bundle "product"; its children are the members.
 *
 *   GET api/bundles          -> List<BundleSummaryResponse>
 *   GET api/bundles/{slug}   -> BundleDetailResponse
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Bundle_Controller {

	public function register_routes(): void {
		$ns = CB_REST_NAMESPACE;
		register_rest_route( $ns, '/api/bundles', array( 'methods' => 'GET', 'callback' => array( $this, 'list_bundles' ), 'permission_callback' => '__return_true' ) );
		register_rest_route( $ns, '/api/bundles/(?P<slug>[a-zA-Z0-9\-_%]+)', array( 'methods' => 'GET', 'callback' => array( $this, 'get_bundle' ), 'permission_callback' => '__return_true' ) );
	}

	public function list_bundles(): WP_REST_Response {
		if ( ! cb_woo_active() ) {
			return cb_response( array() );
		}
		$uid      = get_current_user_id();
		$products = wc_get_products( array( 'type' => 'grouped', 'status' => 'publish', 'limit' => 100 ) );
		$out      = array();
		foreach ( $products as $g ) {
			$out[] = array(
				'id'          => (int) $g->get_id(),
				'title'       => $g->get_name(),
				'slug'        => $g->get_slug(),
				'description' => $g->get_short_description() ?: null,
				'product'     => cb_product_summary_dto( $g, $uid ),
				'memberCount' => count( $g->get_children() ),
			);
		}
		return cb_response( $out );
	}

	public function get_bundle( WP_REST_Request $request ): WP_REST_Response {
		if ( ! cb_woo_active() ) {
			return cb_error( 'WooCommerce غیرفعال است', 400, 'WOO_INACTIVE', 'api/bundles' );
		}
		$g = get_page_by_path( sanitize_title( $request['slug'] ), OBJECT, 'product' );
		$product = $g ? wc_get_product( $g->ID ) : null;
		if ( ! $product || ! $product->is_type( 'grouped' ) ) {
			return cb_error( 'مجموعه یافت نشد', 404, 'NOT_FOUND', 'api/bundles' );
		}
		$uid     = get_current_user_id();
		$members = array();
		foreach ( $product->get_children() as $cid ) {
			$child = wc_get_product( $cid );
			if ( $child ) {
				$members[] = cb_product_summary_dto( $child, $uid );
			}
		}
		return cb_response( array(
			'id'          => (int) $product->get_id(),
			'title'       => $product->get_name(),
			'slug'        => $product->get_slug(),
			'description' => $product->get_short_description() ?: null,
			'product'     => cb_product_summary_dto( $product, $uid ),
			'members'     => $members,
		) );
	}
}
