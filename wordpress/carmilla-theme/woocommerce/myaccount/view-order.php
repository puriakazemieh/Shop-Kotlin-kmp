<?php
/**
 * My-account single order ← OrderDetailScreen's invoice view («دانلود فاکتور»):
 * invoice header, item lines, totals, addresses, and a print button. The theme's
 * tracking timeline hooks after the details table as before.
 *
 * @package Carmilla
 */

defined( 'ABSPATH' ) || exit;

// WooCommerce passes $order_id into this template; resolve the order ourselves.
$order = wc_get_order( $order_id ); // phpcs:ignore WordPress.WP.GlobalVariablesOverride
if ( ! $order ) {
	return;
}

$carmilla_status = $order->get_status();
list( $carmilla_chip, $carmilla_fg, $carmilla_bg ) = function_exists( 'carmilla_acct_status_chip' )
	? carmilla_acct_status_chip( $carmilla_status )
	: array( wc_get_order_status_name( $carmilla_status ), 'var(--ink-soft)', 'var(--surface-2)' );

$carmilla_is_digital = ! $order->needs_shipping_address();
?>
<div class="cb-invoice" style="animation:fadeUp .3s both;">

	<div class="card card--pad" id="cb-invoice-print" style="margin-bottom:14px;">
		<div style="display:flex;align-items:flex-start;gap:10px;flex-wrap:wrap;border-bottom:1px dashed var(--line);padding-bottom:14px;margin-bottom:14px;">
			<div>
				<div style="font-size:16px;font-weight:800;"><?php printf( esc_html__( 'فاکتور سفارش %s', 'carmilla' ), esc_html( carmilla_to_persian_digits( '#' . $order->get_order_number() ) ) ); ?></div>
				<div class="t-caption t-muted" style="margin-top:5px;"><?php printf( esc_html__( 'تاریخ ثبت: %s', 'carmilla' ), esc_html( carmilla_to_persian_digits( wc_format_datetime( $order->get_date_created(), 'Y/m/d — H:i' ) ) ) ); ?></div>
			</div>
			<span style="margin-inline-start:auto;font-size:11px;font-weight:700;color:<?php echo esc_attr( $carmilla_fg ); ?>;background:<?php echo esc_attr( $carmilla_bg ); ?>;padding:5px 12px;border-radius:99px;"><?php echo esc_html( $carmilla_chip ); ?></span>
		</div>

		<?php foreach ( $order->get_items() as $item_id => $item ) : ?>
			<?php
			$product = $item->get_product();
			$thumb   = $product ? $product->get_image( array( 48, 48 ), array( 'style' => 'width:48px;height:48px;border-radius:11px;object-fit:cover;' ) ) : '';
			?>
			<div style="display:flex;align-items:center;gap:12px;padding:9px 0;">
				<?php if ( $thumb ) : ?><div style="flex-shrink:0;"><?php echo wp_kses_post( $thumb ); ?></div><?php endif; ?>
				<div style="min-width:0;flex:1;">
					<div style="font-size:13px;font-weight:700;line-height:1.7;"><?php echo esc_html( $item->get_name() ); ?></div>
					<div class="t-caption t-muted"><?php printf( esc_html__( 'تعداد: %s', 'carmilla' ), esc_html( carmilla_to_persian_digits( number_format_i18n( $item->get_quantity() ) ) ) ); ?></div>
				</div>
				<div style="font-size:13px;font-weight:800;flex-shrink:0;"><?php echo wp_kses_post( carmilla_price( (float) $order->get_line_total( $item, true ) ) ); ?></div>
			</div>
		<?php endforeach; ?>

		<div style="border-top:1px dashed var(--line);margin-top:12px;padding-top:12px;display:flex;flex-direction:column;gap:8px;">
			<?php foreach ( $order->get_order_item_totals() as $total_key => $total_row ) : ?>
				<div style="display:flex;justify-content:space-between;gap:12px;font-size:12.5px;<?php echo 'order_total' === $total_key ? 'font-weight:800;font-size:14px;' : 'color:var(--ink-soft);'; ?>">
					<span><?php echo esc_html( $total_row['label'] ); ?></span>
					<span><?php echo wp_kses_post( carmilla_to_persian_digits( $total_row['value'] ) ); ?></span>
				</div>
			<?php endforeach; ?>
		</div>

		<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:10px;margin-top:14px;">
			<?php if ( ! $carmilla_is_digital && $order->get_formatted_shipping_address() ) : ?>
				<div style="background:var(--surface-2);border-radius:13px;padding:13px 15px;">
					<div class="t-caption t-muted" style="margin-bottom:6px;"><?php esc_html_e( 'آدرس تحویل', 'carmilla' ); ?></div>
					<div style="font-size:12.5px;line-height:2;"><?php echo wp_kses_post( $order->get_formatted_shipping_address() ); ?></div>
				</div>
			<?php elseif ( $carmilla_is_digital ) : ?>
				<div style="background:var(--surface-2);border-radius:13px;padding:13px 15px;">
					<div class="t-caption t-muted" style="margin-bottom:6px;"><?php esc_html_e( 'نوع سفارش', 'carmilla' ); ?></div>
					<div style="font-size:12.5px;line-height:2;"><?php esc_html_e( 'دیجیتال — بدون نیاز به ارسال؛ دسترسی از حساب کاربری فعال می‌شود.', 'carmilla' ); ?></div>
				</div>
			<?php endif; ?>
			<?php if ( $order->get_payment_method_title() ) : ?>
				<div style="background:var(--surface-2);border-radius:13px;padding:13px 15px;">
					<div class="t-caption t-muted" style="margin-bottom:6px;"><?php esc_html_e( 'روش پرداخت', 'carmilla' ); ?></div>
					<div style="font-size:12.5px;line-height:2;"><?php echo esc_html( $order->get_payment_method_title() ); ?></div>
				</div>
			<?php endif; ?>
		</div>
	</div>

	<div style="display:flex;gap:10px;flex-wrap:wrap;margin-bottom:16px;" class="cb-invoice__actions">
		<button type="button" onclick="window.print()" style="font-size:13px;font-weight:700;color:#fff;background:var(--accent);padding:12px 20px;border-radius:13px;border:none;cursor:pointer;font-family:inherit;"><?php esc_html_e( 'دانلود / چاپ فاکتور', 'carmilla' ); ?></button>
		<a href="<?php echo esc_url( wc_get_account_endpoint_url( 'orders' ) ); ?>" style="font-size:13px;font-weight:700;color:var(--accent);background:var(--accent-soft);padding:12px 20px;border-radius:13px;text-decoration:none;"><?php esc_html_e( 'بازگشت به سفارش‌ها', 'carmilla' ); ?></a>
	</div>

	<?php
	// Notes + the theme's tracking timeline (hooked on this action in inc/orders-extra.php).
	$notes = $order->get_customer_order_notes();
	if ( $notes ) :
		?>
		<div class="card card--pad" style="margin-bottom:14px;">
			<h3 class="t-title-sm" style="margin:0 0 10px;"><?php esc_html_e( 'یادداشت‌های سفارش', 'carmilla' ); ?></h3>
			<?php foreach ( $notes as $note ) : ?>
				<div style="border-inline-start:3px solid var(--accent-soft);padding:4px 12px;margin-bottom:8px;">
					<div class="t-caption t-muted"><?php echo esc_html( carmilla_to_persian_digits( date_i18n( 'Y/m/d — H:i', strtotime( $note->comment_date ) ) ) ); ?></div>
					<div class="t-body-sm" style="line-height:2;"><?php echo wp_kses_post( wpautop( wptexturize( $note->comment_content ) ) ); ?></div>
				</div>
			<?php endforeach; ?>
		</div>
	<?php endif; ?>

	<?php do_action( 'woocommerce_order_details_after_order_table', $order ); ?>
</div>

<style media="print">
	header, footer, .cb-acct-nav, .cb-invoice__actions, .bottom-nav, .cb-track { display: none !important; }
	#cb-invoice-print { border: none !important; }
</style>
