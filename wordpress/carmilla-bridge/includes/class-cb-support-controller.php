<?php
/**
 * Support tickets — stored in user meta cb_tickets (each with an embedded
 * message thread). Shaped to the app's support DTOs.
 *
 *   GET  api/support/tickets                     -> List<SupportTicketResponse>
 *   GET  api/support/tickets/{id}                -> SupportTicketDetailResponse
 *   POST api/support/tickets                     -> SupportTicketDetailResponse (CreateTicketRequestDto)
 *   POST api/support/tickets/{id}/messages       -> SupportTicketDetailResponse (PostMessageRequestDto)
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Support_Controller {

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$login = array( 'CB_Plugin', 'require_login' );

		register_rest_route( $ns, '/api/support/tickets', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_tickets' ), 'permission_callback' => $login ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'create_ticket' ), 'permission_callback' => $login ),
		) );
		register_rest_route( $ns, '/api/support/tickets/(?P<id>\d+)', array( 'methods' => 'GET', 'callback' => array( $this, 'get_ticket' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/support/tickets/(?P<id>\d+)/messages', array( 'methods' => 'POST', 'callback' => array( $this, 'post_message' ), 'permission_callback' => $login ) );
	}

	private function tickets( int $uid ): array {
		$v = get_user_meta( $uid, 'cb_tickets', true );
		return is_array( $v ) ? array_values( $v ) : array();
	}

	private function summary_dto( array $t ): array {
		$msgs = $t['messages'] ?? array();
		$last = end( $msgs );
		return array(
			'id'          => (int) $t['id'],
			'subject'     => (string) $t['subject'],
			'status'      => (string) $t['status'],
			'lastMessage' => $last ? (string) $last['body'] : null,
			'createdAt'   => (string) $t['createdAt'],
			'updatedAt'   => (string) $t['updatedAt'],
		);
	}

	private function detail_dto( array $t, int $uid ): array {
		return array(
			'id'        => (int) $t['id'],
			'subject'   => (string) $t['subject'],
			'status'    => (string) $t['status'],
			'userId'    => $uid,
			'messages'  => array_values( $t['messages'] ?? array() ),
			'createdAt' => (string) $t['createdAt'],
			'updatedAt' => (string) $t['updatedAt'],
		);
	}

	public function list_tickets(): WP_REST_Response {
		$out = array_map( array( $this, 'summary_dto' ), $this->tickets( get_current_user_id() ) );
		return cb_response( array_values( $out ) );
	}

	public function get_ticket( WP_REST_Request $request ): WP_REST_Response {
		$uid = get_current_user_id();
		$id  = (int) $request['id'];
		foreach ( $this->tickets( $uid ) as $t ) {
			if ( (int) $t['id'] === $id ) {
				return cb_response( $this->detail_dto( $t, $uid ) );
			}
		}
		return cb_error( 'تیکت یافت نشد', 404, 'NOT_FOUND', 'api/support/tickets' );
	}

	public function create_ticket( WP_REST_Request $request ): WP_REST_Response {
		$uid   = get_current_user_id();
		$body  = $request->get_json_params();
		$list  = $this->tickets( $uid );
		$now   = gmdate( 'c' );
		$ticket = array(
			'id'        => count( $list ) + 1,
			'subject'   => sanitize_text_field( (string) ( $body['subject'] ?? '' ) ),
			'status'    => 'OPEN',
			'createdAt' => $now,
			'updatedAt' => $now,
			'messages'  => array( array(
				'id'         => 1,
				'senderRole' => 'USER',
				'body'       => sanitize_textarea_field( (string) ( $body['message'] ?? '' ) ),
				'createdAt'  => $now,
			) ),
		);
		$list[] = $ticket;
		update_user_meta( $uid, 'cb_tickets', $list );
		return cb_response( $this->detail_dto( $ticket, $uid ), 201 );
	}

	public function post_message( WP_REST_Request $request ): WP_REST_Response {
		$uid   = get_current_user_id();
		$id    = (int) $request['id'];
		$list  = $this->tickets( $uid );
		$found = null;
		foreach ( $list as &$t ) {
			if ( (int) $t['id'] === $id ) {
				$msgs = $t['messages'] ?? array();
				$msgs[] = array(
					'id'         => count( $msgs ) + 1,
					'senderRole' => 'USER',
					'body'       => sanitize_textarea_field( (string) ( $request->get_json_params()['body'] ?? '' ) ),
					'createdAt'  => gmdate( 'c' ),
				);
				$t['messages']  = $msgs;
				$t['status']    = 'OPEN';
				$t['updatedAt'] = gmdate( 'c' );
				$found          = $t;
			}
		}
		unset( $t );
		if ( ! $found ) {
			return cb_error( 'تیکت یافت نشد', 404, 'NOT_FOUND', 'api/support/tickets' );
		}
		update_user_meta( $uid, 'cb_tickets', $list );
		return cb_response( $this->detail_dto( $found, $uid ) );
	}
}
