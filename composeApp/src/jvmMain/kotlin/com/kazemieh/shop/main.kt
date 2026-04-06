package com.kazemieh.shop

import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry

fun main() = application {

    val windowState = rememberWindowState()
    val lifecycle = LifecycleRegistry()

    Window(
        onCloseRequest = ::exitApplication,
        title = "kmp-shop",
    ) {
        LifecycleController(
            lifecycleRegistry = lifecycle,
            windowState = windowState,
            windowInfo = LocalWindowInfo.current
        )

        App()
    }
}
