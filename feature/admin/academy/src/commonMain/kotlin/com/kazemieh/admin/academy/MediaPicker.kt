package com.kazemieh.admin.academy

import androidx.compose.runtime.Composable

expect class MediaPicker() {
    @Composable
    fun InitializeMediaPicker(onMediaSelect: (bytes: ByteArray, isVideo: Boolean) -> Unit)
    fun open()
}
