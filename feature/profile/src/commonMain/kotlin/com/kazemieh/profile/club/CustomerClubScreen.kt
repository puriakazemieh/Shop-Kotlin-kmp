package com.kazemieh.profile.club

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerClubScreen(
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<CustomerClubViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "باشگاه مشتریان",
                        fontFamily = AppFont(),
                        fontSize = FontSize.LARGE,
                        color = colors.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            modifier = Modifier.graphicsLayer { rotationY = if (isRtl) 180f else 0f },
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = stringResource(Resources.String.BackArrowDesc),
                            tint = colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    scrolledContainerColor = colors.background,
                    navigationIconContentColor = colors.onSurface,
                    titleContentColor = colors.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
                .responsiveMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            // کارتِ امتیاز و سطح
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(colors.primary, colors.accent2)))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "امتیاز باشگاه شما",
                    fontFamily = AppFont(),
                    fontSize = FontSize.REGULAR,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = state.points.toString(),
                    fontFamily = AppFont(),
                    fontSize = FontSize.EXTRA_LARGE,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, null, tint = colors.gold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "سطح ${state.tier.title}",
                        fontFamily = AppFont(),
                        fontSize = FontSize.SMALL,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                state.tier.nextThreshold?.let { next ->
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "${(next - state.points).coerceAtLeast(0)} امتیاز تا سطح بعدی",
                        fontFamily = AppFont(),
                        fontSize = FontSize.EXTRA_SMALL,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "مزایای باشگاه",
                fontFamily = AppFont(),
                fontSize = FontSize.EXTRA_REGULAR,
                fontWeight = FontWeight.ExtraBold,
                color = colors.onSurface
            )
            Spacer(Modifier.height(12.dp))
            val benefits = listOf(
                "کسب ۱ امتیاز به ازای هر ۱۰٬۰۰۰ تومان خرید",
                "استفاده از امتیازها برای تخفیف در خریدهای بعدی",
                "دسترسی زودهنگام به حراج‌ها و کالاهای جدید",
                "ارسال رایگان برای اعضای سطح طلایی و بالاتر"
            )
            benefits.forEach { benefit ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(26.dp).clip(CircleShape).background(colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, tint = colors.primary, modifier = Modifier.size(15.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = benefit,
                        fontFamily = AppFont(),
                        fontSize = FontSize.REGULAR,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            // سطوح باشگاه
            Text(
                text = "سطوح باشگاه",
                fontFamily = AppFont(),
                fontSize = FontSize.EXTRA_REGULAR,
                fontWeight = FontWeight.ExtraBold,
                color = colors.onSurface
            )
            Spacer(Modifier.height(12.dp))
            ClubTier.entries.forEach { tier ->
                val threshold = when (tier) {
                    ClubTier.BRONZE -> 0L
                    ClubTier.SILVER -> 1_000L
                    ClubTier.GOLD -> 5_000L
                    ClubTier.PLATINUM -> 15_000L
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surface)
                        .border(
                            width = if (tier == state.tier) 1.5.dp else 1.dp,
                            color = if (tier == state.tier) colors.primary else colors.line,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tier.title,
                        fontFamily = AppFont(),
                        fontSize = FontSize.REGULAR,
                        fontWeight = if (tier == state.tier) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (tier == state.tier) colors.primary else colors.onSurface
                    )
                    Text(
                        text = "$threshold+ امتیاز",
                        fontFamily = AppFont(),
                        fontSize = FontSize.SMALL,
                        color = colors.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
