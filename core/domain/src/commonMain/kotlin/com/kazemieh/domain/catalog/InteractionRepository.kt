package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult

interface InteractionRepository {
    // Reviews
    suspend fun getReviews(productId: Long): AppResult<List<Review>>
    suspend fun postReview(request: CreateReviewRequest): AppResult<Review>
    suspend fun updateReview(reviewId: Long, rating: Int?, comment: String, images: List<String> = emptyList()): AppResult<Review>
    suspend fun deleteReview(reviewId: Long): AppResult<Unit>
    suspend fun toggleReviewHelpful(reviewId: Long): AppResult<Review>

    // Questions
    suspend fun getQuestions(productId: Long): AppResult<List<Question>>
    suspend fun postQuestion(request: CreateQuestionRequest): AppResult<Question>
    suspend fun updateQuestion(questionId: Long, content: String): AppResult<Question>
    suspend fun deleteQuestion(questionId: Long): AppResult<Unit>
}
