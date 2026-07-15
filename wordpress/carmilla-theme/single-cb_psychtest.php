<?php
/**
 * Single psychology test ← PsychTest detail + TakeTest. Data-driven, theme-only:
 * questions render as radio groups (index-based, no scores exposed); submitting
 * posts to the theme REST route which computes score + interpretation.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
while ( have_posts() ) :
	the_post();
	$id           = get_the_ID();
	$questions    = carmilla_psychtest_questions( $id );
	$accessible   = carmilla_psychtest_accessible( $id );
	$product_slug = get_post_meta( $id, 'cb_product_slug', true );
	?>
<main class="container container--readable" style="padding-block: var(--sp-xl);">
	<nav class="breadcrumb">
		<a href="<?php echo esc_url( home_url( '/' ) ); ?>"><?php esc_html_e( 'خانه', 'carmilla' ); ?></a> ›
		<a href="<?php echo esc_url( get_post_type_archive_link( 'cb_psychtest' ) ); ?>"><?php esc_html_e( 'تست‌های روان‌شناسی', 'carmilla' ); ?></a>
	</nav>

	<h1 class="t-headline"><?php the_title(); ?></h1>
	<?php if ( $questions ) : ?>
		<div class="meta-row"><span><?php echo esc_html( carmilla_to_persian_digits( count( $questions ) ) ); ?> <?php esc_html_e( 'سؤال', 'carmilla' ); ?></span></div>
	<?php endif; ?>

	<div class="entry-content t-body" style="margin-block:var(--sp-lg)"><?php the_content(); ?></div>

	<?php if ( ! $accessible ) : ?>
		<div class="card card--pad">
			<p class="t-body"><?php esc_html_e( 'برای انجام این تست، ابتدا آن را خریداری کنید.', 'carmilla' ); ?></p>
			<?php if ( $product_slug ) : ?>
				<a class="btn btn--primary" href="<?php echo esc_url( home_url( '/product/' . $product_slug ) ); ?>"><?php esc_html_e( 'خرید تست', 'carmilla' ); ?></a>
			<?php endif; ?>
		</div>
	<?php elseif ( $questions ) : ?>
		<form id="pt-form" data-id="<?php echo esc_attr( $id ); ?>">
			<?php foreach ( $questions as $qi => $q ) : ?>
				<div class="card card--pad" style="margin-block-end:var(--sp-md)">
					<p class="t-title" style="margin-block:0 var(--sp-sm)"><?php echo esc_html( carmilla_to_persian_digits( $qi + 1 ) . '. ' . $q['text'] ); ?></p>
					<div style="display:grid;gap:8px">
						<?php foreach ( $q['options'] as $oi => $opt ) : ?>
							<label style="display:flex;gap:8px;align-items:center;cursor:pointer">
								<input type="radio" name="q<?php echo esc_attr( $qi ); ?>" value="<?php echo esc_attr( $oi ); ?>">
								<span class="t-body" style="margin:0"><?php echo esc_html( $opt['label'] ); ?></span>
							</label>
						<?php endforeach; ?>
					</div>
				</div>
			<?php endforeach; ?>
			<button type="submit" class="btn btn--primary"><?php esc_html_e( 'مشاهده نتیجه', 'carmilla' ); ?></button>
		</form>
		<div id="pt-result" class="card card--pad" style="display:none;margin-block-start:var(--sp-lg)"></div>
	<?php else : ?>
		<p class="t-body t-muted"><?php esc_html_e( 'سؤالی برای این تست ثبت نشده است.', 'carmilla' ); ?></p>
	<?php endif; ?>
</main>
	<?php
endwhile;
get_footer();
