package com.kazemieh.shop

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.kazemieh.shop.navigation.RootComponent

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    val lifecycle = LifecycleRegistry()
    val rootComponent = RootComponent(DefaultComponentContext(lifecycle))

    ComposeViewport {
        App(rootComponent)
    }
}