<?php
/** Disposable cloned WordPress database only; two independent DB connections. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || getenv( 'WORDPRESS_DB_NAME' ) !== 'cb_qa_integrity_20261102' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
global $wpdb;
$failed = 0;
$check = static function ( bool $ok, string $label ) use ( &$failed ): void { echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n"; if ( ! $ok ) { ++$failed; } };
$make = static function () use ( $wpdb ): int {
	$suffix = bin2hex( random_bytes( 5 ) );
	$id = wp_create_user( 'qa_concurrent_' . $suffix, wp_generate_password( 24 ), 'qa-concurrent-' . $suffix . '@example.test' );
	if ( ! is_int( $id ) ) { throw new RuntimeException( 'User fixture unavailable.' ); }
	add_user_meta( $id, 'cb_identity_managed', '1' );
	return $id;
};
mysqli_report( MYSQLI_REPORT_OFF );
$other = new mysqli( DB_HOST, DB_USER, DB_PASSWORD, DB_NAME );
$other->set_charset( 'utf8mb4' );
$other->query( 'SET SESSION innodb_lock_wait_timeout=5' );
$service = new CB_Managed_User_Deletion();
$delete = new ReflectionMethod( $service, 'delete' );
$delete->setAccessible( true );
$ids = array();
$comment_ids = array();
$post_ids = array();
$filter = null;
try {
	$dependent = $make(); $ids[] = $dependent;
	$post = wp_insert_post( array( 'post_type' => 'post', 'post_status' => 'draft', 'post_title' => 'QA managed dependency', 'post_author' => $dependent ), true );
	if ( ! is_int( $post ) ) { throw new RuntimeException( 'Post fixture unavailable.' ); }
	$post_ids[] = $post;
	$comment = wp_insert_comment( array( 'comment_post_ID' => $post, 'comment_content' => 'QA managed dependency', 'comment_author' => 'QA', 'user_id' => $dependent, 'comment_approved' => 1 ) );
	if ( ! $comment ) { throw new RuntimeException( 'Comment fixture unavailable.' ); }
	$comment_ids[] = (int) $comment;
	$check( $delete->invoke( $service, $dependent ) === 'dependencies_exist' && get_user_by( 'id', $dependent ) instanceof WP_User, 'pre-existing native WordPress post/comment block managed deletion' );
	$id = $make(); $ids[] = $id;
	$anonymous = $wpdb->insert( $wpdb->comments, array( 'comment_post_ID' => 0, 'comment_content' => 'Anonymous race fixture', 'comment_author' => 'QA', 'user_id' => 0 ) );
	if ( $anonymous ) { $comment_ids[] = (int) $wpdb->insert_id; }
	$check( $anonymous === 1 && $delete->invoke( $service, $id ) === 'deleted', 'anonymous row does not block clean managed deletion' );
	$check( (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE ID=%d", $id ) ) === 0, 'clean managed user removed' );
	$wpdb->last_error = '';
	$after_comment = $other->query( "INSERT INTO `{$wpdb->comments}` (comment_post_ID,comment_content,comment_author,user_id) VALUES (0,'After delete','QA',$id)" );
	$check( $after_comment === false && $other->errno !== 0, 'direct SQL comment insert after deletion is rejected' );
	$after_post = $other->query( "INSERT INTO `{$wpdb->posts}` (post_author,post_title,post_type,post_status) VALUES ($id,'After delete','post','draft')" );
	$check( $after_post === false && $other->errno !== 0, 'direct SQL post insert after deletion is rejected' );
	$id = $make(); $ids[] = $id;
	$async_started = false;
	$filter = static function ( string $sql ) use ( $other, $wpdb, $id, &$async_started ): string {
		if ( ! $async_started && strpos( $sql, "SELECT COUNT(*) FROM {$wpdb->posts} WHERE post_author=$id" ) === 0 ) {
			$async_started = $other->query( "INSERT INTO `{$wpdb->comments}` (comment_post_ID,comment_content,comment_author,user_id) VALUES (0,'During lock','QA',$id)", MYSQLI_ASYNC );
		}
		return $sql;
	};
	add_filter( 'query', $filter );
	$started = microtime( true );
	$status = $delete->invoke( $service, $id );
	$elapsed = microtime( true ) - $started;
	remove_filter( 'query', $filter ); $filter = null;
	$reads = array( $other ); $errors = array(); $reject = array();
	$polled = mysqli_poll( $reads, $errors, $reject, 6 );
	$async_result = $polled > 0 ? $other->reap_async_query() : null;
	$check( $async_started && $status === 'deleted' && $polled > 0 && $async_result === false && $other->errno !== 0, 'insert arriving while user row locked fails after deletion' );
	$check( (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->comments} WHERE user_id=%d", $id ) ) === 0, 'no dependent comment survived concurrent deletion' );
	$orphans = (int) $wpdb->get_var( "SELECT COUNT(*) FROM {$wpdb->posts} p LEFT JOIN {$wpdb->users} u ON u.ID=p.post_author WHERE p.post_author<>0 AND u.ID IS NULL" ) + (int) $wpdb->get_var( "SELECT COUNT(*) FROM {$wpdb->comments} c LEFT JOIN {$wpdb->users} u ON u.ID=c.user_id WHERE c.user_id<>0 AND u.ID IS NULL" );
	$check( $orphans === 0, 'no nonzero orphans under concurrent insert' );
	echo 'primary_connection=' . (int) $wpdb->dbh->thread_id . ' secondary_connection=' . (int) $other->thread_id . ' delete_seconds=' . round( $elapsed, 3 ) . "\n";
} finally {
	if ( $filter ) { remove_filter( 'query', $filter ); }
	foreach ( $comment_ids as $comment_id ) { wp_delete_comment( $comment_id, true ); }
	foreach ( $post_ids as $post_id ) { wp_delete_post( $post_id, true ); }
	foreach ( $ids as $id ) {
		$wpdb->delete( $wpdb->usermeta, array( 'user_id' => $id ), array( '%d' ) );
		$wpdb->delete( $wpdb->users, array( 'ID' => $id ), array( '%d' ) );
		clean_user_cache( $id );
	}
	$other->close();
}
echo "Failures: $failed.\n";
exit( $failed ? 1 : 0 );
