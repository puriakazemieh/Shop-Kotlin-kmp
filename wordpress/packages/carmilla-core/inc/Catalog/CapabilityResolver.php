<?php
namespace Carmilla\Core\Catalog;

/**
 * CapabilityResolver
 *
 * Canonical resolver for feature capability states, environmental prerequisites,
 * release gates, and effective activation under the Independent Products architecture.
 *
 * Enforces:
 * 1. Single source of truth for both Theme and Plugin hosts.
 * 2. Five dimensions of capability state:
 *    - Code presence in package (package_code)
 *    - Valid entitlement (license claims)
 *    - Admin toggle (site settings)
 *    - Environmental prerequisites (WooCommerce, external services)
 *    - Release readiness gate (releaseReady !== false)
 * 3. Distinct status codes:
 *    - ACTIVE
 *    - NOT_ENTITLED
 *    - MISSING_CODE
 *    - DISABLED_BY_ADMIN
 *    - PREREQUISITE_MISSING
 *    - RELEASE_GATE_RESTRICTED
 * 4. Architectural Rules (ADR-006, INDEPENDENT_PRODUCTS_SPEC_FA):
 *    - Admin toggle cannot grant entitlement or invent missing code.
 *    - Missing WooCommerce ONLY disables commerce.* features; does NOT block base UI or other verticals.
 *    - Neither Carmilla host requires the other as a prerequisite.
 *    - Release gates cannot be bypassed by admin toggle.
 *    - Base UI (theme.base, plugin.base, content.core) remains always executable.
 *
 * Part of Task P04-WPPLUGIN-CODE-006.
 */
class CapabilityResolver {

    public const STATUS_ACTIVE                  = 'ACTIVE';
    public const STATUS_NOT_ENTITLED            = 'NOT_ENTITLED';
    public const STATUS_MISSING_CODE            = 'MISSING_CODE';
    public const STATUS_DISABLED_BY_ADMIN       = 'DISABLED_BY_ADMIN';
    public const STATUS_PREREQUISITE_MISSING    = 'PREREQUISITE_MISSING';
    public const STATUS_RELEASE_GATE_RESTRICTED = 'RELEASE_GATE_RESTRICTED';

