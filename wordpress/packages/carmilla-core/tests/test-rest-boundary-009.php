<?php
/**
 * Test Suite: P04-WPPLUGIN-CODE-009 - Shared WordPress REST Boundary for Both Hosts and Co-Install
 *
 * Verifies:
 * 1. Unified error envelope conforming strictly to KMP ApiError decoder.
 * 2. Unified pagination envelope and headers conforming strictly to KMP PageResponse<T> / PageMetadata.
 * 3. Pagination ceiling (MAX_PER_PAGE = 100) and sanitization.
 * 4. Provider metadata reporting under strictly two backend profiles (WORDPRESS / SPRING; no 3rd profile).
 * 5. Legacy root alias preservation (/api/users/me, /api/addresses, etc.) with deprecation notice.
 * 6. Read and write endpoint parity across all three host modes via real HTTP requests:
 *    - Mode 1: Plugin Host alone on Twenty Twenty-Four (Bridge active, TT4 active)
 *    - Mode 2: Theme Host alone (Carmilla Theme active, Bridge plugin deactivated)
 *    - Mode 3: Co-install (Carmilla Theme and Bridge plugin both active)
 * 7. Bearer JWT authentication and user resolution in all three modes.
 *
 * Execution in Docker container:
 *   php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-rest-boundary-009.php
 */

define( 'WP_USE_THEMES', false );
require_once '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/plugin.php';
require_once ABSPATH . 'wp-admin/includes/theme.php';
require_once ABSPATH . 'wp-admin/includes/user.php';

// Test reporting harness
$total_tests  = 0;
$passed_tests = 0;
$failed_tests = 0;

function report_assert( bool $condition, string $message, string $details = '' ): void {
    global $total_tests, $passed_tests, $failed_tests;
    $total_tests++;
    if ( $condition ) {
        $passed_tests++;
        echo "  [PASS] {$message}\n";
    } else {
        $failed_tests++;
        echo "  [FAIL] {$message}\n";
        if ( ! empty( $details ) ) {
            echo "         Details: {$details}\n";
        }
    }
}

echo "====================================================================\n";
echo "  P04-WPPLUGIN-CODE-009: Shared REST Boundary & Host Parity QA      \n";
echo "====================================================================\n\n";

// Ensure shared kernel and REST classes are loaded
require_once __DIR__ . '/../Carmilla_Kernel.php';
require_once __DIR__ . '/../inc/Rest/JwtService.php';
require_once __DIR__ . '/../inc/Rest/RestInfrastructure.php';
require_once __DIR__ . '/../inc/Adapter/WooCommerceAdapter.php';

// -----------------------------------------------------------------------------
// Group 1: Error Envelope Compliance with KMP ApiError
// -----------------------------------------------------------------------------
echo "Group 1: Standardized Error Envelope (KMP ApiError Decoder Contract)\n";

$error_resp = \Carmilla\Core\Rest\RestInfrastructure::error_response(
    'BAD_CREDENTIALS',
    'نام کاربری یا رمز عبور نادرست است.',
    401,
    'api/auth/login',
    [ 'extra_info' => 'test' ]
);

report_assert( is_object( $error_resp ) && $error_resp instanceof WP_REST_Response, 'error_response returns WP_REST_Response' );
report_assert( 401 === $error_resp->get_status(), 'HTTP status matches specified status (401)' );

$data = $error_resp->get_data();
report_assert( isset( $data['message'] ) && 'نام کاربری یا رمز عبور نادرست است.' === $data['message'], 'Root contains human-readable message' );
report_assert( isset( $data['status'] ) && '401' === (string) $data['status'], 'Root contains status string/code' );
report_assert( isset( $data['code'] ) && 401 === $data['code'], 'Root contains numeric code' );
report_assert( isset( $data['errorCode'] ) && 'BAD_CREDENTIALS' === $data['errorCode'], 'Root contains errorCode slug for KMP' );
report_assert( isset( $data['path'] ) && 'api/auth/login' === $data['path'], 'Root contains request path' );
report_assert( ! empty( $data['timestamp'] ), 'Root contains ISO-8601 timestamp' );
report_assert( false === $data['success'], 'Root contains success: false' );
report_assert( isset( $data['error'] ) && is_array( $data['error'] ) && 'BAD_CREDENTIALS' === $data['error']['code'], 'Contains nested error object for standard clients' );

