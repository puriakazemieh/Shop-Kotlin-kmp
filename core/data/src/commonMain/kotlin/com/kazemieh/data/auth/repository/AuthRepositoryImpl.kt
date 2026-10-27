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

    /**
     * A browser startup refresh can finish after an explicit login.  Keep a
     * monotonic local generation so an older restore result cannot clear the
     * credentials and profile created by that newer user action.
     */
    private var sessionGeneration: Long = 0L

    private val _authState = MutableStateFlow(
        if (tokenManager.hasValidToken()) AuthState.Authenticated
        else AuthState.Unauthenticated
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        tokenManager.setOnTokenExpiredListener {
            sessionGeneration++
            _authState.value = AuthState.Unauthenticated
            TokenExpiredEventBus.publish(event = AuthState.Unauthenticated)
        }
        BrowserSessionLifecycle.observeSignOut {
            clearLocalSession()
        }
    }

    override suspend fun restoreSession(): AppResult<Unit> {
        if (!PlatformConfig.usesWebSessionCookie) return AppResult.Success(Unit)
        val restoreGeneration = sessionGeneration
        _authState.value = AuthState.Loading
        return authDataSource.refreshBrowserSession()
            .doOnSuccess { accessToken ->
                if (sessionGeneration == restoreGeneration) {
                    tokenManager.saveTokens(accessToken = accessToken, refreshToken = "")
                    _authState.value = AuthState.Authenticated
                }
            }
            .doOnError {
                if (sessionGeneration == restoreGeneration) {
                    tokenManager.clearTokens()
                    profileLocalDataSource.clearProfile()
                    _authState.value = AuthState.Unauthenticated
                }
            }
            .map { }
    }

    override suspend fun login(
        email: String,
        password: String
    ): AppResult<Unit> {
        return authDataSource.login(email, password)
            .doOnSuccess { auth ->
                sessionGeneration++
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
        email: String?,
        mobile: String?,
        password: String
    ): AppResult<Unit> {
        return authDataSource.register(email, mobile, password)
            .doOnSuccess { auth ->
                sessionGeneration++
                tokenManager.saveTokens(
                    accessToken = auth.accessToken,
                    refreshToken = auth.refreshToken
                )
                profileLocalDataSource.saveProfile(auth.profile)
                _authState.value = AuthState.Authenticated
            }
            .map { }
    }

    override suspend fun forgotPassword(email: String?, mobile: String?): AppResult<Unit> {
        return authDataSource.forgotPassword(email, mobile)
    }

    override suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit> {
        return authDataSource.resetPassword(token, newPassword)
    }

    override suspend fun sendLoginOtp(mobile: String): AppResult<Unit> {
        return authDataSource.sendLoginOtp(mobile)
    }

    override suspend fun loginWithOtp(mobile: String, otpCode: String): AppResult<Unit> {
        return authDataSource.loginWithOtp(mobile, otpCode)
            .doOnSuccess { auth ->
                sessionGeneration++
                tokenManager.saveTokens(
                    accessToken = auth.accessToken,
                    refreshToken = auth.refreshToken
                )
                profileLocalDataSource.saveProfile(auth.profile)
                _authState.value = AuthState.Authenticated
            }
            .map { }
    }

    override suspend fun resetPasswordWithOtp(
        mobile: String,
        otpCode: String,
        newPassword: String
    ): AppResult<Unit> {
        return authDataSource.resetPasswordWithOtp(mobile, otpCode, newPassword)
    }

    override suspend fun signOut(): AppResult<Unit> {
        if (PlatformConfig.usesWebSessionCookie) {
            when (val result = authDataSource.logoutBrowserSession()) {
                is AppResult.Error -> return result
                is AppResult.Loading -> return result
                is AppResult.Success -> Unit
            }
        }
        return try {
            clearLocalSession()
            if (PlatformConfig.usesWebSessionCookie) {
                BrowserSessionLifecycle.publishSignOut()
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown error")
        }
    }

    override fun isLoggedIn(): Flow<Boolean> = flow {
        emit(tokenManager.getAccessToken() != null)
    }

    private suspend fun clearLocalSession() {
        sessionGeneration++
        tokenManager.clearTokens()
        profileLocalDataSource.clearProfile()
        _authState.value = AuthState.Unauthenticated
        TokenExpiredEventBus.publish(event = AuthState.Unauthenticated)
    }

}
