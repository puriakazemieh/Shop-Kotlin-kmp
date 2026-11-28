<?php
namespace Carmilla\Core\Catalog;

use Carmilla\Core\Adapter\WooCommerceAdapter;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\CatalogService', false ) ) {

/**
 * Canonical Shared WooCommerce Catalog Service.
 *
 * Centralizes category tree, product list/search/detail, media mapping,
 * and admin product CRUD via WooCommerceAdapter so Theme-only, Plugin-only,
 * and Co-install modes return identical DTOs and preserve product IDs.
 *
 * Part of Task P04-WORDPRESS-CODE-026A.
 */
class CatalogService {

	public static function can_manage_catalog(): bool {
		return current_user_can( 'manage_options' )
			|| current_user_can( 'carmilla_manage_shop' )
			|| current_user_can( 'carmilla_edit_products' )
			|| current_user_can( 'edit_others_posts' )
			|| current_user_can( 'manage_woocommerce' );
	}

	/**
	 * Returns null when WooCommerce is available, or WP_Error(503) otherwise.
	 *
	 * @return \WP_Error|null
	 */
	public static function ensure_woo() {
		if ( WooCommerceAdapter::is_available() && function_exists( 'wc_get_products' ) ) {
			return null;
		}
		return new \WP_Error(
			'WOOCOMMERCE_INACTIVE',
			'افزونه WooCommerce برای کاتالوگ و محصولات فعال نیست.',
			[ 'status' => 503 ]
		);
	}

	/**
	 * @return array<int, array<string, mixed>>|\WP_Error
	 */
	public static function list_categories() {
		$err = self::ensure_woo();
		if ( $err ) {
			return $err;
		}

		$terms = get_terms( [ 'taxonomy' => 'product_cat', 'hide_empty' => false ] );
		if ( is_wp_error( $terms ) || ! is_array( $terms ) ) {
			return [];
		}

		$by_parent = [];
		foreach ( $terms as $t ) {
			$by_parent[ (int) $t->parent ][] = $t;
		}

		return self::build_category_tree( $by_parent, 0 );
	}

	/**
	 * @param array<int, array<int, \WP_Term>> $by_parent
	 * @return array<int, array<string, mixed>>
	 */
	private static function build_category_tree( array $by_parent, int $parent ): array {
		$out = [];
		foreach ( $by_parent[ $parent ] ?? [] as $t ) {
			$out[] = [
				'id'       => (int) $t->term_id,
				'name'     => $t->name,
				'slug'     => $t->slug,
				'parentId' => $t->parent ? (int) $t->parent : null,
				'children' => self::build_category_tree( $by_parent, (int) $t->term_id ),
			];
		}
		return $out;
	}

