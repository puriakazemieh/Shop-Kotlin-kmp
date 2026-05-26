package com.kazemieh.catalog

import com.kazemieh.domain.model.Category

data class CategoriesState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val categories: List<Category> = emptyList(),
    val error: Any? = null
)

sealed interface CategoriesIntent {
    data object LoadCategories : CategoriesIntent
    data object Refresh : CategoriesIntent
}

sealed interface CategoriesEffect {
    data class ShowError(val message: Any) : CategoriesEffect
}
