<?php
/**
 * «استوری‌های کارمیلا» — the story ring row + fullscreen viewer, identical to
 * the front page. The viewer part is included once per page even if the widget
 * appears several times.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class Carmilla_El_Stories extends Carmilla_El_Widget {

	public function get_name() {
		return 'carmilla_stories';
	}

	public function get_title() {
		return __( 'استوری‌های کارمیلا', 'carmilla' );
	}

	public function get_icon() {
		return 'eicon-carousel';
	}

	protected function register_controls() {
		$this->start_controls_section( 'cb_content', array( 'label' => __( 'محتوا', 'carmilla' ) ) );
		$this->add_control( 'count', array(
			'label'   => __( 'تعداد', 'carmilla' ),
			'type'    => \Elementor\Controls_Manager::NUMBER,
			'default' => 10,
			'min'     => 1,
			'max'     => 20,
		) );
		$this->end_controls_section();
	}

	protected function render() {
		if ( ! post_type_exists( 'cb_story' ) ) {
			echo '<p style="color:var(--ink-soft)">' . esc_html__( 'نوع محتوای استوری فعال نیست.', 'carmilla' ) . '</p>';
			return;
		}
		$stories = get_posts( array(
			'post_type'        => 'cb_story',
			'numberposts'      => max( 1, (int) $this->get_settings_for_display( 'count' ) ),
			'post_status'      => 'publish',
			'suppress_filters' => false,
		) );
		if ( ! $stories ) {
			echo '<p style="color:var(--ink-soft)">' . esc_html__( 'استوری‌ای منتشر نشده است.', 'carmilla' ) . '</p>';
			return;
		}
		$rings = array( 'linear-gradient(45deg,#B08D57,#E7A93B)', 'linear-gradient(45deg,#20305C,#34487E)', 'linear-gradient(45deg,#D8453B,#E7A93B)', 'linear-gradient(45deg,#1F9D6B,#4EA8DE)' );
		?>
		<div class="noscroll" style="display:flex;gap:16px;overflow-x:auto;padding:8px 2px;">
			<?php foreach ( $stories as $i => $s ) :
				$thumb = get_the_post_thumbnail_url( $s->ID, 'thumbnail' ) ?: ( get_post_meta( $s->ID, 'cb_image_url', true ) ?: '' );
				$full  = get_the_post_thumbnail_url( $s->ID, 'large' ) ?: $thumb;
				$ring  = $rings[ $i % count( $rings ) ];
				$title = get_the_title( $s );
				?>
				<button type="button" class="cb-story-open" data-index="<?php echo esc_attr( $i ); ?>" data-image="<?php echo esc_url( $full ); ?>" data-title="<?php echo esc_attr( $title ); ?>" data-content="<?php echo esc_attr( wp_strip_all_tags( $s->post_content ) ); ?>" data-link="<?php echo esc_url( get_post_meta( $s->ID, 'cb_link_url', true ) ); ?>" style="display:flex;flex-direction:column;align-items:center;gap:7px;flex-shrink:0;width:68px;background:none;border:none;cursor:pointer;padding:0;">
					<div style="width:66px;height:66px;border-radius:50%;padding:2.5px;background:<?php echo esc_attr( $ring ); ?>;">
						<div style="width:100%;height:100%;border-radius:50%;border:2px solid var(--surface);overflow:hidden;<?php echo $thumb ? "background:url('" . esc_url( $thumb ) . "') center/cover;" : 'background:var(--surface-2);'; ?>display:grid;place-items:center;color:var(--ink-soft);font-weight:800;font-size:19px;"><?php echo $thumb ? '' : esc_html( mb_substr( $title, 0, 1, 'UTF-8' ) ); ?></div>
					</div>
					<span style="font-size:11px;color:var(--ink);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:64px;"><?php echo esc_html( $title ); ?></span>
				</button>
			<?php endforeach; ?>
		</div>
		<?php
		// The fullscreen viewer once per page (front page may have included it too).
		static $viewer_done = false;
		if ( ! $viewer_done && ! is_front_page() ) {
			$viewer_done = true;
			get_template_part( 'template-parts/story-viewer' );
		}
	}
}