    /**
     * Resolves the complete capability matrix across all known features.
     *
     * @param array $args {
     *     @type array|null $package_code     Map of feature_id => bool. Defaults to all present.
     *     @type mixed      $entitlements      Claims array or map of feature_id => bool. Defaults to fully unlocked fixture if null.
     *     @type array|null $admin_settings    Map of feature_id => bool. Defaults to manifest defaults / enabled if null.
     *     @type array|null $environment       Map of environment flags ('woocommerce_active', 'is_theme_active', etc.).
     *     @type array|null $release_catalog   Map of feature_id => bool (is_release_ready) or feature_id => ['releaseReady' => bool].
     *     @type string     $artifact_kind     'theme'|'plugin'|'both'. Defaults to auto-detect.
     * }
     * @return array{
     *     features: array<string, array>,
     *     effective_features: array<string, bool>,
     *     summary: array<string, int>
     * }
     */
    public static function resolve_matrix(array $args = []): array {
        $manifest = EntitlementSchema::get_feature_manifest();

        // 1. Resolve Package Code presence
        $package_code_input = $args['package_code'] ?? null;
        if ($package_code_input === null && function_exists('get_option')) {
            $stored_pkg = get_option('carmilla_package_manifest', null);
            if (!empty($stored_pkg) && is_array($stored_pkg)) {
                $package_code_input = $stored_pkg['features'] ?? $stored_pkg;
            }
        }

        $package_code_map = [];
        foreach ($manifest as $f_id => $meta) {
            if ($package_code_input !== null && is_array($package_code_input)) {
                $canonical_f = EntitlementSchema::resolve_feature_id($f_id);
                if (array_key_exists($f_id, $package_code_input)) {
                    $package_code_map[$f_id] = (bool) $package_code_input[$f_id];
                } elseif (array_key_exists($canonical_f, $package_code_input)) {
                    $package_code_map[$f_id] = (bool) $package_code_input[$canonical_f];
                } else {
                    $package_code_map[$f_id] = true;
                }
            } else {
                $package_code_map[$f_id] = true;
            }
        }

        // 2. Resolve Entitlements
        $entitlements_input = $args['entitlements'] ?? null;
        if ($entitlements_input === null && function_exists('get_option')) {
            $stored_claims = get_option('carmilla_license_claims', null);
            if (!empty($stored_claims) && is_array($stored_claims)) {
                $entitlements_input = $stored_claims;
            }
        }

        $entitled_map = [];
        if ($entitlements_input === null) {
            // Default: fully unlocked claims for development / runtime fallback
            $claims       = EntitlementClaims::get_fully_unlocked_fixture();
            $entitled_map = EntitlementClaims::resolve_effective_features($claims);
        } elseif (is_array($entitlements_input) && (isset($entitlements_input['skus']) || isset($entitlements_input['schemaVersion']) || isset($entitlements_input['license_key']))) {
            if (isset($entitlements_input['status']) && $entitlements_input['status'] === 'revoked') {
                foreach ($manifest as $f_id => $meta) {
                    $entitled_map[$f_id] = false;
                }
            } else {
                $entitled_map = EntitlementClaims::resolve_effective_features($entitlements_input);
            }
        } elseif (is_array($entitlements_input)) {
            foreach ($manifest as $f_id => $meta) {
                $canonical_f = EntitlementSchema::resolve_feature_id($f_id);
                if (isset($entitlements_input[$f_id])) {
                    $entitled_map[$f_id] = (bool) $entitlements_input[$f_id];
                } elseif (isset($entitlements_input[$canonical_f])) {
                    $entitled_map[$f_id] = (bool) $entitlements_input[$canonical_f];
                } else {
                    $entitled_map[$f_id] = false;
                }
            }
        } else {
            foreach ($manifest as $f_id => $meta) {
                $entitled_map[$f_id] = false;
            }
        }

        // 3. Resolve Release Gates (releaseReady !== false)
        $release_input = $args['release_catalog'] ?? null;
        if ($release_input === null && function_exists('get_option')) {
            $stored_release = get_option('carmilla_release_catalog', null);
            if (!empty($stored_release) && is_array($stored_release)) {
                $release_input = $stored_release;
            }
        }
        $release_map   = [];
        foreach ($manifest as $f_id => $meta) {
            $canonical_f = EntitlementSchema::resolve_feature_id($f_id);
            if ($release_input !== null && is_array($release_input)) {
                $val = null;
                if (array_key_exists($f_id, $release_input)) {
                    $val = $release_input[$f_id];
                } elseif (array_key_exists($canonical_f, $release_input)) {
                    $val = $release_input[$canonical_f];
                }

                if ($val !== null) {
                    if (is_array($val) && isset($val['releaseReady'])) {
                        $release_map[$f_id] = (bool) $val['releaseReady'];
                    } elseif (is_array($val) && isset($val['release_ready'])) {
                        $release_map[$f_id] = (bool) $val['release_ready'];
                    } else {
                        $release_map[$f_id] = (bool) $val;
                    }
                } else {
                    $release_map[$f_id] = $meta['releaseReady'] ?? true;
                }
            } else {
                $release_map[$f_id] = $meta['releaseReady'] ?? true;
            }
        }

        // 4. Resolve Admin Settings (toggles)
        $admin_input      = $args['admin_settings'] ?? null;
        $admin_toggle_map = [];
        if ($admin_input === null && function_exists('get_option')) {
            $saved_options = get_option('cb_manifest_features', null);
            if (is_array($saved_options)) {
                $admin_input = $saved_options;
            }
        }

        foreach ($manifest as $f_id => $meta) {
            $canonical_f = EntitlementSchema::resolve_feature_id($f_id);
            if ($admin_input !== null && is_array($admin_input)) {
                if (array_key_exists($f_id, $admin_input)) {
                    $admin_toggle_map[$f_id] = (bool) $admin_input[$f_id];
                } elseif (array_key_exists($canonical_f, $admin_input)) {
                    $admin_toggle_map[$f_id] = (bool) $admin_input[$canonical_f];
                } else {
                    // Feature omitted from admin input retains its manifest default
                    $admin_toggle_map[$f_id] = (bool) ($meta['default'] ?? false);
                }
            } else {
                // Unconfigured admin settings: Base UI defaults to true, verticals default to true if entitled
                $is_base = ($meta['domain'] ?? '') === 'content' || ($meta['default'] ?? false);
                $admin_toggle_map[$f_id] = $is_base ? true : !empty($entitled_map[$f_id]);
            }
        }

        // 5. Resolve Environment Constraints
        $env_input             = $args['environment'] ?? [];
        $env_woocommerce_active = array_key_exists('woocommerce_active', $env_input)
            ? (bool) $env_input['woocommerce_active']
            : (class_exists('WooCommerce') || defined('WC_VERSION'));

        // 6. First Pass: Compute Initial Status per feature
        $feature_rows = [];
        foreach ($manifest as $f_id => $meta) {
            $code_present = !empty($package_code_map[$f_id]);
            $entitled     = !empty($entitled_map[$f_id]);
            $release_ready = !empty($release_map[$f_id]);
            $admin_enabled = !empty($admin_toggle_map[$f_id]);

            $status               = self::STATUS_ACTIVE;
            $reason               = 'قابلیت فعال و آماده استفاده است.';
            $is_active            = true;
            $missing_prerequisites = [];

            // Gate 1: Code presence in package
            if (!$code_present) {
                $status    = self::STATUS_MISSING_CODE;
                $reason    = 'کد این قابلیت در بسته تحویلی وجود ندارد (نیازمند ارتقای بسته).';
                $is_active = false;
            }
            // Gate 2: Entitlement / license purchase
            elseif (!$entitled) {
                $status    = self::STATUS_NOT_ENTITLED;
                $reason    = 'این قابلیت در مجوز یا بسته خریداری‌شده سایت قرار ندارد.';
                $is_active = false;
            }
            // Gate 3: Release Readiness Gate
            elseif (!$release_ready) {
                $status    = self::STATUS_RELEASE_GATE_RESTRICTED;
                $reason    = 'این قابلیت در این نسخه هنوز آماده انتشار نیست (محدودیت گیت انتشار).';
                $is_active = false;
            }
            // Gate 4: Admin Toggle (disabled by admin)
            elseif (!$admin_enabled) {
                $status    = self::STATUS_DISABLED_BY_ADMIN;
                $reason    = 'این قابلیت توسط مدیر در تنظیمات خاموش شده است.';
                $is_active = false;
            }

            $feature_rows[$f_id] = [
                'feature_id'            => $f_id,
                'is_active'             => $is_active,
                'status'                => $status,
                'reason'                => $reason,
                'code_present'          => $code_present,
                'entitled'              => $entitled,
                'admin_enabled'         => $admin_enabled,
                'release_ready'         => $release_ready,
                'prerequisites_met'     => true,
                'missing_prerequisites' => $missing_prerequisites,
            ];
        }

        // 7. Check External Prerequisites (e.g. WooCommerce for commerce.*)
        $commerce_features = ['commerce.core', 'commerce.cart', 'commerce.payment', 'commerce.wallet'];
        if (!$env_woocommerce_active) {
            foreach ($commerce_features as $c_feat) {
                if (isset($feature_rows[$c_feat])) {
                    if ($feature_rows[$c_feat]['is_active']) {
                        $feature_rows[$c_feat]['is_active']             = false;
                        $feature_rows[$c_feat]['status']                = self::STATUS_PREREQUISITE_MISSING;
                        $feature_rows[$c_feat]['reason']                = 'پیش‌نیاز خارجی ووکامرس (WooCommerce) فعال نیست.';
                        $feature_rows[$c_feat]['prerequisites_met']     = false;
                        $feature_rows[$c_feat]['missing_prerequisites'][] = 'woocommerce';
                    }
                }
            }
        }

        // 8. Recursive Pass: Propagate internal dependencies (multi-pass relaxation)
        $changed        = true;
        $max_iterations = 10;
        $iteration      = 0;

        while ($changed && $iteration < $max_iterations) {
            $changed = false;
            $iteration++;

            foreach ($feature_rows as $f_id => $row) {
                if (!$row['is_active']) {
                    continue;
                }

                $requires = $manifest[$f_id]['requires'] ?? [];
                foreach ($requires as $req_id) {
                    $canonical_req = EntitlementSchema::resolve_feature_id($req_id);
                    $parent_active = !empty($feature_rows[$canonical_req]['is_active']);

                    if (!$parent_active) {
                        $feature_rows[$f_id]['is_active']             = false;
                        $feature_rows[$f_id]['status']                = self::STATUS_PREREQUISITE_MISSING;
                        $feature_rows[$f_id]['reason']                = sprintf('پیش‌نیاز «%s» فعال نیست.', $canonical_req);
                        $feature_rows[$f_id]['prerequisites_met']     = false;
                        if (!in_array($canonical_req, $feature_rows[$f_id]['missing_prerequisites'], true)) {
                            $feature_rows[$f_id]['missing_prerequisites'][] = $canonical_req;
                        }
                        $changed = true;
                    }
                }
            }
        }

        // 9. Build Effective Features Map (including legacy aliases)
        $effective_features = [];
        foreach ($feature_rows as $f_id => $row) {
            $effective_features[$f_id] = $row['is_active'];
        }
        foreach (EntitlementSchema::FEATURE_ALIASES as $alias => $canonical) {
            if (isset($effective_features[$canonical])) {
                $effective_features[$alias] = $effective_features[$canonical];
            }
        }

        // 10. Compute Summary Statistics
        $summary = [
            'total'                     => count($feature_rows),
            'active_count'              => 0,
            'disabled_count'            => 0,
            'missing_prereq_count'      => 0,
            'not_entitled_count'        => 0,
            'missing_code_count'        => 0,
            'release_restricted_count'  => 0,
        ];

        foreach ($feature_rows as $row) {
            switch ($row['status']) {
                case self::STATUS_ACTIVE:
                    $summary['active_count']++;
                    break;
                case self::STATUS_DISABLED_BY_ADMIN:
                    $summary['disabled_count']++;
                    break;
                case self::STATUS_PREREQUISITE_MISSING:
                    $summary['missing_prereq_count']++;
                    break;
                case self::STATUS_NOT_ENTITLED:
                    $summary['not_entitled_count']++;
                    break;
                case self::STATUS_MISSING_CODE:
                    $summary['missing_code_count']++;
                    break;
                case self::STATUS_RELEASE_GATE_RESTRICTED:
                    $summary['release_restricted_count']++;
                    break;
            }
        }

        return [
            'features'           => $feature_rows,
            'effective_features' => $effective_features,
            'summary'            => $summary,
        ];
    }

