<?php
namespace Carmilla\Core\Catalog;

/**
 * Entitlement Schema & SKU Inventory (v1)
 *
 * Defines the canonical catalog of capabilities, features, SKUs (products),
 * artifact inventory mappings, and dependency closure resolution.
 * Part of Task P04-ENTITLEMENT-DATA-039.
 */
class EntitlementSchema {

    public const SCHEMA_VERSION = 1;

    public const PRODUCT_KINDS = ['theme', 'plugin', 'client'];

    public const BUILDER_TARGETS = ['android', 'ios', 'desktop', 'pwa', 'web'];

    /**
     * Legacy and backward-compatible feature aliases mapping alias -> canonical id.
     *
     * @var array<string, string>
     */
    public const FEATURE_ALIASES = [
        'builder.ui'              => 'builder.core',
        'builder.config'          => 'builder.core',
        'consultation.core'       => 'clinic.consultation',
        'clinic.sessions'         => 'clinic.consultation',
        'clinic.psych'            => 'psych.tests',
        'clinic.private_records'  => 'clinic.records',
        'builder.android'         => 'builder.target.android',
        'builder.ios'             => 'builder.target.ios',
        'builder.desktop'         => 'builder.target.desktop',
        'builder.pwa'             => 'builder.target.pwa',
        'builder.web'             => 'builder.target.web',
        'content.blog'            => 'content.core',
        'commerce.physical'       => 'commerce.core',
        'commerce.digital'        => 'commerce.core',
        'academy.quiz'            => 'academy.quizzes',
        'academy.certificate'     => 'academy.certificates',
        'clinic.messaging'        => 'clinic.consultation',
        'wallet'                  => 'commerce.wallet',
    ];

    /**
     * Resolves a feature ID, mapping any known alias to its canonical identifier.
     *
     * @param string $feature_id
     * @return string
     */
    public static function resolve_feature_id(string $feature_id): string {
        return self::FEATURE_ALIASES[$feature_id] ?? $feature_id;
    }

