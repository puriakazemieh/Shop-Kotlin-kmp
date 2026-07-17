<?php
/**
 * Single course ← CourseDetailScreen / CourseLearn — DC styling. Keeps every JS
 * hook intact: #cl-video, #cl-percent, #cl (data-id), .cl-lesson
 * (data-index/data-url/data-playable/cl-done), #cb-quiz, #cb-project.
 *
 * @package Carmilla
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
	$price        = get_post_meta( $id, 'cb_price', true );
	$product_slug = get_post_meta( $id, 'cb_product_slug', true );
	$cta_url      = $product_slug ? home_url( '/product/' . $product_slug ) : '';
	$cover        = get_the_post_thumbnail_url( $id, 'large' );

	$meta_chip = function ( $icon, $text ) {
		if ( ! $text ) { return; }
		echo '<span style="display:inline-flex;align-items:center;gap:5px;font-size:12px;color:var(--ink-soft);background:var(--surface-2);padding:6px 12px;border-radius:9px;">' . $icon . esc_html( $text ) . '</span>'; // phpcs:ignore
	};
	?>
	<div style="animation:fadeUp .35s both;padding-top:18px;max-width:820px;margin:0 auto;">
		<div style="display:flex;align-items:center;gap:8px;margin-bottom:18px;font-size:12px;color:var(--ink-soft);">
			<a href="<?php echo esc_url( home_url( '/' ) ); ?>" style="color:var(--ink-soft);">خانه</a><span>/</span>
			<a href="<?php echo esc_url( get_post_type_archive_link( 'cb_course' ) ); ?>" style="color:var(--ink-soft);">دوره‌ها</a>
		</div>

		<div style="position:relative;border-radius:22px;overflow:hidden;aspect-ratio:2.2;margin-bottom:18px;<?php echo $cover ? "background:url('" . esc_url( $cover ) . "') center/cover;" : 'background:linear-gradient(135deg,var(--accent),var(--accent-2));'; ?>"></div>

		<h1 style="font-size:clamp(21px,3.5vw,30px);font-weight:800;margin:0 0 12px;letter-spacing:-.5px;"><?php the_title(); ?></h1>
		<div style="display:flex;flex-wrap:wrap;gap:8px;margin-bottom:20px;">
			<?php
			$meta_chip( '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 4-6 8-6s8 2 8 6"/></svg>', $instructor );
			$meta_chip( '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M3 12h4l3-8 4 16 3-8h4"/></svg>', $level );
			$meta_chip( '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M8 5v14l11-7z"/></svg>', $format );
			$meta_chip( '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>', $duration ? carmilla_to_persian_digits( $duration ) : '' );
			?>
		</div>

		<div style="font-size:13.5px;color:var(--ink-soft);line-height:1.9;margin-bottom:8px;"><?php the_content(); ?></div>

		<?php
		$syllabus = trim( (string) get_post_meta( $id, 'cb_syllabus', true ) );
		if ( $syllabus ) :
			$items = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $syllabus ) ) );
			if ( $items ) :
				?>
				<h2 style="font-size:clamp(17px,2.5vw,21px);font-weight:800;margin:24px 0 12px;letter-spacing:-.5px;">سرفصل‌ها</h2>
				<div style="background:var(--surface);border:1px solid var(--line);border-radius:16px;overflow:hidden;">
					<?php foreach ( $items as $i => $item ) : ?>
						<div style="display:flex;gap:12px;align-items:center;padding:13px 16px;<?php echo $i ? 'border-top:1px solid var(--line);' : ''; ?>">
							<span style="width:26px;height:26px;border-radius:8px;background:var(--accent-soft);color:var(--accent);font-size:12px;font-weight:800;display:grid;place-items:center;flex-shrink:0;"><?php echo esc_html( carmilla_to_persian_digits( $i + 1 ) ); ?></span>
							<span style="font-size:13.5px;color:var(--ink);"><?php echo esc_html( $item ); ?></span>
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
			<h2 style="font-size:clamp(17px,2.5vw,21px);font-weight:800;margin:24px 0 12px;letter-spacing:-.5px;">درس‌ها</h2>

			<?php if ( $accessible ) : ?>
				<div style="border-radius:16px;overflow:hidden;margin-bottom:10px;">
					<video id="cl-video" controls playsinline style="width:100%;display:block;background:#000;aspect-ratio:16/9;"></video>
				</div>
				<div style="font-size:12.5px;color:var(--ink-soft);margin-bottom:12px;">پیشرفت: <span id="cl-percent"><?php echo esc_html( carmilla_to_persian_digits( carmilla_course_percent( $id ) ) ); ?></span>٪</div>
			<?php endif; ?>

			<div id="cl" data-id="<?php echo esc_attr( $id ); ?>" style="display:flex;flex-direction:column;gap:8px;">
				<?php foreach ( $lessons as $i => $lesson ) :
					$playable = $accessible || $lesson['free'];
					$is_done  = in_array( $i, $done, true );
					?>
					<div class="cl-lesson<?php echo $is_done ? ' cl-done' : ''; ?>" data-index="<?php echo esc_attr( $i ); ?>" data-url="<?php echo esc_attr( $lesson['url'] ); ?>" data-playable="<?php echo $playable ? '1' : '0'; ?>" style="display:flex;align-items:center;justify-content:space-between;gap:12px;background:var(--surface);border:1px solid var(--line);border-radius:14px;padding:13px 15px;cursor:<?php echo $playable ? 'pointer' : 'default'; ?>;">
						<div style="display:flex;align-items:center;gap:12px;min-width:0;">
							<span style="width:28px;height:28px;border-radius:9px;flex-shrink:0;font-size:12px;font-weight:800;display:grid;place-items:center;<?php echo $is_done ? 'background:rgba(31,157,107,.14);color:var(--ok);' : 'background:var(--accent-soft);color:var(--accent);'; ?>"><?php echo $is_done ? '✓' : esc_html( carmilla_to_persian_digits( $i + 1 ) ); ?></span>
							<span style="font-size:13.5px;color:var(--ink);overflow:hidden;text-overflow:ellipsis;white-space:nowrap;"><?php echo esc_html( $lesson['title'] ); ?></span>
						</div>
						<?php if ( $lesson['free'] && ! $accessible ) : ?><span style="font-size:10.5px;font-weight:700;color:var(--gold);background:var(--gold-soft);padding:3px 9px;border-radius:8px;flex-shrink:0;">پیش‌نمایش رایگان</span>
						<?php elseif ( ! $playable ) : ?><span style="color:var(--ink-soft);flex-shrink:0;"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="5" y="11" width="14" height="10" rx="2"/><path d="M8 11V7a4 4 0 018 0v4"/></svg></span><?php endif; ?>
					</div>
				<?php endforeach; ?>
			</div>
		<?php endif; ?>

		<?php
		$has_quiz = function_exists( 'carmilla_course_quiz' ) && carmilla_course_quiz( $id );
		if ( $accessible && ( $has_quiz || is_user_logged_in() ) ) :
			?>
			<?php if ( $has_quiz ) : ?>
				<h2 style="font-size:clamp(17px,2.5vw,21px);font-weight:800;margin:28px 0 12px;letter-spacing:-.5px;">آزمونِ پایانِ دوره</h2>
				<div id="cb-quiz" data-course="<?php echo esc_attr( $id ); ?>"><p style="font-size:12.5px;color:var(--ink-soft);">در حال بارگذاری…</p></div>
			<?php endif; ?>
			<h2 style="font-size:clamp(17px,2.5vw,21px);font-weight:800;margin:28px 0 12px;letter-spacing:-.5px;">پروژه‌ی پایانی و نقدِ همتایان</h2>
			<div id="cb-project" data-course="<?php echo esc_attr( $id ); ?>"><p style="font-size:12.5px;color:var(--ink-soft);">در حال بارگذاری…</p></div>
		<?php endif; ?>

		<?php if ( $cta_url && ! $accessible ) : ?>
			<div style="position:sticky;bottom:20px;margin-top:24px;background:var(--surface);border:1px solid var(--line);border-radius:18px;padding:16px;display:flex;align-items:center;justify-content:space-between;gap:14px;box-shadow:0 12px 30px rgba(20,25,45,.1);">
				<div style="font-size:18px;font-weight:800;color:var(--ink);"><?php echo ( '' !== $price && (float) $price > 0 ) ? esc_html( carmilla_dc_num( $price ) ) . ' <span style="font-size:12px;font-weight:500;color:var(--ink-soft);">تومان</span>' : '<span style="color:var(--ok);">رایگان</span>'; // phpcs:ignore ?></div>
				<a href="<?php echo esc_url( $cta_url ); ?>" style="background:var(--accent);color:#fff;font-weight:700;font-size:14px;padding:14px 26px;border-radius:14px;">ثبت‌نام / خرید دوره</a>
			</div>
		<?php endif; ?>
	</div>
	<?php
endwhile;
get_footer();
