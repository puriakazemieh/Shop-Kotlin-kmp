<?php
/**
 * Theme helper functions (price formatting, nav fallbacks, small view helpers).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/**
 * Format a number as Persian-grouped Toman, mirroring core/designSystem/util/PriceFormat.kt
 * (formatToman): thousands separator "٬", Persian digits, no decimals. Unit added by caller.
 */
function carmilla_format_number( $value ) {
	$n = (int) round( (float) $value );
	$neg = $n < 0;
	$s   = (string) abs( $n );

	// Group thousands with the Arabic thousands separator U+066C.
	$out = '';
	$len = strlen( $s );
	for ( $i = 0; $i < $len; $i++ ) {
		if ( $i > 0 && ( $len - $i ) % 3 === 0 ) {
			$out .= '٬';
		}
		$out .= $s[ $i ];
	}
	$out = carmilla_to_persian_digits( $out );
	return $neg ? '−' . $out : $out;
}

/** Full price label with the «تومان» unit. */
function carmilla_price( $value ) {
	return carmilla_format_number( $value ) . ' تومان';
}

/** Convert ASCII digits to Persian digits. */
function carmilla_to_persian_digits( $str ) {
	$en = array( '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' );
	$fa = array( '۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹' );
	return str_replace( $en, $fa, (string) $str );
}

/** Persian/Arabic-Indic digits → ASCII (e.g. for tel: links). */
function carmilla_to_english_digits( $str ) {
	$fa = array( '۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹', '٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩' );
	$en = array( '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' );
	return str_replace( $fa, $en, (string) $str );
}

/**
 * Primary-menu fallback: a simple list of pages when no menu is assigned.
 */
function carmilla_primary_menu_fallback() {
	echo '<ul class="primary-menu">';
	wp_list_pages( array( 'title_li' => '', 'depth' => 1 ) );
	echo '</ul>';
}

/**
 * Bottom-bar destinations (mobile), echoing the app's bottom navigation.
 * Filterable so a child theme / site can adjust.
 */
function carmilla_bottom_nav_items() {
	$items = array();
	$items[] = array( 'label' => 'خانه', 'url' => home_url( '/' ), 'icon' => 'home' );

	if ( carmilla_feature_enabled( 'shop' ) && function_exists( 'wc_get_page_permalink' ) ) {
		$items[] = array( 'label' => 'فروشگاه', 'url' => wc_get_page_permalink( 'shop' ), 'icon' => 'shop' );
	}
	if ( carmilla_feature_enabled( 'courses' ) && post_type_exists( 'cb_course' ) ) {
		$items[] = array( 'label' => 'دوره‌ها', 'url' => get_post_type_archive_link( 'cb_course' ), 'icon' => 'academy' );
	}
	if ( carmilla_feature_enabled( 'blog' ) ) {
		$items[] = array( 'label' => 'مجله', 'url' => get_permalink( get_option( 'page_for_posts' ) ) ?: home_url( '/blog' ), 'icon' => 'blog' );
	}
	if ( carmilla_feature_enabled( 'shop' ) && function_exists( 'wc_get_cart_url' ) ) {
		$items[] = array( 'label' => 'سبد', 'url' => wc_get_cart_url(), 'icon' => 'cart' );
	}
	$items[] = array(
		'label' => 'حساب',
		'url'   => function_exists( 'wc_get_page_permalink' ) ? wc_get_page_permalink( 'myaccount' ) : home_url( '/account' ),
		'icon'  => 'user',
	);

	// Keep at most 5 for the mobile bar.
	$items = array_slice( $items, 0, 5 );
	return apply_filters( 'carmilla_bottom_nav_items', $items );
}

/** Live cart count (0 when WooCommerce absent). */
function carmilla_cart_count() {
	if ( function_exists( 'WC' ) && WC()->cart ) {
		return (int) WC()->cart->get_cart_contents_count();
	}
	return 0;
}
