package com.kazemieh.network.order

import com.kazemieh.network.order.dto.request.*
import com.kazemieh.network.order.dto.response.*

import com.kazemieh.network.common.PageResponse

import com.kazemieh.network.order.dto.*
import com.kazemieh.network.order.dto.*

interface OrderApi {
    suspend fun listMyOrders(): List<OrderResponse>
    suspend fun getOrder(id: Long): OrderDetailResponse
    suspend fun createOrder(request: CreateOrderRequest): OrderDetailResponse
    suspend fun cancelOrder(id: Long)
}
