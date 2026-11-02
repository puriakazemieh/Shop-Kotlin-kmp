<?php
/** Disposable cloned WordPress only: inject a second-connection comment after dependency checks. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || getenv( 'WORDPRESS_DB_NAME' ) !== 'cb_qa_integrity_20261102' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
global $wpdb;
$id = wp_create_user( 'qa_race_' . bin2hex( random_bytes( 5 ) ), wp_generate_password( 24 ), 'qa-race-' . bin2hex( random_bytes( 5 ) ) . '@example.test' );
if ( ! is_int( $id ) ) { exit( 2 ); }
add_user_meta( $id, 'cb_identity_managed', '1' );
$other = new mysqli( DB_HOST, DB_USER, DB_PASSWORD, DB_NAME );
$other->set_charset( 'utf8mb4' );
$secondary_id = $other->thread_id;
$inserted = false;
$filter = static function ( string $sql ) use ( $other, $wpdb, $id, &$inserted ): string {
	if ( $sql === 'START TRANSACTION' && ! $inserted ) {
		$inserted = $other->query( "INSERT INTO `{$wpdb->comments}` (comment_post_ID,comment_content,comment_author,user_id) VALUES (0,'Synthetic race','QA'," . (int) $id . ')' );
	}
	return $sql;
};
$pass = false;
try {
	$service = new CB_Managed_User_Deletion();
	$delete = new ReflectionMethod( $service, 'delete' );
	$delete->setAccessible( true );
	add_filter( 'query', $filter );
	$status = $delete->invoke( $service, $id );
	remove_filter( 'query', $filter );
	$owner = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE ID=%d", $id ) );
	$dependent = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->comments} WHERE user_id=%d", $id ) );
	$orphans = (int) $wpdb->get_var( "SELECT COUNT(*) FROM {$wpdb->comments} c LEFT JOIN {$wpdb->users} u ON u.ID=c.user_id WHERE c.user_id<>0 AND u.ID IS NULL" );
	$pass = $inserted && $status === 'dependencies_exist' && $owner === 1 && $dependent === 1 && $orphans === 0 && $secondary_id !== (int) $wpdb->dbh->thread_id;
	echo 'second_connection=' . $secondary_id . ' inserted=' . ( $inserted ? 'yes' : 'no' ) . " status=$status owner=$owner dependent=$dependent orphans=$orphans result=" . ( $pass ? 'PASS' : 'FAIL' ) . "\n";
} finally {
	remove_filter( 'query', $filter );
	$wpdb->delete( $wpdb->comments, array( 'user_id' => $id ), array( '%d' ) );
	$wpdb->delete( $wpdb->usermeta, array( 'user_id' => $id ), array( '%d' ) );
	$wpdb->delete( $wpdb->users, array( 'ID' => $id ), array( '%d' ) );
	clean_user_cache( $id );
	$other->close();
}
exit( $pass ? 0 : 1 );
