package com.kazemieh.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// JS/web actual for the expected VideoPlayer. The Compose-Web target renders on a
// canvas (skiko) where embedding a DOM <video> is not straightforward, so — like
// the JVM/desktop actual — this is a placeholder surface. Playback on web can be
// added later via a DOM overlay if needed.
@Composable
actual fun VideoPlayer(url: String, modifier: Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text("پخش ویدیو در نسخه‌ی وب پشتیبانی نمی‌شود")
    }
}
