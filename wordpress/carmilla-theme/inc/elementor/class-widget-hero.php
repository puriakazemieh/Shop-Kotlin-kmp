<?php
/**
 * «هیرو کارمیلا» — the front-page hero banner as an Elementor widget
 * (badge, title, subtitle, two CTAs, optional background image).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class Carmilla_El_Hero extends Carmilla_El_Widget {

	public function get_name() {
		return 'carmilla_hero';
	}

	public function get_title() {
		return __( 'هیرو کارمیلا', 'carmilla' );
	}

	public function get_icon() {
		return 'eicon-banner';
	}

	protected function register_controls() {
		$this->start_controls_section( 'cb_content', array( 'label' => __( 'محتوا', 'carmilla' ) ) );
		$this->add_control( 'badge', array(
			'label'   => __( 'نشان بالا', 'carmilla' ),
			'type'    => \Elementor\Controls_Manager::TEXT,
			'default' => 'کالکشن جدید رسید ✦',
		) );
		$this->add_control( 'title', array(
			'label'   => __( 'عنوان', 'carmilla' ),
			'type'    => \Elementor\Controls_Manager::TEXTAREA,
			'default' => "جدیدترین‌ها\nبا تخفیف ویژه",
		) );
		$this->add_control( 'subtitle', array(
			'label'   => __( 'زیرعنوان', 'carmilla' ),
			'type'    => \Elementor\Controls_Manager::TEXTAREA,
			'default' => 'برترین محصولات فصل را با ارسال رایگان سفارش دهید.',
		) );
		$this->add_control( 'btn1_text', array(
			'label'   => __( 'دکمه اصلی', 'carmilla' ),
			'type'    => \Elementor\Controls_Manager::TEXT,
			'default' => 'مشاهده کالکشن',
		) );
		$this->add_control( 'btn1_link', array(
			'label' => __( 'لینک دکمه اصلی', 'carmilla' ),
			'type'  => \Elementor\Controls_Manager::URL,
		) );
		$this->add_control( 'btn2_text', array(
			'label'   => __( 'دکمه دوم (اختیاری)', 'carmilla' ),
			'type'    => \Elementor\Controls_Manager::TEXT,
			'default' => 'پیشنهاد شگفت‌انگیز',
		) );
		$this->add_control( 'btn2_link', array(
			'label' => __( 'لینک دکمه دوم', 'carmilla' ),
			'type'  => \Elementor\Controls_Manager::URL,
		) );
		$this->add_control( 'bg_image', array(
			'label' => __( 'تصویر پس‌زمینه (اختیاری)', 'carmilla' ),
			'type'  => \Elementor\Controls_Manager::MEDIA,
		) );
		$this->end_controls_section();
	}

	protected function render() {
		$s   = $this->get_settings_for_display();
		$img = ! empty( $s['bg_image']['url'] ) ? $s['bg_image']['url'] : '';
		$u1  = ! empty( $s['btn1_link']['url'] ) ? $s['btn1_link']['url'] : '#';
		$u2  = ! empty( $s['btn2_link']['url'] ) ? $s['btn2_link']['url'] : '#';
		?>
		<div style="position:relative;border-radius:24px;overflow:hidden;min-height:clamp(220px,38vw,400px);background:var(--accent);">
			<?php if ( $img ) : ?>
				<div style="position:absolute;inset:0;background:url('<?php echo esc_url( $img ); ?>') center/cover;"></div>
			<?php else : ?>
				<div style="position:absolute;inset:0;background:linear-gradient(135deg,var(--accent),var(--accent-2));"></div>
				<div style="position:absolute;inset:0;background:radial-gradient(120% 100% at 90% 10%,rgba(176,141,87,.35),transparent 60%);"></div>
			<?php endif; ?>
			<div style="position:absolute;inset:0;background:linear-gradient(270deg,rgba(15,18,32,0),rgba(15,18,32,.62));"></div>
			<div style="position:relative;padding:clamp(24px,5vw,52px);max-width:560px;color:#fff;height:100%;display:flex;flex-direction:column;justify-content:center;">
				<?php if ( $s['badge'] ) : ?>
					<div style="display:inline-flex;width:max-content;align-items:center;gap:7px;background:rgba(255,255,255,.16);backdrop-filter:blur(6px);padding:6px 13px;border-radius:30px;font-size:12px;font-weight:600;margin-bottom:16px;"><?php echo esc_html( $s['badge'] ); ?></div>
				<?php endif; ?>
				<h2 style="font-size:clamp(26px,5vw,46px);font-weight:800;line-height:1.18;margin:0 0 12px;letter-spacing:-1px;color:#fff;"><?php echo nl2br( esc_html( $s['title'] ) ); ?></h2>
				<?php if ( $s['subtitle'] ) : ?>
					<p style="font-size:clamp(13px,2vw,16px);opacity:.9;margin:0 0 24px;line-height:1.7;max-width:380px;"><?php echo esc_html( $s['subtitle'] ); ?></p>
				<?php endif; ?>
				<div style="display:flex;gap:12px;flex-wrap:wrap;">
					<?php if ( $s['btn1_text'] ) : ?>
						<a href="<?php echo esc_url( $u1 ); ?>" style="background:#fff;color:var(--accent);font-weight:700;font-size:14px;padding:13px 26px;border-radius:13px;"><?php echo esc_html( $s['btn1_text'] ); ?></a>
					<?php endif; ?>
					<?php if ( $s['btn2_text'] ) : ?>
						<a href="<?php echo esc_url( $u2 ); ?>" style="background:rgba(255,255,255,.14);color:#fff;font-weight:600;font-size:14px;padding:13px 24px;border-radius:13px;border:1px solid rgba(255,255,255,.3);"><?php echo esc_html( $s['btn2_text'] ); ?></a>
					<?php endif; ?>
				</div>
			</div>
		</div>
		<?php
	}
}
