<?php
/** Disposable installed WordPress/WooCommerce only: synthetic catalog meta query benchmark. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-blog-controller.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-catalog-controller.php';
if ( ! function_exists( 'wc_get_products' ) ) { exit( 2 ); }
global $wpdb;
$suffix = bin2hex( random_bytes( 5 ) );
$title = 'QA catalog perf ' . $suffix;
$post_ids = array();
$product_ids = array();
$failed = 0;
echo "Fixture title: $title\n";
$add_posts = static function ( int $count ) use ( $wpdb, $title, &$post_ids ): void {
	foreach ( array( 'post' => 'cb_is_featured', 'cb_campaign' => 'is_active', 'cb_banner' => 'sort_order' ) as $type => $key ) {
		for ( $remaining = $count; $remaining > 0; $remaining -= 250 ) {
			$batch = min( 250, $remaining );
			$rows = array_fill( 0, $batch, $wpdb->prepare( '(%s,%s,%s,%s,%s)', $title, $type, 'publish', '2026-11-03 12:00:00', '2026-11-03 12:00:00' ) );
			if ( $wpdb->query( "INSERT INTO {$wpdb->posts} (post_title,post_type,post_status,post_date,post_date_gmt) VALUES " . implode( ',', $rows ) ) !== $batch ) { throw new RuntimeException( 'Post fixture failed' ); }
			$first = (int) $wpdb->insert_id;
			$last = $first + $batch - 1;
			for ( $id = $first; $id <= $last; ++$id ) { $post_ids[] = $id; }
			$value = $key === 'sort_order' ? '1' : '1';
			if ( $wpdb->query( $wpdb->prepare( "INSERT INTO {$wpdb->postmeta} (post_id,meta_key,meta_value) SELECT ID,%s,%s FROM {$wpdb->posts} WHERE ID BETWEEN %d AND %d AND post_title=%s", $key, $value, $first, $last, $title ) ) !== $batch ) { throw new RuntimeException( 'Postmeta fixture failed' ); }
		}
	}
};
$add_products = static function ( int $count ) use ( $title, &$product_ids ): void {
	for ( $i = 0; $i < $count; ++$i ) {
		$product = new WC_Product_Simple();
		$product->set_name( $title . ' product ' . count( $product_ids ) );
		$product->set_status( 'publish' );
		$product->set_regular_price( (string) ( 100 + ( $i % 50 ) ) );
		$product->set_catalog_visibility( 'visible' );
		$product_ids[] = $product->save();
	}
};
$measure = static function ( string $label, int $rows, string $needle, callable $call ) use ( $wpdb, &$failed ): void {
	wp_cache_flush();
	$sqls = array();
	$capture = static function ( string $sql ) use ( &$sqls, $needle ): string {
		if ( stripos( $sql, 'SELECT' ) === 0 && strpos( $sql, $needle ) !== false ) { $sqls[] = $sql; }
		return $sql;
	};
	add_filter( 'query', $capture );
	$start = hrtime( true );
	$response = $call();
	$method_ms = ( hrtime( true ) - $start ) / 1000000;
	remove_filter( 'query', $capture );
	$sql = $sqls[0] ?? '';
	if ( $sql === '' ) { ++$failed; echo "FAIL $label no matching query (needle=$needle)\n"; return; }
	$start = hrtime( true );
	$wpdb->get_results( $sql );
	$sql_ms = ( hrtime( true ) - $start ) / 1000000;
	$plan = $wpdb->get_results( 'EXPLAIN ' . $sql, ARRAY_A );
	$actual = json_decode( (string) $wpdb->get_var( 'ANALYZE FORMAT=JSON ' . $sql ), true );
	$ok = $response instanceof WP_REST_Response && $response->get_status() === 200 && is_array( $plan ) && is_array( $actual );
	if ( ! $ok ) { ++$failed; }
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . "$label rows=$rows status=" . $response->get_status() . ' method_ms=' . round( $method_ms, 3 ) . ' sql_ms=' . round( $sql_ms, 3 ) . ' matching_sql=' . count( $sqls ) . "\n";
	echo 'SQL ' . preg_replace( '/\s+/', ' ', $sql ) . "\n";
	if ( strpos( $label, 'price_range_' ) === 0 ) {
		foreach ( array_slice( $sqls, 1 ) as $other ) { echo 'SQL_OTHER ' . preg_replace( '/\s+/', ' ', $other ) . "\n"; }
		$data = $response->get_data();
		$outside = 0;
		foreach ( $data['content'] ?? array() as $item ) {
			if ( ( $item['price'] ?? 0 ) < 110 || ( $item['price'] ?? 0 ) > 130 ) { ++$outside; }
		}
		echo 'PRICE_RANGE_OBS totalElements=' . ( $data['totalElements'] ?? -1 ) . ' pageOutsideRange=' . $outside . "\n";
	}
	echo 'PLAN ' . wp_json_encode( $plan ) . "\n";
	echo 'ACTUAL ' . wp_json_encode( $actual ) . "\n";
};
$blog = new CB_Blog_Controller();
$catalog = new CB_Catalog_Controller();
$empty = new WP_REST_Request( 'GET', '/api/catalog' );
$featured = new WP_REST_Request( 'GET', '/api/blogs/featured' );
$featured->set_param( 'size', 20 );
$price = new WP_REST_Request( 'GET', '/api/products' );
$price->set_param( 'minPrice', 110 );
$price->set_param( 'maxPrice', 130 );
$asc = new WP_REST_Request( 'GET', '/api/products' );
$asc->set_param( 'sort', 'price,asc' );
$desc = new WP_REST_Request( 'GET', '/api/products' );
$desc->set_param( 'sort', 'price,desc' );
try {
	$add_posts( 30 );
	$add_products( 30 );
	foreach ( array( 'small' => 30, 'large' => 270 ) as $phase => $product_additional ) {
		if ( $phase === 'large' ) { $add_posts( 2970 ); $add_products( $product_additional ); }
		$posts = $phase === 'small' ? 30 : 3000;
		$products = $phase === 'small' ? 30 : 300;
		$measure( "featured_$phase", $posts, 'cb_is_featured', static function () use ( $blog, $featured ) { return $blog->featured( $featured ); } );
		$measure( "campaign_$phase", $posts, 'is_active', static function () use ( $catalog, $empty ) { return $catalog->active_campaign( $empty ); } );
		$measure( "banners_$phase", $posts, 'sort_order', static function () use ( $catalog, $empty ) { return $catalog->banners( $empty ); } );
		$measure( "price_range_$phase", $products, $wpdb->posts, static function () use ( $catalog, $price ) { return $catalog->products( $price ); } );
		$measure( "price_asc_$phase", $products, '_price', static function () use ( $catalog, $asc ) { return $catalog->products( $asc ); } );
		$measure( "price_desc_$phase", $products, '_price', static function () use ( $catalog, $desc ) { return $catalog->products( $desc ); } );
	}
} finally {
	foreach ( $product_ids as $id ) { $p = wc_get_product( $id ); if ( $p ) { $p->delete( true ); } }
	if ( $post_ids ) {
		$first = min( $post_ids ); $last = max( $post_ids );
		$wpdb->query( $wpdb->prepare( "DELETE m FROM {$wpdb->postmeta} m JOIN {$wpdb->posts} p ON p.ID=m.post_id WHERE p.ID BETWEEN %d AND %d AND p.post_title=%s", $first, $last, $title ) );
		$wpdb->query( $wpdb->prepare( "DELETE FROM {$wpdb->posts} WHERE ID BETWEEN %d AND %d AND post_title=%s", $first, $last, $title ) );
	}
}
$remaining = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->posts} WHERE post_title LIKE %s", $wpdb->esc_like( $title ) . '%' ) );
echo "Cleanup remaining synthetic posts: $remaining.\n";
if ( $remaining ) { ++$failed; }
echo "Failures: $failed.\n";
exit( $failed ? 1 : 0 );
