package com.kazemieh.data.auth.datasource

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Auth

interface AuthDataSource {
    suspend fun login(email: String, password: String): AppResult<Auth>
    suspend fun register(email: String, password: String): AppResult<Auth>
    suspend fun forgotPassword(email: String): AppResult<Unit>
}