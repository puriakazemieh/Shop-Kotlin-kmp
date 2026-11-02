<?php
/** Disposable installed WordPress only; creates and removes scratch tables/triggers. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
global $wpdb;
$suffix = bin2hex( random_bytes( 4 ) );
$users = 'cb_qa_tu_' . $suffix;
$comments = 'cb_qa_tc_' . $suffix;
$trigger = 'cb_qa_td_' . $suffix;
$pass = false;
try {
	$wpdb->query( "CREATE TABLE `$users` (ID BIGINT UNSIGNED NOT NULL PRIMARY KEY) ENGINE=InnoDB" );
	$wpdb->query( "CREATE TABLE `$comments` (ID BIGINT UNSIGNED NOT NULL PRIMARY KEY, user_id BIGINT UNSIGNED NOT NULL DEFAULT 0, user_ref BIGINT UNSIGNED GENERATED ALWAYS AS (NULLIF(user_id,0)) PERSISTENT, INDEX(user_ref), CONSTRAINT `cb_qa_fk_$suffix` FOREIGN KEY (user_ref) REFERENCES `$users`(ID) ON DELETE RESTRICT) ENGINE=InnoDB" );
	$wpdb->query( "CREATE TRIGGER `$trigger` BEFORE DELETE ON `$users` FOR EACH ROW UPDATE `$comments` SET user_id=0 WHERE user_id=OLD.ID" );
	$wpdb->query( "INSERT INTO `$users` VALUES (1)" );
	$wpdb->query( "INSERT INTO `$comments` (ID,user_id) VALUES (1,1)" );
	$deleted = $wpdb->query( "DELETE FROM `$users` WHERE ID=1" );
	$after = $wpdb->get_var( "SELECT user_id FROM `$comments` WHERE ID=1" );
	$pass = $deleted === 1 && (string) $after === '0';
	echo 'delete_rows=' . var_export( $deleted, true ) . ' comment_user_id=' . var_export( $after, true ) . ' result=' . ( $pass ? 'PASS' : 'FAIL' ) . "\n";
} finally {
	$wpdb->query( "DROP TRIGGER IF EXISTS `$trigger`" );
	$wpdb->query( "DROP TABLE IF EXISTS `$comments`" );
	$wpdb->query( "DROP TABLE IF EXISTS `$users`" );
}
exit( $pass ? 0 : 1 );
