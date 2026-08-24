<?php
/**
 * Blog (articles) endpoints backed by WP posts, shaped to the app's blog DTOs.
 * Article bodies map between Gutenberg blocks and the app's BlogBlockDto array (CB_Blocks).
 *
 * Public:
 *   GET api/blogs                    -> BlogListResponse
 *   GET api/blogs/featured           -> BlogListResponse
 *   GET api/blogs/{slug}             -> BlogResponse (increments views)
 *   GET api/blogs/{slug}/related     -> List<BlogResponse>
 *   GET api/blogs/categories         -> List<BlogCategoryResponse>
 * Admin (Bearer):
 *   GET    api/admin/blogs           -> BlogListResponse (all statuses)
 *   GET    api/admin/blogs/{slug}    -> BlogResponse
 *   POST   api/admin/blogs           -> BlogResponse
 *   PUT    api/admin/blogs/{id}      -> BlogResponse
 *   DELETE api/admin/blogs/{id}      -> 204
 *   POST/PUT/DELETE api/admin/blogs/categories[/{id}]
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Blog_Controller {

	public function register_routes(): void {
		$ns = CB_REST_NAMESPACE;

		register_rest_route( $ns, '/api/blogs', array(
			'methods'             => 'GET',
			'callback'            => array( $this, 'list_blogs' ),
			'permission_callback' => '__return_true',
		) );
		register_rest_route( $ns, '/api/blogs/featured', array(
			'methods'             => 'GET',
			'callback'            => array( $this, 'featured' ),
			'permission_callback' => '__return_true',
		) );
		register_rest_route( $ns, '/api/blogs/categories', array(
			'methods'             => 'GET',
			'callback'            => array( $this, 'categories' ),
			'permission_callback' => '__return_true',
		) );
		register_rest_route( $ns, '/api/blogs/(?P<slug>[a-zA-Z0-9\-_%]+)', array(
			'methods'             => 'GET',
			'callback'            => array( $this, 'get_blog' ),
			'permission_callback' => '__return_true',
		) );
		register_rest_route( $ns, '/api/blogs/(?P<slug>[a-zA-Z0-9\-_%]+)/related', array(
			'methods'             => 'GET',
			'callback'            => array( $this, 'related' ),
			'permission_callback' => '__return_true',
		) );

		// Admin.
		register_rest_route( $ns, '/api/admin/blogs', array(
			array(
				'methods'             => 'GET',
				'callback'            => array( $this, 'admin_list' ),
				'permission_callback' => array( 'CB_Plugin', 'require_admin' ),
			),
			array(
				'methods'             => 'POST',
				'callback'            => array( $this, 'create_blog' ),
				'permission_callback' => array( 'CB_Plugin', 'require_admin' ),
			),
		) );
		register_rest_route( $ns, '/api/admin/blogs/categories', array(
			'methods'             => 'POST',
			'callback'            => array( $this, 'create_category' ),
			'permission_callback' => array( 'CB_Plugin', 'require_admin' ),
		) );
		register_rest_route( $ns, '/api/admin/blogs/categories/(?P<id>\d+)', array(
			array(
				'methods'             => 'PUT',
				'callback'            => array( $this, 'update_category' ),
				'permission_callback' => array( 'CB_Plugin', 'require_admin' ),
			),
			array(
				'methods'             => 'DELETE',
				'callback'            => array( $this, 'delete_category' ),
				'permission_callback' => array( 'CB_Plugin', 'require_admin' ),
			),
		) );
		register_rest_route( $ns, '/api/admin/blogs/(?P<slug>[a-zA-Z0-9\-_%]+)', array(
			'methods'             => 'GET',
			'callback'            => array( $this, 'admin_get' ),
			'permission_callback' => array( 'CB_Plugin', 'require_admin' ),
		) );
		register_rest_route( $ns, '/api/admin/blogs/(?P<id>\d+)', array(
			array(
				'methods'             => 'PUT',
				'callback'            => array( $this, 'update_blog' ),
				'permission_callback' => array( 'CB_Plugin', 'require_admin' ),
			),
			array(
				'methods'             => 'DELETE',
				'callback'            => array( $this, 'delete_blog' ),
				'permission_callback' => array( 'CB_Plugin', 'require_admin' ),
			),
		) );
	}

	// ---- public read --------------------------------------------------------

	public function list_blogs( WP_REST_Request $request ) {
		return cb_response( $this->query_blogs( $request, array( 'publish' ) ), 200 );
	}

	public function admin_list( WP_REST_Request $request ) {
		return cb_response( $this->query_blogs( $request, array( 'publish', 'draft', 'pending', 'private' ) ), 200 );
	}

	private function query_blogs( WP_REST_Request $request, array $statuses ): array {
		$page = max( 0, (int) $request->get_param( 'page' ) );
		$size = min( 100, max( 1, (int) ( $request->get_param( 'size' ) ?: 10 ) ) );

		$args = array(
			'post_type'      => 'post',
			'post_status'    => $statuses,
			'posts_per_page' => $size,
			'paged'          => $page + 1,
		);
		if ( $s = $request->get_param( 'search' ) ) {
			$args['s'] = sanitize_text_field( $s );
		}
		if ( $cat = $request->get_param( 'categoryId' ) ) {
			$args['cat'] = (int) $cat;
		}

		$query = new WP_Query( $args );
		$items = array();
		foreach ( $query->posts as $post ) {
			$items[] = $this->blog_response( $post, false );
		}
		return cb_page( $items, $page, $size, (int) $query->found_posts, (int) $query->max_num_pages );
	}

	public function featured( WP_REST_Request $request ) {
		$size  = min( 100, max( 1, (int) ( $request->get_param( 'size' ) ?: 10 ) ) );
		$query = new WP_Query( array(
			'post_type'      => 'post',
			'post_status'    => 'publish',
			'posts_per_page' => $size,
			'meta_key'       => 'cb_is_featured',
			'meta_value'     => '1',
		) );
		$items = array();
		foreach ( $query->posts as $post ) {
			$items[] = $this->blog_response( $post, false );
		}
		return cb_response( cb_page( $items, 0, $size, (int) $query->found_posts, (int) $query->max_num_pages ), 200 );
	}

	public function get_blog( WP_REST_Request $request ) {
		$post = $this->find_post( (string) $request['slug'], array( 'publish' ) );
		if ( ! $post ) {
			return cb_error( 'مقاله یافت نشد.', 404, 'BLOG_NOT_FOUND' );
		}
		// Increment view count.
		$views = (int) get_post_meta( $post->ID, 'cb_view_count', true );
		update_post_meta( $post->ID, 'cb_view_count', $views + 1 );

		return cb_response( $this->blog_response( $post, true ), 200 );
	}

	public function admin_get( WP_REST_Request $request ) {
		$post = $this->find_post( (string) $request['slug'], array( 'publish', 'draft', 'pending', 'private' ) );
		if ( ! $post ) {
			return cb_error( 'مقاله یافت نشد.', 404, 'BLOG_NOT_FOUND' );
		}
		return cb_response( $this->blog_response( $post, true ), 200 );
	}

	public function related( WP_REST_Request $request ) {
		$post = $this->find_post( (string) $request['slug'], array( 'publish' ) );
		if ( ! $post ) {
			return cb_response( array(), 200 );
		}
		$cats  = wp_get_post_categories( $post->ID );
		$query = new WP_Query( array(
			'post_type'      => 'post',
			'post_status'    => 'publish',
			'posts_per_page' => 4,
			'post__not_in'   => array( $post->ID ),
			'category__in'   => $cats ?: array(),
		) );
		$out = array();
		foreach ( $query->posts as $p ) {
			$out[] = $this->blog_response( $p, false );
		}
		return cb_response( $out, 200 );
	}

	public function categories( WP_REST_Request $request ) {
		$terms = get_terms( array( 'taxonomy' => 'category', 'hide_empty' => false ) );
		$out   = array();
		if ( ! is_wp_error( $terms ) ) {
			foreach ( $terms as $t ) {
				$out[] = array(
					'id'           => (int) $t->term_id,
					'name'         => $t->name,
					'slug'         => $t->slug,
					'description'  => $t->description ?: null,
					'thumbnailUrl' => null,
					'blogCount'    => (int) $t->count,
				);
			}
		}
		return cb_response( $out, 200 );
	}

	// ---- admin write --------------------------------------------------------

	public function create_blog( WP_REST_Request $request ) {
		$title = (string) $request->get_param( 'title' );
		if ( trim( $title ) === '' ) {
			return cb_error( 'عنوان مقاله الزامی است.', 400, 'VALIDATION' );
		}
		$blocks = $request->get_param( 'content' );
		$blocks = is_array( $blocks ) ? $blocks : array();

		$post_id = wp_insert_post( array(
			'post_type'    => 'post',
			'post_title'   => sanitize_text_field( $title ),
			'post_content' => CB_Blocks::blocks_to_html( $blocks ),
			'post_excerpt' => sanitize_textarea_field( (string) $request->get_param( 'summary' ) ),
			'post_status'  => $this->map_status_in( (string) ( $request->get_param( 'status' ) ?: 'PUBLISHED' ) ),
		), true );

		if ( is_wp_error( $post_id ) ) {
			return cb_error( $post_id->get_error_message(), 400, 'CREATE_FAILED' );
		}
		$this->apply_blog_meta( $post_id, $request, true );

		return cb_response( $this->blog_response( get_post( $post_id ), true ), 201 );
	}

	public function update_blog( WP_REST_Request $request ) {
		$post = get_post( (int) $request['id'] );
		if ( ! $post || $post->post_type !== 'post' ) {
			return cb_error( 'مقاله یافت نشد.', 404, 'BLOG_NOT_FOUND' );
		}
		$update = array( 'ID' => $post->ID );

		if ( ( $title = $request->get_param( 'title' ) ) !== null ) {
			$update['post_title'] = sanitize_text_field( $title );
		}
		if ( ( $content = $request->get_param( 'content' ) ) !== null && is_array( $content ) ) {
			$update['post_content'] = CB_Blocks::blocks_to_html( $content );
		}
		if ( ( $summary = $request->get_param( 'summary' ) ) !== null ) {
			$update['post_excerpt'] = sanitize_textarea_field( $summary );
		}
		if ( ( $status = $request->get_param( 'status' ) ) !== null ) {
			$update['post_status'] = $this->map_status_in( (string) $status );
		}
		wp_update_post( $update );
		$this->apply_blog_meta( $post->ID, $request, false );

		return cb_response( $this->blog_response( get_post( $post->ID ), true ), 200 );
	}

	public function delete_blog( WP_REST_Request $request ) {
		$post = get_post( (int) $request['id'] );
		if ( ! $post || $post->post_type !== 'post' ) {
			return cb_error( 'مقاله یافت نشد.', 404, 'BLOG_NOT_FOUND' );
		}
		wp_delete_post( $post->ID, true );
		return new WP_REST_Response( null, 204 );
	}

	private function apply_blog_meta( int $post_id, WP_REST_Request $request, bool $is_create ): void {
		if ( ( $thumb = $request->get_param( 'thumbnailUrl' ) ) !== null && $thumb !== '' ) {
			$attachment_id = attachment_url_to_postid( $thumb );
			if ( $attachment_id ) {
				set_post_thumbnail( $post_id, $attachment_id );
			}
			update_post_meta( $post_id, 'cb_thumbnail_url', esc_url_raw( $thumb ) );
		}
		if ( ( $cat = $request->get_param( 'categoryId' ) ) !== null && $cat ) {
			wp_set_post_categories( $post_id, array( (int) $cat ) );
		}
		if ( ( $featured = $request->get_param( 'isFeatured' ) ) !== null ) {
			update_post_meta( $post_id, 'cb_is_featured', ( $featured === true || $featured === 'true' ) ? '1' : '0' );
		}
		if ( ( $mt = $request->get_param( 'metaTitle' ) ) !== null ) {
			update_post_meta( $post_id, 'cb_meta_title', sanitize_text_field( $mt ) );
		}
		if ( ( $md = $request->get_param( 'metaDescription' ) ) !== null ) {
			update_post_meta( $post_id, 'cb_meta_description', sanitize_textarea_field( $md ) );
		}
	}

	// ---- blog categories CRUD ----------------------------------------------

	public function create_category( WP_REST_Request $request ) {
		$name = (string) $request->get_param( 'name' );
		if ( trim( $name ) === '' ) {
			return cb_error( 'نام دسته الزامی است.', 400, 'VALIDATION' );
		}
		$result = wp_insert_term( sanitize_text_field( $name ), 'category', array(
			'description' => sanitize_textarea_field( (string) $request->get_param( 'description' ) ),
		) );
		if ( is_wp_error( $result ) ) {
			return cb_error( $result->get_error_message(), 400, 'CATEGORY_FAILED' );
		}
		return cb_response( $this->category_response( get_term( $result['term_id'], 'category' ) ), 201 );
	}

	public function update_category( WP_REST_Request $request ) {
		$id     = (int) $request['id'];
		$fields = array();
		if ( ( $name = $request->get_param( 'name' ) ) !== null ) {
			$fields['name'] = sanitize_text_field( $name );
		}
		if ( ( $desc = $request->get_param( 'description' ) ) !== null ) {
			$fields['description'] = sanitize_textarea_field( $desc );
		}
		$result = wp_update_term( $id, 'category', $fields );
		if ( is_wp_error( $result ) ) {
			return cb_error( $result->get_error_message(), 400, 'CATEGORY_FAILED' );
		}
		return cb_response( $this->category_response( get_term( $id, 'category' ) ), 200 );
	}

	public function delete_category( WP_REST_Request $request ) {
		wp_delete_term( (int) $request['id'], 'category' );
		return new WP_REST_Response( null, 204 );
	}

	private function category_response( $term ): array {
		return array(
			'id'           => (int) $term->term_id,
			'name'         => $term->name,
			'slug'         => $term->slug,
			'description'  => $term->description ?: null,
			'thumbnailUrl' => null,
			'blogCount'    => (int) $term->count,
		);
	}

	// ---- builders -----------------------------------------------------------

	private function find_post( string $slug, array $statuses ) {
		$slug  = rawurldecode( $slug );
		$query = new WP_Query( array(
			'post_type'      => 'post',
			'name'           => $slug,
			'post_status'    => $statuses,
			'posts_per_page' => 1,
		) );
		return $query->have_posts() ? $query->posts[0] : null;
	}

	private function blog_response( WP_Post $post, bool $with_content ): array {
		$author  = get_userdata( $post->post_author );
		$cats    = wp_get_post_categories( $post->ID, array( 'fields' => 'all' ) );
		$cat     = ! empty( $cats ) ? $cats[0] : null;
		$thumb   = get_the_post_thumbnail_url( $post->ID, 'full' ) ?: ( get_post_meta( $post->ID, 'cb_thumbnail_url', true ) ?: null );

		$data = array(
			'id'                 => (int) $post->ID,
			'title'              => get_the_title( $post ),
			'slug'               => $post->post_name,
			'summary'            => $post->post_excerpt ?: null,
			'thumbnailUrl'       => $thumb,
			'viewCount'          => (int) get_post_meta( $post->ID, 'cb_view_count', true ),
			'readingTimeMinutes' => $this->reading_time( $post ),
			'authorName'         => $author ? $author->display_name : null,
			'author'             => $author ? array( 'id' => (int) $author->ID, 'name' => $author->display_name ) : null,
			'categoryName'       => $cat ? $cat->name : null,
			'category'           => $cat ? $this->category_response( $cat ) : null,
			'featured'           => get_post_meta( $post->ID, 'cb_is_featured', true ) === '1',
			'status'             => $this->map_status_out( $post->post_status ),
			'metaTitle'          => get_post_meta( $post->ID, 'cb_meta_title', true ) ?: null,
			'metaDescription'    => get_post_meta( $post->ID, 'cb_meta_description', true ) ?: null,
			'createdAt'          => cb_iso( $post->post_date_gmt ),
			'updatedAt'          => cb_iso( $post->post_modified_gmt ),
		);

		if ( $with_content ) {
			$data['content'] = CB_Blocks::post_to_blocks( $post );
		}
		return $data;
	}

	private function reading_time( WP_Post $post ): int {
		$words = str_word_count( wp_strip_all_tags( $post->post_content ) );
		// Persian text isn't space-delimited the same way; fall back to char estimate.
		if ( $words < 5 ) {
			$words = (int) ( mb_strlen( wp_strip_all_tags( $post->post_content ) ) / 5 );
		}
		return max( 1, (int) ceil( $words / 200 ) );
	}

	private function map_status_in( string $status ): string {
		return strtoupper( $status ) === 'DRAFT' ? 'draft' : 'publish';
	}

	private function map_status_out( string $status ): string {
		return $status === 'publish' ? 'PUBLISHED' : 'DRAFT';
	}
}
