package com.kazemieh.common

actual val isDebugLoggingEnabled: Boolean = System.getProperty("carmilla.debug.logging") == "true"
