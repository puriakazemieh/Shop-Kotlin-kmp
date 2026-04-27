package com.kazemieh.designsystem.util

import kotlinx.browser.window

actual fun getScreenWidth(): Float {
    return window.innerWidth.toFloat()
}