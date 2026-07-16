<?php
/**
 * Admin bundles — mapped to WooCommerce grouped products. Admin/shop-manager.
 *
 *   GET/POST api/admin/bundles ; PATCH/DELETE api/admin/bundles/{id}
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Admin_Bundle_Controller {

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$admin = array( 'CB_Plugin', 'require_admin' );

		register_rest_route( $ns, '/api/admin/bundles', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_bundles' ), 'permission_callback' => $admin ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'create_bundle' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/bundles/(?P<id>\d+)', array(
			array( 'methods' => 'PATCH', 'callback' => array( $this, 'update_bundle' ), 'permission_callback' => $admin ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_bundle' ), 'permission_callback' => $admin ),
		) );
	}

	private function dto( WC_Product $g ): array {
		return array(
			'id'               => (int) $g->get_id(),
			'title'            => $g->get_name(),
			'slug'             => $g->get_slug(),
			'description'      => $g->get_short_description() ?: null,
			'productId'        => (int) $g->get_id(),
			'memberProductIds' => array_map( 'intval', $g->get_children() ),
			'isActive'         => $g->get_status() === 'publish',
		);
	}

	public function list_bundles(): WP_REST_Response {
		if ( ! cb_woo_active() ) {
			return cb_response( array() );
		}
		$out = array();
		foreach ( wc_get_products( array( 'type' => 'grouped', 'status' => array( 'publish', 'draft' ), 'limit' => 100 ) ) as $g ) {
			$out[] = $this->dto( $g );
		}
		return cb_response( $out );
	}

	public function create_bundle( WP_REST_Request $request ): WP_REST_Response {
		if ( ! cb_woo_active() ) {
			return cb_error( 'WooCommerce غیرفعال است', 400, 'WOO_INACTIVE', 'api/admin/bundles' );
		}
		$b = $request->get_json_params();
		$g = new WC_Product_Grouped();
		$g->set_name( sanitize_text_field( (string) ( $b['title'] ?? 'مجموعه' ) ) );
		$g->set_slug( sanitize_title( (string) ( $b['slug'] ?? '' ) ) );
		$g->set_short_description( wp_kses_post( (string) ( $b['description'] ?? '' ) ) );
		$g->set_children( array_map( 'intval', (array) ( $b['memberProductIds'] ?? array() ) ) );
		$g->set_status( ( isset( $b['isActive'] ) && ! $b['isActive'] ) ? 'draft' : 'publish' );
		$g->save();
		return cb_response( array( 'id' => (int) $g->get_id() ), 201 );
	}

	public function update_bundle( WP_REST_Request $request ): WP_REST_Response {
		$g = cb_woo_active() ? wc_get_product( (int) $request['id'] ) : null;
		if ( ! $g || ! $g->is_type( 'grouped' ) ) {
			return cb_error( 'مجموعه یافت نشد', 404, 'NOT_FOUND', 'api/admin/bundles' );
		}
		$b = $request->get_json_params();
		if ( isset( $b['title'] ) ) {
			$g->set_name( sanitize_text_field( (string) $b['title'] ) );
		}
		if ( isset( $b['description'] ) ) {
			$g->set_short_description( wp_kses_post( (string) $b['description'] ) );
		}
		if ( isset( $b['memberProductIds'] ) && is_array( $b['memberProductIds'] ) ) {
			$g->set_children( array_map( 'intval', $b['memberProductIds'] ) );
		}
		if ( isset( $b['isActive'] ) ) {
			$g->set_status( $b['isActive'] ? 'publish' : 'draft' );
		}
		$g->save();
		return cb_response( $this->dto( $g ) );
	}

	public function delete_bundle( WP_REST_Request $request ): WP_REST_Response {
		$g = cb_woo_active() ? wc_get_product( (int) $request['id'] ) : null;
		if ( $g && $g->is_type( 'grouped' ) ) {
			$g->delete( true );
		}
		return cb_response( null, 204 );
	}
}
