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
    private val getOrderUseCase: com.kazemieh.domain.order.GetOrderUseCase,
    private val clearCartUseCase: ClearCartUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentState())
    val state = _state.asStateFlow()

    private val _effect = Channel<PaymentEffect>()
    val effect = _effect.receiveAsFlow()

    fun handleIntent(intent: PaymentIntent) {
        when (intent) {
            is PaymentIntent.VerifyPayment -> verifyPayment(intent.orderId)
            is PaymentIntent.OnBackClick -> {
                viewModelScope.launch {
                    _effect.send(PaymentEffect.NavigateBack)
                }
            }
        }
    }

    private fun verifyPayment(orderId: Long?) {
        viewModelScope.launch {
            _state.update { it.copy(result = AppResult.Loading) }
            if (orderId == null) {
                _state.update { it.copy(result = AppResult.Error("شناسه سفارش نامعتبر است")) }
                return@launch
            }
            
            val result = getOrderUseCase(orderId)
            when (result) {
                is AppResult.Success -> {
                    // Check order status. Usually processing/completed means paid
                    val status = result.data.status.lowercase()
                    if (status == "processing" || status == "completed") {
                        // Payment is successful, now clear the cart
                        clearCartUseCase()
                        _state.update { it.copy(result = AppResult.Success(Unit)) }
                    } else {
                        _state.update { it.copy(result = AppResult.Error("پرداخت تایید نشد (وضعیت: ${result.data.status})")) }
                    }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(result = AppResult.Error(result.message)) }
                }
                is AppResult.Loading -> {}
            }
        }
    }
}
