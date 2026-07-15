<?php
/**
 * Search results ← standalone search screen (recent/empty states).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
?>
<main class="container container--wide" style="padding-block: var(--sp-xl);">
	<h1 class="t-title-lg"><?php printf( esc_html__( 'نتایج برای: %s', 'carmilla' ), '<span class="t-muted">' . esc_html( get_search_query() ) . '</span>' ); ?></h1>
	<div style="max-width:var(--content-readable);margin-block:var(--sp-lg)"><?php get_search_form(); ?></div>

	<?php if ( have_posts() ) : ?>
		<div class="grid-adaptive">
			<?php while ( have_posts() ) : the_post(); get_template_part( 'template-parts/card', 'post' ); endwhile; ?>
		</div>
		<?php the_posts_pagination( array( 'mid_size' => 1, 'prev_text' => '‹', 'next_text' => '›' ) ); ?>
	<?php else : ?>
		<div class="empty-state">
			<p class="t-headline" style="color:var(--ink)">😕</p>
			<p class="t-body"><?php esc_html_e( 'چیزی پیدا نشد. عبارت دیگری امتحان کنید.', 'carmilla' ); ?></p>
		</div>
	<?php endif; ?>
</main>
<?php
get_footer();
