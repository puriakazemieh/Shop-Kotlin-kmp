package com.kazemieh.home.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.component.PrimaryButton
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.home.component.CartItemCard
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navigateToCheckout: (Double) -> Unit
) {
    val messageBarState = rememberMessageBarState()
    val viewModel = koinViewModel<CartViewModel>()
    val cartState by viewModel.cartState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    ContentWithMessageBar(
        contentBackgroundColor = MaterialTheme.colorScheme.surface,
        messageBarState = messageBarState,
        errorMaxLines = 2,
        errorContainerColor = MaterialTheme.colorScheme.errorContainer,
        errorContentColor = MaterialTheme.colorScheme.onErrorContainer,
        successContainerColor = MaterialTheme.colorScheme.primaryContainer,
        successContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshCart() },
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = cartState) {
                is AppResult.Loading -> {
                    LoadingCard(modifier = Modifier.fillMaxSize())
                }

            is AppResult.Success -> {
                val cart = state.data
                if (cart.items.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = cart.items,
                                key = { it.id }
                            ) { item ->
                                CartItemCard(
                                    cartItem = item,
                                    onMinusClick = {
                                        viewModel.adjustQuantity(
                                            variantId = item.variantId,
                                            delta = -1,
                                            onSuccess = {},
                                            onError = { messageBarState.addError(it) }
                                        )
                                    },
                                    onPlusClick = {
                                        viewModel.adjustQuantity(
                                            variantId = item.variantId,
                                            delta = 1,
                                            onSuccess = {},
                                            onError = { messageBarState.addError(it) }
                                        )
                                    },
                                    onDeleteClick = {
                                        viewModel.deleteCartItem(
                                            itemId = item.id,
                                            onSuccess = {},
                                            onError = { messageBarState.addError(it) }
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total:",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$${cart.subtotal}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        PrimaryButton(
                            text = "Checkout",
                            onClick = { navigateToCheckout(cart.subtotal) }
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        InfoCard(
                            image = Resources.Image.ShoppingCart,
                            title = "Empty Cart",
                            subtitle = "Check some of our products."
                        )
                    }
                }
            }

            is AppResult.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    InfoCard(
                        image = Resources.Image.Cat,
                        title = "Oops!",
                        subtitle = state.message
                    )
                }
            }
        }
    }
}
}
