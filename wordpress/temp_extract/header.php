<?php
/**
 * Header — faithful port of the reference sticky top bar (logo + search pill +
 * theme/wishlist/cart/account + category row). Mobile navigation lives in the
 * bottom bar (footer.php). Mirrors docs/design-reference/*.html.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$brand_name   = get_bloginfo( 'name' );
$brand_sub    = get_theme_mod( 'carmilla_brand_sub', 'CARMILLA' );
$brand_init   = mb_substr( wp_strip_all_tags( $brand_name ), 0, 1, 'UTF-8' );
$account_url  = function_exists( 'wc_get_page_permalink' ) ? wc_get_page_permalink( 'myaccount' ) : home_url( '/' );
$cart_url     = function_exists( 'wc_get_cart_url' ) ? wc_get_cart_url() : home_url( '/' );
$search_url   = home_url( '/?s=' );
$cart_count   = carmilla_cart_count();
$wish_count   = function_exists( 'carmilla_wishlist_count' ) ? (int) carmilla_wishlist_count() : 0;
$is_logged_in = is_user_logged_in();
$account_lbl  = $is_logged_in ? ( wp_get_current_user()->display_name ?: 'حساب من' ) : 'ورود / ثبت‌نام';

// Category row: top-level product categories (falls back to nothing when WC is off).
$navcats = array();
if ( function_exists( 'get_terms' ) && function_exists( 'wc_get_page_permalink' ) ) {
	$terms = get_terms( array( 'taxonomy' => 'product_cat', 'hide_empty' => true, 'parent' => 0, 'number' => 8 ) );
	if ( ! is_wp_error( $terms ) ) {
		foreach ( $terms as $t ) {
			$navcats[] = array( 'name' => $t->name, 'url' => get_term_link( $t ) );
		}
	}
}
?>
<!doctype html>
<html <?php language_attributes(); ?>>
<head>
	<meta charset="<?php bloginfo( 'charset' ); ?>">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<script>/* apply saved light/dark before paint (← SettingsScreen) */
	(function(){try{var t=localStorage.getItem('cb_theme');if(t==='light'||t==='dark'){document.documentElement.setAttribute('data-theme',t);}}catch(e){}})();</script>
	<?php wp_head(); ?>
</head>
<body <?php body_class( 'has-bottom-nav' ); ?>>
<?php wp_body_open(); ?>

