<?php
/**
 * Vertical grids — courses / therapists / psych-tests rendered with the same
 * carmilla_dc_media_card() the archives use, so Elementor pages match the
 * theme's vertical listings exactly.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

abstract class Carmilla_El_Media_Grid extends Carmilla_El_Widget {

	/** @return string CPT slug. */
	abstract protected function post_type();

	/** @return array carmilla_dc_media_card() args for one post. */
	abstract protected function card_args( $post_id );

	protected function register_controls() {
		$this->add_grid_controls( 4 );
		$this->end_controls_section();
	}

	protected function render() {
		if ( ! post_type_exists( $this->post_type() ) || ! function_exists( 'carmilla_dc_media_card' ) ) {
			return;
		}
		$posts = get_posts( array(
			'post_type'        => $this->post_type(),
			'numberposts'      => max( 1, (int) $this->get_settings_for_display( 'count' ) ),
			'post_status'      => 'publish',
			'suppress_filters' => false,
		) );
		if ( ! $posts ) {
			echo '<p style="color:var(--ink-soft)">' . esc_html__( 'موردی یافت نشد.', 'carmilla' ) . '</p>';
			return;
		}
		$this->grid_open( $this->get_settings_for_display( 'columns' ) );
		foreach ( $posts as $p ) {
			carmilla_dc_media_card( $this->card_args( $p->ID ) );
		}
		$this->grid_close();
	}
}

class Carmilla_El_Courses extends Carmilla_El_Media_Grid {

	public function get_name() {
		return 'carmilla_courses';
	}

	public function get_title() {
		return __( 'دوره‌های کارمیلا', 'carmilla' );
	}

	public function get_icon() {
		return 'eicon-play';
	}

	protected function post_type() {
		return 'cb_course';
	}

	protected function card_args( $id ) {
		$price = get_post_meta( $id, 'cb_price', true );
		return array(
			'name'     => get_the_title( $id ),
			'url'      => get_permalink( $id ),
			'image'    => get_the_post_thumbnail_url( $id, 'large' ),
			'subtitle' => get_post_meta( $id, 'cb_instructor', true ),
			'badge'    => get_post_meta( $id, 'cb_level', true ) ?: '',
			'price'    => ( '' !== $price ) ? (float) $price : null,
			'cta'      => 'مشاهده دوره',
			'seed'     => $id,
		);
	}
}

class Carmilla_El_Therapists extends Carmilla_El_Media_Grid {

	public function get_name() {
		return 'carmilla_therapists';
	}

	public function get_title() {
		return __( 'مشاوران کارمیلا', 'carmilla' );
	}

	public function get_icon() {
		return 'eicon-heart-o';
	}

	protected function post_type() {
		return 'cb_therapist';
	}

	protected function card_args( $id ) {
		return array(
			'name'     => get_the_title( $id ),
			'url'      => get_permalink( $id ),
			'image'    => get_the_post_thumbnail_url( $id, 'large' ),
			'subtitle' => get_post_meta( $id, 'cb_specialty', true ),
			'badge'    => '',
			'price'    => null,
			'seed'     => $id,
		);
	}
}

class Carmilla_El_Psychtests extends Carmilla_El_Media_Grid {

	public function get_name() {
		return 'carmilla_psychtests';
	}

	public function get_title() {
		return __( 'تست‌های روان‌شناسی کارمیلا', 'carmilla' );
	}

	public function get_icon() {
		return 'eicon-checkbox';
	}

	protected function post_type() {
		return 'cb_psychtest';
	}

	protected function card_args( $id ) {
		$price = get_post_meta( $id, 'cb_price', true );
		$count = function_exists( 'carmilla_psychtest_questions' ) ? count( carmilla_psychtest_questions( $id ) ) : 0;
		return array(
			'name'     => get_the_title( $id ),
			'url'      => get_permalink( $id ),
			'image'    => get_the_post_thumbnail_url( $id, 'large' ),
			'subtitle' => $count ? carmilla_to_persian_digits( $count ) . ' سؤال' : '',
			'badge'    => '',
			'price'    => ( '' !== $price ) ? (float) $price : null,
			'cta'      => 'شروع تست',
			'seed'     => $id,
		);
	}
}
