package com.kazemieh.home.category

import com.kazemieh.domain.model.ProductSummary

data class CategorySearchState(
    val isLoading: Boolean = false,
    val products: List<ProductSummary> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val categoryId: Long? = null,
    val categoryName: String = ""
)

sealed interface CategorySearchIntent {
    data class Init(val categoryId: Long, val categoryName: String) : CategorySearchIntent
    data class UpdateSearchQuery(val query: String) : CategorySearchIntent
}

sealed interface CategorySearchEffect {
    data class ShowError(val message: String) : CategorySearchEffect
}
