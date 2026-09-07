<?php
namespace Carmilla\Core\Privacy;

use Carmilla\Core\Migration\SchemaRunner;

/**
 * GDPR and Personal Data Privacy Manager.
 *
 * Provides WordPress Privacy API integration for data export, data erasure/anonymization,
 * and Privacy Policy guide declarations across Carmilla Bridge and Carmilla Theme.
 *
 * Financial and transactional records are anonymized rather than hard-deleted to satisfy
 * statutory accounting and tax compliance requirements.
 *
 * Task: P04-WPPLUGIN-CODE-013
 *
 * @package Carmilla\Core\Privacy
 */
class PrivacyManager {

	/**
	 * Tracks whether privacy hooks have been registered in the current request.
	 *
	 * @var bool
	 */
	private static bool $booted = false;

	/**
	 * Boots the Privacy Manager, preventing duplicate hook registration in co-install cycles.
	 *
	 * @return void
	 */
	public static function boot(): void {
		if ( self::$booted ) {
			return;
		}

		if ( class_exists( '\Carmilla\Core\Migration\SchemaRunner' ) ) {
			if ( ! SchemaRunner::register_checkpoint( 'privacy_manager_boot' ) ) {
				return;
			}
		}

		self::$booted = true;

		self::register_exporters();
		self::register_erasers();

		add_action( 'admin_init', array( __CLASS__, 'register_privacy_policy' ) );
	}

	/**
	 * Registers personal data exporters with WordPress Core.
	 *
	 * @return void
	 */
	public static function register_exporters(): void {
		add_filter(
			'wp_privacy_personal_data_exporters',
			array( __CLASS__, 'add_data_exporters' )
		);
	}

	/**
	 * Adds Carmilla data exporter callback to the exporters list.
	 *
	 * @param array $exporters Registered exporters.
	 * @return array
	 */
	public static function add_data_exporters( array $exporters ): array {
		$exporters['carmilla-core-data'] = array(
			'exporter_friendly_name' => __( 'Carmilla Personal Data (Orders, Bookings & Tests)', 'carmilla' ),
			'callback'               => array( __CLASS__, 'export_user_data' ),
		);
		return $exporters;
	}

	/**
	 * Registers personal data erasers with WordPress Core.
	 *
	 * @return void
	 */
	public static function register_erasers(): void {
		add_filter(
			'wp_privacy_personal_data_erasers',
			array( __CLASS__, 'add_data_erasers' )
		);
	}

	/**
	 * Adds Carmilla data eraser callback to the erasers list.
	 *
	 * @param array $erasers Registered erasers.
	 * @return array
	 */
	public static function add_data_erasers( array $erasers ): array {
		$erasers['carmilla-core-eraser'] = array(
			'eraser_friendly_name' => __( 'Carmilla Personal Data Anonymizer', 'carmilla' ),
			'callback'             => array( __CLASS__, 'erase_user_data' ),
		);
		return $erasers;
	}

	/**
	 * Exports a user's Carmilla-specific data (orders, clinic bookings, psych test results, academy progress).
	 *
	 * @param string $email_address Email address of the user being exported.
	 * @param int    $page          Pagination page number.
	 * @return array Standard WordPress privacy export array.
	 */
	public static function export_user_data( string $email_address, int $page = 1 ): array {
		$data_to_export = array();
		$user           = get_user_by( 'email', $email_address );

		if ( ! $user ) {
			return array(
				'data' => array(),
				'done' => true,
			);
		}

		$user_id = (int) $user->ID;

		// 1. Export Carmilla Orders & Commerce Data.
		$orders_data = self::get_user_orders_export( $user_id, $email_address );
		if ( ! empty( $orders_data ) ) {
			$data_to_export = array_merge( $data_to_export, $orders_data );
		}

		// 2. Export Clinic Bookings & Appointments.
		$bookings_data = self::get_user_bookings_export( $user_id, $email_address );
		if ( ! empty( $bookings_data ) ) {
			$data_to_export = array_merge( $data_to_export, $bookings_data );
		}

		// 3. Export Psychological Test Results.
		$tests_data = self::get_user_psychtests_export( $user_id );
		if ( ! empty( $tests_data ) ) {
			$data_to_export = array_merge( $data_to_export, $tests_data );
		}

		// 4. Export Academy Course Progress.
		$academy_data = self::get_user_academy_export( $user_id );
		if ( ! empty( $academy_data ) ) {
			$data_to_export = array_merge( $data_to_export, $academy_data );
		}

		return array(
			'data' => $data_to_export,
			'done' => true,
		);
	}

