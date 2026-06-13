package com.kazemieh.orders.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.order.TrackOrderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrderTrackingViewModel(
    private val trackOrderUseCase: TrackOrderUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OrderTrackingState())
    val state: StateFlow<OrderTrackingState> = _state.asStateFlow()

    fun handleIntent(intent: OrderTrackingIntent) {
        when (intent) {
            is OrderTrackingIntent.LoadTracking -> loadTracking(intent.id)
        }
    }

    private fun loadTracking(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(trackingState = AppResult.Loading) }
            val result = trackOrderUseCase(id)
            _state.update { it.copy(trackingState = result) }
        }
    }
}
