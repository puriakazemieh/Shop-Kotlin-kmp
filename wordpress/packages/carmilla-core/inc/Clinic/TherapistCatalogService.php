<?php
namespace Carmilla\Core\Clinic;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\TherapistCatalogService', false ) ) {

/**
 * Canonical Shared Therapist Catalog & Availability Service.
 *
 * Centralizes cb_therapist CPT, cb_slots parsing/generation with explicit
 * timezone validation, synthetic slot IDs (therapistId * 100000 + index),
 * and capability checks across Theme-only, Plugin-only, and Co-install modes.
 *
 * Independent of PsychTest (psych.tests) and private clinical files (P14).
 * Booking/cancel/conflict belong to P04-WORDPRESS-CODE-028A.
 *
 * Part of Task P04-WORDPRESS-CODE-028.
 */
class TherapistCatalogService {

	public const SLOT_BASE = 100000;

	public static function is_enabled(): bool {
		if ( class_exists( '\Carmilla_Kernel' ) && \Carmilla_Kernel::is_booted() ) {
			return \Carmilla_Kernel::is_feature_active( 'clinic.booking' );
		}
		return true;
	}

	public static function can_manage_therapists(): bool {
		return current_user_can( 'manage_options' )
			|| current_user_can( 'carmilla_manage_appointments' )
			|| current_user_can( 'carmilla_edit_appointments' )
			|| current_user_can( 'edit_others_posts' );
	}

	public static function slot_id( int $therapist_id, int $index ): int {
		return $therapist_id * self::SLOT_BASE + $index;
	}

	/**
	 * @return array{0:int,1:int}
	 */
	public static function decode_slot( int $slot_id ): array {
		return [ intdiv( $slot_id, self::SLOT_BASE ), $slot_id % self::SLOT_BASE ];
	}

	/**
	 * Resolves a valid DateTimeZone for a therapist (cb_timezone meta -> wp_timezone -> UTC).
	 */
	public static function resolve_timezone( int $therapist_id, ?string $override_tz = null ): \DateTimeZone {
		$tz_str = $override_tz ?: (string) get_post_meta( $therapist_id, 'cb_timezone', true );
		if ( '' !== $tz_str ) {
			try {
				return new \DateTimeZone( $tz_str );
			} catch ( \Throwable $e ) {
				// Fall through to site timezone.
			}
		}
		if ( function_exists( 'wp_timezone' ) ) {
			return wp_timezone();
		}
		return new \DateTimeZone( 'UTC' );
	}

	/**
	 * Validates a timezone identifier string.
	 */
	public static function is_valid_timezone( string $tz_str ): bool {
		if ( '' === trim( $tz_str ) ) {
			return false;
		}
		try {
			new \DateTimeZone( $tz_str );
			return true;
		} catch ( \Throwable $e ) {
			return false;
		}
	}

	/**
	 * Parses a slot datetime string in the given timezone. Returns null if invalid.
	 */
	public static function parse_slot_datetime( string $raw, \DateTimeZone $tz ): ?\DateTimeImmutable {
		$raw = trim( $raw );
		if ( '' === $raw ) {
			return null;
		}
		try {
			$dt = new \DateTimeImmutable( $raw, $tz );
			return $dt;
		} catch ( \Throwable $e ) {
			return null;
		}
	}

