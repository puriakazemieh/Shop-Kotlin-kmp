package com.kazemieh.data.catalog.repository

import com.kazemieh.common.AppResult
import com.kazemieh.network.common.safeApiCall
import com.kazemieh.domain.catalog.*
import com.kazemieh.network.catalog.InteractionApi
import com.kazemieh.network.catalog.dto.CreateQuestionRequestDto
import com.kazemieh.network.catalog.dto.CreateReviewRequestDto
import com.kazemieh.network.catalog.dto.QuestionResponse
import com.kazemieh.network.catalog.dto.ReviewResponse
import com.kazemieh.network.catalog.dto.UpdateQuestionRequest
import com.kazemieh.network.catalog.dto.UpdateReviewRequest

class InteractionRepositoryImpl(
    private val api: InteractionApi
) : InteractionRepository {

    override suspend fun getReviews(productId: Long): AppResult<List<Review>> = safeApiCall {
        api.getReviews(productId).map { it.toDomain() }
    }

    override suspend fun postReview(request: CreateReviewRequest): AppResult<Review> = safeApiCall {
        api.postReview(
            CreateReviewRequestDto(
                productId = request.productId,
                rating = request.rating,
                comment = request.comment,
                parentId = request.parentId
            )
        ).toDomain()
    }

    override suspend fun updateReview(reviewId: Long, rating: Int?, comment: String): AppResult<Review> = safeApiCall {
        api.updateReview(
            reviewId,
            UpdateReviewRequest(
                rating = rating,
                comment = comment
            )
        ).toDomain()
    }

    override suspend fun deleteReview(reviewId: Long): AppResult<Unit> = safeApiCall {
        api.deleteReview(reviewId)
    }

    override suspend fun getQuestions(productId: Long): AppResult<List<Question>> = safeApiCall {
        api.getQuestions(productId).map { it.toDomain() }
    }

    override suspend fun postQuestion(request: CreateQuestionRequest): AppResult<Question> = safeApiCall {
        api.postQuestion(
            CreateQuestionRequestDto(
                productId = request.productId,
                content = request.content,
                parentId = request.parentId
            )
        ).toDomain()
    }

    override suspend fun updateQuestion(questionId: Long, content: String): AppResult<Question> = safeApiCall {
        api.updateQuestion(
            questionId,
            UpdateQuestionRequest(
                content = content
            )
        ).toDomain()
    }

    override suspend fun deleteQuestion(questionId: Long): AppResult<Unit> = safeApiCall {
        api.deleteQuestion(questionId)
    }

    private fun ReviewResponse.toDomain(): Review = Review(
        id = id,
        userId = userId,
        userName = userName,
        rating = rating,
        comment = comment,
        replies = replies.map { it.toDomain() },
        createdAt = createdAt,
        isSupport = isSupport,
        verifiedPurchase = verifiedPurchase
    )

    private fun QuestionResponse.toDomain(): Question = Question(
        id = id,
        userId = userId,
        userName = userName,
        content = content,
        replies = replies.map { it.toDomain() },
        createdAt = createdAt,
        isSupport = isSupport
    )
}
