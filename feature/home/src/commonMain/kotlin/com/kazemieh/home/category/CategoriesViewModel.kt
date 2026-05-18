package com.kazemieh.home.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.usecase.catalog.GetCategoriesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CategoriesEffect>()
    val effect: SharedFlow<CategoriesEffect> = _effect.asSharedFlow()

    init {
        onIntent(CategoriesIntent.LoadCategories)
    }

    fun onIntent(intent: CategoriesIntent) {
        when (intent) {
            is CategoriesIntent.LoadCategories -> loadCategories()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
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
                    _effect.emit(CategoriesEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
