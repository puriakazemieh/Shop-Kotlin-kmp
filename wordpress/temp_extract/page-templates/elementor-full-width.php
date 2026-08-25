<?php
/**
 * Template Name: کارمیلا — تمام‌عرض (المنتور)
 * Template Post Type: page
 *
 * Full-bleed content with the Carmilla header/footer/bottom-nav kept — the
 * professional alternative to Elementor's Canvas: build edge-to-edge sections
 * in Elementor while the brand chrome and design tokens stay on.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

get_header();

while ( have_posts() ) :
	the_post();
	the_content();
endwhile;

get_footer();
