<?php
namespace Carmilla\Core\Entitlement;

use Carmilla\Core\Catalog\EntitlementSchema;
use Carmilla\Core\Catalog\EntitlementClaims;
use Carmilla\Core\Catalog\DependencyResolver;

/**
 * Entitlement Resolver (v1)
 *
 * Validates license claims against site binding, signature integrity, and expiry.
 * Resolves effective features, manages quota single-counting in co-install mode,
 * and enforces platform builder targets and lifecycle states (grace, revoke, transfer).
 *
 * Part of Task P04-ENTITLEMENT-DATA-039 / P04-ENTITLEMENT-CODE-040.
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
     * @param bool  $allow_grace Whether to treat active grace-period as technically valid. Defaults to false.
     * @return bool True if license is valid and bound to this site, false otherwise.
     */
    public static function validate_license($claims, bool $allow_grace = false): bool {
        if (!is_array($claims) || empty($claims)) {
            return false;
        }

        // 1. Check status is strictly 'active' (or grace_period if allowed)
        if (!isset($claims['status'])) {
            return false;
        }
        if ($claims['status'] !== 'active' && !($allow_grace && $claims['status'] === 'grace_period')) {
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
            $grace_seconds = $allow_grace ? (14 * 86400) : 0;
            if (is_numeric($claims['expires_at'])) {
                if ($now > ((int) $claims['expires_at'] + $grace_seconds)) {
                    return false;
                }
            } else {
                $exp_time = strtotime((string) $claims['expires_at']);
                if ($exp_time !== false && $now > ($exp_time + $grace_seconds)) {
                    return false;
                }
            }
        }

        // 4. Site binding check
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
     * Determines detailed lifecycle status of the license.
     *
     * @param mixed $claims
     * @return string 'active'|'grace_period'|'expired'|'revoked'|'transferred'|'invalid'
     */
    public static function get_license_status($claims): string {
        if (!is_array($claims) || empty($claims)) {
            return 'invalid';
        }

        if (!empty($claims['tampered']) || (isset($claims['is_valid']) && $claims['is_valid'] === false)) {
            return 'invalid';
        }

        $raw_status = $claims['status'] ?? 'invalid';
        if ($raw_status === 'revoked') {
            return 'revoked';
        }
        if ($raw_status === 'transferred') {
            return 'transferred';
        }
        if ($raw_status === 'suspended') {
            return 'suspended';
        }

        // Check expiration and grace
        if (!empty($claims['expires_at'])) {
            $now = time();
            $exp_time = is_numeric($claims['expires_at']) ? (int) $claims['expires_at'] : strtotime((string) $claims['expires_at']);
            if ($exp_time && $now > $exp_time) {
                $grace_limit = $exp_time + (14 * 86400);
                if ($now <= $grace_limit) {
                    return 'grace_period';
                }
                return 'expired';
            }
        }

        return ($raw_status === 'active') ? 'active' : 'invalid';
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
     * Checks if a specific feature is active in the resolved manifest.
     *
     * @param mixed  $claims
     * @param string $feature_id
     * @return bool
     */
    public static function has_feature($claims, string $feature_id): bool {
        $canonical = EntitlementSchema::resolve_feature_id($feature_id);
        $features = self::resolve($claims);
        return !empty($features[$canonical]);
    }

    /**
     * Checks if a specific builder target platform is entitled.
     *
     * @param mixed  $claims The license claims array.
     * @param string $target Target platform: 'android'|'ios'|'desktop'|'pwa'|'web'.
     * @return bool True if license is valid and target is explicitly entitled.
     */
    public static function has_builder_target($claims, string $target): bool {
        if (!self::validate_license($claims)) {
            return false;
        }

        $features = self::resolve($claims);
        $target_key = "builder.target.{$target}";
        return !empty($features[$target_key]);
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

        if (isset(self::$resolved_quotas[$quota_key])) {
            return self::$resolved_quotas[$quota_key];
        }

        $quota_val = $default;
        if (!empty($claims['quotas']) && is_array($claims['quotas']) && isset($claims['quotas'][$quota_key])) {
            $quota_val = (int) $claims['quotas'][$quota_key];
        }

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
        $url = preg_replace('#^https?://#i', '', $url);
        $url = rtrim($url, "/\\");
        return strtolower($url);
    }
}
