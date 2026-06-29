package com.kazemieh.main.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.Resources
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * نوار پایینِ شناور مطابق اسپک کارمیلا:
 * یک «قرص» با پس‌زمینه‌ی surface، حاشیه‌ی line و گوشه‌های ۲۲dp، هر تب آیکن + برچسب.
 * تب انتخاب‌شده پس‌زمینه‌ی accentSoft و رنگ primary می‌گیرد.
 */
@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    cartItemCount: Int,
    selected: BottomBarDestination,
    onSelect: (BottomBarDestination) -> Unit,
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.lg))
            .background(colors.surface)
            .border(BorderStroke(1.dp, colors.line), RoundedCornerShape(Radius.lg))
            .padding(7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        BottomBarDestination.entries.forEach { destination ->
            val isSelected = selected == destination
            val animatedTint by animateColorAsState(
                targetValue = if (isSelected) colors.primary else colors.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.button))
                    .background(if (isSelected) colors.accentSoft else Color.Transparent)
                    .clickable { onSelect(destination) }
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (destination == BottomBarDestination.Cart && cartItemCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = colors.sale,
                                    contentColor = Color.White
                                ) {
                                    Text(text = cartItemCount.toString(), fontSize = 9.sp)
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(destination.icon),
                                contentDescription = stringResource(Resources.String.BottomBarDesc),
                                tint = animatedTint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(destination.icon),
                            contentDescription = stringResource(Resources.String.BottomBarDesc),
                            tint = animatedTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                // برچسب فقط برای تبِ انتخاب‌شده (الگوی pill)
                if (isSelected) {
                    Text(
                        text = stringResource(destination.title),
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
