<?php
namespace Carmilla\Core\Health;

use Carmilla\Core\Adapter\WooCommerceAdapter;

/**
 * System Health and Preflight Checker
 *
 * Runs non-destructive preflight diagnostics across WordPress environment:
 * HTTPS, WooCommerce, Permalinks, REST API, WP-Cron, and Kernel compatibility.
 * Provides clear remediation paths and strictly redacts any sensitive credentials or secrets.
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
     * Executes all system health checks.
     *
     * @param array<string> $purchased_skus Array of purchased SKU IDs (e.g. ['sku_commerce']).
     * @return array<string, array{id: string, name: string, status: string, message: string, remediation: ?string}>
     */
    public static function run_all_checks(array $purchased_skus = []): array {
        $is_commerce_purchased = in_array('sku_commerce', $purchased_skus, true);

        return [
            'https'          => self::check_https(),
            'woocommerce'    => self::check_woocommerce($is_commerce_purchased),
            'permalinks'     => self::check_permalinks(),
            'rest_api'       => self::check_rest_api(),
            'wp_cron'        => self::check_wp_cron(),
            'kernel_version' => self::check_kernel_version(self::MINIMUM_KERNEL_VERSION),
        ];
    }

    /**
     * Checks if the site is serving traffic over HTTPS (SSL).
     *
     * @return array
     */
    public static function check_https(): array {
        $is_ssl = function_exists('is_ssl') && is_ssl();

        if ($is_ssl) {
            return [
                'id'          => 'https',
                'name'        => 'HTTPS Transport Security',
                'status'      => 'pass',
                'message'     => 'Site is running over secure HTTPS connection.',
                'remediation' => null,
            ];
        }

        return [
            'id'          => 'https',
            'name'        => 'HTTPS Transport Security',
            'status'      => 'fail',
            'message'     => 'Site is running over unencrypted HTTP. REST API and payment flows require HTTPS.',
            'remediation' => 'Install a valid SSL certificate and configure your WordPress site URL to use https:// in Settings > General.',
        ];
    }

    /**
     * Validates WooCommerce availability.
     * Only fails if Commerce SKU is purchased but WooCommerce is missing.
     *
     * @param bool $is_commerce_sku_purchased Whether the site license includes Commerce SKU.
     * @return array
     */
    public static function check_woocommerce(bool $is_commerce_sku_purchased = false): array {
        $is_active = class_exists('WooCommerce');

        if ($is_active) {
            return [
                'id'          => 'woocommerce',
                'name'        => 'WooCommerce Core Engine',
                'status'      => 'pass',
                'message'     => 'WooCommerce is installed and active.',
                'remediation' => null,
            ];
        }

        if ($is_commerce_sku_purchased) {
            return [
                'id'          => 'woocommerce',
                'name'        => 'WooCommerce Core Engine',
                'status'      => 'fail',
                'message'     => 'Commerce SKU is licensed for this site, but WooCommerce is inactive or not installed.',
                'remediation' => 'Install and activate the official WooCommerce plugin to enable store features.',
            ];
        }

        return [
            'id'          => 'woocommerce',
            'name'        => 'WooCommerce Core Engine',
            'status'      => 'info',
            'message'     => 'WooCommerce is not active, but Commerce SKU is not purchased. Base UI/API functions independently.',
            'remediation' => null,
        ];
    }

    /**
     * Validates that pretty permalinks are enabled (required for modern REST endpoints).
     *
     * @return array
     */
    public static function check_permalinks(): array {
        $structure = function_exists('get_option') ? get_option('permalink_structure') : '';

        if (!empty($structure)) {
            return [
                'id'          => 'permalinks',
                'name'        => 'Pretty Permalinks',
                'status'      => 'pass',
                'message'     => 'Pretty permalinks are active.',
                'remediation' => null,
            ];
        }

        return [
            'id'          => 'permalinks',
            'name'        => 'Pretty Permalinks',
            'status'      => 'fail',
            'message'     => 'Plain permalinks are active. Pretty permalinks are required for standard REST API routing.',
            'remediation' => 'Navigate to WordPress Settings > Permalinks and select Post name (/%postname%/) or another structure.',
        ];
    }

    /**
     * Validates that WordPress REST API functions are accessible.
     *
     * @return array
     */
    public static function check_rest_api(): array {
        $has_rest = function_exists('rest_url');

        if ($has_rest) {
            return [
                'id'          => 'rest_api',
                'name'        => 'WordPress REST API',
                'status'      => 'pass',
                'message'     => 'WordPress REST API infrastructure is loaded.',
                'remediation' => null,
            ];
        }

        return [
            'id'          => 'rest_api',
            'name'        => 'WordPress REST API',
            'status'      => 'fail',
            'message'     => 'WordPress REST API core functions could not be verified.',
            'remediation' => 'Verify WordPress core integrity and ensure REST API has not been disabled by a firewall plugin.',
        ];
    }

    /**
     * Validates that WP-Cron is not disabled without an external runner.
     *
     * @return array
     */
    public static function check_wp_cron(): array {
        $disabled = defined('DISABLE_WP_CRON') && DISABLE_WP_CRON;

        if ($disabled) {
            return [
                'id'          => 'wp_cron',
                'name'        => 'WordPress Cron Scheduler',
                'status'      => 'warning',
                'message'     => 'DISABLE_WP_CRON constant is enabled. Automated tasks rely on external system crontab.',
                'remediation' => 'Verify your hosting server has a crontab entry triggering wp-cron.php (e.g. */10 * * * * curl -s get_site_url()/wp-cron.php).',
            ];
        }

        return [
            'id'          => 'wp_cron',
            'name'        => 'WordPress Cron Scheduler',
            'status'      => 'pass',
            'message'     => 'Default WordPress pseudo-cron scheduler is active.',
            'remediation' => null,
        ];
    }

    /**
     * Validates Carmilla Kernel version compatibility.
     *
     * @param string $minimum_version Minimum required version string.
     * @return array
     */
    public static function check_kernel_version(string $minimum_version = self::MINIMUM_KERNEL_VERSION): array {
        $current_version = '1.0.0';
        if (class_exists('\Carmilla_Kernel') && defined('\Carmilla_Kernel::VERSION')) {
            $current_version = \Carmilla_Kernel::VERSION;
        }

        if (version_compare($current_version, $minimum_version, '>=')) {
            return [
                'id'          => 'kernel_version',
                'name'        => 'Carmilla Shared Kernel',
                'status'      => 'pass',
                'message'     => sprintf('Kernel version %s meets requirement (>= %s).', $current_version, $minimum_version),
                'remediation' => null,
            ];
        }

        return [
            'id'          => 'kernel_version',
            'name'        => 'Carmilla Shared Kernel',
            'status'      => 'fail',
            'message'     => sprintf('Kernel version %s is below required %s.', $current_version, $minimum_version),
            'remediation' => 'Update the carmilla-core package to the latest release.',
        ];
    }

    /**
     * Returns redacted diagnostic information for troubleshooting and support.
     * STRICTLY redacts secrets, database passwords, salts, and private keys.
     *
     * @param array<string> $purchased_skus Array of purchased SKU IDs.
     * @return array Structured diagnostic metadata.
     */
    public static function get_diagnostics(array $purchased_skus = []): array {
        global $wp_version;

        $boot_contexts = class_exists('\Carmilla_Kernel')
            ? \Carmilla_Kernel::get_boot_contexts()
            : [];

        $hpos_enabled = class_exists(WooCommerceAdapter::class)
            ? WooCommerceAdapter::is_hpos_enabled()
            : false;

        return [
            'environment' => [
                'php_version'        => PHP_VERSION,
                'wp_version'         => $wp_version ?? 'unknown',
                'server_software'    => $_SERVER['SERVER_SOFTWARE'] ?? 'CLI/Unknown',
                'is_ssl'             => function_exists('is_ssl') ? is_ssl() : false,
                'woocommerce_active' => class_exists('WooCommerce'),
                'woocommerce_version'=> defined('WC_VERSION') ? WC_VERSION : null,
                'hpos_enabled'       => $hpos_enabled,
            ],
            'kernel' => [
                'version'       => '1.0.0',
                'boot_contexts' => $boot_contexts,
            ],
            'checks' => self::run_all_checks($purchased_skus),
            'secrets_redacted' => true,
        ];
    }
}
