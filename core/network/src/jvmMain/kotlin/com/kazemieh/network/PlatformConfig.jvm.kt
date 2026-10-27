package com.kazemieh.network.common

actual object PlatformConfig {
    actual val baseUrl = "http://localhost:8080/"
    actual val usesWebSessionCookie = false
    actual val isReleaseBuild: Boolean
        get() = System.getProperty("carmilla.environment") == "production" || !com.kazemieh.common.isDebugLoggingEnabled
}
