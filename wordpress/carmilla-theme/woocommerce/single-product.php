<?php
/**
 * Single product ← DetailsScreen — faithful port of the reference PRODUCT DETAIL:
 * breadcrumb, sticky gallery + info (brand, title, rating/stock, price box with
 * quantity + add-to-cart / buy-now / wishlist, delivery estimate, service badges,
 * description, specs), reviews summary + WooCommerce review form, product Q&A, and
 * related products. Mirrors docs/design-reference/*.html.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

get_header();

while ( have_posts() ) :
	the_post();
	$product = wc_get_product( get_the_ID() );
	if ( ! $product ) {
		continue;
	}
	$id        = $product->get_id();
	$brand     = get_post_meta( $id, 'cb_brand', true );
	$rating    = (float) $product->get_average_rating();
	$rcount    = (int) $product->get_review_count();
	$in_stock  = $product->is_in_stock();
	$stock_qty = $product->get_stock_quantity();
	$reg       = (float) $product->get_regular_price();
	$price     = (float) wc_get_price_to_display( $product );
	$has_off   = $product->is_on_sale() && $reg > 0 && $price < $reg;
	$off_pct   = $has_off ? round( ( ( $reg - $price ) / $reg ) * 100 ) : 0;

	$main_img  = get_the_post_thumbnail_url( $id, 'large' );
	$gallery   = $product->get_gallery_image_ids();
	$cart_url  = wc_get_cart_url();
	$wished    = function_exists( 'carmilla_is_wished' ) && carmilla_is_wished( $id );
	?>
	<div style="animation:fadeUp .35s both;padding-top:18px;">

		<!-- breadcrumb -->
		<div style="display:flex;align-items:center;gap:8px;margin-bottom:18px;font-size:12px;color:var(--ink-soft);">
			<a href="<?php echo esc_url( home_url( '/' ) ); ?>" style="color:var(--ink-soft);">خانه</a><span>/</span>
			<a href="<?php echo esc_url( wc_get_page_permalink( 'shop' ) ); ?>" style="color:var(--ink-soft);">فروشگاه</a>
			<?php if ( $brand ) : ?><span>/</span><span style="color:var(--ink);"><?php echo esc_html( $brand ); ?></span><?php endif; ?>
		</div>

		<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(300px,1fr));gap:clamp(20px,4vw,44px);align-items:start;">

			<!-- gallery -->
			<div class="sticky-col" style="position:sticky;top:120px;">
				<div style="position:relative;aspect-ratio:.92;border-radius:22px;overflow:hidden;background:var(--surface-2);display:grid;place-items:center;border:1px solid var(--line);<?php echo $main_img ? "background:url('" . esc_url( $main_img ) . "') center/cover;" : ''; ?>">
					<?php if ( ! $main_img ) : ?><svg width="32%" viewBox="0 0 24 24" fill="none" stroke="rgba(25,32,56,.18)" stroke-width="1.1"><path d="M12 3a1.6 1.6 0 00-.8 3l-7 4.6A1.5 1.5 0 004 14h16a1.5 1.5 0 00-.2-3.4L12.8 6A1.6 1.6 0 0012 3z"/></svg><?php endif; ?>
					<?php if ( $has_off ) : ?><div style="position:absolute;top:14px;right:14px;background:var(--sale);color:#fff;font-size:13px;font-weight:800;padding:6px 12px;border-radius:11px;"><?php echo esc_html( carmilla_to_persian_digits( $off_pct ) ); ?>٪ تخفیف</div><?php endif; ?>
				</div>
				<?php if ( $gallery ) : ?>
				<div style="display:flex;gap:10px;margin-top:12px;">
					<?php foreach ( array_slice( $gallery, 0, 4 ) as $gid ) :
						$turl = wp_get_attachment_image_url( $gid, 'woocommerce_thumbnail' ); ?>
						<div style="flex:1;aspect-ratio:1;border-radius:13px;border:1.5px solid var(--line);<?php echo $turl ? "background:url('" . esc_url( $turl ) . "') center/cover;" : 'background:var(--surface-2);'; ?>"></div>
					<?php endforeach; ?>
				</div>
				<?php endif; ?>
			</div>

			<!-- info -->
			<div>
				<?php if ( $brand ) : ?><div style="font-size:13px;color:var(--accent);font-weight:700;margin-bottom:8px;"><?php echo esc_html( $brand ); ?></div><?php endif; ?>
				<h1 style="font-size:clamp(20px,3vw,28px);font-weight:800;line-height:1.4;margin:0 0 14px;letter-spacing:-.5px;"><?php the_title(); ?></h1>

				<div style="display:flex;align-items:center;gap:16px;margin-bottom:22px;flex-wrap:wrap;">
					<?php if ( $rating > 0 ) : ?>
					<div style="display:flex;align-items:center;gap:5px;"><svg width="17" height="17" viewBox="0 0 24 24" fill="var(--star)"><path d="M12 3l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.6 6.8 19.2l1-5.8L3.5 9.2l5.9-.9z"/></svg><span style="font-weight:700;font-size:14px;"><?php echo esc_html( carmilla_to_persian_digits( number_format( $rating, 1 ) ) ); ?></span><span style="color:var(--ink-soft);font-size:12.5px;">(<?php echo esc_html( carmilla_to_persian_digits( $rcount ) ); ?> نظر)</span></div>
					<?php endif; ?>
					<?php if ( $in_stock ) : ?>
					<div style="display:flex;align-items:center;gap:5px;color:var(--ok);font-size:12.5px;font-weight:600;"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4"><path d="M20 6L9 17l-5-5"/></svg> <?php echo $stock_qty ? esc_html( carmilla_to_persian_digits( $stock_qty ) . ' عدد موجود' ) : 'موجود'; ?></div>
					<?php endif; ?>
				</div>

				<!-- price + actions -->
				<div style="background:var(--surface);border:1px solid var(--line);border-radius:20px;padding:18px;margin-bottom:22px;">
					<div style="display:flex;align-items:flex-end;justify-content:space-between;margin-bottom:16px;">
						<div>
							<?php if ( $has_off ) : ?><div style="font-size:13px;color:var(--ink-soft);text-decoration:line-through;margin-bottom:3px;"><?php echo esc_html( carmilla_dc_num( $reg ) ); ?> تومان</div><?php endif; ?>
							<div style="font-size:24px;font-weight:800;color:var(--ink);"><?php echo esc_html( carmilla_dc_num( $price ) ); ?> <span style="font-size:13px;font-weight:600;color:var(--ink-soft);">تومان</span></div>
						</div>
						<?php if ( ! $in_stock ) : ?>
							<div style="display:flex;align-items:center;gap:6px;background:rgba(216,69,59,.1);color:var(--sale);font-size:12.5px;font-weight:700;padding:9px 14px;border-radius:11px;"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"/><path d="M15 9l-6 6M9 9l6 6"/></svg> ناموجود</div>
						<?php endif; ?>
					</div>

					<?php if ( $in_stock ) : ?>
						<?php if ( $product->is_type( 'simple' ) ) : ?>
							<form action="<?php echo esc_url( $cart_url ); ?>" method="get" style="display:flex;gap:11px;align-items:stretch;">
								<input type="hidden" name="add-to-cart" value="<?php echo esc_attr( $id ); ?>">
								<input type="number" name="quantity" value="1" min="1" style="width:64px;text-align:center;border:1px solid var(--line);border-radius:13px;font-weight:700;font-size:15px;font-family:inherit;background:var(--surface);color:var(--ink);">
								<button type="submit" style="flex:1;background:var(--accent-soft);color:var(--accent);font-weight:700;font-size:14px;padding:15px;border-radius:14px;border:none;cursor:pointer;display:flex;align-items:center;justify-content:center;gap:7px;"><svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 6h15l-1.6 9H7.5z M6 6L5 3H2"/></svg> افزودن به سبد</button>
								<a href="<?php echo esc_url( add_query_arg( 'add-to-cart', $id, wc_get_checkout_url() ) ); ?>" style="flex:1;background:var(--accent);color:#fff;font-weight:700;font-size:14px;padding:15px;border-radius:14px;text-align:center;">خرید فوری</a>
								<a href="<?php echo esc_url( add_query_arg( 'cb_wish', $id, get_permalink() ) ); ?>" style="width:52px;border:1px solid var(--line);border-radius:14px;display:grid;place-items:center;color:<?php echo $wished ? 'var(--sale)' : 'var(--ink-soft)'; ?>;flex-shrink:0;"><svg width="21" height="21" viewBox="0 0 24 24" fill="<?php echo $wished ? 'var(--sale)' : 'none'; ?>" stroke="currentColor" stroke-width="2"><path d="M12 21s-7-4.6-9.4-9A5 5 0 0112 5a5 5 0 019.4 7C19 16.4 12 21 12 21z"/></svg></a>
							</form>
						<?php else : ?>
							<?php woocommerce_template_single_add_to_cart(); ?>
						<?php endif; ?>
					<?php endif; ?>
				</div>

				<!-- delivery estimate -->
				<div style="background:var(--accent-soft);border-radius:16px;padding:14px 16px;margin-bottom:16px;">
					<div style="display:flex;align-items:center;gap:11px;">
						<div style="width:38px;height:38px;border-radius:11px;background:var(--surface);display:grid;place-items:center;color:var(--accent);flex-shrink:0;"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><path d="M1 4h13v11H1z"/><path d="M14 8h4l3 3v4h-7"/><circle cx="6" cy="18.5" r="1.6"/><circle cx="17.5" cy="18.5" r="1.6"/></svg></div>
						<div style="flex:1;min-width:0;">
							<div style="font-size:13px;font-weight:700;color:var(--ink);">ارسال سریع به سراسر کشور</div>
							<div style="font-size:11.5px;color:var(--ink-soft);margin-top:2px;">تحویل درب منزل توسط پیک</div>
						</div>
						<div style="background:var(--surface);color:var(--accent);font-size:12px;font-weight:700;padding:6px 12px;border-radius:10px;white-space:nowrap;">۲ تا ۳ روز</div>
					</div>
				</div>

				<!-- service badges -->
				<div style="display:grid;grid-template-columns:repeat(3,1fr);gap:10px;margin-bottom:24px;">
					<div style="text-align:center;padding:14px 6px;border:1px solid var(--line);border-radius:14px;"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="var(--accent)" stroke-width="1.7" style="margin-bottom:6px;"><path d="M12 2l8 4v6c0 5-3.5 8-8 10-4.5-2-8-5-8-10V6z"/></svg><div style="font-size:10.5px;color:var(--ink-soft);font-weight:600;">ضمانت اصالت</div></div>
					<div style="text-align:center;padding:14px 6px;border:1px solid var(--line);border-radius:14px;"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="var(--accent)" stroke-width="1.7" style="margin-bottom:6px;"><path d="M3 12a9 9 0 109-9 M3 4v5h5"/></svg><div style="font-size:10.5px;color:var(--ink-soft);font-weight:600;">۷ روز بازگشت</div></div>
					<div style="text-align:center;padding:14px 6px;border:1px solid var(--line);border-radius:14px;"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="var(--accent)" stroke-width="1.7" style="margin-bottom:6px;"><path d="M1 3h15v13H1z M16 8h4l3 3v5h-7"/></svg><div style="font-size:10.5px;color:var(--ink-soft);font-weight:600;">ارسال سریع</div></div>
				</div>

				<?php if ( $product->get_description() ) : ?>
				<!-- description -->
				<div style="margin-bottom:8px;font-size:16px;font-weight:800;">معرفی محصول</div>
				<div style="font-size:13.5px;color:var(--ink-soft);line-height:1.9;margin-bottom:22px;"><?php echo wp_kses_post( wpautop( $product->get_description() ) ); ?></div>
				<?php endif; ?>

				<?php
				$attrs = $product->get_attributes();
				$rows  = array();
				if ( $product->get_sku() ) { $rows['کد محصول'] = $product->get_sku(); }
				foreach ( $attrs as $attr ) {
					if ( ! $attr->get_visible() ) { continue; }
					$name = wc_attribute_label( $attr->get_name() );
					$vals = $product->get_attribute( $attr->get_name() );
					if ( $vals ) { $rows[ $name ] = $vals; }
				}
				if ( $rows ) : ?>
				<!-- specs -->
				<div style="margin-bottom:8px;font-size:16px;font-weight:800;">مشخصات</div>
				<div style="border:1px solid var(--line);border-radius:16px;overflow:hidden;">
					<?php $i = 0; $n = count( $rows ); foreach ( $rows as $k => $v ) : $i++; ?>
						<div style="display:flex;justify-content:space-between;padding:13px 16px;font-size:13px;<?php echo $i < $n ? 'border-bottom:1px solid var(--line);' : ''; ?>"><span style="color:var(--ink-soft);"><?php echo esc_html( $k ); ?></span><span style="font-weight:600;color:var(--ink);"><?php echo esc_html( $v ); ?></span></div>
					<?php endforeach; ?>
				</div>
				<?php endif; ?>
			</div>
		</div>

		<!-- reviews summary + WooCommerce review form/list -->
		<div style="margin-top:40px;padding-top:30px;border-top:1px solid var(--line);">
			<h2 style="font-size:clamp(17px,2.5vw,22px);font-weight:800;margin:0 0 18px;letter-spacing:-.5px;">دیدگاه خریداران</h2>
			<?php if ( $rating > 0 ) : ?>
			<div style="display:flex;align-items:center;gap:18px;margin-bottom:22px;">
				<div style="text-align:center;flex-shrink:0;">
					<div style="font-size:40px;font-weight:800;line-height:1;"><?php echo esc_html( carmilla_to_persian_digits( number_format( $rating, 1 ) ) ); ?></div>
					<div style="display:flex;gap:2px;justify-content:center;margin:8px 0 5px;"><?php for ( $s = 1; $s <= 5; $s++ ) : ?><svg width="15" height="15" viewBox="0 0 24 24" fill="<?php echo $s <= round( $rating ) ? 'var(--star)' : 'var(--surface-2)'; ?>" stroke="none"><path d="M12 3l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.6 6.8 19.2l1-5.8L3.5 9.2l5.9-.9z"/></svg><?php endfor; ?></div>
					<div style="font-size:11.5px;color:var(--ink-soft);"><?php echo esc_html( carmilla_to_persian_digits( $rcount ) ); ?> دیدگاه</div>
				</div>
			</div>
			<?php endif; ?>
			<?php comments_template(); ?>
		</div>

		<!-- Q&A (theme product-qna widget) -->
		<div style="margin-top:34px;">
			<div id="qna" data-product="<?php echo esc_attr( $id ); ?>">
				<div style="display:flex;align-items:center;gap:9px;margin-bottom:16px;">
					<h2 style="font-size:clamp(17px,2.5vw,22px);font-weight:800;margin:0;letter-spacing:-.5px;">پرسش و پاسخ</h2>
				</div>
				<div id="qna-form" style="margin-bottom:18px;"></div>
				<div id="qna-list" style="display:flex;flex-direction:column;gap:13px;"></div>
			</div>
		</div>

		<!-- related -->
		<?php
		$related_ids = wc_get_related_products( $id, 4 );
		if ( $related_ids ) : ?>
		<h2 style="font-size:clamp(17px,2.5vw,22px);font-weight:800;margin:38px 0 18px;letter-spacing:-.5px;">محصولات مشابه</h2>
		<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(165px,1fr));gap:clamp(10px,2vw,18px);">
			<?php foreach ( $related_ids as $rid ) { carmilla_dc_product_card( $rid ); } ?>
		</div>
		<?php endif; ?>

	</div>
	<?php
endwhile;
get_footer();
