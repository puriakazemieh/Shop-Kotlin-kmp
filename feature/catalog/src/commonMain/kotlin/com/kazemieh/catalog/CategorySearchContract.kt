package com.kazemieh.catalog

import com.kazemieh.domain.model.ProductSummary

data class CategorySearchState(
    val isLoading: Boolean = false,
    val products: List<ProductSummary> = emptyList(),
    val error: Any? = null,
    val searchQuery: String = "",
    val categoryId: Long? = null,
    val categoryName: String = "",
    val selectedOptions: Map<String, String> = emptyMap(),
    val availableOptions: Map<String, Set<String>> = emptyMap()
)

sealed interface CategorySearchIntent {
    data class Init(val categoryId: Long, val categoryName: String) : CategorySearchIntent
    data class UpdateSearchQuery(val query: String) : CategorySearchIntent
    data class ToggleOption(val key: String, val value: String) : CategorySearchIntent
}

sealed interface CategorySearchEffect {
    data class ShowError(val message: Any) : CategorySearchEffect
}
