<?php
/**
 * Checkout ← CheckoutScreen — the reference CHECKOUT layout (header, step
 * indicator, delivery address, order summary, place-order) wrapped around
 * WooCommerce's real checkout form so payment/order placement keep working.
 * Field markup comes from WooCommerce; visual styling is in assets/css/woocommerce.css.
 * Mirrors docs/design-reference/*.html.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

/** @var WC_Checkout $checkout */
$checkout = WC()->checkout();

do_action( 'woocommerce_before_checkout_form', $checkout );

if ( ! $checkout->is_registration_enabled() && $checkout->is_registration_required() && ! is_user_logged_in() ) {
	echo '<div style="text-align:center;padding:50px 20px;color:var(--ink-soft);">' . esc_html( apply_filters( 'woocommerce_checkout_must_be_logged_in_message', __( 'You must be logged in to checkout.', 'woocommerce' ) ) ) . '</div>';
	return;
}
?>
<div class="cb-checkout" style="animation:fadeUp .35s both;padding-top:20px;max-width:620px;margin:0 auto;">

	<div style="display:flex;align-items:center;gap:12px;margin-bottom:22px;">
		<a href="<?php echo esc_url( wc_get_cart_url() ); ?>" class="mob-hide" style="width:42px;height:42px;border-radius:13px;border:1px solid var(--line);background:var(--surface);display:grid;place-items:center;color:var(--ink);flex-shrink:0;"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 6l6 6-6 6"/></svg></a>
		<h1 style="font-size:clamp(19px,3vw,24px);font-weight:800;margin:0;">تکمیل خرید</h1>
	</div>

	<!-- step indicator -->
	<div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:26px;">
		<div style="display:flex;flex-direction:column;align-items:center;gap:6px;flex:1;"><div style="width:34px;height:34px;border-radius:50%;background:var(--accent);color:#fff;display:grid;place-items:center;font-weight:700;font-size:14px;">۱</div><span style="font-size:11px;font-weight:600;color:var(--ink);">آدرس</span></div>
		<div style="flex:1;height:2px;background:var(--accent);margin-bottom:18px;"></div>
		<div style="display:flex;flex-direction:column;align-items:center;gap:6px;flex:1;"><div style="width:34px;height:34px;border-radius:50%;background:var(--accent);color:#fff;display:grid;place-items:center;font-weight:700;font-size:14px;">۲</div><span style="font-size:11px;font-weight:600;color:var(--ink);">پرداخت</span></div>
		<div style="flex:1;height:2px;background:var(--line);margin-bottom:18px;"></div>
		<div style="display:flex;flex-direction:column;align-items:center;gap:6px;flex:1;"><div style="width:34px;height:34px;border-radius:50%;background:var(--surface-2);color:var(--ink-soft);display:grid;place-items:center;font-weight:700;font-size:14px;">۳</div><span style="font-size:11px;font-weight:600;color:var(--ink-soft);">ثبت</span></div>
	</div>

	<form name="checkout" method="post" class="checkout woocommerce-checkout" action="<?php echo esc_url( wc_get_checkout_url() ); ?>" enctype="multipart/form-data">

		<?php if ( $checkout->get_checkout_fields() ) : ?>
			<?php do_action( 'woocommerce_checkout_before_customer_details' ); ?>
			<div style="font-size:14px;font-weight:700;margin-bottom:11px;">آدرس تحویل</div>
			<div id="customer_details">
				<div class="woocommerce-billing-fields__cb"><?php do_action( 'woocommerce_checkout_billing' ); ?></div>
				<div class="woocommerce-shipping-fields__cb"><?php do_action( 'woocommerce_checkout_shipping' ); ?></div>
			</div>
			<?php do_action( 'woocommerce_checkout_after_customer_details' ); ?>
		<?php endif; ?>

		<div style="font-size:14px;font-weight:700;margin:22px 0 11px;">خلاصه و پرداخت</div>
		<div id="order_review" class="woocommerce-checkout-review-order">
			<?php do_action( 'woocommerce_checkout_order_review' ); ?>
		</div>

	</form>
</div>
<?php
do_action( 'woocommerce_after_checkout_form', $checkout );
