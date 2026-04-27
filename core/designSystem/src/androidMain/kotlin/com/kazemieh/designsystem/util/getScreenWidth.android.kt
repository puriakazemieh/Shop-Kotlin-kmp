package com.kazemieh.designsystem.util

import android.content.res.Resources

actual fun getScreenWidth(): Float {
    return Resources.getSystem()
        .displayMetrics
        .widthPixels / Resources.getSystem().displayMetrics.density
}