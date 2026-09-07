<?php
namespace Carmilla\Core\Catalog;

/**
 * Validates dependencies and environment constraints before
 * allowing features to remain active in the effective manifest.
 */
class DependencyResolver {

    /**
     * Filters the resolved features against the current WordPress environment.
     * E.g. disables commerce features if WooCommerce is missing.
     * Disables backend-heavy features if the Bridge plugin is missing (Theme-only).
     */
    public static function filter_environment_capabilities(array $effective_features, array $boot_contexts): array {
        
        $has_woocommerce = class_exists('WooCommerce');
        $has_bridge = isset($boot_contexts['plugin']);
        
        // If WooCommerce is missing, restrict commerce capabilities
        if (!$has_woocommerce) {
            $effective_features['commerce.core'] = false;
            $effective_features['commerce.cart'] = false;
            $effective_features['commerce.payment'] = false;
            
            // Any vertical relying on commerce (like academy or clinic payments)
            // would be gracefully degraded here, but for now we just drop commerce.
        }

        // If the Bridge plugin is missing (Theme-only mode), we cannot
        // support CPTs or endpoints for verticals, because the Theme does not own them.
        if (!$has_bridge) {
            $effective_features['academy.core'] = false;
            $effective_features['academy.courses'] = false;
            $effective_features['clinic.booking'] = false;
            $effective_features['clinic.therapists'] = false;
            $effective_features['psych.tests'] = false;
            $effective_features['psych.results'] = false;
            $effective_features['builder.ui'] = false;
            $effective_features['builder.config'] = false;
        }

        return $effective_features;
    }
}
