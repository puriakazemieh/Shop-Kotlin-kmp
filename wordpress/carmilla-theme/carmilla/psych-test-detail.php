<?php
/**
 * Carmilla Theme override for shared psych test form (`carmilla/psych-test-detail.php`).
 * Option scores are never exposed to the client.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$t         = isset( $carmilla_psych_test_data ) && is_array( $carmilla_psych_test_data ) ? $carmilla_psych_test_data : array();
$questions = isset( $t['questions'] ) && is_array( $t['questions'] ) ? $t['questions'] : array();
?>
<section class="card carmilla-theme-psych-test" data-carmilla-surface="psych-test-form" data-test-id="<?php echo esc_attr( (string) ( $t['id'] ?? 0 ) ); ?>">
	<h2 class="t-headline"><?php echo esc_html( (string) ( $t['title'] ?? '' ) ); ?></h2>
	<form method="post" action="<?php echo esc_url( rest_url( 'carmilla/v1/api/psych-tests/' . (int) ( $t['id'] ?? 0 ) . '/submit' ) ); ?>">
		<?php foreach ( $questions as $q ) : ?>
			<fieldset>
				<legend><?php echo esc_html( (string) ( $q['text'] ?? '' ) ); ?></legend>
				<?php foreach ( (array) ( $q['options'] ?? array() ) as $oi => $o ) : ?>
					<label><input type="radio" name="q<?php echo esc_attr( (string) ( $q['index'] ?? 0 ) ); ?>" value="<?php echo esc_attr( (string) $oi ); ?>" required /> <?php echo esc_html( (string) ( $o['text'] ?? '' ) ); ?></label>
				<?php endforeach; ?>
			</fieldset>
		<?php endforeach; ?>
		<button type="submit" class="btn btn--primary"><?php echo esc_html__( 'ثبت پاسخ و مشاهده نتیجه', 'carmilla' ); ?></button>
	</form>
</section>
