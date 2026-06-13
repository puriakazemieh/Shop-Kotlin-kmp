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
    private val adjustCartVariantQtyUseCase: AdjustCartVariantQtyUseCase,
    private val moveToSaveForLaterUseCase: MoveToSaveForLaterUseCase,
    private val moveToCartUseCase: MoveToCartUseCase,
    private val applyDiscountUseCase: ApplyDiscountUseCase,
    private val removeDiscountUseCase: RemoveDiscountUseCase
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
            is CartIntent.MoveToSaveForLater -> moveToSaveForLater(intent.itemId)
            is CartIntent.MoveToCart -> moveToCart(intent.itemId)
            is CartIntent.ApplyDiscount -> applyDiscount(intent.code)
            is CartIntent.RemoveDiscount -> removeDiscount()
        }
    }

    private fun applyDiscount(code: String) {
        if (code.isBlank()) {
            viewModelScope.launch {
                _effect.send(CartEffect.ShowError(com.kazemieh.common.Res.string.invalid_input))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isApplyingDiscount = true) }
            when (val result = applyDiscountUseCase(code.uppercase().trim())) {
                is AppResult.Success -> {
                    _state.update { it.copy(cartState = result, isApplyingDiscount = false) }
                }

                is AppResult.Error -> {
                    _effect.send(CartEffect.ShowError(result.message))
                    _state.update { it.copy(isApplyingDiscount = false) }
                }

                else -> {
                    _state.update { it.copy(isApplyingDiscount = false) }
                }
            }
        }
    }

    private fun removeDiscount() {
        viewModelScope.launch {
            _state.update { it.copy(isApplyingDiscount = true) }
            when (val result = removeDiscountUseCase()) {
                is AppResult.Success -> {
                    _state.update { it.copy(cartState = result, isApplyingDiscount = false) }
                }

                is AppResult.Error -> {
                    _effect.send(CartEffect.ShowError(result.message))
                    _state.update { it.copy(isApplyingDiscount = false) }
                }

                else -> {
                    _state.update { it.copy(isApplyingDiscount = false) }
                }
            }
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

    private fun moveToSaveForLater(itemId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            when (val result = moveToSaveForLaterUseCase(itemId)) {
                is AppResult.Success -> {
                    _state.update { it.copy(cartState = result, isRefreshing = false) }
                }
                is AppResult.Error -> {
                    _effect.send(CartEffect.ShowError(result.message))
                    _state.update { it.copy(isRefreshing = false) }
                }
                else -> {
                    _state.update { it.copy(isRefreshing = false) }
                }
            }
        }
    }

    private fun moveToCart(itemId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            when (val result = moveToCartUseCase(itemId)) {
                is AppResult.Success -> {
                    _state.update { it.copy(cartState = result, isRefreshing = false) }
                }
                is AppResult.Error -> {
                    _effect.send(CartEffect.ShowError(result.message))
                    _state.update { it.copy(isRefreshing = false) }
                }
                else -> {
                    _state.update { it.copy(isRefreshing = false) }
                }
            }
        }
    }
}

data class CartState(
    val cartState: AppResult<Cart> = AppResult.Loading,
    val isRefreshing: Boolean = false,
    val isApplyingDiscount: Boolean = false
)

sealed interface CartIntent {
    data object Refresh : CartIntent
    data class AdjustQuantity(val variantId: Long, val delta: Int) : CartIntent
    data class DeleteItem(val itemId: Long) : CartIntent
    data class MoveToSaveForLater(val itemId: Long) : CartIntent
    data class MoveToCart(val itemId: Long) : CartIntent
    data class ApplyDiscount(val code: String) : CartIntent
    data object RemoveDiscount : CartIntent
}

sealed interface CartEffect {
    data class ShowError(val message: Any) : CartEffect
}
