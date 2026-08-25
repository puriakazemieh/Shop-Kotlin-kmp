<?php
/**
 * Single psychology test ← PsychTest detail + TakeTest — DC styling; keeps the
 * test hooks (#pt-form / #pt-result / q<i> radios / data-id) intact for the
 * server-scored submit. No scores are exposed to the client.
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
	$questions    = carmilla_psychtest_questions( $id );
	$accessible   = carmilla_psychtest_accessible( $id );
	$product_slug = get_post_meta( $id, 'cb_product_slug', true );
	?>
	<div style="animation:fadeUp .35s both;padding-top:18px;max-width:720px;margin:0 auto;">
		<div style="display:flex;align-items:center;gap:8px;margin-bottom:18px;font-size:12px;color:var(--ink-soft);">
			<a href="<?php echo esc_url( home_url( '/' ) ); ?>" style="color:var(--ink-soft);">خانه</a><span>/</span>
			<a href="<?php echo esc_url( get_post_type_archive_link( 'cb_psychtest' ) ); ?>" style="color:var(--ink-soft);">تست‌های روان‌شناسی</a>
		</div>

		<h1 style="font-size:clamp(21px,3.5vw,30px);font-weight:800;margin:0 0 10px;letter-spacing:-.5px;"><?php the_title(); ?></h1>
		<?php if ( $questions ) : ?>
			<div style="display:inline-flex;align-items:center;gap:6px;font-size:12.5px;color:var(--ink-soft);background:var(--surface-2);padding:5px 12px;border-radius:9px;margin-bottom:16px;"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M9 11l3 3 8-8 M20 12v6a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2h9"/></svg><?php echo esc_html( carmilla_to_persian_digits( count( $questions ) ) ); ?> سؤال</div>
		<?php endif; ?>

		<div style="font-size:13.5px;color:var(--ink-soft);line-height:1.9;margin-bottom:22px;"><?php the_content(); ?></div>

		<?php if ( ! $accessible ) : ?>
			<div style="background:var(--surface);border:1px solid var(--line);border-radius:18px;padding:18px;">
				<p style="font-size:13.5px;color:var(--ink-soft);line-height:1.9;margin:0 0 14px;">برای انجام این تست، ابتدا آن را خریداری کنید.</p>
				<?php if ( $product_slug ) : ?><a href="<?php echo esc_url( home_url( '/product/' . $product_slug ) ); ?>" style="display:inline-block;background:var(--accent);color:#fff;font-weight:700;font-size:14px;padding:13px 24px;border-radius:13px;">خرید تست</a><?php endif; ?>
			</div>
		<?php elseif ( $questions ) : ?>
			<form id="pt-form" data-id="<?php echo esc_attr( $id ); ?>" style="display:flex;flex-direction:column;gap:12px;">
				<?php foreach ( $questions as $qi => $q ) : ?>
					<div style="background:var(--surface);border:1px solid var(--line);border-radius:16px;padding:16px;">
						<p style="font-size:14px;font-weight:700;margin:0 0 12px;line-height:1.7;"><?php echo esc_html( carmilla_to_persian_digits( $qi + 1 ) . '. ' . $q['text'] ); ?></p>
						<div style="display:flex;flex-direction:column;gap:8px;">
							<?php foreach ( $q['options'] as $oi => $opt ) : ?>
								<label style="display:flex;gap:9px;align-items:center;cursor:pointer;background:var(--surface-2);border:1px solid var(--line);border-radius:11px;padding:11px 13px;">
									<input type="radio" name="q<?php echo esc_attr( $qi ); ?>" value="<?php echo esc_attr( $oi ); ?>" style="accent-color:var(--accent);">
									<span style="font-size:13px;color:var(--ink);"><?php echo esc_html( $opt['label'] ); ?></span>
								</label>
							<?php endforeach; ?>
						</div>
					</div>
				<?php endforeach; ?>
				<button type="submit" style="background:var(--accent);color:#fff;font-weight:700;font-size:15px;padding:15px;border-radius:14px;border:none;cursor:pointer;font-family:inherit;">مشاهده نتیجه</button>
			</form>
			<div id="pt-result" style="display:none;background:var(--surface);border:1px solid var(--line);border-radius:18px;padding:18px;margin-top:18px;"></div>
		<?php else : ?>
			<p style="font-size:13.5px;color:var(--ink-soft);">سؤالی برای این تست ثبت نشده است.</p>
		<?php endif; ?>
	</div>
	<?php
endwhile;
get_footer();
