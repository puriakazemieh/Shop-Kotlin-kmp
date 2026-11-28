<?php
/**
 * Carmilla Theme override for shared user appointments list (`carmilla/my-appointments.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$appointments = isset( $carmilla_appointments_data ) && is_array( $carmilla_appointments_data ) ? $carmilla_appointments_data : array();
?>
<section class="card carmilla-theme-my-appointments" data-carmilla-surface="my-appointments">
	<h2 class="t-headline"><?php echo esc_html__( 'نوبت‌های من', 'carmilla' ); ?></h2>
	<ul class="carmilla-appointments-list">
		<?php foreach ( $appointments as $a ) : ?>
			<li data-appointment-id="<?php echo esc_attr( (string) ( $a['id'] ?? 0 ) ); ?>" data-status="<?php echo esc_attr( (string) ( $a['status'] ?? '' ) ); ?>">
				<strong><?php echo esc_html( (string) ( $a['therapistName'] ?? '' ) ); ?></strong>
				<span>(<?php echo esc_html( (string) ( $a['dayLabel'] ?? '' ) . ' ' . (string) ( $a['timeLabel'] ?? '' ) ); ?>)</span>
				<em><?php echo esc_html( (string) ( $a['status'] ?? '' ) ); ?></em>
			</li>
		<?php endforeach; ?>
	</ul>
</section>
