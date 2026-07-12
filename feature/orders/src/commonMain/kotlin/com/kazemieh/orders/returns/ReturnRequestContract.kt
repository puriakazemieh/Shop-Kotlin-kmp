package com.kazemieh.orders.returns

import com.kazemieh.common.AppResult
import com.kazemieh.domain.order.ReturnRequest

data class ReturnRequestState(
    val myRequests: AppResult<List<ReturnRequest>> = AppResult.Loading,
    val isSubmitting: Boolean = false
)

sealed interface ReturnRequestIntent {
    data object LoadMine : ReturnRequestIntent
    data class Submit(val orderItemId: Long, val type: String, val reason: String) : ReturnRequestIntent
}

sealed interface ReturnRequestEffect {
    data class ShowError(val message: Any) : ReturnRequestEffect
    data object Submitted : ReturnRequestEffect
}
