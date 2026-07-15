<?php
/**
 * Blog post card (used in blog archive + home teaser). Expects the loop post.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
?>
<article <?php post_class( 'card' ); ?>>
	<?php if ( has_post_thumbnail() ) : ?>
		<a href="<?php the_permalink(); ?>" class="thumb" aria-hidden="true"><?php the_post_thumbnail( 'carmilla-card' ); ?></a>
	<?php endif; ?>
	<div class="card--pad">
		<?php
		$cats = get_the_category();
		if ( ! empty( $cats ) ) :
			?>
			<span class="badge badge--new"><?php echo esc_html( $cats[0]->name ); ?></span>
		<?php endif; ?>
		<h3 class="t-title" style="margin-block-start:8px"><a href="<?php the_permalink(); ?>"><?php the_title(); ?></a></h3>
		<p class="t-body-sm t-muted"><?php echo esc_html( wp_trim_words( get_the_excerpt(), 18 ) ); ?></p>
		<div class="meta-row">
			<span><?php echo esc_html( carmilla_to_persian_digits( get_the_date() ) ); ?></span>
		</div>
	</div>
</article>
