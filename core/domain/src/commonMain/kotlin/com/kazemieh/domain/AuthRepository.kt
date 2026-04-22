package com.kazemieh.domain

import com.kazemieh.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String): Result<User>
    suspend fun forgotPassword(email: String): Result<Unit>
}