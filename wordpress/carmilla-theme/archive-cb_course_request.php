<?php
/**
 * Course requests ← app's CourseRequest screen. A fully theme-contained,
 * data-driven feature: submit a request + like others', via the theme's own
 * REST routes (inc/rest.php). No plugin involved.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
?>
<main class="container container--medium" style="padding-block: var(--sp-xl);">
	<div class="section-head"><h1 class="t-title-lg"><?php esc_html_e( 'درخواست دوره', 'carmilla' ); ?></h1></div>
	<p class="t-body t-muted"><?php esc_html_e( 'دوره‌ای می‌خواهید که هنوز نیست؟ ثبتش کنید و بقیه لایک کنند تا در اولویت ساخت قرار گیرد.', 'carmilla' ); ?></p>

	<?php if ( is_user_logged_in() ) : ?>
		<form id="cr-form" class="card card--pad" style="margin-block:var(--sp-lg);display:grid;gap:var(--sp-md)">
			<div class="field">
				<label><?php esc_html_e( 'عنوان دوره‌ی پیشنهادی', 'carmilla' ); ?></label>
				<input type="text" id="cr-title" required>
			</div>
			<div class="field">
				<label><?php esc_html_e( 'توضیح (اختیاری)', 'carmilla' ); ?></label>
				<textarea id="cr-desc" rows="3"></textarea>
			</div>
			<div><button type="submit" class="btn btn--primary"><?php esc_html_e( 'ثبت درخواست', 'carmilla' ); ?></button></div>
		</form>
	<?php else : ?>
		<p class="t-body-sm t-muted" style="margin-block:var(--sp-lg)"><?php esc_html_e( 'برای ثبت درخواست وارد شوید.', 'carmilla' ); ?></p>
	<?php endif; ?>

	<div id="cr-list" style="display:grid;gap:var(--sp-md)">
		<?php
		if ( have_posts() ) :
			while ( have_posts() ) :
				the_post();
				$dto = carmilla_course_request_dto( get_post() );
				?>
				<article class="card card--pad" data-id="<?php echo esc_attr( $dto['id'] ); ?>" style="display:flex;align-items:center;justify-content:space-between;gap:var(--sp-md)">
					<div>
						<h3 class="t-title" style="margin:0"><?php echo esc_html( $dto['title'] ); ?></h3>
						<?php if ( $dto['description'] ) : ?><p class="t-body-sm t-muted" style="margin:4px 0 0"><?php echo esc_html( $dto['description'] ); ?></p><?php endif; ?>
					</div>
					<button class="chip cr-like" aria-pressed="<?php echo $dto['liked'] ? 'true' : 'false'; ?>" data-id="<?php echo esc_attr( $dto['id'] ); ?>">
						♥ <span class="cnt"><?php echo esc_html( carmilla_to_persian_digits( $dto['likeCount'] ) ); ?></span>
					</button>
				</article>
			<?php endwhile; ?>
		<?php else : ?>
			<div class="empty-state"><p class="t-body"><?php esc_html_e( 'هنوز درخواستی ثبت نشده. اولین نفر باشید!', 'carmilla' ); ?></p></div>
		<?php endif; ?>
	</div>
</main>
<?php
get_footer();
