<?php
if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class CB_SMS_HTTP_Adapter {
    /**
     * Sends an SMS via a generic HTTP provider.
     *
     * @param string $to Phone number
     * @param string $message Message body
     * @return array|WP_Error Response or Error
     */
    public static function send($to, $message) {
        $settings = get_option('carmilla_message_settings', []);
        
        $api_url = $settings['sms_api_url'] ?? '';
        $encrypted_key = $settings['sms_api_key'] ?? '';
        $api_key = \Carmilla\Core\Settings\MessageSettings::decrypt_secret($encrypted_key);
        $method  = $settings['sms_method'] ?? 'POST';

        if (empty($api_url)) {
            return new WP_Error('missing_config', 'SMS provider URL is not configured.');
        }

        // Masking the API key in any logged context
        $masked_key = !empty($api_key) ? substr($api_key, 0, 4) . '***' : '';

        $headers = [
            'Content-Type' => 'application/json',
            'Authorization' => 'Bearer ' . $api_key
        ];

        $body = [
            'recipient' => $to,
            'text'      => $message
        ];

        $args = [
            'method'  => $method,
            'headers' => $headers,
            'body'    => json_encode($body),
            'timeout' => 15
        ];

        $response = wp_remote_request($api_url, $args);

        if (is_wp_error($response)) {
            return $response;
        }

        $status_code = wp_remote_retrieve_response_code($response);
        // configurable success code, default 200
        $success_code = intval($settings['sms_success_code'] ?? 200);

        if ($status_code !== $success_code) {
            return new WP_Error('sms_failed', 'SMS failed to send. Status: ' . $status_code);
        }

        $body_response = json_decode(wp_remote_retrieve_body($response), true);
        $message_id = $body_response['message_id'] ?? 'unknown';

        return [
            'success' => true,
            'message_id' => $message_id,
            'masked_key_used' => $masked_key
        ];
    }
}
