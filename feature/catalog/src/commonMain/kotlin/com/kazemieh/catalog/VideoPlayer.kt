package com.kazemieh.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    isPaused: Boolean = false,
    isMuted: Boolean = false,
    onLoading: (Boolean) -> Unit = {},
    onEnd: () -> Unit = {}
)
