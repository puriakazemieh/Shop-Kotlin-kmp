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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.kazemieh.domain.catalog.ProductAttribute
import com.kazemieh.domain.catalog.ProductImage
import com.kazemieh.domain.catalog.ProductSummary
import com.kazemieh.domain.catalog.ProductVideo
import com.kazemieh.domain.catalog.Question
import com.kazemieh.domain.catalog.Review
import com.kazemieh.common.ComparisonStore
import com.kazemieh.designsystem.brand.BrandConfig
import com.seiko.imageloader.rememberImagePainter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

sealed interface MediaItem {
    data class Image(val image: ProductImage) : MediaItem
    data class Video(val video: ProductVideo) : MediaItem
}

/**
 * صفحه‌ی جزئیات محصول — بازطراحی‌شده مطابق اسپک کارمیلا.
 * اسکرول پیوسته (بدون تب): بردکرامب ← گالری ← برند/عنوان/امتیاز+موجودی ←
 * انتخاب ویژگی‌ها ← کارتِ قیمت و اکشن‌ها ← نشان‌های خدمات ← معرفی محصول ←
 * دیدگاه خریداران ← پرسش و پاسخ.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    slug: String,
    navigateBack: () -> Unit,
    navigateToCart: () -> Unit,
    navigateToAuth: () -> Unit,
    navigateToDetails: (String) -> Unit = {}
) {
    val messageBarState = rememberMessageBarState()
    val viewModel = koinViewModel<DetailsViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val galleryScope = rememberCoroutineScope()
    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }
    var showSizeGuide by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var showQuestionDialog by remember { mutableStateOf(false) }
    var activeParentId by remember { mutableStateOf<Long?>(null) }
    var editReview by remember { mutableStateOf<Review?>(null) }
    var editQuestion by remember { mutableStateOf<Question?>(null) }
    var newReviewRating by remember { mutableStateOf(5) }
    var newReviewText by remember { mutableStateOf("") }
    var newQuestionText by remember { mutableStateOf("") }

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

    Scaffold(containerColor = colors.background) { padding ->
        ContentWithMessageBar(
            modifier = Modifier.padding(padding),
            messageBarState = messageBarState,
            contentBackgroundColor = colors.background
        ) {
            when {
                state.isLoading -> LoadingCard(modifier = Modifier.fillMaxSize())
                state.error != null -> InfoCard(
                    image = Resources.Image.Cat,
                    title = stringResource(Resources.String.Oops),
                    subtitle = state.error!!
                )
                else -> state.product?.let { product ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp, bottom = 24.dp)
                    ) {
                        // ---- بردکرامب ----
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(Radius.sm))
                                    .background(colors.surface)
                                    .border(1.dp, colors.line, RoundedCornerShape(Radius.sm))
                                    .clickable { navigateBack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .size(19.dp)
                                        .graphicsLayer { rotationY = if (isRtl) 180f else 0f },
                                    painter = painterResource(Resources.Icon.BackArrow),
                                    contentDescription = stringResource(Resources.String.BackDesc),
                                    tint = colors.onSurface
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "خانه",
                                fontSize = FontSize.SMALL,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.clickable { navigateBack() }
                            )
                            product.categoryName?.let {
                                Text(
                                    text = "  /  ",
                                    fontSize = FontSize.SMALL,
                                    color = colors.onSurfaceVariant
                                )
                                Text(
                                    text = it,
                                    fontSize = FontSize.SMALL,
                                    color = colors.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // ---- گالری ----
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
                        val pagerState = rememberPagerState(pageCount = { mediaItems.size.coerceAtLeast(1) })
                        val variant = state.selectedVariant
                        val basePrice = variant?.price ?: product.basePrice ?: 0.0
                        val discountedPrice = variant?.discountedPrice ?: product.discountedPrice
                        val galleryDiscountPercent =
                            if (discountedPrice != null && basePrice > 0.0)
                                ((1.0 - discountedPrice / basePrice) * 100).roundToInt()
                            else 0

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp)
                                .clip(RoundedCornerShape(Radius.lg))
                                .background(colors.surfaceVariant)
                                .border(1.dp, colors.line, RoundedCornerShape(Radius.lg))
                        ) {
                            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                                when (val item = mediaItems.getOrNull(page)) {
                                    is MediaItem.Image -> {
                                        val imageUrl = item.image.url
                                        Image(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { if (imageUrl.isNotEmpty()) fullscreenImageUrl = imageUrl },
                                            painter = rememberImagePainter(imageUrl),
                                            contentDescription = stringResource(Resources.String.ProductImageDesc),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    is MediaItem.Video -> VideoPlayer(url = item.video.url, modifier = Modifier.fillMaxSize())
                                    null -> Box(Modifier.fillMaxSize().background(colors.surfaceVariant))
                                }
                            }

                            if (galleryDiscountPercent > 0) {
                                Text(
                                    text = "$galleryDiscountPercent٪ تخفیف",
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(14.dp)
                                        .clip(RoundedCornerShape(Radius.sm))
                                        .background(colors.sale)
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = Color.White,
                                    fontSize = FontSize.SMALL,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (mediaItems.size > 1) {
                                Row(
                                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    repeat(mediaItems.size) { i ->
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(if (pagerState.currentPage == i) colors.primary else colors.primary.copy(alpha = 0.4f))
                                                .size(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // ---- نوار بندانگشتی (تصاویر + ویدیو) ----
                        if (mediaItems.size > 1) {
                            Spacer(Modifier.height(12.dp))
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(mediaItems) { index, item ->
                                    MediaThumbnail(
                                        item = item,
                                        selected = pagerState.currentPage == index,
                                        onClick = { galleryScope.launch { pagerState.animateScrollToPage(index) } }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        // ---- برند ----
                        product.categoryName?.let {
                            Text(
                                text = it,
                                fontSize = FontSize.REGULAR,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        // ---- عنوان ----
                        Text(
                            text = product.title,
                            fontSize = FontSize.EXTRA_MEDIUM,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.onSurface
                        )

                        Spacer(Modifier.height(14.dp))

                        // ---- امتیاز + موجودی ----
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val ratingValues = state.reviews.mapNotNull { it.rating }
                            if (ratingValues.isNotEmpty()) {
                                val avg = (ratingValues.average() * 10).roundToInt() / 10.0
                                Icon(Icons.Default.Star, null, tint = colors.star, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(5.dp))
                                Text(avg.toString(), fontSize = FontSize.REGULAR, fontWeight = FontWeight.Bold, color = colors.onSurface)
                                Spacer(Modifier.width(5.dp))
                                Text("(${ratingValues.size} نظر)", fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
                                Spacer(Modifier.width(16.dp))
                            }
                            val available = variant?.available ?: 0
                            if (available > 0) {
                                Icon(Icons.Default.Check, null, tint = colors.ok, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    "$available عدد موجود",
                                    fontSize = FontSize.SMALL,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.ok
                                )
                            }
                        }

                        // ---- انتخاب ویژگی‌ها ----
                        if (product.variants.isNotEmpty()) {
                            val groupedOptions = remember(product.variants) {
                                val map = mutableMapOf<String, MutableSet<String>>()
                                product.variants.forEach { v ->
                                    v.options.forEach { (k, value) -> map.getOrPut(k) { mutableSetOf() }.add(value) }
                                }
                                map
                            }
                            groupedOptions.forEach { (optionName, values) ->
                                val isColor = optionName.contains("رنگ") || optionName.contains("color", ignoreCase = true)
                                val isSize = optionName.contains("سایز") || optionName.contains("size", ignoreCase = true)
                                Spacer(Modifier.height(22.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "انتخاب $optionName",
                                        fontSize = FontSize.REGULAR,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface
                                    )
                                    if (isSize) {
                                        Text(
                                            text = "راهنمای سایز",
                                            fontSize = FontSize.SMALL,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.primary,
                                            modifier = Modifier.clickable { showSizeGuide = true }
                                        )
                                    }
                                }
                                Spacer(Modifier.height(11.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                                    verticalArrangement = Arrangement.spacedBy(9.dp)
                                ) {
                                    values.forEach { value ->
                                        val enabled = product.variants.any { it.options[optionName] == value }
                                        val selected = state.selectedOptions[optionName] == value
                                        val swatch = if (isColor) colorForName(value) else null
                                        if (swatch != null) {
                                            ColorSwatch(
                                                color = swatch,
                                                isSelected = selected,
                                                enabled = enabled,
                                                onClick = { viewModel.handleIntent(DetailsIntent.SelectOption(optionName, value)) }
                                            )
                                        } else {
                                            VariantChip(
                                                label = value,
                                                isSelected = selected,
                                                enabled = enabled,
                                                onClick = { viewModel.handleIntent(DetailsIntent.SelectOption(optionName, value)) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(22.dp))

                        // ---- کارت قیمت و اکشن‌ها ----
                        PriceActionCard(
                            basePrice = basePrice,
                            discountedPrice = discountedPrice,
                            compareAtPrice = variant?.compareAtPrice,
                            inStock = (variant?.available ?: 0) > 0,
                            isAddedToCart = state.isAddedToCart,
                            quantity = state.quantity,
                            isFavorite = product.isFavorite,
                            notifyRequested = state.notifyRequested,
                            onNotifyMe = { viewModel.handleIntent(DetailsIntent.RequestBackInStock) },
                            onAddToCart = { viewModel.handleIntent(DetailsIntent.AddToCart) },
                            onBuyNow = {
                                viewModel.handleIntent(DetailsIntent.AddToCart)
                                navigateToCart()
                            },
                            onQuantityChange = { viewModel.handleIntent(DetailsIntent.UpdateQuantity(it)) },
                            onGoToCart = navigateToCart,
                            onToggleFavorite = {
                                viewModel.handleIntent(DetailsIntent.ToggleFavorite(product.id, product.isFavorite))
                            }
                        )

                        Spacer(Modifier.height(22.dp))

                        // ---- افزودن به مقایسه (فقط وقتی برند این فیچر را روشن کرده) ----
                        val brand = koinInject<BrandConfig>()
                        if (brand.features.productComparison) {
                            val comparedSlugs by ComparisonStore.slugs.collectAsState()
                            val inComparison = comparedSlugs.contains(product.slug)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (inComparison) "✓ در فهرستِ مقایسه" else "افزودن به مقایسه",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Radius.button))
                                    .border(1.dp, if (inComparison) colors.primary else colors.line, RoundedCornerShape(Radius.button))
                                    .background(if (inComparison) colors.accentSoft else colors.surface)
                                    .clickable { ComparisonStore.toggle(product.slug) }
                                    .padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                color = if (inComparison) colors.primary else colors.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = FontSize.SMALL
                            )
                        }

                        // ---- نشان‌های خدمات ----
                        Spacer(Modifier.height(12.dp))
                        ServiceBadges()

                        // ---- تخمین زمان ارسال + اطلاعات فیت مدل ----
                        Spacer(Modifier.height(12.dp))
                        ShippingAndFitInfo(city = state.defaultCity, attributes = product.attributes)

                        // ---- معرفی محصول ----
                        if (!product.description.isNullOrBlank()) {
                            Spacer(Modifier.height(24.dp))
                            Text("معرفی محصول", fontSize = FontSize.EXTRA_REGULAR, fontWeight = FontWeight.ExtraBold, color = colors.onSurface)
                            Spacer(Modifier.height(10.dp))
                            product.description!!.split("\n").map { it.trim() }.filter { it.isNotEmpty() }.forEach { line ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(Icons.Default.Check, null, tint = colors.primary, modifier = Modifier.size(17.dp).padding(top = 2.dp))
                                    Spacer(Modifier.width(9.dp))
                                    Text(line, fontSize = FontSize.REGULAR, color = colors.onSurfaceVariant, lineHeight = FontSize.EXTRA_MEDIUM)
                                }
                            }
                        }

                        // ---- مشخصات / ویژگی‌های محصول ----
                        val specs: List<Pair<String, String>> = remember(product) {
                            buildList {
                                product.brand?.takeIf { it.isNotBlank() }?.let { add("برند" to it) }
                                if (product.attributes.isNotEmpty()) {
                                    // مشخصاتِ واقعیِ ثبت‌شده توسط ادمین
                                    product.attributes.forEach { add(it.name to it.value) }
                                } else {
                                    product.categoryName?.takeIf { it.isNotBlank() }
                                        ?.let { add("دسته‌بندی" to it) }
                                    // گزینه‌های واریانت‌ها (سایز، رنگ، …) را تجمیع می‌کنیم
                                    val optionMap = linkedMapOf<String, LinkedHashSet<String>>()
                                    product.variants.forEach { v ->
                                        v.options.forEach { (k, value) ->
                                            if (value.isNotBlank()) {
                                                optionMap.getOrPut(k) { linkedSetOf() }.add(value)
                                            }
                                        }
                                    }
                                    optionMap.forEach { (k, values) ->
                                        add(k to values.joinToString("، "))
                                    }
                                    val totalAvailable = product.variants.sumOf { it.available }
                                    add("وضعیت موجودی" to if (totalAvailable > 0) "موجود در انبار" else "ناموجود")
                                }
                            }
                        }
                        if (specs.isNotEmpty()) {
                            Spacer(Modifier.height(22.dp))
                            Text("مشخصات", fontSize = FontSize.EXTRA_REGULAR, fontWeight = FontWeight.ExtraBold, color = colors.onSurface)
                            Spacer(Modifier.height(10.dp))
                            ProductSpecsCard(specs = specs)
                        }

                        // ---- هرمِ رایحه (فقط عطر: وقتی نُت‌های آغازین/میانی/پایه ثبت شده باشند) ----
                        val scent = remember(product) { extractScentPyramid(product.attributes) }
                        if (scent != null) {
                            Spacer(Modifier.height(22.dp))
                            Text("هرم رایحه", fontSize = FontSize.EXTRA_REGULAR, fontWeight = FontWeight.ExtraBold, color = colors.onSurface)
                            Spacer(Modifier.height(10.dp))
                            ScentPyramid(pyramid = scent)
                        }

                        // ---- دیدگاه خریداران ----
                        Spacer(Modifier.height(28.dp))
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "دیدگاه خریداران (${state.reviews.size})",
                            fontSize = FontSize.MEDIUM,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.onSurface
                        )
                        Spacer(Modifier.height(16.dp))

                        // ---- خلاصه‌ی امتیاز (میانگین + نمودار میله‌ای) ----
                        val ratings = state.reviews.mapNotNull { it.rating }
                        if (ratings.isNotEmpty()) {
                            ReviewSummary(ratings = ratings)
                            Spacer(Modifier.height(16.dp))
                        }

                        // ---- ثبت دیدگاهِ اینلاین ----
                        WriteReviewCard(
                            rating = newReviewRating,
                            onRatingChange = { newReviewRating = it },
                            text = newReviewText,
                            onTextChange = { newReviewText = it },
                            onSubmit = {
                                state.product?.id?.let { pid ->
                                    viewModel.handleIntent(DetailsIntent.AddReview(pid, newReviewRating, newReviewText, null))
                                    newReviewText = ""
                                    newReviewRating = 5
                                }
                            }
                        )
                        Spacer(Modifier.height(16.dp))

                        if (state.reviews.isEmpty()) {
                            Text(
                                stringResource(Resources.String.NoReviewsYet),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                textAlign = TextAlign.Center,
                                fontSize = FontSize.REGULAR,
                                color = colors.onSurfaceVariant
                            )
                        }
                        state.reviews.forEach { review ->
                            ReviewItem(
                                review = review,
                                onReplyClick = { activeParentId = it; showReviewDialog = true },
                                onEditClick = { editReview = it },
                                onDeleteClick = {
                                    state.product?.id?.let { pid -> viewModel.handleIntent(DetailsIntent.DeleteReview(it, pid)) }
                                },
                                onHelpfulClick = { viewModel.handleIntent(DetailsIntent.ToggleReviewHelpful(it)) }
                            )
                        }

                        // ---- پرسش و پاسخ ----
                        Spacer(Modifier.height(28.dp))
                        Text(
                            "پرسش و پاسخ (${state.questions.size})",
                            fontSize = FontSize.MEDIUM,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.onSurface
                        )
                        Spacer(Modifier.height(14.dp))
                        // ---- ثبت پرسشِ اینلاین (مثل بخش دیدگاه‌ها) ----
                        WriteQuestionCard(
                            text = newQuestionText,
                            onTextChange = { newQuestionText = it },
                            onSubmit = {
                                state.product?.id?.let { pid ->
                                    viewModel.handleIntent(DetailsIntent.AddQuestion(pid, newQuestionText, null))
                                    newQuestionText = ""
                                }
                            }
                        )
                        Spacer(Modifier.height(16.dp))
                        if (state.questions.isEmpty()) {
                            Text(
                                stringResource(Resources.String.NoQuestionsYet),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                textAlign = TextAlign.Center,
                                fontSize = FontSize.REGULAR,
                                color = colors.onSurfaceVariant
                            )
                        }
                        state.questions.forEach { question ->
                            QuestionItem(
                                question = question,
                                onReplyClick = { activeParentId = it; showQuestionDialog = true },
                                onEditClick = { editQuestion = it },
                                onDeleteClick = {
                                    state.product?.id?.let { pid -> viewModel.handleIntent(DetailsIntent.DeleteQuestion(it, pid)) }
                                }
                            )
                        }

                        // ---- محصولات مشابه ----
                        if (state.similarProducts.isNotEmpty()) {
                            Spacer(Modifier.height(28.dp))
                            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "محصولات مشابه",
                                fontSize = FontSize.MEDIUM,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.onSurface
                            )
                            Spacer(Modifier.height(16.dp))
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.similarProducts) { similar ->
                                    SimilarProductCard(
                                        product = similar,
                                        onClick = { navigateToDetails(similar.slug) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSizeGuide) {
        SizeGuideDialog(onDismiss = { showSizeGuide = false })
    }

    if (showReviewDialog) {
        AddReviewDialog(
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment ->
                state.product?.id?.let {
                    viewModel.handleIntent(DetailsIntent.AddReview(it, rating, comment, activeParentId))
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
                    viewModel.handleIntent(DetailsIntent.UpdateReview(review.id, it, rating, comment))
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
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = rememberImagePainter(url),
                    contentDescription = null,
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { fullscreenImageUrl = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
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

/** کارتِ قیمت و اکشن‌های خرید — مطابق اسپک کارمیلا (قیمت + شمارنده + افزودن/خرید فوری + علاقه‌مندی). */
@Composable
private fun PriceActionCard(
    basePrice: Double,
    discountedPrice: Double?,
    compareAtPrice: Double?,
    inStock: Boolean,
    isAddedToCart: Boolean,
    quantity: Int,
    isFavorite: Boolean,
    notifyRequested: Boolean,
    onNotifyMe: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onGoToCart: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val colors = AppTheme.colors
    val displayPrice = discountedPrice ?: basePrice
    val oldPrice = when {
        discountedPrice != null -> basePrice
        compareAtPrice != null && compareAtPrice > basePrice -> compareAtPrice
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                if (oldPrice != null) {
                    Text(
                        text = stringResource(Resources.String.PriceFormat, oldPrice),
                        fontSize = FontSize.SMALL,
                        color = colors.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Spacer(Modifier.height(3.dp))
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = stringResource(Resources.String.PriceFormat, displayPrice),
                        fontSize = FontSize.LARGE,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onSurface
                    )
                }
            }
            if (inStock && isAddedToCart) {
                QuantityCounter(
                    size = QuantityCounterSize.Medium,
                    value = quantity,
                    onMinusClick = onQuantityChange,
                    onPlusClick = onQuantityChange
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (!inStock) {
            Text(
                text = stringResource(Resources.String.OutOfStock),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.button))
                    .background(colors.surfaceVariant)
                    .padding(vertical = 15.dp),
                textAlign = TextAlign.Center,
                color = colors.sale,
                fontSize = FontSize.REGULAR,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(11.dp))
            // «موجود شد خبرم کن» — اشتراکِ اطلاع‌رسانیِ موجودی
            if (notifyRequested) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.button))
                        .background(colors.accentSoft)
                        .padding(vertical = 15.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, null, tint = colors.ok, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "به‌محضِ موجود شدن خبرتان می‌کنیم",
                        color = colors.primary,
                        fontSize = FontSize.REGULAR,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "موجود شد خبرم کن",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.button))
                        .background(colors.primary)
                        .clickable { onNotifyMe() }
                        .padding(vertical = 15.dp),
                    textAlign = TextAlign.Center,
                    color = colors.onPrimary,
                    fontSize = FontSize.REGULAR,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                // افزودن به سبد / مشاهده سبد
                Text(
                    text = if (isAddedToCart) stringResource(Resources.String.Cart) else stringResource(Resources.String.AddToCart),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Radius.button))
                        .background(colors.accentSoft)
                        .clickable { if (isAddedToCart) onGoToCart() else onAddToCart() }
                        .padding(vertical = 15.dp),
                    textAlign = TextAlign.Center,
                    color = colors.primary,
                    fontSize = FontSize.REGULAR,
                    fontWeight = FontWeight.Bold
                )
                // خرید فوری
                Text(
                    text = "خرید فوری",
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Radius.button))
                        .background(colors.primary)
                        .clickable { onBuyNow() }
                        .padding(vertical = 15.dp),
                    textAlign = TextAlign.Center,
                    color = colors.onPrimary,
                    fontSize = FontSize.REGULAR,
                    fontWeight = FontWeight.Bold
                )
                // علاقه‌مندی
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(Radius.button))
                        .border(1.dp, colors.line, RoundedCornerShape(Radius.button))
                        .clickable { onToggleFavorite() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) colors.sale else colors.onSurfaceVariant,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }
    }
}

