<?php
/**
 * Carmilla Theme override for shared cart summary (`carmilla/cart-summary.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$cart = isset( $carmilla_cart_data ) && is_array( $carmilla_cart_data ) ? $carmilla_cart_data : array();
?>
<section class="card carmilla-theme-cart" data-carmilla-surface="cart" data-cart-total="<?php echo esc_attr( (string) ( $cart['total'] ?? 0 ) ); ?>">
	<h2 class="t-headline"><?php esc_html_e( 'سبد خرید', 'carmilla' ); ?></h2>
	<p class="t-body"><?php echo esc_html( sprintf( 'تعداد اقلام: %d | جمع کل: %s', (int) ( $cart['totalQty'] ?? 0 ), (string) ( $cart['total'] ?? 0 ) ) ); ?></p>
</section>
