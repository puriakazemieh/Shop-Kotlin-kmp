package com.kazemieh.network

import com.kazemieh.network.common.ApiError
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiErrorStatusTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun wordpressNumericStatusPreservesBadCredentials() {
        val error = json.decodeFromString<ApiError>(
            """{"message":"Unauthorized","status":401,"errorCode":"BAD_CREDENTIALS"}"""
        )
        assertEquals("401", error.status)
        assertEquals("BAD_CREDENTIALS", error.errorCode)
    }

    @Test
    fun transitionalStringStatusPreservesErrorCode() {
        val error = json.decodeFromString<ApiError>(
            """{"message":"Unauthorized","status":"UNAUTHORIZED","errorCode":"BAD_CREDENTIALS"}"""
        )
        assertEquals("UNAUTHORIZED", error.status)
        assertEquals("BAD_CREDENTIALS", error.errorCode)
    }
}
