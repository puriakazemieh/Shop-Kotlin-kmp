<?php
namespace Carmilla\Bridge\Migration;

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

/** Common security configuration for Migration. */
class Migration_Config {

    /** Post types that must never leave the source site. */
    public static function get_denied_post_types(): array {
        return array(
            'shop_order',
            'shop_order_refund',
            'shop_coupon',
            'cb_health_record',
            'cb_psych_result',
            'cb_payment',
            'cb_user_secret',
            'attachment',
            'revision',
        );
    }

    /** Meta keys that may contain personal, payment, or authentication data. */
    public static function get_denied_meta_keys(): array {
        return array(
            '_customer_user',
            '_billing_first_name',
            '_billing_last_name',
            '_billing_company',
            '_billing_address_1',
            '_billing_address_2',
            '_billing_city',
            '_billing_postcode',
            '_billing_country',
            '_billing_state',
            '_billing_email',
            '_billing_phone',
            '_shipping_first_name',
            '_shipping_last_name',
            '_shipping_company',
            '_shipping_address_1',
            '_shipping_address_2',
            '_shipping_city',
            '_shipping_postcode',
            '_shipping_country',
            '_shipping_state',
            '_payment_method',
            '_payment_method_title',
            '_transaction_id',
            '_order_key',
            'password',
            'secret',
            'token',
        );
    }

    public static function is_denied_post_type( string $post_type ): bool {
        return in_array( $post_type, self::get_denied_post_types(), true );
    }

    public static function is_denied_meta_key( string $meta_key ): bool {
        $normalized_key = strtolower( $meta_key );

        if ( in_array( $normalized_key, self::get_denied_meta_keys(), true ) ) {
            return true;
        }

        return str_contains( $normalized_key, 'token' )
            || str_contains( $normalized_key, 'password' )
            || str_contains( $normalized_key, 'secret' )
            || str_contains( $normalized_key, 'api_key' );
    }
}