<div class="dc-root" dir="rtl" style="min-height:100vh;background:var(--bg);color:var(--ink);transition:background .25s,color .25s;">

	<!-- ===== HEADER ===== -->
	<?php
	// Elementor Pro Theme Builder can take over the header; otherwise render ours.
	if ( ! function_exists( 'elementor_theme_do_location' ) || ! elementor_theme_do_location( 'header' ) ) :
		?>
	<div class="dc-header" style="position:sticky;top:0;z-index:40;background:var(--surface);border-bottom:1px solid var(--line);backdrop-filter:saturate(1.1);">
		<div style="max-width:1240px;margin:0 auto;display:flex;align-items:center;gap:clamp(10px,2vw,18px);padding:11px clamp(14px,4vw,28px);">
			<a href="<?php echo esc_url( home_url( '/' ) ); ?>" style="display:flex;align-items:center;gap:10px;flex-shrink:0;">
				<?php if ( has_custom_logo() ) : ?>
					<?php the_custom_logo(); ?>
				<?php else : ?>
					<div style="width:38px;height:38px;border-radius:11px;background:var(--accent);display:grid;place-items:center;color:#fff;font-weight:800;font-size:20px;"><?php echo esc_html( $brand_init ); ?></div>
					<div style="line-height:1;">
						<div style="font-weight:800;font-size:21px;color:var(--ink);letter-spacing:-.5px;"><?php echo esc_html( $brand_name ); ?></div>
						<div style="font-size:9.5px;color:var(--ink-soft);letter-spacing:1px;margin-top:2px;"><?php echo esc_html( $brand_sub ); ?></div>
					</div>
				<?php endif; ?>
			</a>

			<a href="<?php echo esc_url( $search_url ); ?>" style="flex:1;min-width:0;display:flex;align-items:center;gap:9px;background:var(--surface-2);border:1px solid var(--line);border-radius:13px;padding:11px 15px;color:var(--ink-soft);">
				<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4-4"/></svg>
				<span style="font-size:13px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">جستجو در میان هزاران محصول…</span>
			</a>

			<div style="display:flex;align-items:center;gap:4px;flex-shrink:0;">
				<button type="button" onclick="window.cbToggleTheme&&window.cbToggleTheme()" title="تغییر تم" style="width:42px;height:42px;border-radius:12px;display:grid;place-items:center;cursor:pointer;color:var(--ink-soft);background:none;border:none;">
					<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="<?php echo esc_attr( carmilla_dc_theme_icon() ); ?>"/></svg>
				</button>
				<div class="top-actions" style="display:flex;align-items:center;gap:4px;">
					<a href="<?php echo esc_url( home_url( '/?cb_view=wishlist' ) ); ?>" style="position:relative;width:42px;height:42px;border-radius:12px;display:grid;place-items:center;color:var(--ink-soft);">
						<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 21s-7-4.6-9.4-9A5 5 0 0112 5a5 5 0 019.4 7C19 16.4 12 21 12 21z"/></svg>
						<?php if ( $wish_count > 0 ) : ?><div style="position:absolute;top:5px;left:5px;background:var(--sale);color:#fff;font-size:10px;font-weight:700;min-width:16px;height:16px;border-radius:9px;display:grid;place-items:center;padding:0 3px;"><?php echo esc_html( carmilla_to_persian_digits( $wish_count ) ); ?></div><?php endif; ?>
					</a>
					<a href="<?php echo esc_url( $cart_url ); ?>" style="position:relative;width:42px;height:42px;border-radius:12px;display:grid;place-items:center;color:var(--ink-soft);">
						<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 6h15l-1.6 9H7.5z M6 6L5 3H2 M9 20a1 1 0 100 .1 M18 20a1 1 0 100 .1"/></svg>
						<?php if ( $cart_count > 0 ) : ?><div style="position:absolute;top:5px;left:5px;background:var(--accent);color:#fff;font-size:10px;font-weight:700;min-width:16px;height:16px;border-radius:9px;display:grid;place-items:center;padding:0 3px;"><?php echo esc_html( carmilla_to_persian_digits( $cart_count ) ); ?></div><?php endif; ?>
					</a>
					<a href="<?php echo esc_url( $account_url ); ?>" style="display:flex;align-items:center;gap:8px;background:var(--surface-2);border:1px solid var(--line);border-radius:12px;padding:8px 14px;color:var(--ink);margin-right:2px;">
						<svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 4-6 8-6s8 2 8 6"/></svg>
						<span style="font-size:13px;font-weight:600;white-space:nowrap;"><?php echo esc_html( $account_lbl ); ?></span>
					</a>
				</div>
			</div>
		</div>

		<?php if ( $navcats ) : ?>
		<div class="noscroll" style="max-width:1240px;margin:0 auto;display:flex;align-items:center;gap:18px;padding:0 clamp(14px,4vw,28px) 10px;overflow-x:auto;">
			<?php foreach ( $navcats as $c ) : ?>
				<a href="<?php echo esc_url( $c['url'] ); ?>" style="font-size:13px;font-weight:600;color:var(--ink-soft);white-space:nowrap;padding:3px 0;"><?php echo esc_html( $c['name'] ); ?></a>
			<?php endforeach; ?>
		</div>
		<?php endif; ?>
	</div>
	<?php endif; // end header location fallback. ?>

	<?php
	// The «کارمیلا — تمام‌عرض (المنتور)» template gets an edge-to-edge content
	// area (Elementor sections handle their own widths); everything else keeps
	// the standard 1240px container.
	$cb_content_style = is_page_template( 'page-templates/elementor-full-width.php' )
		? 'padding:0 0 120px;min-height:60vh;'
		: 'max-width:1240px;margin:0 auto;padding:0 clamp(14px,4vw,28px) 120px;min-height:60vh;';
	?>
	<div id="content" class="route-pad" style="<?php echo esc_attr( $cb_content_style ); ?>">
