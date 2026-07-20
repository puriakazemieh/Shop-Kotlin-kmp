<?php
/**
 * Shared base for all Carmilla Elementor widgets: the «کارمیلا» category plus
 * common controls (count / columns) and the DC grid wrapper used by the theme
 * archives, so widget output is pixel-identical to the theme.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

abstract class Carmilla_El_Widget extends \Elementor\Widget_Base {

	public function get_categories() {
		return array( 'carmilla' );
	}

	/** Standard count + columns controls under a «محتوا» section. */
	protected function add_grid_controls( $default_count = 8 ) {
		$this->start_controls_section( 'cb_content', array( 'label' => __( 'محتوا', 'carmilla' ) ) );
		$this->add_control( 'count', array(
			'label'   => __( 'تعداد', 'carmilla' ),
			'type'    => \Elementor\Controls_Manager::NUMBER,
			'default' => $default_count,
			'min'     => 1,
			'max'     => 24,
		) );
		$this->add_control( 'columns', array(
			'label'   => __( 'ستون‌ها', 'carmilla' ),
			'type'    => \Elementor\Controls_Manager::SELECT,
			'default' => 'auto',
			'options' => array(
				'auto' => __( 'خودکار (ریسپانسیو)', 'carmilla' ),
				'2'    => '۲',
				'3'    => '۳',
				'4'    => '۴',
			),
		) );
	}

	/** DC grid opener matching the theme archives (auto-fill responsive). */
	protected function grid_open( $columns ) {
		$min = array( '2' => '250px', '3' => '210px', '4' => '175px' );
		$tpl = isset( $min[ $columns ] )
			? 'repeat(auto-fill,minmax(' . $min[ $columns ] . ',1fr))'
			: 'repeat(auto-fill,minmax(210px,1fr))';
		echo '<div style="display:grid;grid-template-columns:' . esc_attr( $tpl ) . ';gap:14px;">';
	}

	protected function grid_close() {
		echo '</div>';
	}
}
