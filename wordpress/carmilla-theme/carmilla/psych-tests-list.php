<?php
/**
 * Carmilla Theme override for shared psych tests catalog (`carmilla/psych-tests-list.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$tests = isset( $carmilla_psych_tests_data ) && is_array( $carmilla_psych_tests_data ) ? $carmilla_psych_tests_data : array();
?>
<section class="card carmilla-theme-psych-tests" data-carmilla-surface="psych-tests">
	<h2 class="t-headline"><?php echo esc_html__( 'تست‌های روان‌شناسی', 'carmilla' ); ?></h2>
	<ul class="carmilla-psych-tests-list">
		<?php foreach ( $tests as $t ) : ?>
			<li data-test-id="<?php echo esc_attr( (string) ( $t['id'] ?? 0 ) ); ?>">
				<strong><?php echo esc_html( (string) ( $t['title'] ?? '' ) ); ?></strong>
				<span>(<?php echo esc_html( (string) ( $t['questionCount'] ?? 0 ) ); ?> سؤال)</span>
			</li>
		<?php endforeach; ?>
	</ul>
</section>
