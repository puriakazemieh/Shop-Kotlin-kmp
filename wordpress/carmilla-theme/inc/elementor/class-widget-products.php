<?php
/**
 * «محصولات کارمیلا» — WooCommerce product grid rendered with the theme's own
 * product card (template-parts/card-product.php), with source / category /
 * count / columns controls.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class Carmilla_El_Products extends Carmilla_El_Widget {

	public function get_name() {
		return 'carmilla_products';
	}

	public function get_title() {
		return __( 'محصولات کارمیلا', 'carmilla' );
	}

	public function get_icon() {
		return 'eicon-products';
	}

	protected function register_controls() {
		$this->add_grid_controls( 8 );
		$this->add_control( 'source', array(
			'label'   => __( 'منبع', 'carmilla' ),
			'type'    => \Elementor\Controls_Manager::SELECT,
			'default' => 'latest',
			'options' => array(
				'latest'   => __( 'جدیدترین', 'carmilla' ),
				'sale'     => __( 'حراج / تخفیف‌دار', 'carmilla' ),
				'featured' => __( 'ویژه', 'carmilla' ),
				'category' => __( 'یک دسته‌ی مشخص', 'carmilla' ),
			),
		) );
		$cats = array();
		if ( taxonomy_exists( 'product_cat' ) ) {
			foreach ( (array) get_terms( array( 'taxonomy' => 'product_cat', 'hide_empty' => false ) ) as $t ) {
				if ( isset( $t->term_id ) ) {
					$cats[ $t->term_id ] = $t->name;
				}
			}
		}
		$this->add_control( 'category', array(
			'label'     => __( 'دسته', 'carmilla' ),
			'type'      => \Elementor\Controls_Manager::SELECT2,
			'options'   => $cats,
			'condition' => array( 'source' => 'category' ),
		) );
		$this->end_controls_section();
	}

	protected function render() {
		if ( ! class_exists( 'WooCommerce' ) ) {
			echo '<p>' . esc_html__( 'برای این ویجت باید ووکامرس فعال باشد.', 'carmilla' ) . '</p>';
			return;
		}
		$s    = $this->get_settings_for_display();
		$args = array(
			'status'  => 'publish',
			'limit'   => max( 1, (int) $s['count'] ),
			'orderby' => 'date',
			'order'   => 'DESC',
		);
		if ( 'sale' === $s['source'] ) {
			$ids = array_filter( (array) wc_get_product_ids_on_sale() );
			if ( ! $ids ) {
				echo '<p style="color:var(--ink-soft)">' . esc_html__( 'محصول تخفیف‌داری موجود نیست.', 'carmilla' ) . '</p>';
				return;
			}
			$args['include'] = $ids;
		} elseif ( 'featured' === $s['source'] ) {
			$args['featured'] = true;
		} elseif ( 'category' === $s['source'] && ! empty( $s['category'] ) ) {
			$term = get_term( (int) $s['category'], 'product_cat' );
			if ( $term && ! is_wp_error( $term ) ) {
				$args['category'] = array( $term->slug );
			}
		}

		$products = wc_get_products( $args );
		if ( ! $products ) {
			echo '<p style="color:var(--ink-soft)">' . esc_html__( 'محصولی یافت نشد.', 'carmilla' ) . '</p>';
			return;
		}

		global $product, $post;
		$this->grid_open( $s['columns'] );
		foreach ( $products as $p ) {
			$product = $p;                       // card part reads the loop globals.
			$post    = get_post( $p->get_id() ); // phpcs:ignore WordPress.WP.GlobalVariablesOverride
			setup_postdata( $post );
			get_template_part( 'template-parts/card-product' );
		}
		wp_reset_postdata();
		$this->grid_close();
	}
}
