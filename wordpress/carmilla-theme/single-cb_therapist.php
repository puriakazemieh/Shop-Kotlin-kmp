<?php
/**
 * Single therapist ← TherapistDetailScreen — DC styling; keeps the booking hooks
 * (#bk / #bk-slots / .bk-slot / #bk-result) intact for booking.js.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
while ( have_posts() ) :
	the_post();
	$id           = get_the_ID();
	$specialty    = get_post_meta( $id, 'cb_specialty', true );
	$approach     = get_post_meta( $id, 'cb_approach', true );
	$product_slug = get_post_meta( $id, 'cb_product_slug', true );
	$cta_url      = $product_slug ? home_url( '/product/' . $product_slug ) : '';
	$avatar       = get_the_post_thumbnail_url( $id, 'medium' );
	?>
	<div style="animation:fadeUp .35s both;padding-top:18px;max-width:760px;margin:0 auto;">
		<div style="display:flex;align-items:center;gap:8px;margin-bottom:18px;font-size:12px;color:var(--ink-soft);">
			<a href="<?php echo esc_url( home_url( '/' ) ); ?>" style="color:var(--ink-soft);">خانه</a><span>/</span>
			<a href="<?php echo esc_url( get_post_type_archive_link( 'cb_therapist' ) ); ?>" style="color:var(--ink-soft);">مشاوران</a>
		</div>

		<div style="display:flex;gap:16px;align-items:center;margin-bottom:20px;">
			<div style="width:84px;height:84px;border-radius:22px;flex-shrink:0;<?php echo $avatar ? "background:url('" . esc_url( $avatar ) . "') center/cover;" : 'background:var(--accent-soft);'; ?>display:grid;place-items:center;color:var(--accent);"><?php if ( ! $avatar ) : ?><svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 4-6 8-6s8 2 8 6"/></svg><?php endif; ?></div>
			<div>
				<h1 style="font-size:clamp(20px,3vw,26px);font-weight:800;margin:0 0 8px;letter-spacing:-.5px;"><?php the_title(); ?></h1>
				<div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;">
					<?php if ( $specialty ) : ?><span style="font-size:11.5px;font-weight:700;color:var(--accent);background:var(--accent-soft);padding:4px 11px;border-radius:9px;"><?php echo esc_html( $specialty ); ?></span><?php endif; ?>
					<?php if ( $approach ) : ?><span style="font-size:12.5px;color:var(--ink-soft);"><?php echo esc_html( $approach ); ?></span><?php endif; ?>
				</div>
			</div>
		</div>

		<div style="font-size:13.5px;color:var(--ink-soft);line-height:1.9;margin-bottom:24px;"><?php the_content(); ?></div>

		<?php
		$accessible = carmilla_therapist_accessible( $id );
		$slots      = $accessible ? carmilla_available_slots( $id ) : array();
		?>
		<h2 style="font-size:clamp(17px,2.5vw,21px);font-weight:800;margin:0 0 14px;letter-spacing:-.5px;">رزرو نوبت</h2>

		<?php if ( ! $accessible ) : ?>
			<div style="background:var(--surface);border:1px solid var(--line);border-radius:18px;padding:18px;">
				<p style="font-size:13.5px;color:var(--ink-soft);line-height:1.9;margin:0 0 14px;">برای رزرو نوبت، ابتدا اعتبار جلسه را خریداری کنید.</p>
				<?php if ( $cta_url ) : ?><a href="<?php echo esc_url( $cta_url ); ?>" style="display:inline-block;background:var(--accent);color:#fff;font-weight:700;font-size:14px;padding:13px 24px;border-radius:13px;">خرید اعتبار جلسه</a><?php endif; ?>
			</div>
		<?php elseif ( $slots ) : ?>
			<?php
			$by_day = array();
			foreach ( $slots as $s ) {
				$ts  = strtotime( $s );
				$day = $ts ? gmdate( 'Y-m-d', $ts ) : '—';
				$by_day[ $day ][] = array( 'slot' => $s, 'time' => $ts ? gmdate( 'H:i', $ts ) : $s );
			}
			if ( $product_slug ) {
				$credits = carmilla_therapist_credits( $id );
				echo '<p style="font-size:12.5px;color:var(--ink-soft);margin:0 0 12px;"><span style="font-weight:700;color:var(--ok);background:rgba(31,157,107,.12);padding:3px 9px;border-radius:8px;">' . esc_html( carmilla_to_persian_digits( $credits ) ) . '</span> اعتبار جلسه دارید.</p>';
			}
			?>
			<div id="bk" data-id="<?php echo esc_attr( $id ); ?>">
				<p style="font-size:12.5px;color:var(--ink-soft);margin:0 0 12px;">یک بازه‌ی زمانی را انتخاب کنید:</p>
				<div id="bk-slots" style="display:grid;gap:12px;grid-template-columns:repeat(auto-fill,minmax(170px,1fr));">
					<?php foreach ( $by_day as $day => $times ) : ?>
						<div style="background:var(--surface);border:1px solid var(--line);border-radius:16px;padding:14px;">
							<div style="font-size:13px;font-weight:700;margin-bottom:10px;"><?php echo esc_html( carmilla_to_persian_digits( $day ) ); ?></div>
							<div style="display:flex;gap:8px;flex-wrap:wrap;">
								<?php foreach ( $times as $t ) : ?>
									<button class="chip bk-slot" data-slot="<?php echo esc_attr( $t['slot'] ); ?>" style="background:var(--accent-soft);color:var(--accent);border:1px solid var(--line);border-radius:10px;padding:8px 13px;font-size:12.5px;font-weight:700;font-family:inherit;cursor:pointer;"><?php echo esc_html( carmilla_to_persian_digits( $t['time'] ) ); ?></button>
								<?php endforeach; ?>
							</div>
						</div>
					<?php endforeach; ?>
				</div>
				<div id="bk-result" style="font-size:13px;margin-top:14px;"></div>
			</div>
		<?php else : ?>
			<p style="font-size:13.5px;color:var(--ink-soft);">فعلاً بازه‌ی خالی موجود نیست.</p>
		<?php endif; ?>
	</div>
	<?php
endwhile;
get_footer();
