package com.kazemieh.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.catalog.GetProductsUseCase
import com.kazemieh.domain.catalog.ProductSummary
import com.kazemieh.domain.auth.IsUserLoggedInUseCase
import com.kazemieh.domain.favorite.ObserveFavoriteIdsUseCase
import com.kazemieh.domain.favorite.ToggleFavoriteUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategorySearchViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CategorySearchState())
    val state: StateFlow<CategorySearchState> = _state.asStateFlow()

    private val _effect = Channel<CategorySearchEffect>()
    val effect: Flow<CategorySearchEffect> = _effect.receiveAsFlow()

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            observeFavoriteIdsUseCase().collectLatest { ids ->
                _state.update { state ->
                    state.copy(
                        products = state.products.map { product ->
                            product.copy(isFavorite = ids.contains(product.id))
                        }
                    )
                }
            }
        }
    }

    fun handleIntent(intent: CategorySearchIntent) {
        when (intent) {
            is CategorySearchIntent.Init -> {
                _state.update { it.copy(categoryId = intent.categoryId, categoryName = intent.categoryName) }
                loadProducts()
            }
            is CategorySearchIntent.UpdateSearchQuery -> {
                _state.update { it.copy(searchQuery = intent.query) }
                loadProducts()
            }
            is CategorySearchIntent.ToggleOption -> {
                _state.update {
                    val currentSelected = it.selectedOptions.toMutableMap()
                    if (currentSelected[intent.key] == intent.value) {
                        currentSelected.remove(intent.key)
                    } else {
                        currentSelected[intent.key] = intent.value
                    }
                    it.copy(selectedOptions = currentSelected)
                }
                loadProducts()
            }
            is CategorySearchIntent.UpdateSort -> {
                _state.update { it.copy(sort = intent.sort) }
                loadProducts()
            }
            is CategorySearchIntent.ToggleFavorite -> toggleFavorite(intent.product)
        }
    }

    private fun toggleFavorite(product: ProductSummary) {
        viewModelScope.launch {
            val isLoggedIn = isUserLoggedInUseCase().first()
            if (!isLoggedIn) {
                _effect.send(CategorySearchEffect.NavigateToAuth)
                return@launch
            }

            val isAdding = !product.isFavorite
            updateProductFavoriteState(product.id, isAdding)

            val result = toggleFavoriteUseCase(product.id, isAdding)

            if (result is AppResult.Error) {
                updateProductFavoriteState(product.id, !isAdding)
            }
        }
    }

    private fun updateProductFavoriteState(productId: Long, isFavorite: Boolean) {
        _state.update { currentState ->
            currentState.copy(
                products = currentState.products.map {
                    if (it.id == productId) it.copy(isFavorite = isFavorite) else it
                }
            )
        }
    }

    private fun loadProducts() {
        val currentState = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = getProductsUseCase(
                query = currentState.searchQuery.ifBlank { null },
                categoryId = currentState.categoryId,
                options = currentState.selectedOptions.ifEmpty { null },
                sort = currentState.sort
            )
            when (result) {
                is AppResult.Success -> {
                    val availableOptions = result.data.items
                        .flatMap { it.options.entries }
                        .groupBy({ it.key }, { it.value })
                        .mapValues { it.value.flatten().toSet() }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            products = result.data.items,
                            totalCount = result.data.totalElements,
                            availableOptions = availableOptions,
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
                    _effect.send(CategorySearchEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
