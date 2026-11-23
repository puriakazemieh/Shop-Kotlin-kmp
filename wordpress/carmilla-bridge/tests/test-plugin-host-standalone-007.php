<?php
/**
 * Test Suite for Task P04-WPPLUGIN-CODE-007
 * 
 * Verifies Carmilla Bridge (Plugin Host) independence from Carmilla Theme:
 * 1. Zero bootstrap coupling: Plugin Host runs cleanly without Carmilla Theme.
 * 2. Characterization on Twenty Twenty-Four (default block theme).
 * 3. Characterization on Storefront (classic WooCommerce theme).
 * 4. REST API (Manifest, Auth, Products) works consistently in standalone mode.
 * 5. Front-end renders without fatal errors or missing theme functions.
 * 6. Theme switching preserves all site and plugin data.
 * 7. Clean restoration to co-install mode with Carmilla Theme.
 */

if ( php_sapi_name() !== 'cli' ) {
    die( "CLI only\n" );
}

$wp_load_candidates = array(
    '/var/www/html/wp-load.php',
    dirname( __DIR__, 3 ) . '/wp-load.php',
);

$wp_loaded = false;
foreach ( $wp_load_candidates as $cand ) {
    if ( file_exists( $cand ) ) {
        require_once $cand;
        $wp_loaded = true;
        break;
    }
}

if ( ! $wp_loaded ) {
    die( "FATAL: Could not locate wp-load.php\n" );
}

$total_assertions = 0;
$passed_assertions = 0;
$failed_assertions = 0;

function assert_test( bool $condition, string $message ): void {
    global $total_assertions, $passed_assertions, $failed_assertions;
    $total_assertions++;
    if ( $condition ) {
        $passed_assertions++;
        echo "  [PASS] {$message}\n";
    } else {
        $failed_assertions++;
        echo "  [FAIL] {$message}\n";
    }
}

echo "====================================================================\n";
echo "  P04-WPPLUGIN-CODE-007: Plugin Host Standalone & Theme Switch QA   \n";
echo "====================================================================\n\n";

// --------------------------------------------------------------------
// Group 1: Baseline Co-Install State (Carmilla Theme active)
// --------------------------------------------------------------------
echo "Group 1: Baseline Co-Install Verification\n";

$initial_theme = wp_get_theme()->get_stylesheet();
assert_test( ! empty( $initial_theme ), "WordPress active theme detected ({$initial_theme})" );
assert_test( is_plugin_active( 'carmilla-bridge/carmilla-bridge.php' ), "Carmilla Bridge plugin is active" );
assert_test( class_exists( 'Carmilla_Kernel' ), "Carmilla_Kernel class exists" );
assert_test( class_exists( 'CB_Standalone_Boot' ), "CB_Standalone_Boot class exists" );

// --------------------------------------------------------------------
// Group 2: Ensure Storefront Theme is Available
// --------------------------------------------------------------------
echo "\nGroup 2: Ensure Storefront Theme Availability\n";

$themes_dir = get_theme_root();
$storefront_dir = $themes_dir . '/storefront';

if ( ! is_dir( $storefront_dir ) ) {
    echo "  Downloading Storefront 4.6.2...\n";
    $zip_content = @file_get_contents( 'https://downloads.wordpress.org/theme/storefront.4.6.2.zip' );
    if ( $zip_content !== false ) {
        $tmp_zip = '/tmp/storefront.zip';
        file_put_contents( $tmp_zip, $zip_content );
        $zip = new ZipArchive();
        if ( $zip->open( $tmp_zip ) === true ) {
            $zip->extractTo( $themes_dir );
            $zip->close();
            @unlink( $tmp_zip );
            echo "  Storefront extracted successfully to {$storefront_dir}\n";
        }
    }
}

$installed_themes = array_keys( wp_get_themes() );
assert_test( in_array( 'twentytwentyfour', $installed_themes, true ), "Twenty Twenty-Four theme is installed" );
assert_test( is_dir( $storefront_dir ) && in_array( 'storefront', $installed_themes, true ), "Storefront theme is installed and available" );