/** خلاصه‌ی امتیازِ نظرات — میانگینِ درشت + نمودار میله‌ایِ ۵→۱ ستاره (مطابق اسپک). */
@Composable
private fun ReviewSummary(ratings: List<Int>) {
    val colors = AppTheme.colors
    val avg = (ratings.average() * 10).roundToInt() / 10.0
    val total = ratings.size
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(avg.toString(), fontSize = FontSize.EXTRA_LARGE, fontWeight = FontWeight.ExtraBold, color = colors.onSurface)
            Row {
                repeat(5) { i ->
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i < avg.roundToInt()) colors.star else colors.line,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(Modifier.height(5.dp))
            Text("$total دیدگاه", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
        }
        Spacer(Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (star in 5 downTo 1) {
                val count = ratings.count { it == star }
                val fraction = if (total > 0) count.toFloat() / total else 0f
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$star", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant, modifier = Modifier.width(10.dp))
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.Star, contentDescription = null, tint = colors.star, modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(Radius.full))
                            .background(colors.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(6.dp)
                                .clip(RoundedCornerShape(Radius.full))
                                .background(colors.star)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("$count", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant, modifier = Modifier.width(22.dp))
                }
            }
        }
    }
}

/** کارتِ ثبتِ دیدگاهِ اینلاین — انتخاب ستاره + متن + دکمه (مطابق اسپک). */
@Composable
private fun WriteReviewCard(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
            .padding(16.dp)
    ) {
        Text("دیدگاه خود را بنویسید", fontSize = FontSize.REGULAR, fontWeight = FontWeight.Bold, color = colors.onSurface)
        Spacer(Modifier.height(11.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(5) { i ->
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = if (i < rating) colors.star else colors.line,
                    modifier = Modifier.size(26.dp).clickable { onRatingChange(i + 1) }
                )
            }
        }
        Spacer(Modifier.height(11.dp))
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("تجربه‌تان از این محصول را بنویسید…", fontSize = FontSize.SMALL, color = colors.onSurfaceVariant) },
            minLines = 2,
            shape = RoundedCornerShape(Radius.sm),
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
        Spacer(Modifier.height(12.dp))
        Text(
            text = "ثبت دیدگاه",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.button))
                .background(if (text.isNotBlank()) colors.primary else colors.line)
                .clickable(enabled = text.isNotBlank()) { onSubmit() }
                .padding(vertical = 13.dp),
            textAlign = TextAlign.Center,
            color = colors.onPrimary,
            fontSize = FontSize.REGULAR,
            fontWeight = FontWeight.Bold
        )
    }
}

