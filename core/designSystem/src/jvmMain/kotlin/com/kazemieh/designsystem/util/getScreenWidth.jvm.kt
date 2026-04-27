package com.kazemieh.designsystem.util

import java.awt.Toolkit

actual fun getScreenWidth(): Float {
    val screenWidthPx = Toolkit
        .getDefaultToolkit()
        .screenSize
        .width

    return screenWidthPx.toFloat()
}