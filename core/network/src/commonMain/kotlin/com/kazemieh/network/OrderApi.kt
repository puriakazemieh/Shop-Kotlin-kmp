package com.kazemieh.network

import com.kazemieh.network.dto.order.request.*
import com.kazemieh.network.dto.order.response.*

interface OrderApi {
    suspend fun listMyOrders(): List<OrderResponse>
    suspend fun getOrder(id: Long): OrderDetailResponse
    suspend fun createOrder(request: CreateOrderRequest): OrderDetailResponse
    suspend fun cancelOrder(id: Long)
}
