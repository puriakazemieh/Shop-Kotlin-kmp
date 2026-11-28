<?php
/**
 * Carmilla Theme override for shared product detail (`carmilla/catalog-product.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$product = isset( $carmilla_product_data ) && is_array( $carmilla_product_data ) ? $carmilla_product_data : array();
?>
<article class="card carmilla-theme-product" data-carmilla-surface="product" data-product-id="<?php echo esc_attr( (string) ( $product['id'] ?? 0 ) ); ?>">
	<h2 class="t-headline"><?php echo esc_html( (string) ( $product['title'] ?? '' ) ); ?></h2>
	<div class="entry-content t-body"><?php echo wp_kses_post( (string) ( $product['description'] ?? '' ) ); ?></div>
</article>
