package com.kazemieh.home.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.Alpha
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.domain.model.ProductSummary
import com.seiko.imageloader.rememberImagePainter
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProductCard(
    modifier: Modifier = Modifier,
    product: ProductSummary,
    onClick: (String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = Alpha.HALF))
            .clickable { onClick(product.slug) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val painter = rememberImagePainter(product.thumbnailUrl ?: "")
        Image(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp)),
            painter = painter,
            contentDescription = stringResource(Resources.String.ProductThumbnailDesc),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = product.title,
                fontSize = FontSize.MEDIUM,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            product.categoryName?.let {
                Text(
                    text = it,
                    fontSize = FontSize.EXTRA_SMALL,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alpha.HALF),
                    maxLines = 1
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            horizontalAlignment = Alignment.End
        ) {
            val priceText = if (product.minPrice == product.maxPrice) {
                stringResource(Resources.String.PriceFormat, product.minPrice ?: 0.0)
            } else {
                stringResource(
                    Resources.String.PriceRangeFormat,
                    stringResource(Resources.String.PriceFormat, product.minPrice ?: 0.0),
                    stringResource(Resources.String.PriceFormat, product.maxPrice ?: 0.0)
                )
            }
            Text(
                text = priceText,
                fontSize = FontSize.REGULAR,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            if (!product.inStock) {
                Text(
                    text = stringResource(Resources.String.OutOfStock),
                    fontSize = FontSize.EXTRA_SMALL,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
