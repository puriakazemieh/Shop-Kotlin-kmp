package com.kazemieh.domain.auth

import com.kazemieh.common.AppResult
import com.kazemieh.common.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>
    /** Restores a browser session from its server-owned cookie; native clients are unchanged. */
    suspend fun restoreSession(): AppResult<Unit>
    suspend fun login(email: String, password: String): AppResult<Unit>
    suspend fun register(email: String? = null, mobile: String? = null, password: String): AppResult<Unit>
    suspend fun forgotPassword(email: String? = null, mobile: String? = null): AppResult<Unit>
    suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit>
    suspend fun signOut(): AppResult<Unit>
    fun isLoggedIn(): Flow<Boolean>

    suspend fun sendLoginOtp(mobile: String): AppResult<Unit>
    suspend fun loginWithOtp(mobile: String, otpCode: String): AppResult<Unit>
    suspend fun resetPasswordWithOtp(mobile: String, otpCode: String, newPassword: String): AppResult<Unit>
}
