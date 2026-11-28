<?php
/**
 * Carmilla Theme override for shared posts list (`carmilla/content-posts.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$items = isset( $carmilla_posts_data['content'] ) && is_array( $carmilla_posts_data['content'] ) ? $carmilla_posts_data['content'] : array();
?>
<section class="card carmilla-theme-posts" data-carmilla-surface="posts">
	<h2 class="t-headline"><?php esc_html_e( 'نوشته‌ها', 'carmilla' ); ?></h2>
	<ul class="carmilla-posts-list">
		<?php foreach ( $items as $item ) : ?>
			<li data-post-id="<?php echo esc_attr( (string) ( $item['id'] ?? 0 ) ); ?>">
				<strong><?php echo esc_html( (string) ( $item['title'] ?? '' ) ); ?></strong>
				<?php if ( ! empty( $item['summary'] ) ) : ?>
					<p class="t-body"><?php echo esc_html( (string) $item['summary'] ); ?></p>
				<?php endif; ?>
			</li>
		<?php endforeach; ?>
	</ul>
</section>
