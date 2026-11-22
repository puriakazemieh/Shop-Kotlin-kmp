<?php
/**
 * Dedicated Acceptance Test Suite for Task P04-ENTITLEMENT-DATA-039:
 * 1. Schema version and product kinds validation
 * 2. Feature catalog, controller coverage, and artifact inventory mapping
 * 3. Base package without verticals
 * 4. Booking package without psych requirement
 * 5. App builder targets isolation (4 booleans, missing=false, core requirement, no cross-privilege)
 * 6. Legacy aliases backward compatibility
 * 7. Dependency closure resolution
 * 8. Lifecycle states: offline, grace period (14d), revoked, site transfer, data preservation
 *
 * Can be executed via Docker:
 * docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/packages/carmilla-core/tests/test-entitlement-data-039.php
 */

if (!defined('ABSPATH')) {
    define('ABSPATH', __DIR__ . '/');
}
if (!function_exists('get_site_url')) {
    function get_site_url() {
        return 'https://example.com';
    }
}

require_once dirname(__DIR__) . '/Carmilla_Kernel.php';

use Carmilla\Core\Catalog\EntitlementSchema;
use Carmilla\Core\Catalog\EntitlementClaims;
use Carmilla\Core\Catalog\DependencyResolver;
use Carmilla\Core\Entitlement\EntitlementResolver;

$fail = 0;
function assert_test(bool $condition, string $description) {
    global $fail;
    echo ($condition ? 'PASS' : 'FAIL') . " - " . $description . "\n";
    if (!$condition) {
        $fail++;
    }
}

echo "=================================================================\n";
echo " Running Acceptance Tests for P04-ENTITLEMENT-DATA-039\n";
echo "=================================================================\n\n";

// -------------------------------------------------------------
// Test Group 1: Contract Schema and Catalog Fixtures
// -------------------------------------------------------------
echo "-- Group 1: Contract Schema & Catalog Files --\n";

$schema_file = dirname(__DIR__, 4) . '/docs/contracts/carmilla-entitlement-catalog-v1.schema.json';
assert_test(file_exists($schema_file), 'Schema JSON file exists under docs/contracts/');

$catalog_file = dirname(__DIR__, 4) . '/docs/contracts/carmilla-entitlement-catalog-v1.json';
assert_test(file_exists($catalog_file), 'Catalog JSON file exists under docs/contracts/');

$catalog_json = json_decode(file_get_contents($catalog_file), true);
assert_test(isset($catalog_json['schemaVersion']) && $catalog_json['schemaVersion'] === 1, 'Catalog schemaVersion is 1');
assert_test(in_array('theme', $catalog_json['productKinds'], true), 'productKinds includes theme');
assert_test(in_array('plugin', $catalog_json['productKinds'], true), 'productKinds includes plugin');
assert_test(in_array('client', $catalog_json['productKinds'], true), 'productKinds includes client');

// -------------------------------------------------------------
// Test Group 2: Base Package has ZERO Verticals
// -------------------------------------------------------------
echo "\n-- Group 2: Base Package Isolation (No Verticals) --\n";

$skus = EntitlementSchema::get_sku_inventory();
$theme_base = $skus['sku_theme_base'];
assert_test($theme_base['artifactKind'] === 'theme', 'Theme Base artifactKind is theme');
assert_test(!in_array('commerce.core', $theme_base['features'], true), 'Theme Base does not include commerce.core');
assert_test(!in_array('academy.core', $theme_base['features'], true), 'Theme Base does not include academy.core');
assert_test(!in_array('clinic.booking', $theme_base['features'], true), 'Theme Base does not include clinic.booking');
assert_test(!in_array('psych.tests', $theme_base['features'], true), 'Theme Base does not include psych.tests');
assert_test($theme_base['builders']['android'] === false, 'Theme Base android builder is false');
assert_test($theme_base['builders']['ios'] === false, 'Theme Base ios builder is false');

$plugin_base = $skus['sku_plugin_base'];
assert_test($plugin_base['artifactKind'] === 'plugin', 'Plugin Base artifactKind is plugin');
assert_test(!in_array('clinic.booking', $plugin_base['features'], true), 'Plugin Base does not include clinic.booking');
assert_test(!in_array('psych.tests', $plugin_base['features'], true), 'Plugin Base does not include psych.tests');

