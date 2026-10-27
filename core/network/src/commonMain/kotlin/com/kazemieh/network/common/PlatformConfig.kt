package com.kazemieh.network.common

expect object PlatformConfig {
    val baseUrl: String
    val usesWebSessionCookie: Boolean
    val isReleaseBuild: Boolean
}
