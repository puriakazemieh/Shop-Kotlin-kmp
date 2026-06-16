package com.kazemieh.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.domain.blog.BlogBlock
import io.github.aakira.napier.Napier // Assuming Napier for logging if needed, or just remove
// Replace Kamel with ImageLoader if that's what's in libs.versions.toml
import com.seiko.imageloader.rememberImagePainter

@Composable
fun BlogContentRenderer(
    content: List<BlogBlock>,
    modifier: Modifier = Modifier
) {
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
                is BlogBlock.Unknown -> {
                    // Skip or handle as placeholder
                }
            }
        }
    }
}
