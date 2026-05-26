package com.kazemieh.admin.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminProduct
import com.kazemieh.domain.repository.AdminPage
import com.kazemieh.domain.usecase.admin.GetAdminProductsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminPanelViewModel(
    private val getAdminProductsUseCase: GetAdminProductsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminPanelState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AdminPanelEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        // Initial load
        loadProducts()

        _state
            .map { it.searchQuery }
            .distinctUntilChanged()
            .debounce(500)
            .onEach { query ->
                loadProducts(query)
            }
            .launchIn(viewModelScope)
    }

    fun handleIntent(intent: AdminPanelIntent) {
        when (intent) {
            is AdminPanelIntent.LoadProducts -> loadProducts(_state.value.searchQuery)
            is AdminPanelIntent.Refresh -> refresh()
            is AdminPanelIntent.SearchProducts -> {
                _state.update { it.copy(searchQuery = intent.query) }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            val result = getAdminProductsUseCase(
                page = 0,
                size = 100,
                includeInactive = true,
                query = _state.value.searchQuery
            )
            _state.update { it.copy(productsState = result, isRefreshing = false) }
        }
    }

    private fun loadProducts(query: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(productsState = AppResult.Loading) }
            val result = getAdminProductsUseCase(page = 0, size = 100, includeInactive = true, query = query)
            _state.update { it.copy(productsState = result) }
        }
    }
}

data class AdminPanelState(
    val productsState: AppResult<AdminPage<AdminProduct>> = AppResult.Loading,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false
)

sealed interface AdminPanelIntent {
    data object LoadProducts : AdminPanelIntent
    data object Refresh : AdminPanelIntent
    data class SearchProducts(val query: String) : AdminPanelIntent
}

sealed interface AdminPanelEffect {
    data class ShowError(val message: Any) : AdminPanelEffect
}
