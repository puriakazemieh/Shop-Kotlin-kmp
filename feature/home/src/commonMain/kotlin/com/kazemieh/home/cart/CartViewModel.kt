package com.kazemieh.home.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Cart
import com.kazemieh.domain.usecase.cart.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(
    private val getCartUseCase: GetCartUseCase,
    private val updateCartItemUseCase: UpdateCartItemUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val adjustCartVariantQtyUseCase: AdjustCartVariantQtyUseCase
) : ViewModel() {

    private val _cartState = MutableStateFlow<AppResult<Cart>>(AppResult.Loading)
    val cartState: StateFlow<AppResult<Cart>> = _cartState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadCart()
    }

    fun loadCart() {
        viewModelScope.launch {
            getCartUseCase().collect {
                _cartState.value = it
            }
        }
    }

    fun refreshCart() {
        viewModelScope.launch {
            _isRefreshing.value = true
            getCartUseCase().collect {
                _cartState.value = it
                _isRefreshing.value = false
            }
        }
    }

    fun updateCartItemQuantity(itemId: Long, quantity: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = updateCartItemUseCase(itemId, quantity)
            handleResult(result, onSuccess, onError)
        }
    }

    fun deleteCartItem(itemId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = removeFromCartUseCase(itemId)
            handleResult(result, onSuccess, onError)
        }
    }

    fun adjustQuantity(variantId: Long, delta: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = adjustCartVariantQtyUseCase(variantId, delta)
            handleResult(result, onSuccess, onError)
        }
    }

    private fun handleResult(result: AppResult<Cart>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        when (result) {
            is AppResult.Success -> {
                _cartState.value = result
                onSuccess()
            }
            is AppResult.Error -> {
                onError(result.message)
            }
            else -> {}
        }
    }
}
