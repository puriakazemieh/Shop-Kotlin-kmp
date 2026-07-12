package com.kazemieh.orders.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.order.CancelRecurringOrderUseCase
import com.kazemieh.domain.order.ListMyRecurringOrdersUseCase
import com.kazemieh.domain.order.RecurringOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecurringOrdersState(
    val listState: AppResult<List<RecurringOrder>> = AppResult.Loading
)

class RecurringOrdersViewModel(
    private val listMyRecurringOrdersUseCase: ListMyRecurringOrdersUseCase,
    private val cancelRecurringOrderUseCase: CancelRecurringOrderUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecurringOrdersState())
    val state: StateFlow<RecurringOrdersState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(listState = AppResult.Loading) }
            _state.update { it.copy(listState = listMyRecurringOrdersUseCase()) }
        }
    }

    fun cancel(id: Long) {
        viewModelScope.launch {
            cancelRecurringOrderUseCase(id)
            load()
        }
    }
}
