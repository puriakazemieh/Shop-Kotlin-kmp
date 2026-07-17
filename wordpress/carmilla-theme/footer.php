<?php
/**
 * Footer — closes the DC root, renders a light footer, the faithful floating
 * bottom navigation (mobile) and the light/dark toggle script.
 * Mirrors docs/design-reference/*.html.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$current   = untrailingslashit( home_url( add_query_arg( array(), $GLOBALS['wp']->request ?? '' ) ) );
$nav_items = carmilla_bottom_nav_items();
?>
	</div><!-- #content -->

	<!-- ===== FOOTER (desktop) ===== -->
	<div class="mob-hide" style="border-top:1px solid var(--line);background:var(--surface);margin-top:40px;">
		<div style="max-width:1240px;margin:0 auto;display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:28px;padding:36px clamp(14px,4vw,28px);">
			<div>
				<div style="font-weight:800;font-size:18px;color:var(--ink);margin-bottom:8px;"><?php bloginfo( 'name' ); ?></div>
				<div style="font-size:12.5px;color:var(--ink-soft);line-height:1.9;"><?php echo esc_html( get_bloginfo( 'description' ) ); ?></div>
			</div>
			<div>
				<div style="font-weight:700;font-size:13px;color:var(--ink);margin-bottom:12px;">دسترسی سریع</div>
				<div style="display:flex;flex-direction:column;gap:9px;font-size:12.5px;color:var(--ink-soft);">
					<?php
					if ( has_nav_menu( 'footer' ) ) {
						wp_nav_menu( array( 'theme_location' => 'footer', 'container' => false, 'items_wrap' => '%3$s', 'depth' => 1, 'fallback_cb' => false, 'link_before' => '', 'walker' => null ) );
					} else {
						wp_list_pages( array( 'title_li' => '', 'depth' => 1 ) );
					}
					?>
				</div>
			</div>
			<div>
				<div style="font-weight:700;font-size:13px;color:var(--ink);margin-bottom:12px;">خبرنامه</div>
				<div style="font-size:12.5px;color:var(--ink-soft);line-height:1.9;margin-bottom:10px;">برای اطلاع از جدیدترین‌ها عضو شوید.</div>
				<form role="search" method="get" action="<?php echo esc_url( home_url( '/' ) ); ?>" style="display:flex;gap:8px;">
					<input type="email" placeholder="ایمیل شما" style="flex:1;min-width:0;background:var(--surface-2);border:1px solid var(--line);border-radius:11px;padding:10px 13px;color:var(--ink);font-family:inherit;font-size:12.5px;">
				</form>
			</div>
		</div>
		<div style="border-top:1px solid var(--line);text-align:center;font-size:12px;color:var(--ink-soft);padding:16px;">© <?php echo esc_html( carmilla_to_persian_digits( date_i18n( 'Y' ) ) ); ?> <?php bloginfo( 'name' ); ?></div>
	</div>

</div><!-- .dc-root -->

<!-- ===== BOTTOM NAV ===== -->
<div class="bottom-nav" style="position:fixed;left:0;right:0;bottom:14px;display:flex;justify-content:center;z-index:45;pointer-events:none;padding:0 14px;">
	<div style="pointer-events:auto;display:flex;gap:2px;background:var(--surface);border:1px solid var(--line);box-shadow:0 12px 34px rgba(20,25,45,.14);border-radius:22px;padding:7px;max-width:100%;">
		<?php
		foreach ( $nav_items as $item ) {
			$active = ( untrailingslashit( $item['url'] ) === $current );
			$style  = $active
				? 'display:flex;flex-direction:column;align-items:center;gap:3px;padding:8px 15px;border-radius:15px;background:var(--accent-soft);color:var(--accent);'
				: 'display:flex;flex-direction:column;align-items:center;gap:3px;padding:8px 15px;border-radius:15px;color:var(--ink-soft);';
			printf(
				'<a href="%s" style="%s"><span style="display:grid;place-items:center;">%s</span><span style="font-size:10.5px;font-weight:600;">%s</span></a>',
				esc_url( $item['url'] ),
				esc_attr( $style ),
				carmilla_icon( $item['icon'], 22 ), // phpcs:ignore -- trusted inline svg
				esc_html( $item['label'] )
			);
		}
		?>
	</div>
</div>

<script>
/* Light/dark toggle — persists to localStorage under cb_theme (shared with the app boot script). */
window.cbToggleTheme = function () {
	try {
		var el = document.documentElement;
		var cur = el.getAttribute('data-theme');
		if (!cur) { cur = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'; }
		var next = cur === 'dark' ? 'light' : 'dark';
		el.setAttribute('data-theme', next);
		localStorage.setItem('cb_theme', next);
	} catch (e) {}
};
</script>

<?php wp_footer(); ?>
</body>
</html>
