package com.kazemieh.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
 * صفحه‌ی جستجوی مستقل (تبِ جستجو) — مطابق اسپک کارمیلا:
 * فیلدِ جستجو ← پیش از جستجو: «جستجوهای اخیر» + «پرطرفدار» ← پس از جستجو: گرید یا حالتِ خالی.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    navigateToDetails: (String) -> Unit,
    navigateToAuth: () -> Unit
) {
    val viewModel = koinViewModel<SearchViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SearchEffect.NavigateToAuth -> navigateToAuth()
                is SearchEffect.ShowError -> {}
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
    ) {
        // ---- فیلدِ جستجو ----
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.handleIntent(SearchIntent.UpdateQuery(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Resources.String.SearchHere)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Resources.Icon.Search),
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(Radius.button),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    viewModel.handleIntent(SearchIntent.Submit(state.query))
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.line
                )
            )
        }

        when {
            // ---- پیش از جستجو: پیشنهادها ----
            !state.hasSearched -> {
                if (state.recentSearches.isNotEmpty()) {
                    item {
                        SuggestionSection(
                            title = "جستجوهای اخیر",
                            actionLabel = "پاک کردن",
                            onAction = { viewModel.handleIntent(SearchIntent.ClearRecent) },
                            items = state.recentSearches,
                            onItemClick = { viewModel.handleIntent(SearchIntent.Submit(it)) }
                        )
                    }
                }
                item {
                    SuggestionSection(
                        title = "جستجوهای پرطرفدار",
                        actionLabel = null,
                        onAction = {},
                        items = state.popularSearches,
                        onItemClick = { viewModel.handleIntent(SearchIntent.Submit(it)) }
                    )
                }
            }

            state.isLoading -> item { LoadingCard(modifier = Modifier.fillMaxWidth().height(320.dp)) }

            state.results.isEmpty() -> item {
                InfoCard(
                    image = Resources.Image.Cat,
                    title = stringResource(Resources.String.NothingHere),
                    subtitle = "برای «${state.query}» نتیجه‌ای پیدا نشد"
                )
            }

            else -> items(state.results.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { product ->
                        MainProductCard(
                            modifier = Modifier.weight(1f),
                            product = product,
                            onClick = { navigateToDetails(it) },
                            onFavoriteClick = { viewModel.handleIntent(SearchIntent.ToggleFavorite(product)) }
                        )
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestionSection(
    title: String,
    actionLabel: String?,
    onAction: () -> Unit,
    items: List<String>,
    onItemClick: (String) -> Unit
) {
    val colors = AppTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = FontSize.REGULAR, fontWeight = FontWeight.Bold, color = colors.onSurface)
            Spacer(Modifier.weight(1f))
            if (actionLabel != null) {
                Text(
                    actionLabel,
                    fontSize = FontSize.SMALL,
                    color = colors.primary,
                    modifier = Modifier.clickable { onAction() }
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                CarmillaFilterChip(text = item, selected = false, onClick = { onItemClick(item) })
            }
        }
    }
}
