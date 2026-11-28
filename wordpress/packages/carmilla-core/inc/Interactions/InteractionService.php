<?php
namespace Carmilla\Core\Interactions;

use Carmilla\Core\Rest\RestInfrastructure;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\InteractionService', false ) ) {

/**
 * Canonical Shared Content & Product Interactions Service.
 *
 * Centralizes product reviews (`comment_type = review`), product Q&A
 * (`comment_type = cb_qna`), helpful votes, and user favorites (`cb_wishlist`)
 * across Theme-only, Plugin-only, and Co-install modes.
 *
 * Part of Task P04-WORDPRESS-CODE-029B.
 */
class InteractionService {

	public const T_REVIEW = 'review';
	public const T_QNA    = 'cb_qna';

	private static bool $filters_registered = false;

	public static function is_enabled(): bool {
		if ( class_exists( '\Carmilla_Kernel' ) && \Carmilla_Kernel::is_booted() ) {
			return \Carmilla_Kernel::is_feature_active( 'commerce.core' );
		}
		return true;
	}

	public static function can_moderate(): bool {
		return current_user_can( 'manage_options' )
			|| current_user_can( 'carmilla_manage_content' )
			|| current_user_can( 'moderate_comments' );
	}

	/**
	 * Registers comment feed isolation filter once per request.
	 */
	public static function register_comment_filters(): void {
		if ( self::$filters_registered ) {
			return;
		}
		self::$filters_registered = true;
		if ( function_exists( 'add_filter' ) ) {
			add_filter( 'comments_clauses', [ self::class, 'hide_qna_clauses' ], 10, 1 );
		}
	}

	/**
	 * Hide cb_qna comments from normal WordPress comment feeds.
	 *
	 * @param array<string, string> $clauses
	 * @return array<string, string>
	 */
	public static function hide_qna_clauses( array $clauses ): array {
		global $wpdb;
		if ( isset( $clauses['where'] ) && strpos( $clauses['where'], 'comment_type' ) === false ) {
			$clauses['where'] .= $wpdb->prepare( " AND {$wpdb->comments}.comment_type != %s", self::T_QNA );
		}
		return $clauses;
	}

	// ---- reviews ------------------------------------------------------------

	/**
	 * @return array<int, array<string, mixed>>
	 */
	public static function list_reviews( int $product_id ): array {
		return self::tree( $product_id, self::T_REVIEW, true );
	}

	/**
	 * @param array<string, mixed> $params
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function create_review( int $uid, array $params ) {
		if ( ! self::is_enabled() ) {
			return new \WP_Error( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', [ 'status' => 403 ] );
		}
		if ( $uid < 1 ) {
			return new \WP_Error( 'UNAUTHORIZED', 'ابتدا وارد شوید', [ 'status' => 401 ] );
		}
		$product_id = (int) ( $params['productId'] ?? 0 );
		if ( $product_id < 1 || ! get_post( $product_id ) ) {
			return new \WP_Error( 'NOT_FOUND', 'محصول یافت نشد', [ 'status' => 404 ] );
		}
		$id = self::insert_comment( $uid, $product_id, self::T_REVIEW, (string) ( $params['comment'] ?? '' ), (int) ( $params['parentId'] ?? 0 ) );
		if ( ! $id ) {
			return new \WP_Error( 'CREATE_FAILED', 'ثبت نظر ناموفق بود', [ 'status' => 400 ] );
		}
		if ( isset( $params['rating'] ) && null !== $params['rating'] ) {
			add_comment_meta( $id, 'rating', max( 1, min( 5, (int) $params['rating'] ) ) );
		}
		if ( function_exists( 'wc_customer_bought_product' ) ) {
			$user = get_userdata( $uid );
			if ( $user && wc_customer_bought_product( $user->user_email, $uid, $product_id ) ) {
				add_comment_meta( $id, 'verified', 1 );
			}
		}
		$c = get_comment( $id );
		return $c instanceof \WP_Comment
			? self::review_dto( $c, true )
			: new \WP_Error( 'CREATE_FAILED', 'ثبت نظر ناموفق بود', [ 'status' => 400 ] );
	}

	/**
	 * @param array<string, mixed> $params
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function update_review( int $uid, int $review_id, array $params ) {
		if ( ! self::is_enabled() ) {
			return new \WP_Error( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', [ 'status' => 403 ] );
		}
		$comment = self::owned_comment( $uid, $review_id, self::T_REVIEW );
		if ( ! $comment ) {
			return new \WP_Error( 'NOT_FOUND', 'نظر یافت نشد', [ 'status' => 404 ] );
		}
		wp_update_comment( [
			'comment_ID'      => $comment->comment_ID,
			'comment_content' => sanitize_textarea_field( (string) ( $params['comment'] ?? $comment->comment_content ) ),
		] );
		if ( array_key_exists( 'rating', $params ) && null !== $params['rating'] ) {
			update_comment_meta( $comment->comment_ID, 'rating', max( 1, min( 5, (int) $params['rating'] ) ) );
		}
		return self::review_dto( get_comment( $comment->comment_ID ), true );
	}

	/**
	 * @return true|\WP_Error
	 */
	public static function delete_review( int $uid, int $review_id ) {
		return self::delete_comment( $uid, $review_id, self::T_REVIEW );
	}

