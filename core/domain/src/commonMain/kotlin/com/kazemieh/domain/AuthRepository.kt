package com.kazemieh.domain

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): AppResult<User>
    suspend fun register(email: String, password: String): AppResult<User>
    suspend fun forgotPassword(email: String): AppResult<Unit>
}