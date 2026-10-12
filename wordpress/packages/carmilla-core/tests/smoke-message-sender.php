<?php
echo "Running Core MessageSender smoke tests...\n";
if ( ! defined( 'ABSPATH' ) ) { define( 'ABSPATH', __DIR__ . '/' ); }
$GLOBALS['wp_options'] = [ 'carmilla_message_settings' => [ 'sms_api_url' => 'https://sms.example.test/send', 'sms_success_code' => 200 ] ];
function get_option( $key, $default = false ) { return $GLOBALS['wp_options'][ $key ] ?? $default; }
function add_action() {}
function get_bloginfo() { return 'Carmilla'; }
function wp_json_encode( $value ) { return json_encode( $value ); }
function is_wp_error( $value ) { return $value instanceof WP_Error; }
function wp_safe_remote_request( $url, $args ) { $GLOBALS['last_request'] = [ $url, $args ]; return [ 'response' => [ 'code' => 200 ], 'body' => '{"message_id":"smoke-1"}' ]; }
function wp_remote_retrieve_response_code( $response ) { return $response['response']['code']; }
function wp_remote_retrieve_body( $response ) { return $response['body']; }
function wp_mail( $to, $subject, $body ) { $GLOBALS['last_mail'] = [ $to, $subject, $body ]; return true; }
class WP_Error { private $message; public function __construct( $code, $message ) { $this->message = $message; } public function get_error_message() { return $this->message; } }
require_once __DIR__ . '/../inc/Settings/MessageSettings.php';
require_once __DIR__ . '/../inc/Messaging/MessageSender.php';
$GLOBALS['wp_options']['carmilla_message_settings']['sms_api_key'] = \Carmilla\Core\Settings\MessageSettings::encrypt_secret( 'safe-key' );
$sms = \Carmilla\Core\Messaging\MessageSender::send_sms( '09120000000', 'hello' );
if ( empty( $sms['success'] ) || 'https://sms.example.test/send' !== $GLOBALS['last_request'][0] || 'Bearer safe-key' !== $GLOBALS['last_request'][1]['headers']['Authorization'] ) { fwrite( STDERR, "FAIL: generic HTTPS SMS not sent safely\n" ); exit( 1 ); }
$GLOBALS['wp_options']['carmilla_message_settings']['sms_api_url'] = 'http://127.0.0.1/send';
if ( ! is_wp_error( \Carmilla\Core\Messaging\MessageSender::send_sms( '09120000000', 'hello' ) ) ) { fwrite( STDERR, "FAIL: non-HTTPS SMS endpoint accepted\n" ); exit( 1 ); }
if ( ! \Carmilla\Core\Messaging\MessageSender::send_email( 'qa@example.test', 'Subject', 'Body' ) || 'qa@example.test' !== $GLOBALS['last_mail'][0] ) { fwrite( STDERR, "FAIL: email fallback did not call wp_mail\n" ); exit( 1 ); }
echo "Core MessageSender smoke tests PASSED.\n";
