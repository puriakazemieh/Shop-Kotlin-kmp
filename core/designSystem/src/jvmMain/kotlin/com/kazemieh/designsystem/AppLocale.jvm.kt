package com.kazemieh.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

private val LocalJvmLocale = staticCompositionLocalOf { Locale.getDefault().toString() }

actual object AppLocale {
    @Composable
    actual infix fun provides(value: String): ProvidedValue<*> {
        val locale = Locale(value)
        Locale.setDefault(locale)
        return LocalJvmLocale.provides(value)
    }
}
