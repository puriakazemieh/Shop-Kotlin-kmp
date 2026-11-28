<?php
/**
 * Real WordPress Verification for P04-WORDPRESS-CODE-029B
 * Shared Content & Product Interactions (Reviews, Q&A & Favorites).
 *
 * Run inside WordPress container:
 *   php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-interactions-029b.php
 */

$wp_load = '/var/www/html/wp-load.php';
if ( ! file_exists( $wp_load ) ) {
	fwrite( STDERR, "wp-load.php not found\n" );
	exit( 1 );
}
require_once $wp_load;

$passed = 0;
$failed = 0;

function assert_029b( bool $cond, string $label ): void {
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
	'theme.base'    => true,
	'commerce.core' => true,
) );
Carmilla_Kernel::resolve_features();

$product_id = 0;
$u1_id      = 0;
$u2_id      = 0;

try {
	$u1_id = username_exists( 'qa_inter_u1' );
	if ( ! $u1_id ) {
		$u1_id = wp_create_user( 'qa_inter_u1', 'Pass123456!', 'qa_inter_u1@example.com' );
	}
	( new WP_User( $u1_id ) )->set_role( 'subscriber' );

	$u2_id = username_exists( 'qa_inter_u2' );
	if ( ! $u2_id ) {
		$u2_id = wp_create_user( 'qa_inter_u2', 'Pass123456!', 'qa_inter_u2@example.com' );
	}
	( new WP_User( $u2_id ) )->set_role( 'subscriber' );

	delete_user_meta( $u1_id, 'cb_wishlist' );

	$product_id = wp_insert_post( array(
		'post_type'   => 'post',
		'post_status' => 'publish',
		'post_title'  => 'محصول آزمایشی تعاملات ۰۲۹ب',
	) );
	assert_029b( $product_id > 0, 'Target post/product fixture created' );

	$server = rest_get_server();
	$routes = $server->get_routes();
	assert_029b( ! empty( $routes['/carmilla/v1/api/reviews'] ) && count( $routes['/carmilla/v1/api/reviews'] ) === 1, 'REST /api/reviews registered once' );
	assert_029b( ! empty( $routes['/carmilla/v1/api/questions'] ) && count( $routes['/carmilla/v1/api/questions'] ) === 1, 'REST /api/questions registered once' );
	assert_029b( ! empty( $routes['/carmilla/v1/api/favorites'] ) && count( $routes['/carmilla/v1/api/favorites'] ) === 1, 'REST /api/favorites registered once' );

	// 1. Review CRUD + helpful + ownership
	wp_set_current_user( $u1_id );
	$req_rev = new WP_REST_Request( 'POST', '/carmilla/v1/api/reviews' );
	$req_rev->set_header( 'content-type', 'application/json' );
	$req_rev->set_body( wp_json_encode( array( 'productId' => $product_id, 'comment' => 'کیفیت عالی بود', 'rating' => 5 ) ) );
	$res_rev = $server->dispatch( $req_rev );
	$rev_id  = (int) ( $res_rev->get_data()['id'] ?? 0 );
	assert_029b( 201 === $res_rev->get_status() && $rev_id > 0 && 5 === (int) ( $res_rev->get_data()['rating'] ?? 0 ), 'User 1 creates review (201, rating=5)' );

	$req_upd = new WP_REST_Request( 'PUT', '/carmilla/v1/api/reviews/' . $rev_id );
	$req_upd->set_header( 'content-type', 'application/json' );
	$req_upd->set_body( wp_json_encode( array( 'comment' => 'کیفیت بسیار عالی', 'rating' => 4 ) ) );
	$res_upd = $server->dispatch( $req_upd );
	assert_029b( 200 === $res_upd->get_status() && 4 === (int) ( $res_upd->get_data()['rating'] ?? 0 ), 'User 1 updates own review' );

	$res_help = $server->dispatch( new WP_REST_Request( 'POST', '/carmilla/v1/api/reviews/' . $rev_id . '/helpful' ) );
	assert_029b( 200 === $res_help->get_status() && 1 === (int) ( $res_help->get_data()['helpfulCount'] ?? 0 ), 'Helpful vote increments helpfulCount' );

	wp_set_current_user( $u2_id );
	$res_u2_del = $server->dispatch( new WP_REST_Request( 'DELETE', '/carmilla/v1/api/reviews/' . $rev_id ) );
	assert_029b( 404 === $res_u2_del->get_status(), 'User 2 cannot delete User 1 review (404)' );

	// 2. Q&A CRUD + feed isolation
	wp_set_current_user( $u1_id );
	$req_q = new WP_REST_Request( 'POST', '/carmilla/v1/api/questions' );
	$req_q->set_header( 'content-type', 'application/json' );
	$req_q->set_body( wp_json_encode( array( 'productId' => $product_id, 'content' => 'آیا گارانتی دارد؟' ) ) );
	$res_q = $server->dispatch( $req_q );
	$q_id  = (int) ( $res_q->get_data()['id'] ?? 0 );
	assert_029b( 201 === $res_q->get_status() && $q_id > 0, 'User 1 creates Q&A question (201)' );

	$normal_feed = get_comments( array( 'post_id' => $product_id, 'status' => 'approve' ) );
	$qna_leaked  = false;
	foreach ( $normal_feed as $nc ) {
		if ( (int) $nc->comment_ID === $q_id ) {
			$qna_leaked = true;
		}
	}
	assert_029b( ! $qna_leaked, 'cb_qna comment hidden from standard WP comment feed' );

	// 3. Favorites CRUD
	$res_fav_add = $server->dispatch( new WP_REST_Request( 'POST', '/carmilla/v1/api/favorites/' . $product_id ) );
	$res_fav_get = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/favorites' ) );
	$fav_content = $res_fav_get->get_data()['content'] ?? array();
	assert_029b( 200 === $res_fav_add->get_status() && 200 === $res_fav_get->get_status() && count( $fav_content ) === 1 && (int) $fav_content[0]['id'] === $product_id, 'Favorites add & list works' );

	// 4. Shortcodes
	$html_rev = do_shortcode( '[carmilla_product_reviews product_id="' . $product_id . '"]' );
	$html_fav = do_shortcode( '[carmilla_favorites]' );
	assert_029b( strpos( $html_rev, 'data-carmilla-surface="product-reviews"' ) !== false && strpos( $html_rev, 'کیفیت بسیار عالی' ) !== false, 'Product reviews shortcode renders' );
	assert_029b( strpos( $html_fav, 'data-carmilla-surface="favorites"' ) !== false && strpos( $html_fav, 'محصول آزمایشی تعاملات ۰۲۹ب' ) !== false, 'Favorites shortcode renders' );

	$res_fav_del = $server->dispatch( new WP_REST_Request( 'DELETE', '/carmilla/v1/api/favorites/' . $product_id ) );
	assert_029b( 200 === $res_fav_del->get_status() && empty( get_user_meta( $u1_id, 'cb_wishlist', true ) ), 'Favorites remove works' );

	// 5. Delete own review & question
	$res_del_r = $server->dispatch( new WP_REST_Request( 'DELETE', '/carmilla/v1/api/reviews/' . $rev_id ) );
	$res_del_q = $server->dispatch( new WP_REST_Request( 'DELETE', '/carmilla/v1/api/questions/' . $q_id ) );
	assert_029b( 204 === $res_del_r->get_status() && 204 === $res_del_q->get_status(), 'Owner deletes review & question (204)' );

	// 6. Feature disabled gate
	update_option( 'cb_manifest_features', array(
		'theme.base'    => true,
		'commerce.core' => false,
	) );
	Carmilla_Kernel::resolve_features();
	$res_dis  = $server->dispatch( new WP_REST_Request( 'GET', '/carmilla/v1/api/reviews/product/' . $product_id ) );
	$dis_data = $res_dis->get_data();
	$dis_code = $dis_data['errorCode'] ?? $dis_data['code'] ?? '';
	assert_029b( 403 === $res_dis->get_status() && 'FEATURE_DISABLED' === $dis_code, 'Disabled commerce.core returns 403 FEATURE_DISABLED on reviews' );
	$html_dis = do_shortcode( '[carmilla_product_reviews product_id="' . $product_id . '"]' );
	assert_029b( strpos( $html_dis, 'data-carmilla-surface="interactions-disabled"' ) !== false, 'Disabled commerce.core shortcode renders disabled notice' );
} finally {
	if ( null === $orig_features ) {
		delete_option( 'cb_manifest_features' );
	} else {
		update_option( 'cb_manifest_features', $orig_features );
	}
	Carmilla_Kernel::resolve_features();

	if ( $product_id > 0 ) {
		wp_delete_post( $product_id, true );
	}
	if ( $u1_id > 0 ) {
		delete_user_meta( $u1_id, 'cb_wishlist' );
	}
	wp_set_current_user( 0 );
}

echo "P04-WORDPRESS-CODE-029B: {$passed} passed, {$failed} failed.\n";
exit( $failed > 0 ? 1 : 0 );
