package com.kazemieh.designsystem.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun anyToString(value: Any?): String {
    return when (value) {
        is StringResource -> stringResource(value)
        is String -> value
        null -> ""
        else -> value.toString()
    }
}
