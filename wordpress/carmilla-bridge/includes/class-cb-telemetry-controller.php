<?php
if ( ! defined( 'ABSPATH' ) ) { exit; }

/** Privacy-first aggregate telemetry; no user, payment or free-text payload is stored. */
class CB_Telemetry_Controller {
	private const OPTION = 'cb_telemetry_aggregate_v1';
	private const EVENTS = array( 'app_activated', 'pwa_installed', 'catalog_viewed', 'cart_changed', 'checkout_started', 'checkout_result', 'app_error', 'support_opened' );

	public function register_routes(): void {
		register_rest_route( CB_REST_NAMESPACE, '/telemetry/events', array(
			'methods' => 'POST', 'callback' => array( $this, 'capture' ),
			'permission_callback' => '__return_true',
		) );
	}
	public function capture( WP_REST_Request $request ) {
		$body = $request->get_json_params();
		if ( ! is_array( $body ) || empty( $body['consent'] ) || ! in_array( $body['name'] ?? '', self::EVENTS, true ) ) {
			return new WP_Error( 'cb_telemetry_rejected', 'Telemetry event rejected.', array( 'status' => 400 ) );
		}
		$client = sanitize_key( (string) ( $body['client'] ?? '' ) );
		$tenant = sanitize_key( (string) ( $body['tenantId'] ?? '' ) );
		if ( ! in_array( $client, array( 'pwa', 'android', 'web' ), true ) || $tenant === '' ) {
			return new WP_Error( 'cb_telemetry_rejected', 'Telemetry context rejected.', array( 'status' => 400 ) );
		}
		$aggregate = get_option( self::OPTION, array() ); $key = gmdate( 'Y-m-d' ) . '|' . $tenant . '|' . $client . '|' . $body['name'];
		$aggregate[ $key ] = isset( $aggregate[ $key ] ) ? (int) $aggregate[ $key ] + 1 : 1;
		update_option( self::OPTION, $aggregate, false );
		return new WP_REST_Response( array( 'accepted' => true ), 202 );
	}
	public function register_admin_page(): void {
		add_submenu_page( 'carmilla-settings', 'Carmilla Telemetry', 'Telemetry', 'manage_options', 'carmilla-telemetry', array( $this, 'render_admin_page' ) );
	}
	public function render_admin_page(): void {
		if ( ! current_user_can( 'manage_options' ) ) { return; }
		$aggregate = get_option( self::OPTION, array() ); ?>
		<div class="wrap"><h1>Carmilla Telemetry</h1><p>Only consented, aggregated and redacted synthetic/product events are shown.</p><table class="widefat"><thead><tr><th>Bucket</th><th>Count</th></tr></thead><tbody><?php foreach ( $aggregate as $key => $count ) : ?><tr><td><?php echo esc_html( $key ); ?></td><td><?php echo esc_html( (string) $count ); ?></td></tr><?php endforeach; ?></tbody></table></div><?php
	}
}
