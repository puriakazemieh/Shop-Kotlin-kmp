package com.kazemieh.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius

// =====================================================================================
//  Carmilla Design System — Filter Chip
//  انتخاب‌شده: پس‌زمینه primary · انتخاب‌نشده: surfaceVariant با بوردر line
//  شعاع ۱۱، padding 8×16
// =====================================================================================

@Composable
fun CarmillaFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    )
    val content by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    )
    val shape = RoundedCornerShape(Radius.sm)

    Text(
        text = text,
        modifier = modifier
            .clip(shape)
            .background(container)
            .then(
                if (selected) Modifier
                else Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = content,
        fontSize = FontSize.REGULAR,
        fontWeight = FontWeight.SemiBold
    )
}
