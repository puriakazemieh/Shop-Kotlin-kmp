package com.kazemieh.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalJsLocale = staticCompositionLocalOf { "en" }

actual object AppLocale {
    @Composable
    actual infix fun provides(value: String): ProvidedValue<*> {
        return LocalJsLocale.provides(value)
    }
}
