<?php
namespace Carmilla\Core\Health;

use Carmilla\Core\Adapter\WooCommerceAdapter;
use Carmilla\Core\Capabilities\CapabilityRegistry;

if ( ! class_exists( __NAMESPACE__ . '\HealthChecker', false ) ) {

/**
 * System Health and Preflight Checker
 *
 * Runs non-destructive preflight diagnostics across WordPress environment:
 * HTTPS, WooCommerce (only when Commerce SKU is purchased), Permalinks, REST API,
 * WP-Cron, and Kernel compatibility.
 *
 * Strictly separates shared site prerequisites from SKU-dependent prerequisites,
 * provides clear remediation paths, and redacts all secrets/credentials/PII.
 *
 * Part of Task P04-WPPLUGIN-CODE-011.
 */
class HealthChecker {

    /**
     * Minimum required Carmilla Kernel version.
     *
     * @var string
     */
    public const MINIMUM_KERNEL_VERSION = '1.0.0';

    /**
     * Regex matching sensitive keys that must always be redacted from diagnostics.
     */
    private const SENSITIVE_KEY_PATTERN = '/(secret|token|password|passwd|pwd|api_key|apikey|private_key|salt|auth_key|nonce|cookie|jwt|authorization|email|phone|mobile|address|ssn|national_id)/i';

    /**
     * Executes all system health checks.
     *
     * @param array<string> $purchased_skus Array of purchased SKU IDs (e.g. ['sku_commerce']).
     * @param array<string, mixed> $overrides Optional environment overrides for deterministic testing.
     * @return array<string, array{id: string, name: string, status: string, scope: string, message: string, remediation: ?string}>
     */
    public static function run_all_checks(array $purchased_skus = [], array $overrides = []): array {
        $is_commerce_purchased = in_array('sku_commerce', $purchased_skus, true);

        return [
            'https'          => self::check_https($overrides),
            'woocommerce'    => self::check_woocommerce($is_commerce_purchased, $overrides),
            'permalinks'     => self::check_permalinks($overrides),
            'rest_api'       => self::check_rest_api($overrides),
            'wp_cron'        => self::check_wp_cron($overrides),
            'kernel_version' => self::check_kernel_version(self::MINIMUM_KERNEL_VERSION, $overrides),
        ];
    }

    /**
     * Executes full SKU-aware onboarding preflight for a specific host context.
     *
     * @param string               $host_context   'theme' | 'plugin' | 'co-install' | 'auto'
     * @param array<string>        $purchased_skus Optional explicit SKU list. Defaults to stored claims or host base SKU.
     * @param array<string, mixed> $overrides      Optional environment overrides for testing.
     * @return array<string, mixed>
     */
    public static function run_preflight(string $host_context = 'auto', array $purchased_skus = [], array $overrides = []): array {
        $resolved_host = self::resolve_host_context($host_context);
        $skus = self::resolve_purchased_skus($purchased_skus, $resolved_host);
        $checks = self::run_all_checks($skus, $overrides);

        $blocking_issues = [];
        $warnings = [];

        foreach ($checks as $key => $check) {
            if ($check['status'] === 'fail') {
                $blocking_issues[] = [
                    'id'          => $check['id'],
                    'name'        => $check['name'],
                    'scope'       => $check['scope'],
                    'message'     => $check['message'],
                    'remediation' => $check['remediation'],
                ];
            } elseif ($check['status'] === 'warning') {
                $warnings[] = [
                    'id'          => $check['id'],
                    'name'        => $check['name'],
                    'scope'       => $check['scope'],
                    'message'     => $check['message'],
                    'remediation' => $check['remediation'],
                ];
            }
        }

        $result = [
            'ready'            => empty($blocking_issues),
            'host_context'     => $resolved_host,
            'purchased_skus'   => $skus,
            'blocking_issues'  => $blocking_issues,
            'warnings'         => $warnings,
            'checks'           => $checks,
            'checked_at'       => gmdate('c'),
            'secrets_redacted' => true,
        ];

        return self::redact_payload($result);
    }

    /**
     * Checks if the site is serving traffic over HTTPS (SSL).
     *
     * @param array<string, mixed> $overrides
     * @return array
     */
    public static function check_https(array $overrides = []): array {
        $is_ssl = array_key_exists('is_ssl', $overrides)
            ? (bool) $overrides['is_ssl']
            : (function_exists('is_ssl') && is_ssl());

        if ($is_ssl) {
            return [
                'id'          => 'https',
                'name'        => 'HTTPS Transport Security',
                'status'      => 'pass',
                'scope'       => 'shared',
                'message'     => 'Site is running over secure HTTPS connection.',
                'remediation' => null,
            ];
        }

        return [
            'id'          => 'https',
            'name'        => 'HTTPS Transport Security',
            'status'      => 'fail',
            'scope'       => 'shared',
            'message'     => 'Site is running over unencrypted HTTP. REST API and payment flows require HTTPS.',
            'remediation' => 'Install a valid SSL certificate and configure your WordPress site URL to use https:// in Settings > General.',
        ];
    }

    /**
     * Validates WooCommerce availability.
     * Only fails if Commerce SKU is purchased but WooCommerce is missing.
     *
     * @param bool                 $is_commerce_sku_purchased Whether the site license includes Commerce SKU.
     * @param array<string, mixed> $overrides
     * @return array
     */
    public static function check_woocommerce(bool $is_commerce_sku_purchased = false, array $overrides = []): array {
        $is_active = array_key_exists('woocommerce_active', $overrides)
            ? (bool) $overrides['woocommerce_active']
            : class_exists('WooCommerce');

        if ($is_active) {
            return [
                'id'          => 'woocommerce',
                'name'        => 'WooCommerce Core Engine',
                'status'      => 'pass',
                'scope'       => 'sku:sku_commerce',
                'message'     => 'WooCommerce is installed and active.',
                'remediation' => null,
            ];
        }

        if ($is_commerce_sku_purchased) {
            return [
                'id'          => 'woocommerce',
                'name'        => 'WooCommerce Core Engine',
                'status'      => 'fail',
                'scope'       => 'sku:sku_commerce',
                'message'     => 'Commerce SKU is licensed for this site, but WooCommerce is inactive or not installed.',
                'remediation' => 'Install and activate the official WooCommerce plugin to enable store features.',
            ];
        }

        return [
            'id'          => 'woocommerce',
            'name'        => 'WooCommerce Core Engine',
            'status'      => 'info',
            'scope'       => 'sku:sku_commerce',
            'message'     => 'WooCommerce is not active, but Commerce SKU is not purchased. Base UI/API functions independently.',
            'remediation' => null,
        ];
    }

    /**
     * Validates that pretty permalinks are enabled (required for modern REST endpoints).
     *
     * @param array<string, mixed> $overrides
     * @return array
     */
    public static function check_permalinks(array $overrides = []): array {
        $structure = array_key_exists('permalink_structure', $overrides)
            ? (string) $overrides['permalink_structure']
            : (function_exists('get_option') ? (string) get_option('permalink_structure') : '');

        if (!empty($structure)) {
            return [
                'id'          => 'permalinks',
                'name'        => 'Pretty Permalinks',
                'status'      => 'pass',
                'scope'       => 'shared',
                'message'     => 'Pretty permalinks are active.',
                'remediation' => null,
            ];
        }

        return [
            'id'          => 'permalinks',
            'name'        => 'Pretty Permalinks',
            'status'      => 'fail',
            'scope'       => 'shared',
            'message'     => 'Plain permalinks are active. Pretty permalinks are required for standard REST API routing.',
            'remediation' => 'Navigate to WordPress Settings > Permalinks and select Post name (/%postname%/) or another structure.',
        ];
    }

    /**
     * Validates that WordPress REST API functions are accessible.
     *
     * @param array<string, mixed> $overrides
     * @return array
     */
    public static function check_rest_api(array $overrides = []): array {
        $has_rest = array_key_exists('rest_available', $overrides)
            ? (bool) $overrides['rest_available']
            : function_exists('rest_url');

        if ($has_rest) {
            return [
                'id'          => 'rest_api',
                'name'        => 'WordPress REST API',
                'status'      => 'pass',
                'scope'       => 'shared',
                'message'     => 'WordPress REST API infrastructure is loaded.',
                'remediation' => null,
            ];
        }

        return [
            'id'          => 'rest_api',
            'name'        => 'WordPress REST API',
            'status'      => 'fail',
            'scope'       => 'shared',
            'message'     => 'WordPress REST API core functions could not be verified.',
            'remediation' => 'Verify WordPress core integrity and ensure REST API has not been disabled by a firewall plugin.',
        ];
    }

    /**
     * Validates that WP-Cron is not disabled without an external runner.
     *
     * @param array<string, mixed> $overrides
     * @return array
     */
    public static function check_wp_cron(array $overrides = []): array {
        $disabled = array_key_exists('wp_cron_disabled', $overrides)
            ? (bool) $overrides['wp_cron_disabled']
            : (defined('DISABLE_WP_CRON') && DISABLE_WP_CRON);

        if ($disabled) {
            return [
                'id'          => 'wp_cron',
                'name'        => 'WordPress Cron Scheduler',
                'status'      => 'warning',
                'scope'       => 'shared',
                'message'     => 'DISABLE_WP_CRON constant is enabled. Automated tasks rely on external system crontab.',
                'remediation' => 'Verify your hosting server has a crontab entry triggering wp-cron.php (e.g. */10 * * * * curl -s <site_url>/wp-cron.php).',
            ];
        }

        return [
            'id'          => 'wp_cron',
            'name'        => 'WordPress Cron Scheduler',
            'status'      => 'pass',
            'scope'       => 'shared',
            'message'     => 'Default WordPress pseudo-cron scheduler is active.',
            'remediation' => null,
        ];
    }

    /**
     * Validates Carmilla Kernel version compatibility.
     *
     * @param string               $minimum_version Minimum required version string.
     * @param array<string, mixed> $overrides
     * @return array
     */
    public static function check_kernel_version(string $minimum_version = self::MINIMUM_KERNEL_VERSION, array $overrides = []): array {
        $current_version = array_key_exists('kernel_version', $overrides)
            ? (string) $overrides['kernel_version']
            : ((class_exists('\Carmilla_Kernel') && defined('\Carmilla_Kernel::VERSION')) ? \Carmilla_Kernel::VERSION : '1.0.0');

        if (version_compare($current_version, $minimum_version, '>=')) {
            return [
                'id'          => 'kernel_version',
                'name'        => 'Carmilla Shared Kernel',
                'status'      => 'pass',
                'scope'       => 'shared',
                'message'     => sprintf('Kernel version %s meets requirement (>= %s).', $current_version, $minimum_version),
                'remediation' => null,
            ];
        }

        return [
            'id'          => 'kernel_version',
            'name'        => 'Carmilla Shared Kernel',
            'status'      => 'fail',
            'scope'       => 'shared',
            'message'     => sprintf('Kernel version %s is below required %s.', $current_version, $minimum_version),
            'remediation' => 'Update the carmilla-core package to the latest release.',
        ];
    }

    /**
     * Returns redacted diagnostic information for troubleshooting and support.
     * STRICTLY redacts secrets, database passwords, salts, private keys, and PII.
     *
     * @param array<string>        $purchased_skus Array of purchased SKU IDs.
     * @param array<string, mixed> $overrides      Optional environment overrides.
     * @return array Structured diagnostic metadata.
     */
    public static function get_diagnostics(array $purchased_skus = [], array $overrides = []): array {
        global $wp_version;

        $boot_contexts = class_exists('\Carmilla_Kernel')
            ? \Carmilla_Kernel::get_boot_contexts()
            : [];

        $hpos_enabled = class_exists(WooCommerceAdapter::class)
            ? WooCommerceAdapter::is_hpos_enabled()
            : false;

        $preflight = self::run_preflight('auto', $purchased_skus, $overrides);

        $payload = [
            'environment' => [
                'php_version'         => PHP_VERSION,
                'wp_version'          => $wp_version ?? 'unknown',
                'server_software'     => $_SERVER['SERVER_SOFTWARE'] ?? 'CLI/Unknown',
                'is_ssl'              => array_key_exists('is_ssl', $overrides) ? (bool) $overrides['is_ssl'] : (function_exists('is_ssl') ? is_ssl() : false),
                'woocommerce_active'  => array_key_exists('woocommerce_active', $overrides) ? (bool) $overrides['woocommerce_active'] : class_exists('WooCommerce'),
                'woocommerce_version' => defined('WC_VERSION') ? WC_VERSION : null,
                'hpos_enabled'        => $hpos_enabled,
            ],
            'kernel' => [
                'version'       => (class_exists('\Carmilla_Kernel') && defined('\Carmilla_Kernel::VERSION')) ? \Carmilla_Kernel::VERSION : '1.0.0',
                'boot_contexts' => $boot_contexts,
            ],
            'preflight' => [
                'ready'           => $preflight['ready'],
                'host_context'    => $preflight['host_context'],
                'purchased_skus'  => $preflight['purchased_skus'],
                'blocking_issues' => $preflight['blocking_issues'],
                'warnings'        => $preflight['warnings'],
            ],
            'checks'           => $preflight['checks'],
            'secrets_redacted' => true,
        ];

        return self::redact_payload($payload);
    }

    /**
     * Recursively redacts any sensitive keys (secrets, tokens, passwords, PII) from arrays.
     *
     * @param array<mixed> $data
     * @return array<mixed>
     */
    public static function redact_payload(array $data): array {
        $sanitized = [];
        foreach ($data as $key => $value) {
            if (is_string($key) && $key !== 'secrets_redacted' && preg_match(self::SENSITIVE_KEY_PATTERN, $key)) {
                $sanitized[$key] = '[REDACTED]';
                continue;
            }
            if (is_array($value)) {
                $sanitized[$key] = self::redact_payload($value);
            } else {
                $sanitized[$key] = $value;
            }
        }
        return $sanitized;
    }

    /**
     * Registers protected REST preflight endpoints for admin diagnostics and re-checking.
     *
     * @return void
     */
    public static function register_rest_routes(): void {
        if (!function_exists('register_rest_route')) {
            return;
        }

        $perm_cb = class_exists(CapabilityRegistry::class)
            ? CapabilityRegistry::get_permission_callback('admin', 'read', 'theme.base')
            : function () {
                return function_exists('current_user_can') && current_user_can('manage_options');
            };

        register_rest_route('carmilla/v1', '/preflight', [
            [
                'methods'             => 'GET',
                'callback'            => [self::class, 'rest_get_preflight'],
                'permission_callback' => $perm_cb,
            ],
        ]);

        register_rest_route('carmilla/v1', '/preflight/recheck', [
            [
                'methods'             => 'POST',
                'callback'            => [self::class, 'rest_get_preflight'],
                'permission_callback' => $perm_cb,
            ],
        ]);
    }

    /**
     * REST handler for GET /carmilla/v1/preflight and POST /carmilla/v1/preflight/recheck.
     *
     * @param mixed $request
     * @return \WP_REST_Response|array
     */
    public static function rest_get_preflight($request) {
        $host = 'auto';
        $skus = [];

        if (is_object($request) && method_exists($request, 'get_param')) {
            $host_param = $request->get_param('host');
            if (is_string($host_param) && in_array($host_param, ['theme', 'plugin', 'co-install', 'auto'], true)) {
                $host = $host_param;
            }
            $sku_param = $request->get_param('skus');
            if (is_array($sku_param)) {
                $skus = array_values(array_filter(array_map('strval', $sku_param)));
            }
        }

        $diag = self::get_diagnostics($skus);
        if (function_exists('rest_ensure_response')) {
            return rest_ensure_response($diag);
        }
        return $diag;
    }

    /**
     * Resolves host context ('theme', 'plugin', or 'co-install').
     */
    private static function resolve_host_context(string $host_context): string {
        if ($host_context !== 'auto' && in_array($host_context, ['theme', 'plugin', 'co-install'], true)) {
            return $host_context;
        }
        if (class_exists('\Carmilla_Kernel')) {
            $by_theme  = \Carmilla_Kernel::is_booted_by('theme');
            $by_plugin = \Carmilla_Kernel::is_booted_by('plugin');
            if ($by_theme && $by_plugin) {
                return 'co-install';
            }
            if ($by_theme) {
                return 'theme';
            }
            if ($by_plugin) {
                return 'plugin';
            }
        }
        return 'theme';
    }

    /**
     * Resolves purchased SKUs from explicit input, stored license claims, or host base fallback.
     *
     * @param array<string> $purchased_skus
     * @param string        $resolved_host
     * @return array<string>
     */
    private static function resolve_purchased_skus(array $purchased_skus, string $resolved_host): array {
        if (!empty($purchased_skus)) {
            return array_values(array_unique($purchased_skus));
        }

        if (function_exists('get_option')) {
            $claims = get_option('carmilla_license_claims', null);
            if (is_array($claims) && !empty($claims['skus']) && is_array($claims['skus'])) {
                return array_values(array_unique(array_map('strval', $claims['skus'])));
            }
        }

        return $resolved_host === 'plugin' ? ['sku_plugin_base'] : ['sku_theme_base'];
    }
}

}
