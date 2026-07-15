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

		<?php
		$syllabus = trim( (string) get_post_meta( $id, 'cb_syllabus', true ) );
		if ( $syllabus ) :
			$items = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $syllabus ) ) );
			if ( $items ) :
				?>
				<h2 class="t-title-lg" style="margin-block:var(--sp-lg) var(--sp-sm)"><?php esc_html_e( 'سرفصل‌ها', 'carmilla' ); ?></h2>
				<div class="card">
					<?php foreach ( $items as $i => $item ) : ?>
						<div style="display:flex;gap:var(--sp-md);align-items:center;padding:var(--sp-md);<?php echo $i ? 'border-block-start:1px solid var(--line)' : ''; ?>">
							<span class="badge badge--new"><?php echo esc_html( carmilla_to_persian_digits( $i + 1 ) ); ?></span>
							<span class="t-body" style="margin:0"><?php echo esc_html( $item ); ?></span>
						</div>
					<?php endforeach; ?>
				</div>
			<?php endif; ?>
		<?php endif; ?>

		<?php
		$lessons    = carmilla_course_lessons( $id );
		$accessible = carmilla_course_accessible( $id );
		$done       = carmilla_course_progress( $id );
		if ( $lessons ) :
			?>
			<h2 class="t-title-lg" style="margin-block:var(--sp-lg) var(--sp-sm)"><?php esc_html_e( 'درس‌ها', 'carmilla' ); ?></h2>

			<?php if ( $accessible ) : ?>
				<div class="card" style="margin-block-end:var(--sp-md);overflow:hidden">
					<video id="cl-video" controls playsinline style="width:100%;display:block;background:#000;aspect-ratio:16/9"></video>
				</div>
				<div class="t-body-sm t-muted"><?php esc_html_e( 'پیشرفت:', 'carmilla' ); ?> <span id="cl-percent"><?php echo esc_html( carmilla_to_persian_digits( carmilla_course_percent( $id ) ) ); ?></span>٪</div>
			<?php endif; ?>

			<div id="cl" data-id="<?php echo esc_attr( $id ); ?>" style="margin-block-start:var(--sp-md)">
				<?php foreach ( $lessons as $i => $lesson ) :
					$playable = $accessible || $lesson['free'];
					$is_done  = in_array( $i, $done, true );
					?>
					<div class="card card--pad cl-lesson<?php echo $is_done ? ' cl-done' : ''; ?>" data-index="<?php echo esc_attr( $i ); ?>" data-url="<?php echo esc_attr( $lesson['url'] ); ?>" data-playable="<?php echo $playable ? '1' : '0'; ?>" style="display:flex;align-items:center;justify-content:space-between;gap:var(--sp-md);margin-block-end:8px;cursor:<?php echo $playable ? 'pointer' : 'default'; ?>">
						<div style="display:flex;align-items:center;gap:var(--sp-md)">
							<span class="badge <?php echo $is_done ? 'badge--stock' : 'badge--new'; ?>"><?php echo $is_done ? '✓' : esc_html( carmilla_to_persian_digits( $i + 1 ) ); ?></span>
							<span class="t-body" style="margin:0"><?php echo esc_html( $lesson['title'] ); ?></span>
						</div>
						<?php if ( $lesson['free'] && ! $accessible ) : ?><span class="badge badge--rating"><?php esc_html_e( 'پیش‌نمایش رایگان', 'carmilla' ); ?></span>
						<?php elseif ( ! $playable ) : ?><span class="cico" style="color:var(--ink-soft)">🔒</span><?php endif; ?>
					</div>
				<?php endforeach; ?>
			</div>
		<?php endif; ?>

		<?php if ( $cta_url && ! $accessible ) : ?>
			<a class="btn btn--primary" href="<?php echo esc_url( $cta_url ); ?>" style="margin-block-start:var(--sp-lg)"><?php esc_html_e( 'ثبت‌نام / خرید دوره', 'carmilla' ); ?></a>
		<?php endif; ?>
	</article>
</main>
	<?php
endwhile;
get_footer();
