<?php
/**
 * Fullscreen story viewer overlay (← StoryDetailScreen). Populated by stories.js.
 */
if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
?>
<div id="cb-story-viewer" class="cb-sv" hidden aria-hidden="true">
	<div class="cb-sv__bars" id="cb-sv-bars"></div>
	<button type="button" class="cb-sv__close" id="cb-sv-close" aria-label="<?php esc_attr_e( 'بستن', 'carmilla' ); ?>"><?php echo carmilla_icon( 'close', 26 ); ?></button>
	<div class="cb-sv__stage">
		<img id="cb-sv-img" class="cb-sv__img" src="" alt="">
		<button type="button" class="cb-sv__nav cb-sv__nav--prev" id="cb-sv-prev" aria-label="<?php esc_attr_e( 'قبلی', 'carmilla' ); ?>"></button>
		<button type="button" class="cb-sv__nav cb-sv__nav--next" id="cb-sv-next" aria-label="<?php esc_attr_e( 'بعدی', 'carmilla' ); ?>"></button>
		<div class="cb-sv__meta">
			<h3 id="cb-sv-title" class="cb-sv__title"></h3>
			<p id="cb-sv-content" class="cb-sv__content"></p>
			<a id="cb-sv-cta" class="btn btn--primary cb-sv__cta" href="#" hidden><?php esc_html_e( 'مشاهده', 'carmilla' ); ?></a>
		</div>
	</div>
</div>