    /**
     * Canonical list of all known SKUs / Vertical Products.
     * Supports both new modular SKUs and legacy SKUs for backward compatibility.
     *
     * @return array<string, array>
     */
    public static function get_sku_inventory(): array {
        return [
            // Base packages without verticals
            'sku_theme_base' => [
                'id'           => 'sku_theme_base',
                'name'         => 'Carmilla Theme Base',
                'artifactKind' => 'theme',
                'type'         => 'base',
                'requires'     => [],
                'features'     => ['theme.base', 'content.core', 'content.stories', 'content.banners', 'content.support'],
                'builders'     => ['android' => false, 'ios' => false, 'desktop' => false, 'pwa' => false, 'web' => false],
                'description'  => 'Base presentation theme without commercial verticals or builder targets.'
            ],
            'sku_plugin_base' => [
                'id'           => 'sku_plugin_base',
                'name'         => 'Carmilla Plugin Base',
                'artifactKind' => 'plugin',
                'type'         => 'base',
                'requires'     => [],
                'features'     => ['plugin.base', 'content.core', 'content.support'],
                'builders'     => ['android' => false, 'ios' => false, 'desktop' => false, 'pwa' => false, 'web' => false],
                'description'  => 'Base plugin adapter running on third-party themes without commercial verticals.'
            ],

            // Commercial Verticals
            'sku_commerce' => [
                'id'           => 'sku_commerce',
                'name'         => 'Carmilla Commerce Base',
                'artifactKind' => 'both',
                'type'         => 'base',
                'requires'     => [],
                'features'     => ['commerce.core', 'commerce.cart', 'commerce.payment', 'commerce.wallet'],
                'builders'     => ['android' => false, 'ios' => false, 'desktop' => false, 'pwa' => false, 'web' => false],
                'description'  => 'Base e-commerce capabilities via WooCommerce.'
            ],
            'sku_academy' => [
                'id'           => 'sku_academy',
                'name'         => 'Carmilla Academy',
                'artifactKind' => 'both',
                'type'         => 'vertical',
                'requires'     => [],
                'features'     => ['academy.core', 'academy.courses', 'academy.quizzes', 'academy.certificates'],
                'builders'     => ['android' => false, 'ios' => false, 'desktop' => false, 'pwa' => false, 'web' => false],
                'description'  => 'LMS and academy features.'
            ],
            'sku_clinic' => [ // Legacy alias for clinic booking
                'id'           => 'sku_clinic',
                'name'         => 'Carmilla Clinic & Booking',
                'artifactKind' => 'both',
                'type'         => 'vertical',
                'requires'     => [],
                'features'     => ['clinic.booking', 'clinic.therapists'],
                'builders'     => ['android' => false, 'ios' => false, 'desktop' => false, 'pwa' => false, 'web' => false],
                'description'  => 'Booking and appointment management without psych test dependency.'
            ],
            'sku_clinic_booking' => [
                'id'           => 'sku_clinic_booking',
                'name'         => 'Carmilla Clinic Booking',
                'artifactKind' => 'both',
                'type'         => 'vertical',
                'requires'     => [],
                'features'     => ['clinic.booking', 'clinic.therapists'],
                'builders'     => ['android' => false, 'ios' => false, 'desktop' => false, 'pwa' => false, 'web' => false],
                'description'  => 'Appointment scheduling without psych test dependency.'
            ],
            'sku_clinic_consultation' => [
                'id'           => 'sku_clinic_consultation',
                'name'         => 'Carmilla Clinic Consultation & Records',
                'artifactKind' => 'both',
                'type'         => 'vertical',
                'requires'     => ['sku_clinic_booking'],
                'features'     => ['clinic.consultation', 'clinic.records'],
                'builders'     => ['android' => false, 'ios' => false, 'desktop' => false, 'pwa' => false, 'web' => false],
                'description'  => 'Online consultation rooms and private clinical records.'
            ],
            'sku_psych' => [
                'id'           => 'sku_psych',
                'name'         => 'Carmilla Psych Tests',
                'artifactKind' => 'both',
                'type'         => 'vertical',
                'requires'     => [],
                'features'     => ['psych.tests', 'psych.results'],
                'builders'     => ['android' => false, 'ios' => false, 'desktop' => false, 'pwa' => false, 'web' => false],
                'description'  => 'Psychological tests engine, independent from booking.'
            ],

            // App Builder Add-ons (independent target platforms)
            'sku_app_builder' => [ // Legacy all-round builder
                'id'           => 'sku_app_builder',
                'name'         => 'Carmilla App Builder',
                'artifactKind' => 'both',
                'type'         => 'addon',
                'requires'     => [],
                'features'     => ['builder.core', 'builder.target.android', 'builder.target.pwa'],
                'builders'     => ['android' => true, 'ios' => false, 'desktop' => false, 'pwa' => true, 'web' => false],
                'description'  => 'Mobile App Builder capabilities.'
            ],
            'sku_builder_android' => [
                'id'           => 'sku_builder_android',
                'name'         => 'Carmilla App Builder - Android',
                'artifactKind' => 'both',
                'type'         => 'addon',
                'requires'     => [],
                'features'     => ['builder.core', 'builder.target.android'],
                'builders'     => ['android' => true, 'ios' => false, 'desktop' => false, 'pwa' => false, 'web' => false],
                'description'  => 'Android App Generation Target.'
            ],
            'sku_builder_ios' => [
                'id'           => 'sku_builder_ios',
                'name'         => 'Carmilla App Builder - iOS',
                'artifactKind' => 'both',
                'type'         => 'addon',
                'requires'     => [],
                'features'     => ['builder.core', 'builder.target.ios'],
                'builders'     => ['android' => false, 'ios' => true, 'desktop' => false, 'pwa' => false, 'web' => false],
                'description'  => 'iOS App Generation Target.'
            ],
            'sku_builder_desktop' => [
                'id'           => 'sku_builder_desktop',
                'name'         => 'Carmilla App Builder - Desktop',
                'artifactKind' => 'both',
                'type'         => 'addon',
                'requires'     => [],
                'features'     => ['builder.core', 'builder.target.desktop'],
                'builders'     => ['android' => false, 'ios' => false, 'desktop' => true, 'pwa' => false, 'web' => false],
                'description'  => 'Desktop App Generation Target.'
            ],
            'sku_builder_pwa' => [
                'id'           => 'sku_builder_pwa',
                'name'         => 'Carmilla App Builder - PWA',
                'artifactKind' => 'both',
                'type'         => 'addon',
                'requires'     => [],
                'features'     => ['builder.core', 'builder.target.pwa'],
                'builders'     => ['android' => false, 'ios' => false, 'desktop' => false, 'pwa' => true, 'web' => false],
                'description'  => 'Progressive Web App Generation Target.'
            ],

            // Bundles
            'theme-booking-android-pwa' => [
                'id'           => 'theme-booking-android-pwa',
                'name'         => 'Carmilla Theme + Booking + Android + PWA Bundle',
                'artifactKind' => 'theme',
                'type'         => 'bundle',
                'requires'     => [],
                'features'     => [
                    'theme.base', 'content.core', 'commerce.core',
                    'clinic.booking', 'clinic.therapists',
                    'builder.core', 'builder.target.android', 'builder.target.pwa'
                ],
                'builders'     => ['android' => true, 'ios' => false, 'desktop' => false, 'pwa' => true, 'web' => false],
                'description'  => 'Canonical bundle matching INDEPENDENT_PRODUCTS_SPEC_FA.md.'
            ],
            'sku_all_in_one' => [
                'id'           => 'sku_all_in_one',
                'name'         => 'Carmilla Complete Enterprise Bundle',
                'artifactKind' => 'both',
                'type'         => 'bundle',
                'requires'     => [],
                'features'     => [
                    'theme.base', 'plugin.base', 'content.core', 'content.stories', 'content.banners', 'content.support',
                    'commerce.core', 'commerce.cart', 'commerce.payment', 'commerce.wallet',
                    'academy.core', 'academy.courses', 'academy.quizzes', 'academy.certificates',
                    'clinic.booking', 'clinic.therapists', 'clinic.consultation', 'clinic.records',
                    'psych.tests', 'psych.results',
                    'builder.core', 'builder.target.android', 'builder.target.ios', 'builder.target.desktop', 'builder.target.pwa', 'builder.target.web'
                ],
                'builders'     => ['android' => true, 'ios' => true, 'desktop' => true, 'pwa' => true, 'web' => true],
                'description'  => 'Enterprise bundle covering all verticals and targets.'
            ]
        ];
    }

