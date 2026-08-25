package com.kazemieh.network.common

import io.ktor.http.Url

/**
 * پیکربندیِ زمانِ اجرا برای شبکه. اگر یک برند `apiBaseUrl` داشته باشد،
 * در `initKoin` روی `baseUrlOverride` ست می‌شود و جایگزینِ `PlatformConfig.baseUrl` می‌گردد.
 */
object ApiConfig {
    var baseUrlOverride: String? = null

    val baseUrl: String
        get() = baseUrlOverride?.takeIf { it.isNotBlank() } ?: PlatformConfig.baseUrl

    fun isApprovedApiHost(host: String): Boolean = runCatching {
        Url(baseUrl).host.equals(host, ignoreCase = true)
    }.getOrDefault(false)
}
