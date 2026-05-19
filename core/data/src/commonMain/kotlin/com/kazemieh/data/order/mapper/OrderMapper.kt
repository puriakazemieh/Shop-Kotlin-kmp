package com.kazemieh.data.order.mapper

import com.kazemieh.domain.model.Order
import com.kazemieh.domain.model.OrderDetail
import com.kazemieh.domain.model.OrderItem
import com.kazemieh.network.dto.order.response.OrderDetailResponse
import com.kazemieh.network.dto.order.response.OrderItemResponse
import com.kazemieh.network.dto.order.response.OrderResponse

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
    size = size,
    color = color
)
