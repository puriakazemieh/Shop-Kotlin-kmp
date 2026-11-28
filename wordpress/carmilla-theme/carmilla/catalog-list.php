<?php
/**
 * Carmilla Theme override for shared catalog list (`carmilla/catalog-list.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$items = isset( $carmilla_catalog_data['content'] ) && is_array( $carmilla_catalog_data['content'] ) ? $carmilla_catalog_data['content'] : array();
?>
<section class="card carmilla-theme-catalog" data-carmilla-surface="catalog">
	<h2 class="t-headline"><?php esc_html_e( 'فروشگاه', 'carmilla' ); ?></h2>
	<ul class="carmilla-catalog-list">
		<?php foreach ( $items as $item ) : ?>
			<li data-product-id="<?php echo esc_attr( (string) ( $item['id'] ?? 0 ) ); ?>">
				<strong><?php echo esc_html( (string) ( $item['title'] ?? '' ) ); ?></strong>
			</li>
		<?php endforeach; ?>
	</ul>
</section>
