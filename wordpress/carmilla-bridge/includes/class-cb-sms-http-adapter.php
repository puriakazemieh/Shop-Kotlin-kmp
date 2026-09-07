<?php
if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class CB_SMS_HTTP_Adapter {
    /**
     * Sends an SMS via a generic HTTP provider.
     *
     * @param string \ Phone number
     * @param string \ Message body
     * @return array|WP_Error Response or Error
     */
    public static function send(\, \) {
        \ = get_option('carmilla_message_settings', []);
        
        \ = \['sms_api_url'] ?? '';
        \ = \['sms_api_key'] ?? '';
        \  = \['sms_method'] ?? 'POST';

        if (empty(\)) {
            return new WP_Error('missing_config', 'SMS provider URL is not configured.');
        }

        // Masking the API key in any logged context
        \ = !empty(\) ? substr(\, 0, 4) . '***' : '';

        \ = [
            'Content-Type' => 'application/json',
            'Authorization' => 'Bearer ' . \
        ];

        \ = [
            'recipient' => \,
            'text'      => \
        ];

        \ = [
            'method'  => \,
            'headers' => \,
            'body'    => json_encode(\),
            'timeout' => 15
        ];

        \ = wp_remote_request(\, \);

        if (is_wp_error(\)) {
            return \;
        }

        \ = wp_remote_retrieve_response_code(\);
        // configurable success code, default 200
        \ = intval(\['sms_success_code'] ?? 200);

        if (\ !== \) {
            return new WP_Error('sms_failed', 'SMS failed to send. Status: ' . \);
        }

        \ = json_decode(wp_remote_retrieve_body(\), true);
        \ = \['message_id'] ?? 'unknown';

        return [
            'success' => true,
            'message_id' => \,
            'masked_key_used' => \
        ];
    }
}
