package com.kazemieh.network.psychtest

import com.kazemieh.network.psychtest.dto.*
import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.*
import io.ktor.client.request.*

class PsychTestApiImpl(
    private val client: HttpClient
) : PsychTestApi {

    override suspend fun getTests(): List<PsychTestSummaryResponse> = safeApiCallRaw {
        client.get("api/psych-tests")
    }

    override suspend fun getTest(slug: String): PsychTestDetailResponse = safeApiCallRaw {
        client.get("api/psych-tests/$slug")
    }

    override suspend fun getMyTests(): List<UserPsychTestResponse> = safeApiCallRaw {
        client.get("api/my-psych-tests")
    }

    override suspend fun getUserTestQuestions(userTestId: Long): PsychTestDetailResponse = safeApiCallRaw {
        client.get("api/my-psych-tests/$userTestId/questions")
    }

    override suspend fun submit(userTestId: Long, request: SubmitTestRequestDto): UserPsychTestResponse = safeApiCallRaw {
        client.post("api/my-psych-tests/$userTestId/submit") { setBody(request) }
    }
}

class AdminPsychTestApiImpl(
    private val client: HttpClient
) : AdminPsychTestApi {

    override suspend fun list(): List<PsychTestSummaryResponse> = safeApiCallRaw {
        client.get("api/admin/psych-tests")
    }

    override suspend fun create(request: AdminCreatePsychTestRequestDto): Long =
        safeApiCallRaw<IdResponse> {
            client.post("api/admin/psych-tests") { setBody(request) }
        }.id

    override suspend fun delete(id: Long): Unit = safeApiCallRaw {
        client.delete("api/admin/psych-tests/$id")
    }

    override suspend fun pendingInterpretations(): List<UserPsychTestResponse> = safeApiCallRaw {
        client.get("api/admin/psych-tests/pending-interpretations")
    }

    override suspend fun interpret(userTestId: Long, request: AdminInterpretRequestDto): Unit = safeApiCallRaw {
        client.post("api/admin/psych-tests/user-tests/$userTestId/interpret") { setBody(request) }
    }
}
