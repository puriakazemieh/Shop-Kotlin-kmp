package com.kazemieh.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.component.PrimaryButton
import com.kazemieh.designsystem.component.QuantityCounter
import com.kazemieh.designsystem.component.QuantityCounterSize
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.details.component.AddQuestionDialog
import com.kazemieh.details.component.AddReviewDialog
import com.kazemieh.details.component.EditQuestionDialog
import com.kazemieh.details.component.EditReviewDialog
import com.kazemieh.details.component.QuestionItem
import com.kazemieh.details.component.ReviewItem
import com.kazemieh.details.component.VariantChip
import com.kazemieh.domain.catalog.ProductImage
import com.kazemieh.domain.catalog.ProductVideo
import com.kazemieh.domain.catalog.Question
import com.kazemieh.domain.catalog.Review
import com.seiko.imageloader.rememberImagePainter
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

sealed interface MediaItem {
    data class Image(val image: ProductImage) : MediaItem
    data class Video(val video: ProductVideo) : MediaItem
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    slug: String,
    navigateBack: () -> Unit,
    navigateToCart: () -> Unit,
    navigateToAuth: () -> Unit
) {
    val messageBarState = rememberMessageBarState()
    val viewModel = koinViewModel<DetailsViewModel>()
    val state by viewModel.state.collectAsState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var showQuestionDialog by remember { mutableStateOf(false) }
    var activeParentId by remember { mutableStateOf<Long?>(null) }
    var editReview by remember { mutableStateOf<Review?>(null) }
    var editQuestion by remember { mutableStateOf<Question?>(null) }

    LaunchedEffect(state.isAddedToCart, state.quantity, state.isCounterMode) {
        if (state.isAddedToCart && state.isCounterMode) {
            kotlinx.coroutines.delay(5000)
            viewModel.handleIntent(DetailsIntent.SetCounterMode(false))
        }
    }

    LaunchedEffect(slug) {
        viewModel.handleIntent(DetailsIntent.LoadProduct(slug))
    }

    val productAddedToCartMessage = stringResource(Resources.String.ProductAddedToCart)

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DetailsEffect.ShowError -> messageBarState.addError(effect.message)
                is DetailsEffect.AddedToCart -> messageBarState.addSuccess(productAddedToCartMessage)
                is DetailsEffect.NavigateToAuth -> navigateToAuth()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Resources.String.Details),
                        fontSize = FontSize.LARGE,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            modifier = Modifier.graphicsLayer {
                                rotationY = if (isRtl) 180f else 0f
                            },
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = stringResource(Resources.String.BackDesc),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    state.product?.let { product ->
                        IconButton(onClick = {
                            viewModel.handleIntent(
                                DetailsIntent.ToggleFavorite(
                                    product.id,
                                    product.isFavorite
                                )
                            )
                        }) {
                            Icon(
                                imageVector = if (product.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (product.isFavorite) AppTheme.colors.sale else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        ContentWithMessageBar(
            modifier = Modifier.padding(padding),
            messageBarState = messageBarState,
            contentBackgroundColor = MaterialTheme.colorScheme.surface
        ) {
            if (state.isLoading) {
                LoadingCard(modifier = Modifier.fillMaxSize())
            } else if (state.error != null) {
                InfoCard(
                    image = Resources.Image.Cat,
                    title = stringResource(Resources.String.Oops),
                    subtitle = state.error!!
                )
            } else {
                state.product?.let { product ->
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp)
                                .padding(top = 12.dp)
                        ) {
                            val mediaItems = remember(product) {
                                val images = product.images.map { MediaItem.Image(it) }
                                val videos = product.videos.map { MediaItem.Video(it) }
                                (images + videos).sortedBy {
                                    when (it) {
                                        is MediaItem.Image -> it.image.sortOrder
                                        is MediaItem.Video -> it.video.sortOrder
                                    }
                                }
                            }
                            val pagerState =
                                rememberPagerState(pageCount = { mediaItems.size.coerceAtLeast(1) })
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(size = Radius.md))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(size = Radius.md)
                                    )
                            ) {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    when (val item = mediaItems.getOrNull(page)) {
                                        is MediaItem.Image -> {
                                            val imageUrl = item.image.url
                                            val painter = rememberImagePainter(imageUrl)
                                            Image(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clickable {
                                                        if (imageUrl.isNotEmpty()) fullscreenImageUrl =
                                                            imageUrl
                                                    },
                                                painter = painter,
                                                contentDescription = stringResource(Resources.String.ProductImageDesc),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        is MediaItem.Video -> {
                                            VideoPlayer(
                                                url = item.video.url,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        null -> {
                                            // Fallback if no images/videos
                                            Box(
                                                modifier = Modifier.fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                            )
                                        }
                                    }
                                }

                                if (mediaItems.size > 1) {
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        repeat(mediaItems.size) { iteration ->
                                            val color = if (pagerState.currentPage == iteration)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(color)
                                                    .size(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = product.categoryName ?: "",
                                    fontSize = FontSize.REGULAR,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                val variant = state.selectedVariant
                                val basePrice = variant?.price ?: product.basePrice ?: 0.0
                                val discountedPrice =
                                    variant?.discountedPrice ?: product.discountedPrice
                                val compareAtPrice = variant?.compareAtPrice

                                Column(horizontalAlignment = Alignment.End) {
                                    if (discountedPrice != null) {
                                        Text(
                                            text = stringResource(
                                                Resources.String.PriceFormat,
                                                discountedPrice
                                            ),
                                            fontSize = FontSize.MEDIUM,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = stringResource(
                                                Resources.String.PriceFormat,
                                                basePrice
                                            ),
                                            fontSize = FontSize.SMALL,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                    } else if (compareAtPrice != null && compareAtPrice > basePrice) {
                                        Text(
                                            text = stringResource(
                                                Resources.String.PriceFormat,
                                                basePrice
                                            ),
                                            fontSize = FontSize.MEDIUM,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = stringResource(
                                                Resources.String.PriceFormat,
                                                compareAtPrice
                                            ),
                                            fontSize = FontSize.SMALL,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                    } else {
                                        Text(
                                            text = stringResource(
                                                Resources.String.PriceFormat,
                                                basePrice
                                            ),
                                            fontSize = FontSize.MEDIUM,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = product.title,
                                fontSize = FontSize.EXTRA_MEDIUM,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = product.description ?: "",
                                fontSize = FontSize.REGULAR,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Tabs for Details, Reviews, and Questions
                            PrimaryTabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = { Text(stringResource(Resources.String.Details)) }
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = { Text(stringResource(Resources.String.ReviewsTab, state.reviews.size)) }
                                )
                                Tab(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    text = { Text(stringResource(Resources.String.QuestionsTab, state.questions.size)) }
                                )
                            }

                            when (selectedTab) {
                                0 -> {
                                    if (product.variants.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(24.dp))
                                        val groupedOptions = remember(product.variants) {
                                            val map = mutableMapOf<String, MutableSet<String>>()
                                            product.variants.forEach { variant ->
                                                variant.options.forEach { (key, value) ->
                                                    map.getOrPut(key) { mutableSetOf() }.add(value)
                                                }
                                            }
                                            map
                                        }

                                        groupedOptions.forEach { (optionName, values) ->
                                            Text(
                                                text = optionName,
                                                fontSize = FontSize.MEDIUM,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            FlowRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                values.forEach { value ->
                                                    val isSelected =
                                                        state.selectedOptions[optionName] == value
                                                    // An option is available if it exists in ANY variant of the product
                                                    val isAvailable =
                                                        product.variants.any { variant ->
                                                            variant.options[optionName] == value
                                                        }

                                                    VariantChip(
                                                        label = value,
                                                        isSelected = isSelected,
                                                        enabled = isAvailable,
                                                        onClick = {
                                                            viewModel.handleIntent(
                                                                DetailsIntent.SelectOption(
                                                                    optionName,
                                                                    value
                                                                )
                                                            )
                                                        }
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }

                                1 -> {
                                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                                        PrimaryButton(
                                            text = stringResource(Resources.String.AddReview),
                                            onClick = {
                                                activeParentId = null
                                                showReviewDialog = true
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (state.reviews.isEmpty()) {
                                            Text(
                                                stringResource(Resources.String.NoReviewsYet),
                                                modifier = Modifier.fillMaxWidth()
                                                    .padding(top = 16.dp),
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        state.reviews.forEach { review ->
                                            ReviewItem(
                                                review = review,
                                                onReplyClick = {
                                                    activeParentId = it
                                                    showReviewDialog = true
                                                },
                                                onEditClick = { editReview = it },
                                                onDeleteClick = {
                                                    state.product?.id?.let { pid ->
                                                        viewModel.handleIntent(
                                                            DetailsIntent.DeleteReview(
                                                                it,
                                                                pid
                                                            )
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                2 -> {
                                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                                        PrimaryButton(
                                            text = stringResource(Resources.String.AddQuestion),
                                            onClick = {
                                                activeParentId = null
                                                showQuestionDialog = true
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (state.questions.isEmpty()) {
                                            Text(
                                                stringResource(Resources.String.NoQuestionsYet),
                                                modifier = Modifier.fillMaxWidth()
                                                    .padding(top = 16.dp),
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        state.questions.forEach { question ->
                                            QuestionItem(
                                                question = question,
                                                onReplyClick = {
                                                    activeParentId = it
                                                    showQuestionDialog = true
                                                },
                                                onEditClick = { editQuestion = it },
                                                onDeleteClick = {
                                                    state.product?.id?.let { pid ->
                                                        viewModel.handleIntent(
                                                            DetailsIntent.DeleteQuestion(
                                                                it,
                                                                pid
                                                            )
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .padding(all = 24.dp)
                        ) {
                            if (state.isAddedToCart) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (state.isCounterMode) {
                                        QuantityCounter(
                                            size = QuantityCounterSize.Medium,
                                            value = state.quantity,
                                            onMinusClick = {
                                                viewModel.handleIntent(
                                                    DetailsIntent.UpdateQuantity(
                                                        it
                                                    )
                                                )
                                            },
                                            onPlusClick = {
                                                viewModel.handleIntent(
                                                    DetailsIntent.UpdateQuantity(
                                                        it
                                                    )
                                                )
                                            }
                                        )
                                    } else {
                                        PrimaryButton(
                                            modifier = Modifier.weight(1f),
                                            text = stringResource(
                                                Resources.String.CheckoutWithQty,
                                                state.quantity
                                            ),
                                            onClick = navigateToCart
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(size = Radius.md))
                                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                                .clickable {
                                                    viewModel.handleIntent(
                                                        DetailsIntent.SetCounterMode(
                                                            true
                                                        )
                                                    )
                                                }
                                                .padding(all = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = state.quantity.toString(),
                                                fontSize = FontSize.MEDIUM,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            } else {
                                PrimaryButton(
                                    text = if (state.selectedVariant != null)
                                        stringResource(Resources.String.AddToCart)
                                    else
                                        stringResource(Resources.String.OutOfStock),
                                    enabled = !state.isLoading && state.selectedVariant != null,
                                    onClick = { viewModel.handleIntent(DetailsIntent.AddToCart) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReviewDialog) {
        AddReviewDialog(
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment ->
                state.product?.id?.let {
                    viewModel.handleIntent(
                        DetailsIntent.AddReview(
                            it,
                            rating,
                            comment,
                            activeParentId
                        )
                    )
                }
                showReviewDialog = false
            }
        )
    }

    if (showQuestionDialog) {
        AddQuestionDialog(
            onDismiss = { showQuestionDialog = false },
            onSubmit = { content ->
                state.product?.id?.let {
                    viewModel.handleIntent(DetailsIntent.AddQuestion(it, content, activeParentId))
                }
                showQuestionDialog = false
            }
        )
    }

    editReview?.let { review ->
        EditReviewDialog(
            review = review,
            onDismiss = { editReview = null },
            onSubmit = { rating, comment ->
                state.product?.id?.let {
                    viewModel.handleIntent(
                        DetailsIntent.UpdateReview(
                            review.id,
                            it,
                            rating,
                            comment
                        )
                    )
                }
                editReview = null
            }
        )
    }

    editQuestion?.let { question ->
        EditQuestionDialog(
            question = question,
            onDismiss = { editQuestion = null },
            onSubmit = { content ->
                state.product?.id?.let {
                    viewModel.handleIntent(DetailsIntent.UpdateQuestion(question.id, it, content))
                }
                editQuestion = null
            }
        )
    }

    fullscreenImageUrl?.let { url ->
        Dialog(
            onDismissRequest = { fullscreenImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                val painter = rememberImagePainter(url)
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { fullscreenImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        painter = painterResource(Resources.Icon.Close),
                        contentDescription = stringResource(Resources.String.Cancel),
                        tint = Color.White
                    )
                }
            }
        }
    }
}
