<?php
namespace Carmilla\Core\Psych;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\PsychTestService', false ) ) {

/**
 * Canonical Shared Psychological Test Service.
 *
 * Centralizes cb_psychtest CPT catalog, server-side question/range parsing,
 * deterministic scoring & interpretation, ownership checks, private user
 * attempts (cb_psych_attempts), and admin counselor interpretation across
 * Theme-only, Plugin-only, and Co-install modes.
 *
 * Strictly independent of clinic.booking. Option scores never leak to clients,
 * and private attempt/result access is strictly owner-scoped (IDOR-safe).
 *
 * Part of Task P04-WORDPRESS-CODE-029.
 */
class PsychTestService {

	public static function is_enabled(): bool {
		if ( class_exists( '\Carmilla_Kernel' ) && \Carmilla_Kernel::is_booted() ) {
			return \Carmilla_Kernel::is_feature_active( 'psych.tests' );
		}
		return true;
	}

	public static function can_manage_psych_tests(): bool {
		return current_user_can( 'manage_options' )
			|| current_user_can( 'carmilla_manage_content' )
			|| current_user_can( 'edit_others_posts' );
	}

	// ---- parsing & scoring (canonical, deterministic) -----------------------

	/**
	 * «text | label=score , label=score» -> [ ['index'=>, 'text'=>, 'options'=>[ ['text'=>,'score'=>], ... ]], ... ]
	 *
	 * @return array<int, array<string, mixed>>
	 */
	public static function parse_questions( string $raw ): array {
		$lines = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $raw ) ?: [] ) );
		$out   = [];
		$i     = 0;
		foreach ( $lines as $line ) {
			$parts = explode( '|', $line, 2 );
			$text  = trim( $parts[0] );
			if ( '' === $text ) {
				continue;
			}
			$options = [];
			if ( isset( $parts[1] ) ) {
				foreach ( preg_split( '/[,،]/u', $parts[1] ) ?: [] as $opt ) {
					$opt = trim( $opt );
					if ( '' === $opt ) {
						continue;
					}
					$pos = strrpos( $opt, '=' );
					if ( false === $pos ) {
						$options[] = [ 'text' => $opt, 'score' => 0 ];
					} else {
						$options[] = [
							'text'  => trim( substr( $opt, 0, $pos ) ),
							'score' => (int) trim( substr( $opt, $pos + 1 ) ),
						];
					}
				}
			}
			$out[] = [ 'index' => $i, 'text' => $text, 'options' => $options ];
			++$i;
		}
		return $out;
	}

	/**
	 * «min | max | interpretation» -> [ ['min'=>,'max'=>,'interpretation'=>], ... ]
	 *
	 * @return array<int, array<string, mixed>>
	 */
	public static function parse_ranges( string $raw ): array {
		$lines = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $raw ) ?: [] ) );
		$out   = [];
		foreach ( $lines as $line ) {
			$p = array_map( 'trim', explode( '|', $line ) );
			if ( count( $p ) < 3 ) {
				continue;
			}
			$out[] = [ 'min' => (int) $p[0], 'max' => (int) $p[1], 'interpretation' => $p[2] ];
		}
		return $out;
	}

	/**
	 * First range containing the score, or null.
	 *
	 * @param array<int, array<string, mixed>> $ranges
	 */
	public static function interpret( array $ranges, int $score ): ?string {
		foreach ( $ranges as $r ) {
			if ( $score >= (int) $r['min'] && $score <= (int) $r['max'] ) {
				return (string) $r['interpretation'];
			}
		}
		return null;
	}

	/**
	 * Sum the chosen options' scores. $answers = [ questionIndex => optionIndex ].
	 *
	 * @param array<int, array<string, mixed>> $questions
	 * @param array<int|string, mixed>         $answers
	 */
	public static function score_answers( array $questions, array $answers ): int {
		$score = 0;
		foreach ( $questions as $q ) {
			$idx    = $q['index'];
			$choice = $answers[ (string) $idx ] ?? ( $answers[ $idx ] ?? null );
			if ( null !== $choice && isset( $q['options'][ (int) $choice ] ) ) {
				$score += (int) $q['options'][ (int) $choice ]['score'];
			}
		}
		return $score;
	}

	/**
	 * @param array<int, array<string, mixed>> $questions
	 */
	public static function max_score( array $questions ): int {
		$max = 0;
		foreach ( $questions as $q ) {
			$best = 0;
			foreach ( $q['options'] as $o ) {
				$best = max( $best, (int) $o['score'] );
			}
			$max += $best;
		}
		return $max;
	}

	/**
	 * @param array<int, array<string, mixed>> $questions
	 */
	public static function build_question_lines( array $questions ): string {
		$lines = [];
		foreach ( $questions as $q ) {
			$opts = [];
			foreach ( (array) ( $q['options'] ?? [] ) as $o ) {
				$opts[] = trim( (string) ( $o['text'] ?? '' ) ) . '=' . (int) ( $o['score'] ?? 0 );
			}
			$lines[] = trim( (string) ( $q['text'] ?? '' ) ) . ' | ' . implode( ' , ', $opts );
		}
		return implode( "\n", $lines );
	}

	/**
	 * @param array<int, array<string, mixed>> $ranges
	 */
	public static function build_range_lines( array $ranges ): string {
		$lines = [];
		foreach ( $ranges as $r ) {
			$lines[] = (int) ( $r['minScore'] ?? ( $r['min'] ?? 0 ) ) . ' | ' . (int) ( $r['maxScore'] ?? ( $r['max'] ?? 0 ) ) . ' | ' . trim( (string) ( $r['interpretation'] ?? '' ) );
		}
		return implode( "\n", $lines );
	}

	// ---- model helpers ------------------------------------------------------

	/**
	 * @return array<int, array<string, mixed>>
	 */
	public static function questions( int $test_id ): array {
		return self::parse_questions( (string) get_post_meta( $test_id, 'cb_questions', true ) );
	}

	/**
	 * @return array<int, array<string, mixed>>
	 */
	public static function ranges( int $test_id ): array {
		return self::parse_ranges( (string) get_post_meta( $test_id, 'cb_ranges', true ) );
	}

	public static function result_mode( int $test_id ): string {
		$m = strtoupper( (string) get_post_meta( $test_id, 'cb_result_mode', true ) );
		return 'COUNSELOR' === $m ? 'COUNSELOR' : 'AUTO';
	}

	public static function product_slug( int $test_id ): ?string {
		$slug = get_post_meta( $test_id, 'cb_product_slug', true );
		return $slug ? (string) $slug : null;
	}

	public static function product_id( int $test_id ): ?int {
		$slug = self::product_slug( $test_id );
		if ( ! $slug ) {
			return null;
		}
		$p = get_page_by_path( $slug, OBJECT, 'product' );
		return $p ? (int) $p->ID : null;
	}

	public static function is_owned( int $test_id, int $uid ): bool {
		$slug = self::product_slug( $test_id );
		if ( ! $slug ) {
			return true;
		}
		if ( $uid < 1 || ! function_exists( 'wc_customer_bought_product' ) ) {
			return false;
		}
		$pid = self::product_id( $test_id );
		return $pid ? (bool) wc_customer_bought_product( '', $uid, $pid ) : false;
	}

	/**
	 * Questions with option scores stripped (client never sees scores).
	 *
	 * @return array<int, array<string, mixed>>
	 */
	public static function public_questions( int $test_id ): array {
		$out = [];
		foreach ( self::questions( $test_id ) as $q ) {
			$opts = [];
			foreach ( $q['options'] as $o ) {
				$opts[] = [ 'text' => $o['text'], 'score' => null ];
			}
			$out[] = [ 'index' => $q['index'], 'text' => $q['text'], 'options' => $opts ];
		}
		return $out;
	}

	// ---- catalog & detail ---------------------------------------------------

	/**
	 * @return array<int, array<string, mixed>>
	 */
	public static function list_tests( int $uid = 0, bool $include_drafts = false ): array {
		$posts = get_posts( [
			'post_type'   => 'cb_psychtest',
			'post_status' => $include_drafts ? [ 'publish', 'draft' ] : 'publish',
			'numberposts' => 100,
		] );
		$out = [];
		foreach ( $posts as $t ) {
			if ( $t instanceof \WP_Post ) {
				$out[] = self::summary_dto( $t, $uid );
			}
		}
		return $out;
	}

	/**
	 * @param int|string $id_or_slug
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function get_test_detail( $id_or_slug, int $uid = 0, array $statuses = [ 'publish' ] ) {
		if ( is_int( $id_or_slug ) || ctype_digit( (string) $id_or_slug ) ) {
			$t = get_post( (int) $id_or_slug );
			if ( $t && 'cb_psychtest' === $t->post_type && in_array( $t->post_status, $statuses, true ) ) {
				return self::detail_dto( $t, $uid );
			}
			return new \WP_Error( 'NOT_FOUND', 'تست یافت نشد', [ 'status' => 404 ] );
		}
		$t = get_page_by_path( sanitize_title( (string) $id_or_slug ), OBJECT, 'cb_psychtest' );
		if ( ! $t || ! in_array( $t->post_status, $statuses, true ) ) {
			return new \WP_Error( 'NOT_FOUND', 'تست یافت نشد', [ 'status' => 404 ] );
		}
		return self::detail_dto( $t, $uid );
	}

	// ---- private attempts & results (user meta cb_psych_attempts) ----------

	/**
	 * @return array<int, array<string, mixed>>
	 */
	public static function raw_attempts( int $uid ): array {
		$v = get_user_meta( $uid, 'cb_psych_attempts', true );
		return is_array( $v ) ? array_values( $v ) : [];
	}

	/**
	 * @param array<int, array<string, mixed>> $list
	 */
	private static function save_attempts( int $uid, array $list ): void {
		update_user_meta( $uid, 'cb_psych_attempts', array_values( $list ) );
	}

	private static function next_attempt_id( int $uid ): int {
		$n = (int) get_user_meta( $uid, 'cb_psych_attempt_seq', true ) + 1;
		update_user_meta( $uid, 'cb_psych_attempt_seq', $n );
		return $n;
	}

	/**
	 * Ensures an attempt row exists for each owned test and returns user's attempts.
	 *
	 * @return array<int, array<string, mixed>>
	 */
	public static function my_tests( int $uid ): array {
		if ( $uid < 1 ) {
			return [];
		}
		$attempts = self::raw_attempts( $uid );
		$posts    = get_posts( [ 'post_type' => 'cb_psychtest', 'post_status' => 'publish', 'numberposts' => 100 ] );
		$known    = array_map( static function ( $a ) { return (int) ( $a['testId'] ?? 0 ); }, $attempts );
		foreach ( $posts as $t ) {
			$id = (int) $t->ID;
			if ( self::is_owned( $id, $uid ) && ! in_array( $id, $known, true ) ) {
				$attempts[] = [
					'id'             => self::next_attempt_id( $uid ),
					'testId'         => $id,
					'testTitle'      => get_the_title( $t ),
					'status'         => 'IN_PROGRESS',
					'resultMode'     => self::result_mode( $id ),
					'totalScore'     => null,
					'interpretation' => null,
					'completedAt'    => null,
				];
			}
		}
		self::save_attempts( $uid, $attempts );
		return array_map( [ self::class, 'attempt_dto' ], $attempts );
	}

	/**
	 * Returns questions for a user-owned attempt (IDOR-safe: 404 if not owned by $uid).
	 *
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function attempt_questions( int $attempt_id, int $uid ) {
		if ( ! self::is_enabled() ) {
			return new \WP_Error( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', [ 'status' => 403 ] );
		}
		foreach ( self::raw_attempts( $uid ) as $a ) {
			if ( (int) ( $a['id'] ?? 0 ) === $attempt_id ) {
				$t = get_post( (int) ( $a['testId'] ?? 0 ) );
				if ( ! $t || 'cb_psychtest' !== $t->post_type ) {
					return new \WP_Error( 'NOT_FOUND', 'تست یافت نشد', [ 'status' => 404 ] );
				}
				return self::detail_dto( $t, $uid );
			}
		}
		return new \WP_Error( 'NOT_FOUND', 'آزمون یافت نشد', [ 'status' => 404 ] );
	}

	/**
	 * Returns private result for a user-owned attempt (IDOR-safe: 404 if not owned by $uid).
	 *
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function get_private_result( int $attempt_id, int $uid ) {
		if ( ! self::is_enabled() ) {
			return new \WP_Error( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', [ 'status' => 403 ] );
		}
		foreach ( self::raw_attempts( $uid ) as $a ) {
			if ( (int) ( $a['id'] ?? 0 ) === $attempt_id ) {
				return self::attempt_dto( $a );
			}
		}
		return new \WP_Error( 'NOT_FOUND', 'نتیجه آزمون یافت نشد', [ 'status' => 404 ] );
	}

	/**
	 * Submits answers for an existing user attempt ID (canonical /api/my-psych-tests/{id}/submit).
	 *
	 * @param array<int|string, mixed> $answers
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function submit_attempt( int $attempt_id, int $uid, array $answers ) {
		if ( ! self::is_enabled() ) {
			return new \WP_Error( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', [ 'status' => 403 ] );
		}
		$attempts = self::raw_attempts( $uid );
		$idx      = null;
		foreach ( $attempts as $i => $a ) {
			if ( (int) ( $a['id'] ?? 0 ) === $attempt_id ) {
				$idx = $i;
				break;
			}
		}
		if ( null === $idx ) {
			return new \WP_Error( 'NOT_FOUND', 'آزمون یافت نشد', [ 'status' => 404 ] );
		}

		$test_id   = (int) $attempts[ $idx ]['testId'];
		$questions = self::questions( $test_id );
		$score     = self::score_answers( $questions, $answers );
		$mode      = self::result_mode( $test_id );

		if ( 'AUTO' === $mode ) {
			$attempts[ $idx ]['status']         = 'COMPLETED';
			$attempts[ $idx ]['interpretation'] = self::interpret( self::ranges( $test_id ), $score );
		} else {
			$attempts[ $idx ]['status']         = 'AWAITING_INTERPRETATION';
			$attempts[ $idx ]['interpretation'] = null;
		}
		$attempts[ $idx ]['completedAt'] = gmdate( 'c' );
		$attempts[ $idx ]['totalScore']  = $score;
		self::save_attempts( $uid, $attempts );

		return self::attempt_dto( $attempts[ $idx ] );
	}

	/**
	 * Direct test submit (used by Theme psychtest.js: /psych-tests/{testId}/submit).
	 * Creates or updates an attempt for the authenticated owner and scores server-side.
	 *
	 * @param array<int|string, mixed> $answers
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function submit_for_test( int $test_id, int $uid, array $answers ) {
		if ( ! self::is_enabled() ) {
			return new \WP_Error( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', [ 'status' => 403 ] );
		}
		if ( $uid < 1 ) {
			return new \WP_Error( 'UNAUTHORIZED', 'ابتدا وارد شوید', [ 'status' => 401 ] );
		}
		$t = get_post( $test_id );
		if ( ! $t || 'cb_psychtest' !== $t->post_type || 'publish' !== $t->post_status ) {
			return new \WP_Error( 'NOT_FOUND', 'تست یافت نشد', [ 'status' => 404 ] );
		}
		if ( ! self::is_owned( $test_id, $uid ) ) {
			return new \WP_Error( 'FORBIDDEN', 'ابتدا این تست را خریداری کنید', [ 'status' => 403 ] );
		}

		$attempts = self::raw_attempts( $uid );
		$idx      = null;
		foreach ( $attempts as $i => $a ) {
			if ( (int) ( $a['testId'] ?? 0 ) === $test_id ) {
				$idx = $i;
				break;
			}
		}
		if ( null === $idx ) {
			$attempts[] = [
				'id'             => self::next_attempt_id( $uid ),
				'testId'         => $test_id,
				'testTitle'      => get_the_title( $t ),
				'status'         => 'IN_PROGRESS',
				'resultMode'     => self::result_mode( $test_id ),
				'totalScore'     => null,
				'interpretation' => null,
				'completedAt'    => null,
			];
			self::save_attempts( $uid, $attempts );
		}
		$attempt_id = (int) ( $attempts[ $idx ?? ( count( $attempts ) - 1 ) ]['id'] );
		$dto        = self::submit_attempt( $attempt_id, $uid, $answers );
		if ( is_wp_error( $dto ) ) {
			return $dto;
		}
		$dto['score']    = $dto['totalScore'];
		$dto['maxScore'] = self::max_score( self::questions( $test_id ) );
		return $dto;
	}

	// ---- admin management ---------------------------------------------------

	/**
	 * @param array<string, mixed> $params
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function create_test( array $params ) {
		$id = wp_insert_post( [
			'post_type'    => 'cb_psychtest',
			'post_status'  => ( isset( $params['isPublished'] ) && ! $params['isPublished'] ) ? 'draft' : 'publish',
			'post_title'   => sanitize_text_field( (string) ( $params['title'] ?? 'تست' ) ),
			'post_name'    => sanitize_title( (string) ( $params['slug'] ?? '' ) ),
			'post_content' => wp_kses_post( (string) ( $params['description'] ?? '' ) ),
		], true );
		if ( is_wp_error( $id ) ) {
			return new \WP_Error( 'CREATE_FAILED', 'ساخت تست ناموفق بود', [ 'status' => 400 ] );
		}
		self::apply_test_meta( (int) $id, $params );
		return [ 'id' => (int) $id ];
	}

	/**
	 * @param array<string, mixed> $params
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function update_test( int $id, array $params ) {
		if ( get_post_type( $id ) !== 'cb_psychtest' ) {
			return new \WP_Error( 'NOT_FOUND', 'تست یافت نشد', [ 'status' => 404 ] );
		}
		$upd = [ 'ID' => $id ];
		if ( isset( $params['title'] ) ) {
			$upd['post_title'] = sanitize_text_field( (string) $params['title'] );
		}
		if ( isset( $params['description'] ) ) {
			$upd['post_content'] = wp_kses_post( (string) $params['description'] );
		}
		if ( isset( $params['isPublished'] ) ) {
			$upd['post_status'] = $params['isPublished'] ? 'publish' : 'draft';
		}
		wp_update_post( $upd );
		self::apply_test_meta( $id, $params );
		return [ 'id' => $id ];
	}

	/**
	 * @return true|\WP_Error
	 */
	public static function delete_test( int $id ) {
		if ( get_post_type( $id ) !== 'cb_psychtest' ) {
			return new \WP_Error( 'NOT_FOUND', 'تست یافت نشد', [ 'status' => 404 ] );
		}
		wp_delete_post( $id, true );
		return true;
	}

	/**
	 * @return array<int, array<string, mixed>>
	 */
	public static function pending_interpretations(): array {
		$users = get_users( [ 'meta_key' => 'cb_psych_attempts', 'number' => 500 ] );
		$out   = [];
		foreach ( $users as $u ) {
			foreach ( self::raw_attempts( (int) $u->ID ) as $a ) {
				if ( ( $a['status'] ?? '' ) === 'AWAITING_INTERPRETATION' ) {
					$a['userId']   = (int) $u->ID;
					$a['userName'] = $u->display_name;
					$out[]         = $a;
				}
			}
		}
		return $out;
	}

	/**
	 * @return true|\WP_Error
	 */
	public static function interpret_attempt( int $attempt_id, string $text ) {
		$text  = sanitize_textarea_field( $text );
		$users = get_users( [ 'meta_key' => 'cb_psych_attempts', 'number' => 500 ] );
		foreach ( $users as $u ) {
			$list    = self::raw_attempts( (int) $u->ID );
			$changed = false;
			foreach ( $list as &$a ) {
				if ( (int) ( $a['id'] ?? 0 ) === $attempt_id ) {
					$a['interpretation'] = $text;
					$a['status']         = 'COMPLETED';
					$changed             = true;
				}
			}
			unset( $a );
			if ( $changed ) {
				self::save_attempts( (int) $u->ID, $list );
				return true;
			}
		}
		return new \WP_Error( 'NOT_FOUND', 'آزمون یافت نشد', [ 'status' => 404 ] );
	}

	/**
	 * @param array<string, mixed> $b
	 */
	private static function apply_test_meta( int $id, array $b ): void {
		if ( isset( $b['resultMode'] ) ) {
			update_post_meta( $id, 'cb_result_mode', strtoupper( (string) $b['resultMode'] ) === 'COUNSELOR' ? 'COUNSELOR' : 'AUTO' );
		}
		if ( isset( $b['price'] ) ) {
			update_post_meta( $id, 'cb_price', (float) $b['price'] );
		}
		if ( isset( $b['discountedPrice'] ) ) {
			update_post_meta( $id, 'cb_discounted_price', null !== $b['discountedPrice'] ? (float) $b['discountedPrice'] : '' );
		}
		if ( isset( $b['productId'] ) ) {
			$p = get_post( (int) $b['productId'] );
			update_post_meta( $id, 'cb_product_slug', $p ? $p->post_name : '' );
		}
		if ( isset( $b['productSlug'] ) ) {
			update_post_meta( $id, 'cb_product_slug', sanitize_title( (string) $b['productSlug'] ) );
		}
		if ( isset( $b['questions'] ) && is_array( $b['questions'] ) ) {
			update_post_meta( $id, 'cb_questions', self::build_question_lines( $b['questions'] ) );
		}
		if ( isset( $b['ranges'] ) && is_array( $b['ranges'] ) ) {
			update_post_meta( $id, 'cb_ranges', self::build_range_lines( $b['ranges'] ) );
		}
	}

	/**
	 * @return array<string, mixed>
	 */
	private static function summary_dto( \WP_Post $t, int $uid ): array {
		$id = (int) $t->ID;
		$d  = get_post_meta( $id, 'cb_discounted_price', true );
		return [
			'id'              => $id,
			'title'           => get_the_title( $t ),
			'slug'            => $t->post_name,
			'description'     => $t->post_excerpt ?: null,
			'price'           => (float) get_post_meta( $id, 'cb_price', true ),
			'discountedPrice' => ( '' !== $d && null !== $d ) ? (float) $d : null,
			'resultMode'      => self::result_mode( $id ),
			'questionCount'   => count( self::questions( $id ) ),
			'owned'           => self::is_owned( $id, $uid ),
			'productId'       => self::product_id( $id ),
			'productSlug'     => self::product_slug( $id ),
		];
	}

	/**
	 * @return array<string, mixed>
	 */
	private static function detail_dto( \WP_Post $t, int $uid ): array {
		$id = (int) $t->ID;
		$d  = get_post_meta( $id, 'cb_discounted_price', true );
		return [
			'id'              => $id,
			'title'           => get_the_title( $t ),
			'slug'            => $t->post_name,
			'description'     => $t->post_content ? wp_strip_all_tags( $t->post_content ) : null,
			'price'           => (float) get_post_meta( $id, 'cb_price', true ),
			'discountedPrice' => ( '' !== $d && null !== $d ) ? (float) $d : null,
			'resultMode'      => self::result_mode( $id ),
			'questions'       => self::public_questions( $id ),
			'owned'           => self::is_owned( $id, $uid ),
			'productId'       => self::product_id( $id ),
		];
	}

	/**
	 * @param array<string, mixed> $a
	 * @return array<string, mixed>
	 */
	private static function attempt_dto( array $a ): array {
		return [
			'id'             => (int) ( $a['id'] ?? 0 ),
			'testId'         => (int) ( $a['testId'] ?? 0 ),
			'testTitle'      => (string) ( $a['testTitle'] ?? '' ),
			'status'         => (string) ( $a['status'] ?? 'IN_PROGRESS' ),
			'resultMode'     => (string) ( $a['resultMode'] ?? 'AUTO' ),
			'totalScore'     => $a['totalScore'] ?? null,
			'interpretation' => $a['interpretation'] ?? null,
			'completedAt'    => $a['completedAt'] ?? null,
		];
	}
}

}
