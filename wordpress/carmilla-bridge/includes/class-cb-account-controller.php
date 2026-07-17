<?php
/**
 * Account endpoints — profile (api/users/me) and shipping addresses
 * (api/addresses). Addresses live in user meta (cb_addresses).
 *
 * NOTE: the app calls these with a leading slash (/api/users/me, /api/addresses),
 * which resolves relative to the host root when the base URL has no path. Under
 * the WordPress base (/wp-json/carmilla/v1/), the `wp` app flavor's override
 * uses relative paths so they land inside this namespace.
 *
 *   GET   api/users/me            -> ProfileResponse
 *   PATCH api/users/me            -> ProfileResponse            (UpdateProfileRequest)
 *   GET   api/addresses           -> List<AddressResponse>
 *   GET   api/addresses/default   -> AddressResponse
 *   GET   api/addresses/{id}      -> AddressResponse
 *   POST  api/addresses           -> AddressResponse            (CreateAddressRequest)
 *   PATCH api/addresses/{id}      -> AddressResponse            (UpdateAddressRequest)
 *   DELETE api/addresses/{id}     -> 204
 *   POST  api/addresses/{id}/default -> AddressResponse
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Account_Controller {

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$login = array( 'CB_Plugin', 'require_login' );

		register_rest_route( $ns, '/api/users/me', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'get_profile' ), 'permission_callback' => $login ),
			array( 'methods' => 'PATCH', 'callback' => array( $this, 'update_profile' ), 'permission_callback' => $login ),
		) );

		register_rest_route( $ns, '/api/addresses', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_addresses' ), 'permission_callback' => $login ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'create_address' ), 'permission_callback' => $login ),
		) );
		register_rest_route( $ns, '/api/addresses/default', array(
			'methods' => 'GET', 'callback' => array( $this, 'default_address' ), 'permission_callback' => $login,
		) );
		register_rest_route( $ns, '/api/addresses/(?P<id>\d+)', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'get_address' ), 'permission_callback' => $login ),
			array( 'methods' => 'PATCH', 'callback' => array( $this, 'update_address' ), 'permission_callback' => $login ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_address' ), 'permission_callback' => $login ),
		) );
		register_rest_route( $ns, '/api/addresses/(?P<id>\d+)/default', array(
			'methods' => 'POST', 'callback' => array( $this, 'set_default_address' ), 'permission_callback' => $login,
		) );
	}

	// ---- profile ------------------------------------------------------------

	private function profile_dto( WP_User $user ): array {
		$postcode = get_user_meta( $user->ID, 'billing_postcode', true );
		return array(
			'id'        => (int) $user->ID,
			'email'     => $user->user_email ?: null,
			'firstName' => $user->first_name ?: null,
			'lastName'  => $user->last_name ?: null,
			'phone'     => get_user_meta( $user->ID, 'billing_phone', true ) ?: null,
			'city'      => get_user_meta( $user->ID, 'billing_city', true ) ?: null,
			'postalCode' => is_numeric( $postcode ) ? (int) $postcode : null,
			'role'      => class_exists( 'CB_JWT' ) ? CB_JWT::primary_role( $user ) : ( $user->roles[0] ?? 'customer' ),
			'active'    => true,
			'createdAt' => cb_iso( $user->user_registered ),
			'updatedAt' => cb_iso( $user->user_registered ),
		);
	}

	public function get_profile(): WP_REST_Response {
		$user = wp_get_current_user();
		return cb_response( $this->profile_dto( $user ) );
	}

	public function update_profile( WP_REST_Request $request ): WP_REST_Response {
		$uid  = get_current_user_id();
		$body = $request->get_json_params();
		if ( array_key_exists( 'firstName', $body ) ) {
			update_user_meta( $uid, 'first_name', sanitize_text_field( (string) $body['firstName'] ) );
		}
		if ( array_key_exists( 'lastName', $body ) ) {
			update_user_meta( $uid, 'last_name', sanitize_text_field( (string) $body['lastName'] ) );
		}
		if ( array_key_exists( 'phone', $body ) ) {
			update_user_meta( $uid, 'billing_phone', sanitize_text_field( (string) $body['phone'] ) );
		}
		if ( array_key_exists( 'city', $body ) ) {
			update_user_meta( $uid, 'billing_city', sanitize_text_field( (string) $body['city'] ) );
		}
		if ( array_key_exists( 'postalCode', $body ) && $body['postalCode'] !== null ) {
			update_user_meta( $uid, 'billing_postcode', preg_replace( '/\D/', '', (string) $body['postalCode'] ) );
		}
		return cb_response( $this->profile_dto( wp_get_current_user() ) );
	}

	// ---- addresses ----------------------------------------------------------

	public function list_addresses(): WP_REST_Response {
		$out = array_map( 'cb_address_dto', cb_addresses( get_current_user_id() ) );
		return cb_response( array_values( $out ) );
	}

	public function default_address(): WP_REST_Response {
		foreach ( cb_addresses( get_current_user_id() ) as $a ) {
			if ( ! empty( $a['default'] ) ) {
				return cb_response( cb_address_dto( $a ) );
			}
		}
		return cb_error( 'آدرس پیش‌فرض یافت نشد', 404, 'NOT_FOUND', 'api/addresses/default' );
	}

	public function get_address( WP_REST_Request $request ): WP_REST_Response {
		$a = cb_find_address( get_current_user_id(), (int) $request['id'] );
		if ( ! $a ) {
			return cb_error( 'آدرس یافت نشد', 404, 'NOT_FOUND', 'api/addresses' );
		}
		return cb_response( cb_address_dto( $a ) );
	}

	public function create_address( WP_REST_Request $request ): WP_REST_Response {
		$uid   = get_current_user_id();
		$body  = $request->get_json_params();
		$lines = cb_addresses( $uid );
		$seq   = (int) get_user_meta( $uid, 'cb_addr_seq', true ) + 1;
		update_user_meta( $uid, 'cb_addr_seq', $seq );

		$set_default = ! empty( $body['setAsDefault'] ) || empty( $lines );
		if ( $set_default ) {
			foreach ( $lines as &$l ) {
				$l['default'] = false;
			}
			unset( $l );
		}
		$addr = array(
			'id'            => $seq,
			'receiverName'  => sanitize_text_field( (string) ( $body['receiverName'] ?? '' ) ),
			'receiverPhone' => sanitize_text_field( (string) ( $body['receiverPhone'] ?? '' ) ),
			'country'       => sanitize_text_field( (string) ( $body['country'] ?? 'IR' ) ),
			'province'      => sanitize_text_field( (string) ( $body['province'] ?? '' ) ),
			'city'          => sanitize_text_field( (string) ( $body['city'] ?? '' ) ),
			'addressLine1'  => sanitize_text_field( (string) ( $body['addressLine1'] ?? '' ) ),
			'addressLine2'  => isset( $body['addressLine2'] ) ? sanitize_text_field( (string) $body['addressLine2'] ) : null,
			'postalCode'    => isset( $body['postalCode'] ) ? sanitize_text_field( (string) $body['postalCode'] ) : null,
			'default'       => $set_default,
			'createdAt'     => gmdate( 'c' ),
		);
		$lines[] = $addr;
		cb_save_addresses( $uid, $lines );
		return cb_response( cb_address_dto( $addr ), 201 );
	}

	public function update_address( WP_REST_Request $request ): WP_REST_Response {
		$uid   = get_current_user_id();
		$id    = (int) $request['id'];
		$body  = $request->get_json_params();
		$lines = cb_addresses( $uid );
		$found = null;
		foreach ( $lines as &$a ) {
			if ( (int) $a['id'] === $id ) {
				foreach ( array( 'receiverName', 'receiverPhone', 'country', 'province', 'city', 'addressLine1', 'addressLine2', 'postalCode' ) as $k ) {
					if ( array_key_exists( $k, $body ) ) {
						$a[ $k ] = $body[ $k ] !== null ? sanitize_text_field( (string) $body[ $k ] ) : null;
					}
				}
				$found = $a;
			}
		}
		unset( $a );
		if ( ! $found ) {
			return cb_error( 'آدرس یافت نشد', 404, 'NOT_FOUND', 'api/addresses' );
		}
		cb_save_addresses( $uid, $lines );
		return cb_response( cb_address_dto( $found ) );
	}

	public function delete_address( WP_REST_Request $request ): WP_REST_Response {
		$uid   = get_current_user_id();
		$id    = (int) $request['id'];
		$lines = array_values( array_filter( cb_addresses( $uid ), function ( $a ) use ( $id ) {
			return (int) $a['id'] !== $id;
		} ) );
		cb_save_addresses( $uid, $lines );
		return cb_response( null, 204 );
	}

	public function set_default_address( WP_REST_Request $request ): WP_REST_Response {
		$uid   = get_current_user_id();
		$id    = (int) $request['id'];
		$lines = cb_addresses( $uid );
		$found = null;
		foreach ( $lines as &$a ) {
			$a['default'] = ( (int) $a['id'] === $id );
			if ( $a['default'] ) {
				$found = $a;
			}
		}
		unset( $a );
		if ( ! $found ) {
			return cb_error( 'آدرس یافت نشد', 404, 'NOT_FOUND', 'api/addresses' );
		}
		cb_save_addresses( $uid, $lines );
		return cb_response( cb_address_dto( $found ) );
	}
}
