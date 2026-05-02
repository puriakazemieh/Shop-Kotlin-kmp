package com.kazemieh.shop

import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {

    Window(
        onCloseRequest = ::exitApplication,
        title = "kmp-shop",
    ) {
        App()
    }
}
