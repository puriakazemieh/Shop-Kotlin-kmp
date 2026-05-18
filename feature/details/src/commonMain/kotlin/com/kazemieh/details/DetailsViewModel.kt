package com.kazemieh.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.usecase.cart.AddToCartUseCase
import com.kazemieh.domain.usecase.catalog.GetProductDetailUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val addToCartUseCase: AddToCartUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DetailsState())
    val state: StateFlow<DetailsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<DetailsEffect>()
    val effect: SharedFlow<DetailsEffect> = _effect.asSharedFlow()

    fun onIntent(intent: DetailsIntent) {
        when (intent) {
            is DetailsIntent.LoadProduct -> loadProduct(intent.slug)
            is DetailsIntent.UpdateQuantity -> {
                _state.update { it.copy(quantity = intent.quantity) }
            }
            is DetailsIntent.SelectVariant -> {
                _state.update { it.copy(selectedVariant = intent.variant) }
            }
            is DetailsIntent.AddToCart -> addToCart()
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
            when (val result = addToCartUseCase(variantId, currentState.quantity)) {
                is AppResult.Success -> {
                    _effect.emit(DetailsEffect.AddedToCart)
                }
                is AppResult.Error -> {
                    _effect.emit(DetailsEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
