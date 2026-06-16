package com.kazemieh.blog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.CustomTextField
import com.kazemieh.designsystem.components.AppScaffold
import com.kazemieh.designsystem.components.LoadingData
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogListScreen(
    navigateToDetail: (String) -> Unit,
    navigateBack: () -> Unit,
    viewModel: BlogListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    AppScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blogs") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search Bar
            CustomTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.handleIntent(BlogListIntent.Search(it))
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = "Search blogs...",
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            // Category Selector
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedCategoryId == null,
                        onClick = { viewModel.handleIntent(BlogListIntent.SelectCategory(null)) },
                        label = { Text("All") }
                    )
                }
                items(state.categories) { category ->
                    FilterChip(
                        selected = state.selectedCategoryId == category.id,
                        onClick = { viewModel.handleIntent(BlogListIntent.SelectCategory(category.id)) },
                        label = { Text(category.name) }
                    )
                }
            }

            if (state.isLoading) {
                LoadingData()
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.blogs) { blog ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navigateToDetail(blog.slug) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = blog.title, style = MaterialTheme.typography.titleMedium)
                            blog.summary?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
