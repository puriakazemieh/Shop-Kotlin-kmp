package com.kazemieh.domain.psychtest

import com.kazemieh.common.AppResult

interface PsychTestRepository {
    suspend fun getTests(): AppResult<List<PsychTestSummary>>
    suspend fun getTest(slug: String): AppResult<PsychTestDetail>
    suspend fun getMyTests(): AppResult<List<UserPsychTest>>
    suspend fun getUserTestQuestions(userTestId: Long): AppResult<PsychTestDetail>
    suspend fun submit(userTestId: Long, answers: Map<Int, Int>): AppResult<UserPsychTest>
}

interface AdminPsychTestRepository {
    suspend fun list(): AppResult<List<PsychTestSummary>>
    suspend fun create(
        title: String,
        slug: String,
        description: String?,
        price: Double,
        productId: Long?,
        resultMode: String,
        questions: List<AdminTestQuestion>,
        ranges: List<AdminScoreRange>
    ): AppResult<Long>
    suspend fun update(
        id: Long,
        title: String?,
        description: String?,
        price: Double?,
        resultMode: String?,
        questions: List<AdminTestQuestion>?,
        ranges: List<AdminScoreRange>?
    ): AppResult<Unit>
    suspend fun delete(id: Long): AppResult<Unit>
    suspend fun pendingInterpretations(): AppResult<List<UserPsychTest>>
    suspend fun interpret(userTestId: Long, interpretation: String): AppResult<Unit>
}
