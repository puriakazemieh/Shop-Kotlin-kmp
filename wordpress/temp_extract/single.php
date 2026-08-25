<?php
/**
 * Single blog post ← BlogDetailScreen (readable width, Gutenberg content).
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
get_header();
?>
<main class="container container--readable" style="padding-block: var(--sp-xl);">
	<?php
	while ( have_posts() ) :
		the_post();
		?>
		<nav class="breadcrumb">
			<a href="<?php echo esc_url( home_url( '/' ) ); ?>"><?php esc_html_e( 'خانه', 'carmilla' ); ?></a> ›
			<?php $c = get_the_category(); echo $c ? esc_html( $c[0]->name ) : esc_html__( 'مجله', 'carmilla' ); ?>
		</nav>

		<article <?php post_class(); ?>>
			<h1 class="t-headline" style="margin-block:var(--sp-sm)"><?php the_title(); ?></h1>
			<div class="meta-row">
				<span><?php echo esc_html( get_the_author() ); ?></span>
				<span><?php echo esc_html( carmilla_to_persian_digits( get_the_date() ) ); ?></span>
				<span><?php echo esc_html( carmilla_to_persian_digits( ceil( str_word_count( wp_strip_all_tags( get_the_content() ) ) / 200 ) ) ); ?> <?php esc_html_e( 'دقیقه مطالعه', 'carmilla' ); ?></span>
			</div>

			<?php if ( has_post_thumbnail() ) : ?>
				<div class="card" style="margin-block:var(--sp-lg)"><?php the_post_thumbnail( 'large' ); ?></div>
			<?php endif; ?>

			<div class="entry-content t-body">
				<?php the_content(); ?>
			</div>
		</article>

		<?php
		if ( comments_open() || get_comments_number() ) {
			comments_template();
		}
	endwhile;
	?>
</main>
<?php
get_footer();
