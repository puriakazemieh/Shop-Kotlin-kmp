<?php
/** Disposable installed WordPress without integrity migration. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || getenv( 'WORDPRESS_DB_NAME' ) !== 'wordpress' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
global $wpdb;
if ( CB_User_Integrity::inspect()['state'] !== 'uninstalled' ) { exit( 2 ); }
$id = wp_create_user( 'qa_missing_' . bin2hex( random_bytes( 5 ) ), wp_generate_password( 24 ), 'qa-missing-' . bin2hex( random_bytes( 5 ) ) . '@example.test' );
if ( ! is_int( $id ) ) { exit( 2 ); }
add_user_meta( $id, 'cb_identity_managed', '1' );
try {
	$service = new CB_Managed_User_Deletion();
	$method = new ReflectionMethod( $service, 'delete' );
	$method->setAccessible( true );
	$ok = $method->invoke( $service, $id ) === 'unavailable' && get_user_by( 'id', $id ) instanceof WP_User && metadata_exists( 'user', $id, 'cb_identity_managed' );
	echo ( $ok ? 'PASS' : 'FAIL' ) . " missing constraints refuse managed deletion and preserve identity\n";
} finally {
	$wpdb->delete( $wpdb->usermeta, array( 'user_id' => $id ), array( '%d' ) );
	$wpdb->delete( $wpdb->users, array( 'ID' => $id ), array( '%d' ) );
	clean_user_cache( $id );
}
exit( $ok ? 0 : 1 );
