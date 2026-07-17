<?php
/**
 * Home ← ProductsOverviewScreen — faithful port of the reference HOME route:
 * stories, hero, category tiles, amazing offers (countdown), new arrivals grid,
 * promo banners, blog teaser, trust badges.
 * Mirrors docs/design-reference/*.html, wired to WooCommerce + WordPress.
 *
 * @package Carmilla
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
$woo = class_exists( 'WooCommerce' );

/* stories (cb_story CPT) */
$stories = ( carmilla_feature_enabled( 'stories' ) && post_type_exists( 'cb_story' ) )
	? get_posts( array( 'post_type' => 'cb_story', 'numberposts' => 10, 'post_status' => 'publish', 'suppress_filters' => false ) )
	: array();

/* hero copy */
$hero_badge = get_theme_mod( 'carmilla_hero_badge', 'کالکشن جدید رسید ✦' );
$hero_title = get_theme_mod( 'carmilla_hero_title', "جدیدترین‌ها\nبا تخفیف ویژه" );
$hero_sub   = get_theme_mod( 'carmilla_hero_sub', 'برترین محصولات فصل را با ارسال رایگان سفارش دهید.' );
$shop_url   = $woo ? wc_get_page_permalink( 'shop' ) : home_url( '/' );

/* ring palette for stories */
$rings = array( 'linear-gradient(45deg,#B08D57,#E7A93B)', 'linear-gradient(45deg,#20305C,#34487E)', 'linear-gradient(45deg,#D8453B,#E7A93B)', 'linear-gradient(45deg,#1F9D6B,#4EA8DE)' );
?>

