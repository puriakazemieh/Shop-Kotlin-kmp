<?php
namespace Carmilla\Core\Support;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\SupportService', false ) ) {

/**
 * Canonical Shared Support Ticket & Thread Service.
 *
 * Centralizes support tickets & message threads stored in user meta `cb_tickets`
 * (strictly isolated from `cb_psych_attempts` and clinical `cb_msg` threads),
 * with owner-scoped access, admin reply/status management, and `content.support`
 * feature gating across Theme-only, Plugin-only, and Co-install modes.
 *
 * Part of Task P04-WORDPRESS-CODE-029A.
 */
class SupportService {

	public const ALLOWED_STATUSES = [ 'OPEN', 'ANSWERED', 'CLOSED' ];

	public static function is_enabled(): bool {
		if ( class_exists( '\Carmilla_Kernel' ) && \Carmilla_Kernel::is_booted() ) {
			return \Carmilla_Kernel::is_feature_active( 'content.support' );
		}
		return true;
	}

	public static function can_manage_support(): bool {
		return current_user_can( 'manage_options' )
			|| current_user_can( 'carmilla_manage_content' )
			|| current_user_can( 'edit_others_posts' );
	}

	/**
	 * @return array<int, array<string, mixed>>
	 */
	private static function raw_tickets( int $uid ): array {
		if ( $uid < 1 ) {
			return [];
		}
		$v = get_user_meta( $uid, 'cb_tickets', true );
		return is_array( $v ) ? array_values( $v ) : [];
	}

	/**
	 * @param array<int, array<string, mixed>> $list
	 */
	private static function save_tickets( int $uid, array $list ): void {
		update_user_meta( $uid, 'cb_tickets', array_values( $list ) );
	}

	/**
	 * @return array<int, array<string, mixed>>
	 */
	public static function list_tickets( int $uid ): array {
		return array_map( [ self::class, 'summary_dto' ], self::raw_tickets( $uid ) );
	}

	/**
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function get_ticket( int $uid, int $ticket_id ) {
		if ( ! self::is_enabled() ) {
			return new \WP_Error( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', [ 'status' => 403 ] );
		}
		foreach ( self::raw_tickets( $uid ) as $t ) {
			if ( (int) ( $t['id'] ?? 0 ) === $ticket_id ) {
				return self::detail_dto( $t, $uid );
			}
		}
		return new \WP_Error( 'NOT_FOUND', 'تیکت یافت نشد', [ 'status' => 404 ] );
	}

	/**
	 * @param array<string, mixed> $params
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function create_ticket( int $uid, array $params ) {
		if ( ! self::is_enabled() ) {
			return new \WP_Error( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', [ 'status' => 403 ] );
		}
		if ( $uid < 1 ) {
			return new \WP_Error( 'UNAUTHORIZED', 'ابتدا وارد شوید', [ 'status' => 401 ] );
		}

		$subject = sanitize_text_field( (string) ( $params['subject'] ?? 'پشتیبانی' ) );
		$body    = sanitize_textarea_field( (string) ( $params['message'] ?? ( $params['body'] ?? '' ) ) );
		if ( '' === trim( $subject ) || '' === trim( $body ) ) {
			return new \WP_Error( 'VALIDATION_ERROR', 'موضوع و متن پیام الزامی است', [ 'status' => 400 ] );
		}

		$list = self::raw_tickets( $uid );
		$max  = 0;
		foreach ( $list as $existing ) {
			$max = max( $max, (int) ( $existing['id'] ?? 0 ) );
		}
		$now = gmdate( 'c' );
		$msg = [
			'id'         => 1,
			'senderRole' => 'USER',
			'body'       => $body,
			'createdAt'  => $now,
		];
		if ( ! empty( $params['attachmentId'] ) ) {
			$msg['attachmentId'] = (int) $params['attachmentId'];
		}

		$ticket = [
			'id'        => $max + 1,
			'subject'   => $subject,
			'status'    => 'OPEN',
			'createdAt' => $now,
			'updatedAt' => $now,
			'messages'  => [ $msg ],
		];
		$list[] = $ticket;
		self::save_tickets( $uid, $list );
		return self::detail_dto( $ticket, $uid );
	}

	/**
	 * @param array<string, mixed> $params
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function post_message( int $uid, int $ticket_id, array $params, string $sender_role = 'USER' ) {
		if ( ! self::is_enabled() ) {
			return new \WP_Error( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', [ 'status' => 403 ] );
		}
		$body = sanitize_textarea_field( (string) ( $params['body'] ?? ( $params['message'] ?? '' ) ) );
		if ( '' === trim( $body ) ) {
			return new \WP_Error( 'VALIDATION_ERROR', 'متن پیام خالی است', [ 'status' => 400 ] );
		}

		$list  = self::raw_tickets( $uid );
		$found = null;
		$now   = gmdate( 'c' );
		foreach ( $list as &$t ) {
			if ( (int) ( $t['id'] ?? 0 ) === $ticket_id ) {
				$msgs = isset( $t['messages'] ) && is_array( $t['messages'] ) ? $t['messages'] : [];
				$msg  = [
					'id'         => count( $msgs ) + 1,
					'senderRole' => $sender_role,
					'body'       => $body,
					'createdAt'  => $now,
				];
				if ( ! empty( $params['attachmentId'] ) ) {
					$msg['attachmentId'] = (int) $params['attachmentId'];
				}
				$msgs[]         = $msg;
				$t['messages']  = $msgs;
				$t['status']    = 'ADMIN' === $sender_role ? 'ANSWERED' : 'OPEN';
				$t['updatedAt'] = $now;
				$found          = $t;
				break;
			}
		}
		unset( $t );

		if ( null === $found ) {
			return new \WP_Error( 'NOT_FOUND', 'تیکت یافت نشد', [ 'status' => 404 ] );
		}
		self::save_tickets( $uid, $list );
		return self::detail_dto( $found, $uid );
	}

	/**
	 * Legacy support.js chat thread adapter (maps to user's latest or default support ticket).
	 *
	 * @return array<string, mixed>
	 */
	public static function legacy_thread( int $uid ): array {
		$tickets = self::raw_tickets( $uid );
		if ( empty( $tickets ) ) {
			return [ 'messages' => [] ];
		}
		$last = end( $tickets );
		$out  = [];
		foreach ( (array) ( $last['messages'] ?? [] ) as $m ) {
			$out[] = [
				'id'   => (int) ( $m['id'] ?? 0 ),
				'me'   => ( $m['senderRole'] ?? 'USER' ) === 'USER',
				'text' => (string) ( $m['body'] ?? '' ),
			];
		}
		return [ 'messages' => $out ];
	}

