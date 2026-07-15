<?php
/**
 * Blog archive / fallback ← BlogListScreen (wide grid of post cards).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
?>
<main class="container container--wide" style="padding-block: var(--sp-xl);">
	<?php if ( is_home() && ! is_front_page() ) : ?>
		<h1 class="t-title-lg"><?php echo esc_html( get_the_title( get_option( 'page_for_posts' ) ) ?: __( 'مجله', 'carmilla' ) ); ?></h1>
	<?php endif; ?>

	<?php if ( have_posts() ) : ?>
		<div class="grid-adaptive">
			<?php
			while ( have_posts() ) :
				the_post();
				get_template_part( 'template-parts/card', 'post' );
			endwhile;
			?>
		</div>
		<?php the_posts_pagination( array( 'mid_size' => 1, 'prev_text' => '‹', 'next_text' => '›' ) ); ?>
	<?php else : ?>
		<div class="empty-state">
			<p class="t-body"><?php esc_html_e( 'محتوایی یافت نشد.', 'carmilla' ); ?></p>
		</div>
	<?php endif; ?>
</main>
<?php
get_footer();
