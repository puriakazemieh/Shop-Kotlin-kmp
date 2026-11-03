<?php
/** Disposable installed WordPress only: synthetic admin user-meta query benchmark. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-admin-content-controller.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-admin-controller.php';
global $wpdb;
$suffix = bin2hex( random_bytes( 5 ) );
$prefix = 'qa_um_' . $suffix . '_';
echo "Fixture prefix: $prefix\n";
$content = new CB_Admin_Content_Controller();
$admin = new CB_Admin_Controller();
$keys = array( 'cb_course_refunds', 'cb_switch_requests', 'cb_psych_attempts', 'cb_returns' );
$first_id = 0;
$last_id = 0;
$failures = 0;
$add = static function ( int $count, int $offset ) use ( $wpdb, $prefix, $keys, &$first_id, &$last_id ): void {
	for ( $start = 0; $start < $count; $start += 100 ) {
		$batch = min( 100, $count - $start );
		$users = array();
		for ( $i = 0; $i < $batch; ++$i ) {
			$n = $offset + $start + $i + 1;
			$login = $prefix . str_pad( (string) $n, 5, '0', STR_PAD_LEFT );
			$users[] = $wpdb->prepare( '(%s,%s,%s,%s,%s,%s)', $login, '*', $login, $login . '@example.test', '2026-11-03 12:00:00', $login );
		}
		if ( $wpdb->query( "INSERT INTO {$wpdb->users} (user_login,user_pass,user_nicename,user_email,user_registered,display_name) VALUES " . implode( ',', $users ) ) !== $batch ) { throw new RuntimeException( 'User fixture insert failed' ); }
		$first = (int) $wpdb->insert_id;
		$last = $first + $batch - 1;
		if ( ! $first_id ) { $first_id = $first; }
		$last_id = $last;
		$meta = array();
		for ( $id = $first; $id <= $last; ++$id ) {
			foreach ( $keys as $key ) {
				$base = $key === 'cb_psych_attempts' ? 200000 : ( $key === 'cb_returns' ? 300000 : 100000 );
				$value = serialize( array( array( 'id' => $base + $id, 'status' => $key === 'cb_psych_attempts' ? 'AWAITING_INTERPRETATION' : 'PENDING' ) ) );
				$meta[] = $wpdb->prepare( '(%d,%s,%s)', $id, $key, $value );
			}
		}
		if ( $wpdb->query( "INSERT INTO {$wpdb->usermeta} (user_id,meta_key,meta_value) VALUES " . implode( ',', $meta ) ) !== count( $meta ) ) { throw new RuntimeException( 'Usermeta fixture insert failed' ); }
	}
};
$measure = static function ( string $label, int $total, callable $call ) use ( $wpdb, &$failures ): void {
	wp_cache_flush();
	$sql = '';
	$capture = static function ( string $query ) use ( &$sql, $wpdb ): string {
		if ( stripos( $query, 'SELECT' ) === 0 && strpos( $query, $wpdb->usermeta ) !== false && strpos( $query, 'meta_key' ) !== false && strpos( $query, $wpdb->users ) !== false ) { $sql = $query; }
		return $query;
	};
	add_filter( 'query', $capture );
	$start = hrtime( true );
	$response = $call();
	$method_ms = ( hrtime( true ) - $start ) / 1000000;
	remove_filter( 'query', $capture );
	if ( $sql === '' ) { ++$failures; echo "FAIL $label no SQL captured\n"; return; }
	$start = hrtime( true );
	$wpdb->get_results( $sql );
	$sql_ms = ( hrtime( true ) - $start ) / 1000000;
	$plan = $wpdb->get_results( 'EXPLAIN ' . $sql, ARRAY_A );
	$actual = json_decode( (string) $wpdb->get_var( 'ANALYZE FORMAT=JSON ' . $sql ), true );
	$count = is_array( $response->get_data() ) ? count( $response->get_data() ) : -1;
	$expected_status = strpos( $label, '_target_after_500' ) !== false ? 404 : 200;
	$expected_count = $expected_status === 200 ? min( 500, $total ) : 6;
	$ok = $response instanceof WP_REST_Response && is_array( $plan ) && is_array( $actual ) && $response->get_status() === $expected_status && $count === $expected_count;
	if ( ! $ok ) { ++$failures; }
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . "$label users=$total status=" . $response->get_status() . " output=$count method_ms=" . round( $method_ms, 3 ) . ' sql_ms=' . round( $sql_ms, 3 ) . "\n";
	echo 'PLAN ' . wp_json_encode( $plan ) . "\n";
	echo 'ACTUAL ' . wp_json_encode( $actual ) . "\n";
};
try {
	foreach ( array( 'small' => 30, 'limit' => 471, 'large' => 2499 ) as $phase => $additional ) {
		$offset = $phase === 'small' ? 0 : ( $phase === 'limit' ? 30 : 501 );
		$add( $additional, $offset );
		$total = $offset + $additional;
		$measure( "refund_requests_$phase", $total, static function () use ( $content ) { return $content->refund_requests(); } );
		$measure( "switch_requests_$phase", $total, static function () use ( $content ) { return $content->switch_requests(); } );
		$measure( "pending_interpretations_$phase", $total, static function () use ( $content ) { return $content->pending_interpretations(); } );
		$measure( "return_requests_$phase", $total, static function () use ( $admin ) { return $admin->return_requests(); } );
	}
	$last = $last_id;
	$interpret = new WP_REST_Request( 'POST', '/api/admin/psych-tests/' . ( 200000 + $last ) . '/interpret' );
	$interpret->set_param( 'uid', 200000 + $last );
	$interpret->set_body( wp_json_encode( array( 'interpretation' => 'Synthetic interpretation' ) ) );
	$interpret->set_header( 'content-type', 'application/json' );
	$measure( 'interpret_target_after_500', 3000, static function () use ( $content, $interpret ) { return $content->interpret( $interpret ); } );
	$refund = new WP_REST_Request( 'POST', '/api/admin/academy/refund-requests/' . ( 100000 + $last ) . '/review' );
	$refund->set_param( 'id', 100000 + $last );
	$refund->set_body( wp_json_encode( array( 'approve' => true ) ) );
	$refund->set_header( 'content-type', 'application/json' );
	$measure( 'review_refund_target_after_500', 3000, static function () use ( $content, $refund ) { return $content->review_refund( $refund ); } );
	$return = new WP_REST_Request( 'POST', '/api/admin/return-requests/' . ( 300000 + $last ) . '/review' );
	$return->set_param( 'id', 300000 + $last );
	$return->set_body( wp_json_encode( array( 'status' => 'APPROVED' ) ) );
	$return->set_header( 'content-type', 'application/json' );
	$measure( 'update_return_target_after_500', 3000, static function () use ( $admin, $return ) { return $admin->update_return( $return ); } );
	$untouched = get_user_meta( $last, 'cb_psych_attempts', true )[0]['status'] === 'AWAITING_INTERPRETATION'
		&& get_user_meta( $last, 'cb_course_refunds', true )[0]['status'] === 'PENDING'
		&& get_user_meta( $last, 'cb_returns', true )[0]['status'] === 'PENDING';
	echo ( $untouched ? 'PASS ' : 'FAIL ' ) . "target beyond 500 remains unresolved\n";
	if ( ! $untouched ) { ++$failures; }
} finally {
	if ( $first_id && $last_id ) {
		$wpdb->query( $wpdb->prepare( "DELETE m FROM {$wpdb->usermeta} m JOIN {$wpdb->users} u ON u.ID=m.user_id WHERE u.ID BETWEEN %d AND %d AND u.user_login LIKE %s", $first_id, $last_id, $wpdb->esc_like( $prefix ) . '%' ) );
		$wpdb->query( $wpdb->prepare( "DELETE FROM {$wpdb->users} WHERE ID BETWEEN %d AND %d AND user_login LIKE %s", $first_id, $last_id, $wpdb->esc_like( $prefix ) . '%' ) );
	}
}
$remaining = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE user_login LIKE %s", $wpdb->esc_like( $prefix ) . '%' ) );
echo "Cleanup remaining synthetic users: $remaining.\n";
if ( $remaining ) { ++$failures; }
echo "Failures: $failures.\n";
exit( $failures ? 1 : 0 );
