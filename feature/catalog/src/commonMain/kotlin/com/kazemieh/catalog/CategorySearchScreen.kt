package com.kazemieh.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.CarmillaFilterChip
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * صفحه‌ی لیست/دسته‌بندی و جستجو — بازطراحی‌شده مطابق اسپک کارمیلا.
 * ساختار: هدر (دکمه‌ی بازگشتِ کادری + عنوان + «X کالا») ← فیلدِ جستجوی پیل‌شکل ←
 * نوار چیپ‌های مرتب‌سازی/فیلتر با تاگلِ «فقط تخفیف‌دار» ← گریدِ دو‌ستونه‌ی محصولات.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySearchScreen(
    categoryId: Long,
    categoryName: String,
    navigateBack: () -> Unit,
    navigateToDetails: (String) -> Unit,
    navigateToAuth: () -> Unit
) {
    val viewModel = koinViewModel<CategorySearchViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    LaunchedEffect(categoryId, categoryName) {
        viewModel.handleIntent(CategorySearchIntent.Init(categoryId, categoryName))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CategorySearchEffect.ShowError -> {}
                is CategorySearchEffect.NavigateToAuth -> navigateToAuth()
            }
        }
    }

    val displayProducts = state.products

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---- هدر: دکمه‌ی بازگشت + عنوان + تعداد ----
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(Radius.button))
                        .background(colors.surface)
                        .border(1.dp, colors.line, RoundedCornerShape(Radius.button))
                        .clickable { navigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer { rotationY = if (isRtl) 180f else 0f },
                        painter = painterResource(Resources.Icon.BackArrow),
                        contentDescription = stringResource(Resources.String.BackDesc),
                        tint = colors.onSurface
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.categoryName.ifBlank { categoryName },
                        fontSize = FontSize.EXTRA_MEDIUM,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "${state.totalCount} کالا",
                        fontSize = FontSize.SMALL,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }

        // ---- فیلد جستجوی پیل‌شکل ----
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.handleIntent(CategorySearchIntent.UpdateSearchQuery(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(Resources.String.SearchInCategoryFormat, state.categoryName),
                        fontSize = FontSize.REGULAR,
                        color = colors.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(Resources.Icon.Search),
                        contentDescription = stringResource(Resources.String.SearchDesc),
                        tint = colors.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        Icon(
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { viewModel.handleIntent(CategorySearchIntent.UpdateSearchQuery("")) },
                            painter = painterResource(Resources.Icon.Close),
                            contentDescription = stringResource(Resources.String.CloseDesc),
                            tint = colors.onSurfaceVariant
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(Radius.button),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surfaceVariant,
                    unfocusedContainerColor = colors.surfaceVariant,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.line,
                    cursorColor = colors.primary,
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface
                )
            )
        }

        // ---- نوار مرتب‌سازی/فیلتر ----
        if (state.availableOptions.isNotEmpty() || state.products.isNotEmpty()) {
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(Resources.String.SortBy),
                            fontSize = FontSize.SMALL,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurfaceVariant
                        )
                    }
                    item {
                        CarmillaFilterChip(
                            text = stringResource(Resources.String.SortNewest),
                            selected = state.sort == "newest",
                            onClick = { viewModel.handleIntent(CategorySearchIntent.UpdateSort("newest")) }
                        )
                    }
                    item {
                        CarmillaFilterChip(
                            text = stringResource(Resources.String.SortPriceAsc),
                            selected = state.sort == "price_asc",
                            onClick = { viewModel.handleIntent(CategorySearchIntent.UpdateSort("price_asc")) }
                        )
                    }
                    item {
                        CarmillaFilterChip(
                            text = stringResource(Resources.String.SortPriceDesc),
                            selected = state.sort == "price_desc",
                            onClick = { viewModel.handleIntent(CategorySearchIntent.UpdateSort("price_desc")) }
                        )
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .height(22.dp)
                                .width(1.dp)
                                .background(colors.line)
                        )
                    }
                    item {
                        CarmillaFilterChip(
                            text = "🏷️ فقط تخفیف‌دار",
                            selected = state.discountedOnly,
                            onClick = { viewModel.handleIntent(CategorySearchIntent.SetDiscountedOnly(!state.discountedOnly)) }
                        )
                    }
                }
            }

            // ---- فیلترهای ویژگی (رنگ/سایز) ----
            state.availableOptions.forEach { (key, values) ->
                item {
                    Column {
                        Text(
                            text = key,
                            fontSize = FontSize.SMALL,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(values.toList()) { value ->
                                CarmillaFilterChip(
                                    text = value,
                                    selected = state.selectedOptions[key] == value,
                                    onClick = { viewModel.handleIntent(CategorySearchIntent.ToggleOption(key, value)) }
                                )
                            }
                        }
                    }
                }
            }

            item { HorizontalDivider(color = colors.line) }
        }

        // ---- محتوا: لودینگ / خطا / خالی / گرید ----
        when {
            state.isLoading -> item { LoadingCard(modifier = Modifier.fillMaxWidth().height(320.dp)) }
            state.error != null -> item {
                InfoCard(
                    image = Resources.Image.Cat,
                    title = stringResource(Resources.String.Oops),
                    subtitle = state.error
                )
            }
            displayProducts.isEmpty() -> item {
                InfoCard(
                    image = Resources.Image.Cat,
                    title = stringResource(Resources.String.NothingHere),
                    subtitle = stringResource(Resources.String.NoProductInCategory)
                )
            }
            else -> items(displayProducts.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { product ->
                        MainProductCard(
                            modifier = Modifier.weight(1f),
                            product = product,
                            onClick = { navigateToDetails(it) },
                            onFavoriteClick = {
                                viewModel.handleIntent(CategorySearchIntent.ToggleFavorite(product))
                            }
                        )
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
