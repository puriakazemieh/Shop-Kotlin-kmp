<?php
namespace Carmilla\Bridge\Migration;

if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

/**
 * Common configuration and utilities for Migration.
 */
class Migration_Config {
    
    /**
     * Strictly blocked Post Types that must NEVER be exported.
     * Prevents leakage of PII, PHI, Orders, and Payments.
     */
    public static function get_denied_post_types(): array {
        return [
            'shop_order',
            'shop_order_refund',
            'shop_coupon',
            'cb_health_record',
            'cb_psych_result',
            'cb_payment',
            'cb_user_secret',
            'attachment', // Handled separately
            'revision'
        ];
    }

    /**
     * Strictly blocked Meta Keys that must NEVER be exported.
     * Prevents leakage of passwords, tokens, or PII.
     */
    public static function get_denied_meta_keys(): array {
        return [
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
            'token'
        ];
    }
}
