package com.kazemieh.network.catalog

import com.kazemieh.network.catalog.dto.*

interface InteractionApi {
    // Reviews
    suspend fun getReviews(productId: Long): List<ReviewResponse>
    suspend fun postReview(request: CreateReviewRequestDto): ReviewResponse
    suspend fun updateReview(reviewId: Long, request: UpdateReviewRequest): ReviewResponse
    suspend fun deleteReview(reviewId: Long)

    // Questions
    suspend fun getQuestions(productId: Long): List<QuestionResponse>
    suspend fun postQuestion(request: CreateQuestionRequestDto): QuestionResponse
    suspend fun updateQuestion(questionId: Long, request: UpdateQuestionRequest): QuestionResponse
    suspend fun deleteQuestion(questionId: Long)
}
