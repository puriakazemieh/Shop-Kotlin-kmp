<?php
namespace Carmilla\Core\Content;

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( __NAMESPACE__ . '\ContentService', false ) ) {

/**
 * Canonical Shared Content Service (Posts, Pages, Categories, Media).
 *
 * Used by both Carmilla Theme and Carmilla Bridge Plugin so that all three
 * deployment modes (theme-only, plugin-only, co-install) read/write identical
 * schemas, preserve post/attachment IDs, enforce capability & media validation,
 * and prevent duplicate writes.
 *
 * Part of Task P04-WORDPRESS-CODE-026.
 */
class ContentService {

	public const DEFAULT_MAX_UPLOAD_BYTES = 10485760; // 10 MB

	/**
	 * Allowed MIME types for media uploads (strictly excludes executable/script/SVG payloads).
	 *
	 * @var array<string, string>
	 */
	public const ALLOWED_MEDIA_MIMES = [
		'jpg|jpeg|jpe' => 'image/jpeg',
		'png'          => 'image/png',
		'gif'          => 'image/gif',
		'webp'         => 'image/webp',
		'pdf'          => 'application/pdf',
	];

	// ---- Permissions --------------------------------------------------------

	public static function can_manage_content(): bool {
		return current_user_can( 'manage_options' )
			|| current_user_can( 'carmilla_manage_content' )
			|| current_user_can( 'carmilla_edit_content' )
			|| current_user_can( 'edit_others_posts' )
			|| current_user_can( 'manage_woocommerce' );
	}

	public static function can_edit_post_item( \WP_Post $post ): bool {
		if ( self::can_manage_content() ) {
			return true;
		}
		$uid = get_current_user_id();
		return $uid > 0 && (int) $post->post_author === $uid && ( current_user_can( 'edit_posts' ) || current_user_can( 'edit_pages' ) );
	}

	public static function can_upload_media(): bool {
		return current_user_can( 'manage_options' )
			|| current_user_can( 'carmilla_manage_content' )
			|| current_user_can( 'carmilla_edit_content' )
			|| current_user_can( 'upload_files' )
			|| current_user_can( 'edit_others_posts' );
	}

	// ---- Posts (Blog) -------------------------------------------------------

	/**
	 * @param array<string, mixed> $params
	 * @param array<int, string>   $statuses
	 * @return array<string, mixed>
	 */
	public static function list_posts( array $params = [], array $statuses = [ 'publish' ] ): array {
		$page = max( 0, (int) ( $params['page'] ?? 0 ) );
		$size = min( 100, max( 1, (int) ( $params['size'] ?? 10 ) ) );

		$args = [
			'post_type'      => 'post',
			'post_status'    => $statuses,
			'posts_per_page' => $size,
			'paged'          => $page + 1,
		];

		if ( ! empty( $params['search'] ) ) {
			$args['s'] = sanitize_text_field( (string) $params['search'] );
		}
		if ( ! empty( $params['categoryId'] ) ) {
			$args['cat'] = (int) $params['categoryId'];
		}

		$query = new \WP_Query( $args );
		$items = [];
		foreach ( $query->posts as $post ) {
			if ( $post instanceof \WP_Post ) {
				$items[] = self::format_blog_response( $post, false );
			}
		}

		return self::paginate( $items, $page, $size, (int) $query->found_posts, (int) $query->max_num_pages );
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function featured_posts( int $size = 10 ): array {
		$size  = min( 100, max( 1, $size ) );
		$query = new \WP_Query( [
			'post_type'      => 'post',
			'post_status'    => 'publish',
			'posts_per_page' => $size,
			'meta_key'       => 'cb_is_featured',
			'meta_value'     => '1',
		] );

		$items = [];
		foreach ( $query->posts as $post ) {
			if ( $post instanceof \WP_Post ) {
				$items[] = self::format_blog_response( $post, false );
			}
		}

		return self::paginate( $items, 0, $size, (int) $query->found_posts, (int) $query->max_num_pages );
	}

	/**
	 * @param array<int, string> $statuses
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function get_post_by_slug( string $slug, array $statuses = [ 'publish' ], bool $increment_views = false ) {
		$post = self::find_by_slug( 'post', $slug, $statuses );
		if ( ! $post ) {
			return new \WP_Error( 'BLOG_NOT_FOUND', 'مقاله یافت نشد.', [ 'status' => 404 ] );
		}

		if ( $increment_views ) {
			$views = (int) get_post_meta( $post->ID, 'cb_view_count', true );
			update_post_meta( $post->ID, 'cb_view_count', $views + 1 );
		}

		return self::format_blog_response( $post, true );
	}

	/**
	 * @return array<int, array<string, mixed>>
	 */
	public static function related_posts( string $slug, int $limit = 4 ): array {
		$post = self::find_by_slug( 'post', $slug, [ 'publish' ] );
		if ( ! $post ) {
			return [];
		}

		$cats  = wp_get_post_categories( $post->ID );
		$query = new \WP_Query( [
			'post_type'      => 'post',
			'post_status'    => 'publish',
			'posts_per_page' => max( 1, min( 20, $limit ) ),
			'post__not_in'   => [ $post->ID ],
			'category__in'   => $cats ?: [],
		] );

		$out = [];
		foreach ( $query->posts as $p ) {
			if ( $p instanceof \WP_Post ) {
				$out[] = self::format_blog_response( $p, false );
			}
		}
		return $out;
	}

	/**
	 * @param array<string, mixed> $data
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function create_post( array $data ) {
		$title = trim( (string) ( $data['title'] ?? '' ) );
		if ( '' === $title ) {
			return new \WP_Error( 'VALIDATION', 'عنوان مقاله الزامی است.', [ 'status' => 400 ] );
		}

		// Idempotency / duplicate prevention by explicit slug when requested.
		if ( ! empty( $data['preventDuplicate'] ) && ! empty( $data['slug'] ) ) {
			$existing = self::find_by_slug( 'post', sanitize_title( (string) $data['slug'] ), [ 'publish', 'draft', 'pending', 'private' ] );
			if ( $existing ) {
				return self::format_blog_response( $existing, true );
			}
		}

		$content_html = '';
		if ( isset( $data['content'] ) && is_array( $data['content'] ) ) {
			$content_html = BlockMapper::blocks_to_html( $data['content'] );
		} elseif ( isset( $data['rawContent'] ) ) {
			$content_html = wp_kses_post( (string) $data['rawContent'] );
		}

		$insert = [
			'post_type'    => 'post',
			'post_title'   => sanitize_text_field( $title ),
			'post_content' => $content_html,
			'post_excerpt' => sanitize_textarea_field( (string) ( $data['summary'] ?? '' ) ),
			'post_status'  => self::map_status_in( (string) ( $data['status'] ?? 'PUBLISHED' ) ),
		];
		if ( ! empty( $data['slug'] ) ) {
			$insert['post_name'] = sanitize_title( (string) $data['slug'] );
		}

		$post_id = wp_insert_post( $insert, true );
		if ( is_wp_error( $post_id ) ) {
			return new \WP_Error( 'CREATE_FAILED', $post_id->get_error_message(), [ 'status' => 400 ] );
		}

		self::apply_blog_meta( (int) $post_id, $data );
		return self::format_blog_response( get_post( $post_id ), true );
	}

	/**
	 * @param array<string, mixed> $data
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function update_post( int $id, array $data ) {
		$post = get_post( $id );
		if ( ! $post || 'post' !== $post->post_type ) {
			return new \WP_Error( 'BLOG_NOT_FOUND', 'مقاله یافت نشد.', [ 'status' => 404 ] );
		}

		$update = [ 'ID' => $post->ID ];
		if ( array_key_exists( 'title', $data ) && null !== $data['title'] ) {
			$update['post_title'] = sanitize_text_field( (string) $data['title'] );
		}
		if ( array_key_exists( 'content', $data ) && is_array( $data['content'] ) ) {
			$update['post_content'] = BlockMapper::blocks_to_html( $data['content'] );
		} elseif ( array_key_exists( 'rawContent', $data ) && null !== $data['rawContent'] ) {
			$update['post_content'] = wp_kses_post( (string) $data['rawContent'] );
		}
		if ( array_key_exists( 'summary', $data ) && null !== $data['summary'] ) {
			$update['post_excerpt'] = sanitize_textarea_field( (string) $data['summary'] );
		}
		if ( array_key_exists( 'status', $data ) && null !== $data['status'] ) {
			$update['post_status'] = self::map_status_in( (string) $data['status'] );
		}

		$res = wp_update_post( $update, true );
		if ( is_wp_error( $res ) ) {
			return new \WP_Error( 'UPDATE_FAILED', $res->get_error_message(), [ 'status' => 400 ] );
		}

		self::apply_blog_meta( $post->ID, $data );
		return self::format_blog_response( get_post( $post->ID ), true );
	}

	/**
	 * @return true|\WP_Error
	 */
	public static function delete_post( int $id ) {
		$post = get_post( $id );
		if ( ! $post || 'post' !== $post->post_type ) {
			return new \WP_Error( 'BLOG_NOT_FOUND', 'مقاله یافت نشد.', [ 'status' => 404 ] );
		}
		wp_delete_post( $post->ID, true );
		return true;
	}

	/**
	 * @param array<string, mixed> $data
	 */
	private static function apply_blog_meta( int $post_id, array $data ): void {
		if ( ! empty( $data['thumbnailUrl'] ) ) {
			$thumb         = (string) $data['thumbnailUrl'];
			$attachment_id = attachment_url_to_postid( $thumb );
			if ( $attachment_id ) {
				set_post_thumbnail( $post_id, $attachment_id );
			}
			update_post_meta( $post_id, 'cb_thumbnail_url', esc_url_raw( $thumb ) );
		}
		if ( ! empty( $data['categoryId'] ) ) {
			wp_set_post_categories( $post_id, [ (int) $data['categoryId'] ] );
		}
		if ( array_key_exists( 'isFeatured', $data ) && null !== $data['isFeatured'] ) {
			$featured = $data['isFeatured'];
			update_post_meta( $post_id, 'cb_is_featured', ( true === $featured || 'true' === $featured || '1' === (string) $featured ) ? '1' : '0' );
		}
		if ( array_key_exists( 'metaTitle', $data ) && null !== $data['metaTitle'] ) {
			update_post_meta( $post_id, 'cb_meta_title', sanitize_text_field( (string) $data['metaTitle'] ) );
		}
		if ( array_key_exists( 'metaDescription', $data ) && null !== $data['metaDescription'] ) {
			update_post_meta( $post_id, 'cb_meta_description', sanitize_textarea_field( (string) $data['metaDescription'] ) );
		}
	}

	// ---- Categories ---------------------------------------------------------

	/**
	 * @return array<int, array<string, mixed>>
	 */
	public static function list_categories(): array {
		$terms = get_terms( [ 'taxonomy' => 'category', 'hide_empty' => false ] );
		$out   = [];
		if ( ! is_wp_error( $terms ) ) {
			foreach ( $terms as $t ) {
				$out[] = self::format_category_response( $t );
			}
		}
		return $out;
	}

	/**
	 * @param array<string, mixed> $data
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function create_category( array $data ) {
		$name = trim( (string) ( $data['name'] ?? '' ) );
		if ( '' === $name ) {
			return new \WP_Error( 'VALIDATION', 'نام دسته الزامی است.', [ 'status' => 400 ] );
		}

		$result = wp_insert_term(
			sanitize_text_field( $name ),
			'category',
			[ 'description' => sanitize_textarea_field( (string) ( $data['description'] ?? '' ) ) ]
		);

		if ( is_wp_error( $result ) ) {
			return new \WP_Error( 'CATEGORY_FAILED', $result->get_error_message(), [ 'status' => 400 ] );
		}

		return self::format_category_response( get_term( $result['term_id'], 'category' ) );
	}

	/**
	 * @param array<string, mixed> $data
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function update_category( int $id, array $data ) {
		$fields = [];
		if ( array_key_exists( 'name', $data ) && null !== $data['name'] ) {
			$fields['name'] = sanitize_text_field( (string) $data['name'] );
		}
		if ( array_key_exists( 'description', $data ) && null !== $data['description'] ) {
			$fields['description'] = sanitize_textarea_field( (string) $data['description'] );
		}

		$result = wp_update_term( $id, 'category', $fields );
		if ( is_wp_error( $result ) ) {
			return new \WP_Error( 'CATEGORY_FAILED', $result->get_error_message(), [ 'status' => 400 ] );
		}

		return self::format_category_response( get_term( $id, 'category' ) );
	}

	public static function delete_category( int $id ): void {
		wp_delete_term( $id, 'category' );
	}

	// ---- Pages --------------------------------------------------------------

	/**
	 * @param array<string, mixed> $params
	 * @param array<int, string>   $statuses
	 * @return array<string, mixed>
	 */
	public static function list_pages( array $params = [], array $statuses = [ 'publish' ] ): array {
		$page = max( 0, (int) ( $params['page'] ?? 0 ) );
		$size = min( 100, max( 1, (int) ( $params['size'] ?? 20 ) ) );

		$args = [
			'post_type'      => 'page',
			'post_status'    => $statuses,
			'posts_per_page' => $size,
			'paged'          => $page + 1,
			'orderby'        => 'menu_order title',
			'order'          => 'ASC',
		];
		if ( ! empty( $params['search'] ) ) {
			$args['s'] = sanitize_text_field( (string) $params['search'] );
		}

		$query = new \WP_Query( $args );
		$items = [];
		foreach ( $query->posts as $p ) {
			if ( $p instanceof \WP_Post ) {
				$items[] = self::format_page_response( $p, false );
			}
		}

		return self::paginate( $items, $page, $size, (int) $query->found_posts, (int) $query->max_num_pages );
	}

	/**
	 * @param int|string         $id_or_slug
	 * @param array<int, string> $statuses
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function get_page( $id_or_slug, array $statuses = [ 'publish' ] ) {
		$post = null;
		if ( is_numeric( $id_or_slug ) ) {
			$candidate = get_post( (int) $id_or_slug );
			if ( $candidate instanceof \WP_Post && 'page' === $candidate->post_type && in_array( $candidate->post_status, $statuses, true ) ) {
				$post = $candidate;
			}
		} else {
			$post = self::find_by_slug( 'page', (string) $id_or_slug, $statuses );
		}

		if ( ! $post ) {
			return new \WP_Error( 'PAGE_NOT_FOUND', 'برگه یافت نشد.', [ 'status' => 404 ] );
		}

		return self::format_page_response( $post, true );
	}

	/**
	 * @param array<string, mixed> $data
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function create_page( array $data ) {
		$title = trim( (string) ( $data['title'] ?? '' ) );
		if ( '' === $title ) {
			return new \WP_Error( 'VALIDATION', 'عنوان برگه الزامی است.', [ 'status' => 400 ] );
		}

		$slug = ! empty( $data['slug'] ) ? sanitize_title( (string) $data['slug'] ) : '';
		if ( ! empty( $data['preventDuplicate'] ) && '' !== $slug ) {
			$existing = self::find_by_slug( 'page', $slug, [ 'publish', 'draft', 'pending', 'private' ] );
			if ( $existing ) {
				return self::format_page_response( $existing, true );
			}
		}

		$content_html = '';
		if ( isset( $data['content'] ) && is_array( $data['content'] ) ) {
			$content_html = BlockMapper::blocks_to_html( $data['content'] );
		} elseif ( isset( $data['rawContent'] ) ) {
			$content_html = wp_kses_post( (string) $data['rawContent'] );
		} elseif ( isset( $data['content'] ) && is_string( $data['content'] ) ) {
			$content_html = wp_kses_post( $data['content'] );
		}

		$insert = [
			'post_type'    => 'page',
			'post_title'   => sanitize_text_field( $title ),
			'post_content' => $content_html,
			'post_excerpt' => sanitize_textarea_field( (string) ( $data['summary'] ?? '' ) ),
			'post_status'  => self::map_status_in( (string) ( $data['status'] ?? 'PUBLISHED' ) ),
			'post_parent'  => isset( $data['parentId'] ) ? max( 0, (int) $data['parentId'] ) : 0,
			'menu_order'   => isset( $data['menuOrder'] ) ? (int) $data['menuOrder'] : 0,
		];
		if ( '' !== $slug ) {
			$insert['post_name'] = $slug;
		}

		$page_id = wp_insert_post( $insert, true );
		if ( is_wp_error( $page_id ) ) {
			return new \WP_Error( 'CREATE_FAILED', $page_id->get_error_message(), [ 'status' => 400 ] );
		}

		if ( array_key_exists( 'metaTitle', $data ) && null !== $data['metaTitle'] ) {
			update_post_meta( (int) $page_id, 'cb_meta_title', sanitize_text_field( (string) $data['metaTitle'] ) );
		}
		if ( array_key_exists( 'metaDescription', $data ) && null !== $data['metaDescription'] ) {
			update_post_meta( (int) $page_id, 'cb_meta_description', sanitize_textarea_field( (string) $data['metaDescription'] ) );
		}

		return self::format_page_response( get_post( $page_id ), true );
	}

	/**
	 * @param array<string, mixed> $data
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function update_page( int $id, array $data ) {
		$post = get_post( $id );
		if ( ! $post || 'page' !== $post->post_type ) {
			return new \WP_Error( 'PAGE_NOT_FOUND', 'برگه یافت نشد.', [ 'status' => 404 ] );
		}

		$update = [ 'ID' => $post->ID ];
		if ( array_key_exists( 'title', $data ) && null !== $data['title'] ) {
			$update['post_title'] = sanitize_text_field( (string) $data['title'] );
		}
		if ( array_key_exists( 'content', $data ) && is_array( $data['content'] ) ) {
			$update['post_content'] = BlockMapper::blocks_to_html( $data['content'] );
		} elseif ( array_key_exists( 'rawContent', $data ) && null !== $data['rawContent'] ) {
			$update['post_content'] = wp_kses_post( (string) $data['rawContent'] );
		} elseif ( array_key_exists( 'content', $data ) && is_string( $data['content'] ) ) {
			$update['post_content'] = wp_kses_post( $data['content'] );
		}
		if ( array_key_exists( 'summary', $data ) && null !== $data['summary'] ) {
			$update['post_excerpt'] = sanitize_textarea_field( (string) $data['summary'] );
		}
		if ( array_key_exists( 'status', $data ) && null !== $data['status'] ) {
			$update['post_status'] = self::map_status_in( (string) $data['status'] );
		}

		$res = wp_update_post( $update, true );
		if ( is_wp_error( $res ) ) {
			return new \WP_Error( 'UPDATE_FAILED', $res->get_error_message(), [ 'status' => 400 ] );
		}

		if ( array_key_exists( 'metaTitle', $data ) && null !== $data['metaTitle'] ) {
			update_post_meta( $post->ID, 'cb_meta_title', sanitize_text_field( (string) $data['metaTitle'] ) );
		}
		if ( array_key_exists( 'metaDescription', $data ) && null !== $data['metaDescription'] ) {
			update_post_meta( $post->ID, 'cb_meta_description', sanitize_textarea_field( (string) $data['metaDescription'] ) );
		}

		return self::format_page_response( get_post( $post->ID ), true );
	}

	/**
	 * @return true|\WP_Error
	 */
	public static function delete_page( int $id ) {
		$post = get_post( $id );
		if ( ! $post || 'page' !== $post->post_type ) {
			return new \WP_Error( 'PAGE_NOT_FOUND', 'برگه یافت نشد.', [ 'status' => 404 ] );
		}
		wp_delete_post( $post->ID, true );
		return true;
	}

	// ---- Media --------------------------------------------------------------

	/**
	 * Validates and stores an uploaded media file via WordPress Media Library.
	 *
	 * @param array<string, mixed> $file      Entry from $_FILES / WP_REST_Request::get_file_params().
	 * @param int                  $parent_id Optional parent post ID.
	 * @param array<string, mixed> $options   Optional overrides ('max_bytes', 'allowed_mimes', 'test_form').
	 * @return array<string, mixed>|\WP_Error
	 */
	public static function upload_media( array $file, int $parent_id = 0, array $options = [] ) {
		if ( empty( $file['name'] ) || empty( $file['tmp_name'] ) || ! file_exists( (string) $file['tmp_name'] ) ) {
			return new \WP_Error( 'NO_FILE', 'فایلی ارسال نشده است.', [ 'status' => 400 ] );
		}

		if ( ! empty( $file['error'] ) && UPLOAD_ERR_OK !== (int) $file['error'] ) {
			return new \WP_Error( 'UPLOAD_ERROR', 'خطا در دریافت فایل.', [ 'status' => 400 ] );
		}

		$max_bytes = isset( $options['max_bytes'] ) ? (int) $options['max_bytes'] : self::DEFAULT_MAX_UPLOAD_BYTES;
		$size      = isset( $file['size'] ) ? (int) $file['size'] : (int) @filesize( (string) $file['tmp_name'] );
		if ( $size <= 0 || $size > $max_bytes ) {
			return new \WP_Error(
				'INVALID_MEDIA_SIZE',
				sprintf( 'حجم فایل باید بین ۱ بایت تا %d بایت باشد.', $max_bytes ),
				[ 'status' => 400 ]
			);
		}

		$allowed_mimes = isset( $options['allowed_mimes'] ) && is_array( $options['allowed_mimes'] )
			? $options['allowed_mimes']
			: self::ALLOWED_MEDIA_MIMES;

		$checked = wp_check_filetype_and_ext( (string) $file['tmp_name'], (string) $file['name'], $allowed_mimes );
		if ( empty( $checked['ext'] ) || empty( $checked['type'] ) ) {
			return new \WP_Error( 'INVALID_MEDIA_TYPE', 'نوع فایل مجاز نیست.', [ 'status' => 400 ] );
		}

		if ( ! function_exists( 'media_handle_sideload' ) ) {
			require_once ABSPATH . 'wp-admin/includes/file.php';
			require_once ABSPATH . 'wp-admin/includes/media.php';
			require_once ABSPATH . 'wp-admin/includes/image.php';
		}

		// Use wp_handle_sideload / media_handle_sideload so both real HTTP uploads and local fixtures work cleanly.
		$sideload_file = [
			'name'     => sanitize_file_name( (string) $file['name'] ),
			'type'     => $checked['type'],
			'tmp_name' => (string) $file['tmp_name'],
			'error'    => 0,
			'size'     => $size,
		];

		$overrides = [
			'test_form' => false,
			'mimes'     => $allowed_mimes,
		];
		if ( isset( $options['test_upload'] ) ) {
			$overrides['test_upload'] = (bool) $options['test_upload'];
		}

		$moved = wp_handle_sideload( $sideload_file, $overrides );
		if ( isset( $moved['error'] ) ) {
			return new \WP_Error( 'UPLOAD_FAILED', (string) $moved['error'], [ 'status' => 400 ] );
		}

		$attachment = [
			'post_mime_type' => $moved['type'],
			'post_title'     => preg_replace( '/\.[^.]+$/', '', basename( $moved['file'] ) ),
			'post_content'   => '',
			'post_status'    => 'inherit',
		];

		$attachment_id = wp_insert_attachment( $attachment, $moved['file'], $parent_id, true );
		if ( is_wp_error( $attachment_id ) ) {
			return new \WP_Error( 'UPLOAD_FAILED', $attachment_id->get_error_message(), [ 'status' => 400 ] );
		}

		$meta = wp_generate_attachment_metadata( (int) $attachment_id, $moved['file'] );
		if ( is_array( $meta ) && ! empty( $meta ) ) {
			wp_update_attachment_metadata( (int) $attachment_id, $meta );
		}

		return self::format_media_response( get_post( $attachment_id ) );
	}

	/**
	 * @param array<string, mixed> $params
	 * @return array<string, mixed>
	 */
	public static function list_media( array $params = [] ): array {
		$page  = max( 0, (int) ( $params['page'] ?? 0 ) );
		$size  = min( 100, max( 1, (int) ( $params['size'] ?? 20 ) ) );
		$query = new \WP_Query( [
			'post_type'      => 'attachment',
			'post_status'    => 'inherit',
			'posts_per_page' => $size,
			'paged'          => $page + 1,
		] );

		$items = [];
		foreach ( $query->posts as $p ) {
			if ( $p instanceof \WP_Post ) {
				$items[] = self::format_media_response( $p );
			}
		}

		return self::paginate( $items, $page, $size, (int) $query->found_posts, (int) $query->max_num_pages );
	}

	// ---- Helpers & Formatters -----------------------------------------------

	private static function find_by_slug( string $post_type, string $slug, array $statuses ): ?\WP_Post {
		$slug  = rawurldecode( $slug );
		$query = new \WP_Query( [
			'post_type'      => $post_type,
			'name'           => $slug,
			'post_status'    => $statuses,
			'posts_per_page' => 1,
		] );
		return $query->have_posts() ? $query->posts[0] : null;
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function format_blog_response( \WP_Post $post, bool $with_content ): array {
		$author = get_userdata( (int) $post->post_author );
		$cats   = wp_get_post_categories( $post->ID, [ 'fields' => 'all' ] );
		$cat    = ! empty( $cats ) ? $cats[0] : null;
		$thumb  = get_the_post_thumbnail_url( $post->ID, 'full' ) ?: ( get_post_meta( $post->ID, 'cb_thumbnail_url', true ) ?: null );

		$data = [
			'id'                 => (int) $post->ID,
			'title'              => get_the_title( $post ),
			'slug'               => $post->post_name,
			'summary'            => $post->post_excerpt ?: null,
			'thumbnailUrl'       => $thumb,
			'viewCount'          => (int) get_post_meta( $post->ID, 'cb_view_count', true ),
			'readingTimeMinutes' => self::reading_time( $post ),
			'authorName'         => $author ? $author->display_name : null,
			'author'             => $author ? [ 'id' => (int) $author->ID, 'name' => $author->display_name ] : null,
			'categoryName'       => $cat ? $cat->name : null,
			'category'           => $cat ? self::format_category_response( $cat ) : null,
			'featured'           => '1' === get_post_meta( $post->ID, 'cb_is_featured', true ),
			'status'             => self::map_status_out( $post->post_status ),
			'metaTitle'          => get_post_meta( $post->ID, 'cb_meta_title', true ) ?: null,
			'metaDescription'    => get_post_meta( $post->ID, 'cb_meta_description', true ) ?: null,
			'createdAt'          => self::iso_date( $post->post_date_gmt ),
			'updatedAt'          => self::iso_date( $post->post_modified_gmt ),
		];

		if ( $with_content ) {
			$data['content'] = BlockMapper::post_to_blocks( $post );
		}

		return $data;
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function format_page_response( \WP_Post $post, bool $with_content ): array {
		$data = [
			'id'              => (int) $post->ID,
			'title'           => get_the_title( $post ),
			'slug'            => $post->post_name,
			'summary'         => $post->post_excerpt ?: null,
			'status'          => self::map_status_out( $post->post_status ),
			'parentId'        => (int) $post->post_parent,
			'menuOrder'       => (int) $post->menu_order,
			'metaTitle'       => get_post_meta( $post->ID, 'cb_meta_title', true ) ?: null,
			'metaDescription' => get_post_meta( $post->ID, 'cb_meta_description', true ) ?: null,
			'permalink'       => get_permalink( $post->ID ) ?: null,
			'createdAt'       => self::iso_date( $post->post_date_gmt ),
			'updatedAt'       => self::iso_date( $post->post_modified_gmt ),
		];

		if ( $with_content ) {
			$data['content']    = BlockMapper::post_to_blocks( $post );
			$data['rawContent'] = $post->post_content;
		}

		return $data;
	}

	/**
	 * @return array<string, mixed>
	 */
	public static function format_media_response( \WP_Post $attachment ): array {
		return [
			'id'       => (int) $attachment->ID,
			'title'    => get_the_title( $attachment ),
			'url'      => wp_get_attachment_url( $attachment->ID ) ?: '',
			'mimeType' => $attachment->post_mime_type,
		];
	}

	/**
	 * @param \WP_Term $term
	 * @return array<string, mixed>
	 */
	public static function format_category_response( $term ): array {
		return [
			'id'           => (int) $term->term_id,
			'name'         => $term->name,
			'slug'         => $term->slug,
			'description'  => $term->description ?: null,
			'thumbnailUrl' => null,
			'blogCount'    => (int) $term->count,
		];
	}

	private static function reading_time( \WP_Post $post ): int {
		$words = str_word_count( wp_strip_all_tags( $post->post_content ) );
		if ( $words < 5 ) {
			$words = (int) ( mb_strlen( wp_strip_all_tags( $post->post_content ) ) / 5 );
		}
		return max( 1, (int) ceil( $words / 200 ) );
	}

	private static function map_status_in( string $status ): string {
		$upper = strtoupper( $status );
		if ( 'DRAFT' === $upper ) {
			return 'draft';
		}
		if ( 'PRIVATE' === $upper ) {
			return 'private';
		}
		return 'publish';
	}

	private static function map_status_out( string $status ): string {
		if ( 'publish' === $status ) {
			return 'PUBLISHED';
		}
		if ( 'private' === $status ) {
			return 'PRIVATE';
		}
		return 'DRAFT';
	}

	private static function iso_date( ?string $gmt ): ?string {
		if ( empty( $gmt ) || '0000-00-00 00:00:00' === $gmt ) {
			return null;
		}
		return gmdate( 'c', strtotime( $gmt . ' UTC' ) );
	}

	/**
	 * @param array<int, mixed> $items
	 * @return array<string, mixed>
	 */
	private static function paginate( array $items, int $page, int $size, int $total, int $total_pages ): array {
		return [
			'content'       => $items,
			'page'          => $page,
			'size'          => $size,
			'totalElements' => $total,
			'totalPages'    => $total_pages,
			'last'          => ( $page + 1 ) >= max( 1, $total_pages ),
		];
	}
}

}
