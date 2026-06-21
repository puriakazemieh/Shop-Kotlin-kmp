package com.kazemieh.admin.products

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual class MediaPicker actual constructor() {
    private var launcher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>? = null

    @Composable
    actual fun InitializeMediaPicker(onMediaSelect: (ByteArray, Boolean) -> Unit) {
        val context = LocalContext.current
        launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                val mimeType = context.contentResolver.getType(it)
                val isVideo = mimeType?.startsWith("video") == true
                val bytes = context.contentResolver.openInputStream(it)?.readBytes()
                bytes?.let { onMediaSelect(it, isVideo) }
            }
        }
    }

    actual fun open() {
        launcher?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
    }
}
