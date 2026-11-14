<?php
/**
 * Temporary HTTP fault injection and cancellation route enablement for disposable WordPress QA only.
 * Enables CB_Clinic_Controller::cancel for testing appointment cancellation consistency.
 */
if ( ! file_exists( '/tmp/carmilla-qa-clinic-cancel-enabled' ) ) {
	return;
}

add_filter( 'cb_enable_health_lms', '__return_true' );
add_filter( 'wp_is_application_passwords_available', '__return_true' );
add_filter( 'pre_option_cb_manifest_features', static function () {
	return array( 'clinic.booking' => true );
} );

// Override cancellation route to point directly to CB_Clinic_Controller::cancel
add_action( 'rest_api_init', static function () {
	register_rest_route( 'carmilla/v1', '/api/clinic/appointments/(?P<id>\d+)/cancel', array(
		'methods'             => 'POST',
		'callback'            => array( new CB_Clinic_Controller(), 'cancel' ),
		'permission_callback' => array( 'CB_Plugin', 'require_login' ),
	), true );
}, 99 );

// Fault injection: simulate failure during slot release (DELETE FROM cb_bookings)
add_filter( 'query', static function ( string $sql ): string {
	if ( ( $_SERVER['HTTP_X_CARMILLA_QA_DB_FAILURE'] ?? '' ) !== 'synthetic' ) {
		return $sql;
	}
	if ( strpos( $sql, 'DELETE FROM' ) !== false && strpos( $sql, 'cb_bookings' ) !== false ) {
		return 'DELETE FROM cb_qa_missing_cancel_lock WHERE id=1';
	}
	return $sql;
} );