	/**
	 * Anonymizes a user's Carmilla data.
	 *
	 * Adheres to financial record retention rules: WooCommerce orders and billing transactions
	 * are NOT deleted; personal customer details are stripped/anonymized while financial line
	 * items are retained for statutory accounting compliance.
	 *
	 * @param string $email_address Email address of the user.
	 * @param int    $page          Pagination page number.
	 * @return array Standard WordPress privacy eraser response array.
	 */
	public static function erase_user_data( string $email_address, int $page = 1 ): array {
		$items_removed  = 0;
		$items_retained = 0;
		$messages       = array();
		$user           = get_user_by( 'email', $email_address );

		if ( ! $user ) {
			return array(
				'items_removed'  => 0,
				'items_retained' => 0,
				'messages'       => array(),
				'done'           => true,
			);
		}

		$user_id = (int) $user->ID;

		// 1. Anonymize Orders (Retain financial ledger, remove PII).
		$order_results = self::anonymize_user_orders( $user_id, $email_address );
		$items_retained += $order_results['retained'];
		$messages = array_merge( $messages, $order_results['messages'] );

		// 2. Anonymize Clinic Bookings & Appointments.
		$booking_count = self::anonymize_user_bookings( $user_id, $email_address );
		$items_removed += $booking_count;
		if ( $booking_count > 0 ) {
			$messages[] = sprintf(
				/* translators: %d: number of appointments */
				__( 'Anonymized personal details from %d clinic consultation appointment(s).', 'carmilla' ),
				$booking_count
			);
		}

		// 3. Erase / Anonymize Psychological Test Records.
		$test_count = self::anonymize_user_psychtests( $user_id );
		$items_removed += $test_count;
		if ( $test_count > 0 ) {
			$messages[] = sprintf(
				/* translators: %d: number of psych tests */
				__( 'Scrubbed personal identifiers from %d psychological test submission(s).', 'carmilla' ),
				$test_count
			);
		}

		// 4. Remove Carmilla-specific user metadata.
		$meta_keys = array(
			'cb_enrolled_courses',
			'cb_course_progress',
			'cb_favorites',
			'cb_recently_viewed',
			'cb_psychtest_history',
			'cb_device_tokens',
			'cb_referral_code',
		);
		foreach ( $meta_keys as $meta_key ) {
			if ( metadata_exists( 'user', $user_id, $meta_key ) ) {
				delete_user_meta( $user_id, $meta_key );
				$items_removed++;
			}
		}

		return array(
			'items_removed'  => $items_removed,
			'items_retained' => $items_retained,
			'messages'       => $messages,
			'done'           => true,
		);
	}

	/**
	 * Returns human-readable Privacy Policy guide content.
	 *
	 * @return string
	 */
	public static function get_privacy_policy_content(): string {
		return __(
			'<h2>Carmilla Platform Privacy Policy Guide</h2>' .
			'<p>Carmilla processes user personal data to deliver integrated commerce, academic learning (LMS), psychological testing, and clinic appointment booking services.</p>' .
			'<h3>What Personal Data We Collect and Why</h3>' .
			'<ul>' .
			'<li><strong>Commerce & Orders:</strong> Billing details, shipping addresses, phone numbers, and payment transaction metadata are collected to fulfill purchases and prevent fraud.</li>' .
			'<li><strong>Clinic & Appointments:</strong> Consultation requests, booked therapist slots, and optional medical intake notes are processed to provide specialized clinical services.</li>' .
			'<li><strong>Psychological Testing:</strong> Test answers, computed scores, and interpretive assessments are stored securely for diagnostic evaluation and user review.</li>' .
			'<li><strong>Academy & Courses:</strong> Course enrollments, lesson completion status, quiz scores, and issued certificates are tracked to manage educational progress.</li>' .
			'</ul>' .
			'<h3>Data Retention and Financial Compliance</h3>' .
			'<p>Financial transaction records, invoices, and purchase histories are retained in accordance with statutory accounting and tax regulations (typically between 5 and 10 years depending on local law). When a user requests data erasure, financial ledger records are strictly anonymized rather than deleted, ensuring personal identifying details are removed while accounting integrity is maintained.</p>' .
			'<h3>User Rights</h3>' .
			'<p>Under GDPR and applicable privacy legislation, registered users have the right to request a complete export of their personal data or request anonymization and erasure of their profile records.</p>',
			'carmilla'
		);
	}

