package com.kazemieh.catalog

import com.kazemieh.domain.catalog.ProductSummary

data class ProductsOverviewState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val products: List<ProductSummary> = emptyList(),
    val error: Any? = null
)

sealed interface ProductsOverviewIntent {
    data object LoadProducts : ProductsOverviewIntent
    data object Refresh : ProductsOverviewIntent
    data class OnProductClick(val slug: String) : ProductsOverviewIntent
}

sealed interface ProductsOverviewEffect {
    data class NavigateToDetails(val slug: String) : ProductsOverviewEffect
}