	/**
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function mark_helpful( int $review_id ) {
		if ( ! self::is_enabled() ) {
			return new \WP_Error( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', [ 'status' => 403 ] );
		}
		$comment = get_comment( $review_id );
		if ( ! $comment instanceof \WP_Comment || self::T_REVIEW !== $comment->comment_type ) {
			return new \WP_Error( 'NOT_FOUND', 'نظر یافت نشد', [ 'status' => 404 ] );
		}
		$count = (int) get_comment_meta( $comment->comment_ID, 'cb_helpful', true ) + 1;
		update_comment_meta( $comment->comment_ID, 'cb_helpful', $count );
		return self::review_dto( $comment, true );
	}

	// ---- Q&A ----------------------------------------------------------------

	/**
	 * @return array<int, array<string, mixed>>
	 */
	public static function list_questions( int $product_id ): array {
		return self::tree( $product_id, self::T_QNA, false );
	}

	/**
	 * @param array<string, mixed> $params
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function create_question( int $uid, array $params ) {
		if ( ! self::is_enabled() ) {
			return new \WP_Error( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', [ 'status' => 403 ] );
		}
		if ( $uid < 1 ) {
			return new \WP_Error( 'UNAUTHORIZED', 'ابتدا وارد شوید', [ 'status' => 401 ] );
		}
		$product_id = (int) ( $params['productId'] ?? 0 );
		if ( $product_id < 1 || ! get_post( $product_id ) ) {
			return new \WP_Error( 'NOT_FOUND', 'محصول یافت نشد', [ 'status' => 404 ] );
		}
		$id = self::insert_comment( $uid, $product_id, self::T_QNA, (string) ( $params['content'] ?? '' ), (int) ( $params['parentId'] ?? 0 ) );
		if ( ! $id ) {
			return new \WP_Error( 'CREATE_FAILED', 'ثبت پرسش ناموفق بود', [ 'status' => 400 ] );
		}
		$c = get_comment( $id );
		return $c instanceof \WP_Comment
			? self::question_dto( $c, true )
			: new \WP_Error( 'CREATE_FAILED', 'ثبت پرسش ناموفق بود', [ 'status' => 400 ] );
	}

	/**
	 * @param array<string, mixed> $params
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function update_question( int $uid, int $question_id, array $params ) {
		if ( ! self::is_enabled() ) {
			return new \WP_Error( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', [ 'status' => 403 ] );
		}
		$comment = self::owned_comment( $uid, $question_id, self::T_QNA );
		if ( ! $comment ) {
			return new \WP_Error( 'NOT_FOUND', 'پرسش یافت نشد', [ 'status' => 404 ] );
		}
		wp_update_comment( [
			'comment_ID'      => $comment->comment_ID,
			'comment_content' => sanitize_textarea_field( (string) ( $params['content'] ?? $comment->comment_content ) ),
		] );
		return self::question_dto( get_comment( $comment->comment_ID ), true );
	}

	/**
	 * @return true|\WP_Error
	 */
	public static function delete_question( int $uid, int $question_id ) {
		return self::delete_comment( $uid, $question_id, self::T_QNA );
	}

	// ---- favorites (user meta cb_wishlist) ---------------------------------