	/**
	 * Legacy support.js message post adapter.
	 *
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function legacy_post( int $uid, string $message ) {
		$tickets = self::raw_tickets( $uid );
		if ( empty( $tickets ) ) {
			$created = self::create_ticket( $uid, [ 'subject' => 'پشتیبانی', 'message' => $message ] );
			if ( is_wp_error( $created ) ) {
				return $created;
			}
			$last_msg = end( $created['messages'] );
			return [ 'id' => (int) ( $last_msg['id'] ?? 1 ), 'me' => true, 'text' => (string) ( $last_msg['body'] ?? $message ) ];
		}
		$last = end( $tickets );
		$res  = self::post_message( $uid, (int) $last['id'], [ 'body' => $message ], 'USER' );
		if ( is_wp_error( $res ) ) {
			return $res;
		}
		$last_msg = end( $res['messages'] );
		return [ 'id' => (int) ( $last_msg['id'] ?? 1 ), 'me' => true, 'text' => (string) ( $last_msg['body'] ?? $message ) ];
	}

	/**
	 * Admin list of all support tickets across users.
	 *
	 * @return array<int, array<string, mixed>>
	 */
	public static function admin_list_tickets(): array {
		$users = get_users( [ 'meta_key' => 'cb_tickets', 'number' => 500 ] );
		$out   = [];
		foreach ( $users as $u ) {
			foreach ( self::raw_tickets( (int) $u->ID ) as $t ) {
				$dto             = self::summary_dto( $t );
				$dto['userId']   = (int) $u->ID;
				$dto['userName'] = $u->display_name;
				$out[]           = $dto;
			}
		}
		return $out;
	}

	/**
	 * Admin updates ticket status.
	 *
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function admin_set_status( int $uid, int $ticket_id, string $status ) {
		$status = strtoupper( trim( $status ) );
		if ( ! in_array( $status, self::ALLOWED_STATUSES, true ) ) {
			return new \WP_Error( 'INVALID_STATUS', 'وضعیت تیکت نامعتبر است', [ 'status' => 400 ] );
		}
		$list  = self::raw_tickets( $uid );
		$found = null;
		foreach ( $list as &$t ) {
			if ( (int) ( $t['id'] ?? 0 ) === $ticket_id ) {
				$t['status']    = $status;
				$t['updatedAt'] = gmdate( 'c' );
				$found          = $t;
				break;
			}
		}
		unset( $t );
		if ( null === $found ) {
			return new \WP_Error( 'NOT_FOUND', 'تیکت یافت نشد', [ 'status' => 404 ] );
		}
		self::save_tickets( $uid, $list );
		return self::detail_dto( $found, $uid );
	}

	/**
	 * @param array<string, mixed> $t
	 * @return array<string, mixed>
	 */
	private static function summary_dto( array $t ): array {
		$msgs = isset( $t['messages'] ) && is_array( $t['messages'] ) ? $t['messages'] : [];
		$last = $msgs ? end( $msgs ) : null;
		return [
			'id'          => (int) ( $t['id'] ?? 0 ),
			'subject'     => (string) ( $t['subject'] ?? '' ),
			'status'      => (string) ( $t['status'] ?? 'OPEN' ),
			'lastMessage' => $last ? (string) ( $last['body'] ?? '' ) : null,
			'createdAt'   => (string) ( $t['createdAt'] ?? '' ),
			'updatedAt'   => (string) ( $t['updatedAt'] ?? '' ),
		];
	}

	/**
	 * @param array<string, mixed> $t
	 * @return array<string, mixed>
	 */
	private static function detail_dto( array $t, int $uid ): array {
		$msgs = isset( $t['messages'] ) && is_array( $t['messages'] ) ? array_values( $t['messages'] ) : [];
		return [
			'id'        => (int) ( $t['id'] ?? 0 ),
			'subject'   => (string) ( $t['subject'] ?? '' ),
			'status'    => (string) ( $t['status'] ?? 'OPEN' ),
			'userId'    => $uid,
			'messages'  => $msgs,
			'createdAt' => (string) ( $t['createdAt'] ?? '' ),
			'updatedAt' => (string) ( $t['updatedAt'] ?? '' ),
		];
	}
}

}
