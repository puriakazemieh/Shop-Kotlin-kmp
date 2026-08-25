<?php
/**
 * Psychology-test vertical — data-driven, theme-only (no plugin).
 * Questions and score ranges are stored as simple line-based meta (edited in the
 * admin meta box) and parsed here; scoring + interpretation run server-side via a
 * theme REST route so option scores are never exposed to the client.
 *
 * Question line:  «متن سؤال؟ | گزینه‌ی الف=۲ , گزینه‌ی ب=۱ , گزینه‌ی ج=۰»
 * Range line:     «۰ | ۵ | تفسیرِ این بازه»
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/** Persian/Arabic digits → integer. */
function carmilla_fa_to_int( $s ) {
	$map = array( '۰' => '0', '۱' => '1', '۲' => '2', '۳' => '3', '۴' => '4', '۵' => '5', '۶' => '6', '۷' => '7', '۸' => '8', '۹' => '9', '٬' => '', '،' => ',' );
	$s   = strtr( (string) $s, $map );
	return (int) preg_replace( '/[^0-9\-]/', '', $s );
}

/** Parse a test's questions into [ ['text'=>, 'options'=>[ ['label'=>,'score'=>], ... ] ], ... ]. */
function carmilla_psychtest_questions( $post_id ) {
	$raw   = (string) get_post_meta( $post_id, 'cb_questions', true );
	$lines = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $raw ) ) );
	$out   = array();
	foreach ( $lines as $line ) {
		$parts = explode( '|', $line, 2 );
		$text  = trim( $parts[0] );
		if ( '' === $text ) {
			continue;
		}
		$options = array();
		if ( isset( $parts[1] ) ) {
			foreach ( preg_split( '/[,،]/u', $parts[1] ) as $opt ) {
				$opt = trim( $opt );
				if ( '' === $opt ) {
					continue;
				}
				$pos = strrpos( $opt, '=' );
				if ( false === $pos ) {
					$options[] = array( 'label' => $opt, 'score' => 0 );
				} else {
					$options[] = array(
						'label' => trim( substr( $opt, 0, $pos ) ),
						'score' => carmilla_fa_to_int( substr( $opt, $pos + 1 ) ),
					);
				}
			}
		}
		$out[] = array( 'text' => $text, 'options' => $options );
	}
	return $out;
}

/** Parse ranges into [ ['min'=>,'max'=>,'text'=>], ... ]. */
function carmilla_psychtest_ranges( $post_id ) {
	$raw   = (string) get_post_meta( $post_id, 'cb_ranges', true );
	$lines = array_filter( array_map( 'trim', preg_split( '/\r\n|\r|\n/', $raw ) ) );
	$out   = array();
	foreach ( $lines as $line ) {
		$p = array_map( 'trim', explode( '|', $line ) );
		if ( count( $p ) < 3 ) {
			continue;
		}
		$out[] = array(
			'min'  => carmilla_fa_to_int( $p[0] ),
			'max'  => carmilla_fa_to_int( $p[1] ),
			'text' => $p[2],
		);
	}
	return $out;
}

/** Interpretation for a score (first matching range). */
function carmilla_psychtest_interpret( $post_id, $score ) {
	foreach ( carmilla_psychtest_ranges( $post_id ) as $r ) {
		if ( $score >= $r['min'] && $score <= $r['max'] ) {
			return $r['text'];
		}
	}
	return '';
}

/** Whether the current visitor may take the test (free, or bought the product). */
function carmilla_psychtest_accessible( $post_id ) {
	$slug = get_post_meta( $post_id, 'cb_product_slug', true );
	if ( ! $slug || ! function_exists( 'wc_get_product_id_by_sku' ) ) {
		return true; // free / no WooCommerce gating
	}
	if ( ! is_user_logged_in() ) {
		return false;
	}
	$product = get_page_by_path( $slug, OBJECT, 'product' );
	if ( ! $product ) {
		return true;
	}
	return function_exists( 'wc_customer_bought_product' )
		? wc_customer_bought_product( '', get_current_user_id(), $product->ID )
		: true;
}

add_action( 'rest_api_init', function () {
	register_rest_route( 'carmilla/v1', '/psych-tests/(?P<id>\d+)/submit', array(
		'methods'             => 'POST',
		'callback'            => 'carmilla_rest_submit_psychtest',
		'permission_callback' => '__return_true',
	) );
} );

function carmilla_rest_submit_psychtest( WP_REST_Request $req ) {
	$id = (int) $req['id'];
	if ( get_post_type( $id ) !== 'cb_psychtest' ) {
		return new WP_Error( 'not_found', 'تست یافت نشد.', array( 'status' => 404 ) );
	}
	if ( ! carmilla_psychtest_accessible( $id ) ) {
		return new WP_Error( 'forbidden', 'برای انجام این تست باید آن را خریداری کنید.', array( 'status' => 403 ) );
	}
	$questions = carmilla_psychtest_questions( $id );
	$answers   = (array) $req->get_param( 'answers' ); // question index => chosen option index
	$score     = 0;
	$max       = 0;
	foreach ( $questions as $qi => $q ) {
		$scores = array_map( function ( $o ) { return (int) $o['score']; }, $q['options'] );
		$max   += $scores ? max( $scores ) : 0;
		$choice = isset( $answers[ $qi ] ) ? (int) $answers[ $qi ] : -1;
		if ( isset( $q['options'][ $choice ] ) ) {
			$score += (int) $q['options'][ $choice ]['score'];
		}
	}
	return rest_ensure_response( array(
		'score'          => $score,
		'maxScore'       => $max,
		'interpretation' => carmilla_psychtest_interpret( $id, $score ),
	) );
}
