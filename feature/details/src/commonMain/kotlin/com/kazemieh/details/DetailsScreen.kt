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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.component.PrimaryButton
import com.kazemieh.designsystem.component.QuantityCounter
import com.kazemieh.designsystem.component.QuantityCounterSize
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.details.component.VariantChip
import com.seiko.imageloader.rememberImagePainter
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
                            modifier = Modifier.graphicsLayer { rotationY = if (isRtl) 180f else 0f },
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = stringResource(Resources.String.BackDesc),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                            val pagerState = rememberPagerState(pageCount = { product.images.size.coerceAtLeast(1) })
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(size = 12.dp))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(size = 12.dp)
                                    )
                            ) {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    val imageUrl = product.images.getOrNull(page)?.url ?: ""
                                    val painter = rememberImagePainter(imageUrl)
                                    Image(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {
                                                if (imageUrl.isNotEmpty()) fullscreenImageUrl = imageUrl
                                            },
                                        painter = painter,
                                        contentDescription = stringResource(Resources.String.ProductImageDesc),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                if (product.images.size > 1) {
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        repeat(product.images.size) { iteration ->
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
                                Text(
                                    text = stringResource(Resources.String.PriceFormat, (state.selectedVariant?.price ?: 0.0)),
                                    fontSize = FontSize.MEDIUM,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
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
                        }
                        Column(
                            modifier = Modifier
                                .padding(all = 24.dp)
                        ) {
                            if (product.variants.isNotEmpty()) {
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
                                            val isSelected = state.selectedOptions[optionName] == value
                                            // Check if this option value is available for the current other selections
                                            val isAvailable = product.variants.any { variant ->
                                                variant.options[optionName] == value && 
                                                state.selectedOptions.filterKeys { it != optionName }.all { (k, v) ->
                                                    variant.options[k] == v
                                                }
                                            }
                                            
                                            VariantChip(
                                                label = value,
                                                isSelected = isSelected,
                                                enabled = isAvailable,
                                                onClick = {
                                                    viewModel.handleIntent(
                                                        DetailsIntent.SelectOption(optionName, value)
                                                    )
                                                }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
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
                                            text = stringResource(Resources.String.CheckoutWithQty, state.quantity),
                                            onClick = navigateToCart
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(size = 12.dp))
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
