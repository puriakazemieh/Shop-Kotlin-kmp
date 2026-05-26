package com.kazemieh.admin.products

import androidx.compose.runtime.Composable

expect class PhotoPicker() {
    @Composable
    fun InitializePhotoPicker(onImageSelect: (ByteArray) -> Unit)
    fun open()
}
