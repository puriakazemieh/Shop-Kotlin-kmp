<?php
/**
 * My-account navigation ← ProfileScreen's section list, as Carmilla pills.
 * Wraps WooCommerce's endpoints (incl. the theme's club/wallet/referral extras)
 * in a white card; the active item gets the accent pill.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

do_action( 'woocommerce_before_account_navigation' );

$carmilla_acct_icons = array(
	'dashboard'       => 'home',
	'orders'          => 'cart',
	'downloads'       => 'play',
	'edit-address'    => 'map-pin',
	'edit-account'    => 'user',
	'customer-logout' => 'close',
	'club'            => 'star',
	'wallet'          => 'wallet',
	'referral'        => 'heart',
);
?>
<nav class="cb-acct-nav" aria-label="<?php esc_attr_e( 'ناوبری حساب کاربری', 'carmilla' ); ?>" style="margin-bottom:18px;">
	<ul style="list-style:none;margin:0;padding:10px;background:var(--surface);border:1px solid var(--line);border-radius:18px;display:flex;flex-wrap:wrap;gap:8px;">
		<?php foreach ( wc_get_account_menu_items() as $endpoint => $label ) : ?>
			<?php
			$is_active = wc_is_current_account_menu_item( $endpoint );
			$style     = $is_active
				? 'background:var(--accent);color:#fff;'
				: 'background:var(--surface-2);color:var(--ink);';
			$icon      = isset( $carmilla_acct_icons[ $endpoint ] ) && function_exists( 'carmilla_icon' )
				? carmilla_icon( $carmilla_acct_icons[ $endpoint ], 15 )
				: '';
			?>
			<li style="margin:0;">
				<a href="<?php echo esc_url( wc_get_account_endpoint_url( $endpoint ) ); ?>"
					style="display:inline-flex;align-items:center;gap:7px;padding:9px 14px;border-radius:12px;font-size:12.5px;font-weight:700;text-decoration:none;<?php echo esc_attr( $style ); ?>">
					<?php echo $icon; // phpcs:ignore WordPress.Security.EscapeOutput -- svg from theme icon helper. ?>
					<?php echo esc_html( $label ); ?>
				</a>
			</li>
		<?php endforeach; ?>
	</ul>
</nav>
<?php do_action( 'woocommerce_after_account_navigation' ); ?>
