package com.kazemieh.network.common

/**
 * پیکربندیِ زمانِ اجرا برای شبکه. اگر یک برند `apiBaseUrl` داشته باشد،
 * در `initKoin` روی `baseUrlOverride` ست می‌شود و جایگزینِ `PlatformConfig.baseUrl` می‌گردد.
 */
object ApiConfig {
    var baseUrlOverride: String? = null

    val baseUrl: String
        get() = baseUrlOverride?.takeIf { it.isNotBlank() } ?: PlatformConfig.baseUrl
}
