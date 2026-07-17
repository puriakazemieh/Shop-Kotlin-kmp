<?php
/**
 * WooCommerce loop item override → render the faithful Carmilla product card.
 * Mirrors the product card in docs/design-reference/*.html.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
global $product;
if ( empty( $product ) || ! $product->is_visible() ) {
	return;
}
carmilla_dc_product_card( $product );
