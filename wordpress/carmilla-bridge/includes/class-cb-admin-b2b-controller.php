<?php
/**
 * Admin B2B — organizations and course seats (options cb_organizations /
 * cb_seats). Buying seats mints seat rows; assigning a seat grants the assignee
 * course access (seeds a progress record so my-courses lists it).
 *
 *   GET/POST api/admin/organizations
 *   GET/POST api/admin/organizations/{id}/seats     (POST = BuySeatsRequestDto)
 *   POST     api/admin/organizations/{id}/seats/assign  (AssignSeatRequestDto)
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Admin_B2B_Controller {

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$admin = array( 'CB_Plugin', 'require_admin' );

		register_rest_route( $ns, '/api/admin/organizations', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_orgs' ), 'permission_callback' => $admin ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'create_org' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/organizations/(?P<id>\d+)/seats', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_seats' ), 'permission_callback' => $admin ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'buy_seats' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/organizations/(?P<id>\d+)/seats/assign', array( 'methods' => 'POST', 'callback' => array( $this, 'assign_seat' ), 'permission_callback' => $admin ) );
	}

	private function orgs(): array {
		$v = get_option( 'cb_organizations', array() );
		return is_array( $v ) ? $v : array();
	}

	private function seats(): array {
		$v = get_option( 'cb_seats', array() );
		return is_array( $v ) ? $v : array();
	}

	public function list_orgs(): WP_REST_Response {
		return cb_response( array_values( $this->orgs() ) );
	}

	public function create_org( WP_REST_Request $request ): WP_REST_Response {
		$b     = $request->get_json_params();
		$list  = $this->orgs();
		$id    = (int) get_option( 'cb_org_seq', 0 ) + 1;
		update_option( 'cb_org_seq', $id, false );
		$org   = array(
			'id'           => $id,
			'name'         => sanitize_text_field( (string) ( $b['name'] ?? '' ) ),
			'contactEmail' => isset( $b['contactEmail'] ) ? sanitize_email( (string) $b['contactEmail'] ) : null,
			'createdAt'    => gmdate( 'c' ),
		);
		$list[] = $org;
		update_option( 'cb_organizations', $list, false );
		return cb_response( $org, 201 );
	}

	public function list_seats( WP_REST_Request $request ): WP_REST_Response {
		$org = (int) $request['id'];
		$out = array_values( array_filter( $this->seats(), function ( $s ) use ( $org ) {
			return (int) $s['organizationId'] === $org;
		} ) );
		return cb_response( $out );
	}

	public function buy_seats( WP_REST_Request $request ): WP_REST_Response {
		$org   = (int) $request['id'];
		$b     = $request->get_json_params();
		$count = max( 1, (int) ( $b['count'] ?? 1 ) );
		$cid   = (int) ( $b['courseId'] ?? 0 );
		$seats = $this->seats();
		$made  = array();
		for ( $i = 0; $i < $count; $i++ ) {
			$id  = (int) get_option( 'cb_seat_seq', 0 ) + 1;
			update_option( 'cb_seat_seq', $id, false );
			$seat = array( 'id' => $id, 'organizationId' => $org, 'courseId' => $cid, 'assignedUserId' => null, 'assignedEmail' => null, 'assignedAt' => null );
			$seats[] = $seat;
			$made[]  = $seat;
		}
		update_option( 'cb_seats', $seats, false );
		return cb_response( $made, 201 );
	}

	public function assign_seat( WP_REST_Request $request ): WP_REST_Response {
		$org   = (int) $request['id'];
		$b     = $request->get_json_params();
		$cid   = (int) ( $b['courseId'] ?? 0 );
		$email = sanitize_email( (string) ( $b['email'] ?? '' ) );
		$seats = $this->seats();
		foreach ( $seats as &$s ) {
			if ( (int) $s['organizationId'] === $org && (int) $s['courseId'] === $cid && empty( $s['assignedEmail'] ) ) {
				$s['assignedEmail'] = $email;
				$s['assignedAt']    = gmdate( 'c' );
				$user               = get_user_by( 'email', $email );
				if ( $user ) {
					$s['assignedUserId'] = (int) $user->ID;
					// Grant course access: seed a progress record.
					if ( ! is_array( get_user_meta( $user->ID, "cb_course_prog_$cid", true ) ) ) {
						update_user_meta( $user->ID, "cb_course_prog_$cid", array() );
					}
				}
				update_option( 'cb_seats', $seats, false );
				return cb_response( $s );
			}
		}
		return cb_error( 'صندلی خالی موجود نیست', 409, 'NO_FREE_SEAT', 'api/admin/organizations' );
	}
}
