<?php
/**
 * Admin clinic — patient CRM (file, tags, notes, messages, homework, shared
 * journal), therapist match-question management, and organizational clinic
 * seats. Admin/shop-manager only.
 *
 *   GET/POST api/admin/therapists/appointments/{id}/notes
 *   GET  api/admin/therapists/{id}/patients ; GET api/admin/therapists/{id}/patients/{uid}
 *   PUT  api/admin/therapists/{id}/patients/{uid}/tags
 *   GET/POST api/admin/therapists/{id}/patients/{uid}/messages
 *   GET/POST api/admin/therapists/{id}/patients/{uid}/homework
 *   GET  api/admin/therapists/{id}/patients/{uid}/journal
 *   GET/POST api/admin/therapists/match-questions ; DELETE .../match-questions/{id}
 *   GET/POST api/admin/organizations/{id}/clinic-seats ; POST .../clinic-seats/assign
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Admin_Clinic_Controller {

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$admin = array( 'CB_Plugin', 'require_health_admin' );

		register_rest_route( $ns, '/api/admin/therapists/appointments/(?P<aid>\d+)/notes', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_notes' ), 'permission_callback' => $admin ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'add_note' ), 'permission_callback' => $admin ),
		) );

		register_rest_route( $ns, '/api/admin/therapists/(?P<id>\d+)/patients', array( 'methods' => 'GET', 'callback' => array( $this, 'list_patients' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/therapists/(?P<id>\d+)/patients/(?P<uid>\d+)', array( 'methods' => 'GET', 'callback' => array( $this, 'patient_file' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/therapists/(?P<id>\d+)/patients/(?P<uid>\d+)/tags', array( 'methods' => 'PUT', 'callback' => array( $this, 'set_tags' ), 'permission_callback' => $admin ) );
		register_rest_route( $ns, '/api/admin/therapists/(?P<id>\d+)/patients/(?P<uid>\d+)/messages', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_messages' ), 'permission_callback' => $admin ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'send_message' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/therapists/(?P<id>\d+)/patients/(?P<uid>\d+)/homework', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_homework' ), 'permission_callback' => $admin ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'assign_homework' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/therapists/(?P<id>\d+)/patients/(?P<uid>\d+)/journal', array( 'methods' => 'GET', 'callback' => array( $this, 'shared_journal' ), 'permission_callback' => $admin ) );

		register_rest_route( $ns, '/api/admin/therapists/match-questions', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_match_questions' ), 'permission_callback' => $admin ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'create_match_question' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/therapists/match-questions/(?P<qid>\d+)', array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_match_question' ), 'permission_callback' => $admin ) );

		register_rest_route( $ns, '/api/admin/organizations/(?P<id>\d+)/clinic-seats', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_clinic_seats' ), 'permission_callback' => $admin ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'buy_clinic_seats' ), 'permission_callback' => $admin ),
		) );
		register_rest_route( $ns, '/api/admin/organizations/(?P<id>\d+)/clinic-seats/assign', array( 'methods' => 'POST', 'callback' => array( $this, 'assign_clinic_seat' ), 'permission_callback' => $admin ) );
	}

	// ---- appointment notes --------------------------------------------------

	public function list_notes( WP_REST_Request $request ): WP_REST_Response {
		$aid  = (int) $request['aid'];
		$list = (array) get_post_meta( $aid, 'cb_notes_list', true );
		return cb_response( array_values( $list ) );
	}

	public function add_note( WP_REST_Request $request ): WP_REST_Response {
		$aid  = (int) $request['aid'];
		$list = array_values( (array) get_post_meta( $aid, 'cb_notes_list', true ) );
		$note = array(
			'id'            => count( $list ) + 1,
			'appointmentId' => $aid,
			'counselorId'   => get_current_user_id(),
			'note'          => sanitize_textarea_field( (string) ( $request->get_json_params()['note'] ?? '' ) ),
			'createdAt'     => gmdate( 'c' ),
		);
		$list[] = $note;
		update_post_meta( $aid, 'cb_notes_list', $list );
		return cb_response( array( 'id' => $note['id'] ), 201 );
	}

	// ---- patients -----------------------------------------------------------

	/** Appointment posts for a therapist. */
	private function therapist_appointments( int $tid ): array {
		return get_posts( array( 'post_type' => 'cb_appointment', 'post_status' => 'publish', 'numberposts' => 500, 'meta_key' => 'cb_therapist_id', 'meta_value' => $tid ) );
	}

	private function tags( int $tid, int $uid ): array {
		$v = get_user_meta( $uid, "cb_patient_tags_$tid", true );
		return is_array( $v ) ? array_values( $v ) : array();
	}

	public function list_patients( WP_REST_Request $request ): WP_REST_Response {
		$tid   = (int) $request['id'];
		$byUser = array();
		foreach ( $this->therapist_appointments( $tid ) as $p ) {
			$uid = (int) get_post_meta( $p->ID, 'cb_user_id', true );
			if ( ! isset( $byUser[ $uid ] ) ) {
				$byUser[ $uid ] = array( 'count' => 0, 'last' => null );
			}
			$byUser[ $uid ]['count']++;
			$slot = (string) get_post_meta( $p->ID, 'cb_slot', true );
			if ( ! $byUser[ $uid ]['last'] || strtotime( $slot ) > strtotime( $byUser[ $uid ]['last'] ) ) {
				$byUser[ $uid ]['last'] = $slot;
			}
		}
		$out = array();
		foreach ( $byUser as $uid => $info ) {
			$u     = get_userdata( $uid );
			$out[] = array(
				'userId'            => $uid,
				'userName'          => $u ? $u->display_name : 'کاربر',
				'therapistId'       => $tid,
				'appointmentCount'  => $info['count'],
				'lastAppointmentAt' => $info['last'] ? cb_iso( $info['last'] ) : null,
				'tags'              => $this->tags( $tid, $uid ),
			);
		}
		return cb_response( array_values( $out ) );
	}

	public function patient_file( WP_REST_Request $request ): WP_REST_Response {
		$tid = (int) $request['id'];
		$uid = (int) $request['uid'];
		$u   = get_userdata( $uid );
		$appointments = array();
		foreach ( $this->therapist_appointments( $tid ) as $p ) {
			if ( (int) get_post_meta( $p->ID, 'cb_user_id', true ) !== $uid ) {
				continue;
			}
			$slot = (string) get_post_meta( $p->ID, 'cb_slot', true );
			$ts   = strtotime( $slot ) ?: time();
			$appointments[] = array(
				'id'        => (int) $p->ID,
				'status'    => get_post_meta( $p->ID, 'cb_status', true ) ?: 'PENDING',
				'dayLabel'  => gmdate( 'Y-m-d', $ts ),
				'timeLabel' => gmdate( 'H:i', $ts ),
				'notes'     => array_values( (array) get_post_meta( $p->ID, 'cb_notes_list', true ) ),
			);
		}
		$tests = array();
		foreach ( (array) get_user_meta( $uid, 'cb_psych_attempts', true ) as $a ) {
			if ( ( $a['status'] ?? '' ) === 'COMPLETED' ) {
				$tests[] = array(
					'testTitle'      => (string) ( $a['testTitle'] ?? '' ),
					'totalScore'     => isset( $a['totalScore'] ) ? (int) $a['totalScore'] : null,
					'interpretation' => $a['interpretation'] ?? null,
					'completedAt'    => $a['completedAt'] ?? null,
				);
			}
		}
		return cb_response( array(
			'userId'       => $uid,
			'userName'     => $u ? $u->display_name : 'کاربر',
			'therapistId'  => $tid,
			'tags'         => $this->tags( $tid, $uid ),
			'appointments' => $appointments,
			'testResults'  => $tests,
		) );
	}

	public function set_tags( WP_REST_Request $request ): WP_REST_Response {
		$tid  = (int) $request['id'];
		$uid  = (int) $request['uid'];
		$tags = array_map( 'sanitize_text_field', (array) ( $request->get_json_params()['tags'] ?? array() ) );
		update_user_meta( $uid, "cb_patient_tags_$tid", array_values( $tags ) );
		return cb_response( null, 200 );
	}

	// ---- messaging with a patient (cb_msg comments on the therapist) --------

	public function list_messages( WP_REST_Request $request ): WP_REST_Response {
		$tid = (int) $request['id'];
		$uid = (int) $request['uid'];
		$out = array();
		foreach ( get_comments( array( 'post_id' => $tid, 'type' => 'cb_msg', 'meta_key' => 'cb_user_id', 'meta_value' => $uid, 'status' => 'approve', 'orderby' => 'comment_date_gmt', 'order' => 'ASC' ) ) as $c ) {
			$out[] = array(
				'id'         => (int) $c->comment_ID,
				'senderType' => user_can( (int) $c->user_id, 'edit_posts' ) ? 'THERAPIST' : 'PATIENT',
				'body'       => $c->comment_content,
				'createdAt'  => cb_iso( $c->comment_date_gmt ),
			);
		}
		return cb_response( $out );
	}

	public function send_message( WP_REST_Request $request ): WP_REST_Response {
		$tid  = (int) $request['id'];
		$uid  = (int) $request['uid'];
		$body = sanitize_textarea_field( (string) ( $request->get_json_params()['body'] ?? '' ) );
		if ( $body === '' ) {
			return cb_error( 'متن پیام خالی است', 400, 'EMPTY', 'api/admin/therapists' );
		}
		$me  = wp_get_current_user();
		$cid = wp_insert_comment( array( 'comment_post_ID' => $tid, 'comment_type' => 'cb_msg', 'comment_content' => $body, 'user_id' => $me->ID, 'comment_author' => $me->display_name, 'comment_approved' => 1 ) );
		add_comment_meta( $cid, 'cb_user_id', $uid ); // thread belongs to the patient
		return cb_response( array( 'id' => (int) $cid, 'senderType' => 'THERAPIST', 'body' => $body, 'createdAt' => gmdate( 'c' ) ), 201 );
	}

	// ---- homework / journal -------------------------------------------------

	public function list_homework( WP_REST_Request $request ): WP_REST_Response {
		$tid = (int) $request['id'];
		$uid = (int) $request['uid'];
		$out = array_values( array_filter( (array) get_user_meta( $uid, 'cb_homework', true ), function ( $h ) use ( $tid ) {
			return (int) ( $h['therapistId'] ?? 0 ) === $tid;
		} ) );
		return cb_response( $out );
	}

	public function assign_homework( WP_REST_Request $request ): WP_REST_Response {
		$tid  = (int) $request['id'];
		$uid  = (int) $request['uid'];
		$b    = $request->get_json_params();
		$list = array_values( (array) get_user_meta( $uid, 'cb_homework', true ) );
		$hw   = array(
			'id'            => count( $list ) + 1,
			'therapistId'   => $tid,
			'therapistName' => get_the_title( $tid ),
			'title'         => sanitize_text_field( (string) ( $b['title'] ?? '' ) ),
			'description'   => isset( $b['description'] ) ? sanitize_textarea_field( (string) $b['description'] ) : null,
			'status'        => 'PENDING',
			'dueDate'       => isset( $b['dueDate'] ) ? sanitize_text_field( (string) $b['dueDate'] ) : null,
			'completedAt'   => null,
			'createdAt'     => gmdate( 'c' ),
		);
		$list[] = $hw;
		update_user_meta( $uid, 'cb_homework', $list );
		return cb_response( $hw, 201 );
	}

	public function shared_journal( WP_REST_Request $request ): WP_REST_Response {
		$tid = (int) $request['id'];
		$uid = (int) $request['uid'];
		$out = array_values( array_filter( (array) get_user_meta( $uid, 'cb_journal', true ), function ( $j ) use ( $tid ) {
			return (int) ( $j['sharedWithTherapistId'] ?? 0 ) === $tid;
		} ) );
		return cb_response( $out );
	}

	// ---- match questions (option cb_match_questions) -----------------------

	public function list_match_questions(): WP_REST_Response {
		return cb_response( cb_match_questions() );
	}

	public function create_match_question( WP_REST_Request $request ): WP_REST_Response {
		$b    = $request->get_json_params();
		$list = cb_match_questions();
		$id   = (int) get_option( 'cb_match_seq', count( $list ) ) + 1;
		update_option( 'cb_match_seq', $id, false );
		$q = array(
			'id'           => $id,
			'questionText' => sanitize_text_field( (string) ( $b['questionText'] ?? '' ) ),
			'tag'          => sanitize_text_field( (string) ( $b['tag'] ?? '' ) ),
		);
		$list[] = $q;
		update_option( 'cb_match_questions', $list, false );
		return cb_response( $q, 201 );
	}

	public function delete_match_question( WP_REST_Request $request ): WP_REST_Response {
		$qid  = (int) $request['qid'];
		$list = array_values( array_filter( cb_match_questions(), function ( $q ) use ( $qid ) {
			return (int) $q['id'] !== $qid;
		} ) );
		update_option( 'cb_match_questions', $list, false );
		return cb_response( null, 204 );
	}

	// ---- clinic seats (option cb_clinic_seats) -----------------------------

	private function clinic_seats(): array {
		$v = get_option( 'cb_clinic_seats', array() );
		return is_array( $v ) ? $v : array();
	}

	public function list_clinic_seats( WP_REST_Request $request ): WP_REST_Response {
		$org = (int) $request['id'];
		return cb_response( array_values( array_filter( $this->clinic_seats(), function ( $s ) use ( $org ) {
			return (int) $s['organizationId'] === $org;
		} ) ) );
	}

	public function buy_clinic_seats( WP_REST_Request $request ): WP_REST_Response {
		$org   = (int) $request['id'];
		$b     = $request->get_json_params();
		$count = max( 1, (int) ( $b['count'] ?? 1 ) );
		$tid   = (int) ( $b['therapistId'] ?? 0 );
		$sess  = max( 1, (int) ( $b['sessionCount'] ?? 1 ) );
		$seats = $this->clinic_seats();
		$made  = array();
		for ( $i = 0; $i < $count; $i++ ) {
			$id = (int) get_option( 'cb_clinic_seat_seq', 0 ) + 1;
			update_option( 'cb_clinic_seat_seq', $id, false );
			$seat = array( 'id' => $id, 'organizationId' => $org, 'therapistId' => $tid, 'sessionCount' => $sess, 'assignedUserId' => null, 'assignedEmail' => null, 'assignedAt' => null );
			$seats[] = $seat;
			$made[]  = $seat;
		}
		update_option( 'cb_clinic_seats', $seats, false );
		return cb_response( $made, 201 );
	}

	public function assign_clinic_seat( WP_REST_Request $request ): WP_REST_Response {
		$org   = (int) $request['id'];
		$b     = $request->get_json_params();
		$tid   = (int) ( $b['therapistId'] ?? 0 );
		$email = sanitize_email( (string) ( $b['email'] ?? '' ) );
		$seats = $this->clinic_seats();
		foreach ( $seats as &$s ) {
			if ( (int) $s['organizationId'] === $org && (int) $s['therapistId'] === $tid && empty( $s['assignedEmail'] ) ) {
				$s['assignedEmail'] = $email;
				$s['assignedAt']    = gmdate( 'c' );
				$user               = get_user_by( 'email', $email );
				if ( $user ) {
					$s['assignedUserId'] = (int) $user->ID;
					// Grant session credits equal to the seat's sessionCount.
					$cur = max( 0, (int) get_user_meta( $user->ID, "cb_ther_credits_$tid", true ) );
					update_user_meta( $user->ID, "cb_ther_credits_$tid", $cur + (int) $s['sessionCount'] );
				}
				update_option( 'cb_clinic_seats', $seats, false );
				return cb_response( $s );
			}
		}
		return cb_error( 'صندلی خالی موجود نیست', 409, 'NO_FREE_SEAT', 'api/admin/organizations' );
	}
}
