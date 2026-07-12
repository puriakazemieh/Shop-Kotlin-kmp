<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
?>
<footer class="site-footer" style="background: var(--surface); border-block-start: 1px solid var(--line); margin-block-start: var(--sp-xxl);">
	<div class="container t-body-sm t-muted" style="padding-block: var(--sp-xl);">
		© <?php echo esc_html( date_i18n( 'Y' ) ); ?> <?php bloginfo( 'name' ); ?>
	</div>
</footer>
<?php wp_footer(); ?>
</body>
</html>
