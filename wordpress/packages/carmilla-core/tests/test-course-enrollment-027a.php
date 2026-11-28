<?php
/**
 * Real WordPress Verification for P04-WORDPRESS-CODE-027A
 * Shared Course Enrollment & Progress Service.
 *
 * Run inside WordPress container:
 *   php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-course-enrollment-027a.php
 */

$wp_load = '/var/www/html/wp-load.php';
if ( ! file_exists( $wp_load ) ) {
	fwrite( STDERR, "wp-load.php not found\n" );
	exit( 1 );
}
require_once $wp_load;

use Carmilla\Core\Academy\CourseCatalogService;
use Carmilla\Core\Academy\CourseEnrollmentService;

$passed = 0;
$failed = 0;

function assert_027a( bool $cond, string $label ): void {
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
	$admin_id = username_exists( 'qa_enroll_admin' );
	if ( ! $admin_id ) {
		$admin_id = wp_create_user( 'qa_enroll_admin', 'Pass123456!', 'qa_enroll_admin@example.com' );
	}
	( new WP_User( $admin_id ) )->set_role( 'administrator' );

	$user_id = username_exists( 'qa_enroll_user' );
	if ( ! $user_id ) {
		$user_id = wp_create_user( 'qa_enroll_user', 'Pass123456!', 'qa_enroll_user@example.com' );
	}
	( new WP_User( $user_id ) )->set_role( 'subscriber' );

	// 1. Create a free course (2 lessons) and a paid course (1 protected lesson)
	wp_set_current_user( $admin_id );
	$free_course = CourseCatalogService::create_course( array(
		'title'   => 'دوره رایگان ۰۲۷A',
		'slug'    => 'qa-free-course-027a',
		'lessons' => array(
			array( 'title' => 'درس ۱', 'videoUrl' => 'https://example.com/1.mp4', 'freePreview' => true ),
			array( 'title' => 'درس ۲', 'videoUrl' => 'https://example.com/2.mp4', 'freePreview' => true ),
		),
	) );
	$free_id = (int) $free_course['id'];
	$created_course_ids[] = $free_id;

	$paid_course = CourseCatalogService::create_course( array(
		'title'       => 'دوره پولی ۰۲۷A',
		'slug'        => 'qa-paid-course-027a',
		'productSlug' => 'qa-paid-slug-027a',
		'lessons'     => array(
			array( 'title' => 'درس خصوصی ۱', 'videoUrl' => 'https://example.com/p1.mp4', 'freePreview' => false ),
		),
	) );
	$paid_id = (int) $paid_course['id'];
	$created_course_ids[] = $paid_id;

	// 2. REST routes registered without duplicates
	$server = rest_get_server();
	$routes = $server->get_routes();
	assert_027a( ! empty( $routes['/carmilla/v1/api/academy/my-courses'] ), 'REST /api/academy/my-courses registered' );
	assert_027a( count( $routes['/carmilla/v1/api/academy/my-courses'] ) === 1, 'No duplicate /api/academy/my-courses handler' );

	// 3. Unauthenticated blocked
	wp_set_current_user( 0 );
	$res_unauth = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/academy/my-courses' ) );
	assert_027a( in_array( $res_unauth->get_status(), array( 401, 403 ), true ), 'Unauthenticated blocked on /api/academy/my-courses' );

	// 4. Paid course cannot be enrolled or progressed without purchase/grant
	wp_set_current_user( $user_id );
	$paid_enroll = CourseEnrollmentService::enroll( $paid_id, $user_id );
	assert_027a( is_wp_error( $paid_enroll ) && 'ENROLLMENT_REQUIRED' === $paid_enroll->get_error_code(), 'Paid course enroll fails closed (403 ENROLLMENT_REQUIRED)' );

	$paid_lid  = CourseCatalogService::lesson_id( $paid_id, 0 );
	$paid_prog = CourseEnrollmentService::update_lesson_progress( $paid_lid, $user_id, true );
	assert_027a( is_wp_error( $paid_prog ) && 'ENROLLMENT_REQUIRED' === $paid_prog->get_error_code(), 'Paid lesson progress fails closed without enrollment' );

	// 5. Free course enroll & progress calculation (1 of 2 lessons -> 50%)
	$free_enroll = CourseEnrollmentService::enroll( $free_id, $user_id );
	assert_027a( is_array( $free_enroll ) && true === $free_enroll['enrolled'], 'Free course enroll succeeds' );

	$free_lid1 = CourseCatalogService::lesson_id( $free_id, 0 );
	$prog_res  = CourseEnrollmentService::update_lesson_progress( $free_lid1, $user_id, true );
	assert_027a( is_array( $prog_res ) && 50 === $prog_res['progressPercent'] && 1 === $prog_res['completedLessons'], 'Progress updates to 50% (1/2)' );

	$my = CourseEnrollmentService::my_courses( $user_id );
	assert_027a( count( $my ) >= 1 && 50 === $my[0]['progressPercent'], 'my_courses returns enrolled course with 50% progress' );

	// 6. Shortcode [carmilla_my_courses] renders canonical ID & progress
	$sc_my = do_shortcode( '[carmilla_my_courses]' );
	assert_027a( false !== strpos( $sc_my, 'data-course-id="' . $free_id . '"' ) && false !== strpos( $sc_my, 'data-progress="50"' ), '[carmilla_my_courses] renders canonical ID and 50% progress' );

	// 7. Disabled academy.core closes /api/academy/my-courses
	update_option( 'cb_manifest_features', array(
		'theme.base'   => true,
		'academy.core' => false,
	) );
	Carmilla_Kernel::resolve_features();
	$res_gated = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/academy/my-courses' ) );
	assert_027a( 403 === $res_gated->get_status(), 'Disabled academy.core returns 403 on /api/academy/my-courses' );
} finally {
	foreach ( $created_course_ids as $cid ) {
		delete_user_meta( $user_id, "cb_course_prog_{$cid}" );
		CourseCatalogService::delete_course( $cid );
	}
	if ( null === $orig_features ) {
		delete_option( 'cb_manifest_features' );
	} else {
		update_option( 'cb_manifest_features', $orig_features );
	}
	wp_set_current_user( 0 );
}

echo "Course Enrollment 027A QA: {$passed} passed, {$failed} failed.\n";
exit( $failed > 0 ? 1 : 0 );
