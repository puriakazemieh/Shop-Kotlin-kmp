<?php
/**
 * Carmilla Theme override for shared therapist detail (`carmilla/therapist-detail.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$t     = isset( $carmilla_therapist_data ) && is_array( $carmilla_therapist_data ) ? $carmilla_therapist_data : array();
$slots = isset( $t['slots'] ) && is_array( $t['slots'] ) ? $t['slots'] : array();
?>
<article class="card carmilla-theme-therapist" data-carmilla-surface="therapist" data-therapist-id="<?php echo esc_attr( (string) ( $t['id'] ?? 0 ) ); ?>" data-timezone="<?php echo esc_attr( (string) ( $t['timezone'] ?? 'UTC' ) ); ?>">
	<h2 class="t-headline"><?php echo esc_html( (string) ( $t['name'] ?? '' ) ); ?></h2>
	<ul class="carmilla-therapist-slots">
		<?php foreach ( $slots as $s ) : ?>
			<li data-slot-id="<?php echo esc_attr( (string) ( $s['id'] ?? 0 ) ); ?>">
				<?php echo esc_html( (string) ( $s['dayLabel'] ?? '' ) . ' ' . (string) ( $s['timeLabel'] ?? '' ) ); ?>
			</li>
		<?php endforeach; ?>
	</ul>
</article>
