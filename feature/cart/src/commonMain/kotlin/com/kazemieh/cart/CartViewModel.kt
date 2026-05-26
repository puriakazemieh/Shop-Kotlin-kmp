package com.kazemieh.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.common.CartEventBus
import com.kazemieh.domain.cart.Cart
import com.kazemieh.domain.cart.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CartViewModel(
    private val getCartUseCase: GetCartUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val adjustCartVariantQtyUseCase: AdjustCartVariantQtyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CartState())
    val state = _state.asStateFlow()

    private val _effect = Channel<CartEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        observeCart()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeCart() {
        viewModelScope.launch {
            merge(flowOf(Unit), CartEventBus.events)
                .flatMapLatest { getCartUseCase() }
                .collect { result ->
                    _state.update { it.copy(cartState = result) }
                }
        }
    }

    fun handleIntent(intent: CartIntent) {
        when (intent) {
            is CartIntent.Refresh -> refreshCart()
            is CartIntent.AdjustQuantity -> adjustQuantity(intent.variantId, intent.delta)
            is CartIntent.DeleteItem -> deleteCartItem(intent.itemId)
        }
    }

    private fun refreshCart() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            getCartUseCase().collect { result ->
                _state.update { it.copy(cartState = result, isRefreshing = false) }
            }
        }
    }

    private fun deleteCartItem(itemId: Long) {
        viewModelScope.launch {
            when (val result = removeFromCartUseCase(itemId)) {
                is AppResult.Success -> {
                    _state.update { it.copy(cartState = result) }
                }
                is AppResult.Error -> {
                    _effect.send(CartEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    private fun adjustQuantity(variantId: Long, delta: Int) {
        viewModelScope.launch {
            when (val result = adjustCartVariantQtyUseCase(variantId, delta)) {
                is AppResult.Success -> {
                    _state.update { it.copy(cartState = result) }
                }
                is AppResult.Error -> {
                    _effect.send(CartEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}

data class CartState(
    val cartState: AppResult<Cart> = AppResult.Loading,
    val isRefreshing: Boolean = false
)

sealed interface CartIntent {
    data object Refresh : CartIntent
    data class AdjustQuantity(val variantId: Long, val delta: Int) : CartIntent
    data class DeleteItem(val itemId: Long) : CartIntent
}

sealed interface CartEffect {
    data class ShowError(val message: Any) : CartEffect
}
