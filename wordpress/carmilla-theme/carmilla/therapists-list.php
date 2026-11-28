<?php
/**
 * Carmilla Theme override for shared therapists list (`carmilla/therapists-list.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$therapists = isset( $carmilla_therapists_data ) && is_array( $carmilla_therapists_data ) ? $carmilla_therapists_data : array();
?>
<section class="card carmilla-theme-therapists" data-carmilla-surface="therapists">
	<h2 class="t-headline"><?php esc_html_e( 'متخصصان و مشاوران', 'carmilla' ); ?></h2>
	<ul class="carmilla-therapists-list">
		<?php foreach ( $therapists as $t ) : ?>
			<li data-therapist-id="<?php echo esc_attr( (string) ( $t['id'] ?? 0 ) ); ?>">
				<strong><?php echo esc_html( (string) ( $t['name'] ?? '' ) ); ?></strong>
				<span>(<?php echo esc_html( (string) ( $t['availableSlotCount'] ?? 0 ) ); ?> بازه)</span>
			</li>
		<?php endforeach; ?>
	</ul>
</section>