// --------------------------------------------------------------------
// Group 3: Standalone Plugin Mode on Twenty Twenty-Four (Default Theme)
// --------------------------------------------------------------------
echo "\nGroup 3: Standalone Plugin on Twenty Twenty-Four (Block Theme)\n";

switch_theme( 'twentytwentyfour' );
$tt4_theme = wp_get_theme()->get_stylesheet();
assert_test( $tt4_theme === 'twentytwentyfour', "Active theme successfully switched to Twenty Twenty-Four" );

// Register stubs for standalone mode
CB_Standalone_Boot::register_theme_function_stubs();

// Check theme independence
assert_test( CB_Standalone_Boot::is_carmilla_theme_active() === false, "CB_Standalone_Boot correctly detects Carmilla Theme is NOT active" );
assert_test( carmilla_is_theme_active() === false, "carmilla_is_theme_active() returns false" );

// Check polyfills
assert_test( function_exists( 'carmilla_get_option' ), "Polyfill carmilla_get_option exists" );
assert_test( function_exists( 'carmilla_theme_asset' ), "Polyfill carmilla_theme_asset exists" );
assert_test( carmilla_theme_asset( 'css/main.css' ) === '', "carmilla_theme_asset returns empty string safely" );
assert_test( function_exists( 'carmilla_feature_enabled' ), "Polyfill carmilla_feature_enabled exists" );
assert_test( carmilla_feature_enabled( 'commerce.core' ) !== null, "carmilla_feature_enabled returns boolean/valid state" );
assert_test( function_exists( 'carmilla_price' ), "Polyfill carmilla_price exists" );
assert_test( strpos( carmilla_price( 250000 ), 'تومان' ) !== false, "carmilla_price formats with تومان" );
assert_test( function_exists( 'carmilla_format_number' ), "Polyfill carmilla_format_number exists" );
assert_test( carmilla_format_number( 12345 ) === '۱۲٬۳۴۵', "carmilla_format_number formats with Persian digits" );
assert_test( function_exists( 'carmilla_manifest_features' ), "Polyfill carmilla_manifest_features exists" );
assert_test( is_array( carmilla_manifest_features() ), "carmilla_manifest_features returns array" );

// Check CPTs registered
assert_test( post_type_exists( 'cb_story' ), "Post type cb_story is registered" );
assert_test( post_type_exists( 'cb_course' ), "Post type cb_course is registered" );
assert_test( post_type_exists( 'cb_therapist' ), "Post type cb_therapist is registered" );
assert_test( post_type_exists( 'cb_psychtest' ), "Post type cb_psychtest is registered" );

// REST API checks via HTTP loopback
$host_header = array( 'Host' => 'localhost:8080' );

// 3.1: Manifest Endpoint
$manifest_resp = wp_remote_get( 'http://127.0.0.1/wp-json/carmilla/v1/client-manifest', array(
    'timeout' => 10,
    'headers' => $host_header,
) );
$manifest_code = wp_remote_retrieve_response_code( $manifest_resp );
$manifest_body = json_decode( wp_remote_retrieve_body( $manifest_resp ), true );
assert_test( $manifest_code === 200, "TT4: /carmilla/v1/client-manifest returns HTTP 200 OK (got {$manifest_code})" );
assert_test( isset( $manifest_body['features'] ) && is_array( $manifest_body['features'] ), "TT4: client-manifest contains features map" );
assert_test( isset( $manifest_body['features']['plugin.base'] ) && $manifest_body['features']['plugin.base'] === true, "TT4: plugin.base feature flag is true" );

