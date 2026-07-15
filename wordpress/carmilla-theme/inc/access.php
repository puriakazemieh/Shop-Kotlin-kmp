<?php
/**
 * Purchase → access bridge (WooCommerce). On a completed order:
 *   - a therapist's linked product grants session credits (consumed on booking),
 *   - course/test access is already covered by wc_customer_bought_product.
 * Theme-only; degrades to no-op without WooCommerce.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/** First published post of $type whose cb_product_slug meta equals $slug. */
function carmilla_find_by_product_slug( $type, $slug ) {
	if ( ! $slug || ! post_type_exists( $type ) ) {
		return 0;
	}
	$ids = get_posts( array(
		'post_type'      => $type,
		'post_status'    => 'publish',
		'posts_per_page' => 1,
		'fields'         => 'ids',
		'meta_key'       => 'cb_product_slug',
		'meta_value'     => $slug,
	) );
	return $ids ? (int) $ids[0] : 0;
}

/** Session credits a user holds for a therapist. */
function carmilla_therapist_credits( $therapist_id, $user_id = 0 ) {
	$user_id = $user_id ?: get_current_user_id();
	return $user_id ? max( 0, (int) get_user_meta( $user_id, "cb_ther_credits_$therapist_id", true ) ) : 0;
}

function carmilla_add_therapist_credit( $therapist_id, $user_id, $qty = 1 ) {
	$new = carmilla_therapist_credits( $therapist_id, $user_id ) + (int) $qty;
	update_user_meta( $user_id, "cb_ther_credits_$therapist_id", $new );
	return $new;
}

function carmilla_spend_therapist_credit( $therapist_id, $user_id ) {
	$cur = carmilla_therapist_credits( $therapist_id, $user_id );
	if ( $cur <= 0 ) {
		return false;
	}
	update_user_meta( $user_id, "cb_ther_credits_$therapist_id", $cur - 1 );
	return true;
}

/** Grant access when an order is paid/completed. */
function carmilla_grant_access_for_order( $order_id ) {
	if ( ! function_exists( 'wc_get_order' ) ) {
		return;
	}
	$order = wc_get_order( $order_id );
	if ( ! $order ) {
		return;
	}
	$uid = $order->get_user_id();
	if ( ! $uid ) {
		return;
	}
	if ( $order->get_meta( '_carmilla_access_granted' ) ) {
		return; // idempotent
	}
	foreach ( $order->get_items() as $item ) {
		$product = $item->get_product();
		if ( ! $product ) {
			continue;
		}
		$slug = $product->get_slug();
		$qty  = max( 1, (int) $item->get_quantity() );

		$tid = carmilla_find_by_product_slug( 'cb_therapist', $slug );
		if ( $tid ) {
			carmilla_add_therapist_credit( $tid, $uid, $qty );
		}
		// Courses & tests: access is derived from the purchase itself
		// (carmilla_course_accessible / carmilla_psychtest_accessible).
	}
	$order->update_meta_data( '_carmilla_access_granted', 1 );
	$order->save();
}
add_action( 'woocommerce_order_status_completed', 'carmilla_grant_access_for_order' );
add_action( 'woocommerce_payment_complete', 'carmilla_grant_access_for_order' );
