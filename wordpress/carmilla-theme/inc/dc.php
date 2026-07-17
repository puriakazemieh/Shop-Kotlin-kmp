<?php
/**
 * Helpers for the DC (Design-Compose) faithful templates. These emit markup that
 * mirrors docs/design-reference/*.html pixel-for-pixel (inline styles + var(--token)),
 * wired to real WooCommerce / WordPress data.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

// "Only discounted" filter for the shop/category archive (?on_sale=1).
add_action( 'woocommerce_product_query', function ( $q ) {
	if ( is_admin() || ! isset( $_GET['on_sale'] ) ) { // phpcs:ignore WordPress.Security.NonceVerification
		return;
	}
	$ids = function_exists( 'wc_get_product_ids_on_sale' ) ? wc_get_product_ids_on_sale() : array();
	$q->set( 'post__in', array_merge( array( 0 ), array_map( 'absint', (array) $ids ) ) );
} );

/** Moon/sun path for the header theme toggle (light shows moon, JS swaps to sun). */
function carmilla_dc_theme_icon() {
	return 'M21 12.8A9 9 0 1111.2 3a7 7 0 009.8 9.8z';
}

/** A soft brand-tinted placeholder background for products without an image. */
function carmilla_dc_placeholder_bg( $seed = 0 ) {
	$tints = array(
		'linear-gradient(135deg,#EAEDF6,#F1EFE9)',
		'linear-gradient(135deg,#F3ECE0,#F6F5F1)',
		'linear-gradient(135deg,#E7EEF6,#EEF1F5)',
		'linear-gradient(135deg,#F1E9F0,#F5F1F4)',
	);
	return $tints[ absint( $seed ) % count( $tints ) ];
}

/** Price number (Persian digits, thousands separator) without the unit. */
function carmilla_dc_num( $value ) {
	return carmilla_to_persian_digits( carmilla_format_number( $value ) );
}

/**
 * Faithful product card (mirrors the "new arrivals" card in the reference home /
 * listing). Accepts a WC_Product or a product ID.
 */
