package com.kazemieh.home.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.home.component.ProductCard
import org.jetbrains.compose.resources.stringResource
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.util.anyToString
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySearchScreen(
    categoryId: Long,
    categoryName: String,
    navigateBack: () -> Unit,
    navigateToDetails: (String) -> Unit
) {
    val viewModel = koinViewModel<CategorySearchViewModel>()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(categoryId, categoryName) {
        viewModel.onIntent(CategorySearchIntent.Init(categoryId, categoryName))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SearchBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        inputField = {
                            SearchBarDefaults.InputField(
                                modifier = Modifier.fillMaxWidth(),
                                query = state.searchQuery,
                                onQueryChange = { viewModel.onIntent(CategorySearchIntent.UpdateSearchQuery(it)) },
                                onSearch = {},
                                expanded = false,
                                onExpandedChange = {},
                                placeholder = {
                                    Text(
                                        text = stringResource(Resources.String.SearchInCategoryFormat, state.categoryName),
                                        fontSize = FontSize.REGULAR,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                },
                                trailingIcon = {
                                    if (state.searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.onIntent(CategorySearchIntent.UpdateSearchQuery("")) }) {
                                            Icon(
                                                painter = painterResource(Resources.Icon.Close),
                                                contentDescription = stringResource(Resources.String.CloseDesc)
                                            )
                                        }
                                    }
                                }
                            )
                        },
                        expanded = false,
                        onExpandedChange = {},
                        content = {},
                        colors = SearchBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            dividerColor = MaterialTheme.colorScheme.outline
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = stringResource(Resources.String.BackDesc)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(Resources.Icon.Search),
                            contentDescription = stringResource(Resources.String.SearchDesc)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (state.isLoading) {
                LoadingCard(modifier = Modifier.fillMaxSize())
            } else if (state.error != null) {
                InfoCard(
                    image = Resources.Image.Cat,
                    title = stringResource(Resources.String.Oops),
                    subtitle = state.error!!
                )
            } else if (state.products.isEmpty()) {
                InfoCard(
                    image = Resources.Image.Cat,
                    title = stringResource(Resources.String.NothingHere),
                    subtitle = stringResource(Resources.String.NoProductInCategory)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.products) { product ->
                        ProductCard(
                            product = product,
                            onClick = { navigateToDetails(it) }
                        )
                    }
                }
            }
        }
    }
}
