<?php
/**
 * Course archive ← CourseListScreen (wide grid of course cards).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
?>
<main class="container container--wide" style="padding-block: var(--sp-xl);">
	<div class="section-head"><h1 class="t-title-lg"><?php esc_html_e( 'دوره‌های آموزشی', 'carmilla' ); ?></h1></div>
	<?php if ( function_exists( 'carmilla_course_filter_bar' ) ) { carmilla_course_filter_bar(); } ?>
	<?php if ( have_posts() ) : ?>
		<div class="grid-adaptive">
			<?php
			$archive_link = get_post_type_archive_link( 'cb_course' );
			while ( have_posts() ) :
				the_post();
				$instructor = get_post_meta( get_the_ID(), 'cb_instructor', true );
				?>
				<article class="card">
					<a href="<?php the_permalink(); ?>" class="thumb"><?php the_post_thumbnail( 'carmilla-card' ); ?></a>
					<div class="card--pad">
						<h3 class="t-title-sm"><a href="<?php the_permalink(); ?>"><?php the_title(); ?></a></h3>
						<?php if ( $instructor ) : ?><div class="meta-row"><span>👤 <a href="<?php echo esc_url( add_query_arg( 'instructor', rawurlencode( $instructor ), $archive_link ) ); ?>"><?php echo esc_html( $instructor ); ?></a></span></div><?php endif; ?>
					</div>
				</article>
			<?php endwhile; ?>
		</div>
		<?php the_posts_pagination( array( 'mid_size' => 1, 'prev_text' => '‹', 'next_text' => '›' ) ); ?>
	<?php else : ?>
		<div class="empty-state"><p class="t-body"><?php esc_html_e( 'هنوز دوره‌ای ثبت نشده.', 'carmilla' ); ?></p></div>
	<?php endif; ?>
</main>
<?php
get_footer();
