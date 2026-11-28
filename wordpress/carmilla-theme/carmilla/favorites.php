<?php
/**
 * Carmilla Theme override for shared favorites (`carmilla/favorites.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$items = isset( $carmilla_favorites_data ) && is_array( $carmilla_favorites_data ) ? $carmilla_favorites_data : array();
?>
<section class="card carmilla-theme-favorites" data-carmilla-surface="favorites">
	<h2 class="t-headline"><?php echo esc_html__( 'علاقه‌مندی‌های من', 'carmilla' ); ?></h2>
	<ul class="carmilla-favorites-list">
		<?php foreach ( $items as $item ) : ?>
			<li data-product-id="<?php echo esc_attr( (string) ( $item['id'] ?? 0 ) ); ?>">
				<?php echo esc_html( (string) ( $item['name'] ?? '' ) ); ?>
			</li>
		<?php endforeach; ?>
	</ul>
</section>
