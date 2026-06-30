package com.kazemieh.admin.products

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    navigateBack: () -> Unit,
    embedded: Boolean = false
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
            topBar = topBarSlot@{
                if (embedded) return@topBarSlot
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
                                verticalArrangement = Arrangement.spacedBy(11.dp)
                            ) {
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
fun DiscountItem(
    discount: Discount,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = AppTheme.colors
    val valueLabel = if (discount.type == DiscountType.PERCENTAGE) "${discount.value}٪" else "${discount.value}"
    val typeLabel = if (discount.type == DiscountType.PERCENTAGE) "درصدی" else "تومان"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(15.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = discount.code,
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.accentSoft)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = FontSize.REGULAR,
                fontWeight = FontWeight.ExtraBold,
                color = colors.primary
            )
            Spacer(Modifier.width(10.dp))
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                Text(valueLabel, fontSize = FontSize.REGULAR, fontWeight = FontWeight.ExtraBold, color = colors.onSurface)
                Spacer(Modifier.width(4.dp))
                Text(typeLabel, fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
            }
            val statusColor = if (discount.isActive) colors.ok else colors.sale
            Text(
                text = if (discount.isActive) "فعال" else "غیرفعال",
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                fontSize = FontSize.EXTRA_SMALL,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
        Spacer(Modifier.height(11.dp))
        Text(
            text = "استفاده: ${discount.usageCount} از ${discount.usageLimit ?: "∞"}",
            fontSize = FontSize.EXTRA_SMALL,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(11.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Resources.String.Edit),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.accentSoft)
                    .clickable { onClick() }
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
                fontSize = FontSize.SMALL,
                fontWeight = FontWeight.SemiBold,
                color = colors.primary
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.sale.copy(alpha = 0.1f))
                    .clickable { onDelete() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Resources.Icon.Delete),
                    contentDescription = stringResource(Resources.String.Delete),
                    tint = colors.sale,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
