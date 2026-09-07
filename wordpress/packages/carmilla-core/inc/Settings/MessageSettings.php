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
     * Helper to encrypt secrets before storing in DB.
     * Uses CARMILLA_SECRET_KEY if defined in wp-config.php, otherwise falls back to WP_SALT.
     */
    public static function encrypt_secret($plain_text) {
        if (empty($plain_text)) return '';
        $key = defined('CARMILLA_SECRET_KEY') ? CARMILLA_SECRET_KEY : (defined('SECURE_AUTH_KEY') ? SECURE_AUTH_KEY : 'default-fallback-key');
        $iv = openssl_random_pseudo_bytes(openssl_cipher_iv_length('aes-256-cbc'));
        $encrypted = openssl_encrypt($plain_text, 'aes-256-cbc', $key, 0, $iv);
        return base64_encode($encrypted . '::' . $iv);
    }

    /**
     * Helper to decrypt secrets when using them.
     */
    public static function decrypt_secret($encrypted_text) {
        if (empty($encrypted_text)) return '';
        $key = defined('CARMILLA_SECRET_KEY') ? CARMILLA_SECRET_KEY : (defined('SECURE_AUTH_KEY') ? SECURE_AUTH_KEY : 'default-fallback-key');
        $parts = explode('::', base64_decode($encrypted_text), 2);
        if (count($parts) !== 2) return '';
        return openssl_decrypt($parts[0], 'aes-256-cbc', $key, 0, $parts[1]);
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
                $sanitized['sms_api_key'] = self::encrypt_secret(sanitize_text_field($input['sms_api_key']));
            }
        }
        if (isset($input['email_provider'])) {
            $sanitized['email_provider'] = sanitize_text_field($input['email_provider']);
        }
        if (isset($input['smtp_pass'])) {
            if (empty($input['smtp_pass'])) {
                $old_settings = get_option(self::OPTION_NAME, []);
                $sanitized['smtp_pass'] = $old_settings['smtp_pass'] ?? '';
            } else {
                $sanitized['smtp_pass'] = self::encrypt_secret(sanitize_text_field($input['smtp_pass']));
            }
        }
        
        // Pass through other settings
        foreach (['sms_api_url', 'sms_method', 'sms_success_code', 'smtp_host', 'smtp_port', 'smtp_user', 'smtp_secure', 'email_from', 'email_from_name'] as $field) {
            if (isset($input[$field])) {
                $sanitized[$field] = sanitize_text_field($input[$field]);
            }
        }
        
        return $sanitized;
    }
}
