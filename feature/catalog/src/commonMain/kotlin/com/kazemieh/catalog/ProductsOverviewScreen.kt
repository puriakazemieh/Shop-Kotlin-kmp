package com.kazemieh.catalog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.Alpha
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.adaptiveGridColumns
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.domain.blog.Blog
import com.kazemieh.domain.catalog.Category
import com.kazemieh.domain.academy.CourseSummary
import com.kazemieh.domain.clinic.TherapistSummary
import com.kazemieh.domain.psychtest.PsychTestSummary
import com.seiko.imageloader.rememberImagePainter
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsOverviewScreen(
    navigateToDetails: (String) -> Unit,
    navigateToCategorySearch: (Long, String) -> Unit,
    navigateToBlogDetail: (String) -> Unit,
    navigateToAuth: () -> Unit,
    navigateToCourseDetail: (String) -> Unit = {},
    navigateToCourseCatalog: () -> Unit = {},
    navigateToTherapistDetail: (String) -> Unit = {},
    navigateToTherapistCatalog: () -> Unit = {},
    navigateToPsychTestCatalog: () -> Unit = {}
) {
    val viewModel = koinViewModel<ProductsOverviewViewModel>()
    val state by viewModel.state.collectAsState()
    // تعدادِ ستونِ گریدِ محصولات و دسته‌ها بر اساسِ عرضِ صفحه (موبایل ۲ / تبلت ۳ / دسکتاپ ۴)
    val productColumns = adaptiveGridColumns(compact = 2, medium = 3, expanded = 4)
    val categoryColumns = adaptiveGridColumns(compact = 3, medium = 5, expanded = 6)
    val homeListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // ترتیب آیتم‌های خانه مطابق اسپک: 0=استوری، 1=هیرو، (دسته‌ها)، (پیشنهاد شگفت‌انگیز)، سرتیتر جدیدترین
    val hasCampaign = state.campaign?.products?.isNotEmpty() == true
    val hasCategories = state.categories.isNotEmpty()
    val newestHeaderIndex = 2 + (if (hasCategories) 1 else 0) + (if (hasCampaign) 1 else 0)
    val dealsIndex = if (hasCampaign) 2 + (if (hasCategories) 1 else 0) else newestHeaderIndex

    val pullToRefreshState = rememberPullToRefreshState()

    var showStoryDetail by remember { mutableStateOf(false) }
    var initialStoryIndex by remember { mutableStateOf(0) }

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
                    subtitle = state.error?:""
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
                                    scope.launch { homeListState.animateScrollToItem(newestHeaderIndex) }
                                },
                                onDealsClick = {
                                    scope.launch { homeListState.animateScrollToItem(dealsIndex) }
                                }
                            )
                        }

                        // دسته‌ها — گرید کاشی‌ها، بلافاصله بعد از هیرو (مطابق اسپک)
                        if (hasCategories) {
                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    state.categories.chunked(categoryColumns).forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            rowItems.forEach { category ->
                                                SquareCategoryCard(
                                                    modifier = Modifier.weight(1f),
                                                    category = category,
                                                    onClick = {
                                                        viewModel.handleIntent(
                                                            ProductsOverviewIntent.OnCategoryClick(category.id, category.name)
                                                        )
                                                    }
                                                )
                                            }
                                            repeat(categoryColumns - rowItems.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // پیشنهاد شگفت‌انگیز (کمپین) — بعد از دسته‌ها
                        state.campaign?.let { campaign ->
                            if (campaign.products.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(34.dp))
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

                        // ---- عمودی‌های دیگرِ برند (دوره‌ها/مشاوره/تست‌های روان‌شناسی) — مستقیماً روی صفحه‌ی اصلی،
                        // درست مثلِ محصولات، نه فقط داخلِ پروفایل ----
                        if (state.courses.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(30.dp))
                                CourseHomeSection(
                                    courses = state.courses,
                                    onSeeAll = navigateToCourseCatalog,
                                    onCourseClick = { navigateToCourseDetail(it.slug) }
                                )
                            }
                        }
                        if (state.therapists.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(30.dp))
                                TherapistHomeSection(
                                    therapists = state.therapists,
                                    onSeeAll = navigateToTherapistCatalog,
                                    onTherapistClick = { navigateToTherapistDetail(it.slug) }
                                )
                            }
                        }
                        if (state.psychTests.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(30.dp))
                                PsychTestHomeSection(
                                    tests = state.psychTests,
                                    onSeeAll = navigateToPsychTestCatalog
                                )
                            }
                        }

                        // جدیدترین محصولات — گرید دو ستونه
                        item {
                            Spacer(modifier = Modifier.height(36.dp))
                            HomeSectionHeader(
                                title = stringResource(Resources.String.NewestProducts),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        items(
                            items = products.chunked(productColumns),
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
                                repeat(productColumns - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        if (state.banners.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                                PromoBanners(
                                    banners = state.banners,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    onBannerClick = { banner ->
                                        banner.categoryId?.let { id ->
                                            viewModel.handleIntent(
                                                ProductsOverviewIntent.OnCategoryClick(id, banner.title)
                                            )
                                        }
                                    }
                                )
                            }
                        }

                        // مجله/بلاگ — تیزرِ آخرین مقالات (مطابق اسپک)
                        if (state.blogPosts.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(30.dp))
                                HomeSectionHeader(
                                    title = stringResource(Resources.String.BlogTeaserTitle),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(state.blogPosts, key = { it.id }) { post ->
                                        BlogTeaserCard(
                                            post = post,
                                            onClick = { navigateToBlogDetail(post.slug) }
                                        )
                                    }
                                }
                            }
                        }

                        // بازدیدهای اخیر شما — تاریخچه‌ی محلیِ محصولاتِ بازدیدشده (مطابق اسپک)
                        if (state.recentlyViewed.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(30.dp))
                                HomeSectionHeader(
                                    title = stringResource(Resources.String.RecentlyViewedTitle),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(state.recentlyViewed, key = { it.id }) { item ->
                                        MainProductCard(
                                            modifier = Modifier.width(170.dp),
                                            product = item,
                                            onClick = {
                                                viewModel.handleIntent(ProductsOverviewIntent.OnProductClick(it))
                                            },
                                            onFavoriteClick = {
                                                viewModel.handleIntent(ProductsOverviewIntent.OnFavoriteClick(item))
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
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
            onProductClick = { id ->
                showStoryDetail = false
                navigateToDetails(id.toString())
            },
            onCategoryClick = { id ->
                showStoryDetail = false
                navigateToCategorySearch(id, "")
            },
            onBlogClick = { slug ->
                showStoryDetail = false
                navigateToBlogDetail(slug)
            }
        )
    }
}

@Composable
fun SquareCategoryCard(
    modifier: Modifier = Modifier,
    category: Category,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
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

/** کارتِ تیزرِ مقاله در صفحه‌ی اصلی — تصویر + عنوان + دسته/زمانِ مطالعه. */
@Composable
private fun BlogTeaserCard(
    post: Blog,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .width(230.dp)
            .clip(RoundedCornerShape(Radius.lg))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.lg))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(Radius.sm)),
            painter = rememberImagePainter(post.thumbnailUrl ?: ""),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = post.title,
            fontSize = FontSize.REGULAR,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.height(44.dp)
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            post.categoryName?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    fontSize = FontSize.EXTRA_SMALL,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = "${post.readingTimeMinutes} دقیقه مطالعه",
                fontSize = FontSize.EXTRA_SMALL,
                color = colors.onSurfaceVariant
            )
        }
    }
}