	/**
	 * @param array<string, mixed> $params
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function list_products( array $params = [] ) {
		$err = self::ensure_woo();
		if ( $err ) {
			return $err;
		}

		$page = max( 0, (int) ( $params['page'] ?? 0 ) );
		$size = min( 100, max( 1, (int) ( $params['size'] ?? ( $params['per_page'] ?? 20 ) ) ) );

		$args = [
			'status'   => 'publish',
			'limit'    => $size,
			'page'     => $page + 1,
			'paginate' => true,
			'return'   => 'objects',
		];

		if ( ! empty( $params['query'] ) ) {
			$args['s'] = sanitize_text_field( (string) $params['query'] );
		} elseif ( ! empty( $params['search'] ) ) {
			$args['s'] = sanitize_text_field( (string) $params['search'] );
		}

		if ( ! empty( $params['categoryId'] ) ) {
			$slug = self::cat_slug( (int) $params['categoryId'] );
			if ( '' !== $slug ) {
				$args['category'] = [ $slug ];
			}
		}

		if ( isset( $params['inStock'] ) && ( 'true' === $params['inStock'] || true === $params['inStock'] ) ) {
			$args['stock_status'] = 'instock';
		}

		$min = $params['minPrice'] ?? null;
		$max = $params['maxPrice'] ?? null;
		if ( null !== $min || null !== $max ) {
			$args['meta_query'] = [
				[
					'key'     => '_price',
					'value'   => [ null !== $min ? (float) $min : 0, null !== $max ? (float) $max : PHP_INT_MAX ],
					'compare' => 'BETWEEN',
					'type'    => 'NUMERIC',
				],
			];
		}

		self::apply_sort( $args, (string) ( $params['sort'] ?? '' ) );

		$result = WooCommerceAdapter::get_products( $args );
		$items  = [];
		$total  = 0;
		$pages  = 1;

		if ( is_object( $result ) && isset( $result->products ) ) {
			foreach ( $result->products as $product ) {
				if ( $product instanceof \WC_Product ) {
					$items[] = self::format_product_summary( $product );
				}
			}
			$total = (int) $result->total;
			$pages = (int) $result->max_num_pages;
		} elseif ( is_array( $result ) ) {
			foreach ( $result as $product ) {
				if ( $product instanceof \WC_Product ) {
					$items[] = self::format_product_summary( $product );
				}
			}
			$total = count( $items );
		}

		return [
			'content'       => $items,
			'page'          => $page,
			'size'          => $size,
			'totalElements' => $total,
			'totalPages'    => $pages,
			'last'          => ( $page + 1 ) >= max( 1, $pages ),
		];
	}

	/**
	 * @param int|string $id_or_slug
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function get_product_detail( $id_or_slug ) {
		$err = self::ensure_woo();
		if ( $err ) {
			return $err;
		}

		$product = null;
		if ( is_numeric( $id_or_slug ) ) {
			$product = WooCommerceAdapter::get_product( (int) $id_or_slug );
		} else {
			$slug = rawurldecode( (string) $id_or_slug );
			$post = get_page_by_path( $slug, OBJECT, 'product' );
			if ( $post ) {
				$product = WooCommerceAdapter::get_product( (int) $post->ID );
			}
		}

		if ( ! $product ) {
			return new \WP_Error( 'PRODUCT_NOT_FOUND', 'محصول یافت نشد.', [ 'status' => 404 ] );
		}

		return self::format_product_detail( $product );
	}

	/**
	 * @param array<string, mixed> $data
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function create_product( array $data ) {
		$err = self::ensure_woo();
		if ( $err ) {
			return $err;
		}

		if ( ! class_exists( '\WC_Product_Simple' ) ) {
			return new \WP_Error( 'WOOCOMMERCE_INACTIVE', 'کلاس محصول WooCommerce در دسترس نیست.', [ 'status' => 503 ] );
		}

		$product = new \WC_Product_Simple();
		self::apply_product_fields( $product, $data, true );
		$product->save();

		return self::format_product_detail( $product );
	}

	/**
	 * @param array<string, mixed> $data
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function update_product( int $id, array $data ) {
		$err = self::ensure_woo();
		if ( $err ) {
			return $err;
		}

		$product = WooCommerceAdapter::get_product( $id );
		if ( ! $product ) {
			return new \WP_Error( 'PRODUCT_NOT_FOUND', 'محصول یافت نشد.', [ 'status' => 404 ] );
		}

		self::apply_product_fields( $product, $data, false );
		$product->save();

		return self::format_product_detail( $product );
	}

	/**
	 * @return true|\WP_Error
	 */
	public static function delete_product( int $id ) {
		$err = self::ensure_woo();
		if ( $err ) {
			return $err;
		}

		$product = WooCommerceAdapter::get_product( $id );
		if ( ! $product ) {
			return new \WP_Error( 'PRODUCT_NOT_FOUND', 'محصول یافت نشد.', [ 'status' => 404 ] );
		}

		$product->delete( true );
		return true;
	}

	/**
	 * @return array<string, mixed>|null
	 */
	public static function active_campaign(): ?array {
		$posts = get_posts( [
			'post_type'   => 'cb_campaign',
			'post_status' => 'publish',
			'numberposts' => 1,
			'meta_key'    => 'is_active',
			'meta_value'  => '1',
		] );
		if ( empty( $posts ) ) {
			return null;
		}

		$c         = $posts[0];
		$ends_at   = (string) get_post_meta( $c->ID, 'ends_at', true );
		$remaining = $ends_at ? max( 0, strtotime( $ends_at ) - time() ) : 0;

		$products = [];
		if ( WooCommerceAdapter::is_available() ) {
			$ids = array_filter( array_map( 'intval', explode( ',', (string) get_post_meta( $c->ID, 'product_ids', true ) ) ) );
			foreach ( $ids as $pid ) {
				$p = WooCommerceAdapter::get_product( $pid );
				if ( $p ) {
					$products[] = self::format_product_summary( $p );
				}
			}
		}

		return [
			'id'               => (int) $c->ID,
			'title'            => $c->post_title,
			'endsAt'           => $ends_at ? gmdate( 'c', strtotime( $ends_at . ' UTC' ) ) : '',
			'remainingSeconds' => (int) $remaining,
			'products'         => $products,
		];
	}

