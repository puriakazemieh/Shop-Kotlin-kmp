package com.kazemieh.orders.tracking

import com.kazemieh.common.AppResult
import com.kazemieh.domain.order.OrderTracking

data class OrderTrackingState(
    val trackingState: AppResult<OrderTracking> = AppResult.Loading
)

sealed interface OrderTrackingIntent {
    data class LoadTracking(val id: Long) : OrderTrackingIntent
}
