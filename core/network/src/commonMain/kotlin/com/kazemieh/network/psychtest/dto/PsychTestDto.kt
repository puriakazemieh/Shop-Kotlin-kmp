package com.kazemieh.network.psychtest.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdResponse(val id: Long)

@Serializable
data class PsychTestSummaryResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String? = null,
    val price: Double,
    val discountedPrice: Double? = null,
    val resultMode: String,
    val questionCount: Int,
    val owned: Boolean = false,
    val productId: Long? = null,
    val productSlug: String? = null
)

@Serializable
data class TestOptionResponse(
    val text: String,
    val score: Int? = null
)

@Serializable
data class TestQuestionResponse(
    val index: Int,
    val text: String,
    val options: List<TestOptionResponse>
)

@Serializable
data class ScoreRangeResponse(
    val minScore: Int,
    val maxScore: Int,
    val interpretation: String
)

@Serializable
data class PsychTestDetailResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String? = null,
    val price: Double,
    val discountedPrice: Double? = null,
    val resultMode: String,
    val questions: List<TestQuestionResponse>,
    val owned: Boolean,
    val productId: Long? = null
)

@Serializable
data class UserPsychTestResponse(
    val id: Long,
    val testId: Long,
    val testTitle: String,
    val status: String,
    val resultMode: String,
    val totalScore: Int? = null,
    val interpretation: String? = null,
    val completedAt: String? = null
)

@Serializable
data class SubmitTestRequestDto(
    val answers: Map<Int, Int> = emptyMap()
)

// ---------- Admin ----------
@Serializable
data class AdminCreatePsychTestRequestDto(
    val title: String,
    val slug: String,
    val description: String? = null,
    val price: Double = 0.0,
    val discountedPrice: Double? = null,
    val productId: Long? = null,
    val resultMode: String = "AUTO",
    @SerialName("published") val isPublished: Boolean = true,
    val questions: List<TestQuestionResponse> = emptyList(),
    val ranges: List<ScoreRangeResponse> = emptyList()
)

@Serializable
data class AdminPsychTestDetailResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String? = null,
    val price: Double = 0.0,
    val discountedPrice: Double? = null,
    val productId: Long? = null,
    val resultMode: String = "AUTO",
    @SerialName("published") val isPublished: Boolean = true,
    val questions: List<TestQuestionResponse> = emptyList(),
    val ranges: List<ScoreRangeResponse> = emptyList()
)

@Serializable
data class AdminUpdatePsychTestRequestDto(
    val title: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val discountedPrice: Double? = null,
    val resultMode: String? = null,
    @SerialName("published") val isPublished: Boolean? = null,
    val questions: List<TestQuestionResponse>? = null,
    val ranges: List<ScoreRangeResponse>? = null
)

@Serializable
data class AdminInterpretRequestDto(
    val interpretation: String
)
