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
		<?php
		// Group available slots by day for a calendar-like layout.
		$by_day = array();
		foreach ( $slots as $s ) {
			$ts  = strtotime( $s );
			$day = $ts ? gmdate( 'Y-m-d', $ts ) : '—';
			$by_day[ $day ][] = array( 'slot' => $s, 'time' => $ts ? gmdate( 'H:i', $ts ) : $s );
		}
		if ( get_post_meta( $id, 'cb_product_slug', true ) ) {
			$credits = carmilla_therapist_credits( $id );
			echo '<p class="t-body-sm"><span class="badge badge--stock">' . esc_html( carmilla_to_persian_digits( $credits ) ) . '</span> ' . esc_html__( 'اعتبار جلسه دارید.', 'carmilla' ) . '</p>';
		}
		?>
		<div id="bk" data-id="<?php echo esc_attr( $id ); ?>">
			<p class="t-body-sm t-muted"><?php esc_html_e( 'یک بازه‌ی زمانی را انتخاب کنید:', 'carmilla' ); ?></p>
			<div id="bk-slots" style="display:grid;gap:var(--sp-md);grid-template-columns:repeat(auto-fill,minmax(160px,1fr))">
				<?php foreach ( $by_day as $day => $times ) : ?>
					<div class="card card--pad">
						<div class="t-title-sm" style="margin-block-end:var(--sp-sm)"><?php echo esc_html( carmilla_to_persian_digits( $day ) ); ?></div>
						<div class="variant-row">
							<?php foreach ( $times as $t ) : ?>
								<button class="chip bk-slot" data-slot="<?php echo esc_attr( $t['slot'] ); ?>"><?php echo esc_html( carmilla_to_persian_digits( $t['time'] ) ); ?></button>
							<?php endforeach; ?>
						</div>
					</div>
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
