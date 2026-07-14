<?php
/**
 * Product card matching the app's MainProductCard. Uses the WooCommerce loop $product.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
global $product;
if ( ! $product instanceof WC_Product ) {
	return;
}
$on_sale = $product->is_on_sale();
?>
<article <?php wc_product_class( 'card product-card', $product ); ?>>
	<a href="<?php the_permalink(); ?>" class="thumb" aria-hidden="true">
		<?php echo $product->get_image( 'carmilla-card' ); // phpcs:ignore ?>
	</a>
	<div class="body">
		<div class="variant-row" style="margin:0 0 6px">
			<?php if ( $on_sale ) : ?><span class="badge badge--sale"><?php esc_html_e( 'حراج', 'carmilla' ); ?></span><?php endif; ?>
			<?php if ( ! $product->is_in_stock() ) : ?><span class="badge" style="background:var(--surface-2);color:var(--ink-soft)"><?php esc_html_e( 'ناموجود', 'carmilla' ); ?></span><?php endif; ?>
			<?php if ( $product->get_average_rating() ) : ?><span class="badge badge--rating">★ <?php echo esc_html( carmilla_to_persian_digits( $product->get_average_rating() ) ); ?></span><?php endif; ?>
		</div>
		<h3 class="t-title-sm"><a href="<?php the_permalink(); ?>"><?php echo esc_html( $product->get_name() ); ?></a></h3>
		<div class="price-row" style="margin-block-start:6px">
			<span class="price"><?php echo wp_kses_post( $product->get_price_html() ); ?></span>
		</div>
	</div>
</article>