	/**
	 * Registers privacy policy content with WordPress Core.
	 *
	 * @return void
	 */
	public static function register_privacy_policy(): void {
		if ( function_exists( 'wp_add_privacy_policy_content' ) ) {
			wp_add_privacy_policy_content(
				__( 'Carmilla Platform (Bridge & Theme)', 'carmilla' ),
				wpautop( wp_kses_post( self::get_privacy_policy_content() ) )
			);
		}
	}

	/**
	 * Helper: queries and formats user orders for export.
	 *
	 * @param int    $user_id       User ID.
	 * @param string $email_address Email address.
	 * @return array
	 */
	private static function get_user_orders_export( int $user_id, string $email_address ): array {
		$export_data = array();

		if ( function_exists( 'wc_get_orders' ) ) {
			$orders = wc_get_orders( array(
				'customer' => array( $user_id, $email_address ),
				'limit'    => -1,
			) );

			foreach ( $orders as $order ) {
				$order_id = $order->get_id();
				$export_data[] = array(
					'group_id'    => 'carmilla_orders',
					'group_label' => __( 'Carmilla Orders & Transactions', 'carmilla' ),
					'item_id'     => 'carmilla-order-' . $order_id,
					'data'        => array(
						array( 'name' => __( 'Order ID', 'carmilla' ), 'value' => $order_id ),
						array( 'name' => __( 'Date', 'carmilla' ), 'value' => $order->get_date_created() ? $order->get_date_created()->date( 'Y-m-d H:i:s' ) : '' ),
						array( 'name' => __( 'Status', 'carmilla' ), 'value' => $order->get_status() ),
						array( 'name' => __( 'Total', 'carmilla' ), 'value' => $order->get_total() . ' ' . $order->get_currency() ),
						array( 'name' => __( 'Payment Method', 'carmilla' ), 'value' => $order->get_payment_method_title() ),
					),
				);
			}
		}

		return $export_data;
	}

	/**
	 * Helper: queries and formats user appointments for export.
	 *
	 * @param int    $user_id       User ID.
	 * @param string $email_address Email address.
	 * @return array
	 */
	private static function get_user_bookings_export( int $user_id, string $email_address ): array {
		$export_data = array();

		$appointments = get_posts( array(
			'post_type'      => 'cb_appointment',
			'post_status'    => 'any',
			'posts_per_page' => 100,
			'author'         => $user_id,
		) );

		foreach ( $appointments as $post ) {
			$therapist_id = get_post_meta( $post->ID, 'cb_therapist_id', true );
			$therapist    = $therapist_id ? get_the_title( (int) $therapist_id ) : __( 'N/A', 'carmilla' );
			$slot         = get_post_meta( $post->ID, 'cb_appointment_slot', true );

			$export_data[] = array(
				'group_id'    => 'carmilla_bookings',
				'group_label' => __( 'Carmilla Clinic Appointments', 'carmilla' ),
				'item_id'     => 'carmilla-booking-' . $post->ID,
				'data'        => array(
					array( 'name' => __( 'Appointment ID', 'carmilla' ), 'value' => $post->ID ),
					array( 'name' => __( 'Therapist', 'carmilla' ), 'value' => $therapist ),
					array( 'name' => __( 'Scheduled Slot', 'carmilla' ), 'value' => $slot ),
					array( 'name' => __( 'Status', 'carmilla' ), 'value' => $post->post_status ),
				),
			);
		}

		return $export_data;
	}

	/**
	 * Helper: queries and formats user psychological test results for export.
	 *
	 * @param int $user_id User ID.
	 * @return array
	 */
	private static function get_user_psychtests_export( int $user_id ): array {
		$export_data = array();
		$history     = get_user_meta( $user_id, 'cb_psychtest_history', true );

		if ( is_array( $history ) ) {
			foreach ( $history as $index => $item ) {
				$export_data[] = array(
					'group_id'    => 'carmilla_psychtests',
					'group_label' => __( 'Carmilla Psychological Tests', 'carmilla' ),
					'item_id'     => 'carmilla-test-' . $index,
					'data'        => array(
						array( 'name' => __( 'Test Title', 'carmilla' ), 'value' => $item['title'] ?? '' ),
						array( 'name' => __( 'Date Completed', 'carmilla' ), 'value' => $item['date'] ?? '' ),
						array( 'name' => __( 'Score', 'carmilla' ), 'value' => $item['score'] ?? '' ),
						array( 'name' => __( 'Interpretation', 'carmilla' ), 'value' => $item['interpretation'] ?? '' ),
					),
				);
			}
		}

		return $export_data;
	}

