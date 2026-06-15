package com.kazemieh.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun VideoPlayer(
    url: String,
    modifier: Modifier,
    isPaused: Boolean,
    isMuted: Boolean,
    onLoading: (Boolean) -> Unit,
    onEnd: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Text("Video Player not implemented for iOS", color = Color.White)
    }
}