	/**
	 * Future slots as [ index => raw_string ] preserving stable indices.
	 *
	 * @return array<int, string>
	 */
	public static function future_slots( int $therapist_id ): array {
		$raw   = (string) get_post_meta( $therapist_id, 'cb_slots', true );
		$lines = array_values( array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $raw ) ?: [] ) ) );
		$tz    = self::resolve_timezone( $therapist_id );
		$now   = time();
		$out   = [];

		foreach ( $lines as $i => $s ) {
			$dt = self::parse_slot_datetime( $s, $tz );
			if ( $dt && $dt->getTimestamp() > $now ) {
				$out[ $i ] = $s;
			}
		}
		return $out;
	}

	/**
	 * @return array<int, string>
	 */
	public static function booked_slot_times( int $therapist_id ): array {
		global $wpdb;
		if ( ! isset( $wpdb ) || ! is_object( $wpdb ) ) {
			return [];
		}
		$table = $wpdb->prefix . 'cb_bookings';
		if ( function_exists( 'cb_bookings_table' ) ) {
			$table = cb_bookings_table();
		}
		$exists = $wpdb->get_var( $wpdb->prepare( 'SHOW TABLES LIKE %s', $table ) );
		if ( $exists !== $table ) {
			return [];
		}
		$rows = $wpdb->get_col( $wpdb->prepare( "SELECT slot_time FROM {$table} WHERE therapist_id = %d", $therapist_id ) );
		return is_array( $rows ) ? $rows : [];
	}

	public static function session_minutes( int $therapist_id ): int {
		$m = (int) get_post_meta( $therapist_id, 'cb_duration', true );
		return $m > 0 ? $m : 45;
	}

	/**
	 * @return array<int, array<string, mixed>>
	 */
	public static function slot_dtos( int $therapist_id ): array {
		$booked  = self::booked_slot_times( $therapist_id );
		$minutes = self::session_minutes( $therapist_id );
		$tz      = self::resolve_timezone( $therapist_id );
		$utc     = new \DateTimeZone( 'UTC' );
		$out     = [];

		foreach ( self::future_slots( $therapist_id ) as $index => $s ) {
			if ( in_array( $s, $booked, true ) ) {
				continue;
			}
			$dt = self::parse_slot_datetime( $s, $tz );
			if ( ! $dt ) {
				continue;
			}
			$dt_local = $dt->setTimezone( $tz );
			$dt_utc   = $dt->setTimezone( $utc );
			$end_utc  = $dt_utc->modify( "+{$minutes} minutes" );

			$out[] = [
				'id'                => self::slot_id( $therapist_id, $index ),
				'startTime'         => $dt_utc->format( 'c' ),
				'endTime'           => $end_utc->format( 'c' ),
				'dayLabel'          => $dt_local->format( 'Y-m-d' ),
				'timeLabel'         => $dt_local->format( 'H:i' ),
				'timezone'          => $tz->getName(),
				'capacity'          => 1,
				'remainingCapacity' => 1,
			];
		}

		return $out;
	}

	/**
	 * @return array<int, array<string, mixed>>
	 */
	public static function list_therapists( bool $include_drafts = false ): array {
		$posts = get_posts( [
			'post_type'   => 'cb_therapist',
			'post_status' => $include_drafts ? [ 'publish', 'draft', 'pending', 'private' ] : [ 'publish' ],
			'numberposts' => 200,
			'orderby'     => 'date',
			'order'       => 'DESC',
		] );

		$out = [];
		foreach ( $posts as $t ) {
			if ( $t instanceof \WP_Post ) {
				$out[] = self::summary_dto( $t );
			}
		}
		return $out;
	}

	/**
	 * @param int|string $id_or_slug
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function get_therapist_detail( $id_or_slug, array $statuses = [ 'publish' ] ) {
		$t = null;
		if ( is_numeric( $id_or_slug ) ) {
			$candidate = get_post( (int) $id_or_slug );
			if ( $candidate instanceof \WP_Post && 'cb_therapist' === $candidate->post_type && in_array( $candidate->post_status, $statuses, true ) ) {
				$t = $candidate;
			}
		} else {
			$slug  = rawurldecode( (string) $id_or_slug );
			$query = new \WP_Query( [
				'post_type'      => 'cb_therapist',
				'name'           => $slug,
				'post_status'    => $statuses,
				'posts_per_page' => 1,
			] );
			$t = $query->have_posts() ? $query->posts[0] : null;
		}

		if ( ! $t ) {
			return new \WP_Error( 'NOT_FOUND', 'درمانگر یافت نشد', [ 'status' => 404 ] );
		}

		return self::detail_dto( $t );
	}

	/**
	 * @param array<string, mixed> $data
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function create_therapist( array $data ) {
		$name = trim( (string) ( $data['name'] ?? ( $data['title'] ?? '' ) ) );
		if ( '' === $name ) {
			return new \WP_Error( 'VALIDATION', 'نام متخصص الزامی است', [ 'status' => 400 ] );
		}

		if ( ! empty( $data['timezone'] ) && ! self::is_valid_timezone( (string) $data['timezone'] ) ) {
			return new \WP_Error( 'INVALID_TIMEZONE', 'منطقه زمانی نامعتبر است', [ 'status' => 400 ] );
		}

		$insert = [
			'post_type'    => 'cb_therapist',
			'post_title'   => sanitize_text_field( $name ),
			'post_content' => wp_kses_post( (string) ( $data['bio'] ?? '' ) ),
			'post_status'  => 'publish',
		];
		if ( ! empty( $data['slug'] ) ) {
			$insert['post_name'] = sanitize_title( (string) $data['slug'] );
		}

		$id = wp_insert_post( $insert, true );
		if ( is_wp_error( $id ) ) {
			return new \WP_Error( 'CREATE_FAILED', $id->get_error_message(), [ 'status' => 400 ] );
		}

		$meta_err = self::apply_therapist_meta( (int) $id, $data );
		if ( is_wp_error( $meta_err ) ) {
			wp_delete_post( (int) $id, true );
			return $meta_err;
		}

		return self::detail_dto( get_post( $id ) );
	}

	/**
	 * @param array<string, mixed> $data
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function update_therapist( int $id, array $data ) {
		$t = get_post( $id );
		if ( ! $t || 'cb_therapist' !== $t->post_type ) {
			return new \WP_Error( 'NOT_FOUND', 'درمانگر یافت نشد', [ 'status' => 404 ] );
		}

		if ( ! empty( $data['timezone'] ) && ! self::is_valid_timezone( (string) $data['timezone'] ) ) {
			return new \WP_Error( 'INVALID_TIMEZONE', 'منطقه زمانی نامعتبر است', [ 'status' => 400 ] );
		}

		$update = [ 'ID' => $id ];
		if ( isset( $data['name'] ) ) {
			$update['post_title'] = sanitize_text_field( (string) $data['name'] );
		}
		if ( isset( $data['bio'] ) ) {
			$update['post_content'] = wp_kses_post( (string) $data['bio'] );
		}
		wp_update_post( $update );

		$meta_err = self::apply_therapist_meta( $id, $data );
		if ( is_wp_error( $meta_err ) ) {
			return $meta_err;
		}

		return self::detail_dto( get_post( $id ) );
	}

	/**
	 * @return true|\WP_Error
	 */
	public static function delete_therapist( int $id ) {
		$t = get_post( $id );
		if ( ! $t || 'cb_therapist' !== $t->post_type ) {
			return new \WP_Error( 'NOT_FOUND', 'درمانگر یافت نشد', [ 'status' => 404 ] );
		}
		wp_delete_post( $id, true );
		return true;
	}

	/**
	 * Adds a validated future slot to cb_slots meta.
	 *
	 * @param array<string, mixed> $data
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function add_slot( int $therapist_id, array $data ) {
		$t = get_post( $therapist_id );
		if ( ! $t || 'cb_therapist' !== $t->post_type ) {
			return new \WP_Error( 'NOT_FOUND', 'درمانگر یافت نشد', [ 'status' => 404 ] );
		}

		$slot_str = trim( (string) ( $data['slotTime'] ?? ( $data['startTime'] ?? '' ) ) );
		if ( '' === $slot_str ) {
			return new \WP_Error( 'INVALID_SLOT_TIME', 'زمان بازه الزامی است', [ 'status' => 400 ] );
		}

		if ( ! empty( $data['timezone'] ) && ! self::is_valid_timezone( (string) $data['timezone'] ) ) {
			return new \WP_Error( 'INVALID_TIMEZONE', 'منطقه زمانی نامعتبر است', [ 'status' => 400 ] );
		}

		$tz = self::resolve_timezone( $therapist_id, isset( $data['timezone'] ) ? (string) $data['timezone'] : null );
		$dt = self::parse_slot_datetime( $slot_str, $tz );
		if ( ! $dt || $dt->getTimestamp() <= time() ) {
			return new \WP_Error( 'INVALID_SLOT_TIME', 'بازه زمانی نامعتبر یا در گذشته است', [ 'status' => 400 ] );
		}

		$canonical = $dt->format( 'Y-m-d\TH:i' );
		$raw       = trim( (string) get_post_meta( $therapist_id, 'cb_slots', true ) );
		$existing  = '' === $raw ? [] : array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $raw ) ?: [] ) );
		if ( ! in_array( $canonical, $existing, true ) ) {
			$existing[] = $canonical;
			update_post_meta( $therapist_id, 'cb_slots', implode( "\n", $existing ) );
		}

		return [ 'slots' => self::slot_dtos( $therapist_id ) ];
	}

	/**
	 * @param array<string, mixed> $data
	 * @return true|\WP_Error
	 */
	private static function apply_therapist_meta( int $id, array $data ) {
		if ( isset( $data['timezone'] ) && '' !== trim( (string) $data['timezone'] ) ) {
			update_post_meta( $id, 'cb_timezone', sanitize_text_field( (string) $data['timezone'] ) );
		}
		if ( isset( $data['specialty'] ) ) {
			update_post_meta( $id, 'cb_specialty', sanitize_text_field( (string) $data['specialty'] ) );
		}
		if ( isset( $data['bio'] ) ) {
			update_post_meta( $id, 'cb_approach', wp_kses_post( (string) $data['bio'] ) );
		}
		if ( isset( $data['sessionPrice'] ) ) {
			update_post_meta( $id, 'cb_session_price', (float) $data['sessionPrice'] );
		}
		if ( isset( $data['sessionDurationMinutes'] ) ) {
			update_post_meta( $id, 'cb_duration', max( 5, (int) $data['sessionDurationMinutes'] ) );
		}
		if ( isset( $data['mode'] ) ) {
			update_post_meta( $id, 'cb_mode', sanitize_text_field( (string) $data['mode'] ) );
		}
		if ( isset( $data['location'] ) ) {
			update_post_meta( $id, 'cb_location', sanitize_text_field( (string) $data['location'] ) );
		}
		if ( isset( $data['productSlug'] ) ) {
			update_post_meta( $id, 'cb_product_slug', sanitize_title( (string) $data['productSlug'] ) );
		}
		if ( isset( $data['slots'] ) && is_array( $data['slots'] ) ) {
			$tz    = self::resolve_timezone( $id );
			$clean = [];
			foreach ( $data['slots'] as $s ) {
				$dt = self::parse_slot_datetime( (string) $s, $tz );
				if ( ! $dt ) {
					return new \WP_Error( 'INVALID_SLOT_TIME', 'بازه زمانی نامعتبر است', [ 'status' => 400 ] );
				}
				$clean[] = $dt->format( 'Y-m-d\TH:i' );
			}
			update_post_meta( $id, 'cb_slots', implode( "\n", $clean ) );
		}
		return true;
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function summary_dto( \WP_Post $t ): array {
		$id    = (int) $t->ID;
		$slug  = (string) get_post_meta( $id, 'cb_product_slug', true );
		$slots = self::slot_dtos( $id );

		return [
			'id'                 => $id,
			'name'               => get_the_title( $t ),
			'slug'               => $t->post_name,
			'specialty'          => get_post_meta( $id, 'cb_specialty', true ) ?: null,
			'photoUrl'           => get_the_post_thumbnail_url( $t, 'medium' ) ?: null,
			'sessionPrice'       => (float) get_post_meta( $id, 'cb_session_price', true ),
			'availableSlotCount' => count( $slots ),
			'requiresPurchase'   => '' !== $slug,
			'productSlug'        => '' !== $slug ? $slug : null,
			'isPaid'             => (bool) get_post_meta( $id, 'cb_is_paid', true ),
			'timezone'           => self::resolve_timezone( $id )->getName(),
		];
	}

	/**
	 * Public therapist profile + availability (strictly excludes any private clinical/patient data).
	 *
	 * @return array<string, mixed>
	 */
	public static function detail_dto( \WP_Post $t ): array {
		$id    = (int) $t->ID;
		$slug  = (string) get_post_meta( $id, 'cb_product_slug', true );
		$slots = self::slot_dtos( $id );
		$p     = '' !== $slug ? get_page_by_path( $slug, OBJECT, 'product' ) : null;

		return [
			'id'                     => $id,
			'name'                   => get_the_title( $t ),
			'slug'                   => $t->post_name,
			'specialty'              => get_post_meta( $id, 'cb_specialty', true ) ?: null,
			'bio'                    => get_post_meta( $id, 'cb_approach', true ) ?: ( $t->post_content ?: null ),
			'photoUrl'               => get_the_post_thumbnail_url( $t, 'large' ) ?: null,
			'sessionPrice'           => (float) get_post_meta( $id, 'cb_session_price', true ),
			'sessionDurationMinutes' => self::session_minutes( $id ),
			'timezone'               => self::resolve_timezone( $id )->getName(),
			'slots'                  => $slots,
			'requiresPurchase'       => '' !== $slug,
			'productSlug'            => '' !== $slug ? $slug : null,
			'isPaid'                 => (bool) get_post_meta( $id, 'cb_is_paid', true ),
			'mode'                   => get_post_meta( $id, 'cb_mode', true ) ?: 'ONLINE',
			'location'               => get_post_meta( $id, 'cb_location', true ) ?: null,
			'productId'              => $p ? (int) $p->ID : null,
		];
	}
}

}
