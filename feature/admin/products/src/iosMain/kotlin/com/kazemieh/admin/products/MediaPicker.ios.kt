package com.kazemieh.admin.products

import androidx.compose.runtime.Composable
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import platform.Foundation.NSData
import platform.Foundation.getBytes
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

actual class MediaPicker actual constructor() {
    private var onMediaSelect: ((ByteArray, Boolean) -> Unit)? = null

    @Composable
    actual fun InitializeMediaPicker(onMediaSelect: (ByteArray, Boolean) -> Unit) {
        this.onMediaSelect = onMediaSelect
    }

    actual fun open() {
        val configuration = PHPickerConfiguration()
        // Using null filter to allow both images and videos
        configuration.filter = null
        configuration.selectionLimit = 1

        val picker = PHPickerViewController(configuration)
        picker.delegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, null)
                val result = didFinishPicking.firstOrNull() as? PHPickerResult
                val itemProvider = result?.itemProvider
                
                if (itemProvider != null) {
                    val isVideo = itemProvider.hasItemConformingToTypeIdentifier("public.movie")
                    val typeIdentifier = if (isVideo) "public.movie" else "public.image"
                    
                    itemProvider.loadDataRepresentationForTypeIdentifier(typeIdentifier) { data, error ->
                        if (data != null) {
                            onMediaSelect?.invoke(data.toByteArray(), isVideo)
                        }
                    }
                }
            }
        }

        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(picker, true, null)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val byteArray = ByteArray(size)
    if (size > 0) {
        byteArray.usePinned { pinned ->
            getBytes(pinned.addressOf(0), length)
        }
    }
    return byteArray
}