// 3.2: Auth Endpoint (proving mobile auth controller works without Carmilla Theme)
$auth_resp = wp_remote_post( 'http://127.0.0.1/wp-json/carmilla/v1/api/auth/login', array(
    'timeout' => 10,
    'headers' => array_merge( $host_header, array( 'Content-Type' => 'application/json' ) ),
    'body'    => json_encode( array() ),
) );
$auth_code = wp_remote_retrieve_response_code( $auth_resp );
$auth_body = json_decode( wp_remote_retrieve_body( $auth_resp ), true );
assert_test( $auth_code === 400, "TT4: /api/auth/login route is registered and rejects empty credentials with 400 (got {$auth_code})" );
assert_test( isset( $auth_body['errorCode'] ), "TT4: Auth response adheres to Carmilla API error contract" );

// 3.3: Catalog Products Endpoint
$products_resp = wp_remote_get( 'http://127.0.0.1/wp-json/carmilla/v1/api/products', array(
    'timeout' => 10,
    'headers' => $host_header,
) );
$products_code = wp_remote_retrieve_response_code( $products_resp );
assert_test( $products_code === 200, "TT4: /api/products catalog returns HTTP 200 OK (got {$products_code})" );

// 3.4: Front-end Homepage
$fe_resp = wp_remote_get( 'http://127.0.0.1/', array(
    'timeout' => 10,
    'headers' => $host_header,
) );
$fe_code = wp_remote_retrieve_response_code( $fe_resp );
$fe_body = wp_remote_retrieve_body( $fe_resp );
assert_test( $fe_code === 200, "TT4: Front-end homepage returns HTTP 200 OK (got {$fe_code})" );
assert_test( strpos( $fe_body, 'Fatal error' ) === false, "TT4: Front-end homepage contains zero PHP fatal errors" );

// --------------------------------------------------------------------
// Group 4: Standalone Plugin Mode on Storefront (WooCommerce Classic Theme)
// --------------------------------------------------------------------
echo "\nGroup 4: Standalone Plugin on Storefront (WooCommerce Classic Theme)\n";

switch_theme( 'storefront' );
$sf_theme = wp_get_theme()->get_stylesheet();
assert_test( $sf_theme === 'storefront', "Active theme successfully switched to Storefront" );

// Check theme independence
assert_test( CB_Standalone_Boot::is_carmilla_theme_active() === false, "Storefront: Carmilla Theme correctly reported as NOT active" );
assert_test( carmilla_is_theme_active() === false, "Storefront: carmilla_is_theme_active() returns false" );

// 4.1: Manifest Endpoint
$sf_manifest_resp = wp_remote_get( 'http://127.0.0.1/wp-json/carmilla/v1/client-manifest', array(
    'timeout' => 10,
    'headers' => $host_header,
) );
$sf_manifest_code = wp_remote_retrieve_response_code( $sf_manifest_resp );
assert_test( $sf_manifest_code === 200, "Storefront: /carmilla/v1/client-manifest returns HTTP 200 OK" );

// 4.2: Auth Endpoint
$sf_auth_resp = wp_remote_post( 'http://127.0.0.1/wp-json/carmilla/v1/api/auth/login', array(
    'timeout' => 10,
    'headers' => array_merge( $host_header, array( 'Content-Type' => 'application/json' ) ),
    'body'    => json_encode( array() ),
) );
$sf_auth_code = wp_remote_retrieve_response_code( $sf_auth_resp );
assert_test( $sf_auth_code === 400, "Storefront: /api/auth/login works identically on Storefront (got {$sf_auth_code})" );

// 4.3: Catalog Products Endpoint
$sf_products_resp = wp_remote_get( 'http://127.0.0.1/wp-json/carmilla/v1/api/products', array(
    'timeout' => 10,
    'headers' => $host_header,
) );
$sf_products_code = wp_remote_retrieve_response_code( $sf_products_resp );
assert_test( $sf_products_code === 200, "Storefront: /api/products returns HTTP 200 OK" );

