<?php
/** Run only in disposable installed WordPress with qa-wallet-guard-mu.php copied to mu-plugins. */
if ( getenv( 'CARMILLA_WALLET_QA_DISPOSABLE' ) !== 'yes' || ! file_exists( '/tmp/carmilla-wallet-qa-enabled' ) ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
$user = wp_create_user( 'qa_wallet_' . bin2hex( random_bytes( 4 ) ), wp_generate_password( 24 ), 'qa-wallet@example.test' );
if ( ! is_int( $user ) ) { exit( 2 ); }
( new WP_User( $user ) )->set_role( 'administrator' );
update_user_meta( $user, '_carmilla_wallet_balance', 250 );
$token = WP_Session_Tokens::get_instance( $user )->create( time() + 3600 );
$auth = wp_generate_auth_cookie( $user, time() + 3600, 'auth', $token );
$logged = wp_generate_auth_cookie( $user, time() + 3600, 'logged_in', $token );
$_COOKIE[ LOGGED_IN_COOKIE ] = $logged;
wp_set_current_user( $user );
$nonce = wp_create_nonce( 'carmilla_qa_wallet_guard' );
$cookie = AUTH_COOKIE . '=' . $auth . '; ' . LOGGED_IN_COOKIE . '=' . $logged;
try {
    $response = wp_remote_post( 'http://127.0.0.1/wp-admin/admin-post.php', array(
        'body' => array( 'action' => 'carmilla_qa_wallet_guard', 'qa_nonce' => $nonce ),
        'headers' => array( 'Cookie' => $cookie ), 'redirection' => 0, 'timeout' => 15,
    ) );
    $data = is_wp_error( $response ) ? null : json_decode( wp_remote_retrieve_body( $response ), true );
    $ok = ! is_wp_error( $response ) && wp_remote_retrieve_response_code( $response ) === 200 &&
        is_array( $data ) && $data['before'] === 250 && $data['after'] === 250 &&
        $data['credit'] === false && $data['debit'] === false &&
        (float) get_user_meta( $user, '_carmilla_wallet_balance', true ) === 250.0;
    echo $ok ? "PASS installed WordPress HTTP wallet mutations denied\n" : "FAIL installed WordPress HTTP wallet guard\n";
    if ( ! $ok ) { exit( 1 ); }
} finally {
    WP_Session_Tokens::get_instance( $user )->destroy( $token );
    wp_delete_user( $user );
}
