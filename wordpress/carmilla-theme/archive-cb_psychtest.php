<?php
/**
 * Psychology-test archive ← PsychTestList (wide grid of test cards).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
?>
<main class="container container--wide" style="padding-block: var(--sp-xl);">
	<div class="section-head"><h1 class="t-title-lg"><?php esc_html_e( 'تست‌های روان‌شناسی', 'carmilla' ); ?></h1></div>
	<?php if ( have_posts() ) : ?>
		<div class="grid-adaptive">
			<?php
			while ( have_posts() ) :
				the_post();
				$count = count( carmilla_psychtest_questions( get_the_ID() ) );
				?>
				<article class="card">
					<?php if ( has_post_thumbnail() ) : ?><a href="<?php the_permalink(); ?>" class="thumb"><?php the_post_thumbnail( 'carmilla-card' ); ?></a><?php endif; ?>
					<div class="card--pad">
						<h3 class="t-title-sm"><a href="<?php the_permalink(); ?>"><?php the_title(); ?></a></h3>
						<?php if ( $count ) : ?><div class="meta-row"><span><?php echo esc_html( carmilla_to_persian_digits( $count ) ); ?> سؤال</span></div><?php endif; ?>
					</div>
				</article>
			<?php endwhile; ?>
		</div>
		<?php the_posts_pagination( array( 'mid_size' => 1, 'prev_text' => '‹', 'next_text' => '›' ) ); ?>
	<?php else : ?>
		<div class="empty-state"><p class="t-body"><?php esc_html_e( 'هنوز تستی ثبت نشده.', 'carmilla' ); ?></p></div>
	<?php endif; ?>
</main>
<?php
get_footer();
