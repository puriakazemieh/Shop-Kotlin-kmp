package com.kazemieh.orders.returns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.order.CreateReturnRequestUseCase
import com.kazemieh.domain.order.ListMyReturnRequestsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReturnRequestViewModel(
    private val createReturnRequestUseCase: CreateReturnRequestUseCase,
    private val listMyReturnRequestsUseCase: ListMyReturnRequestsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ReturnRequestState())
    val state: StateFlow<ReturnRequestState> = _state.asStateFlow()

    private val _effect = Channel<ReturnRequestEffect>()
    val effect: Flow<ReturnRequestEffect> = _effect.receiveAsFlow()

    fun handleIntent(intent: ReturnRequestIntent) {
        when (intent) {
            is ReturnRequestIntent.LoadMine -> loadMine()
            is ReturnRequestIntent.Submit -> submit(intent.orderItemId, intent.type, intent.reason)
        }
    }

    private fun loadMine() {
        viewModelScope.launch {
            _state.update { it.copy(myRequests = AppResult.Loading) }
            _state.update { it.copy(myRequests = listMyReturnRequestsUseCase()) }
        }
    }

    private fun submit(orderItemId: Long, type: String, reason: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            when (val result = createReturnRequestUseCase(orderItemId, type, reason)) {
                is AppResult.Success -> {
                    _state.update { it.copy(isSubmitting = false) }
                    _effect.send(ReturnRequestEffect.Submitted)
                    loadMine()
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isSubmitting = false) }
                    _effect.send(ReturnRequestEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
