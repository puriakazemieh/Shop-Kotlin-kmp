package com.kazemieh.network.psychtest

import com.kazemieh.network.psychtest.dto.*

interface PsychTestApi {
    suspend fun getTests(): List<PsychTestSummaryResponse>
    suspend fun getTest(slug: String): PsychTestDetailResponse
    suspend fun getMyTests(): List<UserPsychTestResponse>
    suspend fun getUserTestQuestions(userTestId: Long): PsychTestDetailResponse
    suspend fun submit(userTestId: Long, request: SubmitTestRequestDto): UserPsychTestResponse
}

interface AdminPsychTestApi {
    suspend fun list(): List<PsychTestSummaryResponse>
    suspend fun detail(id: Long): AdminPsychTestDetailResponse
    suspend fun create(request: AdminCreatePsychTestRequestDto): Long
    suspend fun update(id: Long, request: AdminUpdatePsychTestRequestDto)
    suspend fun delete(id: Long)
    suspend fun pendingInterpretations(): List<UserPsychTestResponse>
    suspend fun interpret(userTestId: Long, request: AdminInterpretRequestDto)
}