/** کارتِ مشخصاتِ محصول — ردیف‌های کلید/مقدار با خط جداکننده (مطابق اسپک). */
@Composable
private fun ProductSpecsCard(specs: List<Pair<String, String>>) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
    ) {
        specs.forEachIndexed { index, (key, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(key, fontSize = FontSize.REGULAR, color = colors.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
                Text(
                    value,
                    fontSize = FontSize.REGULAR,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    textAlign = TextAlign.End
                )
            }
            if (index < specs.lastIndex) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
            }
        }
    }
}

/** سه لایه‌ی هرمِ رایحه‌ی یک عطر (نُت‌های آغازین/میانی/پایه). */
private data class ScentPyramidData(
    val top: List<String>,
    val heart: List<String>,
    val base: List<String>
)

/**
 * از میانِ attributesِ جنریکِ محصول، نُت‌های عطر را استخراج می‌کند.
 * کلیدها به‌صورتِ منعطف تطبیق داده می‌شوند (فارسی یا انگلیسیِ HANDOFF عطر:
 * notesTop/notesHeart/notesBase یا «نُت آغازین/میانی/پایه»). اگر هیچ‌کدام نبود null.
 */
private fun extractScentPyramid(attributes: List<ProductAttribute>): ScentPyramidData? {
    fun find(vararg keys: String): List<String> {
        val attr = attributes.firstOrNull { a ->
            keys.any { k -> a.name.replace(" ", "").equals(k.replace(" ", ""), ignoreCase = true) }
        } ?: return emptyList()
        return attr.value.split("،", ",", "/").map { it.trim() }.filter { it.isNotEmpty() }
    }
    val top = find("notesTop", "نُت آغازین", "نت آغازین", "نت‌های آغازین", "نُت‌های آغازین")
    val heart = find("notesHeart", "نُت میانی", "نت میانی", "نت‌های میانی", "نُت‌های میانی")
    val base = find("notesBase", "نُت پایه", "نت پایه", "نت‌های پایه", "نُت‌های پایه")
    return if (top.isEmpty() && heart.isEmpty() && base.isEmpty()) null
    else ScentPyramidData(top, heart, base)
}

