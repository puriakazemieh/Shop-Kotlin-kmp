<?php
/**
 * Cart ← CartScreen — faithful port of the reference CART route: item cards with a
 * quantity stepper + remove, and a sticky order summary with coupon, savings,
 * shipping and the checkout button. Mirrors docs/design-reference/*.html while
 * keeping WooCommerce's cart form working (update/remove/coupon).
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

do_action( 'woocommerce_before_cart' );

$cart     = WC()->cart;
$items    = $cart->get_cart();
$totals   = $cart->get_totals();
$cart_url = wc_get_cart_url();

/* estimated savings: sum of (regular - active) * qty across the cart */
$saved = 0.0;
foreach ( $items as $ci ) {
	$p = $ci['data'];
	if ( $p && $p->is_on_sale() ) {
		$saved += ( (float) wc_get_price_to_display( $p, array( 'price' => $p->get_regular_price() ) ) - (float) wc_get_price_to_display( $p ) ) * $ci['quantity'];
	}
}
$ship_total = (float) $totals['shipping_total'];
?>
<div style="animation:fadeUp .35s both;padding-top:20px;">
	<h1 style="font-size:clamp(20px,3vw,26px);font-weight:800;margin:0 0 20px;letter-spacing:-.5px;">سبد خرید</h1>

	<?php if ( empty( $items ) ) : ?>
		<div style="text-align:center;padding:70px 20px;">
			<div style="width:90px;height:90px;border-radius:50%;background:var(--surface-2);display:grid;place-items:center;margin:0 auto 18px;color:var(--ink-soft);"><svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><path d="M6 6h15l-1.6 9H7.5z M6 6L5 3H2 M9 20a1 1 0 100 .1 M18 20a1 1 0 100 .1"/></svg></div>
			<div style="font-size:16px;font-weight:700;margin-bottom:8px;">سبد خرید شما خالی است</div>
			<div style="font-size:13px;color:var(--ink-soft);margin-bottom:22px;">از فروشگاه دیدن کنید و محصولات موردعلاقه‌تان را اضافه کنید.</div>
			<a href="<?php echo esc_url( wc_get_page_permalink( 'shop' ) ); ?>" style="display:inline-block;background:var(--accent);color:#fff;font-weight:700;font-size:14px;padding:13px 28px;border-radius:13px;">شروع خرید</a>
		</div>
	<?php else : ?>
		<form action="<?php echo esc_url( $cart_url ); ?>" method="post">
			<?php wp_nonce_field( 'woocommerce-cart', 'woocommerce-cart-nonce' ); ?>
			<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(300px,1fr));gap:24px;align-items:start;">

				<div style="display:flex;flex-direction:column;gap:12px;">
					<?php foreach ( $items as $key => $ci ) :
						$product   = $ci['data'];
						if ( ! $product || ! $product->exists() || $ci['quantity'] <= 0 ) { continue; }
						$permalink = $product->is_visible() ? $product->get_permalink( $ci ) : '';
						$img       = $product->get_image_id() ? wp_get_attachment_image_url( $product->get_image_id(), 'woocommerce_thumbnail' ) : '';
						$line      = (float) $ci['line_total'] + (float) $ci['line_tax'];
						$meta      = wc_get_formatted_cart_item_data( $ci, true );
						?>
						<div style="display:flex;gap:14px;background:var(--surface);border:1px solid var(--line);border-radius:18px;padding:13px;">
							<a href="<?php echo esc_url( $permalink ); ?>" style="width:90px;height:104px;border-radius:13px;flex-shrink:0;display:grid;place-items:center;<?php echo $img ? "background:url('" . esc_url( $img ) . "') center/cover;" : 'background:var(--surface-2);'; ?>"><?php if ( ! $img ) : ?><svg width="38%" viewBox="0 0 24 24" fill="none" stroke="rgba(25,32,56,.2)" stroke-width="1.2"><path d="M12 3a1.6 1.6 0 00-.8 3l-7 4.6A1.5 1.5 0 004 14h16z"/></svg><?php endif; ?></a>
							<div style="flex:1;min-width:0;display:flex;flex-direction:column;">
								<div style="display:flex;justify-content:space-between;gap:8px;">
									<a href="<?php echo esc_url( $permalink ); ?>" style="font-size:13.5px;font-weight:600;line-height:1.5;color:var(--ink);"><?php echo esc_html( $product->get_name() ); ?></a>
									<a href="<?php echo esc_url( wc_get_cart_remove_url( $key ) ); ?>" style="color:var(--ink-soft);flex-shrink:0;" title="حذف"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M3 6h18 M8 6V4h8v2 M6 6l1 14h10l1-14"/></svg></a>
								</div>
								<?php if ( $meta ) : ?><div style="margin-top:6px;font-size:11.5px;color:var(--ink-soft);"><?php echo wp_kses_post( $meta ); ?></div><?php endif; ?>
								<div style="margin-top:auto;display:flex;align-items:center;justify-content:space-between;padding-top:10px;gap:10px;">
									<div style="display:flex;align-items:center;border:1px solid var(--line);border-radius:11px;overflow:hidden;">
										<?php if ( $product->is_sold_individually() ) : ?>
											<input type="hidden" name="cart[<?php echo esc_attr( $key ); ?>][qty]" value="1">
											<div style="min-width:32px;text-align:center;font-weight:700;font-size:14px;padding:8px 10px;">۱</div>
										<?php else : ?>
											<input type="number" name="cart[<?php echo esc_attr( $key ); ?>][qty]" value="<?php echo esc_attr( $ci['quantity'] ); ?>" min="0" inputmode="numeric" style="width:56px;height:34px;text-align:center;font-weight:700;font-size:14px;border:none;background:transparent;color:var(--ink);font-family:inherit;">
										<?php endif; ?>
									</div>
									<div style="font-size:15px;font-weight:800;"><?php echo esc_html( carmilla_dc_num( $line ) ); ?> <span style="font-size:10px;font-weight:500;color:var(--ink-soft);">تومان</span></div>
								</div>
							</div>
						</div>
					<?php endforeach; ?>
					<div style="display:flex;gap:10px;flex-wrap:wrap;margin-top:2px;">
						<button type="submit" name="update_cart" value="1" style="background:var(--surface);border:1px solid var(--line);color:var(--ink);font-weight:600;font-size:12.5px;padding:10px 16px;border-radius:11px;cursor:pointer;">به‌روزرسانی سبد</button>
						<a href="<?php echo esc_url( wc_get_page_permalink( 'shop' ) ); ?>" style="background:var(--surface);border:1px solid var(--line);color:var(--ink-soft);font-weight:600;font-size:12.5px;padding:10px 16px;border-radius:11px;">ادامه خرید</a>
					</div>
				</div>

				<div class="sticky-col" style="background:var(--surface);border:1px solid var(--line);border-radius:20px;padding:20px;position:sticky;top:120px;">
					<div style="font-size:16px;font-weight:800;margin-bottom:16px;">خلاصه سفارش</div>
					<div style="display:flex;justify-content:space-between;font-size:13px;color:var(--ink-soft);margin-bottom:11px;"><span>قیمت کالاها</span><span style="color:var(--ink);font-weight:600;"><?php echo esc_html( carmilla_dc_num( $totals['subtotal'] ) ); ?> تومان</span></div>
					<?php if ( $saved > 0 ) : ?><div style="display:flex;justify-content:space-between;font-size:13px;color:var(--ink-soft);margin-bottom:11px;"><span>سود شما از خرید</span><span style="color:var(--sale);font-weight:700;">−<?php echo esc_html( carmilla_dc_num( $saved ) ); ?> تومان</span></div><?php endif; ?>
					<div style="display:flex;justify-content:space-between;font-size:13px;color:var(--ink-soft);margin-bottom:11px;"><span>هزینه ارسال</span><span style="color:var(--ok);font-weight:700;"><?php echo $ship_total > 0 ? esc_html( carmilla_dc_num( $ship_total ) . ' تومان' ) : 'رایگان'; ?></span></div>
					<?php foreach ( $cart->get_applied_coupons() as $code ) : ?>
						<div style="display:flex;justify-content:space-between;font-size:13px;color:var(--accent);margin-bottom:11px;"><span>تخفیف کد (<?php echo esc_html( $code ); ?>)</span><span style="font-weight:700;">−<?php echo esc_html( carmilla_dc_num( $cart->get_coupon_discount_amount( $code ) ) ); ?> تومان</span></div>
					<?php endforeach; ?>

					<?php if ( wc_coupons_enabled() ) : ?>
					<div style="display:flex;gap:8px;margin:14px 0;">
						<input type="text" name="coupon_code" placeholder="کد تخفیف" style="flex:1;border:1px solid var(--line);border-radius:11px;padding:11px 13px;font-family:inherit;font-size:12.5px;background:var(--surface-2);color:var(--ink);min-width:0;">
						<button type="submit" name="apply_coupon" value="1" style="background:var(--ink);color:var(--bg);font-weight:700;font-size:12.5px;padding:11px 16px;border-radius:11px;cursor:pointer;border:none;white-space:nowrap;">اعمال</button>
					</div>
					<?php endif; ?>

					<div style="height:1px;background:var(--line);margin:15px 0;"></div>
					<div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:18px;"><span style="font-size:14px;font-weight:700;">مبلغ قابل پرداخت</span><span style="font-size:18px;font-weight:800;"><?php echo esc_html( carmilla_dc_num( $totals['total'] ) ); ?> <span style="font-size:11px;font-weight:500;color:var(--ink-soft);">تومان</span></span></div>
					<a href="<?php echo esc_url( wc_get_checkout_url() ); ?>" style="display:block;background:var(--accent);color:#fff;font-weight:700;font-size:15px;padding:15px;border-radius:14px;text-align:center;">ادامه فرایند خرید</a>
					<div style="display:flex;align-items:center;gap:7px;justify-content:center;margin-top:14px;font-size:11px;color:var(--ink-soft);"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 11V7a7 7 0 0114 0v4 M4 11h16v10H4z"/></svg> پرداخت امن و رمزنگاری‌شده</div>
				</div>
			</div>
		</form>
	<?php endif; ?>
</div>
<?php
do_action( 'woocommerce_after_cart' );
