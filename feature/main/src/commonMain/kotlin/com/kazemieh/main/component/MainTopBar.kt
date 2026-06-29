package com.kazemieh.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.Radius

/** هدرِ صفحه‌ی اصلی مطابق اسپک: لوگوی کارمیلا + نوار جستجو. */
@Composable
fun HomeTopBar(
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ک",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(Modifier.size(10.dp))
            Column {
                Text(
                    text = "کارمیلا",
                    color = colors.onSurface,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "MODA · POSHAK",
                    color = colors.onSurfaceVariant,
                    fontSize = 9.5.sp,
                    letterSpacing = 1.sp
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        SearchPill(onClick = onSearchClick)
        Spacer(Modifier.height(2.dp))
    }
}

/** نوار بالای صفحات تب‌دارِ غیرِ خانه (سبد، منو) — فقط عنوان. */
@Composable
fun TitleTopBar(
    title: String,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = colors.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun SearchPill(onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.button))
            .background(colors.surfaceVariant)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.button))
            .clickable { onClick() }
            .padding(horizontal = 15.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "جستجو در میان هزاران محصول…",
            color = colors.onSurfaceVariant,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
