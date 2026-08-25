<?php
/**
 * Therapist archive ← TherapistListScreen — DC grid of therapist cards, with the
 * TherapistMatch questionnaire on top.
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
		<h1 style="font-size:clamp(19px,3vw,25px);font-weight:800;margin:0;letter-spacing:-.5px;">مشاوران</h1>
		<div style="font-size:12px;color:var(--ink-soft);margin-top:3px;"><?php echo esc_html( carmilla_to_persian_digits( (int) $wp_query->found_posts ) ); ?> مشاور</div>
	</div>

	<!-- Therapist match: pick a concern → filtered suggestions (JS + REST). -->
	<div id="cb-match" style="background:var(--surface);border:1px solid var(--line);border-radius:18px;padding:18px;margin-bottom:18px;">
		<h2 style="font-size:15px;font-weight:800;margin:0 0 5px;">یافتن مشاور مناسب</h2>
		<p style="font-size:12.5px;color:var(--ink-soft);margin:0 0 12px;line-height:1.8;">موضوع مورد نظرتان را انتخاب کنید تا مناسب‌ترین مشاوران را ببینید.</p>
		<div class="cb-match__chips" id="cb-match-chips" style="display:flex;gap:9px;flex-wrap:wrap;"></div>
		<div id="cb-match-results" style="display:grid;grid-template-columns:repeat(auto-fill,minmax(240px,1fr));gap:14px;margin-top:14px;"></div>
	</div>

	<?php if ( have_posts() ) : ?>
		<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(240px,1fr));gap:clamp(12px,2vw,18px);">
			<?php
			while ( have_posts() ) :
				the_post();
				$id = get_the_ID();
				carmilla_dc_media_card( array(
					'name'     => get_the_title(),
					'url'      => get_permalink(),
					'image'    => get_the_post_thumbnail_url( $id, 'large' ),
					'subtitle' => get_post_meta( $id, 'cb_specialty', true ),
					'badge'    => '',
					'price'    => null,
					'seed'     => $id,
				) );
			endwhile;
			?>
		</div>
		<div style="margin-top:24px;display:flex;justify-content:center;"><?php the_posts_pagination( array( 'mid_size' => 1, 'prev_text' => '‹', 'next_text' => '›' ) ); ?></div>
	<?php else : ?>
		<div style="text-align:center;padding:70px 20px;color:var(--ink-soft);"><div style="font-size:46px;margin-bottom:10px;">🧑‍⚕️</div><div style="font-size:15px;font-weight:600;">هنوز مشاوری ثبت نشده</div></div>
	<?php endif; ?>
</div>
<?php
get_footer();
