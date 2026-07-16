<?php
/**
 * Stories — read the cb_story CPT into the app's StoryResponse. Only active,
 * non-expired stories are returned, newest first.
 *
 *   GET api/stories -> List<StoryResponse>
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Story_Controller {

	public function register_routes(): void {
		register_rest_route( CB_REST_NAMESPACE, '/api/stories', array(
			'methods'             => 'GET',
			'callback'            => array( $this, 'stories' ),
			'permission_callback' => '__return_true',
		) );
	}

	public function stories(): WP_REST_Response {
		$posts = get_posts( array( 'post_type' => 'cb_story', 'post_status' => 'publish', 'numberposts' => 50, 'orderby' => 'date', 'order' => 'DESC' ) );
		$now   = time();
		$out   = array();
		foreach ( $posts as $p ) {
			$id      = (int) $p->ID;
			$active  = get_post_meta( $id, 'is_active', true );
			if ( $active === '0' || $active === false ) {
				// treat unset as active; only explicit "0" hides it
				if ( $active === '0' ) {
					continue;
				}
			}
			$expires = get_post_meta( $id, 'expires_at', true );
			if ( $expires && strtotime( $expires ) && strtotime( $expires ) < $now ) {
				continue;
			}
			$media = get_post_meta( $id, 'media_url', true );
			if ( ! $media && has_post_thumbnail( $id ) ) {
				$media = get_the_post_thumbnail_url( $id, 'large' );
			}
			$out[] = array(
				'id'         => $id,
				'mediaUrl'   => (string) $media,
				'mediaType'  => get_post_meta( $id, 'media_type', true ) ?: 'IMAGE',
				'productId'  => ( $pid = (int) get_post_meta( $id, 'product_id', true ) ) ? $pid : null,
				'linkType'   => get_post_meta( $id, 'link_type', true ) ?: 'NONE',
				'categoryId' => ( $cid = (int) get_post_meta( $id, 'category_id', true ) ) ? $cid : null,
				'blogSlug'   => get_post_meta( $id, 'blog_slug', true ) ?: null,
				'title'      => get_the_title( $p ) ?: null,
				'createdAt'  => cb_iso( $p->post_date_gmt ),
			);
		}
		return cb_response( $out );
	}
}
