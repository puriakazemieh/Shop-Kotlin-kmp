<?php
/**
 * Theme header. Bottom-bar (mobile) <-> side-rail (large) shell is Track A phase 2;
 * this is the minimal document + top bar wrapper.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
?>
<!doctype html>
<html <?php language_attributes(); ?>>
<head>
	<meta charset="<?php bloginfo( 'charset' ); ?>">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<?php wp_head(); ?>
</head>
<body <?php body_class(); ?>>
<?php wp_body_open(); ?>
<header class="site-header" style="background: var(--surface); border-block-end: 1px solid var(--line);">
	<div class="container" style="display:flex; align-items:center; justify-content:space-between; height:64px;">
		<a href="<?php echo esc_url( home_url( '/' ) ); ?>" class="t-title-lg" style="margin:0; color: var(--accent);">
			<?php bloginfo( 'name' ); ?>
		</a>
		<nav>
			<?php
			wp_nav_menu( array(
				'theme_location' => 'primary',
				'container'      => false,
				'fallback_cb'    => false,
				'menu_class'     => 'primary-menu',
			) );
			?>
		</nav>
	</div>
</header>
