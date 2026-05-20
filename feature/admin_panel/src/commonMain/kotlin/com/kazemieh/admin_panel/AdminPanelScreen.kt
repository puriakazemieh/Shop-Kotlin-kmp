package com.kazemieh.admin_panel

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.domain.model.ProductSummary
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    navigateBack: () -> Unit,
    navigateToManageProduct: (Long?) -> Unit,
    navigateToManageOrders: () -> Unit,
) {
    val viewModel = koinViewModel<AdminPanelViewModel>()
    val state by viewModel.state.collectAsState()
    var searchBarVisible by mutableStateOf(false)

    LaunchedEffect(Unit) {
        viewModel.handleIntent(AdminPanelIntent.LoadProducts)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AnimatedContent(
                targetState = searchBarVisible
            ) { visible ->
                if (visible) {
                    SearchBar(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxWidth(),
                        inputField = {
                            SearchBarDefaults.InputField(
                                modifier = Modifier.fillMaxWidth(),
                                query = state.searchQuery,
                                onQueryChange = { viewModel.handleIntent(AdminPanelIntent.SearchProducts(it)) },
                                expanded = false,
                                onExpandedChange = {},
                                onSearch = {},
                                placeholder = {
                                    Text(
                                        text = stringResource(Resources.String.SearchHere),
                                        fontSize = FontSize.REGULAR,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        modifier = Modifier.size(14.dp),
                                        onClick = {
                                            if (state.searchQuery.isNotEmpty()) viewModel.handleIntent(AdminPanelIntent.SearchProducts(""))
                                            else searchBarVisible = false
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(Resources.Icon.Close),
                                            contentDescription = stringResource(Resources.String.CloseIconDesc)
                                        )
                                    }
                                }
                            )
                        },
                        colors = SearchBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            dividerColor = MaterialTheme.colorScheme.outline
                        ),
                        expanded = false,
                        onExpandedChange = {},
                        content = {}
                    )
                } else {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(Resources.String.AdminPanel),
                                fontSize = FontSize.LARGE,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = navigateBack) {
                                Icon(
                                    painter = painterResource(Resources.Icon.BackArrow),
                                    contentDescription = stringResource(Resources.String.BackArrowIconDesc),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = navigateToManageOrders) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = stringResource(Resources.String.OrdersIconDesc),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            IconButton(onClick = { searchBarVisible = true }) {
                                Icon(
                                    painter = painterResource(Resources.Icon.Search),
                                    contentDescription = stringResource(Resources.String.SearchIconDesc),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            scrolledContainerColor = MaterialTheme.colorScheme.background,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigateToManageProduct(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                content = {
                    Icon(
                        painter = painterResource(Resources.Icon.Plus),
                        contentDescription = stringResource(Resources.String.AddIconDesc)
                    )
                }
            )
        }
    ) { padding ->
        val result = state.productsState
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.handleIntent(AdminPanelIntent.Refresh) },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            when (result) {
                is AppResult.Loading -> LoadingCard(modifier = Modifier.fillMaxSize())
                is AppResult.Success -> {
                    val products = result.data.items
                    if (products.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(all = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = products,
                                key = { it.id }
                            ) { product ->
                                AdminProductCard(
                                    product = product,
                                    onClick = { navigateToManageProduct(product.id) }
                                )
                            }
                        }
                    } else {
                        InfoCard(
                            image = Resources.Image.Cat,
                            title = stringResource(Resources.String.Oops),
                            subtitle = stringResource(Resources.String.NothingHere)
                        )
                    }
                }

                is AppResult.Error -> {
                    InfoCard(
                        image = Resources.Image.Cat,
                        title = stringResource(Resources.String.Oops),
                        subtitle = result.message
                    )
                }
            }
        }
    }
}
