package com.kazemieh.admin.blog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.domain.blog.Blog
import com.kazemieh.domain.blog.BlogCategory
import com.kazemieh.designsystem.component.LoadingCard
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
                title = { Text("Admin Panel - Blogs") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (selectedTab == 0) navigateToManageBlog(null, null)
                        else navigateToManageCategory(null)
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text(text = "Articles", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text(text = "Categories", modifier = Modifier.padding(16.dp))
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = blog.title, style = MaterialTheme.typography.titleMedium)
                        SuggestionChip(
                            onClick = {},
                            label = { Text(blog.status ?: "PUBLISHED") }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Views: ${blog.viewCount}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        blog.category?.let {
                            Text(
                                text = "Category: ${it.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { navigateToManageBlog(blog.id, blog.slug) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { viewModel.handleIntent(AdminBlogListIntent.DeleteBlog(blog.id)) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = category.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Blogs: ${category.blogCount}", style = MaterialTheme.typography.bodySmall)
                    }
                    Row {
                        IconButton(onClick = { navigateToManageCategory(category.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { viewModel.handleIntent(AdminBlogListIntent.DeleteCategory(category.id)) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
