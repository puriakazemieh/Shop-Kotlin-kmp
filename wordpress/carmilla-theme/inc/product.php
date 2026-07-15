<?php
/**
 * Single product parity with the app's DetailsScreen (single-scroll, no tabs):
 *   gallery → summary → intro → spec card → rating summary → reviews (+images)
 *   → product Q&A (separate from reviews).
 * Everything is theme-only and built on native WooCommerce/WordPress comment APIs.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/* -------------------------------------------------------------------------
 * 1. Replace WooCommerce's tabbed data area with an inline, app-like layout.
 * ---------------------------------------------------------------------- */
add_action( 'after_setup_theme', function () {
	// Drop the default tabs; we render description/specs/reviews inline instead.
	remove_action( 'woocommerce_after_single_product_summary', 'woocommerce_output_product_data_tabs', 10 );
} );

add_action( 'woocommerce_after_single_product_summary', 'carmilla_product_intro', 12 );
add_action( 'woocommerce_after_single_product_summary', 'carmilla_product_specs', 14 );
add_action( 'woocommerce_after_single_product_summary', 'carmilla_product_reviews', 16 );
add_action( 'woocommerce_after_single_product_summary', 'carmilla_product_qna', 18 );

/** «معرفی محصول» — description rendered as checkmark bullets (mirrors the app). */
function carmilla_product_intro() {
	global $product;
	$desc = $product ? $product->get_description() : '';
	if ( ! $desc ) {
		return;
	}
	$lines = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', wp_strip_all_tags( $desc ) ) ) );
	echo '<section class="cb-psection"><h2 class="t-title-lg cb-psection__h">' . esc_html__( 'معرفی محصول', 'carmilla' ) . '</h2>';
	if ( $lines ) {
		echo '<ul class="cb-bullets">';
		foreach ( $lines as $line ) {
			echo '<li>' . carmilla_icon( 'check' ) . '<span>' . esc_html( $line ) . '</span></li>';
		}
		echo '</ul>';
	} else {
		echo '<div class="t-body">' . wp_kses_post( wpautop( $desc ) ) . '</div>';
	}
	echo '</section>';
}

/** «مشخصات» — brand meta + product attributes as a two-column spec card. */
function carmilla_product_specs() {
	global $product;
	if ( ! $product ) {
		return;
	}
	$specs = array();

	$brand = get_post_meta( $product->get_id(), 'cb_brand', true );
	if ( $brand ) {
		$specs[ __( 'برند', 'carmilla' ) ] = $brand;
	}

	foreach ( $product->get_attributes() as $attribute ) {
		$name   = wc_attribute_label( $attribute->get_name() );
		$values = $attribute->is_taxonomy()
			? wc_get_product_terms( $product->get_id(), $attribute->get_name(), array( 'fields' => 'names' ) )
			: $attribute->get_options();
		$values = array_filter( array_map( 'trim', (array) $values ) );
		if ( $values ) {
			$specs[ $name ] = implode( '، ', $values );
		}
	}

	if ( ! $specs ) {
		$cats = wc_get_product_category_list( $product->get_id() );
		if ( $cats ) {
			$specs[ __( 'دسته‌بندی', 'carmilla' ) ] = wp_strip_all_tags( $cats );
		}
		$specs[ __( 'وضعیت موجودی', 'carmilla' ) ] = $product->is_in_stock()
			? __( 'موجود در انبار', 'carmilla' )
			: __( 'ناموجود', 'carmilla' );
	}

	echo '<section class="cb-psection"><h2 class="t-title-lg cb-psection__h">' . esc_html__( 'مشخصات', 'carmilla' ) . '</h2>';
	echo '<div class="card cb-specs">';
	foreach ( $specs as $k => $v ) {
		echo '<div class="cb-specs__row"><span class="cb-specs__k">' . esc_html( $k ) . '</span><span class="cb-specs__v">' . esc_html( $v ) . '</span></div>';
	}
	echo '</div></section>';
}

/* -------------------------------------------------------------------------
 * 2. Reviews: rating summary bar chart + review images.
 * ---------------------------------------------------------------------- */

/** «دیدگاه خریداران» — rating distribution (5→1) + native review list. */
function carmilla_product_reviews() {
	global $product;
	if ( ! $product || ! comments_open() ) {
		return;
	}
	echo '<section class="cb-psection" id="reviews">';
	echo '<h2 class="t-title-lg cb-psection__h">' . sprintf( esc_html__( 'دیدگاه خریداران (%s)', 'carmilla' ), esc_html( carmilla_to_persian_digits( (int) $product->get_review_count() ) ) ) . '</h2>';
	carmilla_rating_summary( $product );
	comments_template();
	echo '</section>';
}

