package com.kazemieh.data.auth.repository

import com.kazemieh.network.auth.dto.request.*
import com.kazemieh.network.auth.dto.response.*
import com.kazemieh.domain.auth.*
import com.kazemieh.domain.profile.Profile
import com.kazemieh.network.common.*
import com.kazemieh.common.*
import com.kazemieh.data.auth.source.AuthDataSource
import com.kazemieh.data.local.TokenManager
import com.kazemieh.data.local.ProfileLocalDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow



class AuthRepositoryImpl(
    private val authDataSource: AuthDataSource,
    private val tokenManager: TokenManager,
    private val profileLocalDataSource: ProfileLocalDataSource,
) : AuthRepository {


    private val _authState = MutableStateFlow(
        if (tokenManager.hasValidToken()) AuthState.Authenticated
        else AuthState.Unauthenticated
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        tokenManager.setOnTokenExpiredListener {
            _authState.value = AuthState.Unauthenticated
            TokenExpiredEventBus.publish(event = AuthState.Unauthenticated)
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): AppResult<Unit> {
        return authDataSource.login(email, password)
            .doOnSuccess { auth ->
                tokenManager.saveTokens(
                    accessToken = auth.accessToken,
                    refreshToken = auth.refreshToken
                )
                profileLocalDataSource.saveProfile(auth.profile)
                _authState.value = AuthState.Authenticated
            }
            .map { }
    }

    override suspend fun register(
        email: String,
        password: String
    ): AppResult<Unit> {
        return authDataSource.register(email, password)
            .doOnSuccess { auth ->
                tokenManager.saveTokens(
                    accessToken = auth.accessToken,
                    refreshToken = auth.refreshToken
                )
                profileLocalDataSource.saveProfile(auth.profile)
                _authState.value = AuthState.Authenticated
            }
            .map { }
    }

    override suspend fun forgotPassword(email: String): AppResult<Unit> {
        return authDataSource.forgotPassword(email)
    }

    override suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit> {
        return authDataSource.resetPassword(token, newPassword)
    }

    override suspend fun signOut(): AppResult<Unit> {
        return try {
            tokenManager.clearTokens()
            _authState.value = AuthState.Unauthenticated
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown error")
        }
    }

    override fun isLoggedIn(): Flow<Boolean> = flow {
        emit(tokenManager.getAccessToken() != null)
    }

}
