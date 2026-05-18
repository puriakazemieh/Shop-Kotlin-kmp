package com.kazemieh.data.order.source

import com.kazemieh.common.AppResult
import com.kazemieh.network.dto.order.request.CreateOrderRequest
import com.kazemieh.network.dto.order.response.OrderDetailResponse
import com.kazemieh.network.dto.order.response.OrderResponse

interface OrderDataSource {
    suspend fun listMyOrders(): AppResult<List<OrderResponse>>
    suspend fun getOrder(id: Long): AppResult<OrderDetailResponse>
    suspend fun createOrder(request: CreateOrderRequest): AppResult<OrderDetailResponse>
    suspend fun cancelOrder(id: Long): AppResult<Unit>
}
