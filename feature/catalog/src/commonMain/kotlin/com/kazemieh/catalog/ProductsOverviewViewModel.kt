package com.kazemieh.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.catalog.GetCategoriesUseCase
import com.kazemieh.domain.catalog.GetProductsUseCase
import com.kazemieh.domain.catalog.ProductSummary
import com.kazemieh.domain.favorite.ObserveFavoriteIdsUseCase
import com.kazemieh.domain.favorite.ToggleFavoriteUseCase
import com.kazemieh.domain.story.GetStoriesUseCase
import com.kazemieh.domain.story.MarkStoryAsSeenUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductsOverviewViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase,
    private val getStoriesUseCase: GetStoriesUseCase,
    private val markStoryAsSeenUseCase: MarkStoryAsSeenUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProductsOverviewState())
    val state = _state.asStateFlow()

    private val _effect = Channel<ProductsOverviewEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        handleIntent(ProductsOverviewIntent.LoadProducts)
        loadStories()
        loadCategories()
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

    fun handleIntent(intent: ProductsOverviewIntent) {
        when (intent) {
            is ProductsOverviewIntent.LoadProducts -> {
                loadProducts()
                loadCategories()
            }
            is ProductsOverviewIntent.Refresh -> refresh()
            is ProductsOverviewIntent.OnProductClick -> {
                viewModelScope.launch {
                    _effect.send(ProductsOverviewEffect.NavigateToDetails(intent.slug))
                }
            }
            is ProductsOverviewIntent.OnCategoryClick -> {
                viewModelScope.launch {
                    _effect.send(ProductsOverviewEffect.NavigateToCategory(intent.id, intent.name))
                }
            }
            is ProductsOverviewIntent.OnFavoriteClick -> toggleFavorite(intent.product)
            is ProductsOverviewIntent.OnStoryClick -> {
                viewModelScope.launch {
                    _effect.send(ProductsOverviewEffect.NavigateToStory(intent.index))
                }
            }
            is ProductsOverviewIntent.OnStorySeen -> {
                viewModelScope.launch {
                    markStoryAsSeenUseCase(intent.id)
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isCategoriesLoading = true) }
            when (val result = getCategoriesUseCase()) {
                is AppResult.Success<*> -> {
                    _state.update { it.copy(categories = result.data as List<com.kazemieh.domain.catalog.Category>, isCategoriesLoading = false) }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isCategoriesLoading = false) }
                }
                is AppResult.Loading -> {
                    _state.update { it.copy(isCategoriesLoading = true) }
                }
            }
        }
    }

    private fun loadStories() {
        viewModelScope.launch {
            getStoriesUseCase().collectLatest { result ->
                when (result) {
                    is AppResult.Success -> {
                        _state.update { it.copy(stories = result.data, isStoriesLoading = false) }
                    }
                    is AppResult.Error -> {
                        _state.update { it.copy(isStoriesLoading = false) }
                    }
                    is AppResult.Loading -> {
                        _state.update { it.copy(isStoriesLoading = true) }
                    }
                }
            }
        }
    }

    private fun toggleFavorite(product: ProductSummary) {
        viewModelScope.launch {
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
