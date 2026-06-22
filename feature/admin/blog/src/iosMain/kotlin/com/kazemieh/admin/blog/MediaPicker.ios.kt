package com.kazemieh.admin.blog

import androidx.compose.runtime.Composable

actual class MediaPicker actual constructor() {
    @Composable
    actual fun InitializeMediaPicker(onMediaSelect: (ByteArray, Boolean) -> Unit) {
    }

    actual fun open() {
    }
}
