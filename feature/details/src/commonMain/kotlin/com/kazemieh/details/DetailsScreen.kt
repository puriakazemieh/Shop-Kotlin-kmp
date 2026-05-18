package com.kazemieh.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.kazemieh.details.DetailsEffect
import com.kazemieh.details.DetailsIntent
import com.kazemieh.details.DetailsViewModel
import com.kazemieh.details.component.VariantChip
import com.seiko.imageloader.rememberImagePainter
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    slug: String,
    navigateBack: () -> Unit
) {
    val messageBarState = rememberMessageBarState()
    val viewModel = koinViewModel<DetailsViewModel>()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(slug) {
        viewModel.onIntent(DetailsIntent.LoadProduct(slug))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DetailsEffect.ShowError -> messageBarState.addError(effect.message)
                is DetailsEffect.AddedToCart -> messageBarState.addSuccess("Product added to cart.")
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Details",
                        fontSize = FontSize.LARGE,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    QuantityCounter(
                        size = QuantityCounterSize.Medium,
                        value = state.quantity,
                        onMinusClick = { viewModel.onIntent(DetailsIntent.UpdateQuantity(it)) },
                        onPlusClick = { viewModel.onIntent(DetailsIntent.UpdateQuantity(it)) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
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
                    title = "Oops!",
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
                            val painter = rememberImagePainter(product.images.firstOrNull()?.url ?: "")
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
                                contentDescription = "Product image",
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
                                    text = "$${state.selectedVariant?.price ?: 0.0}",
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
                                    text = "Variants",
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
                                            label = "${variant.sizeName} / ${variant.colorName}",
                                            isSelected = state.selectedVariant == variant,
                                            onClick = { viewModel.onIntent(DetailsIntent.SelectVariant(variant)) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            PrimaryButton(
                                text = "Add to Cart",
                                enabled = state.selectedVariant != null,
                                onClick = { viewModel.onIntent(DetailsIntent.AddToCart) }
                            )
                        }
                    }
                }
            }
        }
    }
}
