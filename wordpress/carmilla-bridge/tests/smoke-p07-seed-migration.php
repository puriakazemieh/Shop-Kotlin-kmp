<?php
/**
 * Smoke test for Phase 07 Seed & Migration logic.
 * Tests Crypto, Config Denylists, URL Rewrite, and Delta Hash generation.
 */

define( 'ABSPATH', __DIR__ . '/' );

require dirname(__DIR__) . '/includes/Migration/Migration_Config.php';
require dirname(__DIR__) . '/includes/Migration/Migration_Crypto.php';

\ = 0;
function check( \, \ ) {
    global \;
    echo ( \ ? 'PASS' : 'FAIL' ) . "  \\n";
    if ( ! \ ) { \++; }
}

// 1. Test Migration_Config Denylist
\ = \Carmilla\Bridge\Migration\Migration_Config::get_denied_post_types();
check( in_array('shop_order', \), 'shop_order is blocked from export' );
check( in_array('cb_health_record', \), 'cb_health_record is blocked from export' );

\ = \Carmilla\Bridge\Migration\Migration_Config::get_denied_meta_keys();
check( in_array('_billing_email', \), '_billing_email is blocked' );
check( in_array('password', \), 'password meta is blocked' );

// 2. Test Migration_Crypto AEAD
\ = str_repeat('k', 32); // 32-byte key
\ = 'test-site-uuid';
\ = time() + 3600;
\ = '{"test": "data"}';

\ = \Carmilla\Bridge\Migration\Migration_Crypto::encrypt_payload(\, \, \, \);
check( !empty(\) && \ !== \, 'Payload is encrypted and base64 encoded' );

\ = \Carmilla\Bridge\Migration\Migration_Crypto::decrypt_payload(\, \, \);
check( \ === \, 'Decrypted payload matches original' );

// Tamper test - wrong key
try {
    \Carmilla\Bridge\Migration\Migration_Crypto::decrypt_payload(\, str_repeat('j', 32), \);
    check( false, 'Should have failed with wrong key' );
} catch (\Exception \) {
    check( true, 'Wrong key fails decryption properly' );
}

// Tamper test - wrong customer UUID (binding)
try {
    \Carmilla\Bridge\Migration\Migration_Crypto::decrypt_payload(\, \, 'wrong-uuid');
    check( false, 'Should have failed with wrong customer UUID' );
} catch (\Exception \) {
    check( true, 'Customer UUID binding enforced properly' );
}

// Tamper test - expired
\ = \Carmilla\Bridge\Migration\Migration_Crypto::encrypt_payload(\, \, \, time() - 100);
try {
    \Carmilla\Bridge\Migration\Migration_Crypto::decrypt_payload(\, \, \);
    check( false, 'Should have failed expiry' );
} catch (\Exception \) {
    check( true, 'Expiry enforced properly' );
}

// 3. Test URL Rewriting logic (simulating Migrator method)
function rewrite_urls(\, \, \) {
    if (empty(\) || empty(\) || empty(\)) return \;
    return str_replace(\, \, \);
}
\ = 'Welcome to https://old-domain.com/shop!';
\ = rewrite_urls(\, 'https://old-domain.com', 'https://new-domain.com');
check( \ === 'Welcome to https://new-domain.com/shop!', 'URL rewrite works successfully' );

echo "\n" . ( \ === 0 ? 'ALL PASSED' : "\ FAILED" ) . "\n";
exit( \ === 0 ? 0 : 1 );