/** رندرِ گرافیکیِ هرمِ رایحه در سه ردیف با چیپ‌های نُت. */
@Composable
private fun ScentPyramid(pyramid: ScentPyramidData) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
            .padding(vertical = 4.dp)
    ) {
        ScentLayer(title = "نُت‌های آغازین", notes = pyramid.top, accent = colors.primary)
        if (pyramid.heart.isNotEmpty() || pyramid.base.isNotEmpty()) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
        }
        ScentLayer(title = "نُت‌های میانی", notes = pyramid.heart, accent = colors.gold)
        if (pyramid.base.isNotEmpty()) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
        }
        ScentLayer(title = "نُت‌های پایه", notes = pyramid.base, accent = colors.onSurfaceVariant)
    }
}

@Composable
private fun ScentLayer(title: String, notes: List<String>, accent: Color) {
    if (notes.isEmpty()) return
    val colors = AppTheme.colors
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(8.dp))
            Text(title, fontSize = FontSize.REGULAR, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
        }
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            notes.forEach { note ->
                Text(
                    note,
                    fontSize = FontSize.SMALL,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(colors.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/** کارتِ ثبتِ پرسشِ اینلاین — متن + دکمه (هم‌سبک با بخش دیدگاه‌ها، به‌جای دیالوگ). */
@Composable
private fun WriteQuestionCard(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
            .padding(16.dp)
    ) {
        Text("پرسش خود را بنویسید", fontSize = FontSize.REGULAR, fontWeight = FontWeight.Bold, color = colors.onSurface)
        Spacer(Modifier.height(11.dp))
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("پرسش خود درباره این محصول را بنویسید…", fontSize = FontSize.SMALL, color = colors.onSurfaceVariant) },
            minLines = 2,
            shape = RoundedCornerShape(Radius.sm),
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
        Spacer(Modifier.height(12.dp))
        Text(
            text = "ثبت پرسش",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.button))
                .background(if (text.isNotBlank()) colors.primary else colors.line)
                .clickable(enabled = text.isNotBlank()) { onSubmit() }
                .padding(vertical = 13.dp),
            textAlign = TextAlign.Center,
            color = colors.onPrimary,
            fontSize = FontSize.REGULAR,
            fontWeight = FontWeight.Bold
        )
    }
}

