<?php
/** Disposable installed WordPress only. Never sends mail/SMS; pre_wp_mail intercepts mail. */
if ( getenv( 'CARMILLA_LOGIN_DISPOSABLE' ) !== 'yes' ) { exit( 2 ); }
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/user.php';
require_once ABSPATH . 'wp-admin/includes/template.php';
require_once '/var/www/html/wp-content/plugins/carmilla-bridge/includes/class-cb-integrations-controller.php';
$mode = $argv[1] ?? '';
if ( ! in_array( $mode, array( 'email', 'invalid', 'array', 'tab', 'bad_nonce', 'no_cap' ), true ) ) { exit( 2 ); }
$suffix = bin2hex( random_bytes( 4 ) );
$user = wp_create_user( 'qa_integration_' . $suffix, wp_generate_password( 24 ), 'qa-integration-' . $suffix . '@example.test' );
if ( ! is_int( $user ) ) { exit( 2 ); }
( new WP_User( $user ) )->set_role( $mode === 'no_cap' ? 'subscriber' : 'administrator' );
wp_set_current_user( $user );
$seen = array( 'mail' => false, 'body' => false, 'redirect' => false, 'die' => false, 'tab' => false );
register_shutdown_function( static function () use ( &$seen, $mode, $user ): void {
	wp_delete_user( $user );
	$ok = $mode === 'email' ? ( $seen['mail'] && $seen['body'] && $seen['redirect'] )
		: ( $mode === 'tab' ? $seen['tab'] : ( $seen['die'] && ! $seen['mail'] && ! $seen['redirect'] ) );
	echo ( $ok ? 'PASS ' : 'FAIL ' ) . "integration_$mode mail=" . (int) $seen['mail'] . ' body=' . (int) $seen['body'] . ' redirect=' . (int) $seen['redirect'] . ' die=' . (int) $seen['die'] . ' tab=' . (int) $seen['tab'] . "\n";
} );
add_filter( 'pre_wp_mail', static function ( $result, $args ) use ( &$seen ) {
	$seen['mail'] = true;
	$seen['body'] = ( $args['message'] ?? '' ) === "Hello'world";
	return true; // no external delivery
}, 10, 2 );
add_filter( 'wp_redirect', static function ( $location ) use ( &$seen ) {
	$seen['redirect'] = strpos( $location, 'admin.php?page=carmilla-integrations' ) !== false && strpos( $location, 'status=success' ) !== false;
	return $location;
} );
add_filter( 'wp_die_handler', static function () use ( &$seen ) {
	return static function () use ( &$seen ): void { $seen['die'] = true; exit; };
} );
$controller = new CB_Integrations_Controller();
if ( $mode === 'tab' ) {
	$_GET['tab'] = array( 'not-a-tab' );
	ob_start();
	$controller->render_admin_page();
	$html = ob_get_clean();
	$seen['tab'] = strpos( $html, 'sms_provider' ) !== false;
	exit;
}
$_POST = array(
	'carmilla_test_message_nonce' => wp_create_nonce( 'carmilla_test_message' ),
	'test_type' => $mode === 'invalid' ? 'fax' : ( $mode === 'array' ? array( 'email' ) : 'email' ),
	'test_recipient' => $mode === 'array' ? array( 'qa@example.test' ) : 'qa@example.test',
	'test_body' => $mode === 'array' ? array( 'Hello' ) : "Hello\\'world",
);
if ( $mode === 'bad_nonce' ) { $_POST['carmilla_test_message_nonce'] = 'invalid'; }
$_REQUEST = $_POST;
$controller->handle_test_message();
