<?php
require __DIR__ . '/experiment-identity-shared-references.php';
require_once '/work/includes/class-cb-managed-user-deletion.php';

try {
	global $wpdb, $wp_version;
	CB_Managed_User_Deletion::boot();
	$admin = get_user_by( 'login', 'qa_admin' ); if ( ! $admin instanceof WP_User ) { throw new RuntimeException(); }
	wp_set_current_user( (int) $admin->ID );
	$managed = shared_user( 'managed_delete', 'managed_delete@example.test' ); add_user_meta( $managed, 'cb_identity_managed', '1' );
	shared_check( in_array( 'do_not_allow', apply_filters( 'map_meta_cap', array( 'delete_users' ), 'delete_user', (int) $admin->ID, array( $managed ) ), true ), 'native deletion capability is denied only for a managed account' );
	$plain = shared_user( 'plain_delete', 'plain_delete@example.test' );
	shared_check( ! in_array( 'do_not_allow', apply_filters( 'map_meta_cap', array( 'delete_users' ), 'delete_user', (int) $admin->ID, array( $plain ) ), true ), 'native deletion remains available for a normal WordPress account' );
	$service = new CB_Managed_User_Deletion();
	$delete = new ReflectionMethod( $service, 'delete' ); $delete->setAccessible( true );
	shared_check( $delete->invoke( $service, $managed ) === 'deleted' && ! get_user_by( 'id', $managed ), 'managed deletion commits only after metadata and account are removed' );
	$protected = shared_user( 'managed_content', 'managed_content@example.test' ); add_user_meta( $protected, 'cb_identity_managed', '1' );
	$wpdb->insert( $wpdb->posts, array( 'post_author' => $protected, 'post_title' => 'synthetic', 'post_status' => 'draft', 'post_type' => 'post' ) );
	shared_check( $delete->invoke( $service, $protected ) === 'dependencies_exist' && get_user_by( 'id', $protected ) instanceof WP_User, 'managed deletion refuses an account with dependent content without writes' );
	echo 'PASS managed user deletion WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n";
} catch ( Throwable $error ) { fwrite( STDERR, 'FAIL managed deletion: ' . get_class( $error ) . "\n" ); exit( 1 ); }
