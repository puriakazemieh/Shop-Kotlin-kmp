<?php
/**
 * Carmilla Theme override for shared page view (`carmilla/content-page.php`).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$page = isset( $carmilla_page_data ) && is_array( $carmilla_page_data ) ? $carmilla_page_data : array();
?>
<article class="card carmilla-theme-page" data-carmilla-surface="page" data-page-id="<?php echo esc_attr( (string) ( $page['id'] ?? 0 ) ); ?>">
	<h2 class="t-headline"><?php echo esc_html( (string) ( $page['title'] ?? '' ) ); ?></h2>
	<div class="entry-content t-body"><?php echo wp_kses_post( (string) ( $page['rawContent'] ?? '' ) ); ?></div>
</article>