// -------------------------------------------------------------
// Test Group 3: Booking Package has NO Psych Requirement
// -------------------------------------------------------------
echo "\n-- Group 3: Booking Package Independence (No Psych Requirement) --\n";

$booking_claims = EntitlementClaims::get_booking_without_psych_fixture();
$resolved_booking = EntitlementClaims::resolve_effective_features($booking_claims);

assert_test($resolved_booking['clinic.booking'] === true, 'clinic.booking is true in Booking package');
assert_test($resolved_booking['clinic.therapists'] === true, 'clinic.therapists is true in Booking package');
assert_test($resolved_booking['psych.tests'] === false, 'psych.tests is strictly FALSE in Booking package');
assert_test($resolved_booking['psych.results'] === false, 'psych.results is strictly FALSE in Booking package');

// Check Psych package independence
$psych_claims = [
    'license_key' => 'fixture-psych',
    'status'      => 'active',
    'site_url'    => 'https://example.com',
    'skus'        => ['sku_psych']
];
$resolved_psych = EntitlementClaims::resolve_effective_features($psych_claims);
assert_test($resolved_psych['psych.tests'] === true, 'psych.tests is true in Psych package');
assert_test($resolved_psych['psych.results'] === true, 'psych.results is true in Psych package');
assert_test($resolved_psych['clinic.booking'] === false, 'clinic.booking is strictly FALSE in Psych package');

// -------------------------------------------------------------
// Test Group 4: App Builder Targets Isolation
// -------------------------------------------------------------
echo "\n-- Group 4: App Builder Targets Isolation --\n";

// Spec Example: theme-booking-android-pwa
$partial_builder = EntitlementClaims::get_app_builder_partial_fixture();
$closure = EntitlementSchema::resolve_dependency_closure(
    $partial_builder['features'],
    $partial_builder['builders']
);

assert_test($closure['builders']['android'] === true, 'Android builder target is true');
assert_test($closure['builders']['pwa'] === true, 'PWA builder target is true');
assert_test($closure['builders']['ios'] === false, 'Missing iOS target defaults to FALSE');
assert_test($closure['builders']['desktop'] === false, 'Missing Desktop target defaults to FALSE');
assert_test($closure['features']['builder.core'] === true, 'builder.core is automatically required for targets');
assert_test($closure['features']['builder.target.android'] === true, 'builder.target.android is true');
assert_test($closure['features']['builder.target.ios'] === false, 'builder.target.ios is false');
assert_test($closure['features']['builder.target.desktop'] === false, 'builder.target.desktop is false');

// EntitlementResolver check for specific targets
$valid_partial_claims = [
    'license_key' => 'valid-builder-key',
    'status'      => 'active',
    'site_url'    => 'https://example.com',
    'skus'        => ['sku_builder_android']
];
assert_test(EntitlementResolver::has_builder_target($valid_partial_claims, 'android') === true, 'has_builder_target(android) is true');
assert_test(EntitlementResolver::has_builder_target($valid_partial_claims, 'ios') === false, 'has_builder_target(ios) is FALSE (no privilege escalation)');
assert_test(EntitlementResolver::has_builder_target($valid_partial_claims, 'desktop') === false, 'has_builder_target(desktop) is FALSE');

// -------------------------------------------------------------
// Test Group 5: Legacy Aliases Backward Compatibility
// -------------------------------------------------------------
echo "\n-- Group 5: Legacy Feature Aliases Resolution --\n";

assert_test(EntitlementSchema::resolve_feature_id('builder.ui') === 'builder.core', 'builder.ui maps to builder.core');
assert_test(EntitlementSchema::resolve_feature_id('builder.config') === 'builder.core', 'builder.config maps to builder.core');
assert_test(EntitlementSchema::resolve_feature_id('clinic.psych') === 'psych.tests', 'clinic.psych maps to psych.tests');
assert_test(EntitlementSchema::resolve_feature_id('consultation.core') === 'clinic.consultation', 'consultation.core maps to clinic.consultation');
assert_test(EntitlementSchema::resolve_feature_id('clinic.sessions') === 'clinic.consultation', 'clinic.sessions maps to clinic.consultation');
assert_test(EntitlementSchema::resolve_feature_id('clinic.private_records') === 'clinic.records', 'clinic.private_records maps to clinic.records');
assert_test(EntitlementSchema::resolve_feature_id('builder.android') === 'builder.target.android', 'builder.android maps to builder.target.android');

