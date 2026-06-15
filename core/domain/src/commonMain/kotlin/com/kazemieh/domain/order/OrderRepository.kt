package com.kazemieh.domain.order

import com.kazemieh.common.AppResult
import com.kazemieh.domain.order.Order
import com.kazemieh.domain.order.OrderDetail
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getMyOrders(): Flow<AppResult<List<Order>>>
    suspend fun getOrder(id: Long): AppResult<OrderDetail>
    suspend fun createOrder(items: List<Pair<Long, Int>>, addressId: Long? = null, useWallet: Boolean = false): AppResult<OrderDetail>
    suspend fun cancelOrder(id: Long): AppResult<Unit>
    suspend fun trackOrder(id: Long): AppResult<OrderTracking>
}
