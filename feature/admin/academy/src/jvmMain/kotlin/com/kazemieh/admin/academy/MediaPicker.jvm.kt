package com.kazemieh.admin.academy

import androidx.compose.runtime.Composable
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual class MediaPicker actual constructor() {
    private var onMediaSelect: ((ByteArray, Boolean) -> Unit)? = null

    @Composable
    actual fun InitializeMediaPicker(onMediaSelect: (ByteArray, Boolean) -> Unit) {
        this.onMediaSelect = onMediaSelect
    }

    actual fun open() {
        val fileChooser = JFileChooser()
        fileChooser.fileFilter = FileNameExtensionFilter("Media Files", "jpg", "png", "jpeg", "mp4", "mov", "avi", "pdf", "zip")
        val result = fileChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            val isVideo = file.extension.lowercase() in listOf("mp4", "mov", "avi")
            onMediaSelect?.invoke(file.readBytes(), isVideo)
        }
    }
}