function carmilla_dc_product_card( $product ) {
	if ( ! $product instanceof WC_Product ) {
		$product = wc_get_product( $product );
	}
	if ( ! $product ) {
		return;
	}
	$id        = $product->get_id();
	$permalink = get_permalink( $id );
	$name      = $product->get_name();
	$brand     = get_post_meta( $id, 'cb_brand', true );
	$img       = get_the_post_thumbnail_url( $id, 'woocommerce_thumbnail' );
	$rating    = (float) $product->get_average_rating();
	$reviews   = (int) $product->get_review_count();
	$sold      = (int) get_post_meta( $id, 'total_sales', true );
	$in_stock  = $product->is_in_stock();

	$reg  = (float) $product->get_regular_price();
	$sale = (float) $product->get_sale_price();
	$price = (float) wc_get_price_to_display( $product );
	$has_off = $product->is_on_sale() && $reg > 0 && $sale > 0 && $sale < $reg;
	$off_pct = $has_off ? round( ( ( $reg - $sale ) / $reg ) * 100 ) : 0;

	$badge = '';
	if ( $product->is_featured() ) {
		$badge = 'ویژه';
	} elseif ( ( time() - get_post_time( 'U', true, $id ) ) < 15 * DAY_IN_SECONDS ) {
		$badge = 'جدید';
	}

	$add_url = $in_stock ? esc_url( add_query_arg( 'add-to-cart', $id, wc_get_cart_url() ) ) : '#';
	$wish_url = esc_url( add_query_arg( array( 'cb_wish' => $id ), $permalink ) );
	?>
	<a href="<?php echo esc_url( $permalink ); ?>" style="display:block;background:var(--surface);border:1px solid var(--line);border-radius:18px;overflow:hidden;cursor:pointer;transition:transform .15s,box-shadow .15s;" onmouseover="this.style.transform='translateY(-4px)';this.style.boxShadow='0 14px 30px rgba(20,25,45,.1)'" onmouseout="this.style.transform='';this.style.boxShadow=''">
		<div style="position:relative;aspect-ratio:.82;background:var(--surface-2);overflow:hidden;">
			<?php if ( $img ) : ?>
				<div style="position:absolute;inset:0;background:url('<?php echo esc_url( $img ); ?>') center/cover no-repeat;"></div>
			<?php else : ?>
				<div style="position:absolute;inset:0;background:<?php echo esc_attr( carmilla_dc_placeholder_bg( $id ) ); ?>;display:grid;place-items:center;"><svg width="40%" viewBox="0 0 24 24" fill="none" stroke="rgba(25,32,56,.2)" stroke-width="1.2"><path d="M12 3a1.6 1.6 0 00-.8 3l-7 4.6A1.5 1.5 0 004 14h16a1.5 1.5 0 00-.2-3.4L12.8 6A1.6 1.6 0 0012 3z"/></svg></div>
			<?php endif; ?>
			<?php if ( ! $in_stock ) : ?>
				<div style="position:absolute;inset:0;background:rgba(248,247,244,.6);display:grid;place-items:center;z-index:2;"><div style="background:rgba(30,35,50,.82);color:#fff;font-size:12px;font-weight:700;padding:7px 16px;border-radius:30px;">ناموجود</div></div>
			<?php endif; ?>
			<?php if ( $has_off ) : ?>
				<div style="position:absolute;top:10px;right:10px;background:var(--sale);color:#fff;font-size:11px;font-weight:700;padding:4px 9px;border-radius:9px;"><?php echo esc_html( carmilla_to_persian_digits( $off_pct ) ); ?>٪</div>
			<?php elseif ( $badge ) : ?>
				<div style="position:absolute;top:10px;right:10px;background:var(--accent);color:#fff;font-size:11px;font-weight:700;padding:4px 9px;border-radius:9px;"><?php echo esc_html( $badge ); ?></div>
			<?php endif; ?>
			<a href="<?php echo $wish_url; // phpcs:ignore ?>" onclick="event.stopPropagation()" style="position:absolute;top:9px;left:9px;width:32px;height:32px;border-radius:10px;background:var(--surface);display:grid;place-items:center;color:var(--ink-soft);box-shadow:0 3px 8px rgba(0,0,0,.08);">
				<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 21s-7-4.6-9.4-9A5 5 0 0112 5a5 5 0 019.4 7C19 16.4 12 21 12 21z"/></svg>
			</a>
		</div>
		<div style="padding:12px 13px 14px;">
			<?php if ( $brand ) : ?><div style="font-size:10.5px;color:var(--ink-soft);margin-bottom:5px;"><?php echo esc_html( $brand ); ?></div><?php endif; ?>
			<div style="font-size:13px;font-weight:600;color:var(--ink);height:38px;overflow:hidden;line-height:1.5;"><?php echo esc_html( $name ); ?></div>
			<?php if ( $rating > 0 ) : ?>
				<div style="display:flex;align-items:center;gap:5px;margin:9px 0;">
					<svg width="14" height="14" viewBox="0 0 24 24" fill="var(--star)" stroke="none"><path d="M12 3l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.6 6.8 19.2l1-5.8L3.5 9.2l5.9-.9z"/></svg>
					<span style="font-size:11.5px;font-weight:600;color:var(--ink);"><?php echo esc_html( carmilla_to_persian_digits( number_format( $rating, 1 ) ) ); ?></span>
					<span style="font-size:11px;color:var(--ink-soft);">(<?php echo esc_html( carmilla_to_persian_digits( $reviews ?: $sold ) ); ?>)</span>
				</div>
			<?php else : ?><div style="height:9px"></div><?php endif; ?>
			<div style="display:flex;align-items:flex-end;justify-content:space-between;">
				<div>
					<?php if ( $has_off ) : ?><div style="font-size:11px;color:var(--ink-soft);text-decoration:line-through;"><?php echo esc_html( carmilla_dc_num( $reg ) ); ?></div><?php endif; ?>
					<div style="font-size:15px;font-weight:800;color:var(--ink);"><?php echo esc_html( carmilla_dc_num( $price ) ); ?> <span style="font-size:10px;font-weight:500;color:var(--ink-soft);">تومان</span></div>
				</div>
				<?php if ( $in_stock ) : ?>
				<a href="<?php echo $add_url; // phpcs:ignore ?>" onclick="event.stopPropagation()" style="width:34px;height:34px;border-radius:11px;background:var(--accent);display:grid;place-items:center;color:#fff;flex-shrink:0;">
					<svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
				</a>
				<?php endif; ?>
			</div>
		</div>
	</a>
	<?php
}
