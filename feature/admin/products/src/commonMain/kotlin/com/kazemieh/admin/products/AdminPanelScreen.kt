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
import com.kazemieh.designsystem.ContentWidth
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.domain.admin.AdminProduct
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
import com.kazemieh.designsystem.util.formatToman
import com.kazemieh.domain.catalog.ProductSummary
import com.kazemieh.admin.options.ManageOptionsScreen
import com.kazemieh.admin.orders.AdminOrderScreen
import com.kazemieh.admin.wallet.AdminFinanceScreen
import com.kazemieh.admin.blog.AdminBlogListScreen
import com.kazemieh.admin.story.AdminStoryScreen
import com.kazemieh.admin.academy.AdminAcademyScreen
import com.kazemieh.admin.psychtest.AdminPsychTestScreen
import com.kazemieh.admin.clinic.AdminClinicScreen
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    navigateBack: () -> Unit,
    navigateToManageProduct: (Long?) -> Unit,
    navigateToManageBlog: (Long?, String?) -> Unit,
) {
    val viewModel = koinViewModel<AdminPanelViewModel>()
    val state by viewModel.state.collectAsState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val colors = AppTheme.colors
    var selectedTab by remember { mutableStateOf(0) }
    var productToDelete by remember { mutableStateOf<com.kazemieh.domain.admin.AdminProduct?>(null) }

    val tabs = listOf(
        "داشبورد", "محصولات", "دوره و کارگاه", "نوبت‌دهی", "تست‌ها",
        "واریانت‌ها", "سفارش‌ها", "کد تخفیف", "استوری", "بلاگ",
        "مالی و برداشت"
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
            // هر تب اکشنِ افزودنِ خودش را به‌صورت اینلاین دارد (مطابق مرجع)
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().responsiveMaxWidth(ContentWidth.wide)) {
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
                        onEdit = { id -> navigateToManageProduct(id) },
                        onVariants = { id -> navigateToManageProduct(id) },
                        onDelete = { product -> productToDelete = product },
                        onAdd = { navigateToManageProduct(null) }
                    )
                    2 -> AdminAcademyScreen(onBackClick = { selectedTab = 1 }, embedded = true)
                    3 -> AdminClinicScreen(onBackClick = { selectedTab = 1 }, embedded = true)
                    4 -> AdminPsychTestScreen(onBackClick = { selectedTab = 1 }, embedded = true)
                    5 -> ManageOptionsScreen(onBackClick = { selectedTab = 1 }, embedded = true)
                    6 -> AdminOrderScreen(onBackClick = { selectedTab = 1 }, embedded = true)
                    7 -> AdminDiscountsScreen(navigateBack = { selectedTab = 1 }, embedded = true)
                    8 -> AdminStoryScreen(navigateBack = { selectedTab = 1 }, embedded = true)
                    9 -> AdminBlogListScreen(
                        navigateToManageBlog = navigateToManageBlog,
                        navigateBack = { selectedTab = 1 },
                        embedded = true
                    )
                    10 -> AdminFinanceScreen(onBackClick = { selectedTab = 1 })
                }
            }
        }
    }

    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("حذف محصول") },
            text = { Text("آیا از حذف «${product.title}» مطمئن هستید؟ این عمل قابل بازگشت نیست.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.handleIntent(AdminPanelIntent.DeleteProduct(product.id))
                    productToDelete = null
                }) { Text("حذف", color = colors.sale, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) { Text("انصراف") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductsTab(
    state: AdminPanelState,
    onRefresh: () -> Unit,
    onEdit: (Long) -> Unit,
    onVariants: (Long) -> Unit,
    onDelete: (AdminProduct) -> Unit,
    onAdd: () -> Unit
) {
    val colors = AppTheme.colors
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        when (val result = state.productsState) {
            is AppResult.Loading -> LoadingCard(modifier = Modifier.fillMaxSize())
            is AppResult.Success -> {
                val products = result.data.items
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(all = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // هدرِ مدیریت محصولات + دکمه‌ی افزودن (مطابق مرجع)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مدیریت محصولات (${result.data.totalElements} کالا)",
                                fontWeight = FontWeight.Bold,
                                fontSize = FontSize.REGULAR,
                                color = colors.onSurface
                            )
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(colors.primary)
                                    .clickable { onAdd() }
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(painterResource(Resources.Icon.Plus), contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(15.dp))
                                Text("افزودن محصول", color = colors.onPrimary, fontSize = FontSize.SMALL, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (products.isEmpty()) {
                        item {
                            InfoCard(
                                image = Resources.Image.Cat,
                                title = stringResource(Resources.String.Oops),
                                subtitle = stringResource(Resources.String.NothingHere)
                            )
                        }
                    } else {
                        items(items = products, key = { it.id }) { product ->
                            AdminProductCard(
                                product = product,
                                onEdit = { onEdit(product.id) },
                                onVariants = { onVariants(product.id) },
                                onDelete = { onDelete(product) }
                            )
                        }
                    }
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
            item { StatCard("سفارش‌های امروز", stats.newOrdersToday.toString(), "عدد", trend = stats.ordersTrendPercent) }
            item { StatCard("فروش امروز", stringResource(Resources.String.PriceFormat, formatToman(stats.salesToday)), "تومان", trend = stats.salesTrendPercent) }
            item {
                StatCard(
                    "کالای موجود", stats.totalProducts.toString(), "عدد",
                    highlight = if (stats.lowStockCount > 0) "${stats.lowStockCount} کم‌موجود" else null
                )
            }
            item { StatCard("درآمد کل", stringResource(Resources.String.PriceFormat, formatToman(stats.totalRevenue)), "تومان") }
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
private fun StatCard(label: String, value: String, sub: String, trend: Int? = null, highlight: String? = null) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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
            // نشانِ روند (سبز/قرمز) یا برچسبِ کم‌موجودی
            if (trend != null) {
                val up = trend >= 0
                Text(
                    text = (if (up) "+" else "") + "$trend٪",
                    color = if (up) colors.ok else colors.sale,
                    fontWeight = FontWeight.Bold,
                    fontSize = FontSize.EXTRA_SMALL
                )
            } else if (highlight != null) {
                Text(
                    text = highlight,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.gold.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    color = colors.gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = FontSize.EXTRA_SMALL,
                    maxLines = 1
                )
            }
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
