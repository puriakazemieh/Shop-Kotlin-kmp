package com.kazemieh.data.auth.source

import com.kazemieh.network.auth.dto.request.*
import com.kazemieh.network.auth.dto.response.*
import com.kazemieh.domain.auth.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*



interface AuthDataSource {
    suspend fun login(email: String, password: String): AppResult<Auth>
    suspend fun register(email: String, password: String): AppResult<Auth>
    suspend fun forgotPassword(email: String): AppResult<Unit>
    suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit>
}
