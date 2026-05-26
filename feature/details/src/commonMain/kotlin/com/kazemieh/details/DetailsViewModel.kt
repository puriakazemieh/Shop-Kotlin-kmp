package com.kazemieh.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.auth.IsUserLoggedInUseCase
import com.kazemieh.domain.cart.AddToCartUseCase
import com.kazemieh.domain.catalog.GetProductDetailUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DetailsState())
    val state: StateFlow<DetailsState> = _state.asStateFlow()

    private val _effect = Channel<DetailsEffect>()
    val effect: Flow<DetailsEffect> = _effect.receiveAsFlow()

    fun handleIntent(intent: DetailsIntent) {
        when (intent) {
            is DetailsIntent.LoadProduct -> loadProduct(intent.slug)
            is DetailsIntent.UpdateQuantity -> {
                val newQty = intent.quantity
                if (newQty <= 0) {
                    updateQuantityInCart(0)
                    _state.update { it.copy(quantity = 1, isAddedToCart = false) }
                } else {
                    _state.update { it.copy(quantity = newQty) }
                    updateQuantityInCart(newQty)
                }
            }
            is DetailsIntent.SelectVariant -> {
                _state.update {
                    it.copy(
                        selectedVariant = intent.variant,
                        selectedOptions = intent.variant.options,
                        isAddedToCart = false,
                        quantity = 1,
                        isCounterMode = true
                    )
                }
            }
            is DetailsIntent.SelectOption -> {
                val product = _state.value.product ?: return
                val currentOptions = _state.value.selectedOptions.toMutableMap()
                
                // Update the changed option
                currentOptions[intent.key] = intent.value
                
                // Try to find an exact match
                var matchingVariant = product.variants.find { it.options == currentOptions }
                
                // If no exact match, find the first variant that contains the newly selected option
                if (matchingVariant == null) {
                    matchingVariant = product.variants.find { 
                        it.options[intent.key] == intent.value 
                    }
                }

                _state.update {
                    it.copy(
                        selectedOptions = matchingVariant?.options ?: currentOptions,
                        selectedVariant = matchingVariant,
                        isAddedToCart = false,
                        quantity = 1,
                        isCounterMode = true
                    )
                }
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
                    val product = result.data
                    val firstVariant = product.variants.firstOrNull()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            product = product,
                            selectedVariant = firstVariant,
                            selectedOptions = firstVariant?.options ?: emptyMap(),
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
                    _effect.send(DetailsEffect.ShowError(result.message))
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
                _effect.send(DetailsEffect.NavigateToAuth)
                return@launch
            }

            when (val result = addToCartUseCase(variantId, currentState.quantity)) {
                is AppResult.Success -> {
                    _state.update { it.copy(isAddedToCart = true) }
                    _effect.send(DetailsEffect.AddedToCart)
                }
                is AppResult.Error -> {
                    _effect.send(DetailsEffect.ShowError(result.message))
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
