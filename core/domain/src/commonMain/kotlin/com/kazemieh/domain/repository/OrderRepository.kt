package com.kazemieh.domain.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Order
import com.kazemieh.domain.model.OrderDetail
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getMyOrders(): Flow<AppResult<List<Order>>>
    suspend fun getOrder(id: Long): AppResult<OrderDetail>
    suspend fun createOrder(items: List<Pair<Long, Int>>, addressId: Long? = null): AppResult<OrderDetail>
    suspend fun cancelOrder(id: Long): AppResult<Unit>
}
