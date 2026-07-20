<?php
/**
 * «دکمه کارمیلا» — brand button in the theme's four styles
 * (accent / ghost / soft / gold), so Elementor pages keep the design language.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class Carmilla_El_Button extends Carmilla_El_Widget {

	public function get_name() {
		return 'carmilla_button';
	}

	public function get_title() {
		return __( 'دکمه کارمیلا', 'carmilla' );
	}

	public function get_icon() {
		return 'eicon-button';
	}

	protected function register_controls() {
		$this->start_controls_section( 'cb_content', array( 'label' => __( 'محتوا', 'carmilla' ) ) );
		$this->add_control( 'text', array(
			'label'   => __( 'متن', 'carmilla' ),
			'type'    => \Elementor\Controls_Manager::TEXT,
			'default' => 'مشاهده بیشتر',
		) );
		$this->add_control( 'link', array(
			'label' => __( 'لینک', 'carmilla' ),
			'type'  => \Elementor\Controls_Manager::URL,
		) );
		$this->add_control( 'style', array(
			'label'   => __( 'سبک', 'carmilla' ),
			'type'    => \Elementor\Controls_Manager::SELECT,
			'default' => 'primary',
			'options' => array(
				'primary' => __( 'اصلی (سرمه‌ای)', 'carmilla' ),
				'soft'    => __( 'ملایم', 'carmilla' ),
				'ghost'   => __( 'خطی', 'carmilla' ),
				'gold'    => __( 'طلایی', 'carmilla' ),
			),
		) );
		$this->add_control( 'align', array(
			'label'   => __( 'چینش', 'carmilla' ),
			'type'    => \Elementor\Controls_Manager::SELECT,
			'default' => 'start',
			'options' => array(
				'start'  => __( 'راست', 'carmilla' ),
				'center' => __( 'وسط', 'carmilla' ),
				'end'    => __( 'چپ', 'carmilla' ),
			),
		) );
		$this->end_controls_section();
	}

	protected function render() {
		$s      = $this->get_settings_for_display();
		$url    = ! empty( $s['link']['url'] ) ? $s['link']['url'] : '#';
		$styles = array(
			'primary' => 'background:var(--accent);color:#fff;',
			'soft'    => 'background:var(--accent-soft);color:var(--accent);',
			'ghost'   => 'background:transparent;color:var(--accent);border:1.5px solid var(--accent);',
			'gold'    => 'background:var(--gold);color:#fff;',
		);
		$style  = $styles[ $s['style'] ] ?? $styles['primary'];
		echo '<div style="display:flex;justify-content:' . esc_attr( $s['align'] ) . ';">';
		echo '<a href="' . esc_url( $url ) . '" style="' . esc_attr( $style ) . 'font-weight:700;font-size:14px;padding:13px 26px;border-radius:13px;display:inline-flex;align-items:center;gap:8px;">' . esc_html( $s['text'] ) . '</a>';
		echo '</div>';
	}
}