/** سه نشانِ خدماتِ محصول — استاتیک، مطابق اسپک کارمیلا. */
@Composable
private fun ServiceBadges(modifier: Modifier = Modifier) {
    val items = listOf(
        Icons.Default.VerifiedUser to "ضمانت اصالت",
        Icons.Default.Autorenew to "۷ روز بازگشت",
        Icons.Default.LocalShipping to "ارسال سریع"
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { (icon, label) ->
            ServiceBadgeTile(icon = icon, label = label, modifier = Modifier.weight(1f))
        }
    }
}

/**
 * تخمینِ زمانِ ارسال (بر اساسِ شهرِ آدرسِ پیش‌فرض) + اطلاعاتِ فیتِ مدل (از attributes).
 * کلاینت‌ساید است؛ برای وایت‌لیبل، فیت به‌صورتِ جنریک از attributes خوانده می‌شود.
 */
@Composable
private fun ShippingAndFitInfo(
    city: String?,
    attributes: List<ProductAttribute>,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val estimate = when {
        city == null -> "۲ تا ۴ روز کاری"
        city.contains("تهران") -> "۱ تا ۲ روز کاری"
        else -> "۲ تا ۴ روز کاری"
    }
    val fit = attributes.firstOrNull {
        it.name.contains("فیت") || it.name.contains("قد مدل") ||
            it.name.contains("fit", ignoreCase = true) || it.name.contains("سایز مدل")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocalShipping, null, tint = colors.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(9.dp))
            Text("تخمین زمان ارسال", fontSize = FontSize.REGULAR, fontWeight = FontWeight.Bold, color = colors.onSurface)
            Spacer(Modifier.weight(1f))
            Text(
                text = estimate + if (city != null) " • $city" else "",
                fontSize = FontSize.SMALL,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurfaceVariant
            )
        }
        if (fit != null) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📏", fontSize = FontSize.REGULAR)
                Spacer(Modifier.width(9.dp))
                Text("اطلاعات فیت مدل", fontSize = FontSize.REGULAR, fontWeight = FontWeight.Bold, color = colors.onSurface)
                Spacer(Modifier.weight(1f))
                Text(
                    text = fit.value,
                    fontSize = FontSize.SMALL,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

/** بندانگشتیِ گالری — تصویر یا ویدیو (با برچسب «ویدیو»)، با قابِ فعال هنگام انتخاب. */
@Composable
private fun MediaThumbnail(
    item: MediaItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .size(66.dp)
            .clip(RoundedCornerShape(Radius.sm))
            .background(colors.surfaceVariant)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colors.primary else colors.line,
                shape = RoundedCornerShape(Radius.sm)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (item) {
            is MediaItem.Image -> Image(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(Radius.sm)),
                painter = rememberImagePainter(item.image.url),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            is MediaItem.Video -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp).align(Alignment.Center)
                    )
                    Text(
                        text = "ویدیو",
                        fontSize = FontSize.EXTRA_SMALL,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color(0xCC0B0E18))
                            .padding(vertical = 2.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/** دایره‌ی انتخاب رنگ — مطابق اسپک؛ هنگام انتخاب حلقه‌ی دور آن پررنگ می‌شود. */
@Composable
private fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.line,
                shape = CircleShape
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(if (enabled) color else color.copy(alpha = 0.35f))
                .border(1.dp, colors.line, CircleShape)
        )
    }
}