	/**
	 * @return array<int, int>
	 */
	public static function get_favorite_ids( int $uid ): array {
		if ( $uid < 1 ) {
			return [];
		}
		$v = get_user_meta( $uid, 'cb_wishlist', true );
		return is_array( $v ) ? array_values( array_map( 'intval', $v ) ) : [];
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function list_favorites( int $uid, int $page = 0, int $size = 20 ): array {
		$ids   = self::get_favorite_ids( $uid );
		$total = count( $ids );
		$slice = array_slice( $ids, max( 0, $page ) * max( 1, $size ), max( 1, $size ) );
		$items = [];
		foreach ( $slice as $pid ) {
			if ( function_exists( 'wc_get_product' ) && class_exists( '\Carmilla\Core\Catalog\CatalogService' ) ) {
				$p = wc_get_product( $pid );
				if ( $p ) {
					$items[] = \Carmilla\Core\Catalog\CatalogService::product_summary( $p, $uid );
					continue;
				}
			}
			$post = get_post( $pid );
			if ( $post ) {
				$items[] = [
					'id'    => (int) $post->ID,
					'name'  => get_the_title( $post ),
					'slug'  => $post->post_name,
				];
			}
		}
		$res = RestInfrastructure::paginated_response( $items, $total, max( 0, $page ), max( 1, $size ) );
		return is_array( $res ) ? $res : $res->get_data();
	}

	public static function add_favorite( int $uid, int $product_id ): bool {
		if ( $uid < 1 || $product_id < 1 ) {
			return false;
		}
		$ids = self::get_favorite_ids( $uid );
		if ( ! in_array( $product_id, $ids, true ) ) {
			$ids[] = $product_id;
			update_user_meta( $uid, 'cb_wishlist', $ids );
		}
		return true;
	}

	public static function remove_favorite( int $uid, int $product_id ): bool {
		if ( $uid < 1 ) {
			return false;
		}
		update_user_meta( $uid, 'cb_wishlist', array_values( array_diff( self::get_favorite_ids( $uid ), [ $product_id ] ) ) );
		return true;
	}

	// ---- internal comment helpers -------------------------------------------

	private static function insert_comment( int $uid, int $product_id, string $type, string $content, int $parent ): int {
		$content = sanitize_textarea_field( $content );
		if ( '' === trim( $content ) ) {
			return 0;
		}
		$user = get_userdata( $uid );
		$id   = wp_insert_comment( [
			'comment_post_ID'      => $product_id,
			'comment_type'         => $type,
			'comment_content'      => $content,
			'comment_parent'       => $parent,
			'user_id'              => $uid,
			'comment_author'       => $user ? $user->display_name : 'کاربر',
			'comment_author_email' => $user ? $user->user_email : '',
			'comment_approved'     => 1,
		] );
		return $id ? (int) $id : 0;
	}

	private static function owned_comment( int $uid, int $id, string $type ): ?\WP_Comment {
		$comment = get_comment( $id );
		if ( ! $comment instanceof \WP_Comment || $comment->comment_type !== $type ) {
			return null;
		}
		if ( (int) $comment->user_id !== $uid && ! self::can_moderate() ) {
			return null;
		}
		return $comment;
	}

	/**
	 * @return true|\WP_Error
	 */
	private static function delete_comment( int $uid, int $id, string $type ) {
		if ( ! self::is_enabled() ) {
			return new \WP_Error( 'FEATURE_DISABLED', 'این قابلیت در حال حاضر غیرفعال است', [ 'status' => 403 ] );
		}
		$comment = self::owned_comment( $uid, $id, $type );
		if ( ! $comment ) {
			return new \WP_Error( 'NOT_FOUND', 'یافت نشد', [ 'status' => 404 ] );
		}
		wp_delete_comment( $comment->comment_ID, true );
		return true;
	}

	/**
	 * @return array<int, array<string, mixed>>
	 */
	private static function tree( int $product_id, string $type, bool $is_review ): array {
		$top = get_comments( [
			'post_id' => $product_id,
			'type'    => $type,
			'parent'  => 0,
			'status'  => 'approve',
			'orderby' => 'comment_date_gmt',
			'order'   => 'DESC',
		] );
		$out = [];
		foreach ( $top as $c ) {
			if ( $c instanceof \WP_Comment ) {
				$out[] = $is_review ? self::review_dto( $c, true ) : self::question_dto( $c, true );
			}
		}
		return $out;
	}

	/**
	 * @return array<int, array<string, mixed>>
	 */
	private static function replies( \WP_Comment $comment, bool $is_review ): array {
		$children = get_comments( [
			'parent'  => $comment->comment_ID,
			'status'  => 'approve',
			'orderby' => 'comment_date_gmt',
			'order'   => 'ASC',
		] );
		$out = [];
		foreach ( $children as $c ) {
			if ( $c instanceof \WP_Comment ) {
				$out[] = $is_review ? self::review_dto( $c, false ) : self::question_dto( $c, false );
			}
		}
		return $out;
	}

	private static function is_support( \WP_Comment $comment ): bool {
		return $comment->user_id && user_can( (int) $comment->user_id, 'edit_posts' );
	}

	/**
	 * @return array<string, mixed>
	 */
	private static function review_dto( \WP_Comment $comment, bool $with_replies ): array {
		$rating = get_comment_meta( $comment->comment_ID, 'rating', true );
		return [
			'id'               => (int) $comment->comment_ID,
			'userId'           => (int) $comment->user_id,
			'userName'         => $comment->comment_author ?: 'کاربر',
			'rating'           => '' !== $rating ? (int) $rating : null,
			'comment'          => $comment->comment_content,
			'helpfulCount'     => (int) get_comment_meta( $comment->comment_ID, 'cb_helpful', true ),
			'replies'          => $with_replies ? self::replies( $comment, true ) : [],
			'createdAt'        => gmdate( 'c', strtotime( $comment->comment_date_gmt ?: 'now' ) ?: time() ),
			'isSupport'        => self::is_support( $comment ),
			'verifiedPurchase' => (bool) get_comment_meta( $comment->comment_ID, 'verified', true ),
		];
	}

	/**
	 * @return array<string, mixed>
	 */
	private static function question_dto( \WP_Comment $comment, bool $with_replies ): array {
		return [
			'id'        => (int) $comment->comment_ID,
			'userId'    => (int) $comment->user_id,
			'userName'  => $comment->comment_author ?: 'کاربر',
			'content'   => $comment->comment_content,
			'replies'   => $with_replies ? self::replies( $comment, false ) : [],
			'createdAt' => gmdate( 'c', strtotime( $comment->comment_date_gmt ?: 'now' ) ?: time() ),
			'isSupport' => self::is_support( $comment ),
		];
	}
}

}
