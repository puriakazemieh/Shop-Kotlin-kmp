package com.kazemieh.admin.orders

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminOrderDetail
import com.kazemieh.domain.model.admin.AdminOrderSummary
import com.kazemieh.domain.repository.AdminPage

data class AdminOrderState(
    val ordersState: AppResult<AdminPage<AdminOrderSummary>> = AppResult.Loading,
    val orderDetailState: AppResult<AdminOrderDetail>? = null,
    val selectedStatus: String? = null,
    val isUpdatingStatus: Boolean = false,
    val showDetailDialog: Boolean = false
)

sealed interface AdminOrderIntent {
    data object LoadOrders : AdminOrderIntent
    data class FilterByStatus(val status: String?) : AdminOrderIntent
    data class ShowOrderDetail(val orderId: Long) : AdminOrderIntent
    data object DismissOrderDetail : AdminOrderIntent
    data class UpdateStatus(val orderId: Long, val status: String) : AdminOrderIntent
}

sealed interface AdminOrderEffect {
    data class ShowError(val message: Any) : AdminOrderEffect
    data object StatusUpdated : AdminOrderEffect
}
