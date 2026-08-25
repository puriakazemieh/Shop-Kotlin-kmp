<?php
/**
 * Category / tag / date archives ← BlogListScreen (wide grid).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
?>
<main class="container container--wide" style="padding-block: var(--sp-xl);">
	<div class="section-head"><h1 class="t-title-lg"><?php the_archive_title(); ?></h1></div>
	<?php if ( have_posts() ) : ?>
		<div class="grid-adaptive">
			<?php while ( have_posts() ) : the_post(); get_template_part( 'template-parts/card', 'post' ); endwhile; ?>
		</div>
		<?php the_posts_pagination( array( 'mid_size' => 1, 'prev_text' => '‹', 'next_text' => '›' ) ); ?>
	<?php else : ?>
		<div class="empty-state"><p class="t-body"><?php esc_html_e( 'موردی یافت نشد.', 'carmilla' ); ?></p></div>
	<?php endif; ?>
</main>
<?php
get_footer();