	/**
	 * Helper: queries and formats user academy course progress for export.
	 *
	 * @param int $user_id User ID.
	 * @return array
	 */
	private static function get_user_academy_export( int $user_id ): array {
		$export_data = array();
		$courses     = get_user_meta( $user_id, 'cb_enrolled_courses', true );
		$progress    = get_user_meta( $user_id, 'cb_course_progress', true );

		if ( is_array( $courses ) ) {
			foreach ( $courses as $course_id ) {
				$course_title = get_the_title( (int) $course_id );
				$prog_val     = is_array( $progress ) && isset( $progress[ $course_id ] ) ? $progress[ $course_id ] . '%' : '0%';

				$export_data[] = array(
					'group_id'    => 'carmilla_academy',
					'group_label' => __( 'Carmilla Academy Enrollments', 'carmilla' ),
					'item_id'     => 'carmilla-course-' . $course_id,
					'data'        => array(
						array( 'name' => __( 'Course ID', 'carmilla' ), 'value' => $course_id ),
						array( 'name' => __( 'Course Title', 'carmilla' ), 'value' => $course_title ),
						array( 'name' => __( 'Progress', 'carmilla' ), 'value' => $prog_val ),
					),
				);
			}
		}

		return $export_data;
	}

	/**
	 * Helper: anonymizes user orders without deleting financial ledger records.
	 *
	 * @param int    $user_id       User ID.
	 * @param string $email_address Email address.
	 * @return array
	 */
	private static function anonymize_user_orders( int $user_id, string $email_address ): array {
		$retained = 0;
		$messages = array();

		if ( function_exists( 'wc_get_orders' ) ) {
			$orders = wc_get_orders( array(
				'customer' => array( $user_id, $email_address ),
				'limit'    => -1,
			) );

			foreach ( $orders as $order ) {
				$order_id = $order->get_id();

				// Anonymize personal details.
				$order->set_billing_first_name( __( 'Anonymized', 'carmilla' ) );
				$order->set_billing_last_name( __( 'Customer', 'carmilla' ) );
				$order->set_billing_email( 'anonymized@example.invalid' );
				$order->set_billing_phone( '' );
				$order->set_billing_address_1( '' );
				$order->set_billing_address_2( '' );
				$order->set_shipping_first_name( '' );
				$order->set_shipping_last_name( '' );
				$order->set_shipping_address_1( '' );
				$order->set_shipping_address_2( '' );
				$order->set_customer_id( 0 );
				$order->set_customer_ip_address( '0.0.0.0' );
				$order->set_customer_user_agent( '' );
				$order->save();

				$retained++;
				$messages[] = sprintf(
					/* translators: %d: order ID */
					__( 'Order #%d retained for statutory accounting/tax compliance; customer details anonymized.', 'carmilla' ),
					$order_id
				);
			}
		}

		return array(
			'retained' => $retained,
			'messages' => $messages,
		);
	}

	/**
	 * Helper: anonymizes clinic appointments for the user.
	 *
	 * @param int    $user_id       User ID.
	 * @param string $email_address Email address.
	 * @return int Number of appointments anonymized.
	 */
	private static function anonymize_user_bookings( int $user_id, string $email_address ): int {
		$appointments = get_posts( array(
			'post_type'      => 'cb_appointment',
			'post_status'    => 'any',
			'posts_per_page' => 100,
			'author'         => $user_id,
		) );

		$count = 0;
		foreach ( $appointments as $post ) {
			wp_update_post( array(
				'ID'          => $post->ID,
				'post_author' => 0,
				'post_title'  => sprintf( __( 'Anonymized Appointment #%d', 'carmilla' ), $post->ID ),
			) );

			delete_post_meta( $post->ID, 'cb_patient_notes' );
			delete_post_meta( $post->ID, 'cb_patient_phone' );
			delete_post_meta( $post->ID, 'cb_patient_email' );

			$count++;
		}

		return $count;
	}

	/**
	 * Helper: scrubs user psychological test records.
	 *
	 * @param int $user_id User ID.
	 * @return int Number of records scrubbed.
	 */
	private static function anonymize_user_psychtests( int $user_id ): int {
		if ( metadata_exists( 'user', $user_id, 'cb_psychtest_history' ) ) {
			delete_user_meta( $user_id, 'cb_psychtest_history' );
			return 1;
		}
		return 0;
	}
}
