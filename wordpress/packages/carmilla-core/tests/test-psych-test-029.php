<?php
/**
 * Real WordPress Verification for P04-WORDPRESS-CODE-029
 * Shared Psychological Test Service, Deterministic Scoring & Private Results.
 *
 * Run inside WordPress container:
 *   php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-psych-test-029.php
 */

$wp_load = '/var/www/html/wp-load.php';
if ( ! file_exists( $wp_load ) ) {
	fwrite( STDERR, "wp-load.php not found\n" );
	exit( 1 );
}
require_once $wp_load;

use Carmilla\Core\Psych\PsychTestService;

$passed = 0;
$failed = 0;

function assert_029( bool $cond, string $label ): void {
	global $passed, $failed;
	if ( $cond ) {
		++$passed;
	} else {
		++$failed;
		fwrite( STDERR, "FAIL: {$label}\n" );
	}
}

$orig_features = get_option( 'cb_manifest_features', null );
// Enable psych.tests WITHOUT clinic.booking to prove independence!
update_option( 'cb_manifest_features', array(
	'theme.base'     => true,
	'psych.tests'    => true,
	'clinic.booking' => false,
) );
Carmilla_Kernel::resolve_features();

$created_tests = array();
$u1_id         = 0;
$u2_id         = 0;

try {
	$admin_id = username_exists( 'qa_psych_admin' );
	if ( ! $admin_id ) {
		$admin_id = wp_create_user( 'qa_psych_admin', 'Pass123456!', 'qa_psych_admin@example.com' );
	}
	( new WP_User( $admin_id ) )->set_role( 'administrator' );

	$u1_id = username_exists( 'qa_psych_u1' );
	if ( ! $u1_id ) {
		$u1_id = wp_create_user( 'qa_psych_u1', 'Pass123456!', 'qa_psych_u1@example.com' );
	}
	( new WP_User( $u1_id ) )->set_role( 'subscriber' );

	$u2_id = username_exists( 'qa_psych_u2' );
	if ( ! $u2_id ) {
		$u2_id = wp_create_user( 'qa_psych_u2', 'Pass123456!', 'qa_psych_u2@example.com' );
	}
	( new WP_User( $u2_id ) )->set_role( 'subscriber' );

	delete_user_meta( $u1_id, 'cb_psych_attempts' );
	delete_user_meta( $u1_id, 'cb_psych_attempt_seq' );
	delete_user_meta( $u2_id, 'cb_psych_attempts' );
	delete_user_meta( $u2_id, 'cb_psych_attempt_seq' );

	// 1. Create synthetic AUTO test + COUNSELOR test via admin API
	wp_set_current_user( $admin_id );
	$server = rest_get_server();
	$routes = $server->get_routes();
	assert_029( ! empty( $routes['/carmilla/v1/api/psych-tests'] ) && count( $routes['/carmilla/v1/api/psych-tests'] ) === 1, 'REST /api/psych-tests registered once' );
	assert_029( ! empty( $routes['/carmilla/v1/api/admin/psych-tests'] ) && count( $routes['/carmilla/v1/api/admin/psych-tests'] ) === 2, 'REST /api/admin/psych-tests registered once (GET+POST)' );

	$req_auto = new WP_REST_Request( 'POST', '/carmilla/v1/api/admin/psych-tests' );
	$req_auto->set_header( 'content-type', 'application/json' );
	$req_auto->set_body( wp_json_encode( array(
		'title'      => 'تست خلق آزمایشی ۰۲۹',
		'slug'       => 'qa-psych-auto-029',
		'resultMode' => 'AUTO',
		'questions'  => array(
			array(
				'text'    => 'انرژی امروز؟',
				'options' => array(
					array( 'text' => 'کم', 'score' => 1 ),
					array( 'text' => 'متوسط', 'score' => 3 ),
					array( 'text' => 'زیاد', 'score' => 5 ),
				),
			),
			array(
				'text'    => 'کیفیت خواب؟',
				'options' => array(
					array( 'text' => 'بد', 'score' => 1 ),
					array( 'text' => 'خوب', 'score' => 5 ),
				),
			),
		),
		'ranges'     => array(
			array( 'minScore' => 2, 'maxScore' => 5, 'interpretation' => 'سطح پایین (آزمایشی)' ),
			array( 'minScore' => 6, 'maxScore' => 10, 'interpretation' => 'سطح مطلوب (آزمایشی)' ),
		),
	) ) );
	$res_auto = $server->dispatch( $req_auto );
	$auto_id  = (int) ( $res_auto->get_data()['id'] ?? 0 );
	assert_029( 201 === $res_auto->get_status() && $auto_id > 0, 'Admin created AUTO psych test' );
	$created_tests[] = $auto_id;

	$req_cns = new WP_REST_Request( 'POST', '/carmilla/v1/api/admin/psych-tests' );
	$req_cns->set_header( 'content-type', 'application/json' );
	$req_cns->set_body( wp_json_encode( array(
		'title'      => 'تست تفسیری آزمایشی ۰۲۹',
		'slug'       => 'qa-psych-counselor-029',
		'resultMode' => 'COUNSELOR',
		'questions'  => array(
			array(
				'text'    => 'سؤال تفسیری؟',
				'options' => array(
					array( 'text' => 'گزینه الف', 'score' => 2 ),
					array( 'text' => 'گزینه ب', 'score' => 4 ),
				),
			),
		),
	) ) );
	$res_cns = $server->dispatch( $req_cns );
	$cns_id  = (int) ( $res_cns->get_data()['id'] ?? 0 );
	assert_029( 201 === $res_cns->get_status() && $cns_id > 0, 'Admin created COUNSELOR psych test' );
	$created_tests[] = $cns_id;

	// 2. Public detail strips option scores (never leaked to client)
	wp_set_current_user( 0 );
	$res_pub  = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/psych-tests/qa-psych-auto-029' ) );
	$pub_data = $res_pub->get_data();
	$opt_0    = $pub_data['questions'][0]['options'][0] ?? array();
	assert_029( 200 === $res_pub->get_status() && is_array( $opt_0 ) && array_key_exists( 'score', $opt_0 ) && null === $opt_0['score'], 'Public detail strips option scores (score === null)' );

	// 3. User 1 fetches my-psych-tests and submits AUTO test deterministically (option 2 [5] + option 1 [5] = 10)
	wp_set_current_user( $u1_id );
	$res_mine = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/my-psych-tests' ) );
	$mine     = $res_mine->get_data();
	assert_029( 200 === $res_mine->get_status() && is_array( $mine ) && count( $mine ) >= 2, 'User 1 receives attempts in my-psych-tests' );

	$auto_attempt_id = 0;
	$cns_attempt_id  = 0;
	foreach ( $mine as $a ) {
		if ( (int) $a['testId'] === $auto_id ) {
			$auto_attempt_id = (int) $a['id'];
		}
		if ( (int) $a['testId'] === $cns_id ) {
			$cns_attempt_id = (int) $a['id'];
		}
	}
	assert_029( $auto_attempt_id > 0 && $cns_attempt_id > 0, 'Attempt IDs assigned for both tests' );

	$req_sub1 = new WP_REST_Request( 'POST', '/carmilla/v1/api/my-psych-tests/' . $auto_attempt_id . '/submit' );
	$req_sub1->set_header( 'content-type', 'application/json' );
	$req_sub1->set_body( wp_json_encode( array( 'answers' => array( 0 => 2, 1 => 1 ) ) ) );
	$res_sub1 = $server->dispatch( $req_sub1 );
	$s1       = $res_sub1->get_data();
	assert_029( 200 === $res_sub1->get_status() && 10 === (int) ( $s1['totalScore'] ?? -1 ) && 'سطح مطلوب (آزمایشی)' === ( $s1['interpretation'] ?? '' ) && 'COMPLETED' === ( $s1['status'] ?? '' ), 'Deterministic AUTO scoring & interpretation works (score=10)' );

	// 4. Theme psychtest.js route /api/psych-tests/{id}/submit
	$req_js = new WP_REST_Request( 'POST', '/carmilla/v1/api/psych-tests/' . $auto_id . '/submit' );
	$req_js->set_header( 'content-type', 'application/json' );
	$req_js->set_body( wp_json_encode( array( 'answers' => array( 0, 0 ) ) ) ); // 1 + 1 = 2
	$res_js = $server->dispatch( $req_js );
	$js_d   = $res_js->get_data();
	assert_029( 200 === $res_js->get_status() && 2 === (int) ( $js_d['score'] ?? -1 ) && 10 === (int) ( $js_d['maxScore'] ?? -1 ) && 'سطح پایین (آزمایشی)' === ( $js_d['interpretation'] ?? '' ), 'psychtest.js submit route returns {score:2, maxScore:10, interpretation}' );

	// 5. IDOR check: User 2 cannot view or submit User 1's attempt
	wp_set_current_user( $u2_id );
	$res_idor_q = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/my-psych-tests/' . $auto_attempt_id . '/questions' ) );
	$res_idor_r = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/my-psych-tests/' . $auto_attempt_id . '/result' ) );
	$res_idor_s = $server->dispatch( new WP_REST_Request( 'POST', '/carmilla/v1/api/my-psych-tests/' . $auto_attempt_id . '/submit' ) );
	assert_029( 404 === $res_idor_q->get_status() && 404 === $res_idor_r->get_status() && 404 === $res_idor_s->get_status(), 'IDOR blocked: User 2 gets 404 on User 1 attempt questions/result/submit' );

	// 6. COUNSELOR mode + admin interpretation
	wp_set_current_user( $u1_id );
	$req_sub_cns = new WP_REST_Request( 'POST', '/carmilla/v1/api/my-psych-tests/' . $cns_attempt_id . '/submit' );
	$req_sub_cns->set_header( 'content-type', 'application/json' );
	$req_sub_cns->set_body( wp_json_encode( array( 'answers' => array( 0 => 1 ) ) ) );
	$res_sub_cns = $server->dispatch( $req_sub_cns );
	assert_029( 200 === $res_sub_cns->get_status() && 'AWAITING_INTERPRETATION' === ( $res_sub_cns->get_data()['status'] ?? '' ), 'COUNSELOR test enters AWAITING_INTERPRETATION' );

	wp_set_current_user( $admin_id );
	$res_pend = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/admin/psych-tests/pending-interpretations' ) );
	assert_029( 200 === $res_pend->get_status() && is_array( $res_pend->get_data() ) && count( $res_pend->get_data() ) >= 1, 'Admin sees pending interpretation' );

	$req_interp = new WP_REST_Request( 'POST', '/carmilla/v1/api/admin/psych-tests/user-tests/' . $cns_attempt_id . '/interpret' );
	$req_interp->set_header( 'content-type', 'application/json' );
	$req_interp->set_body( wp_json_encode( array( 'interpretation' => 'تفسیر آزمایشی مشاور' ) ) );
	$res_interp = $server->dispatch( $req_interp );
	assert_029( 200 === $res_interp->get_status(), 'Admin submits counselor interpretation' );

	wp_set_current_user( $u1_id );
	$res_priv = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/my-psych-tests/' . $cns_attempt_id . '/result' ) );
	assert_029( 200 === $res_priv->get_status() && 'COMPLETED' === ( $res_priv->get_data()['status'] ?? '' ) && 'تفسیر آزمایشی مشاور' === ( $res_priv->get_data()['interpretation'] ?? '' ), 'Owner sees completed counselor interpretation in private result' );

	// 7. Shortcodes render
	$html_list   = do_shortcode( '[carmilla_psych_tests]' );
	$html_form   = do_shortcode( '[carmilla_psych_test id="' . $auto_id . '"]' );
	$html_result = do_shortcode( '[carmilla_my_psych_results]' );
	assert_029( strpos( $html_list, 'data-carmilla-surface="psych-tests"' ) !== false, 'Psych tests list shortcode renders' );
	assert_029( strpos( $html_form, 'data-carmilla-surface="psych-test-form"' ) !== false && strpos( $html_form, 'score=' ) === false, 'Psych test form shortcode renders without scores' );
	assert_029( strpos( $html_result, 'data-carmilla-surface="my-psych-results"' ) !== false, 'My psych results shortcode renders' );

	// 8. Feature disabled gate + data preservation on toggle off/on
	update_option( 'cb_manifest_features', array(
		'theme.base'     => true,
		'psych.tests'    => false,
	) );
	Carmilla_Kernel::resolve_features();
	$res_dis  = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/psych-tests' ) );
	$dis_data = $res_dis->get_data();
	$dis_code = $dis_data['errorCode'] ?? $dis_data['code'] ?? '';
	assert_029( 403 === $res_dis->get_status() && 'FEATURE_DISABLED' === $dis_code, 'Disabled psych.tests returns 403 FEATURE_DISABLED' );
	$html_dis = do_shortcode( '[carmilla_psych_tests]' );
	assert_029( strpos( $html_dis, 'data-carmilla-surface="psych-disabled"' ) !== false, 'Disabled psych.tests shortcode renders disabled notice' );

	update_option( 'cb_manifest_features', array(
		'theme.base'  => true,
		'psych.tests' => true,
	) );
	Carmilla_Kernel::resolve_features();
	$res_after = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/my-psych-tests/' . $cns_attempt_id . '/result' ) );
	assert_029( 200 === $res_after->get_status() && 'تفسیر آزمایشی مشاور' === ( $res_after->get_data()['interpretation'] ?? '' ), 'User psych test data preserved after toggle off/on' );
} finally {
	if ( null === $orig_features ) {
		delete_option( 'cb_manifest_features' );
	} else {
		update_option( 'cb_manifest_features', $orig_features );
	}
	Carmilla_Kernel::resolve_features();

	foreach ( $created_tests as $tid ) {
		if ( $tid > 0 ) {
			wp_delete_post( $tid, true );
		}
	}
	if ( $u1_id > 0 ) {
		delete_user_meta( $u1_id, 'cb_psych_attempts' );
		delete_user_meta( $u1_id, 'cb_psych_attempt_seq' );
	}
	if ( $u2_id > 0 ) {
		delete_user_meta( $u2_id, 'cb_psych_attempts' );
		delete_user_meta( $u2_id, 'cb_psych_attempt_seq' );
	}
	wp_set_current_user( 0 );
}

echo "P04-WORDPRESS-CODE-029: {$passed} passed, {$failed} failed.\n";
exit( $failed > 0 ? 1 : 0 );
