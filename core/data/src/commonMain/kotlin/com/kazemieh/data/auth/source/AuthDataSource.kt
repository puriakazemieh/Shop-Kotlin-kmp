package com.kazemieh.data.auth.source

import com.kazemieh.network.auth.dto.request.*
import com.kazemieh.network.auth.dto.response.*
import com.kazemieh.domain.auth.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*



interface AuthDataSource {
    suspend fun login(email: String, password: String): AppResult<Auth>
    suspend fun register(email: String? = null, mobile: String? = null, password: String): AppResult<Auth>
    suspend fun forgotPassword(email: String? = null, mobile: String? = null): AppResult<Unit>
    suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit>

    suspend fun sendLoginOtp(mobile: String): AppResult<Unit>
    suspend fun loginWithOtp(mobile: String, otpCode: String): AppResult<Auth>
    suspend fun resetPasswordWithOtp(mobile: String, otpCode: String, newPassword: String): AppResult<Unit>
}
