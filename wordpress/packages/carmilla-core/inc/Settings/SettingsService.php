<?php
namespace Carmilla\Core\Settings;

use Carmilla\Core\Capabilities\CapabilityRegistry;
use Carmilla\Core\Health\HealthChecker;
use Carmilla\Core\Rest\RestInfrastructure;

if ( ! class_exists( __NAMESPACE__ . '\SettingsService', false ) ) {

/**
 * Shared Operational Settings Service.
 *
 * Manages canonical site-owned operational settings with strict allowlist validation,
 * capability + CSRF/nonce enforcement, and redacted audit logging across both hosts.
 *
 * ARCHITECTURAL BOUNDARIES:
 * - Operational settings -> stored in canonical `carmilla_settings` option.
 * - Theme appearance settings -> separated (managed via Theme Mods / Customizer).
 * - Entitlement / License claims -> strictly read-only here (managed by EntitlementResolver).
 * - Feature toggles -> managed in dedicated toggle panel (P04-ENTITLEMENT-CODE-041).
 *
 * Part of Task P04-WPPLUGIN-CODE-014.
 */
class SettingsService {

    public const OPTION_KEY    = 'carmilla_settings';
    public const AUDIT_LOG_KEY = 'carmilla_settings_audit_log';
    public const NONCE_ACTION  = 'carmilla_settings_save';

    /**
     * Strict allowlist schema for operational settings.
     */
    public const OPERATIONAL_SCHEMA = [
        'sms_provider' => [
            'type'    => 'string',
            'allowed' => ['kavenegar', 'ghasedak', 'farazsms', 'melipayamak', 'mock'],
            'default' => 'kavenegar',
        ],
        'payment_gateway' => [
            'type'    => 'string',
            'allowed' => ['zarinpal', 'zibal', 'idpay', 'sandbox', 'wallet'],
            'default' => 'zarinpal',
        ],
        'enable_debug_log' => [
            'type'    => 'bool',
            'default' => false,
        ],
        'maintenance_mode' => [
            'type'    => 'bool',
            'default' => false,
        ],
        'max_upload_size_mb' => [
            'type'    => 'int',
            'min'     => 1,
            'max'     => 100,
            'default' => 10,
        ],
    ];

    private static $defaults = [
        // Presentation settings (read-only defaults; Theme Mods own live appearance)
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
        $settings = function_exists('get_option') ? get_option(self::OPTION_KEY, []) : [];
        if (is_array($settings) && array_key_exists($key, $settings)) {
            return $settings[$key];
        }
        return $default ?? (self::$defaults[$key] ?? null);
    }

    /**
     * Set a single operational setting value with capability & schema check.
     */
    public static function set(string $key, $value): bool {
        $uid = function_exists('get_current_user_id') ? get_current_user_id() : 0;
        $res = self::save_operational_settings([$key => $value], $uid, null, false);
        return !empty($res['ok']);
    }

    /**
     * Get all settings merged with defaults.
     */
    public static function get_all(): array {
        $settings = function_exists('get_option') ? get_option(self::OPTION_KEY, []) : [];
        if (!is_array($settings)) {
            $settings = [];
        }
        return array_merge(self::$defaults, $settings);
    }

    /**
     * Get only presentation (frontend-safe) settings.
     * Note: When Carmilla Theme is active, Theme Mods take precedence for appearance.
     */
    public static function get_presentation_settings(): array {
        $all = self::get_all();
        $base = array_intersect_key($all, array_flip(self::$presentation_keys));
        if (function_exists('get_theme_mod')) {
            $base['primary_color'] = get_theme_mod('carmilla_color_accent', $base['primary_color']);
            $base['site_title']    = get_theme_mod('carmilla_header_title', $base['site_title']);
        }
        return $base;
    }

    /**
     * Get only operational (admin-only) settings.
     */
    public static function get_operational_settings(): array {
        $uid = function_exists('get_current_user_id') ? get_current_user_id() : 0;
        $can_read = class_exists(CapabilityRegistry::class)
            ? CapabilityRegistry::user_can_for_domain($uid, 'admin', 'read')
            : (function_exists('current_user_can') && current_user_can('manage_options'));

        if (!$can_read) {
            return [];
        }

        $stored = function_exists('get_option') ? get_option(self::OPTION_KEY, []) : [];
        if (!is_array($stored)) {
            $stored = [];
        }

        $result = [];
        foreach (self::OPERATIONAL_SCHEMA as $key => $rules) {
            $result[$key] = array_key_exists($key, $stored) ? $stored[$key] : $rules['default'];
        }
        return $result;
    }

    /**
     * Strict validation, authorization, CSRF verification, save, and redacted audit logging.
     *
     * @param array<string, mixed> $data          Candidate operational settings.
     * @param int                  $user_id       Acting user ID.
     * @param string|null          $nonce         CSRF nonce (wp_rest or carmilla_settings_save).
     * @param bool                 $require_nonce Whether to enforce CSRF nonce verification.
     * @return array{ok: bool, code: string, status: int, message: string, settings?: array, audit?: array}
     */
    public static function save_operational_settings(
        array $data,
        int $user_id = 0,
        ?string $nonce = null,
        bool $require_nonce = true
    ): array {
        if ($user_id <= 0 && function_exists('get_current_user_id')) {
            $user_id = get_current_user_id();
        }

        // 1. Authorization gate
        if ($user_id <= 0) {
            return [
                'ok'      => false,
                'code'    => 'UNAUTHENTICATED',
                'status'  => 401,
                'message' => 'احراز هویت برای تغییر تنظیمات الزامی است.',
            ];
        }

        $can_manage = class_exists(CapabilityRegistry::class)
            ? CapabilityRegistry::user_can_for_domain($user_id, 'admin', 'manage')
            : (function_exists('user_can') && user_can($user_id, 'manage_options'));

        if (!$can_manage) {
            return [
                'ok'      => false,
                'code'    => 'FORBIDDEN',
                'status'  => 403,
                'message' => 'شما مجوز تغییر تنظیمات عملیاتی سایت را ندارید.',
            ];
        }

        // 2. CSRF / Nonce gate
        if ($require_nonce) {
            $nonce_str = is_string($nonce) ? $nonce : '';
            $valid_nonce = !empty($nonce_str) && function_exists('wp_verify_nonce') && (
                wp_verify_nonce($nonce_str, 'wp_rest') ||
                wp_verify_nonce($nonce_str, self::NONCE_ACTION)
            );
            if (!$valid_nonce) {
                return [
                    'ok'      => false,
                    'code'    => 'CSRF_INVALID',
                    'status'  => 403,
                    'message' => 'توکن امنیتی (CSRF/Nonce) نامعتبر یا غایب است.',
                ];
            }
        }

        if (empty($data)) {
            return [
                'ok'      => false,
                'code'    => 'INVALID_PAYLOAD',
                'status'  => 400,
                'message' => 'هیچ کلید تنظیماتی ارسال نشده است.',
            ];
        }

        // 3. Strict Key Classification & Schema Validation
        $sanitized = [];
        foreach ($data as $key => $value) {
            $key_str = (string) $key;

            // Reject any attempt to write or forge entitlement/license state
            if (self::is_entitlement_key($key_str)) {
                return [
                    'ok'      => false,
                    'code'    => 'ENTITLEMENT_WRITE_FORBIDDEN',
                    'status'  => 403,
                    'message' => sprintf("نوشتن یا تغییر مجوز خرید ('%s') از طریق تنظیمات مجاز نیست.", $key_str),
                ];
            }

            // Reject theme presentation keys from operational settings write path
            if (in_array($key_str, self::$presentation_keys, true)) {
                return [
                    'ok'      => false,
                    'code'    => 'PRESENTATION_KEY_SEPARATED',
                    'status'  => 400,
                    'message' => sprintf("تنظیم ظاهر پوسته ('%s') از تنظیمات عملیاتی جدا است.", $key_str),
                ];
            }

            // Reject unknown options
            if (!isset(self::OPERATIONAL_SCHEMA[$key_str])) {
                return [
                    'ok'      => false,
                    'code'    => 'UNKNOWN_OPTION',
                    'status'  => 400,
                    'message' => sprintf("کلید تنظیمات ناشناخته ('%s') مجاز نیست.", $key_str),
                ];
            }

            $rules = self::OPERATIONAL_SCHEMA[$key_str];
            $validated = self::validate_value($value, $rules);
            if (!$validated['valid']) {
                return [
                    'ok'      => false,
                    'code'    => 'INVALID_OPTION_VALUE',
                    'status'  => 400,
                    'message' => sprintf("مقدار نامعتبر برای '%s': %s", $key_str, $validated['error']),
                ];
            }

            $sanitized[$key_str] = $validated['value'];
        }

        // 4. Persist to canonical storage
        $existing = function_exists('get_option') ? get_option(self::OPTION_KEY, []) : [];
        if (!is_array($existing)) {
            $existing = [];
        }

        $old_snapshot = [];
        foreach (array_keys($sanitized) as $k) {
            $old_snapshot[$k] = $existing[$k] ?? (self::OPERATIONAL_SCHEMA[$k]['default'] ?? null);
        }

        $merged = array_merge($existing, $sanitized);
        if (function_exists('update_option')) {
            update_option(self::OPTION_KEY, $merged);
        }

        $audit_entry = self::log_audit('operational_update', $old_snapshot, $sanitized, $user_id);

        return [
            'ok'       => true,
            'code'     => 'SETTINGS_SAVED',
            'status'   => 200,
            'message'  => 'تنظیمات عملیاتی سایت با موفقیت ذخیره شد.',
            'settings' => self::get_operational_settings(),
            'audit'    => $audit_entry,
        ];
    }

    /**
     * Backward-compatible batch save delegating to strict validator.
     */
    public static function validate_and_save(array $data): bool {
        $uid = function_exists('get_current_user_id') ? get_current_user_id() : 0;
        $res = self::save_operational_settings($data, $uid, null, false);
        return !empty($res['ok']);
    }

    /**
     * Returns redacted audit log entries.
     *
     * @return array<int, array<string, mixed>>
     */
    public static function get_audit_log(): array {
        $log = function_exists('get_option') ? get_option(self::AUDIT_LOG_KEY, []) : [];
        if (!is_array($log)) {
            return [];
        }
        return class_exists(HealthChecker::class) ? HealthChecker::redact_payload($log) : $log;
    }

    /**
     * Registers shared REST API endpoints for site operational settings.
     */
    public static function register_rest_routes(): void {
        if (!function_exists('register_rest_route')) {
            return;
        }

        $read_perm = class_exists(CapabilityRegistry::class)
            ? CapabilityRegistry::get_permission_callback('admin', 'read', 'theme.base')
            : function () {
                return function_exists('current_user_can') && current_user_can('manage_options');
            };

        $write_perm = class_exists(CapabilityRegistry::class)
            ? CapabilityRegistry::get_permission_callback('admin', 'manage', 'theme.base')
            : function () {
                return function_exists('current_user_can') && current_user_can('manage_options');
            };

        register_rest_route('carmilla/v1', '/settings', [
            [
                'methods'             => 'GET',
                'callback'            => [self::class, 'rest_get_settings'],
                'permission_callback' => $read_perm,
            ],
            [
                'methods'             => ['POST', 'PUT', 'PATCH'],
                'callback'            => [self::class, 'rest_save_settings'],
                'permission_callback' => $write_perm,
            ],
        ]);

        register_rest_route('carmilla/v1', '/settings/audit', [
            [
                'methods'             => 'GET',
                'callback'            => [self::class, 'rest_get_audit'],
                'permission_callback' => $read_perm,
            ],
        ]);
    }

    /**
     * REST GET /carmilla/v1/settings handler.
     */
    public static function rest_get_settings($request) {
        $host_mode = class_exists(RestInfrastructure::class) ? RestInfrastructure::get_provider_mode() : 'unknown';
        $payload = [
            'operational'           => self::get_operational_settings(),
            'presentation_readonly' => self::get_presentation_settings(),
            'entitlement_readonly'  => true,
            'host_mode'             => $host_mode,
            'storage_key'           => self::OPTION_KEY,
        ];
        return function_exists('rest_ensure_response') ? rest_ensure_response($payload) : $payload;
    }

    /**
     * REST POST/PUT/PATCH /carmilla/v1/settings handler.
     */
    public static function rest_save_settings($request) {
        $uid = function_exists('get_current_user_id') ? get_current_user_id() : 0;
        $nonce = null;
        if (is_object($request) && method_exists($request, 'get_header')) {
            $nonce = $request->get_header('x_wp_nonce') ?: $request->get_header('X-WP-Nonce');
        }
        if (empty($nonce) && is_object($request) && method_exists($request, 'get_param')) {
            $nonce = $request->get_param('_wpnonce');
        }

        $body = [];
        if (is_object($request) && method_exists($request, 'get_json_params')) {
            $json = $request->get_json_params();
            if (is_array($json) && !empty($json)) {
                $body = $json;
            }
        }
        if (empty($body) && is_object($request) && method_exists($request, 'get_params')) {
            $body = $request->get_params();
            unset($body['_wpnonce'], $body['rest_route']);
        }

        $res = self::save_operational_settings(is_array($body) ? $body : [], $uid, is_string($nonce) ? $nonce : null, true);

        if (!$res['ok']) {
            if (class_exists('\WP_Error')) {
                return new \WP_Error($res['code'], $res['message'], ['status' => $res['status']]);
            }
            return $res;
        }

        return function_exists('rest_ensure_response') ? rest_ensure_response($res) : $res;
    }

    /**
     * REST GET /carmilla/v1/settings/audit handler.
     */
    public static function rest_get_audit($request) {
        $payload = [
            'audit'            => self::get_audit_log(),
            'secrets_redacted' => true,
        ];
        return function_exists('rest_ensure_response') ? rest_ensure_response($payload) : $payload;
    }

    /**
     * Checks if a key is an entitlement/license/feature-toggle key that cannot be forged here.
     */
    private static function is_entitlement_key(string $key): bool {
        $forbidden_exact = [
            'skus', 'claims', 'entitled', 'effective_features',
            'cb_manifest_features', 'carmilla_license_claims',
            'carmilla_package_manifest', 'carmilla_release_catalog',
        ];
        if (in_array($key, $forbidden_exact, true)) {
            return true;
        }
        foreach (['entitlement_', 'license_', 'sku_', 'carmilla_license'] as $prefix) {
            if (strpos($key, $prefix) === 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates a single value against its schema rule.
     *
     * @param mixed $value
     * @param array $rules
     * @return array{valid: bool, value?: mixed, error?: string}
     */
    private static function validate_value($value, array $rules): array {
        $type = $rules['type'] ?? 'string';

        if ($type === 'bool') {
            if (is_bool($value)) {
                return ['valid' => true, 'value' => $value];
            }
            if ($value === 0 || $value === 1 || $value === '0' || $value === '1' || $value === 'true' || $value === 'false') {
                return ['valid' => true, 'value' => filter_var($value, FILTER_VALIDATE_BOOLEAN)];
            }
            return ['valid' => false, 'error' => 'باید مقدار منطقی (boolean) باشد.'];
        }

        if ($type === 'int') {
            if (!is_numeric($value)) {
                return ['valid' => false, 'error' => 'باید عدد صحیح باشد.'];
            }
            $int_val = (int) $value;
            if (isset($rules['min']) && $int_val < $rules['min']) {
                return ['valid' => false, 'error' => sprintf('حداقل مقدار مجاز %d است.', $rules['min'])];
            }
            if (isset($rules['max']) && $int_val > $rules['max']) {
                return ['valid' => false, 'error' => sprintf('حداکثر مقدار مجاز %d است.', $rules['max'])];
            }
            return ['valid' => true, 'value' => $int_val];
        }

        $str_val = function_exists('sanitize_text_field') ? sanitize_text_field((string) $value) : trim((string) $value);
        if (isset($rules['allowed']) && !in_array($str_val, $rules['allowed'], true)) {
            return ['valid' => false, 'error' => 'مقدار انتخاب‌شده در فهرست مجاز نیست.'];
        }
        return ['valid' => true, 'value' => $str_val];
    }

    /**
     * Append a redacted audit log entry.
     */
    private static function log_audit(string $key, $old_value, $new_value, int $user_id): array {
        $log = function_exists('get_option') ? get_option(self::AUDIT_LOG_KEY, []) : [];
        if (!is_array($log)) {
            $log = [];
        }

        if (count($log) >= 100) {
            $log = array_slice($log, -99);
        }

        $host_mode = class_exists(RestInfrastructure::class) ? RestInfrastructure::get_provider_mode() : 'unknown';

        $entry = [
            'key'       => $key,
            'old'       => $old_value,
            'new'       => $new_value,
            'user_id'   => $user_id,
            'host_mode' => $host_mode,
            'timestamp' => function_exists('current_time') ? current_time('mysql') : gmdate('c'),
        ];

        if (class_exists(HealthChecker::class)) {
            $entry = HealthChecker::redact_payload($entry);
        }

        $log[] = $entry;

        if (function_exists('update_option')) {
            update_option(self::AUDIT_LOG_KEY, $log);
        }

        return $entry;
    }
}

}
