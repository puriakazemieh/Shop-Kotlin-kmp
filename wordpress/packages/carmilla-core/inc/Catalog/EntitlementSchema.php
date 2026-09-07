<?php
namespace Carmilla\Core\Catalog;

/**
 * Entitlement Schema & SKU Inventory
 * Defines the canonical catalog of capabilities, features, and SKUs (products)
 * available across Carmilla Bridge and Carmilla Theme.
 */
class EntitlementSchema {
    
    /**
     * Canonical list of all known SKUs / Vertical Products.
     * Each product has an ID, required capabilities, and dependent features.
     */
    public static function get_sku_inventory() {
        return [
            'sku_commerce' => [
                'id' => 'sku_commerce',
                'name' => 'Carmilla Commerce Base',
                'features' => ['commerce.core', 'commerce.cart', 'commerce.payment'],
                'type' => 'base',
                'description' => 'Base e-commerce capabilities.'
            ],
            'sku_academy' => [
                'id' => 'sku_academy',
                'name' => 'Carmilla Academy',
                'features' => ['academy.core', 'academy.courses'],
                'requires' => ['sku_commerce'],
                'type' => 'vertical',
                'description' => 'LMS and academy features.'
            ],
            'sku_clinic' => [
                'id' => 'sku_clinic',
                'name' => 'Carmilla Clinic & Booking',
                'features' => ['clinic.booking', 'clinic.therapists'],
                'requires' => ['sku_commerce'],
                'type' => 'vertical',
                'description' => 'Booking and appointment management.'
            ],
            'sku_psych' => [
                'id' => 'sku_psych',
                'name' => 'Carmilla Psych Tests',
                'features' => ['psych.tests', 'psych.results'],
                'type' => 'vertical',
                'description' => 'Psychological tests engine.'
            ],
            'sku_app_builder' => [
                'id' => 'sku_app_builder',
                'name' => 'Carmilla App Builder',
                'features' => ['builder.ui', 'builder.config'],
                'type' => 'addon',
                'description' => 'Mobile App Builder capabilities.'
            ]
        ];
    }

    /**
     * Maps fine-grained features to their default states and dependencies.
     */
    public static function get_feature_manifest() {
        return [
            'commerce.core'    => ['default' => true,  'label' => 'Core Commerce Engine'],
            'commerce.cart'    => ['default' => true,  'label' => 'Shopping Cart'],
            'commerce.payment' => ['default' => true,  'label' => 'Payment Gateways'],
            'academy.core'     => ['default' => false, 'label' => 'Academy Core Engine'],
            'academy.courses'  => ['default' => false, 'label' => 'Course Management'],
            'clinic.booking'   => ['default' => false, 'label' => 'Booking System'],
            'clinic.therapists'=> ['default' => false, 'label' => 'Therapist Management'],
            'psych.tests'      => ['default' => false, 'label' => 'Psychological Tests'],
            'psych.results'    => ['default' => false, 'label' => 'Test Results & Analysis'],
            'builder.ui'       => ['default' => false, 'label' => 'App Builder UI'],
            'builder.config'   => ['default' => false, 'label' => 'App Builder Configurator']
        ];
    }
}
