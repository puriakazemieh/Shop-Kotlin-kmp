package com.kazemieh.auth.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize

/** هدر برندِ کارمیلا برای صفحات احراز هویت — لوگوی «ک» + عنوان + زیرعنوان (مطابق اسپک). */
@Composable
fun AuthBrandHeader(
    title: String,
    subtitle: String
) {
    val colors = AppTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(19.dp))
                .background(colors.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ک",
                color = Color.White,
                fontSize = FontSize.LARGE,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = FontSize.LARGE,
            fontWeight = FontWeight.ExtraBold,
            color = colors.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            fontSize = FontSize.SMALL,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