// Resolve features with legacy aliases in claims
$legacy_alias_claims = [
    'license_key' => 'legacy-alias-test',
    'status'      => 'active',
    'site_url'    => 'https://example.com',
    'features_override' => [
        'consultation.core' => true,
        'builder.ui'        => true
    ]
];
$resolved_alias = EntitlementClaims::resolve_effective_features($legacy_alias_claims);
assert_test($resolved_alias['clinic.consultation'] === true, 'consultation.core override activates clinic.consultation');
assert_test($resolved_alias['builder.core'] === true, 'builder.ui override activates builder.core');

// -------------------------------------------------------------
// Test Group 6: Dependency Closure Resolution
// -------------------------------------------------------------
echo "\n-- Group 6: Transitive Dependency Closures --\n";

$course_claims = [
    'license_key' => 'course-test',
    'status'      => 'active',
    'site_url'    => 'https://example.com',
    'features_override' => [
        'academy.quizzes' => true // requires academy.courses and academy.core
    ]
];
$resolved_courses = EntitlementClaims::resolve_effective_features($course_claims);
assert_test($resolved_courses['academy.quizzes'] === true, 'academy.quizzes is true');
assert_test($resolved_courses['academy.courses'] === true, 'academy.courses transitively resolved to true');
assert_test($resolved_courses['academy.core'] === true, 'academy.core transitively resolved to true');

// -------------------------------------------------------------
// Test Group 7: Lifecycle Policies & Data Preservation
// -------------------------------------------------------------
echo "\n-- Group 7: Lifecycle Policies (Grace, Revoke, Transfer) --\n";

$policies = EntitlementSchema::get_lifecycle_policies();
assert_test($policies['grace_period_days'] === 14, 'Default grace period is 14 days');
assert_test($policies['offline_allowed'] === true, 'Offline verification is supported');
assert_test($policies['halt_writes_on_revocation'] === true, 'Halt writes on revocation');
assert_test($policies['preserve_site_data_on_revocation'] === true, 'Preserve site data on revocation');

// Grace period test
$grace_claims = EntitlementClaims::get_grace_period_fixture();
$status_grace = EntitlementResolver::get_license_status($grace_claims);
assert_test($status_grace === 'grace_period', 'Expired 5 days ago is classified as grace_period');
assert_test(EntitlementResolver::validate_license($grace_claims, false) === false, 'Strict validation without grace rejects expired license');
assert_test(EntitlementResolver::validate_license($grace_claims, true) === true, 'Validation with allow_grace accepts grace_period license');

// Revocation test
$revoked_claims = EntitlementClaims::get_revoked_fixture();
$status_revoked = EntitlementResolver::get_license_status($revoked_claims);
assert_test($status_revoked === 'revoked', 'Revoked license returns status=revoked');
assert_test(EntitlementResolver::validate_license($revoked_claims) === false, 'Revoked license is rejected');
$revoked_features = EntitlementResolver::resolve($revoked_claims);
assert_test($revoked_features['commerce.core'] === false, 'Revoked license yields all-false features (fail closed)');
assert_test($revoked_features['academy.core'] === false, 'Revoked license shuts down academy operations');

// Site transfer test
$xfer_claims = EntitlementClaims::get_site_transfer_fixture();
assert_test(!empty($xfer_claims['transfer_token']), 'Site transfer fixture includes transfer_token');
assert_test($xfer_claims['bound_site_url'] === 'https://new-domain.com', 'Site transfer targets new domain');

// Artifact inventory coverage check
$inventory = EntitlementSchema::get_artifact_inventory();
assert_test(isset($inventory['clinic.booking']), 'Artifact inventory maps clinic.booking');
assert_test(isset($inventory['clinic.consultation']), 'Artifact inventory maps clinic.consultation');
assert_test(isset($inventory['psych.tests']), 'Artifact inventory maps psych.tests');
assert_test(isset($inventory['builder.core']), 'Artifact inventory maps builder.core');
assert_test(isset($inventory['builder.target.android']), 'Artifact inventory maps builder.target.android');

echo "\n=================================================================\n";
echo ($fail === 0 ? "ALL P04-ENTITLEMENT-DATA-039 ACCEPTANCE TESTS PASSED!\n" : "$fail TESTS FAILED!\n");
echo "=================================================================\n";

exit($fail === 0 ? 0 : 1);
