<?php
/**
 * Carmilla Theme override for shared private psych test results (`carmilla/my-psych-results.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$results = isset( $carmilla_psych_results_data ) && is_array( $carmilla_psych_results_data ) ? $carmilla_psych_results_data : array();
?>
<section class="card carmilla-theme-psych-results" data-carmilla-surface="my-psych-results">
	<h2 class="t-headline"><?php echo esc_html__( 'نتایج تست‌های من', 'carmilla' ); ?></h2>
	<ul class="carmilla-psych-results-list">
		<?php foreach ( $results as $r ) : ?>
			<li data-attempt-id="<?php echo esc_attr( (string) ( $r['id'] ?? 0 ) ); ?>" data-status="<?php echo esc_attr( (string) ( $r['status'] ?? '' ) ); ?>">
				<strong><?php echo esc_html( (string) ( $r['testTitle'] ?? '' ) ); ?></strong>
				<span><?php echo esc_html( (string) ( $r['status'] ?? '' ) ); ?></span>
				<?php if ( isset( $r['totalScore'] ) && null !== $r['totalScore'] ) : ?>
					<em>امتیاز: <?php echo esc_html( (string) $r['totalScore'] ); ?></em>
				<?php endif; ?>
			</li>
		<?php endforeach; ?>
	</ul>
</section>
