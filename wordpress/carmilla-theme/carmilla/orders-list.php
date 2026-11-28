<?php
/**
 * Carmilla Theme override for shared orders list (`carmilla/orders-list.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$orders = isset( $carmilla_orders_data ) && is_array( $carmilla_orders_data ) ? $carmilla_orders_data : array();
?>
<section class="card carmilla-theme-orders" data-carmilla-surface="orders">
	<h2 class="t-headline"><?php esc_html_e( 'سفارش‌های من', 'carmilla' ); ?></h2>
	<ul class="carmilla-orders-list">
		<?php foreach ( $orders as $o ) : ?>
			<li data-order-id="<?php echo esc_attr( (string) ( $o['id'] ?? 0 ) ); ?>">#<?php echo esc_html( (string) ( $o['id'] ?? 0 ) ); ?> — <?php echo esc_html( (string) ( $o['totalPrice'] ?? 0 ) ); ?></li>
		<?php endforeach; ?>
	</ul>
</section>
