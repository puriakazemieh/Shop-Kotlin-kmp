<?php
namespace Carmilla\Core\Clinic;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\AppointmentBookingService', false ) ) {

/**
 * Canonical Shared Appointment Booking, Cancellation & Management Service.
 *
 * Centralizes atomic slot locking via `{prefix}cb_bookings` (UNIQUE on
 * therapist_id + slot_time), cb_appointment CPT lifecycle, ownership checks,
 * receipts, and admin confirmation/completion across Theme-only, Plugin-only,
 * and Co-install modes.
 *
 * Independent of PsychTest (psych.tests) and private clinical files (P14).
 *
 * Part of Task P04-WORDPRESS-CODE-028A.
 */
class AppointmentBookingService {

	private static bool $table_ensured = false;

	public static function is_enabled(): bool {
		return TherapistCatalogService::is_enabled();
	}

	public static function can_manage_appointments(): bool {
		return TherapistCatalogService::can_manage_therapists();
	}

	public static function bookings_table(): string {
		global $wpdb;
		return $wpdb->prefix . 'cb_bookings';
	}

	/**
	 * Ensures the atomic slot lock table exists (idempotent; safe in Theme-only mode).
	 */
	public static function ensure_bookings_table(): void {
		if ( self::$table_ensured ) {
			return;
		}
		global $wpdb;
		if ( ! $wpdb ) {
			return;
		}
		$table   = self::bookings_table();
		$charset = method_exists( $wpdb, 'get_charset_collate' ) ? $wpdb->get_charset_collate() : '';
		$sql     = "CREATE TABLE IF NOT EXISTS $table (
			id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
			therapist_id BIGINT UNSIGNED NOT NULL,
			slot_time VARCHAR(40) NOT NULL,
			appointment_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
			user_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
			created_at DATETIME NOT NULL,
			PRIMARY KEY  (id),
			UNIQUE KEY uniq_slot (therapist_id, slot_time)
		) $charset;";
		if ( defined( 'ABSPATH' ) && file_exists( ABSPATH . 'wp-admin/includes/upgrade.php' ) ) {
			require_once ABSPATH . 'wp-admin/includes/upgrade.php';
		}
		if ( function_exists( 'dbDelta' ) ) {
			dbDelta( $sql );
		} else {
			$wpdb->query( $sql );
		}
		self::$table_ensured = true;
	}

	/**
	 * Books an appointment atomically using InnoDB transaction + UNIQUE slot lock.
	 * Accepts either canonical `{ slotId, notes }` or legacy `{ therapistId, slot, notes }`.
	 *
	 * @param int                  $user_id
	 * @param array<string, mixed> $params
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function book( int $user_id, array $params ) {
		if ( ! self::is_enabled() ) {
			return new \WP_Error( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', [ 'status' => 403 ] );
		}
		if ( $user_id < 1 ) {
			return new \WP_Error( 'UNAUTHORIZED', 'ابتدا وارد شوید', [ 'status' => 401 ] );
		}

		$notes   = isset( $params['notes'] ) ? sanitize_text_field( (string) $params['notes'] ) : null;
		$slot_id = isset( $params['slotId'] ) ? (int) $params['slotId'] : 0;

		if ( $slot_id > 0 ) {
			list( $tid, $index ) = TherapistCatalogService::decode_slot( $slot_id );
		} else {
			$tid        = isset( $params['therapistId'] ) ? (int) $params['therapistId'] : 0;
			$slot_input = isset( $params['slot'] ) ? trim( (string) $params['slot'] ) : '';
			$index      = -1;
			if ( $tid > 0 && '' !== $slot_input ) {
				foreach ( TherapistCatalogService::future_slots( $tid ) as $i => $s ) {
					if ( $s === $slot_input ) {
						$index = (int) $i;
						break;
					}
				}
			}
		}

		$therapist = $tid > 0 ? get_post( $tid ) : null;
		if ( ! $therapist || 'cb_therapist' !== $therapist->post_type ) {
			return new \WP_Error( 'NOT_FOUND', 'درمانگر یافت نشد', [ 'status' => 404 ] );
		}

		$slots = TherapistCatalogService::future_slots( $tid );
		if ( $index < 0 || ! isset( $slots[ $index ] ) ) {
			return new \WP_Error( 'INVALID_SLOT', 'این بازه معتبر نیست', [ 'status' => 400 ] );
		}
		$slot_time = $slots[ $index ];

		global $wpdb;
		$table = self::bookings_table();
		$wpdb->last_error = '';
		$engines = $wpdb->get_results( $wpdb->prepare(
			'SELECT TABLE_NAME,ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN (%s,%s,%s)',
			$wpdb->posts, $wpdb->postmeta, $table
		), ARRAY_A );
		if ( $wpdb->last_error !== '' || ! is_array( $engines ) || count( $engines ) !== 3 ) {
			return new \WP_Error( 'BOOKING_UNAVAILABLE', 'رزرو در حال حاضر در دسترس نیست', [ 'status' => 503 ] );
		}
		foreach ( $engines as $engine ) {
			if ( strcasecmp( (string) $engine['ENGINE'], 'InnoDB' ) !== 0 ) {
				return new \WP_Error( 'BOOKING_UNAVAILABLE', 'رزرو در حال حاضر در دسترس نیست', [ 'status' => 503 ] );
			}
		}

		if ( $wpdb->query( 'START TRANSACTION' ) === false ) {
			return new \WP_Error( 'BOOKING_UNAVAILABLE', 'رزرو در حال حاضر در دسترس نیست', [ 'status' => 503 ] );
		}

		$appt_id = 0;
		try {
			$wpdb->last_error = '';
			$inserted = $wpdb->query( $wpdb->prepare(
				"INSERT IGNORE INTO $table (therapist_id, slot_time, user_id, created_at) VALUES (%d, %s, %d, %s)",
				$tid, $slot_time, $user_id, current_time( 'mysql', true )
			) );
			if ( $inserted === 0 && $wpdb->last_error === '' ) {
				$wpdb->query( 'ROLLBACK' );
				return new \WP_Error( 'SLOT_TAKEN', 'این بازه هم‌اکنون رزرو شد', [ 'status' => 409 ] );
			}
			if ( $inserted !== 1 || $wpdb->last_error !== '' ) {
				throw new \RuntimeException( 'slot_lock' );
			}
			$booking_id = (int) $wpdb->insert_id;
			if ( $booking_id < 1 ) {
				throw new \RuntimeException( 'slot_lock_id' );
			}

			$appt_id = wp_insert_post( [
				'post_type'   => 'cb_appointment',
				'post_status' => 'publish',
				'post_title'  => 'نوبت ' . get_the_title( $therapist ) . ' — ' . $slot_time,
				'post_author' => $user_id,
			], true );
			if ( is_wp_error( $appt_id ) || ! is_int( $appt_id ) || $appt_id < 1 ) {
				throw new \RuntimeException( 'appointment_post' );
			}
			foreach ( [ 'cb_therapist_id' => $tid, 'cb_user_id' => $user_id, 'cb_slot' => $slot_time, 'cb_status' => 'PENDING' ] as $key => $value ) {
				if ( update_post_meta( $appt_id, $key, $value ) === false ) {
					throw new \RuntimeException( 'appointment_meta' );
				}
			}
			if ( $notes && update_post_meta( $appt_id, 'cb_notes', $notes ) === false ) {
				throw new \RuntimeException( 'appointment_notes' );
			}
			if ( $wpdb->update( $table, [ 'appointment_id' => $appt_id ], [ 'id' => $booking_id ], [ '%d' ], [ '%d' ] ) !== 1 ) {
				throw new \RuntimeException( 'slot_link' );
			}
			$linked = $wpdb->get_var( $wpdb->prepare(
				"SELECT appointment_id FROM $table WHERE id=%d AND therapist_id=%d AND user_id=%d",
				$booking_id, $tid, $user_id
			) );
			if ( $wpdb->last_error !== '' || (int) $linked !== $appt_id ) {
				throw new \RuntimeException( 'slot_link_check' );
			}
			$appointment = get_post( $appt_id );
			if ( ! $appointment instanceof \WP_Post ) {
				throw new \RuntimeException( 'appointment_read' );
			}
			$dto = self::appointment_dto( $appointment );
			if ( $wpdb->query( 'COMMIT' ) === false ) {
				throw new \RuntimeException( 'booking_commit' );
			}
			return $dto;
		} catch ( \Throwable $error ) {
			$wpdb->query( 'ROLLBACK' );
			if ( is_int( $appt_id ) && $appt_id > 0 ) {
				clean_post_cache( $appt_id );
				wp_cache_delete( $appt_id, 'post_meta' );
			}
			return new \WP_Error( 'BOOKING_UNAVAILABLE', 'رزرو در حال حاضر در دسترس نیست', [ 'status' => 503 ] );
		}
	}

	/**
	 * Cancels an appointment atomically, preserving status and releasing slot lock.
	 *
	 * @return true|\WP_Error
	 */
	public static function cancel( int $appointment_id, int $user_id ) {
		if ( ! self::is_enabled() ) {
			return new \WP_Error( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', [ 'status' => 403 ] );
		}

		$appt = get_post( $appointment_id );
		if ( ! $appt || 'cb_appointment' !== $appt->post_type || (int) get_post_meta( $appt->ID, 'cb_user_id', true ) !== $user_id ) {
			return new \WP_Error( 'NOT_FOUND', 'نوبت یافت نشد', [ 'status' => 404 ] );
		}

		$tid   = (int) get_post_meta( $appt->ID, 'cb_therapist_id', true );
		$slot  = (string) get_post_meta( $appt->ID, 'cb_slot', true );
		$table = self::bookings_table();
		if ( $tid < 1 || '' === $slot ) {
			return new \WP_Error( 'CANCELLATION_UNAVAILABLE', 'لغو نوبت در دسترس نیست', [ 'status' => 503 ] );
		}

		global $wpdb;
		$wpdb->last_error = '';
		$engines = $wpdb->get_results( $wpdb->prepare(
			'SELECT TABLE_NAME,ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN (%s,%s)',
			$wpdb->postmeta, $table
		), ARRAY_A );
		if ( $wpdb->last_error !== '' || ! is_array( $engines ) || count( $engines ) !== 2 ) {
			return new \WP_Error( 'CANCELLATION_UNAVAILABLE', 'لغو نوبت در دسترس نیست', [ 'status' => 503 ] );
		}
		foreach ( $engines as $engine ) {
			if ( strcasecmp( (string) $engine['ENGINE'], 'InnoDB' ) !== 0 ) {
				return new \WP_Error( 'CANCELLATION_UNAVAILABLE', 'لغو نوبت در دسترس نیست', [ 'status' => 503 ] );
			}
		}

		if ( $wpdb->query( 'START TRANSACTION' ) === false ) {
			return new \WP_Error( 'CANCELLATION_UNAVAILABLE', 'لغو نوبت در دسترس نیست', [ 'status' => 503 ] );
		}

		try {
			$wpdb->last_error = '';
			$lock = $wpdb->get_row( $wpdb->prepare(
				"SELECT id,appointment_id FROM $table WHERE therapist_id=%d AND slot_time=%s FOR UPDATE",
				$tid, $slot
			), ARRAY_A );
			if ( $wpdb->last_error !== '' || ( $lock && (int) $lock['appointment_id'] !== $appt->ID ) ) {
				throw new \RuntimeException( 'slot_lock' );
			}
			$wpdb->last_error = '';
			$status = $wpdb->get_var( $wpdb->prepare(
				"SELECT meta_value FROM {$wpdb->postmeta} WHERE post_id=%d AND meta_key='cb_status' ORDER BY meta_id DESC LIMIT 1 FOR UPDATE",
				$appt->ID
			) );
			if ( $wpdb->last_error !== '' || ! is_string( $status ) || ( ! $lock && $status !== 'CANCELLED' ) ) {
				throw new \RuntimeException( 'appointment_status' );
			}
			if ( $status !== 'CANCELLED' && update_post_meta( $appt->ID, 'cb_status', 'CANCELLED' ) === false ) {
				throw new \RuntimeException( 'status_update' );
			}
			if ( $lock && $wpdb->delete( $table, [ 'id' => (int) $lock['id'] ], [ '%d' ] ) !== 1 ) {
				throw new \RuntimeException( 'slot_release' );
			}
			if ( $wpdb->query( 'COMMIT' ) === false ) {
				throw new \RuntimeException( 'cancel_commit' );
			}
			return true;
		} catch ( \Throwable $error ) {
			$wpdb->query( 'ROLLBACK' );
			wp_cache_delete( $appt->ID, 'post_meta' );
			return new \WP_Error( 'CANCELLATION_UNAVAILABLE', 'لغو نوبت در دسترس نیست', [ 'status' => 503 ] );
		}
	}

	/**
	 * Lists appointments owned by the given user.
	 *
	 * @return array<int, array<string, mixed>>
	 */
	public static function my_appointments( int $user_id ): array {
		if ( $user_id < 1 ) {
			return [];
		}
		$posts = get_posts( [
			'post_type'   => 'cb_appointment',
			'post_status' => 'publish',
			'numberposts' => 100,
			'meta_key'    => 'cb_user_id',
			'meta_value'  => $user_id,
			'orderby'     => 'date',
			'order'       => 'DESC',
		] );
		$out = [];
		foreach ( $posts as $p ) {
			if ( $p instanceof \WP_Post ) {
				$out[] = self::appointment_dto( $p );
			}
		}
		return $out;
	}

	/**
	 * Returns receipt for an appointment owned by the given user.
	 *
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function receipt( int $appointment_id, int $user_id ) {
		if ( ! self::is_enabled() ) {
			return new \WP_Error( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', [ 'status' => 403 ] );
		}
		$appt = get_post( $appointment_id );
		if ( ! $appt || 'cb_appointment' !== $appt->post_type || (int) get_post_meta( $appt->ID, 'cb_user_id', true ) !== $user_id ) {
			return new \WP_Error( 'NOT_FOUND', 'نوبت یافت نشد', [ 'status' => 404 ] );
		}
		$tid    = (int) get_post_meta( $appt->ID, 'cb_therapist_id', true );
		$slot   = (string) get_post_meta( $appt->ID, 'cb_slot', true );
		$status = (string) ( get_post_meta( $appt->ID, 'cb_status', true ) ?: 'PENDING' );
		$user   = get_userdata( $user_id );
		$tz     = TherapistCatalogService::resolve_timezone( $tid );
		$dt     = TherapistCatalogService::parse_slot_datetime( $slot, $tz );

		return [
			'appointmentId'          => (int) $appt->ID,
			'patientName'            => $user ? $user->display_name : '',
			'therapistName'          => get_the_title( $tid ),
			'therapistSpecialty'     => get_post_meta( $tid, 'cb_specialty', true ) ?: null,
			'specialty'              => get_post_meta( $tid, 'cb_specialty', true ) ?: null,
			'sessionMode'            => get_post_meta( $tid, 'cb_mode', true ) ?: 'ONLINE',
			'sessionDate'            => $dt ? $dt->format( 'c' ) : gmdate( 'c', strtotime( $slot ) ?: time() ),
			'slot'                   => $slot,
			'status'                 => $status,
			'sessionDurationMinutes' => TherapistCatalogService::session_minutes( $tid ),
			'amountPaid'             => (float) get_post_meta( $tid, 'cb_session_price', true ),
		];
	}

	/**
	 * Lists all appointments for admin management.
	 *
	 * @return array<int, array<string, mixed>>
	 */
	public static function admin_list_appointments(): array {
		$posts = get_posts( [
			'post_type'   => 'cb_appointment',
			'post_status' => 'publish',
			'numberposts' => 300,
			'orderby'     => 'date',
			'order'       => 'DESC',
		] );
		$out = [];
		foreach ( $posts as $p ) {
			if ( ! $p instanceof \WP_Post ) {
				continue;
			}
			$dto           = self::appointment_dto( $p );
			$dto['userId'] = (int) get_post_meta( $p->ID, 'cb_user_id', true );
			$out[]         = $dto;
		}
		return $out;
	}

	/**
	 * Admin confirms an appointment (optional videoRoomUrl).
	 *
	 * @param array<string, mixed> $params
	 * @return true|\WP_Error
	 */
	public static function admin_confirm_appointment( int $appointment_id, array $params = [] ) {
		if ( get_post_type( $appointment_id ) !== 'cb_appointment' ) {
			return new \WP_Error( 'NOT_FOUND', 'نوبت یافت نشد', [ 'status' => 404 ] );
		}
		$url = esc_url_raw( (string) ( $params['videoRoomUrl'] ?? '' ) );
		update_post_meta( $appointment_id, 'cb_video_url', $url );
		update_post_meta( $appointment_id, 'cb_status', 'CONFIRMED' );
		return true;
	}

	/**
	 * Admin marks an appointment completed.
	 *
	 * @return true|\WP_Error
	 */
	public static function admin_complete_appointment( int $appointment_id ) {
		if ( get_post_type( $appointment_id ) !== 'cb_appointment' ) {
			return new \WP_Error( 'NOT_FOUND', 'نوبت یافت نشد', [ 'status' => 404 ] );
		}
		update_post_meta( $appointment_id, 'cb_status', 'COMPLETED' );
		return true;
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function appointment_dto( \WP_Post $appt ): array {
		$tid    = (int) get_post_meta( $appt->ID, 'cb_therapist_id', true );
		$slot   = (string) get_post_meta( $appt->ID, 'cb_slot', true );
		$status = (string) ( get_post_meta( $appt->ID, 'cb_status', true ) ?: 'PENDING' );
		$video  = get_post_meta( $appt->ID, 'cb_video_url', true ) ?: null;
		$tz     = TherapistCatalogService::resolve_timezone( $tid );
		$dt     = TherapistCatalogService::parse_slot_datetime( $slot, $tz );
		$ts     = $dt ? $dt->getTimestamp() : ( strtotime( $slot ) ?: time() );

		return [
			'id'                => (int) $appt->ID,
			'therapistId'       => $tid,
			'therapistName'     => get_the_title( $tid ),
			'therapistPhotoUrl' => get_the_post_thumbnail_url( $tid, 'medium' ) ?: null,
			'status'            => $status,
			'slot'              => $slot,
			'dayLabel'          => $dt ? $dt->format( 'Y-m-d' ) : gmdate( 'Y-m-d', $ts ),
			'timeLabel'         => $dt ? $dt->format( 'H:i' ) : gmdate( 'H:i', $ts ),
			'videoRoomUrl'      => $video,
			'canJoin'           => 'CONFIRMED' === $status && ! empty( $video ),
			'notes'             => get_post_meta( $appt->ID, 'cb_notes', true ) ?: null,
			'mode'              => get_post_meta( $tid, 'cb_mode', true ) ?: 'ONLINE',
		];
	}
}

}
