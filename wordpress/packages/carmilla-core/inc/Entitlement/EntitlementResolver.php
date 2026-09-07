<?php
namespace Carmilla\Core\Entitlement;

use Carmilla\Core\Catalog\EntitlementSchema;
use Carmilla\Core\Catalog\EntitlementClaims;
use Carmilla\Core\Catalog\DependencyResolver;

/**
 * Entitlement Resolver
 *
 * Validates license claims against site binding, signature integrity, and expiry.
 * Resolves effective features and manages quota single-counting in co-install mode.
 *
 * Part of Task P04-ENTITLEMENT-CODE-040.
 */
class EntitlementResolver {

    /**
     * Cache for resolved quotas to prevent duplicate counting during co-install.
     *
     * @var array<string, int>
     */
    private static $resolved_quotas = [];

    /**
     * Validates license claims against site binding, status, expiry, and tampering.
     *
     * @param mixed $claims The license claims array to validate.
     * @return bool True if license is valid and bound to this site, false otherwise.
     */
    public static function validate_license($claims): bool {
        if (!is_array($claims) || empty($claims)) {
            return false;
        }

        // 1. Check status is strictly 'active'
        if (!isset($claims['status']) || $claims['status'] !== 'active') {
            return false;
        }

        // 2. Reject explicitly tampered or unsigned licenses
        if (!empty($claims['tampered']) || (isset($claims['is_valid']) && $claims['is_valid'] === false)) {
            return false;
        }
        if (isset($claims['signed']) && $claims['signed'] === false) {
            return false;
        }
        if (isset($claims['signature']) && ($claims['signature'] === false || $claims['signature'] === '')) {
            return false;
        }

        // 3. Expiration check
        if (!empty($claims['expires_at'])) {
            $now = time();
            if (is_numeric($claims['expires_at'])) {
                if ($now > (int) $claims['expires_at']) {
                    return false;
                }
            } else {
                $exp_time = strtotime((string) $claims['expires_at']);
                if ($exp_time !== false && $now > $exp_time) {
                    return false;
                }
            }
        }

        // 4. Site binding check
        // If site binding is specified in the claims, it must match the current site URL
        $claim_site = $claims['site_url'] ?? $claims['site_binding'] ?? $claims['bound_site_url'] ?? null;
        if (!empty($claim_site)) {
            $current_site = function_exists('get_site_url') ? get_site_url() : '';
            if (!empty($current_site)) {
                $normalized_claim = self::normalize_url((string) $claim_site);
                $normalized_site  = self::normalize_url((string) $current_site);

                if ($normalized_claim !== $normalized_site) {
                    return false; // Wrong-site binding rejected
                }
            }
        }

        return true;
    }

    /**
     * Resolves effective features based on valid claims and environment constraints.
     * Rejects unsigned, tampered, or wrong-site licenses by returning all-false features.
     *
     * @param mixed $claims The license claims array.
     * @return array<string, bool> Map of feature ID to boolean enabled state.
     */
    public static function resolve($claims): array {
        // Reject invalid/unsigned/tampered/wrong-site licenses by returning all-false features
        if (!self::validate_license($claims)) {
            return self::get_all_false_features();
        }

        // 1. Resolve raw features based on SKU inventory & claims
        $effective = EntitlementClaims::resolve_effective_features($claims);

        // 2. Obtain current boot contexts (Theme, Plugin, or Co-install)
        $boot_contexts = class_exists('\Carmilla_Kernel') 
            ? \Carmilla_Kernel::get_boot_contexts() 
            : [];

        // 3. Filter against environmental capabilities (e.g. WooCommerce availability)
        return DependencyResolver::filter_environment_capabilities($effective, $boot_contexts);
    }

    /**
     * Checks if a specific SKU is included in the claims and the license is valid.
     *
     * @param mixed  $claims The license claims array.
     * @param string $sku_id The SKU identifier to check.
     * @return bool True if license is valid and contains the SKU.
     */
    public static function has_sku($claims, string $sku_id): bool {
        if (!self::validate_license($claims)) {
            return false;
        }

        if (empty($claims['skus']) || !is_array($claims['skus'])) {
            return false;
        }

        return in_array($sku_id, $claims['skus'], true);
    }

    /**
     * Resolves an effective quota value, preventing quota duplication in co-install mode.
     * If both Theme and Plugin inspect or register quotas, this ensures a canonical single count.
     *
     * @param mixed  $claims    The license claims array.
     * @param string $quota_key The quota identifier (e.g. 'max_products', 'max_courses').
     * @param int    $default   Default quota value if not specified.
     * @return int Effective single-counted quota value.
     */
    public static function resolve_effective_quota($claims, string $quota_key, int $default = 0): int {
        if (!self::validate_license($claims)) {
            return 0;
        }

        // Return from memory cache if already resolved in this request cycle to avoid co-install double counting
        if (isset(self::$resolved_quotas[$quota_key])) {
            return self::$resolved_quotas[$quota_key];
        }

        $quota_val = $default;
        if (!empty($claims['quotas']) && is_array($claims['quotas']) && isset($claims['quotas'][$quota_key])) {
            $quota_val = (int) $claims['quotas'][$quota_key];
        }

        // In co-install, quota represents the absolute entitlement ceiling, never additive per product
        self::$resolved_quotas[$quota_key] = max(0, $quota_val);

        return self::$resolved_quotas[$quota_key];
    }

    /**
     * Resets internal quota resolution cache.
     *
     * @return void
     */
    public static function reset_quota_cache(): void {
        self::$resolved_quotas = [];
    }

    /**
     * Generates a feature map where every known manifest feature is disabled (false).
     *
     * @return array<string, bool>
     */
    public static function get_all_false_features(): array {
        $manifest = EntitlementSchema::get_feature_manifest();
        $features = [];
        foreach (array_keys($manifest) as $feature_id) {
            $features[$feature_id] = false;
        }
        return $features;
    }

    /**
     * Normalizes a URL for robust comparison across protocols, ports, and trailing slashes.
     *
     * @param string $url The URL to normalize.
     * @return string
     */
    private static function normalize_url(string $url): string {
        $url = trim($url);
        // Strip scheme
        $url = preg_replace('#^https?://#i', '', $url);
        // Strip trailing slash
        $url = rtrim($url, "/\\");
        return strtolower($url);
    }
}
