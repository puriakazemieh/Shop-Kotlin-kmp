package com.kazemieh.catalog

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.Resources
import com.kazemieh.domain.catalog.ProductSummary
import com.seiko.imageloader.rememberImagePainter
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * کارت محصولِ کارمیلا — مطابق اسپک دیزاین (کارت سفید).
 * تصویر بالا (بَج تخفیف + قلب + پوشش ناموجود)، بدنه: دسته، نام، قیمت + دکمه‌ی افزودن.
 */
@Composable
fun MainProductCard(
    modifier: Modifier = Modifier,
    product: ProductSummary,
    isLarge: Boolean = false,
    onClick: (String) -> Unit,
    onFavoriteClick: () -> Unit = {}
) {
    val colors = AppTheme.colors
    val hasDiscount = product.minDiscountedPrice != null
    val displayPrice = product.minDiscountedPrice ?: product.minPrice
    val discountPercent = if (hasDiscount && product.minPrice != null && product.minPrice!! > 0.0) {
        ((1.0 - (product.minDiscountedPrice!! / product.minPrice!!)) * 100).roundToInt()
    } else 0

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
            .clickable { onClick(product.slug) }
    ) {
        // ---- تصویر ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isLarge) Modifier.height(220.dp) else Modifier.height(170.dp))
                .background(colors.surfaceVariant)
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = rememberImagePainter(product.thumbnailUrl ?: ""),
                contentDescription = stringResource(Resources.String.ProductThumbnailDesc),
                contentScale = ContentScale.Crop
            )

            if (hasDiscount && discountPercent > 0) {
                Text(
                    text = "$discountPercent٪",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(Radius.xs))
                        .background(colors.sale)
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                    color = Color.White,
                    fontSize = FontSize.SMALL,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(9.dp)
                    .size(32.dp)
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(colors.surface)
                    .clickable { onFavoriteClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = if (product.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (product.isFavorite) colors.sale else colors.onSurfaceVariant
                )
            }

            if (!product.inStock) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Resources.String.OutOfStock),
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.full))
                            .background(colors.onBackground.copy(alpha = 0.82f))
                            .padding(horizontal = 16.dp, vertical = 7.dp),
                        color = colors.surface,
                        fontSize = FontSize.SMALL,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ---- بدنه ----
        Column(modifier = Modifier.padding(start = 13.dp, end = 13.dp, top = 12.dp, bottom = 14.dp)) {
            product.categoryName?.let {
                Text(
                    text = it,
                    fontSize = FontSize.EXTRA_SMALL,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
            }
            Text(
                text = product.title,
                fontSize = FontSize.REGULAR,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                lineHeight = FontSize.MEDIUM,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(40.dp)
            )
            Spacer(Modifier.height(9.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    if (hasDiscount && product.minPrice != null) {
                        Text(
                            text = stringResource(Resources.String.PriceFormat, product.minPrice ?: 0.0),
                            fontSize = FontSize.EXTRA_SMALL,
                            color = colors.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                    Text(
                        text = stringResource(Resources.String.PriceFormat, displayPrice ?: 0.0),
                        fontSize = FontSize.EXTRA_REGULAR,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(Radius.sm))
                        .background(colors.primary)
                        .clickable { onClick(product.slug) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(17.dp),
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = colors.onPrimary
                    )
                }
            }
        }
    }
}
