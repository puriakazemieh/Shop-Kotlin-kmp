<?php
/**
 * «پیشنهاد شگفت‌انگیز» — the front-page amazing-offers strip as a widget:
 * gradient band, live countdown (Persian digits) and a horizontal scroller of
 * on-sale products. Countdown targets an editable end time; ids are unique per
 * widget instance so several strips can coexist on a page.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class Carmilla_El_Deals extends Carmilla_El_Widget {

	public function get_name() {
		return 'carmilla_deals';
	}

	public function get_title() {
		return __( 'پیشنهاد شگفت‌انگیز', 'carmilla' );
	}

	public function get_icon() {
		return 'eicon-countdown';
	}

	protected function register_controls() {
		$this->start_controls_section( 'cb_content', array( 'label' => __( 'محتوا', 'carmilla' ) ) );
		$this->add_control( 'title', array(
			'label'   => __( 'عنوان', 'carmilla' ),
			'type'    => \Elementor\Controls_Manager::TEXT,
			'default' => 'پیشنهاد شگفت‌انگیز',
		) );
		$this->add_control( 'end_time', array(
			'label'       => __( 'پایان شمارش معکوس', 'carmilla' ),
			'type'        => \Elementor\Controls_Manager::DATE_TIME,
			'description' => __( 'خالی = تا پایان امروز', 'carmilla' ),
		) );
		$this->add_control( 'count', array(
			'label'   => __( 'تعداد محصول', 'carmilla' ),
			'type'    => \Elementor\Controls_Manager::NUMBER,
			'default' => 10,
			'min'     => 1,
			'max'     => 20,
		) );
		$this->add_control( 'more_link', array(
			'label' => __( 'لینک «مشاهده همه»', 'carmilla' ),
			'type'  => \Elementor\Controls_Manager::URL,
		) );
		$this->end_controls_section();
	}

	protected function render() {
		if ( ! class_exists( 'WooCommerce' ) ) {
			echo '<p>' . esc_html__( 'برای این ویجت باید ووکامرس فعال باشد.', 'carmilla' ) . '</p>';
			return;
		}
		$s        = $this->get_settings_for_display();
		$deal_ids = array_slice( array_filter( (array) wc_get_product_ids_on_sale() ), 0, max( 1, (int) $s['count'] ) );
		if ( ! $deal_ids ) {
			echo '<p style="color:var(--ink-soft)">' . esc_html__( 'محصول تخفیف‌داری موجود نیست.', 'carmilla' ) . '</p>';
			return;
		}
		$uid  = 'cbdl' . esc_attr( $this->get_id() );
		$end  = ! empty( $s['end_time'] ) ? strtotime( $s['end_time'] ) : strtotime( 'tomorrow midnight' );
		$more = ! empty( $s['more_link']['url'] )
			? $s['more_link']['url']
			: add_query_arg( 'on_sale', '1', get_permalink( wc_get_page_id( 'shop' ) ) );
		$box  = 'background:rgba(255,255,255,.18);min-width:32px;text-align:center;padding:5px 7px;border-radius:9px;font-weight:700;font-size:14px;';
		?>
		<div style="background:linear-gradient(135deg,var(--accent),var(--accent-2));border-radius:24px;padding:20px clamp(14px,3vw,24px);overflow:hidden;">
			<div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;color:#fff;">
				<div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap;">
					<div style="display:flex;align-items:center;gap:8px;font-weight:800;font-size:clamp(16px,2.5vw,20px);"><span style="font-size:22px;">⚡</span> <?php echo esc_html( $s['title'] ); ?></div>
					<div style="display:flex;align-items:center;gap:5px;direction:ltr;">
						<span id="<?php echo esc_attr( $uid ); ?>-h" style="<?php echo esc_attr( $box ); ?>">۰۰</span><span style="font-weight:800;">:</span>
						<span id="<?php echo esc_attr( $uid ); ?>-m" style="<?php echo esc_attr( $box ); ?>">۰۰</span><span style="font-weight:800;">:</span>
						<span id="<?php echo esc_attr( $uid ); ?>-s" style="<?php echo esc_attr( $box ); ?>">۰۰</span>
					</div>
				</div>
				<a href="<?php echo esc_url( $more ); ?>" style="font-size:12.5px;font-weight:600;opacity:.92;color:#fff;">مشاهده همه ‹</a>
			</div>
			<div class="noscroll" style="display:flex;gap:12px;overflow-x:auto;padding-bottom:4px;">
				<?php foreach ( $deal_ids as $pid ) :
					$p = wc_get_product( $pid );
					if ( ! $p ) {
						continue;
					}
					$reg  = (float) $p->get_regular_price();
					$sale = (float) wc_get_price_to_display( $p );
					$off  = ( $reg > 0 && $sale < $reg ) ? round( ( ( $reg - $sale ) / $reg ) * 100 ) : 0;
					$pimg = get_the_post_thumbnail_url( $pid, 'woocommerce_thumbnail' );
					?>
					<a href="<?php echo esc_url( get_permalink( $pid ) ); ?>" style="flex-shrink:0;width:150px;background:var(--surface);border-radius:16px;padding:10px;">
						<div style="position:relative;aspect-ratio:1;border-radius:11px;overflow:hidden;background:var(--surface-2);margin-bottom:9px;<?php echo $pimg ? "background:url('" . esc_url( $pimg ) . "') center/cover;" : ''; ?>">
							<div style="position:absolute;top:7px;right:7px;background:var(--sale);color:#fff;font-size:11px;font-weight:700;padding:3px 7px;border-radius:8px;"><?php echo esc_html( carmilla_to_persian_digits( $off ) ); ?>٪</div>
						</div>
						<div style="font-size:12px;font-weight:500;color:var(--ink);height:34px;overflow:hidden;line-height:1.5;"><?php echo esc_html( $p->get_name() ); ?></div>
						<div style="display:flex;align-items:center;justify-content:space-between;margin-top:6px;">
							<div>
								<div style="font-size:11px;color:var(--ink-soft);text-decoration:line-through;"><?php echo esc_html( carmilla_dc_num( $reg ) ); ?></div>
								<div style="font-size:13px;font-weight:800;color:var(--ink);"><?php echo esc_html( carmilla_dc_num( $sale ) ); ?></div>
							</div>
							<div style="font-size:9px;color:var(--ink-soft);">تومان</div>
						</div>
					</a>
				<?php endforeach; ?>
			</div>
		</div>
		<script>
		(function () {
			var end = <?php echo (int) $end; ?> * 1000;
			var fa = function (n) { n = String(n).padStart(2, '0'); return n.replace(/\d/g, function (d) { return '۰۱۲۳۴۵۶۷۸۹'[d]; }); };
			function tick() {
				var left = Math.max(0, Math.floor((end - Date.now()) / 1000));
				var h = document.getElementById('<?php echo esc_js( $uid ); ?>-h');
				if (!h) { return; }
				h.textContent = fa(Math.floor(left / 3600));
				document.getElementById('<?php echo esc_js( $uid ); ?>-m').textContent = fa(Math.floor((left % 3600) / 60));
				document.getElementById('<?php echo esc_js( $uid ); ?>-s').textContent = fa(left % 60);
				if (left > 0) { setTimeout(tick, 1000); }
			}
			tick();
		})();
		</script>
		<?php
	}
}
