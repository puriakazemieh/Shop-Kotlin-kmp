<?php
/**
 * Carmilla admin dashboard — a top-level wp-admin menu that mirrors the app's
 * admin panel: quick counts + shortcuts to manage every section (products,
 * orders, courses, therapists, tests, content, appearance). Native wp-admin.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

add_action( 'admin_menu', function () {
	add_menu_page(
		__( 'مدیریت کارمیلا', 'carmilla' ),
		__( 'کارمیلا', 'carmilla' ),
		'edit_posts',
		'carmilla-dashboard',
		'carmilla_render_admin_dashboard',
		'dashicons-store',
		2
	);
} );

function carmilla_admin_count( $post_type ) {
	if ( ! post_type_exists( $post_type ) ) {
		return null;
	}
	$c = wp_count_posts( $post_type );
	return (int) ( $c->publish ?? 0 ) + (int) ( $c->draft ?? 0 );
}

function carmilla_render_admin_dashboard() {
	$cards = array();

	if ( carmilla_feature_enabled( 'shop' ) && class_exists( 'WooCommerce' ) ) {
		$cards[] = array( 'محصولات', carmilla_admin_count( 'product' ), admin_url( 'edit.php?post_type=product' ), 'افزودن/ویرایش محصول', 'dashicons-cart' );
		$cards[] = array( 'سفارش‌ها', null, admin_url( 'edit.php?post_type=shop_order' ), 'مدیریت سفارش‌ها', 'dashicons-list-view' );
	}
	if ( carmilla_feature_enabled( 'courses' ) ) {
		$cards[] = array( 'دوره‌ها', carmilla_admin_count( 'cb_course' ), admin_url( 'edit.php?post_type=cb_course' ), 'مدیریت دوره‌ها', 'dashicons-welcome-learn' );
		$cards[] = array( 'درخواست‌های دوره', carmilla_admin_count( 'cb_course_request' ), admin_url( 'edit.php?post_type=cb_course_request' ), 'بررسی درخواست‌ها', 'dashicons-megaphone' );
	}
	if ( carmilla_feature_enabled( 'clinic' ) ) {
		$cards[] = array( 'مشاوران', carmilla_admin_count( 'cb_therapist' ), admin_url( 'edit.php?post_type=cb_therapist' ), 'مدیریت مشاوران', 'dashicons-heart' );
	}
	if ( carmilla_feature_enabled( 'psychtests' ) ) {
		$cards[] = array( 'تست‌های روان‌شناسی', carmilla_admin_count( 'cb_psychtest' ), admin_url( 'edit.php?post_type=cb_psychtest' ), 'مدیریت تست‌ها', 'dashicons-clipboard' );
	}
	if ( carmilla_feature_enabled( 'blog' ) ) {
		$cards[] = array( 'مقالات', carmilla_admin_count( 'post' ), admin_url( 'edit.php' ), 'مدیریت مجله', 'dashicons-admin-post' );
	}
	if ( carmilla_feature_enabled( 'stories' ) ) {
		$cards[] = array( 'استوری‌ها', carmilla_admin_count( 'cb_story' ), admin_url( 'edit.php?post_type=cb_story' ), 'مدیریت استوری', 'dashicons-format-image' );
	}
	$cards[] = array( 'بنرهای خانه', carmilla_admin_count( 'cb_banner' ), admin_url( 'edit.php?post_type=cb_banner' ), 'مدیریت بنر', 'dashicons-images-alt2' );
	$cards[] = array( 'ظاهر و برند', null, admin_url( 'customize.php' ), 'رنگ، لوگو، فعال/غیرفعال بخش‌ها', 'dashicons-art' );

	echo '<div class="wrap"><h1 style="display:flex;align-items:center;gap:8px"><span class="dashicons dashicons-store"></span> ' . esc_html__( 'مدیریت کارمیلا', 'carmilla' ) . '</h1>';
	echo '<p class="description">' . esc_html__( 'میان‌بر به همه‌ی بخش‌های سایت. بخش‌های خاموش‌شده در «ظاهر و برند ← تنظیمات کارمیلا» اینجا نمایش داده نمی‌شوند.', 'carmilla' ) . '</p>';

	// One-click demo content.
	if ( ! get_option( 'carmilla_demo_imported' ) ) {
		echo '<form method="post" action="' . esc_url( admin_url( 'admin-post.php' ) ) . '" style="margin:12px 0;padding:14px;background:#fff;border:1px solid #dcdcde;border-radius:10px">';
		wp_nonce_field( 'carmilla_import_demo' );
		echo '<input type="hidden" name="action" value="carmilla_import_demo">';
		echo '<strong>' . esc_html__( 'شروع سریع:', 'carmilla' ) . '</strong> ' . esc_html__( 'محتوای نمونه (محصول، مقاله، دوره، مشاور، تست، استوری، بنر) را با یک کلیک بسازید. ', 'carmilla' );
		echo '<button type="submit" class="button button-primary">' . esc_html__( 'درون‌ریزی محتوای نمونه', 'carmilla' ) . '</button>';
		echo '</form>';
	}

	echo '<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(240px,1fr));gap:16px;margin-top:16px">';
	foreach ( $cards as $card ) {
		list( $title, $count, $url, $desc, $icon ) = $card;
		echo '<a href="' . esc_url( $url ) . '" style="text-decoration:none;background:#fff;border:1px solid #dcdcde;border-radius:10px;padding:18px;display:block;color:#1d2327">';
		echo '<div style="display:flex;align-items:center;justify-content:space-between">';
		echo '<span class="dashicons ' . esc_attr( $icon ) . '" style="font-size:26px;width:26px;height:26px;color:#20305C"></span>';
		if ( null !== $count ) {
			echo '<span style="font-size:22px;font-weight:700">' . esc_html( number_format_i18n( $count ) ) . '</span>';
		}
		echo '</div>';
		echo '<div style="margin-top:10px;font-size:15px;font-weight:600">' . esc_html( $title ) . '</div>';
		echo '<div style="color:#646970;font-size:12px;margin-top:2px">' . esc_html( $desc ) . '</div>';
		echo '</a>';
	}
	echo '</div></div>';
}
