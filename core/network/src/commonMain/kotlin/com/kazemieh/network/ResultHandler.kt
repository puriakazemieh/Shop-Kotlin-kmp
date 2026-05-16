package com.kazemieh.network

import com.kazemieh.common.AppResult
import com.kazemieh.common.ld
import com.kazemieh.common.toUserMessage
import com.kazemieh.network.dto.ApiError
import com.kazemieh.network.dto.ApiException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): AppResult<T> {
    return try {
        AppResult.Success(apiCall())
    } catch (e: ApiException) {
        AppResult.Error(
            message = e.messageText.toUserMessage(),
            code = e.code
        )
    } catch (e: Exception) {
        AppResult.Error(
            message = "خطای نامشخص رخ داد",
            code = 0
        )
    }
}

suspend inline fun <reified T> safeApiCallRaw(
    crossinline request: suspend () -> HttpResponse
): T {
    return try {
        val response = request()
        val bodyText = response.bodyAsText()

        if (response.status.isSuccess()) {
            if (T::class == Unit::class) {
                return Unit as T
            }
            try {
                json.decodeFromString<T>(bodyText)
            } catch (e: SerializationException) {
                e.ld("SerializationException")
                throw ApiException(
                    messageText = "خطا در پردازش اطلاعات دریافتی",
                    code = 0
                )
            }
        } else {
            response.status.value.ld("❌ API Error - Status ")
            bodyText.ld("Response Body:")

            val apiError = try {
                json.decodeFromString<ApiError>(bodyText)
            } catch (e: Exception) {
                e.message.ld("⚠️ Failed to parse ApiError")
                ApiError(
                    message = "خطای سرور رخ داد",
                    status = response.status.value.toString(),
                    code = response.status.value,
                    errorCode = "UNKNOWN_ERROR",
                    path = null,
                    timestamp = null
                )
            }

            throw ApiException(
                messageText = apiError.errorCode.toUserMessage(),
                code = apiError.code
            )
        }
    } catch (e: ApiException) {
        e.message.ld("ApiException")
        throw e
    } catch (e: Exception) {
        "Exception: ${e::class.simpleName} - ${e.message}".ld("❌ Unexpected Error in safeApiCallRaw")
        e.printStackTrace()
        throw ApiException(
            messageText = "خطای نامشخص رخ داد",
            code = 0
        )
    }
}
