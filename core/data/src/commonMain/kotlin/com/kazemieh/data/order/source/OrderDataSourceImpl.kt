package com.kazemieh.data.order.source

import com.kazemieh.network.order.OrderApi
import com.kazemieh.network.order.dto.request.*
import com.kazemieh.network.order.dto.response.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*


class OrderDataSourceImpl(private val api: OrderApi) : OrderDataSource {
    override suspend fun listMyOrders(): AppResult<List<OrderResponse>> = safeApiCall { api.listMyOrders() }
    override suspend fun getOrder(id: Long): AppResult<OrderDetailResponse> = safeApiCall { api.getOrder(id) }
    override suspend fun createOrder(request: CreateOrderRequest): AppResult<OrderDetailResponse> = safeApiCall { api.createOrder(request) }
    override suspend fun cancelOrder(id: Long): AppResult<Unit> = safeApiCall { api.cancelOrder(id) }
    override suspend fun trackOrder(id: Long): AppResult<OrderTrackingResponse> = safeApiCall { api.trackOrder(id) }
    override suspend fun reorder(id: Long): AppResult<ReorderResponse> = safeApiCall { api.reorder(id) }
}