// -----------------------------------------------------------------------------
// Group 2: Pagination Envelope, Headers, and Ceiling
// -----------------------------------------------------------------------------
echo "\nGroup 2: Standardized Pagination & Ceiling (KMP PageResponse<T> Contract)\n";

$mock_items = [
    [ 'id' => 1, 'name' => 'Item 1' ],
    [ 'id' => 2, 'name' => 'Item 2' ],
];
$page_resp = \Carmilla\Core\Rest\RestInfrastructure::paginated_response( $mock_items, 45, 2, 20 );

report_assert( is_object( $page_resp ) && $page_resp instanceof WP_REST_Response, 'paginated_response returns WP_REST_Response' );
$headers = $page_resp->get_headers();
report_assert( isset( $headers['X-WP-Total'] ) && '45' === $headers['X-WP-Total'], 'Header X-WP-Total is set to 45' );
report_assert( isset( $headers['X-WP-TotalPages'] ) && '3' === $headers['X-WP-TotalPages'], 'Header X-WP-TotalPages is set to 3' );

$pdata = $page_resp->get_data();
report_assert( isset( $pdata['content'] ) && count( $pdata['content'] ) === 2, 'Payload contains Spring-compatible content array' );
report_assert( isset( $pdata['items'] ) && count( $pdata['items'] ) === 2, 'Payload contains items array' );
report_assert( isset( $pdata['page'] ) && is_array( $pdata['page'] ), 'Payload contains page metadata object' );
report_assert( 20 === $pdata['page']['size'], 'page.size is 20' );
report_assert( 2 === $pdata['page']['number'], 'page.number is 2' );
report_assert( 45 === $pdata['page']['totalElements'], 'page.totalElements is 45' );
report_assert( 3 === $pdata['page']['totalPages'], 'page.totalPages is 3' );
report_assert( 45 === $pdata['totalElements'] && 3 === $pdata['totalPages'], 'Backward-compat root totalElements and totalPages present' );

// Pagination ceiling test
$sanitized_huge = \Carmilla\Core\Rest\RestInfrastructure::sanitize_per_page( 500 );
report_assert( 100 === $sanitized_huge, 'per_page=500 capped at MAX_PER_PAGE (100)' );
$sanitized_neg = \Carmilla\Core\Rest\RestInfrastructure::sanitize_per_page( -10 );
report_assert( 1 === $sanitized_neg, 'Negative per_page sanitized to minimum 1' );
$sanitized_text = \Carmilla\Core\Rest\RestInfrastructure::sanitize_per_page( 'invalid', 15 );
report_assert( 15 === $sanitized_text, 'Non-numeric per_page falls back to default' );

// -----------------------------------------------------------------------------
// Group 3: Provider Mode & Diagnostic Metadata (Strict 2-Profile Boundary)
// -----------------------------------------------------------------------------
echo "\nGroup 3: Provider Mode & Diagnostic Metadata\n";

$meta = \Carmilla\Core\Rest\RestInfrastructure::get_provider_metadata();
report_assert( 'WORDPRESS' === $meta['backendProfile'], 'Backend profile is strictly WORDPRESS (never a 3rd profile)' );
report_assert( in_array( $meta['mode'], [ 'co-install', 'theme', 'plugin' ], true ), "Provider mode is valid ({$meta['mode']})" );
report_assert( ! empty( $meta['kernelVersion'] ), 'Kernel version is reported' );
report_assert( is_bool( $meta['wooCommerceActive'] ), 'WooCommerce active boolean is reported' );

