<?php
/**
 * Media upload endpoint used by the app's block editor / product image picker.
 *   POST api/admin/blogs/media/upload   (multipart field: file) -> { url }
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

class CB_Media_Controller {

	public function register_routes(): void {
		register_rest_route( CB_REST_NAMESPACE, '/api/admin/blogs/media/upload', array(
			'methods'             => 'POST',
			'callback'            => array( $this, 'upload' ),
			'permission_callback' => array( 'CB_Plugin', 'require_admin' ),
		) );
	}

	public function upload( WP_REST_Request $request ) {
		$files = $request->get_file_params();
		if ( empty( $files['file'] ) ) {
			return cb_error( 'فایلی ارسال نشده است.', 400, 'NO_FILE' );
		}

		require_once ABSPATH . 'wp-admin/includes/file.php';
		require_once ABSPATH . 'wp-admin/includes/media.php';
		require_once ABSPATH . 'wp-admin/includes/image.php';

		// media_handle_sideload expects the file in $_FILES; use the provided upload.
		$_FILES['file'] = $files['file'];
		$attachment_id  = media_handle_upload( 'file', 0 );

		if ( is_wp_error( $attachment_id ) ) {
			return cb_error( $attachment_id->get_error_message(), 400, 'UPLOAD_FAILED' );
		}

		return cb_response( array( 'url' => wp_get_attachment_url( $attachment_id ) ), 200 );
	}
}
