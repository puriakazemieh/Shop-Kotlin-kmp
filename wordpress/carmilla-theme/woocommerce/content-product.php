<?php
/**
 * WooCommerce loop item override → render the Carmilla product card.
 * Kept in sync with template-parts/card-product.php.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
global $product;
if ( empty( $product ) || ! $product->is_visible() ) {
	return;
}
get_template_part( 'template-parts/card', 'product' );
