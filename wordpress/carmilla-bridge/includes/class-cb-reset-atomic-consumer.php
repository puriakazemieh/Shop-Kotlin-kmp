<?php
/** Consume a database-backed password-reset mapping exactly once. */
if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

final class CB_Reset_Atomic_Consumer {
	public function consume( string $key, string $expected_login ): bool {
		global $wpdb;
		if ( wp_using_ext_object_cache() || ! preg_match( '/\Acb_reset_[a-f0-9]{64}\z/', $key ) || $expected_login === '' ) {
			return false;
		}
		$name = '_transient_' . $key;
		$expiry_name = '_transient_timeout_' . $key;
		$wpdb->last_error = '';
		$value = $wpdb->get_var( $wpdb->prepare( "SELECT option_value FROM {$wpdb->options} WHERE option_name = %s", $name ) );
		if ( $wpdb->last_error || ! is_string( $value ) || ! hash_equals( $expected_login, $value ) ) {
			return false;
		}
		$wpdb->last_error = '';
		$deleted = $wpdb->query( $wpdb->prepare(
			"DELETE mapping FROM {$wpdb->options} AS mapping INNER JOIN {$wpdb->options} AS expiry ON expiry.option_name = %s WHERE mapping.option_name = %s AND BINARY mapping.option_value = BINARY %s AND expiry.option_value REGEXP '^[0-9]+$' AND CAST(expiry.option_value AS UNSIGNED) > UNIX_TIMESTAMP()",
			$expiry_name,
			$name,
			$value
		) );
		if ( $deleted !== 1 || $wpdb->last_error ) {
			return false;
		}
		wp_cache_delete( $name, 'options' );
		return true;
	}
}
