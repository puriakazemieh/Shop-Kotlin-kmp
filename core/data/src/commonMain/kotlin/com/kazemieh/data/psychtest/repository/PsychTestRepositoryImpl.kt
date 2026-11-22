package com.kazemieh.data.psychtest.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.psychtest.PsychTestDetail
import com.kazemieh.domain.psychtest.PsychTestRepository
import com.kazemieh.domain.psychtest.PsychTestSummary
import com.kazemieh.domain.psychtest.TestOption
import com.kazemieh.domain.psychtest.TestQuestion
import com.kazemieh.domain.psychtest.TestResultMode
import com.kazemieh.domain.psychtest.UserPsychTest
import com.kazemieh.domain.psychtest.UserTestStatus
import com.kazemieh.network.psychtest.PsychTestApi
import com.kazemieh.network.psychtest.dto.PsychTestDetailResponse
import com.kazemieh.network.psychtest.dto.PsychTestSummaryResponse
import com.kazemieh.network.psychtest.dto.SubmitTestRequestDto
import com.kazemieh.network.psychtest.dto.UserPsychTestResponse
import com.kazemieh.network.common.safeApiCall
import com.kazemieh.config.capabilities.FeatureUseCaseGuard
import com.kazemieh.data.common.guardOrError

class PsychTestRepositoryImpl(
    private val api: PsychTestApi,
    private val guard: FeatureUseCaseGuard? = null
) : PsychTestRepository {

    override suspend fun getTests(): AppResult<List<PsychTestSummary>> =
        guard.guardOrError("psych.tests") {
            safeApiCall {
                api.getTests().map { it.toDomain() }
            }
        }

    override suspend fun getTest(slug: String): AppResult<PsychTestDetail> =
        guard.guardOrError("psych.tests") {
            safeApiCall {
                api.getTest(slug).toDomain()
            }
        }

    override suspend fun getMyTests(): AppResult<List<UserPsychTest>> =
        guard.guardOrError("psych.tests") {
            safeApiCall {
                api.getMyTests().map { it.toDomain() }
            }
        }

    override suspend fun getUserTestQuestions(userTestId: Long): AppResult<PsychTestDetail> =
        guard.guardOrError("psych.tests") {
            safeApiCall {
                api.getUserTestQuestions(userTestId).toDomain()
            }
        }

    override suspend fun submit(userTestId: Long, answers: Map<Int, Int>): AppResult<UserPsychTest> =
        guard.guardOrError("psych.tests") {
            safeApiCall {
                api.submit(userTestId, SubmitTestRequestDto(answers)).toDomain()
            }
        }

    private fun PsychTestSummaryResponse.toDomain() = PsychTestSummary(
        id = id, title = title, slug = slug, description = description, price = price,
        discountedPrice = discountedPrice, resultMode = TestResultMode.from(resultMode),
        questionCount = questionCount, owned = owned, productId = productId, productSlug = productSlug
    )

    private fun PsychTestDetailResponse.toDomain() = PsychTestDetail(
        id = id, title = title, slug = slug, description = description, price = price,
        discountedPrice = discountedPrice, resultMode = TestResultMode.from(resultMode),
        owned = owned, productId = productId,
        questions = questions.map { q ->
            TestQuestion(index = q.index, text = q.text, options = q.options.map { TestOption(it.text) })
        }
    )

    private fun UserPsychTestResponse.toDomain() = UserPsychTest(
        id = id, testId = testId, testTitle = testTitle, status = UserTestStatus.from(status),
        resultMode = TestResultMode.from(resultMode), totalScore = totalScore,
        interpretation = interpretation, completedAt = completedAt
    )
}