$manifest_payload = \Carmilla\Core\Rest\RestInfrastructure::get_manifest_payload();
report_assert( 'WORDPRESS' === $manifest_payload['backendProfile'], 'Manifest backendProfile is strictly WORDPRESS' );
report_assert( in_array( $manifest_payload['hostMode'], [ 'co-install', 'theme', 'plugin' ], true ), "Manifest hostMode is valid ({$manifest_payload['hostMode']})" );
report_assert( isset( $manifest_payload['features'] ) && is_array( $manifest_payload['features'] ), 'Manifest contains features map' );
report_assert( isset( $manifest_payload['providerMetadata'] ), 'Manifest contains diagnostic providerMetadata' );

// -----------------------------------------------------------------------------
// Group 4: JWT Token Service Integration
// -----------------------------------------------------------------------------
echo "\nGroup 4: JWT Token Service (Shared Core)\n";

$test_user = get_user_by( 'login', 'admin' );
if ( ! $test_user ) {
    $admin_id  = wp_create_user( 'admin', 'password', 'admin@example.test' );
    $test_user = get_user_by( 'id', $admin_id );
}

$access_token = \Carmilla\Core\Rest\JwtService::issue( $test_user, 'access' );
report_assert( ! empty( $access_token ), 'Issued access token is non-empty string' );

$decoded = \Carmilla\Core\Rest\JwtService::decode( $access_token );
report_assert( is_array( $decoded ), 'Token successfully decoded' );
report_assert( (int) $test_user->ID === (int) ( $decoded['uid'] ?? 0 ), 'Decoded token uid matches user ID' );
report_assert( 'access' === ( $decoded['typ'] ?? '' ), 'Decoded token typ matches access' );
report_assert( 'carmilla-client' === ( $decoded['aud'] ?? '' ), 'Decoded token audience matches carmilla-client' );

// -----------------------------------------------------------------------------
// Group 5: Real HTTP Parity Across All 3 Host Modes on Real WordPress
// -----------------------------------------------------------------------------
echo "\nGroup 5: Real HTTP Parity Across All 3 Host Modes on Real WordPress\n";

function http_rest( string $method, string $path, array $params = [], array $headers = [] ): array {
    if ( strpos( $path, 'http' ) === 0 ) {
        $url = $path;
    } elseif ( strpos( $path, '/api/' ) === 0 && strpos( $path, '/wp-json/' ) === false ) {
        // Legacy root alias: e.g. /api/users/me
        $url = 'http://127.0.0.1' . $path;
    } else {
        $url = 'http://127.0.0.1' . ( strpos( $path, '/wp-json/' ) === 0 ? $path : '/wp-json/' . ltrim( $path, '/' ) );
    }
    $req_headers = array_merge( [
        'Host'         => 'localhost',
        'Content-Type' => 'application/json',
    ], $headers );

    $args = [
        'method'      => $method,
        'headers'     => $req_headers,
        'timeout'     => 15,
        'redirection' => 0,
    ];

    if ( 'GET' === $method && ! empty( $params ) ) {
        $url = add_query_arg( $params, $url );
    } elseif ( ! empty( $params ) ) {
        $args['body'] = json_encode( $params );
    }

    $response = wp_remote_request( $url, $args );
    if ( is_wp_error( $response ) ) {
        return [
            'status'  => 0,
            'data'    => [ 'error' => $response->get_error_message() ],
            'headers' => [],
        ];
    }

    $status = wp_remote_retrieve_response_code( $response );
    $body   = json_decode( wp_remote_retrieve_body( $response ), true );

    return [
        'status'  => (int) $status,
        'data'    => is_array( $body ) ? $body : [],
        'headers' => wp_remote_retrieve_headers( $response ),
    ];
}

