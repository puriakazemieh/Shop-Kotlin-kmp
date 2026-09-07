<?php
/**
 * Smoke tests for Carmilla Core Kernel and Shared Classes:
 * 1. EntitlementResolver (P04-ENTITLEMENT-CODE-040)
 * 2. WooCommerceAdapter (P04-WPPLUGIN-CODE-008)
 * 3. RestInfrastructure (P04-WPPLUGIN-CODE-009)
 * 4. CapabilityRegistry (P04-WPPLUGIN-CODE-010)
 * 5. HealthChecker (P04-WPPLUGIN-CODE-011)
 *
 * Can be executed via CLI: php wordpress/packages/carmilla-core/tests/smoke-carmilla-core.php
 */

// Define mock WP / WC functions if running outside WordPress
if (!function_exists('get_site_url')) {
    function get_site_url() {
        return 'https://example.com';
    }
}
if (!function_exists('is_ssl')) {
    function is_ssl() {
        return true;
    }
}
if (!function_exists('get_option')) {
    function get_option($name, $default = false) {
        if ($name === 'permalink_structure') {
            return '/%postname%/';
        }
        return $default;
    }
}
if (!function_exists('rest_url')) {
    function rest_url() {
        return 'https://example.com/wp-json/';
    }
}
if (!function_exists('wp_verify_nonce')) {
    function wp_verify_nonce($nonce, $action = -1) {
        return $nonce === 'valid-test-nonce';
    }
}

// Load Carmilla Kernel (which loads all components)
require_once dirname(__DIR__) . '/Carmilla_Kernel.php';

use Carmilla\Core\Entitlement\EntitlementResolver;
use Carmilla\Core\Adapter\WooCommerceAdapter;
use Carmilla\Core\Rest\RestInfrastructure;
use Carmilla\Core\Capabilities\CapabilityRegistry;
use Carmilla\Core\Health\HealthChecker;
use Carmilla\Core\Catalog\EntitlementClaims;

$fail = 0;
function check($cond, $label) {
    global $fail;
    echo ($cond ? 'PASS' : 'FAIL') . "  $label\n";
    if (!$cond) {
        $fail++;
    }
}

echo "=== Testing Carmilla Core Shared Kernel Components ===\n\n";

// ==========================================
// 1. EntitlementResolver Tests
// ==========================================
echo "-- 1. EntitlementResolver Tests --\n";

$valid_claims = [
    'license_key' => 'carmilla-test-key-123',
    'status'      => 'active',
    'expires_at'  => time() + 3600,
    'site_url'    => 'https://example.com',
    'skus'        => ['sku_commerce', 'sku_academy'],
    'quotas'      => ['max_products' => 500]
];
check(EntitlementResolver::validate_license($valid_claims) === true, 'Valid license validates successfully');
check(EntitlementResolver::has_sku($valid_claims, 'sku_commerce') === true, 'has_sku finds sku_commerce');
check(EntitlementResolver::has_sku($valid_claims, 'sku_clinic') === false, 'has_sku returns false for unpurchased SKU');

// Wrong site rejection
$wrong_site_claims = $valid_claims;
$wrong_site_claims['site_url'] = 'https://malicious-site.org';
check(EntitlementResolver::validate_license($wrong_site_claims) === false, 'Wrong site binding is rejected');
$wrong_features = EntitlementResolver::resolve($wrong_site_claims);
check($wrong_features['commerce.core'] === false, 'Wrong site returns all-false features');

// Expired license rejection
$expired_claims = $valid_claims;
$expired_claims['expires_at'] = time() - 3600;
check(EntitlementResolver::validate_license($expired_claims) === false, 'Expired license is rejected');

// Tampered license rejection
$tampered_claims = $valid_claims;
$tampered_claims['tampered'] = true;
check(EntitlementResolver::validate_license($tampered_claims) === false, 'Tampered license is rejected');

// Inactive status rejection
$inactive_claims = $valid_claims;
$inactive_claims['status'] = 'revoked';
check(EntitlementResolver::validate_license($inactive_claims) === false, 'Revoked license is rejected');

// Quota deduplication in co-install
EntitlementResolver::reset_quota_cache();
$quota1 = EntitlementResolver::resolve_effective_quota($valid_claims, 'max_products');
$quota2 = EntitlementResolver::resolve_effective_quota($valid_claims, 'max_products');
check($quota1 === 500 && $quota2 === 500, 'Quota resolves correctly without doubling in co-install');

// ==========================================
// 2. WooCommerceAdapter Tests
// ==========================================
echo "\n-- 2. WooCommerceAdapter Tests --\n";

// In mock/test environment without WC loaded
check(WooCommerceAdapter::is_available() === false, 'is_available returns false when WooCommerce is not loaded');
check(WooCommerceAdapter::get_products() === [], 'get_products gracefully returns empty array without WooCommerce');
check(WooCommerceAdapter::get_product(1) === null, 'get_product returns null without WooCommerce');
check(WooCommerceAdapter::get_orders() === [], 'get_orders gracefully returns empty array without WooCommerce');
check(WooCommerceAdapter::get_order(10) === null, 'get_order returns null without WooCommerce');
check(WooCommerceAdapter::get_cart() === null, 'get_cart returns null without WooCommerce');
check(WooCommerceAdapter::add_to_cart(1, 1) === false, 'add_to_cart returns false without WooCommerce');
check(WooCommerceAdapter::is_hpos_enabled() === false, 'is_hpos_enabled returns false when WC is absent');

