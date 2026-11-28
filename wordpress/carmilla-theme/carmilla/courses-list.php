<?php
/**
 * Carmilla Theme override for shared courses list (`carmilla/courses-list.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$courses = isset( $carmilla_courses_data ) && is_array( $carmilla_courses_data ) ? $carmilla_courses_data : array();
?>
<section class="card carmilla-theme-courses" data-carmilla-surface="courses">
	<h2 class="t-headline"><?php esc_html_e( 'دوره‌های آموزشی', 'carmilla' ); ?></h2>
	<ul class="carmilla-courses-list">
		<?php foreach ( $courses as $c ) : ?>
			<li data-course-id="<?php echo esc_attr( (string) ( $c['id'] ?? 0 ) ); ?>">
				<strong><?php echo esc_html( (string) ( $c['title'] ?? '' ) ); ?></strong>
			</li>
		<?php endforeach; ?>
	</ul>
</section>
