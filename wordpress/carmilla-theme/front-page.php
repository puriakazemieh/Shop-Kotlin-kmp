<?php
/**
 * Home page ← ProductsOverviewScreen: hero, stories, categories, product grid,
 * blog teaser. Uses WooCommerce for products and cb_story/cb_banner CPTs when present.
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
$woo = class_exists( 'WooCommerce' );
?>
<div class="container container--wide">

	<!-- Hero -->
	<section class="hero">
		<p class="t-caption" style="color:rgba(255,255,255,.7)"><?php bloginfo( 'name' ); ?></p>
		<h1><?php echo esc_html( get_theme_mod( 'carmilla_hero_title', 'کالکشن پاییز و زمستان' ) ); ?></h1>
		<p><?php echo esc_html( get_theme_mod( 'carmilla_hero_sub', 'جدیدترین محصولات، دوره‌ها و خدمات مشاوره — همه در یک‌جا.' ) ); ?></p>
		<?php if ( $woo ) : ?>
			<a class="btn btn--gold" href="<?php echo esc_url( wc_get_page_permalink( 'shop' ) ); ?>"><?php esc_html_e( 'ورود به فروشگاه', 'carmilla' ); ?></a>
		<?php endif; ?>
	</section>

	<!-- Stories -->
	<?php
	$stories = get_posts( array( 'post_type' => 'cb_story', 'numberposts' => 12, 'post_status' => 'publish', 'suppress_filters' => false ) );
	if ( $stories ) :
		?>
		<section class="section">
			<div class="story-rail">
				<?php foreach ( $stories as $s ) : ?>
					<div class="story">
						<span class="story-ring"><span class="avatar" style="background-image:url('<?php echo esc_url( get_the_post_thumbnail_url( $s->ID, 'thumbnail' ) ?: '' ); ?>');background-size:cover"></span></span>
						<span class="cap"><?php echo esc_html( get_the_title( $s ) ); ?></span>
					</div>
				<?php endforeach; ?>
			</div>
		</section>
	<?php endif; ?>

	<!-- Categories -->
	<?php
	if ( $woo ) :
		$cats = get_terms( array( 'taxonomy' => 'product_cat', 'hide_empty' => true, 'number' => 8, 'parent' => 0 ) );
		if ( ! is_wp_error( $cats ) && $cats ) :
			?>
			<section class="section">
				<div class="section-head"><h2 class="t-title-lg"><?php esc_html_e( 'دسته‌بندی‌ها', 'carmilla' ); ?></h2></div>
				<div class="cat-rail">
					<?php foreach ( $cats as $c ) : ?>
						<a class="chip" href="<?php echo esc_url( get_term_link( $c ) ); ?>"><?php echo esc_html( $c->name ); ?></a>
					<?php endforeach; ?>
				</div>
			</section>
		<?php endif; ?>

		<!-- Product grid -->
		<section class="section">
			<div class="section-head">
				<h2 class="t-title-lg"><?php esc_html_e( 'جدیدترین محصولات', 'carmilla' ); ?></h2>
				<a class="more" href="<?php echo esc_url( wc_get_page_permalink( 'shop' ) ); ?>"><?php esc_html_e( 'مشاهده همه', 'carmilla' ); ?></a>
			</div>
			<div class="grid-adaptive">
				<?php
				$loop = new WP_Query( array( 'post_type' => 'product', 'posts_per_page' => 8, 'post_status' => 'publish' ) );
				while ( $loop->have_posts() ) :
					$loop->the_post();
					wc_get_template_part( 'content', 'product' ) ?: get_template_part( 'template-parts/card', 'product' );
				endwhile;
				wp_reset_postdata();
				?>
			</div>
		</section>
	<?php endif; ?>

	<!-- Blog teaser -->
	<?php
	$posts = get_posts( array( 'numberposts' => 3, 'post_status' => 'publish' ) );
	if ( $posts ) :
		?>
		<section class="section">
			<div class="section-head">
				<h2 class="t-title-lg"><?php esc_html_e( 'از مجله', 'carmilla' ); ?></h2>
				<a class="more" href="<?php echo esc_url( get_permalink( get_option( 'page_for_posts' ) ) ?: home_url( '/blog' ) ); ?>"><?php esc_html_e( 'همه مقالات', 'carmilla' ); ?></a>
			</div>
			<div class="grid-adaptive">
				<?php
				global $post;
				foreach ( $posts as $post ) :
					setup_postdata( $post );
					get_template_part( 'template-parts/card', 'post' );
				endforeach;
				wp_reset_postdata();
				?>
			</div>
		</section>
	<?php endif; ?>

</div>
<?php
get_footer();
