<?php
namespace Carmilla\Bridge\Migration;

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

/**
 * Handles AEAD Encryption, Signature, and Customer Binding for Migration Packs.
 */
class Migration_Crypto {

    /**
     * Encrypts and binds a migration payload to a customer.
     * 
     * @param string \ The raw JSON string (e.g., ndjson data).
     * @param string \ A 256-bit encryption key (32 bytes). Must be provided out-of-band.
     * @param string \ The target customer/site UUID.
     * @param int \ Unix timestamp when this payload expires.
     * @return string Base64 encoded encrypted package.
     */
    public static function encrypt_payload(string \, string \, string \, int \): string {
        \ = openssl_cipher_iv_length('aes-256-gcm');
        \ = openssl_random_pseudo_bytes(\);
        
        // Additional Authenticated Data (AAD) binds the payload to the customer and expiry.
        \ = json_encode(['customer_uuid' => \, 'expiry' => \]);
        
        \ = '';
        \ = openssl_encrypt(\, 'aes-256-gcm', \, OPENSSL_RAW_DATA, \, \, \, 16);
        
        if (\ === false) {
            throw new \Exception('Encryption failed.');
        }
        
        \ = [
            'iv' => base64_encode(\),
            'ciphertext' => base64_encode(\),
            'tag' => base64_encode(\),
            'aad' => base64_encode(\)
        ];
        
        return base64_encode(json_encode(\));
    }

    /**
     * Decrypts and verifies a migration payload.
     * 
     * @param string \ The base64 encoded package.
     * @param string \ The 32-byte encryption key.
     * @param string \ The UUID of the current site receiving the import.
     * @return string The decrypted raw payload.
     * @throws \Exception If tampered, expired, or wrong customer.
     */
    public static function decrypt_payload(string \, string \, string \): string {
        \ = base64_decode(\);
        \ = json_decode(\, true);
        
        if (!\ || empty(\['iv']) || empty(\['ciphertext']) || empty(\['tag']) || empty(\['aad'])) {
            throw new \Exception('Invalid encrypted package format. Potential tampering.');
        }
        
        \ = base64_decode(\['iv']);
        \ = base64_decode(\['ciphertext']);
        \ = base64_decode(\['tag']);
        \ = base64_decode(\['aad']);
        
        \ = json_decode(\, true);
        if (!\ || empty(\['customer_uuid']) || empty(\['expiry'])) {
            throw new \Exception('Invalid AAD data. Potential tampering.');
        }
        
        if (\['customer_uuid'] !== \) {
            throw new \Exception('Wrong customer binding: This package was not encrypted for this site.');
        }
        
        if (time() > \['expiry']) {
            throw new \Exception('Migration package has expired.');
        }
        
        \ = openssl_decrypt(\, 'aes-256-gcm', \, OPENSSL_RAW_DATA, \, \, \);
        
        if (\ === false) {
            throw new \Exception('Decryption or Authentication failed. Payload was tampered with or incorrect key.');
        }
        
        return \;
    }
}
