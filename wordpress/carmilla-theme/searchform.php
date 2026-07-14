<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit;
}
?>
<form role="search" method="get" class="field" style="display:flex;gap:var(--sp-sm)" action="<?php echo esc_url( home_url( '/' ) ); ?>">
	<input type="search" name="s" value="<?php echo get_search_query(); ?>" placeholder="<?php esc_attr_e( 'جستجو…', 'carmilla' ); ?>" aria-label="<?php esc_attr_e( 'جستجو', 'carmilla' ); ?>">
	<button type="submit" class="btn btn--primary"><?php esc_html_e( 'جستجو', 'carmilla' ); ?></button>
</form>
