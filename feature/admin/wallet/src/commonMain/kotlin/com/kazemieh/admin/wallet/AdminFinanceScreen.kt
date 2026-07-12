package com.kazemieh.admin.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.ContentWidth
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize

/**
 * صفحهٔ «مالی و برداشت» — دو زیرتب:
 *  ۱) درخواست‌های برداشت (AdminWithdrawalsScreen)
 *  ۲) موجودی کیف پول کاربران (AdminWalletScreen)
 */
@Composable
fun AdminFinanceScreen(
    onBackClick: () -> Unit
) {
    val colors = AppTheme.colors
    var subTab by remember { mutableStateOf(0) }
    val subTabs = listOf("درخواست‌های برداشت", "موجودی کیف پول")

    Column(modifier = Modifier.fillMaxSize().responsiveMaxWidth(ContentWidth.medium)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subTabs.forEachIndexed { index, label ->
                val sel = subTab == index
                Text(
                    text = label,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .then(
                            if (sel) Modifier.background(colors.primary)
                            else Modifier.background(colors.surfaceVariant)
                                .border(1.dp, colors.line, RoundedCornerShape(11.dp))
                        )
                        .clickable { subTab = index }
                        .padding(vertical = 10.dp),
                    fontSize = FontSize.SMALL,
                    fontWeight = FontWeight.Bold,
                    color = if (sel) colors.onPrimary else colors.onSurfaceVariant,
                    fontFamily = AppFont(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (subTab) {
                0 -> AdminWithdrawalsScreen(onBackClick = onBackClick, embedded = true)
                1 -> AdminWalletScreen(onBackClick = onBackClick, embedded = true)
            }
        }
    }
}
