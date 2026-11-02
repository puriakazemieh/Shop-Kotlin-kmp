<?php
/** Disposable installed WordPress only; operates on scratch tables, never wp_posts. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-user-integrity.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-user-integrity-migration.php';
global $wpdb;
$failures = 0;
$check = static function ( bool $ok, string $label ) use ( &$failures ): void {
	if ( ! $ok ) { ++$failures; }
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n";
};
$fixture = static function ( string $kind ) use ( $wpdb, $check ): void {
	$suffix = bin2hex( random_bytes( 4 ) );
	$t = array( 'users' => 'cb_qa_mu_' . $suffix, 'posts' => 'cb_qa_mp_' . $suffix, 'comments' => 'cb_qa_mc_' . $suffix );
	$filter = null;
	try {
		$wpdb->query( "CREATE TABLE `{$t['users']}` (ID BIGINT UNSIGNED NOT NULL PRIMARY KEY) ENGINE=InnoDB" );
		$wpdb->query( "CREATE TABLE `{$t['posts']}` (ID BIGINT UNSIGNED NOT NULL PRIMARY KEY, post_author BIGINT UNSIGNED NOT NULL DEFAULT 0) ENGINE=InnoDB" );
		$wpdb->query( "CREATE TABLE `{$t['comments']}` (ID BIGINT UNSIGNED NOT NULL PRIMARY KEY, user_id BIGINT UNSIGNED NOT NULL DEFAULT 0) ENGINE=InnoDB" );
		if ( $kind === 'legacy' || $kind === 'orphan' ) {
			$wpdb->query( "INSERT INTO `{$t['users']}` VALUES (1)" );
			$author = $kind === 'orphan' ? 99 : 1;
			$wpdb->query( "INSERT INTO `{$t['posts']}` VALUES (1,$author)" );
			$wpdb->query( "INSERT INTO `{$t['comments']}` VALUES (1,0)" );
		}
		if ( $kind === 'interrupted' ) {
			$needle = "ALTER TABLE `{$t['comments']}`";
			$filter = static function ( string $sql ) use ( $needle ): string { return strpos( $sql, $needle ) === 0 ? 'ALTER TABLE cb_qa_missing_migration ADD COLUMN bad INT' : $sql; };
			add_filter( 'query', $filter );
		}
		$result = CB_User_Integrity_Migration::install( $t );
		if ( $filter ) { remove_filter( 'query', $filter ); $filter = null; }
		if ( $kind === 'orphan' ) {
			$check( $result['status'] === 'refused' && CB_User_Integrity::inspect( $t )['state'] === 'unsupported', 'legacy orphan refuses without DDL' );
		} elseif ( $kind === 'interrupted' ) {
			$check( $result['status'] === 'failed' && $result['state'] === 'partial' && CB_User_Integrity_Migration::install( $t )['status'] === 'refused', 'interrupted DDL remains partial and fails closed' );
		} else {
			$check( $result === array( 'status' => 'installed', 'changed' => true ) && CB_User_Integrity::inspect( $t )['state'] === 'installed', $kind . ' installs both constraints' );
			$check( CB_User_Integrity_Migration::install( $t ) === array( 'status' => 'installed', 'changed' => false ), $kind . ' rerun is idempotent' );
			if ( $kind === 'legacy' ) {
				$wpdb->last_error = '';
				$bad = $wpdb->query( "INSERT INTO `{$t['comments']}` (ID,user_id) VALUES (2,99)" );
				$check( $bad === false && $wpdb->last_error !== '', 'installed FK rejects missing user' );
			}
		}
	} finally {
		if ( $filter ) { remove_filter( 'query', $filter ); }
		$names = CB_User_Integrity::names( $t['posts'], $t['comments'] );
		$wpdb->query( "DROP TRIGGER IF EXISTS `{$names['users_trigger']}`" );
		$wpdb->query( "DROP TABLE IF EXISTS `{$t['comments']}`" );
		$wpdb->query( "DROP TABLE IF EXISTS `{$t['posts']}`" );
		$wpdb->query( "DROP TABLE IF EXISTS `{$t['users']}`" );
	}
};
foreach ( array( 'fresh', 'legacy', 'orphan', 'interrupted' ) as $kind ) { $fixture( $kind ); }
echo "Failures: $failures.\n";
exit( $failures ? 1 : 0 );
