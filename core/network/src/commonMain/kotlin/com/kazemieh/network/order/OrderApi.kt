package com.kazemieh.network.order

import com.kazemieh.network.order.dto.request.*
import com.kazemieh.network.order.dto.response.*

interface OrderApi {
    suspend fun listMyOrders(): List<OrderResponse>
    suspend fun getOrder(id: Long): OrderDetailResponse
    suspend fun createOrder(request: CreateOrderRequest): OrderDetailResponse
    suspend fun cancelOrder(id: Long)
    suspend fun trackOrder(id: Long): OrderTrackingResponse
    suspend fun reorder(id: Long): ReorderResponse
}
