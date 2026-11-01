<?php
// Run with "keyless" and "keyed" in separate PHP processes: constants cannot be unset.
$mode = $argv[1] ?? '';
if ( ! in_array( $mode, [ 'keyless', 'keyed' ], true ) ) { exit( 2 ); }
define( 'ABSPATH', __DIR__ . '/' );
if ( 'keyed' === $mode ) { define( 'SECURE_AUTH_KEY', hash( 'sha256', 'synthetic-message-secret-test' ) ); }
$GLOBALS['wp_options'] = [ 'carmilla_message_settings' => [ 'sms_api_url' => 'https://sms.example.test/send', 'sms_api_key' => 'previous-ciphertext', 'smtp_host' => 'smtp.example.test', 'smtp_user' => 'qa' ] ];
$GLOBALS['settings_errors'] = [];
$GLOBALS['requests'] = 0;
function get_option( $key, $default = false ) { return $GLOBALS['wp_options'][$key] ?? $default; }
function sanitize_text_field( $value ) { return trim( strip_tags( $value ) ); }
function add_settings_error( $setting, $code, $message ) { $GLOBALS['settings_errors'][] = [ $setting, $code, $message ]; }
function wp_safe_remote_request() { ++$GLOBALS['requests']; return [ 'response' => [ 'code' => 200 ], 'body' => '{}' ]; }
function is_wp_error( $value ) { return $value instanceof WP_Error; }
function wp_remote_retrieve_response_code( $value ) { return $value['response']['code']; }
function wp_remote_retrieve_body( $value ) { return $value['body']; }
function wp_json_encode( $value ) { return json_encode( $value ); }
class WP_Error { public function __construct( public $code, public $message ) {} }
class SyntheticMailer { public $configured = false; public function isSMTP() { $this->configured = true; } }
require_once __DIR__ . '/../inc/Settings/MessageSettings.php';
require_once __DIR__ . '/../inc/Messaging/MessageSender.php';
require_once __DIR__ . '/../../../carmilla-bridge/includes/class-cb-sms-http-adapter.php';
require_once __DIR__ . '/../../../carmilla-bridge/includes/class-cb-mail-adapter.php';
$settings = new \Carmilla\Core\Settings\MessageSettings();
$cipher = \Carmilla\Core\Settings\MessageSettings::encrypt_secret( 'synthetic-provider-key' );
$plain = \Carmilla\Core\Settings\MessageSettings::decrypt_secret( $cipher );
if ( 'keyless' === $mode ) {
    $saved = $settings->sanitize_settings( [ 'sms_api_key' => 'new-key', 'smtp_pass' => 'new-pass' ] );
    $core = \Carmilla\Core\Messaging\MessageSender::send_sms( '000', 'test' );
    $bridge = \CB_SMS_HTTP_Adapter::send( '000', 'test' );
    $core_mailer = new SyntheticMailer();
    $bridge_mailer = new SyntheticMailer();
    \Carmilla\Core\Messaging\MessageSender::configure_smtp( $core_mailer );
    \CB_Mail_Adapter::configure_smtp( $bridge_mailer );
    $ok = '' === $cipher && '' === $plain && 'previous-ciphertext' === $saved['sms_api_key']
        && '' === $saved['smtp_pass'] && 2 === count( $GLOBALS['settings_errors'] )
        && $core instanceof WP_Error && $bridge instanceof WP_Error && 0 === $GLOBALS['requests']
        && ! $core_mailer->configured && ! $bridge_mailer->configured;
} else {
    $saved = $settings->sanitize_settings( [ 'sms_api_key' => 'new-key', 'smtp_pass' => 'new-pass' ] );
    $ok = '' !== $cipher && 'synthetic-provider-key' === $plain
        && 'new-key' === \Carmilla\Core\Settings\MessageSettings::decrypt_secret( $saved['sms_api_key'] )
        && 'new-pass' === \Carmilla\Core\Settings\MessageSettings::decrypt_secret( $saved['smtp_pass'] )
        && [] === $GLOBALS['settings_errors'];
}
echo $ok ? "PASS: {$mode} secret handling\n" : "FAIL: {$mode} secret handling\n";
exit( $ok ? 0 : 1 );
