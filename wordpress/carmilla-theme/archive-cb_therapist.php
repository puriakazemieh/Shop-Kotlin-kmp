<?php
/**
 * Therapist archive ← TherapistListScreen + TherapistMatch questionnaire on top.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
?>
<main class="container container--wide" style="padding-block: var(--sp-xl);">
	<div class="section-head"><h1 class="t-title-lg"><?php esc_html_e( 'مشاوران', 'carmilla' ); ?></h1></div>

	<!-- Therapist match: pick a concern → filtered suggestions (JS + REST). -->
	<section class="card card--pad cb-match" id="cb-match" style="margin-block-end:var(--sp-lg)">
		<h2 class="t-title-sm"><?php esc_html_e( 'یافتن مشاور مناسب', 'carmilla' ); ?></h2>
		<p class="t-body-sm t-muted"><?php esc_html_e( 'موضوع مورد نظرتان را انتخاب کنید تا مناسب‌ترین مشاوران را ببینید.', 'carmilla' ); ?></p>
		<div class="cb-match__chips" id="cb-match-chips"></div>
		<div class="grid-adaptive" id="cb-match-results" style="margin-block-start:var(--sp-md)"></div>
	</section>

	<?php if ( have_posts() ) : ?>
		<div class="grid-adaptive">
			<?php
			while ( have_posts() ) :
				the_post();
				$specialty = get_post_meta( get_the_ID(), 'cb_specialty', true );
				?>
				<article class="card">
					<a href="<?php the_permalink(); ?>" class="thumb"><?php the_post_thumbnail( 'carmilla-card' ); ?></a>
					<div class="card--pad">
						<h3 class="t-title-sm"><a href="<?php the_permalink(); ?>"><?php the_title(); ?></a></h3>
						<?php if ( $specialty ) : ?><div class="meta-row"><span class="badge badge--new"><?php echo esc_html( $specialty ); ?></span></div><?php endif; ?>
					</div>
				</article>
			<?php endwhile; ?>
		</div>
		<?php the_posts_pagination( array( 'mid_size' => 1, 'prev_text' => '‹', 'next_text' => '›' ) ); ?>
	<?php else : ?>
		<div class="empty-state"><p class="t-body"><?php esc_html_e( 'هنوز مشاوری ثبت نشده.', 'carmilla' ); ?></p></div>
	<?php endif; ?>
</main>
<?php
get_footer();
