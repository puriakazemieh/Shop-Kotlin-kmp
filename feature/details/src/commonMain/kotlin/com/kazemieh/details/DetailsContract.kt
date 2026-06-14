package com.kazemieh.details

import com.kazemieh.domain.catalog.ProductDetail
import com.kazemieh.domain.catalog.ProductVariant
import com.kazemieh.domain.catalog.Question
import com.kazemieh.domain.catalog.Review

data class DetailsState(
    val isLoading: Boolean = false,
    val product: ProductDetail? = null,
    val reviews: List<Review> = emptyList(),
    val questions: List<Question> = emptyList(),
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
    data class LoadInteractions(val productId: Long) : DetailsIntent
    data class AddReview(val productId: Long, val rating: Int?, val comment: String, val parentId: Long?) : DetailsIntent
    data class UpdateReview(val reviewId: Long, val productId: Long, val rating: Int?, val comment: String) : DetailsIntent
    data class DeleteReview(val reviewId: Long, val productId: Long) : DetailsIntent
    data class AddQuestion(val productId: Long, val content: String, val parentId: Long?) : DetailsIntent
    data class UpdateQuestion(val questionId: Long, val productId: Long, val content: String) : DetailsIntent
    data class DeleteQuestion(val questionId: Long, val productId: Long) : DetailsIntent
}

sealed interface DetailsEffect {
    data class ShowError(val message: Any) : DetailsEffect
    data object AddedToCart : DetailsEffect
    data object NavigateToAuth : DetailsEffect
}
