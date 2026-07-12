<?php
/**
 * Shared helpers for shaping REST responses to match the Carmilla app DTO contract.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/**
 * Return a WP_REST_Response with the given payload and status.
 */
function cb_response( $data, int $status = 200 ) {
	return new WP_REST_Response( $data, $status );
}

/**
 * Return an error payload shaped like the server's ApiError
 * ({ message, status, code, errorCode, path, timestamp }) so the app's
 * ApiException decoder keeps working unchanged.
 */
function cb_error( string $message, int $status = 400, string $error_code = 'ERROR', string $path = '' ) {
	return new WP_REST_Response(
		array(
			'message'   => $message,
			'status'    => $status,
			'code'      => $status,
			'errorCode' => $error_code,
			'path'      => $path,
			'timestamp' => gmdate( 'c' ),
		),
		$status
	);
}

/**
 * Cast a WooCommerce price string to a nullable float (null when empty).
 */
function cb_price( $value ): ?float {
	if ( $value === '' || $value === null ) {
		return null;
	}
	return (float) $value;
}

/**
 * ISO-8601 timestamp from a WP date string / post.
 */
function cb_iso( $date ): string {
	if ( empty( $date ) ) {
		return gmdate( 'c' );
	}
	$ts = is_numeric( $date ) ? (int) $date : strtotime( $date );
	return gmdate( 'c', $ts ?: time() );
}

/**
 * Standard Spring-style Page<T> wrapper: { content, page:{size,number,totalElements,totalPages} }.
 */
function cb_page( array $content, int $page, int $size, int $total_elements, int $total_pages ): array {
	return array(
		'content' => $content,
		'page'    => array(
			'size'          => $size,
			'number'        => $page,
			'totalElements' => $total_elements,
			'totalPages'    => $total_pages,
		),
		// Backward-compat root fields (BlogListResponse reads these too).
		'totalPages'    => $total_pages,
		'totalElements' => $total_elements,
	);
}

/**
 * Whether WooCommerce is active.
 */
function cb_woo_active(): bool {
	return class_exists( 'WooCommerce' );
}
