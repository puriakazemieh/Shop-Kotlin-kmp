<?php
/** Inactive, database-backed transient consumer. Not wired to any route. */
if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

final class CB_OTP_Atomic_Consumer {
	/**
	 * Consume only the exact, unexpired database-backed OTP hash that was checked.
	 * This does not issue OTPs, count attempts, or resolve a user identity.
	 */
	public function consume( string $mobile, string $purpose, string $code ): bool {
		global $wpdb;
		if ( wp_using_ext_object_cache() || ! in_array( $purpose, array( 'login', 'reset' ), true ) || trim( $mobile ) === '' || ! preg_match( '/\A[0-9]{6}\z/', trim( $code ) ) ) {
			return false;
		}

		$key = 'cb_otp_' . md5( trim( $mobile ) . '_' . $purpose );
		$name = '_transient_' . $key;
		$expiry_name = '_transient_timeout_' . $key;
		// Read from the authoritative database, not a per-process option cache.
		// $wpdb keeps the previous query error, so clear it before each operation;
		// otherwise an unrelated failed query would permanently reject every OTP.
		$wpdb->last_error = '';
		$hash = $wpdb->get_var( $wpdb->prepare( "SELECT option_value FROM {$wpdb->options} WHERE option_name = %s", $name ) );
		if ( $wpdb->last_error || ! is_string( $hash ) || $hash === '' || ! wp_check_password( trim( $code ), $hash ) ) {
			return false;
		}

		// The value check prevents two requests that observed the same hash from
		// both succeeding. Check expiry in the same statement, using DB time.
		$wpdb->last_error = '';
		$deleted = $wpdb->query( $wpdb->prepare(
			"DELETE otp FROM {$wpdb->options} AS otp INNER JOIN {$wpdb->options} AS expiry ON expiry.option_name = %s WHERE otp.option_name = %s AND BINARY otp.option_value = BINARY %s AND expiry.option_value REGEXP '^[0-9]+$' AND CAST(expiry.option_value AS UNSIGNED) > UNIX_TIMESTAMP()",
			$expiry_name,
			$name,
			$hash
		) );
		if ( $deleted !== 1 || $wpdb->last_error ) {
			return false;
		}
		wp_cache_delete( $name, 'options' );
		return true;
	}
}
