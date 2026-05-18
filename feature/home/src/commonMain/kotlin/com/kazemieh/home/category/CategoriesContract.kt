package com.kazemieh.home.category

import com.kazemieh.domain.model.Category

data class CategoriesState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val error: String? = null
)

sealed interface CategoriesIntent {
    data object LoadCategories : CategoriesIntent
}

sealed interface CategoriesEffect {
    data class ShowError(val message: String) : CategoriesEffect
}
