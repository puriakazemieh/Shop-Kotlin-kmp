<?php
/**
 * Carmilla Theme override for shared appointment booking form (`carmilla/booking-form.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$t     = isset( $carmilla_therapist_data ) && is_array( $carmilla_therapist_data ) ? $carmilla_therapist_data : array();
$slots = isset( $t['slots'] ) && is_array( $t['slots'] ) ? $t['slots'] : array();
?>
<section class="card carmilla-theme-booking" data-carmilla-surface="booking-form" data-therapist-id="<?php echo esc_attr( (string) ( $t['id'] ?? 0 ) ); ?>">
	<h2 class="t-headline"><?php echo esc_html( sprintf( 'رزرو نوبت: %s', (string) ( $t['name'] ?? '' ) ) ); ?></h2>
	<form method="post" action="<?php echo esc_url( rest_url( 'carmilla/v1/api/clinic/appointments' ) ); ?>">
		<ul class="carmilla-therapist-slots">
			<?php foreach ( $slots as $s ) : ?>
				<li data-slot-id="<?php echo esc_attr( (string) ( $s['id'] ?? 0 ) ); ?>">
					<label>
						<input type="radio" name="slotId" value="<?php echo esc_attr( (string) ( $s['id'] ?? 0 ) ); ?>" required />
						<span><?php echo esc_html( (string) ( $s['dayLabel'] ?? '' ) . ' ' . (string) ( $s['timeLabel'] ?? '' ) ); ?></span>
					</label>
				</li>
			<?php endforeach; ?>
		</ul>
		<button type="submit" class="btn btn--primary"><?php echo esc_html__( 'ثبت نوبت', 'carmilla' ); ?></button>
	</form>
</section>
