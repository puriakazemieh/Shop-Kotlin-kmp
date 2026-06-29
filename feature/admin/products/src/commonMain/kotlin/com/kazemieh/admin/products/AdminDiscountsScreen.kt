package com.kazemieh.admin.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.admin.Discount
import com.kazemieh.domain.admin.DiscountType
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDiscountsScreen(
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<AdminDiscountsViewModel>()
    val state by viewModel.state.collectAsState()
    val messageBarState = rememberMessageBarState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    var showDialog by remember { mutableStateOf(false) }
    var selectedDiscount by remember { mutableStateOf<Discount?>(null) }
    var searchVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AdminDiscountsEffect.ShowError -> messageBarState.addError(effect.message)
                is AdminDiscountsEffect.Success -> messageBarState.addSuccess(effect.message)
            }
        }
    }

    ContentWithMessageBar(messageBarState = messageBarState) {
        Scaffold(
            topBar = {
                if (searchVisible) {
                    SearchBar(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = state.searchQuery,
                                onQueryChange = { viewModel.handleIntent(AdminDiscountsIntent.Search(it)) },
                                onSearch = {},
                                expanded = false,
                                onExpandedChange = {},
                                placeholder = { Text(stringResource(Resources.String.SearchHere)) },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        searchVisible = false
                                        viewModel.handleIntent(AdminDiscountsIntent.Search(""))
                                    }) {
                                        Icon(painter = painterResource(Resources.Icon.Close), contentDescription = null)
                                    }
                                }
                            )
                        },
                        expanded = false,
                        onExpandedChange = {},
                        content = {}
                    )
                } else {
                    TopAppBar(
                        title = { Text(stringResource(Resources.String.CreateDiscount)) },
                        navigationIcon = {
                            IconButton(onClick = navigateBack) {
                                Icon(
                                    modifier = Modifier.graphicsLayer { rotationY = if (isRtl) 180f else 0f },
                                    painter = painterResource(Resources.Icon.BackArrow),
                                    contentDescription = null
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { searchVisible = true }) {
                                Icon(painter = painterResource(Resources.Icon.Search), contentDescription = null)
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    selectedDiscount = null
                    showDialog = true
                }) {
                    Icon(painter = painterResource(Resources.Icon.Plus), contentDescription = null)
                }
            }
        ) { padding ->
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.handleIntent(AdminDiscountsIntent.Refresh) },
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                when (val result = state.discountsState) {
                    is AppResult.Loading -> LoadingCard(modifier = Modifier.fillMaxSize())
                    is AppResult.Success -> {
                        val discounts = state.filteredDiscounts
                        if (discounts.isEmpty()) {
                            InfoCard(
                                image = Resources.Image.Cat,
                                title = stringResource(Resources.String.NothingHere),
                                subtitle = ""
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    DiscountHeader()
                                }
                                items(discounts) { discount ->
                                    DiscountItem(
                                        discount = discount,
                                        onClick = {
                                            selectedDiscount = discount
                                            showDialog = true
                                        },
                                        onDelete = {
                                            viewModel.handleIntent(AdminDiscountsIntent.DeleteDiscount(discount.id))
                                        }
                                    )
                                }
                            }
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

    if (showDialog) {
        CreateDiscountDialog(
            discount = selectedDiscount,
            onDismiss = { showDialog = false },
            onConfirm = { code, type, value, max, min, limit, active ->
                if (selectedDiscount == null) {
                    viewModel.handleIntent(
                        AdminDiscountsIntent.CreateDiscount(
                            com.kazemieh.domain.admin.CreateDiscountParam(
                                code, type, value, max, min, null, null, limit, active
                            )
                        )
                    )
                } else {
                    viewModel.handleIntent(
                        AdminDiscountsIntent.UpdateDiscount(
                            selectedDiscount!!.id,
                            com.kazemieh.domain.admin.UpdateDiscountParam(
                                code, type, value, max, min, null, null, limit, active
                            )
                        )
                    )
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun DiscountHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(Resources.String.DiscountCodeLabel), modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL)
        Text(stringResource(Resources.String.DiscountValueLabel), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL)
        Text("Usage", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL)
        Text("Status", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL)
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
fun DiscountItem(
    discount: Discount,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(discount.code, modifier = Modifier.weight(1.5f), fontSize = FontSize.REGULAR)
            Text(
                text = if (discount.type == DiscountType.PERCENTAGE) "${discount.value}%" else "${discount.value}",
                modifier = Modifier.weight(1f),
                fontSize = FontSize.REGULAR
            )
            Text(
                text = "${discount.usageCount}/${discount.usageLimit ?: "∞"}",
                modifier = Modifier.weight(1f),
                fontSize = FontSize.REGULAR
            )
            Badge(
                containerColor = if (discount.isActive) AppTheme.colors.ok else AppTheme.colors.sale,
                contentColor = Color.White
            ) {
                Text(if (discount.isActive) "Active" else "Inactive")
            }
            IconButton(onClick = onDelete) {
                Icon(painter = painterResource(Resources.Icon.Delete), contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
