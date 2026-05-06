package com.kazemieh.network

import com.kazemieh.common.AppResult
import com.kazemieh.common.toUserMessage
import com.kazemieh.network.dto.ApiError
import com.kazemieh.network.dto.ApiException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
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
    val response = request()
    val bodyText = response.bodyAsText()

    return if (response.status.isSuccess()) {
        json.decodeFromString<T>(bodyText)
    } else {
        val apiError = try {
            json.decodeFromString<ApiError>(bodyText)
        } catch (_: Exception) {
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
}
