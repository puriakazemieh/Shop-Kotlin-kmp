package com.kazemieh.comparison

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.domain.catalog.ProductDetail
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(
    navigateBack: () -> Unit,
    navigateToDetail: (String) -> Unit = {}
) {
    val viewModel = koinViewModel<ComparisonViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("مقایسه‌ی محصولات", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                actions = {
                    if (state.products.isNotEmpty()) {
                        Text(
                            "پاک‌کردن",
                            modifier = Modifier.clickable { viewModel.clear() }.padding(horizontal = 14.dp),
                            color = colors.sale, fontSize = FontSize.SMALL, fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.primary)
                state.products.isEmpty() -> Text(
                    "محصولی برای مقایسه انتخاب نشده. از صفحه‌ی محصول، «افزودن به مقایسه» را بزنید.",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    color = colors.onSurfaceVariant, fontSize = FontSize.REGULAR
                )
                else -> ComparisonTable(products = state.products, onRemove = viewModel::remove, onOpen = navigateToDetail)
            }
        }
    }
}

@Composable
private fun ComparisonTable(products: List<ProductDetail>, onRemove: (String) -> Unit, onOpen: (String) -> Unit) {
    val colors = AppTheme.colors
    // اتحادِ همه‌ی کلیدهای مشخصات (به‌ترتیبِ ظاهرشدن) به‌علاوه‌ی چند ردیفِ پایه.
    val attributeKeys = remember(products) {
        val keys = linkedSetOf<String>()
        products.forEach { p -> p.attributes.forEach { keys.add(it.name) } }
        keys.toList()
    }
    val colWidth = 160.dp

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState())) {
        // سرستون: کارتِ هر محصول
        Row {
            HeaderCell("مشخصه", colWidth, colors.surfaceVariant)
            products.forEach { p ->
                Column(
                    modifier = Modifier.width(colWidth).padding(4.dp).clip(RoundedCornerShape(Radius.sm)).background(colors.surface).padding(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            p.title, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.SMALL,
                            maxLines = 2, modifier = Modifier.weight(1f).clickable { onOpen(p.slug) }
                        )
                        Icon(
                            Icons.Default.Close, contentDescription = null, tint = colors.onSurfaceVariant,
                            modifier = Modifier.clickable { onRemove(p.slug) }
                        )
                    }
                }
            }
        }
        // ردیف‌های پایه
        RenderRow("قیمت", colWidth, products) { p ->
            val price = p.discountedPrice ?: p.basePrice
            if (price != null) "${price.toLong()} تومان" else "—"
        }
        RenderRow("برند", colWidth, products) { it.brand ?: "—" }
        RenderRow("دسته", colWidth, products) { it.categoryName ?: "—" }
        // ردیف‌های مشخصاتِ اختصاصی
        attributeKeys.forEach { key ->
            RenderRow(key, colWidth, products) { p -> p.attributes.firstOrNull { it.name == key }?.value ?: "—" }
        }
    }
}

@Composable
private fun RenderRow(label: String, width: androidx.compose.ui.unit.Dp, products: List<ProductDetail>, value: (ProductDetail) -> String) {
    val colors = AppTheme.colors
    Row(modifier = Modifier.height(1.dp).fillMaxWidth().background(colors.line)) {}
    Row {
        HeaderCell(label, width, colors.surfaceVariant)
        products.forEach { p ->
            Box(modifier = Modifier.width(width).padding(horizontal = 4.dp, vertical = 10.dp)) {
                Text(value(p), color = colors.onSurface, fontSize = FontSize.SMALL)
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp, bg: androidx.compose.ui.graphics.Color) {
    val colors = AppTheme.colors
    Box(modifier = Modifier.width(width).background(bg).padding(horizontal = 8.dp, vertical = 10.dp)) {
        Text(text, fontWeight = FontWeight.SemiBold, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
    }
}
