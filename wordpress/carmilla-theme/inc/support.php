<?php
/**
 * Support chat — data-driven, theme-only. Each user has one support thread
 * (a private cb_ticket post); messages are stored as comments so staff can reply
 * from the ticket's comments in wp-admin. Front-end via [carmilla_support].
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

add_action( 'init', function () {
	if ( ! post_type_exists( 'cb_ticket' ) ) {
		$args                 = carmilla_cpt_args( 'گفتگوی پشتیبانی', 'پشتیبانی', 'dashicons-format-chat', 'support-threads', array( 'title', 'comments' ) );
		$args['public']       = false;
		$args['show_ui']      = true;
		$args['has_archive']  = false;
		$args['show_in_rest'] = false;
		register_post_type( 'cb_ticket', $args );
	}
}, 11 );

/** Find (or create) the current user's support thread; returns post id. */
function carmilla_support_thread( $create = true ) {
	$uid = get_current_user_id();
	if ( ! $uid ) {
		return 0;
	}
	$found = get_posts( array(
		'post_type'      => 'cb_ticket',
		'author'         => $uid,
		'posts_per_page' => 1,
		'fields'         => 'ids',
		'post_status'    => 'publish',
	) );
	if ( $found ) {
		return (int) $found[0];
	}
	if ( ! $create ) {
		return 0;
	}
	$id = wp_insert_post( array(
		'post_type'   => 'cb_ticket',
		'post_status' => 'publish',
		'post_author' => $uid,
		'post_title'  => sprintf( 'پشتیبانی — %s', wp_get_current_user()->display_name ),
	), true );
	return is_wp_error( $id ) ? 0 : (int) $id;
}

/** Messages of a thread as [ ['me'=>bool,'text'=>,'time'=>], ... ]. */
function carmilla_support_messages( $thread_id ) {
	$uid      = get_current_user_id();
	$comments = get_comments( array( 'post_id' => $thread_id, 'order' => 'ASC', 'status' => 'approve' ) );
	return array_map( function ( $c ) use ( $uid ) {
		return array(
			'me'   => (int) $c->user_id === (int) $uid,
			'text' => $c->comment_content,
			'time' => get_comment_date( 'c', $c ),
		);
	}, $comments );
}

add_action( 'rest_api_init', function () {
	register_rest_route( 'carmilla/v1', '/support', array(
		array(
			'methods'             => 'GET',
			'callback'            => function () {
				$t = carmilla_support_thread( false );
				return rest_ensure_response( array( 'messages' => $t ? carmilla_support_messages( $t ) : array() ) );
			},
			'permission_callback' => 'is_user_logged_in',
		),
		array(
			'methods'             => 'POST',
			'callback'            => 'carmilla_rest_support_send',
			'permission_callback' => 'is_user_logged_in',
		),
	) );
} );

function carmilla_rest_support_send( WP_REST_Request $req ) {
	$text = sanitize_textarea_field( (string) $req->get_param( 'message' ) );
	if ( '' === trim( $text ) ) {
		return new WP_Error( 'validation', 'پیام خالی است.', array( 'status' => 400 ) );
	}
	$thread = carmilla_support_thread( true );
	if ( ! $thread ) {
		return new WP_Error( 'failed', 'ایجاد گفتگو ناموفق بود.', array( 'status' => 400 ) );
	}
	$user = wp_get_current_user();
	wp_insert_comment( array(
		'comment_post_ID'      => $thread,
		'comment_content'      => $text,
		'user_id'              => $user->ID,
		'comment_author'       => $user->display_name,
		'comment_author_email' => $user->user_email,
		'comment_approved'     => 1,
		'comment_type'         => 'cb_support',
	) );
	return rest_ensure_response( array( 'me' => true, 'text' => $text, 'time' => current_time( 'c' ) ) );
}

/** [carmilla_support] — renders the chat UI (login-gated). */
add_shortcode( 'carmilla_support', function () {
	if ( ! is_user_logged_in() ) {
		return '<div class="card card--pad"><p class="t-body">برای گفتگو با پشتیبانی وارد شوید.</p></div>';
	}
	wp_enqueue_script( 'carmilla-support', get_template_directory_uri() . '/assets/js/support.js', array(), CARMILLA_THEME_VERSION, true );
	wp_localize_script( 'carmilla-support', 'CarmillaData', array(
		'restUrl' => esc_url_raw( rest_url( 'carmilla/v1/' ) ),
		'nonce'   => wp_create_nonce( 'wp_rest' ),
	) );
	ob_start();
	?>
	<div id="cs" class="card" style="display:flex;flex-direction:column;height:min(70vh,560px)">
		<div id="cs-messages" style="flex:1;overflow-y:auto;padding:var(--sp-lg);display:flex;flex-direction:column;gap:8px"></div>
		<form id="cs-form" style="display:flex;gap:var(--sp-sm);padding:var(--sp-md);border-block-start:1px solid var(--line)">
			<input type="text" id="cs-input" placeholder="پیام شما…" autocomplete="off">
			<button type="submit" class="btn btn--primary"><?php esc_html_e( 'ارسال', 'carmilla' ); ?></button>
		</form>
	</div>
	<?php
	return ob_get_clean();
} );
