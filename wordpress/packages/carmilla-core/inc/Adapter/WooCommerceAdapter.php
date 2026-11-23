<?php
namespace Carmilla\Core\Adapter;

/**
 * Unified WooCommerce Adapter
 *
 * Provides safe access to WooCommerce products, orders, and cart operations
 * using official WooCommerce CRUD and Store APIs.
 *
 * CRITICAL RULE: NO direct SQL queries ($wpdb). Uses wc_get_orders() and
 * wc_get_order() which automatically support High-Performance Order Storage (HPOS)
 * and legacy post storage identically across Theme and Plugin hosts.
 *
 * Part of Task P04-WPPLUGIN-CODE-008.
 */
if ( ! class_exists( 'Carmilla\\Core\\Adapter\\WooCommerceAdapter', false ) ) {
class WooCommerceAdapter {

    /**
     * Checks if WooCommerce is active and its classes are loaded.
     *
     * @return bool True if WooCommerce is loaded, false otherwise.
     */
    public static function is_available(): bool {
        return class_exists('WooCommerce');
    }

    /**
     * Retrieves a collection of products using official wc_get_products().
     *
     * @param array $args Query arguments passed to wc_get_products().
     * @return \WC_Product[] Array of WC_Product instances or empty array if unavailable.
     */
    public static function get_products(array $args = []) {
        if (!self::is_available() || !function_exists('wc_get_products')) {
            return [];
        }

        return wc_get_products($args);
    }

    /**
     * Retrieves a single product by ID or SKU using official wc_get_product().
     *
     * @param int|string|\WC_Product $id Product ID or object.
     * @return \WC_Product|null WC_Product instance or null if unavailable/not found.
     */
    public static function get_product($id) {
        if (!self::is_available() || !function_exists('wc_get_product')) {
            return null;
        }

        $product = wc_get_product($id);
        return $product instanceof \WC_Product ? $product : null;
    }

    /**
     * Retrieves a collection of orders using official wc_get_orders().
     * Works identically whether HPOS (High-Performance Order Storage) is enabled or disabled.
     *
     * @param array $args Query arguments passed to wc_get_orders().
     * @return \WC_Order[] Array of WC_Order instances or empty array if unavailable.
     */
    public static function get_orders(array $args = []): array {
        if (!self::is_available() || !function_exists('wc_get_orders')) {
            return [];
        }

        return wc_get_orders($args);
    }

    /**
     * Retrieves a single order by ID using official wc_get_order().
     * Works identically whether HPOS is enabled or disabled.
     *
     * @param int|string $id Order ID.
     * @return \WC_Order|null WC_Order instance or null if unavailable/not found.
     */
    public static function get_order($id) {
        if (!self::is_available() || !function_exists('wc_get_order')) {
            return null;
        }

        $order = wc_get_order($id);
        return $order instanceof \WC_Order ? $order : null;
    }

    /**
     * Retrieves the active WooCommerce Cart instance via WC()->cart.
     *
     * @return \WC_Cart|null Active cart or null if unavailable/uninitialized.
     */
    public static function get_cart() {
        if (!self::is_available() || !function_exists('WC')) {
            return null;
        }

        $wc = WC();
        if (!is_object($wc) || empty($wc->cart)) {
            // If running during early REST/CLI requests where cart is not yet loaded,
            // attempt initialization if frontend functions are available.
            if (function_exists('wc_load_cart') && empty($wc->cart)) {
                wc_load_cart();
            }
        }

        return isset($wc->cart) && $wc->cart instanceof \WC_Cart ? $wc->cart : null;
    }

    /**
     * Adds an item to the active WooCommerce cart using WC()->cart->add_to_cart().
     *
     * @param int   $product_id     Product ID to add.
     * @param int   $quantity       Quantity of the product.
     * @param int   $variation_id   Variation ID if variable product.
     * @param array $variation      Variation attributes.
     * @param array $cart_item_data Additional cart item data.
     * @return string|false Cart item key on success, false on failure or if unavailable.
     */
    public static function add_to_cart(
        int $product_id,
        int $quantity = 1,
        int $variation_id = 0,
        array $variation = [],
        array $cart_item_data = []
    ) {
        $cart = self::get_cart();
        if (!$cart) {
            return false;
        }

        try {
            return $cart->add_to_cart($product_id, $quantity, $variation_id, $variation, $cart_item_data);
        } catch (\Throwable $e) {
            return false;
        }
    }

    /**
     * Checks if WooCommerce High-Performance Order Storage (HPOS) is enabled.
     *
     * @return bool True if HPOS is active, false otherwise.
     */
    public static function is_hpos_enabled(): bool {
        if (!self::is_available()) {
            return false;
        }

        if (class_exists('\Automattic\WooCommerce\Utilities\OrderUtil') &&
            method_exists('\Automattic\WooCommerce\Utilities\OrderUtil', 'custom_orders_table_usage_is_enabled')) {
            return \Automattic\WooCommerce\Utilities\OrderUtil::custom_orders_table_usage_is_enabled();
        }

        return get_option('woocommerce_custom_orders_table_enabled') === 'yes';
    }

    public const STORAGE_HPOS = 'hpos';
    public const STORAGE_LEGACY = 'legacy_posts';

    /**
     * Returns the active order storage mechanism ('hpos' or 'legacy_posts').
     *
     * @return string
     */
    public static function get_order_storage_type(): string {
        return self::is_hpos_enabled() ? self::STORAGE_HPOS : self::STORAGE_LEGACY;
    }

    /**
     * Sets HPOS enabled/disabled option in representative test/runtime environments.
     *
     * @param bool $enabled
     * @return bool
     */
    public static function set_hpos_enabled(bool $enabled): bool {
        if (!self::is_available()) {
            return false;
        }
        update_option('woocommerce_custom_orders_table_enabled', $enabled ? 'yes' : 'no');
        return true;
    }

    /**
     * Creates a synthetic or standard product via official WC CRUD.
     *
     * @param array $props Product properties (name, regular_price, status, sku).
     * @return \WC_Product|null Created product instance or null on failure.
     */
    public static function create_product(array $props = []) {
        if (!self::is_available() || !class_exists('\WC_Product_Simple')) {
            return null;
        }

        try {
            $product = new \WC_Product_Simple();
            if (isset($props['name'])) {
                $product->set_name((string) $props['name']);
            }
            if (isset($props['regular_price'])) {
                $product->set_regular_price((string) $props['regular_price']);
            }
            if (isset($props['status'])) {
                $product->set_status((string) $props['status']);
            }
            if (isset($props['sku'])) {
                $product->set_sku((string) $props['sku']);
            }

            $product_id = $product->save();
            return $product_id > 0 ? $product : null;
        } catch (\Throwable $e) {
            return null;
        }
    }

    /**
     * Deletes a product via official WC CRUD.
     *
     * @param int|\WC_Product $id Product ID or object.
     * @param bool            $force Whether to permanently delete (bypass trash).
     * @return bool True on success, false on failure.
     */
    public static function delete_product($id, bool $force = true): bool {
        $product = self::get_product($id);
        if (!$product) {
            return false;
        }

        try {
            return (bool) $product->delete($force);
        } catch (\Throwable $e) {
            return false;
        }
    }

    /**
     * Creates an order via official wc_create_order().
     * Supports both HPOS and legacy post storage without raw SQL.
     *
     * @param array $args Order arguments (customer_id, status, etc.).
     * @return \WC_Order|null Created order instance or null on failure.
     */
    public static function create_order(array $args = []) {
        if (!self::is_available() || !function_exists('wc_create_order')) {
            return null;
        }

        try {
            $order = wc_create_order($args);
            return $order instanceof \WC_Order ? $order : null;
        } catch (\Throwable $e) {
            return null;
        }
    }

    /**
     * Deletes an order via official WC CRUD.
     * Supports both HPOS and legacy post storage without raw SQL.
     *
     * @param int|\WC_Order $id Order ID or object.
     * @param bool          $force Whether to permanently delete.
     * @return bool True on success, false on failure.
     */
    public static function delete_order($id, bool $force = true): bool {
        $order = self::get_order($id);
        if (!$order) {
            return false;
        }

        try {
            return (bool) $order->delete($force);
        } catch (\Throwable $e) {
            return false;
        }
    }

    /**
     * Retrieves active cart items.
     *
     * @return array
     */
    public static function get_cart_items(): array {
        $cart = self::get_cart();
        if (!$cart || !method_exists($cart, 'get_cart')) {
            return [];
        }
        return $cart->get_cart();
    }

    /**
     * Clears the active cart.
     *
     * @return bool
     */
    public static function clear_cart(): bool {
        $cart = self::get_cart();
        if (!$cart || !method_exists($cart, 'empty_cart')) {
            return false;
        }
        $cart->empty_cart();
        return true;
    }

    /**
     * Returns the active cart session storage driver name.
     *
     * @return string 'wc_persistent_usermeta' for authenticated users, 'wc_session' otherwise.
     */
    public static function get_cart_storage_driver(): string {
        if (function_exists('is_user_logged_in') && is_user_logged_in()) {
            return 'wc_persistent_usermeta';
        }
        return 'wc_session';
    }
}
}
