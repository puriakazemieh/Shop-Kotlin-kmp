<?php
/**
 * Carmilla Theme override for shared support thread (`carmilla/support-thread.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$ticket   = isset( $carmilla_support_thread_data ) && is_array( $carmilla_support_thread_data ) ? $carmilla_support_thread_data : array();
$messages = isset( $ticket['messages'] ) && is_array( $ticket['messages'] ) ? $ticket['messages'] : array();
?>
<section class="card carmilla-theme-support-thread" data-carmilla-surface="support-thread" data-ticket-id="<?php echo esc_attr( (string) ( $ticket['id'] ?? 0 ) ); ?>">
	<h2 class="t-headline"><?php echo esc_html( (string) ( $ticket['subject'] ?? '' ) ); ?></h2>
	<ul class="carmilla-support-thread-messages">
		<?php foreach ( $messages as $m ) : ?>
			<li data-sender-role="<?php echo esc_attr( (string) ( $m['senderRole'] ?? 'USER' ) ); ?>">
				<?php echo esc_html( (string) ( $m['body'] ?? '' ) ); ?>
			</li>
		<?php endforeach; ?>
	</ul>
	<form method="post" action="<?php echo esc_url( rest_url( 'carmilla/v1/api/support/tickets/' . (int) ( $ticket['id'] ?? 0 ) . '/messages' ) ); ?>">
		<textarea name="body" placeholder="<?php echo esc_attr__( 'پاسخ...', 'carmilla' ); ?>" required></textarea>
		<button type="submit" class="btn btn--primary"><?php echo esc_html__( 'ارسال پاسخ', 'carmilla' ); ?></button>
	</form>
</section>
