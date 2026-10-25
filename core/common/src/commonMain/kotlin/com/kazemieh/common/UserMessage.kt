package com.kazemieh.common

import org.jetbrains.compose.resources.StringResource

fun String.toUserMessage(): StringResource {
    return when (this) {
        // Auth & User
        "USER_NOT_FOUND" -> Res.string.userNotFound
        "EMAIL_ALREADY_EXISTS" -> Res.string.emailAlreadyExists
        "USER_INACTIVE" -> Res.string.userInactive
        "INVALID_CREDENTIALS", "BAD_CREDENTIALS" -> Res.string.invalidCredentials
        "ACCESS_DENIED" -> Res.string.accessDenied
        "INVALID_CURRENT_PASSWORD" -> Res.string.invalidCurrentPassword
        "SAME_AS_OLD_PASSWORD" -> Res.string.sameAsOldPassword
        "INVALID_RESET_TOKEN" -> Res.string.invalidResetToken
        "BAD_OTP" -> Res.string.invalidOtp
        "VALIDATION" -> Res.string.validationError
        "WEAK_PASSWORD" -> Res.string.passwordTooShort
        "INVALID_TOKEN", "INVALID_REFRESH_TOKEN" -> Res.string.sessionExpired
        "OTP_UNAVAILABLE" -> Res.string.otpTemporarilyUnavailable
        "RATE_LIMIT" -> Res.string.tryAgainLater
        "REGISTER_FAILED" -> Res.string.registrationFailed
        "UNAUTHORIZED" -> Res.string.authRequired
        "UNAVAILABLE", "GATEWAY_REJECTED", "ORDER_FAILED" -> Res.string.authServiceUnavailable

        // Carmilla Bridge's active generic route errors. Keep this explicit so
        // a newly introduced server code cannot silently become an unknown UI
        // error in the PWA.
        "NOT_FOUND" -> Res.string.userNotFound
        "EMPTY", "EMPTY_CODE", "INVALID_URL", "INVALID_WINDOW", "INVALID_TIME",
        "INVALID_SLOT", "INVALID_STATUS", "INVALID_IMAGE", "INVALID_VIDEO",
        "VARIANT_REQUIRED" -> Res.string.validationError
        "NO_FILE", "UPLOAD_FAILED", "CREATE_FAILED", "UPDATE_FAILED", "CATEGORY_FAILED" -> Res.string.internalError
        "NO_QUIZ", "NO_FREE_SEAT", "WALLET_DISABLED", "FEATURE_DISABLED" -> Res.string.accessDenied
        "WOO_INACTIVE", "WOOCOMMERCE_INACTIVE", "GATEWAY_NOT_CONFIGURED" -> Res.string.authServiceUnavailable
        "EMPTY_CART" -> Res.string.cartIsEmptyError
        "INVALID_COUPON" -> Res.string.invalidDiscountCode
        "SLOT_TAKEN", "STALE_VERSION", "NOT_CANCELLABLE", "ALREADY_PAID" -> Res.string.tryAgainLater

        // Category
        "CATEGORY_NOT_FOUND" -> Res.string.categoryNotFound
        "CATEGORY_SLUG_EXISTS" -> Res.string.categorySlugExists
        "CATEGORY_CYCLE" -> Res.string.categoryCycle

        // Product
        "PRODUCT_NOT_FOUND" -> Res.string.productNotFound
        "PRODUCT_SLUG_EXISTS" -> Res.string.productSlugExists
        "PRODUCT_INACTIVE" -> Res.string.productInactive
        "INVALID_PRODUCT_PRICE" -> Res.string.invalidProductPrice
        "PRODUCT_PRICE_REQUIRED" -> Res.string.productPriceRequired
        "PRODUCT_INVENTORY_REQUIRED" -> Res.string.productInventoryRequired
        "PRODUCT_USED_IN_ORDERS" -> Res.string.productUsedInOrders

        // New Error Codes
        "PRODUCT_NO_ACTIVE_VARIANT" -> Res.string.productNoActiveVariant
        "PRODUCT_MULTIPLE_VARIANTS" -> Res.string.productMultipleVariants
        "MISSING_VARIANT_OR_PRODUCT" -> Res.string.missingVariantOrProduct
        "VARIANT_OPTIONS_REQUIRED" -> Res.string.variantOptionsRequired

        // File Upload
        "INVALID_IMAGE_TYPE" -> Res.string.invalidImageType
        "INVALID_VIDEO_TYPE" -> Res.string.invalidVideoType

        // Size & Color
        "SIZE_NOT_FOUND" -> Res.string.sizeNotFound
        "SIZE_EXISTS" -> Res.string.sizeExists
        "COLOR_NOT_FOUND" -> Res.string.colorNotFound
        "COLOR_EXISTS" -> Res.string.colorExists
        "INVALID_COLOR_HEX" -> Res.string.invalidColorHex

        // Variant
        "VARIANT_NOT_FOUND" -> Res.string.variantNotFound
        "SKU_EXISTS" -> Res.string.skuExists
        "VARIANT_COMBO_EXISTS" -> Res.string.variantComboExists
        "VARIANT_INACTIVE" -> Res.string.variantInactive
        "INVALID_VARIANT_PRICE" -> Res.string.invalidVariantPrice

        // Inventory
        "INVENTORY_NOT_FOUND" -> Res.string.inventoryNotFound
        "INSUFFICIENT_STOCK" -> Res.string.insufficientStock
        "INVENTORY_CONFLICT" -> Res.string.inventoryConflict
        "INVALID_INVENTORY" -> Res.string.invalidInventory

        // Address
        "ADDRESS_NOT_FOUND" -> Res.string.addressNotFound
        "ADDRESS_ACCESS_DENIED" -> Res.string.addressAccessDenied
        "DEFAULT_ADDRESS_CONFLICT" -> Res.string.defaultAddressConflict
        "INVALID_ADDRESS" -> Res.string.invalidAddress

        // Order
        "ORDER_NOT_FOUND" -> Res.string.orderNotFound
        "ORDER_ACCESS_DENIED" -> Res.string.orderAccessDenied
        "EMPTY_ORDER" -> Res.string.emptyOrder
        "INVALID_ORDER_STATUS" -> Res.string.invalidOrderStatus
        "ORDER_STATUS_TRANSITION" -> Res.string.orderStatusTransition
        "ORDER_FINALIZED" -> Res.string.orderFinalized
        "ORDER_PRICE_MISMATCH" -> Res.string.orderPriceMismatch

        // Discount
        "INVALID_INPUT" -> Res.string.invalidInput
        "DISCOUNT_NOT_FOUND" -> Res.string.discountNotFound
        "DISCOUNT_EXPIRED" -> Res.string.discountExpired
        "DISCOUNT_NOT_ACTIVE" -> Res.string.discountNotActive
        "DISCOUNT_LIMIT_EXCEEDED" -> Res.string.discountLimitExceeded
        "DISCOUNT_MIN_AMOUNT_NOT_MET" -> Res.string.discountMinAmountNotMet

        // Order Item
        "ORDER_ITEM_NOT_FOUND" -> Res.string.orderItemNotFound
        "INVALID_QUANTITY" -> Res.string.invalidQuantity
        "INVALID_ORDER_ITEM_PRICE" -> Res.string.invalidOrderItemPrice
        "VARIANT_NOT_AVAILABLE" -> Res.string.variantNotAvailable

        // DB & Generic
        "DATA_INTEGRITY_VIOLATION" -> Res.string.dataIntegrityViolation
        "FOREIGN_KEY_VIOLATION" -> Res.string.foreignKeyViolation
        "UNIQUE_VIOLATION" -> Res.string.uniqueViolation
        "CHECK_VIOLATION" -> Res.string.checkViolation
        "VALIDATION_ERROR" -> Res.string.validationError
        "INVALID_JSON" -> Res.string.invalidJson
        "INTERNAL_ERROR" -> Res.string.internalError

        "OPTION_VALUE_IN_USE" -> Res.string.optionValueInUse
        "OPTION_TYPE_IN_USE" -> Res.string.optionTypeInUse

        // Blog
        "BLOG_NOT_FOUND" -> Res.string.blogNotFound
        "BLOG_SLUG_EXISTS" -> Res.string.blogSlugExists

        "UNKNOWN_ERROR" -> Res.string.invalidServerResponse

        // Wallet
        "INSUFFICIENT_WALLET_BALANCE" -> Res.string.insufficientWalletBalance

        else -> Res.string.invalidServerResponse
    }
}
