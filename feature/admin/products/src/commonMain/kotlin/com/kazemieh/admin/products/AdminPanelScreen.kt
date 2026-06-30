package com.kazemieh.admin.products

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.domain.catalog.ProductSummary
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    navigateBack: () -> Unit,
    navigateToManageProduct: (Long?) -> Unit,
    navigateToManageOrders: () -> Unit,
    navigateToManageOptions: () -> Unit,
    navigateToManageDiscounts: () -> Unit,
    navigateToManageWallets: () -> Unit,
    navigateToManageWithdrawals: () -> Unit,
    navigateToManageStories: () -> Unit,
    navigateToBlog: () -> Unit,
    navigateToAdminBlog: () -> Unit,
) {
    val viewModel = koinViewModel<AdminPanelViewModel>()
    val state by viewModel.state.collectAsState()
    var searchBarVisible by mutableStateOf(false)
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AdminPanelEffect.ShowError -> {
                    // Handle error
                }
                else -> {}
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
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
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = navigateBack) {
                                Icon(
                                    modifier = Modifier.graphicsLayer { rotationY = if (isRtl) 180f else 0f },
                                    painter = painterResource(Resources.Icon.BackArrow),
                                    contentDescription = stringResource(Resources.String.BackArrowIconDesc),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { searchBarVisible = true }) {
                                Icon(
                                    painter = painterResource(Resources.Icon.Search),
                                    contentDescription = stringResource(Resources.String.SearchIconDesc),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = "خوش آمدید، مدیر فروشگاه کارمیلا",
                fontSize = FontSize.SMALL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            AdminNavChips(
                onProducts = {},
                onVariants = navigateToManageOptions,
                onOrders = navigateToManageOrders,
                onDiscounts = navigateToManageDiscounts,
                onStories = navigateToManageStories,
                onBlog = navigateToAdminBlog,
                onWallets = navigateToManageWallets,
                onWithdrawals = navigateToManageWithdrawals
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = com.kazemieh.designsystem.AppTheme.colors.line)
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.handleIntent(AdminPanelIntent.Refresh) },
            modifier = Modifier.weight(1f).fillMaxWidth()
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
}

/** نوار چیپ‌های پیمایشِ پنل مدیریت — مطابق اسپک. */
@Composable
private fun AdminNavChips(
    onProducts: () -> Unit,
    onVariants: () -> Unit,
    onOrders: () -> Unit,
    onDiscounts: () -> Unit,
    onStories: () -> Unit,
    onBlog: () -> Unit,
    onWallets: () -> Unit,
    onWithdrawals: () -> Unit
) {
    val colors = com.kazemieh.designsystem.AppTheme.colors
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { AdminNavChip("محصولات", selected = true, onClick = onProducts) }
        item { AdminNavChip("واریانت‌ها", selected = false, onClick = onVariants) }
        item { AdminNavChip("سفارش‌ها", selected = false, onClick = onOrders) }
        item { AdminNavChip("کد تخفیف", selected = false, onClick = onDiscounts) }
        item { AdminNavChip("استوری", selected = false, onClick = onStories) }
        item { AdminNavChip("بلاگ", selected = false, onClick = onBlog) }
        item { AdminNavChip("کیف پول‌ها", selected = false, onClick = onWallets) }
        item { AdminNavChip("برداشت‌ها", selected = false, onClick = onWithdrawals) }
    }
}

@Composable
private fun AdminNavChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = com.kazemieh.designsystem.AppTheme.colors
    Text(
        text = label,
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .then(
                if (selected) Modifier.background(colors.primary)
                else Modifier.background(colors.surface).border(1.dp, colors.line, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 9.dp),
        fontSize = FontSize.REGULAR,
        fontWeight = FontWeight.Bold,
        color = if (selected) colors.onPrimary else colors.onSurfaceVariant
    )
}
