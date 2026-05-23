package com.kazemieh.home.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.usecase.catalog.GetProductsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategorySearchViewModel(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CategorySearchState())
    val state: StateFlow<CategorySearchState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CategorySearchEffect>()
    val effect: SharedFlow<CategorySearchEffect> = _effect.asSharedFlow()

    fun onIntent(intent: CategorySearchIntent) {
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
        }
    }

    private fun loadProducts() {
        val currentState = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = getProductsUseCase(
                query = currentState.searchQuery.ifBlank { null },
                categoryId = currentState.categoryId,
                options = currentState.selectedOptions.ifEmpty { null }
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
                    _effect.emit(CategorySearchEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
