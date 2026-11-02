<?php
/** Disposable installed WordPress only; synthetic clinic meta-query benchmark. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-clinic-controller.php';
global $wpdb;
$suffix = bin2hex( random_bytes( 5 ) );
$user = wp_create_user( 'qa_clinic_perf_' . $suffix, wp_generate_password( 24 ), 'qa-clinic-perf-' . $suffix . '@example.test' );
if ( ! is_int( $user ) ) { exit( 2 ); }
$previous_user = get_current_user_id();
wp_set_current_user( $user );
$therapist = wp_insert_post( array( 'post_type' => 'cb_therapist', 'post_status' => 'publish', 'post_title' => 'QA clinic perf therapist ' . $suffix ), true );
if ( ! is_int( $therapist ) ) { exit( 2 ); }
$title = 'QA clinic perf appointment ' . $suffix;
$controller = new CB_Clinic_Controller();
$request = new WP_REST_Request( 'GET', '/api/clinic/therapists/' . $therapist . '/messages' );
$request->set_param( 'id', $therapist );
$failed = 0;
$add = static function ( int $count ) use ( $wpdb, $user, $therapist, $title ): void {
	for ( $remaining = $count; $remaining > 0; $remaining -= 250 ) {
		$batch = min( 250, $remaining );
		$post_rows = array_fill( 0, $batch, '(' . (int) $user . ",'$title','cb_appointment','publish','2026-11-02 12:00:00','2026-11-02 12:00:00')" );
		$post_sql = "INSERT INTO {$wpdb->posts} (post_author,post_title,post_type,post_status,post_date,post_date_gmt) VALUES " . implode( ',', $post_rows );
		if ( $wpdb->query( $post_sql ) !== $batch ) { throw new RuntimeException( 'Appointment fixture failed.' ); }
		$first = (int) $wpdb->insert_id; $last = $first + $batch - 1;
		$meta_sql = "INSERT INTO {$wpdb->postmeta} (post_id,meta_key,meta_value) SELECT ID,'cb_user_id','" . (int) $user . "' FROM {$wpdb->posts} WHERE ID BETWEEN $first AND $last AND post_title='$title'";
		if ( $wpdb->query( $meta_sql ) !== $batch ) { throw new RuntimeException( 'Appointment meta fixture failed.' ); }
		$comment_rows = array_fill( 0, $batch, '(' . (int) $therapist . ',' . (int) $user . ",'Synthetic clinic message','cb_msg','1','2026-11-02 12:00:00','2026-11-02 12:00:00')" );
		$comment_sql = "INSERT INTO {$wpdb->comments} (comment_post_ID,user_id,comment_content,comment_type,comment_approved,comment_date,comment_date_gmt) VALUES " . implode( ',', $comment_rows );
		if ( $wpdb->query( $comment_sql ) !== $batch ) { throw new RuntimeException( 'Message fixture failed.' ); }
		$first = (int) $wpdb->insert_id; $last = $first + $batch - 1;
		$message_meta_sql = "INSERT INTO {$wpdb->commentmeta} (comment_id,meta_key,meta_value) SELECT comment_ID,'cb_user_id','" . (int) $user . "' FROM {$wpdb->comments} WHERE comment_ID BETWEEN $first AND $last AND comment_post_ID=" . (int) $therapist;
		if ( $wpdb->query( $message_meta_sql ) !== $batch ) { throw new RuntimeException( 'Message meta fixture failed.' ); }
	}
};
$measure = static function ( string $label, int $total, string $target, callable $call ) use ( $wpdb, &$failed ): void {
	wp_cache_flush();
	$sql = '';
	$capture = static function ( string $query ) use ( &$sql, $wpdb, $target ): string {
		$meta_table = $target === 'appointments' ? $wpdb->postmeta : $wpdb->commentmeta;
		if ( stripos( $query, 'SELECT' ) === 0 && strpos( $query, $meta_table ) !== false && strpos( $query, 'cb_user_id' ) !== false ) { $sql = $query; }
		return $query;
	};
	add_filter( 'query', $capture );
	$start = hrtime( true );
	$response = $call();
	$method_ms = ( hrtime( true ) - $start ) / 1000000;
	remove_filter( 'query', $capture );
	if ( $sql === '' ) { ++$failed; echo "FAIL $label no SQL captured\n"; return; }
	$start = hrtime( true );
	$wpdb->get_results( $sql );
	$sql_ms = ( hrtime( true ) - $start ) / 1000000;
	$plan = $wpdb->get_results( 'EXPLAIN ' . $sql, ARRAY_A );
	$actual = $wpdb->get_var( 'ANALYZE FORMAT=JSON ' . $sql );
	$actual_plan = is_string( $actual ) ? json_decode( $actual, true ) : null;
	$ok = $response->get_status() === 200 && is_array( $plan ) && $plan && is_array( $actual_plan );
	if ( ! $ok ) { ++$failed; }
	$count = is_array( $response->get_data() ) ? count( $response->get_data() ) : 0;
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . "$label total=$total output_count=$count method_ms=" . round( $method_ms, 3 ) . ' sql_ms=' . round( $sql_ms, 3 ) . "\n";
	echo 'PLAN ' . wp_json_encode( $plan ) . "\n";
	echo 'ACTUAL ' . wp_json_encode( $actual_plan ) . "\n";
};
try {
	foreach ( array( 'small' => 30, 'large' => 2970, 'stress' => 27000 ) as $phase => $add_count ) {
		$add( $add_count );
		$total = $phase === 'small' ? 30 : ( $phase === 'large' ? 3000 : 30000 );
		$measure( 'my_appointments_' . $phase, $total, 'appointments', static function () use ( $controller ) { return $controller->my_appointments(); } );
		$measure( 'list_messages_' . $phase, $total, 'messages', static function () use ( $controller, $request ) { return $controller->list_messages( $request ); } );
		$measure( 'messaging_status_' . $phase, $total, 'messages', static function () use ( $controller, $request ) { return $controller->messaging_status( $request ); } );
	}
} finally {
	$wpdb->query( $wpdb->prepare( "DELETE m FROM {$wpdb->postmeta} m JOIN {$wpdb->posts} p ON p.ID=m.post_id WHERE p.post_title=%s", $title ) );
	$wpdb->delete( $wpdb->posts, array( 'post_title' => $title ), array( '%s' ) );
	$wpdb->query( $wpdb->prepare( "DELETE m FROM {$wpdb->commentmeta} m JOIN {$wpdb->comments} c ON c.comment_ID=m.comment_id WHERE c.comment_post_ID=%d", $therapist ) );
	$wpdb->delete( $wpdb->comments, array( 'comment_post_ID' => $therapist ), array( '%d' ) );
	wp_delete_post( $therapist, true );
	wp_set_current_user( $previous_user );
	wp_delete_user( $user );
}
echo "Failures: $failed.\n";
exit( $failed ? 1 : 0 );
