package com.kazemieh.common


sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val message: Any, val code: Int? = null) : AppResult<Nothing>()
    data object Loading : AppResult<Nothing>()
}


inline fun <T, R> AppResult<T>.doOnSuccess(func: (AppResult: T) -> R): AppResult<T> {
    if (this is AppResult.Success) {
        func.invoke(data)
    }
    return this
}


inline fun <T, R> AppResult<T>.doOnError(func: (error: Any?) -> R): AppResult<T> {
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

fun <T> AppResult<T>.getMessage(): Any? {
    return when (this) {
        is AppResult.Success -> data
        is AppResult.Error -> message
        is AppResult.Loading -> "Loading"
    }
}

val <T> AppResult<T>.isSuccessful: Boolean
    get() {
        return this is AppResult.Success
    }

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> {
    return when (this) {
        is AppResult.Success -> AppResult.Success(transform(data))
        is AppResult.Error -> this
        is AppResult.Loading -> this
    }
}
