package com.kazemieh.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSURL
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(url: String, modifier: Modifier) {
    val player = remember(url) {
        val nsUrl = NSURL.URLWithString(url) ?: return@remember AVPlayer()
        AVPlayer(uRL = nsUrl)
    }

    val playerViewController = remember {
        AVPlayerViewController().apply {
            this.player = player
            this.showsPlaybackControls = true
        }
    }

    UIKitView(
        factory = {
            val view = playerViewController.view
            player.play()
            view
        },
        modifier = modifier,
        update = { _ ->
            player.play()
        },
        onRelease = {
            player.pause()
        }
    )
}
