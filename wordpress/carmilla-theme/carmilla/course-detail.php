<?php
/**
 * Carmilla Theme override for shared course detail (`carmilla/course-detail.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$course  = isset( $carmilla_course_data ) && is_array( $carmilla_course_data ) ? $carmilla_course_data : array();
$lessons = $course['sections'][0]['lessons'] ?? array();
?>
<article class="card carmilla-theme-course" data-carmilla-surface="course" data-course-id="<?php echo esc_attr( (string) ( $course['id'] ?? 0 ) ); ?>">
	<h2 class="t-headline"><?php echo esc_html( (string) ( $course['title'] ?? '' ) ); ?></h2>
	<ul class="carmilla-lessons-list">
		<?php foreach ( $lessons as $l ) : ?>
			<li data-lesson-id="<?php echo esc_attr( (string) ( $l['id'] ?? 0 ) ); ?>" data-locked="<?php echo empty( $l['videoUrl'] ) && empty( $l['freePreview'] ) ? '1' : '0'; ?>">
				<?php echo esc_html( (string) ( $l['title'] ?? '' ) ); ?>
			</li>
		<?php endforeach; ?>
	</ul>
</article>
