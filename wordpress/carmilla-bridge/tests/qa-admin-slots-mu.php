<?php
/** Temporary HTTP fault injection for disposable WordPress only. Copy to mu-plugins for the QA run. */
if ( ! file_exists( '/tmp/carmilla-qa-admin-slots-enabled' ) ) { return; }
add_filter( 'cb_enable_health_lms', '__return_true' );
add_filter( 'wp_is_application_passwords_available', '__return_true' );
add_filter( 'pre_option_cb_manifest_features', static function () { return array( 'clinic.booking' => true ); } );
add_filter( 'query', static function ( string $sql ): string {
	if ( ( $_SERVER['HTTP_X_CARMILLA_QA_DB_FAILURE'] ?? '' ) !== 'synthetic' ) { return $sql; }
	if ( strpos( $sql, 'SELECT slot_time FROM ' ) !== false && strpos( $sql, 'cb_bookings' ) !== false ) {
		return 'SELECT slot_time FROM cb_synthetic_missing_bookings';
	}
	return $sql;
} );
