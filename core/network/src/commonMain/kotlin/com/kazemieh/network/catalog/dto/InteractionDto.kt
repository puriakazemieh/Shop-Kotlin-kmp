package com.kazemieh.network.catalog.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReviewResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val rating: Int?,
    val comment: String,
    val replies: List<ReviewResponse>,
    val createdAt: String,
    val isSupport: Boolean = false,
    val verifiedPurchase: Boolean = false
)

@Serializable
data class CreateReviewRequestDto(
    val productId: Long,
    val rating: Int?,
    val comment: String,
    val parentId: Long? = null
)

@Serializable
data class QuestionResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val content: String,
    val replies: List<QuestionResponse>,
    val createdAt: String,
    val isSupport: Boolean = false
)

@Serializable
data class CreateQuestionRequestDto(
    val productId: Long,
    val content: String,
    val parentId: Long? = null
)

@Serializable
data class UpdateReviewRequest(
    val rating: Int?,
    val comment: String,
    val images: List<String> = emptyList()
)

@Serializable
data class UpdateQuestionRequest(
    val content: String
)
