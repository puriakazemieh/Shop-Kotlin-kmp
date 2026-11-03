<?php
/** Disposable installed WordPress only: exercise the registered save_post callback. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/meta-boxes.php';
$suffix = bin2hex( random_bytes( 4 ) );
$user = wp_create_user( 'qa_meta_' . $suffix, wp_generate_password( 24 ), 'qa-meta-' . $suffix . '@example.test' );
if ( ! is_int( $user ) ) { exit( 2 ); }
( new WP_User( $user ) )->set_role( 'administrator' );
$before_user = get_current_user_id();
$before_post = $_POST;
wp_set_current_user( $user );
$post = wp_insert_post( array( 'post_type' => 'cb_banner', 'post_status' => 'draft', 'post_title' => 'Synthetic meta box ' . $suffix ), true );
if ( is_wp_error( $post ) || ! $post ) { wp_set_current_user( $before_user ); wp_delete_user( $user ); exit( 2 ); }
$failed = 0;
$check = static function ( bool $ok, string $label ) use ( &$failed ): void {
	if ( ! $ok ) { ++$failed; }
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n";
};
try {
	update_post_meta( $post, 'cb_subtitle', 'Original' );
	update_post_meta( $post, 'cb_sort', '7' );
	update_post_meta( $post, 'cb_link_url', 'https://example.test/original' );
	$_POST = array( 'carmilla_meta_nonce' => wp_create_nonce( 'carmilla_meta_save' ), 'cb_subtitle' => array( '<script>x</script>' ), 'cb_sort' => array( '99' ), 'cb_link_url' => array( 'https://example.test/new' ) );
	do_action( 'save_post', $post, get_post( $post ), true );
	$check( get_post_meta( $post, 'cb_subtitle', true ) === 'Original', 'array text ignored' );
	$check( get_post_meta( $post, 'cb_sort', true ) === '7', 'array number ignored' );
	$check( get_post_meta( $post, 'cb_link_url', true ) === 'https://example.test/original', 'array URL ignored' );
	$_POST = array( 'carmilla_meta_nonce' => 'invalid', 'cb_subtitle' => 'Changed' );
	do_action( 'save_post', $post, get_post( $post ), true );
	$check( get_post_meta( $post, 'cb_subtitle', true ) === 'Original', 'invalid nonce denied' );
	$_POST = array( 'carmilla_meta_nonce' => wp_create_nonce( 'carmilla_meta_save' ), 'cb_subtitle' => 'Unauthorized' );
	wp_set_current_user( 0 );
	do_action( 'save_post', $post, get_post( $post ), true );
	$check( get_post_meta( $post, 'cb_subtitle', true ) === 'Original', 'missing edit capability denied' );
	wp_set_current_user( $user );
	$_POST = array( 'carmilla_meta_nonce' => wp_create_nonce( 'carmilla_meta_save' ), 'cb_subtitle' => '<b>Updated</b>', 'cb_sort' => '12x' );
	do_action( 'save_post', $post, get_post( $post ), true );
	$check( get_post_meta( $post, 'cb_subtitle', true ) === 'Updated' && get_post_meta( $post, 'cb_sort', true ) === '12', 'valid scalar sanitized and saved' );
} finally {
	$_POST = $before_post;
	wp_delete_post( $post, true );
	wp_set_current_user( $before_user );
	wp_delete_user( $user );
}
$check( get_post( $post ) === null && get_user_by( 'id', $user ) === false, 'synthetic post and user cleaned' );
echo "Failures: $failed.\n";
exit( $failed ? 1 : 0 );
