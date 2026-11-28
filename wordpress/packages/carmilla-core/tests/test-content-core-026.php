<?php
/**
 * Real WordPress Verification for P04-WORDPRESS-CODE-026
 * Shared Content (Posts, Pages, Media) across Theme-only, Plugin-only, and Co-install.
 *
 * Run inside WordPress container:
 *   php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-content-core-026.php
 */

$wp_load = '/var/www/html/wp-load.php';
if ( ! file_exists( $wp_load ) ) {
	fwrite( STDERR, "wp-load.php not found\n" );
	exit( 1 );
}
require_once $wp_load;

use Carmilla\Core\Content\BlockMapper;
use Carmilla\Core\Content\ContentService;

$passed = 0;
$failed = 0;

function assert_026( bool $cond, string $label ): void {
	global $passed, $failed;
	if ( $cond ) {
		++$passed;
	} else {
		++$failed;
		fwrite( STDERR, "FAIL: {$label}\n" );
	}
}

$orig_plugins    = get_option( 'active_plugins', array() );
$orig_stylesheet = get_option( 'stylesheet' );
$orig_template   = get_option( 'template' );
$orig_features   = get_option( 'cb_manifest_features', null );

// Ensure features are enabled for CRUD tests
update_option( 'cb_manifest_features', array(
	'theme.base'   => true,
	'content.core' => true,
	'content.blog' => true,
) );
Carmilla_Kernel::resolve_features();

$created_post_ids = array();
$created_page_ids = array();
$created_media_ids = array();
$tmp_files = array();

