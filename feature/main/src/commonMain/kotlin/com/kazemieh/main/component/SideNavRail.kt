package com.kazemieh.main.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
 * نوارِ ناوبریِ کناری برای صفحاتِ بزرگ (تبلت/لپ‌تاپ/دسکتاپ/وب).
 * جایگزینِ نوارِ پایینِ موبایل می‌شود؛ تب‌های یکسان ولی به‌صورتِ عمودی و همراه با برچسبِ همیشگی.
 *
 * @param expanded اگر true باشد برچسب‌ها کنارِ آیکن نمایش داده می‌شوند (حالتِ drawer برای دسکتاپِ پهن)؛
 *                 در غیر این‌صورت فقط آیکن + برچسبِ کوچکِ زیرِ آن (حالتِ railِ باریک برای تبلت).
 */
@Composable
fun SideNavRail(
    cartItemCount: Int,
    selected: BottomBarDestination,
    onSelect: (BottomBarDestination) -> Unit,
    expanded: Boolean = false,
    header: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(if (expanded) 240.dp else 92.dp)
            .background(colors.surface)
            .border(BorderStroke(1.dp, colors.line), RoundedCornerShape(0.dp))
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = if (expanded) Alignment.Start else Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (header != null) {
            header()
            Spacer(Modifier.height(8.dp))
        }
        BottomBarDestination.entries.forEach { destination ->
            val isSelected = selected == destination
            val animatedTint by animateColorAsState(
                targetValue = if (isSelected) colors.primary else colors.onSurfaceVariant
            )
            if (expanded) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.button))
                        .background(if (isSelected) colors.accentSoft else Color.Transparent)
                        .clickable { onSelect(destination) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RailIcon(destination, cartItemCount, animatedTint)
                    Text(
                        text = stringResource(destination.title),
                        color = if (isSelected) colors.primary else colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.button))
                        .background(if (isSelected) colors.accentSoft else Color.Transparent)
                        .clickable { onSelect(destination) }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    RailIcon(destination, cartItemCount, animatedTint)
                    Text(
                        text = stringResource(destination.title),
                        color = if (isSelected) colors.primary else colors.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun RailIcon(
    destination: BottomBarDestination,
    cartItemCount: Int,
    tint: Color,
) {
    val colors = AppTheme.colors
    Box(contentAlignment = Alignment.Center) {
        if (destination == BottomBarDestination.Cart && cartItemCount > 0) {
            BadgedBox(
                badge = {
                    Badge(containerColor = colors.sale, contentColor = Color.White) {
                        Text(text = cartItemCount.toString(), fontSize = 9.sp)
                    }
                }
            ) {
                Icon(
                    painter = painterResource(destination.icon),
                    contentDescription = stringResource(Resources.String.BottomBarDesc),
                    tint = tint,
                    modifier = Modifier.size(24.dp),
                )
            }
        } else {
            Icon(
                painter = painterResource(destination.icon),
                contentDescription = stringResource(Resources.String.BottomBarDesc),
                tint = tint,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