    /**
     * Maps fine-grained features to their default states, domain, and dependencies.
     *
     * @return array<string, array>
     */
    public static function get_feature_manifest(): array {
        return [
            // Base
            'theme.base'             => ['default' => true,  'domain' => 'content',   'label' => 'Theme Base Layout', 'requires' => []],
            'plugin.base'            => ['default' => true,  'domain' => 'content',   'label' => 'Plugin Base Adapter', 'requires' => []],
            'content.core'           => ['default' => true,  'domain' => 'content',   'label' => 'Content & Media Core', 'requires' => []],
            'content.stories'        => ['default' => true,  'domain' => 'content',   'label' => 'Stories & Feeds', 'requires' => ['content.core']],
            'content.banners'        => ['default' => true,  'domain' => 'content',   'label' => 'Promotional Banners', 'requires' => ['content.core']],
            'content.support'        => ['default' => true,  'domain' => 'support',   'label' => 'Customer Support & Notifications', 'requires' => ['content.core']],

            // Shop / Commerce
            'commerce.core'          => ['default' => false, 'domain' => 'shop',      'label' => 'Core Commerce Engine', 'requires' => []],
            'commerce.cart'          => ['default' => false, 'domain' => 'shop',      'label' => 'Shopping Cart', 'requires' => ['commerce.core']],
            'commerce.payment'       => ['default' => false, 'domain' => 'shop',      'label' => 'Payment Gateways', 'requires' => ['commerce.core', 'commerce.cart']],
            'commerce.wallet'        => ['default' => false, 'domain' => 'shop',      'label' => 'Customer Wallet', 'requires' => ['commerce.core', 'commerce.payment']],

            // Education / Academy
            'academy.core'           => ['default' => false, 'domain' => 'education', 'label' => 'Academy Core Engine', 'requires' => []],
            'academy.courses'        => ['default' => false, 'domain' => 'education', 'label' => 'Course Syllabus & Lessons', 'requires' => ['academy.core']],
            'academy.quizzes'        => ['default' => false, 'domain' => 'education', 'label' => 'Course Quizzes', 'requires' => ['academy.core', 'academy.courses']],
            'academy.certificates'   => ['default' => false, 'domain' => 'education', 'label' => 'Course Certificates', 'requires' => ['academy.core', 'academy.courses']],

            // Clinic & Booking (NO psych dependency)
            'clinic.booking'         => ['default' => false, 'domain' => 'clinic',    'label' => 'Booking System', 'requires' => []],
            'clinic.therapists'      => ['default' => false, 'domain' => 'clinic',    'label' => 'Therapist Management', 'requires' => ['clinic.booking']],
            'clinic.consultation'    => ['default' => false, 'domain' => 'clinic',    'label' => 'Teleconsultation Sessions', 'requires' => ['clinic.booking']],
            'clinic.records'         => ['default' => false, 'domain' => 'clinic',    'label' => 'Clinical Confidential Records', 'requires' => ['clinic.booking', 'clinic.consultation']],

            // Psychological Testing (independent from booking)
            'psych.tests'            => ['default' => false, 'domain' => 'clinic',    'label' => 'Psychological Tests', 'requires' => []],
            'psych.results'          => ['default' => false, 'domain' => 'clinic',    'label' => 'Test Results & Analytics', 'requires' => ['psych.tests']],

            // App Builder & Target platforms
            'builder.core'           => ['default' => false, 'domain' => 'admin',     'label' => 'App Builder Core', 'requires' => []],
            'builder.target.android' => ['default' => false, 'domain' => 'admin',     'label' => 'App Builder - Android Target', 'requires' => ['builder.core']],
            'builder.target.ios'     => ['default' => false, 'domain' => 'admin',     'label' => 'App Builder - iOS Target', 'requires' => ['builder.core']],
            'builder.target.desktop' => ['default' => false, 'domain' => 'admin',     'label' => 'App Builder - Desktop Target', 'requires' => ['builder.core']],
            'builder.target.pwa'     => ['default' => false, 'domain' => 'admin',     'label' => 'App Builder - PWA Target', 'requires' => ['builder.core']],
            'builder.target.web'     => ['default' => false, 'domain' => 'admin',     'label' => 'App Builder - Web Client Target', 'requires' => ['builder.core']],

            // Legacy entries for backward compatibility
            'builder.ui'             => ['default' => false, 'domain' => 'admin',     'label' => 'App Builder UI (Legacy)', 'requires' => ['builder.core']],
            'builder.config'         => ['default' => false, 'domain' => 'admin',     'label' => 'App Builder Configurator (Legacy)', 'requires' => ['builder.core']]
        ];
    }