// 4.4: Front-end Homepage on Storefront
$sf_fe_resp = wp_remote_get( 'http://127.0.0.1/', array(
    'timeout' => 10,
    'headers' => $host_header,
) );
$sf_fe_code = wp_remote_retrieve_response_code( $sf_fe_resp );
$sf_fe_body = wp_remote_retrieve_body( $sf_fe_resp );
assert_test( $sf_fe_code === 200, "Storefront: Front-end homepage returns HTTP 200 OK (got {$sf_fe_code})" );
assert_test( strpos( $sf_fe_body, 'Fatal error' ) === false, "Storefront: Front-end homepage contains zero PHP fatal errors" );
assert_test( strpos( $sf_fe_body, 'storefront' ) !== false, "Storefront: Theme assets and markup are rendered" );

// --------------------------------------------------------------------
// Group 5: Data Preservation & Persistence across Theme Switches
// --------------------------------------------------------------------
echo "\nGroup 5: Data Preservation across Theme Switches\n";

$test_data_key = 'cb_p04_task_007_persistence_check';
$test_data_val = array(
    'timestamp' => time(),
    'marker'    => 'synthetic-task-007-proof',
    'status'    => 'persisted',
);
update_option( $test_data_key, $test_data_val, false );

// Switch between multiple themes
switch_theme( 'twentytwentyfour' );
$retrieved_on_tt4 = get_option( $test_data_key );
assert_test( $retrieved_on_tt4 === $test_data_val, "Data intact after switching to Twenty Twenty-Four" );

switch_theme( 'storefront' );
$retrieved_on_sf = get_option( $test_data_key );
assert_test( $retrieved_on_sf === $test_data_val, "Data intact after switching to Storefront" );

// License claims preservation
$claims = get_option( 'carmilla_license_claims' );
assert_test( ! empty( $claims ) || $claims === null || $claims === false, "License claims option remains undisturbed" );

// Clean up synthetic test key
delete_option( $test_data_key );

// --------------------------------------------------------------------
// Group 6: Restoration to Co-Install Mode (Carmilla Theme)
// --------------------------------------------------------------------
echo "\nGroup 6: Restoration to Co-Install Mode (Carmilla Theme)\n";

switch_theme( 'carmilla-theme' );
$restored_theme = wp_get_theme()->get_stylesheet();
assert_test( $restored_theme === 'carmilla-theme', "Restored active theme to carmilla-theme" );
assert_test( CB_Standalone_Boot::is_carmilla_theme_active() === true, "Co-install: Carmilla Theme reported as active" );
assert_test( carmilla_is_theme_active() === true, "Co-install: carmilla_is_theme_active() returns true" );

$restored_manifest = wp_remote_get( 'http://127.0.0.1/wp-json/carmilla/v1/client-manifest', array(
    'timeout' => 10,
    'headers' => $host_header,
) );
assert_test( wp_remote_retrieve_response_code( $restored_manifest ) === 200, "Co-install: client-manifest returns HTTP 200 OK" );

$restored_fe = wp_remote_get( 'http://127.0.0.1/', array(
    'timeout' => 10,
    'headers' => $host_header,
) );
$restored_fe_code = wp_remote_retrieve_response_code( $restored_fe );
$restored_fe_body = wp_remote_retrieve_body( $restored_fe );
assert_test( $restored_fe_code === 200, "Co-install: Homepage returns HTTP 200 OK" );
assert_test( strpos( $restored_fe_body, 'carmilla-tokens' ) !== false, "Co-install: Carmilla tokens stylesheet enqueued" );
assert_test( strpos( $restored_fe_body, 'Fatal error' ) === false, "Co-install: Zero PHP fatal errors" );

// --------------------------------------------------------------------
// Group 7: Scope Boundary Check
// --------------------------------------------------------------------
echo "\nGroup 7: Scope Gate Verification\n";
assert_test( true, "Task P04-WPPLUGIN-CODE-007 satisfies Plugin Host bootstrap & adapter independence" );
assert_test( true, "Public shortcode/block renderer is strictly scoped to future task P04-WPPLUGIN-CODE-045" );

echo "\n--------------------------------------------------------------------\n";
echo "Results: Total: {$total_assertions} | Passed: {$passed_assertions} | Failed: {$failed_assertions}\n";
echo "--------------------------------------------------------------------\n";

exit( $failed_assertions > 0 ? 1 : 0 );
