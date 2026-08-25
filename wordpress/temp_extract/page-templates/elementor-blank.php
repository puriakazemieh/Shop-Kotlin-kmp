<?php
/**
 * Template Name: کارمیلا — بوم خالی (المنتور)
 * Template Post Type: page
 *
 * Like Elementor Canvas — no header/footer/nav — but the Carmilla design
 * tokens, fonts and dark-mode boot still load, so widgets keep the brand look
 * on a completely blank page (landing pages, campaigns, …).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
?><!doctype html>
<html <?php language_attributes(); ?>>
<head>
	<meta charset="<?php bloginfo( 'charset' ); ?>">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<?php wp_head(); ?>
</head>
<body <?php body_class( 'carmilla-blank' ); ?>>
<?php wp_body_open(); ?>
<div dir="rtl" style="min-height:100vh;background:var(--bg);color:var(--ink);">
	<?php
	while ( have_posts() ) :
		the_post();
		the_content();
	endwhile;
	?>
</div>
<?php wp_footer(); ?>
</body>
</html>
