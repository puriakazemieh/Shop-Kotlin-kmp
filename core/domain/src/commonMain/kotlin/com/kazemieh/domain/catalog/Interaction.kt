package com.kazemieh.domain.catalog

data class Review(
    val id: Long,
    val userId: Long,
    val userName: String,
    val rating: Int?,
    val comment: String,
    val replies: List<Review>,
    val createdAt: String,
    val isSupport: Boolean = false,
    val verifiedPurchase: Boolean = false
)

data class CreateReviewRequest(
    val productId: Long,
    val rating: Int?,
    val comment: String,
    val parentId: Long? = null
)

data class Question(
    val id: Long,
    val userId: Long,
    val userName: String,
    val content: String,
    val replies: List<Question>,
    val createdAt: String,
    val isSupport: Boolean = false
)

data class CreateQuestionRequest(
    val productId: Long,
    val content: String,
    val parentId: Long? = null
)
