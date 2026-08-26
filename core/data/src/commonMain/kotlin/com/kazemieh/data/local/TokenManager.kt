package com.kazemieh.data.local

import com.kazemieh.network.common.*
import com.kazemieh.common.*

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.kazemieh.config.capabilities.PrivateSessionNamespace

class TokenManager(
    private val settings: Settings,
    private var namespace: PrivateSessionNamespace
) : TokenProvider {

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    private var onTokenExpiredListener: (() -> Unit)? = null

    override fun saveTokens(accessToken: String, refreshToken: String) {
        settings[namespace.key(KEY_ACCESS_TOKEN)] = accessToken
        settings[namespace.key(KEY_REFRESH_TOKEN)] = refreshToken
    }

    override fun getAccessToken(): String? {
        return settings.getStringOrNull(namespace.key(KEY_ACCESS_TOKEN))
    }

    override fun getRefreshToken(): String? {
        return settings.getStringOrNull(namespace.key(KEY_REFRESH_TOKEN))
    }

    override fun clearTokens() {
        settings.remove(namespace.key(KEY_ACCESS_TOKEN))
        settings.remove(namespace.key(KEY_REFRESH_TOKEN))
    }

    override fun setOnTokenExpiredListener(listener: () -> Unit) {
        onTokenExpiredListener = listener
    }

    override fun notifyTokenExpired() {
        onTokenExpiredListener?.invoke()
    }

    fun hasValidToken(): Boolean {
        return !getAccessToken().isNullOrEmpty()
    }

    /** تغییر tenant/backend/origin باید logout و پاک‌سازی دادهٔ خصوصی فعال را اجباری کند. */
    fun switchNamespace(next: PrivateSessionNamespace): Boolean {
        if (namespace.fingerprint == next.fingerprint) return false
        clearTokens()
        namespace = next
        return true
    }
}
