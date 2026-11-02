<?php
/** Disposable installed WordPress only; scratch tables are removed afterward. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-user-integrity.php';
global $wpdb;
$suffix = bin2hex( random_bytes( 4 ) );
$tables = array( 'users' => 'cb_qa_u_' . $suffix, 'posts' => 'cb_qa_p_' . $suffix, 'comments' => 'cb_qa_c_' . $suffix );
$names = CB_User_Integrity::names( $tables['posts'], $tables['comments'] );
$failures = 0;
$check = static function ( string $expected, string $label ) use ( $tables, &$failures ): void {
	$result = CB_User_Integrity::inspect( $tables );
	$ok = $result['state'] === $expected;
	if ( ! $ok ) { ++$failures; }
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . ' state=' . $result['state'] . ' reason=' . $result['reason'] . "\n";
};
try {
	$wpdb->query( "CREATE TABLE `{$tables['users']}` (ID BIGINT UNSIGNED NOT NULL PRIMARY KEY) ENGINE=InnoDB" );
	$wpdb->query( "CREATE TABLE `{$tables['posts']}` (ID BIGINT UNSIGNED NOT NULL PRIMARY KEY, post_author BIGINT UNSIGNED NOT NULL DEFAULT 0) ENGINE=InnoDB" );
	$wpdb->query( "CREATE TABLE `{$tables['comments']}` (ID BIGINT UNSIGNED NOT NULL PRIMARY KEY, user_id BIGINT UNSIGNED NOT NULL DEFAULT 0) ENGINE=InnoDB" );
	$check( 'uninstalled', 'valid schema without constraints' );
	$wpdb->query( "INSERT INTO `{$tables['posts']}` VALUES (1,77)" );
	$check( 'unsupported', 'orphan reference refuses migration' );
	$wpdb->query( "DELETE FROM `{$tables['posts']}` WHERE ID=1" );
	$wpdb->query( "ALTER TABLE `{$tables['posts']}` ADD COLUMN `{$names['posts_column']}` BIGINT UNSIGNED GENERATED ALWAYS AS (NULLIF(post_author,0)) PERSISTENT, ADD INDEX (`{$names['posts_column']}`)" );
	$check( 'partial', 'generated column without FK' );
	$wpdb->query( "ALTER TABLE `{$tables['posts']}` ADD CONSTRAINT `{$names['posts_fk']}` FOREIGN KEY (`{$names['posts_column']}`) REFERENCES `{$tables['users']}`(ID) ON DELETE RESTRICT" );
	$wpdb->query( "ALTER TABLE `{$tables['comments']}` ADD COLUMN `{$names['comments_column']}` BIGINT UNSIGNED GENERATED ALWAYS AS (NULLIF(user_id,0)) PERSISTENT, ADD INDEX (`{$names['comments_column']}`), ADD CONSTRAINT `{$names['comments_fk']}` FOREIGN KEY (`{$names['comments_column']}`) REFERENCES `{$tables['users']}`(ID) ON DELETE RESTRICT" );
	$check( 'partial', 'both FKs without deletion trigger remain incomplete' );
	$wpdb->query( "CREATE TRIGGER `{$names['users_trigger']}` BEFORE DELETE ON `{$tables['users']}` FOR EACH ROW UPDATE `{$tables['comments']}` SET user_id=0 WHERE `{$names['comments_column']}`=OLD.ID" );
	$check( 'installed', 'both generated FKs and trigger verified' );
	$filter = static function ( string $sql ): string { return strpos( $sql, 'information_schema.TABLES' ) !== false ? 'SELECT * FROM cb_qa_missing_metadata' : $sql; };
	add_filter( 'query', $filter );
	$check( 'unreadable', 'SQL metadata failure is never clean' );
	remove_filter( 'query', $filter );
	$check( 'installed', 'healthy verifier recovers' );
} finally {
	if ( isset( $filter ) ) { remove_filter( 'query', $filter ); }
	$wpdb->query( "DROP TRIGGER IF EXISTS `{$names['users_trigger']}`" );
	$wpdb->query( "DROP TABLE IF EXISTS `{$tables['comments']}`" );
	$wpdb->query( "DROP TABLE IF EXISTS `{$tables['posts']}`" );
	$wpdb->query( "DROP TABLE IF EXISTS `{$tables['users']}`" );
}
echo "Failures: $failures.\n";
exit( $failures ? 1 : 0 );
