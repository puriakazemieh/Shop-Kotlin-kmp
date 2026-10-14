<?php
/**
 * Default static-page template.
 *
 * WooCommerce renders Cart, Checkout and My Account through the content of
 * their assigned pages. Without this template WordPress falls back to
 * index.php, which treats those pages as blog cards and never executes the
 * WooCommerce shortcodes/blocks.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

get_header();
?>
<main class="container container--medium" style="padding-block: var(--sp-xl);">
	<?php
	while ( have_posts() ) :
		the_post();
		the_content();
	endwhile;
	?>
</main>
<?php
get_footer();
