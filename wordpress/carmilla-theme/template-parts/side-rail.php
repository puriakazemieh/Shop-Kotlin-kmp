<?php
/**
 * Left navigation rail (← SideNavRail). Shown on tablet/desktop (≥600px):
 * compact icons at 600–840, icons + labels at ≥840. Mirrors the mobile
 * bottom bar's destinations so navigation is identical across breakpoints.
 * In RTL it sits on the start (right) side, exactly like the Compose rail.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
$current = home_url( add_query_arg( array(), $GLOBALS['wp']->request ?? '' ) );
?>
<aside class="side-rail" aria-label="<?php esc_attr_e( 'ناوبری اصلی', 'carmilla' ); ?>">
	<a class="side-rail__brand" href="<?php echo esc_url( home_url( '/' ) ); ?>" aria-label="<?php echo esc_attr( get_bloginfo( 'name' ) ); ?>">
		<?php
		if ( has_custom_logo() ) {
			the_custom_logo();
		} else {
			echo '<span class="side-rail__mark">' . esc_html( mb_substr( get_bloginfo( 'name' ), 0, 1 ) ) . '</span>';
			echo '<span class="side-rail__brandname">' . esc_html( get_bloginfo( 'name' ) ) . '</span>';
		}
		?>
	</a>
	<nav class="side-rail__nav">
		<?php
		foreach ( carmilla_bottom_nav_items() as $item ) {
			$active = ( untrailingslashit( $item['url'] ) === untrailingslashit( $current ) ) ? ' is-active' : '';
			printf(
				'<a class="sr-item%s" href="%s"><span class="ic">%s</span><span class="lbl">%s</span></a>',
				esc_attr( $active ),
				esc_url( $item['url'] ),
				carmilla_icon( $item['icon'], 24 ), // phpcs:ignore -- trusted inline svg
				esc_html( $item['label'] )
			);
		}
		?>
	</nav>
</aside>
