package com.kazemieh.catalog

import com.kazemieh.domain.catalog.ProductSummary

data class SearchState(
    val query: String = "",
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val results: List<ProductSummary> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val error: Any? = null
) {
    /** جستجوهای پرطرفدارِ استاتیک (پیشنهادِ اولیه، مطابق اسپک). */
    val popularSearches: List<String> = listOf("مانتو", "شومیز", "شلوار", "کیف", "شال", "کفش")
}

sealed interface SearchIntent {
    data class UpdateQuery(val query: String) : SearchIntent
    data class Submit(val query: String) : SearchIntent
    data object ClearRecent : SearchIntent
    data class ToggleFavorite(val product: ProductSummary) : SearchIntent
}

sealed interface SearchEffect {
    data class ShowError(val message: Any) : SearchEffect
    data object NavigateToAuth : SearchEffect
}
