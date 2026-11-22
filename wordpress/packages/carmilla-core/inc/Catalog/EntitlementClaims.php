<?php
namespace Carmilla\Core\Catalog;

/**
 * Entitlement Claims Fixtures & Resolver (v1)
 *
 * For local development, testing, and runtime resolution.
 * Provides fixtures of activated claims to simulate purchased licenses
 * under the Independent Products architecture.
 *
 * Part of Task P04-ENTITLEMENT-DATA-039.
 */
class EntitlementClaims {

    /**
     * Get a fixture of a fully unlocked environment (all verticals, all targets).
     *
     * @return array
     */
    public static function get_fully_unlocked_fixture(): array {
        return [
            'schemaVersion' => 1,
            'license_key'   => 'fixture-license-unlocked',
            'status'        => 'active',
            'expires_at'    => time() + 31536000, // 1 year ahead
            'site_url'      => 'https://example.com',
            'skus'          => [
                'sku_theme_base',
                'sku_plugin_base',
                'sku_commerce',
                'sku_academy',
                'sku_clinic_booking',
                'sku_clinic_consultation',
                'sku_psych',
                'sku_builder_android',
                'sku_builder_ios',
                'sku_builder_desktop',
                'sku_builder_pwa'
            ],
            'builders' => [
                'android' => true,
                'ios'     => true,
                'desktop' => true,
                'pwa'     => true,
                'web'     => true
            ],
            'features_override' => []
        ];
    }

    /**
     * Get a fixture for Theme Base (no verticals, no builder targets).
     *
     * @return array
     */
    public static function get_base_theme_fixture(): array {
        return [
            'schemaVersion' => 1,
            'artifactKind'  => 'theme',
            'license_key'   => 'fixture-license-theme-base',
            'status'        => 'active',
            'expires_at'    => null,
            'site_url'      => 'https://example.com',
            'skus'          => ['sku_theme_base'],
            'builders'      => [
                'android' => false,
                'ios'     => false,
                'desktop' => false,
                'pwa'     => false,
                'web'     => false
            ],
            'features_override' => []
        ];
    }

    /**
     * Get a fixture for Plugin Base (no verticals, no builder targets).
     *
     * @return array
     */
    public static function get_base_plugin_fixture(): array {
        return [
            'schemaVersion' => 1,
            'artifactKind'  => 'plugin',
            'license_key'   => 'fixture-license-plugin-base',
            'status'        => 'active',
            'expires_at'    => null,
            'site_url'      => 'https://example.com',
            'skus'          => ['sku_plugin_base'],
            'builders'      => [
                'android' => false,
                'ios'     => false,
                'desktop' => false,
                'pwa'     => false,
                'web'     => false
            ],
            'features_override' => []
        ];
    }

    /**
     * Get a fixture for Booking without Psych test dependency.
     *
     * @return array
     */
    public static function get_booking_without_psych_fixture(): array {
        return [
            'schemaVersion' => 1,
            'artifactKind'  => 'both',
            'license_key'   => 'fixture-license-booking-no-psych',
            'status'        => 'active',
            'expires_at'    => time() + 864000,
            'site_url'      => 'https://example.com',
            'skus'          => ['sku_clinic_booking'],
            'builders'      => [
                'android' => false,
                'ios'     => false,
                'desktop' => false,
                'pwa'     => false
            ],
            'features_override' => []
        ];
    }

    /**
     * Get a fixture for partial App Builder (Android + PWA, missing iOS/Desktop default to false).
     *
     * @return array
     */
    public static function get_app_builder_partial_fixture(): array {
        return [
            'schemaVersion' => 1,
            'artifactKind'  => 'theme',
            'sku'           => 'theme-booking-android-pwa',
            'license_key'   => 'fixture-license-booking-android-pwa',
            'status'        => 'active',
            'expires_at'    => time() + 864000,
            'site_url'      => 'https://example.com',
            'skus'          => ['theme-booking-android-pwa'],
            'features'      => [
                'commerce.core'       => true,
                'academy.core'        => false,
                'clinic.booking'      => true,
                'clinic.consultation' => false,
                'psych.tests'         => false
            ],
            'builders' => [
                'android' => true,
                // ios is omitted intentionally (missing)
                // desktop is omitted intentionally (missing)
                'pwa'     => true
            ]
        ];
    }

