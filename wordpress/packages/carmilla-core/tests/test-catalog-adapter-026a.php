<?php
/**
 * Real WordPress Verification for P04-WORDPRESS-CODE-026A
 * Shared WooCommerce Catalog Adapter (Categories, Products, Search, Media Mapping).
 *
 * Run inside WordPress container:
 *   php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-catalog-adapter-026a.php
 */

$wp_load = '/var/www/html/wp-load.php';
if ( ! file_exists( $wp_load ) ) {
	fwrite( STDERR, "wp-load.php not found\n" );
	exit( 1 );
}
require_once $wp_load;

use Carmilla\Core\Adapter\WooCommerceAdapter;
use Carmilla\Core\Catalog\CatalogService;

$passed = 0;
$failed = 0;

function assert_026a( bool $cond, string $label ): void {
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

$created_product_ids = array();

try {
	$server = rest_get_server();
	$routes = $server->get_routes();

	// 1. Singular REST routes registered
	assert_026a( ! empty( $routes['/carmilla/v1/api/products'] ), 'REST route /carmilla/v1/api/products registered' );
	assert_026a( ! empty( $routes['/carmilla/v1/api/categories'] ), 'REST route /carmilla/v1/api/categories registered' );
	assert_026a( ! empty( $routes['/carmilla/v1/api/admin/products'] ), 'REST route /carmilla/v1/api/admin/products registered' );
	assert_026a( count( $routes['/carmilla/v1/api/products'] ) === 1, 'No duplicate /api/products handler in co-install' );

	// 2. Unauthorized subscriber blocked on admin product creation
	$sub_id = username_exists( 'qa_catalog_sub' );
	if ( ! $sub_id ) {
		$sub_id = wp_create_user( 'qa_catalog_sub', 'Pass123456!', 'qa_catalog_sub@example.com' );
	}
	$sub_user = new WP_User( $sub_id );
	$sub_user->set_role( 'subscriber' );
	wp_set_current_user( $sub_id );

	$req_unauth = new WP_REST_Request( 'POST', '/carmilla/v1/api/admin/products' );
	$req_unauth->set_param( 'title', 'محصول غیرمجاز' );
	$res_unauth = $server->dispatch( $req_unauth );
	assert_026a( 403 === $res_unauth->get_status(), 'Subscriber blocked (403) on POST /api/admin/products' );

	// 3. Feature gate disabled -> 403 FEATURE_DISABLED
	wp_set_current_user( 0 );
	update_option( 'cb_manifest_features', array(
		'theme.base'    => true,
		'commerce.core' => false,
	) );
	Carmilla_Kernel::resolve_features();
	$req_gated = new WP_REST_Request( 'GET', '/carmilla/v1/api/products' );
	$res_gated = $server->dispatch( $req_gated );
	assert_026a( 403 === $res_gated->get_status(), 'Disabled commerce.core returns 403 FEATURE_DISABLED on /api/products' );

	update_option( 'cb_manifest_features', array(
		'theme.base'    => true,
		'commerce.core' => true,
	) );
	Carmilla_Kernel::resolve_features();

	// 4. WooCommerce active vs inactive behavior
	if ( WooCommerceAdapter::is_available() && function_exists( 'wc_get_products' ) ) {
		$admin_id = username_exists( 'qa_catalog_admin' );
		if ( ! $admin_id ) {
			$admin_id = wp_create_user( 'qa_catalog_admin', 'Pass123456!', 'qa_catalog_admin@example.com' );
		}
		( new WP_User( $admin_id ) )->set_role( 'administrator' );
		wp_set_current_user( $admin_id );

		$created = CatalogService::create_product( array(
			'title'     => 'محصول آزمایشی ۰۲۶A',
			'slug'      => 'qa-catalog-product-026a',
			'basePrice' => 150000,
			'isActive'  => true,
		) );
		assert_026a( is_array( $created ) && ! empty( $created['id'] ), 'Product created via CatalogService' );
		$pid = (int) $created['id'];
		$created_product_ids[] = $pid;

		$list = CatalogService::list_products( array( 'query' => '۰۲۶A' ) );
		assert_026a( is_array( $list ) && ! empty( $list['content'] ) && (int) $list['content'][0]['id'] === $pid, 'Search finds product with constant ID' );

		$detail = CatalogService::get_product_detail( 'qa-catalog-product-026a' );
		assert_026a( is_array( $detail ) && (int) $detail['id'] === $pid, 'Detail by slug returns constant ID' );

		$req_list = new WP_REST_Request( 'GET', '/carmilla/v1/api/products' );
		$req_list->set_param( 'query', '۰۲۶A' );
		$res_list = $server->dispatch( $req_list );
		assert_026a( 200 === $res_list->get_status() && (int) $res_list->get_data()['content'][0]['id'] === $pid, 'REST /api/products matches CatalogService ID' );

		$sc_list = do_shortcode( '[carmilla_catalog query="۰۲۶A"]' );
		$sc_det  = do_shortcode( '[carmilla_product id="' . $pid . '"]' );
		assert_026a( false !== strpos( $sc_list, 'data-product-id="' . $pid . '"' ), '[carmilla_catalog] renders constant product ID' );
		assert_026a( false !== strpos( $sc_det, 'data-product-id="' . $pid . '"' ), '[carmilla_product] renders constant product ID' );
	} else {
		// WooCommerce is NOT active: verify clean prerequisite message & 503 without fatal
		$list = CatalogService::list_products();
		assert_026a( is_wp_error( $list ) && 'WOOCOMMERCE_INACTIVE' === $list->get_error_code(), 'CatalogService::list_products returns WOOCOMMERCE_INACTIVE without Woo' );

		$cats = CatalogService::list_categories();
		assert_026a( is_wp_error( $cats ) && 'WOOCOMMERCE_INACTIVE' === $cats->get_error_code(), 'CatalogService::list_categories returns WOOCOMMERCE_INACTIVE without Woo' );

		$det = CatalogService::get_product_detail( 1 );
		assert_026a( is_wp_error( $det ) && 'WOOCOMMERCE_INACTIVE' === $det->get_error_code(), 'CatalogService::get_product_detail returns WOOCOMMERCE_INACTIVE without Woo' );

		$req_list = new WP_REST_Request( 'GET', '/carmilla/v1/api/products' );
		$res_list = $server->dispatch( $req_list );
		assert_026a( 503 === $res_list->get_status() && 'WOOCOMMERCE_INACTIVE' === $res_list->get_data()['errorCode'], 'REST /api/products returns 503 WOOCOMMERCE_INACTIVE without fatal' );

		$sc_list = do_shortcode( '[carmilla_catalog]' );
		$sc_det  = do_shortcode( '[carmilla_product id="1"]' );
		assert_026a( false !== strpos( $sc_list, 'WooCommerce' ), '[carmilla_catalog] renders prerequisite notice without fatal' );
		assert_026a( false !== strpos( $sc_det, 'WooCommerce' ), '[carmilla_product] renders prerequisite notice without fatal' );
	}
} finally {
	foreach ( $created_product_ids as $pid ) {
		CatalogService::delete_product( $pid );
	}
	if ( null === $orig_features ) {
		delete_option( 'cb_manifest_features' );
	} else {
		update_option( 'cb_manifest_features', $orig_features );
	}
	wp_set_current_user( 0 );
}

echo "Catalog Adapter 026A QA: {$passed} passed, {$failed} failed.\n";
exit( $failed > 0 ? 1 : 0 );