/** Average + per-star bar chart, mirroring ReviewSummary in the app. */
function carmilla_rating_summary( $product ) {
	$count = (int) $product->get_review_count();
	if ( $count < 1 ) {
		return;
	}
	$comments = get_comments( array(
		'post_id' => $product->get_id(),
		'status'  => 'approve',
		'type'    => 'review',
	) );
	$dist = array( 5 => 0, 4 => 0, 3 => 0, 2 => 0, 1 => 0 );
	foreach ( $comments as $c ) {
		$r = (int) get_comment_meta( $c->comment_ID, 'rating', true );
		if ( isset( $dist[ $r ] ) ) {
			$dist[ $r ]++;
		}
	}
	$total = array_sum( $dist );
	$avg   = $product->get_average_rating();

	echo '<div class="card card--pad cb-ratesum">';
	echo '<div class="cb-ratesum__avg"><span class="cb-ratesum__num">' . esc_html( carmilla_to_persian_digits( number_format_i18n( (float) $avg, 1 ) ) ) . '</span>';
	echo '<span class="cb-stars" aria-hidden="true">' . str_repeat( carmilla_icon( 'star' ), 5 ) . '</span>';
	echo '<span class="t-body-sm t-muted">' . sprintf( esc_html__( 'از %s نظر', 'carmilla' ), esc_html( carmilla_to_persian_digits( $total ) ) ) . '</span></div>';
	echo '<div class="cb-ratesum__bars">';
	for ( $s = 5; $s >= 1; $s-- ) {
		$pct = $total ? round( $dist[ $s ] * 100 / $total ) : 0;
		echo '<div class="cb-bar"><span class="cb-bar__label">' . esc_html( carmilla_to_persian_digits( $s ) ) . '</span>';
		echo '<span class="cb-bar__track"><span class="cb-bar__fill" style="width:' . esc_attr( $pct ) . '%"></span></span>';
		echo '<span class="cb-bar__count">' . esc_html( carmilla_to_persian_digits( $dist[ $s ] ) ) . '</span></div>';
	}
	echo '</div></div>';
}

/** Allow image uploads on the review form (enctype + file input). */
add_filter( 'comment_form_defaults', function ( $defaults ) {
	if ( function_exists( 'is_product' ) && is_product() ) {
		$defaults['format'] = 'html5';
	}
	return $defaults;
} );

add_filter( 'woocommerce_product_review_comment_form_args', function ( $args ) {
	$field  = '<p class="comment-form-image cb-review-upload">';
	$field .= '<label for="carmilla_review_image">' . esc_html__( 'افزودن تصویر (اختیاری)', 'carmilla' ) . '</label>';
	$field .= '<input type="file" name="carmilla_review_image" id="carmilla_review_image" accept="image/*"></p>';
	if ( isset( $args['comment_field'] ) ) {
		$args['comment_field'] .= $field;
	}
	return $args;
} );

/** Add multipart enctype so the file input actually uploads. */
add_action( 'comment_form_top', function () {
	if ( function_exists( 'is_product' ) && is_product() ) {
		echo '<script>document.addEventListener("DOMContentLoaded",function(){var f=document.querySelector("#commentform,#respond form");if(f)f.setAttribute("enctype","multipart/form-data");});</script>';
	}
} );

/** Store an uploaded review image as an attachment on the comment. */
add_action( 'comment_post', function ( $comment_id ) {
	if ( empty( $_FILES['carmilla_review_image']['name'] ) ) {
		return;
	}
	if ( ! function_exists( 'media_handle_upload' ) ) {
		require_once ABSPATH . 'wp-admin/includes/image.php';
		require_once ABSPATH . 'wp-admin/includes/file.php';
		require_once ABSPATH . 'wp-admin/includes/media.php';
	}
	$type = isset( $_FILES['carmilla_review_image']['type'] ) ? sanitize_text_field( wp_unslash( $_FILES['carmilla_review_image']['type'] ) ) : '';
	if ( strpos( $type, 'image/' ) !== 0 ) {
		return;
	}
	$attach_id = media_handle_upload( 'carmilla_review_image', 0 );
	if ( ! is_wp_error( $attach_id ) ) {
		add_comment_meta( $comment_id, 'carmilla_review_image', (int) $attach_id, true );
	}
}, 10, 1 );

/** Render the review image beneath the review text. */
add_action( 'woocommerce_review_after_comment_text', function ( $comment ) {
	$attach_id = (int) get_comment_meta( $comment->comment_ID, 'carmilla_review_image', true );
	if ( $attach_id ) {
		$img = wp_get_attachment_image( $attach_id, 'medium', false, array( 'class' => 'cb-review-img', 'loading' => 'lazy' ) );
		if ( $img ) {
			echo '<div class="cb-review-imgwrap">' . $img . '</div>'; // phpcs:ignore
		}
	}
} );

