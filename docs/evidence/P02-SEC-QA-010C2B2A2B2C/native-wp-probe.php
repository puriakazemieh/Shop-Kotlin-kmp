<?php
/** Disposable cloned WordPress database only. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' || getenv( 'WORDPRESS_DB_NAME' ) !== 'cb_qa_integrity_20261102' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
global $wpdb;
$failed = 0;
$check = static function ( bool $ok, string $label ) use ( &$failed ): void { if ( ! $ok ) { ++$failed; } echo ( $ok ? 'PASS ' : 'FAIL ' ) . $label . "\n"; };
$suffix = bin2hex( random_bytes( 4 ) );
$make = static function ( string $label ) use ( $suffix ): int {
	$id = wp_create_user( 'qa_integrity_' . $label . '_' . $suffix, wp_generate_password( 20 ), 'qa-integrity-' . $label . '-' . $suffix . '@example.test' );
	if ( ! is_int( $id ) ) { throw new RuntimeException( 'Synthetic user creation failed.' ); }
	return $id;
};
$ids = array();
$posts = array();
$comments = array();
try {
	$clean = $make( 'clean' ); $ids[] = $clean;
	$check( wp_delete_user( $clean ) === true && ! get_user_by( 'id', $clean ), 'native clean user deletion succeeds' );
	$owner = $make( 'owner' ); $ids[] = $owner;
	$reassign = $make( 'reassign' ); $ids[] = $reassign;
	$post = wp_insert_post( array( 'post_type' => 'post', 'post_status' => 'draft', 'post_title' => 'Synthetic integrity post', 'post_author' => $owner ), true );
	if ( ! is_int( $post ) ) { throw new RuntimeException( 'Synthetic post creation failed.' ); }
	$posts[] = $post;
	$comment = wp_insert_comment( array( 'comment_post_ID' => $post, 'comment_content' => 'Synthetic', 'comment_author' => 'QA', 'comment_author_email' => 'qa@example.test', 'user_id' => $owner, 'comment_approved' => 1 ) );
	if ( ! $comment ) { throw new RuntimeException( 'Synthetic comment creation failed.' ); }
	$comments[] = $comment;
	$check( (int) get_post( $post )->post_author === $owner && (int) get_comment( $comment )->user_id === $owner, 'native post and comment writes preserve owner' );
	$deleted = wp_delete_user( $owner, $reassign );
	$check( $deleted === true && ! get_user_by( 'id', $owner ), 'native deletion with reassignment succeeds' );
	$check( (int) get_post( $post )->post_author === $reassign, 'post author is reassigned' );
	$check( (int) get_comment( $comment )->user_id !== $owner, 'comment no longer references deleted user' );
	$wpdb->last_error = '';
	$bad = $wpdb->insert( $wpdb->comments, array( 'comment_post_ID' => $post, 'comment_content' => 'Bad synthetic', 'comment_author' => 'QA', 'user_id' => $owner + 1000000000 ) );
	$check( $bad === false && $wpdb->last_error !== '', 'direct SQL cannot insert deleted user reference' );
	$wpdb->last_error = '';
	$zero = $wpdb->insert( $wpdb->comments, array( 'comment_post_ID' => $post, 'comment_content' => 'Anonymous synthetic', 'comment_author' => 'QA', 'user_id' => 0 ) );
	if ( $zero ) { $comments[] = (int) $wpdb->insert_id; }
	$check( $zero === 1, 'anonymous zero comment remains valid' );
	$orphans = (int) $wpdb->get_var( "SELECT COUNT(*) FROM {$wpdb->posts} p LEFT JOIN {$wpdb->users} u ON u.ID=p.post_author WHERE p.post_author<>0 AND u.ID IS NULL" ) + (int) $wpdb->get_var( "SELECT COUNT(*) FROM {$wpdb->comments} c LEFT JOIN {$wpdb->users} u ON u.ID=c.user_id WHERE c.user_id<>0 AND u.ID IS NULL" );
	$check( $orphans === 0, 'no nonzero post/comment orphans after native operations' );
} finally {
	foreach ( $comments as $id ) { wp_delete_comment( $id, true ); }
	foreach ( $posts as $id ) { wp_delete_post( $id, true ); }
	foreach ( $ids as $id ) { if ( get_user_by( 'id', $id ) ) { wp_delete_user( $id ); } }
}
echo "Failures: $failed.\n";
exit( $failed ? 1 : 0 );
