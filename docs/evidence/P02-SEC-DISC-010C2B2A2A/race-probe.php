<?php
/** Disposable WordPress DB only; creates uniquely named scratch tables and drops them. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
$a = new mysqli( DB_HOST, DB_USER, DB_PASSWORD, DB_NAME );
$b = new mysqli( DB_HOST, DB_USER, DB_PASSWORD, DB_NAME );
$a->set_charset( 'utf8mb4' );
$b->set_charset( 'utf8mb4' );
$suffix = bin2hex( random_bytes( 5 ) );
$users = 'cb_qa_users_' . $suffix;
$posts = 'cb_qa_posts_' . $suffix;
$pass = false;
try {
	$a->query( "CREATE TABLE `$users` (id BIGINT UNSIGNED PRIMARY KEY) ENGINE=InnoDB" );
	$a->query( "CREATE TABLE `$posts` (id BIGINT UNSIGNED PRIMARY KEY, post_author BIGINT UNSIGNED NOT NULL) ENGINE=InnoDB" );
	$a->query( "INSERT INTO `$users` VALUES (1)" );
	$before = (int) $a->query( "SELECT COUNT(*) AS n FROM `$posts` WHERE post_author=1" )->fetch_assoc()['n'];
	$b->query( "INSERT INTO `$posts` VALUES (1,1)" );
	$a->query( "DELETE FROM `$users` WHERE id=1" );
	$orphans = (int) $a->query( "SELECT COUNT(*) AS n FROM `$posts` p LEFT JOIN `$users` u ON u.id=p.post_author WHERE u.id IS NULL" )->fetch_assoc()['n'];
	$after = $b->query( "INSERT INTO `$posts` VALUES (2,1)" );
	$total = (int) $a->query( "SELECT COUNT(*) AS n FROM `$posts`" )->fetch_assoc()['n'];
	echo "connections=" . $a->thread_id . ',' . $b->thread_id . " before=$before orphan_after_interleaving=$orphans post_delete_insert=" . ( $after ? 'accepted' : 'rejected' ) . " total_orphans=$total\n";
	$pass = $before === 0 && $orphans === 1 && $after && $total === 2 && $a->thread_id !== $b->thread_id;
} finally {
	$a->query( "DROP TABLE IF EXISTS `$posts`" );
	$a->query( "DROP TABLE IF EXISTS `$users`" );
	$a->close();
	$b->close();
}
exit( $pass ? 0 : 1 );
