package com.kazemieh.network.common

interface TokenProvider {
    fun saveTokens(accessToken: String, refreshToken: String)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun clearTokens()
    fun setOnTokenExpiredListener(listener: () -> Unit)
    fun notifyTokenExpired()
}