    /**
     * Resolves the dependency closure for a requested set of features and builders.
     *
     * Rules:
     * 1. Missing builder targets default to false.
     * 2. If any builder target is true, builder.core is automatically required.
     * 3. Having one target (e.g. android) does NOT give rights to other targets.
     * 4. Transitive feature dependencies are recursively satisfied.
     *
     * @param array<string, bool> $features
     * @param array<string, bool> $builders
     * @return array{features: array<string, bool>, builders: array<string, bool>}
     */
    public static function resolve_dependency_closure(array $features, array $builders = []): array {
        $manifest = self::get_feature_manifest();

        // 1. Normalize builder targets: missing default to false
        $resolved_builders = [];
        $has_any_target = false;
        foreach (self::BUILDER_TARGETS as $target) {
            $is_enabled = !empty($builders[$target]);
            $resolved_builders[$target] = $is_enabled;
            if ($is_enabled) {
                $has_any_target = true;
            }
        }

        // 2. Canonicalize feature keys
        $resolved_features = [];
        foreach ($manifest as $f_id => $meta) {
            $resolved_features[$f_id] = false;
        }

        foreach ($features as $f_key => $is_enabled) {
            if ($is_enabled) {
                $canonical = self::resolve_feature_id($f_key);
                if (isset($resolved_features[$canonical])) {
                    $resolved_features[$canonical] = true;
                }
            }
        }

        // 3. Connect builder targets to feature flags
        if ($has_any_target) {
            $resolved_features['builder.core'] = true;
        }
        foreach (self::BUILDER_TARGETS as $target) {
            $target_feature = "builder.target.{$target}";
            if ($resolved_builders[$target]) {
                $resolved_features[$target_feature] = true;
                $resolved_features['builder.core'] = true;
            } else {
                $resolved_features[$target_feature] = false;
            }
        }

        // 4. Resolve transitive feature dependencies (recursive pass)
        $changed = true;
        $max_iterations = 10;
        $iteration = 0;
        while ($changed && $iteration < $max_iterations) {
            $changed = false;
            $iteration++;
            foreach ($resolved_features as $f_id => $is_active) {
                if ($is_active && !empty($manifest[$f_id]['requires'])) {
                    foreach ($manifest[$f_id]['requires'] as $req) {
                        $canonical_req = self::resolve_feature_id($req);
                        if (isset($resolved_features[$canonical_req]) && !$resolved_features[$canonical_req]) {
                            $resolved_features[$canonical_req] = true;
                            $changed = true;
                        }
                    }
                }
            }
        }

        return [
            'features' => $resolved_features,
            'builders' => $resolved_builders
        ];
    }

