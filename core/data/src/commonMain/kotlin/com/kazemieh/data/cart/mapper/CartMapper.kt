package com.kazemieh.data.cart.mapper

import com.kazemieh.network.cart.dto.request.*
import com.kazemieh.network.cart.dto.response.*
import com.kazemieh.domain.cart.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*
import com.kazemieh.config.capabilities.AssetUrlResolver



fun CartResponse.toDomain(assetUrlResolver: AssetUrlResolver): Cart = Cart(
    items = items.map { it.toDomain(assetUrlResolver) },
    savedForLater = savedForLater.map { it.toDomain(assetUrlResolver) },
    subtotal = subtotal,
    totalQty = totalQty,
    discountAmount = discountAmount,
    total = total,
    appliedDiscountCode = appliedDiscountCode,
    updatedAt = updatedAt
)

fun CartItemResponse.toDomain(assetUrlResolver: AssetUrlResolver): CartItem = CartItem(
    id = id,
    variantId = variantId,
    qty = qty,
    productId = productId,
    productTitle = productTitle,
    productSlug = productSlug,
    imageUrl = imageUrl?.let(assetUrlResolver::resolve),
    options = options,
    price = price,
    compareAtPrice = compareAtPrice,
    availableQty = availableQty,
    savedForLater = savedForLater,
    isActive = isActive,
    lineTotal = lineTotal
)
