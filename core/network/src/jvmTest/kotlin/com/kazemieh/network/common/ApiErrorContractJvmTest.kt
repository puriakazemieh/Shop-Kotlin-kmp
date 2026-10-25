package com.kazemieh.network.common

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiErrorContractJvmTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `WordPress numeric status preserves the localized credential error`() {
        val error = json.decodeFromString<ApiError>(
            """{"message":"redacted","status":401,"errorCode":"BAD_CREDENTIALS"}"""
        )

        assertEquals("401", error.status)
        assertEquals("BAD_CREDENTIALS", error.errorCode)
    }

    @Test
    fun `transitional Spring string status preserves the error code`() {
        val error = json.decodeFromString<ApiError>(
            """{"message":"redacted","status":"UNAUTHORIZED","errorCode":"BAD_CREDENTIALS"}"""
        )

        assertEquals("UNAUTHORIZED", error.status)
        assertEquals("BAD_CREDENTIALS", error.errorCode)
    }
}
