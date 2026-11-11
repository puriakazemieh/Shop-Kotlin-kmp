<?php
/**
 * Test MessageSettings encryption & backward compatibility.
 * Task: P02-SEC-CODE-010C3D1
 */

define( 'ABSPATH', __DIR__ . '/' );
define( 'SECURE_AUTH_KEY', hash( 'sha256', 'synthetic-test-key-p02-sec-010c3d1' ) );

require_once __DIR__ . '/../inc/Settings/MessageSettings.php';

use Carmilla\Core\Settings\MessageSettings;

$passed = 0;
$failed = 0;

function assert_test( $condition, $message ) {
    global $passed, $failed;
    if ( $condition ) {
        echo "PASS: {$message}\n";
        $passed++;
    } else {
        echo "FAIL: {$message}\n";
        $failed++;
    }
}

// 1. Verify base64_decode is absent from MessageSettings.php source
$source = file_get_contents( __DIR__ . '/../inc/Settings/MessageSettings.php' );
assert_test( false === strpos( $source, 'base64_decode' ), 'MessageSettings.php does not contain base64_decode' );
assert_test( false === strpos( $source, 'base64_encode' ), 'MessageSettings.php does not contain base64_encode' );

// 2. Encryption generates valid hex
$plain = 'super-secret-api-token-9876543210!#$';
$cipher = MessageSettings::encrypt_secret( $plain );
assert_test( '' !== $cipher, 'encrypt_secret returns non-empty string' );
assert_test( ctype_xdigit( $cipher ), 'Ciphertext is valid hexadecimal' );
assert_test( 0 === ( strlen( $cipher ) % 2 ), 'Ciphertext has even length' );

// 3. Hex decryption roundtrip
$decrypted = MessageSettings::decrypt_secret( $cipher );
assert_test( $decrypted === $plain, 'Hex ciphertext decrypts back to original plain text' );

// 4. Backward compatibility with legacy base64-encoded payload
$key = SECURE_AUTH_KEY;
$iv = openssl_random_pseudo_bytes( openssl_cipher_iv_length( 'aes-256-cbc' ) );
$enc = openssl_encrypt( $plain, 'aes-256-cbc', $key, 0, $iv );
$legacy_base64 = base64_encode( $enc . '::' . $iv );

$legacy_decrypted = MessageSettings::decrypt_secret( $legacy_base64 );
assert_test( $legacy_decrypted === $plain, 'Legacy base64 ciphertext decrypts back to original plain text' );

// 5. Edge cases & error handling
assert_test( '' === MessageSettings::encrypt_secret( '' ), 'encrypt_secret("") returns empty string' );
assert_test( '' === MessageSettings::decrypt_secret( '' ), 'decrypt_secret("") returns empty string' );
assert_test( '' === MessageSettings::decrypt_secret( 'garbage-not-valid' ), 'decrypt_secret(garbage) returns empty string' );
assert_test( '' === MessageSettings::decrypt_secret( 'deadbeef001122' ), 'decrypt_secret(tampered_hex) returns empty string' );

echo "\nSummary: {$passed} passed, {$failed} failed.\n";
exit( 0 === $failed ? 0 : 1 );
