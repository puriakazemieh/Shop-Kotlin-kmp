package com.kazemieh.home.productsOverview

import com.kazemieh.domain.model.ProductSummary

data class ProductsOverviewState(
    val isLoading: Boolean = false,
    val products: List<ProductSummary> = emptyList(),
    val error: String? = null
)

sealed interface ProductsOverviewIntent {
    data object LoadProducts : ProductsOverviewIntent
    data class OnProductClick(val slug: String) : ProductsOverviewIntent
}

sealed interface ProductsOverviewEffect {
    data class NavigateToDetails(val slug: String) : ProductsOverviewEffect
}
