package com.kazemieh.catalog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.Alpha
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.domain.catalog.Category
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsOverviewScreen(
    navigateToDetails: (String) -> Unit,
    navigateToCategorySearch: (Long, String) -> Unit,
    navigateToAuth: () -> Unit
) {
    val viewModel = koinViewModel<ProductsOverviewViewModel>()
    val state by viewModel.state.collectAsState()
    val featuredListState = rememberLazyListState()
    val homeListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // ایندکس سرتیتر «جدیدترین محصولات» برای اسکرول دکمه‌های هیرو
    val hasCampaign = state.campaign?.products?.isNotEmpty() == true
    val gridHeaderIndex = 3 +
        (if (hasCampaign) 1 else 0) +
        (if (state.categories.isNotEmpty()) 1 else 0)

    val pullToRefreshState = rememberPullToRefreshState()

    var showStoryDetail by remember { mutableStateOf(false) }
    var initialStoryIndex by remember { mutableStateOf(0) }

    val centeredIndex: Int? by remember {
        derivedStateOf {
            val layoutInfo = featuredListState.layoutInfo
            val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2
            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                kotlin.math.abs(itemCenter - viewportCenter)
            }?.index
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProductsOverviewEffect.NavigateToDetails -> {
                    navigateToDetails(effect.slug)
                }
                is ProductsOverviewEffect.NavigateToStory -> {
                    initialStoryIndex = effect.initialIndex
                    showStoryDetail = true
                }
                is ProductsOverviewEffect.NavigateToCategory -> {
                    navigateToCategorySearch(effect.id, effect.name)
                }
                is ProductsOverviewEffect.NavigateToAuth -> {
                    navigateToAuth()
                }
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.handleIntent(ProductsOverviewIntent.Refresh) },
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.isLoading) {
            LoadingCard(modifier = Modifier.fillMaxSize())
        } else if (state.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                InfoCard(
                    image = Resources.Image.Cat,
                    title = stringResource(Resources.String.Oops),
                    subtitle = state.error.toString()
                )
            }
        } else {
            AnimatedContent(
                targetState = state.products
            ) { products ->
                if (products.isNotEmpty() || state.stories.isNotEmpty()) {
                    LazyColumn(
                        state = homeListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            StoryCircleRow(
                                stories = state.stories,
                                isLoading = state.isStoriesLoading,
                                onStoryClick = { index ->
                                    viewModel.handleIntent(ProductsOverviewIntent.OnStoryClick(index))
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            HomeHero(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                onShopClick = {
                                    scope.launch { homeListState.animateScrollToItem(gridHeaderIndex) }
                                },
                                onDealsClick = {
                                    scope.launch { homeListState.animateScrollToItem(2) }
                                }
                            )
                        }

                        state.campaign?.let { campaign ->
                            if (campaign.products.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    AmazingOffersSection(
                                        campaign = campaign,
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        onProductClick = {
                                            viewModel.handleIntent(
                                                ProductsOverviewIntent.OnProductClick(it)
                                            )
                                        },
                                        onFavoriteClick = { product ->
                                            viewModel.handleIntent(
                                                ProductsOverviewIntent.OnFavoriteClick(product)
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                text = stringResource(Resources.String.FeaturedProducts),
                                fontSize = FontSize.LARGE,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(
                                state = featuredListState,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                itemsIndexed(
                                    items = products.take(6),
                                    key = { _, item -> "featured_${item.id}" }
                                ) { index, product ->
                                    val isLarge = index == centeredIndex
                                    val animatedScale by animateFloatAsState(
                                        targetValue = if (isLarge) 1f else 0.85f,
                                        animationSpec = tween(300)
                                    )

                                    MainProductCard(
                                        modifier = Modifier
                                            .scale(animatedScale)
                                            .height(350.dp)
                                            .fillParentMaxWidth(0.7f),
                                        product = product,
                                        isLarge = isLarge,
                                        onClick = {
                                            viewModel.handleIntent(
                                                ProductsOverviewIntent.OnProductClick(it)
                                            )
                                        },
                                        onFavoriteClick = {
                                            viewModel.handleIntent(
                                                ProductsOverviewIntent.OnFavoriteClick(product)
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        if (state.categories.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    text = stringResource(Resources.String.Categories),
                                    fontSize = FontSize.LARGE,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    items(state.categories) { category ->
                                        SquareCategoryCard(
                                            category = category,
                                            onClick = {
                                                viewModel.handleIntent(
                                                    ProductsOverviewIntent.OnCategoryClick(category.id, category.name)
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            HomeSectionHeader(
                                title = stringResource(Resources.String.NewestProducts),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        items(
                            items = products.chunked(2),
                            key = { row -> "grid_${row.first().id}" }
                        ) { rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { product ->
                                    MainProductCard(
                                        modifier = Modifier.weight(1f),
                                        product = product,
                                        onClick = {
                                            viewModel.handleIntent(
                                                ProductsOverviewIntent.OnProductClick(it)
                                            )
                                        },
                                        onFavoriteClick = {
                                            viewModel.handleIntent(
                                                ProductsOverviewIntent.OnFavoriteClick(product)
                                            )
                                        }
                                    )
                                }
                                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }

                        item {
                            TrustBadges(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        InfoCard(
                            image = Resources.Image.Cat,
                            title = stringResource(Resources.String.NothingHere),
                            subtitle = stringResource(Resources.String.EmptyProductList)
                        )
                    }
                }
            }
        }
    }

    if (showStoryDetail) {
        StoryDetailScreen(
            stories = state.stories,
            initialIndex = initialStoryIndex,
            onClose = { showStoryDetail = false },
            onStorySeen = { id -> viewModel.handleIntent(ProductsOverviewIntent.OnStorySeen(id)) },
            onProductClick = { slug ->
                showStoryDetail = false
                navigateToDetails(slug.toString())
            }
        )
    }
}

@Composable
fun SquareCategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(96.dp)
            .clip(RoundedCornerShape(Radius.lg))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Radius.lg))
            .clickable { onClick() }
            .padding(vertical = 18.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(Radius.sm))
                .background(AppTheme.colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = category.name,
            fontSize = FontSize.SMALL,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold
        )
    }
}
