package com.kazemieh.admin.products

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.domain.admin.AdminStats
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
import com.kazemieh.admin.options.ManageOptionsScreen
import com.kazemieh.admin.orders.AdminOrderScreen
import com.kazemieh.admin.wallet.AdminWalletScreen
import com.kazemieh.admin.wallet.AdminWithdrawalsScreen
import com.kazemieh.admin.blog.AdminBlogListScreen
import com.kazemieh.admin.story.AdminStoryScreen
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    navigateBack: () -> Unit,
    navigateToManageProduct: (Long?) -> Unit,
    navigateToManageBlog: (Long?, String?) -> Unit,
    navigateToManageCategory: (Long?) -> Unit,
) {
    val viewModel = koinViewModel<AdminPanelViewModel>()
    val state by viewModel.state.collectAsState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val colors = AppTheme.colors
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        "داشبورد", "محصولات", "واریانت‌ها", "سفارش‌ها",
        "کد تخفیف", "استوری", "بلاگ", "کیف پول‌ها", "برداشت‌ها"
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AdminPanelEffect.ShowError -> {}
                else -> {}
            }
        }
    }

    Scaffold(
        containerColor = colors.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Resources.String.AdminPanel),
                        fontSize = FontSize.LARGE,
                        color = colors.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            modifier = Modifier.graphicsLayer { rotationY = if (isRtl) 180f else 0f },
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = stringResource(Resources.String.BackArrowIconDesc),
                            tint = colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.surface,
                    scrolledContainerColor = colors.surface,
                    navigationIconContentColor = colors.onSurface,
                    titleContentColor = colors.onSurface
                )
            )
        },
        floatingActionButton = {
            // فقط در تبِ محصولات؛ بقیه‌ی تب‌ها FAB/اکشنِ خودشان را دارند
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { navigateToManageProduct(null) },
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                ) {
                    Icon(
                        painter = painterResource(Resources.Icon.Plus),
                        contentDescription = stringResource(Resources.String.AddIconDesc)
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (selectedTab == 0) {
                Text(
                    text = "خوش آمدید، مدیر فروشگاه کارمیلا",
                    fontSize = FontSize.SMALL,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            AdminNavChips(selected = selectedTab, tabs = tabs, onSelect = { selectedTab = it })
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = colors.line)

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                    0 -> {
                        val stats = (state.statsState as? AppResult.Success)?.data
                        if (stats != null) AdminDashboard(stats = stats)
                        else LoadingCard(modifier = Modifier.fillMaxSize())
                    }
                    1 -> ProductsTab(
                        state = state,
                        onRefresh = { viewModel.handleIntent(AdminPanelIntent.Refresh) },
                        onProductClick = { id -> navigateToManageProduct(id) }
                    )
                    2 -> ManageOptionsScreen(onBackClick = { selectedTab = 1 }, embedded = true)
                    3 -> AdminOrderScreen(onBackClick = { selectedTab = 1 }, embedded = true)
                    4 -> AdminDiscountsScreen(navigateBack = { selectedTab = 1 }, embedded = true)
                    5 -> AdminStoryScreen(navigateBack = { selectedTab = 1 }, embedded = true)
                    6 -> AdminBlogListScreen(
                        navigateToManageBlog = navigateToManageBlog,
                        navigateToManageCategory = navigateToManageCategory,
                        navigateBack = { selectedTab = 1 },
                        embedded = true
                    )
                    7 -> AdminWalletScreen(onBackClick = { selectedTab = 1 }, embedded = true)
                    8 -> AdminWithdrawalsScreen(onBackClick = { selectedTab = 1 }, embedded = true)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductsTab(
    state: AdminPanelState,
    onRefresh: () -> Unit,
    onProductClick: (Long) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        when (val result = state.productsState) {
            is AppResult.Loading -> LoadingCard(modifier = Modifier.fillMaxSize())
            is AppResult.Success -> {
                val products = result.data.items
                if (products.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(all = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = products, key = { it.id }) { product ->
                            AdminProductCard(product = product, onClick = { onProductClick(product.id) })
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
            is AppResult.Error -> InfoCard(
                image = Resources.Image.Cat,
                title = stringResource(Resources.String.Oops),
                subtitle = result.message
            )
        }
    }
}

/** داشبورد مدیریت — کارت‌های آمار + نمودار فروشِ ۷ روز اخیر (مطابق اسپک). */
@Composable
private fun AdminDashboard(stats: AdminStats) {
    val colors = AppTheme.colors
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item { StatCard("درآمد کل", stringResource(Resources.String.PriceFormat, stats.totalRevenue), "تومان") }
            item { StatCard("سفارش‌ها", stats.totalOrders.toString(), "عدد") }
            item { StatCard("محصولات", stats.totalProducts.toString(), "عدد") }
            item { StatCard("مشتریان", stats.totalCustomers.toString(), "نفر") }
        }
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.line, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("نمودار فروش هفتگی", fontSize = FontSize.EXTRA_REGULAR, fontWeight = FontWeight.ExtraBold, color = colors.onSurface)
                Text("۷ روز اخیر", fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
            }
            Spacer(Modifier.height(16.dp))
            val maxSale = stats.weeklySales.maxOfOrNull { it.total }?.takeIf { it > 0.0 } ?: 1.0
            Row(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.Bottom
            ) {
                stats.weeklySales.forEach { day ->
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        val fraction = (day.total / maxSale).toFloat().coerceIn(0.02f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fraction)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(Brush.verticalGradient(listOf(colors.accent2, colors.primary)))
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = day.date.takeLast(2),
                            fontSize = FontSize.EXTRA_SMALL,
                            color = colors.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, sub: String) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.accentSoft),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(value.take(1), color = colors.primary, fontWeight = FontWeight.ExtraBold, fontSize = FontSize.MEDIUM)
        }
        Spacer(Modifier.height(14.dp))
        Text(value, fontSize = FontSize.MEDIUM, fontWeight = FontWeight.ExtraBold, color = colors.onSurface, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text("$label · $sub", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant, maxLines = 1)
    }
}

/** نوار چیپ‌های پیمایشِ پنل مدیریت — مطابق اسپک. */
@Composable
private fun AdminNavChips(
    selected: Int,
    tabs: List<String>,
    onSelect: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(tabs) { index, label ->
            AdminNavChip(label = label, selected = selected == index, onClick = { onSelect(index) })
        }
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
