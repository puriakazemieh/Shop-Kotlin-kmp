<?php
/**
 * Carmilla Theme override for shared support tickets list & creation (`carmilla/support-tickets.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$tickets = isset( $carmilla_support_tickets_data ) && is_array( $carmilla_support_tickets_data ) ? $carmilla_support_tickets_data : array();
?>
<section class="card carmilla-theme-support-tickets" data-carmilla-surface="support-tickets">
	<h2 class="t-headline"><?php echo esc_html__( 'تیکت‌های پشتیبانی من', 'carmilla' ); ?></h2>
	<ul class="carmilla-support-tickets-list">
		<?php foreach ( $tickets as $t ) : ?>
			<li data-ticket-id="<?php echo esc_attr( (string) ( $t['id'] ?? 0 ) ); ?>" data-status="<?php echo esc_attr( (string) ( $t['status'] ?? '' ) ); ?>">
				<strong><?php echo esc_html( (string) ( $t['subject'] ?? '' ) ); ?></strong>
				<span>(<?php echo esc_html( (string) ( $t['status'] ?? '' ) ); ?>)</span>
			</li>
		<?php endforeach; ?>
	</ul>
	<form method="post" action="<?php echo esc_url( rest_url( 'carmilla/v1/api/support/tickets' ) ); ?>">
		<input type="text" name="subject" placeholder="<?php echo esc_attr__( 'موضوع تیکت', 'carmilla' ); ?>" required />
		<textarea name="message" placeholder="<?php echo esc_attr__( 'متن پیام پشتیبانی', 'carmilla' ); ?>" required></textarea>
		<button type="submit" class="btn btn--primary"><?php echo esc_html__( 'ارسال تیکت', 'carmilla' ); ?></button>
	</form>
</section>
