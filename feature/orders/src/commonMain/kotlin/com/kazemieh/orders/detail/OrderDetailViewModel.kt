package com.kazemieh.orders.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.order.CancelOrderUseCase
import com.kazemieh.domain.order.GetOrderUseCase
import com.kazemieh.domain.order.ReorderUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrderDetailViewModel(
    private val getOrderUseCase: GetOrderUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    private val reorderUseCase: ReorderUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OrderDetailState())
    val state: StateFlow<OrderDetailState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<OrderDetailEffect>()
    val effect: SharedFlow<OrderDetailEffect> = _effect.asSharedFlow()

    fun handleIntent(intent: OrderDetailIntent) {
        when (intent) {
            is OrderDetailIntent.LoadOrderDetail -> loadOrderDetail(intent.id)
            is OrderDetailIntent.CancelOrder -> cancelOrder(intent.id)
            is OrderDetailIntent.TrackOrder -> {
                viewModelScope.launch {
                    _effect.emit(OrderDetailEffect.NavigateToTracking(intent.id))
                }
            }
            is OrderDetailIntent.Reorder -> reorder(intent.id)
        }
    }

    private fun reorder(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isReordering = true) }
            val result = reorderUseCase(id)
            _state.update { it.copy(isReordering = false) }

            when (result) {
                is AppResult.Success -> _effect.emit(OrderDetailEffect.Reordered(result.data.skippedTitles))
                is AppResult.Error -> _effect.emit(OrderDetailEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    private fun loadOrderDetail(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(orderDetailState = AppResult.Loading) }
            val result = getOrderUseCase(id)
            _state.update { it.copy(orderDetailState = result) }
        }
    }

    private fun cancelOrder(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isCancelling = true) }
            val result = cancelOrderUseCase(id)
            _state.update { it.copy(isCancelling = false) }
            
            when (result) {
                is AppResult.Success -> {
                    _effect.emit(OrderDetailEffect.OrderCancelled)
                    loadOrderDetail(id)
                }
                is AppResult.Error -> {
                    _effect.emit(OrderDetailEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