    /**
     * Resolves and returns flat map of effective feature flags.
     *
     * @param array $args Same arguments as resolve_matrix().
     * @return array<string, bool> Map of canonical feature id and legacy aliases to boolean.
     */
    public static function resolve_effective(array $args = []): array {
        $matrix = self::resolve_matrix($args);
        return $matrix['effective_features'];
    }

    /**
     * Checks if a single feature is active and entitled.
     *
     * @param string $feature_id Feature ID or alias.
     * @param array  $args       Optional matrix arguments.
     * @return bool
     */
    public static function is_feature_active(string $feature_id, array $args = []): bool {
        $canonical = EntitlementSchema::resolve_feature_id($feature_id);
        $effective = self::resolve_effective($args);
        return !empty($effective[$canonical]);
    }

    /**
     * Returns detailed status record for a single feature.
     *
     * @param string $feature_id Feature ID or alias.
     * @param array  $args       Optional matrix arguments.
     * @return array|null
     */
    public static function get_feature_status(string $feature_id, array $args = []): ?array {
        $canonical = EntitlementSchema::resolve_feature_id($feature_id);
        $matrix    = self::resolve_matrix($args);
        return $matrix['features'][$canonical] ?? null;
    }

    /**
     * Identifies invalid child features configured by admin when required parents are disabled.
     * Used for form validation in admin controllers.
     *
     * @param array $configured Map of feature_id => bool.
     * @return array<string> List of child feature identifiers with unsatisfied dependencies.
     */
    public static function get_dependency_violations(array $configured): array {
        $deps       = self::get_canonical_dependencies();
        $violations = [];

        foreach ($deps as $child => $parents) {
            $is_child_configured = !empty($configured[$child]);
            if (!$is_child_configured) {
                // Check if alias was used
                $canonical_child = EntitlementSchema::resolve_feature_id($child);
                $is_child_configured = !empty($configured[$canonical_child]);
            }

            if (!$is_child_configured) {
                continue;
            }

            foreach ($parents as $parent) {
                $canonical_parent   = EntitlementSchema::resolve_feature_id($parent);
                $is_parent_present = !empty($configured[$parent]) || !empty($configured[$canonical_parent]);

                if (!$is_parent_present) {
                    $violations[] = $child;
                    break;
                }
            }
        }

        return array_values(array_unique($violations));
    }

