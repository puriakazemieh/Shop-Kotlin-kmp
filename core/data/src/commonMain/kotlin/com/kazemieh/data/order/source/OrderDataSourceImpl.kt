package com.kazemieh.data.order.source

import com.kazemieh.common.AppResult
import com.kazemieh.network.OrderApi
import com.kazemieh.network.dto.order.request.CreateOrderRequest
import com.kazemieh.network.dto.order.response.OrderDetailResponse
import com.kazemieh.network.dto.order.response.OrderResponse
import com.kazemieh.network.safeApiCall

class OrderDataSourceImpl(private val api: OrderApi) : OrderDataSource {
    override suspend fun listMyOrders(): AppResult<List<OrderResponse>> = safeApiCall { api.listMyOrders() }
    override suspend fun getOrder(id: Long): AppResult<OrderDetailResponse> = safeApiCall { api.getOrder(id) }
    override suspend fun createOrder(request: CreateOrderRequest): AppResult<OrderDetailResponse> = safeApiCall { api.createOrder(request) }
    override suspend fun cancelOrder(id: Long): AppResult<Unit> = safeApiCall { api.cancelOrder(id) }
}
