<?php
/**
 * Admin product deep-management — images, videos, variations, inventory and
 * global attributes (options), backed by WooCommerce. Admin/shop-manager only.
 *
 *   POST   api/admin/products/{id}/images            (multipart file | {url})
 *   PATCH  api/admin/products/{id}/images/reorder    ({items:[{id,sortOrder}]})
 *   DELETE api/admin/products/{id}/images/{imageId}
 *   POST   api/admin/products/{id}/videos            (multipart file | {url})
 *   DELETE api/admin/products/{id}/videos/{videoId}
 *   POST   api/admin/products/{id}/variants          (AdminCreateVariantRequest)
 *   PATCH/DELETE api/admin/variants/{id}
 *   GET    api/admin/variants/{id}/inventory
 *   PUT    api/admin/variants/{id}/inventory         ({onHand})
 *   PATCH  api/admin/variants/{id}/inventory/adjust  ({delta})
 *   GET    api/admin/options ; POST api/admin/options/types ; PUT/DELETE .../types/{id}
 *   POST   api/admin/options/values ; PUT/DELETE api/admin/options/values/{id}
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Admin_Product_Controller {

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$admin = array( 'CB_Plugin', 'require_admin' );

		register_rest_route( $ns, '/api/admin/products/(?P<id>\d+)/images', array( 'methods' => 'POST', 'callback' => array( $this, 'add_image' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/products/(?P<id>\d+)/images/reorder', array( 'methods' => 'PATCH', 'callback' => array( $this, 'reorder_images' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/products/(?P<id>\d+)/images/(?P<img>\d+)', array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_image' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/products/(?P<id>\d+)/videos', array( 'methods' => 'POST', 'callback' => array( $this, 'add_video' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/products/(?P<id>\d+)/videos/(?P<vid>\d+)', array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_video' ), 'permission_callback' => $admin ) );

		register_rest_route( $ns, '/api/admin/products/(?P<id>\d+)/variants', array( 'methods' => 'POST', 'callback' => array( $this, 'create_variant' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/variants/(?P<id>\d+)', array(
			array( 'methods' => 'PATCH', 'callback' => array( $this, 'update_variant' ), 'permission_callback' => $admin ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_variant' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/variants/(?P<id>\d+)/inventory', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'get_inventory' ), 'permission_callback' => $admin ),
			array( 'methods' => 'PUT', 'callback' => array( $this, 'set_inventory' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/variants/(?P<id>\d+)/inventory/adjust', array( 'methods' => 'PATCH', 'callback' => array( $this, 'adjust_inventory' ), 'permission_callback' => $admin ) );

		register_rest_route( $ns, '/api/admin/options', array( 'methods' => 'GET', 'callback' => array( $this, 'list_options' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/options/types', array( 'methods' => 'POST', 'callback' => array( $this, 'create_option_type' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/options/types/(?P<id>\d+)', array(
			array( 'methods' => 'PUT', 'callback' => array( $this, 'update_option_type' ), 'permission_callback' => $admin ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_option_type' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/options/values', array( 'methods' => 'POST', 'callback' => array( $this, 'create_option_value' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/options/values/(?P<id>\d+)', array(
			array( 'methods' => 'PUT', 'callback' => array( $this, 'update_option_value' ), 'permission_callback' => $admin ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_option_value' ), 'permission_callback' => $admin ),
		) );
	}

	private function woo_or_404( string $path ) {
		return cb_woo_active() ? null : cb_error( 'WooCommerce غیرفعال است', 400, 'WOO_INACTIVE', $path );
	}

	// ---- images -------------------------------------------------------------

	private function upload_attachment( int $parent ) {
		if ( empty( $_FILES['file'] ) ) {
			return 0;
		}
		require_once ABSPATH . 'wp-admin/includes/file.php';
		require_once ABSPATH . 'wp-admin/includes/media.php';
		require_once ABSPATH . 'wp-admin/includes/image.php';
		$aid = media_handle_upload( 'file', $parent );
		return is_wp_error( $aid ) ? 0 : (int) $aid;
	}

	public function add_image( WP_REST_Request $request ): WP_REST_Response {
		if ( $g = $this->woo_or_404( 'api/admin/products' ) ) {
			return $g;
		}
		$pid = (int) $request['id'];
		$aid = $this->upload_attachment( $pid );
		if ( ! $aid ) {
			$url = esc_url_raw( (string) ( $request->get_json_params()['url'] ?? '' ) );
			$aid = $url ? attachment_url_to_postid( $url ) : 0;
		}
		if ( ! $aid ) {
			return cb_error( 'تصویر نامعتبر است', 400, 'INVALID_IMAGE', 'api/admin/products' );
		}
		$product = wc_get_product( $pid );
		if ( ! $product->get_image_id() ) {
			$product->set_image_id( $aid );
		} else {
			$gallery = $product->get_gallery_image_ids();
			$gallery[] = $aid;
			$product->set_gallery_image_ids( array_values( array_unique( $gallery ) ) );
		}
		$product->save();
		$order = count( $product->get_gallery_image_ids() );
		return cb_response( array( 'id' => $aid, 'url' => wp_get_attachment_url( $aid ), 'sortOrder' => $order ), 201 );
	}

	public function reorder_images( WP_REST_Request $request ): WP_REST_Response {
		if ( $g = $this->woo_or_404( 'api/admin/products' ) ) {
			return $g;
		}
		$product = wc_get_product( (int) $request['id'] );
		$items   = (array) ( $request->get_json_params()['items'] ?? array() );
		usort( $items, function ( $a, $b ) {
			return ( (int) $a['sortOrder'] ) <=> ( (int) $b['sortOrder'] );
		} );
		$ids = array();
		foreach ( $items as $it ) {
			$ids[] = (int) $it['id'];
		}
		// First id becomes featured; the rest become the gallery.
		if ( $ids ) {
			$product->set_image_id( array_shift( $ids ) );
			$product->set_gallery_image_ids( $ids );
			$product->save();
		}
		$out   = array();
		$order = 0;
		foreach ( array_merge( array( $product->get_image_id() ), $product->get_gallery_image_ids() ) as $iid ) {
			$out[] = array( 'id' => (int) $iid, 'url' => wp_get_attachment_url( $iid ), 'sortOrder' => $order++ );
		}
		return cb_response( $out );
	}

	public function delete_image( WP_REST_Request $request ): WP_REST_Response {
		if ( $g = $this->woo_or_404( 'api/admin/products' ) ) {
			return $g;
		}
		$product = wc_get_product( (int) $request['id'] );
		$img     = (int) $request['img'];
		if ( (int) $product->get_image_id() === $img ) {
			$product->set_image_id( '' );
		}
		$product->set_gallery_image_ids( array_values( array_diff( $product->get_gallery_image_ids(), array( $img ) ) ) );
		$product->save();
		return cb_response( null, 204 );
	}

	// ---- videos (product meta cb_videos) -----------------------------------

	public function add_video( WP_REST_Request $request ): WP_REST_Response {
		$pid  = (int) $request['id'];
		$aid  = $this->upload_attachment( $pid );
		$url  = $aid ? wp_get_attachment_url( $aid ) : esc_url_raw( (string) ( $request->get_json_params()['url'] ?? '' ) );
		if ( ! $url ) {
			return cb_error( 'ویدیو نامعتبر است', 400, 'INVALID_VIDEO', 'api/admin/products' );
		}
		$list = array_values( (array) get_post_meta( $pid, 'cb_videos', true ) );
		$id   = (int) ( get_post_meta( $pid, 'cb_video_seq', true ) ) + 1;
		update_post_meta( $pid, 'cb_video_seq', $id );
		$entry = array( 'id' => $id, 'url' => $url, 'sortOrder' => count( $list ) );
		$list[] = $entry;
		update_post_meta( $pid, 'cb_videos', $list );
		return cb_response( $entry, 201 );
	}

	public function delete_video( WP_REST_Request $request ): WP_REST_Response {
		$pid  = (int) $request['id'];
		$vid  = (int) $request['vid'];
		$list = array_values( array_filter( (array) get_post_meta( $pid, 'cb_videos', true ), function ( $v ) use ( $vid ) {
			return (int) $v['id'] !== $vid;
		} ) );
		update_post_meta( $pid, 'cb_videos', $list );
		return cb_response( null, 204 );
	}

	// ---- variants -----------------------------------------------------------

	private function inventory_dto( WC_Product $v ): array {
		$on = $v->get_manage_stock() ? (int) $v->get_stock_quantity() : 0;
		return array(
			'variantId' => (int) $v->get_id(),
			'onHand'    => $on,
			'reserved'  => 0,
			'available' => $on,
			'version'   => (int) $v->get_meta( '_cb_inv_version' ),
		);
	}

	private function variant_dto( WC_Product $v ): array {
		$options = array();
		foreach ( $v->get_variation_attributes() as $name => $value ) {
			$options[ wc_attribute_label( str_replace( 'attribute_', '', $name ) ) ] = $value;
		}
		$sale = $v->get_sale_price();
		return array(
			'id'              => (int) $v->get_id(),
			'sku'             => $v->get_sku() ?: (string) $v->get_id(),
			'price'           => (float) $v->get_regular_price(),
			'discountedPrice' => ( $sale !== '' && $sale !== null ) ? (float) $sale : null,
			'compareAtPrice'  => null,
			'isActive'        => $v->get_status() === 'publish',
			'options'         => (object) $options,
			'inventory'       => $this->inventory_dto( $v ),
		);
	}

	public function create_variant( WP_REST_Request $request ): WP_REST_Response {
		if ( $g = $this->woo_or_404( 'api/admin/products' ) ) {
			return $g;
		}
		$pid     = (int) $request['id'];
		$product = wc_get_product( $pid );
		if ( ! $product ) {
			return cb_error( 'محصول یافت نشد', 404, 'NOT_FOUND', 'api/admin/products' );
		}
		$b       = $request->get_json_params();
		$options = (array) ( $b['options'] ?? array() );

		// Ensure the product is variable and carries the referenced attributes.
		if ( ! $product->is_type( 'variable' ) ) {
			wp_set_object_terms( $pid, 'variable', 'product_type' );
			$product = wc_get_product( $pid );
		}
		$attrs = $product->get_attributes();
		foreach ( $options as $opt ) {
			$label = (string) ( $opt['type'] ?? '' );
			$value = (string) ( $opt['value'] ?? '' );
			$key   = sanitize_title( $label );
			if ( ! isset( $attrs[ $key ] ) ) {
				$a = new WC_Product_Attribute();
				$a->set_name( $label );
				$a->set_options( array( $value ) );
				$a->set_visible( true );
				$a->set_variation( true );
				$attrs[ $key ] = $a;
			} else {
				$vals = $attrs[ $key ]->get_options();
				if ( ! in_array( $value, $vals, true ) ) {
					$vals[] = $value;
					$attrs[ $key ]->set_options( $vals );
				}
				$attrs[ $key ]->set_variation( true );
			}
		}
		$product->set_attributes( $attrs );
		$product->save();

		$variation  = new WC_Product_Variation();
		$variation->set_parent_id( $pid );
		$var_attrs = array();
		foreach ( $options as $opt ) {
			$var_attrs[ sanitize_title( (string) $opt['type'] ) ] = (string) $opt['value'];
		}
		$variation->set_attributes( $var_attrs );
		$variation->set_sku( sanitize_text_field( (string) ( $b['sku'] ?? '' ) ) );
		$variation->set_regular_price( (string) ( $b['price'] ?? 0 ) );
		if ( isset( $b['discountedPrice'] ) && $b['discountedPrice'] !== null ) {
			$variation->set_sale_price( (string) $b['discountedPrice'] );
		}
		$variation->set_manage_stock( true );
		$variation->set_stock_quantity( (int) ( $b['initialOnHand'] ?? 0 ) );
		$variation->set_status( ( isset( $b['isActive'] ) && ! $b['isActive'] ) ? 'private' : 'publish' );
		$variation->save();
		return cb_response( $this->variant_dto( $variation ), 201 );
	}

	public function update_variant( WP_REST_Request $request ): WP_REST_Response {
		$v = cb_woo_active() ? wc_get_product( (int) $request['id'] ) : null;
		if ( ! $v || ! $v->is_type( 'variation' ) ) {
			return cb_error( 'تنوع یافت نشد', 404, 'NOT_FOUND', 'api/admin/variants' );
		}
		$b = $request->get_json_params();
		if ( isset( $b['sku'] ) ) {
			$v->set_sku( sanitize_text_field( (string) $b['sku'] ) );
		}
		if ( isset( $b['price'] ) ) {
			$v->set_regular_price( (string) $b['price'] );
		}
		if ( array_key_exists( 'discountedPrice', $b ) ) {
			$v->set_sale_price( $b['discountedPrice'] !== null ? (string) $b['discountedPrice'] : '' );
		}
		if ( isset( $b['isActive'] ) ) {
			$v->set_status( $b['isActive'] ? 'publish' : 'private' );
		}
		$v->save();
		return cb_response( $this->variant_dto( $v ) );
	}

	public function delete_variant( WP_REST_Request $request ): WP_REST_Response {
		$v = cb_woo_active() ? wc_get_product( (int) $request['id'] ) : null;
		if ( $v && $v->is_type( 'variation' ) ) {
			$v->delete( true );
		}
		return cb_response( null, 204 );
	}

	// ---- inventory (optimistic version in _cb_inv_version) -----------------

	public function get_inventory( WP_REST_Request $request ): WP_REST_Response {
		$v = cb_woo_active() ? wc_get_product( (int) $request['id'] ) : null;
		if ( ! $v ) {
			return cb_error( 'تنوع یافت نشد', 404, 'NOT_FOUND', 'api/admin/variants' );
		}
		return cb_response( $this->inventory_dto( $v ) );
	}

	private function bump_version( WC_Product $v ): void {
		$v->update_meta_data( '_cb_inv_version', (int) $v->get_meta( '_cb_inv_version' ) + 1 );
	}

	public function set_inventory( WP_REST_Request $request ): WP_REST_Response {
		$v = cb_woo_active() ? wc_get_product( (int) $request['id'] ) : null;
		if ( ! $v ) {
			return cb_error( 'تنوع یافت نشد', 404, 'NOT_FOUND', 'api/admin/variants' );
		}
		$b = $request->get_json_params();
		if ( isset( $b['version'] ) && (int) $b['version'] !== (int) $v->get_meta( '_cb_inv_version' ) ) {
			return cb_error( 'نسخه‌ی موجودی قدیمی است', 409, 'STALE_VERSION', 'api/admin/variants' );
		}
		$v->set_manage_stock( true );
		$v->set_stock_quantity( (int) ( $b['onHand'] ?? 0 ) );
		$this->bump_version( $v );
		$v->save();
		return cb_response( $this->inventory_dto( $v ) );
	}

	public function adjust_inventory( WP_REST_Request $request ): WP_REST_Response {
		$v = cb_woo_active() ? wc_get_product( (int) $request['id'] ) : null;
		if ( ! $v ) {
			return cb_error( 'تنوع یافت نشد', 404, 'NOT_FOUND', 'api/admin/variants' );
		}
		$b = $request->get_json_params();
		if ( isset( $b['version'] ) && (int) $b['version'] !== (int) $v->get_meta( '_cb_inv_version' ) ) {
			return cb_error( 'نسخه‌ی موجودی قدیمی است', 409, 'STALE_VERSION', 'api/admin/variants' );
		}
		$v->set_manage_stock( true );
		$v->set_stock_quantity( max( 0, (int) $v->get_stock_quantity() + (int) ( $b['delta'] ?? 0 ) ) );
		$this->bump_version( $v );
		$v->save();
		return cb_response( $this->inventory_dto( $v ) );
	}

	// ---- global attributes (options) ---------------------------------------

	public function list_options(): WP_REST_Response {
		if ( ! function_exists( 'wc_get_attribute_taxonomies' ) ) {
			return cb_response( array() );
		}
		$out = array();
		foreach ( wc_get_attribute_taxonomies() as $tax ) {
			$taxonomy = wc_attribute_taxonomy_name( $tax->attribute_name );
			$values   = array();
			foreach ( get_terms( array( 'taxonomy' => $taxonomy, 'hide_empty' => false ) ) as $term ) {
				if ( ! is_wp_error( $term ) ) {
					$values[] = array( 'id' => (int) $term->term_id, 'value' => $term->name );
				}
			}
			$out[] = array( 'id' => (int) $tax->attribute_id, 'name' => $tax->attribute_label, 'values' => $values );
		}
		return cb_response( $out );
	}

	public function create_option_type( WP_REST_Request $request ): WP_REST_Response {
		$name = sanitize_text_field( (string) ( $request->get_json_params()['name'] ?? '' ) );
		if ( $name === '' || ! function_exists( 'wc_create_attribute' ) ) {
			return cb_error( 'نام نامعتبر است', 400, 'VALIDATION', 'api/admin/options' );
		}
		$id = wc_create_attribute( array( 'name' => $name, 'slug' => wc_sanitize_taxonomy_name( $name ) ) );
		if ( is_wp_error( $id ) ) {
			return cb_error( $id->get_error_message(), 400, 'CREATE_FAILED', 'api/admin/options' );
		}
		return cb_response( array( 'id' => (int) $id, 'name' => $name, 'values' => array() ), 201 );
	}

	public function update_option_type( WP_REST_Request $request ): WP_REST_Response {
		$id   = (int) $request['id'];
		$name = sanitize_text_field( (string) ( $request->get_json_params()['name'] ?? '' ) );
		if ( ! function_exists( 'wc_update_attribute' ) ) {
			return cb_error( 'ناموجود', 400, 'WOO_INACTIVE', 'api/admin/options' );
		}
		wc_update_attribute( $id, array( 'name' => $name, 'slug' => wc_sanitize_taxonomy_name( $name ) ) );
		return cb_response( array( 'id' => $id, 'name' => $name, 'values' => array() ) );
	}

	public function delete_option_type( WP_REST_Request $request ): WP_REST_Response {
		if ( function_exists( 'wc_delete_attribute' ) ) {
			wc_delete_attribute( (int) $request['id'] );
		}
		return cb_response( null, 204 );
	}

	private function taxonomy_for_attribute( int $attribute_id ): ?string {
		foreach ( wc_get_attribute_taxonomies() as $tax ) {
			if ( (int) $tax->attribute_id === $attribute_id ) {
				return wc_attribute_taxonomy_name( $tax->attribute_name );
			}
		}
		return null;
	}

	public function create_option_value( WP_REST_Request $request ): WP_REST_Response {
		$b   = $request->get_json_params();
		$tax = $this->taxonomy_for_attribute( (int) ( $b['optionTypeId'] ?? 0 ) );
		if ( ! $tax ) {
			return cb_error( 'ویژگی یافت نشد', 404, 'NOT_FOUND', 'api/admin/options/values' );
		}
		$res = wp_insert_term( sanitize_text_field( (string) ( $b['value'] ?? '' ) ), $tax );
		if ( is_wp_error( $res ) ) {
			return cb_error( 'افزودن مقدار ناموفق بود', 400, 'CREATE_FAILED', 'api/admin/options/values' );
		}
		$term = get_term( $res['term_id'] );
		return cb_response( array( 'id' => (int) $term->term_id, 'value' => $term->name ), 201 );
	}

	public function update_option_value( WP_REST_Request $request ): WP_REST_Response {
		$id   = (int) $request['id'];
		$term = get_term( $id );
		if ( ! $term || is_wp_error( $term ) ) {
			return cb_error( 'مقدار یافت نشد', 404, 'NOT_FOUND', 'api/admin/options/values' );
		}
		wp_update_term( $id, $term->taxonomy, array( 'name' => sanitize_text_field( (string) ( $request->get_json_params()['value'] ?? $term->name ) ) ) );
		$term = get_term( $id );
		return cb_response( array( 'id' => $id, 'value' => $term->name ) );
	}

	public function delete_option_value( WP_REST_Request $request ): WP_REST_Response {
		$id   = (int) $request['id'];
		$term = get_term( $id );
		if ( $term && ! is_wp_error( $term ) ) {
			wp_delete_term( $id, $term->taxonomy );
		}
		return cb_response( null, 204 );
	}
}
