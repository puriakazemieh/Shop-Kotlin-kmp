<?php
/**
 * Single psychology test ← PsychTest detail (readable): description + buy CTA.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
while ( have_posts() ) :
	the_post();
	$id           = get_the_ID();
	$q_count      = get_post_meta( $id, 'cb_question_count', true );
	$product_slug = get_post_meta( $id, 'cb_product_slug', true );
	$cta_url      = $product_slug ? home_url( '/product/' . $product_slug ) : '';
	?>
<main class="container container--readable" style="padding-block: var(--sp-xl);">
	<nav class="breadcrumb">
		<a href="<?php echo esc_url( home_url( '/' ) ); ?>"><?php esc_html_e( 'خانه', 'carmilla' ); ?></a> ›
		<a href="<?php echo esc_url( get_post_type_archive_link( 'cb_psychtest' ) ); ?>"><?php esc_html_e( 'تست‌های روان‌شناسی', 'carmilla' ); ?></a>
	</nav>

	<h1 class="t-headline"><?php the_title(); ?></h1>
	<?php if ( $q_count ) : ?>
		<div class="meta-row"><span>📝 <?php echo esc_html( carmilla_to_persian_digits( $q_count ) ); ?> <?php esc_html_e( 'سؤال', 'carmilla' ); ?></span></div>
	<?php endif; ?>

	<div class="entry-content t-body" style="margin-block:var(--sp-lg)"><?php the_content(); ?></div>

	<?php if ( $cta_url ) : ?>
		<a class="btn btn--primary" href="<?php echo esc_url( $cta_url ); ?>"><?php esc_html_e( 'خرید و شروع تست', 'carmilla' ); ?></a>
	<?php endif; ?>
</main>
	<?php
endwhile;
get_footer();
