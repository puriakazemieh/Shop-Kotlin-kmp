<?php
/**
 * Psychology-test archive ← PsychTestList — DC grid of test cards.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
global $wp_query;
?>
<div style="animation:fadeUp .35s both;padding-top:20px;">
	<div style="margin-bottom:16px;">
		<h1 style="font-size:clamp(19px,3vw,25px);font-weight:800;margin:0;letter-spacing:-.5px;">تست‌های روان‌شناسی</h1>
		<div style="font-size:12px;color:var(--ink-soft);margin-top:3px;"><?php echo esc_html( carmilla_to_persian_digits( (int) $wp_query->found_posts ) ); ?> تست</div>
	</div>

	<?php if ( have_posts() ) : ?>
		<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(240px,1fr));gap:clamp(12px,2vw,18px);">
			<?php
			while ( have_posts() ) :
				the_post();
				$id    = get_the_ID();
				$count = function_exists( 'carmilla_psychtest_questions' ) ? count( carmilla_psychtest_questions( $id ) ) : 0;
				$price = get_post_meta( $id, 'cb_price', true );
				carmilla_dc_media_card( array(
					'name'     => get_the_title(),
					'url'      => get_permalink(),
					'image'    => get_the_post_thumbnail_url( $id, 'large' ),
					'subtitle' => $count ? carmilla_to_persian_digits( $count ) . ' سؤال' : '',
					'badge'    => '',
					'price'    => ( '' !== $price ) ? (float) $price : null,
					'cta'      => 'شروع تست',
					'seed'     => $id,
				) );
			endwhile;
			?>
		</div>
		<div style="margin-top:24px;display:flex;justify-content:center;"><?php the_posts_pagination( array( 'mid_size' => 1, 'prev_text' => '‹', 'next_text' => '›' ) ); ?></div>
	<?php else : ?>
		<div style="text-align:center;padding:70px 20px;color:var(--ink-soft);"><div style="font-size:46px;margin-bottom:10px;">🧠</div><div style="font-size:15px;font-weight:600;">هنوز تستی ثبت نشده</div></div>
	<?php endif; ?>
</div>
<?php
get_footer();
