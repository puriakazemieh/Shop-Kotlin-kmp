package com.kazemieh.orders.list

import com.kazemieh.common.AppResult
import com.kazemieh.domain.order.Order

data class OrderListState(
    val ordersState: AppResult<List<Order>> = AppResult.Loading
)

sealed interface OrderListIntent {
    data object LoadOrders : OrderListIntent
    data class OnOrderClick(val orderId: Long) : OrderListIntent
    data class OnReorderClick(val orderId: Long) : OrderListIntent
}

sealed interface OrderListEffect {
    data class NavigateToDetail(val orderId: Long) : OrderListEffect
    data class ReorderDone(val skippedTitles: List<String>) : OrderListEffect
    data class ReorderFailed(val message: Any) : OrderListEffect
}
