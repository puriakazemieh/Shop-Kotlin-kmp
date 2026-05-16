package com.kazemieh.admin_panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminProduct
import com.kazemieh.domain.repository.AdminPage
import com.kazemieh.domain.usecase.admin.GetAdminProductsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminPanelViewModel(
    private val getAdminProductsUseCase: GetAdminProductsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminPanelState())
    val state = _state.asStateFlow()

    init {
        handleIntent(AdminPanelIntent.LoadProducts)
    }

    fun handleIntent(intent: AdminPanelIntent) {
        when (intent) {
            is AdminPanelIntent.LoadProducts -> loadProducts()
            is AdminPanelIntent.SearchProducts -> _state.update { it.copy(searchQuery = intent.query) }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _state.update { it.copy(productsState = AppResult.Loading) }
            val result = getAdminProductsUseCase(page = 0, size = 100, includeInactive = true)
            _state.update { it.copy(productsState = result) }
        }
    }
}

data class AdminPanelState(
    val productsState: AppResult<AdminPage<AdminProduct>> = AppResult.Loading,
    val searchQuery: String = ""
)

sealed interface AdminPanelIntent {
    data object LoadProducts : AdminPanelIntent
    data class SearchProducts(val query: String) : AdminPanelIntent
}
