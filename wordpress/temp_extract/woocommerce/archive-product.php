<?php
/**
 * Shop / category archive ← CategoryListingScreen — faithful port of the reference
 * LISTING route: title + count, category chips, sort / price / discount filter row,
 * responsive product grid, pagination. Mirrors docs/design-reference/*.html.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

get_header();

global $wp_query;
$total   = (int) $wp_query->found_posts;
$title   = woocommerce_page_title( false );
$base_url = remove_query_arg( array( 'orderby', 'paged' ) );
$current_orderby = isset( $_GET['orderby'] ) ? sanitize_text_field( wp_unslash( $_GET['orderby'] ) ) : 'menu_order';
$on_sale_active  = isset( $_GET['on_sale'] );

// Category chips: children of the current category, else top-level categories.
$chips = array();
$current_term = is_tax( 'product_cat' ) ? get_queried_object() : null;
$chip_parent  = $current_term ? $current_term->term_id : 0;
$terms = get_terms( array( 'taxonomy' => 'product_cat', 'hide_empty' => true, 'parent' => $chip_parent, 'number' => 12 ) );
if ( ( is_wp_error( $terms ) || ! $terms ) && $current_term ) {
	$terms = get_terms( array( 'taxonomy' => 'product_cat', 'hide_empty' => true, 'parent' => $current_term->parent, 'number' => 12 ) );
}
if ( ! is_wp_error( $terms ) ) {
	foreach ( $terms as $t ) {
		$chips[] = array( 'name' => $t->name, 'url' => get_term_link( $t ), 'active' => $current_term && $current_term->term_id === $t->term_id );
	}
}

$sorts = array(
	'menu_order' => 'پیش‌فرض',
	'date'       => 'جدیدترین',
	'popularity' => 'پرفروش‌ترین',
	'rating'     => 'امتیاز',
);
$prices = array(
	'price'      => 'ارزان‌ترین',
	'price-desc' => 'گران‌ترین',
);
$chip_off = 'background:var(--surface);color:var(--ink-soft);';
$chip_on  = 'background:var(--accent);color:#fff;border-color:var(--accent) !important;';
?>
<div style="animation:fadeUp .35s both;padding-top:20px;">

	<?php if ( function_exists( 'woocommerce_output_all_notices' ) ) { woocommerce_output_all_notices(); } ?>

	<div style="display:flex;align-items:center;gap:12px;margin-bottom:18px;">
		<div style="flex:1;">
			<h1 style="font-size:clamp(19px,3vw,25px);font-weight:800;margin:0;letter-spacing:-.5px;"><?php echo esc_html( $title ?: 'فروشگاه' ); ?></h1>
			<div style="font-size:12px;color:var(--ink-soft);margin-top:3px;"><?php echo esc_html( carmilla_to_persian_digits( $total ) ); ?> کالا</div>
		</div>
	</div>

	<?php if ( $chips ) : ?>
	<div class="noscroll" style="display:flex;gap:9px;overflow-x:auto;padding-bottom:12px;margin-bottom:8px;">
		<?php foreach ( $chips as $c ) : ?>
			<a href="<?php echo esc_url( $c['url'] ); ?>" style="<?php echo $c['active'] ? esc_attr( $chip_on ) : esc_attr( $chip_off ); ?>padding:9px 16px;border-radius:12px;font-size:13px;font-weight:600;white-space:nowrap;flex-shrink:0;border:1px solid var(--line);"><?php echo esc_html( $c['name'] ); ?></a>
		<?php endforeach; ?>
	</div>
	<?php endif; ?>

	<div class="noscroll" style="display:flex;align-items:center;gap:9px;overflow-x:auto;padding-bottom:14px;margin-bottom:14px;border-bottom:1px solid var(--line);">
		<div style="display:flex;align-items:center;gap:5px;color:var(--ink-soft);font-size:12px;font-weight:600;flex-shrink:0;">
			<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18M7 12h10M11 18h2"/></svg> مرتب‌سازی:
		</div>
		<?php foreach ( $sorts as $key => $label ) :
			$active = ( $current_orderby === $key && ! $on_sale_active ); ?>
			<a href="<?php echo esc_url( add_query_arg( 'orderby', $key, remove_query_arg( 'on_sale', $base_url ) ) ); ?>" style="<?php echo $active ? esc_attr( $chip_on ) : esc_attr( $chip_off ); ?>padding:7px 14px;border-radius:11px;font-size:12.5px;font-weight:600;white-space:nowrap;flex-shrink:0;border:1px solid var(--line);"><?php echo esc_html( $label ); ?></a>
		<?php endforeach; ?>
		<div style="width:1px;height:22px;background:var(--line);flex-shrink:0;"></div>
		<?php foreach ( $prices as $key => $label ) :
			$active = ( $current_orderby === $key && ! $on_sale_active ); ?>
			<a href="<?php echo esc_url( add_query_arg( 'orderby', $key, remove_query_arg( 'on_sale', $base_url ) ) ); ?>" style="<?php echo $active ? esc_attr( $chip_on ) : esc_attr( $chip_off ); ?>padding:7px 14px;border-radius:11px;font-size:12.5px;font-weight:600;white-space:nowrap;flex-shrink:0;border:1px solid var(--line);"><?php echo esc_html( $label ); ?></a>
		<?php endforeach; ?>
		<a href="<?php echo esc_url( $on_sale_active ? remove_query_arg( 'on_sale', $base_url ) : add_query_arg( 'on_sale', '1', $base_url ) ); ?>" style="<?php echo $on_sale_active ? esc_attr( $chip_on ) : esc_attr( $chip_off ); ?>padding:7px 14px;border-radius:11px;font-size:12.5px;font-weight:600;white-space:nowrap;flex-shrink:0;border:1px solid var(--line);display:flex;align-items:center;gap:5px;">🏷️ فقط تخفیف‌دار</a>
	</div>

	<?php if ( woocommerce_product_loop() && have_posts() ) : ?>
		<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(165px,1fr));gap:clamp(10px,2vw,18px);">
			<?php
			while ( have_posts() ) {
				the_post();
				wc_get_template_part( 'content', 'product' );
			}
			?>
		</div>
		<div style="margin-top:26px;display:flex;justify-content:center;">
			<?php
			echo wp_kses_post( paginate_links( array(
				'total'     => $wp_query->max_num_pages,
				'current'   => max( 1, get_query_var( 'paged' ) ),
				'prev_text' => '‹',
				'next_text' => '›',
			) ) );
			?>
		</div>
	<?php else : ?>
		<div style="text-align:center;padding:70px 20px;color:var(--ink-soft);">
			<div style="font-size:46px;margin-bottom:10px;">🔍</div>
			<div style="font-size:15px;font-weight:600;">کالایی با این فیلترها پیدا نشد</div>
		</div>
	<?php endif; ?>

</div>
<?php
get_footer();
