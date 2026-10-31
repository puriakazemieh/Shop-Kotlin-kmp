<?php
/** Smoke test for the self-contained Phase 07 migration safety primitives. */

define( 'ABSPATH', __DIR__ . '/' );

if ( ! function_exists( 'wp_json_encode' ) ) {
    function wp_json_encode( $value, $flags = 0 ) {
        return json_encode( $value, $flags );
    }
}

require dirname( __DIR__ ) . '/includes/Migration/Migration_Config.php';
require dirname( __DIR__ ) . '/includes/Migration/Migration_Crypto.php';

$failures = 0;
function migration_check( bool $condition, string $message ): void {
    global $failures;
    echo ( $condition ? 'PASS' : 'FAIL' ) . "  {$message}\n";
    if ( ! $condition ) {
        ++$failures;
    }
}

$post_types = \Carmilla\Bridge\Migration\Migration_Config::get_denied_post_types();
migration_check( in_array( 'shop_order', $post_types, true ), 'orders are blocked from export' );
migration_check( in_array( 'cb_health_record', $post_types, true ), 'health records are blocked from export' );
migration_check( \Carmilla\Bridge\Migration\Migration_Config::is_denied_meta_key( '_billing_email' ), 'billing email is blocked' );
migration_check( \Carmilla\Bridge\Migration\Migration_Config::is_denied_meta_key( 'provider_api_key' ), 'provider API keys are blocked' );
migration_check( \Carmilla\Bridge\Migration\Migration_Config::is_denied_meta_key( 'Provider_TOKEN_v2' ), 'mixed-case token keys are blocked' );
migration_check( \Carmilla\Bridge\Migration\Migration_Config::is_denied_meta_key( 'backup_Password_hint' ), 'mixed-case password keys are blocked' );
migration_check( \Carmilla\Bridge\Migration\Migration_Config::is_denied_meta_key( 'client_Secret_label' ), 'mixed-case secret keys are blocked' );
migration_check( \Carmilla\Bridge\Migration\Migration_Config::is_denied_meta_key( 'vendor_API_KEY_hint' ), 'mixed-case API keys are blocked' );
migration_check( ! \Carmilla\Bridge\Migration\Migration_Config::is_denied_meta_key( 'public_display_name' ), 'public metadata remains eligible' );

$key     = str_repeat( 'k', 32 );
$site_id = 'test-site-uuid';
$payload = '{"test":"data"}';
$package = \Carmilla\Bridge\Migration\Migration_Crypto::encrypt_payload( $payload, $key, $site_id, time() + 3600 );
migration_check( $package !== $payload, 'payload is encrypted' );
migration_check( $payload === \Carmilla\Bridge\Migration\Migration_Crypto::decrypt_payload( $package, $key, $site_id ), 'payload decrypts for its site' );

foreach (
    array(
        array( str_repeat( 'j', 32 ), $site_id, 'a wrong key is rejected' ),
        array( $key, 'another-site', 'a wrong site is rejected' ),
    ) as $case
) {
    try {
        \Carmilla\Bridge\Migration\Migration_Crypto::decrypt_payload( $package, $case[0], $case[1] );
        migration_check( false, $case[2] );
    } catch ( \RuntimeException $exception ) {
        migration_check( true, $case[2] );
    }
}

try {
    \Carmilla\Bridge\Migration\Migration_Crypto::encrypt_payload( $payload, $key, $site_id, time() - 1 );
    migration_check( false, 'expired package is rejected' );
} catch ( \InvalidArgumentException $exception ) {
    migration_check( true, 'expired package is rejected' );
}

exit( 0 === $failures ? 0 : 1 );
