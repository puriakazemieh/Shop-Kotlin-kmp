<?php
/** Disposable installed WordPress; operator command runs against scratch tables. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-content/plugins/carmilla-bridge/tools/user-integrity.php';
global $wpdb;
$suffix = bin2hex( random_bytes( 4 ) );
$tables = array( 'users' => 'cb_qa_ou_' . $suffix, 'posts' => 'cb_qa_op_' . $suffix, 'comments' => 'cb_qa_oc_' . $suffix );
$backup = '/tmp/cb-qa-backup-' . $suffix . '.sql';
$failed = 0;
$check = static function ( bool $ok, string $label ) use ( &$failed ): void { if ( ! $ok ) { ++$failed; } echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n"; };
try {
	$wpdb->query( "CREATE TABLE `{$tables['users']}` (ID BIGINT UNSIGNED NOT NULL PRIMARY KEY) ENGINE=InnoDB" );
	$wpdb->query( "CREATE TABLE `{$tables['posts']}` (ID BIGINT UNSIGNED NOT NULL PRIMARY KEY, post_author BIGINT UNSIGNED NOT NULL DEFAULT 0) ENGINE=InnoDB" );
	$wpdb->query( "CREATE TABLE `{$tables['comments']}` (ID BIGINT UNSIGNED NOT NULL PRIMARY KEY, user_id BIGINT UNSIGNED NOT NULL DEFAULT 0) ENGINE=InnoDB" );
	$check( cb_user_integrity_command( array( 'tool', 'preflight' ), $tables )['code'] === 0, 'preflight accepts supported uninstalled schema' );
	$check( cb_user_integrity_command( array( 'tool', 'install' ), $tables )['code'] === 3, 'missing backup refuses DDL' );
	file_put_contents( $backup, 'synthetic disposable backup marker' );
	$check( cb_user_integrity_command( array( 'tool', 'install', $backup, str_repeat( '0', 64 ) ), $tables )['code'] === 3, 'wrong backup digest refuses DDL' );
	$check( CB_User_Integrity::inspect( $tables )['state'] === 'uninstalled', 'denials leave schema unchanged' );
	$digest = hash_file( 'sha256', $backup );
	$needle = "ALTER TABLE `{$tables['posts']}`";
	$deny_ddl = static function ( string $sql ) use ( $needle ): string { return strpos( $sql, $needle ) === 0 ? 'ALTER TABLE cb_qa_missing_privilege ADD COLUMN bad INT' : $sql; };
	add_filter( 'query', $deny_ddl );
	$blocked = cb_user_integrity_command( array( 'tool', 'install', $backup, $digest ), $tables );
	remove_filter( 'query', $deny_ddl );
	$check( $blocked['code'] === 4 && CB_User_Integrity::inspect( $tables )['state'] === 'uninstalled', 'DDL failure reports failure without claiming installation' );
	$installed = cb_user_integrity_command( array( 'tool', 'install', $backup, $digest ), $tables );
	$check( $installed['code'] === 0 && CB_User_Integrity::inspect( $tables )['state'] === 'installed', 'valid operator invocation installs both FKs' );
	$check( cb_user_integrity_command( array( 'tool', 'install' ), $tables )['code'] === 0, 'installed invocation is no-op' );
	$check( CB_User_Integrity::inspect()['state'] === 'uninstalled', 'real WordPress tables remain unchanged' );
} finally {
	$wpdb->query( "DROP TABLE IF EXISTS `{$tables['comments']}`" );
	$wpdb->query( "DROP TABLE IF EXISTS `{$tables['posts']}`" );
	$wpdb->query( "DROP TABLE IF EXISTS `{$tables['users']}`" );
	@unlink( $backup );
}
echo "Failures: $failed.\n";
exit( $failed ? 1 : 0 );
