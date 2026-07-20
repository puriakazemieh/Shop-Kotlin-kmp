<?php
/**
 * My-account orders list ← OrdersScreen: white cards with a status chip,
 * Persian date/amount, and a view action — replacing the default table.
 *
 * @package Carmilla
 */

defined( 'ABSPATH' ) || exit;

do_action( 'woocommerce_before_account_orders', $has_orders );
?>

<div class="cb-acct-orders" style="animation:fadeUp .3s both;">
<?php if ( $has_orders ) : ?>
	<div style="display:flex;flex-direction:column;gap:10px;">
		<?php foreach ( $customer_orders->orders as $customer_order ) : ?>
			<?php
			$order = wc_get_order( $customer_order );
			if ( ! $order ) {
				continue;
			}
			$item_count             = $order->get_item_count() - $order->get_item_count_refunded();
			list( $chip, $fg, $bg ) = carmilla_acct_status_chip( $order->get_status() );
			$view_url               = $order->get_view_order_url();
			?>
			<div class="card" style="padding:16px;">
				<div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap;">
					<span style="font-size:13.5px;font-weight:800;"><?php printf( esc_html__( 'سفارش %s', 'carmilla' ), esc_html( carmilla_to_persian_digits( '#' . $order->get_order_number() ) ) ); ?></span>
					<span style="font-size:11px;font-weight:700;color:<?php echo esc_attr( $fg ); ?>;background:<?php echo esc_attr( $bg ); ?>;padding:4px 10px;border-radius:99px;"><?php echo esc_html( $chip ); ?></span>
					<span class="t-caption t-muted" style="margin-inline-start:auto;"><?php echo esc_html( carmilla_to_persian_digits( wc_format_datetime( $order->get_date_created(), 'Y/m/d' ) ) ); ?></span>
				</div>
				<div style="display:flex;align-items:center;gap:10px;margin-top:12px;flex-wrap:wrap;">
					<span class="t-body-sm t-muted"><?php printf( esc_html( _n( '%s کالا', '%s کالا', $item_count, 'carmilla' ) ), esc_html( carmilla_to_persian_digits( number_format_i18n( $item_count ) ) ) ); ?></span>
					<span style="font-size:14px;font-weight:800;margin-inline-start:auto;"><?php echo wp_kses_post( carmilla_price( (float) $order->get_total() ) ); ?></span>
					<a href="<?php echo esc_url( $view_url ); ?>" style="font-size:12px;font-weight:700;color:#fff;background:var(--accent);padding:9px 16px;border-radius:11px;text-decoration:none;"><?php esc_html_e( 'مشاهده و فاکتور', 'carmilla' ); ?></a>
				</div>
			</div>
		<?php endforeach; ?>
	</div>

	<?php do_action( 'woocommerce_before_account_orders_pagination' ); ?>

	<?php if ( 1 < $customer_orders->max_num_pages ) : ?>
		<div style="display:flex;justify-content:center;gap:10px;margin-top:16px;">
			<?php if ( 1 !== $current_page ) : ?>
				<a href="<?php echo esc_url( wc_get_endpoint_url( 'orders', $current_page - 1 ) ); ?>" style="font-size:12.5px;font-weight:700;color:var(--accent);background:var(--accent-soft);padding:9px 16px;border-radius:11px;text-decoration:none;"><?php esc_html_e( 'صفحه قبل', 'carmilla' ); ?></a>
			<?php endif; ?>
			<?php if ( intval( $customer_orders->max_num_pages ) !== $current_page ) : ?>
				<a href="<?php echo esc_url( wc_get_endpoint_url( 'orders', $current_page + 1 ) ); ?>" style="font-size:12.5px;font-weight:700;color:var(--accent);background:var(--accent-soft);padding:9px 16px;border-radius:11px;text-decoration:none;"><?php esc_html_e( 'صفحه بعد', 'carmilla' ); ?></a>
			<?php endif; ?>
		</div>
	<?php endif; ?>

<?php else : ?>
	<div class="card card--pad" style="text-align:center;padding:44px 20px;">
		<div style="font-size:34px;margin-bottom:10px;">🛍️</div>
		<h3 class="t-title-sm" style="margin:0 0 6px;"><?php esc_html_e( 'هنوز سفارشی ندارید', 'carmilla' ); ?></h3>
		<p class="t-body-sm t-muted" style="margin:0 0 18px;"><?php esc_html_e( 'اولین خرید خود را از فروشگاه شروع کنید.', 'carmilla' ); ?></p>
		<a href="<?php echo esc_url( apply_filters( 'woocommerce_return_to_shop_redirect', wc_get_page_permalink( 'shop' ) ) ); ?>" style="display:inline-block;font-size:13px;font-weight:700;color:#fff;background:var(--accent);padding:12px 22px;border-radius:13px;text-decoration:none;"><?php esc_html_e( 'رفتن به فروشگاه', 'carmilla' ); ?></a>
	</div>
<?php endif; ?>
</div>

<?php do_action( 'woocommerce_after_account_orders', $has_orders ); ?>
