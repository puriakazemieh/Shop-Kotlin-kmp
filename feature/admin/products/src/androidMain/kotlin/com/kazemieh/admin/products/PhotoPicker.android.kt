package com.kazemieh.admin.products

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual class PhotoPicker actual constructor() {
    private var launcher: ManagedActivityResultLauncher<String, Uri?>? = null

    @Composable
    actual fun InitializePhotoPicker(onImageSelect: (ByteArray) -> Unit) {
        val context = LocalContext.current
        launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val bytes = context.contentResolver.openInputStream(it)?.readBytes()
                bytes?.let { onImageSelect(it) }
            }
        }
    }

    actual fun open() {
        launcher?.launch("image/*")
    }
}
