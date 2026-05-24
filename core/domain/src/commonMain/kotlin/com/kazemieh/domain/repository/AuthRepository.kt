package com.kazemieh.domain.repository

import com.kazemieh.common.AppResult
import com.kazemieh.common.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>
    suspend fun login(email: String, password: String): AppResult<Unit>
    suspend fun register(email: String, password: String): AppResult<Unit>
    suspend fun forgotPassword(email: String): AppResult<Unit>
    suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit>
    suspend fun signOut(): AppResult<Unit>
    fun isLoggedIn(): Flow<Boolean>
}