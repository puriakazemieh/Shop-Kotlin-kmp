<?php
/** Verify the operational runtime, including its bounded schema migration. */
require __DIR__ . '/experiment-identity-shared-references.php';
require_once '/work/includes/class-cb-deletion-incident-runtime.php';

try {
	global $wpdb, $prefix, $wp_version;
	CB_Deletion_Incident_Runtime::boot();
	$table = $prefix . 'carmilla_deletion_incidents';
	shared_check( (int) $wpdb->get_var( $wpdb->prepare( 'SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=%s', $table ) ) === 1, 'runtime creates its bounded incident table' );
	$trigger = "CREATE TRIGGER `{$prefix}runtime_delete_fault` BEFORE DELETE ON {$wpdb->usermeta} FOR EACH ROW BEGIN IF @runtime_delete_mid=OLD.umeta_id THEN SIGNAL SQLSTATE '45000'; END IF; END";
	shared_check( $wpdb->query( $trigger ) !== false, 'create disposable partial-delete fault' );
	$admin = get_user_by( 'login', 'qa_admin' );
	if ( ! $admin instanceof WP_User ) { throw new RuntimeException(); }
	wp_set_current_user( (int) $admin->ID );
	$target = shared_user( 'runtime_partial', 'runtime_partial@example.test' );
	$mid = add_user_meta( $target, 'cb_mobile', '09128881111' );
	$wpdb->query( 'SET @runtime_delete_mid=' . (int) $mid );
	$reported = wp_delete_user( $target );
	$wpdb->query( 'SET @runtime_delete_mid=NULL' );
	$rows = $wpdb->get_results( $wpdb->prepare( "SELECT account_id,incident_code FROM `$table` WHERE account_id=%d", $target ), ARRAY_A );
	shared_check( $reported === true && get_user_by( 'id', $target ) instanceof WP_User && $rows === array( array( 'account_id' => (string) $target, 'incident_code' => 'PARTIAL_USER_SURVIVES' ), ), 'runtime records a native false-success survivor without changing the native return' );
	ob_start(); do_action( 'admin_notices' ); $notice = (string) ob_get_clean();
	shared_check( strpos( $notice, 'ممکن است حذف برخی کاربران کامل نشده باشد' ) !== false, 'runtime exposes an administrator warning without claiming recovery' );
	echo 'PASS operational deletion incident runtime WP ' . $wp_version . ' PHP ' . PHP_VERSION . "\n";
} catch ( Throwable $error ) {
	fwrite( STDERR, 'FAIL deletion incident runtime: ' . get_class( $error ) . "\n" ); exit( 1 );
}
