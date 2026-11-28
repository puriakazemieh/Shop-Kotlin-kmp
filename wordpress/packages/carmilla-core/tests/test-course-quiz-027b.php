<?php
/**
 * Real WordPress Verification for P04-WORDPRESS-CODE-027B
 * Shared Course Quiz & Certificate Service.
 *
 * Run inside WordPress container:
 *   php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-course-quiz-027b.php
 */

$wp_load = '/var/www/html/wp-load.php';
if ( ! file_exists( $wp_load ) ) {
	fwrite( STDERR, "wp-load.php not found\n" );
	exit( 1 );
}
require_once $wp_load;

use Carmilla\Core\Academy\CourseCatalogService;
use Carmilla\Core\Academy\CourseQuizService;

$passed = 0;
$failed = 0;

function assert_027b( bool $cond, string $label ): void {
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
$issued_cert_num    = null;

try {
	$admin_id = username_exists( 'qa_quiz_admin' );
	if ( ! $admin_id ) {
		$admin_id = wp_create_user( 'qa_quiz_admin', 'Pass123456!', 'qa_quiz_admin@example.com' );
	}
	( new WP_User( $admin_id ) )->set_role( 'administrator' );

	$user_id = username_exists( 'qa_quiz_user' );
	if ( ! $user_id ) {
		$user_id = wp_create_user( 'qa_quiz_user', 'Pass123456!', 'qa_quiz_user@example.com' );
	}
	( new WP_User( $user_id ) )->set_role( 'subscriber' );

	// 1. Create a course + upsert quiz
	wp_set_current_user( $admin_id );
	$course = CourseCatalogService::create_course( array(
		'title'   => 'دوره آزمون ۰۲۷B',
		'slug'    => 'qa-quiz-course-027b',
		'lessons' => array(
			array( 'title' => 'درس ۱', 'videoUrl' => 'https://example.com/1.mp4', 'freePreview' => true ),
		),
	) );
	$cid = (int) $course['id'];
	$created_course_ids[] = $cid;

	$upsert = CourseQuizService::upsert_course_quiz( $cid, array(
		'passScore' => 50,
		'questions' => "سوال اول؟ | گزینه الف | گزینه ب* | گزینه ج\nسوال دوم؟ | درست* | غلط",
	) );
	assert_027b( is_array( $upsert ) && ! empty( $upsert['ok'] ), 'Admin upserted course quiz' );

	// 2. Get quiz does NOT leak correct answer flags
	wp_set_current_user( $user_id );
	$quiz = CourseQuizService::get_course_quiz( $cid, $user_id );
	assert_027b( is_array( $quiz ) && 2 === count( $quiz['questions'] ), 'get_course_quiz returns 2 questions' );
	assert_027b( null === $quiz['questions'][0]['options'][1]['correct'], 'get_course_quiz does NOT leak correct answer flag' );

	// 3. Submit passing answers -> issues certificate
	$sub1 = CourseQuizService::submit_course_quiz( $cid, $user_id, array( '0' => 1, '1' => 0 ) );
	assert_027b( is_array( $sub1 ) && true === $sub1['passed'] && 100 === $sub1['score'] && ! empty( $sub1['certificateNumber'] ), 'Passing quiz issues certificate' );
	$issued_cert_num = $sub1['certificateNumber'];

	// 4. Duplicate attempt control: re-submit returns same certNumber & duplicateAttempt=true, no duplicate cert in cb_certs
	$sub2 = CourseQuizService::submit_course_quiz( $cid, $user_id, array( '0' => 0, '1' => 1 ) );
	assert_027b( is_array( $sub2 ) && true === $sub2['duplicateAttempt'] && $sub2['certificateNumber'] === $issued_cert_num, 'Duplicate quiz attempt controlled and returns existing certificate' );

	$certs = CourseQuizService::list_certificates( $user_id );
	$matching = array_filter( $certs, static function ( $c ) use ( $issued_cert_num ) {
		return ( $c['certNumber'] ?? '' ) === $issued_cert_num;
	} );
	assert_027b( count( $matching ) === 1, 'Exactly one certificate stored (no duplicate)' );

	// 5. Public certificate verification
	wp_set_current_user( 0 );
	$verify = CourseQuizService::verify_certificate( $issued_cert_num );
	assert_027b( true === $verify['valid'] && $issued_cert_num === $verify['certNumber'], 'verify_certificate confirms valid certificate' );

	// 6. REST routes singular & permission/feature gate checks
	$server = rest_get_server();
	$routes = $server->get_routes();
	assert_027b( ! empty( $routes['/carmilla/v1/api/academy/certificates'] ), 'REST /api/academy/certificates registered' );
	assert_027b( count( $routes['/carmilla/v1/api/academy/certificates'] ) === 1, 'No duplicate /api/academy/certificates handler' );

	wp_set_current_user( $user_id );
	$sc_certs = do_shortcode( '[carmilla_certificates]' );
	assert_027b( false !== strpos( $sc_certs, 'data-cert-number="' . $issued_cert_num . '"' ), '[carmilla_certificates] renders issued certificate' );

	// Disabled academy.core -> 403
	update_option( 'cb_manifest_features', array(
		'theme.base'   => true,
		'academy.core' => false,
	) );
	Carmilla_Kernel::resolve_features();
	$res_gated = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/academy/certificates' ) );
	assert_027b( 403 === $res_gated->get_status(), 'Disabled academy.core returns 403 on /api/academy/certificates' );
} finally {
	foreach ( $created_course_ids as $cid ) {
		delete_user_meta( $user_id, "cb_quiz_passed_{$cid}" );
		CourseCatalogService::delete_course( $cid );
	}
	delete_user_meta( $user_id, 'cb_certs' );
	if ( $issued_cert_num ) {
		$idx = get_option( 'cb_cert_index', array() );
		if ( is_array( $idx ) ) {
			unset( $idx[ $issued_cert_num ] );
			update_option( 'cb_cert_index', $idx, false );
		}
	}
	if ( null === $orig_features ) {
		delete_option( 'cb_manifest_features' );
	} else {
		update_option( 'cb_manifest_features', $orig_features );
	}
	wp_set_current_user( 0 );
}

echo "Course Quiz 027B QA: {$passed} passed, {$failed} failed.\n";
exit( $failed > 0 ? 1 : 0 );
