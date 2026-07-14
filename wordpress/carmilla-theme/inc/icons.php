<?php
/**
 * Inline SVG icon system (Feather-style line icons, stroke = currentColor) to match
 * the Compose app's vector drawables. Usage: echo carmilla_icon( 'home' );
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

function carmilla_icon_paths() {
	return array(
		'home'      => '<path d="M3 9.5 12 3l9 6.5"/><path d="M5 9.5V21h14V9.5"/><path d="M9 21v-6h6v6"/>',
		'shop'      => '<path d="M3 9h18l-1.5 11.5H4.5L3 9Z"/><path d="M8 9V6a4 4 0 0 1 8 0v3"/>',
		'grid'      => '<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>',
		'blog'      => '<path d="M4 4h16v16H4z"/><path d="M8 8h8M8 12h8M8 16h5"/>',
		'cart'      => '<circle cx="9" cy="20" r="1.5"/><circle cx="18" cy="20" r="1.5"/><path d="M2 3h3l2.5 12.5h11L21 7H6"/>',
		'user'      => '<circle cx="12" cy="8" r="4"/><path d="M4 21c0-4 3.6-6 8-6s8 2 8 6"/>',
		'search'    => '<circle cx="11" cy="11" r="7"/><path d="m20 20-3.2-3.2"/>',
		'heart'     => '<path d="M12 21s-7-4.6-9.3-9C1 9 2.7 5 6.5 5 9 5 12 8 12 8s3-3 5.5-3C21.3 5 23 9 21.3 12 19 16.4 12 21 12 21Z"/>',
		'star'      => '<path d="m12 3 2.7 5.6 6.1.9-4.4 4.3 1 6.1-5.4-2.9-5.4 2.9 1-6.1L3.2 9.5l6.1-.9L12 3Z"/>',
		'clock'     => '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
		'menu'      => '<path d="M3 6h18M3 12h18M3 18h18"/>',
        'close'     => '<path d="M6 6l12 12M18 6 6 18"/>',
		'plus'      => '<path d="M12 5v14M5 12h14"/>',
		'minus'     => '<path d="M5 12h14"/>',
		'check'     => '<path d="m5 12 5 5L20 7"/>',
		'chevron'   => '<path d="m9 6 6 6-6 6"/>',
		'map-pin'   => '<path d="M12 21s7-5.4 7-11a7 7 0 1 0-14 0c0 5.6 7 11 7 11Z"/><circle cx="12" cy="10" r="2.5"/>',
		'play'      => '<path d="M7 4v16l13-8L7 4Z"/>',
		'academy'   => '<path d="M12 4 2 9l10 5 10-5-10-5Z"/><path d="M6 11v5c0 1.5 2.7 3 6 3s6-1.5 6-3v-5"/>',
		'clinic'    => '<path d="M12 21s-7-4.6-9.3-9C1 9 2.7 5 6.5 5 9 5 12 8 12 8s3-3 5.5-3C21.3 5 23 9 21.3 12 19 16.4 12 21 12 21Z"/><path d="M8 12h2l1-2 2 4 1-2h2"/>',
		'test'      => '<rect x="5" y="3" width="14" height="18" rx="2"/><path d="M9 8h6M9 12h6M9 16h4"/>',
		'bell'      => '<path d="M6 9a6 6 0 0 1 12 0c0 5 2 6 2 6H4s2-1 2-6Z"/><path d="M10 20a2 2 0 0 0 4 0"/>',
		'wallet'    => '<rect x="3" y="6" width="18" height="13" rx="2"/><path d="M16 12h3M3 9h14a2 2 0 0 1 0 4H3"/>',
		'filter'    => '<path d="M3 5h18l-7 8v6l-4-2v-4L3 5Z"/>',
		'edit'      => '<path d="M4 20h4L20 8l-4-4L4 16v4Z"/>',
		'trash'     => '<path d="M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13"/>',
		'arrow'     => '<path d="M20 12H4M10 6l-6 6 6 6"/>',
		'compare'   => '<path d="M12 3v18"/><path d="M7 8 3 12l4 4"/><path d="m17 8 4 4-4 4"/>',
	);
}

/**
 * Render an inline SVG icon.
 *
 * @param string $name  Icon key.
 * @param int    $size  Pixel size (width=height).
 * @param string $class Extra CSS classes.
 */
function carmilla_icon( $name, $size = 20, $class = '' ) {
	$paths = carmilla_icon_paths();
	if ( ! isset( $paths[ $name ] ) ) {
		$name = 'grid';
	}
	return sprintf(
		'<svg class="cico %1$s" width="%2$d" height="%2$d" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">%3$s</svg>',
		esc_attr( $class ),
		(int) $size,
		$paths[ $name ] // phpcs:ignore -- static trusted markup
	);
}
