package com.kazemieh.admin.products

import androidx.compose.runtime.Composable
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get

actual class MediaPicker actual constructor() {
    private var onMediaSelect: ((ByteArray, Boolean) -> Unit)? = null

    @Composable
    actual fun InitializeMediaPicker(onMediaSelect: (ByteArray, Boolean) -> Unit) {
        this.onMediaSelect = onMediaSelect
    }

    actual fun open() {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/*,video/*"
        input.onchange = {
            val file = input.files?.get(0)
            if (file != null) {
                val isVideo = file.type.startsWith("video")
                val reader = FileReader()
                reader.onload = { _ ->
                    val arrayBuffer = reader.result as? ArrayBuffer
                    if (arrayBuffer != null) {
                        val int8Array = Int8Array(arrayBuffer)
                        val bytes = ByteArray(int8Array.length)

                        for (i in 0 until int8Array.length) {
                            bytes[i] = int8Array[i]
                        }

                        onMediaSelect?.invoke(bytes, isVideo)
                    }
                }
                reader.readAsArrayBuffer(file)
            }
        }
        input.click()
    }
}
