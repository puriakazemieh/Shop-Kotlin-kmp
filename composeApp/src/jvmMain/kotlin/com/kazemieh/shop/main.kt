package com.kazemieh.shop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension

fun main() = application {
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            width = 1000.dp,
            height = 700.dp
        )
    ) {
        window.minimumSize = Dimension(800, 600)
        App()
    }
}
