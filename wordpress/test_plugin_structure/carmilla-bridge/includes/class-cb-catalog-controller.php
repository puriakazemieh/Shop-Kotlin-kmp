<?php
/**
 * Catalog endpoints backed by WooCommerce, shaped to the app's catalog DTOs.
 *
 * Public:
 *   GET  api/categories                 -> List<CategoryResponse> (tree)
 *   GET  api/products                   -> PageResponse<ProductSummaryResponse>
 *   GET  api/products/{slug}            -> ProductDetailResponse
 *   GET  api/campaigns/active           -> CampaignResponse | null
 *   GET  api/banners                    -> List<BannerResponse>
 * Admin (Bearer, edit_others_posts / manage_woocommerce):
 *   GET    api/admin/products           -> PageResponse<ProductSummaryResponse>
 *   POST   api/admin/products           -> ProductDetailResponse (simple product)
 *   PATCH  api/admin/products/{id}      -> ProductDetailResponse
 *   DELETE api/admin/products/{id}      -> 204
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Catalog_Controller {

	public function register_routes(): void {
		$ns = CB_REST_NAMESPACE;

		register_rest_route( $ns, '/api/categories', array(
			'methods'             => 'GET',
			'callback'            => array( $this, 'categories' ),
			'permission_callback' => '__return_true',
		) );

		register_rest_route( $ns, '/api/products', array(
			'methods'             => 'GET',
			'callback'            => array( $this, 'products' ),
			'permission_callback' => '__return_true',
		) );

		register_rest_route( $ns, '/api/products/(?P<slug>[a-zA-Z0-9\-_%]+)', array(
			'methods'             => 'GET',
			'callback'            => array( $this, 'product_detail' ),
			'permission_callback' => '__return_true',
		) );

		register_rest_route( $ns, '/api/campaigns/active', array(
			'methods'             => 'GET',
			'callback'            => array( $this, 'active_campaign' ),
			'permission_callback' => '__return_true',
		) );

		register_rest_route( $ns, '/api/banners', array(
			'methods'             => 'GET',
			'callback'            => array( $this, 'banners' ),
			'permission_callback' => '__return_true',
		) );

		// Admin write.
		register_rest_route( $ns, '/api/admin/products', array(
			array(
				'methods'             => 'GET',
				'callback'            => array( $this, 'products' ),
				'permission_callback' => array( 'CB_Plugin', 'require_admin' ),
			),
			array(
				'methods'             => 'POST',
				'callback'            => array( $this, 'create_product' ),
				'permission_callback' => array( 'CB_Plugin', 'require_admin' ),
			),
		) );

		register_rest_route( $ns, '/api/admin/products/(?P<id>\d+)', array(
			array(
				'methods'             => 'GET',
				'callback'            => array( $this, 'admin_product_by_id' ),
				'permission_callback' => array( 'CB_Plugin', 'require_admin' ),
			),
			array(
				'methods'             => 'PATCH',
				'callback'            => array( $this, 'update_product' ),
				'permission_callback' => array( 'CB_Plugin', 'require_admin' ),
			),
			array(
				'methods'             => 'DELETE',
				'callback'            => array( $this, 'delete_product' ),
				'permission_callback' => array( 'CB_Plugin', 'require_admin' ),
			),
		) );
	}

	private function ensure_woo() {
		return cb_woo_active() ? null : cb_error( 'WooCommerce فعال نیست.', 503, 'WOOCOMMERCE_INACTIVE' );
	}

	// ---- categories ---------------------------------------------------------

	public function categories( WP_REST_Request $request ) {
		if ( $err = $this->ensure_woo() ) {
			return $err;
		}
		$terms = get_terms( array( 'taxonomy' => 'product_cat', 'hide_empty' => false ) );
		if ( is_wp_error( $terms ) ) {
			return cb_response( array(), 200 );
		}

		$by_parent = array();
		foreach ( $terms as $t ) {
			$by_parent[ (int) $t->parent ][] = $t;
		}
		return cb_response( $this->build_category_tree( $by_parent, 0 ), 200 );
	}

	private function build_category_tree( array $by_parent, int $parent ): array {
		$out = array();
		foreach ( $by_parent[ $parent ] ?? array() as $t ) {
			$out[] = array(
				'id'       => (int) $t->term_id,
				'name'     => $t->name,
				'slug'     => $t->slug,
				'parentId' => $t->parent ? (int) $t->parent : null,
				'children' => $this->build_category_tree( $by_parent, (int) $t->term_id ),
			);
		}
		return $out;
	}

	// ---- product listing ----------------------------------------------------

	public function products( WP_REST_Request $request ) {
		if ( $err = $this->ensure_woo() ) {
			return $err;
		}
		$page = max( 0, (int) $request->get_param( 'page' ) );
		$size = min( 100, max( 1, (int) ( $request->get_param( 'size' ) ?: 20 ) ) );

		$args = array(
			'status'   => 'publish',
			'limit'    => $size,
			'page'     => $page + 1, // WC pages are 1-based; app is 0-based.
			'paginate' => true,
			'return'   => 'objects',
		);

		if ( $q = $request->get_param( 'query' ) ) {
			$args['s'] = sanitize_text_field( $q );
		}
		if ( $cat = $request->get_param( 'categoryId' ) ) {
			$args['category'] = array( $this->cat_slug( (int) $cat ) );
		}
		if ( $request->get_param( 'inStock' ) === 'true' || $request->get_param( 'inStock' ) === true ) {
			$args['stock_status'] = 'instock';
		}
		$min = $request->get_param( 'minPrice' );
		$max = $request->get_param( 'maxPrice' );
		if ( $min !== null || $max !== null ) {
			$args['meta_query'] = array(
				array(
					'key'     => '_price',
					'value'   => array( $min !== null ? (float) $min : 0, $max !== null ? (float) $max : PHP_INT_MAX ),
					'compare' => 'BETWEEN',
					'type'    => 'NUMERIC',
				),
			);
		}
		$this->apply_sort( $args, (string) $request->get_param( 'sort' ) );

		$result = wc_get_products( $args );
		$items  = array();
		foreach ( $result->products as $product ) {
			$items[] = $this->product_summary( $product );
		}

		return cb_response( cb_page( $items, $page, $size, (int) $result->total, (int) $result->max_num_pages ), 200 );
	}

	private function apply_sort( array &$args, string $sort ): void {
		switch ( $sort ) {
			case 'price,asc':
				$args['orderby'] = 'meta_value_num';
				$args['meta_key'] = '_price';
				$args['order'] = 'ASC';
				break;
			case 'price,desc':
				$args['orderby'] = 'meta_value_num';
				$args['meta_key'] = '_price';
				$args['order'] = 'DESC';
				break;
			case 'newest':
			case 'createdAt,desc':
				$args['orderby'] = 'date';
				$args['order'] = 'DESC';
				break;
		}
	}

	private function cat_slug( int $term_id ): string {
		$term = get_term( $term_id, 'product_cat' );
		return ( $term && ! is_wp_error( $term ) ) ? $term->slug : '';
	}

	// ---- product detail -----------------------------------------------------

	public function product_detail( WP_REST_Request $request ) {
		if ( $err = $this->ensure_woo() ) {
			return $err;
		}
		$slug = rawurldecode( (string) $request['slug'] );
		$post = get_page_by_path( $slug, OBJECT, 'product' );
		if ( ! $post ) {
			return cb_error( 'محصول یافت نشد.', 404, 'PRODUCT_NOT_FOUND' );
		}
		$product = wc_get_product( $post->ID );
		if ( ! $product ) {
			return cb_error( 'محصول یافت نشد.', 404, 'PRODUCT_NOT_FOUND' );
		}
		return cb_response( $this->product_detail_dto( $product ), 200 );
	}

	public function admin_product_by_id( WP_REST_Request $request ) {
		if ( $err = $this->ensure_woo() ) {
			return $err;
		}
		$product = wc_get_product( (int) $request['id'] );
		if ( ! $product ) {
			return cb_error( 'محصول یافت نشد.', 404, 'PRODUCT_NOT_FOUND' );
		}
		return cb_response( $this->product_detail_dto( $product ), 200 );
	}

	// ---- admin write --------------------------------------------------------

	public function create_product( WP_REST_Request $request ) {
		if ( $err = $this->ensure_woo() ) {
			return $err;
		}
		$product = new WC_Product_Simple();
		$this->apply_product_fields( $product, $request, true );
		$product->save();

		return cb_response( $this->product_detail_dto( $product ), 201 );
	}

	public function update_product( WP_REST_Request $request ) {
		if ( $err = $this->ensure_woo() ) {
			return $err;
		}
		$product = wc_get_product( (int) $request['id'] );
		if ( ! $product ) {
			return cb_error( 'محصول یافت نشد.', 404, 'PRODUCT_NOT_FOUND' );
		}
		$this->apply_product_fields( $product, $request, false );
		$product->save();

		return cb_response( $this->product_detail_dto( $product ), 200 );
	}

	public function delete_product( WP_REST_Request $request ) {
		if ( $err = $this->ensure_woo() ) {
			return $err;
		}
		$product = wc_get_product( (int) $request['id'] );
		if ( ! $product ) {
			return cb_error( 'محصول یافت نشد.', 404, 'PRODUCT_NOT_FOUND' );
		}
		$product->delete( true );
		return new WP_REST_Response( null, 204 );
	}

	/**
	 * Map AdminCreate/UpdateProductRequest fields onto a WC_Product (simple).
	 * Variant/option handling is intentionally out of scope for this first increment.
	 */
	private function apply_product_fields( WC_Product $product, WP_REST_Request $request, bool $is_create ): void {
		$title = $request->get_param( 'title' );
		if ( $title !== null ) {
			$product->set_name( sanitize_text_field( $title ) );
		}
		$slug = $request->get_param( 'slug' );
		if ( $slug !== null && $slug !== '' ) {
			$product->set_slug( sanitize_title( $slug ) );
		}
		$desc = $request->get_param( 'description' );
		if ( $desc !== null ) {
			$product->set_description( wp_kses_post( $desc ) );
		}
		$base = $request->get_param( 'basePrice' );
		if ( $base !== null ) {
			$product->set_regular_price( (string) (float) $base );
		}
		$disc = $request->get_param( 'discountedPrice' );
		if ( $disc !== null ) {
			$product->set_sale_price( $disc === '' ? '' : (string) (float) $disc );
		}
		$active = $request->get_param( 'isActive' );
		if ( $active !== null ) {
			$product->set_status( ( $active === true || $active === 'true' ) ? 'publish' : 'draft' );
		} elseif ( $is_create ) {
			$product->set_status( 'publish' );
		}
		$sku = $request->get_param( 'sku' );
		if ( $sku ) {
			$product->set_sku( sanitize_text_field( $sku ) );
		}
		$cat = $request->get_param( 'categoryId' );
		if ( $cat ) {
			$product->set_category_ids( array( (int) $cat ) );
		}
		$stock = $request->get_param( 'initialOnHand' );
		if ( $stock !== null ) {
			$product->set_manage_stock( true );
			$product->set_stock_quantity( (int) $stock );
		}
		$brand = $request->get_param( 'brand' );
		if ( $brand !== null ) {
			$product->update_meta_data( 'cb_brand', sanitize_text_field( $brand ) );
		}
		$attrs = $request->get_param( 'attributes' );
		if ( is_array( $attrs ) ) {
			$product->update_meta_data( 'cb_attributes', wp_json_encode( array_values( $attrs ) ) );
		}
	}

	// ---- DTO builders -------------------------------------------------------

	private function product_summary( WC_Product $product ): array {
		$cats = $product->get_category_ids();
		$cat_id = $cats ? (int) $cats[0] : null;
		$prices = $this->price_range( $product );

		return array(
			'id'                  => $product->get_id(),
			'title'               => $product->get_name(),
			'slug'                => $product->get_slug(),
			'thumbnailUrl'        => wp_get_attachment_url( $product->get_image_id() ) ?: null,
			'minPrice'            => $prices['minReg'],
			'maxPrice'            => $prices['maxReg'],
			'minDiscountedPrice'  => $prices['minSale'],
			'maxDiscountedPrice'  => $prices['maxSale'],
			'inStock'             => $product->is_in_stock(),
			'categoryId'          => $cat_id,
			'categoryName'        => $cat_id ? $this->cat_name( $cat_id ) : null,
			// (object) so an empty map serializes as {} not [] (the app decodes a Map).
			'options'             => (object) $this->variation_options( $product ),
			'isFavorite'          => false,
			'averageRating'       => (float) $product->get_average_rating() ?: null,
			'reviewCount'         => (int) $product->get_review_count(),
		);
	}

	private function product_detail_dto( WC_Product $product ): array {
		$cats = $product->get_category_ids();
		$cat_id = $cats ? (int) $cats[0] : null;
		$prices = $this->price_range( $product );

		$images = array();
		$order  = 0;
		if ( $product->get_image_id() ) {
			$images[] = array( 'id' => (int) $product->get_image_id(), 'url' => wp_get_attachment_url( $product->get_image_id() ), 'sortOrder' => $order++ );
		}
		foreach ( $product->get_gallery_image_ids() as $img_id ) {
			$images[] = array( 'id' => (int) $img_id, 'url' => wp_get_attachment_url( $img_id ), 'sortOrder' => $order++ );
		}

		return array(
			'id'              => $product->get_id(),
			'title'           => $product->get_name(),
			'slug'            => $product->get_slug(),
			'description'     => $product->get_description(),
			'brand'           => $product->get_meta( 'cb_brand' ) ?: null,
			'attributes'      => $this->spec_attributes( $product ),
			'categoryId'      => $cat_id,
			'categoryName'    => $cat_id ? $this->cat_name( $cat_id ) : null,
			'images'          => $images,
			'videos'          => array(),
			'variants'        => $this->variants( $product ),
			'createdAt'       => cb_iso( $product->get_date_created() ? $product->get_date_created()->date( 'c' ) : '' ),
			'basePrice'       => $prices['minReg'],
			'discountedPrice' => $prices['minSale'],
			'isFavorite'      => false,
		);
	}

	private function cat_name( int $id ) {
		$term = get_term( $id, 'product_cat' );
		return ( $term && ! is_wp_error( $term ) ) ? $term->name : null;
	}

	/** attribute name => list of option values (variation attributes). */
	private function variation_options( WC_Product $product ): array {
		if ( ! $product->is_type( 'variable' ) ) {
			return array();
		}
		$out = array();
		foreach ( $product->get_variation_attributes() as $name => $values ) {
			$out[ wc_attribute_label( $name ) ] = array_values( $values );
		}
		return $out;
	}

	/** Non-variation custom attributes + JSON spec meta => List<ProductAttributeDto>. */
	private function spec_attributes( WC_Product $product ): array {
		$out = array();
		$json = $product->get_meta( 'cb_attributes' );
		if ( $json ) {
			$decoded = json_decode( $json, true );
			if ( is_array( $decoded ) ) {
				foreach ( $decoded as $a ) {
					$out[] = array( 'name' => (string) ( $a['name'] ?? '' ), 'value' => (string) ( $a['value'] ?? '' ) );
				}
			}
		}
		foreach ( $product->get_attributes() as $attr ) {
			if ( is_a( $attr, 'WC_Product_Attribute' ) && ! $attr->get_variation() ) {
				$out[] = array(
					'name'  => wc_attribute_label( $attr->get_name() ),
					'value' => implode( '، ', $attr->get_options() ),
				);
			}
		}
		return $out;
	}

	private function variants( WC_Product $product ): array {
		$out = array();
		if ( $product->is_type( 'variable' ) ) {
			foreach ( $product->get_children() as $vid ) {
				$v = wc_get_product( $vid );
				if ( ! $v ) {
					continue;
				}
				$options = array();
				foreach ( $v->get_variation_attributes() as $name => $value ) {
					$label = wc_attribute_label( str_replace( 'attribute_', '', $name ) );
					$options[ $label ] = $value;
				}
				$out[] = array(
					'id'              => $v->get_id(),
					'sku'             => $v->get_sku() ?: (string) $v->get_id(),
					'price'           => (float) $v->get_regular_price(),
					'discountedPrice' => cb_price( $v->get_sale_price() ),
					'compareAtPrice'  => null,
					'options'         => (object) $options,
					'availableQty'    => $v->get_manage_stock() ? (int) $v->get_stock_quantity() : ( $v->is_in_stock() ? 999 : 0 ),
					'active'          => $v->get_status() === 'publish',
				);
			}
		} else {
			// Simple product => single synthetic variant so the app can add to cart.
			$out[] = array(
				'id'              => $product->get_id(),
				'sku'             => $product->get_sku() ?: (string) $product->get_id(),
				'price'           => (float) $product->get_regular_price(),
				'discountedPrice' => cb_price( $product->get_sale_price() ),
				'compareAtPrice'  => null,
				'options'         => (object) array(),
				'availableQty'    => $product->get_manage_stock() ? (int) $product->get_stock_quantity() : ( $product->is_in_stock() ? 999 : 0 ),
				'active'          => $product->get_status() === 'publish',
			);
		}
		return $out;
	}

	private function price_range( WC_Product $product ): array {
		if ( $product->is_type( 'variable' ) ) {
			$prices = $product->get_variation_prices( true );
			$reg = $prices['regular_price'] ?? array();
			$sale = array();
			foreach ( ( $prices['sale_price'] ?? array() ) as $k => $sp ) {
				if ( isset( $reg[ $k ] ) && (float) $sp < (float) $reg[ $k ] ) {
					$sale[ $k ] = $sp;
				}
			}
			return array(
				'minReg'  => $reg ? (float) min( $reg ) : null,
				'maxReg'  => $reg ? (float) max( $reg ) : null,
				'minSale' => $sale ? (float) min( $sale ) : null,
				'maxSale' => $sale ? (float) max( $sale ) : null,
			);
		}
		$reg  = cb_price( $product->get_regular_price() );
		$sale = cb_price( $product->get_sale_price() );
		return array( 'minReg' => $reg, 'maxReg' => $reg, 'minSale' => $sale, 'maxSale' => $sale );
	}

	// ---- campaigns & banners (custom post types) ----------------------------

	public function active_campaign( WP_REST_Request $request ) {
		$posts = get_posts( array(
			'post_type'   => 'cb_campaign',
			'post_status' => 'publish',
			'numberposts' => 1,
			'meta_key'    => 'is_active',
			'meta_value'  => '1',
		) );
		if ( empty( $posts ) ) {
			return cb_response( null, 200 );
		}
		$c        = $posts[0];
		$ends_at  = (string) get_post_meta( $c->ID, 'ends_at', true );
		$remaining = $ends_at ? max( 0, strtotime( $ends_at ) - time() ) : 0;

		$products = array();
		if ( cb_woo_active() ) {
			$ids = array_filter( array_map( 'intval', explode( ',', (string) get_post_meta( $c->ID, 'product_ids', true ) ) ) );
			foreach ( $ids as $pid ) {
				$p = wc_get_product( $pid );
				if ( $p ) {
					$products[] = $this->product_summary( $p );
				}
			}
		}

		return cb_response( array(
			'id'               => (int) $c->ID,
			'title'            => $c->post_title,
			'endsAt'           => $ends_at ? cb_iso( $ends_at ) : '',
			'remainingSeconds' => (int) $remaining,
			'products'         => $products,
		), 200 );
	}

	public function banners( WP_REST_Request $request ) {
		$posts = get_posts( array(
			'post_type'   => 'cb_banner',
			'post_status' => 'publish',
			'numberposts' => -1,
			'orderby'     => 'meta_value_num',
			'meta_key'    => 'sort_order',
			'order'       => 'ASC',
		) );
		$out = array();
		foreach ( $posts as $b ) {
			if ( get_post_meta( $b->ID, 'is_active', true ) === '0' ) {
				continue;
			}
			$cat = get_post_meta( $b->ID, 'category_id', true );
			$out[] = array(
				'id'        => (int) $b->ID,
				'title'     => $b->post_title,
				'subtitle'  => get_post_meta( $b->ID, 'subtitle', true ) ?: null,
				'imageUrl'  => get_post_meta( $b->ID, 'image_url', true ) ?: get_the_post_thumbnail_url( $b->ID, 'full' ) ?: null,
				'categoryId'=> $cat !== '' ? (int) $cat : null,
				'sortOrder' => (int) get_post_meta( $b->ID, 'sort_order', true ),
				'isActive'  => true,
			);
		}
		return cb_response( $out, 200 );
	}
}
