<?php
/**
 * Carmilla Theme override for shared certificates list (`carmilla/certificates.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$certs = isset( $carmilla_certs_data ) && is_array( $carmilla_certs_data ) ? $carmilla_certs_data : array();
?>
<section class="card carmilla-theme-certificates" data-carmilla-surface="certificates">
	<h2 class="t-headline"><?php esc_html_e( 'گواهی‌های من', 'carmilla' ); ?></h2>
	<ul class="carmilla-certificates-list">
		<?php foreach ( $certs as $c ) : ?>
			<li data-cert-number="<?php echo esc_attr( (string) ( $c['certNumber'] ?? '' ) ); ?>">
				<strong><?php echo esc_html( (string) ( $c['courseTitle'] ?? '' ) ); ?></strong>
				<span>(<?php echo esc_html( (string) ( $c['certNumber'] ?? '' ) ); ?>)</span>
			</li>
		<?php endforeach; ?>
	</ul>
</section>
