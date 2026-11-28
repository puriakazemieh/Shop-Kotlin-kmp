<?php
/**
 * Real WordPress Verification for P04-WORDPRESS-CODE-029A
 * Shared Support Ticket, Thread & Admin Management Service.
 *
 * Run inside WordPress container:
 *   php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-support-029a.php
 */

$wp_load = '/var/www/html/wp-load.php';
if ( ! file_exists( $wp_load ) ) {
	fwrite( STDERR, "wp-load.php not found\n" );
	exit( 1 );
}
require_once $wp_load;

$passed = 0;
$failed = 0;

function assert_029a( bool $cond, string $label ): void {
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
	'theme.base'      => true,
	'content.support' => true,
) );
Carmilla_Kernel::resolve_features();

$u1_id = 0;
$u2_id = 0;

try {
	$admin_id = username_exists( 'qa_support_admin' );
	if ( ! $admin_id ) {
		$admin_id = wp_create_user( 'qa_support_admin', 'Pass123456!', 'qa_support_admin@example.com' );
	}
	( new WP_User( $admin_id ) )->set_role( 'administrator' );

	$u1_id = username_exists( 'qa_support_u1' );
	if ( ! $u1_id ) {
		$u1_id = wp_create_user( 'qa_support_u1', 'Pass123456!', 'qa_support_u1@example.com' );
	}
	( new WP_User( $u1_id ) )->set_role( 'subscriber' );

	$u2_id = username_exists( 'qa_support_u2' );
	if ( ! $u2_id ) {
		$u2_id = wp_create_user( 'qa_support_u2', 'Pass123456!', 'qa_support_u2@example.com' );
	}
	( new WP_User( $u2_id ) )->set_role( 'subscriber' );

	delete_user_meta( $u1_id, 'cb_tickets' );
	delete_user_meta( $u2_id, 'cb_tickets' );

	$server = rest_get_server();
	$routes = $server->get_routes();
	assert_029a( ! empty( $routes['/carmilla/v1/api/support/tickets'] ) && count( $routes['/carmilla/v1/api/support/tickets'] ) === 2, 'REST /api/support/tickets registered once (GET+POST)' );
	assert_029a( ! empty( $routes['/carmilla/v1/support'] ) && count( $routes['/carmilla/v1/support'] ) === 2, 'Legacy /support alias registered once (GET+POST)' );
	assert_029a( ! empty( $routes['/carmilla/v1/api/admin/support/tickets'] ), 'Admin /api/admin/support/tickets registered' );

	// 1. Anonymous rejected
	wp_set_current_user( 0 );
	$res_anon = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/support/tickets' ) );
	assert_029a( in_array( $res_anon->get_status(), array( 401, 403 ), true ), 'Anonymous user blocked from support tickets' );

	// 2. User 1 creates ticket & posts reply
	wp_set_current_user( $u1_id );
	$req_create = new WP_REST_Request( 'POST', '/carmilla/v1/api/support/tickets' );
	$req_create->set_header( 'content-type', 'application/json' );
	$req_create->set_body( wp_json_encode( array( 'subject' => 'سؤال پشتیبانی آزمایشی', 'message' => 'سلام، نیاز به راهنمایی دارم.' ) ) );
	$res_create = $server->dispatch( $req_create );
	$t_data     = $res_create->get_data();
	$tid        = (int) ( $t_data['id'] ?? 0 );
	assert_029a( 201 === $res_create->get_status() && $tid > 0 && 'OPEN' === ( $t_data['status'] ?? '' ), 'User 1 creates support ticket (201, OPEN)' );

	$req_msg = new WP_REST_Request( 'POST', '/carmilla/v1/api/support/tickets/' . $tid . '/messages' );
	$req_msg->set_header( 'content-type', 'application/json' );
	$req_msg->set_body( wp_json_encode( array( 'body' => 'پیام تکمیلی کاربر' ) ) );
	$res_msg = $server->dispatch( $req_msg );
	assert_029a( 200 === $res_msg->get_status() && count( $res_msg->get_data()['messages'] ?? array() ) === 2, 'User 1 posts follow-up message (2 messages)' );

	// 3. Legacy /support alias
	$res_leg_get = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/support' ) );
	assert_029a( 200 === $res_leg_get->get_status() && count( $res_leg_get->get_data()['messages'] ?? array() ) === 2, 'Legacy GET /support returns thread messages' );

	// 4. Shortcodes for owner
	$html_list   = do_shortcode( '[carmilla_support_tickets]' );
	$html_thread = do_shortcode( '[carmilla_support_thread id="' . $tid . '"]' );
	assert_029a( strpos( $html_list, 'data-carmilla-surface="support-tickets"' ) !== false && strpos( $html_list, 'سؤال پشتیبانی آزمایشی' ) !== false, 'Support tickets shortcode renders for owner' );
	assert_029a( strpos( $html_thread, 'data-carmilla-surface="support-thread"' ) !== false && strpos( $html_thread, 'پیام تکمیلی کاربر' ) !== false, 'Support thread shortcode renders for owner' );

	// 5. IDOR check: User 2 cannot read/post/render User 1's ticket
	wp_set_current_user( $u2_id );
	$res_idor_get  = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/support/tickets/' . $tid ) );
	$res_idor_post = $server->dispatch( new WP_REST_Request( 'POST', '/carmilla/v1/api/support/tickets/' . $tid . '/messages' ) );
	$html_idor     = do_shortcode( '[carmilla_support_thread id="' . $tid . '"]' );
	assert_029a( 404 === $res_idor_get->get_status(), 'IDOR blocked: User 2 gets 404 on User 1 ticket' );
	assert_029a( 404 === $res_idor_post->get_status() || 400 === $res_idor_post->get_status(), 'IDOR blocked: User 2 cannot post to User 1 ticket' );
	assert_029a( strpos( $html_idor, 'data-carmilla-surface="support-not-found"' ) !== false && strpos( $html_idor, 'پیام تکمیلی کاربر' ) === false, 'Shortcode does not leak User 1 thread to User 2' );

	// 6. Subscriber blocked from admin support routes
	$res_sub_adm = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/admin/support/tickets' ) );
	assert_029a( 403 === $res_sub_adm->get_status(), 'Subscriber blocked from admin support list' );

	// 7. Data isolation: no cb_psych_attempts created
	assert_029a( empty( get_user_meta( $u1_id, 'cb_psych_attempts', true ) ), 'Support tickets isolated from cb_psych_attempts' );

	// 8. Admin lists, replies (ANSWERED), and closes (CLOSED)
	wp_set_current_user( $admin_id );
	$res_adm = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/admin/support/tickets' ) );
	assert_029a( 200 === $res_adm->get_status() && is_array( $res_adm->get_data() ) && count( $res_adm->get_data() ) >= 1, 'Admin lists support tickets' );

	$req_reply = new WP_REST_Request( 'POST', '/carmilla/v1/api/admin/support/tickets/' . $u1_id . '/' . $tid . '/reply' );
	$req_reply->set_header( 'content-type', 'application/json' );
	$req_reply->set_body( wp_json_encode( array( 'body' => 'پاسخ تیم پشتیبانی' ) ) );
	$res_reply = $server->dispatch( $req_reply );
	assert_029a( 200 === $res_reply->get_status() && 'ANSWERED' === ( $res_reply->get_data()['status'] ?? '' ), 'Admin reply sets status ANSWERED' );

	$req_close = new WP_REST_Request( 'POST', '/carmilla/v1/api/admin/support/tickets/' . $u1_id . '/' . $tid . '/status' );
	$req_close->set_header( 'content-type', 'application/json' );
	$req_close->set_body( wp_json_encode( array( 'status' => 'CLOSED' ) ) );
	$res_close = $server->dispatch( $req_close );
	assert_029a( 200 === $res_close->get_status() && 'CLOSED' === ( $res_close->get_data()['status'] ?? '' ), 'Admin sets status CLOSED' );

	// 9. Feature disabled gate
	update_option( 'cb_manifest_features', array(
		'theme.base'      => true,
		'content.support' => false,
	) );
	Carmilla_Kernel::resolve_features();
	wp_set_current_user( $u1_id );
	$res_dis  = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/support/tickets' ) );
	$dis_data = $res_dis->get_data();
	$dis_code = $dis_data['errorCode'] ?? $dis_data['code'] ?? '';
	assert_029a( 403 === $res_dis->get_status() && 'FEATURE_DISABLED' === $dis_code, 'Disabled content.support returns 403 FEATURE_DISABLED' );
	$html_dis = do_shortcode( '[carmilla_support_tickets]' );
	assert_029a( strpos( $html_dis, 'data-carmilla-surface="support-disabled"' ) !== false, 'Disabled content.support shortcode renders disabled notice' );
} finally {
	if ( null === $orig_features ) {
		delete_option( 'cb_manifest_features' );
	} else {
		update_option( 'cb_manifest_features', $orig_features );
	}
	Carmilla_Kernel::resolve_features();

	if ( $u1_id > 0 ) {
		delete_user_meta( $u1_id, 'cb_tickets' );
	}
	if ( $u2_id > 0 ) {
		delete_user_meta( $u2_id, 'cb_tickets' );
	}
	wp_set_current_user( 0 );
}

echo "P04-WORDPRESS-CODE-029A: {$passed} passed, {$failed} failed.\n";
exit( $failed > 0 ? 1 : 0 );
