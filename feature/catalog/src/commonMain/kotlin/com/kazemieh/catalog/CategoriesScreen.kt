package com.kazemieh.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.catalog.CategoryCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    navigateToCategorySearch: (Long, String) -> Unit,
) {
    val viewModel = koinViewModel<CategoriesViewModel>()
    val state by viewModel.state.collectAsState()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.handleIntent(CategoriesIntent.Refresh) },
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.isLoading) {
            LoadingCard(modifier = Modifier.fillMaxSize())
        } else if (state.categories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                InfoCard(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    image = Resources.Image.Cat,
                    title = stringResource(Resources.String.CategoriesIsEmpty),
                    subtitle = stringResource(Resources.String.CategoriesIsEmptySubtitle)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = state.categories,
                    key = { it.id }
                ) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { navigateToCategorySearch(category.id, category.name) }
                    )
                }
            }
        }
    }
}
