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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
    navigateToCart: () -> Unit
) {
    val messageBarState = rememberMessageBarState()
    val viewModel = koinViewModel<DetailsViewModel>()
    val state by viewModel.state.collectAsState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    LaunchedEffect(state.isAddedToCart, state.quantity, state.isCounterMode) {
        if (state.isAddedToCart && state.isCounterMode) {
            kotlinx.coroutines.delay(5000)
            viewModel.onIntent(DetailsIntent.SetCounterMode(false))
        }
    }

    LaunchedEffect(slug) {
        viewModel.onIntent(DetailsIntent.LoadProduct(slug))
    }

    val productAddedToCartMessage = stringResource(Resources.String.ProductAddedToCart)

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DetailsEffect.ShowError -> messageBarState.addError(effect.message)
                is DetailsEffect.AddedToCart -> messageBarState.addSuccess(productAddedToCartMessage)
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
                            val painter =
                                rememberImagePainter(product.images.firstOrNull()?.url ?: "")
                            Image(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(size = 12.dp))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(size = 12.dp)
                                    ),
                                painter = painter,
                                contentDescription = stringResource(Resources.String.ProductImageDesc),
                                contentScale = ContentScale.Crop
                            )
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
                                    text = stringResource(Resources.String.PriceFormat, state.selectedVariant?.price ?: 0.0),
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
                                Text(
                                    text = stringResource(Resources.String.Variants),
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
                                    product.variants.forEach { variant ->
                                        VariantChip(
                                            label = variant.options.entries.joinToString(", ") { "${it.key}: ${it.value}" },
                                            isSelected = state.selectedVariant == variant,
                                            onClick = {
                                                viewModel.onIntent(
                                                    DetailsIntent.SelectVariant(
                                                        variant
                                                    )
                                                )
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
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
                                                viewModel.onIntent(
                                                    DetailsIntent.UpdateQuantity(
                                                        it
                                                    )
                                                )
                                            },
                                            onPlusClick = {
                                                viewModel.onIntent(
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
                                                    viewModel.onIntent(
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
                                    text = stringResource(Resources.String.AddToCart),
                                    enabled = !state.isLoading && (state.product?.variants?.isNotEmpty() == true),
                                    onClick = { viewModel.onIntent(DetailsIntent.AddToCart) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
