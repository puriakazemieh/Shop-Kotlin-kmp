package com.kazemieh.orders.detail

import com.kazemieh.common.AppResult
import com.kazemieh.domain.order.OrderDetail

data class OrderDetailState(
    val orderDetailState: AppResult<OrderDetail> = AppResult.Loading,
    val isCancelling: Boolean = false
)

sealed interface OrderDetailIntent {
    data class LoadOrderDetail(val id: Long) : OrderDetailIntent
    data class CancelOrder(val id: Long) : OrderDetailIntent
    data class TrackOrder(val id: Long) : OrderDetailIntent
}

sealed interface OrderDetailEffect {
    data class ShowError(val message: Any) : OrderDetailEffect
    data object OrderCancelled : OrderDetailEffect
    data class NavigateToTracking(val id: Long) : OrderDetailEffect
}