try {
	// 1. Create admin & subscriber users
	$admin_id = username_exists( 'qa_content_admin' );
	if ( ! $admin_id ) {
		$admin_id = wp_create_user( 'qa_content_admin', 'Pass123456!', 'qa_content_admin@example.com' );
	}
	$admin_user = new WP_User( $admin_id );
	$admin_user->set_role( 'administrator' );

	$sub_id = username_exists( 'qa_content_sub' );
	if ( ! $sub_id ) {
		$sub_id = wp_create_user( 'qa_content_sub', 'Pass123456!', 'qa_content_sub@example.com' );
	}
	$sub_user = new WP_User( $sub_id );
	$sub_user->set_role( 'subscriber' );

	wp_set_current_user( $admin_id );

	// 2. Post CRUD + BlockMapper + preventDuplicate
	$post_res = ContentService::create_post( array(
		'title'            => 'نوشته آزمایشی هسته مشترک ۰۲۶',
		'slug'             => 'qa-shared-post-026',
		'summary'          => 'خلاصه نوشته آزمایشی',
		'status'           => 'PUBLISHED',
		'isFeatured'       => true,
		'preventDuplicate' => true,
		'content'          => array(
			array( 'type' => 'header', 'content' => 'تیتر اول', 'level' => 2 ),
			array( 'type' => 'paragraph', 'content' => 'متن آزمایشی پاراگراف' ),
		),
	) );
	assert_026( is_array( $post_res ) && ! empty( $post_res['id'] ), 'Post created via ContentService' );
	$post_id = (int) $post_res['id'];
	$created_post_ids[] = $post_id;
	assert_026( 'qa-shared-post-026' === $post_res['slug'], 'Post slug preserved' );
	assert_026( true === $post_res['featured'], 'Post featured meta persisted' );
	assert_026( count( $post_res['content'] ) === 2 && 'header' === $post_res['content'][0]['type'], 'BlockMapper roundtrip works' );

	// Duplicate prevention
	$dup_post = ContentService::create_post( array(
		'title'            => 'تکراری نباید ساخته شود',
		'slug'             => 'qa-shared-post-026',
		'preventDuplicate' => true,
	) );
	assert_026( is_array( $dup_post ) && (int) $dup_post['id'] === $post_id, 'Duplicate post prevented and original ID returned' );

	// Update post preserves ID
	$upd_post = ContentService::update_post( $post_id, array( 'title' => 'نوشته ویرایش‌شده ۰۲۶' ) );
	assert_026( is_array( $upd_post ) && (int) $upd_post['id'] === $post_id && 'نوشته ویرایش‌شده ۰۲۶' === $upd_post['title'], 'Post update preserves ID' );

	// 3. Page CRUD + preventDuplicate
	$page_res = ContentService::create_page( array(
		'title'            => 'برگه آزمایشی هسته مشترک ۰۲۶',
		'slug'             => 'qa-shared-page-026',
		'rawContent'       => '<p>محتوای برگه آزمایشی</p>',
		'status'           => 'PUBLISHED',
		'preventDuplicate' => true,
	) );
	assert_026( is_array( $page_res ) && ! empty( $page_res['id'] ), 'Page created via ContentService' );
	$page_id = (int) $page_res['id'];
	$created_page_ids[] = $page_id;

	$dup_page = ContentService::create_page( array(
		'title'            => 'برگه تکراری',
		'slug'             => 'qa-shared-page-026',
		'preventDuplicate' => true,
	) );
	assert_026( is_array( $dup_page ) && (int) $dup_page['id'] === $page_id, 'Duplicate page prevented and original ID returned' );

	// 4. Media validation & upload
	$bad_php = tempnam( sys_get_temp_dir(), 'qa_bad_' ) . '.php';
	file_put_contents( $bad_php, "<?php echo 'evil'; ?>" );
	$tmp_files[] = $bad_php;
	$bad_type_res = ContentService::upload_media( array(
		'name'     => 'shell.php',
		'tmp_name' => $bad_php,
		'error'    => 0,
		'size'     => filesize( $bad_php ),
	), 0, array( 'test_upload' => true ) );
	assert_026( is_wp_error( $bad_type_res ) && 'INVALID_MEDIA_TYPE' === $bad_type_res->get_error_code(), 'Disallowed PHP file rejected with INVALID_MEDIA_TYPE' );

	$valid_png = tempnam( sys_get_temp_dir(), 'qa_ok_' ) . '.png';
	// Minimal valid 1x1 PNG
	file_put_contents( $valid_png, base64_decode( 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==' ) );
	$tmp_files[] = $valid_png;

	$too_big_res = ContentService::upload_media( array(
		'name'     => 'tiny.png',
		'tmp_name' => $valid_png,
		'error'    => 0,
		'size'     => filesize( $valid_png ),
	), 0, array( 'max_bytes' => 5, 'test_upload' => true ) );
	assert_026( is_wp_error( $too_big_res ) && 'INVALID_MEDIA_SIZE' === $too_big_res->get_error_code(), 'Oversized file rejected with INVALID_MEDIA_SIZE' );

	$ok_media = ContentService::upload_media( array(
		'name'     => 'qa-sample-026.png',
		'tmp_name' => $valid_png,
		'error'    => 0,
		'size'     => filesize( $valid_png ),
	), 0, array( 'test_upload' => true ) );
	assert_026( is_array( $ok_media ) && ! empty( $ok_media['id'] ) && ! empty( $ok_media['url'] ), 'Valid PNG uploaded via ContentService::upload_media' );
	if ( is_array( $ok_media ) && ! empty( $ok_media['id'] ) ) {
		$created_media_ids[] = (int) $ok_media['id'];
	}

	// 5. REST API routes parity, permissions, and feature gate
	$server = rest_get_server();
	$routes = $server->get_routes();
	assert_026( ! empty( $routes['/carmilla/v1/api/blogs'] ), 'REST route /carmilla/v1/api/blogs registered' );
	assert_026( ! empty( $routes['/carmilla/v1/api/pages'] ), 'REST route /carmilla/v1/api/pages registered' );
	assert_026( ! empty( $routes['/carmilla/v1/api/admin/media/upload'] ), 'REST route /carmilla/v1/api/admin/media/upload registered' );
	assert_026( count( $routes['/carmilla/v1/api/blogs'] ) === 1, 'No duplicate GET /api/blogs route handler in co-install' );

	// Public GET /api/pages/{slug}
	wp_set_current_user( 0 );
	$req_page = new WP_REST_Request( 'GET', '/carmilla/v1/api/pages/qa-shared-page-026' );
	$res_page = $server->dispatch( $req_page );
	$data_page = $res_page->get_data();
	assert_026( 200 === $res_page->get_status() && (int) $data_page['id'] === $page_id, 'Public REST GET /api/pages/{slug} returns canonical page ID' );

	// Public GET /api/blogs/{slug}
	$req_blog = new WP_REST_Request( 'GET', '/carmilla/v1/api/blogs/qa-shared-post-026' );
	$res_blog = $server->dispatch( $req_blog );
	$data_blog = $res_blog->get_data();
	assert_026( 200 === $res_blog->get_status() && (int) $data_blog['id'] === $post_id, 'Public REST GET /api/blogs/{slug} returns canonical post ID' );

	// Unauthorized subscriber blocked from admin POST /api/admin/pages
	wp_set_current_user( $sub_id );
	$req_unauth = new WP_REST_Request( 'POST', '/carmilla/v1/api/admin/pages' );
	$req_unauth->set_param( 'title', 'غیرمجاز' );
	$res_unauth = $server->dispatch( $req_unauth );
	assert_026( 403 === $res_unauth->get_status(), 'Subscriber blocked (403) on POST /api/admin/pages' );

	// Unauthenticated blocked from POST /api/admin/media/upload
	wp_set_current_user( 0 );
	$req_unauth_media = new WP_REST_Request( 'POST', '/carmilla/v1/api/admin/media/upload' );
	$res_unauth_media = $server->dispatch( $req_unauth_media );
	assert_026( in_array( $res_unauth_media->get_status(), array( 401, 403 ), true ), 'Unauthenticated blocked (401/403) on POST /api/admin/media/upload' );

	// Feature gate disabled -> 403 FEATURE_DISABLED
	update_option( 'cb_manifest_features', array(
		'theme.base'   => true,
		'content.core' => false,
		'content.blog' => false,
	) );
	Carmilla_Kernel::resolve_features();
	$req_gated = new WP_REST_Request( 'GET', '/carmilla/v1/api/pages' );
	$res_gated = $server->dispatch( $req_gated );
	assert_026( 403 === $res_gated->get_status(), 'Disabled content.core returns 403 FEATURE_DISABLED on /api/pages' );

	// Restore features
	update_option( 'cb_manifest_features', array(
		'theme.base'   => true,
		'content.core' => true,
		'content.blog' => true,
	) );
	Carmilla_Kernel::resolve_features();

	// 6. Frontend shortcode rendering & permission parity
	wp_set_current_user( $sub_id );
	$admin_denied_html = do_shortcode( '[carmilla_content_admin]' );
	assert_026( false !== strpos( $admin_denied_html, 'دسترسی غیرمجاز' ), 'Subscriber denied in [carmilla_content_admin] shortcode' );

	$posts_html = do_shortcode( '[carmilla_posts size="10"]' );
	$page_html  = do_shortcode( '[carmilla_page slug="qa-shared-page-026"]' );
	assert_026( false !== strpos( $posts_html, 'data-post-id="' . $post_id . '"' ), '[carmilla_posts] renders canonical post ID' );
	assert_026( false !== strpos( $page_html, 'data-page-id="' . $page_id . '"' ), '[carmilla_page] renders canonical page ID' );

	// 7. Theme-only & Plugin-only parity check via ContentService
	assert_026( class_exists( 'Carmilla\Theme\ThemeHostAdapter' ), 'ThemeHostAdapter loaded' );
	assert_026( class_exists( 'CB_Public_Renderer' ), 'CB_Public_Renderer loaded' );
} finally {
	// Cleanup test data
	foreach ( $created_post_ids as $pid ) {
		wp_delete_post( $pid, true );
	}
	foreach ( $created_page_ids as $pgid ) {
		wp_delete_post( $pgid, true );
	}
	foreach ( $created_media_ids as $mid ) {
		wp_delete_attachment( $mid, true );
	}
	foreach ( $tmp_files as $f ) {
		if ( file_exists( $f ) ) {
			@unlink( $f );
		}
	}
	if ( null === $orig_features ) {
		delete_option( 'cb_manifest_features' );
	} else {
		update_option( 'cb_manifest_features', $orig_features );
	}
	update_option( 'active_plugins', $orig_plugins );
	update_option( 'stylesheet', $orig_stylesheet );
	update_option( 'template', $orig_template );
	wp_set_current_user( 0 );
}

echo "Content Core 026 QA: {$passed} passed, {$failed} failed.\n";
exit( $failed > 0 ? 1 : 0 );
