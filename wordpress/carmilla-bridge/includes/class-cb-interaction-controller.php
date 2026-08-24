<?php
/**
 * Product reviews + Q&A, backed by WordPress comments on the product post.
 *   Reviews  -> comment_type 'review' (+ rating meta), like WooCommerce reviews.
 *   Q&A      -> comment_type 'cb_qna'.
 * Replies are child comments (comment_parent). Admin/shop-manager authors are
 * flagged isSupport. cb_qna comments are hidden from the normal comment feed.
 *
 *   GET    api/reviews/product/{productId}   -> List<ReviewResponse>
 *   POST   api/reviews                       -> ReviewResponse   (CreateReviewRequestDto)
 *   PUT    api/reviews/{reviewId}            -> ReviewResponse   (UpdateReviewRequest)
 *   DELETE api/reviews/{reviewId}            -> 204
 *   POST   api/reviews/{reviewId}/helpful    -> ReviewResponse
 *   GET    api/questions/product/{productId} -> List<QuestionResponse>
 *   POST   api/questions                     -> QuestionResponse (CreateQuestionRequestDto)
 *   PUT    api/questions/{questionId}        -> QuestionResponse (UpdateQuestionRequest)
 *   DELETE api/questions/{questionId}        -> 204
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Interaction_Controller {

	const T_REVIEW = 'review';
	const T_QNA    = 'cb_qna';

	public function register_routes(): void {
		$ns    = CB_REST_NAMESPACE;
		$login = array( 'CB_Plugin', 'require_login' );

		register_rest_route( $ns, '/api/reviews/product/(?P<id>\d+)', array(
			'methods' => 'GET', 'callback' => array( $this, 'list_reviews' ), 'permission_callback' => '__return_true',
		) );
		register_rest_route( $ns, '/api/reviews', array(
			'methods' => 'POST', 'callback' => array( $this, 'create_review' ), 'permission_callback' => $login,
		) );
		register_rest_route( $ns, '/api/reviews/(?P<id>\d+)', array(
			array( 'methods' => 'PUT', 'callback' => array( $this, 'update_review' ), 'permission_callback' => $login ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_review' ), 'permission_callback' => $login ),
		) );
		register_rest_route( $ns, '/api/reviews/(?P<id>\d+)/helpful', array(
			'methods' => 'POST', 'callback' => array( $this, 'helpful' ), 'permission_callback' => $login,
		) );

		register_rest_route( $ns, '/api/questions/product/(?P<id>\d+)', array(
			'methods' => 'GET', 'callback' => array( $this, 'list_questions' ), 'permission_callback' => '__return_true',
		) );
		register_rest_route( $ns, '/api/questions', array(
			'methods' => 'POST', 'callback' => array( $this, 'create_question' ), 'permission_callback' => $login,
		) );
		register_rest_route( $ns, '/api/questions/(?P<id>\d+)', array(
			array( 'methods' => 'PUT', 'callback' => array( $this, 'update_question' ), 'permission_callback' => $login ),
			array( 'methods' => 'DELETE', 'callback' => array( $this, 'delete_question' ), 'permission_callback' => $login ),
		) );
	}

	/** Hide cb_qna comments from the normal WordPress comment feed. */
	public static function hide_qna_clauses( array $clauses ): array {
		global $wpdb;
		if ( strpos( $clauses['where'], 'comment_type' ) === false ) {
			$clauses['where'] .= $wpdb->prepare( " AND {$wpdb->comments}.comment_type != %s", self::T_QNA );
		}
		return $clauses;
	}

	// ---- reviews ------------------------------------------------------------

	public function list_reviews( WP_REST_Request $request ): WP_REST_Response {
		return cb_response( $this->tree( (int) $request['id'], self::T_REVIEW, true ) );
	}

	public function create_review( WP_REST_Request $request ): WP_REST_Response {
		$body       = $request->get_json_params();
		$product_id = (int) ( $body['productId'] ?? 0 );
		if ( ! get_post( $product_id ) ) {
			return cb_error( 'محصول یافت نشد', 404, 'NOT_FOUND', 'api/reviews' );
		}
		$id = $this->insert_comment( $product_id, self::T_REVIEW, (string) ( $body['comment'] ?? '' ), (int) ( $body['parentId'] ?? 0 ) );
		if ( ! $id ) {
			return cb_error( 'ثبت نظر ناموفق بود', 400, 'CREATE_FAILED', 'api/reviews' );
		}
		if ( isset( $body['rating'] ) && $body['rating'] !== null ) {
			add_comment_meta( $id, 'rating', max( 1, min( 5, (int) $body['rating'] ) ) );
		}
		if ( function_exists( 'wc_customer_bought_product' ) ) {
			$user = wp_get_current_user();
			if ( wc_customer_bought_product( $user->user_email, $user->ID, $product_id ) ) {
				add_comment_meta( $id, 'verified', 1 );
			}
		}
		return cb_response( $this->review_dto( get_comment( $id ), true ), 201 );
	}

	public function update_review( WP_REST_Request $request ): WP_REST_Response {
		$comment = $this->owned_comment( (int) $request['id'] );
		if ( ! $comment ) {
			return cb_error( 'نظر یافت نشد', 404, 'NOT_FOUND', 'api/reviews' );
		}
		$body = $request->get_json_params();
		wp_update_comment( array( 'comment_ID' => $comment->comment_ID, 'comment_content' => sanitize_textarea_field( (string) ( $body['comment'] ?? $comment->comment_content ) ) ) );
		if ( array_key_exists( 'rating', $body ) && $body['rating'] !== null ) {
			update_comment_meta( $comment->comment_ID, 'rating', max( 1, min( 5, (int) $body['rating'] ) ) );
		}
		return cb_response( $this->review_dto( get_comment( $comment->comment_ID ), true ) );
	}

	public function delete_review( WP_REST_Request $request ): WP_REST_Response {
		return $this->delete_comment( (int) $request['id'], 'api/reviews' );
	}

	public function helpful( WP_REST_Request $request ): WP_REST_Response {
		$comment = get_comment( (int) $request['id'] );
		if ( ! $comment ) {
			return cb_error( 'نظر یافت نشد', 404, 'NOT_FOUND', 'api/reviews' );
		}
		$count = (int) get_comment_meta( $comment->comment_ID, 'cb_helpful', true ) + 1;
		update_comment_meta( $comment->comment_ID, 'cb_helpful', $count );
		return cb_response( $this->review_dto( $comment, true ) );
	}

	// ---- questions ----------------------------------------------------------

	public function list_questions( WP_REST_Request $request ): WP_REST_Response {
		return cb_response( $this->tree( (int) $request['id'], self::T_QNA, false ) );
	}

	public function create_question( WP_REST_Request $request ): WP_REST_Response {
		$body       = $request->get_json_params();
		$product_id = (int) ( $body['productId'] ?? 0 );
		if ( ! get_post( $product_id ) ) {
			return cb_error( 'محصول یافت نشد', 404, 'NOT_FOUND', 'api/questions' );
		}
		$id = $this->insert_comment( $product_id, self::T_QNA, (string) ( $body['content'] ?? '' ), (int) ( $body['parentId'] ?? 0 ) );
		if ( ! $id ) {
			return cb_error( 'ثبت پرسش ناموفق بود', 400, 'CREATE_FAILED', 'api/questions' );
		}
		return cb_response( $this->question_dto( get_comment( $id ), true ), 201 );
	}

	public function update_question( WP_REST_Request $request ): WP_REST_Response {
		$comment = $this->owned_comment( (int) $request['id'] );
		if ( ! $comment ) {
			return cb_error( 'پرسش یافت نشد', 404, 'NOT_FOUND', 'api/questions' );
		}
		wp_update_comment( array( 'comment_ID' => $comment->comment_ID, 'comment_content' => sanitize_textarea_field( (string) ( $request->get_json_params()['content'] ?? $comment->comment_content ) ) ) );
		return cb_response( $this->question_dto( get_comment( $comment->comment_ID ), true ) );
	}

	public function delete_question( WP_REST_Request $request ): WP_REST_Response {
		return $this->delete_comment( (int) $request['id'], 'api/questions' );
	}

	// ---- shared -------------------------------------------------------------

	private function insert_comment( int $product_id, string $type, string $content, int $parent ): int {
		$user = wp_get_current_user();
		$id   = wp_insert_comment( array(
			'comment_post_ID'      => $product_id,
			'comment_type'         => $type,
			'comment_content'      => sanitize_textarea_field( $content ),
			'comment_parent'       => $parent,
			'user_id'              => $user->ID,
			'comment_author'       => $user->display_name,
			'comment_author_email' => $user->user_email,
			'comment_approved'     => 1,
		) );
		return $id ? (int) $id : 0;
	}

	/** A comment owned by the current user (or the user is an admin). */
	private function owned_comment( int $id ) {
		$comment = get_comment( $id );
		if ( ! $comment ) {
			return null;
		}
		if ( (int) $comment->user_id !== get_current_user_id() && ! CB_Plugin::require_admin() ) {
			return null;
		}
		return $comment;
	}

	private function delete_comment( int $id, string $path ): WP_REST_Response {
		$comment = $this->owned_comment( $id );
		if ( ! $comment ) {
			return cb_error( 'یافت نشد', 404, 'NOT_FOUND', $path );
		}
		wp_delete_comment( $comment->comment_ID, true );
		return cb_response( null, 204 );
	}

	/** Build the top-level list with nested replies for a product + type. */
	private function tree( int $product_id, string $type, bool $is_review ): array {
		$top = get_comments( array(
			'post_id' => $product_id,
			'type'    => $type,
			'parent'  => 0,
			'status'  => 'approve',
			'orderby' => 'comment_date_gmt',
			'order'   => 'DESC',
		) );
		$out = array();
		foreach ( $top as $c ) {
			$out[] = $is_review ? $this->review_dto( $c, true ) : $this->question_dto( $c, true );
		}
		return $out;
	}

	private function replies( WP_Comment $comment, bool $is_review ): array {
		$children = get_comments( array(
			'parent'  => $comment->comment_ID,
			'status'  => 'approve',
			'orderby' => 'comment_date_gmt',
			'order'   => 'ASC',
		) );
		$out = array();
		foreach ( $children as $c ) {
			$out[] = $is_review ? $this->review_dto( $c, false ) : $this->question_dto( $c, false );
		}
		return $out;
	}

	private function is_support( WP_Comment $comment ): bool {
		return $comment->user_id && user_can( (int) $comment->user_id, 'edit_posts' );
	}

	private function review_dto( WP_Comment $comment, bool $with_replies ): array {
		$rating = get_comment_meta( $comment->comment_ID, 'rating', true );
		return array(
			'id'                => (int) $comment->comment_ID,
			'userId'            => (int) $comment->user_id,
			'userName'          => $comment->comment_author ?: 'کاربر',
			'rating'            => $rating !== '' ? (int) $rating : null,
			'comment'           => $comment->comment_content,
			'replies'           => $with_replies ? $this->replies( $comment, true ) : array(),
			'createdAt'         => cb_iso( $comment->comment_date_gmt ),
			'isSupport'         => $this->is_support( $comment ),
			'verifiedPurchase'  => (bool) get_comment_meta( $comment->comment_ID, 'verified', true ),
		);
	}

	private function question_dto( WP_Comment $comment, bool $with_replies ): array {
		return array(
			'id'        => (int) $comment->comment_ID,
			'userId'    => (int) $comment->user_id,
			'userName'  => $comment->comment_author ?: 'کاربر',
			'content'   => $comment->comment_content,
			'replies'   => $with_replies ? $this->replies( $comment, false ) : array(),
			'createdAt' => cb_iso( $comment->comment_date_gmt ),
			'isSupport' => $this->is_support( $comment ),
		);
	}
}
