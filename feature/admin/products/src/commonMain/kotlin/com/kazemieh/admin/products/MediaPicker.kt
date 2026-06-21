package com.kazemieh.admin.products

import androidx.compose.runtime.Composable

expect class MediaPicker() {
    @Composable
    fun InitializeMediaPicker(onMediaSelect: (bytes: ByteArray, isVideo: Boolean) -> Unit)
    fun open()
}
