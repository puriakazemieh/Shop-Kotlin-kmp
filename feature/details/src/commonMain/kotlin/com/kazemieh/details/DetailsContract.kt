package com.kazemieh.details

import com.kazemieh.domain.model.ProductDetail
import com.kazemieh.domain.model.ProductVariant

data class DetailsState(
    val isLoading: Boolean = false,
    val product: ProductDetail? = null,
    val error: String? = null,
    val quantity: Int = 1,
    val selectedVariant: ProductVariant? = null
)

sealed interface DetailsIntent {
    data class LoadProduct(val slug: String) : DetailsIntent
    data class UpdateQuantity(val quantity: Int) : DetailsIntent
    data class SelectVariant(val variant: ProductVariant) : DetailsIntent
    data object AddToCart : DetailsIntent
}

sealed interface DetailsEffect {
    data class ShowError(val message: String) : DetailsEffect
    data object AddedToCart : DetailsEffect
}
