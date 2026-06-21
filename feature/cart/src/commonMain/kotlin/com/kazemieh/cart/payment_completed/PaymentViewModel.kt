package com.kazemieh.cart.payment_completed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.cart.ClearCartUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val clearCartUseCase: ClearCartUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentState())
    val state = _state.asStateFlow()

    private val _effect = Channel<PaymentEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        handleIntent(PaymentIntent.ClearCart)
    }

    fun handleIntent(intent: PaymentIntent) {
        when (intent) {
            is PaymentIntent.ClearCart -> clearCart()
            is PaymentIntent.OnBackClick -> {
                viewModelScope.launch {
                    _effect.send(PaymentEffect.NavigateBack)
                }
            }
        }
    }

    private fun clearCart() {
        viewModelScope.launch {
            _state.update { it.copy(result = AppResult.Loading) }
            val result = clearCartUseCase()
            _state.update { it.copy(result = result) }
        }
    }
}
