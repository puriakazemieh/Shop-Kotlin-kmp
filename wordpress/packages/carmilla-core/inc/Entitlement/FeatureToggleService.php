<?php
namespace Carmilla\Core\Entitlement;

use Carmilla\Core\Capabilities\CapabilityRegistry;
use Carmilla\Core\Catalog\CapabilityResolver;
use Carmilla\Core\Catalog\EntitlementSchema;
use Carmilla\Core\Rest\RestInfrastructure;

if ( ! class_exists( __NAMESPACE__ . '\FeatureToggleService', false ) ) {

/**
 * Shared Purchased Features & Builder Targets Toggle Service.
 *
 * Provides a single canonical read/write service and admin view model for both
 * Carmilla Theme (standalone) and Carmilla Bridge (standalone or co-installed).
 *
 * Enforces:
 * - Same canonical site storage (`cb_manifest_features`, `carmilla_builder_targets`).
 * - Admin capability + CSRF/Nonce validation.
 * - Cannot enable unpurchased (`NOT_ENTITLED`) or unpackaged (`MISSING_CODE`) features.
 * - Rejects invalid dependency combinations (child enabled without parent).
 * - Disabling a feature NEVER deletes site data.
 * - Four builder targets (`android`, `ios`, `pwa`, `desktop`) are managed independently.
 *
 * Part of Task P04-ENTITLEMENT-CODE-041.
 */
class FeatureToggleService {

    public const FEATURES_OPTION = 'cb_manifest_features';
    public const BUILDERS_OPTION = 'carmilla_builder_targets';
    public const AUDIT_OPTION    = 'cb_manifest_audit_log';
    public const NONCE_ACTION    = 'carmilla_save_feature_toggles';

    public const BUILDER_TARGETS = ['android', 'ios', 'pwa', 'desktop'];

    /**
     * Returns the complete panel state (features + independent builder targets).
     *
     * @param array|null $matrix_args Optional overrides for CapabilityResolver::resolve_matrix().
     * @return array<string, mixed>
     */
    public static function get_panel_state(?array $matrix_args = null): array {
        $matrix = CapabilityResolver::resolve_matrix($matrix_args ?? []);
        $manifest = EntitlementSchema::get_feature_manifest();
        $saved_toggles = function_exists('get_option') ? get_option(self::FEATURES_OPTION, []) : [];
        if (!is_array($saved_toggles)) {
            $saved_toggles = [];
        }

        $items = [];
        foreach ($matrix['features'] as $f_id => $row) {
            $meta = $manifest[$f_id] ?? [];
            $code_present  = !empty($row['code_present']);
            $entitled      = !empty($row['entitled']);
            $release_ready = !empty($row['release_ready']);
            $can_toggle    = $code_present && $entitled && $release_ready;

            $badge = 'ACTIVE';
            $upgrade_message = null;
            if (!$code_present) {
                $badge = 'NOT_PACKAGED';
                $upgrade_message = 'این قابلیت در بسته ZIP فعلی قرار ندارد و نیازمند دریافت نسخه جدید است.';
            } elseif (!$entitled) {
                $badge = 'NOT_ENTITLED';
                $upgrade_message = 'این قابلیت در لایسنس خریداری‌شده سایت وجود ندارد.';
            } elseif (!$release_ready) {
                $badge = 'RELEASE_GATE_RESTRICTED';
            } elseif ($row['status'] === CapabilityResolver::STATUS_PREREQUISITE_MISSING) {
                $badge = 'MISSING_PREREQUISITE';
            } elseif (empty($row['admin_enabled'])) {
                $badge = 'OFF';
            }

            $items[$f_id] = [
                'id'              => $f_id,
                'title'           => $meta['label_fa'] ?? $meta['title'] ?? $f_id,
                'status'          => $row['status'],
                'badge'           => $badge,
                'is_active'       => !empty($row['is_active']),
                'admin_enabled'   => array_key_exists($f_id, $saved_toggles) ? (bool) $saved_toggles[$f_id] : !empty($row['admin_enabled']),
                'entitled'        => $entitled,
                'code_present'    => $code_present,
                'can_toggle'      => $can_toggle,
                'requires'        => $meta['requires'] ?? [],
                'reason'          => $row['reason'] ?? '',
                'upgrade_message' => $upgrade_message,
            ];
        }

        $builders_state = self::get_builders_state($matrix_args);
        $host_mode = class_exists(RestInfrastructure::class) ? RestInfrastructure::get_provider_mode() : 'unknown';

        return [
            'features'  => $items,
            'builders'  => $builders_state,
            'host_mode' => $host_mode,
        ];
    }

    /**
     * Resolves independent state of the 4 builder targets (`android`, `ios`, `pwa`, `desktop`).
     *
     * @param array|null $matrix_args
     * @return array<string, array<string, mixed>>
     */
    public static function get_builders_state(?array $matrix_args = null): array {
        $claims = $matrix_args['entitlements'] ?? (function_exists('get_option') ? get_option('carmilla_license_claims', null) : null);
        $saved_builders = function_exists('get_option') ? get_option(self::BUILDERS_OPTION, []) : [];
        if (!is_array($saved_builders)) {
            $saved_builders = [];
        }

        $result = [];
        foreach (self::BUILDER_TARGETS as $target) {
            $entitled = true;
            if (is_array($claims) && isset($claims['builders']) && is_array($claims['builders'])) {
                $entitled = !empty($claims['builders'][$target]);
            }
            $enabled = array_key_exists($target, $saved_builders) ? (bool) $saved_builders[$target] : $entitled;
            $result[$target] = [
                'target'     => $target,
                'entitled'   => $entitled,
                'enabled'    => $entitled ? $enabled : false,
                'can_toggle' => $entitled,
            ];
        }
        return $result;
    }

    /**
     * Validates and saves feature toggles and independent builder target toggles.
     *
     * @param array<string, mixed> $submitted_features Map of feature_id => bool.
     * @param int                  $user_id            Acting user ID.
     * @param string|null          $nonce              CSRF nonce.
     * @param bool                 $require_nonce      Whether to enforce CSRF nonce check.
     * @param array<string, mixed> $submitted_builders Optional map of builder target => bool.
     * @param array|null           $matrix_args        Optional matrix overrides for testing.
     * @return array{ok: bool, code: string, status: int, message: string, violations?: array, panel?: array}
     */
    public static function save_toggles(
        array $submitted_features,
        int $user_id = 0,
        ?string $nonce = null,
        bool $require_nonce = true,
        array $submitted_builders = [],
        ?array $matrix_args = null
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
                'message' => 'احراز هویت الزامی است.',
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
                'message' => 'فقط مدیر مجاز می‌تواند قابلیت‌های سایت را تغییر دهد.',
            ];
        }

        // 2. CSRF / Nonce gate
        if ($require_nonce) {
            $nonce_str = is_string($nonce) ? $nonce : '';
            $valid = !empty($nonce_str) && function_exists('wp_verify_nonce') && (
                wp_verify_nonce($nonce_str, 'wp_rest') ||
                wp_verify_nonce($nonce_str, self::NONCE_ACTION)
            );
            if (!$valid) {
                return [
                    'ok'      => false,
                    'code'    => 'CSRF_INVALID',
                    'status'  => 403,
                    'message' => 'توکن امنیتی (CSRF/Nonce) نامعتبر یا غایب است.',
                ];
            }
        }

        $current_panel = self::get_panel_state($matrix_args);
        $current_features = $current_panel['features'];
        $existing_saved = function_exists('get_option') ? get_option(self::FEATURES_OPTION, []) : [];
        if (!is_array($existing_saved)) {
            $existing_saved = [];
        }

        // Build candidate feature map preserving unspecified features
        $candidate = [];
        foreach ($current_features as $f_id => $meta) {
            if (array_key_exists($f_id, $submitted_features)) {
                $want_enable = (bool) $submitted_features[$f_id];
                if ($want_enable && empty($meta['code_present'])) {
                    return [
                        'ok'      => false,
                        'code'    => 'FEATURE_NOT_PACKAGED',
                        'status'  => 403,
                        'message' => sprintf("قابلیت '%s' در بسته فعلی موجود نیست و نیازمند نسخه جدید است.", $f_id),
                    ];
                }
                if ($want_enable && empty($meta['entitled'])) {
                    return [
                        'ok'      => false,
                        'code'    => 'FEATURE_NOT_ENTITLED',
                        'status'  => 403,
                        'message' => sprintf("قابلیت '%s' خریداری نشده و قابل فعال‌سازی نیست.", $f_id),
                    ];
                }
                $candidate[$f_id] = $want_enable;
            } else {
                $candidate[$f_id] = array_key_exists($f_id, $existing_saved)
                    ? (bool) $existing_saved[$f_id]
                    : !empty($meta['admin_enabled']);
            }
        }

        // Reject unknown feature keys
        foreach (array_keys($submitted_features) as $req_key) {
            if (!isset($current_features[$req_key])) {
                return [
                    'ok'      => false,
                    'code'    => 'UNKNOWN_FEATURE',
                    'status'  => 400,
                    'message' => sprintf("شناسه قابلیت ناشناخته است: '%s'", $req_key),
                ];
            }
        }

        // 3. Dependency validation (child enabled without parent -> reject)
        $violations = CapabilityResolver::get_dependency_violations($candidate);
        if (!empty($violations)) {
            return [
                'ok'         => false,
                'code'       => 'INVALID_DEPENDENCIES',
                'status'     => 400,
                'message'    => 'ترکیب وابستگی نامعتبر است؛ قابلیت فرزند بدون پیش‌نیاز والد قابل ذخیره نیست.',
                'violations' => $violations,
            ];
        }

        // 4. Validate independent builder targets if provided
        $existing_builders = function_exists('get_option') ? get_option(self::BUILDERS_OPTION, []) : [];
        if (!is_array($existing_builders)) {
            $existing_builders = [];
        }
        $candidate_builders = $existing_builders;
        foreach ($submitted_builders as $target => $val) {
            if (!in_array($target, self::BUILDER_TARGETS, true)) {
                return [
                    'ok'      => false,
                    'code'    => 'UNKNOWN_BUILDER_TARGET',
                    'status'  => 400,
                    'message' => sprintf("هدف ساخت ناشناخته است: '%s'", $target),
                ];
            }
            $want_target = (bool) $val;
            $target_meta = $current_panel['builders'][$target] ?? ['entitled' => false];
            if ($want_target && empty($target_meta['entitled'])) {
                return [
                    'ok'      => false,
                    'code'    => 'BUILDER_NOT_ENTITLED',
                    'status'  => 403,
                    'message' => sprintf("هدف ساخت '%s' در لایسنس خریداری نشده است.", $target),
                ];
            }
            $candidate_builders[$target] = $want_target;
        }

        // 5. Persist toggles (NOTE: disabling NEVER deletes site data)
        if (function_exists('update_option')) {
            update_option(self::FEATURES_OPTION, $candidate, false);
            if (!empty($submitted_builders)) {
                update_option(self::BUILDERS_OPTION, $candidate_builders, false);
            }
        }

        if (class_exists('\Carmilla_Kernel')) {
            \Carmilla_Kernel::resolve_features();
        }

        return [
            'ok'      => true,
            'code'    => 'TOGGLES_SAVED',
            'status'  => 200,
            'message' => 'تنظیمات قابلیت‌ها با موفقیت ذخیره شد (اطلاعات موجود سایت دست‌نخورده حفظ شدند).',
            'panel'   => self::get_panel_state($matrix_args),
        ];
    }

    /**
     * Registers admin_post handler for the shared toggle panel form.
     */
    public static function register_admin_post_handler(): void {
        if (function_exists('add_action')) {
            add_action('admin_post_carmilla_save_feature_toggles', [self::class, 'handle_admin_post']);
        }
    }

    /**
     * Handles admin_post_carmilla_save_feature_toggles form submission.
     */
    public static function handle_admin_post(): void {
        $uid   = function_exists('get_current_user_id') ? get_current_user_id() : 0;
        $nonce = isset($_POST['_wpnonce']) ? sanitize_text_field(wp_unslash($_POST['_wpnonce'])) : '';
        $submitted_features = isset($_POST['cb_manifest_features']) && is_array($_POST['cb_manifest_features'])
            ? $_POST['cb_manifest_features'] : [];
        $submitted_builders = isset($_POST['carmilla_builder_targets']) && is_array($_POST['carmilla_builder_targets'])
            ? $_POST['carmilla_builder_targets'] : [];

        $panel = self::get_panel_state();
        $normalized_features = [];
        foreach ($panel['features'] as $f_id => $meta) {
            if (!empty($meta['can_toggle'])) {
                $normalized_features[$f_id] = !empty($submitted_features[$f_id]);
            }
        }
        $normalized_builders = [];
        foreach ($panel['builders'] as $target => $meta) {
            if (!empty($meta['can_toggle'])) {
                $normalized_builders[$target] = !empty($submitted_builders[$target]);
            }
        }

        $res = self::save_toggles($normalized_features, $uid, $nonce, true, $normalized_builders);

        if (!$res['ok'] && function_exists('wp_die')) {
            wp_die(esc_html($res['message']), esc_html($res['code']), ['response' => $res['status']]);
        }

        if (function_exists('wp_safe_redirect') && function_exists('admin_url')) {
            wp_safe_redirect(add_query_arg('carmilla_toggles_saved', '1', admin_url('admin.php?page=carmilla-settings')));
            exit;
        }
    }

    /**
     * Registers REST routes for GET/POST /carmilla/v1/entitlements/panel.
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

        register_rest_route('carmilla/v1', '/entitlements/panel', [
            [
                'methods'             => 'GET',
                'callback'            => [self::class, 'rest_get_panel'],
                'permission_callback' => $read_perm,
            ],
            [
                'methods'             => 'POST',
                'callback'            => [self::class, 'rest_save_panel'],
                'permission_callback' => $write_perm,
            ],
        ]);
    }

    public static function rest_get_panel($request) {
        $panel = self::get_panel_state();
        return function_exists('rest_ensure_response') ? rest_ensure_response($panel) : $panel;
    }

    public static function rest_save_panel($request) {
        $uid = function_exists('get_current_user_id') ? get_current_user_id() : 0;
        $nonce = null;
        if (is_object($request) && method_exists($request, 'get_header')) {
            $nonce = $request->get_header('x_wp_nonce') ?: $request->get_header('X-WP-Nonce');
        }

        $body = is_object($request) && method_exists($request, 'get_json_params') ? $request->get_json_params() : [];
        if (!is_array($body)) {
            $body = [];
        }

        $features = isset($body['features']) && is_array($body['features']) ? $body['features'] : [];
        $builders = isset($body['builders']) && is_array($body['builders']) ? $body['builders'] : [];

        $res = self::save_toggles($features, $uid, is_string($nonce) ? $nonce : null, true, $builders);

        if (!$res['ok'] && class_exists('\WP_Error')) {
            return new \WP_Error($res['code'], $res['message'], ['status' => $res['status'], 'violations' => $res['violations'] ?? []]);
        }

        return function_exists('rest_ensure_response') ? rest_ensure_response($res) : $res;
    }
}

}
