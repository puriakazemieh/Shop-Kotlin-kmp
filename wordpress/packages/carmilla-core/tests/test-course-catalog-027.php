<?php
/**
 * Real WordPress Verification for P04-WORDPRESS-CODE-027
 * Shared Course Catalog & Lesson Content Service (without enrollment/quiz).
 *
 * Run inside WordPress container:
 *   php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-course-catalog-027.php
 */

$wp_load = '/var/www/html/wp-load.php';
if ( ! file_exists( $wp_load ) ) {
	fwrite( STDERR, "wp-load.php not found\n" );
	exit( 1 );
}
require_once $wp_load;

use Carmilla\Core\Academy\CourseCatalogService;

$passed = 0;
$failed = 0;

function assert_027( bool $cond, string $label ): void {
	global $passed, $failed;
	if ( $cond ) {
		++$passed;
	} else {
		++$failed;
		fwrite( STDERR, "FAIL: {$label}\n" );
	}
}

$orig_features = get_option( 'cb_manifest_features', null );
update_option( 'cb_manifest_features', array(
	'theme.base'   => true,
	'academy.core' => true,
) );
Carmilla_Kernel::resolve_features();

$created_course_ids = array();

try {
	$admin_id = username_exists( 'qa_academy_admin' );
	if ( ! $admin_id ) {
		$admin_id = wp_create_user( 'qa_academy_admin', 'Pass123456!', 'qa_academy_admin@example.com' );
	}
	( new WP_User( $admin_id ) )->set_role( 'administrator' );

	$sub_id = username_exists( 'qa_academy_sub' );
	if ( ! $sub_id ) {
		$sub_id = wp_create_user( 'qa_academy_sub', 'Pass123456!', 'qa_academy_sub@example.com' );
	}
	( new WP_User( $sub_id ) )->set_role( 'subscriber' );

	// 1. Create paid course with 1 free preview lesson & 1 protected lesson
	wp_set_current_user( $admin_id );
	$course = CourseCatalogService::create_course( array(
		'title'       => 'دوره آزمایشی ۰۲۷',
		'slug'        => 'qa-course-027',
		'instructor'  => 'استاد آزمایشی',
		'price'       => 250000,
		'productSlug' => 'qa-paid-course-product',
		'lessons'     => array(
			array( 'title' => 'درس اول رایگان', 'videoUrl' => 'https://example.com/free.mp4', 'freePreview' => true ),
			array( 'title' => 'درس دوم خصوصی', 'videoUrl' => 'https://example.com/secret.mp4', 'freePreview' => false ),
		),
	) );
	assert_027( is_array( $course ) && ! empty( $course['id'] ), 'Course created via CourseCatalogService' );
	$cid = (int) $course['id'];
	$created_course_ids[] = $cid;
	$l1_id = CourseCatalogService::lesson_id( $cid, 0 );
	$l2_id = CourseCatalogService::lesson_id( $cid, 1 );
	assert_027( $course['sections'][0]['lessons'][0]['id'] === $l1_id, 'Synthetic lesson ID 1 matches LESSON_BASE formula' );
	assert_027( $course['sections'][0]['lessons'][1]['id'] === $l2_id, 'Synthetic lesson ID 2 matches LESSON_BASE formula' );

	// 2. Guest/unpurchased user sees free lesson videoUrl, but protected lesson videoUrl is REDACTED (null)
	wp_set_current_user( 0 );
	$guest_detail = CourseCatalogService::get_course_detail( 'qa-course-027', 0 );
	assert_027( is_array( $guest_detail ) && 'https://example.com/free.mp4' === $guest_detail['sections'][0]['lessons'][0]['videoUrl'], 'Guest can access freePreview lesson videoUrl' );
	assert_027( null === $guest_detail['sections'][0]['lessons'][1]['videoUrl'], 'Guest CANNOT access protected lesson videoUrl (fail-closed)' );

	// 3. Subscriber with explicit enrollment sees protected lesson
	update_user_meta( $sub_id, "cb_course_enrolled_{$cid}", '1' );
	$enrolled_detail = CourseCatalogService::get_course_detail( $cid, $sub_id );
	assert_027( 'https://example.com/secret.mp4' === $enrolled_detail['sections'][0]['lessons'][1]['videoUrl'], 'Enrolled user can access protected lesson videoUrl' );
	delete_user_meta( $sub_id, "cb_course_enrolled_{$cid}" );

	// 4. REST routes: singular registration, unauthorized admin blocked, feature gate enforced
	$server = rest_get_server();
	$routes = $server->get_routes();
	assert_027( ! empty( $routes['/carmilla/v1/api/courses'] ), 'REST /carmilla/v1/api/courses registered' );
	assert_027( ! empty( $routes['/carmilla/v1/api/admin/courses'] ), 'REST /carmilla/v1/api/admin/courses registered' );
	assert_027( count( $routes['/carmilla/v1/api/courses'] ) === 1, 'No duplicate /api/courses handler' );

	wp_set_current_user( $sub_id );
	$req_unauth = new WP_REST_Request( 'POST', '/carmilla/v1/api/admin/courses' );
	$req_unauth->set_param( 'title', 'غیرمجاز' );
	$res_unauth = $server->dispatch( $req_unauth );
	assert_027( 403 === $res_unauth->get_status(), 'Subscriber blocked (403) on POST /api/admin/courses' );

	// Public REST detail redacts protected lesson for guest
	wp_set_current_user( 0 );
	$req_pub = new WP_REST_Request( 'GET', '/carmilla/v1/api/courses/qa-course-027' );
	$res_pub = $server->dispatch( $req_pub );
	$pub_data = $res_pub->get_data();
	assert_027( 200 === $res_pub->get_status() && null === $pub_data['sections'][0]['lessons'][1]['videoUrl'], 'REST GET /api/courses/{slug} redacts protected lesson for guest' );

	// 5. Feature gate disabled -> 403 FEATURE_DISABLED & data preserved
	update_option( 'cb_manifest_features', array(
		'theme.base'   => true,
		'academy.core' => false,
	) );
	Carmilla_Kernel::resolve_features();
	$req_gated = new WP_REST_Request( 'GET', '/carmilla/v1/api/courses' );
	$res_gated = $server->dispatch( $req_gated );
	assert_027( 403 === $res_gated->get_status(), 'Disabled academy.core returns 403 FEATURE_DISABLED on /api/courses' );

	$sc_off = do_shortcode( '[carmilla_courses]' );
	assert_027( false !== strpos( $sc_off, 'academy-disabled' ), '[carmilla_courses] closes when academy.core is disabled' );
	assert_027( get_post( $cid ) instanceof WP_Post, 'Disabling academy.core preserves course data' );

	// Re-enable and verify shortcodes
	update_option( 'cb_manifest_features', array(
		'theme.base'   => true,
		'academy.core' => true,
	) );
	Carmilla_Kernel::resolve_features();
	$sc_list = do_shortcode( '[carmilla_courses]' );
	$sc_det  = do_shortcode( '[carmilla_course id="' . $cid . '"]' );
	assert_027( false !== strpos( $sc_list, 'data-course-id="' . $cid . '"' ), '[carmilla_courses] renders canonical course ID' );
	assert_027( false !== strpos( $sc_det, 'data-lesson-id="' . $l2_id . '" data-locked="1"' ), '[carmilla_course] marks protected lesson locked for guest' );
} finally {
	foreach ( $created_course_ids as $cid ) {
		CourseCatalogService::delete_course( $cid );
	}
	if ( null === $orig_features ) {
		delete_option( 'cb_manifest_features' );
	} else {
		update_option( 'cb_manifest_features', $orig_features );
	}
	wp_set_current_user( 0 );
}

echo "Course Catalog 027 QA: {$passed} passed, {$failed} failed.\n";
exit( $failed > 0 ? 1 : 0 );
