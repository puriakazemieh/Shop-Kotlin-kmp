package com.kazemieh.data.cart.mapper

import com.kazemieh.network.cart.dto.request.*
import com.kazemieh.network.cart.dto.response.*
import com.kazemieh.domain.cart.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*



fun CartResponse.toDomain(): Cart = Cart(
    items = items.map { it.toDomain() },
    savedForLater = savedForLater.map { it.toDomain() },
    subtotal = subtotal,
    totalQty = totalQty,
    discountAmount = discountAmount,
    total = total,
    appliedDiscountCode = appliedDiscountCode
)

fun CartItemResponse.toDomain(): CartItem = CartItem(
    id = id,
    variantId = variantId,
    qty = qty,
    productId = productId,
    productTitle = productTitle,
    productSlug = productSlug,
    imageUrl =  if (imageUrl?.startsWith("http") == true) imageUrl else "${PlatformConfig.baseUrl.removeSuffix("/")}$imageUrl",
    options = options,
    price = price,
    compareAtPrice = compareAtPrice,
    availableQty = availableQty,
    savedForLater = savedForLater,
    isActive = isActive,
    lineTotal = lineTotal
)
