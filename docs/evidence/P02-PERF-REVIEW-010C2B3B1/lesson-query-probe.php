<?php
/** Disposable installed WordPress only; synthetic academy comment query benchmark. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-academy-controller.php';
global $wpdb;
$course = wp_insert_post( array( 'post_type' => 'cb_course', 'post_status' => 'publish', 'post_title' => 'QA lesson query ' . bin2hex( random_bytes( 5 ) ) ), true );
if ( ! is_int( $course ) ) { exit( 2 ); }
$request = new WP_REST_Request( 'GET', '/api/academy/lessons/questions' );
$request->set_param( 'lid', CB_Academy_Controller::lesson_id( $course, 0 ) );
$controller = new CB_Academy_Controller();
$failed = 0;
$add = static function ( int $count ) use ( $wpdb, $course ): void {
	$rows = array();
	for ( $i = 0; $i < $count; ++$i ) {
		$rows[] = '(' . (int) $course . ",'Synthetic performance','cb_lesson_q','1','2026-11-02 12:00:00','2026-11-02 12:00:00')";
		if ( count( $rows ) === 250 || $i === $count - 1 ) {
			$sql = "INSERT INTO {$wpdb->comments} (comment_post_ID,comment_content,comment_type,comment_approved,comment_date,comment_date_gmt) VALUES " . implode( ',', $rows );
			if ( $wpdb->query( $sql ) !== count( $rows ) ) { throw new RuntimeException( 'Comment fixture INSERT failed.' ); }
			$first = (int) $wpdb->insert_id;
			$last = $first + count( $rows ) - 1;
			$meta = "INSERT INTO {$wpdb->commentmeta} (comment_id,meta_key,meta_value) SELECT comment_ID,'lesson_index',IF(MOD(comment_ID,2)=0,'0','1') FROM {$wpdb->comments} WHERE comment_ID BETWEEN $first AND $last AND comment_post_ID=" . (int) $course;
			if ( $wpdb->query( $meta ) !== count( $rows ) ) { throw new RuntimeException( 'Metadata fixture INSERT failed.' ); }
			$rows = array();
		}
	}
};
$measure = static function ( string $label, int $total ) use ( $wpdb, $course, $request, $controller, &$failed ): void {
	wp_cache_flush();
	$sql = '';
	$capture = static function ( string $query ) use ( &$sql, $wpdb ): string {
		if ( strpos( $query, $wpdb->commentmeta ) !== false && strpos( $query, 'lesson_index' ) !== false && stripos( $query, 'SELECT' ) === 0 ) { $sql = $query; }
		return $query;
	};
	add_filter( 'query', $capture );
	$start = hrtime( true );
	$response = $controller->lesson_questions( $request );
	$method_ms = ( hrtime( true ) - $start ) / 1000000;
	remove_filter( 'query', $capture );
	if ( $sql === '' ) { ++$failed; echo "FAIL $label query not captured\n"; return; }
	$start = hrtime( true );
	$wpdb->get_results( $sql );
	$sql_ms = ( hrtime( true ) - $start ) / 1000000;
	$plan = $wpdb->get_results( 'EXPLAIN ' . $sql, ARRAY_A );
	$actual = $wpdb->get_var( 'ANALYZE FORMAT=JSON ' . $sql );
	$actual_plan = is_string( $actual ) ? json_decode( $actual, true ) : null;
	$found = count( $response->get_data() );
	$ok = $response->get_status() === 200 && $found >= 1 && $found <= $total && is_array( $plan ) && $plan && is_array( $actual_plan );
	if ( ! $ok ) { ++$failed; }
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . "$label total=$total returned=$found method_ms=" . round( $method_ms, 3 ) . ' sql_ms=' . round( $sql_ms, 3 ) . "\n";
	echo 'PLAN ' . wp_json_encode( $plan ) . "\n";
	echo 'ACTUAL ' . wp_json_encode( $actual_plan ) . "\n";
};
$index_added = false;
try {
	$add( 30 );
	$measure( 'small', 30 );
	$add( 29970 );
	$measure( 'large', 30000 );
	$index_added = $wpdb->query( "ALTER TABLE {$wpdb->commentmeta} ADD INDEX cb_qa_lesson_meta (meta_key(191), meta_value(20), comment_id)" ) !== false;
	if ( ! $index_added ) { throw new RuntimeException( 'Temporary index unavailable.' ); }
	$measure( 'large_index', 30000 );
} finally {
	if ( $index_added ) { $wpdb->query( "ALTER TABLE {$wpdb->commentmeta} DROP INDEX cb_qa_lesson_meta" ); }
	$wpdb->query( "DELETE m FROM {$wpdb->commentmeta} m JOIN {$wpdb->comments} c ON c.comment_ID=m.comment_id WHERE c.comment_post_ID=" . (int) $course );
	$wpdb->delete( $wpdb->comments, array( 'comment_post_ID' => $course ), array( '%d' ) );
	wp_delete_post( $course, true );
}
echo "Failures: $failed.\n";
exit( $failed ? 1 : 0 );
