package com.kazemieh.config.capabilities

/** namespace داده‌های خصوصی؛ با تغییر هر جزء، نشست قبلی معتبر نیست. */
data class PrivateSessionNamespace(
    val backendKind: BackendKind,
    val tenantId: String,
    val origin: String
) {
    init {
        require(tenantId.isNotBlank()) { "Tenant id must not be blank." }
        require(origin.startsWith("https://") || origin.startsWith("http://localhost") || origin.startsWith("http://127.0.0.1")) {
            "Origin must be trusted."
        }
    }

    val fingerprint: String = "$backendKind|$tenantId|${origin.removeSuffix("/").lowercase()}"

    fun key(name: String): String = "private.$fingerprint.$name"
}

fun BackendProfile.privateSessionNamespace(tenantId: String): PrivateSessionNamespace =
    PrivateSessionNamespace(kind, tenantId, apiRoot)
