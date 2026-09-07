<?php
namespace Carmilla\Core\Catalog;

/**
 * Entitlement Claims Fixtures
 * For local development and CI testing, provides a fixture
 * of activated claims / entitlements to simulate a purchased license.
 */
class EntitlementClaims {

    /**
     * Get a fixture of a fully unlocked environment (for P04 / CI)
     */
    public static function get_fully_unlocked_fixture() {
        return [
            'license_key' => 'fixture-license-unlocked',
            'status' => 'active',
            'expires_at' => null,
            'skus' => [
                'sku_commerce',
                'sku_academy',
                'sku_clinic',
                'sku_psych',
                'sku_app_builder'
            ],
            'features_override' => []
        ];
    }

    /**
     * Get a fixture for Theme-only mode (No plugins, only commerce base if any)
     */
    public static function get_theme_only_fixture() {
        return [
            'license_key' => 'fixture-license-theme',
            'status' => 'active',
            'expires_at' => null,
            'skus' => [
                'sku_commerce'
            ],
            'features_override' => [
                'academy.core' => false,
                'clinic.booking' => false,
                'psych.tests' => false
            ]
        ];
    }
    
    /**
     * Resolves the current effective features based on the claims
     * and the static sku inventory.
     */
    public static function resolve_effective_features($claims) {
        $inventory = EntitlementSchema::get_sku_inventory();
        $manifest = EntitlementSchema::get_feature_manifest();
        
        $effective = [];
        // Start with defaults
        foreach ($manifest as $f_id => $f_def) {
            $effective[$f_id] = false;
        }

        // Enable based on SKUs
        foreach ($claims['skus'] as $sku_id) {
            if (isset($inventory[$sku_id])) {
                foreach ($inventory[$sku_id]['features'] as $feature) {
                    $effective[$feature] = true;
                }
            }
        }

        // Apply manual overrides
        if (!empty($claims['features_override'])) {
            foreach ($claims['features_override'] as $f_id => $val) {
                $effective[$f_id] = (bool) $val;
            }
        }

        return $effective;
    }
}
