package com.kazemieh.admin_panel.manage_product

import androidx.compose.runtime.Composable
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.Foundation.NSData
import platform.Foundation.getBytes
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

actual class PhotoPicker actual constructor() {
    private var onImageSelect: ((ByteArray) -> Unit)? = null

    @Composable
    actual fun InitializePhotoPicker(onImageSelect: (ByteArray) -> Unit) {
        this.onImageSelect = onImageSelect
    }

    actual fun open() {
        val configuration = PHPickerConfiguration()
        configuration.filter = PHPickerFilter.imagesFilter
        configuration.selectionLimit = 1

        val picker = PHPickerViewController(configuration)
        picker.delegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, null)
                val result = didFinishPicking.firstOrNull() as? PHPickerResult
                result?.itemProvider?.loadDataRepresentationForTypeIdentifier("public.image") { data, error ->
                    if (data != null) {
                        onImageSelect?.invoke(data.toByteArray())
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
