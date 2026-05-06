package com.kazemieh.domain.repository

import com.kazemieh.common.AppResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): AppResult<Unit>
    suspend fun register(email: String, password: String): AppResult<Unit>
    suspend fun forgotPassword(email: String): AppResult<Unit>
    suspend fun signOut(): AppResult<Unit>
    fun isLoggedIn(): Flow<Boolean>
}