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

    /** Credentials may only reach the configured origin and its API path subtree. */
    fun isApprovedApiUrl(url: Url): Boolean = runCatching {
        val base = Url(baseUrl)
        val path = url.encodedPath
        val basePath = base.encodedPath.trimEnd('/')
        val ambiguousPath = Regex("%2e|%2f|%5c|%25|\\\\|(?:^|/)\\.{1,2}(?:/|$)", RegexOption.IGNORE_CASE)
        base.protocol.name in setOf("http", "https") &&
            url.protocol == base.protocol && url.host.equals(base.host, ignoreCase = true) &&
            url.port == base.port && !ambiguousPath.containsMatchIn(path) &&
            !ambiguousPath.containsMatchIn(base.encodedPath) &&
            (basePath.isEmpty() || path == basePath || path.startsWith("$basePath/"))
    }.getOrDefault(false)

    fun isApprovedApiOrigin(url: Url): Boolean = runCatching {
        val base = Url(baseUrl)
        base.protocol.name in setOf("http", "https") && url.protocol == base.protocol &&
            url.host.equals(base.host, ignoreCase = true) && url.port == base.port
    }.getOrDefault(false)

    /** Keeps internal API calls under a WordPress installation subdirectory. */
    fun resolveInternalApiPath(path: String): String {
        if (!path.startsWith("/api/")) return path
        val basePath = Url(baseUrl).encodedPath.trimEnd('/')
        return if (basePath.isEmpty()) path else "$basePath$path"
    }
}
