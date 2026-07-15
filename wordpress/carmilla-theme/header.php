<?php
/**
 * Header: sticky top bar (logo + primary menu + search/account/cart).
 * The mobile bottom navigation lives in footer.php (echoing the app's bottom bar).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
$account_url = function_exists( 'wc_get_page_permalink' ) ? wc_get_page_permalink( 'myaccount' ) : home_url( '/' );
$cart_url    = function_exists( 'wc_get_cart_url' ) ? wc_get_cart_url() : home_url( '/' );
$cart_count  = carmilla_cart_count();
?>
<!doctype html>
<html <?php language_attributes(); ?>>
<head>
	<meta charset="<?php bloginfo( 'charset' ); ?>">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<script>/* theme-boot: apply the saved light/dark choice before paint (← SettingsScreen) */
	(function(){try{var t=localStorage.getItem('cb_theme');if(t==='light'||t==='dark'){document.documentElement.setAttribute('data-theme',t);}}catch(e){}})();</script>
	<?php wp_head(); ?>
</head>
<body <?php body_class( 'has-bottom-nav' ); ?>>
<?php wp_body_open(); ?>

<header class="site-header">
	<div class="container container--wide">
		<div class="bar">
			<?php
			$header_title = get_theme_mod( 'carmilla_header_title', '' );
			if ( has_custom_logo() ) {
				the_custom_logo();
			} else {
				printf(
					'<a href="%s" class="brand">%s</a>',
					esc_url( home_url( '/' ) ),
					esc_html( $header_title ?: get_bloginfo( 'name' ) )
				);
			}
			?>

			<nav aria-label="<?php esc_attr_e( 'منوی اصلی', 'carmilla' ); ?>">
				<?php
				if ( has_nav_menu( 'primary' ) ) {
					wp_nav_menu( array(
						'theme_location' => 'primary',
						'container'      => false,
						'menu_class'     => 'primary-menu',
						'depth'          => 1,
						'fallback_cb'    => false,
					) );
				} else {
					carmilla_primary_menu_fallback();
				}
				?>
			</nav>

			<div class="header-actions">
				<a class="icon-btn" href="<?php echo esc_url( home_url( '/?s=' ) ); ?>" aria-label="<?php esc_attr_e( 'جستجو', 'carmilla' ); ?>"><?php echo carmilla_icon( 'search' ); // phpcs:ignore ?></a>
				<a class="icon-btn" href="<?php echo esc_url( $account_url ); ?>" aria-label="<?php esc_attr_e( 'حساب کاربری', 'carmilla' ); ?>"><?php echo carmilla_icon( 'user' ); // phpcs:ignore ?></a>
				<a class="icon-btn" href="<?php echo esc_url( $cart_url ); ?>" aria-label="<?php esc_attr_e( 'سبد خرید', 'carmilla' ); ?>"><?php echo carmilla_icon( 'cart' ); // phpcs:ignore ?>
					<?php if ( $cart_count > 0 ) : ?>
						<span class="count"><?php echo esc_html( carmilla_to_persian_digits( $cart_count ) ); ?></span>
					<?php endif; ?>
				</a>
			</div>
		</div>
	</div>
</header>

<div id="content" class="site-content">
