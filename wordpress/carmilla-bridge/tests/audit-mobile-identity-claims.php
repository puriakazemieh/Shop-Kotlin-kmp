<?php
/** Demonstrate the missing mobile-meta protection only in disposable test data. */
$prefix = getenv( 'CARMILLA_LOGIN_TEST_PREFIX' );
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || ! preg_match( '/\Aqa_[0-9a-f]{12}_\z/', (string) $prefix ) ) {
	fwrite( STDERR, "Disposable test flag and random prefix required.\n" ); exit( 2 );
}
define( 'ABSPATH', '/usr/src/wordpress/' ); define( 'WP_INSTALLING', true ); define( 'WP_DEBUG', false );
define( 'DB_NAME', 'carmilla_login_qa' ); define( 'DB_USER', 'synthetic_qa' ); define( 'DB_PASSWORD', 'synthetic_qa_password' ); define( 'DB_HOST', '127.0.0.1:3306' );
define( 'DB_CHARSET', 'utf8mb4' ); define( 'DB_COLLATE', '' ); define( 'WP_HOME', 'https://qa.example.test/wordpress' ); define( 'WP_SITEURL', WP_HOME );
foreach ( array( 'AUTH_KEY', 'SECURE_AUTH_KEY', 'LOGGED_IN_KEY', 'NONCE_KEY', 'AUTH_SALT', 'SECURE_AUTH_SALT', 'LOGGED_IN_SALT', 'NONCE_SALT' ) as $name ) { define( $name, hash( 'sha256', 'synthetic-wp-' . $name ) ); }
$table_prefix = $prefix;
$_SERVER['HTTP_HOST'] = 'qa.example.test'; $_SERVER['SERVER_NAME'] = 'qa.example.test'; $_SERVER['SERVER_PORT'] = '443'; $_SERVER['HTTPS'] = 'on'; $_SERVER['REQUEST_METHOD'] = 'GET';

try {
	$pipes = array();
	$seed = proc_open( array( PHP_BINARY, __DIR__ . '/experiment-identity-user-lifecycle.php' ), array( array( 'pipe', 'r' ), array( 'pipe', 'w' ), array( 'pipe', 'w' ) ), $pipes );
	if ( ! is_resource( $seed ) ) { throw new RuntimeException( 'seed' ); }
	fclose( $pipes[0] ); $output = stream_get_contents( $pipes[1] ); fclose( $pipes[1] ); stream_get_contents( $pipes[2] ); fclose( $pipes[2] );
	if ( proc_close( $seed ) !== 0 || strpos( $output, 'User-row lifecycle only; not production protection.' ) === false ) { throw new RuntimeException( 'seed' ); }
	require ABSPATH . 'wp-settings.php'; global $wpdb; $wpdb->hide_errors(); $wpdb->suppress_errors( true );
	$mobile = '09129990001'; // Synthetic exact ASCII value, not a real-phone normalization rule.
	$owner = wp_insert_user( array( 'user_login' => $mobile, 'user_email' => 'synthetic-mobile-owner@example.test', 'user_pass' => 'synthetic-only-password' ) );
	$other = wp_insert_user( array( 'user_login' => 'synthetic_mobile_other', 'user_email' => 'synthetic-mobile-other@example.test', 'user_pass' => 'synthetic-only-password' ) );
	if ( is_wp_error( $owner ) || is_wp_error( $other ) || (int) $owner < 1 || (int) $other < 1 ) { throw new RuntimeException( 'fixture' ); }
	$first = add_user_meta( $owner, 'cb_mobile', $mobile ); $second = add_user_meta( $owner, 'cb_mobile', $mobile );
	$claim_before = (int) $wpdb->get_var( $wpdb->prepare( "SELECT owner_id FROM `{$prefix}identity_claims` WHERE claim_key=%s", $mobile ) );
	if ( ! $first || ! $second || $claim_before !== (int) $owner ) { throw new RuntimeException( 'fixture' ); }
	$changed = $wpdb->update( $wpdb->users, array( 'user_login' => 'synthetic_mobile_new_login' ), array( 'ID' => $owner ) );
	$claim_after = (int) $wpdb->get_var( $wpdb->prepare( "SELECT owner_id FROM `{$prefix}identity_claims` WHERE claim_key=%s", $mobile ) );
	$references = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->usermeta} WHERE user_id=%d AND meta_key=%s AND BINARY meta_value=BINARY %s", $owner, 'cb_mobile', $mobile ) );
	if ( $changed !== 1 || $claim_after !== 0 || $references !== 2 ) { throw new RuntimeException( 'unexpected-result' ); }
	echo "PASS reproduced premature release while two same-owner mobile references remain\n";
	$conflict = add_user_meta( $other, 'cb_mobile', $mobile );
	$owners = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(DISTINCT user_id) FROM {$wpdb->usermeta} WHERE meta_key=%s AND BINARY meta_value=BINARY %s", 'cb_mobile', $mobile ) );
	if ( ! $conflict || $owners !== 2 ) { throw new RuntimeException( 'unexpected-result' ); }
	echo "PASS reproduced conflicting WordPress mobile metadata across two owners\n";
	$billing = add_user_meta( $other, 'billing_phone', 'synthetic-contact-only' );
	$billing_claim = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM `{$prefix}identity_claims` WHERE claim_key=%s", 'synthetic-contact-only' ) );
	if ( ! $billing || $billing_claim !== 0 ) { throw new RuntimeException( 'unexpected-result' ); }
	echo "PASS unrelated billing contact is not a login claim in this fixture\n";
	echo "Unsafe baseline reproduced; mobile identity is not protected.\n";
} catch ( Throwable $e ) {
	$known = array( 'seed', 'fixture', 'unexpected-result' ); $category = in_array( $e->getMessage(), $known, true ) ? $e->getMessage() : get_class( $e );
	fwrite( STDERR, 'FAIL mobile identity audit: ' . $category . "\n" ); exit( 1 );
}
