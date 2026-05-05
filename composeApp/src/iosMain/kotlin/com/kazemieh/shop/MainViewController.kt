package com.kazemieh.shop

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

private var koinInitialized = false

fun MainViewController(): UIViewController {
    if (!koinInitialized) {
        initKoin()
        koinInitialized = true
    }

    return ComposeUIViewController {
        App()
    }
}