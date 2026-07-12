package com.kazemieh.data.order.repository

import com.kazemieh.network.order.dto.request.*
import com.kazemieh.network.order.dto.response.*
import com.kazemieh.domain.order.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*
import com.kazemieh.data.cart.mapper.toDomain
import com.kazemieh.data.order.source.OrderDataSource
import com.kazemieh.data.order.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow




class OrderRepositoryImpl(
    private val dataSource: OrderDataSource
) : OrderRepository {

    override fun getMyOrders(): Flow<AppResult<List<Order>>> = flow {
        emit(AppResult.Loading)
        emit(dataSource.listMyOrders().map { it.map { item -> item.toDomain() } })
    }

    override suspend fun getOrder(id: Long): AppResult<OrderDetail> {
        return dataSource.getOrder(id).map { it.toDomain() }
    }

    override suspend fun createOrder(
        items: List<Pair<Long, Int>>,
        addressId: Long?,
        useWallet: Boolean,
        isGift: Boolean,
        giftMessage: String?
    ): AppResult<OrderDetail> {
        val request = CreateOrderRequest(
            items = items.map { OrderItemRequest(it.first, it.second) },
            addressId = addressId,
            useWallet = useWallet,
            isGift = isGift,
            giftMessage = giftMessage
        )
        return dataSource.createOrder(request).map { it.toDomain() }
    }

    override suspend fun cancelOrder(id: Long): AppResult<Unit> {
        return dataSource.cancelOrder(id)
    }

    override suspend fun trackOrder(id: Long): AppResult<OrderTracking> {
        return dataSource.trackOrder(id).map { it.toDomain() }
    }

    override suspend fun reorder(id: Long): AppResult<ReorderResult> {
        return dataSource.reorder(id).map {
            ReorderResult(cart = it.cart.toDomain(), skippedTitles = it.skippedTitles)
        }
    }
}
