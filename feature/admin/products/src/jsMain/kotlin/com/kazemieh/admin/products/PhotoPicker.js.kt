package com.kazemieh.admin.products

import androidx.compose.runtime.Composable
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader

actual class PhotoPicker actual constructor() {
    private var onImageSelect: ((ByteArray) -> Unit)? = null

    @Composable
    actual fun InitializePhotoPicker(onImageSelect: (ByteArray) -> Unit) {
        this.onImageSelect = onImageSelect
    }

    actual fun open() {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/*"
        input.onchange = {
            val file = input.files?.item(0)
            if (file != null) {
                val reader = FileReader()
                reader.onload = { _ ->
                    val arrayBuffer = reader.result as? org.khronos.webgl.ArrayBuffer
                    if (arrayBuffer != null) {
                        val int8Array = org.khronos.webgl.Int8Array(arrayBuffer)
                        val bytes = ByteArray(int8Array.length)

                        for (i in 0 until int8Array.length) {
                            bytes[i] = int8Array.unsafeCast<ByteArray>()[i]
                        }

                        onImageSelect?.invoke(bytes)
                    }
                }
                reader.readAsArrayBuffer(file)
            }
        }
        input.click()
    }
}