package com.kazemieh.data.admin.repository

import com.kazemieh.network.admin.dto.request.*
import com.kazemieh.network.admin.dto.response.*
import com.kazemieh.domain.admin.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*



fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> {
    return when (this) {
        is AppResult.Success -> AppResult.Success(transform(this.data))
        is AppResult.Error -> AppResult.Error(this.message)
        is AppResult.Loading -> AppResult.Loading
    }
}