<div style="animation:fadeUp .4s both;">

	<?php if ( $stories ) : ?>
	<!-- stories -->
	<div class="noscroll" style="display:flex;gap:16px;overflow-x:auto;padding:20px 2px 6px;">
		<?php foreach ( $stories as $i => $s ) :
			$thumb = get_the_post_thumbnail_url( $s->ID, 'thumbnail' ) ?: ( get_post_meta( $s->ID, 'cb_image_url', true ) ?: '' );
			$full  = get_the_post_thumbnail_url( $s->ID, 'large' ) ?: $thumb;
			$ring  = $rings[ $i % count( $rings ) ];
			$title = get_the_title( $s );
			?>
			<button type="button" class="cb-story-open" data-index="<?php echo esc_attr( $i ); ?>" data-image="<?php echo esc_url( $full ); ?>" data-title="<?php echo esc_attr( $title ); ?>" data-content="<?php echo esc_attr( wp_strip_all_tags( $s->post_content ) ); ?>" data-link="<?php echo esc_url( get_post_meta( $s->ID, 'cb_link_url', true ) ); ?>" style="display:flex;flex-direction:column;align-items:center;gap:7px;flex-shrink:0;width:68px;background:none;border:none;cursor:pointer;padding:0;">
				<div style="width:66px;height:66px;border-radius:50%;padding:2.5px;background:<?php echo esc_attr( $ring ); ?>;">
					<div style="width:100%;height:100%;border-radius:50%;border:2px solid var(--surface);overflow:hidden;<?php echo $thumb ? "background:url('" . esc_url( $thumb ) . "') center/cover;" : 'background:var(--surface-2);'; ?>display:grid;place-items:center;color:var(--ink-soft);font-weight:800;font-size:19px;"><?php echo $thumb ? '' : esc_html( mb_substr( $title, 0, 1, 'UTF-8' ) ); ?></div>
				</div>
				<span style="font-size:11px;color:var(--ink);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:64px;"><?php echo esc_html( $title ); ?></span>
			</button>
		<?php endforeach; ?>
	</div>
	<?php get_template_part( 'template-parts/story-viewer' ); ?>
	<?php endif; ?>

	<!-- hero -->
	<div style="position:relative;border-radius:24px;overflow:hidden;margin-top:14px;min-height:clamp(220px,38vw,400px);background:var(--accent);">
		<div style="position:absolute;inset:0;background:linear-gradient(135deg,var(--accent),var(--accent-2));"></div>
		<div style="position:absolute;inset:0;background:radial-gradient(120% 100% at 90% 10%,rgba(176,141,87,.35),transparent 60%);"></div>
		<div style="position:absolute;inset:0;background:linear-gradient(270deg,rgba(15,18,32,0),rgba(15,18,32,.62));"></div>
		<div style="position:relative;padding:clamp(24px,5vw,52px);max-width:560px;color:#fff;height:100%;display:flex;flex-direction:column;justify-content:center;">
			<div style="display:inline-flex;width:max-content;align-items:center;gap:7px;background:rgba(255,255,255,.16);backdrop-filter:blur(6px);padding:6px 13px;border-radius:30px;font-size:12px;font-weight:600;margin-bottom:16px;"><?php echo esc_html( $hero_badge ); ?></div>
			<h1 style="font-size:clamp(26px,5vw,46px);font-weight:800;line-height:1.18;margin:0 0 12px;letter-spacing:-1px;"><?php echo nl2br( esc_html( $hero_title ) ); ?></h1>
			<p style="font-size:clamp(13px,2vw,16px);opacity:.9;margin:0 0 24px;line-height:1.7;max-width:380px;"><?php echo esc_html( $hero_sub ); ?></p>
			<div style="display:flex;gap:12px;flex-wrap:wrap;">
				<a href="<?php echo esc_url( $shop_url ); ?>" style="background:#fff;color:var(--accent);font-weight:700;font-size:14px;padding:13px 26px;border-radius:13px;">مشاهده کالکشن</a>
				<a href="<?php echo esc_url( add_query_arg( 'on_sale', '1', $shop_url ) ); ?>" style="background:rgba(255,255,255,.14);color:#fff;font-weight:600;font-size:14px;padding:13px 24px;border-radius:13px;border:1px solid rgba(255,255,255,.3);">پیشنهاد شگفت‌انگیز</a>
			</div>
		</div>
	</div>

	<?php
	if ( $woo ) :
		$cats = get_terms( array( 'taxonomy' => 'product_cat', 'hide_empty' => true, 'number' => 8, 'parent' => 0 ) );
		if ( ! is_wp_error( $cats ) && $cats ) : ?>
		<!-- categories -->
		<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(96px,1fr));gap:12px;margin-top:30px;">
			<?php foreach ( $cats as $c ) :
				$thumb_id = get_term_meta( $c->term_id, 'thumbnail_id', true );
				$cimg     = $thumb_id ? wp_get_attachment_image_url( $thumb_id, 'thumbnail' ) : '';
				?>
				<a href="<?php echo esc_url( get_term_link( $c ) ); ?>" style="display:flex;flex-direction:column;align-items:center;gap:10px;background:var(--surface);border:1px solid var(--line);border-radius:18px;padding:18px 8px;" onmouseover="this.style.transform='translateY(-3px)';this.style.borderColor='var(--accent)'" onmouseout="this.style.transform='';this.style.borderColor='var(--line)'">
					<div style="width:48px;height:48px;border-radius:14px;background:var(--accent-soft);display:grid;place-items:center;color:var(--accent);overflow:hidden;">
						<?php if ( $cimg ) : ?><img src="<?php echo esc_url( $cimg ); ?>" alt="" style="width:100%;height:100%;object-fit:cover;"><?php else : ?><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M3 9l9-6 9 6v10a2 2 0 01-2 2H5a2 2 0 01-2-2z"/></svg><?php endif; ?>
					</div>
					<span style="font-size:12px;font-weight:600;color:var(--ink);text-align:center;"><?php echo esc_html( $c->name ); ?></span>
				</a>
			<?php endforeach; ?>
		</div>
		<?php endif; ?>

		<?php
		$deal_ids = function_exists( 'wc_get_product_ids_on_sale' ) ? wc_get_product_ids_on_sale() : array();
		$deal_ids = array_slice( array_filter( (array) $deal_ids ), 0, 10 );
		if ( $deal_ids ) : ?>
		<!-- amazing offers -->
		<div style="margin-top:34px;background:linear-gradient(135deg,var(--accent),var(--accent-2));border-radius:24px;padding:20px clamp(14px,3vw,24px);overflow:hidden;">
			<div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;color:#fff;">
				<div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap;">
					<div style="display:flex;align-items:center;gap:8px;font-weight:800;font-size:clamp(16px,2.5vw,20px);"><span style="font-size:22px;">⚡</span> پیشنهاد شگفت‌انگیز</div>
					<div style="display:flex;align-items:center;gap:5px;direction:ltr;">
						<span id="cb-h" style="background:rgba(255,255,255,.18);min-width:32px;text-align:center;padding:5px 7px;border-radius:9px;font-weight:700;font-size:14px;">۰۰</span><span style="font-weight:800;">:</span>
						<span id="cb-m" style="background:rgba(255,255,255,.18);min-width:32px;text-align:center;padding:5px 7px;border-radius:9px;font-weight:700;font-size:14px;">۰۰</span><span style="font-weight:800;">:</span>
						<span id="cb-s" style="background:rgba(255,255,255,.18);min-width:32px;text-align:center;padding:5px 7px;border-radius:9px;font-weight:700;font-size:14px;">۰۰</span>
					</div>
				</div>
				<a href="<?php echo esc_url( add_query_arg( 'on_sale', '1', $shop_url ) ); ?>" style="font-size:12.5px;font-weight:600;opacity:.92;color:#fff;">مشاهده همه ‹</a>
			</div>
			<div class="noscroll" style="display:flex;gap:12px;overflow-x:auto;padding-bottom:4px;">
				<?php foreach ( $deal_ids as $pid ) :
					$p = wc_get_product( $pid );
					if ( ! $p ) { continue; }
					$reg = (float) $p->get_regular_price(); $sale = (float) wc_get_price_to_display( $p );
					$off = ( $reg > 0 && $sale < $reg ) ? round( ( ( $reg - $sale ) / $reg ) * 100 ) : 0;
					$pimg = get_the_post_thumbnail_url( $pid, 'woocommerce_thumbnail' );
					?>
					<a href="<?php echo esc_url( get_permalink( $pid ) ); ?>" style="flex-shrink:0;width:150px;background:var(--surface);border-radius:16px;padding:10px;">
						<div style="position:relative;aspect-ratio:1;border-radius:11px;overflow:hidden;background:var(--surface-2);margin-bottom:9px;<?php echo $pimg ? "background:url('" . esc_url( $pimg ) . "') center/cover;" : ''; ?>">
							<div style="position:absolute;top:7px;right:7px;background:var(--sale);color:#fff;font-size:11px;font-weight:700;padding:3px 7px;border-radius:8px;"><?php echo esc_html( carmilla_to_persian_digits( $off ) ); ?>٪</div>
						</div>
						<div style="font-size:12px;font-weight:500;color:var(--ink);height:34px;overflow:hidden;line-height:1.5;"><?php echo esc_html( $p->get_name() ); ?></div>
						<div style="display:flex;align-items:center;justify-content:space-between;margin-top:6px;">
							<div>
								<div style="font-size:11px;color:var(--ink-soft);text-decoration:line-through;"><?php echo esc_html( carmilla_dc_num( $reg ) ); ?></div>
								<div style="font-size:13px;font-weight:800;color:var(--ink);"><?php echo esc_html( carmilla_dc_num( $sale ) ); ?></div>
							</div>
							<div style="font-size:9px;color:var(--ink-soft);">تومان</div>
						</div>
					</a>
				<?php endforeach; ?>
			</div>
		</div>
		<script>(function(){var end=new Date();end.setHours(23,59,59,999);function fa(n){return String(n).padStart(2,'0').replace(/[0-9]/g,function(d){return '۰۱۲۳۴۵۶۷۸۹'[d];});}function tick(){var d=Math.max(0,(end-new Date())/1000);var h=Math.floor(d/3600),m=Math.floor(d%3600/60),s=Math.floor(d%60);var H=document.getElementById('cb-h');if(H){H.textContent=fa(h);document.getElementById('cb-m').textContent=fa(m);document.getElementById('cb-s').textContent=fa(s);}}tick();setInterval(tick,1000);})();</script>
		<?php endif; ?>

		<!-- new arrivals -->
		<div style="display:flex;align-items:center;justify-content:space-between;margin:36px 0 18px;">
			<h2 style="font-size:clamp(18px,3vw,24px);font-weight:800;margin:0;letter-spacing:-.5px;">جدیدترین محصولات</h2>
			<a href="<?php echo esc_url( $shop_url ); ?>" style="font-size:13px;font-weight:600;color:var(--accent);">مشاهده همه ‹</a>
		</div>
		<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(165px,1fr));gap:clamp(10px,2vw,18px);">
			<?php
			$loop = new WP_Query( array( 'post_type' => 'product', 'posts_per_page' => 8, 'post_status' => 'publish' ) );
			while ( $loop->have_posts() ) {
				$loop->the_post();
				carmilla_dc_product_card( get_the_ID() );
			}
			wp_reset_postdata();
			?>
		</div>
	<?php endif; // woo ?>

	<?php
	/* promo banners (cb_banner CPT) */
	$banners = ( carmilla_feature_enabled( 'stories' ) && post_type_exists( 'cb_banner' ) )
		? get_posts( array( 'post_type' => 'cb_banner', 'numberposts' => 2, 'post_status' => 'publish' ) )
		: array();
	if ( $banners ) : ?>
	<!-- promo banners -->
	<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:16px;margin-top:34px;">
		<?php foreach ( $banners as $b ) :
			$bimg  = get_the_post_thumbnail_url( $b->ID, 'large' ) ?: get_post_meta( $b->ID, 'cb_image_url', true );
			$blink = get_post_meta( $b->ID, 'cb_link_url', true ) ?: '#';
			?>
			<a href="<?php echo esc_url( $blink ); ?>" style="position:relative;border-radius:20px;overflow:hidden;min-height:160px;background:var(--surface-2);<?php echo $bimg ? "background:url('" . esc_url( $bimg ) . "') center/cover;" : ''; ?>">
				<div style="position:absolute;inset:0;background:linear-gradient(270deg,transparent,rgba(15,18,32,.55));"></div>
				<div style="position:relative;padding:24px;color:#fff;">
					<div style="font-size:20px;font-weight:800;line-height:1.4;letter-spacing:-.5px;"><?php echo esc_html( get_the_title( $b ) ); ?></div>
					<div style="margin-top:14px;display:inline-flex;align-items:center;gap:5px;background:rgba(255,255,255,.18);padding:8px 15px;border-radius:11px;font-size:13px;font-weight:600;">مشاهده</div>
				</div>
			</a>
		<?php endforeach; ?>
	</div>
	<?php endif; ?>

	<?php
	/* blog teaser */
	$blogposts = carmilla_feature_enabled( 'blog' ) ? get_posts( array( 'numberposts' => 3, 'post_status' => 'publish' ) ) : array();
	if ( $blogposts ) : ?>
	<!-- blog teaser -->
	<div style="display:flex;align-items:center;justify-content:space-between;margin:38px 0 18px;">
		<h2 style="font-size:clamp(18px,3vw,24px);font-weight:800;margin:0;letter-spacing:-.5px;">مجله <?php bloginfo( 'name' ); ?></h2>
		<a href="<?php echo esc_url( get_permalink( get_option( 'page_for_posts' ) ) ?: home_url( '/blog' ) ); ?>" style="font-size:13px;font-weight:600;color:var(--accent);">همه مقالات ‹</a>
	</div>
	<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(240px,1fr));gap:18px;">
		<?php foreach ( $blogposts as $a ) :
			$aimg = get_the_post_thumbnail_url( $a->ID, 'large' );
			$cat  = get_the_category( $a->ID );
			?>
			<a href="<?php echo esc_url( get_permalink( $a ) ); ?>" style="display:block;background:var(--surface);border:1px solid var(--line);border-radius:18px;overflow:hidden;">
				<div style="aspect-ratio:1.7;overflow:hidden;<?php echo $aimg ? "background:url('" . esc_url( $aimg ) . "') center/cover;" : 'background:var(--surface-2);'; ?>display:grid;place-items:center;">
					<?php if ( ! $aimg ) : ?><svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="rgba(25,32,56,.28)" stroke-width="1.2"><path d="M4 5h16v14H4z M8 9h8 M8 13h6"/></svg><?php endif; ?>
				</div>
				<div style="padding:15px 16px 18px;">
					<?php if ( $cat ) : ?><div style="font-size:11px;font-weight:600;color:var(--accent);margin-bottom:8px;"><?php echo esc_html( $cat[0]->name ); ?></div><?php endif; ?>
					<div style="font-size:15px;font-weight:700;color:var(--ink);line-height:1.6;margin-bottom:9px;"><?php echo esc_html( get_the_title( $a ) ); ?></div>
					<div style="font-size:12.5px;color:var(--ink-soft);line-height:1.8;height:46px;overflow:hidden;"><?php echo esc_html( wp_trim_words( wp_strip_all_tags( $a->post_content ), 22 ) ); ?></div>
				</div>
			</a>
		<?php endforeach; ?>
	</div>
	<?php endif; ?>

	<!-- trust -->
	<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:14px;margin-top:38px;padding-top:30px;border-top:1px solid var(--line);">
		<?php
		$trust = array(
			array( 'M20 6L9 17l-5-5', 'ضمانت اصالت', 'کالای اورجینال' ),
			array( 'M3 7h13v10H3z M16 10h3l2 3v4h-5', 'ارسال سریع', 'تحویل درب منزل' ),
			array( 'M12 2a10 10 0 100 20 10 10 0 000-20 M12 6v6l4 2', 'پشتیبانی ۲۴/۷', 'همیشه در دسترس' ),
			array( 'M5 5h14a2 2 0 012 2v10a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2z M3 10h18', 'پرداخت امن', 'درگاه معتبر' ),
		);
		foreach ( $trust as $t ) : ?>
			<div style="display:flex;align-items:center;gap:13px;">
				<div style="width:46px;height:46px;border-radius:13px;background:var(--accent-soft);display:grid;place-items:center;color:var(--accent);flex-shrink:0;">
					<svg width="23" height="23" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="<?php echo esc_attr( $t[0] ); ?>"/></svg>
				</div>
				<div>
					<div style="font-size:13px;font-weight:700;color:var(--ink);"><?php echo esc_html( $t[1] ); ?></div>
					<div style="font-size:11px;color:var(--ink-soft);margin-top:3px;"><?php echo esc_html( $t[2] ); ?></div>
				</div>
			</div>
		<?php endforeach; ?>
	</div>

</div>
<?php
get_footer();
