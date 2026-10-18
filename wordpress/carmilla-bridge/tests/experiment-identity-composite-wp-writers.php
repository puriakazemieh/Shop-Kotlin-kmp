<?php
/** Synthetic composite research only; never install its schema on a real site. */
$prefix = getenv( 'CARMILLA_LOGIN_TEST_PREFIX' );
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) ) {
	fwrite( STDERR, "Disposable test flag and random prefix required.\n" ); exit( 2 );
}

try {
	// This creates and exercises the same multi-source claims/reference schema
	// used by the concurrency experiment, but on a fresh synthetic prefix.
	require __DIR__ . '/experiment-identity-shared-references.php';
	global $wpdb;
	$owner = shared_user( 'composite_login@example.test', 'composite_owner@example.test' );
	$updated = shared_user( 'composite_updated', 'composite_old@example.test' );
	$zero_hook = false;
	add_action( 'user_register', static function ( $id ) use ( &$zero_hook ) {
		if ( (int) $id === 0 ) { $zero_hook = true; }
	} );
	$insert = wp_insert_user( array(
		'user_login' => 'composite_owner@example.test',
		'user_email' => 'composite_new@example.test',
		'user_pass'  => 'synthetic-only-password',
	) );
	$insert_rows = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->users} WHERE BINARY user_login=BINARY %s", 'composite_owner@example.test' ) );
	shared_check( ! is_wp_error( $insert ) && (int) $insert === 0 && $zero_hook && $insert_rows === 0 && shared_owner( 'composite_owner@example.test' ) === $owner && shared_owner( 'composite_new@example.test' ) === 0, 'rejected shared-schema insert returns zero and invokes user_register' );

	$profile_hook = false;
	add_action( 'profile_update', static function ( $id ) use ( &$profile_hook, $updated ) {
		if ( (int) $id === $updated ) { $profile_hook = true; }
	} );
	$result = wp_update_user( array( 'ID' => $updated, 'user_email' => 'composite_login@example.test' ) );
	$email = $wpdb->get_var( $wpdb->prepare( "SELECT user_email FROM {$wpdb->users} WHERE ID=%d", $updated ) );
	shared_check( ! is_wp_error( $result ) && (int) $result === $updated && $profile_hook && $email === 'composite_old@example.test' && shared_owner( 'composite_login@example.test' ) === $owner && shared_owner( 'composite_old@example.test' ) === $updated, 'rejected shared-schema update returns ID and invokes profile_update' );

	echo "BLOCKER failed WordPress writers can report success and execute callbacks; fixture is not a supported writer solution.\n";
	exit( 4 );
} catch ( Throwable $e ) {
	// Avoid database errors, credentials, identity values or request data in output.
	fwrite( STDERR, "FAIL composite writer experiment.\n" ); exit( 1 );
}
