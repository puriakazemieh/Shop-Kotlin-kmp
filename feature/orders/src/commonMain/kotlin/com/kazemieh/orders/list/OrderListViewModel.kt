package com.kazemieh.orders.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.order.GetMyOrdersUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrderListViewModel(
    private val getMyOrdersUseCase: GetMyOrdersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OrderListState())
    val state: StateFlow<OrderListState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<OrderListEffect>()
    val effect: SharedFlow<OrderListEffect> = _effect.asSharedFlow()

    init {
        handleIntent(OrderListIntent.LoadOrders)
    }

    fun handleIntent(intent: OrderListIntent) {
        when (intent) {
            OrderListIntent.LoadOrders -> loadOrders()
            is OrderListIntent.OnOrderClick -> {
                viewModelScope.launch {
                    _effect.emit(OrderListEffect.NavigateToDetail(intent.orderId))
                }
            }
        }
    }

    private fun loadOrders() {
        viewModelScope.launch {
            getMyOrdersUseCase().collect { result ->
                _state.update { it.copy(ordersState = result) }
            }
        }
    }
}
