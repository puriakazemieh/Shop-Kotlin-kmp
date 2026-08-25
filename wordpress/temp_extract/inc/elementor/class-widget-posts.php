<?php
/**
 * «مقالات کارمیلا» — blog cards using the theme's card-post part
 * («مجلهٔ کارمیلا» look), with optional category filter.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class Carmilla_El_Posts extends Carmilla_El_Widget {

	public function get_name() {
		return 'carmilla_posts';
	}

	public function get_title() {
		return __( 'مقالات کارمیلا', 'carmilla' );
	}

	public function get_icon() {
		return 'eicon-post-list';
	}

	protected function register_controls() {
		$this->add_grid_controls( 3 );
		$cats = array();
		foreach ( (array) get_categories( array( 'hide_empty' => false ) ) as $c ) {
			$cats[ $c->term_id ] = $c->name;
		}
		$this->add_control( 'category', array(
			'label'   => __( 'دسته (اختیاری)', 'carmilla' ),
			'type'    => \Elementor\Controls_Manager::SELECT2,
			'options' => $cats,
		) );
		$this->end_controls_section();
	}

	protected function render() {
		$s    = $this->get_settings_for_display();
		$args = array(
			'post_type'      => 'post',
			'posts_per_page' => max( 1, (int) $s['count'] ),
			'post_status'    => 'publish',
		);
		if ( ! empty( $s['category'] ) ) {
			$args['cat'] = (int) $s['category'];
		}
		$q = new WP_Query( $args );
		if ( ! $q->have_posts() ) {
			echo '<p style="color:var(--ink-soft)">' . esc_html__( 'مقاله‌ای یافت نشد.', 'carmilla' ) . '</p>';
			return;
		}
		$this->grid_open( $s['columns'] );
		while ( $q->have_posts() ) {
			$q->the_post();
			get_template_part( 'template-parts/card-post' );
		}
		wp_reset_postdata();
		$this->grid_close();
	}
}
