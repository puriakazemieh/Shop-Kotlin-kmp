package com.kazemieh.data.psychtest.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.psychtest.AdminPsychTestDetail
import com.kazemieh.domain.psychtest.AdminPsychTestRepository
import com.kazemieh.domain.psychtest.AdminScoreRange
import com.kazemieh.domain.psychtest.AdminTestQuestion
import com.kazemieh.domain.psychtest.PsychTestSummary
import com.kazemieh.domain.psychtest.TestResultMode
import com.kazemieh.domain.psychtest.UserPsychTest
import com.kazemieh.domain.psychtest.UserTestStatus
import com.kazemieh.network.psychtest.AdminPsychTestApi
import com.kazemieh.network.psychtest.dto.AdminPsychTestDetailResponse
import com.kazemieh.network.psychtest.dto.AdminCreatePsychTestRequestDto
import com.kazemieh.network.psychtest.dto.AdminInterpretRequestDto
import com.kazemieh.network.psychtest.dto.AdminUpdatePsychTestRequestDto
import com.kazemieh.network.psychtest.dto.PsychTestSummaryResponse
import com.kazemieh.network.psychtest.dto.ScoreRangeResponse
import com.kazemieh.network.psychtest.dto.TestOptionResponse
import com.kazemieh.network.psychtest.dto.TestQuestionResponse
import com.kazemieh.network.psychtest.dto.UserPsychTestResponse
import com.kazemieh.network.common.safeApiCall

class AdminPsychTestRepositoryImpl(
    private val api: AdminPsychTestApi
) : AdminPsychTestRepository {

    override suspend fun list(): AppResult<List<PsychTestSummary>> = safeApiCall {
        api.list().map { it.toDomain() }
    }

    override suspend fun detail(id: Long): AppResult<AdminPsychTestDetail> = safeApiCall {
        api.detail(id).toDomain()
    }

    override suspend fun create(
        title: String,
        slug: String,
        description: String?,
        price: Double,
        productId: Long?,
        resultMode: String,
        questions: List<AdminTestQuestion>,
        ranges: List<AdminScoreRange>
    ): AppResult<Long> = safeApiCall {
        api.create(
            AdminCreatePsychTestRequestDto(
                title = title, slug = slug, description = description, price = price,
                productId = productId, resultMode = resultMode,
                questions = questions.mapIndexed { i, q ->
                    TestQuestionResponse(
                        index = i, text = q.text,
                        options = q.options.map { TestOptionResponse(text = it.first, score = it.second) }
                    )
                },
                ranges = ranges.map { ScoreRangeResponse(it.minScore, it.maxScore, it.interpretation) }
            )
        )
    }

    override suspend fun update(
        id: Long,
        title: String?,
        description: String?,
        price: Double?,
        productId: Long?,
        resultMode: String?,
        questions: List<AdminTestQuestion>?,
        ranges: List<AdminScoreRange>?
    ): AppResult<Unit> = safeApiCall {
        api.update(
            id,
            AdminUpdatePsychTestRequestDto(
                title = title, description = description, price = price, productId = productId, resultMode = resultMode,
                questions = questions?.mapIndexed { i, q ->
                    TestQuestionResponse(
                        index = i, text = q.text,
                        options = q.options.map { TestOptionResponse(text = it.first, score = it.second) }
                    )
                },
                ranges = ranges?.map { ScoreRangeResponse(it.minScore, it.maxScore, it.interpretation) }
            )
        )
    }

    override suspend fun delete(id: Long): AppResult<Unit> = safeApiCall {
        api.delete(id)
    }

    override suspend fun pendingInterpretations(): AppResult<List<UserPsychTest>> = safeApiCall {
        api.pendingInterpretations().map { it.toDomain() }
    }

    override suspend fun interpret(userTestId: Long, interpretation: String): AppResult<Unit> = safeApiCall {
        api.interpret(userTestId, AdminInterpretRequestDto(interpretation))
    }

    private fun AdminPsychTestDetailResponse.toDomain() = AdminPsychTestDetail(
        id = id, title = title, slug = slug, description = description, price = price,
        productId = productId, resultMode = TestResultMode.from(resultMode), isPublished = isPublished,
        questions = questions.map { q ->
            AdminTestQuestion(q.text, q.options.map { it.text to (it.score ?: 0) })
        },
        ranges = ranges.map { AdminScoreRange(it.minScore, it.maxScore, it.interpretation) }
    )

    private fun PsychTestSummaryResponse.toDomain() = PsychTestSummary(
        id = id, title = title, slug = slug, description = description, price = price,
        discountedPrice = discountedPrice, resultMode = TestResultMode.from(resultMode),
        questionCount = questionCount, owned = owned, productId = productId, productSlug = productSlug
    )

    private fun UserPsychTestResponse.toDomain() = UserPsychTest(
        id = id, testId = testId, testTitle = testTitle, status = UserTestStatus.from(status),
        resultMode = TestResultMode.from(resultMode), totalScore = totalScore,
        interpretation = interpretation, completedAt = completedAt
    )
}
