<?php
namespace Carmilla\Core\Capabilities;

/**
 * Shared Capability Registry
 *
 * Defines canonical WordPress capabilities across business domains:
 * content, shop, education, clinic, support, and admin.
 *
 * ARCHITECTURAL PRINCIPLE:
 * Strictly decouples user permissions (RBAC) from purchase entitlements (SKU license)
 * and feature toggles (Admin settings).
 * - Site Entitlement: Defines whether the site legally purchased the feature.
 * - Feature Toggle: Defines whether the site admin enabled the feature in UI/API.
 * - User Capability: Defines whether an individual user role has permission to execute an action.
 * A regular user (e.g. Subscriber) NEVER gains administrative access merely because
 * a feature is purchased or enabled.
 *
 * Part of Task P04-WPPLUGIN-CODE-010.
 */
class CapabilityRegistry {

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
     * Registers all Carmilla custom capabilities to the WordPress 'administrator' role.
     *
     * @return void
     */
    public static function register_capabilities(): void {
        if (!function_exists('get_role')) {
            return;
        }

        $admin_role = get_role('administrator');
        if (!$admin_role) {
            return;
        }

        foreach (self::DOMAIN_CAPS as $domain => $caps) {
            foreach ($caps as $action => $cap_name) {
                $admin_role->add_cap($cap_name);
            }
        }
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
                // If administrator, standard super-admin check
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
        return array_values(array_unique($all_caps));
    }
}
