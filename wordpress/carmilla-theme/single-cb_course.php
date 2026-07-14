<?php
/**
 * Single course ← CourseDetailScreen (readable). Reads meta the Carmilla Bridge
 * plugin sets on cb_course; degrades gracefully when a field is absent.
 * Purchase/enroll CTA links to the linked WooCommerce product (cb_product_slug).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
while ( have_posts() ) :
	the_post();
	$id           = get_the_ID();
	$instructor   = get_post_meta( $id, 'cb_instructor', true );
	$level        = get_post_meta( $id, 'cb_level', true );
	$format       = get_post_meta( $id, 'cb_format', true );
	$duration     = get_post_meta( $id, 'cb_duration', true );
	$product_slug = get_post_meta( $id, 'cb_product_slug', true );
	$cta_url      = $product_slug && function_exists( 'wc_get_page_permalink' ) ? home_url( '/product/' . $product_slug ) : '';
	?>
<main class="container container--readable" style="padding-block: var(--sp-xl);">
	<nav class="breadcrumb">
		<a href="<?php echo esc_url( home_url( '/' ) ); ?>"><?php esc_html_e( 'خانه', 'carmilla' ); ?></a> ›
		<a href="<?php echo esc_url( get_post_type_archive_link( 'cb_course' ) ); ?>"><?php esc_html_e( 'دوره‌ها', 'carmilla' ); ?></a>
	</nav>

	<article>
		<?php if ( has_post_thumbnail() ) : ?>
			<div class="card" style="margin-block-end:var(--sp-lg)"><?php the_post_thumbnail( 'large' ); ?></div>
		<?php endif; ?>

		<h1 class="t-headline"><?php the_title(); ?></h1>
		<div class="meta-row">
			<?php if ( $instructor ) : ?><span>👤 <?php echo esc_html( $instructor ); ?></span><?php endif; ?>
			<?php if ( $level ) : ?><span>📶 <?php echo esc_html( $level ); ?></span><?php endif; ?>
			<?php if ( $format ) : ?><span>🎬 <?php echo esc_html( $format ); ?></span><?php endif; ?>
			<?php if ( $duration ) : ?><span>⏱ <?php echo esc_html( carmilla_to_persian_digits( $duration ) ); ?></span><?php endif; ?>
		</div>

		<div class="entry-content t-body" style="margin-block:var(--sp-lg)">
			<?php the_content(); ?>
		</div>

		<?php if ( $cta_url ) : ?>
			<a class="btn btn--primary" href="<?php echo esc_url( $cta_url ); ?>"><?php esc_html_e( 'ثبت‌نام / خرید دوره', 'carmilla' ); ?></a>
		<?php endif; ?>
	</article>
</main>
	<?php
endwhile;
get_footer();