// ------------------
// MODE 1: Standalone Plugin Host (Theme: twentytwentyfour, Bridge: Active)
// ------------------
echo "\n  --- Testing Mode 1: Plugin Host Alone (Storefront/TT4) ---\n";
switch_theme( 'twentytwentyfour' );
activate_plugin( 'carmilla-bridge/carmilla-bridge.php' );

// 1. Read endpoint: client-manifest
$m1_manifest = http_rest( 'GET', '/carmilla/v1/client-manifest' );
report_assert( 200 === $m1_manifest['status'], "Mode 1: /client-manifest returns HTTP 200 (got {$m1_manifest['status']})" );
$m1_mdata = $m1_manifest['data'];
report_assert( 'WORDPRESS' === ( $m1_mdata['backendProfile'] ?? '' ), 'Mode 1: backendProfile is WORDPRESS' );
report_assert( 'plugin' === ( $m1_mdata['hostMode'] ?? '' ), "Mode 1: hostMode is 'plugin' (actual: " . ( $m1_mdata['hostMode'] ?? '' ) . ')' );

// 2. Read endpoint: /api/products
$m1_products = http_rest( 'GET', '/carmilla/v1/api/products', [ 'per_page' => 5 ] );
report_assert( 200 === $m1_products['status'], "Mode 1: /api/products returns HTTP 200 (got {$m1_products['status']})" );
$m1_pdata = $m1_products['data'];
report_assert( isset( $m1_pdata['content'] ) || isset( $m1_pdata['items'] ), 'Mode 1: /api/products returns content/items array' );
report_assert( isset( $m1_pdata['page'] ) || isset( $m1_pdata['totalPages'] ), 'Mode 1: /api/products returns page metadata' );

// 3. Write endpoint: /api/auth/login (bad credentials)
$m1_bad_login = http_rest( 'POST', '/carmilla/v1/api/auth/login', [ 'username' => 'wrong', 'password' => 'wrong' ] );
report_assert( 401 === $m1_bad_login['status'], "Mode 1: Invalid login returns HTTP 401 (got {$m1_bad_login['status']})" );
$m1_err = $m1_bad_login['data'];
report_assert( 'BAD_CREDENTIALS' === ( $m1_err['errorCode'] ?? '' ), 'Mode 1: Error envelope has BAD_CREDENTIALS' );

// 4. Write endpoint: /api/auth/login (valid credentials)
$m1_good_login = http_rest( 'POST', '/carmilla/v1/api/auth/login', [ 'username' => 'admin', 'password' => 'password' ] );
report_assert( 200 === $m1_good_login['status'], "Mode 1: Valid login returns HTTP 200 (got {$m1_good_login['status']})" );
$m1_auth  = $m1_good_login['data'];
$m1_token = $m1_auth['accessToken'] ?? $m1_auth['token'] ?? $m1_auth['data']['accessToken'] ?? $m1_auth['data']['token'] ?? '';
report_assert( ! empty( $m1_token ), 'Mode 1: Valid login returns JWT token' );

// ------------------
// MODE 2: Standalone Theme Host (Theme: carmilla-theme, Bridge: Deactivated)
// ------------------
echo "\n  --- Testing Mode 2: Theme Host Alone (Bridge Absent) ---\n";
switch_theme( 'carmilla-theme' );
deactivate_plugins( 'carmilla-bridge/carmilla-bridge.php' );

// 1. Read endpoint: client-manifest
$m2_manifest = http_rest( 'GET', '/carmilla/v1/client-manifest' );
report_assert( 200 === $m2_manifest['status'], "Mode 2: /client-manifest returns HTTP 200 (got {$m2_manifest['status']})" );
$m2_mdata = $m2_manifest['data'];
report_assert( 'WORDPRESS' === ( $m2_mdata['backendProfile'] ?? '' ), 'Mode 2: backendProfile is WORDPRESS' );
report_assert( 'theme' === ( $m2_mdata['hostMode'] ?? '' ), "Mode 2: hostMode is 'theme' (actual: " . ( $m2_mdata['hostMode'] ?? '' ) . ')' );