    /**
     * Artifact inventory mapping feature -> module, files, resources, routes, admin, view, dependencies.
     *
     * @return array<string, array>
     */
    public static function get_artifact_inventory(): array {
        $candidate_paths = [
            dirname(__DIR__, 5) . '/docs/contracts/carmilla-entitlement-catalog-v1.json',
            dirname(__DIR__, 4) . '/docs/contracts/carmilla-entitlement-catalog-v1.json',
            dirname(__DIR__, 3) . '/docs/contracts/carmilla-entitlement-catalog-v1.json',
            '/var/www/html/docs/contracts/carmilla-entitlement-catalog-v1.json',
            '/app/docs/contracts/carmilla-entitlement-catalog-v1.json'
        ];
        foreach ($candidate_paths as $json_file) {
            if (file_exists($json_file)) {
                $data = json_decode(file_get_contents($json_file), true);
                if (!empty($data['artifactInventory'])) {
                    return $data['artifactInventory'];
                }
            }
        }
        return [];
    }

    /**
     * Returns lifecycle policy parameters.
     *
     * @return array
     */
    public static function get_lifecycle_policies(): array {
        return [
            'statuses' => ['active', 'grace_period', 'expired', 'revoked', 'suspended', 'transferred'],
            'grace_period_days' => 14,
            'offline_allowed' => true,
            'require_online_ping' => false,
            'halt_writes_on_revocation' => true,
            'preserve_site_data_on_revocation' => true,
        ];
    }
}
