<?php
/** Disposable installed WordPress only; verify booking rollback and ownership. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/helpers.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-clinic-controller.php';
global $wpdb;
cb_create_tables();
$table = cb_bookings_table();
$controller = new CB_Clinic_Controller();
$failures = 0;
$previous_user = get_current_user_id();
$suffix = bin2hex( random_bytes( 5 ) );
$user_id = wp_create_user( 'qa_booking_atomic_' . $suffix, wp_generate_password( 24 ), 'qa-booking-' . $suffix . '@example.test' );
if ( ! is_int( $user_id ) ) { throw new RuntimeException( 'User fixture unavailable.' ); }
wp_set_current_user( $user_id );
foreach ( array( 'engine_unreadable', 'engine_unsupported', 'post_insert', 'meta_insert', 'booking_update', 'healthy' ) as $case ) {
	$suffix = bin2hex( random_bytes( 4 ) );
	$therapist = wp_insert_post( array( 'post_type' => 'cb_therapist', 'post_status' => 'publish', 'post_title' => 'QA booking ' . $suffix ), true );
	if ( is_wp_error( $therapist ) || ! $therapist ) { throw new RuntimeException( 'Therapist fixture unavailable.' ); }
	$slot = gmdate( 'Y-m-d H:i:s', time() + 86400 );
	update_post_meta( $therapist, 'cb_slots', $slot );
	$before_id = (int) $wpdb->get_var( "SELECT MAX(ID) FROM {$wpdb->posts}" );
	$request = new WP_REST_Request( 'POST', '/api/clinic/appointments' );
	$request->set_body( wp_json_encode( array( 'slotId' => CB_Clinic_Controller::slot_id( $therapist, 0 ), 'notes' => 'QA ' . $case ) ) );
	$request->set_header( 'content-type', 'application/json' );
	$lock_seen = false;
	$injected = false;
	$filter = static function ( string $sql ) use ( $case, $table, $wpdb, &$lock_seen, &$injected ): string {
		if ( strpos( $sql, 'SELECT TABLE_NAME,ENGINE FROM information_schema.TABLES' ) === 0 && $case === 'engine_unreadable' ) {
			$injected = true;
			return 'SELECT TABLE_NAME,ENGINE FROM cb_qa_missing_booking_engine';
		}
		if ( strpos( $sql, 'SELECT TABLE_NAME,ENGINE FROM information_schema.TABLES' ) === 0 && $case === 'engine_unsupported' ) {
			$injected = true;
			return str_replace( 'TABLE_NAME,ENGINE', "TABLE_NAME,'MyISAM' AS ENGINE", $sql );
		}
		if ( strpos( $sql, "INSERT IGNORE INTO $table" ) === 0 ) { $lock_seen = true; return $sql; }
		if ( ! $lock_seen || $injected ) { return $sql; }
		$target = ( $case === 'post_insert' && strpos( $sql, "INSERT INTO `{$wpdb->posts}`" ) === 0 && strpos( $sql, 'cb_appointment' ) !== false )
			|| ( $case === 'meta_insert' && strpos( $sql, "INSERT INTO `{$wpdb->postmeta}`" ) === 0 && strpos( $sql, 'cb_therapist_id' ) !== false )
			|| ( $case === 'booking_update' && strpos( $sql, "UPDATE `{$table}`" ) === 0 && strpos( $sql, 'appointment_id' ) !== false );
		if ( $target ) { $injected = true; return 'UPDATE cb_qa_missing_booking_stage SET x=1'; }
		return $sql;
	};
	add_filter( 'query', $filter );
	$response = null;
	$error = null;
	try { $response = $controller->book( $request ); } catch ( Throwable $caught ) { $error = get_class( $caught ) . ': ' . $caught->getMessage(); }
	remove_filter( 'query', $filter );
	$lock = $wpdb->get_row( $wpdb->prepare( "SELECT id,appointment_id,user_id FROM $table WHERE therapist_id=%d AND slot_time=%s", $therapist, $slot ), ARRAY_A );
	$posts = $wpdb->get_col( $wpdb->prepare( "SELECT ID FROM {$wpdb->posts} WHERE post_type='cb_appointment' AND ID>%d ORDER BY ID", $before_id ) );
	$meta_count = 0;
	foreach ( $posts as $post_id ) { $meta_count += (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM {$wpdb->postmeta} WHERE post_id=%d AND meta_key LIKE 'cb_%%'", $post_id ) ); }
	$status = $response instanceof WP_REST_Response ? $response->get_status() : 'exception';
	$data = $response instanceof WP_REST_Response ? $response->get_data() : array();
	$owner = count( $posts ) === 1 ? (int) get_post( (int) $posts[0] )->post_author : 0;
	$ok = $case === 'healthy'
		? ( ! $injected && $status === 201 && $lock && count( $posts ) === 1 && (int) $lock['appointment_id'] === (int) $posts[0] && (int) $lock['user_id'] === $user_id && $owner === $user_id && (int) ( $data['id'] ?? 0 ) === (int) $posts[0] )
		: ( $injected && $status === 503 && ( $data['errorCode'] ?? '' ) === 'BOOKING_UNAVAILABLE' && ! $lock && !$posts && $meta_count === 0 );
	if ( ! $ok ) { ++$failures; }
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . "$case injected=" . ( $injected ? 'yes' : 'no' ) . " api=$status lock=" . ( $lock ? 1 : 0 ) . ' appointment_id=' . (int) ( $lock['appointment_id'] ?? 0 ) . ' posts=' . count( $posts ) . " meta=$meta_count" . ( $error ? " error=$error" : '' ) . "\n";
	if ( $case === 'healthy' ) {
		$duplicate = $controller->book( $request );
		$duplicate_lock_count = (int) $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(*) FROM $table WHERE therapist_id=%d AND slot_time=%s", $therapist, $slot ) );
		$duplicate_ok = $duplicate->get_status() === 409 && ( $duplicate->get_data()['errorCode'] ?? '' ) === 'SLOT_TAKEN' && $duplicate_lock_count === 1;
		if ( ! $duplicate_ok ) { ++$failures; }
		echo ( $duplicate_ok ? 'PASS' : 'FAIL' ) . " duplicate slot keeps one lock status=" . $duplicate->get_status() . " rows=$duplicate_lock_count\n";
	}
	$wpdb->delete( $table, array( 'therapist_id' => $therapist ), array( '%d' ) );
	foreach ( $posts as $post_id ) { wp_delete_post( (int) $post_id, true ); }
	wp_delete_post( $therapist, true );
}
wp_set_current_user( $previous_user );
wp_delete_user( $user_id );
echo "Failures: $failures.\n";
exit( $failures ? 1 : 0 );
