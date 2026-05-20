package com.kazemieh.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

expect object AppLocale {
    @Composable
    infix fun provides(value: String): ProvidedValue<*>
}
