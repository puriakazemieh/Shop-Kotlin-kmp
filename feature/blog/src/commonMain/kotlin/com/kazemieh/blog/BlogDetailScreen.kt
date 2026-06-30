package com.kazemieh.blog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.util.formatDateTime
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.component.BlogContentRenderer
import com.kazemieh.designsystem.component.LoadingCard
import com.seiko.imageloader.rememberImagePainter
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogDetailScreen(
    slug: String,
    navigateBack: () -> Unit,
    navigateToDetail: (String) -> Unit,
    viewModel: BlogDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(slug) {
        viewModel.handleIntent(BlogDetailIntent.LoadBlog(slug))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.blog?.title ?: "مقاله",
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingCard(modifier = Modifier.fillMaxSize())
        } else {
            state.blog?.let { blog ->
                val colors = AppTheme.colors
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    blog.thumbnailUrl?.let { url ->
                        item {
                            Image(
                                painter = rememberImagePainter(url),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(colors.surfaceVariant)
                                    .border(1.dp, colors.line, RoundedCornerShape(22.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    item {
                        Text(
                            text = (blog.categoryName ?: blog.category?.name ?: "عمومی"),
                            fontFamily = AppFont(),
                            fontSize = FontSize.SMALL,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = blog.title,
                            fontFamily = AppFont(),
                            fontSize = FontSize.LARGE,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "نویسنده: ${blog.authorName ?: "کارمیلا"} · ${formatDateTime(blog.createdAt)} · ${blog.readingTimeMinutes} دقیقه مطالعه · ${blog.viewCount} بازدید",
                            fontFamily = AppFont(),
                            fontSize = FontSize.SMALL,
                            color = colors.onSurfaceVariant
                        )
                    }
                    blog.content?.let { blocks ->
                        item {
                            BlogContentRenderer(content = blocks)
                        }
                    }

                    if (state.relatedBlogs.isNotEmpty()) {
                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                            Text(
                                text = "مطالب مرتبط",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(top = 8.dp)
                            ) {
                                items(state.relatedBlogs) { related ->
                                    Card(
                                        modifier = Modifier
                                            .width(200.dp)
                                            .clickable { navigateToDetail(related.slug) }
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = related.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