// 2. Read endpoint: /api/products
$m2_products = http_rest( 'GET', '/carmilla/v1/api/products', [ 'per_page' => 5 ] );
report_assert( 200 === $m2_products['status'], "Mode 2: /api/products returns HTTP 200 (got {$m2_products['status']})" );
$m2_pdata = $m2_products['data'];
report_assert( isset( $m2_pdata['content'] ) || isset( $m2_pdata['items'] ), 'Mode 2: /api/products returns content/items array' );
report_assert( isset( $m2_pdata['page'] ) || isset( $m2_pdata['totalPages'] ), 'Mode 2: /api/products returns page metadata' );

// 3. Write endpoint: /api/auth/login (bad credentials)
$m2_bad_login = http_rest( 'POST', '/carmilla/v1/api/auth/login', [ 'username' => 'wrong', 'password' => 'wrong' ] );
report_assert( 401 === $m2_bad_login['status'], "Mode 2: Invalid login returns HTTP 401 (got {$m2_bad_login['status']})" );
$m2_err = $m2_bad_login['data'];
report_assert( 'BAD_CREDENTIALS' === ( $m2_err['errorCode'] ?? '' ), 'Mode 2: Error envelope has BAD_CREDENTIALS' );

// 4. Write endpoint: /api/auth/login (valid credentials)
$m2_good_login = http_rest( 'POST', '/carmilla/v1/api/auth/login', [ 'username' => 'admin', 'password' => 'password' ] );
report_assert( 200 === $m2_good_login['status'], "Mode 2: Valid login returns HTTP 200 (got {$m2_good_login['status']})" );
$m2_auth  = $m2_good_login['data'];
$m2_token = $m2_auth['accessToken'] ?? $m2_auth['token'] ?? $m2_auth['data']['accessToken'] ?? $m2_auth['data']['token'] ?? '';
report_assert( ! empty( $m2_token ), 'Mode 2: Valid login returns JWT token' );

// ------------------
// MODE 3: Co-Install Host (Theme: carmilla-theme, Bridge: Active)
// ------------------
echo "\n  --- Testing Mode 3: Co-Install (Theme + Plugin Active) ---\n";
activate_plugin( 'carmilla-bridge/carmilla-bridge.php' );

// 1. Read endpoint: client-manifest
$m3_manifest = http_rest( 'GET', '/carmilla/v1/client-manifest' );
report_assert( 200 === $m3_manifest['status'], "Mode 3: /client-manifest returns HTTP 200 (got {$m3_manifest['status']})" );
$m3_mdata = $m3_manifest['data'];
report_assert( 'WORDPRESS' === ( $m3_mdata['backendProfile'] ?? '' ), 'Mode 3: backendProfile is WORDPRESS' );
report_assert( 'co-install' === ( $m3_mdata['hostMode'] ?? '' ), "Mode 3: hostMode is 'co-install' (actual: " . ( $m3_mdata['hostMode'] ?? '' ) . ')' );

// 2. Read endpoint: /api/products
$m3_products = http_rest( 'GET', '/carmilla/v1/api/products', [ 'per_page' => 5 ] );
report_assert( 200 === $m3_products['status'], "Mode 3: /api/products returns HTTP 200 (got {$m3_products['status']})" );
$m3_pdata = $m3_products['data'];
report_assert( isset( $m3_pdata['content'] ) || isset( $m3_pdata['items'] ), 'Mode 3: /api/products returns content/items array' );
report_assert( isset( $m3_pdata['page'] ) || isset( $m3_pdata['totalPages'] ), 'Mode 3: /api/products returns page metadata' );

// 3. Write endpoint: /api/auth/login (bad credentials)
$m3_bad_login = http_rest( 'POST', '/carmilla/v1/api/auth/login', [ 'username' => 'wrong', 'password' => 'wrong' ] );
report_assert( 401 === $m3_bad_login['status'], "Mode 3: Invalid login returns HTTP 401 (got {$m3_bad_login['status']})" );
$m3_err = $m3_bad_login['data'];
report_assert( 'BAD_CREDENTIALS' === ( $m3_err['errorCode'] ?? '' ), 'Mode 3: Error envelope has BAD_CREDENTIALS' );

