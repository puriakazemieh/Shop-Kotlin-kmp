package com.kazemieh.details

import com.kazemieh.domain.catalog.ProductDetail
import com.kazemieh.domain.catalog.ProductVariant

data class DetailsState(
    val isLoading: Boolean = false,
    val product: ProductDetail? = null,
    val error: Any? = null,
    val quantity: Int = 1,
    val selectedVariant: ProductVariant? = null,
    val selectedOptions: Map<String, String> = emptyMap(),
    val isAddedToCart: Boolean = false,
    val isCounterMode: Boolean = true
)

sealed interface DetailsIntent {
    data class LoadProduct(val slug: String) : DetailsIntent
    data class UpdateQuantity(val quantity: Int) : DetailsIntent
    data class SelectVariant(val variant: ProductVariant) : DetailsIntent
    data class SelectOption(val key: String, val value: String) : DetailsIntent
    data object AddToCart : DetailsIntent
    data class SetCounterMode(val isCounterMode: Boolean) : DetailsIntent
}

sealed interface DetailsEffect {
    data class ShowError(val message: Any) : DetailsEffect
    data object AddedToCart : DetailsEffect
    data object NavigateToAuth : DetailsEffect
}
