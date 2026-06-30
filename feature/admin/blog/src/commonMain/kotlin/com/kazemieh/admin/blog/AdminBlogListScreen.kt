package com.kazemieh.admin.blog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.domain.blog.Blog
import com.kazemieh.domain.blog.BlogCategory
import com.kazemieh.designsystem.component.LoadingCard
import com.seiko.imageloader.rememberImagePainter
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBlogListScreen(
    navigateToManageBlog: (Long?, String?) -> Unit,
    navigateToManageCategory: (Long?) -> Unit,
    navigateBack: () -> Unit,
    viewModel: AdminBlogListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مدیریت مجله") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (selectedTab == 0) navigateToManageBlog(null, null)
                        else navigateToManageCategory(null)
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text(text = "مقاله‌ها", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text(text = "دسته‌بندی‌ها", modifier = Modifier.padding(16.dp))
                }
            }

            if (state.isLoading) {
                LoadingCard(modifier = Modifier.fillMaxSize())
            } else {
                when (selectedTab) {
                    0 -> ArticlesList(state.blogs, navigateToManageBlog, viewModel)
                    1 -> CategoriesList(state.categories, navigateToManageCategory, viewModel)
                }
            }
        }
    }
}

@Composable
private fun ArticlesList(
    blogs: List<Blog>,
    navigateToManageBlog: (Long?, String?) -> Unit,
    viewModel: AdminBlogListViewModel
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(blogs) { blog ->
            val colors = AppTheme.colors
            val published = (blog.status ?: "PUBLISHED").uppercase() == "PUBLISHED"
            val statusColor = if (published) colors.ok else colors.star
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.line, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 54.dp, height = 40.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(colors.surfaceVariant)
                        .clickable { navigateToManageBlog(blog.id, blog.slug) }
                ) {
                    blog.thumbnailUrl?.let {
                        Image(
                            painter = rememberImagePainter(it),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.width(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = blog.title,
                        fontSize = FontSize.SMALL,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    blog.category?.let {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(text = it.name, fontSize = FontSize.EXTRA_SMALL, color = colors.primary)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (published) "منتشرشده" else "پیش‌نویس",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = FontSize.EXTRA_SMALL,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(colors.accentSoft)
                        .clickable { navigateToManageBlog(blog.id, blog.slug) },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp), tint = colors.primary) }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(colors.sale.copy(alpha = 0.1f))
                        .clickable { viewModel.handleIntent(AdminBlogListIntent.DeleteBlog(blog.id)) },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp), tint = colors.sale) }
            }
        }
    }
}

@Composable
private fun CategoriesList(
    categories: List<BlogCategory>,
    navigateToManageCategory: (Long?) -> Unit,
    viewModel: AdminBlogListViewModel
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { category ->
            val colors = AppTheme.colors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.line, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = category.name, fontSize = FontSize.REGULAR, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = "${category.blogCount} مقاله", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(colors.accentSoft)
                        .clickable { navigateToManageCategory(category.id) },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp), tint = colors.primary) }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(colors.sale.copy(alpha = 0.1f))
                        .clickable { viewModel.handleIntent(AdminBlogListIntent.DeleteCategory(category.id)) },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp), tint = colors.sale) }
            }
        }
    }
}