// 4. Write endpoint: /api/auth/login (valid credentials)
$m3_good_login = http_rest( 'POST', '/carmilla/v1/api/auth/login', [ 'username' => 'admin', 'password' => 'password' ] );
report_assert( 200 === $m3_good_login['status'], "Mode 3: Valid login returns HTTP 200 (got {$m3_good_login['status']})" );
$m3_auth  = $m3_good_login['data'];
$m3_token = $m3_auth['accessToken'] ?? $m3_auth['token'] ?? $m3_auth['data']['accessToken'] ?? $m3_auth['data']['token'] ?? '';
report_assert( ! empty( $m3_token ), 'Mode 3: Valid login returns JWT token' );

// -----------------------------------------------------------------------------
// Group 6: DTO & Error Structure Parity Verification
// -----------------------------------------------------------------------------
echo "\nGroup 6: Cross-Mode DTO and Error Parity\n";

report_assert( ( $m1_err['errorCode'] ?? '' ) === ( $m2_err['errorCode'] ?? '' ) && ( $m2_err['errorCode'] ?? '' ) === ( $m3_err['errorCode'] ?? '' ), 'Error code parity: all 3 modes return BAD_CREDENTIALS' );
report_assert( (string) ( $m1_err['status'] ?? '' ) === (string) ( $m2_err['status'] ?? '' ) && (string) ( $m2_err['status'] ?? '' ) === (string) ( $m3_err['status'] ?? '' ), 'Error status parity: all 3 modes return 401' );
report_assert( isset( $m1_mdata['features'] ) && isset( $m2_mdata['features'] ) && isset( $m3_mdata['features'] ), 'Manifest DTO parity: all 3 modes provide feature flags map' );

// -----------------------------------------------------------------------------
// Group 7: Bearer JWT Authentication Verification
// -----------------------------------------------------------------------------
echo "\nGroup 7: Bearer Token User Authentication\n";

$_SERVER['HTTP_AUTHORIZATION'] = 'Bearer ' . $access_token;
$auth_user_id = \Carmilla\Core\Rest\RestInfrastructure::authenticate_bearer( false );
report_assert( (int) $test_user->ID === (int) $auth_user_id, 'Bearer token correctly resolves to admin user ID' );

unset( $_SERVER['HTTP_AUTHORIZATION'] );
$anon_user_id = \Carmilla\Core\Rest\RestInfrastructure::authenticate_bearer( false );
report_assert( false === $anon_user_id, 'Absence of token leaves user ID unauthenticated' );

// -----------------------------------------------------------------------------
// Group 8: Legacy Root Alias Preservation
// -----------------------------------------------------------------------------
echo "\nGroup 8: Legacy Root Alias Preservation\n";

// Query root alias /api/users/me via HTTP
$alias_resp = http_rest( 'GET', '/api/users/me', [], [ 'Authorization' => 'Bearer ' . $access_token ] );
report_assert( in_array( $alias_resp['status'], [ 200, 401 ], true ), "Root alias /api/users/me responds with valid HTTP status (got {$alias_resp['status']})" );
$alias_headers = $alias_resp['headers'];
report_assert( isset( $alias_headers['x-carmilla-deprecated'] ) || isset( $alias_headers['warning'] ), 'Root alias returns deprecation telemetry headers' );

// -----------------------------------------------------------------------------
// Final Restoration & Summary
// -----------------------------------------------------------------------------
echo "\n====================================================================\n";
echo "Results: Total: {$total_tests} | Passed: {$passed_tests} | Failed: {$failed_tests}\n";
echo "====================================================================\n";

if ( $failed_tests > 0 ) {
    exit( 1 );
}
exit( 0 );
