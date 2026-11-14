package com.kazemieh.config.capabilities

/** namespace داده‌های خصوصی؛ با تغییر هر جزء (backend، tenant، origin یا hostMode)، نشست قبلی معتبر نیست. */
data class PrivateSessionNamespace(
    val backendKind: BackendKind,
    val tenantId: String,
    val origin: String,
    val hostMode: WordPressHostMode? = null
) {
    init {
        require(tenantId.isNotBlank()) { "Tenant id must not be blank." }
        require(origin.startsWith("https://") || origin.startsWith("http://localhost") || origin.startsWith("http://127.0.0.1") || origin.startsWith("http://10.0.2.2")) {
            "Origin must be trusted."
        }
        if (backendKind == BackendKind.SPRING) {
            require(hostMode == null) { "Spring session namespace cannot specify a hostMode." }
        }
    }

    val fingerprint: String = if (backendKind == BackendKind.WORDPRESS && hostMode != null) {
        "$backendKind|$hostMode|$tenantId|${origin.removeSuffix("/").lowercase()}"
    } else {
        "$backendKind|$tenantId|${origin.removeSuffix("/").lowercase()}"
    }

    fun key(name: String): String = "private.$fingerprint.$name"
}

fun BackendProfile.privateSessionNamespace(tenantId: String): PrivateSessionNamespace =
    PrivateSessionNamespace(kind, tenantId, apiRoot, wordPressHostMode)
