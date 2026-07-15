<?php
/**
 * Footer: site footer columns + mobile bottom navigation (app-style bottom bar).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
?>
</div><!-- #content -->

<footer class="site-footer">
	<div class="container container--wide">
		<div class="cols">
			<div>
				<h4 class="brand" style="font-size:18px"><?php bloginfo( 'name' ); ?></h4>
				<p class="t-body-sm t-muted"><?php echo esc_html( get_bloginfo( 'description' ) ); ?></p>
			</div>
			<div>
				<h4><?php esc_html_e( 'دسترسی سریع', 'carmilla' ); ?></h4>
				<?php
				if ( has_nav_menu( 'footer' ) ) {
					wp_nav_menu( array( 'theme_location' => 'footer', 'container' => false, 'menu_class' => '', 'depth' => 1, 'fallback_cb' => false ) );
				} else {
					wp_list_pages( array( 'title_li' => '', 'depth' => 1 ) );
				}
				?>
			</div>
			<div>
				<h4><?php esc_html_e( 'خبرنامه', 'carmilla' ); ?></h4>
				<p class="t-body-sm t-muted"><?php esc_html_e( 'برای اطلاع از جدیدترین‌ها عضو شوید.', 'carmilla' ); ?></p>
				<form class="field" role="search" method="get" action="<?php echo esc_url( home_url( '/' ) ); ?>">
					<input type="email" placeholder="<?php esc_attr_e( 'ایمیل شما', 'carmilla' ); ?>">
				</form>
			</div>
		</div>
		<div class="copy">© <?php echo esc_html( carmilla_to_persian_digits( date_i18n( 'Y' ) ) ); ?> <?php bloginfo( 'name' ); ?></div>
	</div>
</footer>

</div><!-- .app-col -->
</div><!-- .app-shell -->

<nav class="bottom-nav" aria-label="<?php esc_attr_e( 'ناوبری موبایل', 'carmilla' ); ?>">
	<?php
	$current = home_url( add_query_arg( array(), $GLOBALS['wp']->request ?? '' ) );
	foreach ( carmilla_bottom_nav_items() as $item ) {
		$active = ( untrailingslashit( $item['url'] ) === untrailingslashit( $current ) ) ? ' is-active' : '';
		printf(
			'<a class="bn%s" href="%s"><span class="ic">%s</span>%s</a>',
			esc_attr( $active ),
			esc_url( $item['url'] ),
			carmilla_icon( $item['icon'], 22 ), // phpcs:ignore -- trusted inline svg
			esc_html( $item['label'] )
		);
	}
	?>
</nav>

<?php wp_footer(); ?>
</body>
</html>
