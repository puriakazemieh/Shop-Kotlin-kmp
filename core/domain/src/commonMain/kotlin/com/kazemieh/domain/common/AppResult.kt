package com.kazemieh.domain.common


sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val message: String, val code: Int? = null) : AppResult<Nothing>()
    data object Loading : AppResult<Nothing>()
}


inline fun <T, R> AppResult<T>.doOnSuccess(func: (AppResult: T) -> R): AppResult<T> {
    if (this is AppResult.Success) {
        func.invoke(data)
    }
    return this
}


inline fun <T, R> AppResult<T>.doOnError(func: (error: String?) -> R): AppResult<T> {
    if (this is AppResult.Error) {
        func.invoke(message)
    }
    return this
}

fun <T> AppResult<T>.getSuccessValue(): T? {
    if (this is AppResult.Success)
        return data
    return null
}

fun <T> AppResult<T>.getMessage(): String? {
    return when (this) {
        is AppResult.Success -> data as String
        is AppResult.Error -> message
        is AppResult.Loading -> "Loading"
    }
}

val <T> AppResult<T>.isSuccessful: Boolean
    get() {
        return this is AppResult.Success
    }


suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): AppResult<T> {
    return try {
        AppResult.Success(apiCall())
    } catch (e: Exception) {
        AppResult.Error(e.message ?: "error")
    }
}

