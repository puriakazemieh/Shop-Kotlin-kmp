package com.kazemieh.data.cart.mapper

import com.kazemieh.domain.model.Cart
import com.kazemieh.domain.model.CartItem
import com.kazemieh.network.dto.cart.response.CartItemResponse
import com.kazemieh.network.dto.cart.response.CartResponse

fun CartResponse.toDomain(): Cart = Cart(
    items = items.map { it.toDomain() },
    subtotal = subtotal,
    totalQty = totalQty
)

fun CartItemResponse.toDomain(): CartItem = CartItem(
    id = id,
    variantId = variantId,
    qty = qty,
    productId = productId,
    productTitle = productTitle,
    productSlug = productSlug,
    imageUrl = imageUrl,
    options = options,
    price = price,
    compareAtPrice = compareAtPrice,
    availableQty = availableQty,
    isActive = isActive,
    lineTotal = lineTotal
)
