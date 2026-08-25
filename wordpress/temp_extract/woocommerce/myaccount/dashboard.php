<?php
/**
 * My-account dashboard ← ProfileScreen: greeting + stat tiles + quick links,
 * replacing WooCommerce's plain text dashboard.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

$carmilla_user   = wp_get_current_user();
$carmilla_uid    = $carmilla_user->ID;
$carmilla_orders = function_exists( 'wc_get_customer_order_count' ) ? (int) wc_get_customer_order_count( $carmilla_uid ) : 0;
$carmilla_spent  = function_exists( 'wc_get_customer_total_spent' ) ? (float) wc_get_customer_total_spent( $carmilla_uid ) : 0.0;
$carmilla_wallet = (float) get_user_meta( $carmilla_uid, 'cb_wallet_balance', true );
$carmilla_name   = $carmilla_user->display_name ? $carmilla_user->display_name : $carmilla_user->user_login;
?>
<div class="cb-acct-dash" style="animation:fadeUp .3s both;">

	<div class="card card--pad" style="display:flex;align-items:center;gap:14px;margin-bottom:14px;">
		<div style="width:52px;height:52px;border-radius:16px;background:var(--accent);color:#fff;display:grid;place-items:center;font-weight:800;font-size:22px;flex-shrink:0;">
			<?php echo esc_html( mb_substr( $carmilla_name, 0, 1, 'UTF-8' ) ); ?>
		</div>
		<div style="min-width:0;">
			<div style="font-size:16px;font-weight:800;"><?php printf( esc_html__( 'سلام، %s', 'carmilla' ), esc_html( $carmilla_name ) ); ?> 👋</div>
			<div class="t-body-sm t-muted" style="margin-top:3px;"><?php echo esc_html( $carmilla_user->user_email ); ?></div>
		</div>
		<a href="<?php echo esc_url( wc_get_account_endpoint_url( 'edit-account' ) ); ?>" style="margin-inline-start:auto;flex-shrink:0;font-size:12px;font-weight:700;color:var(--accent);text-decoration:none;background:var(--accent-soft);padding:8px 13px;border-radius:11px;"><?php esc_html_e( 'ویرایش', 'carmilla' ); ?></a>
	</div>

	<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:10px;margin-bottom:14px;">
		<a href="<?php echo esc_url( wc_get_account_endpoint_url( 'orders' ) ); ?>" class="card" style="padding:16px;text-decoration:none;color:inherit;">
			<div class="t-caption t-muted"><?php esc_html_e( 'سفارش‌ها', 'carmilla' ); ?></div>
			<div style="font-size:22px;font-weight:800;margin-top:4px;"><?php echo esc_html( carmilla_to_persian_digits( number_format_i18n( $carmilla_orders ) ) ); ?></div>
		</a>
		<a href="<?php echo esc_url( wc_get_account_endpoint_url( 'wallet' ) ); ?>" class="card" style="padding:16px;text-decoration:none;color:inherit;">
			<div class="t-caption t-muted"><?php esc_html_e( 'کیف پول', 'carmilla' ); ?></div>
			<div style="font-size:16px;font-weight:800;margin-top:8px;"><?php echo wp_kses_post( carmilla_price( $carmilla_wallet ) ); ?></div>
		</a>
		<a href="<?php echo esc_url( wc_get_account_endpoint_url( 'club' ) ); ?>" class="card" style="padding:16px;text-decoration:none;color:inherit;">
			<div class="t-caption t-muted"><?php esc_html_e( 'مجموع خرید', 'carmilla' ); ?></div>
			<div style="font-size:16px;font-weight:800;margin-top:8px;"><?php echo wp_kses_post( carmilla_price( $carmilla_spent ) ); ?></div>
		</a>
	</div>

	<div class="card card--pad">
		<h3 class="t-title-sm" style="margin:0 0 12px;"><?php esc_html_e( 'دسترسی سریع', 'carmilla' ); ?></h3>
		<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(170px,1fr));gap:8px;">
			<?php
			$carmilla_quick = array(
				'orders'       => __( 'پیگیری سفارش‌ها', 'carmilla' ),
				'edit-address' => __( 'آدرس‌های من', 'carmilla' ),
				'referral'     => __( 'معرفی به دوستان', 'carmilla' ),
				'club'         => __( 'باشگاه مشتریان', 'carmilla' ),
			);
			foreach ( $carmilla_quick as $carmilla_ep => $carmilla_label ) :
				?>
				<a href="<?php echo esc_url( wc_get_account_endpoint_url( $carmilla_ep ) ); ?>" style="display:flex;align-items:center;justify-content:space-between;gap:8px;background:var(--surface-2);border-radius:13px;padding:13px 15px;font-size:13px;font-weight:700;text-decoration:none;color:var(--ink);">
					<?php echo esc_html( $carmilla_label ); ?>
					<span style="color:var(--ink-soft);display:inline-flex;transform:scaleX(-1);"><?php echo function_exists( 'carmilla_icon' ) ? carmilla_icon( 'chevron', 14 ) : '›'; // phpcs:ignore WordPress.Security.EscapeOutput ?></span>
				</a>
			<?php endforeach; ?>
		</div>
	</div>

	<?php
	/**
	 * Keep WooCommerce extension points working.
	 */
	do_action( 'woocommerce_account_dashboard' );
	?>
</div>
