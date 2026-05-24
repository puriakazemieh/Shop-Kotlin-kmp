package com.kazemieh.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.usecase.IsUserLoggedInUseCase
import com.kazemieh.domain.usecase.cart.AddToCartUseCase
import com.kazemieh.domain.usecase.catalog.GetProductDetailUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DetailsState())
    val state: StateFlow<DetailsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<DetailsEffect>()
    val effect: SharedFlow<DetailsEffect> = _effect.asSharedFlow()

    fun onIntent(intent: DetailsIntent) {
        when (intent) {
            is DetailsIntent.LoadProduct -> loadProduct(intent.slug)
            is DetailsIntent.UpdateQuantity -> {
                val newQty = intent.quantity
                if (newQty <= 0) {
                    _state.update { it.copy(quantity = 1, isAddedToCart = false) }
                    // Here we could also call remove from cart if we had the itemId
                } else {
                    _state.update { it.copy(quantity = newQty) }
                    updateQuantityInCart(newQty)
                }
            }
            is DetailsIntent.SelectVariant -> {
                _state.update { it.copy(selectedVariant = intent.variant, isAddedToCart = false, quantity = 1, isCounterMode = true) }
            }
            is DetailsIntent.AddToCart -> addToCart()
            is DetailsIntent.SetCounterMode -> {
                _state.update { it.copy(isCounterMode = intent.isCounterMode) }
            }
        }
    }

    private fun loadProduct(slug: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = getProductDetailUseCase(slug)) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            product = result.data,
                            selectedVariant = result.data.variants.firstOrNull(),
                            error = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    _effect.emit(DetailsEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    private fun addToCart() {
        val currentState = _state.value
        val variantId = currentState.selectedVariant?.id ?: return
        
        viewModelScope.launch {
            val isLoggedIn = isUserLoggedInUseCase().first()
            if (!isLoggedIn) {
                _effect.emit(DetailsEffect.NavigateToAuth)
                return@launch
            }

            when (val result = addToCartUseCase(variantId, currentState.quantity)) {
                is AppResult.Success -> {
                    _state.update { it.copy(isAddedToCart = true) }
                    _effect.emit(DetailsEffect.AddedToCart)
                }
                is AppResult.Error -> {
                    _effect.emit(DetailsEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    private fun updateQuantityInCart(newQty: Int) {
        val currentState = _state.value
        if (!currentState.isAddedToCart) return
        val variantId = currentState.selectedVariant?.id ?: return

        viewModelScope.launch {
            // Using addToCart with a specific quantity effectively sets/updates it in our backend implementation
            addToCartUseCase(variantId, newQty)
        }
    }
}
