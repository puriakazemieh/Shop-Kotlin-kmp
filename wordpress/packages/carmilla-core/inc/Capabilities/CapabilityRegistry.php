<?php
namespace Carmilla\Core\Capabilities;

/**
 * Shared Capability Registry & Role Access Manager
 *
 * Defines canonical WordPress capabilities and roles across business domains:
 * content, shop, education, clinic, support, and admin.
 *
 * ARCHITECTURAL PRINCIPLE (Tri-Layer Decoupled Security Model):
 * Strictly decouples:
 * 1. Site Entitlement (SKU License): Legal right of the site to run the feature.
 * 2. Feature Toggle (Admin Setting): Operational switch in site options.
 * 3. User Capability (RBAC): Individual user role permissions (current_user_can).
 *
 * CRITICAL SECURITY INVARIANT:
 * Purchasing or enabling a feature NEVER grants a normal user (Subscriber, Customer)
 * administrative privileges or access to other users' private data.
 *
 * Part of Task P04-WPPLUGIN-CODE-010.
 */
if ( ! class_exists( __NAMESPACE__ . '\CapabilityRegistry', false ) ) {

class CapabilityRegistry {

    /** Capability required to configure messaging providers and send test messages. */
    public const MANAGE_INTEGRATIONS = 'manage_carmilla_integrations';

    /**
     * Canonical business domains.
     */
    public const DOMAIN_CONTENT   = 'content';
    public const DOMAIN_SHOP      = 'shop';
    public const DOMAIN_EDUCATION = 'education';
    public const DOMAIN_CLINIC    = 'clinic';
    public const DOMAIN_SUPPORT   = 'support';
    public const DOMAIN_ADMIN     = 'admin';

    /**
     * Mapping of domains to primary canonical feature identifiers in EntitlementSchema.
     *
     * @var array<string, string>
     */
    public const DOMAIN_PRIMARY_FEATURES = [
        'content'   => 'content.core',
        'shop'      => 'commerce.core',
        'education' => 'academy.core',
        'clinic'    => 'clinic.booking',
        'support'   => 'content.support',
        'admin'     => 'theme.base',
    ];

    /**
     * Mapping of domains to WordPress capabilities.
     *
     * Domains:
     * - content
     * - shop
     * - education
     * - clinic
     * - support
     * - admin
     *
     * @var array<string, array<string, string>>
     */
    public const DOMAIN_CAPS = [
        'content'   => [
            'manage' => 'carmilla_manage_content',
            'read'   => 'carmilla_read_content',
            'edit'   => 'carmilla_edit_content',
            'delete' => 'carmilla_delete_content',
        ],
        'shop'      => [
            'manage' => 'carmilla_manage_shop',
            'read'   => 'carmilla_view_orders',
            'edit'   => 'carmilla_edit_products',
            'delete' => 'carmilla_delete_products',
        ],
        'education' => [
            'manage' => 'carmilla_manage_courses',
            'read'   => 'carmilla_view_courses',
            'edit'   => 'carmilla_edit_courses',
            'delete' => 'carmilla_delete_courses',
        ],
        'clinic'    => [
            'manage' => 'carmilla_manage_appointments',
            'read'   => 'carmilla_view_appointments',
            'edit'   => 'carmilla_edit_appointments',
            'delete' => 'carmilla_delete_appointments',
        ],
        'support'   => [
            'manage' => 'carmilla_manage_tickets',
            'read'   => 'carmilla_view_tickets',
            'edit'   => 'carmilla_reply_tickets',
            'delete' => 'carmilla_delete_tickets',
        ],
        'admin'     => [
            'manage' => 'carmilla_manage_settings',
            'read'   => 'carmilla_view_settings',
            'edit'   => 'carmilla_manage_licenses',
            'delete' => 'carmilla_reset_system',
        ],
    ];

    /**
     * Role definitions with least-privilege capability mapping.
     * Crucial: NONE of these domain roles receive `manage_options`, `carmilla_manage_settings`,
     * or cross-domain admin capabilities.
     *
     * @var array<string, array{display_name: string, capabilities: array<string, bool>}>
     */
    public const ROLES = [
        'carmilla_instructor' => [
            'display_name' => 'مدرس کارمیلا (Carmilla Instructor)',
            'capabilities' => [
                'read'                   => true,
                'upload_files'           => true,
                'carmilla_read_content'  => true,
                'carmilla_view_courses'  => true,
                'carmilla_edit_courses'  => true,
                // Explicitly forbidden:
                'manage_options'               => false,
                'carmilla_manage_settings'     => false,
                'carmilla_manage_licenses'     => false,
                'carmilla_manage_courses'      => false,
                'carmilla_delete_courses'      => false,
                'carmilla_manage_shop'         => false,
                'carmilla_manage_appointments' => false,
            ],
        ],
        'carmilla_therapist' => [
            'display_name' => 'متخصص کارمیلا (Carmilla Therapist)',
            'capabilities' => [
                'read'                         => true,
                'carmilla_read_content'        => true,
                'carmilla_view_appointments'   => true,
                'carmilla_edit_appointments'   => true,
                // Explicitly forbidden:
                'manage_options'               => false,
                'carmilla_manage_settings'     => false,
                'carmilla_manage_licenses'     => false,
                'carmilla_manage_appointments' => false,
                'carmilla_delete_appointments' => false,
                'carmilla_manage_courses'      => false,
                'carmilla_manage_shop'         => false,
            ],
        ],
        'carmilla_support_agent' => [
            'display_name' => 'پشتیبان کارمیلا (Carmilla Support Agent)',
            'capabilities' => [
                'read'                     => true,
                'carmilla_read_content'    => true,
                'carmilla_view_tickets'    => true,
                'carmilla_reply_tickets'   => true,
                // Explicitly forbidden:
                'manage_options'           => false,
                'carmilla_manage_settings' => false,
                'carmilla_manage_licenses' => false,
                'carmilla_manage_tickets'  => false,
                'carmilla_delete_tickets'  => false,
                'carmilla_manage_courses'  => false,
                'carmilla_manage_shop'     => false,
            ],
        ],
        'carmilla_shop_manager' => [
            'display_name' => 'مدیر فروشگاه کارمیلا (Carmilla Shop Manager)',
            'capabilities' => [
                'read'                     => true,
                'upload_files'             => true,
                'carmilla_read_content'    => true,
                'carmilla_manage_shop'     => true,
                'carmilla_view_orders'     => true,
                'carmilla_edit_products'   => true,
                'carmilla_delete_products' => true,
                // Explicitly forbidden:
                'manage_options'               => false,
                'carmilla_manage_settings'     => false,
                'carmilla_manage_licenses'     => false,
                'carmilla_manage_courses'      => false,
                'carmilla_manage_appointments' => false,
            ],
        ],
    ];

    /**
     * Registers all Carmilla custom capabilities to the WordPress 'administrator' role
     * and WooCommerce 'shop_manager' role if present.
     *
     * @return void
     */
    public static function register_capabilities(): void {
        if (!function_exists('get_role')) {
            return;
        }

        $admin_role = get_role('administrator');
        if ($admin_role) {
            foreach (self::DOMAIN_CAPS as $domain => $caps) {
                foreach ($caps as $action => $cap_name) {
                    $admin_role->add_cap($cap_name);
                }
            }
            $admin_role->add_cap(self::MANAGE_INTEGRATIONS);
        }

        // Add commerce caps to WooCommerce shop_manager if it exists
        $shop_role = get_role('shop_manager');
        if ($shop_role) {
            foreach (self::DOMAIN_CAPS['shop'] as $action => $cap_name) {
                $shop_role->add_cap($cap_name);
            }
            $shop_role->add_cap(self::DOMAIN_CAPS['content']['read']);
        }
    }

    /**
     * Registers canonical Carmilla domain roles in WordPress with strict least privilege.
     *
     * @return void
     */
    public static function register_roles(): void {
        if (!function_exists('add_role') || !function_exists('get_role')) {
            return;
        }

        foreach (self::ROLES as $role_slug => $role_info) {
            $existing = get_role($role_slug);
            if (!$existing) {
                // Filter only true grants for add_role
                $granted_caps = array_filter($role_info['capabilities']);
                add_role($role_slug, $role_info['display_name'], $granted_caps);
            } else {
                // Ensure canonical capabilities are present and forbidden ones removed
                foreach ($role_info['capabilities'] as $cap => $grant) {
                    if ($grant) {
                        $existing->add_cap($cap);
                    } else {
                        $existing->remove_cap($cap);
                    }
                }
                // Enforce zero administrative elevation
                $existing->remove_cap('manage_options');
                $existing->remove_cap('carmilla_manage_settings');
                $existing->remove_cap('carmilla_manage_licenses');
            }
        }
    }

    /**
     * Removes canonical Carmilla roles from WordPress (useful for testing and reset).
     *
     * @return void
     */
    public static function unregister_roles(): void {
        if (!function_exists('remove_role')) {
            return;
        }
        foreach (array_keys(self::ROLES) as $role_slug) {
            remove_role($role_slug);
        }
    }

    /**
     * Returns the capability definitions for a specific Carmilla role.
     *
     * @param string $role_slug
     * @return array<string, bool>
     */
    public static function get_role_capabilities(string $role_slug): array {
        return self::ROLES[$role_slug]['capabilities'] ?? [];
    }

    /**
     * Checks if a user has permission for a specific domain action.
     *
     * @param int|\WP_User|null $user_id User ID or WP_User object. If null, current user is checked.
     * @param string            $domain  Domain slug (content, shop, education, clinic, support, admin).
     * @param string            $action  Action key ('read', 'manage', 'edit', 'delete') or specific capability string.
     * @return bool True if user possesses the required capability, false otherwise.
     */
    public static function user_can_for_domain($user_id, string $domain, string $action = 'read'): bool {
        // Resolve target capability
        $target_cap = self::resolve_domain_cap($domain, $action);
        if (empty($target_cap)) {
            return false;
        }

        // Check for current user if user_id is null/0
        if (empty($user_id)) {
            if (function_exists('current_user_can')) {
                // Super-admin / manage_options override
                if (current_user_can('manage_options') || current_user_can($target_cap)) {
                    return true;
                }
                return false;
            }
            return false;
        }

        // Specific user check
        if (function_exists('user_can')) {
            if (user_can($user_id, 'manage_options') || user_can($user_id, $target_cap)) {
                return true;
            }
            return false;
        }

        return false;
    }

    /**
     * Resolves domain and action into a registered capability name.
     *
     * @param string $domain Domain name.
     * @param string $action Action key or full capability name.
     * @return string|null Capability name or null if invalid domain/action.
     */
    public static function resolve_domain_cap(string $domain, string $action): ?string {
        // If action is already a known capability string, return directly
        if (strpos($action, 'carmilla_') === 0) {
            return $action;
        }

        if (!isset(self::DOMAIN_CAPS[$domain])) {
            return null;
        }

        $domain_actions = self::DOMAIN_CAPS[$domain];
        if (isset($domain_actions[$action])) {
            return $domain_actions[$action];
        }

        // Fall back to default 'manage' capability if action not explicitly defined
        return $domain_actions['manage'] ?? null;
    }

    /**
     * Returns all registered capabilities across all domains.
     *
     * @return array<string> List of unique capability strings.
     */
    public static function get_all_capabilities(): array {
        $all_caps = [];
        foreach (self::DOMAIN_CAPS as $domain => $caps) {
            foreach ($caps as $cap) {
                $all_caps[] = $cap;
            }
        }
        $all_caps[] = self::MANAGE_INTEGRATIONS;
        return array_values(array_unique($all_caps));
    }

    /**
     * Tri-Layer Decoupled Access Control Gate
     *
     * Evaluates access according to the strict 3-layer architecture:
     * - Layer 1: Site Entitlement (Did the site legally purchase this feature SKU?)
     * - Layer 2: Feature Toggle & Environment (Is the feature enabled by admin & prerequisites present?)
     * - Layer 3: User Capability / RBAC (Does this user role possess the required capability?)
     * - Layer 4: Private Data & Ownership (Does the user own the target resource or have manager override?)
     *
     * @param int|\WP_User|null $user_id    User ID or WP_User object. 0 or null for unauthenticated.
     * @param string            $domain     Domain slug ('content', 'shop', 'education', 'clinic', 'support', 'admin').
     * @param string            $action     Action key ('read', 'manage', 'edit', 'delete') or specific capability name.
     * @param string|null       $feature_id Canonical feature ID or alias. If null, mapped from domain.
     * @param array             $context    Optional context flags:
     *                                      - 'owner_id' / 'resource_owner_id': (int) owner ID for private data checks.
     *                                      - 'feature_matrix': (array) precomputed capability matrix for synthetic tests.
     *                                      - 'is_public': (bool) whether this specific action allows unauthenticated access.
     * @return array{
     *     allowed: bool,
     *     code: string,
     *     status: int,
     *     layer: string,
     *     reason: string,
     *     feature: string
     * }
     */
    public static function check_access(
        $user_id,
        string $domain,
        string $action = 'read',
        ?string $feature_id = null,
        array $context = []
    ): array {
        // Resolve canonical feature identifier
        if (empty($feature_id)) {
            $feature_id = self::DOMAIN_PRIMARY_FEATURES[$domain] ?? 'platform.core';
        }

        $canonical_feature = class_exists('\Carmilla\Core\Catalog\EntitlementSchema')
            ? \Carmilla\Core\Catalog\EntitlementSchema::resolve_feature_id($feature_id)
            : $feature_id;

        // Obtain capability matrix
        $matrix = $context['feature_matrix'] ?? null;
        if ($matrix === null) {
            if (class_exists('\Carmilla_Kernel') && \Carmilla_Kernel::is_booted()) {
                $matrix = \Carmilla_Kernel::get_feature_matrix();
            } elseif (class_exists('\Carmilla\Core\Catalog\CapabilityResolver')) {
                $matrix = \Carmilla\Core\Catalog\CapabilityResolver::resolve_matrix();
            }
        }

        $feature_data = $matrix['features'][$canonical_feature] ?? null;

        // Base platform/theme/plugin features are inherently entitled
        $is_base_feature = in_array($canonical_feature, ['theme.base', 'plugin.base', 'platform.core'], true);

        // -------------------------------------------------------------
        // LAYER 1: Site Entitlement Gate (حق خرید سایت)
        // -------------------------------------------------------------
        if (!$is_base_feature && $feature_data !== null) {
            if (empty($feature_data['entitled'])) {
                return [
                    'allowed' => false,
                    'code'    => 'FEATURE_NOT_ENTITLED',
                    'status'  => 403,
                    'layer'   => 'entitlement',
                    'reason'  => 'این قابلیت در لایسنس یا بسته خریداری‌شده سایت قرار ندارد.',
                    'feature' => $canonical_feature,
                ];
            }
        }

        // -------------------------------------------------------------
        // LAYER 2: Feature Toggle & Environmental Prerequisites Gate (روشن‌بودن قابلیت)
        // -------------------------------------------------------------
        if ($feature_data !== null) {
            // Check status codes from CapabilityResolver
            $status = $feature_data['status'] ?? null;

            if ($status === 'DISABLED_BY_ADMIN' || (isset($feature_data['admin_enabled']) && !$feature_data['admin_enabled'])) {
                return [
                    'allowed' => false,
                    'code'    => 'FEATURE_DISABLED',
                    'status'  => 403,
                    'layer'   => 'toggle',
                    'reason'  => 'این قابلیت توسط مدیر در تنظیمات خاموش شده است.',
                    'feature' => $canonical_feature,
                ];
            }

            if ($status === 'PREREQUISITE_MISSING') {
                return [
                    'allowed' => false,
                    'code'    => 'PREREQUISITE_MISSING',
                    'status'  => 403,
                    'layer'   => 'prerequisite',
                    'reason'  => 'پیش‌نیازهای فنی لازم برای این قابلیت در سایت فعال نیستند.',
                    'feature' => $canonical_feature,
                ];
            }

            if ($status === 'RELEASE_GATE_RESTRICTED') {
                return [
                    'allowed' => false,
                    'code'    => 'RELEASE_GATE_RESTRICTED',
                    'status'  => 403,
                    'layer'   => 'release_gate',
                    'reason'  => 'این قابلیت در این نسخه هنوز آماده انتشار نیست.',
                    'feature' => $canonical_feature,
                ];
            }
        }

        // -------------------------------------------------------------
        // LAYER 3: User Capability / RBAC Gate (مجوز نقش کاربر)
        // -------------------------------------------------------------
        $is_public = !empty($context['is_public']);
        $user_id_num = is_object($user_id) ? (int)$user_id->ID : (int)$user_id;

        if (empty($user_id_num)) {
            if (!$is_public) {
                return [
                    'allowed' => false,
                    'code'    => 'UNAUTHENTICATED',
                    'status'  => 401,
                    'layer'   => 'rbac',
                    'reason'  => 'برای دسترسی به این بخش باید وارد حساب کاربری شوید.',
                    'feature' => $canonical_feature,
                ];
            }
        } else {
            // Authenticated user: check capability
            $has_cap = self::user_can_for_domain($user_id, $domain, $action);
            if (!$has_cap) {
                return [
                    'allowed' => false,
                    'code'    => 'FORBIDDEN',
                    'status'  => 403,
                    'layer'   => 'rbac',
                    'reason'  => 'شما مجوز کافی برای انجام این عملیات را ندارید.',
                    'feature' => $canonical_feature,
                ];
            }
        }

        // -------------------------------------------------------------
        // LAYER 4: Private Data & Resource Ownership Gate (مالکیت داده)
        // -------------------------------------------------------------
        $resource_owner_id = isset($context['resource_owner_id'])
            ? (int) $context['resource_owner_id']
            : (isset($context['owner_id']) ? (int) $context['owner_id'] : null);

        if ($resource_owner_id !== null && $resource_owner_id > 0) {
            // If current user is not the owner
            if ($user_id_num !== $resource_owner_id) {
                // Must possess domain 'manage' or super-admin capability to view/edit another user's private resource
                $can_manage = self::user_can_for_domain($user_id, $domain, 'manage');
                if (!$can_manage) {
                    return [
                        'allowed' => false,
                        'code'    => 'FORBIDDEN',
                        'status'  => 403,
                        'layer'   => 'ownership',
                        'reason'  => 'مشاهده یا ویرایش داده‌های خصوصی سایر کاربران مجاز نیست.',
                        'feature' => $canonical_feature,
                    ];
                }
            }
        }

        // All layers passed
        return [
            'allowed' => true,
            'code'    => 'OK',
            'status'  => 200,
            'layer'   => 'none',
            'reason'  => 'عملیات مجاز است.',
            'feature' => $canonical_feature,
        ];
    }

    /**
     * Generates a WordPress REST API permission callback enforcing the tri-layer security model.
     *
     * @param string      $domain     Domain slug.
     * @param string      $action     Action key.
     * @param string|null $feature_id Optional feature ID.
     * @param array       $context    Optional context flags.
     * @return callable
     */
    public static function get_permission_callback(
        string $domain,
        string $action = 'read',
        ?string $feature_id = null,
        array $context = []
    ): callable {
        return function ($request) use ($domain, $action, $feature_id, $context) {
            $user_id = function_exists('get_current_user_id') ? get_current_user_id() : 0;

            // Extract resource owner from request parameters if available
            if (is_object($request) && method_exists($request, 'get_param')) {
                $owner = $request->get_param('user_id') ?: $request->get_param('customer_id') ?: $request->get_param('owner_id');
                if ($owner && !isset($context['owner_id'])) {
                    $context['owner_id'] = (int) $owner;
                }
            }

            $result = self::check_access($user_id, $domain, $action, $feature_id, $context);

            if ($result['allowed']) {
                return true;
            }

            if (class_exists('\WP_Error')) {
                return new \WP_Error(
                    $result['code'],
                    $result['reason'],
                    [
                        'status'  => $result['status'],
                        'layer'   => $result['layer'],
                        'feature' => $result['feature'],
                    ]
                );
            }

            return false;
        };
    }
}
}

