<?php
/**
 * Minimal fallback template. Full template hierarchy (front-page, WooCommerce overrides,
 * single-product, blog, and CPT templates for course/therapist/psychtest) is Track A phase 2.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

get_header();
?>
<main class="container" style="padding-block: var(--sp-xl);">
	<?php if ( have_posts() ) : ?>
		<div class="grid-adaptive">
			<?php while ( have_posts() ) : the_post(); ?>
				<article <?php post_class( 'card' ); ?>>
					<?php if ( has_post_thumbnail() ) : ?>
						<a href="<?php the_permalink(); ?>" class="thumb"><?php the_post_thumbnail( 'large' ); ?></a>
					<?php endif; ?>
					<div class="card--pad">
						<h2 class="t-title"><a href="<?php the_permalink(); ?>"><?php the_title(); ?></a></h2>
						<p class="t-body-sm t-muted"><?php echo esc_html( get_the_excerpt() ); ?></p>
					</div>
				</article>
			<?php endwhile; ?>
		</div>
		<?php the_posts_pagination(); ?>
	<?php else : ?>
		<p class="t-body"><?php esc_html_e( 'محتوایی یافت نشد.', 'carmilla' ); ?></p>
	<?php endif; ?>
</main>
<?php
get_footer();
