package com.kazemieh.data

import com.kazemieh.domain.AuthRepository
import com.kazemieh.domain.common.AppResult
import com.kazemieh.domain.model.User

class AuthRepositoryImpl(private val remoteDataSource: RemoteDataSource) : AuthRepository {
    override suspend fun login(
        email: String,
        password: String
    ): AppResult<User> {
        return remoteDataSource.login(email, password)
    }

    override suspend fun register(
        email: String,
        password: String
    ): AppResult<User> {
        return remoteDataSource.register(email, password)
    }

    override suspend fun forgotPassword(email: String): AppResult<Unit> {
        return remoteDataSource.forgotPassword(email)
    }

}