<?php
/**
 * Psychological test endpoints — cb_psychtest CPT with line-based questions and
 * score ranges (theme-aligned). Buying the linked product grants ownership.
 * Scoring + interpretation run server-side so option scores never reach the
 * client (options are always returned with score = null).
 *
 * Attempts (user tests) live in user meta cb_psych_attempts.
 *   Questions:  «text | label=score , label=score , ...»
 *   Ranges:     «min | max | interpretation»
 *   resultMode: AUTO (interpret immediately) | COUNSELOR (await interpretation).
 *
 * Public:  api/psych-tests, api/psych-tests/{slug}
 * Auth:    api/my-psych-tests, api/my-psych-tests/{userTestId}/questions|submit
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Psychtest_Controller {

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$login = array( 'CB_Plugin', 'require_login' );

		register_rest_route( $ns, '/api/psych-tests', array( 'methods' => 'GET', 'callback' => array( $this, 'list_tests' ), 'permission_callback' => '__return_true' ) );
		register_rest_route( $ns, '/api/psych-tests/(?P<slug>[a-zA-Z0-9\-_%]+)', array( 'methods' => 'GET', 'callback' => array( $this, 'test_detail' ), 'permission_callback' => '__return_true' ) );

		register_rest_route( $ns, '/api/my-psych-tests', array( 'methods' => 'GET', 'callback' => array( $this, 'my_tests' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/my-psych-tests/(?P<id>\d+)/questions', array( 'methods' => 'GET', 'callback' => array( $this, 'attempt_questions' ), 'permission_callback' => $login ) );
		register_rest_route( $ns, '/api/my-psych-tests/(?P<id>\d+)/submit', array( 'methods' => 'POST', 'callback' => array( $this, 'submit' ), 'permission_callback' => $login ) );
	}

	// ---- parsing (static, testable) ----------------------------------------

	/** «text | label=score , label=score» -> [ ['text'=>, 'options'=>[ ['text'=>,'score'=>], ... ]], ... ] */
	public static function parse_questions( string $raw ): array {
		$lines = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $raw ) ) );
		$out   = array();
		$i     = 0;
		foreach ( $lines as $line ) {
			$parts = explode( '|', $line, 2 );
			$text  = trim( $parts[0] );
			if ( $text === '' ) {
				continue;
			}
			$options = array();
			if ( isset( $parts[1] ) ) {
				foreach ( preg_split( '/[,،]/u', $parts[1] ) as $opt ) {
					$opt = trim( $opt );
					if ( $opt === '' ) {
						continue;
					}
					$pos = strrpos( $opt, '=' );
					if ( $pos === false ) {
						$options[] = array( 'text' => $opt, 'score' => 0 );
					} else {
						$options[] = array( 'text' => trim( substr( $opt, 0, $pos ) ), 'score' => (int) trim( substr( $opt, $pos + 1 ) ) );
					}
				}
			}
			$out[] = array( 'index' => $i, 'text' => $text, 'options' => $options );
			$i++;
		}
		return $out;
	}

	/** «min | max | interpretation» -> [ ['min'=>,'max'=>,'interpretation'=>], ... ] */
	public static function parse_ranges( string $raw ): array {
		$lines = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $raw ) ) );
		$out   = array();
		foreach ( $lines as $line ) {
			$p = array_map( 'trim', explode( '|', $line ) );
			if ( count( $p ) < 3 ) {
				continue;
			}
			$out[] = array( 'min' => (int) $p[0], 'max' => (int) $p[1], 'interpretation' => $p[2] );
		}
		return $out;
	}

	/** First range containing the score, or null. */
	public static function interpret( array $ranges, int $score ): ?string {
		foreach ( $ranges as $r ) {
			if ( $score >= (int) $r['min'] && $score <= (int) $r['max'] ) {
				return (string) $r['interpretation'];
			}
		}
		return null;
	}

	/** Sum the chosen options' scores. $answers = [ questionIndex => optionIndex ]. */
	public static function score_answers( array $questions, array $answers ): int {
		$score = 0;
		foreach ( $questions as $q ) {
			$idx    = $q['index'];
			$choice = $answers[ (string) $idx ] ?? ( $answers[ $idx ] ?? null );
			if ( $choice !== null && isset( $q['options'][ (int) $choice ] ) ) {
				$score += (int) $q['options'][ (int) $choice ]['score'];
			}
		}
		return $score;
	}

	// ---- model helpers ------------------------------------------------------

	private function questions( int $test_id ): array {
		return self::parse_questions( (string) get_post_meta( $test_id, 'cb_questions', true ) );
	}

	private function ranges( int $test_id ): array {
		return self::parse_ranges( (string) get_post_meta( $test_id, 'cb_ranges', true ) );
	}

	private function result_mode( int $test_id ): string {
		$m = strtoupper( (string) get_post_meta( $test_id, 'cb_result_mode', true ) );
		return $m === 'COUNSELOR' ? 'COUNSELOR' : 'AUTO';
	}

	private function product_slug( int $test_id ) {
		return get_post_meta( $test_id, 'cb_product_slug', true ) ?: null;
	}

	private function product_id( int $test_id ): ?int {
		$slug = $this->product_slug( $test_id );
		if ( ! $slug ) {
			return null;
		}
		$p = get_page_by_path( $slug, OBJECT, 'product' );
		return $p ? (int) $p->ID : null;
	}

	private function owned( int $test_id, int $uid ): bool {
		$slug = $this->product_slug( $test_id );
		if ( ! $slug ) {
			return true; // free test
		}
		if ( ! $uid || ! function_exists( 'wc_customer_bought_product' ) ) {
			return false;
		}
		$pid = $this->product_id( $test_id );
		return $pid ? wc_customer_bought_product( '', $uid, $pid ) : false;
	}

	/** Questions with option scores stripped (client never sees scores). */
	private function public_questions( int $test_id ): array {
		$out = array();
		foreach ( $this->questions( $test_id ) as $q ) {
			$opts = array();
			foreach ( $q['options'] as $o ) {
				$opts[] = array( 'text' => $o['text'], 'score' => null );
			}
			$out[] = array( 'index' => $q['index'], 'text' => $q['text'], 'options' => $opts );
		}
		return $out;
	}

	private function summary_dto( WP_Post $t, int $uid ): array {
		$id = (int) $t->ID;
		$d  = get_post_meta( $id, 'cb_discounted_price', true );
		return array(
			'id'              => $id,
			'title'           => get_the_title( $t ),
			'slug'            => $t->post_name,
			'description'     => $t->post_excerpt ?: null,
			'price'           => (float) get_post_meta( $id, 'cb_price', true ),
			'discountedPrice' => ( $d !== '' && $d !== null ) ? (float) $d : null,
			'resultMode'      => $this->result_mode( $id ),
			'questionCount'   => count( $this->questions( $id ) ),
			'owned'           => $this->owned( $id, $uid ),
			'productId'       => $this->product_id( $id ),
			'productSlug'     => $this->product_slug( $id ),
		);
	}

	private function detail_dto( WP_Post $t, int $uid ): array {
		$id = (int) $t->ID;
		$d  = get_post_meta( $id, 'cb_discounted_price', true );
		return array(
			'id'              => $id,
			'title'           => get_the_title( $t ),
			'slug'            => $t->post_name,
			'description'     => $t->post_content ? wp_strip_all_tags( $t->post_content ) : null,
			'price'           => (float) get_post_meta( $id, 'cb_price', true ),
			'discountedPrice' => ( $d !== '' && $d !== null ) ? (float) $d : null,
			'resultMode'      => $this->result_mode( $id ),
			'questions'       => $this->public_questions( $id ),
			'owned'           => $this->owned( $id, $uid ),
			'productId'       => $this->product_id( $id ),
		);
	}

	// ---- attempts (user meta cb_psych_attempts) -----------------------------

	private function attempts( int $uid ): array {
		$v = get_user_meta( $uid, 'cb_psych_attempts', true );
		return is_array( $v ) ? array_values( $v ) : array();
	}

	private function save_attempts( int $uid, array $list ): void {
		update_user_meta( $uid, 'cb_psych_attempts', array_values( $list ) );
	}

	private function attempt_dto( array $a ): array {
		return array(
			'id'             => (int) $a['id'],
			'testId'         => (int) $a['testId'],
			'testTitle'      => (string) $a['testTitle'],
			'status'         => (string) $a['status'],
			'resultMode'     => (string) $a['resultMode'],
			'totalScore'     => isset( $a['totalScore'] ) ? $a['totalScore'] : null,
			'interpretation' => $a['interpretation'] ?? null,
			'completedAt'    => $a['completedAt'] ?? null,
		);
	}

	// ---- endpoints ----------------------------------------------------------

	public function list_tests(): WP_REST_Response {
		$uid   = get_current_user_id();
		$posts = get_posts( array( 'post_type' => 'cb_psychtest', 'post_status' => 'publish', 'numberposts' => 100 ) );
		$out   = array();
		foreach ( $posts as $t ) {
			$out[] = $this->summary_dto( $t, $uid );
		}
		return cb_response( $out );
	}

	public function test_detail( WP_REST_Request $request ): WP_REST_Response {
		$t = get_page_by_path( sanitize_title( $request['slug'] ), OBJECT, 'cb_psychtest' );
		if ( ! $t ) {
			return cb_error( 'تست یافت نشد', 404, 'NOT_FOUND', 'api/psych-tests' );
		}
		return cb_response( $this->detail_dto( $t, get_current_user_id() ) );
	}

	public function my_tests(): WP_REST_Response {
		$uid      = get_current_user_id();
		$attempts = $this->attempts( $uid );
		// Ensure an attempt row exists for each owned test.
		$posts = get_posts( array( 'post_type' => 'cb_psychtest', 'post_status' => 'publish', 'numberposts' => 100 ) );
		$known = array_map( function ( $a ) { return (int) $a['testId']; }, $attempts );
		foreach ( $posts as $t ) {
			$id = (int) $t->ID;
			if ( $this->owned( $id, $uid ) && ! in_array( $id, $known, true ) ) {
				$attempts[] = array(
					'id'         => $this->next_attempt_id( $uid ),
					'testId'     => $id,
					'testTitle'  => get_the_title( $t ),
					'status'     => 'IN_PROGRESS',
					'resultMode' => $this->result_mode( $id ),
					'totalScore' => null,
					'interpretation' => null,
					'completedAt' => null,
				);
			}
		}
		$this->save_attempts( $uid, $attempts );
		return cb_response( array_map( array( $this, 'attempt_dto' ), $attempts ) );
	}

	private function next_attempt_id( int $uid ): int {
		$n = (int) get_user_meta( $uid, 'cb_psych_attempt_seq', true ) + 1;
		update_user_meta( $uid, 'cb_psych_attempt_seq', $n );
		return $n;
	}

	private function find_attempt( int $uid, int $attempt_id ): ?array {
		foreach ( $this->attempts( $uid ) as $a ) {
			if ( (int) $a['id'] === $attempt_id ) {
				return $a;
			}
		}
		return null;
	}

	public function attempt_questions( WP_REST_Request $request ): WP_REST_Response {
		$uid = get_current_user_id();
		$a   = $this->find_attempt( $uid, (int) $request['id'] );
		if ( ! $a ) {
			return cb_error( 'آزمون یافت نشد', 404, 'NOT_FOUND', 'api/my-psych-tests' );
		}
		$t = get_post( (int) $a['testId'] );
		if ( ! $t ) {
			return cb_error( 'تست یافت نشد', 404, 'NOT_FOUND', 'api/my-psych-tests' );
		}
		return cb_response( $this->detail_dto( $t, $uid ) );
	}

	public function submit( WP_REST_Request $request ): WP_REST_Response {
		$uid       = get_current_user_id();
		$attempt_id = (int) $request['id'];
		$attempts  = $this->attempts( $uid );
		$idx       = null;
		foreach ( $attempts as $i => $a ) {
			if ( (int) $a['id'] === $attempt_id ) {
				$idx = $i;
				break;
			}
		}
		if ( $idx === null ) {
			return cb_error( 'آزمون یافت نشد', 404, 'NOT_FOUND', 'api/my-psych-tests' );
		}
		$test_id   = (int) $attempts[ $idx ]['testId'];
		$answers   = (array) ( $request->get_json_params()['answers'] ?? array() );
		$questions = $this->questions( $test_id );
		$score     = self::score_answers( $questions, $answers );
		$mode      = $this->result_mode( $test_id );

		if ( $mode === 'AUTO' ) {
			$attempts[ $idx ]['status']         = 'COMPLETED';
			$attempts[ $idx ]['interpretation'] = self::interpret( $this->ranges( $test_id ), $score );
			$attempts[ $idx ]['completedAt']    = gmdate( 'c' );
		} else {
			$attempts[ $idx ]['status']         = 'AWAITING_INTERPRETATION';
			$attempts[ $idx ]['interpretation'] = null;
			$attempts[ $idx ]['completedAt']    = gmdate( 'c' );
		}
		$attempts[ $idx ]['totalScore'] = $score;
		$this->save_attempts( $uid, $attempts );
		return cb_response( $this->attempt_dto( $attempts[ $idx ] ) );
	}
}