/** نگاشتِ نامِ فارسیِ رنگ به کد رنگ؛ برای رنگ‌های ناشناخته null برمی‌گرداند تا چیپِ متنی نمایش داده شود. */
private fun colorForName(name: String): Color? {
    val n = name.trim()
    return when {
        n.contains("مشکی") || n.contains("سیاه") -> Color(0xFF1A1A1A)
        n.contains("سفید") -> Color(0xFFFFFFFF)
        n.contains("سرمه") || n.contains("نیوی") -> Color(0xFF1F2A44)
        n.contains("آبی") -> Color(0xFF2563EB)
        n.contains("قرمز") || n.contains("قرمزی") -> Color(0xFFDC2626)
        n.contains("سبز") -> Color(0xFF16A34A)
        n.contains("زرد") -> Color(0xFFEAB308)
        n.contains("نارنجی") -> Color(0xFFEA580C)
        n.contains("صورتی") -> Color(0xFFEC4899)
        n.contains("بنفش") -> Color(0xFF7C3AED)
        n.contains("قهوه") -> Color(0xFF6B4226)
        n.contains("کرم") -> Color(0xFFD9C7A3)
        n.contains("بژ") -> Color(0xFFE8D9C0)
        n.contains("طلای") -> Color(0xFFC9A227)
        n.contains("نقره") -> Color(0xFFC0C0C0)
        n.contains("خاکستری") || n.contains("طوسی") -> Color(0xFF9CA3AF)
        else -> null
    }
}