	/**
	 * @return array<int, array<string, mixed>>
	 */
	public static function list_banners(): array {
		$posts = get_posts( [
			'post_type'   => 'cb_banner',
			'post_status' => 'publish',
			'numberposts' => -1,
			'orderby'     => 'meta_value_num',
			'meta_key'    => 'sort_order',
			'order'       => 'ASC',
		] );

		$out = [];
		foreach ( $posts as $b ) {
			if ( '0' === get_post_meta( $b->ID, 'is_active', true ) ) {
				continue;
			}
			$cat   = get_post_meta( $b->ID, 'category_id', true );
			$out[] = [
				'id'         => (int) $b->ID,
				'title'      => $b->post_title,
				'subtitle'   => get_post_meta( $b->ID, 'subtitle', true ) ?: null,
				'imageUrl'   => get_post_meta( $b->ID, 'image_url', true ) ?: get_the_post_thumbnail_url( $b->ID, 'full' ) ?: null,
				'categoryId' => '' !== $cat ? (int) $cat : null,
				'sortOrder'  => (int) get_post_meta( $b->ID, 'sort_order', true ),
				'isActive'   => true,
			];
		}
		return $out;
	}

	/**
	 * Maps product featured + gallery attachments into canonical image DTOs.
	 *
	 * @return array<int, array<string, mixed>>
	 */
	public static function map_product_images( \WC_Product $product ): array {
		$images = [];
		$order  = 0;
		$main   = (int) $product->get_image_id();
		if ( $main > 0 ) {
			$images[] = [
				'id'        => $main,
				'url'       => wp_get_attachment_url( $main ) ?: '',
				'sortOrder' => $order++,
			];
		}
		foreach ( $product->get_gallery_image_ids() as $img_id ) {
			$img_id = (int) $img_id;
			if ( $img_id > 0 ) {
				$images[] = [
					'id'        => $img_id,
					'url'       => wp_get_attachment_url( $img_id ) ?: '',
					'sortOrder' => $order++,
				];
			}
		}
		return $images;
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function format_product_summary( \WC_Product $product ): array {
		$cats   = $product->get_category_ids();
		$cat_id = ! empty( $cats ) ? (int) $cats[0] : null;
		$prices = self::price_range( $product );

		return [
			'id'                 => (int) $product->get_id(),
			'title'              => $product->get_name(),
			'slug'               => $product->get_slug(),
			'thumbnailUrl'       => wp_get_attachment_url( (int) $product->get_image_id() ) ?: null,
			'minPrice'           => $prices['minReg'],
			'maxPrice'           => $prices['maxReg'],
			'minDiscountedPrice' => $prices['minSale'],
			'maxDiscountedPrice' => $prices['maxSale'],
			'inStock'            => $product->is_in_stock(),
			'categoryId'         => $cat_id,
			'categoryName'       => $cat_id ? self::cat_name( $cat_id ) : null,
			'options'            => (object) self::variation_options( $product ),
			'isFavorite'         => false,
			'averageRating'      => (float) $product->get_average_rating() ?: null,
			'reviewCount'        => (int) $product->get_review_count(),
		];
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function format_product_detail( \WC_Product $product ): array {
		$cats   = $product->get_category_ids();
		$cat_id = ! empty( $cats ) ? (int) $cats[0] : null;
		$prices = self::price_range( $product );
		$dt     = $product->get_date_created();

		return [
			'id'              => (int) $product->get_id(),
			'title'           => $product->get_name(),
			'slug'            => $product->get_slug(),
			'description'     => $product->get_description(),
			'brand'           => $product->get_meta( 'cb_brand' ) ?: null,
			'attributes'      => self::spec_attributes( $product ),
			'categoryId'      => $cat_id,
			'categoryName'    => $cat_id ? self::cat_name( $cat_id ) : null,
			'images'          => self::map_product_images( $product ),
			'videos'          => [],
			'variants'        => self::variants( $product ),
			'createdAt'       => $dt ? gmdate( 'c', $dt->getTimestamp() ) : gmdate( 'c' ),
			'basePrice'       => $prices['minReg'],
			'discountedPrice' => $prices['minSale'],
			'isFavorite'      => false,
		];
	}

	/**
	 * @param array<string, mixed> $data
	 */
	private static function apply_product_fields( \WC_Product $product, array $data, bool $is_create ): void {
		if ( array_key_exists( 'title', $data ) && null !== $data['title'] ) {
			$product->set_name( sanitize_text_field( (string) $data['title'] ) );
		}
		if ( ! empty( $data['slug'] ) ) {
			$product->set_slug( sanitize_title( (string) $data['slug'] ) );
		}
		if ( array_key_exists( 'description', $data ) && null !== $data['description'] ) {
			$product->set_description( wp_kses_post( (string) $data['description'] ) );
		}
		if ( array_key_exists( 'basePrice', $data ) && null !== $data['basePrice'] ) {
			$product->set_regular_price( (string) (float) $data['basePrice'] );
		}
		if ( array_key_exists( 'discountedPrice', $data ) && null !== $data['discountedPrice'] ) {
			$disc = $data['discountedPrice'];
			$product->set_sale_price( '' === $disc ? '' : (string) (float) $disc );
		}
		if ( array_key_exists( 'isActive', $data ) && null !== $data['isActive'] ) {
			$active = $data['isActive'];
			$product->set_status( ( true === $active || 'true' === $active ) ? 'publish' : 'draft' );
		} elseif ( $is_create ) {
			$product->set_status( 'publish' );
		}
		if ( ! empty( $data['sku'] ) ) {
			$product->set_sku( sanitize_text_field( (string) $data['sku'] ) );
		}
		if ( ! empty( $data['categoryId'] ) ) {
			$product->set_category_ids( [ (int) $data['categoryId'] ] );
		}
		if ( array_key_exists( 'initialOnHand', $data ) && null !== $data['initialOnHand'] ) {
			$product->set_manage_stock( true );
			$product->set_stock_quantity( (int) $data['initialOnHand'] );
		}
		if ( ! empty( $data['imageId'] ) ) {
			$product->set_image_id( (int) $data['imageId'] );
		}
		if ( array_key_exists( 'brand', $data ) && null !== $data['brand'] ) {
			$product->update_meta_data( 'cb_brand', sanitize_text_field( (string) $data['brand'] ) );
		}
		if ( isset( $data['attributes'] ) && is_array( $data['attributes'] ) ) {
			$product->update_meta_data( 'cb_attributes', wp_json_encode( array_values( $data['attributes'] ) ) );
		}
	}

	/**
	 * @param array<string, mixed> $args
	 */
	private static function apply_sort( array &$args, string $sort ): void {
		switch ( $sort ) {
			case 'price,asc':
				$args['orderby']  = 'meta_value_num';
				$args['meta_key'] = '_price';
				$args['order']    = 'ASC';
				break;
			case 'price,desc':
				$args['orderby']  = 'meta_value_num';
				$args['meta_key'] = '_price';
				$args['order']    = 'DESC';
				break;
			case 'newest':
			case 'createdAt,desc':
				$args['orderby'] = 'date';
				$args['order']   = 'DESC';
				break;
		}
	}

	private static function cat_slug( int $term_id ): string {
		$term = get_term( $term_id, 'product_cat' );
		return ( $term && ! is_wp_error( $term ) ) ? $term->slug : '';
	}

	private static function cat_name( int $term_id ): ?string {
		$term = get_term( $term_id, 'product_cat' );
		return ( $term && ! is_wp_error( $term ) ) ? $term->name : null;
	}

	/**
	 * @return array<string, array<int, string>>
	 */
	private static function variation_options( \WC_Product $product ): array {
		if ( ! $product->is_type( 'variable' ) ) {
			return [];
		}
		$out = [];
		foreach ( $product->get_variation_attributes() as $name => $values ) {
			$out[ wc_attribute_label( $name ) ] = array_values( $values );
		}
		return $out;
	}

	/**
	 * @return array<int, array<string, string>>
	 */
	private static function spec_attributes( \WC_Product $product ): array {
		$out  = [];
		$json = $product->get_meta( 'cb_attributes' );
		if ( $json ) {
			$decoded = json_decode( (string) $json, true );
			if ( is_array( $decoded ) ) {
				foreach ( $decoded as $a ) {
					$out[] = [
						'name'  => (string) ( $a['name'] ?? '' ),
						'value' => (string) ( $a['value'] ?? '' ),
					];
				}
			}
		}
		foreach ( $product->get_attributes() as $attr ) {
			if ( is_a( $attr, 'WC_Product_Attribute' ) && ! $attr->get_variation() ) {
				$out[] = [
					'name'  => wc_attribute_label( $attr->get_name() ),
					'value' => implode( '، ', $attr->get_options() ),
				];
			}
		}
		return $out;
	}

	/**
	 * @return array<int, array<string, mixed>>
	 */
	private static function variants( \WC_Product $product ): array {
		$out = [];
		if ( $product->is_type( 'variable' ) ) {
			foreach ( $product->get_children() as $vid ) {
				$v = WooCommerceAdapter::get_product( $vid );
				if ( ! $v ) {
					continue;
				}
				$options = [];
				foreach ( $v->get_variation_attributes() as $name => $value ) {
					$label             = wc_attribute_label( str_replace( 'attribute_', '', $name ) );
					$options[ $label ] = $value;
				}
				$out[] = [
					'id'              => (int) $v->get_id(),
					'sku'             => $v->get_sku() ?: (string) $v->get_id(),
					'price'           => (float) $v->get_regular_price(),
					'discountedPrice' => self::num_or_null( $v->get_sale_price() ),
					'compareAtPrice'  => null,
					'options'         => (object) $options,
					'availableQty'    => $v->get_manage_stock() ? (int) $v->get_stock_quantity() : ( $v->is_in_stock() ? 999 : 0 ),
					'active'          => 'publish' === $v->get_status(),
				];
			}
		} else {
			$out[] = [
				'id'              => (int) $product->get_id(),
				'sku'             => $product->get_sku() ?: (string) $product->get_id(),
				'price'           => (float) $product->get_regular_price(),
				'discountedPrice' => self::num_or_null( $product->get_sale_price() ),
				'compareAtPrice'  => null,
				'options'         => (object) [],
				'availableQty'    => $product->get_manage_stock() ? (int) $product->get_stock_quantity() : ( $product->is_in_stock() ? 999 : 0 ),
				'active'          => 'publish' === $product->get_status(),
			];
		}
		return $out;
	}

	/**
	 * @return array<string, float|null>
	 */
	private static function price_range( \WC_Product $product ): array {
		if ( $product->is_type( 'variable' ) ) {
			$prices = $product->get_variation_prices( true );
			$reg    = $prices['regular_price'] ?? [];
			$sale   = [];
			foreach ( ( $prices['sale_price'] ?? [] ) as $k => $sp ) {
				if ( isset( $reg[ $k ] ) && (float) $sp < (float) $reg[ $k ] ) {
					$sale[ $k ] = $sp;
				}
			}
			return [
				'minReg'  => $reg ? (float) min( $reg ) : null,
				'maxReg'  => $reg ? (float) max( $reg ) : null,
				'minSale' => $sale ? (float) min( $sale ) : null,
				'maxSale' => $sale ? (float) max( $sale ) : null,
			];
		}

		$reg  = self::num_or_null( $product->get_regular_price() );
		$sale = self::num_or_null( $product->get_sale_price() );
		return [
			'minReg'  => $reg,
			'maxReg'  => $reg,
			'minSale' => $sale,
			'maxSale' => $sale,
		];
	}

	/**
	 * @param mixed $val
	 */
	private static function num_or_null( $val ): ?float {
		return ( null === $val || '' === $val ) ? null : (float) $val;
	}
}

}
