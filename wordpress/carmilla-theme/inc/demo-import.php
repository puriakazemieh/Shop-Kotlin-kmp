<?php
/**
 * One-click demo content importer. From the Carmilla admin dashboard it seeds a
 * complete sample site — posts, courses (with lessons + syllabus), therapists
 * (with slots), psych-tests (with questions + ranges), stories, banners, and
 * WooCommerce products — so a fresh install looks finished immediately.
 * Idempotent: guarded by the carmilla_demo_imported option.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/** Create a post of a type with meta (skips if the CPT isn't registered). */
function carmilla_demo_post( $type, $title, $content, $meta = array() ) {
	if ( ! post_type_exists( $type ) ) {
		return 0;
	}
	$id = wp_insert_post( array(
		'post_type'    => $type,
		'post_title'   => $title,
		'post_content' => $content,
		'post_status'  => 'publish',
	), true );
	if ( is_wp_error( $id ) ) {
		return 0;
	}
	foreach ( $meta as $k => $v ) {
		update_post_meta( $id, $k, $v );
	}
	return (int) $id;
}

function carmilla_import_demo() {
	if ( get_option( 'carmilla_demo_imported' ) ) {
		return 0;
	}
	$n = 0;

	// ---- Blog posts ----
	foreach ( array(
		array( '۷ ترفند ست‌کردن لباس پاییزی', 'راهنمای کوتاه برای ترکیب رنگ‌ها و لایه‌بندی در پاییز.' ),
		array( 'چطور یک دوره‌ی آنلاین موفق بسازیم؟', 'از ایده تا انتشار، مسیر ساخت دوره را مرور می‌کنیم.' ),
		array( 'نشانه‌های فرسودگی شغلی و راهکارها', 'علائم رایج فرسودگی و چند راهکار عملی برای مقابله.' ),
	) as $p ) {
		$n += carmilla_demo_post( 'post', $p[0], $p[1] ) ? 1 : 0;
	}

	// ---- Courses ----
	$courses = array(
		array( 'دوره‌ی جامع طراحی UI', 'مبانی طراحی رابط کاربری از صفر تا پروژه‌ی واقعی.', array(
			'cb_instructor' => 'مینا رضایی', 'cb_level' => 'مقدماتی', 'cb_format' => 'ONLINE_RECORDED', 'cb_duration' => '۱۲ ساعت',
			'cb_syllabus'   => "آشنایی با اصول طراحی\nرنگ و تایپوگرافی\nطراحی کامپوننت\nپروژه‌ی پایانی",
			'cb_lessons'    => "مقدمه و معرفی دوره | https://example.com/v/intro.mp4 | free\nاصول رنگ | https://example.com/v/color.mp4\nتایپوگرافی | https://example.com/v/type.mp4",
		) ),
		array( 'دوره‌ی مدیریت زمان', 'تکنیک‌های عملی برای بهره‌وری بیشتر.', array(
			'cb_instructor' => 'سارا احمدی', 'cb_level' => 'همه‌سطوح', 'cb_format' => 'ONLINE_LIVE', 'cb_duration' => '۶ ساعت',
			'cb_lessons'    => "چرا مدیریت زمان؟ | https://example.com/v/tm1.mp4 | free\nماتریس اولویت | https://example.com/v/tm2.mp4",
		) ),
	);
	foreach ( $courses as $c ) {
		$n += carmilla_demo_post( 'cb_course', $c[0], $c[1], $c[2] ) ? 1 : 0;
	}

	// ---- Therapists ----
	$future1 = gmdate( 'Y-m-d\TH:i', strtotime( '+3 days 14:00' ) );
	$future2 = gmdate( 'Y-m-d\TH:i', strtotime( '+4 days 10:00' ) );
	$therapists = array(
		array( 'دکتر نیلوفر کریمی', 'روان‌شناس بالینی با ۱۰ سال سابقه.', array(
			'cb_specialty' => 'اضطراب و افسردگی', 'cb_approach' => 'شناختی-رفتاری',
			'cb_slots'     => "$future1\n$future2",
		) ),
		array( 'دکتر امیر مرادی', 'مشاور خانواده و زوج‌درمانگر.', array(
			'cb_specialty' => 'زوج‌درمانی', 'cb_approach' => 'هیجان‌مدار',
			'cb_slots'     => gmdate( 'Y-m-d\TH:i', strtotime( '+5 days 16:00' ) ),
		) ),
	);
	foreach ( $therapists as $t ) {
		$n += carmilla_demo_post( 'cb_therapist', $t[0], $t[1], $t[2] ) ? 1 : 0;
	}

	// ---- Psychology tests ----
	$n += carmilla_demo_post( 'cb_psychtest', 'تست سنجش استرس', 'در چند دقیقه سطح استرس خود را بسنجید.', array(
		'cb_questions' => "در طول روز چقدر احساس فشار می‌کنید؟ | کم=۰ , متوسط=۱ , زیاد=۲\nکیفیت خواب شما چطور است؟ | خوب=۰ , متوسط=۱ , بد=۲\nتمرکزتان چگونه است؟ | خوب=۰ , متوسط=۱ , ضعیف=۲",
		'cb_ranges'    => "۰ | ۲ | استرس پایین — وضعیت مطلوب\n۳ | ۴ | استرس متوسط — مراقب باشید\n۵ | ۶ | استرس بالا — توصیه به مشاوره",
	) ) ? 1 : 0;

	// ---- Stories & banners (with destination links) ----
	foreach ( array(
		array( 'تخفیف‌های هفته', 'محصولات با بهترین قیمت این هفته.' ),
		array( 'محصولات جدید', 'تازه‌ترین‌ها را ببینید.' ),
		array( 'دوره‌های تازه', 'دوره‌های آموزشی جدید منتشر شد.' ),
	) as $s ) {
		$n += carmilla_demo_post( 'cb_story', $s[0], $s[1], array( 'cb_link_url' => home_url( '/shop/' ) ) ) ? 1 : 0;
	}
	$n += carmilla_demo_post( 'cb_banner', 'حراج پاییزی', '', array( 'cb_subtitle' => 'تا ۵۰٪ تخفیف', 'cb_sort' => 1 ) ) ? 1 : 0;

	// ---- WooCommerce products (+ a grouped "bundle") ----
	if ( class_exists( 'WC_Product_Simple' ) ) {
		$products = array(
			array( 'مانتو کتان مدل ترمه', '1280000', '1600000' ),
			array( 'شال نخی', '320000', '' ),
			array( 'کیف چرم دست‌دوز', '2100000', '' ),
			array( 'کفش تابستانی', '890000', '' ),
		);
		$product_ids = array();
		foreach ( $products as $p ) {
			$product = new WC_Product_Simple();
			$product->set_name( $p[0] );
			$product->set_regular_price( $p[1] );
			if ( $p[2] ) {
				$product->set_sale_price( $p[2] );
			}
			$product->set_status( 'publish' );
			$product->set_catalog_visibility( 'visible' );
			$pid = $product->save();
			if ( $pid ) {
				$n++;
				$product_ids[] = $pid;
			}
		}
		// A grouped product = a bundle (first three items).
		if ( class_exists( 'WC_Product_Grouped' ) && count( $product_ids ) >= 3 ) {
			$bundle = new WC_Product_Grouped();
			$bundle->set_name( 'ست کامل پاییزی' );
			$bundle->set_status( 'publish' );
			$bundle->set_catalog_visibility( 'visible' );
			$bundle->set_children( array_slice( $product_ids, 0, 3 ) );
			if ( $bundle->save() ) {
				$n++;
			}
		}
	}

	// ---- Shortcode pages (compare, assistant, bundles, support) ----
	$pages = array(
		'compare'   => array( 'مقایسه‌ی محصولات', '[carmilla_compare]' ),
		'assistant' => array( 'دستیار خرید', '[carmilla_assistant]' ),
		'bundles'   => array( 'مجموعه‌ها', '[carmilla_bundles]' ),
		'support'   => array( 'پشتیبانی', '[carmilla_support]' ),
		'placement' => array( 'آزمون تعیین سطح', '[carmilla_placement]' ),
		'verify'    => array( 'تأیید گواهی', '[carmilla_verify]' ),
	);
	foreach ( $pages as $slug => $pg ) {
		if ( ! get_page_by_path( $slug ) ) {
			$pid = wp_insert_post( array(
				'post_type'    => 'page',
				'post_name'    => $slug,
				'post_title'   => $pg[0],
				'post_content' => $pg[1],
				'post_status'  => 'publish',
			) );
			if ( $pid && ! is_wp_error( $pid ) ) {
				$n++;
			}
		}
	}

	update_option( 'carmilla_demo_imported', 1 );
	flush_rewrite_rules();
	return $n;
}

/** Handle the dashboard "import demo" button. */
add_action( 'admin_post_carmilla_import_demo', function () {
	if ( ! current_user_can( 'manage_options' ) || ! check_admin_referer( 'carmilla_import_demo' ) ) {
		wp_die( 'اجازه‌ی دسترسی ندارید.' );
	}
	$count = carmilla_import_demo();
	wp_safe_redirect( add_query_arg( 'carmilla_imported', (int) $count, admin_url( 'admin.php?page=carmilla-dashboard' ) ) );
	exit;
} );

/** Success notice after import. */
add_action( 'admin_notices', function () {
	if ( isset( $_GET['carmilla_imported'] ) && isset( $_GET['page'] ) && 'carmilla-dashboard' === $_GET['page'] ) {
		$c = (int) $_GET['carmilla_imported'];
		echo '<div class="notice notice-success is-dismissible"><p>' .
			esc_html( sprintf( '%s مورد محتوای نمونه ساخته شد.', number_format_i18n( $c ) ) ) .
			'</p></div>';
	}
} );
