package com.kazemieh.details.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius

@Composable
fun VariantChip(
    label: String,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.sm))
            .clickable(enabled = enabled) { onClick() }
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .border(
                width = 1.dp,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.outline
                },
                shape = RoundedCornerShape(Radius.sm)
            )
            .padding(vertical = 8.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = FontSize.SMALL,
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.onSurface
            },
            fontWeight = FontWeight.Medium
        )
    }
}
