<?php
namespace Carmilla\Core\Settings;

/**
 * MessageSettings defines the schema and handles options for messaging providers.
 * Settings are stored in the shared 'wp_options' so that both Theme and Bridge can access them,
 * persisting even if one host is deactivated.
 */
class MessageSettings {
    const OPTION_NAME = 'carmilla_message_settings';

    /**
     * Initializes the hooks for registering settings and capabilities.
     */
    public function init() {
        add_action('admin_init', [$this, 'register_settings']);
    }

    /**
     * Registers the messaging settings schema.
     */
    public function register_settings() {
        register_setting('carmilla_options', self::OPTION_NAME, [
            'type' => 'array',
            'description' => 'Carmilla Messaging Settings',
            'sanitize_callback' => [$this, 'sanitize_settings'],
            'show_in_rest' => false, // Ensure secrets are not exposed to the REST API payload!
        ]);
    }

    /**
     * Sanitizes incoming settings.
     */
    public function sanitize_settings($input) {
        $sanitized = [];
        if (isset($input['sms_provider'])) {
            $sanitized['sms_provider'] = sanitize_text_field($input['sms_provider']);
        }
        if (isset($input['sms_api_key'])) {
            // Keep the previous key if not updated
            if (empty($input['sms_api_key'])) {
                $old_settings = get_option(self::OPTION_NAME, []);
                $sanitized['sms_api_key'] = $old_settings['sms_api_key'] ?? '';
            } else {
                $sanitized['sms_api_key'] = sanitize_text_field($input['sms_api_key']);
            }
        }
        if (isset($input['email_provider'])) {
            $sanitized['email_provider'] = sanitize_text_field($input['email_provider']);
        }
        return $sanitized;
    }
}
