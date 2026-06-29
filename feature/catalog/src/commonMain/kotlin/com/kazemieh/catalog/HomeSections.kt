package com.kazemieh.catalog

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.Spacing
import com.kazemieh.domain.catalog.Campaign
import com.kazemieh.domain.catalog.ProductSummary
import kotlinx.coroutines.delay

/**
 * بنر هیروی صفحه‌ی اصلی — مطابق اسپک کارمیلا.
 * توجه: متن تبلیغاتی فعلاً ثابت است؛ در فاز بنرهای سرور (S3) داینامیک می‌شود.
 */
@Composable
fun HomeHero(
    modifier: Modifier = Modifier,
    onShopClick: () -> Unit,
    onDealsClick: () -> Unit
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp)
            .clip(RoundedCornerShape(Radius.lg))
            .background(
                Brush.linearGradient(listOf(colors.primary, colors.accent2))
            )
    ) {
        // شیدِ تیره برای خوانایی متن
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x9E0F1220), Color(0x000F1220))
                    )
                )
        )
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.xl, vertical = 28.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "کالکشن پاییز ۱۴۰۴ رسید ✦",
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.full))
                    .background(Color(0x29FFFFFF))
                    .padding(horizontal = 13.dp, vertical = 6.dp),
                color = Color.White,
                fontSize = FontSize.SMALL,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "ست‌های پاییزی\nبا تخفیف تا ۵۰٪",
                color = Color.White,
                fontSize = FontSize.EXTRA_LARGE,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = FontSize.EXTRA_LARGE
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "جدیدترین مانتو، پالتو و بافت‌های فصل را با ارسال رایگان سفارش دهید.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = FontSize.SMALL,
                lineHeight = FontSize.MEDIUM
            )
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "مشاهده کالکشن",
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.button))
                        .background(Color.White)
                        .clickable { onShopClick() }
                        .padding(horizontal = 24.dp, vertical = 13.dp),
                    color = colors.primary,
                    fontSize = FontSize.REGULAR,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "پیشنهاد شگفت‌انگیز",
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.button))
                        .background(Color(0x24FFFFFF))
                        .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(Radius.button))
                        .clickable { onDealsClick() }
                        .padding(horizontal = 22.dp, vertical = 13.dp),
                    color = Color.White,
                    fontSize = FontSize.REGULAR,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/** سرتیتر بخش با لینک «مشاهده همه». */
@Composable
fun HomeSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = FontSize.LARGE,
            fontWeight = FontWeight.ExtraBold,
            color = colors.onBackground
        )
        if (onSeeAll != null) {
            Row(
                modifier = Modifier.clickable { onSeeAll() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مشاهده همه",
                    fontSize = FontSize.SMALL,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * بخش «پیشنهاد شگفت‌انگیز» — کارتِ گرادیانی با تایمر شمارش معکوس و ردیفِ افقیِ محصولاتِ کمپین.
 * داده از سرور (S2)؛ اگر کمپینِ فعالی نباشد یا محصول نداشته باشد رندر نمی‌شود.
 */
@Composable
fun AmazingOffersSection(
    campaign: Campaign,
    modifier: Modifier = Modifier,
    onProductClick: (String) -> Unit,
    onFavoriteClick: (ProductSummary) -> Unit
) {
    if (campaign.products.isEmpty()) return
    val colors = AppTheme.colors

    var remaining by remember(campaign.remainingSeconds) { mutableStateOf(campaign.remainingSeconds) }
    LaunchedEffect(campaign.remainingSeconds) {
        while (remaining > 0) {
            delay(1000)
            remaining -= 1
        }
    }
    val h = (remaining / 3600).toString().padStart(2, '0')
    val m = ((remaining % 3600) / 60).toString().padStart(2, '0')
    val s = (remaining % 60).toString().padStart(2, '0')

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .background(Brush.linearGradient(listOf(colors.primary, colors.accent2)))
            .padding(Spacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⚡", fontSize = FontSize.MEDIUM)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = campaign.title.ifBlank { "پیشنهاد شگفت‌انگیز" },
                    color = Color.White,
                    fontSize = FontSize.EXTRA_REGULAR,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            // تایمر همیشه چپ‌به‌راست (h:m:s)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TimeBlock(h)
                    Text(":", color = Color.White, fontWeight = FontWeight.ExtraBold)
                    TimeBlock(m)
                    Text(":", color = Color.White, fontWeight = FontWeight.ExtraBold)
                    TimeBlock(s)
                }
            }
        }

        Spacer(Modifier.height(Spacing.md))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(campaign.products) { product ->
                MainProductCard(
                    modifier = Modifier.width(165.dp),
                    product = product,
                    onClick = onProductClick,
                    onFavoriteClick = { onFavoriteClick(product) }
                )
            }
        }
    }
}

@Composable
private fun TimeBlock(value: String) {
    Text(
        text = value,
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.xs))
            .background(Color(0x2EFFFFFF))
            .padding(horizontal = 7.dp, vertical = 5.dp),
        color = Color.White,
        fontSize = FontSize.REGULAR,
        fontWeight = FontWeight.Bold
    )
}

private data class TrustItem(val icon: ImageVector, val title: String, val sub: String)

/** نوار نشان‌های اعتماد — استاتیک، بدون نیاز به سرور. */
@Composable
fun TrustBadges(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val items = listOf(
        TrustItem(Icons.Default.LocalShipping, "ارسال سریع", "ارسال به سراسر کشور"),
        TrustItem(Icons.Default.VerifiedUser, "ضمانت اصالت", "تضمین اصل بودن کالا"),
        TrustItem(Icons.Default.Autorenew, "۷ روز بازگشت", "بازگشت آسان کالا"),
        TrustItem(Icons.Default.SupportAgent, "پشتیبانی", "پاسخگویی همه‌روزه")
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Spacing.lg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.line)
        )
        Spacer(Modifier.height(Spacing.lg))
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                rowItems.forEach { item ->
                    TrustBadgeItem(item = item, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TrustBadgeItem(item: TrustItem, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(Radius.sm))
                .background(colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Column {
            Text(
                text = item.title,
                fontSize = FontSize.SMALL,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = item.sub,
                fontSize = FontSize.EXTRA_SMALL,
                color = colors.onSurfaceVariant
            )
        }
    }
}