/** کارتِ محصولِ مشابه — کارتِ عمودیِ کوچک برای ردیفِ افقی (تصویر + عنوان + قیمت). */
@Composable
private fun SimilarProductCard(
    product: ProductSummary,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    val price = product.minDiscountedPrice ?: product.minPrice
    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(Radius.sm)),
            painter = rememberImagePainter(product.thumbnailUrl ?: ""),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = product.title,
            fontSize = FontSize.SMALL,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.height(38.dp)
        )
        Spacer(Modifier.height(6.dp))
        if (price != null) {
            Text(
                text = stringResource(Resources.String.PriceFormat, price),
                fontSize = FontSize.REGULAR,
                fontWeight = FontWeight.ExtraBold,
                color = colors.onSurface
            )
        }
    }
}

/** دیالوگِ راهنمای سایز — جدولِ استانداردِ اندازه‌ها (مطابق اسپک). */
@Composable
private fun SizeGuideDialog(onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    val rows = listOf(
        Triple("S", "88-92", "72-76"),
        Triple("M", "92-96", "76-80"),
        Triple("L", "96-100", "80-84"),
        Triple("XL", "100-104", "84-88"),
        Triple("XXL", "104-108", "88-92")
    )
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.lg))
                .background(colors.surface)
                .border(1.dp, colors.line, RoundedCornerShape(Radius.lg))
                .padding(20.dp)
        ) {
            Text("راهنمای سایز", fontSize = FontSize.EXTRA_REGULAR, fontWeight = FontWeight.ExtraBold, color = colors.onSurface)
            Spacer(Modifier.height(6.dp))
            Text("اندازه‌ها بر حسب سانتی‌متر", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("سایز", fontSize = FontSize.SMALL, fontWeight = FontWeight.Bold, color = colors.onSurface, modifier = Modifier.weight(1f))
                Text("دور سینه", fontSize = FontSize.SMALL, fontWeight = FontWeight.Bold, color = colors.onSurface, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("دور کمر", fontSize = FontSize.SMALL, fontWeight = FontWeight.Bold, color = colors.onSurface, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
            Spacer(Modifier.height(8.dp))
            rows.forEach { (size, chest, waist) ->
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(size, fontSize = FontSize.SMALL, fontWeight = FontWeight.SemiBold, color = colors.onSurface, modifier = Modifier.weight(1f))
                    Text(chest, fontSize = FontSize.SMALL, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text(waist, fontSize = FontSize.SMALL, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(Resources.String.Cancel),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.button))
                    .background(colors.primary)
                    .clickable { onDismiss() }
                    .padding(vertical = 13.dp),
                textAlign = TextAlign.Center,
                color = colors.onPrimary,
                fontSize = FontSize.REGULAR,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ServiceBadgeTile(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.sm))
            .border(1.dp, colors.line, RoundedCornerShape(Radius.sm))
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = FontSize.EXTRA_SMALL,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
