package com.kazemieh.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.domain.blog.BlogBlock
import com.seiko.imageloader.rememberImagePainter

@Composable
fun BlogContentRenderer(
    content: List<BlogBlock>,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        content.forEach { block ->
            when (block) {
                is BlogBlock.Header -> {
                    Text(
                        text = block.text,
                        style = when (block.level) {
                            1 -> MaterialTheme.typography.headlineLarge
                            2 -> MaterialTheme.typography.headlineMedium
                            else -> MaterialTheme.typography.headlineSmall
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                is BlogBlock.Paragraph -> {
                    Text(
                        text = block.text,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }
                is BlogBlock.Image -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        androidx.compose.foundation.Image(
                            painter = rememberImagePainter(block.url),
                            contentDescription = block.caption,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp),
                            contentScale = ContentScale.Crop
                        )
                        block.caption?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
                is BlogBlock.Button -> {
                    Text(
                        text = block.text,
                        modifier = Modifier
                            .clip(RoundedCornerShape(13.dp))
                            .background(colors.primary)
                            .clickable(enabled = block.url.isNotBlank()) { runCatching { uriHandler.openUri(block.url) } }
                            .padding(horizontal = 26.dp, vertical = 13.dp),
                        color = colors.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                is BlogBlock.ListBlock -> {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        block.items.forEach { item ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text("•  ", color = colors.primary, fontWeight = FontWeight.Bold)
                                Text(item, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                            }
                        }
                    }
                }
                is BlogBlock.Quote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.accentSoft)
                            .padding(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .heightIn(min = 24.dp)
                                .background(colors.primary)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = block.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurface,
                            lineHeight = 26.sp
                        )
                    }
                }
                is BlogBlock.Divider -> {
                    HorizontalDivider(color = colors.line)
                }
                is BlogBlock.Unknown -> {
                    // Skip or handle as placeholder
                }
            }
        }
    }
}
