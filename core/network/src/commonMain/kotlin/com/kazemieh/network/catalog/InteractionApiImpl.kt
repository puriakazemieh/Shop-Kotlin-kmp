package com.kazemieh.network.catalog

import com.kazemieh.network.catalog.dto.*
import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.*
import io.ktor.client.request.*

class InteractionApiImpl(
    private val client: HttpClient
) : InteractionApi {

    override suspend fun getReviews(productId: Long): List<ReviewResponse> = safeApiCallRaw {
        client.get("api/reviews/product/$productId")
    }

    override suspend fun postReview(request: CreateReviewRequestDto): ReviewResponse = safeApiCallRaw {
        client.post("api/reviews") {
            setBody(request)
        }
    }

    override suspend fun updateReview(reviewId: Long, request: CreateReviewRequestDto): ReviewResponse = safeApiCallRaw {
        client.put("api/reviews/$reviewId") {
            setBody(request)
        }
    }

    override suspend fun deleteReview(reviewId: Long): Unit = safeApiCallRaw {
        client.delete("api/reviews/$reviewId")
    }

    override suspend fun getQuestions(productId: Long): List<QuestionResponse> = safeApiCallRaw {
        client.get("api/questions/product/$productId")
    }

    override suspend fun postQuestion(request: CreateQuestionRequestDto): QuestionResponse = safeApiCallRaw {
        client.post("api/questions") {
            setBody(request)
        }
    }

    override suspend fun updateQuestion(questionId: Long, request: CreateQuestionRequestDto): QuestionResponse = safeApiCallRaw {
        client.put("api/questions/$questionId") {
            setBody(request)
        }
    }

    override suspend fun deleteQuestion(questionId: Long): Unit = safeApiCallRaw {
        client.delete("api/questions/$questionId")
    }
}
