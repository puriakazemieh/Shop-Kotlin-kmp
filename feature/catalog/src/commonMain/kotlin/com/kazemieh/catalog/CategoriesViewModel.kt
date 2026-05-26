package com.kazemieh.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.catalog.GetCategoriesUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state.asStateFlow()

    private val _effect = Channel<CategoriesEffect>()
    val effect: Flow<CategoriesEffect> = _effect.receiveAsFlow()

    init {
        handleIntent(CategoriesIntent.LoadCategories)
    }

    fun handleIntent(intent: CategoriesIntent) {
        when (intent) {
            is CategoriesIntent.LoadCategories -> loadCategories()
            is CategoriesIntent.Refresh -> refresh()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getCategories()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            getCategories()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private suspend fun getCategories() {
        when (val result = getCategoriesUseCase()) {
            is AppResult.Success -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        categories = result.data,
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
                _effect.send(CategoriesEffect.ShowError(result.message))
            }
            else -> {}
        }
    }
}
