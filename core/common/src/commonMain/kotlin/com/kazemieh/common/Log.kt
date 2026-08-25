package com.kazemieh.common


import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter

expect var isDebugLoggingEnabled: Boolean

val AppLogger = Logger(
    config = StaticConfig(
        minSeverity = co.touchlab.kermit.Severity.Verbose,
        logWriterList = listOf(platformLogWriter())
    ),
    tag = "949494"
)

// Extension function
fun <T> T.ld(message: String = ""): T {
    return this.apply {
        AppLogger.d { "$message = $this" }
    }
}

fun <T> T.le(message: String = ""): T {
    return this.apply {
        AppLogger.e { "$message = $this" }
    }
}

fun String.redactedForLog(): String =
    replace(Regex("(?i)(authorization|bearer|token|password|otp|authority|payment)[^\\r\\n]*"), "\$1=<redacted>")
