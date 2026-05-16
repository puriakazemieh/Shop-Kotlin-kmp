package com.kazemieh.admin_panel.manage_product

import androidx.compose.runtime.Composable

expect class PhotoPicker() {
    @Composable
    fun InitializePhotoPicker(onImageSelect: (ByteArray) -> Unit)
    fun open()
}
