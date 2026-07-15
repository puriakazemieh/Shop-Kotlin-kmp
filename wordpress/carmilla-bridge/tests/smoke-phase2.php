<?php
/**
 * Dependency-free smoke test for Phase 2 pure-logic helpers (status mapping,
 * coupon math, address shaping). Run: php tests/smoke-phase2.php
 * Stubs the few WP/WC functions these helpers touch so no WordPress is needed.
 */

define( 'ABSPATH', __DIR__ . '/' );

function apply_filters( $tag, $value ) { return $value; }
function get_option( $k, $d = false ) { return $d; }

// Minimal WooCommerce stubs for cb_coupon_discount.
class WooCommerce {}
class WC_Coupon {
	private $code;
	public function __construct( $code ) { $this->code = $code; }
	public function get_id() { return $this->code === 'BAD' ? 0 : 123; }
	public function get_amount() {
		if ( $this->code === 'P10' ) return 10;   // 10 percent
		if ( $this->code === 'F500' ) return 500;  // fixed
		if ( $this->code === 'FP100' ) return 100;  // fixed per product
		return 0;
	}
	public function get_discount_type() {
		if ( $this->code === 'P10' ) return 'percent';
		if ( $this->code === 'FP100' ) return 'fixed_product';
		return 'fixed_cart';
	}
}

require dirname( __DIR__ ) . '/includes/helpers.php';

$fail = 0;
function check( $cond, $label ) {
	global $fail;
	echo ( $cond ? 'PASS' : 'FAIL' ) . "  $label\n";
	if ( ! $cond ) { $fail++; }
}

// ---- order status mapping ----
check( cb_order_status( 'pending' ) === 'AWAITING_PAYMENT', 'pending -> AWAITING_PAYMENT' );
check( cb_order_status( 'failed' ) === 'AWAITING_PAYMENT', 'failed -> AWAITING_PAYMENT' );
check( cb_order_status( 'on-hold' ) === 'PLACED', 'on-hold -> PLACED' );
check( cb_order_status( 'processing' ) === 'PROCESSING', 'processing -> PROCESSING' );
check( cb_order_status( 'completed' ) === 'COMPLETED', 'completed -> COMPLETED' );
check( cb_order_status( 'cancelled' ) === 'CANCELLED', 'cancelled -> CANCELLED' );
check( cb_order_status( 'refunded' ) === 'CANCELLED', 'refunded -> CANCELLED' );

// ---- coupon discount math ----
check( cb_coupon_discount( 'P10', 1000, 2 ) === 100.0, 'percent 10% of 1000 = 100' );
check( cb_coupon_discount( 'F500', 1000, 2 ) === 500.0, 'fixed_cart 500 = 500' );
check( cb_coupon_discount( 'F500', 300, 1 ) === 300.0, 'fixed_cart capped at subtotal' );
check( cb_coupon_discount( 'FP100', 1000, 3 ) === 300.0, 'fixed_product 100 x 3 qty = 300' );
check( cb_coupon_discount( 'BAD', 1000, 1 ) === 0.0, 'invalid coupon -> 0' );
check( cb_coupon_discount( null, 1000, 1 ) === 0.0, 'no code -> 0' );

// ---- address shaping ----
$stored = array(
	'id' => 5, 'receiverName' => 'علی', 'receiverPhone' => '0912', 'country' => 'IR',
	'province' => 'تهران', 'city' => 'تهران', 'addressLine1' => 'خیابان', 'addressLine2' => null,
	'postalCode' => '12345', 'default' => true, 'createdAt' => '2026-01-01T00:00:00+00:00',
);
$dto = cb_address_dto( $stored );
check( $dto['id'] === 5 && $dto['default'] === true, 'address dto id + default' );
check( $dto['receiverName'] === 'علی', 'address dto receiver' );
$snap = cb_address_snapshot( $stored );
check( ! isset( $snap['id'] ) && $snap['city'] === 'تهران', 'snapshot drops id, keeps city' );
$empty = cb_address_snapshot( null );
check( $empty['country'] === 'IR' && $empty['receiverName'] === '', 'snapshot defaults for null' );

echo "\n" . ( $fail === 0 ? 'ALL PASSED' : "$fail FAILED" ) . "\n";
exit( $fail === 0 ? 0 : 1 );
