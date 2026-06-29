package com.kazemieh.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme

// =====================================================================================
//  Carmilla Design System — Story Ring
//  حلقه‌ی گرادیانی استوری (طلایی → accent-2) با هسته‌ی دایره‌ای
//  حالت seen: حلقه‌ی خنثی به‌جای گرادیان
// =====================================================================================

@Composable
fun StoryRing(
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    seen: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = AppTheme.colors
    val ringBrush = if (seen) {
        Brush.linearGradient(listOf(colors.outline, colors.outline))
    } else {
        Brush.linearGradient(listOf(colors.gold, colors.accent2))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(ringBrush)
            .padding(2.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.clip(CircleShape),
                contentAlignment = Alignment.Center
            ) { content() }
        }
    }
}
