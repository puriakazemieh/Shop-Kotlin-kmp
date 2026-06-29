package com.kazemieh.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius

// =====================================================================================
//  Carmilla Design System — Badge
//  بَج‌های کوچک طبق پروتوتایپ: حراج/موجود/جدید/امتیاز/آخرین موجودی
//  شعاع ۸، فونت ۱۲ Bold، padding 4×10
// =====================================================================================

enum class BadgeStyle { Sale, InStock, New, Rating, LastStock }

@Composable
fun CarmillaBadge(
    text: String,
    style: BadgeStyle,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val container: Color
    val content: Color
    when (style) {
        BadgeStyle.Sale -> {
            container = colors.sale; content = Color.White
        }
        BadgeStyle.InStock -> {
            container = colors.ok; content = Color.White
        }
        BadgeStyle.New -> {
            container = colors.gold; content = Color.White
        }
        BadgeStyle.Rating -> {
            container = colors.accentSoft; content = colors.primary
        }
        BadgeStyle.LastStock -> {
            container = colors.sale.copy(alpha = 0.12f); content = colors.sale
        }
    }

    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(Radius.xs))
            .background(container)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = content,
        fontSize = FontSize.SMALL,
        fontWeight = FontWeight.Bold
    )
}
