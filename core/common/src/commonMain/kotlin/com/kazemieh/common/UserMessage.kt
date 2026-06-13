package com.kazemieh.common

import com.kazemieh.common.Res
import org.jetbrains.compose.resources.StringResource

fun String.toUserMessage(): StringResource {
    return when (this) {
        // Auth & User
        "USER_NOT_FOUND" -> Res.string.user_not_found
        "EMAIL_ALREADY_EXISTS" -> Res.string.email_already_exists
        "USER_INACTIVE" -> Res.string.user_inactive
        "INVALID_CREDENTIALS" -> Res.string.invalid_credentials
        "ACCESS_DENIED" -> Res.string.access_denied
        "INVALID_CURRENT_PASSWORD" -> Res.string.invalid_current_password
        "SAME_AS_OLD_PASSWORD" -> Res.string.same_as_old_password
        "INVALID_RESET_TOKEN" -> Res.string.invalid_reset_token

        // Category
        "CATEGORY_NOT_FOUND" -> Res.string.category_not_found
        "CATEGORY_SLUG_EXISTS" -> Res.string.category_slug_exists
        "CATEGORY_CYCLE" -> Res.string.category_cycle

        // Product
        "PRODUCT_NOT_FOUND" -> Res.string.product_not_found
        "PRODUCT_SLUG_EXISTS" -> Res.string.product_slug_exists
        "PRODUCT_INACTIVE" -> Res.string.product_inactive
        "INVALID_PRODUCT_PRICE" -> Res.string.invalid_product_price

        // Size & Color
        "SIZE_NOT_FOUND" -> Res.string.size_not_found
        "SIZE_EXISTS" -> Res.string.size_exists
        "COLOR_NOT_FOUND" -> Res.string.color_not_found
        "COLOR_EXISTS" -> Res.string.color_exists
        "INVALID_COLOR_HEX" -> Res.string.invalid_color_hex

        // Variant
        "VARIANT_NOT_FOUND" -> Res.string.variant_not_found
        "SKU_EXISTS" -> Res.string.sku_exists
        "VARIANT_COMBO_EXISTS" -> Res.string.variant_combo_exists
        "VARIANT_INACTIVE" -> Res.string.variant_inactive
        "INVALID_VARIANT_PRICE" -> Res.string.invalid_variant_price

        // Inventory
        "INVENTORY_NOT_FOUND" -> Res.string.inventory_not_found
        "INSUFFICIENT_STOCK" -> Res.string.insufficient_stock
        "INVENTORY_CONFLICT" -> Res.string.inventory_conflict
        "INVALID_INVENTORY" -> Res.string.invalid_inventory

        // Address
        "ADDRESS_NOT_FOUND" -> Res.string.address_not_found
        "ADDRESS_ACCESS_DENIED" -> Res.string.address_access_denied
        "DEFAULT_ADDRESS_CONFLICT" -> Res.string.default_address_conflict
        "INVALID_ADDRESS" -> Res.string.invalid_address

        // Order
        "ORDER_NOT_FOUND" -> Res.string.order_not_found
        "ORDER_ACCESS_DENIED" -> Res.string.order_access_denied
        "EMPTY_ORDER" -> Res.string.empty_order
        "INVALID_ORDER_STATUS" -> Res.string.invalid_order_status
        "ORDER_STATUS_TRANSITION" -> Res.string.order_status_transition
        "ORDER_FINALIZED" -> Res.string.order_finalized
        "ORDER_PRICE_MISMATCH" -> Res.string.order_price_mismatch

        // Discount
        "INVALID_INPUT" -> Res.string.invalid_input
        "DISCOUNT_NOT_FOUND" -> Res.string.discount_not_found
        "DISCOUNT_EXPIRED" -> Res.string.discount_expired
        "DISCOUNT_NOT_ACTIVE" -> Res.string.discount_not_active
        "DISCOUNT_LIMIT_EXCEEDED" -> Res.string.discount_limit_exceeded
        "DISCOUNT_MIN_AMOUNT_NOT_MET" -> Res.string.discount_min_amount_not_met

        // Order Item
        "ORDER_ITEM_NOT_FOUND" -> Res.string.order_item_not_found
        "INVALID_QUANTITY" -> Res.string.invalid_quantity
        "INVALID_ORDER_ITEM_PRICE" -> Res.string.invalid_order_item_price
        "VARIANT_NOT_AVAILABLE" -> Res.string.variant_not_available

        // DB & Generic
        "DATA_INTEGRITY_VIOLATION" -> Res.string.data_integrity_violation
        "FOREIGN_KEY_VIOLATION" -> Res.string.foreign_key_violation
        "UNIQUE_VIOLATION" -> Res.string.unique_violation
        "CHECK_VIOLATION" -> Res.string.check_violation
        "VALIDATION_ERROR" -> Res.string.validation_error
        "INVALID_JSON" -> Res.string.invalid_json
        "INTERNAL_ERROR" -> Res.string.internal_error

        "OPTION_VALUE_IN_USE" -> Res.string.option_value_in_use
        "OPTION_TYPE_IN_USE" -> Res.string.option_type_in_use

        "UNKNOWN_ERROR" -> Res.string.unknown_error

        else -> Res.string.unknown_error
    }
}
