package com.kazemieh.data.order.source

import com.kazemieh.network.order.dto.request.*
import com.kazemieh.network.order.dto.response.*
import com.kazemieh.common.*




interface OrderDataSource {
    suspend fun listMyOrders(): AppResult<List<OrderResponse>>
    suspend fun getOrder(id: Long): AppResult<OrderDetailResponse>
    suspend fun createOrder(request: CreateOrderRequest): AppResult<OrderDetailResponse>
    suspend fun cancelOrder(id: Long): AppResult<Unit>
    suspend fun trackOrder(id: Long): AppResult<OrderTrackingResponse>
    suspend fun reorder(id: Long): AppResult<ReorderResponse>
}
