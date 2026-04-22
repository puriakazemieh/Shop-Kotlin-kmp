package com.kazemieh.data

import com.kazemieh.domain.common.AppResult
import com.kazemieh.domain.model.User

interface RemoteDataSource {
    suspend fun login(email: String, password: String): AppResult<User>
    suspend fun register(email: String, password: String): AppResult<User>
    suspend fun forgotPassword(email: String): AppResult<Unit>
}