    /**
     * Returns canonical dependencies map for admin validation.
     *
     * @return array<string, array<string>>
     */
    public static function get_canonical_dependencies(): array {
        $manifest = EntitlementSchema::get_feature_manifest();
        $deps     = [];

        foreach ($manifest as $f_id => $meta) {
            if (!empty($meta['requires'])) {
                $deps[$f_id] = $meta['requires'];
            }
        }

        // Include legacy aliases for backward compatibility with older controller tests
        $deps['commerce.physical'] = ['commerce.core'];
        $deps['commerce.digital']  = ['commerce.core'];
        $deps['academy.quiz']       = ['academy.core'];
        $deps['academy.certificate']= ['academy.core'];
        $deps['clinic.messaging']   = ['clinic.booking'];
        $deps['wallet']             = ['commerce.core'];

        return $deps;
    }

    /**
     * Returns default feature enabled states for admin UI and client manifest.
     *
     * @return array<string, bool>
     */
    public static function get_feature_defaults(): array {
        $manifest = EntitlementSchema::get_feature_manifest();
        $defaults = [];

        foreach ($manifest as $f_id => $meta) {
            $defaults[$f_id] = (bool) ($meta['default'] ?? false);
        }

        // Include common legacy aliases for manifest controller compatibility
        $defaults['content.blog']        = true;
        $defaults['commerce.physical']   = true;
        $defaults['commerce.digital']    = false;
        $defaults['academy.quiz']        = false;
        $defaults['academy.certificate'] = false;
        $defaults['clinic.messaging']    = false;
        $defaults['wallet']              = false;
        $defaults['admin.mobile']        = false;

        return $defaults;
    }

