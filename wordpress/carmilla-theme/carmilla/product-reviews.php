<?php
/**
 * Carmilla Theme override for shared product reviews & Q&A (`carmilla/product-reviews.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$reviews    = isset( $carmilla_reviews_data ) && is_array( $carmilla_reviews_data ) ? $carmilla_reviews_data : array();
$questions  = isset( $carmilla_questions_data ) && is_array( $carmilla_questions_data ) ? $carmilla_questions_data : array();
$product_id = isset( $carmilla_product_id_data ) ? (int) $carmilla_product_id_data : 0;
?>
<section class="card carmilla-theme-product-reviews" data-carmilla-surface="product-reviews" data-product-id="<?php echo esc_attr( (string) $product_id ); ?>">
	<h2 class="t-headline"><?php echo esc_html__( 'نظرات و پرسش‌ها', 'carmilla' ); ?></h2>
	<ul class="carmilla-reviews-list">
		<?php foreach ( $reviews as $r ) : ?>
			<li data-review-id="<?php echo esc_attr( (string) ( $r['id'] ?? 0 ) ); ?>">
				<strong><?php echo esc_html( (string) ( $r['userName'] ?? '' ) ); ?></strong>
				<span><?php echo esc_html( (string) ( $r['comment'] ?? '' ) ); ?></span>
			</li>
		<?php endforeach; ?>
	</ul>
	<ul class="carmilla-questions-list">
		<?php foreach ( $questions as $q ) : ?>
			<li data-question-id="<?php echo esc_attr( (string) ( $q['id'] ?? 0 ) ); ?>">
				<strong><?php echo esc_html( (string) ( $q['userName'] ?? '' ) ); ?></strong>
				<span><?php echo esc_html( (string) ( $q['content'] ?? '' ) ); ?></span>
			</li>
		<?php endforeach; ?>
	</ul>
</section>
