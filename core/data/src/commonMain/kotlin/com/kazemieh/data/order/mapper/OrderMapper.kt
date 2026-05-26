package com.kazemieh.data.order.mapper

import com.kazemieh.network.order.dto.request.*
import com.kazemieh.network.order.dto.response.*
import com.kazemieh.domain.order.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*



fun OrderResponse.toDomain(): Order = Order(
    id = id,
    status = status,
    totalPrice = totalPrice,
    createdAt = createdAt,
    itemCount = itemCount
)

fun OrderDetailResponse.toDomain(): OrderDetail = OrderDetail(
    id = id,
    status = status,
    subtotalPrice = subtotalPrice,
    shippingPrice = shippingPrice,
    totalPrice = totalPrice,
    items = items.map { it.toDomain() },
    address = address?.toString(),
    createdAt = createdAt,
    shippingCarrier = shippingCarrier,
    trackingCode = trackingCode,
    shippedAt = shippedAt,
    deliveredAt = deliveredAt
)

fun OrderItemResponse.toDomain(): OrderItem = OrderItem(
    id = id ?: 0L,
    variantId = variantId,
    qty = qty,
    unitPrice = unitPrice,
    title = title,
    options = options
)
