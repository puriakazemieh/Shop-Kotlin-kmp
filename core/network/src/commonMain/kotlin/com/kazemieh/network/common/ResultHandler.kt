package com.kazemieh.network.common

import com.kazemieh.common.AppResult
import com.kazemieh.common.toUserMessage
import com.kazemieh.network.common.ApiError
import com.kazemieh.network.common.ApiException
import io.ktor.client.statement.*
import io.ktor.http.isSuccess
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import com.kazemieh.common.*
import com.kazemieh.common.Res

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
            message = e.messageText,
            code = e.code
        )
    } catch (e: Exception) {
        AppResult.Error(
            message = Res.string.unknownError,
            code = 0
        )
    }
}

suspend inline fun <reified T> safeApiCallRaw(
    crossinline request: suspend () -> HttpResponse
): T {
    return try {
        val response = request()
        val bodyText = try {
            response.bodyAsText()
        } catch (e: Exception) {
            ""
        }
        if (response.status.isSuccess()) {
            if (T::class == Unit::class) {
                return Unit as T
            }
            if (T::class == String::class) {
                return try {
                    json.decodeFromString<T>(bodyText)
                } catch (e: Exception) {
                    bodyText as T
                }
            }
            try {
                json.decodeFromString<T>(bodyText)
            } catch (_: SerializationException) {
                throw ApiException(
                    messageText = Res.string.errorParsingData,
                    code = 0
                )
            }
        } else {
            val apiError = try {
                json.decodeFromString<ApiError>(bodyText)
            } catch (_: Exception) {
                ApiError(
                    message = "Server error",
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
        throw e
    } catch (_: Exception) {
        throw ApiException(
            messageText = Res.string.unknownError,
            code = 0
        )
    }
}
