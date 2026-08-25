package com.kazemieh.common

actual var isDebugLoggingEnabled: Boolean = System.getProperty("carmilla.debug.logging") == "true"
