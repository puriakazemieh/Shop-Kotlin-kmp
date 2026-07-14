<?php
/**
 * WooCommerce wrapper. The before/after main-content hooks (inc/woocommerce.php)
 * open the theme container at the correct width (readable for a product, wide for
 * archives), so here we just render header + WC content + footer.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
woocommerce_content();
get_footer();