/* -------------------------------------------------------------------------
 * 3. Product Q&A (separate from reviews) — comment_type cb_qna + theme REST.
 * ---------------------------------------------------------------------- */

/** One Q&A entry (+ nested answers) as a DTO. */
function carmilla_qna_dto( $c ) {
	return array(
		'id'      => (int) $c->comment_ID,
		'author'  => $c->comment_author ? $c->comment_author : __( 'کاربر', 'carmilla' ),
		'content' => $c->comment_content,
		'time'    => get_comment_date( 'c', $c ),
		'isStaff' => (bool) get_comment_meta( $c->comment_ID, 'cb_qna_staff', true ),
	);
}

function carmilla_qna_list( $product_id ) {
	$roots = get_comments( array(
		'post_id' => $product_id,
		'type'    => 'cb_qna',
		'parent'  => 0,
		'status'  => 'approve',
		'order'   => 'DESC',
	) );
	$out = array();
	foreach ( $roots as $q ) {
		$answers = get_comments( array(
			'post_id' => $product_id,
			'type'    => 'cb_qna',
			'parent'  => $q->comment_ID,
			'status'  => 'approve',
			'order'   => 'ASC',
		) );
		$dto            = carmilla_qna_dto( $q );
		$dto['answers'] = array_map( 'carmilla_qna_dto', $answers );
		$out[]          = $dto;
	}
	return $out;
}

add_action( 'rest_api_init', function () {
	register_rest_route( 'carmilla/v1', '/products/(?P<id>\d+)/questions', array(
		array(
			'methods'             => 'GET',
			'callback'            => function ( $req ) {
				return rest_ensure_response( carmilla_qna_list( (int) $req['id'] ) );
			},
			'permission_callback' => '__return_true',
		),
		array(
			'methods'             => 'POST',
			'callback'            => 'carmilla_rest_post_question',
			'permission_callback' => 'is_user_logged_in',
		),
	) );
} );

function carmilla_rest_post_question( WP_REST_Request $req ) {
	$pid  = (int) $req['id'];
	$text = trim( wp_strip_all_tags( (string) $req->get_param( 'content' ) ) );
	$parent = (int) $req->get_param( 'parent' );

	if ( get_post_type( $pid ) !== 'product' || '' === $text ) {
		return new WP_Error( 'validation', 'پرسش نامعتبر است.', array( 'status' => 400 ) );
	}
	// Only staff may answer (post a reply to an existing question).
	$is_staff = current_user_can( 'edit_products' );
	if ( $parent > 0 && ! $is_staff ) {
		return new WP_Error( 'forbidden', 'اجازه‌ی پاسخ ندارید.', array( 'status' => 403 ) );
	}
	$user = wp_get_current_user();
	$cid  = wp_insert_comment( array(
		'comment_post_ID'      => $pid,
		'comment_parent'       => $parent,
		'comment_content'      => $text,
		'comment_type'         => 'cb_qna',
		'user_id'              => $user->ID,
		'comment_author'       => $user->display_name,
		'comment_author_email' => $user->user_email,
		'comment_approved'     => 1,
	) );
	if ( ! $cid ) {
		return new WP_Error( 'create_failed', 'ثبت نشد.', array( 'status' => 400 ) );
	}
	if ( $is_staff && $parent > 0 ) {
		add_comment_meta( $cid, 'cb_qna_staff', 1, true );
	}
	return rest_ensure_response( carmilla_qna_dto( get_comment( $cid ) ) );
}

/** Keep Q&A comments out of the normal review/comment counts and lists. */
add_filter( 'comments_clauses', function ( $clauses, $query ) {
	if ( ! is_admin() && empty( $query->query_vars['type'] ) && empty( $query->query_vars['include_unapproved'] ) ) {
		$clauses['where'] .= " AND comment_type != 'cb_qna'";
	}
	return $clauses;
}, 10, 2 );

/** Front-end Q&A container; JS (product-qna.js) fills it. */
function carmilla_product_qna() {
	global $product;
	if ( ! $product ) {
		return;
	}
	echo '<section class="cb-psection" id="qna" data-product="' . esc_attr( $product->get_id() ) . '">';
	echo '<h2 class="t-title-lg cb-psection__h">' . esc_html__( 'پرسش و پاسخ', 'carmilla' ) . '</h2>';
	echo '<div id="qna-list" class="cb-qna"></div>';
	echo '<div id="qna-form"></div>';
	echo '</section>';
}
