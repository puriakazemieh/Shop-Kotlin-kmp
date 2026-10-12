<?php
namespace Carmilla\Core\Messaging;

use Carmilla\Core\Settings\MessageSettings;

/**
 * Shared, host-independent message delivery helpers.
 *
 * The Theme can use these when Bridge is not installed; Bridge remains free to
 * provide queueing and provider-specific UI on top of the same settings.
 */
class MessageSender {
    public static function init() {
        add_action( 'phpmailer_init', [ self::class, 'configure_smtp' ] );
    }

    public static function configure_smtp( $phpmailer ) {
        $settings = get_option( MessageSettings::OPTION_NAME, [] );
        $host = $settings['smtp_host'] ?? '';
        if ( empty( $host ) ) {
            return;
        }

        $phpmailer->isSMTP();
        $phpmailer->Host       = $host;
        $phpmailer->SMTPAuth   = ! empty( $settings['smtp_user'] );
        $phpmailer->Port       = (int) ( $settings['smtp_port'] ?? 587 );
        $phpmailer->Username   = $settings['smtp_user'] ?? '';
        $phpmailer->Password   = MessageSettings::decrypt_secret( $settings['smtp_pass'] ?? '' );
        $phpmailer->SMTPSecure = $settings['smtp_secure'] ?? 'tls';

        if ( ! empty( $settings['email_from'] ) ) {
            $phpmailer->From = $settings['email_from'];
            $phpmailer->FromName = $settings['email_from_name'] ?? get_bloginfo( 'name' );
        }
    }

    public static function send_email( $to, $subject, $message ) {
        return wp_mail( $to, $subject, $message );
    }

    /**
     * Deliver through the generic HTTPS JSON endpoint configured in the shared
     * message settings. wp_safe_remote_request preserves WordPress SSRF guards.
     *
     * @return array|\WP_Error
     */
    public static function send_sms( $to, $message ) {
        $settings = get_option( MessageSettings::OPTION_NAME, [] );
        $url = $settings['sms_api_url'] ?? '';
        if ( empty( $url ) || strpos( $url, 'https://' ) !== 0 ) {
            return new \WP_Error( 'invalid_config', 'SMS provider URL must be configured and use HTTPS.' );
        }

        $key = MessageSettings::decrypt_secret( $settings['sms_api_key'] ?? '' );
        $response = wp_safe_remote_request( $url, [
            'method'      => $settings['sms_method'] ?? 'POST',
            'headers'     => [
                'Content-Type'  => 'application/json',
                'Authorization' => 'Bearer ' . $key,
            ],
            'body'        => wp_json_encode( [ 'recipient' => $to, 'text' => $message ] ),
            'timeout'     => 15,
            'redirection' => 0,
        ] );
        if ( is_wp_error( $response ) ) {
            return $response;
        }

        $code = wp_remote_retrieve_response_code( $response );
        $expected = (int) ( $settings['sms_success_code'] ?? 200 );
        if ( $code !== $expected ) {
            return new \WP_Error( 'sms_failed', 'SMS failed to send. Status: ' . $code );
        }

        $body = json_decode( wp_remote_retrieve_body( $response ), true );
        return [
            'success'    => true,
            'message_id' => $body['message_id'] ?? 'unknown',
        ];
    }
}
