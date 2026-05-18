package com.kazemieh.home.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kazemieh.domain.model.ProductSummary
import com.kazemieh.designsystem.Alpha
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.AppFont
import com.seiko.imageloader.rememberImagePainter

@Composable
fun MainProductCard(
    modifier: Modifier = Modifier,
    product: ProductSummary,
    isLarge: Boolean = false,
    onClick: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedScale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val animatedRotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(size = 12.dp))
            .clickable { onClick(product.slug) }
    ) {
        val painter = rememberImagePainter(product.thumbnailUrl ?: "")
        Image(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isLarge) Modifier
                        .scale(animatedScale.value)
                        .rotate(animatedRotation.value)
                    else Modifier
                ),
            painter = painter,
            contentDescription = "Product thumbnail",
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black,
                            Color.Black.copy(Alpha.ZERO)
                        ),
                        startY = Float.POSITIVE_INFINITY,
                        endY = 0.0f
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 12.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = product.title,
                fontSize = FontSize.MEDIUM,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                fontFamily = AppFont(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            product.categoryName?.let {
                Text(
                    text = it,
                    fontSize = FontSize.REGULAR,
                    color = Color.White.copy(alpha = Alpha.HALF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val priceText = if (product.minPrice == product.maxPrice) {
                    "$${product.minPrice}"
                } else {
                    "$${product.minPrice} - $${product.maxPrice}"
                }
                
                Text(
                    text = priceText,
                    fontSize = FontSize.REGULAR,
                    color = Color.Yellow, // Placeholder for TextBrand
                    fontWeight = FontWeight.Medium
                )
                
                if (!product.inStock) {
                    Text(
                        text = "Out of Stock",
                        fontSize = FontSize.EXTRA_SMALL,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
