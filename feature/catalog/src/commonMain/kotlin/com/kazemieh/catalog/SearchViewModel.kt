package com.kazemieh.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.auth.IsUserLoggedInUseCase
import com.kazemieh.domain.catalog.GetProductsUseCase
import com.kazemieh.domain.catalog.ProductSummary
import com.kazemieh.domain.favorite.ObserveFavoriteIdsUseCase
import com.kazemieh.domain.favorite.ToggleFavoriteUseCase
import com.kazemieh.domain.settings.AddRecentSearchUseCase
import com.kazemieh.domain.settings.ClearRecentSearchesUseCase
import com.kazemieh.domain.settings.GetRecentSearchesUseCase
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

class SearchViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
    private val getRecentSearchesUseCase: GetRecentSearchesUseCase,
    private val addRecentSearchUseCase: AddRecentSearchUseCase,
    private val clearRecentSearchesUseCase: ClearRecentSearchesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private val _effect = Channel<SearchEffect>()
    val effect: Flow<SearchEffect> = _effect.receiveAsFlow()

    init {
        loadRecentSearches()
        observeFavorites()
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            _state.update { it.copy(recentSearches = getRecentSearchesUseCase()) }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            observeFavoriteIdsUseCase().collectLatest { ids ->
                _state.update { s ->
                    s.copy(results = s.results.map { it.copy(isFavorite = ids.contains(it.id)) })
                }
            }
        }
    }

    fun handleIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.UpdateQuery -> _state.update { it.copy(query = intent.query) }
            is SearchIntent.Submit -> submit(intent.query)
            is SearchIntent.ClearRecent -> viewModelScope.launch {
                clearRecentSearchesUseCase()
                _state.update { it.copy(recentSearches = emptyList()) }
            }
            is SearchIntent.ToggleFavorite -> toggleFavorite(intent.product)
        }
    }

    private fun submit(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(query = q, isLoading = true, hasSearched = true) }
            addRecentSearchUseCase(q)
            when (val result = getProductsUseCase(query = q, size = 40)) {
                is AppResult.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        results = result.data.items,
                        recentSearches = getRecentSearchesUseCase(),
                        error = null
                    )
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                    _effect.send(SearchEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    private fun toggleFavorite(product: ProductSummary) {
        viewModelScope.launch {
            if (!isUserLoggedInUseCase().first()) {
                _effect.send(SearchEffect.NavigateToAuth)
                return@launch
            }
            val isAdding = !product.isFavorite
            setFavorite(product.id, isAdding)
            if (toggleFavoriteUseCase(product.id, isAdding) is AppResult.Error) {
                setFavorite(product.id, !isAdding)
            }
        }
    }

    private fun setFavorite(productId: Long, isFavorite: Boolean) {
        _state.update { s ->
            s.copy(results = s.results.map { if (it.id == productId) it.copy(isFavorite = isFavorite) else it })
        }
    }
}
