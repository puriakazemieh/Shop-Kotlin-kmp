<?php
namespace Carmilla\Core\Catalog;

/**
 * Validates dependencies and environment constraints before
 * allowing features to remain active in the effective manifest.
 *
 * ARCHITECTURAL RULE (ADR-006, ADR-038, ADR-007):
 * Standalone Theme and Standalone Plugin each bundle the versioned kernel.
 * Theme-only mode DOES NOT require Carmilla Bridge for domain features.
 * WooCommerce remains the canonical external prerequisite for commerce.* features.
 */
class DependencyResolver {

    /**
     * Filters the resolved features against the current WordPress environment.
     * E.g. disables commerce features if WooCommerce is missing.
     *
     * @param array<string, bool> $effective_features
     * @param array<string, mixed> $boot_contexts
     * @return array<string, bool>
     */
    public static function filter_environment_capabilities(array $effective_features, array $boot_contexts): array {
        $has_woocommerce = class_exists('WooCommerce');

        // If WooCommerce is missing, restrict commerce capabilities
        if (!$has_woocommerce) {
            $effective_features['commerce.core'] = false;
            $effective_features['commerce.cart'] = false;
            $effective_features['commerce.payment'] = false;
            $effective_features['commerce.wallet'] = false;
        }

        return $effective_features;
    }
}
