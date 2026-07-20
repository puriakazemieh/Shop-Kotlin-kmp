<?php
/**
 * «دسته‌بندی‌های کارمیلا» — the product-category tiles from the front page
 * (icon/image, name, hover accent border) as a standalone widget.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class Carmilla_El_Categories extends Carmilla_El_Widget {

	public function get_name() {
		return 'carmilla_categories';
	}

	public function get_title() {
		return __( 'دسته‌بندی‌های کارمیلا', 'carmilla' );
	}

	public function get_icon() {
		return 'eicon-gallery-grid';
	}

	protected function register_controls() {
		$this->start_controls_section( 'cb_content', array( 'label' => __( 'محتوا', 'carmilla' ) ) );
		$this->add_control( 'count', array(
			'label'   => __( 'تعداد', 'carmilla' ),
			'type'    => \Elementor\Controls_Manager::NUMBER,
			'default' => 8,
			'min'     => 1,
			'max'     => 20,
		) );
		$this->end_controls_section();
	}

	protected function render() {
		if ( ! taxonomy_exists( 'product_cat' ) ) {
			echo '<p>' . esc_html__( 'برای این ویجت باید ووکامرس فعال باشد.', 'carmilla' ) . '</p>';
			return;
		}
		$cats = get_terms( array(
			'taxonomy'   => 'product_cat',
			'hide_empty' => true,
			'number'     => max( 1, (int) $this->get_settings_for_display( 'count' ) ),
			'parent'     => 0,
		) );
		if ( is_wp_error( $cats ) || ! $cats ) {
			echo '<p style="color:var(--ink-soft)">' . esc_html__( 'دسته‌ای یافت نشد.', 'carmilla' ) . '</p>';
			return;
		}
		?>
		<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(96px,1fr));gap:12px;">
			<?php foreach ( $cats as $c ) :
				$thumb_id = get_term_meta( $c->term_id, 'thumbnail_id', true );
				$cimg     = $thumb_id ? wp_get_attachment_image_url( $thumb_id, 'thumbnail' ) : '';
				?>
				<a href="<?php echo esc_url( get_term_link( $c ) ); ?>" style="display:flex;flex-direction:column;align-items:center;gap:10px;background:var(--surface);border:1px solid var(--line);border-radius:18px;padding:18px 8px;" onmouseover="this.style.transform='translateY(-3px)';this.style.borderColor='var(--accent)'" onmouseout="this.style.transform='';this.style.borderColor='var(--line)'">
					<div style="width:48px;height:48px;border-radius:14px;background:var(--accent-soft);display:grid;place-items:center;color:var(--accent);overflow:hidden;">
						<?php if ( $cimg ) : ?><img src="<?php echo esc_url( $cimg ); ?>" alt="" style="width:100%;height:100%;object-fit:cover;"><?php else : ?><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M3 9l9-6 9 6v10a2 2 0 01-2 2H5a2 2 0 01-2-2z"/></svg><?php endif; ?>
					</div>
					<span style="font-size:12px;font-weight:600;color:var(--ink);text-align:center;"><?php echo esc_html( $c->name ); ?></span>
				</a>
			<?php endforeach; ?>
		</div>
		<?php
	}
}
