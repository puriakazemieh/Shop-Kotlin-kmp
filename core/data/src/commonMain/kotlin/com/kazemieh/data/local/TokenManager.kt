package com.kazemieh.data.local

import com.kazemieh.network.TokenProvider
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class TokenManager(private val settings: Settings) : TokenProvider {

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        settings[KEY_ACCESS_TOKEN] = accessToken
        settings[KEY_REFRESH_TOKEN] = refreshToken
    }

    override fun getAccessToken(): String? {
        return settings.getStringOrNull(KEY_ACCESS_TOKEN)
    }

    override fun getRefreshToken(): String? {
        return settings.getStringOrNull(KEY_REFRESH_TOKEN)
    }

    fun clearTokens() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
    }

    fun hasValidToken(): Boolean {
        return !getAccessToken().isNullOrEmpty()
    }
}