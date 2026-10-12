<?php
namespace Carmilla\Bridge\Migration;

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

/** Encrypts migration packages with AES-256-GCM and binds them to one site. */
class Migration_Crypto {

    public static function encrypt_payload( string $payload, string $encryption_key, string $customer_uuid, int $expiry ): string {
        self::validate_inputs( $encryption_key, $customer_uuid, $expiry );

        $iv_length = openssl_cipher_iv_length( 'aes-256-gcm' );
        if ( false === $iv_length ) {
            throw new \RuntimeException( 'AES-256-GCM is unavailable in this PHP runtime.' );
        }

        $iv  = random_bytes( $iv_length );
        $aad = wp_json_encode(
            array( 'customer_uuid' => $customer_uuid, 'expiry' => $expiry ),
            JSON_THROW_ON_ERROR
        );
        $tag = '';
        $ciphertext = openssl_encrypt(
            $payload,
            'aes-256-gcm',
            $encryption_key,
            OPENSSL_RAW_DATA,
            $iv,
            $tag,
            $aad,
            16
        );
        if ( false === $ciphertext ) {
            throw new \RuntimeException( 'Migration package encryption failed.' );
        }

        return base64_encode(
            wp_json_encode(
                array(
                    'version'    => 1,
                    'iv'         => base64_encode( $iv ),
                    'ciphertext' => base64_encode( $ciphertext ),
                    'tag'        => base64_encode( $tag ),
                    'aad'        => base64_encode( $aad ),
                ),
                JSON_THROW_ON_ERROR
            )
        );
    }

    public static function decrypt_payload( string $package, string $encryption_key, string $customer_uuid ): string {
        if ( '' === $customer_uuid ) {
            throw new \InvalidArgumentException( 'A customer UUID is required to decrypt a migration package.' );
        }
        if ( 32 !== strlen( $encryption_key ) ) {
            throw new \InvalidArgumentException( 'Migration encryption keys must be exactly 32 bytes.' );
        }

        $decoded_package = base64_decode( $package, true );
        $data = false === $decoded_package ? null : json_decode( $decoded_package, true );
        if ( ! is_array( $data ) || ! isset( $data['iv'], $data['ciphertext'], $data['tag'], $data['aad'] ) ) {
            throw new \RuntimeException( 'Invalid encrypted migration package.' );
        }

        $iv         = base64_decode( $data['iv'], true );
        $ciphertext = base64_decode( $data['ciphertext'], true );
        $tag        = base64_decode( $data['tag'], true );
        $aad        = base64_decode( $data['aad'], true );
        if ( false === $iv || false === $ciphertext || false === $tag || false === $aad ) {
            throw new \RuntimeException( 'Invalid migration package encoding.' );
        }

        $binding = json_decode( $aad, true );
        if ( ! is_array( $binding ) || ! isset( $binding['customer_uuid'], $binding['expiry'] ) ) {
            throw new \RuntimeException( 'Invalid migration package binding.' );
        }
        if ( ! hash_equals( (string) $binding['customer_uuid'], $customer_uuid ) ) {
            throw new \RuntimeException( 'This migration package belongs to another site.' );
        }
        if ( time() > (int) $binding['expiry'] ) {
            throw new \RuntimeException( 'This migration package has expired.' );
        }

        $payload = openssl_decrypt(
            $ciphertext,
            'aes-256-gcm',
            $encryption_key,
            OPENSSL_RAW_DATA,
            $iv,
            $tag,
            $aad
        );
        if ( false === $payload ) {
            throw new \RuntimeException( 'Migration package authentication failed.' );
        }

        return $payload;
    }

    private static function validate_inputs( string $encryption_key, string $customer_uuid, int $expiry ): void {
        if ( 32 !== strlen( $encryption_key ) ) {
            throw new \InvalidArgumentException( 'Migration encryption keys must be exactly 32 bytes.' );
        }
        if ( '' === $customer_uuid ) {
            throw new \InvalidArgumentException( 'A customer UUID is required for migration encryption.' );
        }
        if ( $expiry <= time() ) {
            throw new \InvalidArgumentException( 'Migration package expiry must be in the future.' );
        }
    }
}
