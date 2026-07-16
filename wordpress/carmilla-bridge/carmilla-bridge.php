<?php
/**
 * Plugin Name:       Carmilla Bridge
 * Plugin URI:        https://github.com/puriakazemieh/Shop-Kotlin-kmp
 * Description:       REST bridge that exposes WordPress + WooCommerce content (products, articles, stories, banners, campaigns) to the Carmilla KMP client using the same API contract the app already speaks. Adds JWT auth so the mobile/desktop app can read and manage content directly on WordPress.
 * Version:           0.4.0
 * Requires at least: 6.3
 * Requires PHP:      7.4
 * Author:            Carmilla
 * License:           GPL-2.0-or-later
 * Text Domain:       carmilla-bridge
 *
 * The app talks to base URL  https://<site>/wp-json/carmilla/v1/
 * and keeps calling relative paths like  api/products , api/blogs , api/auth/login .
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit; // No direct access.
}

define( 'CB_VERSION', '0.4.0' );
define( 'CB_PLUGIN_FILE', __FILE__ );
define( 'CB_PLUGIN_DIR', plugin_dir_path( __FILE__ ) );
define( 'CB_PLUGIN_URL', plugin_dir_url( __FILE__ ) );

// REST namespace the app points its baseUrl at: /wp-json/carmilla/v1/
define( 'CB_REST_NAMESPACE', 'carmilla/v1' );

require_once CB_PLUGIN_DIR . 'includes/helpers.php';
require_once CB_PLUGIN_DIR . 'includes/class-cb-jwt.php';
require_once CB_PLUGIN_DIR . 'includes/class-cb-blocks.php';
require_once CB_PLUGIN_DIR . 'includes/class-cb-cpt.php';
require_once CB_PLUGIN_DIR . 'includes/class-cb-auth-controller.php';
require_once CB_PLUGIN_DIR . 'includes/class-cb-catalog-controller.php';
require_once CB_PLUGIN_DIR . 'includes/class-cb-blog-controller.php';
require_once CB_PLUGIN_DIR . 'includes/class-cb-media-controller.php';
// Phase 2: full commerce (cart, orders, payment, wallet, account, interactions).
require_once CB_PLUGIN_DIR . 'includes/class-cb-cart-controller.php';
require_once CB_PLUGIN_DIR . 'includes/class-cb-order-controller.php';
require_once CB_PLUGIN_DIR . 'includes/class-cb-payment-controller.php';
require_once CB_PLUGIN_DIR . 'includes/class-cb-wallet-controller.php';
require_once CB_PLUGIN_DIR . 'includes/class-cb-account-controller.php';
require_once CB_PLUGIN_DIR . 'includes/class-cb-interaction-controller.php';
// Phase 3: academy (courses, lessons, enrollment, quiz, certificates, project).
require_once CB_PLUGIN_DIR . 'includes/class-cb-academy-controller.php';
// Phase 4: clinic (therapists, atomic booking, credits) + psychological tests.
require_once CB_PLUGIN_DIR . 'includes/class-cb-clinic-controller.php';
require_once CB_PLUGIN_DIR . 'includes/class-cb-psychtest-controller.php';
require_once CB_PLUGIN_DIR . 'includes/class-cb-plugin.php';

/**
 * Boot the plugin.
 */
function carmilla_bridge() {
	return CB_Plugin::instance();
}
carmilla_bridge();

// Register CPTs + custom tables on activation and flush rewrite rules.
register_activation_hook( __FILE__, function () {
	CB_CPT::register();
	cb_create_tables();
	flush_rewrite_rules();
} );

register_deactivation_hook( __FILE__, function () {
	flush_rewrite_rules();
} );
