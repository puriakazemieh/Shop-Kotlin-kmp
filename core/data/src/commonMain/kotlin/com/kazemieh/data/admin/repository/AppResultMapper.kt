package com.kazemieh.data.admin.repository

import com.kazemieh.common.AppResult

fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> {
    return when (this) {
        is AppResult.Success -> AppResult.Success(transform(data))
        is AppResult.Error -> AppResult.Error(message)
        is AppResult.Loading -> AppResult.Loading
    }
}
