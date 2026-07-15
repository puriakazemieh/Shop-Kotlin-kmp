<?php
/**
 * Single therapist ← TherapistDetailScreen (readable): bio, specialty, book CTA.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
while ( have_posts() ) :
	the_post();
	$id           = get_the_ID();
	$specialty    = get_post_meta( $id, 'cb_specialty', true );
	$approach     = get_post_meta( $id, 'cb_approach', true );
	$product_slug = get_post_meta( $id, 'cb_product_slug', true );
	$cta_url      = $product_slug ? home_url( '/product/' . $product_slug ) : '';
	?>
<main class="container container--readable" style="padding-block: var(--sp-xl);">
	<nav class="breadcrumb">
		<a href="<?php echo esc_url( home_url( '/' ) ); ?>"><?php esc_html_e( 'خانه', 'carmilla' ); ?></a> ›
		<a href="<?php echo esc_url( get_post_type_archive_link( 'cb_therapist' ) ); ?>"><?php esc_html_e( 'مشاوران', 'carmilla' ); ?></a>
	</nav>

	<div style="display:flex;gap:var(--sp-lg);align-items:center;margin-block-end:var(--sp-lg)">
		<?php if ( has_post_thumbnail() ) : ?>
			<?php the_post_thumbnail( 'thumbnail', array( 'class' => 'avatar-lg' ) ); ?>
		<?php endif; ?>
		<div>
			<h1 class="t-title-lg" style="margin:0"><?php the_title(); ?></h1>
			<div class="meta-row">
				<?php if ( $specialty ) : ?><span class="badge badge--new"><?php echo esc_html( $specialty ); ?></span><?php endif; ?>
				<?php if ( $approach ) : ?><span><?php echo esc_html( $approach ); ?></span><?php endif; ?>
			</div>
		</div>
	</div>

	<div class="entry-content t-body"><?php the_content(); ?></div>

	<?php
	$accessible = carmilla_therapist_accessible( $id );
	$slots      = $accessible ? carmilla_available_slots( $id ) : array();
	?>
	<h2 class="t-title-lg" style="margin-block:var(--sp-lg) var(--sp-sm)"><?php esc_html_e( 'رزرو نوبت', 'carmilla' ); ?></h2>

	<?php if ( ! $accessible ) : ?>
		<div class="card card--pad">
			<p class="t-body"><?php esc_html_e( 'برای رزرو نوبت، ابتدا اعتبار جلسه را خریداری کنید.', 'carmilla' ); ?></p>
			<?php if ( $cta_url ) : ?><a class="btn btn--primary" href="<?php echo esc_url( $cta_url ); ?>"><?php esc_html_e( 'خرید اعتبار جلسه', 'carmilla' ); ?></a><?php endif; ?>
		</div>
	<?php elseif ( $slots ) : ?>
		<div id="bk" data-id="<?php echo esc_attr( $id ); ?>">
			<p class="t-body-sm t-muted"><?php esc_html_e( 'یک بازه‌ی زمانی را انتخاب کنید:', 'carmilla' ); ?></p>
			<div class="variant-row" id="bk-slots">
				<?php foreach ( $slots as $s ) : ?>
					<button class="chip bk-slot" data-slot="<?php echo esc_attr( $s ); ?>"><?php echo esc_html( carmilla_to_persian_digits( str_replace( 'T', ' ', $s ) ) ); ?></button>
				<?php endforeach; ?>
			</div>
			<div id="bk-result" class="t-body" style="margin-block-start:var(--sp-md)"></div>
		</div>
	<?php else : ?>
		<p class="t-body t-muted"><?php esc_html_e( 'فعلاً بازه‌ی خالی موجود نیست.', 'carmilla' ); ?></p>
	<?php endif; ?>
</main>
	<?php
endwhile;
get_footer();
