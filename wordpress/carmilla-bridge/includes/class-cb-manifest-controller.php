<?php
/**
 * Canonical, public client capability manifest. It deliberately exposes no
 * credential, private option or backend-origin override.
 */
if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Manifest_Controller {
	public function register_routes(): void {
		register_rest_route( CB_REST_NAMESPACE, '/client-manifest', array(
			'methods'             => 'GET',
			'callback'            => array( $this, 'get_manifest' ),
			'permission_callback' => '__return_true',
		) );
	}

	public function get_manifest( WP_REST_Request $request ): WP_REST_Response {
		$payload = array(
			'schemaVersion'     => 1,
			'manifestVersion'   => (string) get_option( 'cb_manifest_version', '2026.08.1' ),
			'backendProfile'    => 'WORDPRESS',
			'tenantId'          => sanitize_key( (string) get_option( 'cb_manifest_tenant_id', get_current_blog_id() ) ),
			'minimumAppVersion' => (string) get_option( 'cb_manifest_min_client', '1.0.0' ),
			'issuedAt'          => gmdate( 'c' ),
			'expiresAt'         => gmdate( 'c', time() + DAY_IN_SECONDS ),
			'features'          => self::resolve_features( get_option( 'cb_manifest_features', array() ) ),
		);
		$etag = '"' . hash( 'sha256', wp_json_encode( $payload ) ) . '"';
		if ( $request->get_header( 'if-none-match' ) === $etag ) {
			$response = new WP_REST_Response( null, 304 );
			$response->header( 'ETag', $etag );
			return $response;
		}
		$response = new WP_REST_Response( $payload, 200 );
		$response->header( 'ETag', $etag );
		$response->header( 'Cache-Control', 'public, max-age=300' );
		return $response;
	}

	/** Visible for a dependency-free smoke test. */
	public static function resolve_features( $configured ): array {
		$defaults = array(
			'content.blog' => true, 'commerce.core' => true, 'commerce.physical' => true,
			'commerce.digital' => false, 'academy.core' => false, 'academy.quiz' => false,
			'academy.certificate' => false, 'clinic.booking' => false, 'clinic.messaging' => false,
			'psych.tests' => false, 'wallet' => false, 'admin.mobile' => false,
		);
		$configured = is_array( $configured ) ? $configured : array();
		foreach ( $defaults as $id => $default ) {
			if ( array_key_exists( $id, $configured ) ) {
				$defaults[ $id ] = (bool) $configured[ $id ];
			}
		}
		$dependencies = array(
			'commerce.physical' => array( 'commerce.core' ), 'commerce.digital' => array( 'commerce.core' ),
			'academy.core' => array( 'content.blog' ), 'academy.quiz' => array( 'academy.core' ),
			'academy.certificate' => array( 'academy.core' ), 'clinic.booking' => array( 'content.blog' ),
			'clinic.messaging' => array( 'clinic.booking' ), 'psych.tests' => array( 'content.blog' ), 'wallet' => array( 'commerce.core' ),
		);
		foreach ( $dependencies as $child => $parents ) {
			foreach ( $parents as $parent ) {
				if ( empty( $defaults[ $parent ] ) ) { $defaults[ $child ] = false; break; }
			}
		}
		return $defaults;
	}
}
