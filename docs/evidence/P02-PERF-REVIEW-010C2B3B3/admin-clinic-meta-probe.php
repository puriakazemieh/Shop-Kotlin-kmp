<?php
/** Disposable installed WordPress only: synthetic admin clinic comment query benchmark. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-admin-clinic-controller.php';
global $wpdb;
$suffix = bin2hex( random_bytes( 5 ) );
$user = wp_create_user( 'qa_admin_clinic_' . $suffix, wp_generate_password( 24 ), 'qa-admin-clinic-' . $suffix . '@example.test' );
if ( ! is_int( $user ) ) { exit( 2 ); }
$therapist = wp_insert_post( array( 'post_type' => 'cb_therapist', 'post_status' => 'publish', 'post_title' => 'QA admin clinic perf ' . $suffix ), true );
if ( ! is_int( $therapist ) ) { wp_delete_user( $user ); exit( 2 ); }
$request = new WP_REST_Request( 'GET', '/api/admin/therapists/' . $therapist . '/patients/' . $user . '/messages' );
$request->set_param( 'id', $therapist );
$request->set_param( 'uid', $user );
$controller = new CB_Admin_Clinic_Controller();
$failures = 0;
try {
	foreach ( array( 'small' => 30, 'large' => 2970, 'stress' => 27000 ) as $phase => $additional ) {
		for ( $remaining = $additional; $remaining > 0; $remaining -= 250 ) {
			$batch = min( 250, $remaining );
			$rows = array_fill( 0, $batch, '(' . (int) $therapist . ',' . (int) $user . ",'Synthetic admin clinic message','cb_msg','1','2026-11-02 12:00:00','2026-11-02 12:00:00')" );
			if ( $wpdb->query( "INSERT INTO {$wpdb->comments} (comment_post_ID,user_id,comment_content,comment_type,comment_approved,comment_date,comment_date_gmt) VALUES " . implode( ',', $rows ) ) !== $batch ) { throw new RuntimeException( 'Comment fixture failed.' ); }
			$first = (int) $wpdb->insert_id;
			$last = $first + $batch - 1;
			if ( $wpdb->query( "INSERT INTO {$wpdb->commentmeta} (comment_id,meta_key,meta_value) SELECT comment_ID,'cb_user_id','" . (int) $user . "' FROM {$wpdb->comments} WHERE comment_ID BETWEEN $first AND $last AND comment_post_ID=" . (int) $therapist ) !== $batch ) { throw new RuntimeException( 'Comment meta fixture failed.' ); }
		}
		wp_cache_flush();
		$sql = '';
		$capture = static function ( string $query ) use ( &$sql, $wpdb ): string {
			if ( stripos( $query, 'SELECT' ) === 0 && strpos( $query, $wpdb->commentmeta ) !== false && strpos( $query, 'cb_user_id' ) !== false ) { $sql = $query; }
			return $query;
		};
		add_filter( 'query', $capture );
		$start = hrtime( true );
		$response = $controller->list_messages( $request );
		$method_ms = ( hrtime( true ) - $start ) / 1000000;
		remove_filter( 'query', $capture );
		if ( $sql === '' ) { ++$failures; echo "FAIL $phase no SQL captured\n"; continue; }
		$start = hrtime( true );
		$wpdb->get_results( $sql );
		$sql_ms = ( hrtime( true ) - $start ) / 1000000;
		$plan = $wpdb->get_results( 'EXPLAIN ' . $sql, ARRAY_A );
		$actual = json_decode( (string) $wpdb->get_var( 'ANALYZE FORMAT=JSON ' . $sql ), true );
		$count = is_array( $response->get_data() ) ? count( $response->get_data() ) : -1;
		$expected = $phase === 'small' ? 30 : ( $phase === 'large' ? 3000 : 30000 );
		$ok = $response->get_status() === 200 && $count === $expected && is_array( $plan ) && is_array( $actual );
		if ( ! $ok ) { ++$failures; }
		echo ( $ok ? 'PASS ' : 'FAIL ' ) . "$phase rows=$expected output=$count method_ms=" . round( $method_ms, 3 ) . ' sql_ms=' . round( $sql_ms, 3 ) . "\n";
		echo 'PLAN ' . wp_json_encode( $plan ) . "\n";
		echo 'ACTUAL ' . wp_json_encode( $actual ) . "\n";
	}
} finally {
	$wpdb->query( $wpdb->prepare( "DELETE m FROM {$wpdb->commentmeta} m JOIN {$wpdb->comments} c ON c.comment_ID=m.comment_id WHERE c.comment_post_ID=%d", $therapist ) );
	$wpdb->delete( $wpdb->comments, array( 'comment_post_ID' => $therapist ), array( '%d' ) );
	wp_delete_post( $therapist, true );
	wp_delete_user( $user );
}
// Confirm that the exact synthetic fixture IDs are absent after cleanup.
$remaining_comments = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->comments} WHERE comment_post_ID=%d", $therapist ) );
$remaining_post = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->posts} WHERE ID=%d", $therapist ) );
$remaining_user = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE ID=%d", $user ) );
echo "Cleanup: comments=$remaining_comments therapist=$remaining_post user=$remaining_user.\n";
if ( $remaining_comments || $remaining_post || $remaining_user ) { ++$failures; }
echo "Failures: $failures.\n";
exit( $failures ? 1 : 0 );
