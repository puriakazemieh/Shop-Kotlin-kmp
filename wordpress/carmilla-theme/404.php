<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
?>
<main class="container container--readable" style="padding-block: var(--sp-xxl);">
	<div class="empty-state">
		<p class="t-display" style="color:var(--accent)">۴۰۴</p>
		<h1 class="t-title-lg"><?php esc_html_e( 'صفحه پیدا نشد', 'carmilla' ); ?></h1>
		<p class="t-body"><?php esc_html_e( 'آدرس اشتباه است یا صفحه حذف شده.', 'carmilla' ); ?></p>
		<a class="btn btn--primary" href="<?php echo esc_url( home_url( '/' ) ); ?>"><?php esc_html_e( 'بازگشت به خانه', 'carmilla' ); ?></a>
	</div>
</main>
<?php
get_footer();