    /**
     * Returns canonical test fixtures covering all status codes and edge cases according to ADR-006.
     *
     * @return array<string, array>
     */
    public static function get_test_fixtures(): array {
        return [
            'base_ui_only' => [
                'description'       => 'Theme-only or Plugin-only Base UI with zero commercial verticals',
                'package_code'      => null,
                'entitlements'      => EntitlementClaims::get_base_theme_fixture(),
                'admin_settings'    => null,
                'environment'       => ['woocommerce_active' => false],
                'expected_statuses' => [
                    'theme.base'    => self::STATUS_ACTIVE,
                    'content.core'  => self::STATUS_ACTIVE,
                    'commerce.core' => self::STATUS_NOT_ENTITLED,
                    'academy.core'  => self::STATUS_NOT_ENTITLED,
                    'clinic.booking'=> self::STATUS_NOT_ENTITLED,
                ],
            ],
            'admin_toggle_unpurchased' => [
                'description'       => 'Admin toggles unpurchased commerce and academy features ON',
                'package_code'      => null,
                'entitlements'      => EntitlementClaims::get_base_theme_fixture(),
                'admin_settings'    => ['commerce.core' => true, 'academy.core' => true],
                'environment'       => ['woocommerce_active' => true],
                'expected_statuses' => [
                    'commerce.core' => self::STATUS_NOT_ENTITLED,
                    'academy.core'  => self::STATUS_NOT_ENTITLED,
                ],
            ],
            'missing_code_package' => [
                'description'       => 'License purchased but code omitted from package ZIP',
                'package_code'      => ['academy.core' => false, 'clinic.records' => false],
                'entitlements'      => EntitlementClaims::get_fully_unlocked_fixture(),
                'admin_settings'    => ['academy.core' => true, 'clinic.records' => true],
                'environment'       => ['woocommerce_active' => true],
                'expected_statuses' => [
                    'academy.core'   => self::STATUS_MISSING_CODE,
                    'clinic.records' => self::STATUS_MISSING_CODE,
                ],
            ],
            'disabled_by_admin' => [
                'description'       => 'Purchased and code present, but admin explicitly toggled off',
                'package_code'      => null,
                'entitlements'      => EntitlementClaims::get_fully_unlocked_fixture(),
                'admin_settings'    => ['clinic.booking' => false, 'clinic.therapists' => true, 'psych.tests' => false],
                'environment'       => ['woocommerce_active' => true],
                'expected_statuses' => [
                    'clinic.booking'    => self::STATUS_DISABLED_BY_ADMIN,
                    'psych.tests'       => self::STATUS_DISABLED_BY_ADMIN,
                    'clinic.therapists' => self::STATUS_PREREQUISITE_MISSING,
                ],
            ],
            'missing_woocommerce' => [
                'description'       => 'WooCommerce absent: restricts ONLY commerce.*, not other verticals or base UI',
                'package_code'      => null,
                'entitlements'      => EntitlementClaims::get_fully_unlocked_fixture(),
                'admin_settings'    => null,
                'environment'       => ['woocommerce_active' => false],
                'expected_statuses' => [
                    'commerce.core'    => self::STATUS_PREREQUISITE_MISSING,
                    'commerce.cart'    => self::STATUS_PREREQUISITE_MISSING,
                    'commerce.payment' => self::STATUS_PREREQUISITE_MISSING,
                    'commerce.wallet'  => self::STATUS_PREREQUISITE_MISSING,
                    'theme.base'       => self::STATUS_ACTIVE,
                    'content.core'     => self::STATUS_ACTIVE,
                    'academy.core'     => self::STATUS_ACTIVE,
                    'clinic.booking'   => self::STATUS_ACTIVE,
                    'psych.tests'      => self::STATUS_ACTIVE,
                    'builder.core'     => self::STATUS_ACTIVE,
                ],
            ],
            'release_gate_restricted' => [
                'description'       => 'Feature gated by releaseReady = false cannot be activated by admin toggle',
                'package_code'      => null,
                'entitlements'      => EntitlementClaims::get_fully_unlocked_fixture(),
                'admin_settings'    => ['clinic.consultation' => true],
                'release_catalog'   => ['clinic.consultation' => false],
                'environment'       => ['woocommerce_active' => true],
                'expected_statuses' => [
                    'clinic.consultation' => self::STATUS_RELEASE_GATE_RESTRICTED,
                ],
            ],
            'standalone_theme_no_bridge' => [
                'description'       => 'Theme standalone without Bridge plugin: zero prerequisite failures',
                'package_code'      => null,
                'entitlements'      => EntitlementClaims::get_fully_unlocked_fixture(),
                'admin_settings'    => null,
                'environment'       => ['woocommerce_active' => true, 'is_bridge_active' => false, 'is_theme_active' => true],
                'expected_statuses' => [
                    'theme.base'       => self::STATUS_ACTIVE,
                    'commerce.core'    => self::STATUS_ACTIVE,
                    'clinic.booking'   => self::STATUS_ACTIVE,
                    'academy.core'     => self::STATUS_ACTIVE,
                ],
            ],
            'standalone_plugin_no_theme' => [
                'description'       => 'Plugin standalone on 3rd party theme: zero prerequisite failures',
                'package_code'      => null,
                'entitlements'      => EntitlementClaims::get_fully_unlocked_fixture(),
                'admin_settings'    => null,
                'environment'       => ['woocommerce_active' => true, 'is_bridge_active' => true, 'is_theme_active' => false],
                'expected_statuses' => [
                    'plugin.base'      => self::STATUS_ACTIVE,
                    'commerce.core'    => self::STATUS_ACTIVE,
                    'clinic.booking'   => self::STATUS_ACTIVE,
                    'academy.core'     => self::STATUS_ACTIVE,
                ],
            ],
        ];
    }
}
