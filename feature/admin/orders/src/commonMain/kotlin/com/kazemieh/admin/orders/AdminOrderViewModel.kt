package com.kazemieh.admin.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.GetAdminOrderDetailUseCase
import com.kazemieh.domain.admin.ListAdminOrdersUseCase
import com.kazemieh.domain.admin.UpdateAdminOrderStatusUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminOrderViewModel(
    private val listAdminOrdersUseCase: ListAdminOrdersUseCase,
    private val getAdminOrderDetailUseCase: GetAdminOrderDetailUseCase,
    private val updateAdminOrderStatusUseCase: UpdateAdminOrderStatusUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminOrderState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AdminOrderEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        handleIntent(AdminOrderIntent.LoadOrders)
    }

    fun handleIntent(intent: AdminOrderIntent) {
        when (intent) {
            is AdminOrderIntent.LoadOrders -> loadOrders()
            is AdminOrderIntent.FilterByStatus -> {
                _state.update { it.copy(selectedStatus = intent.status) }
                loadOrders()
            }
            is AdminOrderIntent.ShowOrderDetail -> loadOrderDetail(intent.orderId)
            is AdminOrderIntent.DismissOrderDetail -> {
                _state.update { it.copy(showDetailDialog = false, orderDetailState = null) }
            }
            is AdminOrderIntent.UpdateStatus -> updateStatus(intent.orderId, intent.status)
        }
    }

    private fun loadOrders() {
        viewModelScope.launch {
            _state.update { it.copy(ordersState = AppResult.Loading) }
            val result = listAdminOrdersUseCase(
                status = _state.value.selectedStatus,
                page = 0,
                size = 100
            )
            _state.update { it.copy(ordersState = result) }
        }
    }

    private fun loadOrderDetail(orderId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(showDetailDialog = true, orderDetailState = AppResult.Loading) }
            val result = getAdminOrderDetailUseCase(orderId)
            _state.update { it.copy(orderDetailState = result) }
        }
    }

    private fun updateStatus(orderId: Long, status: String) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdatingStatus = true) }
            when (val result = updateAdminOrderStatusUseCase(orderId, status)) {
                is AppResult.Success -> {
                    _effect.send(AdminOrderEffect.StatusUpdated)
                    loadOrders()
                    if (_state.value.showDetailDialog) {
                        loadOrderDetail(orderId)
                    }
                }
                is AppResult.Error -> {
                    _effect.send(AdminOrderEffect.ShowError(result.message))
                }
                else -> {}
            }
            _state.update { it.copy(isUpdatingStatus = false) }
        }
    }
}
