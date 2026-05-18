package com.kazemieh.home.productsOverview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.usecase.catalog.GetProductsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductsOverviewViewModel(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProductsOverviewState())
    val state = _state.asStateFlow()

    private val _effect = Channel<ProductsOverviewEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        handleIntent(ProductsOverviewIntent.LoadProducts)
    }

    fun handleIntent(intent: ProductsOverviewIntent) {
        when (intent) {
            is ProductsOverviewIntent.LoadProducts -> loadProducts()
            is ProductsOverviewIntent.Refresh -> refresh()
            is ProductsOverviewIntent.OnProductClick -> {
                viewModelScope.launch {
                    _effect.send(ProductsOverviewEffect.NavigateToDetails(intent.slug))
                }
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getProducts()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            getProducts()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private suspend fun getProducts() {
        when (val result = getProductsUseCase(page = 0, size = 50)) {
            is AppResult.Success -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        products = result.data.items,
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
            }
            is AppResult.Loading -> {
                _state.update { it.copy(isLoading = true) }
            }
        }
    }
}