    /**
     * Get a fixture for a license in Grace Period (expired within last 14 days).
     *
     * @return array
     */
    public static function get_grace_period_fixture(): array {
        return [
            'schemaVersion' => 1,
            'license_key'   => 'fixture-license-grace',
            'status'        => 'active', // Raw claims might say active or expired
            'expires_at'    => time() - (5 * 86400), // Expired 5 days ago (within 14-day grace)
            'site_url'      => 'https://example.com',
            'skus'          => ['sku_commerce', 'sku_clinic_booking'],
            'builders'      => ['android' => true]
        ];
    }

    /**
     * Get a fixture for an explicitly revoked license.
     *
     * @return array
     */
    public static function get_revoked_fixture(): array {
        return [
            'schemaVersion' => 1,
            'license_key'   => 'fixture-license-revoked',
            'status'        => 'revoked',
            'revoked_at'    => time() - 3600,
            'site_url'      => 'https://example.com',
            'skus'          => ['sku_commerce', 'sku_academy'],
            'builders'      => []
        ];
    }

    /**
     * Get a fixture for site transfer.
     *
     * @return array
     */
    public static function get_site_transfer_fixture(): array {
        return [
            'schemaVersion'  => 1,
            'license_key'    => 'fixture-license-transfer',
            'status'         => 'active',
            'transfer_token' => 'xfer-tok-987654321',
            'bound_site_url' => 'https://new-domain.com',
            'prior_site_url' => 'https://example.com',
            'skus'           => ['sku_theme_base', 'sku_commerce'],
            'builders'       => ['android' => true]
        ];
    }

    /**
     * Get a fixture for Theme-only mode (backward compatible helper).
     *
     * @return array
     */
    public static function get_theme_only_fixture(): array {
        return self::get_base_theme_fixture();
    }

    /**
     * Resolves the current effective features and builder targets
     * based on the claims and the static SKU inventory.
     *
     * @param array $claims
     * @return array<string, bool>
     */
    public static function resolve_effective_features(array $claims): array {
        $inventory = EntitlementSchema::get_sku_inventory();
        $manifest  = EntitlementSchema::get_feature_manifest();

        $active_features = [];
        foreach ($manifest as $f_id => $f_def) {
            $active_features[$f_id] = false;
        }

        $builders_input = $claims['builders'] ?? [];

        // 1. Enable features and builders from SKUs
        if (!empty($claims['skus']) && is_array($claims['skus'])) {
            foreach ($claims['skus'] as $sku_id) {
                if (isset($inventory[$sku_id])) {
                    $sku_def = $inventory[$sku_id];
                    if (!empty($sku_def['features'])) {
                        foreach ($sku_def['features'] as $f) {
                            $canonical_f = EntitlementSchema::resolve_feature_id($f);
                            $active_features[$canonical_f] = true;
                        }
                    }
                    if (!empty($sku_def['builders'])) {
                        foreach ($sku_def['builders'] as $target => $enabled) {
                            if ($enabled) {
                                $builders_input[$target] = true;
                            }
                        }
                    }
                }
            }
        }

        // 2. Direct features specification (e.g. from bundle JSON in spec)
        if (!empty($claims['features']) && is_array($claims['features'])) {
            foreach ($claims['features'] as $f_key => $is_enabled) {
                $canonical_f = EntitlementSchema::resolve_feature_id($f_key);
                $active_features[$canonical_f] = (bool) $is_enabled;
            }
        }

        // 3. Apply manual overrides if present
        if (!empty($claims['features_override']) && is_array($claims['features_override'])) {
            foreach ($claims['features_override'] as $f_id => $val) {
                $canonical_f = EntitlementSchema::resolve_feature_id($f_id);
                $active_features[$canonical_f] = (bool) $val;
            }
        }

        // 4. Compute complete dependency closure and normalize builder targets
        $closure = EntitlementSchema::resolve_dependency_closure($active_features, $builders_input);

        return $closure['features'];
    }
}
