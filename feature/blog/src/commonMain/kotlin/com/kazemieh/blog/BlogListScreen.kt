package com.kazemieh.blog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.component.CarmillaFilterChip
import com.kazemieh.designsystem.component.CustomTextField
import com.kazemieh.designsystem.component.LoadingCard
import com.seiko.imageloader.rememberImagePainter
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogListScreen(
    navigateToDetail: (String) -> Unit,
    navigateBack: () -> Unit,
    viewModel: BlogListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "مجلهٔ کارمیلا",
                            fontFamily = AppFont(),
                            fontSize = FontSize.LARGE,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "جدیدترین مطالب مد، استایل و راهنمای خرید",
                            fontFamily = AppFont(),
                            fontSize = FontSize.EXTRA_SMALL,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search Bar
            CustomTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.handleIntent(BlogListIntent.Search(it))
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = "جستجو در مقالات…"
            )

            // Category Selector
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CarmillaFilterChip(
                        text = "همه",
                        selected = state.selectedCategoryId == null,
                        onClick = { viewModel.handleIntent(BlogListIntent.SelectCategory(null)) }
                    )
                }
                items(state.categories) { category ->
                    CarmillaFilterChip(
                        text = category.name,
                        selected = state.selectedCategoryId == category.id,
                        onClick = { viewModel.handleIntent(BlogListIntent.SelectCategory(category.id)) }
                    )
                }
            }

            if (state.isLoading) {
                LoadingCard(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.featuredBlogs.firstOrNull()?.let { featured ->
                        item {
                            FeaturedBlogHero(blog = featured, onClick = { navigateToDetail(featured.slug) })
                        }
                    }

                    items(state.blogs) { blog ->
                        BlogItem(blog = blog, onClick = { navigateToDetail(blog.slug) })
                    }
                }
            }
        }
    }
}

/** کارتِ هیروی مطلبِ ویژه — مطابق اسپک: کارتِ گرادیانیِ بزرگ با بَجِ «مطلب ویژه». */
@Composable
fun FeaturedBlogHero(blog: com.kazemieh.domain.blog.Blog, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.primary, colors.accent2))
            )
            .clickable { onClick() }
    ) {
        blog.thumbnailUrl?.let {
            Image(
                painter = rememberImagePainter(it),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(Color(0xCC0F1220), Color(0x330F1220))
                        )
                    )
            )
        }
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "مطلب ویژه",
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                fontFamily = AppFont(),
                fontSize = FontSize.SMALL,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = blog.title,
                fontFamily = AppFont(),
                fontSize = FontSize.MEDIUM,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 3
            )
            blog.summary?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    fontFamily = AppFont(),
                    fontSize = FontSize.REGULAR,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun BlogItem(blog: com.kazemieh.domain.blog.Blog, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, colors.line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier.width(120.dp).fillMaxHeight().background(colors.surfaceVariant)
            ) {
                blog.thumbnailUrl?.let {
                    Image(
                        painter = rememberImagePainter(it),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = blog.categoryName ?: blog.category?.name ?: "",
                        fontFamily = AppFont(),
                        fontSize = FontSize.SMALL,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primary
                    )
                    Text(
                        text = "${blog.readingTimeMinutes} دقیقه مطالعه",
                        fontFamily = AppFont(),
                        fontSize = FontSize.EXTRA_SMALL,
                        color = colors.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = blog.title,
                    fontFamily = AppFont(),
                    fontSize = FontSize.EXTRA_REGULAR,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                blog.summary?.let {
                    Text(
                        text = it,
                        fontFamily = AppFont(),
                        fontSize = FontSize.SMALL,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                        maxLines = 3
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "نویسنده: ${blog.authorName ?: "کارمیلا"}",
                        fontFamily = AppFont(),
                        fontSize = FontSize.EXTRA_SMALL,
                        color = colors.onSurfaceVariant
                    )
                    Text(
                        text = "${blog.viewCount} بازدید",
                        fontFamily = AppFont(),
                        fontSize = FontSize.EXTRA_SMALL,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}
