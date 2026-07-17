<?php
/**
 * Clinic endpoints — therapists, appointment booking with an atomic slot lock,
 * session credits, receipts, mood/journal/homework, messaging, therapist match
 * and switch requests. Backed by cb_therapist/cb_appointment CPTs + the
 * wp_cb_bookings table (UNIQUE on therapist_id+slot_time) for atomic booking,
 * aligned with the Carmilla theme's meta keys.
 *
 * Slot ids are synthetic and stable: therapistId * 100000 + slotIndex, where
 * slotIndex is the position in the therapist's (future) cb_slots list.
 * Credits live in user meta cb_ther_credits_{therapistId} (theme-compatible).
 *
 * Public:  api/therapists, api/therapists/{slug}, api/clinic/therapist-match/questions
 * Auth:    everything else (booking, appointments, mood, journal, homework, messages…).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Clinic_Controller {

	const SLOT_BASE = 100000;
	const FREE_MESSAGES = 3;

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$pub   = '__return_true';
		$login = array( 'CB_Plugin', 'require_login' );

		register_rest_route( $ns, '/api/therapists', array( 'methods' => 'GET', 'callback' => array( $this, 'list_therapists' ), 'permission_callback' => $pub ) );
		register_rest_route( $ns, '/api/therapists/(?P<slug>[a-zA-Z0-9\-_%]+)', array( 'methods' => 'GET', 'callback' => array( $this, 'therapist_detail' ), 'permission_callback' => $pub ) );

		register_rest_route( $ns, '/api/clinic/my-appointments', array( 'methods' => 'GET', 'callback' => array( $this, 'my_appointments' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/clinic/appointments', array( 'methods' => 'POST', 'callback' => array( $this, 'book' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/clinic/appointments/(?P<id>\d+)/cancel', array( 'methods' => 'POST', 'callback' => array( $this, 'cancel' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/clinic/appointments/(?P<id>\d+)/receipt', array( 'methods' => 'GET', 'callback' => array( $this, 'receipt' ), 'permission_callback' => $login ) );

		register_rest_route( $ns, '/api/clinic/mood-checkins', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'mood_history' ), 'permission_callback' => $login ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'submit_mood' ), 'permission_callback' => $login ),
		) );
		register_rest_route( $ns, '/api/clinic/switch-requests', array( 'methods' => 'POST', 'callback' => array( $this, 'request_switch' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/clinic/switch-requests/mine', array( 'methods' => 'GET', 'callback' => array( $this, 'my_switches' ), 'permission_callback' => $login ) );

		register_rest_route( $ns, '/api/clinic/therapists/(?P<id>\d+)/messages', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'list_messages' ), 'permission_callback' => $login ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'send_message' ), 'permission_callback' => $login ),
		) );
		register_rest_route( $ns, '/api/clinic/therapists/(?P<id>\d+)/messaging-status', array( 'methods' => 'GET', 'callback' => array( $this, 'messaging_status' ), 'permission_callback' => $login ) );

		register_rest_route( $ns, '/api/clinic/homework', array( 'methods' => 'GET', 'callback' => array( $this, 'my_homework' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/clinic/homework/(?P<id>\d+)/complete', array( 'methods' => 'POST', 'callback' => array( $this, 'complete_homework' ), 'permission_callback' => $login ) );

		register_rest_route( $ns, '/api/clinic/journal', array(
			array( 'methods' => 'GET', 'callback' => array( $this, 'my_journal' ), 'permission_callback' => $login ),
			array( 'methods' => 'POST', 'callback' => array( $this, 'add_journal' ), 'permission_callback' => $login ),
		) );
		register_rest_route( $ns, '/api/clinic/journal/(?P<id>\d+)', array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_journal' ), 'permission_callback' => $login ) );

		register_rest_route( $ns, '/api/clinic/therapist-match/questions', array( 'methods' => 'GET', 'callback' => array( $this, 'match_questions' ), 'permission_callback' => $pub ) );
		register_rest_route( $ns, '/api/clinic/therapist-match/submit', array( 'methods' => 'POST', 'callback' => array( $this, 'submit_match' ), 'permission_callback' => $pub ) );
	}

	// ---- therapist model ----------------------------------------------------

	/** Future slots as [ index => 'YYYY-mm-ddTHH:MM', ... ] preserving stable indices. */
	private function all_slots( int $tid ): array {
		$raw   = (string) get_post_meta( $tid, 'cb_slots', true );
		$lines = array_values( array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $raw ) ) ) );
		$now   = time();
		$out   = array();
		foreach ( $lines as $i => $s ) {
			$ts = strtotime( $s );
			if ( $ts && $ts > $now ) {
				$out[ $i ] = $s;
			}
		}
		return $out;
	}

	private function booked_slot_times( int $tid ): array {
		global $wpdb;
		$table = cb_bookings_table();
		$rows  = $wpdb->get_col( $wpdb->prepare( "SELECT slot_time FROM $table WHERE therapist_id = %d", $tid ) );
		return is_array( $rows ) ? $rows : array();
	}

	private function credits( int $tid, int $uid ): int {
		return $uid ? max( 0, (int) get_user_meta( $uid, "cb_ther_credits_$tid", true ) ) : 0;
	}

	private function requires_purchase( int $tid ): bool {
		return (bool) get_post_meta( $tid, 'cb_product_slug', true );
	}

	private function session_minutes( int $tid ): int {
		$m = (int) get_post_meta( $tid, 'cb_duration', true );
		return $m > 0 ? $m : 45;
	}

	public static function slot_id( int $tid, int $index ): int {
		return $tid * self::SLOT_BASE + $index;
	}

	public static function decode_slot( int $slot_id ): array {
		return array( intdiv( $slot_id, self::SLOT_BASE ), $slot_id % self::SLOT_BASE );
	}

	private function slot_dtos( int $tid ): array {
		$booked  = $this->booked_slot_times( $tid );
		$minutes = $this->session_minutes( $tid );
		$out     = array();
		foreach ( $this->all_slots( $tid ) as $index => $s ) {
			if ( in_array( $s, $booked, true ) ) {
				continue;
			}
			$ts  = strtotime( $s );
			$out[] = array(
				'id'                => self::slot_id( $tid, $index ),
				'startTime'         => gmdate( 'c', $ts ),
				'endTime'           => gmdate( 'c', $ts + $minutes * 60 ),
				'dayLabel'          => gmdate( 'Y-m-d', $ts ),
				'timeLabel'         => gmdate( 'H:i', $ts ),
				'capacity'          => 1,
				'remainingCapacity' => 1,
			);
		}
		return $out;
	}

	private function therapist_summary( WP_Post $t, int $uid ): array {
		$id   = (int) $t->ID;
		$slug = get_post_meta( $id, 'cb_product_slug', true );
		return array(
			'id'                 => $id,
			'name'               => get_the_title( $t ),
			'slug'               => $t->post_name,
			'specialty'          => get_post_meta( $id, 'cb_specialty', true ) ?: null,
			'photoUrl'           => get_the_post_thumbnail_url( $t, 'medium' ) ?: null,
			'sessionPrice'       => (float) get_post_meta( $id, 'cb_session_price', true ),
			'availableSlotCount' => count( $this->slot_dtos( $id ) ),
			'requiresPurchase'   => (bool) $slug,
			'productSlug'        => $slug ?: null,
			'sessionCredits'     => $this->credits( $id, $uid ),
		);
	}

	// ---- endpoints: therapists ---------------------------------------------

	public function list_therapists(): WP_REST_Response {
		$uid   = get_current_user_id();
		$posts = get_posts( array( 'post_type' => 'cb_therapist', 'post_status' => 'publish', 'numberposts' => 100 ) );
		$out   = array();
		foreach ( $posts as $t ) {
			$out[] = $this->therapist_summary( $t, $uid );
		}
		return cb_response( $out );
	}

	public function therapist_detail( WP_REST_Request $request ): WP_REST_Response {
		$t = get_page_by_path( sanitize_title( $request['slug'] ), OBJECT, 'cb_therapist' );
		if ( ! $t ) {
			return cb_error( 'درمانگر یافت نشد', 404, 'NOT_FOUND', 'api/therapists' );
		}
		$id   = (int) $t->ID;
		$uid  = get_current_user_id();
		$slug = get_post_meta( $id, 'cb_product_slug', true );
		return cb_response( array(
			'id'                    => $id,
			'name'                  => get_the_title( $t ),
			'slug'                  => $t->post_name,
			'specialty'             => get_post_meta( $id, 'cb_specialty', true ) ?: null,
			'bio'                   => get_post_meta( $id, 'cb_approach', true ) ?: ( $t->post_content ?: null ),
			'photoUrl'              => get_the_post_thumbnail_url( $t, 'large' ) ?: null,
			'sessionPrice'          => (float) get_post_meta( $id, 'cb_session_price', true ),
			'sessionDurationMinutes' => $this->session_minutes( $id ),
			'slots'                 => $this->slot_dtos( $id ),
			'requiresPurchase'      => (bool) $slug,
			'productSlug'           => $slug ?: null,
			'sessionCredits'        => $this->credits( $id, $uid ),
			'mode'                  => get_post_meta( $id, 'cb_mode', true ) ?: 'ONLINE',
			'location'              => get_post_meta( $id, 'cb_location', true ) ?: null,
			'productId'             => $this->product_id_for_slug( $slug ),
		) );
	}

	private function product_id_for_slug( $slug ): ?int {
		if ( ! $slug ) {
			return null;
		}
		$p = get_page_by_path( $slug, OBJECT, 'product' );
		return $p ? (int) $p->ID : null;
	}

	// ---- endpoints: booking (atomic) ---------------------------------------

	public function book( WP_REST_Request $request ): WP_REST_Response {
		global $wpdb;
		$body    = $request->get_json_params();
		$slot_id = (int) ( $body['slotId'] ?? 0 );
		$notes   = isset( $body['notes'] ) ? sanitize_text_field( (string) $body['notes'] ) : null;
		list( $tid, $index ) = self::decode_slot( $slot_id );

		$therapist = get_post( $tid );
		if ( ! $therapist || $therapist->post_type !== 'cb_therapist' ) {
			return cb_error( 'درمانگر یافت نشد', 404, 'NOT_FOUND', 'api/clinic/appointments' );
		}
		$slots = $this->all_slots( $tid );
		if ( ! isset( $slots[ $index ] ) ) {
			return cb_error( 'این بازه معتبر نیست', 400, 'INVALID_SLOT', 'api/clinic/appointments' );
		}
		$slot_time = $slots[ $index ];
		$uid       = get_current_user_id();

		// Gate paid therapists on an available session credit.
		$paid = $this->requires_purchase( $tid );
		if ( $paid && $this->credits( $tid, $uid ) < 1 ) {
			return cb_error( 'اعتبار جلسه ندارید؛ ابتدا بسته‌ی مشاوره را بخرید', 402, 'NO_CREDIT', 'api/clinic/appointments' );
		}

		// Atomic lock: UNIQUE(therapist_id, slot_time) rejects a double booking.
		$table    = cb_bookings_table();
		$inserted = $wpdb->query( $wpdb->prepare(
			"INSERT IGNORE INTO $table (therapist_id, slot_time, user_id, created_at) VALUES (%d, %s, %d, %s)",
			$tid, $slot_time, $uid, current_time( 'mysql', true )
		) );
		if ( ! $inserted ) {
			return cb_error( 'این بازه هم‌اکنون رزرو شد', 409, 'SLOT_TAKEN', 'api/clinic/appointments' );
		}
		$booking_id = (int) $wpdb->insert_id;

		if ( $paid ) {
			$this->spend_credit( $tid, $uid );
		}

		$appt_id = wp_insert_post( array(
			'post_type'   => 'cb_appointment',
			'post_status' => 'publish',
			'post_title'  => 'نوبت ' . get_the_title( $therapist ) . ' — ' . $slot_time,
			'post_author' => $uid,
		) );
		update_post_meta( $appt_id, 'cb_therapist_id', $tid );
		update_post_meta( $appt_id, 'cb_user_id', $uid );
		update_post_meta( $appt_id, 'cb_slot', $slot_time );
		update_post_meta( $appt_id, 'cb_status', 'PENDING' );
		if ( $notes ) {
			update_post_meta( $appt_id, 'cb_notes', $notes );
		}
		$wpdb->update( $table, array( 'appointment_id' => $appt_id ), array( 'id' => $booking_id ) );

		return cb_response( $this->appointment_dto( get_post( $appt_id ) ), 201 );
	}

	private function spend_credit( int $tid, int $uid ): void {
		$cur = $this->credits( $tid, $uid );
		if ( $cur > 0 ) {
			update_user_meta( $uid, "cb_ther_credits_$tid", $cur - 1 );
		}
	}

	public function cancel( WP_REST_Request $request ): WP_REST_Response {
		global $wpdb;
		$appt = get_post( (int) $request['id'] );
		if ( ! $appt || $appt->post_type !== 'cb_appointment' || (int) get_post_meta( $appt->ID, 'cb_user_id', true ) !== get_current_user_id() ) {
			return cb_error( 'نوبت یافت نشد', 404, 'NOT_FOUND', 'api/clinic/appointments' );
		}
		if ( get_post_meta( $appt->ID, 'cb_status', true ) === 'CANCELLED' ) {
			return cb_response( null, 200 );
		}
		$tid = (int) get_post_meta( $appt->ID, 'cb_therapist_id', true );
		$uid = (int) get_post_meta( $appt->ID, 'cb_user_id', true );
		update_post_meta( $appt->ID, 'cb_status', 'CANCELLED' );
		// Free the slot lock and refund the session credit on a paid therapist.
		$wpdb->delete( cb_bookings_table(), array( 'appointment_id' => $appt->ID ) );
		if ( $this->requires_purchase( $tid ) ) {
			update_user_meta( $uid, "cb_ther_credits_$tid", $this->credits( $tid, $uid ) + 1 );
		}
		return cb_response( null, 200 );
	}

	public function my_appointments(): WP_REST_Response {
		$uid   = get_current_user_id();
		$posts = get_posts( array(
			'post_type'   => 'cb_appointment',
			'post_status' => 'publish',
			'numberposts' => 100,
			'meta_key'    => 'cb_user_id',
			'meta_value'  => $uid,
			'orderby'     => 'date',
			'order'       => 'DESC',
		) );
		$out = array();
		foreach ( $posts as $p ) {
			$out[] = $this->appointment_dto( $p );
		}
		return cb_response( $out );
	}

	private function appointment_dto( WP_Post $appt ): array {
		$tid    = (int) get_post_meta( $appt->ID, 'cb_therapist_id', true );
		$slot   = (string) get_post_meta( $appt->ID, 'cb_slot', true );
		$status = get_post_meta( $appt->ID, 'cb_status', true ) ?: 'PENDING';
		$video  = get_post_meta( $appt->ID, 'cb_video_url', true ) ?: null;
		$ts     = strtotime( $slot ) ?: time();
		return array(
			'id'               => (int) $appt->ID,
			'therapistName'    => get_the_title( $tid ),
			'therapistPhotoUrl' => get_the_post_thumbnail_url( $tid, 'medium' ) ?: null,
			'status'           => $status,
			'dayLabel'         => gmdate( 'Y-m-d', $ts ),
			'timeLabel'        => gmdate( 'H:i', $ts ),
			'videoRoomUrl'     => $video,
			'canJoin'          => $status === 'CONFIRMED' && ! empty( $video ),
			'notes'            => get_post_meta( $appt->ID, 'cb_notes', true ) ?: null,
			'mode'             => get_post_meta( $tid, 'cb_mode', true ) ?: 'ONLINE',
		);
	}

	public function receipt( WP_REST_Request $request ): WP_REST_Response {
		$appt = get_post( (int) $request['id'] );
		if ( ! $appt || $appt->post_type !== 'cb_appointment' || (int) get_post_meta( $appt->ID, 'cb_user_id', true ) !== get_current_user_id() ) {
			return cb_error( 'نوبت یافت نشد', 404, 'NOT_FOUND', 'api/clinic/appointments' );
		}
		$tid  = (int) get_post_meta( $appt->ID, 'cb_therapist_id', true );
		$slot = (string) get_post_meta( $appt->ID, 'cb_slot', true );
		$user = wp_get_current_user();
		return cb_response( array(
			'appointmentId'          => (int) $appt->ID,
			'patientName'            => $user->display_name,
			'therapistName'          => get_the_title( $tid ),
			'therapistSpecialty'     => get_post_meta( $tid, 'cb_specialty', true ) ?: null,
			'sessionMode'            => get_post_meta( $tid, 'cb_mode', true ) ?: 'ONLINE',
			'sessionDate'            => cb_iso( $slot ),
			'sessionDurationMinutes' => $this->session_minutes( $tid ),
			'amountPaid'             => (float) get_post_meta( $tid, 'cb_session_price', true ),
		) );
	}

	// ---- mood / journal / homework -----------------------------------------

	public function submit_mood( WP_REST_Request $request ): WP_REST_Response {
		$uid   = get_current_user_id();
		$body  = $request->get_json_params();
		$score = max( 1, min( 5, (int) ( $body['moodScore'] ?? 3 ) ) );
		$list  = $this->user_list( $uid, 'cb_moods' );
		$entry = array(
			'id'        => $this->next_seq( $uid, 'cb_moods_seq' ),
			'moodScore' => $score,
			'note'      => isset( $body['note'] ) ? sanitize_text_field( (string) $body['note'] ) : null,
			'createdAt' => gmdate( 'c' ),
		);
		array_unshift( $list, $entry );
		update_user_meta( $uid, 'cb_moods', $list );
		return cb_response( $entry, 201 );
	}

	public function mood_history(): WP_REST_Response {
		return cb_response( $this->user_list( get_current_user_id(), 'cb_moods' ) );
	}

	public function my_journal(): WP_REST_Response {
		return cb_response( $this->user_list( get_current_user_id(), 'cb_journal' ) );
	}

	public function add_journal( WP_REST_Request $request ): WP_REST_Response {
		$uid   = get_current_user_id();
		$body  = $request->get_json_params();
		$list  = $this->user_list( $uid, 'cb_journal' );
		$entry = array(
			'id'                   => $this->next_seq( $uid, 'cb_journal_seq' ),
			'content'              => sanitize_textarea_field( (string) ( $body['content'] ?? '' ) ),
			'sharedWithTherapistId' => isset( $body['sharedWithTherapistId'] ) ? (int) $body['sharedWithTherapistId'] : null,
			'createdAt'            => gmdate( 'c' ),
		);
		array_unshift( $list, $entry );
		update_user_meta( $uid, 'cb_journal', $list );
		return cb_response( $entry, 201 );
	}

	public function delete_journal( WP_REST_Request $request ): WP_REST_Response {
		$uid  = get_current_user_id();
		$id   = (int) $request['id'];
		$list = array_values( array_filter( $this->user_list( $uid, 'cb_journal' ), function ( $e ) use ( $id ) {
			return (int) $e['id'] !== $id;
		} ) );
		update_user_meta( $uid, 'cb_journal', $list );
		return cb_response( null, 204 );
	}

	public function my_homework(): WP_REST_Response {
		return cb_response( $this->user_list( get_current_user_id(), 'cb_homework' ) );
	}

	public function complete_homework( WP_REST_Request $request ): WP_REST_Response {
		$uid  = get_current_user_id();
		$id   = (int) $request['id'];
		$list = $this->user_list( $uid, 'cb_homework' );
		$found = null;
		foreach ( $list as &$h ) {
			if ( (int) $h['id'] === $id ) {
				$h['status']      = 'COMPLETED';
				$h['completedAt'] = gmdate( 'c' );
				$found            = $h;
			}
		}
		unset( $h );
		if ( ! $found ) {
			return cb_error( 'تمرین یافت نشد', 404, 'NOT_FOUND', 'api/clinic/homework' );
		}
		update_user_meta( $uid, 'cb_homework', $list );
		return cb_response( $found );
	}

	// ---- messaging ----------------------------------------------------------

	public function list_messages( WP_REST_Request $request ): WP_REST_Response {
		$tid = (int) $request['id'];
		$uid = get_current_user_id();
		$comments = get_comments( array(
			'post_id'    => $tid,
			'type'       => 'cb_msg',
			'meta_key'   => 'cb_user_id',
			'meta_value' => $uid,
			'status'     => 'approve',
			'orderby'    => 'comment_date_gmt',
			'order'      => 'ASC',
		) );
		$out = array();
		foreach ( $comments as $c ) {
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
		$uid  = get_current_user_id();
		$body = sanitize_textarea_field( (string) ( $request->get_json_params()['body'] ?? '' ) );
		if ( $body === '' ) {
			return cb_error( 'متن پیام خالی است', 400, 'EMPTY', 'api/clinic/therapists' );
		}
		$user = wp_get_current_user();
		$cid  = wp_insert_comment( array(
			'comment_post_ID'  => $tid,
			'comment_type'     => 'cb_msg',
			'comment_content'  => $body,
			'user_id'          => $uid,
			'comment_author'   => $user->display_name,
			'comment_approved' => 1,
		) );
		add_comment_meta( $cid, 'cb_user_id', $uid );
		return cb_response( array(
			'id'         => (int) $cid,
			'senderType' => 'PATIENT',
			'body'       => $body,
			'createdAt'  => gmdate( 'c' ),
		), 201 );
	}

	public function messaging_status( WP_REST_Request $request ): WP_REST_Response {
		$tid  = (int) $request['id'];
		$uid  = get_current_user_id();
		$sent = (int) get_comments( array( 'post_id' => $tid, 'type' => 'cb_msg', 'meta_key' => 'cb_user_id', 'meta_value' => $uid, 'count' => true ) );
		$has_plan = (bool) get_post_meta( $tid, 'cb_messaging_product_slug', true ) && $this->credits( $tid, $uid ) > 0;
		return cb_response( array(
			'therapistId'          => $tid,
			'active'               => $has_plan || $sent < self::FREE_MESSAGES,
			'freeMessagesRemaining' => max( 0, self::FREE_MESSAGES - $sent ),
		) );
	}

	// ---- switch requests ----------------------------------------------------

	public function request_switch( WP_REST_Request $request ): WP_REST_Response {
		$uid  = get_current_user_id();
		$body = $request->get_json_params();
		$from = (int) ( $body['fromTherapistId'] ?? 0 );
		$to   = isset( $body['toTherapistId'] ) ? (int) $body['toTherapistId'] : null;
		$list = $this->user_list( $uid, 'cb_switch_requests' );
		$entry = array(
			'id'               => $this->next_seq( $uid, 'cb_switch_seq' ),
			'fromTherapistId'  => $from,
			'fromTherapistName' => get_the_title( $from ),
			'toTherapistId'    => $to,
			'toTherapistName'  => $to ? get_the_title( $to ) : null,
			'reason'           => isset( $body['reason'] ) ? sanitize_text_field( (string) $body['reason'] ) : null,
			'status'           => 'PENDING',
			'adminNote'        => null,
			'createdAt'        => gmdate( 'c' ),
		);
		array_unshift( $list, $entry );
		update_user_meta( $uid, 'cb_switch_requests', $list );
		return cb_response( $entry, 201 );
	}

	public function my_switches(): WP_REST_Response {
		return cb_response( $this->user_list( get_current_user_id(), 'cb_switch_requests' ) );
	}

	// ---- therapist match ----------------------------------------------------

	public function match_questions(): WP_REST_Response {
		return cb_response( cb_match_questions() );
	}

	public function submit_match( WP_REST_Request $request ): WP_REST_Response {
		$tags  = (array) ( $request->get_json_params()['selectedTags'] ?? array() );
		$uid   = get_current_user_id();
		$posts = get_posts( array( 'post_type' => 'cb_therapist', 'post_status' => 'publish', 'numberposts' => 100 ) );
		$scored = array();
		foreach ( $posts as $t ) {
			$hay   = get_post_meta( $t->ID, 'cb_specialty', true ) . ' ' . get_post_meta( $t->ID, 'cb_approach', true );
			$score = self::match_score( $hay, $tags );
			if ( $score > 0 ) {
				$scored[] = array( 'therapist' => $this->therapist_summary( $t, $uid ), 'matchScore' => $score );
			}
		}
		usort( $scored, function ( $a, $b ) {
			return $b['matchScore'] <=> $a['matchScore'];
		} );
		return cb_response( array_values( $scored ) );
	}

	/** Number of selected tags found (case-insensitive) in the therapist's haystack. */
	public static function match_score( string $haystack, array $tags ): int {
		$score = 0;
		foreach ( $tags as $tag ) {
			$tag = trim( (string) $tag );
			if ( $tag !== '' && mb_stripos( $haystack, $tag ) !== false ) {
				$score++;
			}
		}
		return $score;
	}

	// ---- small user-list helpers -------------------------------------------

	private function user_list( int $uid, string $key ): array {
		$v = get_user_meta( $uid, $key, true );
		return is_array( $v ) ? array_values( $v ) : array();
	}

	private function next_seq( int $uid, string $key ): int {
		$n = (int) get_user_meta( $uid, $key, true ) + 1;
		update_user_meta( $uid, $key, $n );
		return $n;
	}
}
