<?php
/** Disposable cloned WordPress database only. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || getenv( 'WORDPRESS_DB_NAME' ) !== 'cb_qa_integrity_20261102' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
global $wpdb;
$id = wp_create_user( 'qa_guard_' . bin2hex( random_bytes( 5 ) ), wp_generate_password( 24 ), 'qa-guard-' . bin2hex( random_bytes( 5 ) ) . '@example.test' );
if ( ! is_int( $id ) ) { exit( 2 ); }
add_user_meta( $id, 'cb_identity_managed', '1' );
$service = new CB_Managed_User_Deletion();
$delete = new ReflectionMethod( $service, 'delete' );
$delete->setAccessible( true );
$names = CB_User_Integrity::names( $wpdb->posts, $wpdb->comments );
$trigger_sql = "CREATE TRIGGER `{$names['users_trigger']}` BEFORE DELETE ON `{$wpdb->users}` FOR EACH ROW UPDATE `{$wpdb->comments}` SET user_id=0 WHERE `{$names['comments_column']}`=OLD.ID";
$failures = 0;
$check = static function ( bool $ok, string $label ) use ( &$failures ): void { if ( ! $ok ) { ++$failures; } echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n"; };
$filter = static function ( string $sql ): string { return $sql === 'SELECT VERSION()' ? 'SELECT VERSION() FROM cb_qa_missing_version' : $sql; };
try {
	$check( in_array( 'do_not_allow', apply_filters( 'map_meta_cap', array( 'delete_users' ), 'delete_user', 1, array( $id ) ), true ), 'native capability still blocks managed account deletion' );
	add_filter( 'query', $filter );
	$status = $delete->invoke( $service, $id );
	remove_filter( 'query', $filter );
	$check( $status === 'unavailable' && get_user_by( 'id', $id ) instanceof WP_User, 'unreadable verifier refuses deletion' );
	$wpdb->query( "DROP TRIGGER `{$names['users_trigger']}`" );
	$check( CB_User_Integrity::inspect()['state'] === 'partial', 'missing trigger makes installed FKs partial' );
	$check( $delete->invoke( $service, $id ) === 'unavailable' && get_user_by( 'id', $id ) instanceof WP_User, 'partial topology refuses deletion without writes' );
	$wpdb->query( $trigger_sql );
	$check( CB_User_Integrity::inspect()['state'] === 'installed', 'test trigger restored to installed state' );
	$check( $delete->invoke( $service, $id ) === 'deleted' && ! get_user_by( 'id', $id ), 'valid clean managed deletion commits' );
} finally {
	remove_filter( 'query', $filter );
	if ( CB_User_Integrity::inspect()['state'] === 'partial' ) { $wpdb->query( $trigger_sql ); }
	$wpdb->delete( $wpdb->usermeta, array( 'user_id' => $id ), array( '%d' ) );
	$wpdb->delete( $wpdb->users, array( 'ID' => $id ), array( '%d' ) );
	clean_user_cache( $id );
}
echo "Failures: $failures.\n";
exit( $failures ? 1 : 0 );
