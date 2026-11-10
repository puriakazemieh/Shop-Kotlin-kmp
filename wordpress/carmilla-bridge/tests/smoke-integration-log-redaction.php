<?php
/** Synthetic integration-admin success and provider-error paths. No real delivery. */
define( 'ABSPATH', __DIR__ . '/' );
$mode = $argv[1] ?? '';
if ( ! in_array( $mode, array( 'success', 'failure' ), true ) ) { exit( 2 ); }
$log = tempnam( sys_get_temp_dir(), 'cb-log-' );
ini_set( 'log_errors', '1' );
ini_set( 'error_log', $log );
class WP_Error {
    public function get_error_message(): string { return 'provider-secret synthetic-body 09123456789'; }
}
class CB_SMS_HTTP_Adapter {
    public static function send( $to, $body ) { return new WP_Error(); }
}
class CB_Mail_Adapter {
    public static $sent = false;
    public static function send( $to, $subject, $body ) { self::$sent = $to === '09123456789' && $body === 'synthetic-body'; return true; }
}
function current_user_can( $cap ) { return true; }
function check_admin_referer( $action, $name ) { return true; }
function sanitize_text_field( $value ) { return $value; }
function sanitize_textarea_field( $value ) { return $value; }
function wp_unslash( $value ) { return $value; }
function is_wp_error( $value ) { return $value instanceof WP_Error; }
function __( $text, $domain = null ) { return $text; }
function wp_die( $message ) { throw new RuntimeException( $message ); }
function admin_url( $path ) { return '/wp-admin/' . $path; }
function add_query_arg( $args, $url ) { return $url . '?' . http_build_query( $args ); }
$redirect = '';
function wp_safe_redirect( $location ) { $GLOBALS['redirect'] = $location; return true; }
require dirname( __DIR__ ) . '/includes/class-cb-integrations-controller.php';
$_POST = array( 'test_type' => $mode === 'success' ? 'email' : 'sms', 'test_recipient' => '09123456789', 'test_body' => 'synthetic-body' );
$die = '';
register_shutdown_function( static function () use ( $mode, $log, &$die ): void {
    $logged = file_get_contents( $log );
    unlink( $log );
    $sensitive = '09123456789|synthetic-body|provider-secret';
    $clean = ! preg_match( "/$sensitive/", $logged . $die . $GLOBALS['redirect'] );
    $ok = $clean && ( $mode === 'success'
        ? ( CB_Mail_Adapter::$sent && str_contains( $GLOBALS['redirect'], 'status=success' ) && $logged === '' && $die === '' )
        : ( $GLOBALS['redirect'] === '' && $die === 'Failed to send test message.' && str_contains( $logged, 'Carmilla test message failed.' ) ) );
    echo $ok ? "PASS $mode\n" : "FAIL $mode\n";
    if ( ! $ok ) { exit( 1 ); }
} );
try { ( new CB_Integrations_Controller() )->handle_test_message(); }
catch ( RuntimeException $e ) { $die = $e->getMessage(); }