// ==========================================
// 3. RestInfrastructure Tests
// ==========================================
echo "\n-- 3. RestInfrastructure Tests --\n";

check(RestInfrastructure::NAMESPACE === 'carmilla/v1', 'RestInfrastructure::NAMESPACE equals carmilla/v1');

$err_resp = RestInfrastructure::error_response('not_found', 'Resource was not found', 404);
check(
    isset($err_resp['success']) && $err_resp['success'] === false &&
    isset($err_resp['error']['code']) && $err_resp['error']['code'] === 'not_found',
    'error_response produces standard {success: false, error: {code, message}} envelope'
);

$success_resp = RestInfrastructure::success_response(['item_id' => 42]);
check(
    isset($success_resp['success']) && $success_resp['success'] === true &&
    isset($success_resp['data']['item_id']) && $success_resp['data']['item_id'] === 42,
    'success_response produces standard {success: true, data: ...} envelope'
);

$paged_resp = RestInfrastructure::paginated_response(['item1', 'item2'], 50, 1, 10);
check(
    isset($paged_resp['pagination']) &&
    $paged_resp['pagination']['total'] === 50 &&
    $paged_resp['pagination']['total_pages'] === 5 &&
    $paged_resp['pagination']['page'] === 1 &&
    $paged_resp['pagination']['per_page'] === 10,
    'paginated_response produces correct pagination metadata'
);

// Nonce validation
$mock_valid_req = ['X-WP-Nonce' => 'valid-test-nonce'];
$mock_invalid_req = ['X-WP-Nonce' => 'invalid-nonce'];
check(RestInfrastructure::validate_nonce($mock_valid_req) === true, 'validate_nonce accepts valid nonce');
check(RestInfrastructure::validate_nonce($mock_invalid_req) === false, 'validate_nonce rejects invalid nonce');
check(RestInfrastructure::sanitize_per_page(999, 10, 100) === 100, 'sanitize_per_page clamps values to ceiling');

// ==========================================
// 4. CapabilityRegistry Tests
// ==========================================
echo "\n-- 4. CapabilityRegistry Tests --\n";

check(isset(CapabilityRegistry::DOMAIN_CAPS['content']), 'Domain content caps registered');
check(isset(CapabilityRegistry::DOMAIN_CAPS['shop']), 'Domain shop caps registered');
check(isset(CapabilityRegistry::DOMAIN_CAPS['education']), 'Domain education caps registered');
check(isset(CapabilityRegistry::DOMAIN_CAPS['clinic']), 'Domain clinic caps registered');
check(isset(CapabilityRegistry::DOMAIN_CAPS['support']), 'Domain support caps registered');
check(isset(CapabilityRegistry::DOMAIN_CAPS['admin']), 'Domain admin caps registered');

check(
    CapabilityRegistry::resolve_domain_cap('shop', 'read') === 'carmilla_view_orders',
    'resolve_domain_cap resolves shop read to carmilla_view_orders'
);
check(
    CapabilityRegistry::resolve_domain_cap('clinic', 'manage') === 'carmilla_manage_appointments',
    'resolve_domain_cap resolves clinic manage to carmilla_manage_appointments'
);

// ==========================================
// 5. HealthChecker Tests
// ==========================================
echo "\n-- 5. HealthChecker Tests --\n";

$https_check = HealthChecker::check_https();
check($https_check['status'] === 'pass', 'check_https passes under SSL');

$woo_not_purchased = HealthChecker::check_woocommerce(false);
check($woo_not_purchased['status'] === 'info', 'check_woocommerce reports info when commerce SKU is not purchased');

$woo_purchased_missing = HealthChecker::check_woocommerce(true);
check($woo_purchased_missing['status'] === 'fail', 'check_woocommerce fails with remediation when commerce SKU is purchased but WooCommerce is missing');
check(!empty($woo_purchased_missing['remediation']), 'Remediation guide provided for missing WooCommerce');

$perm_check = HealthChecker::check_permalinks();
check($perm_check['status'] === 'pass', 'check_permalinks passes with pretty permalinks');

$cron_check = HealthChecker::check_wp_cron();
check($cron_check['status'] === 'pass', 'check_wp_cron passes with default cron');

$diag = HealthChecker::get_diagnostics(['sku_commerce']);
check($diag['secrets_redacted'] === true, 'get_diagnostics reports secrets_redacted=true');
check(isset($diag['environment']['php_version']), 'Diagnostics contains PHP version');
check(isset($diag['checks']['woocommerce']), 'Diagnostics contains WooCommerce check result');

echo "\n==========================================\n";
echo ($fail === 0 ? "ALL SMOKE TESTS PASSED (0 failures)\n" : "$fail TESTS FAILED\n");
echo "==========================================\n";

exit($fail === 0 ? 0 : 1);
