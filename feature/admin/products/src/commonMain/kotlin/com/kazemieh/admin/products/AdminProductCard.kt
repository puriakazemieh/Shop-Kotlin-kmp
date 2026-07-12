package com.kazemieh.admin.products

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.util.formatToman
import com.kazemieh.domain.admin.AdminProduct
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * کارتِ محصول در لیستِ مدیریت — مطابق اسپک:
 * تصویر/رنگِ محصول + عنوان + «موجودی: X · دسته» + قیمت + سه دکمه (حذف/ویرایش/واریانت‌ها).
 */
@Composable
fun AdminProductCard(
    product: AdminProduct,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onVariants: () -> Unit,
) {
    val colors = AppTheme.colors
    val price = product.discountedPrice ?: product.basePrice ?: 0.0
    val lowStock = product.stock <= 5

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // تصویرِ محصول (سمت راست در RTL)
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(placeholderColor(product.id)),
            contentAlignment = Alignment.Center
        ) {
            if (!product.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = product.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.title,
                fontFamily = AppFont(),
                fontSize = FontSize.REGULAR,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = buildString {
                    append("موجودی: ${product.stock}")
                    product.categoryName?.takeIf { it.isNotBlank() }?.let { append(" · $it") }
                },
                fontFamily = AppFont(),
                fontSize = FontSize.SMALL,
                color = if (lowStock) colors.sale else colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(Resources.String.PriceFormat, formatToman(price)),
                fontFamily = AppFont(),
                fontSize = FontSize.REGULAR,
                fontWeight = FontWeight.ExtraBold,
                color = colors.primary,
                maxLines = 1
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ActionIcon(icon = Resources.Icon.Delete, tint = colors.sale, bg = colors.sale.copy(alpha = 0.1f), onClick = onDelete)
                ActionIcon(icon = Resources.Icon.Edit, tint = colors.primary, bg = colors.accentSoft, onClick = onEdit)
                ActionIcon(icon = Resources.Icon.Categories, tint = colors.onSurfaceVariant, bg = colors.surfaceVariant, onClick = onVariants)
            }
        }
    }
}

@Composable
private fun ActionIcon(
    icon: org.jetbrains.compose.resources.DrawableResource,
    tint: Color,
    bg: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
    }
}

/** رنگِ پس‌زمینه‌ی جایگزین برای محصولاتِ بدون تصویر (پالتِ ملایمِ کارمیلا). */
private fun placeholderColor(id: Long): Color {
    val palette = listOf(
        Color(0xFFE9D9C2), Color(0xFFD8E0EC), Color(0xFFE7D3CC),
        Color(0xFFCFE3D5), Color(0xFFE0D3EC), Color(0xFFF0E2C6)
    )
    return palette[(id % palette.size).toInt()]
}
