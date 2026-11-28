<?php
/**
 * Carmilla Theme override for shared enrolled courses (`carmilla/my-courses.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$courses = isset( $carmilla_my_courses_data ) && is_array( $carmilla_my_courses_data ) ? $carmilla_my_courses_data : array();
?>
<section class="card carmilla-theme-my-courses" data-carmilla-surface="my-courses">
	<h2 class="t-headline"><?php esc_html_e( 'دوره‌های من', 'carmilla' ); ?></h2>
	<ul class="carmilla-my-courses-list">
		<?php foreach ( $courses as $c ) : ?>
			<li data-course-id="<?php echo esc_attr( (string) ( $c['id'] ?? 0 ) ); ?>" data-progress="<?php echo esc_attr( (string) ( $c['progressPercent'] ?? 0 ) ); ?>">
				<strong><?php echo esc_html( (string) ( $c['title'] ?? '' ) ); ?></strong>
				<span>(<?php echo esc_html( (string) ( $c['progressPercent'] ?? 0 ) ); ?>%)</span>
			</li>
		<?php endforeach; ?>
	</ul>
</section>
