<?php
/**
 * Plugin Name:       Carmilla Bridge
 * Plugin URI:        https://github.com/puriakazemieh/Shop-Kotlin-kmp
 * Description:       REST bridge that exposes WordPress + WooCommerce content. Includes Shared Core for headless support.
 * Version:           1.0.0
 * Requires at least: 6.3
 * Requires PHP:      7.4
 * Author:            Carmilla
 * License:           GPL-2.0-or-later
 * Text Domain:       carmilla-bridge
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

// 1. Load Shared Core if not already loaded by the Theme
if ( ! class_exists( 'Carmilla_Kernel' ) ) {
    $core_path = plugin_dir_path( __FILE__ ) . 'packages/carmilla-core/Carmilla_Kernel.php';
    if ( ! file_exists( $core_path ) ) {
        $core_path = dirname( plugin_dir_path( __FILE__ ) ) . '/packages/carmilla-core/Carmilla_Kernel.php';
    }
    if ( file_exists( $core_path ) ) {
        require_once $core_path;
    }
}

// 2. Load standalone bootloader
require_once plugin_dir_path( __FILE__ ) . 'includes/class-cb-standalone-boot.php';

// 3. Initialize Plugin-specific functionality
add_action('plugins_loaded', 'carmilla_bridge_init');
function carmilla_bridge_init() {
    if ( class_exists( 'Carmilla_Kernel' ) ) {
        \Carmilla_Kernel::boot('plugin', '1.0.0');
    }
}
