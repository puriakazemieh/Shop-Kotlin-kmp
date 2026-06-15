package com.kazemieh.data.order.mapper

import com.kazemieh.network.order.dto.response.*
import com.kazemieh.domain.address.Address
import com.kazemieh.domain.order.*

fun OrderResponse.toDomain(): Order = Order(
    id = id,
    status = status,
    subtotalPrice = subtotalPrice,
    shippingPrice = shippingPrice,
    totalPrice = totalPrice,
    createdAt = createdAt
)

fun OrderDetailResponse.toDomain(): OrderDetail = OrderDetail(
    id = id,
    status = status,
    subtotalPrice = subtotalPrice,
    shippingPrice = shippingPrice,
    totalPrice = totalPrice,
    items = items.map { it.toDomain() },
    address = address.toDomain(),
    createdAt = createdAt ?: "",
    shippingCarrier = shippingCarrier,
    trackingCode = trackingCode,
    shippedAt = shippedAt,
    deliveredAt = deliveredAt,
    walletPaidAmount = walletPaidAmount,
    gatewayPaidAmount = gatewayPaidAmount
)

fun AddressSnapshotResponse.toDomain(): Address = Address(
    id = 0, // Snapshot doesn't have an ID
    receiverName = receiverName,
    receiverPhone = receiverPhone,
    country = country,
    province = province,
    city = city,
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    postalCode = postalCode,
    isDefault = false
)

fun OrderItemResponse.toDomain(): OrderItem = OrderItem(
    id = id ?: 0L,
    variantId = variantId,
    qty = qty,
    unitPrice = unitPrice,
    title = title,
    options = options
)

fun OrderTrackingResponse.toDomain(): OrderTracking = OrderTracking(
    id = id,
    status = status,
    trackingCode = trackingCode,
    orderedAt = orderedAt,
    shippedAt = shippedAt
)
