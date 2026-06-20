package com.kazemieh.blog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.BlogContentRenderer
import com.kazemieh.designsystem.component.LoadingCard
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
        topBar = {
            TopAppBar(
                title = { Text(state.blog?.title ?: "Blog Detail") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingCard(modifier = Modifier.fillMaxSize())
        } else {
            state.blog?.let { blog ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(text = blog.title, style = MaterialTheme.typography.headlineLarge)
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            blog.category?.let {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(it.name) }
                                )
                            }
                            AssistChip(
                                onClick = {},
                                label = { Text("${blog.viewCount} views") }
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("${blog.readingTimeMinutes} min read") }
                            )
                        }
                        Text(
                            text = "By ${blog.authorName ?: "Staff"} • ${blog.createdAt}",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 8.dp)
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
                                text = "Related Articles",
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
