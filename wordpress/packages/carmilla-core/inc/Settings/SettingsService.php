<?php
namespace Carmilla\Core\Settings;

/**
 * Shared Operational Settings Service.
 * Manages presentation and operational settings with audit logging.
 * Entitlement data is strictly read-only through this service.
 */
class SettingsService {

    public const OPTION_KEY = 'carmilla_settings';
    private const AUDIT_LOG_KEY = 'carmilla_settings_audit_log';

    private static $defaults = [
        // Presentation settings (safe for frontend)
        'site_title'         => '',
        'primary_color'      => '#6200EE',
        'secondary_color'    => '#03DAC5',
        'logo_url'           => '',
        'rtl_enabled'        => true,
        'currency_symbol'    => 'تومان',
        'language'           => 'fa',
        
        // Operational settings (admin only)
        'sms_provider'       => 'kavenegar',
        'payment_gateway'    => 'zarinpal',
        'enable_debug_log'   => false,
        'maintenance_mode'   => false,
        'max_upload_size_mb' => 10,
    ];

    private static $presentation_keys = [
        'site_title', 'primary_color', 'secondary_color',
        'logo_url', 'rtl_enabled', 'currency_symbol', 'language',
    ];

    /**
     * Get a single setting value.
     */
    public static function get(string $key, $default = null) {
        $settings = get_option(self::OPTION_KEY, []);
        if (isset($settings[$key])) {
            return $settings[$key];
        }
        return $default ?? (self::$defaults[$key] ?? null);
    }

    /**
     * Set a single setting value with capability check.
     */
    public static function set(string $key, $value): bool {
        if (!current_user_can('manage_options')) {
            return false;
        }

        $settings = get_option(self::OPTION_KEY, []);
        $old_value = $settings[$key] ?? null;
        $settings[$key] = sanitize_text_field($value);
        $result = update_option(self::OPTION_KEY, $settings);

        if ($result) {
            self::log_audit($key, $old_value, $value);
        }

        return $result;
    }

    /**
     * Get all settings merged with defaults.
     */
    public static function get_all(): array {
        $settings = get_option(self::OPTION_KEY, []);
        return array_merge(self::$defaults, $settings);
    }

    /**
     * Get only presentation (frontend-safe) settings.
     */
    public static function get_presentation_settings(): array {
        $all = self::get_all();
        return array_intersect_key($all, array_flip(self::$presentation_keys));
    }

    /**
     * Get only operational (admin-only) settings.
     */
    public static function get_operational_settings(): array {
        if (!current_user_can('manage_options')) {
            return [];
        }
        $all = self::get_all();
        return array_diff_key($all, array_flip(self::$presentation_keys));
    }

    /**
     * Validate, sanitize, and save a batch of settings.
     */
    public static function validate_and_save(array $data): bool {
        if (!current_user_can('manage_options')) {
            return false;
        }

        $settings = get_option(self::OPTION_KEY, []);
        $sanitized = [];

        foreach ($data as $key => $value) {
            // Skip any entitlement keys (read-only)
            if (strpos($key, 'entitlement_') === 0 || strpos($key, 'license_') === 0) {
                continue;
            }

            if (is_bool($value)) {
                $sanitized[$key] = (bool) $value;
            } elseif (is_numeric($value)) {
                $sanitized[$key] = intval($value);
            } else {
                $sanitized[$key] = sanitize_text_field($value);
            }
        }

        $merged = array_merge($settings, $sanitized);
        $result = update_option(self::OPTION_KEY, $merged);

        if ($result) {
            self::log_audit('batch_update', null, array_keys($sanitized));
        }

        return $result;
    }

    /**
     * Append an audit log entry.
     */
    private static function log_audit(string $key, $old_value, $new_value): void {
        $log = get_option(self::AUDIT_LOG_KEY, []);
        
        // Keep only the last 100 entries
        if (count($log) >= 100) {
            $log = array_slice($log, -99);
        }

        $log[] = [
            'key'       => $key,
            'old'       => $old_value,
            'new'       => $new_value,
            'user_id'   => get_current_user_id(),
            'timestamp' => current_time('mysql'),
        ];

        update_option(self::AUDIT_LOG_KEY, $log);
    }
}
