<?php
/** Disposable installed WordPress only; cancellation must keep status and lock aligned. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/helpers.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-clinic-controller.php';
global $wpdb;
cb_create_tables();
$failures = 0;
$check = static function ( bool $ok, string $label ) use ( &$failures ): void { if ( ! $ok ) { ++$failures; } echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n"; };
$suffix = bin2hex( random_bytes( 5 ) );
$user = wp_create_user( 'qa_cancel_' . $suffix, wp_generate_password( 24 ), 'qa-cancel-' . $suffix . '@example.test' );
if ( ! is_int( $user ) ) { exit( 2 ); }
$previous_user = get_current_user_id();
wp_set_current_user( $user );
$therapist = wp_insert_post( array( 'post_type' => 'cb_therapist', 'post_status' => 'publish', 'post_title' => 'QA cancel therapist ' . $suffix ), true );
$appointment = wp_insert_post( array( 'post_type' => 'cb_appointment', 'post_status' => 'publish', 'post_title' => 'QA cancel appointment ' . $suffix, 'post_author' => $user ), true );
if ( ! is_int( $therapist ) || ! is_int( $appointment ) ) { exit( 2 ); }
$slot = gmdate( 'Y-m-d H:i:s', time() + 86400 );
foreach ( array( 'cb_user_id' => $user, 'cb_therapist_id' => $therapist, 'cb_slot' => $slot, 'cb_status' => 'PENDING' ) as $key => $value ) { update_post_meta( $appointment, $key, $value ); }
$table = cb_bookings_table();
$wpdb->insert( $table, array( 'therapist_id' => $therapist, 'slot_time' => $slot, 'appointment_id' => $appointment, 'user_id' => $user, 'created_at' => current_time( 'mysql', true ) ) );
$controller = new CB_Clinic_Controller();
$request = new WP_REST_Request( 'POST', '/api/clinic/appointments/' . $appointment . '/cancel' );
$request->set_param( 'id', $appointment );
$status_db = static function () use ( $wpdb, $appointment ): string { return (string) $wpdb->get_var( $wpdb->prepare( "SELECT meta_value FROM {$wpdb->postmeta} WHERE post_id=%d AND meta_key='cb_status'", $appointment ) ); };
$lock_count = static function () use ( $wpdb, $table, $appointment ): int { return (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM $table WHERE appointment_id=%d", $appointment ) ); };
$inject_engine = static function ( string $sql ): string { return strpos( $sql, 'SELECT TABLE_NAME,ENGINE FROM information_schema.TABLES' ) === 0 ? 'SELECT TABLE_NAME,ENGINE FROM cb_qa_missing_cancel_engine' : $sql; };
$inject_status = static function ( string $sql ) use ( $wpdb ): string { return strpos( $sql, "UPDATE `{$wpdb->postmeta}`" ) === 0 && strpos( $sql, 'cb_status' ) !== false ? 'UPDATE cb_qa_missing_cancel_status SET x=1' : $sql; };
$inject_delete = static function ( string $sql ) use ( $table ): string { return strpos( $sql, "DELETE FROM `$table`" ) === 0 ? 'DELETE FROM cb_qa_missing_cancel_lock WHERE id=1' : $sql; };
try {
	add_filter( 'query', $inject_engine );
	$unreadable = $controller->cancel( $request );
	remove_filter( 'query', $inject_engine );
	$check( $unreadable->get_status() === 503 && $status_db() === 'PENDING' && $lock_count() === 1, 'unreadable engine metadata refuses cancellation without writes' );
	add_filter( 'query', $inject_status );
	$meta_failed = $controller->cancel( $request );
	remove_filter( 'query', $inject_status );
	$check( $meta_failed->get_status() === 503 && $status_db() === 'PENDING' && $lock_count() === 1, 'status metadata SQL fault preserves appointment and lock' );
	add_filter( 'query', $inject_delete );
	$failed = $controller->cancel( $request );
	remove_filter( 'query', $inject_delete );
	$check( $failed->get_status() === 503 && ( $failed->get_data()['errorCode'] ?? '' ) === 'CANCELLATION_UNAVAILABLE' && $status_db() === 'PENDING' && $lock_count() === 1, 'slot-delete SQL fault rolls back CANCELLED status and preserves lock' );
	$healthy = $controller->cancel( $request );
	$check( $healthy->get_status() === 200 && $status_db() === 'CANCELLED' && $lock_count() === 0, 'successful cancellation updates status and releases one lock' );
	$again = $controller->cancel( $request );
	$check( $again->get_status() === 200 && $status_db() === 'CANCELLED' && $lock_count() === 0, 'repeat cancellation is idempotent' );
	wp_set_current_user( $previous_user );
	$other = $controller->cancel( $request );
	$check( $other->get_status() === 404 && $status_db() === 'CANCELLED' && $lock_count() === 0, 'other user cannot cancel appointment' );
} finally {
	remove_filter( 'query', $inject_engine );
	remove_filter( 'query', $inject_status );
	remove_filter( 'query', $inject_delete );
	$wpdb->delete( $table, array( 'appointment_id' => $appointment ), array( '%d' ) );
	wp_delete_post( $appointment, true );
	wp_delete_post( $therapist, true );
	wp_set_current_user( $previous_user );
	wp_delete_user( $user );
}
echo "Failures: $failures.\n";
exit( $failures ? 1 : 0 );
