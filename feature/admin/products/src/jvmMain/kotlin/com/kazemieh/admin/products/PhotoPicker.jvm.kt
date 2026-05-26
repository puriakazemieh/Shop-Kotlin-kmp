package com.kazemieh.admin.products

import androidx.compose.runtime.Composable
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual class PhotoPicker actual constructor() {
    private var onImageSelect: ((ByteArray) -> Unit)? = null

    @Composable
    actual fun InitializePhotoPicker(onImageSelect: (ByteArray) -> Unit) {
        this.onImageSelect = onImageSelect
    }

    actual fun open() {
        val fileChooser = JFileChooser()
        fileChooser.fileFilter = FileNameExtensionFilter("Images", "jpg", "png", "jpeg")
        val result = fileChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            onImageSelect?.invoke(file.readBytes())
        }
    }
}
